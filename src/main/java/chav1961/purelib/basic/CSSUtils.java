package chav1961.purelib.basic;

import java.util.Map;

import chav1961.purelib.basic.CharUtils.SubstitutionSource;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.enumerations.StylePropertiesSupported;

public class CSSUtils {
//	{...}(1,2)
//	{...}*
//	{...}+
//	<...>(1,2)
//	<...>*
//	<...>+
//	'...'
//	name-name
//
//	type()
//	Map<> split(String,inher)
//	join(EnumMap<>,inher)
	
	public interface AggregateAttr {
		StylePropertiesSupported 				getType();
		StylePropertiesSupported[] 				getDetails();
		Map<StylePropertiesSupported,String> 	split(String content) throws SyntaxException;
		Map<StylePropertiesSupported,String> 	split(String content, SubstitutionSource inherit) throws SyntaxException;
		String									join(Map<StylePropertiesSupported,String> content) throws PrintingException;
		String									join(Map<StylePropertiesSupported,String> content, SubstitutionSource inherit) throws PrintingException;
	}
}
