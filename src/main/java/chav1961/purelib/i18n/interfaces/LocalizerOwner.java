package chav1961.purelib.i18n.interfaces;

/**
 * <p>This interface describes owner of the localizer. Any class that has localizer inside, 
 * can use this interface to provide access to it.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.6
 */
public interface LocalizerOwner {
	/**
	 * <p>Get localizer owned.</p>
	 * @return localzier owned. Can't be null.
	 */
	Localizer getLocalizer();
}
