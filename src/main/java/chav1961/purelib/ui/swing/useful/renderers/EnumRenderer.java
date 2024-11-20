package chav1961.purelib.ui.swing.useful.renderers;

import java.awt.Component;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import chav1961.purelib.basic.ColorUtils;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.ui.swing.interfaces.SwingItemRenderer;

public class EnumRenderer<R> implements SwingItemRenderer<Enum<?>, R> {
	private static final Set<Class<?>>	SUPPORTED_RENDERERDS = Set.of(TableCellRenderer.class, ListCellRenderer.class, TreeCellRenderer.class);

	public EnumRenderer() {
	}

	@Override
	public boolean canServe(Class<Enum<?>> class2Render, Class<R> rendererType, Object... options) {
		if (class2Render == null) {
			throw new NullPointerException("Class to render descriptor can't be null"); 
		}
		else if (rendererType == null) {
			throw new NullPointerException("Renderer type can't be null"); 
		}
		else if (class2Render.isArray()) {
			return canServe((Class<Enum<?>>) class2Render.getComponentType(), rendererType, options);
		}
		else {
			return Enum.class.isAssignableFrom(class2Render) && SUPPORTED_RENDERERDS.contains(rendererType); 
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
				public Component getListCellRendererComponent(final JList list, final Object val, final int index, final boolean isSelected, final boolean cellHasFocus) {
					if (val == null) {
						return new JLabel("unselected");
					}
					else {
						final Enum<?>				value = (Enum<?>)val;
						final JLabel				label = new JLabel();

						label.setOpaque(true);
						if (list.isEnabled()) {
							label.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
							label.setForeground(isSelected ?  list.getSelectionForeground() : list.getForeground());
						}
						else {
							label.setBackground((isSelected ? list.getSelectionBackground() : list.getBackground()).darker());
							label.setForeground((isSelected ?  list.getSelectionForeground() : list.getForeground()).brighter());
						}
						if (cellHasFocus) {
							label.setBorder(new LineBorder(ColorUtils.defaultColorScheme().MANDATORY_SELECTION_FOREGROUND));
						}
						try{if (value.getClass().getField(value.name()).isAnnotationPresent(LocaleResource.class)) {
								final LocaleResource	res = value.getClass().getField(value.name()).getAnnotation(LocaleResource.class);
								final Localizer			localizer = LocalizerFactory.getLocalizer(URI.create(value.getClass().getAnnotation(LocaleResourceLocation.class).value())); 
							
								label.setText(localizer.getValue(res.value()));
								label.setToolTipText(localizer.getValue(res.tooltip()));
								if (!res.icon().isEmpty()) {
									label.setIcon(new ImageIcon(URI.create(res.icon()).toURL()));
								}
							}
							else {
								label.setText(value.name());
								label.setToolTipText(value.name());
							}
						} catch (NoSuchFieldException | LocalizationException | MalformedURLException e) {
							label.setText(value.name());
						}
						return label;
					}
				}
			};
		}
		else if (TableCellRenderer.class.isAssignableFrom(rendererType)) {
			return (R) new DefaultTableCellRenderer() {
				private static final long serialVersionUID = 0L;
				
				private final Map<Class<?>, ListCellRenderer>	nestedRenderers = new HashMap<>();

				@Override
				public Component getTableCellRendererComponent(final JTable table, final Object val, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
					if (val == null) {
						return new JLabel("unselected");
					}
					else {
						final Enum<?>	value = (Enum<?>)val;
						final JLabel	label = new JLabel();

						label.setOpaque(true);
						if (table.isEnabled()) {
							label.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
							label.setForeground(isSelected ?  table.getSelectionForeground() : table.getForeground());
						}
						else {
							label.setBackground((isSelected ? table.getSelectionBackground() : table.getBackground()).darker());
							label.setForeground((isSelected ?  table.getSelectionForeground() : table.getForeground()).brighter());
						}
						if (hasFocus) {
							label.setBorder(new LineBorder(ColorUtils.defaultColorScheme().MANDATORY_SELECTION_FOREGROUND));
						}
						try{if (value.getClass().getField(value.name()).isAnnotationPresent(LocaleResource.class)) {
								final LocaleResource	res = value.getClass().getField(value.name()).getAnnotation(LocaleResource.class);
								final Localizer			localizer = LocalizerFactory.getLocalizer(URI.create(value.getClass().getAnnotation(LocaleResourceLocation.class).value())); 
							
								label.setText(localizer.getValue(res.value()));
								label.setToolTipText(localizer.getValue(res.tooltip()));
								if (!res.icon().isEmpty()) {
									label.setIcon(new ImageIcon(URI.create(res.icon()).toURL()));
								}
							}
							else {
								label.setText(value.name());
								label.setToolTipText(value.name());
							}
						} catch (NoSuchFieldException | LocalizationException | MalformedURLException e) {
							label.setText(value.name());
						}
						return label;
					}
				}
			};
		}
		else {
			throw new UnsupportedOperationException("Required cell renderer ["+rendererType+"] is not supported yet");
		}
	}
}
