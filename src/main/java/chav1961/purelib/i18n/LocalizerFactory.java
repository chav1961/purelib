package chav1961.purelib.i18n;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.text.JTextComponent;

import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.i18n.interfaces.LocaleSpecificTextSetter;
import chav1961.purelib.i18n.interfaces.Localizer;

/**
 * <p>This class contains a set of static methods to manipulate with the localizers.</p>
 * @see Localizer
 * @see LocaleResource
 * @see LocaleResourceLocation
 * @see LocaleSpecificTextSetter
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 * @lastUpdate 0.0.3
 */

public class LocalizerFactory {
	private static final Map<URI,Localizer>	cache = new ConcurrentHashMap<>();

	private LocalizerFactory() {
	}

	/**
	 * <p>Get localizer for the given localizer URI. Any localizers were registered thru SPI can be used. SPI service name is the name of {@linkplain Localizer} class</p>
	 * @param localizerUri resource to get localizer for. Must be any URI with the {@linkplain Localizer#LOCALIZER_SCHEME} scheme
	 * @return localizer built
	 * @throws NullPointerException when localizer URI is null
	 * @throws LocalizationException if any I/O errors were detected during getting localizer
	 */
	public static Localizer getLocalizer(final URI localizerUri) throws NullPointerException, LocalizationException {
		if (localizerUri == null) {
			throw new NullPointerException("Localizer URI can't be null"); 
		}
		else {
			Localizer	localizer;
			
			if ((localizer = cache.get(localizerUri)) != null) {	// Double checked locking!
				return localizer; 
			}
			else {
				for (Localizer item : ServiceLoader.load(Localizer.class)) {
					if (item.canServe(localizerUri)) {
						synchronized(cache) {
							if ((localizer = cache.get(localizerUri)) != null) {
								return localizer; 
							}
							else {
								try{localizer = item.newInstance(localizerUri);
									cache.put(localizerUri,localizer);
									return localizer;
								} catch (EnvironmentException e) {
									throw new LocalizationException("Error creating localizer instance for the URI ["+localizerUri+"]: "+e.getLocalizedMessage());
								}
							}
						}
					}
				}
				throw new LocalizationException("No any LocalizerService instances found for URI ["+localizerUri+"]");
			}
		}
	}
	
	/**
	 * <p>Build localizer instance for the given object instance. Object instance must be annotated with both {@linkplain LocaleResourceLocation} and
	 * {@linkplain LocaleResource} annotations (see annotation descriptions).</p> 
	 * @param instance object instance to build localizer for
	 * @return localizer build or null if no any items were marked by {@linkplain LocaleResource} annotations
	 * @throws NullPointerException if object instance was null
	 * @throws IllegalArgumentException when any errors in the annotations were detected
	 * @throws IOException when any I/O errors were detected on localizer building
	 * @throws LocalizationException when any errors in the annotations were detected
	 * @lastUpdate 0.0.3
	 */
	public static Localizer buildLocalizerForInstance(final Object instance) throws NullPointerException, IllegalArgumentException, IOException, LocalizationException {
		if (instance == null) {
			throw new NullPointerException("Instance to build for can't be null");
		}
		else {
			Class<?>	cl = instance.getClass();
			
			if (!cl.isAnnotationPresent(LocaleResourceLocation.class)) {
				return null;
			}
			else {
				final Localizer		localizer = getLocalizer(URI.create(cl.getAnnotation(LocaleResourceLocation.class).value()));
				final StringBuilder	sb = new StringBuilder();
				boolean				wereErrors = false;
				boolean				marked = false;
			
				while (cl != null) {
					for (Field f : cl.getDeclaredFields()) {
						if (f.isAnnotationPresent(LocaleResource.class)) {
							final LocaleResource	anno = f.getAnnotation(LocaleResource.class); 
							
							if (!anno.value().isEmpty() && !localizer.containsKey(anno.value())) {
								sb.append("field ["+f.getName()+"] - value reference ["+f.getAnnotation(LocaleResource.class).value()+"] is missing\n");
								wereErrors = true;
							}
							if (!localizer.containsKey(anno.tooltip())) {
								sb.append("field ["+f.getName()+"] - tooltip reference ["+f.getAnnotation(LocaleResource.class).tooltip()+"] is missing\n");
								wereErrors = true;
							}
							if (!JComponent.class.isAssignableFrom(f.getType()) && !LocaleSpecificTextSetter.class.isAssignableFrom(f.getType())) {
								sb.append("field ["+f.getName()+"] must be a JComponent instance (or it's child) or must implements "+LocaleSpecificTextSetter.class+" interface\n");
								wereErrors = true;
							}
							marked = true;
						}
					}
					cl = cl.getSuperclass();
				}
				if (wereErrors) {
					throw new LocalizationException("Errors for the ["+instance.getClass().getAnnotation(LocaleResourceLocation.class).value()+"] resources from the given instance:\n"+sb.toString()); 
				}
				else if (marked) {
					return localizer;
				}
				else {
					return null;
				}
			}
		}
	}

	/**
	 * <p>This lambda-styled interface describes callback for special processing of the data localized before assign the value to any target (for example, make automatic upper case for localization strings).</p>
	 * @param <T> Type of the object instance (see {@linkplain LocalizerFactory#buildLocalizerForInstance(Object)} for details)
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.2
	 */
	@FunctionalInterface
	public interface PostProcessCallback<T> {
		/**
		 * <p>Process localized string content before assigning it to target
		 * @param localizer current localizer instance
		 * @param instance current object instance
		 * @param f current object field to assign value to
		 * @param value value to be assigned
		 * @return modified value to be assigned. Return value parameter to skip processing 
		 */
		String process(Localizer localizer, T instance, Field f, String value);
	}
	
	/**
	 * <p>This lambda-styled interface describes callback for storing localized data thru {@linkplain LocaleSpecificTextSetter} interface.</p>
	 * @param <T> Type of the object instance (see {@linkplain LocalizerFactory#buildLocalizerForInstance(Object)} for details)
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.2
	 */
	@FunctionalInterface
	public interface FillLocalizedContentCallback<T> {
		/**
		 * <p>Fill the given field thru {@linkplain LocaleSpecificTextSetter} interface
		 * @param localizer current localizer
		 * @param instance object instance to fill it's field (see {@linkplain LocalizerFactory#buildLocalizerForInstance(Object)})
		 * @param f field description to fill 
		 * @param text value to fill text 
		 * @param tooltip value to fill tooltip 
		 * @param toFill field value to fill
		 * @param postprocess callback to modify localized string before assign it to target 
		 */
		void fill(Localizer localizer, T instance, Field f, String text, String tooltip, LocaleSpecificTextSetter toFill, PostProcessCallback<T> postprocess);
	}

	/**
	 * <p>Fill the fields in the object instance with the localized content</p> 
	 * @param <T> any sort of the class marked with the localization annotations ({@linkplain LocaleResource}, {@linkplain LocaleResourceLocation})
	 * @param fillFrom localizer to fill content from
	 * @param instance object instance to fill it's fields (see {@linkplain LocalizerFactory#buildLocalizerForInstance(Object)})
	 * @throws NullPointerException when any parameters are null
	 * @throws IOException when I/O exception was detected
	 * @throws LocalizationException when localization exception was detected
	 */
	public static <T> void fillLocalizedContent(final Localizer fillFrom, final T instance) throws NullPointerException, IOException, LocalizationException {
		fillLocalizedContent(fillFrom,instance,(localizer, inst, f, text, tooltip, toFill, postproc)->{toFill.setLocaleSpecificText(postproc.process(localizer, inst, f, text)); toFill.setLocaleSpecificToolTipText(postproc.process(localizer, inst, f, tooltip));});
	}

	/**
	 * <p>Fill the fields in the object instance with the localized content</p> 
	 * @param <T> any sort of the class marked with the localization annotations ({@linkplain LocaleResource}, {@linkplain LocaleResourceLocation})
	 * @param fillFrom localizer to fill content from
	 * @param instance object instance to fill it's fields (see {@linkplain LocalizerFactory#buildLocalizerForInstance(Object)})
	 * @param postprocess postporocess callback
	 * @throws NullPointerException when any parameters are null
	 * @throws IOException when I/O exception was detected
	 * @throws LocalizationException when localization exception was detected
	 */
	public static <T> void fillLocalizedContent(final Localizer fillFrom, final T instance, final PostProcessCallback<T> postprocess) throws NullPointerException, IOException, LocalizationException {
		fillLocalizedContent(fillFrom,instance,(localizer, inst, f, text, tooltip, toFill, postproc)->{toFill.setLocaleSpecificText(postproc.process(localizer, inst, f, text)); toFill.setLocaleSpecificToolTipText(postproc.process(localizer, inst, f, tooltip));},postprocess);
	}

	/**
	 * <p>Fill the fields in the object instance with the localized content</p> 
	 * @param <T> any sort of the class marked with the localization annotations ({@linkplain LocaleResource}, {@linkplain LocaleResourceLocation})
	 * @param fillFrom localizer to fill content from
	 * @param instance object instance to fill it's fields (see {@linkplain LocalizerFactory#buildLocalizerForInstance(Object)})
	 * @param callback filling {@linkplain LocaleSpecificTextSetter} callback
	 * @throws NullPointerException when any parameters are null
	 * @throws IOException when I/O exception was detected
	 * @throws LocalizationException when localization exception was detected
	 */
	public static <T> void fillLocalizedContent(final Localizer fillFrom, final T instance, final FillLocalizedContentCallback<T> callback) throws NullPointerException, IOException, LocalizationException {
		fillLocalizedContent(fillFrom,instance,callback,(localizer,inst,f,value)->{return value;});
	}

	/**
	 * <p>Fill the fields in the object instance with the localized content</p> 
	 * @param <T> any sort of the class marked with the localization annotations ({@linkplain LocaleResource}, {@linkplain LocaleResourceLocation})
	 * @param fillFrom localizer to fill content from
	 * @param instance object instance to fill it's fields (see {@linkplain LocalizerFactory#buildLocalizerForInstance(Object)})
	 * @param callback filling {@linkplain LocaleSpecificTextSetter} callback
	 * @param postprocess post-process callback
	 * @throws NullPointerException when any parameters are null
	 * @throws IOException when I/O exception was detected
	 * @throws LocalizationException when localization exception was detected
	 * @lastUpdate 0.0.3
	 */
	public static <T> void fillLocalizedContent(final Localizer fillFrom, final T instance, final FillLocalizedContentCallback<T> callback, final PostProcessCallback<T> postprocess) throws NullPointerException, IOException, LocalizationException {
		if (fillFrom == null) {
			throw new NullPointerException("Localizer to fill from can't be null");
		}
		else if (instance == null) {
			throw new NullPointerException("Instance to fill to can't be null");
		}
		else if (callback == null) {
			throw new NullPointerException("Callback can't be null");
		}
		else if (postprocess == null) {
			throw new NullPointerException("Postproces can't be null");
		}
		else {
			Class<?>	cl = instance.getClass();
			
			while (cl != null) {
				for (Field f : cl.getDeclaredFields()) {
					if (f.isAnnotationPresent(LocaleResource.class)) {
						final LocaleResource	anno = f.getAnnotation(LocaleResource.class);
						
						if (JComponent.class.isAssignableFrom(f.getType())) {
							f.setAccessible(true);
							try{final Object	field = f.get(instance); 
	
								if (field != null) {
									((JComponent)field).setToolTipText(postprocess.process(fillFrom, instance, f, fillFrom.getValue(anno.tooltip())));
									if (field instanceof JTextComponent) {
										((JTextComponent)field).setText(postprocess.process(fillFrom,instance,f,fillFrom.getValue(anno.value())));
									}
									if (field instanceof JLabel) {
										((JLabel)field).setText(postprocess.process(fillFrom,instance,f,fillFrom.getValue(anno.value())));
									}
									if ((field instanceof AbstractButton) && (!anno.value().isEmpty())){
										((AbstractButton)field).setText(postprocess.process(fillFrom,instance,f,fillFrom.getValue(anno.value())));
									}
								}
							} catch (IllegalArgumentException | IllegalAccessException e) {
								throw new LocalizationException("Class ["+cl+"] field ["+f+"]: error setting values ("+e.getLocalizedMessage()+")"); 
							}
						}
						else if (LocaleSpecificTextSetter.class.isAssignableFrom(f.getType())) {
							f.setAccessible(true);
							try{final Object	field = f.get(instance); 
								
								if (field != null) {
									callback.fill(fillFrom,instance,f,fillFrom.getValue(anno.value()),fillFrom.getValue(anno.tooltip()),(LocaleSpecificTextSetter)field,postprocess);
								}
							} catch (IllegalArgumentException | IllegalAccessException e) {
								throw new LocalizationException("Class ["+cl+"] field ["+f+"]: error setting values ("+e.getLocalizedMessage()+")"); 
							}
						}
						else {
							throw new LocalizationException("Class ["+cl+"] field ["+f+"] need be JComponent or it's child to use with the ["+LocaleResource.class.getSimpleName()+"] annotation"); 
						}
					}
				}
				cl = cl.getSuperclass();
			}
		}
	}
}
