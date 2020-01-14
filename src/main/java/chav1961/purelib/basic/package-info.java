/**
 * <p>This package contains a set of classes for the basic abilities of the library. They includes:</p>
 * <ul>
 * <li> a set of logger facades (implements {@linkplain chav1961.purelib.basic.interfaces.LoggerFacade LoggerFacade} interface) to prevent your application from the <b>ZOO</b> 
 * of huge amount of loggers and reduce logger trace due to different criteria</li> 
 * <li> {@link chav1961.purelib.basic.AbstractScriptEngine} and it's children to support scripting languages in your library</li> 
 * <li> {@link chav1961.purelib.basic.AndOrTree} and {@link chav1961.purelib.basic.OrdinalSyntaxTree} classes to implement quick name/id tree for using in the parsers</li> 
 * <li> {@link chav1961.purelib.basic.ArgParser} class to parse and manage of command line arguments for your application</li> 
 * <li> {@link chav1961.purelib.basic.BitCharSet} and {@link chav1961.purelib.basic.ExtendedBitCharSet} classes to quick classification of the characters 
 * (similar to {@link java.lang.Character#isDigit(char)} or {@link java.lang.Character#isJavaIdentifierPart(char)} methods)</li> 
 * <li> {@link chav1961.purelib.basic.CharUtils} class, containing a set of static methods to parse, print and manipulate of character arrays and strings. It's strongly recommended to use them in your compilers</li> 
 * <li> {@link chav1961.purelib.basic.ConsoleCommandManager} class to implement simple-and-easy console command processing in the Java console-oriented applications</li> 
 * <li> {@link chav1961.purelib.basic.FSM} and {@link chav1961.purelib.basic.StackedFSM} classed to support finite state machines in your applications</li> 
 * <li> {@link chav1961.purelib.basic.GettersAndSettersFactory} class to build raw assemble code for fast access to instance fields of any classes</li> 
 * <li> {@link chav1961.purelib.basic.LineByLineProcessor} class for quick splitting input source to <i>lines</i> and support it's line-by-line processing</li> 
 * <li> {@link chav1961.purelib.basic.LongIdMap} class to implement quick tree with long ID as key</li> 
 * <li> {@link chav1961.purelib.basic.PluggableClassLoader} class to implement simple plug-in mechanism in your application</li> 
 * <li> {@link chav1961.purelib.basic.PureLibSettings} class as a center of all the Pure Library settings</li> 
 * <li> {@link chav1961.purelib.basic.ReusableInstances} class to support cache of reusable class instances for reducing memory allocations in your application</li> 
 * <li> {@link chav1961.purelib.basic.SequenceIterator} class for simple joining a set of iterators to one total iterator</li> 
 * <li> {@link chav1961.purelib.basic.SimpleURLClassLoader} class for defining new classes (both with raw class file data and source assembly code - see {@linkplain chav1961.purelib.streams.char2byte.AsmWriter})</li> 
 * <li> {@link chav1961.purelib.basic.SubstitutableProperties} class to process property files content with automatic substitutions and data conversions</li> 
 * <li> {@link chav1961.purelib.basic.TemporaryStore} class to use temporary memory-to-disk-growable files</li> 
 * <li> {@link chav1961.purelib.basic.URIUtils} class, containing a set of static methods to manage URIs.</li> 
 * <li> {@link chav1961.purelib.basic.Utils} class, containing a set of useful methods to use in your applicaiton.</li> 
 * <li> {@link chav1961.purelib.basic.XMLUtils} class, containing a set of static methods to manage XML.</li> 
 * </ul>
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 * @lastUpdate 0.0.4
 */
package chav1961.purelib.basic;