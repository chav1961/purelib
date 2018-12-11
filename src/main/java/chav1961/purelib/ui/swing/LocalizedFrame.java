package chav1961.purelib.ui.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.border.EtchedBorder;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.PureLibLocalizer;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;

public class LocalizedFrame extends JFrame implements LocaleChangeListener {
	private static final long 		serialVersionUID = 4794925705230453470L;
	private static final String		EXIT_ACTION = "exit";
	private static final String		HELP_ACTION = "help";

	protected final Localizer		localizer;
	private final String			titleId;

	public LocalizedFrame(final JComponent parent, final Localizer localizer, final String titleId) throws NullPointerException, IllegalArgumentException {
		this(parent,localizer,titleId,null);
	}
	
	public LocalizedFrame(final JComponent parent, final Localizer localizer, final String titleId, final String helpId) throws NullPointerException, IllegalArgumentException {
		super();		
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (titleId == null || titleId.isEmpty()) {
			throw new IllegalArgumentException("Title id can't be null or empty");
		}
		else {
			this.localizer = localizer;
			this.titleId = titleId;
			
		    SwingUtils.assignHelpKey((JPanel)getContentPane(),localizer,helpId);
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
		SwingUtils.refreshLocale(this,oldLocale, newLocale);
	}

	protected void help(final String helpId) throws LocalizationException, ContentException {
		final LocalizedDialog		helpWindow = new LocalizedDialog(null,ModalityType.DOCUMENT_MODAL,localizer,PureLibLocalizer.TITLE_HELP_SCREEN);
		final SimpleHelpComponent	helpComponent = new SimpleHelpComponent(localizer,helpId); 
		final Dimension				screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		final Dimension				helpWindowSize = new Dimension(screenSize.width*3/4,screenSize.height*3/4);
		
		helpComponent.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
		helpWindow.getContentPane().setLayout(new BorderLayout());
		helpWindow.getContentPane().add(helpComponent,BorderLayout.CENTER);
		helpWindow.setPreferredSize(helpWindowSize);
		helpWindow.setLocation(screenSize.width/8,screenSize.height/8);
		helpWindow.setVisible(true);		
	}
}

