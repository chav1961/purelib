package chav1961.purelib.i18n;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.text.JTextComponent;

import chav1961.purelib.i18n.interfaces.LocaleSpecificTextSetter;
import chav1961.purelib.i18n.interfaces.Localizer;

/**
 * <p>This utility class can be used as container for some swing objects to support automatic localization for them. It implements a {@linkplain LocaleSpecificTextSetter}
 * interface and contains {@linkplain #getComponent()} method to get access to wrapped object. Don't use this class directly, but it's static methods as a factory 
 * methods</p>
 *   
 * @see Localizer
 * @see chav1961.purelib.fsys
 * @see chav1961.purelib.i18n JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 * @lastUpdate 0.0.3
 * @param <T> any JComponent or it's child
 */

public abstract class LocaleSpecificTextWrapper<T extends JComponent> implements LocaleSpecificTextSetter {
	private static final String		PARM_TEXT = "text";
	private static final String		PARM_TOOLTIP = "toopTip";
	
	private final T		component;
	
	private LocaleSpecificTextWrapper(final T component){
		this.component = component;
	}
	
	/**
	 * <p>Get access to wrapped instance</p>
	 * @return instance wrapped. Can't be null
	 */
	public T getComponent() {
		return (T)component;
	}
	
	private static void checkParameter(final String name, final String value) throws NullPointerException {
		if (value == null) {
			throw new NullPointerException("Parameter ["+name+"] can't be null");
		}
	}
	
	/**
	 * <p>Create wrapper for the JComponent instance</p>
	 * @param content wrapper content
	 * @return wrapper created
	 * @throws NullPointerException when content is null
	 * @lastUpdate 0.0.3
	 */
	public static LocaleSpecificTextWrapper<JComponent> wrap(final JComponent content) throws NullPointerException {
		if (content == null) {
			throw new NullPointerException("Content to build wrapeer for can't be null");
		}
		else {
			return new LocaleSpecificTextWrapper<JComponent>(content){
				@Override public void setLocaleSpecificText(final String text) {checkParameter(PARM_TEXT,text);}
				@Override public void setLocaleSpecificToolTipText(final String toolTip) {checkParameter(PARM_TOOLTIP,toolTip); content.setToolTipText(toolTip);}
			};
		}
	}

	/**
	 * <p>Create wrapper for the JTextComponent instance</p>
	 * @param content wrapper content
	 * @return wrapper created
	 * @throws NullPointerException when content is null
	 * @lastUpdate 0.0.3
	 */
	public static LocaleSpecificTextWrapper<JTextComponent> wrap(final JTextComponent content) throws NullPointerException {
		if (content == null) {
			throw new NullPointerException("Content to build wrapeer for can't be null");
		}
		else {
			return new LocaleSpecificTextWrapper<JTextComponent>(content){
				@Override public void setLocaleSpecificText(final String text) {checkParameter(PARM_TEXT,text); content.setText(text);}
				@Override public void setLocaleSpecificToolTipText(final String toolTip) {checkParameter(PARM_TOOLTIP,toolTip); content.setToolTipText(toolTip);}
			};
		}
	}

	/**
	 * <p>Create wrapper for the AbstractButton instance</p>
	 * @param content wrapper content
	 * @return wrapper created
	 * @throws NullPointerException when content is null
	 * @lastUpdate 0.0.3
	 */
	public static LocaleSpecificTextWrapper<AbstractButton> wrap(final AbstractButton content) throws NullPointerException {
		if (content == null) {
			throw new NullPointerException("Content to build wrapeer for can't be null");
		}
		else {
			return new LocaleSpecificTextWrapper<AbstractButton>(content){
				@Override public void setLocaleSpecificText(final String text) {checkParameter(PARM_TEXT,text); content.setText(text);}
				@Override public void setLocaleSpecificToolTipText(final String toolTip) {checkParameter(PARM_TOOLTIP,toolTip); content.setToolTipText(toolTip);}
			};
		}
	}

	/**
	 * <p>Create wrapper for the JLabel instance</p>
	 * @param content wrapper content
	 * @return wrapper created
	 * @throws NullPointerException when content is null
	 * @lastUpdate 0.0.3
	 */
	public static LocaleSpecificTextWrapper<JLabel> wrap(final JLabel content) throws NullPointerException {
		if (content == null) {
			throw new NullPointerException("Content to build wrapeer for can't be null");
		}
		else {
			return new LocaleSpecificTextWrapper<JLabel>(content){
				@Override public void setLocaleSpecificText(final String text) {checkParameter(PARM_TEXT,text); content.setText(text);}
				@Override public void setLocaleSpecificToolTipText(final String toolTip) {checkParameter(PARM_TOOLTIP,toolTip); content.setToolTipText(toolTip);}
			};
		}
	}
}
