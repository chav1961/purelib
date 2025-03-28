package chav1961.purelib.model;


import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;

/**
 * <p>This class is default implementation of {@linkplain ContentMetadataInterface}</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 */
public class SimpleContentMetadata implements ContentMetadataInterface {
	private final ContentNodeMetadata	root;

	/**
	 * <p>Constructor of the class instance</p>
	 * @param root root of nodes tree. Ca't be null
	 * @throws NullPointerException root is null
	 */
	public SimpleContentMetadata(final ContentNodeMetadata root) throws NullPointerException {
		if (root == null) {
			throw new NullPointerException("Root node can't be null"); 
		}
		else {
			this.root = root;
			if (root instanceof MutableContentNodeMetadata) {
				((MutableContentNodeMetadata)root).setOwner(this);
			}
		}
	}
	
	@Override
	public ContentNodeMetadata getRoot() {
		return root;
	}

	@Override
	public ContentNodeMetadata[] byApplicationPath(final URI applicationPath) {
		if (applicationPath == null) {
			throw new NullPointerException("Application path can't be null"); 
		}
		else {
			final List<ContentNodeMetadata>	result = new ArrayList<>();
			
			walkDown((mode,appPath,uiPath,node)->{
				if (mode == NodeEnterMode.ENTER) {
					if (applicationPath.equals(appPath)) {
						result.add(node);
					}
					return ContinueMode.CONTINUE;
				}
				else {
					return ContinueMode.CONTINUE; 
				}
			},getRoot().getUIPath());
			return result.toArray(new ContentNodeMetadata[result.size()]);
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
					if (URIUtils.removeQueryFromURI(uiPath).equals(userInterfacePath)) {
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
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public ContentMetadataInterface[] unmount(final URI uiPath) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public String toString() {
		return "SimpleContentMetadata [root=" + root + "]";
	}

	private static ContinueMode walkDownInternal(final ContentWalker walker, final ContentNodeMetadata root) {
		try {
			return Utils.walkDownEverywhere(root, (ref, node)->{
				switch (ref) {
					case CHILDREN	:
						final ContentNodeMetadata[] children = new ContentNodeMetadata[node.getChildrenCount()];
						
						for(int index = 0; index < children.length; index++) {
							children[index] = node.getChild(index);
						}
						return children;
					case PARENT		:
						return new ContentNodeMetadata[] {node.getParent()};
					case SIBLINGS	:
						return new ContentNodeMetadata[0];
					default :
						throw new UnsupportedOperationException("Ref type ["+ref+"] is not supported yet"); 
				}
			}, 
			(mode, node)->walker.process(mode, node.getApplicationPath(), node.getUIPath(), node));
		} catch (ContentException e) {
			return ContinueMode.STOP;
		}
	}
}
