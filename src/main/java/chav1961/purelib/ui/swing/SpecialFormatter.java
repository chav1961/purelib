package chav1961.purelib.ui.swing;

import java.text.ParseException;

import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.text.DefaultFormatter;

/*	<format pattern> ::= [['@'<function>]...]<template>
	<function> ::= {'F' | 'D' | 'd' | '<<' | '>>' | 'C' | 'S'<number> }
	
	- F - display content in the predefined non-localizable format
	- << - left-aligned content
	- >> - right-aligned content
	- >< - center-aligned content
	- <> - justified content
	- P - color positive numbers
	- N - color negative numbers
	- Z - color zero numbers
	- S<number> - allow horizontal scrolling inside field
	- R - allow non-template chars can be inserted, but not stored in the result
	- M - mandatory field
	
	<template> ::= {'U' | 'L' | 'A' | '9' | 'H' | 'o' | 'B' | '%' | '¤' | '#' | '?' | '*' | '.' | ''''}...
	
	- U - upper-case letter
	- L - lower-case letter
	- A - alphanumeric value
	- # - optional number
	- 9 - mandatory number
	- h - optional hex number
	- H - mandatory hex number
	- o - optional octal number
	- O - mandatory octal number
	- b - optional binary number
	- B - mandatory binary number
	- % - percent. Need be the same last in the format
	- ¤ - currency. Need be the same last in the format
	- ? - any non-blank char
	- * - any char
	- . - decimal point
	- ' - exact sequence
	- [] - repeat the last template 
 */

class SpecialFormatter extends DefaultFormatter {
	private static final long serialVersionUID = 2563028563295278032L;

	public SpecialFormatter(final String format) throws IllegalArgumentException {
		if (format == null || format.isEmpty()) {
			throw new IllegalArgumentException("Format string can't be null");
		}
		else {
			
		}
	}
	
	@Override
	public Object stringToValue(final String text) throws ParseException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String valueToString(final Object value) throws ParseException {
		// TODO Auto-generated method stub
		return null;
	}
}
