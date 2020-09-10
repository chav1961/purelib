package chav1961.purelib.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import chav1961.purelib.model.interfaces.BlackAndWhiteListCallback;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;

public class ContentNodeFilter implements ContentNodeMetadata {
	private final ContentNodeMetadata		nested;
	private final BlackAndWhiteListCallback	whiteListCallback; 
	private final BlackAndWhiteListCallback	blackListCallback;
	private final List<ContentNodeMetadata>	children = new ArrayList<>();
	
	public ContentNodeFilter(final ContentNodeMetadata nested, final BlackAndWhiteListCallback whiteListCallback) {
		this(nested,whiteListCallback,(n)->false);
	}

	public ContentNodeFilter(final ContentNodeMetadata nested, final BlackAndWhiteListCallback whiteListCallback, final BlackAndWhiteListCallback blackListCallback) throws NullPointerException {
		if (nested == null) {
			throw new NullPointerException("Nested node can't be null");
		}
		else if (whiteListCallback == null) {
			throw new NullPointerException("White list callback can't be null");
		}
		else if (blackListCallback == null) {
			throw new NullPointerException("Black list callback can't be null");
		}
		else {
			this.nested = nested;
			this.whiteListCallback = whiteListCallback;
			this.blackListCallback = blackListCallback;
			
			for (ContentNodeMetadata item : nested) {
				if (whiteListCallback.accept(item)) {
					if (!blackListCallback.accept(item)) {
						children.add(item);
					}
				}
			}
		}
	}
	
	@Override
	public Iterator<ContentNodeMetadata> iterator() {
		return children.iterator();
	}

	@Override
	public String getName() {
		return nested.getName();
	}

	@Override
	public boolean mounted() {
		return nested.mounted();
	}

	@Override
	public Class<?> getType() {
		return nested.getType();
	}

	@Override
	public String getLabelId() {
		return nested.getLabelId();
	}

	@Override
	public String getTooltipId() {
		return nested.getTooltipId();
	}

	@Override
	public String getHelpId() {
		return nested.getHelpId();
	}

	@Override
	public FieldFormat getFormatAssociated() {
		return nested.getFormatAssociated();
	}

	@Override
	public URI getApplicationPath() {
		return nested.getApplicationPath();
	}

	@Override
	public URI getUIPath() {
		return nested.getUIPath();
	}

	@Override
	public URI getRelativeUIPath() {
		return nested.getRelativeUIPath();
	}

	@Override
	public URI getLocalizerAssociated() {
		return nested.getLocalizerAssociated();
	}

	@Override
	public URI getIcon() {
		return nested.getIcon();
	}

	@Override
	public ContentNodeMetadata getParent() {
		return nested.getParent();
	}

	@Override
	public int getChildrenCount() {
		return children.size();
	}

	@Override
	public ContentNodeMetadata getChild(final int index) {
		return children.get(index);
	}

	@Override
	public ContentMetadataInterface getOwner() {
		return nested.getOwner();
	}

}
