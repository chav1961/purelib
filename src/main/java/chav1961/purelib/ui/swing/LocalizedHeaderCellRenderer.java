package chav1961.purelib.ui.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.LocaleSpecificTextGetter;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.ui.AbstractLowLevelFormFactory.FieldDescriptor;

class LocalizedHeaderCellRenderer implements TableCellRenderer {
//	private static final Icon	orderingAZ = new ImageIcon(LocalizedHeaderCellRenderer.class.getResource("orderingAZ.png"));
//	private static final Icon	orderingZA = new ImageIcon(LocalizedHeaderCellRenderer.class.getResource("orderingZA.png"));
//	private static final Icon	orderingGray = new ImageIcon(LocalizedHeaderCellRenderer.class.getResource("orderingGray.png"));
//	
	private final Localizer					localizer;
	private final LocaleSpecificTextGetter	desc;
	private final boolean					isMandatory;
	private final Font						currentFont;
	
	LocalizedHeaderCellRenderer(final Localizer localizer, final FieldDescriptor fieldDescriptor) {
		this.localizer = localizer;
		this.currentFont = new JLabel().getFont();
		this.desc = new LocaleSpecificTextGetter(){
			@Override
			public String getLocaleSpecificText() {
				try{return fieldDescriptor.extractLocalizedFieldName(localizer);
				} catch (LocalizationException | IllegalArgumentException e) {
					return fieldDescriptor.field.getName();
				}
			}

			@Override
			public String getLocaleSpecificToolTipText() {
				try{return fieldDescriptor.extractLocalizedFieldTooltip(localizer);
				} catch (LocalizationException | IllegalArgumentException e) {
					return fieldDescriptor.field.getName();
				}
			}
		};
		this.isMandatory = fieldDescriptor.fieldFormat.isMandatory();
	}

	LocalizedHeaderCellRenderer(final Localizer localizer, final LocaleSpecificTextGetter desc) {
		this.localizer = localizer;
		this.currentFont = new JLabel().getFont();
		this.desc = desc;
		this.isMandatory = false;
	}
	
	@Override
	public Component getTableCellRendererComponent(final JTable table, final Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		final JLabel	label  = new JToolTipedLabel();

		label.setText(desc.getLocaleSpecificText());
		label.setToolTipText(desc.getLocaleSpecificToolTipText());

		label.setOpaque(true);
		if (isMandatory) {
			label.setFont(new Font(currentFont.getFamily(),Font.BOLD,currentFont.getSize()));
			label.setBackground(SwingUtils.MANDATORY_BACKGROUND);
			label.setForeground(SwingUtils.MANDATORY_FOREGROUND);
		}
		else {
			label.setFont(new Font(currentFont.getFamily(),Font.PLAIN,currentFont.getSize()));
			label.setBackground(SwingUtils.OPTIONAL_BACKGROUND);
			label.setForeground(SwingUtils.OPTIONAL_FOREGROUND);
		}
		
//			if (orderingOn) {
//				if (LocalizedTable.this.getRowSorter() instanceof RowSorterInfo) {
//					switch (((RowSorterInfo)LocalizedTable.this.getRowSorter()).getColumnOrderingState(fieldDescriptor.field.getName())) {
//						case A2Z	: label.setIcon(orderingAZ); break;
//						case Z2A	: label.setIcon(orderingZA); break;
//						case NONE	: label.setIcon(orderingGray); break;
//						default		: throw new UnsupportedOperationException("Column ordering ["+((RowSorterInfo)LocalizedTable.this.getRowSorter()).getColumnOrderingState(fieldDescriptor.field.getName())+"] is not implemented yet");
//					}
//				}
//				else {
//					label.setIcon(orderingGray);
//				}
//			}
//			else {
//				label.setIcon(null);
//			}
		label.setBorder(SwingUtils.TABLE_HEADER_BORDER);
		label.setHorizontalAlignment(JLabel.CENTER);
		
		return label;
	}
	
	static class JToolTipedLabel extends JLabel {
		private static final long serialVersionUID = -4788447371128183521L;

		public JToolTipedLabel() {
		}
		
		@Override
		public String getToolTipText(final MouseEvent e) {
			return getToolTipText();
		}
	}
}