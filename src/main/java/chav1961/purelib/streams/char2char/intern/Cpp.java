package chav1961.purelib.streams.char2char.intern;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chav1961.purelib.basic.exceptions.SyntaxException;


class Cpp {
	private static final long			PRINT_MASK = 0xFFFFFFFFFFFFFFFFL;

	private final File					includeRoot; 
	private final Map<String,PPSubst>	definitions = new HashMap<String,PPSubst>(); 
	private final Map<String,PPSubst>	funcDefinitions = new HashMap<String,PPSubst>();
	private final List<PPCommand>		anchored = new ArrayList<PPCommand>();
	private final List<PPCommand>		inline = new ArrayList<PPCommand>();
	private long 						printMask = PRINT_MASK;
	private int							ifLevel = 0;

	public Cpp(final File includeRoot) {
		if (includeRoot == null) {
			throw new IllegalArgumentException("Include root can't be null");
		}
		else {
			this.includeRoot = includeRoot;
		}
	}
	
	public void process(final BufferedReader brdr, final BufferedWriter bwr, final PrintStream log) throws IOException, SyntaxException {
		final StringBuilder		sb = new StringBuilder();
		String					buffer;
		int						line = 1;
		
		while ((buffer = brdr.readLine()) != null) {
			buffer = buffer.trim();
			if (buffer.length() > 0) {
			 	if (buffer.charAt(buffer.length()-1) == ';') {
					sb.append(buffer,0,buffer.length()-1).append(' ');
				}
				else if (sb.length() > 0) {
					process(line,sb.append(buffer).toString(),bwr,log);
					sb.setLength(0);
				}
				else {
					process(line,buffer,bwr,log);
				}
			}
			line++;
		}
	} 
 
	private void process(final int line, final String source, final BufferedWriter bwr, final PrintStream log) throws SyntaxException, IOException {
		if (source.charAt(0) == '#') {
			final int	blank = source.indexOf(' ');
			
			switch (blank == -1 ? source.substring(1) : source.substring(1,blank)) {
				case "command"		: if (printMask == PRINT_MASK) processCommand(line,source.substring(blank+1).trim().toCharArray(),true,true); break;
				case "define"		: if (printMask == PRINT_MASK) processDefine(line,source.substring(blank+1).trim().toCharArray()); break;
				case "else"			: processElse(line); break;
				case "end"			: processEnd(line); break;
				case "error"		: if (printMask == PRINT_MASK) processMessage(line,source.substring(blank+1),log,true); break;
				case "ifdef"		: processIf(source.substring(blank+1),true); break;
				case "ifndef"		: processIf(source.substring(blank+1),false); break;
				case "include"		: if (printMask == PRINT_MASK) processInclude(line,source.substring(blank+1),bwr,log); break;
				case "stdout"		: if (printMask == PRINT_MASK) processMessage(line,source.substring(blank+1),log,false); break;
				case "translate"	: if (printMask == PRINT_MASK) processCommand(line,source.substring(blank+1).trim().toCharArray(),true,false); break;
				case "undef"		: if (printMask == PRINT_MASK) processUndef(source.substring(blank+1)); break;
				case "xcommand"		: if (printMask == PRINT_MASK) processCommand(line,source.substring(blank+1).trim().toCharArray(),false,true); break;
				case "xtranslate"	: if (printMask == PRINT_MASK) processCommand(line,source.substring(blank+1).trim().toCharArray(),false,false); break;
				default 			: throw new SyntaxException(line, 1, "Unknown preprocessor directive");
			}
		}
		else if (printMask == PRINT_MASK) {
			processLine(line,source.toCharArray(),true,bwr,log);
			bwr.write('\n');
		}
	}
	
	private void processCommand(final int line, final char[] cmd, final boolean useX, final boolean anchoredCommand) throws SyntaxException {
		final List<String>			names = new ArrayList<String>();
		final List<List<PPItem>>	leftList = new ArrayList<List<PPItem>>();
		final List<PPItem>			rightList = new ArrayList<PPItem>();
		int							index, maxIndex, nameIndex;
		
		leftList.add(new ArrayList<PPItem>());
loop:	for (index = 0, maxIndex = cmd.length; index < maxIndex;) {
			index = Util.skipBlank(cmd,index);
			if (index < maxIndex) {
				switch (cmd[index]) {
					case '=' :
						if (index < maxIndex-1 && cmd[index+1] == '>') {
							index += 2;
							break loop;
						}
						else {
							throw new SyntaxException(line, index+1, "Unknown preprocessor directive"); 
						}
					case '[' :
						leftList.add(new ArrayList<PPItem>());
						index++;
						break;
					case ']' :
						if (leftList.size() > 1) {
							final List<PPItem>	nested = leftList.remove(leftList.size()-1);
							
							leftList.get(leftList.size()-1).add(new PPItem(nested.toArray(new PPItem[nested.size()])));
							index++;
						}
						else {
							throw new SyntaxException(line, index+1, "Unclosed ]"); 
						}
						break;
					case '<' :
						if (index < maxIndex-1) {
							switch (cmd[index+1]) {
								case '*' :
									final int	startTName = index+2, endTName = index = Util.skipName(cmd,index+2);
									
									if (index < maxIndex-2 && cmd[index] == '*' && cmd[index+1] == '>') {
										index += 2;
										
										leftList.get(leftList.size()-1).add(new PPItem(PPItemType.Tail,Util.checkAndAdd(cmd,startTName,endTName,line, index+1,names)));
									}
									else {
										throw new SyntaxException(line, index+1, "Unclosed *>"); 
									}
									break;
								case '(' :
									final int	startXName = index+2, endXName = index = Util.skipName(cmd,index+2);
									
									if (index < maxIndex-2 && cmd[index] == ')' && cmd[index+1] == '>') {
										index += 2;
										leftList.get(leftList.size()-1).add(new PPItem(PPItemType.Extended,Util.checkAndAdd(cmd,startXName,endXName,line, index+1,names)));
									}
									else {
										throw new SyntaxException(line, index+1, "Unclosed )>"); 
									}
									break;
								default :
									final int	startName = index+1, endName = index = Util.skipName(cmd,index+2);
									
									if (index < maxIndex && cmd[index] == '>') {
										index++;
										leftList.get(leftList.size()-1).add(new PPItem(PPItemType.Regular,Util.checkAndAdd(cmd,startName,endName,line, index+1, names)));
									}
									else if (index < maxIndex-5 && cmd[index] == ',' && cmd[index+1] == '.' && cmd[index+2] == '.' && cmd[index+3] == '.' && cmd[index+4] == '>') {
										index += 5;
										leftList.get(leftList.size()-1).add(new PPItem(PPItemType.List,Util.checkAndAdd(cmd,startName,endName,line, index+1, names)));
									}
									else if (index < maxIndex && cmd[index] == ':') {
										final List<char[]>		values = new ArrayList<>();
										
										do {index = Util.skipBlank(cmd,index+1);
											final int		startValue = index, endValue = index = Util.skipNameAndAmpersand(cmd,index); 
											final char[]	item = new char[endValue-startValue];
											
											System.arraycopy(cmd,startValue,item,0,endValue-startValue);
											values.add(item);
											index = Util.skipBlank(cmd,index);
										} while (index < maxIndex && cmd[index] == ',');
										
										if (index < maxIndex && cmd[index] == '>') {
											index++;
											leftList.get(leftList.size()-1).add(new PPItem(Util.checkAndAdd(cmd,startName,endName,line, index+1, names),values));
										}
										else {
											throw new SyntaxException(line, index+1, "Unclosed >");
										}
									}
									else {
										throw new SyntaxException(line, index+1, "Unknown tail"); 
									}
									break;
							}
						}
						break;
					default :
						final int	startName = index, endName = index = Util.skipNonBlank(cmd,index);
						
						leftList.get(leftList.size()-1).add(new PPItem(cmd,startName,endName));
						break;
				}
			}
		}

		final StringBuilder		format = new StringBuilder();
		
		index = Util.skipBlank(cmd,index);
		while(index < maxIndex) {
			switch (cmd[index]) {
				case '<' :
					if (index < maxIndex-1) {
						switch (cmd[index+1]) {
							case '\"' :
								final int	startSName = index+2, endSName = index = Util.skipName(cmd,index+2);
								
								if (index < maxIndex-2 && cmd[index] == '\"' && cmd[index+1] == '>') {
									index += 2;
									
									rightList.add(new PPItem(PPItemType.Quoted,nameIndex = Util.checkAndGet(cmd,startSName,endSName,line, index+1, names)));
									format.append('%').append(nameIndex+1).append("$s");
								}
								else {
									throw new SyntaxException(line, index+1, "Unclosed \">"); 
								}
								break;
							case '(' :
								final int	startXName = index+2, endXName = index = Util.skipName(cmd,index+2);
								
								if (index < maxIndex-2 && cmd[index] == ')' && cmd[index+1] == '>') {
									index += 2;
									rightList.add(new PPItem(PPItemType.Extended,nameIndex = Util.checkAndGet(cmd,startXName,endXName,line, index+1, names)));
									format.append('%').append(nameIndex+1).append("$s");
								}
								else {
									throw new SyntaxException(line, index+1, "Unclosed }>"); 
								}
								break;
							case '{' :
								final int	startBName = index+2, endBName = index = Util.skipName(cmd,index+2);
								
								if (index < maxIndex-2 && cmd[index] == '}' && cmd[index+1] == '>') {
									index += 2;
									rightList.add(new PPItem(PPItemType.Blocked,nameIndex = Util.checkAndGet(cmd,startBName,endBName,line, index+1, names)));
									format.append('%').append(nameIndex+1).append("$s");
								}
								else {
									throw new SyntaxException(line, index+1, "Unclosed }>"); 
								}
								break;
							case '.' :
								final int	startLName = index+2, endLName = index = Util.skipName(cmd,index+2);
								
								if (index < maxIndex-2 && cmd[index] == '.' && cmd[index+1] == '>') {
									index += 2;
									rightList.add(new PPItem(PPItemType.Logical,nameIndex = Util.checkAndGet(cmd,startLName,endLName,line, index+1, names)));
									format.append('%').append(nameIndex+1).append("$s");
								}
								else {
									throw new SyntaxException(line, index+1, "Unclosed .>"); 
								}
								break;
							default :
								final int	startName = index+1, endName = index = Util.skipName(cmd,index+1);
								
								if (index < maxIndex && cmd[index] == '>') {
									index++;
									rightList.add(new PPItem(PPItemType.Regular,nameIndex = Util.checkAndGet(cmd,startName,endName,line, index+1, names)));
									format.append('%').append(nameIndex+1).append("$s");
								}
								else {
									throw new SyntaxException(line, index+1, "Unclosed >"); 
								}
								break;
						}
					}
					break;
				case '#' :
					if (index < maxIndex-2 && cmd[index+1] == '<') {
						final int	startName = index+2, endName = index = Util.skipName(cmd,index+2);
	 					
						if (index < maxIndex && cmd[index] == '>') {
							index++;
							rightList.add(new PPItem(PPItemType.Dumb,nameIndex = Util.checkAndGet(cmd,startName,endName,line, index+1, names)));
							format.append('%').append(nameIndex+1).append("$s");
						}
						else {
							throw new SyntaxException(line, index+1, "Unclosed >"); 
						}

						break;
					}
				default :
					format.append(cmd[index++]);
					break;
			}
		}

		if (anchoredCommand) {
			anchored.add(new PPCommand(useX,names.size(),leftList.get(0),rightList,format.toString()));
		}
		else {
			inline.add(new PPCommand(useX,names.size(),leftList.get(0),rightList,format.toString()));
		}
	}

	private void processDefine(final int line, final char[] def) throws SyntaxException {
		final List<String>	names = new ArrayList<String>(); 
		final int			nameFrom = 0, nameTo = Util.skipName(def,nameFrom), end = def.length;
		final String		defName = new String(def,nameFrom,nameTo-nameFrom);
		final boolean		exists = definitions.containsKey(defName);
		int					skip = Util.skipBlank(def,nameTo);
		
		if (skip < end && def[skip] == '(') {
			do {final int	parmFrom = Util.skipBlank(def,skip+1), parmTo = Util.skipName(def,parmFrom);
				
				if (parmFrom != parmTo) {
					final String	parm = new String(def,parmFrom,parmTo-parmFrom);
					
					if (!names.contains(parm)) {
						names.add(parm);
					}
					else {
						throw new SyntaxException(line, parmFrom+1, "Duplicate parameter name ["+parm+"] in the function definition");
					}
				}				
				else {
					throw new SyntaxException(line, parmFrom+1, "Parameter name is missing in the function definition");
				}
				
				skip = Util.skipBlank(def,parmTo);
			} while (skip < end && def[skip] == ',');
			
			if (skip < end && def[skip] == ')') {
				final StringBuilder		sb = new StringBuilder();
				
				skip = Util.skipBlank(def,skip+1);
loop:			for (int index = skip; index < end; index++) {
					if (Character.isJavaIdentifierStart(def[index])) {
						final int		startAvailable = index, endAvailable = Util.skipName(def,index);
						final String	available = new String(def,startAvailable,endAvailable-startAvailable);

						for (int id = 0; id < names.size(); id++) {
							if (names.get(id).equals(available)) {
								sb.append('%').append(id+1).append("$s");
								index = endAvailable-1;
								continue loop;
							}
						}
						sb.append(available);
						index = endAvailable-1;
					}
					else {
						sb.append(def[index]);
					}
				}
				funcDefinitions.put(defName,new PPSubst(names.size(),sb.toString()));
			}
			else {
				throw new SyntaxException(line, skip+1, "Unclosed bracket in the function definition");
			}
		}
		else {
			definitions.put(defName,new PPSubst(new String(def,skip,def.length-skip)));
		}
		if (exists) {
			throw new SyntaxException(line, skip+1, "Redefinition of variable/function");
		}
	}

	private void processElse(final int line) throws SyntaxException {
		if (ifLevel <= 0) {
			throw new SyntaxException(line, 1, "Else directive without if");
		}
		else {
			printMask ^= (1L << ifLevel);
		}
	}

	private void processEnd(final int line) throws SyntaxException {
		if (ifLevel <= 0) {
			throw new SyntaxException(line,1,"End directive without if");
		}
		else {
			printMask |= (1L << ifLevel--);
		}
	}

	private void processIf(final String name, final boolean exists) {
		ifLevel++;
		if (exists) {
			if (!definitions.containsKey(name.trim()) && !funcDefinitions.containsKey(name.trim())) {
				printMask &= ~(1L << ifLevel);
			}
		}
		else {
			if (definitions.containsKey(name.trim()) || funcDefinitions.containsKey(name.trim())) {
				printMask &= ~(1L << ifLevel);
			}
		}
	}

	private void processInclude(final int line, final String includeSource, final BufferedWriter bwr, final PrintStream log) throws IOException, SyntaxException {
		final String 	name = includeSource.replace('\"',' ').replace('<',' ').replace('>',' ').trim();
		final File		include = new File(includeRoot,name); 
		
		if (include.exists() && include.isFile()) {
			try(final InputStream		fis = new FileInputStream(include);
				final Reader			rdr = new InputStreamReader(fis);
				final BufferedReader	brdr = new BufferedReader(rdr)) {
				
				process(brdr,bwr,log);
			}
		}
		else {
			throw new SyntaxException(line, 1, "Include file ["+include.getAbsolutePath()+"] is not exists or is not available");
		}
	}

	private void processMessage(final int line, final String message, final PrintStream log, final boolean throwException) throws SyntaxException {
		log.println(message);
		if (throwException) {
			throw new SyntaxException(line, 1, message);
		}
	}
	
	private void processLine(final int line, final char[] source, final boolean useAnchor, final BufferedWriter bwr, final PrintStream log) throws IOException, SyntaxException {
		int		cursor = Util.skipBlank(source,0), endData = source.length, processed;
		
		if (useAnchor) {
			for (PPCommand item : anchored) {
				if (cursor < endData && (processed = item.match(source,cursor)) > 0) {
					cursor = Util.skipBlank(source,processed);
					if (cursor < endData-1) {
						throw new SyntaxException(line, cursor+1, "Dust at the line tail");
					}
					else {
						processLine(line,item.substitute().toCharArray(),useAnchor,bwr,log);
						return;
					}
				}
			}
		}
		
		while (cursor < endData) {
			if (Character.isJavaIdentifierStart(source[cursor])) {
				final int		startName = cursor, endName = Util.skipName(source,cursor);
				final String	name = new String(source,startName,endName-startName);
				final PPSubst	subst;
				
				if (definitions.containsKey(name)) {
					definitions.get(name).match(source,0);
					processLine(line,Util.join(source,endName,definitions.get(name).substitute()),false,bwr,log);
					return;
				}
				else if (funcDefinitions.containsKey(name) && (processed = (subst = funcDefinitions.get(name)).match(source,startName)) > 0) {
					processLine(line,Util.join(source,processed,subst.substitute()),false,bwr,log);
					return;
				}
				else {
					for (PPCommand item : inline) {
						if (cursor < endData && (processed = item.match(source,cursor)) > 0) {
							processLine(line,Util.join(source,processed,item.substitute()),false,bwr,log);
							return;
						}
					}
					bwr.write(source,startName,endName-startName);
					cursor = endName;
				}				
			}
			else {
				final int	startNBlank = cursor, endNBlank = cursor = Util.skipNonBlank(source,cursor);
		 		
				for (PPCommand item : inline) { 
					if (startNBlank < endData && (processed = item.match(source,startNBlank)) > 0) {
						processLine(line,Util.join(source,processed,item.substitute()),false,bwr,log);
						return;
 					}
				}
				bwr.write(source,startNBlank,endNBlank-startNBlank);
				cursor = endNBlank;
			}
			
			final int	startBlank = cursor, endBlank = cursor = Util.skipBlank(source,cursor);
			
			if (endBlank > startBlank) {
				bwr.write(source,startBlank,endBlank-startBlank);
			}
		}
	}

	private void processUndef(final String name) {
		definitions.remove(name.trim());
		funcDefinitions.remove(name.trim());
	}
}
