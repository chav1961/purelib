package chav1961.purelib.nanoservice;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.SystemErrLoggerFacade;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.fsys.FileSystemOnFile;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.nanoservice.NanoServiceManager;
import chav1961.purelib.nanoservice.NanoServiceManager.AdvancedURLClassLoader;
import chav1961.purelib.nanoservice.NanoServiceManager.DeploymentDesc;
import chav1961.purelib.nanoservice.NanoServiceManager.DeploymentMode;
import chav1961.purelib.nanoservice.interfaces.NanoService;
import chav1961.purelib.nanoservice.internal.PseudoPlugin2;

@Tag("OrdinalTestCategory")
public class NanoServiceManagerTest {
	final File	rootDir = new File("./src/test/resources/chav1961/purelib/nanoservice/deploy/");
	
	@Before
	public void prepare() {
		rootDir.mkdirs();
	}

	@After
	public void unprepare() {
		Utils.deleteDir(rootDir);
	}

//	@Test
	public void staticsTest() throws IOException, ContentException, SyntaxException, InterruptedException {
		final Map<String,NanoServiceManager.DeploymentDesc>	content = new HashMap<>(), newContent = new HashMap<>(); 
		
		try(final FileSystemInterface	fsi = new FileSystemOnFile(rootDir.toURI())) {
			content.clear();
			NanoServiceManager.parseDeploymentDirectory(fsi,content);
			Assert.assertEquals(0,content.size());
			
			content.clear();
			putClass("PseudoPlugin.class");
			NanoServiceManager.parseDeploymentDirectory(fsi,content);
			Assert.assertEquals(1,content.size());

			content.clear();
			putClass("internal/PseudoPlugin2.class");
			NanoServiceManager.parseDeploymentDirectory(fsi,content);
			Assert.assertEquals(2,content.size());
			
			content.clear();
			new File(rootDir,"PseudoPlugin.class").delete();
			NanoServiceManager.parseDeploymentDirectory(fsi,content);
			Assert.assertEquals(1,content.size());

			content.clear();
			new File(rootDir,"internal/PseudoPlugin2.class").delete();
			NanoServiceManager.parseDeploymentDirectory(fsi,content);
			Assert.assertEquals(0,content.size());
		}

		try(final FileSystemInterface	fsi = new FileSystemOnFile(rootDir.toURI())) {
			content.clear();
			newContent.clear();
			
			NanoServiceManager.parseDeploymentDirectory(fsi,content);
			putClass("PseudoPlugin.class");
			NanoServiceManager.parseDeploymentDirectory(fsi,newContent);
			
			Map<String, DeploymentMode> delta = NanoServiceManager.compareDeploymentDirectory(content, newContent);
			Assert.assertEquals(1,delta.size());
			Assert.assertEquals(DeploymentMode.deploy,delta.get("/PseudoPlugin.class"));
			
			delta = NanoServiceManager.compareDeploymentDirectory(newContent, content);
			Assert.assertEquals(1,delta.size());
			Assert.assertEquals(DeploymentMode.undeploy,delta.get("/PseudoPlugin.class"));

			Thread.sleep(100);
			content.clear();
			content.putAll(newContent);
			newContent.clear();
			putClass("PseudoPlugin.class");
			NanoServiceManager.parseDeploymentDirectory(fsi,newContent);

			delta = NanoServiceManager.compareDeploymentDirectory(content, newContent);
			Assert.assertEquals(1,delta.size());
			Assert.assertEquals(DeploymentMode.redeploy,delta.get("/PseudoPlugin.class"));
		}
		
		try(final FileSystemInterface		fsi = new FileSystemOnFile(rootDir.toURI())) {
			final AdvancedURLClassLoader	loader = new AdvancedURLClassLoader(this.getClass().getClassLoader());
			
			try (final FileSystemInterface	jar = fsi.clone().open("content.jar").create()) {
				try(final OutputStream		os = jar.write();
					final JarOutputStream	jos = new JarOutputStream(os)) {
					JarEntry				je;
					
					je = new JarEntry(PseudoPlugin2.class.getName().replace('.','/').concat(".class"));
					je.setMethod(JarEntry.DEFLATED);
					jos.putNextEntry(je);
					
					try(final InputStream	is = this.getClass().getResourceAsStream("./internal/PseudoPlugin2.class")) {
						Utils.copyStream(is,jos);
					}
					jos.flush();
				}
				
				final DeploymentDesc desc = NanoServiceManager.buildDeploymentDesc(jar,loader,"");
				
				Assert.assertEquals(jar.getPath(),desc.path);
				Assert.assertEquals(1,desc.classes.size());
				
				jar.delete();
				
				putClass(rootDir.getParentFile(),"PseudoPlugin.class");
				try(@SuppressWarnings("resource")
				final FileSystemInterface	cl = new FileSystemOnFile(rootDir.toURI()).open("/PseudoPlugin.class")) {
					final DeploymentDesc desc2 = NanoServiceManager.buildDeploymentDesc(cl,loader,"chav1961.purelib.nanoservice.");
					
					Assert.assertEquals(cl.getPath(),desc2.path);
					Assert.assertEquals(1,desc2.classes.size());
				}
			}
		}
	}	
	
	@Test
	public void basicTest() throws IOException, ContentException, SyntaxException, InterruptedException {
		final Set<String>			pseudoDeploy = new HashSet<>();
		final NanoService			ns = new NanoService(){
			@Override public void start() throws IOException {}
			@Override public void suspend() throws IOException {}
			@Override public void resume() throws IOException {}
			@Override public void stop() throws IOException {}
			@Override public boolean isStarted() {return false;}
			@Override public boolean isSuspended() {return false;}
			@Override public FileSystemInterface getServiceRoot() {return null;}
			@Override public InetSocketAddress getServerAddress() {return null;}
			
			@Override
			public void deploy(String path, Object instance2deploy) throws IOException, ContentException, SyntaxException {
				pseudoDeploy.add(path);
			}

			@Override
			public Object undeploy(String path) {
				pseudoDeploy.remove(path);
				return null;
			}
		};
		
		try(final NanoServiceManager	mgr = new NanoServiceManager(new SystemErrLoggerFacade(), new SubstitutableProperties(Utils.mkProps(
										NanoServiceManager.NANOSERVICE_DEPLOYMENT_PERIOD, "0",
										NanoServiceManager.NANOSERVICE_DEPLOYMENT_CLASSPREFIX, "chav1961.purelib.nanoservice.",
										NanoServiceManager.NANOSERVICE_DEPLOYMENT_DIR, FileSystemInterface.FILESYSTEM_URI_SCHEME+":"+rootDir.toURI().toString())
									),ns)) {
			
			putClass("PseudoPlugin.class");
			Assert.assertEquals(1,mgr.redeploy());
			putClass("PseudoPlugin.class");
			Assert.assertEquals(0,mgr.redeploy());
			new File(rootDir,"PseudoPlugin.class").delete();
			Assert.assertEquals(-1,mgr.redeploy());
		}
	}	
	
//	@Test
	public void lifeCycleTest() throws IOException, ContentException, SyntaxException, InterruptedException {
		final Set<String>			pseudoDeploy = new HashSet<>();
		final NanoService			ns = new NanoService(){
			@Override public void start() throws IOException {}
			@Override public void suspend() throws IOException {}
			@Override public void resume() throws IOException {}
			@Override public void stop() throws IOException {}
			@Override public boolean isStarted() {return false;}
			@Override public boolean isSuspended() {return false;}
			@Override public FileSystemInterface getServiceRoot() {return null;}
			@Override public InetSocketAddress getServerAddress() {return null;}
			
			@Override
			public void deploy(String path, Object instance2deploy) throws IOException, ContentException, SyntaxException {
				pseudoDeploy.add(path);
			}

			@Override
			public Object undeploy(String path) {
				pseudoDeploy.remove(path);
				return null;
			}
		};
		
		try(final NanoServiceManager	mgr = new NanoServiceManager(new SystemErrLoggerFacade(), new SubstitutableProperties(Utils.mkProps(
										NanoServiceManager.NANOSERVICE_DEPLOYMENT_PERIOD, "1000",
										NanoServiceManager.NANOSERVICE_DEPLOYMENT_CLASSPREFIX, "chav1961.purelib.nanoservice.",
										NanoServiceManager.NANOSERVICE_DEPLOYMENT_DIR, FileSystemInterface.FILESYSTEM_URI_SCHEME+":"+rootDir.toURI().toString())
									),ns)) {
		
			Assert.assertEquals(0,pseudoDeploy.size());
			putClass("PseudoPlugin.class");
			Thread.sleep(3000);
			Assert.assertEquals(1,pseudoDeploy.size());
			
			new File(rootDir,"PseudoPlugin.class").delete();
			Thread.sleep(3000);
			Assert.assertEquals(0,pseudoDeploy.size());
		}
	}

	private void putClass(final String subpath) throws IOException {
		putClass(rootDir,subpath);
	}
	
	private void putClass(final File subdir, final String subpath) throws IOException {
		final long	cms = System.currentTimeMillis();
		
		new File(rootDir,subpath).getParentFile().mkdirs();
		try(final InputStream	is = this.getClass().getResourceAsStream(subpath);
			final OutputStream	os = new FileOutputStream(new File(subdir,subpath))) {
			
			Utils.copyStream(is, os);
		}
		new File(rootDir,subpath).setLastModified(cms);
	}
}
