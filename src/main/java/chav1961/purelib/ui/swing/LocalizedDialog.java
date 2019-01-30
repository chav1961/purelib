package chav1961.purelib.ui.swing;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JToolTip;
import javax.swing.KeyStroke;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.PureLibLocalizer;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;

public class LocalizedDialog extends JDialog implements LocaleChangeListener {
	private static final long 		serialVersionUID = -7679745255777120900L;
	private static final String		EXIT_ACTION = "exit";
	private static final String		OK_ACTION = "OK";
	private static final String		CANCEL_ACTION = "cancel";

	protected final Localizer		localizer;
	private final String			titleId;

	public LocalizedDialog(final JComponent parent, final ModalityType modality, final Localizer localizer, final String titleId) throws NullPointerException, IllegalArgumentException, LocalizationException {
		this(parent,modality,localizer,titleId,null);
	}
	
	public LocalizedDialog(final JComponent parent, final ModalityType modality, final Localizer localizer, final String titleId, final String helpId) throws NullPointerException, IllegalArgumentException, LocalizationException {
		super(null,modality);		
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (titleId == null || titleId.isEmpty()) {
			throw new IllegalArgumentException("Title id can't be null or empty");
		}
		else {
			this.localizer = localizer;
			this.titleId = titleId;

			if (helpId != null && !helpId.isEmpty()) {
			    SwingUtils.assignHelpKey((JPanel)getContentPane(),localizer,helpId);
			}
		    SwingUtils.assignActionKey((JPanel)getContentPane(),SwingUtils.KS_EXIT,new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
					dispose();
				}
			},EXIT_ACTION);
		    
		    if (parent != null) {
		    	this.setLocationRelativeTo(parent);
		    }
		    localeChanged(localizer.currentLocale().getLocale(),localizer.currentLocale().getLocale());
		}
	}
	
	@Override
	public void setVisible(final boolean visibility) {
		pack();
		super.setVisible(visibility);
	}
	
	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		setTitle(localizer.getValue(titleId));
		SwingUtils.refreshLocale(this.getContentPane(),oldLocale, newLocale);
	}

	public static <T> boolean askParameters(final Localizer localizer, final AutoBuiltForm<T> form) throws NullPointerException, IllegalArgumentException, LocalizationException {
		return askParameters(null,localizer,PureLibLocalizer.TITLE_ASK_PARAMETERS_SCREEN,null,form);
	}
	
	public static <T> boolean askParameters(final JComponent parent, final Localizer localizer, final String titleId, final String helpId, final AutoBuiltForm<T> form) throws NullPointerException, IllegalArgumentException, LocalizationException {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (titleId == null || titleId.isEmpty()) {
			throw new IllegalArgumentException("Title id can't be null or empty");
		}
		else if (form == null) {
			throw new NullPointerException("Form to show can't be null");
		}
		else if (!localizer.containsKey(titleId)) {
			throw new IllegalArgumentException("Current localizer doesn't contain title id ["+titleId+"]");
		}
		else if (helpId != null && !localizer.containsKey(helpId)) {
			throw new IllegalArgumentException("Current localizer doesn't contain help id ["+helpId+"]");
		}
		else {
			final LocalizedDialog	dialog = new LocalizedDialog(parent,ModalityType.DOCUMENT_MODAL,localizer,titleId);
			final JPanel			buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			final boolean[]			result = new boolean[]{false};

			dialog.getContentPane().add(form,BorderLayout.CENTER);

			final ActionListener	listener = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					switch (e.getActionCommand()) {
						case OK_ACTION		:
							result[0] = true;
							dialog.setVisible(false);
							dialog.dispose();
							break;
						case CANCEL_ACTION	:
							result[0] = false;
							dialog.setVisible(false);
							dialog.dispose();
							break;
						default :
							break;
					}
				}
			};
			
//			if (!form.hasActions()) {
//				final JButton			okButton = new JButton("\uF0FE OK"){private static final long serialVersionUID = 1L; @Override public JToolTip createToolTip(){return new SmartToolTip(localizer,this);}};
//				final JButton			cancelButton = new JButton("\uF0FD Cancel"){private static final long serialVersionUID = 1L; @Override public JToolTip createToolTip(){return new SmartToolTip(localizer,this);}};
//				
//				buttonPanel.add(okButton);
//				buttonPanel.add(cancelButton);
//				dialog.getContentPane().add(buttonPanel,BorderLayout.SOUTH);
//				
//				okButton.addActionListener(listener);
//				okButton.setActionCommand(OK_ACTION);
//				cancelButton.addActionListener(listener);
//				cancelButton.setActionCommand(CANCEL_ACTION);
//			}
//			else {
//				form.addActionListener(listener);
//			}
			
			SwingUtils.assignActionKey(form,KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0),listener,OK_ACTION);
			SwingUtils.assignActionKey(form,KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),listener,CANCEL_ACTION);
			if (helpId != null) {
				SwingUtils.assignHelpKey((JPanel)dialog.getContentPane(),localizer,helpId);
			}
			dialog.pack();
			dialog.doLayout();
			
			if (parent != null) {
				dialog.setLocationRelativeTo(parent);
			}
			else {
				SwingUtils.centerMainWindow(dialog);
			}
			dialog.setVisible(true);
			
			return result[0];
		}
	}
}
