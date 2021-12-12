package chav1961.purelib.ui.swing;


import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

import javax.swing.JButton;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.inner.InternalConstants;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor.MonitorEvent;
import chav1961.purelib.ui.swing.useful.ComponentKeepedBorder;

public class JUriFieldWithMeta extends JTextFieldWithMeta {
	private static final long serialVersionUID = -2602314083682177026L;
	
	private final JButton				gotoButton = new JButton(InternalConstants.ICON_GOTO_LINK);
	private final JComponentMonitor		monitor;
	private final ComponentKeepedBorder	border = new ComponentKeepedBorder(0, gotoButton); 
	
	public JUriFieldWithMeta(final ContentNodeMetadata metadata, final JComponentMonitor monitor) throws LocalizationException {
		super(metadata, monitor);
		this.monitor = monitor;
		gotoButton.setFocusable(false);
		gotoButton.addActionListener((e)->gotoLink(getText()));
		gotoButton.setEnabled(Desktop.isDesktopSupported());
		border.install(this);
	}

	@Override
	public String standardValidation(final Object val) {
		final String	result = super.standardValidation(val);
		
		if (result != null) {
			return result;
		}
		else if (val instanceof String) {
			try{URI.create(val.toString());
				return null;
			} catch (IllegalArgumentException exc) {
				return InternalUtils.buildStandardValidationMessage(getNodeMetadata(), InternalUtils.VALIDATION_ILLEGAL_VALUE, exc.getLocalizedMessage());
			}
		}
		else if (val instanceof URI) {
			return null;
		}
		else {
			return InternalUtils.buildStandardValidationMessage(getNodeMetadata(), InternalUtils.VALIDATION_ILLEGAL_TYPE, URI.class.getCanonicalName());
		}
	}
	
	private void gotoLink(final String uri) {
		try{final URI	ref = URI.create(uri.trim());
		
			if (ref.isAbsolute() && monitor.process(MonitorEvent.Validation, getNodeMetadata(), this)) {
				Desktop.getDesktop().browse(ref);
			}
		} catch (IllegalArgumentException | ContentException | IOException e) {
			SwingUtils.getNearestLogger(this).message(Severity.error, e.getLocalizedMessage());
		}
	}
}
