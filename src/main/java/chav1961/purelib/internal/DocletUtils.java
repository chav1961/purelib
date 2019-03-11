package chav1961.purelib.internal;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.BitCharSet;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.LineByLineProcessor;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.i18n.AbstractLocalizer.SupportedLanguages;

public class DocletUtils {
	static final SyntaxTreeInterface<ActionsCallback>	ACTIONS = new AndOrTree<>();

	private static final BitCharSet						STOPS = new BitCharSet('\n','\r','<','@','&','{');
	
	
	static {
		ACTIONS.placeName("lt",(sb, data, from, to, imports)->{sb.append('<');});
		ACTIONS.placeName("gt",(sb, data, from, to, imports)->{sb.append('>');});
		ACTIONS.placeName("nbsp",(sb, data, from, to, imports)->{sb.append(' ');});
		ACTIONS.placeName("amp",(sb, data, from, to, imports)->{sb.append('&');});
		ACTIONS.placeName("p",(sb, data, from, to, imports)->{});
		ACTIONS.placeName("/p",(sb, data, from, to, imports)->{sb.append("\n\n");});
		ACTIONS.placeName("b",(sb, data, from, to, imports)->{sb.append("**");});
		ACTIONS.placeName("/b",(sb, data, from, to, imports)->{sb.append("**");});
		ACTIONS.placeName("i",(sb, data, from, to, imports)->{sb.append("//");});
		ACTIONS.placeName("/i",(sb, data, from, to, imports)->{sb.append("//");});
		ACTIONS.placeName("li",(sb, data, from, to, imports)->{sb.append("\n* ");});
		ACTIONS.placeName("/li",(sb, data, from, to, imports)->{sb.append("\n");});
		ACTIONS.placeName("about",(sb, data, from, to, imports)->{});
		ACTIONS.placeName("author",(sb, data, from, to, imports)->{
			final int	pos = CharUtils.skipBlank(data,from,true);
			
			sb.append("\n\n//Author:// ").append(data, pos, to - pos);
		});
		ACTIONS.placeName("beta",(sb, data, from, to, imports)->{
			final int	pos = CharUtils.skipBlank(data,from,true);
			
			sb.append("\n\n//**Beta:**// ").append(data, pos, to - pos);
		});
		ACTIONS.placeName("code",(sb, data, from, to, imports)->{
			sb.append("{{{").append(data, from, to - from).append("}}}");
		});
		ACTIONS.placeName("deprecated",(sb, data, from, to, imports)->{
			final int	pos = CharUtils.skipBlank(data,from,true);
			
			sb.append("\n\n//Deprecated:// ").append(data, pos, to - pos);
		});
		ACTIONS.placeName("docRoot",(sb, data, from, to, imports)->{});
		ACTIONS.placeName("exception",(sb, data, from, to, imports)->{
			ACTIONS.getCargo(ACTIONS.seekName("throws")).process(sb, data, from, to, imports);
		});		
		ACTIONS.placeName("inheritDoc",(sb, data, from, to, imports)->{});
		ACTIONS.placeName("keyWords",(sb, data, from, to, imports)->{});
		ACTIONS.placeName("lastUpdated",(sb, data, from, to, imports)->{
			final int	pos = CharUtils.skipBlank(data,from,true);
			
			sb.append("\n\n//Last updated:// ").append(data, pos, to - pos);
		});
		ACTIONS.placeName("link",(sb, data, from, to, imports)->{
			try{final int[]	namePos = new int[2];
				final int	start = CharUtils.skipBlank(data,from,true);
				int			pos = CharUtils.parseName(data,start,namePos);

				if (CharUtils.skipBlank(data,pos,true) >= to) {
					resolveReferences(data, namePos[0], namePos[1]+1, imports, sb);
					sb.append(" : ").append(data,pos,to-pos);
				}
				else {
					sb.append("[[");
					resolveSingleReference(data, namePos[0], namePos[1]+1, imports, sb);
					sb.append("|").append(data,pos,to-pos).append("]]");
				}
			} catch (IllegalArgumentException exc) {
				sb.append("\n\n* ").append(data, from, to-from);
			}
		});		
		ACTIONS.placeName("linkplain",(sb, data, from, to, imports)->{
			ACTIONS.getCargo(ACTIONS.seekName("link")).process(sb, data, from, to, imports);
		});		
		ACTIONS.placeName("param",(sb, data, from, to, imports)->{
			try{final int[]	namePos = new int[2];
				int			pos = CharUtils.parseName(data,CharUtils.skipBlank(data,from,true),namePos);
				
				sb.append("\n\n* **").append(data, namePos[0], namePos[1]-namePos[0]+1).append("**").append(data,pos,to-pos);
			} catch (IllegalArgumentException exc) {
				sb.append("\n\n* ").append(data, from, to-from);
			}
		});
		ACTIONS.placeName("return",(sb, data, from, to, imports)->{
			sb.append("\n\n//Returns://").append(data,from,to-from);
		});
		ACTIONS.placeName("serial",(sb, data, from, to, imports)->{});
		ACTIONS.placeName("serialData",(sb, data, from, to, imports)->{});
		ACTIONS.placeName("serialField",(sb, data, from, to, imports)->{});
		ACTIONS.placeName("see",(sb, data, from, to, imports)->{
			sb.append("\n\n//See also:// ");
			resolveReferences(data, from, to, imports, sb); 
			sb.append('\n');
		});
		ACTIONS.placeName("since",(sb, data, from, to, imports)->{
			final int	pos = CharUtils.skipBlank(data,from,true);
			
			sb.append("\n\n//Since:// ").append(data, pos, to - pos);
		});
		ACTIONS.placeName("threadSafed",(sb, data, from, to, imports)->{
			final int	pos = CharUtils.skipBlank(data,from,true);
			
			sb.append("\n\n//Thread safed:// ").append(data, pos, to - pos);
		});
		ACTIONS.placeName("throws",(sb, data, from, to, imports)->{
			try{final int[]	namePos = new int[2];
				int			pos = CharUtils.parseName(data,CharUtils.skipBlank(data,from,true),namePos);
				
				sb.append("\n\n*");
				resolveReferences(data, namePos[0], namePos[1]+1, imports, sb);
				sb.append(" : ").append(data,pos,to-pos);
			} catch (IllegalArgumentException exc) {
				sb.append("\n\n* ").append(data, from, to-from);
			}
		});		
		ACTIONS.placeName("value",(sb, data, from, to, imports)->{
			ACTIONS.getCargo(ACTIONS.seekName("link")).process(sb, data, from, to, imports);
		});		
		ACTIONS.placeName("version",(sb, data, from, to, imports)->{
			final int	pos = CharUtils.skipBlank(data,from,true);
			
			sb.append("\n\n//Version:// ").append(data, pos, to - pos);
		});
	}
	
	public enum ReferenceType {
		See_REF, Link_Ref, CodeSample_REF, Overview_REF
	}

	public static MultilangContent[] javadoc2Creole(final String content, final SyntaxTreeInterface<char[]> imports) throws IOException, SyntaxException {
		if (content == null || content.isEmpty()) {
			throw new IllegalArgumentException("Content can't be null");
		}
		else {
			final List<MultilangContent>	result = new ArrayList<>();
			final StringBuilder				sb = new StringBuilder();
			
			try(final LineByLineProcessor	proc = new LineByLineProcessor((displacement, lineNo, data, from, length)->{
															parseInternal(lineNo,data,from,length,sb,imports,ACTIONS,result);
															})) {
				proc.write(content.toCharArray(),0,content.length());
			}
			return result.toArray(new MultilangContent[result.size()]);
		}
	}
	
	static void parseInternal(final int lineNo, final char[] data, final int from, final int length, final StringBuilder sb, final SyntaxTreeInterface<char[]> imports, final SyntaxTreeInterface<ActionsCallback> actions, final List<MultilangContent> result) {
		final int	end = from + length;
		int			start = from, current = start;
		long		id;
		
		while (current < end) {
			while (!STOPS.contains(data[current])) {
				current++;
			}
			if (current > start) {
				sb.append(data,start,current-start);
				start = current;
			}
			switch (data[current]) {
				case '\n' : case '\r' :
					sb.append('\n');
					return;
				case '<'	:
					current++;
					if (data[current] == '/') {
						current++;
					}
					while (Character.isJavaIdentifierPart(data[current])) {
						current++;
					}
					id = ACTIONS.seekName(data,start+1,current);					
					if (id >= 0) {
						ACTIONS.getCargo(id).process(sb,data,start+1,current,imports);
					}
					current = start = current + 1;
					break;
				case '@'	:
					current++;
					while (Character.isJavaIdentifierPart(data[current])) {
						current++;
					}
					id = ACTIONS.seekName(data,start+1,current);					
					if (id >= 0) {
						ACTIONS.getCargo(id).process(sb,data,current+1,end,imports);
						return;
					}
					break;
				case '{'	:
					current++;
					if (data[current] == '@') {
						current++;
						while (Character.isJavaIdentifierPart(data[current])) {
							current++;
						}
						id = ACTIONS.seekName(data,start+2,current);					
						if (id >= 0) {
							start = current + 1;
							while (data[current] != '}' && data[current] != '\r' && data[current] != '\n') {
								current++;
							}
							ACTIONS.getCargo(id).process(sb,data,start,current,imports);
							current = start = current + 1;
						}
					}
					else {
						current++;
					}
					break;
				case '&'	:
					current++;
					while (Character.isJavaIdentifierPart(data[current])) {
						current++;
					}
					if (data[current] == ';') {
						id = ACTIONS.seekName(data,start+1,current);					
						if (id >= 0) {
							ACTIONS.getCargo(id).process(sb,data,start+1,current,imports);
							current = start = current + 1;
						}
					}
					break;
				default : throw new UnsupportedOperationException(); 
			}
		}
	}

	private static void resolveReferences(final char[] data, final int from, final int to, final SyntaxTreeInterface<char[]> imports, final StringBuilder sb) {
		int		start = from, current = start, number;
		
		while (current < to && data[current] != '\n') {
			while (current < to && data[current] <= ' ' && data[current] != '\n') {
				current++;
			}
			sb.append(' ');
			
			start = current;
			number = -1;
			while (current < to && data[current] > ' ' && data[current] != '\n') {
				if (data[current] == '#') {
					number = current;
				}
				current++;
			}
			if (number >= 0) {
				final long	id = imports.seekName(data,start,number);
				
				if (id >= 0) {
					sb.append("[[").append(imports.getCargo(id)).append('#').append(data,number+1,current-number-1)
					  .append('|').append(data,start,current-start).append("]]");
				}
				else {
					sb.append("[[").append(data,start,current-start).append('|').append(data,start,current-start).append("]]");
				}
			}
			else {
				final long	id = imports.seekName(data,start,current); 
				
				if (id >= 0) {
					sb.append("[[").append(imports.getCargo(id)).append('|').append(data,start,current-start).append("]]");
				}
				else {
					sb.append("[[").append(data,start,current-start).append('|').append(data,start,current-start).append("]]");
				}
			}
		}
	}	

	private static void resolveSingleReference(final char[] data, final int from, final int to, final SyntaxTreeInterface<char[]> imports, final StringBuilder sb) {
		int		start = from, current = start, number;
		
		while (current < to && data[current] <= ' ' && data[current] != '\n') {
			current++;
		}
		start = current;
		number = -1;
		while (current < to && data[current] > ' ' && data[current] != '\n') {
			if (data[current] == '#') {
				number = current;
			}
			current++;
		}
		if (number >= 0) {
			final long	id = imports.seekName(data,start,number);
			
			if (id >= 0) {
				sb.append(imports.getCargo(id)).append('#').append(data,number+1,current-number-1);
			}
			else {
				sb.append(data,start,current-start);
			}
		}
		else {
			final long	id = imports.seekName(data,start,current); 
			
			if (id >= 0) {
				sb.append(imports.getCargo(id));
			}
			else {
				sb.append(data,start,current-start);
			}
		}
	}	
	
	public static class MultilangContent {
		public final SupportedLanguages	lang;
		public final char[]				content;
		public final Reference[]		refs;
		
		public MultilangContent(SupportedLanguages lang, char[] content, Reference[] refs) {
			this.lang = lang;
			this.content = content;
			this.refs = refs;
		}

		@Override
		public String toString() {
			return "MultilangContent [lang=" + lang + ", content=" + Arrays.toString(content) + ", refs=" + Arrays.toString(refs) + "]";
		}
	}
	
	public static class Reference {
		public final int				row, col;
		public final ReferenceType		refType;
		public final URI				ref;
		
		public Reference(int row, int col, ReferenceType refType, URI ref) {
			this.row = row;
			this.col = col;
			this.refType = refType;
			this.ref = ref;
		}

		@Override
		public String toString() {
			return "Reference [row=" + row + ", col=" + col + ", refType=" + refType + ", ref=" + ref + "]";
		}
	}

	@FunctionalInterface
	interface ActionsCallback {
		void process(final StringBuilder sb, final char[] data, final int from, final int to, final SyntaxTreeInterface<char[]> imports);
	}
}
