package chav1961.purelib.ui.swing.useful.renderers;

import java.awt.Color;
import java.awt.Component;
import java.util.Arrays;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.LineBorder;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.LocalizerOwner;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.SwingItemRenderer;
import chav1961.purelib.ui.swing.useful.JFileSelectionDialog.FilterCallback;

public class FilterCallbackRenderer<R> implements SwingItemRenderer<FilterCallback, R> {
	private static final Set<Class<?>>	SUPPORTED_RENDERERDS = Set.of(ListCellRenderer.class);
	
	public FilterCallbackRenderer() {
	}

	@Override
	public boolean canServe(final Class<FilterCallback> class2Render, final Class<R> rendererType, final Object... options) {
		if (class2Render == null) {
			throw new NullPointerException("Class to render descriptor can't be null"); 
		}
		else if (rendererType == null) {
			throw new NullPointerException("Renderer type can't be null"); 
		}
		else if (class2Render.isArray()) {
			return canServe((Class<FilterCallback>) class2Render.getComponentType(), rendererType, options);
		}
		else {
			return FilterCallback.class.isAssignableFrom(class2Render) && SUPPORTED_RENDERERDS.contains(rendererType); 
		}
	}

	@Override
	public R getRenderer(final Class<R> rendererType, final FieldFormat ff, final Object... options) {
		if (rendererType == null) {
			throw new NullPointerException("Renderer type can't be null"); 
		}
		else if (ListCellRenderer.class.isAssignableFrom(rendererType)) {
			return (R) new DefaultListCellRenderer() {
				private static final long serialVersionUID = 0L;

				@Override
				public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
					final FilterCallback	fcb = (FilterCallback)value;
					final LocalizerOwner	owner = SwingUtils.getNearestOwner(list, LocalizerOwner.class);
					final Localizer			localizer = owner != null ? owner.getLocalizer() : (options.length > 0 && (options[0] instanceof Localizer) ? (Localizer)options[0] : PureLibSettings.PURELIB_LOCALIZER);
					final JLabel			result = new JLabel((localizer.containsKey(fcb.getFilterName()) ? localizer.getValue(fcb.getFilterName()) : fcb.getFilterName())+' '+Arrays.toString(fcb.getFileMask()));
					
					if (isSelected) {
						result.setOpaque(true);
						result.setForeground(list.getSelectionForeground());
						result.setBackground(list.getSelectionBackground());
					}
					if (cellHasFocus) {
						result.setBorder(BorderFactory.createLineBorder(Color.BLACK));
					}
					return result;
				}
			};
		}
		else {
			throw new UnsupportedOperationException("Required cell renderer ["+rendererType+"] is not supported yet");
		}
	}
}
