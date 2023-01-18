package chav1961.purelib.ui.swing.useful.renderers;

import java.awt.Component;
import java.util.Set;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import chav1961.purelib.ui.swing.interfaces.IconAndTextKeeper;
import chav1961.purelib.ui.swing.interfaces.SwingItemRenderer;

public class IconAndTextKeeperRenderer<R> implements SwingItemRenderer<IconAndTextKeeper, R> {
	private static final Set<Class<?>>	SUPPORTED_RENDERERDS = Set.of(TableCellRenderer.class, ListCellRenderer.class, TreeCellRenderer.class);

	public IconAndTextKeeperRenderer() {
	}

	@Override
	public boolean canServe(Class<IconAndTextKeeper> class2Render, Class<R> rendererType, Object... options) {
		if (class2Render == null) {
			throw new NullPointerException("Class to render descriptor can't be null"); 
		}
		else if (rendererType == null) {
			throw new NullPointerException("Renderer type can't be null"); 
		}
		else if (class2Render.isArray()) {
			return canServe((Class<IconAndTextKeeper>) class2Render.getComponentType(), rendererType, options);
		}
		else {
			return IconAndTextKeeper.class.isAssignableFrom(class2Render) && SUPPORTED_RENDERERDS.contains(rendererType); 
		}
	}

	@Override
	public R getRenderer(final Class<R> rendererType, final Object... options) {
		if (rendererType == null) {
			throw new NullPointerException("Renderer type can't be null"); 
		}
		else if (ListCellRenderer.class.isAssignableFrom(rendererType)) {
			return (R) new DefaultListCellRenderer() {
				private static final long serialVersionUID = 0L;
				
				@Override
				public Component getListCellRendererComponent(final JList list, final Object val, final int index, final boolean isSelected, final boolean cellHasFocus) {
					final JLabel			label = (JLabel)super.getListCellRendererComponent(list, "", index, isSelected, cellHasFocus);
					final IconAndTextKeeper	keeper = (IconAndTextKeeper)val;

					label.setIcon(keeper.getIcon());
					label.setText(keeper.getText());
					label.setToolTipText(keeper.getToolTipText());
					return label;
				}
			};
		}
		else {
			throw new UnsupportedOperationException("Required cell renderer ["+rendererType+"] is not supported yet");
		}
	}
}
