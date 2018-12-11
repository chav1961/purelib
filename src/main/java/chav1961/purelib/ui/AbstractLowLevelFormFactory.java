package chav1961.purelib.ui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Map;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.LineByLineProcessor;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.growablearrays.GrowableCharArray;
import chav1961.purelib.fsys.FileSystemURLStreamHandler;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.streams.char2char.CreoleWriter;
import chav1961.purelib.ui.interfacers.FieldRepresentation;
import chav1961.purelib.ui.interfacers.FormManager;
import chav1961.purelib.ui.interfacers.FormModel;
import chav1961.purelib.ui.interfacers.FormRepresentation;
import chav1961.purelib.ui.interfacers.Format;
import chav1961.purelib.ui.interfacers.Wizard;

/**
 * <p>This class is a template for the low-level form factories. It supports parsing and building of the form description, and allows
 * using them for the different UI representations in the different technologies (Swing, WEB etc).</p>
 * <p>Every form for the factory is a Creole-based markup file (see {@linkplain CreoleWriter}). In addition to standard Creole syntax, it contains:</p>
 * <ul>
 * <li>page description(s)</li>  
 * <li>field description(s)</li>  
 * </ul>
 * <p>Page description need be located before page content. It has following syntax:</p>
 * <code>&gt;&gt;pageId[:captionId[(iconURI)]][:tooltipId][:helpId]</code>
 * <ul>
 * <li>pageId - any Java-compatible identifier. Used in the inheritance mechanism (see below)</li>  
 * <li>captionId - any identifier. Used to fill page caption thru I18N localizer. Default value is <i>pageId</i></li>  
 * <li>iconURI - any valid URI to the icon content. Used for tabbed form as tab mark. Default value is <i>null</i></li>  
 * <li>tooltipId - any identifier. Used to show page tool tip thru I18N localizer. Default value is <i>pageId</i></li>  
 * <li>helpId - any identifier. Used to show page help thru I18N localizer. Default value is <i>pageId</i></li>  
 * </ul>
 * <p>Field description has following syntax:</p>
 * <code>&amp;fieldName:length[format];</code>
 * <ul>
 * <li>fieldName - any field name in the root class (see {@linkplain #AbstractLowLevelFormFactory(URI, FormRepresentation, Class, FormManager)} and {@linkplain #AbstractLowLevelFormFactory(URI, FormRepresentation, Class, FormManager)}). Qualified names need be typed as 'name.name'</li>  
 * <li>length - length of the field on the screen</li>  
 * <li>format - representation format for the given field</li>  
 * </ul>
 * <p>Representation format is unordered set of chars (see description of the {@linkplain FormFieldFormat} class)  
 * @author Alexander Chernomyrdin aka chav1961
 * @see CreoleWriter 
 * @see FormManager 
 * @see FormModel 
 * @since 0.0.2
 */

public abstract class AbstractLowLevelFormFactory<Id,Instance> {
	protected static final char					FIELD_MARK = (char)0x1E; 
	protected static final String				FIELD_MARK_STR = new String(new char[]{FIELD_MARK}); 
	
	private static final URI[]					EMPTY_INHERITANCE = new URI[0];
	private static final char[]					FILLER; 

	protected final FormRepresentation			representation;
	protected final FormPage[]					pages;
	protected final FieldDescriptor[]			fieldNames;
	protected final FormManager<Id,Instance>	formManager; 
	
	private final int[]							forBounds = new int[2];
	
	static {
		FILLER = new char[256];
		Arrays.fill(FILLER,'0');
	}
	
	public AbstractLowLevelFormFactory(final URI formDescription, final FormRepresentation representation, final Class<Instance> rootClass, final FormManager<Id,Instance> manager) throws IOException, SyntaxException {
		this(formDescription,EMPTY_INHERITANCE,representation,rootClass,manager);
	}
	
	public <T> AbstractLowLevelFormFactory(final URI formDescription, final URI[] inherited, final FormRepresentation representation, final Class<Instance> rootClass, final FormManager<Id,Instance> manager) throws IOException, SyntaxException {
		if (formDescription == null) {
			throw new NullPointerException("Form description can't be null");
		}
		else if (inherited == null) {
			throw new NullPointerException("Inherited list can't be null");
		}
		else if (representation == null) {
			throw new NullPointerException("Representation type can't be null");
		}
		else if (rootClass == null) {
			throw new NullPointerException("Root class can't be null");
		}
		else if (manager == null) {
			throw new NullPointerException("Form manager can't be null");
		}
		else {
			final List<FormPage>		temp = new ArrayList<>();
			final List<FieldDescriptor>	names = new ArrayList<>();
			
			for (int index = inherited.length-1; index >= 0; index--) {
				if (inherited[index] == null) {
					throw new NullPointerException("Inherited URI array contains null at index ["+index+"]");
				}
				else {
					parseURI(inherited[index],temp);
				}
			}
			parseURI(formDescription,temp);
			this.pages = temp.toArray(new FormPage[temp.size()]);
			
			for(FormPage item : pages) {
				for (NameDescriptor desc : item.fieldNames) {
					names.add(createFieldDesc(desc.name,desc.format,rootClass));
				}
			}
			this.fieldNames = names.toArray(new FieldDescriptor[names.size()]);
			this.representation = representation;
			this.formManager = manager;
		}
	}

	private void parseURI(final URI uri, final List<FormPage> content) throws IOException, SyntaxException {
		if (uri.getScheme().equals(FileSystemInterface.FILESYSTEM_URI_SCHEME)) {
			final URL			url = new URL(null,uri.toString(),new FileSystemURLStreamHandler());
			final URLConnection	conn = url.openConnection();
			
			try(final InputStream	is = conn.getInputStream()) {
				joinPages(content,parsePage(is));
			}
		}
		else {
			try(final InputStream	is = uri.toURL().openStream()) {
				joinPages(content,parsePage(is));
			}
		}
	}
	
	private List<FormPage> parsePage(final InputStream pageDesc) throws IOException, SyntaxException {
		final List<FormPage>	result = new ArrayList<>();
		final GrowableCharArray	gca = new GrowableCharArray(false);
		
		try(final Reader				rdr = new InputStreamReader(pageDesc,"UTF-8");
			final LineByLineProcessor	lblp = new LineByLineProcessor((lineNo,data,from,length)->{parsePage(lineNo,data,from,length,result,gca);})) {
			lblp.write(rdr);
			lblp.write("\n>>end".toCharArray(),0,"\n>>end".length());
		}
		return result;
	}

	private void parsePage(final int lineNo, final char[] data, int from, final int length, final List<FormPage> pages, final GrowableCharArray pageContent) throws IOException, SyntaxException {
		final int	to = from + length, start = from;
		
		if (data[from] == '>' && data[from+1] == '>') {	// >>partId[:[captionId]][(iconURI)][:[tooltipId]][:[helpId]]
			if (pages.size() > 0) {
				pages.get(pages.size()-1).content = pageContent.extract();
				pageContent.clear();
			}
			
			final FormPage	page = new FormPage();

			pages.add(page);
			from = CharUtils.parseName(data,from+2,forBounds);
			if (forBounds[0] == forBounds[1]) {
				throw new SyntaxException(lineNo,from-start,"Page name missing");
			}
			page.formName = new String(data,forBounds[0],forBounds[1]-forBounds[0]+1);
			if (data[from] == ':') {
				if (Character.isJavaIdentifierStart(data[from+1])) {
					from = CharUtils.parseName(data,from+1,forBounds);
					if (forBounds[1] > forBounds[0]) {
						page.captionId = new String(data,forBounds[0],forBounds[1]-forBounds[0]+1);
					}
					else {
						page.captionId = page.formName;
					}
				}
				else {
					page.captionId = page.formName;
					from++;
				}
				if (data[from] == '(') {
					from = CharUtils.parseName(data,from+1,forBounds);
					if (forBounds[1] == forBounds[0]) {
						throw new SyntaxException(lineNo,from-start,"Icon Id is empty");
					}
					else {
						if (data[from] == ')') {
							from++;
							page.iconId = URI.create(new String(data,forBounds[0],forBounds[1]-forBounds[0]+1));
						}
						else {
							throw new SyntaxException(lineNo,from-start,"Close bracket is missing");
						}
					}
				}
				if (data[from] == ':') {
					if (Character.isJavaIdentifierStart(data[from+1])) {
						from = CharUtils.parseName(data,from+1,forBounds);
						if (forBounds[1] > forBounds[0]) {
							page.tooltipId = new String(data,forBounds[0],forBounds[1]-forBounds[0]+1);
						}
						else {
							page.tooltipId = page.captionId;
						}
					}
					else {
						page.tooltipId = page.captionId;
						from++;
					}
					if (data[from] == ':') {
						from = CharUtils.parseName(data,from+1,forBounds);
						if (forBounds[1] > forBounds[0]) {
							page.helpId = new String(data,forBounds[0],forBounds[1]-forBounds[0]+1);
						}
						else {
							page.helpId = page.formName;
						}
					}
				}
			}
		}
		else {	// ... &field:<Len>rnpz; ...
			int		begin;
			
			for (begin = from; from < to; from++) {
				if (data[from] == '&') {
					if (from > begin) {
						pageContent.append(data,begin,from-1);
					}
					from = CharUtils.parseName(data,from+1,forBounds);
					if (forBounds[1] == forBounds[0]) {
						throw new SyntaxException(lineNo,from-start,"Name missing");
					}
					else {
						final NameDescriptor	name = new NameDescriptor();
						
						name.name = new String(data,forBounds[0],forBounds[1]-forBounds[0]+1);
						if (data[from] == ':') {
							final int	oldFrom = from+1;
							
							while (from < to && data[from] != ';') {
								from++;
							}
							
							name.format = new FormFieldFormat(data,oldFrom,from-oldFrom);
						}
						else {
							name.format = new FormFieldFormat();
						}
						if (data[from] == ';') {
							pages.get(pages.size()-1).fieldNames.add(name);
							pageContent.append(FIELD_MARK).append(FILLER,0,name.format.getLen());
							begin = ++from;
						}
						else {
							throw new SyntaxException(lineNo,from-start,"Semicolon is missing");
						}
					}
				}
			}
			pageContent.append(data,begin,from-1).append('\n');
		}
	}
	
	private void joinPages(final List<FormPage> totalPages, final List<FormPage> newPieceOfPages) {
		for (int index = newPieceOfPages.size()-1; index >= 0; index--) {	// Override
			for (int oldIndex = totalPages.size()-1; oldIndex >= 0; oldIndex--) {
				if (newPieceOfPages.get(index).formName.equals(totalPages.get(oldIndex).formName)) {
					totalPages.set(oldIndex,newPieceOfPages.remove(index));
					break;
				}
			}
		}
		for (int index = newPieceOfPages.size()-1; index >= 0; index--) {	// Prepend
			totalPages.add(0,newPieceOfPages.remove(index));
		}
		for (int index = totalPages.size()-1; index >= 0; index--) {		// Remove
			if (totalPages.get(index).content == null || totalPages.get(index).content.length == 0) {
				totalPages.remove(index);
			}
		}
	}

	private static FieldDescriptor createFieldDesc(final String item, final FormFieldFormat format, final Class<?> rootClass) throws SyntaxException {
		final int	prefixDot = item.indexOf('.'); 
		Field		f = null;
		
		try{
			if (prefixDot > 0) {
				final String			prefix = item.substring(0,prefixDot);
				
				if ((f = seekFieldDescription(prefix,rootClass)) == null) {
					throw new SyntaxException(0,0,"Unknown name ["+prefix+"] in the ["+rootClass+"] class");
				}
				else {
					final FieldDescriptor	desc = new FieldDescriptor(f);
					
					desc.fieldType = desc.field.getType();
					desc.fieldRepresentation = null;
					desc.fieldTooltip = null;
					desc.fieldFormat = null;
					desc.nested = createFieldDesc(item.substring(prefixDot+1),format,desc.fieldType);
					return desc;
				}			
			}
			else {
				if ((f = seekFieldDescription(item,rootClass)) == null) {
					throw new SyntaxException(0,0,"Unknown name ["+item+"] in the ["+rootClass+"] class");
				}
				else {
					final FieldDescriptor	desc = new FieldDescriptor(f);
					
					desc.fieldType = desc.field.getType();
					desc.fieldRepresentation = classifyFieldRepresentation(desc.field,desc.fieldType);
					desc.fieldFormat = format;
					desc.fieldTemplate = prepareFieldTemplate(desc.field,desc.fieldRepresentation,format,desc.fieldType);
					desc.fieldTooltip = prepareFieldTooltip(desc.field,format,desc.fieldType);
					desc.nested = null;
					return desc;
				}			
			}
		} catch (IllegalAccessException exc) {
			throw new SyntaxException(0,0,f == null ? "Unknown field in the ["+rootClass+"] class to get access from" : "Field ["+f+"] in the ["+rootClass+"] class can't get access thru reflection mechanism");
		}
	}

	private static FieldRepresentation classifyFieldRepresentation(final Field item, final Class<?> fieldType) {
		if (item.isAnnotationPresent(Wizard.class) || File.class.isAssignableFrom(fieldType)) {
			return FieldRepresentation.WIZARDVALUE;
		}
		else if (boolean.class.isAssignableFrom(fieldType) || Boolean.class.isAssignableFrom(fieldType)) {
			return FieldRepresentation.BOOLVALUE;
		}
		else if (byte.class.isAssignableFrom(fieldType) || Byte.class.isAssignableFrom(fieldType) || short.class.isAssignableFrom(fieldType) || Short.class.isAssignableFrom(fieldType) || int.class.isAssignableFrom(fieldType) || Integer.class.isAssignableFrom(fieldType) || long.class.isAssignableFrom(fieldType) || Long.class.isAssignableFrom(fieldType) || BigInteger.class.isAssignableFrom(fieldType)) {
			return FieldRepresentation.INTVALUE;
		}
		else if (float.class.isAssignableFrom(fieldType) || Float.class.isAssignableFrom(fieldType) || double.class.isAssignableFrom(fieldType) || Double.class.isAssignableFrom(fieldType) || BigDecimal.class.isAssignableFrom(fieldType)) {
			return FieldRepresentation.REALVALUE;
		}
		else if (Currency.class.isAssignableFrom(fieldType)) {
			return FieldRepresentation.CURRENCYVALUE;
		}
		else if (Date.class.isAssignableFrom(fieldType) || Time.class.isAssignableFrom(fieldType) || Timestamp.class.isAssignableFrom(fieldType) || Calendar.class.isAssignableFrom(fieldType)) {
			return FieldRepresentation.DATEVALUE;
		}
		else if (String.class.isAssignableFrom(fieldType)) {
			return FieldRepresentation.TEXTVALUE;
		}
		else if (char[].class.isAssignableFrom(fieldType)) {
			return FieldRepresentation.PASSWDVALUE;
		}
		else if (Enum.class.isAssignableFrom(fieldType)) {
			return FieldRepresentation.DDLISTVALUE;
		}
		else if (Collection.class.isAssignableFrom(fieldType) || fieldType.isArray()) {
			return FieldRepresentation.LISTVALUE;
		}
		else if (Map.class.isAssignableFrom(fieldType)) {
			return FieldRepresentation.MAPVALUE;
		}
		else if (item.isAnnotationPresent(Format.class)) {
			if (Collection.class.isAssignableFrom(fieldType) || fieldType.isArray()) {
				return FieldRepresentation.LISTVALUE;
			}
			else if (Enum.class.isAssignableFrom(fieldType)) {
				return FieldRepresentation.DDLISTVALUE;
			}
			else {
				return FieldRepresentation.FORMATTEDTEXTVALUE;
			}
		}
		else {
			return FieldRepresentation.KEYVALUEPAIR;			
		}
	}

	private static String prepareFieldTemplate(final Field item, final FieldRepresentation fr, final FormFieldFormat format, final Class<?> fieldType) {
		switch (fr) {
			case BOOLVALUE			: return null;
			case INTVALUE			: return "#0"; 
			case REALVALUE			: return "#0.0#";
			case CURRENCYVALUE		: return null;
			case DATEVALUE			: return null; 
			case TEXTVALUE			: return null;
			case FORMATTEDTEXTVALUE	: return item.isAnnotationPresent(Format.class) ? item.getAnnotation(Format.class).value() : null;
			case PASSWDVALUE		: return null;
			case DDLISTVALUE		: return item.isAnnotationPresent(Format.class) ? item.getAnnotation(Format.class).value() : null;
			case LISTVALUE			: return item.isAnnotationPresent(Format.class) ? item.getAnnotation(Format.class).value() : null;
			case MAPVALUE			: return item.isAnnotationPresent(Format.class) ? item.getAnnotation(Format.class).value() : null;
			case WIZARDVALUE		: return item.isAnnotationPresent(Wizard.class) ? item.getAnnotation(Wizard.class).value() : null;
			case KEYVALUEPAIR		: return null;
			default : throw new UnsupportedOperationException("Field representation ["+fr+"] is not supported yet");
		}
	}

	private static String prepareFieldTooltip(final Field item, final FormFieldFormat format, final Class<?> fieldType) {
		if (item.isAnnotationPresent(LocaleResource.class)) {
			return item.getAnnotation(LocaleResource.class).tooltip();
		}
		else {
			return item.getName();
		}
	}
	
	private static Field seekFieldDescription(final String field, final Class<?> rootClass) {
		for (Field f : rootClass.getDeclaredFields()) {
			if (f.getName().equals(field)) {
				return f;
			}
		}
		if (rootClass.getSuperclass() != null) {
			return seekFieldDescription(field,rootClass.getSuperclass());
		}
		else {
			return null;
		}
	}

	protected static class NameDescriptor {
		public String				name;
		public FormFieldFormat		format;
	}

	public static class FieldDescriptor {
		public final Field			field;
		
		public FieldDescriptor		nested;
		public Class<?>				fieldType;
		public FieldRepresentation	fieldRepresentation;
		public int					fieldLen;
		public FormFieldFormat		fieldFormat;
		public String				fieldTemplate;
		public String				fieldTooltip;
		
		private final MethodHandle	getter;
		private final MethodHandle	setter;
		
		FieldDescriptor(final Field field) throws IllegalAccessException {
			this.field = field;
			this.field.setAccessible(true);
			this.getter = MethodHandles.lookup().unreflectGetter(this.field); 
			this.setter = MethodHandles.lookup().unreflectSetter(this.field); 
		}
		
		public Object getFieldValue(Object instance) throws ContentException {
			try{return getter.invoke(instance);
			} catch (Throwable e) {
				throw new ContentException(e.getLocalizedMessage(),e); 
			}
		}

		public void setFieldValue(Object instance, Object value) throws ContentException {
			try{setter.invoke(instance,value);
			} catch (Throwable e) {
				throw new ContentException(e.getLocalizedMessage(),e); 
			}
		}
		
		public String extractLocalizedFieldName(final Localizer localizer) throws LocalizationException {
			if (field.isAnnotationPresent(LocaleResource.class)) {
				return localizer.getValue(field.getAnnotation(LocaleResource.class).value());
			}
			else if (localizer.containsKey(field.getName())) {
				return localizer.getValue(field.getName());
			}
			else {
				return field.getName();
			}
		}

		public String extractLocalizedFieldTooltip(final Localizer localizer) throws LocalizationException {
			if (fieldTooltip != null && !fieldTooltip.isEmpty()) {
				return localizer.getValue(fieldTooltip);
			}
			else if (field.isAnnotationPresent(LocaleResource.class)) {
				return localizer.getValue(field.getAnnotation(LocaleResource.class).tooltip());
			}
			else if (localizer.containsKey(field.getName())) {
				return localizer.getValue(field.getName());
			}
			else {
				return field.getName();
			}
		}
		
		@Override
		public String toString() {
			if (nested != null) {
				return "FieldDescriptor [prefix=" + field + ", nested="+nested +"]";
			}
			else {
				return "FieldDescriptor [fieldName=" + field + ", fieldType=" + fieldType
						+ ", fieldRepresentation=" + fieldRepresentation + ", fieldLen=" + fieldLen + ", fieldFormat="
						+ fieldFormat + ", fieldTemplate=" + fieldTemplate + ", fieldTooltip=" + fieldTooltip + "]";
			}
		}
		
		public static FieldDescriptor newInstance(final String item, final FormFieldFormat format, final Class<?> rootClass) throws IllegalArgumentException, NullPointerException, SyntaxException {
			if (item == null || item.isEmpty()) {
				throw new IllegalArgumentException("Field item can't be null or empty"); 
			}
			else if (format == null) {
				throw new NullPointerException("Field format can't be null"); 
			}
			else if (rootClass == null) {
				throw new NullPointerException("Root class can't be null"); 
			}
			else {
				return createFieldDesc(item,format,rootClass);
			}
		}
	}
	
	public static class FormPage {
		public String				formName;
		public String				captionId;
		public URI					iconId;
		public String				tooltipId;
		public String				helpId;
		public List<NameDescriptor>	fieldNames = new ArrayList<>();
		public char[]				content;
		
		FormPage(){}
		
		@Override
		public String toString() {
			return "FormPage [formName=" + formName + ", captionId=" + captionId + ", iconId=" + iconId + ", tooltipId="
					+ tooltipId + ", helpId=" + helpId + ", fieldNames=" + fieldNames + ", content="
					+ (content == null ? "null" : new String(content)) + "]";
		}
	}
}
