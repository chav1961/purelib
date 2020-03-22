package chav1961.purelib.nanoservice;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import javax.sql.DataSource;

import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.fsys.FileSystemFactory;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.nanoservice.interfaces.NanoService;
import chav1961.purelib.nanoservice.interfaces.RootPath;

public class NanoServiceManager implements Closeable {
	public static final String 		NANOSERVICE_DEPLOYMENT_DIR = "nanoserviceDeploymentDir";
	public static final String 		NANOSERVICE_DEPLOYMENT_CLASSPREFIX = "nanoserviceDeploymentClassPrefix";
	public static final String 		NANOSERVICE_DEPLOYMENT_PERIOD = "nanoserviceDeploymentPeriod";
	
	enum DeploymentMode {
		deploy, redeploy, undeploy
	}
	
	private final LoggerFacade						facade;
	private final DataSource 						dataSource;
	private final NanoService						factory;
	private final Timer								t = new Timer(true);
	private final FileSystemInterface 				deploymentRoot;
	private final String							deplaymentClassPrefix;
	private final long 								deploymentPeriod;
	private final Map<String,DeploymentDesc>		deployed = new ConcurrentHashMap<>();
	private URLClassLoader							currentLoader = new AdvancedURLClassLoader(this.getClass().getClassLoader());
	
	public NanoServiceManager(final LoggerFacade facade, final SubstitutableProperties props, final NanoService factory) throws NullPointerException, IOException, ContentException, SyntaxException {
		this(facade,props,factory,null);
	}
	
	public NanoServiceManager(final LoggerFacade facade, final SubstitutableProperties props, final NanoService factory, final DataSource dataSource) throws NullPointerException, IOException, ContentException, SyntaxException {
		if (facade == null) {
			throw new NullPointerException("Logger facade can't be null"); 
		}
		else if (props == null) {
			throw new NullPointerException("Service properties can't be null"); 
		}
		else if (factory == null) {
			throw new NullPointerException("Nano service factory can't be null"); 
		}
		else {
			this.facade = facade;
			this.dataSource = dataSource;
			this.factory = factory;

			try(final LoggerFacade	check = facade.transaction("Microservice init")) {
				boolean wereErrors = false;

				if (!props.containsKey(NANOSERVICE_DEPLOYMENT_DIR)) {
					wereErrors = true;
					check.message(Severity.error, "Mandatory parameter [%1$s] is missing in the configuration",NANOSERVICE_DEPLOYMENT_DIR);
					deploymentRoot = null;
				}
				else {
					deploymentRoot = FileSystemFactory.createFileSystem(props.getProperty(NANOSERVICE_DEPLOYMENT_DIR,URI.class));
				}
				deplaymentClassPrefix = props.getProperty(NANOSERVICE_DEPLOYMENT_CLASSPREFIX,String.class,"");
				deploymentPeriod = props.getProperty(NANOSERVICE_DEPLOYMENT_PERIOD,long.class,"0");
				
				if (!wereErrors) {
					redeploy();

					if (deploymentPeriod > 0) {
						final TimerTask tt = new TimerTask() {
							@Override
							public void run() {
								try{redeploy();
								} catch (IOException | ContentException e) {
									getLogger().message(Severity.error,e,"Error redeploying plugins");
								}
							}
						};
						t.schedule(tt,deploymentPeriod,deploymentPeriod);
					}
					check.rollback();
				}
				else {
					throw new IllegalArgumentException("Error initializing manager (see log for details)"); 
				}
			}
		}		
	}

	@Override
	public void close() throws IOException {
		t.purge();
		t.cancel();
	}
	
	public LoggerFacade getLogger() {
		return facade;
	}

	protected int redeploy() throws IOException, ContentException, SyntaxException {
		final Map<String,DeploymentDesc> newContent = new HashMap<>();
		int delta = 0;
		
		parseDeploymentDirectory(deploymentRoot,newContent);
		
		final Map<String,DeploymentMode> changedContent = compareDeploymentDirectory(deployed,newContent);
		
		if (!changedContent.isEmpty()) {
			try(final LoggerFacade	lf = facade.transaction("redeploy")) {
				final AdvancedURLClassLoader		newLoader = new AdvancedURLClassLoader(this.getClass().getClassLoader());
				final Map<String,DeploymentDesc>	forDeploy = new HashMap<>();

				for (Entry<String, DeploymentMode> item : changedContent.entrySet()) {
					switch (item.getValue()) {
						case redeploy : case deploy : 
							try(final FileSystemInterface 	fsi = deploymentRoot.clone().open(item.getKey())) {
								forDeploy.put(item.getKey(),buildDeploymentDesc(fsi,newLoader,deplaymentClassPrefix));
							}
							break;
						case undeploy 	:
							break;
						default 		:
							throw new UnsupportedOperationException("Deployment mode ["+item.getValue()+"] is not supported yet");
					}
				}

				for (Entry<String, DeploymentMode> item : changedContent.entrySet()) {
					switch (item.getValue()) {
						case undeploy 	:
							for (Entry<String,Object> entity : deployed.get(item.getKey()).classes.entrySet()) {
								factory.undeploy(entity.getKey());
								delta--;
							}
							break;
						case redeploy	:
							for (Entry<String,Object> entity : deployed.get(item.getKey()).classes.entrySet()) {
								factory.undeploy(entity.getKey());
								delta--;
							}
						case deploy 	:
							for (Entry<String,DeploymentDesc> unit : forDeploy.entrySet()) {
								for (Entry<String,Object> entity : unit.getValue().classes.entrySet()) {
									factory.deploy(entity.getKey(),entity.getValue());
									delta++;
								}
							}
							break;
						default 		:
							throw new UnsupportedOperationException("Deployment mode ["+item.getValue()+"] is not supported yet");
					}
				}
				
				deployed.clear();
				deployed.putAll(forDeploy);
				lf.rollback();
			}
		}
		return delta;
	}

	static void parseDeploymentDirectory(final FileSystemInterface node, final Map<String,DeploymentDesc> content) throws IOException {
		for (String item : node.list()) {
			try(final FileSystemInterface	fsi = node.clone().open(item)) {
				if (item.endsWith(".jar") || item.endsWith(".class")) {
					content.put(fsi.getPath(),new DeploymentDesc(item.endsWith(".jar") ? DeploymentDesc.UNIT_JAR : DeploymentDesc.UNIT_CLASS,fsi.getPath(),fsi.lastModified()));
				}
				else if (fsi.isDirectory()) {
					parseDeploymentDirectory(fsi,content);
				}
			}
		}
	}

	static Map<String,DeploymentMode> compareDeploymentDirectory(final Map<String,DeploymentDesc> oldContent, final Map<String,DeploymentDesc> newContent) throws IOException {
		final Map<String,DeploymentMode>	result = new HashMap<>();
		
		for (Entry<String, DeploymentDesc> item : newContent.entrySet()) {
			if (!oldContent.containsKey(item.getKey())) {
				result.put(item.getKey(),DeploymentMode.deploy);
			}
			else if (oldContent.get(item.getKey()).timestamp < item.getValue().timestamp) {
				result.put(item.getKey(),DeploymentMode.redeploy);
			}
		}
		for (Entry<String, DeploymentDesc> item : oldContent.entrySet()) {
			if (!newContent.containsKey(item.getKey())) {
				result.put(item.getKey(),DeploymentMode.undeploy);
			}
		}
		return result;
	}

	static DeploymentDesc buildDeploymentDesc(final FileSystemInterface source, final AdvancedURLClassLoader loader, final String classPrefix) throws MalformedURLException, IOException, ContentException {
		final DeploymentDesc	dd;
		
		if (source.getName().endsWith(".jar")) {
			dd = new DeploymentDesc(DeploymentDesc.UNIT_JAR,source.getPath(),source.lastModified());
					
			try(final InputStream		is = source.read();
				final JarInputStream	jis = new JarInputStream(is)) {
				JarEntry				je;
				
				while ((je = jis.getNextJarEntry()) != null) {
					if (je.getName().endsWith(".class")) {
						Class<?>	cl = loadFrom(toClassName(je.getName()),loader,jis);
						if (cl.isAnnotationPresent(RootPath.class)) {
							try{dd.classes.put(cl.getAnnotation(RootPath.class).value(),cl.newInstance());					
							} catch (InstantiationException | IllegalAccessException e) {
								throw new ContentException();
							}
						}
					}
				}
			}
			loader.addURL(source.toURI().toURL());
		}
		else if (source.getName().endsWith(".class")) {
			dd = new DeploymentDesc(DeploymentDesc.UNIT_CLASS,source.getPath(),source.lastModified());
			
			try(final InputStream		is = source.read()) {
				
				Class<?>	cl = loadFrom(classPrefix+toClassName(source.getPath()),loader,is);
				if (cl.isAnnotationPresent(RootPath.class)) {
					try{dd.classes.put(cl.getAnnotation(RootPath.class).value(),cl.newInstance());					
					} catch (InstantiationException | IllegalAccessException e) {
						throw new ContentException();
					}
				}
			}
			loader.addURL(source.clone().open("../").toURI().toURL());
		}
		else {
			throw new IllegalArgumentException("Source path ["+source.getPath()+"] is neither .jar nor .class");
		}
		
		return dd;
	}
	
	private static Class<?> loadFrom(final String className, final AdvancedURLClassLoader loader, final InputStream is) throws IOException {
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
			
			Utils.copyStream(is,baos);
			return loader.defineClassInternal(className,baos.toByteArray());
		}
	}

	private static String toClassName(final String name) {
		final String	result = name.replace('/','.').replace(".class","");
		
		if (name.startsWith("/")) {
			return result.substring(1);
		}
		else {
			return result;
		}
	}
	
	static class AdvancedURLClassLoader extends URLClassLoader {
		AdvancedURLClassLoader(ClassLoader parent) {
			super(new URL[0], parent);
		}

		@Override
		protected void addURL(final URL url) {
			super.addURL(url);
		}

		private Class<?> defineClassInternal(final String className, final byte[] content) {
			return defineClass(className,content,0,content.length);
		}
	}
	
	static class DeploymentDesc {
		private static final int	UNIT_JAR = 0;
		private static final int	UNIT_CLASS = 1;
		
		final int					unitType;
		final String				path;
		final long					timestamp;
		final Map<String,Object>	classes = new HashMap<>(); 
		
		private DeploymentDesc(int unitType, String path, long timestamp) {
			this.unitType = unitType;
			this.path = path;
			this.timestamp = timestamp;
		}

		@Override
		public String toString() {
			return "DeploymentDesc [unitType=" + unitType + ", path=" + path + ", timestamp=" + timestamp + ", classes=" + classes.entrySet() + "]";
		}
	}
}
