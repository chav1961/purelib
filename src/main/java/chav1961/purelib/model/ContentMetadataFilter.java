package chav1961.purelib.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.model.interfaces.BlackAndWhiteListCallback;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;

/**
 * <p>This class is a filter for nested {@linkplain ContentMetadataInterface} model. It can be used to exclude some part of model from searching/walking.</p> 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 * @lastUpdate 0.0.4
 */
public class ContentMetadataFilter implements ContentMetadataInterface {
	private final ContentMetadataInterface	nested;
	private final BlackAndWhiteListCallback	blackListCallback, whiteListCallback;

	/**
	 * <p>Constructor of the class.</p>
	 * @param nested nested model to make filter for
	 * @param whiteListCallback callback to filter white list
	 * @throws NullPointerException any argument is null
	 * @since 0.0.4 
	 */
	public ContentMetadataFilter(final ContentMetadataInterface nested, final BlackAndWhiteListCallback whiteListCallback) throws NullPointerException {
		if (nested == null) {
			throw new NullPointerException("Nested metadata can't be null");
		}
		else if (whiteListCallback == null) {
			throw new NullPointerException("White list callback can't be null");
		}
		else {
			this.nested = nested;
			this.whiteListCallback = whiteListCallback;
			this.blackListCallback = (n)->false;
		}
	}	

	/**
	 * <p>Constructor of the class.</p>
	 * @param nested nested model to make filter for
	 * @param whiteListCallback callback to filter white list
	 * @param blackListCallback callback to filter black list
	 * @throws NullPointerException any argument is null 
	 * @since 0.0.4 
	 */
	public ContentMetadataFilter(final ContentMetadataInterface nested, final BlackAndWhiteListCallback whiteListCallback, final BlackAndWhiteListCallback blackListCallback) throws NullPointerException {
		if (nested == null) {
			throw new NullPointerException("Nested metadata can't be null");
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
		}
	}	
	
	/**
	 * <p>Constructor of the class.</p>
	 * @param nested nested model to make filter for
	 * @param whiteList white list of URI will be retained in the nested model (see {@linkplain ContentNodeMetadata#getRelativeUIPath()})
	 * @throws NullPointerException any arguments are null
	 * @throws IllegalArgumentException white list is null, empty or contains nulls inside
	 * @deprecated  
	 */
	@Deprecated(since="0.0.5",forRemoval=true)
	public ContentMetadataFilter(final ContentMetadataInterface nested, final URI[] whiteList) throws NullPointerException, IllegalArgumentException {
		this(nested,(n)->testURI(n,whiteList));
	}

	/**
	 * <p>Constructor of the class.</p>
	 * @param nested nested model to make filter for
	 * @param whiteList white list of URI will be retained in the nested model (see {@linkplain ContentNodeMetadata#getRelativeUIPath()})
	 * @param blackList black list of URI will be excluded from the nested model (see {@linkplain ContentNodeMetadata#getRelativeUIPath()}). Black list priority is higher than white list
	 * @throws NullPointerException any arguments are null
	 * @throws IllegalArgumentException white or black list is null, empty or contains nulls inside 
	 */
	@Deprecated(since="0.0.5",forRemoval=true)
	public ContentMetadataFilter(final ContentMetadataInterface nested, final URI[] whiteList, final URI[] blackList) throws NullPointerException, IllegalArgumentException {
		this(nested,(n)->testURI(n,whiteList),(n)->testURI(n,blackList));
	}

	/**
	 * <p>Constructor of the class.</p>
	 * @param nested nested model to make filter for
	 * @param whiteListRegExp white list URI regular expression will be retained in the nested model (see {@linkplain ContentNodeMetadata#getRelativeUIPath()}) 
	 * @throws NullPointerException any arguments are null
	 * @throws IllegalArgumentException white list is null, empty or has syntax errors 
	 */
	@Deprecated(since="0.0.5",forRemoval=true)
	public ContentMetadataFilter(final ContentMetadataInterface nested, final Pattern whiteListRegExp) throws NullPointerException, IllegalArgumentException {
		this(nested,(n)->testPattern(n,whiteListRegExp));
	}
	
	/**
	 * <p>Constructor of the class.</p>
	 * @param nested nested model to make filter for
	 * @param whiteListRegExp white list URI regular expression will be retained in the nested model (see {@linkplain ContentNodeMetadata#getRelativeUIPath()}) 
	 * @param blackListRegExp white list URI regular expression will be excluded from the nested model (see {@linkplain ContentNodeMetadata#getRelativeUIPath()}). Black list regular expression priority is higher than white list
	 * @throws NullPointerException any arguments are null
	 * @throws IllegalArgumentException white or black list is null, empty or has syntax errors 
	 */
	@Deprecated(since="0.0.5",forRemoval=true)
	public ContentMetadataFilter(final ContentMetadataInterface nested, final Pattern whiteListRegExp, final Pattern blackListRegExp) throws NullPointerException, IllegalArgumentException {
		this(nested,(n)->testPattern(n,whiteListRegExp),(n)->testPattern(n,blackListRegExp));
	} 
	
	@Override
	public ContentNodeMetadata getRoot() {
		return nested.getRoot();
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
			
			walkDown((mode,appPath,uiPath,node)->{
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
			},getRoot().getUIPath());
			return result[0];
		}
	}

	@Override
	public void walkDown(final ContentWalker walker, final URI userInterfacePath) {
		if (walker == null) {
			throw new NullPointerException("Walker can't be null"); 
		}
		else if (userInterfacePath == null) {
			throw new NullPointerException("IP path can't be null"); 
		}
		else {
			nested.walkDown((mode,appPath,uiPath,node)->{
				if (!isAllowed(node)) {
					return ContinueMode.SKIP_CHILDREN;
				}
				else {
					return walker.process(mode,appPath,uiPath,node);
				}
			},userInterfacePath);
		}
	}

	@Override
	public void mount(final URI uiPath, final ContentMetadataInterface model) {
		if (uiPath == null) {
			throw new NullPointerException("UI path to mount can't be null");
		}
		else if (model == null) {
			throw new NullPointerException("Model to mount can't be null");
		}
		else {
			nested.mount(uiPath,model);
		}
	}

	@Override
	public ContentMetadataInterface[] unmount(final URI uiPath) {
		if (uiPath == null) {
			throw new NullPointerException("UI path to unmount can't be null");
		}
		else {
			return nested.unmount(uiPath);
		}
	}

	boolean isAllowed(final ContentNodeMetadata node) {
		if (whiteListCallback.accept(node)) {
			return !blackListCallback.accept(node);
		}
		else {
			return false;
		}
	}
	
	static boolean testURI(final ContentNodeMetadata node, final URI[] uris) {
		for (URI item : uris) {
			if (isEquals(node.getRelativeUIPath(),item)) {
				return true;
			}
		}
		return false;
	}

	static boolean testPattern(final ContentNodeMetadata node, final Pattern pattern) {
		return pattern.matcher(node.getRelativeUIPath().toString()).matches();
	}
	
	static boolean isEquals(final URI tested, final URI template) {
		return tested.getPath().startsWith(template.getPath());
	}

	@Override
	public String toString() {
		return "ContentMetadataFilter [nested=" + nested + "]";
	}
}
