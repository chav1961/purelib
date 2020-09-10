package chav1961.purelib.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import chav1961.purelib.model.interfaces.BlackAndWhiteListCallback;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;

/**
 * <p>This class implements filter for {@linkplain ContentNodeMetadata} tree. </p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.4
 */
public class ContentNodeFilter implements ContentNodeMetadata {
	private final ContentNodeMetadata		nested;
	private final BlackAndWhiteListCallback	whiteListCallback; 
	private final BlackAndWhiteListCallback	blackListCallback;
	private final boolean 					mutable;
	private final boolean 					inherited;
	private final List<ContentNodeMetadata>	children;

	/**
	 * <p>Constructor if the class</p>
	 * @param nested nested metadata tree
	 * @param whiteListCallback callback to test nodes. 'True' nodes will be visible thru the filter
	 * @throws NullPointerException when any parameter is null 
	 */
	public ContentNodeFilter(final ContentNodeMetadata nested, final BlackAndWhiteListCallback whiteListCallback) throws NullPointerException {
		this(nested,false,whiteListCallback,(n)->true,false);
	}
	
	/**
	 * <p>Constructor of the class</p>
	 * @param nested nested metadata tree
	 * @param whiteListCallback callback to test nodes. 'True' nodes will be visible thru the filter
	 * @param inherited true means that all children will have this filter too
	 * @throws NullPointerException when any parameter is null 
	 */
	public ContentNodeFilter(final ContentNodeMetadata nested, final BlackAndWhiteListCallback whiteListCallback, final boolean inherited) throws NullPointerException {
		this(nested,false,whiteListCallback,(n)->false,inherited);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param nested nested metadata tree
	 * @param whiteListCallback callback to test nodes. 'True' nodes will be visible thru the filter
	 * @param blackListCallback callback to test nodes. 'True' nodes will be excluded from visible nodes
	 * @throws NullPointerException when any parameter is null 
	 */
	public ContentNodeFilter(final ContentNodeMetadata nested, final BlackAndWhiteListCallback whiteListCallback, final BlackAndWhiteListCallback blackListCallback) throws NullPointerException {
		this(nested,false,whiteListCallback,blackListCallback,false);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param nested nested metadata tree
	 * @param whiteListCallback callback to test nodes. 'True' nodes will be visible thru the filter
	 * @param blackListCallback callback to test nodes. 'True' nodes will be excluded from visible nodes
	 * @param inherited true means that all children will have this filter too
	 * @throws NullPointerException when any parameter is null 
	 */
	public ContentNodeFilter(final ContentNodeMetadata nested, final BlackAndWhiteListCallback whiteListCallback, final BlackAndWhiteListCallback blackListCallback, final boolean inherited) throws NullPointerException {
		this(nested,false,whiteListCallback,blackListCallback,inherited);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param nested nested metadata tree
	 * @param mutable true means that metadata tree content can be changed after setting filter
	 * @param whiteListCallback callback to test nodes. 'True' nodes will be visible thru the filter
	 * @param blackListCallback callback to test nodes. 'True' nodes will be excluded from visible nodes
	 * @param inherited true means that all children will have this filter too
	 * @throws NullPointerException when any parameter is null 
	 */
	public ContentNodeFilter(final ContentNodeMetadata nested, final boolean mutable, final BlackAndWhiteListCallback whiteListCallback, final BlackAndWhiteListCallback blackListCallback, final boolean inherited) throws NullPointerException {
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
			this.mutable = mutable;
			this.inherited = inherited;
			
			if (!mutable) {
				children = new ArrayList<>();
				buildContent(nested,children);
			}
			else {
				children = null;
			}
		}
	}
	
	@Override
	public Iterator<ContentNodeMetadata> iterator() {
		if (!mutable) {
			return children.iterator();
		}
		else {
			final List<ContentNodeMetadata>	result = new ArrayList<>();
			
			buildContent(nested,result);
			return result.iterator();
		}
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
		if (!mutable) {
			return children.size();
		}
		else {
			final List<ContentNodeMetadata>	result = new ArrayList<>();
			
			buildContent(nested,result);
			return result.size();
		}
	}

	@Override
	public ContentNodeMetadata getChild(final int index) {
		if (!mutable) {
			return children.get(index);
		}
		else {
			final List<ContentNodeMetadata>	result = new ArrayList<>();
			
			buildContent(nested,result);
			return result.get(index);
		}
	}

	@Override
	public ContentMetadataInterface getOwner() {
		return nested.getOwner();
	}

	private void buildContent(final ContentNodeMetadata metadata, final List<ContentNodeMetadata> forContent) {
		for (ContentNodeMetadata item : nested) {
			if (whiteListCallback.accept(item)) {
				if (!blackListCallback.accept(item)) {
					if (inherited) {
						forContent.add(new ContentNodeFilter(metadata,mutable,whiteListCallback,blackListCallback,inherited));
					}
					else {
						forContent.add(item);
					}
				}
			}
		}
	}
}
