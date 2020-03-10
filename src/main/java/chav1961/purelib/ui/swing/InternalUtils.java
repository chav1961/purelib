package chav1961.purelib.ui.swing;


import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.JComponent;
import javax.swing.text.InternationalFormatter;
import javax.swing.text.JTextComponent;
import javax.swing.text.NumberFormatter;

import chav1961.purelib.model.FieldFormat;

class InternalUtils {
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
				component.setForeground(SwingUtils.NEGATIVEMARK_FOREGROUND);
			}
			else if (signum > 0) {
				component.setForeground(SwingUtils.POSITIVEMARK_FOREGROUND);
			}
			else {
				component.setForeground(SwingUtils.ZEROMARK_FOREGROUND);
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
}
