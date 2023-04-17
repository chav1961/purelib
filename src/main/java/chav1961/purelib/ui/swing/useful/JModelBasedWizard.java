package chav1961.purelib.ui.swing.useful;

import javax.swing.JComponent;
import javax.swing.JDialog;

import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.LocalizerOwner;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.interfaces.ErrorProcessing;
import chav1961.purelib.ui.interfaces.WizardStep;

public class JModelBasedWizard<Common, E extends Enum<?>, Comp extends JComponent> extends JDialogContainer<Common, E, Comp> implements LocalizerOwner, NodeMetadataOwner {
	private static final long serialVersionUID = 3281077715206149792L;

	private final Localizer				localizer;
	private final ContentNodeMetadata	meta;
	
	public JModelBasedWizard(Localizer localizer, JDialog parent, final ContentNodeMetadata meta, Common instance, ErrorProcessing<Common, E> err) {
		super(localizer, parent, instance, err, buildWizardSteps(meta));
		// TODO Auto-generated constructor stub
		this.meta = meta;
		this.localizer = localizer;
	}

	@Override
	public ContentNodeMetadata getNodeMetadata() {
		return meta;
	}

	@Override
	public Localizer getLocalizer() {
		return localizer;
	}

	private static <Common, E extends Enum<?>, Comp extends JComponent> WizardStep<Common, E, Comp>[] buildWizardSteps(final ContentNodeMetadata meta) {
		return null;
	}
}
