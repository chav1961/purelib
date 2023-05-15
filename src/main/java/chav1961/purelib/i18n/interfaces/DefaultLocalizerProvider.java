package chav1961.purelib.i18n.interfaces;

import chav1961.purelib.basic.interfaces.SpiService;

/**
 * <p>This interface describes SPI services to get default localizers from the application module(s)/project(s).</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.7
 */
public interface DefaultLocalizerProvider extends LocalizerOwner, SpiService<DefaultLocalizerProvider> {
	/**
	 * <p>Scheme name for the localizer providers</p>
	 */
	public static final String	LOCALIZER_PROVIDER_SCHEME = "i18nProvider";

	/**
	 * <p>Sub-scheme to find any localizer provider</p>
	 */
	public static final String	LOCALIZER_PROVIDER_SUBSCHEME_ANY = "any";
	
	/**
	 * <p>Get provider module description</p>
	 * @return provider module description. Can't be null
	 */
	public Module getModule();
}
