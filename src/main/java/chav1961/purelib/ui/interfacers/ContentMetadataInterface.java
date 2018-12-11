package chav1961.purelib.ui.interfacers;

import java.net.URI;

import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;

public interface ContentMetadataInterface {
	public interface ContentNodeMetadata extends Iterable<ContentNodeMetadata> {
		String getName();
		boolean mounted();
		Class<?> getType();
		String getLabelId();
		String getTooltipId();
		String getHelpId();
		String getFormatAssociated();
		URI getApplicationPath();
		URI getUIPath();
		URI getRelativeUIPath();
		URI getLocalizerAssociated();
		ContentNodeMetadata getParent();
		int getChildCount();
	}

	@FunctionalInterface
	public interface ContentWalker {
		ContinueMode process(NodeEnterMode mode, URI applicationPath, URI uiPath, ContentNodeMetadata node);
	}
	
	ContentNodeMetadata getRoot();
	ContentNodeMetadata byApplicationPath(URI applicationPath);
	ContentNodeMetadata byUIPath(URI uiPath);

	void walkDown(ContentWalker walker, URI uiPath);
	
	void mount(URI uiPath, ContentMetadataInterface model);
	ContentMetadataInterface unmount(URI uiPath);
}
