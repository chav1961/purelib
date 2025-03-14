/**
 * <p>This package contains a set of {@linkplain chav1961.purelib.basic.interfaces.LoggerFacade LoggerFacade} implementations
 * to use in user applications. All the implementations are available thru SPI service. Sub-schemes to get implementations are:</p>
 * <ul>
 * <li>':default:/' - default logger facade</li> 
 * <li>':pure:/' - internal Pure Library logger facade</li> 
 * <li>':jre:/' - standard JRE logger facade (see {@linkplain java.utl.log} package)</li>  
 * <li>':string:/' - string logger facade</li>  
 * <li>':swing:/' - Swing logger facade (see {@linkplain javax.swing.JEditorPane JEditorPane} class</li>
 * <li>':err:/' - System.err logger facade (see {@linkplain java.lang.System} class</li>
 * </ul> 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.8
 */
package chav1961.purelib.basic.logs;