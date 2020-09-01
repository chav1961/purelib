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
import chav1961.purelib.model.interfaces.ContentMetadataInterface;

/**
 * <p>This class is a filter for nested {@linkplain ContentMetadataInterface} model. It can be used to exclude some part of model from searching/walking.</p> 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 * @lastUpdate 0.0.4
 */
public class ContentMetadataFilter implements ContentMetadataInterface {
	private static final URI[]				EMPTY = new URI[0];
	
	private final ContentMetadataInterface	nested;
	private final URI[]						whiteList, blackList;
	private final Pattern					white, black;

	/**
	 * <p>Constructor of the class.</p>
	 * @param nested nested model to make filter for
	 * @param whiteList white list of URI will be retained in the nested model (see {@linkplain ContentNodeMetadata#getRelativeUIPath()})
	 * @throws NullPointerException any arguments are null
	 * @throws IllegalArgumentException white list is null, empty or contains nulls inside 
	 */
	public ContentMetadataFilter(final ContentMetadataInterface nested, final URI[] whiteList) throws NullPointerException, IllegalArgumentException {
		if (nested == null) {
			throw new NullPointerException("Nested metadata can't be null");
		}
		else if (whiteList == null || whiteList.length == 0) {
			throw new IllegalArgumentException("White list can't be null or empty array");
		}
		else {
			boolean	found = false;
			
			for (URI item : whiteList) {
				if (item == null) {
					throw new IllegalArgumentException("Null URI inside white list!");
				}
				else if (isEquals(nested.getRoot().getUIPath(),item)) {
					found = true;
				}
			}
			if (!found) {
				throw new IllegalArgumentException("Root of the nested metadata is not resolved by white list! Metadata will always be unavailable");
			}
			else {
				this.nested = nested;
				this.white = null;
				this.whiteList = whiteList;
				this.black = null;
				this.blackList = EMPTY;
			}
		}
	}

	/**
	 * <p>Constructor of the class.</p>
	 * @param nested nested model to make filter for
	 * @param whiteList white list of URI will be retained in the nested model (see {@linkplain ContentNodeMetadata#getRelativeUIPath()})
	 * @param blackList black list of URI will be excluded from the nested model (see {@linkplain ContentNodeMetadata#getRelativeUIPath()}). Black list priority is higher than white list
	 * @throws NullPointerException any arguments are null
	 * @throws IllegalArgumentException white or black list is null, empty or contains nulls inside 
	 */
	public ContentMetadataFilter(final ContentMetadataInterface nested, final URI[] whiteList, final URI[] blackList) throws NullPointerException, IllegalArgumentException {
		if (nested == null) {
			throw new NullPointerException("Nested metadata can't be null");
		}
		else if (whiteList == null || whiteList.length == 0) {
			throw new IllegalArgumentException("White list can't be null or empty array");
		}
		else if (blackList == null) {
			throw new NullPointerException("Black list can't be null");
		}
		else {
			boolean	foundWhite = false, foundBlack = false;
			
			for (URI item : whiteList) {
				if (item == null) {
					throw new IllegalArgumentException("Null URI inside white list!");
				}
				else if (isEquals(nested.getRoot().getUIPath(),item)) {
					foundWhite = true;
				}
			}
			for (URI item : blackList) {
				if (item == null) {
					throw new IllegalArgumentException("Null URI inside black list!");
				}
				else if (isEquals(nested.getRoot().getUIPath(),item)) {
					foundBlack = true;
				}
			}
			if (!foundWhite) {
				throw new IllegalArgumentException("Root of the nested metadata is not resolved by white list! Metadata will always be unavailable");
			}
			else if (foundBlack) {
				throw new IllegalArgumentException("Root of the nested metadata is resolved by black list! Metadata will always be unavailable");
			}
			else {
				this.nested = nested;
				this.white = null;
				this.whiteList = whiteList;
				this.black = null;
				this.blackList = blackList;
			}
		}
	}

	/**
	 * <p>Constructor of the class.</p>
	 * @param nested nested model to make filter for
	 * @param whiteListRegExp white list URI regular expression will be retained in the nested model (see {@linkplain ContentNodeMetadata#getRelativeUIPath()}) 
	 * @throws NullPointerException any arguments are null
	 * @throws IllegalArgumentException white list is null, empty or has syntax errors 
	 */
	public ContentMetadataFilter(final ContentMetadataInterface nested, final String whiteListRegExp) throws NullPointerException, IllegalArgumentException {
		if (nested == null) {
			throw new NullPointerException("Nested metadata can't be null");
		}
		else if (whiteListRegExp == null || whiteListRegExp.isEmpty()) {
			throw new IllegalArgumentException("White list can't be null or empty string");
		}
		else {
			try{this.white = Pattern.compile(whiteListRegExp);
			
				if (!this.white.matcher(nested.getRoot().getUIPath().toString()).matches()) {
					throw new IllegalArgumentException("Root of the nested metadata is not resolved by white list regular expression! Metadata will always be unavailable");
				}
				else {
					this.nested = nested;
					this.whiteList = EMPTY;
					this.black = null;
					this.blackList = EMPTY;
				}
			} catch(PatternSyntaxException exc) {
				throw new IllegalArgumentException("While list pattern ["+whiteListRegExp+"] : "+exc.getLocalizedMessage());
			}
		}		
	}
	
	/**
	 * <p>Constructor of the class.</p>
	 * @param nested nested model to make filter for
	 * @param whiteListRegExp white list URI regular expression will be retained in the nested model (see {@linkplain ContentNodeMetadata#getRelativeUIPath()}) 
	 * @param blackListRegExp white list URI regular expression will be excluded from the nested model (see {@linkplain ContentNodeMetadata#getRelativeUIPath()}). Black list regular expression priority is higher than white list
	 * @throws NullPointerException any arguments are null
	 * @throws IllegalArgumentException white or black list is null, empty or has syntax errors 
	 */
	public ContentMetadataFilter(final ContentMetadataInterface nested, final String whiteListRegExp, final String blackListRegExp) throws NullPointerException, IllegalArgumentException {
		if (nested == null) {
			throw new NullPointerException("Nested metadata can't be null");
		}
		else if (whiteListRegExp == null || whiteListRegExp.isEmpty()) {
			throw new IllegalArgumentException("White list can't be null or empty string");
		}
		else {
			try{this.white = Pattern.compile(whiteListRegExp);

				try{this.black = Pattern.compile(blackListRegExp);
					if (!this.white.matcher(nested.getRoot().getUIPath().toString()).matches()) {
						throw new IllegalArgumentException("Root of the nested metadata is not resolved by white list regular expression! Metadata will always be unavailable");
					}
					else if (this.black.matcher(nested.getRoot().getUIPath().toString()).matches()) {
						throw new IllegalArgumentException("Root of the nested metadata is resolved by black list regular expression! Metadata will always be unavailable");
					}
					else {
						this.nested = nested;
						this.whiteList = EMPTY;
						this.blackList = EMPTY;
					}
				} catch(PatternSyntaxException exc) {
					throw new IllegalArgumentException("Black list pattern ["+blackListRegExp+"] : "+exc.getLocalizedMessage());
				}
			} catch(PatternSyntaxException exc) {
				throw new IllegalArgumentException("While list pattern ["+whiteListRegExp+"] : "+exc.getLocalizedMessage());
			}
		}		
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
				if (!isAllowed(uiPath)) {
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

	/**
	 * <p>Is given URI allowed for the given filter</p>
	 * @param path relative URI path to test
	 * @return true if yes
	 */
	boolean isAllowed(final URI path) {
		if (whiteList.length > 0) {
			boolean	whiteFound = false;
			
			for (URI item : whiteList) {
				if (isEquals(path,item)) {
					whiteFound = true;
					break;
				}
			}
			if (!whiteFound) {
				return false;
			}
		}
		if (white != null) {
			if (!white.matcher(path.getPath()).matches()) {
				return false;
			}
		}
		if (blackList.length > 0) {
			boolean	blackFound = false;
			
			for (URI item : blackList) {
				if (isEquals(path,item)) {
					blackFound = true;
					break;
				}
			}
			if (blackFound) {
				return false;
			}
		}
		if (black != null) {
			if (black.matcher(path.getPath()).matches()) {
				return false;
			}
		}
		return true;
	}
	
	static boolean isEquals(final URI tested, final URI template) {
		return tested.getPath().startsWith(template.getPath());
	}
	
	@Override
	public String toString() {
		return "ContentMetadataFilter [nested=" + nested + ", whiteList=" + Arrays.toString(whiteList) + ", blackList=" + Arrays.toString(blackList) + ", white=" + white + ", black=" + black + "]";
	}
}
