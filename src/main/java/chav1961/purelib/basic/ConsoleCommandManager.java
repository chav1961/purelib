package chav1961.purelib.basic;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import chav1961.purelib.basic.annotations.ConsoleCommand;
import chav1961.purelib.basic.annotations.ConsoleCommandPrefix;
import chav1961.purelib.basic.annotations.ConsoleCommandParameter;
import chav1961.purelib.basic.exceptions.ConsoleCommandException;
import chav1961.purelib.basic.interfaces.ConsoleManagerInterface;

/**
 * <p>This class is a simplest implementation of the console command manager. It parses command line, search appropriative command line processor and invokes appropriative method in it</p> 
 * <p>The usual use case for any console applications is a command string. It gets the user an ability to interact with the console application by typing
 * any command on the console and get a result of it's execution. To support this ability, there is a command string parser in you program. This class is
 * a simplest implementation of command string parser.</p>
 * <p>To use this class, you need:</p>
 * <ul>
 * <li>create an instance of this class</li> 
 * <li>write any custom class for processing of the console command you wish</li> 
 * <li>annotate the custom class with the {@link chav1961.purelib.basic.annotations.ConsoleCommand ConsoleCommand}, {@link chav1961.purelib.basic.annotations.ConsoleCommandPrefix ConsoleCommandPrefix} and {@link chav1961.purelib.basic.annotations.ConsoleCommandParameter ConsoleCommandParameter} annotations</li> 
 * <li>deploy your custom class into the ConsoleCommandManager instance by using {@link #deploy(Object...) deploy(Object...)} command</li> 
 * <li>assign console input and output to the ConsoleCommandManager instance by using {@link #processCmd(java.io.Reader,java.io.PrintStream) processComd(Reader,PrintStream)} method</li> 
 * </ul>
 * <p>You can also use this class without assigning console input and output (for example, as console simulator). Use the {@link #processCmd(java.lang.String) processComd(String)} instead. This method is thread-safe. You can also mix calling this method with the {@link #processCmd(java.io.Reader,java.io.PrintStream) processComd(Reader,PrintStream)}</p>
 * <p>You can deploy new and undeploy old console command on-the-fly, using {@link #deploy(Object...) deploy(Object...)} and {@link #undeploy(Object...) undeploy(Object...)} methods.</p>  
 * <p>The ConsoleCommandManager class is marked with the {@link chav1961.purelib.basic.annotations.ConsoleCommand ConsoleCommand}, {@link chav1961.purelib.basic.annotations.ConsoleCommandPrefix ConsoleCommandPrefix} and {@link chav1961.purelib.basic.annotations.ConsoleCommandParameter ConsoleCommandParameter} annotations too.
 * They support processing of two default console commands:</p>
 * <ul>
 * <li><code>help</code> - prints a list of all deployed commands</li>
 * <li><code>help cmd=command_prefix</code> - prints all templates for the given command prefix with help strings</li>
 * </ul>
 * <p>Syntax of the command templates is described in the {@link chav1961.purelib.basic.annotations.ConsoleCommand ConsoleCommand} annotation. See {@link #processHelp() processHelp()} and {@link #processHelp(java.lang.String) processHelp(String)} methods for example.</p>
 * <p>When variable parts in the console command contains special symbols (for example, file names with blank chars), use quotas(") to prevent data content against parsing, for example <b><code>help "my command"</code></b></p>      
 * <p>This class implements {@link java.io.Closeable Closeable} interface, so it can be used in the <b>try-with-resource</b> statements.</p>
 * 
 * @see chav1961.purelib.basic.annotations.ConsoleCommand @ConsoleCommand
 * @see chav1961.purelib.basic.annotations.ConsoleCommandPrefix @ConsoleCommandPrefix
 * @see chav1961.purelib.basic.annotations.ConsoleCommandParameter @ConsoleCommandParameter
 * @see chav1961.purelib.basic.interfaces.ConsoleManagerInterface ConsoleManagerInterface
 * @see chav1961.purelib.basic JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */


@ConsoleCommandPrefix("help")
public class ConsoleCommandManager implements ConsoleManagerInterface {
	private static final Set<Class<?>>				AVAILABLE_CLASSES = new HashSet<Class<?>>(){{
																	add(int.class);				add(int[].class);
																	add(long.class);			add(long[].class);
																	add(float.class);			add(float[].class);
																	add(double.class);			add(double[].class);
																	add(boolean.class);			add(boolean[].class);
																	add(Integer.class);			add(Integer[].class);
																	add(Long.class);			add(Long[].class);
																	add(Float.class);			add(Float[].class);
																	add(Double.class);			add(Double[].class);
																	add(Boolean.class);			add(Boolean[].class);
																	add(String.class);			add(String[].class);
																	add(File.class);			add(File[].class);
																	add(URL.class);				add(URL[].class);
																	add(URI.class);				add(URI[].class);
																}};
	
	private final List<Object>						deployment = new ArrayList<>();
	private final Map<String,List<CmdContainer>> 	cmdList = new HashMap<>();
	private boolean									wasClosed = false;
	
	public ConsoleCommandManager(){
		forEach(this,(prefix,obj,clazz,method)->deploy(prefix,obj,clazz,method));
	}

	@Override
	public void close() throws IOException {
		undeployAll();
		deployment.clear();
		cmdList.clear();
		wasClosed = true;
	}
	
	@Override
	public void deploy(final Object... callbacks) {
		if (wasClosed) {
			throw new IllegalStateException("Attempt to deploy command processor(s) on closed manager!");
		}
		else if (callbacks != null && callbacks.length > 0) {
			for (int index = 0; index < callbacks.length; index++) {
				if (callbacks[index] != null) {
					forEach(callbacks[index],(prefix,obj,clazz,method)->deploy(prefix,obj,clazz,method));
					deployment.add(callbacks[index]);
				}
				else {
					throw new IllegalArgumentException("Null object at position ["+index+"] in the parameter's list");
				}
			}
		}
		else {
			throw new IllegalArgumentException("Deployment list can't be null or empty array");
		}
	}

	@Override
	public void undeploy(final Object... callbacks) {
		if (wasClosed) {
			throw new IllegalStateException("Attempt to undeploy command processor(s) on closed manager!");
		}
		else if (callbacks != null && callbacks.length > 0) {
			for (int index = 0; index < callbacks.length; index++) {
				if (callbacks[index] != null) {
					if (deployment.contains(callbacks[index])) {
						forEach(callbacks[index],(prefix,obj,clazz,method)->undeploy(prefix,obj,clazz,method));
						deployment.remove(callbacks[index]);
					}
					else {
						throw new IllegalArgumentException("Object at position ["+index+"] was not deployed earlier!");
					}
				}
				else {
					throw new IllegalArgumentException("Null object an position ["+index+"] in the parameter's list");
				}
			}
		}
		else {
			throw new IllegalArgumentException("Undeployment list can't be null or empty array");
		}
	}

	@Override
	public void undeployAll() {
		if (wasClosed) {
			throw new IllegalStateException("Attempt to undeploy command processor(s) on closed manager!");
		}
		else {
			for (Object item : new ArrayList(){{addAll(deployment);}}) {
				undeploy(item);
			}
		}
	}
	
	@Override
	public synchronized String processCmd(final String cmd) throws ConsoleCommandException {
		if (wasClosed) {
			throw new IllegalStateException("Attempt to process command on closed manager!");
		}
		else if (cmd == null) {
			throw new ConsoleCommandException("Command string can't be null");
		}
		else if (cmd.isEmpty()) {
			return processCmd("help");
		}
		else if (!cmdList.containsKey(extractCommand(cmd))) {
			throw new ConsoleCommandException("Unknown or undeployed command ["+extractCommand(cmd)+"]");
		}
		else {
			return executeCommand(cmdList.get(extractCommand(cmd)),cmd);
		}
	}


	@Override
	public void processCmd(final Reader in, final PrintStream out) throws IOException {
		if (wasClosed) {
			throw new IllegalStateException("Attempt to process command stream on closed manager!");
		}
		else if (in == null) {
			throw new IllegalArgumentException("Reader can't be null"); 
		}
		else if (out == null) {
			throw new IllegalArgumentException("Print stream can't be null"); 
		}
		else {
			try(final BufferedReader	brdr = new BufferedReader(in)) {
				String	buffer;
				
				while ((buffer = brdr.readLine()) != null) {
					try{out.println(processCmd(buffer));
					} catch (ConsoleCommandException ex)  {
						out.println("Error procesing command: "+ex.getMessage());
					}
				}
			} catch (IOException e) {
				out.println("I/O error processing commands ("+e.getMessage()+"). Processing will be terminated");
			}
		}
	}

	@ConsoleCommand(template="help",help="Help about all commands. To get help about command use 'help cmd=command_prefix'")
	public String processHelp() {
		final StringBuilder	sb = new StringBuilder();
		final String[]		names = cmdList.keySet().toArray(new String[cmdList.size()]);
		
		Arrays.sort(names);
		for (String item : names) {
			sb.append(item).append('\n');
		}
		return sb.append("Use help cmd=<command_prefix> for details\n").toString(); 
	}

	@ConsoleCommand(template="help cmd=${cmd}",help="Help about given cmd")
	public String processHelp(@ConsoleCommandParameter(name="cmd") final String commandPrefix) {
		if (!cmdList.containsKey(commandPrefix)) {
			return "command prefix ["+commandPrefix+"] in not known, use help to get list of available commands\n";
		}
		else {
			final StringBuilder		sb = new StringBuilder();
			final CmdContainer[]	commands = cmdList.get(commandPrefix).toArray(new CmdContainer[cmdList.get(commandPrefix).size()]);
			
			Arrays.sort(commands,new Comparator<CmdContainer>(){
										@Override
										public int compare(CmdContainer o1, CmdContainer o2) {
											return o1.getCommandTemplate().compareTo(o2.getCommandTemplate());
										}
									}
			);
			for (CmdContainer item : commands) {
				sb.append(" - ").append(item.getCommandTemplate()).append('\t').append(item.getCommandHelp()).append('\n');
			}
			return sb.toString(); 
		}
	}
	
	@FunctionalInterface
	private interface CommandProcessorCallback {
		void process(String prefix, Object obj, Class<?> clazz, Method method);
	}
	
	private void forEach(final Object object, final CommandProcessorCallback callback) {
		final Class<?>	cl = object.getClass();
		
		if (cl.getAnnotation(ConsoleCommandPrefix.class) == null) {
			throw new IllegalArgumentException("Class ["+cl.getName()+"] to deploy doesn't have mandatory "+ConsoleCommandPrefix.class.getSimpleName()+" annotation");
		}
		else {
			final String		prefix = cl.getAnnotation(ConsoleCommandPrefix.class).value();
			final List<Method>	annotated = new ArrayList<>();
			boolean				commandsPresent = false;
			
			for (Method m : cl.getMethods()) {
				if (m.getAnnotation(ConsoleCommand.class) != null) {
					if (!m.getAnnotation(ConsoleCommand.class).template().startsWith(prefix)) {
						throw new IllegalArgumentException("Prefix in the @ConsoleCommandPrefix ["+prefix+"] is differ than start command template ["+m.getAnnotation(ConsoleCommand.class).template()+"]! ");
					}
					else {
						commandsPresent = true;
						annotated.add(m);
					}
				}
			}
			if (!commandsPresent) {
				throw new IllegalArgumentException("Class ["+cl.getName()+"] doesn't have any methods with ["+ConsoleCommand.class.getSimpleName()+"] annotation!");
			}
			for (Method m : annotated) {
				callback.process(prefix,object,cl,m);
			}
		}
	}

	private void deploy(final String prefix, final Object object, final Class<?> cl, final Method m) {
		final String		template = m.getAnnotation(ConsoleCommand.class).template();
		final String[]		names = new String[m.getParameterCount()], defaults = new String[m.getParameterCount()];
		final Class<?>[]	types = new Class[m.getParameterCount()];

		if (!cmdList.containsKey(prefix)) {
			cmdList.put(prefix,new ArrayList<CmdContainer>());
		}
		for (CmdContainer item : cmdList.get(prefix)) {
			if (item.getCommandTemplate().equals(template)) {
				throw new IllegalArgumentException("Class ["+cl.getName()+"], method ["+m.getName()+"] has the same command template as class ["+item.getClassDescriptor()+"], method ["+item.getMethodDescriptor()+"]!"); 
			}			
		}
		
		for (int index = 0; index < m.getParameterCount(); index++) {
			if (m.getParameters()[index].getAnnotation(ConsoleCommandParameter.class) == null) {
				throw new IllegalArgumentException("Class ["+cl.getName()+"], method ["+m.getName()+"] - parameter="+index+" not contains mandatory "+ConsoleCommandParameter.class.getSimpleName()+" annotation"); 
			}
			else if (!AVAILABLE_CLASSES.contains(m.getParameters()[index].getType()) && !m.getParameters()[index].getType().isEnum()) {
				throw new IllegalArgumentException("Class ["+cl.getName()+"], method ["+m.getName()+"] - parameter="+index+" has unsupported class ["+m.getParameters()[index].getType()+"]. Available classes are "+AVAILABLE_CLASSES); 
			}
			else if (!template.contains("${"+m.getParameters()[index].getAnnotation(ConsoleCommandParameter.class).name()+"}")) {
				throw new IllegalArgumentException("Class ["+cl.getName()+"], method ["+m.getName()+"] - parameter="+index+": name referenced ${"+m.getParameters()[index].getAnnotation(ConsoleCommandParameter.class).name()+"} is missing in the template string ["+template+"]!"); 
			}
			else {
				names[index] = m.getParameters()[index].getAnnotation(ConsoleCommandParameter.class).name();
				defaults[index] = m.getParameters()[index].getAnnotation(ConsoleCommandParameter.class).defaultValue();
				types[index] = m.getParameters()[index].getType();
			}
		}
		cmdList.get(prefix).add(new CmdContainerImpl(object,m,prefix,template,m.getAnnotation(ConsoleCommand.class).help(),names,defaults,types));
	}

	private void undeploy(final String prefix, final Object object, final Class<?> cl, final Method m) {
		final String	template = m.getAnnotation(ConsoleCommand.class).template();
		CmdContainer	cc = null;
		
		if (cmdList.containsKey(prefix)) {
			for (CmdContainer item : cmdList.get(prefix)) {
				if (item.getCommandTemplate().equals(template)) {
					cc = item;
					break;
				}			
			}
			if (cc != null) {
				cmdList.get(prefix).remove(cc);
			}
		}
	}
	
	private String executeCommand(final List<CmdContainer> list, final String cmd) throws ConsoleCommandException {
		final Map<String,List<String>>	props = new HashMap<String,List<String>>();
		String	template = null;
		int		parsed = 0, maxParsed = 0;
		
		for (CmdContainer item : list) {
			final String 	cmdTemplate = item.getCommandTemplate();
			
			if ((parsed = matches(cmdTemplate,cmd,props)) > 0) {
				return process(cmdTemplate,item,props);
			}
			else if (parsed < maxParsed) {
				maxParsed = parsed;
				template = cmdTemplate;
			}
		}
		throw new ConsoleCommandException("No one command processor(s) found to process this command. The most similar command template is ["+template+"] problem at position="+(-maxParsed));
	}
	
	private int matches(final String template, final String cmd, final Map<String,List<String>> props) {
		final List<String[]>	pairs = new ArrayList<String[]>();
		int						result;
		
		props.clear();
		if ((result = InternalUtils.parseCommandLine(cmd,template,pairs)) > 0) {
			for (String[] item : pairs) {
				if (!props.containsKey(item[0])) {
					props.put(item[0],new ArrayList<String>());
				}
				props.get(item[0]).add(item[1]);
			}
		}
		return result;
	}

	private String process(final String template, final CmdContainer item, final Map<String,List<String>> props) throws ConsoleCommandException {
		final String[]		names = item.getParameterNames();		
		final String[]		defaults = item.getParameterDefaults();		
		final Class<?>[]	types = item.getParameters();		
		final Object[]		parms = new Object[names.length];
	
		for (int index = 0; index < names.length; index++) {
			if (!props.containsKey(names[index])){
				props.put(names[index],Arrays.asList(defaults[index]));
			}
			parms[index] = convert(template,names[index],props.get(names[index]),types[index]);
		}
		return item.invoke(parms);
	}
	
	private Object convert(final String template, final String name, final List<String> property, final Class<?> awaited) throws ConsoleCommandException {
		if (awaited.isArray()) {
			final Object	result = Array.newInstance(awaited.getComponentType(),property.size());
			
			for (int index = 0, maxIndex = property.size(); index < maxIndex; index++) {
				Array.set(result,index,InternalUtils.convert(property.get(index),awaited.getComponentType())); 
			}
			return result;
		}
		else if (property.size() != 1) {
			throw new ConsoleCommandException("Command ["+template+"], parameter ["+name+"] need have exactly one value");
		}
		else {
			try{return InternalUtils.convert(property.get(0),awaited);
			} catch (IllegalArgumentException exc) {
				throw new ConsoleCommandException("Command ["+template+"], parameter ["+name+"] : "+exc.getMessage());
			}
		}
	}

	private Object extractCommand(final String cmd) {
		return cmd.trim().split("\\ ")[0];
	}	

	private interface CmdContainer {
		Class<?> getClassDescriptor();
		Method getMethodDescriptor();
		String getCommandPrefix();
		String getCommandTemplate();
		String getCommandHelp();
		String[] getParameterNames();
		String[] getParameterDefaults();
		Class<?>[] getParameters();
		String invoke(final Object... parameters) throws ConsoleCommandException; 
	}

	private static class CmdContainerImpl implements CmdContainer {
		private final Object			instance;
		private final Method			method;
		private final String			prefix;
		private final String			template;
		private final String			help;
		private final String[]			names;
		private final String[]			defaults;
		private final Class<?>[]		types;
		
		public CmdContainerImpl(final Object instance, final Method method, final String prefix, final String template, final String help, final String[] names, final String[] defaults, final Class<?>[] types) {
			this.instance = instance;	this.method = method;
			this.prefix = prefix;		this.template = template;
			this.help = help;			this.names = names;
			this.defaults = defaults;	this.types = types;
		}

		@Override public Class<?> getClassDescriptor(){return instance.getClass();}
		@Override public Method getMethodDescriptor(){return method;}
		@Override public String getCommandPrefix() {return prefix;}
		@Override public String getCommandTemplate() {return template;}
		@Override public String getCommandHelp() {return help;}
		@Override public String[] getParameterNames() {return names;}
		@Override public String[] getParameterDefaults() {return defaults;}
		@Override public Class<?>[] getParameters() {return types;}

		@Override
		public String invoke(final Object... parameters) throws ConsoleCommandException {
			try{final Object	obj = method.invoke(instance,parameters);
			
				return obj != null ? obj.toString() : "";
			} catch (InvocationTargetException exc) {
				throw new ConsoleCommandException(exc.getTargetException());
			} catch (Throwable exc) {
				throw new ConsoleCommandException(exc);
			}
		}
	}
}

