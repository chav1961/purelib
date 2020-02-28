package chav1961.purelib.ui.swing;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
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
import chav1961.purelib.model.Constants;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.interfaces.RefreshMode;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;

class FormManagedUtils {
	static <T> RefreshMode seekAndCall(final T instance, final URI appPath) throws Exception {
		final String[]		parts = URI.create(appPath.getSchemeSpecificPart()).getPath().split("/");
		Class<?>			cl = instance.getClass();
		
		while (cl != null && parts.length >= 3) {
			for (Method m : cl.getDeclaredMethods()) {
				if (m.getParameterCount() == 0 && parts[2].startsWith(m.getName()+"()")) {
					m.setAccessible(true);
					
					try{if (m.getReturnType() == void.class) {
							m.invoke(instance);
							return RefreshMode.DEFAULT;
						}
						else if (RefreshMode.class.isAssignableFrom(m.getReturnType())) {
							return (RefreshMode)m.invoke(instance);
						}
						else {
							throw new IllegalArgumentException("Method ["+m+"] returns neither void nor RefreshMode type");
						}
					} catch (InvocationTargetException exc) {	// unwrap source exception
						final Throwable	t = exc.getTargetException(); 
						
						if (t instanceof Exception) {
							throw (Exception)t; 
						}
						else {
							throw exc;
						}
					}
				}
			}
			cl = cl.getSuperclass();
		}
		return RefreshMode.DEFAULT;
	}
	
	interface FormManagerParserCallback {
		void processActionButton(final ContentNodeMetadata metadata, final JButtonWithMeta button) throws ContentException;
		void processField(final ContentNodeMetadata metadata, final JLabel fieldLabel, final JComponent fieldComponent, final GetterAndSetter gas, final boolean isModifiable) throws ContentException;
	}
	
	static <T> void parseModel4Form(final LoggerFacade logger, final ContentMetadataInterface mdi, final Class<T> instanceClass, final JComponentMonitor monitor, final FormManagerParserCallback callback) {
		try(final LoggerFacade	trans = logger.transaction("parseModel")) {
			
			mdi.walkDown((mode,applicationPath,uiPath,node)->{
				if (mode == NodeEnterMode.ENTER) {
					if (node.getApplicationPath() != null){
						try{if(node.getApplicationPath().toString().contains(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_ACTION)) {
								final JButtonWithMeta		button = new JButtonWithMeta(node,monitor);
								
								button.setName(URIUtils.removeQueryFromURI(node.getUIPath()).toString());
								trans.message(Severity.trace,"Process button [%1$s]",node.getApplicationPath());
		
								button.setActionCommand(node.getApplicationPath().toString());
								callback.processActionButton(node,button);							
							}
							else if(node.getApplicationPath().toString().contains(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD)) {
								final JLabel			label = new JLabel();
								final FieldFormat		ff = node.getFormatAssociated();
								final JComponent 		field = SwingUtils.prepareRenderer(node, ff, monitor);
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
	
	interface MarkupParserCallback {
		void placePlainText(int x, int y, int width, int height, boolean bold, boolean italic, boolean caption, String content) throws ContentException;
		void placeSeparator(int x, int y, int width, int height) throws ContentException;
		void placeField(int x, int y, int width, int height, String componentName, String initialValue) throws ContentException;
		void pushContent(int x, int y, int width, int height, String caption) throws ContentException;
		void popContent() throws ContentException;
	}
	
	static int parseMarkup(final String content, final MarkupParserCallback callback) throws SyntaxException, IllegalArgumentException, NullPointerException {
		return parseMarkup(CharUtils.terminateAndConvert2CharArray(content,'\0'),0,callback);
	}

	private static int parseMarkup(final char[] content, int from, final MarkupParserCallback callback) throws SyntaxException, IllegalArgumentException, NullPointerException {
		int		start = from, row = 0, column = 0, displ, index, forName[] = new int[2], forLength[] = new int[1], forInitialValue[] = new int[2];
		boolean	newLine = true, bold = false, italic = false;

		try{		
loop:		for(;;) {
				while (content[from] <= ' ' && content[from] != '\n' && content[from] != '\0') {
					from++;
					column++;
				}
				
				switch (content[from]) {
					case '\0' 	:
						break loop;
					case '\r' 	:
						processLabel(content,start,from-1,column,row,bold,italic,false,callback);
						start = from + 1;
						break;
					case '\n' 	:
						processLabel(content,start,from-1,column,row,bold,italic,false,callback);
						newLine = true;
						bold = italic = false; 
						start = ++from;
						row++;
						column = 0;
						continue loop;
					case '=' 	:
						if (newLine) {
							while (content[from] == '=') {
								from++;
							}
							start = from;
							while (content[from] != '\r' && content[from] != '\n' && content[from] != '\0') {
								from++;
							}
							processLabel(content,start,from-1,column,row,true,false,true,callback);
						}
						else {
							from++;
						}
						break;
					case '-' 	:
						if (content[from+1] == '-' && content[from+2] == '-') {
							int separatorSize = 0;
							
							processLabel(content,start,from-1,column,row,bold,italic,false,callback);
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
						if (content[from+1] == '-' && content[rowCol2Displ(content,column,row+1,forName)] == '|') {
							processLabel(content,start,from-1,column,row,bold,italic,false,callback);
							start = from;
							from = cutAndParseNested(content,from,row,column,callback);
							column += (from - start);
						}
						break;
					case '|' 	:
						processLabel(content,start,from-1,column,row,bold,italic,false,callback);
						for (index = 0; content[displ = rowCol2Displ(content,column,row+index,forName)] == '|'; index++) {
							content[displ] = '|'; 
						}
						callback.placeSeparator(column,row,1,index);
						break;
					case '\\'	:
						if (content[from+1] != '\0') {
							from += 2;
						}
						break;
					case '*' 	:
						if (content[from+1] == '*') {
							processLabel(content,start,from-1,column,row,bold,italic,false,callback);
							bold = !bold;
							start = from += 2;
							column--;	// format doesn't change column position!
						}
						break;
					case '/' 	:
						if (content[from+1] == '*') {
							processLabel(content,start,from-1,column,row,bold,italic,false,callback);
							italic = !italic;
							start = from += 2;
							column--;	// format doesn't change column position!
						}
						break;
					case '$' 	:
						if (content[from+1] == '{') {	// ${name:length[='initial']}
							processLabel(content,start,from-1,column,row,bold,italic,false,callback);
							from = CharUtils.parseName(content,from+2,forName);
							if (content[from] == ':') {
								from = CharUtils.parseInt(content,from+1,forLength,true);
								if (content[from] == '=') {
									from++;
									if (content[from] == '\'') {
										from = CharUtils.parseUnescapedString(content,from+1,'\'',true,forInitialValue);
									}
									else {
										forInitialValue[0] = from;
										while (content[from] != '}' && content[from] != '\n' && content[from] != '\0') {
											from++;
										}
										forInitialValue[1] = from;
									}
									callback.placeField(column,row,forLength[0],1,new String(content,forName[0],forName[1]-forName[0]+1),new String(content,forName[0],forInitialValue[1]-forInitialValue[0]+1));
								}
								else {
									callback.placeField(column,row,forLength[0],1,new String(content,forName[0],forName[1]-forName[0]+1),null);
								}
								column += forLength[0];
							}
							else {
								throw new SyntaxException(SyntaxException.toRow(content,from),SyntaxException.toCol(content,from),"Missing ':' in field descriptor");  
							}
						}
						break;
					default 	:
				}
				newLine = false;
				from++;
			}
			if (from > start) {
				processLabel(content,start,from,column,row,bold,italic,false,callback);
			}
		} catch (ContentException e) {
			throw new SyntaxException(SyntaxException.toRow(content,from),SyntaxException.toCol(content,from),e.getLocalizedMessage(),e);
		}
		return from;
	}

	private static void processLabel(final char[] content, int from, int to, int x, final int y, final boolean bold, final boolean italic, final boolean caption, final MarkupParserCallback callback) throws ContentException {
		while (from < to && content[from] <= ' ') {	// Trim leading spaces 
			from++;
		}
		while (to >= from && content[to] <= ' ') {	// Trim trailing spaces 
			to--;
		}
		if (to >= from) {
			final int	width = to-from+1;
			
			callback.placePlainText(x,y,width,1,bold,italic,caption,new String(content,from,width));
		}
	}

	private static int cutAndParseNested(final char[] content, int from, final int row, final int column, final MarkupParserCallback callback) throws IllegalArgumentException, NullPointerException, ContentException {
		final int	left = from, right, top = row, bottom, temp[] = new int[2];
		
		from++;
		while (content[from] != '+' && content[from] != '\n' && content[from] != '\0') {
			from++;
		}
		if (content[from] == '+') {
			right = from;

			int	rowCount = row+1;
			
			while (content[rowCol2Displ(content,column,rowCount,temp)] == '|') {
				rowCount++;
			}
			if (content[rowCol2Displ(content,column,rowCount,temp)] == '+') {
				bottom = rowCount;
				
				int	arraySize = 1, ranges[][] = new int[top-bottom][], to = 0;
				
				for(int line = top; line <= bottom; line++) {
					ranges[line-top] = new int[] {rowCol2Displ(content,left,line,temp), rowCol2Displ(content,right,line,temp)};
					arraySize += ranges[line-top][1] - ranges[line-top][0] + 1; 
				}
				final char[]	extracted = new char[arraySize];
				
				for(int line = top; line <= bottom; line++) {
					int	fromPos = (ranges[line-top][1]-ranges[line-top][0])-(right-left);
					
					System.arraycopy(content,ranges[line-top][0],extracted,to,ranges[line-top][1]-ranges[line-top][0]);	// Extract content
					Arrays.fill(content,ranges[line-top][0],ranges[line-top][1],' ');									// Fill it by blanks
					System.arraycopy(content,ranges[line-top][1],content,fromPos,content.length-fromPos);				// Compact
					to += ranges[line-top][1]-ranges[line-top][0];
					extracted[to++] = '\n';		// Append NL
				}
				extracted[to] = '\0';
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
		int 	from = 0, row = 0, col = 0;
		boolean	newLine = true;
		
		while (row < y && content[from] != '\0') {	// Skip rows
			if (content[from] == '\n') {
				row++;
			}
			from++;
		}
		while (col < x && content[from] != '\n' && content[from] != '\0') {	// Skip columns
			switch (content[from]) {
				case '='	:
					if (newLine) {
						while (col < x && content[from] == '=') {
							from++;
						}
						while (col < x && content[from] != '\n' && content[from] != '\0') {
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
						while (col < x && content[from] != ':' && content[from] != '\n' && content[from] != '\0') {
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
									while (content[from] != '}' && content[from] != '\n' && content[from] != '\0') {
										from++;
									}
								}
							}
						}
					}
					break;
				default :
			}
			from++;
		}
		return from;
	}
}
