package chav1961.purelib.ui.swing.useful.renderers;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.ui.interfaces.ItemAndSelection;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.SwingItemRenderer;

public class ItemAndSelectionRenderer<T, R> implements SwingItemRenderer<ItemAndSelection<T>, R> {
	private static final Set<Class<?>>	SUPPORTED_RENDERERDS = Set.of(TableCellRenderer.class, ListCellRenderer.class, TreeCellRenderer.class);

	public ItemAndSelectionRenderer() {
	}

	@Override
	public boolean canServe(final Class<ItemAndSelection<T>> class2Render, final Class<R> rendererType, final Object... options) {
		if (class2Render == null) {
			throw new NullPointerException("Class to render descriptor can't be null"); 
		}
		else if (rendererType == null) {
			throw new NullPointerException("Renderer type can't be null"); 
		}
		else if (class2Render.isArray()) {
			return canServe((Class<ItemAndSelection<T>>) class2Render.getComponentType(), rendererType, options);
		}
		else {
			return ItemAndSelection.class.isAssignableFrom(class2Render) && SUPPORTED_RENDERERDS.contains(rendererType); 
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
				
				private final Map<Class<?>, ListCellRenderer>	nestedRenderers = new HashMap<>();

				@Override
				public Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
					final ItemAndSelection	sel = (ItemAndSelection)value;
					final JLabel			label;
					
					if (sel.getItem() != null) {
						final Class<?>		cl = sel.getItem().getClass();
						
						if (!nestedRenderers.containsKey(cl)) {
							try{nestedRenderers.put(cl, SwingUtils.getCellRenderer(cl, null, ListCellRenderer.class));
							} catch (EnvironmentException e) {
								nestedRenderers.put(cl, (ListCellRenderer) new StringRenderer().getRenderer(ListCellRenderer.class, ff));							
							}
						}
						label = (JLabel) nestedRenderers.get(cl).getListCellRendererComponent(list, sel.getItem(), index, isSelected, cellHasFocus);
					}
					else {
						label = new JLabel();
					}
					final JCheckBox			box = new JCheckBox();
					final JPanel			panel = new JPanel(new BorderLayout());
					
					box.setOpaque(false);
					panel.setBackground(label.getBackground());
					panel.add(box, BorderLayout.WEST);
					panel.add(label, BorderLayout.CENTER);
					box.setSelected(sel.isSelected());
					return panel;
				}
			};
		}
		else {
			throw new UnsupportedOperationException("Required cell renderer ["+rendererType+"] is not supported yet");
		}
	}
}
