package chav1961.purelib.i18n;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.text.JTextComponent;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.DefaultLocalizerProvider;
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
 * @last.update 0.0.7
 */

public final class LocalizerFactory {
	private static final Map<URI,LocalizerCacheItem>	cache = new ConcurrentHashMap<>();
	private static final Map<URI,URI>					aliases = new ConcurrentHashMap<>();

	private LocalizerFactory() {
	}

	/**
	 * <p>Test existence of localizer for the given URI</p>
	 * @param localizerUri localizer URI to test
	 * @return true if appropriative localized is accessible, false otherwise
	 * @throws NullPointerException if localizerUri is null
	 * @since 0.0.4
	 */
	public static boolean hasLocalizerFor(final URI localizerUri) throws NullPointerException {
		if (localizerUri == null) {
			throw new NullPointerException("Localizer URI can't be null"); 
		}
		else {
			for (Localizer item : ServiceLoader.load(Localizer.class)) {
				if (item.canServe(localizerUri)) {
					return true;
				}
			}
			return false;
		}
	}
	
	/**
	 * <p>Get localizer for the given localizer URI. Any localizers were registered thru SPI can be used. SPI service name is the name of {@linkplain Localizer} class</p>
	 * @param localizerUri resource to get localizer for. Must be any URI with the {@linkplain Localizer#LOCALIZER_SCHEME} scheme
	 * @return localizer built
	 * @throws NullPointerException when localizer URI is null
	 * @throws LocalizationException if any I/O errors were detected during getting localizer
	 */
	public static Localizer getLocalizer(final URI localizerUri) throws NullPointerException, LocalizationException {
		return getLocalizer(localizerUri, Thread.currentThread().getContextClassLoader());
	}

	/**
	 * <p>Get localizer for the given localizer URI. Any localizers were registered thru SPI can be used. SPI service name is the name of {@linkplain Localizer} class</p>
	 * @param localizerUri resource to get localizer for. Must be any URI with the {@linkplain Localizer#LOCALIZER_SCHEME} scheme
	 * @param loader class loader to seek service in. Can't be null
	 * @return localizer built
	 * @throws NullPointerException when localizer URI or class loader is null
	 * @throws LocalizationException if any I/O errors were detected during getting localizer
	 * @since 0.0.7
	 */
	public static Localizer getLocalizer(final URI localizerUri, final ClassLoader loader) throws NullPointerException, LocalizationException {
		if (localizerUri == null) {
			throw new NullPointerException("Localizer URI can't be null"); 
		}
		else if (loader == null) {
			throw new NullPointerException("Class loader can't be null"); 
		}
		else {
			Localizer			localizer;
			LocalizerCacheItem	localizerItem;
			URI					alias;
			
			if ((localizerItem = cache.get(localizerUri)) != null) {	// Double checked locking!
				return localizerItem.localizer; 
			}
			else if ((alias = aliases.get(localizerUri)) != null && (localizerItem = cache.get(alias)) != null) {	// Double checked locking!
				return localizerItem.localizer;
			}
			else {
				for (Localizer item : ServiceLoader.load(Localizer.class, loader)) {
					if (item.canServe(localizerUri)) {
						synchronized(cache) {
							if ((localizerItem = cache.get(localizerUri)) != null) {
								return localizerItem.localizer; 
							}
							else {
								try{localizer = item.newInstance(localizerUri);
									
									int		localizerHash = 0;
									for (String key : localizer.localKeys()) {
										localizerHash += key.hashCode();
									}

									for (Entry<URI, LocalizerCacheItem> entry : cache.entrySet()) {
										if (entry.getValue().localizerHash == localizerHash && deepCompare(entry.getValue().localizer,localizer)) {
											aliases.put(localizerUri,entry.getValue().uri);
											return entry.getValue().localizer;
										}
									}
									cache.put(localizerUri,localizerItem = new LocalizerCacheItem(localizerUri,localizer,localizerHash));
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
	 * @last.update 0.0.3
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
	 * @last.update 0.0.3
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

	/**
	 * <p>Gather all default localizers from application and build total localizers tree. Localizer of application module will be 
	 * {@linkplain Localizer#push(Localizer) pushed} into the three, all others will be {@linkplain Localizer#add(Localizer) added} to the tree root.
	 * To support this functionality, all the modules having {@linkplain DefaultLocalizerProvider} providers, must support SPI service for it.</p>
	 * @param root localizer's root. Can't be null. It's strongly recommended to use {@linkplain PureLibSettings#PURELIB_LOCALIZER} as root
	 * @param application any application class (contains public static void main(String[]) method). Can't be null
	 * @return localizers tree. Use this value to pass to all the application methods requires {@linkplain Localizer} as parameter
	 * @throws NullPointerException any parameter is null
	 * @throws IllegalArgumentException application class doen't contain public static void main(String[]) method
	 * @throws EnvironmentException there are more than one {@link DefaultLocalizerProvider} in some module(s)
	 * @since 0.0.7
	 */
	public static Localizer buildDefaultLocalizersTree(final Localizer root, final Object application) throws NullPointerException, IllegalArgumentException {
		if (root == null) {
			throw new NullPointerException("Root localizer can't be null"); 
		}
		else if (application == null) {
			throw new NullPointerException("Application instance can't be null"); 
		}
		else {
			try{
				application.getClass().getMethod("main", String[].class);
			} catch (NoSuchMethodException | SecurityException e) {
				throw new IllegalArgumentException("Application class must contain public static void main(String[]) method");
			}
			final URI	resource = URI.create(DefaultLocalizerProvider.LOCALIZER_PROVIDER_SCHEME+':'+DefaultLocalizerProvider.LOCALIZER_PROVIDER_SUBSCHEME_ANY+":/");
			Localizer	leaf = null;
			
			for (DefaultLocalizerProvider item : ServiceLoader.load(DefaultLocalizerProvider.class)) {
				if (item.canServe(resource)) {
					final DefaultLocalizerProvider	provider = item.newInstance(resource); 
					final Localizer					localizer = provider.getLocalizer();
					
					if (application.getClass().getModule().equals(provider.getModule())) {
						if (leaf == null) {
							root.push(localizer);
							leaf = localizer;
						}
						else {
							throw new EnvironmentException("Module ["+item.getModule().getName()+"] has more than the only DefaultLocalizerProvider"); 
						}
					}
					else {
						root.add(localizer);
					}
				}
			}
			return leaf != null ? leaf : root;
		}
	}
	
	private static boolean deepCompare(final Localizer left, final Localizer right) {
		final Set<String>	leftKeys = new HashSet<>(), rightKeys = new HashSet<>();

		for (String key : left.localKeys()) {
			leftKeys.add(key);
		}
		for (String key : right.localKeys()) {
			rightKeys.add(key);
		}
		return leftKeys.equals(rightKeys);
	}
	
	private static class LocalizerCacheItem {
		final URI			uri;
		final Localizer		localizer;
		final int			localizerHash;
		
		public LocalizerCacheItem(URI uri, Localizer localizer, int localizerHash) {
			this.uri = uri;
			this.localizer = localizer;
			this.localizerHash = localizerHash;
		}

		@Override
		public String toString() {
			return "LocalizerCacheItem [uri=" + uri + ", localizerHash=" + localizerHash + "]";
		}
	}
}
