/**
 * <p>This package doesn't contain Java classes (except {@linkplain XSDConst}), but has a set XSD schemes to validate different XMLs produced or parsed by the Pure Library classes.</p> 
 * <p>Complete list of the XSD schemes is:</p>
 * <ul>
 * <li>CreoleXML.xsd - XML generated by the {@linkplain chav1961.purelib.streams.char2char.CreoleWriter} class with the {@linkplain chav1961.purelib.enumerations.MarkupOutputFormat#XML} type</li>
 * <li>CreoleXMLFO.xsd - unofficial XML for <a href="https://xmlgraphics.apache.org/fop/">XML FOP</a> format generated by the {@linkplain chav1961.purelib.streams.char2char.CreoleWriter} class with the {@linkplain chav1961.purelib.enumerations.MarkupOutputFormat#XML2PDF} type</li>
 * <li>SVG_restricted.xsd - restricted XML to use in {@linkplain chav1961.purelib.ui.swing.useful.svg.SVGParser} class</li>
 * <li>XMLBasedParser.xsd - XML to describe {@linkplain chav1961.purelib.basic.XMLBasedParserText} functionality</li>
 * <li>XMLLocalizerContent.xsd - XML to describe content of {@linkplain chav1961.purelib.i18n.XMLLocalizer} class</li>
 * <li>XMLReadOnlyFSys.xsd - XML to describe content of {@linkplain chav1961.purelib.fsys.FileSystemOnXMLReadOnly} file system</li>
 * </ul>
 * <p>To get access to the XML schemes, you can use {@linkplain XSDConst} class as an anchor to them (see the class description for details)</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 * @lastUpdate 0.0.3
 */
package chav1961.purelib.basic.xsd;