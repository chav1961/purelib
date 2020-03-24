package chav1961.purelib.ui.swing;

import java.util.Arrays;

import javax.swing.JComponent;
import javax.swing.JLabel;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.GettersAndSettersFactory;
import chav1961.purelib.basic.GettersAndSettersFactory.GetterAndSetter;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.model.Constants;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;

public class FormManagedUtils {
	private static final char	EOF_MARKUP = '\0';
	private static final char	SPLITTER_MARKUP = '\1';
	
	public interface FormManagerParserCallback {
		void processActionButton(final ContentNodeMetadata metadata, final JButtonWithMeta button) throws ContentException;
		void processField(final ContentNodeMetadata metadata, final JLabel fieldLabel, final JComponent fieldComponent, final GetterAndSetter gas, final boolean isModifiable) throws ContentException;
	}
	
	public static <T> void parseModel4Form(final LoggerFacade logger, final ContentMetadataInterface mdi, final Localizer localizer, final Class<T> instanceClass, final JComponentMonitor monitor, final FormManagerParserCallback callback) {
		try(final LoggerFacade		trans = logger.transaction("parseModel")) {
			
			mdi.walkDown((mode,applicationPath,uiPath,node)->{
				if (mode == NodeEnterMode.ENTER) {
					if (node.getApplicationPath() != null){
						try(final Localizer	currentLocalizer = localizer.push(node.getLocalizerAssociated())) {
							
							if(node.getApplicationPath().toString().contains(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_ACTION)) {
								final JButtonWithMeta		button = new JButtonWithMeta(node,currentLocalizer,monitor);
								
								button.setName(URIUtils.removeQueryFromURI(node.getUIPath()).toString());
								trans.message(Severity.trace,"Process button [%1$s]",node.getApplicationPath());
		
								button.setActionCommand(node.getApplicationPath().toString());
								callback.processActionButton(node,button);							
							}
							else if(node.getApplicationPath().toString().contains(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD)) {
								final JLabel			label = new JLabel();
								final FieldFormat		ff = node.getFormatAssociated();
								final JComponent 		field = SwingUtils.prepareRenderer(node, currentLocalizer, ff.getContentType(), monitor);
								final GetterAndSetter	gas = GettersAndSettersFactory.buildGetterAndSetter(instanceClass,node.getName());
							
								label.setName(URIUtils.removeQueryFromURI(node.getUIPath()).toString()+"/label");
								field.setName(URIUtils.removeQueryFromURI(node.getUIPath()).toString());
								trans.message(Severity.trace,"Process control [%1$s] type [%2$s]",node.getUIPath(),field.getClass().getCanonicalName());

								callback.processField(node,label,field,gas,!ff.isReadOnly(false) && !ff.isReadOnly(true));
							}
						} catch (LocalizationException | ContentException exc) {
							logger.message(Severity.error,exc,"Control [%1$s]: processing error %2$s",node.getApplicationPath(),exc.getLocalizedMessage());
						}
					} 
				}
				return ContinueMode.CONTINUE;
			}, mdi.getRoot().getUIPath());
			
			trans.rollback();	// All ok, remove trace from logger			
		}
	}
	
	public interface MarkupParserCallback {
		void placePlainText(int x, int y, int width, int height, boolean bold, boolean italic, boolean caption, String content) throws ContentException;
		void placeSeparator(int x, int y, int width, int height) throws ContentException;
		void placeField(int x, int y, int width, int height, String componentName, String initialValue) throws ContentException;
		void pushContent(int x, int y, int width, int height, String caption) throws ContentException;
		void popContent() throws ContentException;
	}
	
	public static int parseMarkup(final String content, final MarkupParserCallback callback) throws SyntaxException, IllegalArgumentException, NullPointerException {
		return parseMarkup(CharUtils.terminateAndConvert2CharArray(content,EOF_MARKUP),0,callback);
	}

	private static int parseMarkup(final char[] content, int from, final MarkupParserCallback callback) throws SyntaxException, IllegalArgumentException, NullPointerException {
		int		start = from, row = 1, column = 1, escapes = 0, displ, index, forName[] = new int[2], forLength[] = new int[1], forInitialValue[] = new int[2];
		boolean	newLine = true, bold = false, italic = false;

		try{		
loop:		while(from < content.length) {
				switch (content[from]) {
					case EOF_MARKUP 		:
						break loop;
					case SPLITTER_MARKUP	:
						processLabel(content,start,from-1,column,row,escapes > 0,bold,italic,false,callback);
						column += from - start + 1 - escapes;
						start = from + 1;
						escapes = 0;
						break;
					case '\r' 	:
						processLabel(content,start,from-1,column,row,escapes > 0,bold,italic,false,callback);
						bold = italic = false; 
						start = from + 1;
						break;
					case '\n' 	:
						processLabel(content,start,from-1,column,row,escapes > 0,bold,italic,false,callback);
						newLine = true;
						bold = italic = false; 
						start = ++from;
						row++;
						column = 1;
						escapes = 0;
						continue loop;
					case '=' 	:
						if (newLine) {
							while (content[from] == '=') {
								from++;
							}
							start = from;
							while (content[from] != '\r' && content[from] != '\n' && content[from] != EOF_MARKUP) {
								from++;
							}
							processLabel(content,start,from-1,column,row,escapes > 0,true,false,true,callback);
							while (content[from] == '\r' || content[from] == '\n') {
								from++;
							}
							start = from;
							row++;
							column = 1;
						}
						else {
							from++;
						}
						break;
					case '-' 	:
						if (content[from+1] == '-' && content[from+2] == '-') {
							int separatorSize = 0;
							
							processLabel(content,start,from-1,column,row,escapes > 0,bold,italic,false,callback);
							column += from - start - escapes;
							escapes = 0;
							
							while (content[from] == '-') {
								from++;
								separatorSize++;
							}
							
							callback.placeSeparator(column,row,separatorSize,1);
							column += separatorSize;
							start = from;
						}
						else {
							from++;
						}
						break;
					case '+' 	:	// Nested component - extract it's content and parse it
						if (content[from+1] == '-' && content[from+2] == '-') {
							processLabel(content,start,from-1,column,row,escapes > 0,bold,italic,false,callback);
							column += from - start - escapes;
							start = from;
							escapes = 0;
							from = cutAndParseNested(content,from,row,column,callback);
						}
						break;
					case '|' 	:
						processLabel(content,start,from-1,column,row,escapes > 0,bold,italic,false,callback);
						column += from - start - escapes;
						escapes = 0;
						
						for (index = 0; content[displ = rowCol2Displ(content,column,row+index,forName)] == '|'; index++) {
							content[displ] = SPLITTER_MARKUP; 
						}
						callback.placeSeparator(column,row,1,index);
						start = from + 1;
						column++;
						break;
					case '\\'	:
						if (content[from+1] != EOF_MARKUP) {
							from++;
							escapes++;
						}
						break;
					case '*' 	:
						if (content[from+1] == '*') {
							processLabel(content,start,from-1,column,row,escapes > 0,bold,italic,false,callback);
							bold = !bold;
							column += from - start - escapes;
							escapes = 0;
							start = from++ + 2;
						}
						break;
					case '/' 	:
						if (content[from+1] == '/') {
							processLabel(content,start,from-1,column,row,escapes > 0,bold,italic,false,callback);
							italic = !italic;
							column += from - start - escapes;
							escapes = 0;
							start = from++ + 2;
						}
						break;
					case '$' 	:
						if (content[from+1] == '{') {	// ${name:length[='initial']}
							processLabel(content,start,from-1,column,row,escapes > 0,bold,italic,false,callback);
							column += from - start - escapes;
							escapes = 0;
							if (Character.isJavaIdentifierStart(content[from+2])) {
								from = CharUtils.parseName(content,from+2,forName);
								if (content[from] == ':') {
									from = CharUtils.parseInt(content,from+1,forLength,true);
									if (content[from] == '=') {
										from++;
										if (content[from] == '\'') {
											try{from = CharUtils.parseUnescapedString(content,from+1,'\'',true,forInitialValue);
											} catch (IllegalArgumentException exc) {
												throw new SyntaxException(SyntaxException.toRow(content,from),SyntaxException.toCol(content,from),"Illegall string format or string is not terminated with (')");
											}
										}
										else {
											forInitialValue[0] = from;
											while (content[from] != '}' && content[from] != '\n' && content[from] != EOF_MARKUP) {
												from++;
											}
											forInitialValue[1] = from-1;
										}
										if (content[from] == '}') {
											callback.placeField(column,row,forLength[0],1,new String(content,forName[0],forName[1]-forName[0]+1),new String(content,forInitialValue[0],forInitialValue[1]-forInitialValue[0]+1));
											from++;
										}
										else {
											throw new SyntaxException(SyntaxException.toRow(content,from),SyntaxException.toCol(content,from),"Missing '}' in field descriptor");
										}
									}
									else if (content[from] == '}') {
										callback.placeField(column,row,forLength[0],1,new String(content,forName[0],forName[1]-forName[0]+1),null);
										from++;
									}
									else {
										throw new SyntaxException(SyntaxException.toRow(content,from),SyntaxException.toCol(content,from),"Missing '}' in field descriptor");
									}
									start = from;
									column += forLength[0];
								}
								else {
									throw new SyntaxException(SyntaxException.toRow(content,from),SyntaxException.toCol(content,from),"Missing ':' in field descriptor");  
								}
							}
							else {
								throw new SyntaxException(SyntaxException.toRow(content,from),SyntaxException.toCol(content,from),"Missing field name in field descriptor");  
							}
						}
						break;
					default 	:
				}
				newLine = false;
				from++;
			}
			if (from > start) {
				processLabel(content,start,Math.min(from,content.length-1),column,row,escapes > 0,bold,italic,false,callback);
			}
		} catch (ContentException e) {
			throw new SyntaxException(SyntaxException.toRow(content,from),SyntaxException.toCol(content,from),e.getLocalizedMessage(),e);
		}
		return from;
	}

	private static void processLabel(final char[] content, int from, int to, int x, final int y, final boolean escapesDetected, final boolean bold, final boolean italic, final boolean caption, final MarkupParserCallback callback) throws ContentException {
		while (from < to && content[from] <= ' ') {	// Trim leading spaces 
			from++;
			x++;
		}
		while (to >= from && content[to] <= ' ') {	// Trim trailing spaces 
			to--;
		}
		if (to >= from) {
			final int	width = to-from+1;
			
			if (escapesDetected) {					// Remove escaped sequences from the string
				final StringBuilder	sb = new StringBuilder();
				
				for (int index = 0; index < width; index++) {
					if (content[from+index] == '\\') {
						sb.append(content[from+index+1]);
						index++;
					}
					else {
						sb.append(content[from+index]);
					}
				}
				callback.placePlainText(x,y,sb.length(),1,bold,italic,caption,sb.toString());
			}
			else {
				callback.placePlainText(x,y,width,1,bold,italic,caption,new String(content,from,width));
			}
		}
	}

	private static int cutAndParseNested(final char[] content, int from, final int row, final int column, final MarkupParserCallback callback) throws IllegalArgumentException, NullPointerException, ContentException {
		final int	left = from, right, top = row, bottom, temp[] = new int[2];
		
		from++;
		while (content[from] != '+' && content[from] != '\n' && content[from] != EOF_MARKUP) {
			from++;
		}
		if (content[from] == '+') {
			right = from+1;

			int	rowCount = row+1;
			
			while (content[rowCol2Displ(content,column,rowCount,temp)] == '|') {
				rowCount++;
			}
			if (content[rowCol2Displ(content,column,rowCount,temp)] == '+' && content[rowCol2Displ(content,column+(right-left)-1,rowCount,temp)] == '+') {
				bottom = rowCount;
				
				int	arraySize = 1, ranges[][] = new int[bottom-top+1][], to = 0;
				
				for(int line = top; line <= bottom; line++) {
					ranges[line-top] = new int[] {rowCol2Displ(content,column,line,temp), rowCol2Displ(content,column+(right-left),line,temp)};
					arraySize += ranges[line-top][1] - ranges[line-top][0] + 1; 
				}
				final char[]	extracted = new char[arraySize-2*(right-left)-2*(bottom-top)];
				
				for(int line = top+1; line < bottom; line++) {	// Escape bounds...
					System.arraycopy(content,ranges[line-top][0]+1,extracted,to,ranges[line-top][1]-ranges[line-top][0]-3);	// Extract content
					to += ranges[line-top][1]-ranges[line-top][0]-3;
					extracted[to++] = '\n';		// Append NL
				}
				extracted[to] = EOF_MARKUP;

				for(int line = top; line <= bottom; line++) {
					Arrays.fill(content,ranges[line-top][0],ranges[line-top][1],SPLITTER_MARKUP);	// Fill content by blanks
				}

				int 	displ = 0, delta;
				
				for(int line = top; line <= bottom; line++) {
					if (ranges[line-top][1] - ranges[line-top][0] > right - left) {
						delta = (ranges[line-top][1] - ranges[line-top][0]) - (right - left);
						System.arraycopy(content,ranges[line-top][0]+displ+delta,content,ranges[line-top][0]+displ,content.length-ranges[line-top][0]-displ-delta);	// Compact content
						displ += delta;
					}
				}
				
				callback.pushContent(left,top,right-left,bottom-top,"");
				parseMarkup(extracted,0,callback);
				callback.popContent();
			}
			else {
				throw new SyntaxException(SyntaxException.toRow(content,from),SyntaxException.toCol(content,from),"Missing bottom '+' in border descriptor");  
			}
		}
		else {
			throw new SyntaxException(SyntaxException.toRow(content,from),SyntaxException.toCol(content,from),"Missing right '+' in border descriptor");  
		}
		return from;
	}
	
	private static int rowCol2Displ(final char[] content, final int x, final int y, final int[] temp) throws SyntaxException {
		int 	from = 0, row = 1, col = 1;
		boolean	newLine = true;
		
		while (row < y && content[from] != EOF_MARKUP) {	// Skip rows
			if (content[from] == '\n') {
				row++;
			}
			from++;
		}
		while (col < x && content[from] != '\n' && content[from] != EOF_MARKUP) {	// Skip columns
			switch (content[from]) {
				case '='	:
					if (newLine) {
						while (col < x && content[from] == '=') {
							from++;
						}
						while (col < x && content[from] != '\n' && content[from] != EOF_MARKUP) {
							from++;
						}
						if (col == x) {
							return from;
						}
						else {
							return content.length-1;
						}
					}
					newLine = false;
					break;
				case '*'	:
					if (content[from+1] == '*') {
						col -= 2;
						from++;
					}
					break;
				case '/'	:
					if (content[from+1] == '/') {
						col -= 2;
						from++;
					}
					break;
				case '\\'	:
					from++;
					break;
				case '$' 	:
					if (content[from+1] == '{') {	// ${name:length[='initial']}
						while (col < x && content[from] != ':' && content[from] != '\n' && content[from] != EOF_MARKUP) {
							from++;
						}
						if (content[from] == ':') {
							from = CharUtils.parseInt(content,from+1,temp,true);
							
							if (col + temp[0] >= x) {
								return from;
							}
							else {
								col += temp[0];
							}
							if (content[from] == '=') {
								if (content[from+1] == '\'') {
									from = CharUtils.parseUnescapedString(content,from+2,'\'',true,temp); 
								}
								else {
									while (content[from] != '}' && content[from] != '\n' && content[from] != EOF_MARKUP) {
										from++;
									}
								}
							}
						}
					}
					break;
				default :
					col++;
			}
			from++;
		}
		return from;
	}
}
