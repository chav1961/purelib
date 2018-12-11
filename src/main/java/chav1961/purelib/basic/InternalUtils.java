package chav1961.purelib.basic;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

class InternalUtils {
	static int parseCommandLine(final String source, final String template, final List<String[]> pairs) {
		if (source == null || source.isEmpty()) {
			throw new IllegalArgumentException("Source string can't be null or empty");
		}
		else if (template == null || template.isEmpty()) {
			throw new IllegalArgumentException("Template string can't be null or empty");
		}
		else if (pairs == null) {
			throw new NullPointerException("Pair's list can't be null");
		}
		else {
			return parseCommandLine((' '+source+'\n').toCharArray(),0,(' '+template+'\n').toCharArray(),0,pairs);
		}
	}

	
	@SuppressWarnings("unchecked")
	static <T> T convert(final String string, final Class<T> awaited) {
		if (string == null || string.isEmpty()) {
			throw new IllegalArgumentException("String value can't be null or empty");
		}
		else if (awaited == null) {
			throw new NullPointerException("Awaited class can't be null or empty");
		}
		else {
			try{
				if (String.class.isAssignableFrom(awaited)) {
					return (T)string;
				}
				else if (int.class.isAssignableFrom(awaited)) {
					return (T)Integer.valueOf(string);
				}
				else if (Integer.class.isAssignableFrom(awaited)) {
					return (T)Integer.valueOf(string);
				}
				else if (long.class.isAssignableFrom(awaited)) {
					return (T)Long.valueOf(string);
				}
				else if (Long.class.isAssignableFrom(awaited)) {
					return (T)Long.valueOf(string);
				}
				else if (float.class.isAssignableFrom(awaited)) {
					return (T)Float.valueOf(string);
				}
				else if (Float.class.isAssignableFrom(awaited)) {
					return (T)Float.valueOf(string);
				}
				else if (double.class.isAssignableFrom(awaited)) {
					return (T)Double.valueOf(string);
				}
				else if (Double.class.isAssignableFrom(awaited)) {
					return (T)Double.valueOf(string);
				}
				else if (boolean.class.isAssignableFrom(awaited)) {
					return (T)Boolean.valueOf(string);
				}
				else if (Boolean.class.isAssignableFrom(awaited)) {
					return (T)Boolean.valueOf(string);
				}
				else if (File.class.isAssignableFrom(awaited)) {
					return (T)new File(string);
				}
				else if (URL.class.isAssignableFrom(awaited)) {
					return (T)new URL(string);
				}
				else if (URI.class.isAssignableFrom(awaited)) {
					return (T)URI.create(string);
				}
				else if (awaited.isEnum()) {
					return (T)awaited.getMethod("valueOf",String.class).invoke(null,string);
				}
				else {
					throw new UnsupportedOperationException("Conversion string to class ["+awaited+"] is not supported");
				}
			} catch (NumberFormatException exc) {
				throw new IllegalArgumentException("Can't convert string ["+string+"] to number format: "+exc.getMessage());
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException exc) {
				throw new IllegalArgumentException("Can't convert string ["+string+"] to enumeration: "+exc.getMessage());
			} catch (MalformedURLException exc) {
				throw new IllegalArgumentException("Can't convert string ["+string+"] to URL: "+exc.getMessage());
			}
		}
	}
	
	private static int parseCommandLine(final char[] source, int fromSource, final char[] template, int fromTemplate, final List<String[]> pairs) {
		int			place = 0, maxPlace = 0;
		
		for (;;) {
			switch (template[fromTemplate]) {
				case ' ' :
					if (source[fromSource] <= ' ' && source[fromSource] != '\n') {
						fromSource++;
					}
					else {
						fromTemplate++;
					}
					break;
				case '\\':
					switch (template[fromTemplate+1]) {
						case '\\' : case '[' : case ']' : case '{' : case '|' : case '}' : case '<' :  case '>' : case '$' :
							if (source[fromSource] == template[fromTemplate+1]) {
								fromSource++;
								fromTemplate += 2;
							}
							else {
								return -fromSource;
							}
							break;
						default :
							return -fromSource;
					}
					break;
				case '[' :
					int		optionPlace = parseCommandLine(source,fromSource,template,fromTemplate+1,pairs);
					
					if (optionPlace >= 0) {
						return optionPlace;
					}
					else {
						fromTemplate = skipUntil(template,fromTemplate+1,1,']','\n')+1;
					}
					break;
				case ']' :
					fromTemplate++;
					break;
				case '{' :
					while ((place = parseCommandLine(source,fromSource,template,fromTemplate+1,pairs)) < fromSource) {
						maxPlace = Math.min(place,maxPlace);
						fromTemplate = skipUntil(template,fromTemplate+1,1,'|','}','\n');

						if (template[fromTemplate] == '}') {
							return Math.min(place,maxPlace);
						}
					}
					return place;
				case '|' :
					fromTemplate = skipUntil(template,fromTemplate+1,1,'}','\n')+1;
					break;
				case '}' :
					fromTemplate++;
					break;
				case '<' :
					fromTemplate++;
					break;
				case '>' :
					if (fromTemplate < template.length - 1 && template[fromTemplate+1] != '.') {
						while (source[fromSource] <= ' ' && source[fromSource] != '\n') {
							fromSource++;
						}
						if (source[fromSource] == template[fromTemplate+1]) {
							fromSource++;
							while (source[fromSource] <= ' ' && source[fromSource] != '\n') {
								fromSource++;
							}
							return parseCommandLine(source,fromSource,template,skipUntil(template,fromTemplate-1,-1,'<')+1,pairs);
						}
						else {
							fromTemplate += 2;
							while (template[fromTemplate] == '.') {
								fromTemplate++;
							}
						}
					}
					else {
						final int	back = fromTemplate-1;
						
						fromTemplate++;
						while (template[fromTemplate] == '.') {
							fromTemplate++;
						}
						if ((place = parseCommandLine(source,fromSource,template,fromTemplate,pairs)) < fromSource) {
							return parseCommandLine(source,fromSource,template,skipUntil(template,back,-1,'<')+1,pairs);
						}
						else {
							return place;
						}
					}
					break;
				case '$' :
					if (template[fromTemplate+1] == '{') {
						final int		startName = fromTemplate + 2, endName = skipUntil(template,startName,1,'}','\n');
						final String	name = new String(template,startName,endName-startName);

						if (source[fromSource] == '\"') {	// Parameter is located inside quotas
							int		startVal = ++fromSource;
							
							while (source[fromSource] != '\"' && source[fromSource] != '\n') {
								fromSource++;
							}
							if (source[fromSource] == '\"') {
								final String	value = new String(source,startVal,fromSource-startVal);
								
								pairs.add(new String[]{name,value});
								fromSource++;
								fromTemplate = endName + 1;
							}
							else {
								return -(startVal-1);
							}							
						}
						else {
							int	startValue = fromSource, endValue = fromSource, location;
							
							pairs.add(new String[]{name,""});
							location = pairs.size()-1;
							
							while (endValue < source.length && (place = parseCommandLine(source,endValue,template,endName+1,pairs)) <= fromSource) {
								endValue++;
							}
							if (place > 0 || place == 0 && fromTemplate == 0) {
								pairs.get(location)[1] = new String(source,startValue,endValue-startValue).trim();
							}
							else {
								pairs.remove(location);
							}
							return place;
						}
					}
					else {
						return -fromSource;
					}
					break;
				case '\n' :
					while(source[fromSource] <= ' ' && source[fromSource] != '\n') {
						fromSource++;
					}
					return source[fromSource] == '\n' ? fromSource : -fromSource;
				default : 
					while (source[fromSource] <= ' ' && source[fromSource] != '\n') {
						fromSource++;
					}
					if (source[fromSource] == template[fromTemplate]) {
						fromSource++;
						fromTemplate++;
					}
					else {
						return -fromSource;
					}
			}
		}
	}

	private static int skipUntil(final char[] template, int from, final int delta, final char... terminals) {
		int	level = 0;
		
		for (int end = template.length; from >= 0 && from < end-1; from += delta) {
			if (level == 0 && inTerminals(template[from],terminals)) {
				break;
			}
			switch (template[from]) {
				case '[' : case '{' : case '<' : level += delta; break;
				case ']' : case '}' : case '>' : level -= delta; break;
			}
		}
		return from;
	}

	private static boolean inTerminals(final char symbol, final char[] terminals) {
		for (char item : terminals) {
			if (symbol == item) {
				return true;
			}
		}
		return false;
	}
}
