package chav1961.purelib.ui;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.enumerations.MarkupOutputFormat;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.streams.char2char.CreoleWriter;
import chav1961.purelib.ui.AbstractLowLevelFormFactory.FieldDescriptor;
import chav1961.purelib.ui.interfacers.Format;
import chav1961.purelib.ui.swing.SwingUtils;

/**
 * <p>This is utility class to support useful methods for UI.</p>  
 * @author Alexander Chernomyrdin aka chav1961
 * @see CreoleWriter 
 * @since 0.0.2
 */
public class UIUtils {
	private UIUtils(){
	}

	/**
	 * <p>Convert Creole-based character array to HTML string</p>
	 * @param source creole-based character array. Can't be null or empty array
	 * @return html converted
	 * @throws IllegalArgumentException when argument is null or empty array
	 * @throws IOException on any errors in the Creole content
	 */
	public static String cre2Html(final char[] source) throws IllegalArgumentException, IOException {
		if (source == null || source.length == 0) {
			throw new IllegalArgumentException("Source content can't be null or empty array"); 
		}
		else {
			try(final Reader			rdr = new CharArrayReader(source);
				final Writer			wr = new StringWriter()){
				try(final CreoleWriter	cwr = new CreoleWriter(wr,MarkupOutputFormat.XML2HTML)) {
				
					Utils.copyStream(rdr,cwr);
				}
				return wr.toString();
			}
		}
	}

	/**
	 * <p>Convert Creole-based string to HTML string</p>
	 * @param source creole-based string. Can't be null or empty
	 * @return html converted
	 * @throws IllegalArgumentException when argument is null or empty
	 * @throws IOException on any errors in the Creole content
	 */
	public static String cre2Html(final String source) throws IllegalArgumentException, IOException {
		if (source == null || source.isEmpty()) {
			throw new IllegalArgumentException("Source string can't be null or empty"); 
		}
		else {
			try(final Reader			rdr = new StringReader(source);
				final Writer			wr = new StringWriter()){
				try(final CreoleWriter	cwr = new CreoleWriter(wr,MarkupOutputFormat.XML2HTML)) {
				
					Utils.copyStream(rdr,cwr);
				}
				return wr.toString();
			}
		}
	}

	/**
	 * <p>Interface to create label for the {@linkplain UIUtils#collectFields(Localizer, Class, Object, List, CreateLabelCallback, CreateFieldCallback)} method call.
	 * Especially designed for using with the lambdas</p> 
	 * @param <LabelType> see {@linkplain UIUtils#collectFields(Localizer, Class, Object, List, CreateLabelCallback, CreateFieldCallback)} 
	 */
	@FunctionalInterface
	public interface CreateLabelCallback<LabelType> {
		/**
		 * <p>Create label by localizer and it's content id</p>
		 * @param localizer localizer to used with the label created
		 * @param valueId value id to use with the label created 
		 * @return label 
		 * @throws LocalizationException on any localization errors
		 * @throws ContentException on any other errors
		 */
		LabelType createLabel(Localizer localizer, String valueId) throws ContentException, LocalizationException;
	}

	/**
	 * <p>Interface to create field for the {@linkplain UIUtils#collectFields(Localizer, Class, Object, List, CreateLabelCallback, CreateFieldCallback)} method call.
	 * Especially designed for using with the lambdas</p> 
	 * @param <FieldType> see {@linkplain UIUtils#collectFields(Localizer, Class, Object, List, CreateLabelCallback, CreateFieldCallback)}
	 */
	@FunctionalInterface
	public interface CreateFieldCallback<FieldType> {
		FieldType createField(Localizer localizer, FieldDescriptor desc, String fieldTooltip, Object initialValue) throws ContentException, LocalizationException;
	}

	/**
	 * <p>Build editable field list by class definitions. All field marked with format annotations will be incuded in the list</p>
	 * @param <LabelType> type of label before field
	 * @param <FieldType> type of the field
	 * @param localizer Localizer to use for label building
	 * @param clazz class description to parse
	 * @param instance class instance to set initial values. If null, initial value settings will be skipped
	 * @param list list was build. If the parameter is not empty, new field will be appended into tail. No checking for duplicated names will be executed.
	 * @param labelConstructor constructor to create label for the given field
	 * @param fieldConstructor UI field instance to create
	 * @throws NullPointerException when any nulls (except instance) will be detected 
	 * @throws IllegalArgumentException when any errors were detected 
	 * @throws SyntaxException in any errors in the Format annotations
	 * @throws LocalizationException on any localization errors
	 * @throws ContentException on any other errors
	 */
	public static <LabelType,FieldType> void collectFields(final Localizer localizer, final Class<?> clazz, final Object instance
															, final List<LabelAndField<LabelType,FieldType>> list
															, final CreateLabelCallback<LabelType> labelConstructor
															, final CreateFieldCallback<FieldType> fieldConstructor) throws IllegalArgumentException, NullPointerException, SyntaxException, LocalizationException, ContentException {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (clazz == null) {
			throw new NullPointerException("Class to collect fields from can't be null");
		}
		else if (list == null) {
			throw new NullPointerException("List to collect field descriptors to can't be null");
		}
		else if (labelConstructor == null) {
			throw new NullPointerException("Label constructor can't be null");
		}
		else if (fieldConstructor == null) {
			throw new NullPointerException("Field constructor can't be null");
		}
		else {
			for (Field item : clazz.getDeclaredFields()) {
				if (item.isAnnotationPresent(LocaleResource.class)) {
					final LocaleResource	ann = item.getAnnotation(LocaleResource.class);
					
					if (!localizer.containsKey(ann.value())) {
						throw new LocalizationException("Class ["+clazz+"], field ["+item+"], annotation value key ["+ann.value()+"] is missing in the current localizer"); 
					}
					else if (!localizer.containsKey(ann.tooltip())) {
						throw new LocalizationException("Class ["+clazz+"], field ["+item+"], annotation tooltip key ["+ann.tooltip()+"] is missing in the current localizer"); 
					}
					else {
						final FieldDescriptor	desc = FieldDescriptor.newInstance(item.getName()
													,item.isAnnotationPresent(Format.class) 
															? new FormFieldFormat(item.getAnnotation(Format.class).value(),item.getAnnotation(Format.class).wizardType(),item.getAnnotation(Format.class).contentType())
															: new FormFieldFormat("")
													,clazz); 
						final FieldType			editor = fieldConstructor.createField(localizer,desc,ann.tooltip(),instance != null ? desc.getFieldValue(instance) : null); 
						final LabelAndField<LabelType,FieldType>	keyValue = new LabelAndField<>(
													labelConstructor.createLabel(localizer,ann.value())
													, ann.value(), ann.tooltip(),
													editor,desc
												);
						list.add(keyValue);
					}
				}
			}
			if (clazz != Object.class) {
				collectFields(localizer,clazz.getSuperclass(),instance,list,labelConstructor,fieldConstructor);
			}
		}
	}
}
