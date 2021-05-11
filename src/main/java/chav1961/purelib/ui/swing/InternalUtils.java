package chav1961.purelib.ui.swing;


import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.text.InternationalFormatter;
import javax.swing.text.JTextComponent;
import javax.swing.text.NumberFormatter;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;

class InternalUtils {
	static final String		VALIDATION_NULL_VALUE = "purelib.ui.validation.nullvalue";
	static final String		VALIDATION_MANDATORY = "purelib.ui.validation.mandatory";
	static final String		VALIDATION_NEITHER_TRUE_NOR_FALSE = "purelib.ui.validation.neithertruenorfalse";

	private static final PropertyChangeListener	TOOLTIP_LISTENER = (evt)->processTooltipChanges(evt);
	
	interface ComponentListenerCallback {
		void process();
	}
	
	static boolean checkClassTypes(final Class<?> toTest, final Class<?>... available) {
		for (Class<?> item : available) {
			if (item.isAssignableFrom(toTest)) {
				return true;
			}
		}
		return false;
	}
	
	static boolean checkNullAvailable(final ContentNodeMetadata metadata) {
		return metadata.getFormatAssociated() != null && metadata.getFormatAssociated().isNullSupported(); 
	}

	static boolean checkMandatory(final ContentNodeMetadata metadata) {
		return metadata.getFormatAssociated() != null && metadata.getFormatAssociated().isMandatory(); 
	}
	
	static void addComponentListener(final JComponent component, final ComponentListenerCallback callback) {
		component.addComponentListener(new ComponentListener() {
			private boolean	loaded = false;
			
			@Override 
			public void componentResized(ComponentEvent e) {
				if (!loaded) {
					callback.process();
					loaded = true;
				}
			}
			
			@Override 
			public void componentMoved(ComponentEvent e) {
				if (!loaded) {
					callback.process();
					loaded = true;
				}
			}
			
			@Override 
			public void componentHidden(ComponentEvent e) {
				if (!loaded) {
					callback.process();
					loaded = true;
				}
			}
			
			@Override
			public void componentShown(ComponentEvent e) {
				if (!loaded) {
					callback.process();
					loaded = true;
				}
			}				
		});
	}
	
	static void setFieldColor(final JTextComponent component, final FieldFormat format, final int signum) {
		if (format.isHighlighted(signum)) {
			if (signum < 0) {
				component.setForeground(PureLibSettings.defaultColorScheme().NEGATIVEMARK_FOREGROUND);
			}
			else if (signum > 0) {
				component.setForeground(PureLibSettings.defaultColorScheme().POSITIVEMARK_FOREGROUND);
			}
			else {
				component.setForeground(PureLibSettings.defaultColorScheme().ZEROMARK_FOREGROUND);
			}
		}
	}
	
	static InternationalFormatter prepareNumberFormatter(final FieldFormat format, final Locale currentLocale) {
		final InternationalFormatter	formatter;
		
		if (format.getFormatMask() != null) {
			formatter = new NumberFormatter(new DecimalFormat(format.getFormatMask(),new DecimalFormatSymbols(currentLocale)));
		}
		else {
			final int 			len = format.getLength() == 0 ? 15 : format.getLength();
			final int 			frac = format.getPrecision();
			final NumberFormat	fmt = NumberFormat.getNumberInstance(currentLocale);
			
			fmt.setGroupingUsed(false);
			fmt.setMinimumIntegerDigits(1);
			fmt.setMaximumIntegerDigits(len);
			if (frac > 0) {
				fmt.setMinimumFractionDigits(frac);
				fmt.setMaximumFractionDigits(frac);
			}
			
			formatter = new NumberFormatter(fmt);
			formatter.setAllowsInvalid(false);
		}
		return formatter;
	}
	
	static String buildStandardValidationMessage(final ContentNodeMetadata metadata, final String messageId) {
		try{return String.format(PureLibSettings.PURELIB_LOCALIZER.getValue(messageId),LocalizerFactory.getLocalizer(metadata.getLocalizerAssociated()).getValue(metadata.getLabelId()));
		} catch (LocalizationException | IllegalArgumentException | NullPointerException e) {
			return messageId + "(" + metadata.getLabelId() + ")";
		}
	}
	
	static <T extends JComponent> void registerAdvancedTooptip(final T instance) {
		if ("advanced".equals(PureLibSettings.instance().getProperty(PureLibSettings.SWING_TOOLTIP_MODE))) {
			instance.addPropertyChangeListener(JComponent.TOOL_TIP_TEXT_KEY, TOOLTIP_LISTENER);
			instance.setToolTipText(instance.getToolTipText());
		}
	}
	
	static <T extends JComponent> String buildAdvancedTooltip(final T instance, final String tooltip) {
		if ("advanced".equals(PureLibSettings.instance().getProperty(PureLibSettings.SWING_TOOLTIP_MODE))) {
			if (tooltip == null || tooltip.isEmpty()) {
				return buildDebuggingTooptip(instance);
			}
			else {
				return tooltip + "\n" + buildDebuggingTooptip(instance);
			}
		}
		else {
			return tooltip == null || tooltip.isEmpty() ? null : tooltip;
		}
	}

	private static void processTooltipChanges(final PropertyChangeEvent evt) {
		if (evt.getNewValue() == null) {
			SwingUtilities.invokeLater(()->{
				((JComponent)evt.getSource()).setToolTipText("");
			});
		}
	}

	private static <T extends JComponent> String buildDebuggingTooptip(final T instance) {
		if (instance == null) {
			return null;
		}
		else {
			final Class<T>		clazz = (Class<T>) instance.getClass();
			final StringBuilder	sb = new StringBuilder();
			
			sb.append("at class: ").append(clazz.getCanonicalName());
			if (clazz.getEnclosingClass() != null) {
				sb.append("\nowned by: ").append(clazz.getEnclosingClass().getCanonicalName());
			}
			if (instance.getParent() != null) {
				sb.append("\nplaced in: ").append(instance.getParent().getClass().getCanonicalName());
			}
			if (instance instanceof NodeMetadataOwner) {
				final ContentNodeMetadata	meta = ((NodeMetadataOwner)instance).getNodeMetadata();
				
				if (meta != null) {
					sb.append("\nmodel item associated: ").append(meta.getName()).append("\nmodel APP URI: ").append(meta.getApplicationPath());
				}
				else {
					sb.append("\nno model item associated now");
				}
			}
			return sb.toString();
		}
	}
}
