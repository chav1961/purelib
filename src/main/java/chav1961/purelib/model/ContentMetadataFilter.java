package chav1961.purelib.model;

import java.net.URI;

import chav1961.purelib.model.interfaces.ContentMetadataInterface;

public class ContentMetadataFilter implements ContentMetadataInterface {
	public ContentMetadataFilter(final ContentMetadataInterface nested, final URI[] whiteList) {
		
	}

	public ContentMetadataFilter(final ContentMetadataInterface nested, final URI[] whiteList, final URI[] blackList) {
		
	}

	public ContentMetadataFilter(final ContentMetadataInterface nested, final String whiteListRegExp) {
		
	}
	
	public ContentMetadataFilter(final ContentMetadataInterface nested, final String whiteListRegExp, final String blackListRegExp) {
		
	} 
	
	@Override
	public ContentNodeMetadata getRoot() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ContentNodeMetadata[] byApplicationPath(URI applicationPath) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ContentNodeMetadata byUIPath(URI uiPath) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void walkDown(ContentWalker walker, URI uiPath) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mount(URI uiPath, ContentMetadataInterface model) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ContentMetadataInterface unmount(URI uiPath) {
		// TODO Auto-generated method stub
		return null;
	}

}
