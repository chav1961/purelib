package chav1961.purelib.model;


import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.Utils.EverywhereWalkerCallback;
import chav1961.purelib.basic.Utils.EverywhereWalkerCollector;
import chav1961.purelib.basic.Utils.EverywhereWalkerCollector.ReferenceType;
import chav1961.purelib.basic.exceptions.ContentException;
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
//		
//		
//		
//		
//		
//		
//		
//		
//		final ContinueMode	enterRC = walker.process(NodeEnterMode.ENTER,node.getApplicationPath(),node.getUIPath(),node);
//		ContinueMode		childRC = null, exitRC = null;
//		
//		switch (enterRC) {
//			case CONTINUE		:
//				for (ContentNodeMetadata child : node) {
//					if ((childRC = walkDownInternal(walker,child)) != ContinueMode.CONTINUE) {
//						break;
//					}
//				}
//				exitRC = walker.process(NodeEnterMode.EXIT, node.getApplicationPath(), node.getUIPath(), node);
//				break;
//			case SKIP_CHILDREN	:
//				break;
//			case SKIP_SIBLINGS	:
//				break;
//			case STOP			:
//			case SKIP_PARENT	:
//			case PARENT_ONLY	:
//			case SIBLINGS_ONLY	:
//				walker.process(NodeEnterMode.EXIT, node.getApplicationPath(), node.getUIPath(), node);
//				return ContinueMode.STOP;
//			default:
//				throw new UnsupportedOperationException("Continue mode ["+enterRC+"] is not supported yet");
//		}
//		if (enterRC == ContinueMode.CONTINUE) {
//			if (node.getChildrenCount() == 0) {
//				childRC = ContinueMode.CONTINUE;
//			}
//			else {
//				for (ContentNodeMetadata child : node) {
//					if ((childRC = walkDownInternal(walker,child)) != ContinueMode.CONTINUE) {
//						break;
//					}
//				}
//			}
//		}
//		final ContinueMode	exitRC = walker.process(NodeEnterMode.EXIT,node.getApplicationPath(),node.getUIPath(),node);
//		
//		if (enterRC == ContinueMode.STOP || exitRC == ContinueMode.STOP || (childRC == null || childRC == ContinueMode.STOP)) {
//			return ContinueMode.STOP;
//		}
//		else {
//			return ContinueMode.CONTINUE;
//		}			
	}
}
