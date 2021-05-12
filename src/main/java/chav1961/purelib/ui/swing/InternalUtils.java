package chav1961.purelib.ui.swing;


import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.TimerTask;

import javax.swing.JComponent;
import javax.swing.ToolTipManager;
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
			instance.addMouseListener(new MouseListener() {
				@Override public void mouseReleased(MouseEvent e) {}
				@Override public void mousePressed(MouseEvent e) {}
				@Override public void mouseExited(MouseEvent e) {}
				@Override public void mouseClicked(MouseEvent e) {}
				
				@Override
				public void mouseEntered(MouseEvent e) {
					internalShowTooltip(instance,e);
				}
			});
		}
	}

	private static <T extends JComponent> void internalShowTooltip(final T instance, final MouseEvent e) {
		final ToolTipManager	mgr = ToolTipManager.sharedInstance();
		final String			tt = instance.getToolTipText();
		final MouseEvent		me = new MouseEvent(instance, 0, 0, 0, e.getX(), e.getY(), 0, 0, 0, false, 0);
		final int				initialDelay = mgr.getInitialDelay();
		final int				dismissDelay = mgr.getDismissDelay();
		
		if (tt == null) {
			mgr.registerComponent(instance);
		}
		mgr.setInitialDelay(PureLibSettings.instance().getProperty(PureLibSettings.SWING_TOOLTIP_MODE,int.class,"1000"));
		mgr.setDismissDelay(PureLibSettings.instance().getProperty(PureLibSettings.SWING_TOOLTIP_MODE,int.class,"5000"));
		instance.setToolTipText(buildDebuggingTooltip(instance,tt));
		mgr.mouseMoved(me);
		
		PureLibSettings.COMMON_MAINTENANCE_TIMER.schedule(new TimerTask(){
			@Override
			public void run() {
				mgr.setInitialDelay(initialDelay);
				mgr.setDismissDelay(dismissDelay);
				if (tt == null) {
					mgr.unregisterComponent(instance);
				}
				else {
					instance.setToolTipText(tt);
				}
			}
		}, mgr.getDismissDelay());
	}
	
	private static <T extends JComponent> String buildDebuggingTooltip(final T instance, final String tooltip) {
		if (instance == null) {
			return "";
		}
		else {
			final Class<T>		clazz = (Class<T>) instance.getClass();
			final StringBuilder	sb = new StringBuilder();
			
			sb.append("<html><body>");
			sb.append("<h3>Control description:</h3>");
			sb.append("<p><b>Class :</b> ").append(clazz.getCanonicalName()).append("</p>");
			if (instance.getName() != null && !instance.getName().isEmpty()) {
				sb.append("<p><b>UI name :</b> ").append(instance.getName()).append("</p>");
			}
			if (clazz.getEnclosingClass() != null) {
				sb.append("<p><b>Owned by:</b> ").append(clazz.getEnclosingClass().getCanonicalName()).append("</p>");
			}
			if (instance.getParent() != null) {
				sb.append("<p><b>Container class:</b> ").append(instance.getParent().getClass().getCanonicalName()).append("</p>");
			}
			if (instance instanceof NodeMetadataOwner) {
				final ContentNodeMetadata	meta = ((NodeMetadataOwner)instance).getNodeMetadata();
				
				if (meta != null) {
					sb.append("<p><b>Model item :</b> ").append(meta.getName()).append("</p>");
					if (meta.getApplicationPath() != null) {
						sb.append("<p><b>Model App URI:</b> ").append(meta.getApplicationPath()).append("</p>");
					}
				}
			}
			if (tooltip != null) {
				sb.append("<ht>").append("<p>").append(tooltip).append("</p>");
			}
			sb.append("</body></html>");
			return sb.toString();
		}
	}
}
