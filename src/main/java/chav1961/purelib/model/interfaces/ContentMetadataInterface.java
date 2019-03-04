package chav1961.purelib.model.interfaces;

import java.net.URI;

import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.model.FieldFormat;

public interface ContentMetadataInterface {
	String		UI_SCHEME = "ui";	
	String		APPLICATION_SCHEME = "app";	
	
	public interface ContentNodeMetadata extends Iterable<ContentNodeMetadata> {
		String getName();
		boolean mounted();
		Class<?> getType();
		String getLabelId();
		String getTooltipId();
		String getHelpId();
		default String[] getKeywords(){return new String[0];}
		default String[] getAttachments(){return new String[0];}
		FieldFormat getFormatAssociated();
		URI getApplicationPath();
		URI getUIPath();
		URI getRelativeUIPath();
		URI getLocalizerAssociated();
		ContentNodeMetadata getParent();
		int getChildrenCount();
		ContentMetadataInterface getOwner();
	}

	@FunctionalInterface
	public interface ContentWalker {
		ContinueMode process(NodeEnterMode mode, URI applicationPath, URI uiPath, ContentNodeMetadata node);
	}
	
	ContentNodeMetadata getRoot();
	ContentNodeMetadata[] byApplicationPath(URI applicationPath);
	ContentNodeMetadata byUIPath(URI uiPath);

	void walkDown(ContentWalker walker, URI uiPath);
	
	void mount(URI uiPath, ContentMetadataInterface model);
	ContentMetadataInterface unmount(URI uiPath);
}
