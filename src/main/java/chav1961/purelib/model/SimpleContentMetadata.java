package chav1961.purelib.model;

import java.net.URI;

import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;

public class SimpleContentMetadata implements ContentMetadataInterface {
	private final ContentNodeMetadata	root;

	public SimpleContentMetadata(final ContentNodeMetadata root) {
		if (root == null) {
			throw new NullPointerException("Root node can't be null"); 
		}
		else {
			this.root = root;
		}
	}
	
	@Override
	public ContentNodeMetadata getRoot() {
		return root;
	}

	@Override
	public ContentNodeMetadata byApplicationPath(final URI applicationPath) {
		if (applicationPath == null) {
			throw new NullPointerException("Application path can't be null"); 
		}
		else {
			final ContentNodeMetadata[]	result = new ContentNodeMetadata[1];
			
			walkDown((mode,appPath,uiPath,node)->{
				if (mode == NodeEnterMode.ENTER) {
					if (appPath.equals(applicationPath)) {
						result[0] = node;
						return ContinueMode.STOP;
					}
					else {
						return ContinueMode.CONTINUE;
					}
				}
				else {
					return ContinueMode.CONTINUE;
				}
			},getRoot().getUIPath());
			return result[0];
		}
	}

	@Override
	public ContentNodeMetadata byUIPath(final URI userInterfacePath) {
		if (userInterfacePath == null) {
			throw new NullPointerException("Application path can't be null"); 
		}
		else {
			final ContentNodeMetadata[]	result = new ContentNodeMetadata[1];
			
			walkDownInternal((mode,appPath,uiPath,node)->{
				if (mode == NodeEnterMode.ENTER) {
					if (uiPath.equals(userInterfacePath)) {
						result[0] = node;
						return ContinueMode.STOP;
					}
					else {
						return ContinueMode.CONTINUE;
					}
				}
				else {
					return ContinueMode.CONTINUE;
				}
			},getRoot());
			return result[0];
		}
	}

	@Override
	public void walkDown(final ContentWalker walker, final URI uiPath) {
		if (walker == null) {
			throw new NullPointerException("Walker can't be null"); 
		}
		else {
			final ContentNodeMetadata	startNode = byUIPath(uiPath);
			
			if (startNode != null) {
				walkDownInternal(walker,startNode);
			}
		}
	}

	@Override
	public void mount(final URI uiPath, final ContentMetadataInterface model) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ContentMetadataInterface unmount(final URI uiPath) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		return "SimpleContentMetadata [root=" + root + "]";
	}

	private static ContinueMode walkDownInternal(final ContentWalker walker, final ContentNodeMetadata node) {
		final ContinueMode	enterRC = walker.process(NodeEnterMode.ENTER,node.getApplicationPath(),node.getUIPath(),node);
		ContinueMode		childRC = null;
		
		if (enterRC == ContinueMode.CONTINUE) {
			if (node.getChildrenCount() == 0) {
				childRC = ContinueMode.CONTINUE;
			}
			else {
				for (ContentNodeMetadata child : node) {
					if ((childRC = walkDownInternal(walker,child)) != ContinueMode.CONTINUE) {
						break;
					}
				}
			}
		}
		final ContinueMode	exitRC = walker.process(NodeEnterMode.EXIT,node.getApplicationPath(),node.getUIPath(),node);
		
		if (enterRC == ContinueMode.STOP || exitRC == ContinueMode.STOP || (childRC == null || childRC == ContinueMode.STOP)) {
			return ContinueMode.STOP;
		}
		else {
			return ContinueMode.CONTINUE;
		}			
	}
}
