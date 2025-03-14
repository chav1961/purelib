package chav1961.purelib.model.interfaces;

import java.net.URI;
import java.util.Iterator;

import chav1961.purelib.basic.NullIterator;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.model.FieldFormat;

/**
 * <p>This interface describes container on the model tree. It supports a set of operations with model:</p>
 * <ul>
 * <li>{@linkplain #getRoot()} - get root of the model tree.</li>
 * <li>{@linkplain #walkDown(ContentWalker, URI)} - walk (in depth) thru model tree</li>
 * <li>{@linkplain #byUIPath(URI)} - find unique node in the model tree by it's full IU path</li>
 * <li>{@linkplain #byApplicationPath(URI)} - find all nodes in the model tree with application paths associated</li>
 * <li>{@linkplain #mount(URI, ContentMetadataInterface)} - mount another container under any node (except root) in the tree model.</li>
 * <li>{@linkplain #unmount(URI)} - unmount all nodes mound earlier.</li>
 * </ul>
 * <p>Mount operation allows you build total model from pieces of many other models</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.3
 * @last.update 0.0.4
 */
public interface ContentMetadataInterface {
	/**
	 * <p>UI scheme for all URI related to user interface
	 */
	String		UI_SCHEME = "ui";	
	
	/**
	 * <p>Application scheme for all URI associated with any model node</p>
	 */
	String		APPLICATION_SCHEME = "app";	
	
	/**
	 * <p>This interface describes a node in the model tree. This node implements {@linkplain Iterable} interface to get access for all children with for-each loop</p> 
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.3
	 * @last.update 0.0.8
	 */
	public interface ContentNodeMetadata extends Iterable<ContentNodeMetadata>, Cloneable {
		/**
		 * <p>Get node name</p>
		 * @return node name. Can't be null or empty
		 */
		String getName();
		
		/**
		 * <p>does the given node have subtree mound</p>
		 * @return true if yes
		 */
		boolean mounted();
		
		/**
		 * <p>Get node type.</p>
		 * @return node type. Can't be null
		 */
		Class<?> getType();
		
		/**
		 * <p>Get label id, associated with the given node in associated localizer.</p>
		 * @return label id. Can't be null or empty 
		 */
		String getLabelId();
		
		/**
		 * <p>Get tooltip id, associated with the given node in associated localizer.</p>
		 * @return tooltip id. Can be null
		 */
		String getTooltipId();
		
		/**
		 * <p>Get help id, associated with the given node in associated localizer.</p>
		 * @return help id. Can be null
		 */
		String getHelpId();
		
		/**
		 * <p>Get keywords associated with the given node.</p>
		 * @return keywords associated. Can be empty but not null
		 */
		default String[] getKeywords(){return new String[0];}
		
		/**
		 * <p>Get attachments associated with the given node.</p>
		 * @return attachments associated. Can be empty but not null
		 */
		default String[] getAttachments(){return new String[0];}
		
		/**
		 * <p>Get value format associated with the given node</p>
		 * @return value format associated. Can't be null
		 */
		FieldFormat getFormatAssociated();
		
		/**
		 * <p>Get application URI associated with the given node.</p>
		 * @return application URI associated. Can be null
		 */
		URI getApplicationPath();
		
		/**
		 * <p>Get UI path associated with the given node. Path bulds from the root of the tree model as concatenation 
		 * of all the {@linkplain #getRelativeUIPath()} values for the path from the root to the given node</p> 
		 * @return UI path associated. Can't be null
		 */
		URI getUIPath();
		
		/**
		 * <p>Get current relative UI path for the given node.</p>
		 * @return relative UI path associated. Can't be null and will be unique with this node's siblings
		 */
		URI getRelativeUIPath();
		
		/**
		 * <p>Get localizer associated with the given node. Returns parent localizer if doesn't have the own one</p>
		 * @return localizer associated. Can be null if nothing in the tree model path have localizers
		 */
		URI getLocalizerAssociated();
		
		/**
		 * <p>Get icon associated with the given node.</p>
		 * @return icon associated. Can be null
		 */
		URI getIcon();
		
		/**
		 * <p>Get parent of the node.</p>
		 * @return parent of the node. Can be null for root nodes
		 */
		ContentNodeMetadata getParent();
		
		/**
		 * <p>Get number of children in the node</p>
		 * @return number of children or 0 if missing
		 */
		int getChildrenCount();
		
		/**
		 * <p>Get child of the node with the given order index</p> 
		 * @param index index if child 
		 * @return child. Can't be null
		 */
		ContentNodeMetadata getChild(int index);

		/**
		 * <p>Get child of the mode with the given name</p>
		 * @param name name to get node for. Can't be null or empty
		 * @return node found or null (if missing)
		 * @since 0.0.6
		 */
		ContentNodeMetadata getChild(String name);
		
		/**
		 * <p>Get owner of the tree model, inside which the node is</p>
		 * @return owner of the model. Can be null
		 */
		ContentMetadataInterface getOwner();

		/**
		 * <p>Create simple implementation of the {@linkplain ContentNodeMetadata} interface.</p>
		 * @param name node name. Can't be null or empty.
		 * @param type node value class type. Can't be null.
		 * @param localizer localizer to use with. Can't be null.
		 * @param labelId label ID in the localizer to use. Can't be null or empty.
		 * @param format field format to use. Can't be null.
		 * @param uiPath full UI path. Can't be null.
		 * @param appPath application path. Can't be null.
		 * @return implementation created. Can't be null.
		 * @throws IllegalArgumentException any string argument is null or empty.
		 * @throws NullPointerException any non-string argument is null.
		 * @since 0.0.8
		 */
		public static ContentNodeMetadata of(final String name, final Class<?> type, final URI localizer, final String labelId, final FieldFormat format, final URI uiPath, final URI appPath) throws NullPointerException, IllegalArgumentException {
			if (Utils.checkEmptyOrNullString(name)) {
				throw new IllegalArgumentException("Node name can't be null or empty"); 
			}
			else if (type == null) {
				throw new NullPointerException("Node type can't be null"); 
			}
			else if (localizer == null) {
				throw new NullPointerException("Localizer can't be null"); 
			}
			else if (Utils.checkEmptyOrNullString(labelId)) {
				throw new IllegalArgumentException("Label ID can't be null or empty"); 
			}
			else if (format == null) {
				throw new NullPointerException("Field format can't be null"); 
			}
			else if (uiPath == null) {
				throw new NullPointerException("UI path can't be null"); 
			}
			else if (appPath == null) {
				throw new NullPointerException("Applicaiton path can't be null"); 
			}
			else {
				return new ContentNodeMetadata() {
					@Override public boolean mounted() {return false;}
					@Override public URI getUIPath() {return uiPath;}
					@Override public Class<?> getType() {return type;}
					@Override public String getTooltipId() {return labelId;}
					@Override public ContentNodeMetadata getParent() {return null;}
					@Override public ContentMetadataInterface getOwner() {return null;}
					@Override public String getName() {return name;}
					@Override public String getLabelId() {return labelId;}
					@Override public URI getIcon() {return null;}
					@Override public String getHelpId() {return null;}
					@Override public FieldFormat getFormatAssociated() {return format;}
					@Override public int getChildrenCount() {return 0;}
					@Override public ContentNodeMetadata getChild(String name) {return null;}
					@Override public ContentNodeMetadata getChild(int index) {return null;}
					@Override public URI getApplicationPath() {return appPath;}
					@Override public Iterator<ContentNodeMetadata> iterator() {return NullIterator.<ContentNodeMetadata>singleton();}
					@Override public URI getLocalizerAssociated() {return localizer;}

					@Override 
					public URI getRelativeUIPath() {
						final String	path = getUIPath().getPath();
						final String	query = URIUtils.extractQueryFromURI(getUIPath());
						final String 	fragment = getUIPath().getFragment();
						
						return URI.create((path.contains("/") ? path.substring(path.lastIndexOf('/')+1) : path)
										+ (Utils.checkEmptyOrNullString(query) ? "" : "?"+query)
										+ (Utils.checkEmptyOrNullString(fragment) ? "" : "#"+fragment)
										);
					}
				};
			}
		}
	}

	@FunctionalInterface
	public interface ContentWalker {
		ContinueMode process(NodeEnterMode mode, URI applicationPath, URI uiPath, ContentNodeMetadata node);
	}
	
	public interface ContentNodeManipulator {
		boolean isEquals(ContentNodeMetadata left, ContentNodeMetadata right);
		ContentNodeMetadata clone(ContentNodeMetadata source);
		ContentNodeMetadata join(ContentNodeMetadata left, ContentNodeMetadata right);
		ContentNodeMetadata addChildren(ContentNodeMetadata root, ContentNodeMetadata... children);
		ContentNodeMetadata removeChildren(ContentNodeMetadata root);
	}
	
	
	/**
	 * <p>Get root of the model tree</p>
	 * @return root of the model tree
	 */
	ContentNodeMetadata getRoot();
	
	/**
	 * <p>Find all nodes with application path associated. Application path scheme can be {@literal #APPLICATION_SCHEME} only</p>
	 * @param applicationPath application path to find
	 * @return set of nodes found. Can be empty but not null
	 */
	ContentNodeMetadata[] byApplicationPath(URI applicationPath);
	
	/**
	 * <p>Find node by it's unique UI path</p> 
	 * @param uiPath UI path of the node. UI path scheme can be {@literal #UI_SCHEME} only
	 * @return node found or null 
	 * @see ContentNodeMetadata#getUIPath()
	 */
	ContentNodeMetadata byUIPath(URI uiPath);

	/**
	 * <p>Walk thru the model tree (in depth) and execute callback on every node</p>
	 * @param walker callback to execute on every node
	 * @param uiPath UI path to start walking for. To walk all the tree use getRoot().getUIPath() value
	 */
	void walkDown(ContentWalker walker, URI uiPath);
	
	/**
	 * <p>Mount another container under the given node</p>
	 * @param uiPath node unique path
	 * @param model model to mount
	 */
	void mount(URI uiPath, ContentMetadataInterface model);
	
	/**
	 * <p>Unmount all the models from the given node</p>
	 * @param uiPath node unique path
	 * @return all nodes unmound. Can be empty but not null
	 */
	ContentMetadataInterface[] unmount(URI uiPath);
}
