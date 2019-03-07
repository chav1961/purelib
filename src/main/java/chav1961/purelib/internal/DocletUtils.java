package chav1961.purelib.internal;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.BitCharSet;
import chav1961.purelib.basic.LineByLineProcessor;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.i18n.AbstractLocalizer.SupportedLanguages;

public class DocletUtils {
	private static final SyntaxTreeInterface<ActionsCallback>	ACTIONS = new AndOrTree<>();
	private static final BitCharSet								STOPS = new BitCharSet('\n','\r','<','@','&');
	
	static {
		ACTIONS.placeName("lt",(sb, data, from, to)->{sb.append('<');});
		ACTIONS.placeName("gt",(sb, data, from, to)->{sb.append('>');});
		ACTIONS.placeName("nbsp",(sb, data, from, to)->{sb.append(' ');});
		ACTIONS.placeName("p",(sb, data, from, to)->{});
		ACTIONS.placeName("/p",(sb, data, from, to)->{sb.append("\n\n");});
		ACTIONS.placeName("b",(sb, data, from, to)->{sb.append("**");});
		ACTIONS.placeName("/b",(sb, data, from, to)->{sb.append("**");});
		ACTIONS.placeName("i",(sb, data, from, to)->{sb.append("//");});
		ACTIONS.placeName("/i",(sb, data, from, to)->{sb.append("//");});
		ACTIONS.placeName("li",(sb, data, from, to)->{sb.append("\n* ");});
		ACTIONS.placeName("/li",(sb, data, from, to)->{sb.append("\n");});
	}
	
	public enum ReferenceType {
		See_REF, Link_Ref, CodeSample_REF, Overview_REF
	}

	public static MultilangContent[] javadoc2Creole(final String content) throws IOException, SyntaxException {
		if (content == null || content.isEmpty()) {
			throw new IllegalArgumentException("Content can't be null");
		}
		else {
			final List<MultilangContent>	result = new ArrayList<>();
			final StringBuilder				sb = new StringBuilder();
			
			try(final LineByLineProcessor	proc = new LineByLineProcessor((displacement, lineNo, data, from, length)->{
															parseInternal(sb,lineNo,data,from,length,result);
															})) {
				proc.write(content.toCharArray(),0,content.length());
			}
			return result.toArray(new MultilangContent[result.size()]);
		}
	}
	
	static void parseInternal(final StringBuilder sb, final int lineNo, final char[] data, final int from, final int length, final List<MultilangContent> result) {
		final int	end = from + length;
		int			start = from, current = start;
		long		id;
		
		while (current < end) {
			while (!STOPS.contains(data[current])) {
				current++;
			}
			sb.append(data,start,current);
			start = current;
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
						ACTIONS.getCargo(id).process(sb,data,start+1,current);
					}
					current = start = current + 1;
					break;
				case '@'	:
					current++;
					if (data[current] == '{') {
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
							ACTIONS.getCargo(id).process(sb,data,start,current);
							current = start = current + 1;
						}
					}
					else {
						while (Character.isJavaIdentifierPart(data[current])) {
							current++;
						}
						id = ACTIONS.seekName(data,start+1,current);					
						if (id >= 0) {
							ACTIONS.getCargo(id).process(sb,data,current+1,end);
							return;
						}
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
							ACTIONS.getCargo(id).process(sb,data,start+1,current);
							current = start = current + 1;
						}
					}
					break;
				default : throw new UnsupportedOperationException(); 
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
	private interface ActionsCallback {
		void process(final StringBuilder sb, final char[] data, final int from, final int to);
	}
}
