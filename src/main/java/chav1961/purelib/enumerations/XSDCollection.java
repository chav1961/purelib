package chav1961.purelib.enumerations;

import chav1961.purelib.streams.char2char.CreoleWriter;

/**
 * <p>This enumeration describes a set of predefined XSD in the purelib. It contains:</p>
 * <ul>
 * <li>{@linkplain #XMLBasedParser} - XSD scheme for XML-based parser descriptor</li> 
 * <li>{@linkplain #CreoleXML} - XSD scheme for the XML, generated by {@linkplain CreoleWriter} class (see {@linkplain MarkupOutputFormat#XML}) </li> 
 * <li>{@linkplain #CreoleXMLFO} - XSD scheme for the XML, generated by {@linkplain CreoleWriter} class (see {@linkplain MarkupOutputFormat#XML2PDF}) </li>
 * </ul> 
 * @see chav1961.purelib.basic.xsd
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 * @lastUpdate 0.0.3
 */

public enum XSDCollection {
	/**
	 * <p>XSD scheme for XML-based parser descriptor</p>
	 */
	XMLBasedParser, 
	/**
	 * <p>XSD scheme for the XML, generated by {@linkplain CreoleWriter} class (see {@linkplain MarkupOutputFormat#XML})</p>
	 */
	CreoleXML, 
	/**
	 * <p>XSD scheme for the XML, generated by {@linkplain CreoleWriter} class (see {@linkplain MarkupOutputFormat#XML2PDF})</p>
	 */
	CreoleXMLFO,
	/**
	 * <p>XSD scheme for the XML, generated by {@linkplain CreoleWriter} class (see {@linkplain MarkupOutputFormat#XML2PDF})</p>
	 */
	PureLibNavigation,
	/**
	 * <p>XSD scheme for the XML, generated by {@linkplain CreoleWriter} class (see {@linkplain MarkupOutputFormat#XML2PDF})</p>
	 */
	XMLLocalizerContent,
	/**
	 * <p>XSD scheme for the XML, generated by {@linkplain CreoleWriter} class (see {@linkplain MarkupOutputFormat#XML2PDF})</p>
	 */
	XMLDescribedApplication,
	/**
	 * <p>XSD scheme for the XML, generated by {@linkplain CreoleWriter} class (see {@linkplain MarkupOutputFormat#XML2PDF})</p>
	 */
	XMLReadOnlyFS,
	
	/**
	 * <p>Official XSD scheme for SVG format (see https://www.w3.org/TR/2002/WD-SVG11-20020108/SVG.xsd)</p>
	 */
	SVG_restricted,
}
