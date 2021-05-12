package chav1961.purelib.ui.swing;

import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.concurrent.LightWeightListenerList;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.model.Constants;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.swing.SwingUtils.JMenuBarWithMeta;

public class SimpleMenuBar extends JMenuBarWithMeta {
	private static final long 				serialVersionUID = 1229280805405347474L;
	
	private final Localizer					localizer;
	private final LightWeightListenerList<ActionListener>	listeners = new LightWeightListenerList<>(ActionListener.class); 
	
	public SimpleMenuBar(final Localizer localizer, final ContentNodeMetadata metadata) throws LocalizationException {
		super(metadata);
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (metadata == null) {
			throw new NullPointerException("Metadata can't be null");
		}
		else if (!metadata.getRelativeUIPath().toString().contains(Constants.MODEL_NAVIGATION_TOP_PREFIX)) {
			throw new IllegalArgumentException("Metadata must be refered to navigation top node");
		}
		else {
			this.localizer = localizer;
			
			final String	name = URIUtils.removeQueryFromURI(metadata.getUIPath()).toString();
			
			setName(name);
			SwingUtils.toMenuEntity(metadata,this);
			SwingUtils.walkDown(this,(mode,component)->{
				if (mode == NodeEnterMode.ENTER) {
					if (component instanceof JMenuItem) {
						((JMenuItem)component).addActionListener((e)->listeners.fireEvent((listener)->listener.actionPerformed(e)));
					}
				}
				return ContinueMode.CONTINUE;
			});
			InternalUtils.registerAdvancedTooptip(this);
		}
	}

	public void addActionListener(final ActionListener listener) {
		if (listener == null) {
			throw new NullPointerException("Listener can't be null");
		}
		else {
			listeners.addListener(listener);
		}
	}

	public void removeActionListener(final ActionListener listener) {
		if (listener == null) {
			throw new NullPointerException("Listener can't be null");
		}
		else {
			listeners.removeListener(listener);
		}
	}
}
