package chav1961.purelib.ui.swing.useful;

import java.awt.Component;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.SupportedLanguages;

public class JLanguageComboBox extends JComboBox<SupportedLanguages> {
	private static final long serialVersionUID = -2309432236224879591L;

	public JLanguageComboBox(final Localizer localizer) {
		super(new LangModel());
		setSelectedItem(SupportedLanguages.getDefaultLanguage());
		setRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				final JLabel				label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				final SupportedLanguages	lang = (SupportedLanguages)value;
				
				label.setIcon(lang.getIcon());
				label.setText("");
				label.setToolTipText(lang.name());
				return label;
			}
		});
		addActionListener((e)->{
			PureLibSettings.PURELIB_LOCALIZER.setCurrentLocale(((SupportedLanguages)getSelectedItem()).getLocale());
		});
	}
	
	private static class LangModel extends DefaultComboBoxModel<SupportedLanguages> {
		private static final long serialVersionUID = -6138987888366395867L;

		@Override
		public int getSize() {
			return SupportedLanguages.values().length;
		}

		@Override
		public SupportedLanguages getElementAt(final int index) {
			return SupportedLanguages.values()[index];
		}
	}
}
