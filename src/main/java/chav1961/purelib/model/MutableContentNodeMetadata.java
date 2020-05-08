package chav1961.purelib.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;

public class MutableContentNodeMetadata implements ContentNodeMetadata, Cloneable {
	private final List<ContentNodeMetadata>	children = new ArrayList<>();
	private final String				name;
	private final Class<?>				type;
	private final String				labelId, tooltipId, helpId, relativeUIPath;
	private final FieldFormat			formatAssociated;
	private final URI					localizerAssociated, applicationPath, iconURI;

	private ContentMetadataInterface	owner = null;
	private ContentNodeMetadata			parent = null;
	private boolean 					mounted = false;	
	
	public MutableContentNodeMetadata(final String name, final Class<?> type, final String relativeUIPath, final URI localizerAssociated, final String labelId, final String tooltipId, final String helpId, final FieldFormat formatAssociated, final URI applicationPath, final URI iconURI) {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Node name can't be null or empty");
		}
		else if (type == null) {
			throw new NullPointerException("Node type can't be null");
		}
		else if (relativeUIPath == null || relativeUIPath.isEmpty()) {
			throw new IllegalArgumentException("Relative UI path can't be null or empty");
		}
		else if (labelId == null || labelId.isEmpty()) {
			throw new IllegalArgumentException("Label identifier can't be null or empty");
		}
		else if (localizerAssociated != null && !Localizer.LOCALIZER_SCHEME.equals(localizerAssociated.getScheme())) {
			throw new IllegalArgumentException("Localizer ["+localizerAssociated+"]: invalid localizer scheme ["+localizerAssociated.getScheme()+"], must be ["+Localizer.LOCALIZER_SCHEME+"]");
		}
		else if (applicationPath != null && !ContentMetadataInterface.APPLICATION_SCHEME.equals(applicationPath.getScheme())) {
			throw new IllegalArgumentException("Application path ["+applicationPath+"]: invalid applicaiton path scheme ["+applicationPath.getScheme()+"], must be ["+ContentMetadataInterface.APPLICATION_SCHEME+"]");
		}
		else {
			this.name = name;
			this.type = type; 
			this.relativeUIPath = relativeUIPath;
			this.localizerAssociated = localizerAssociated;
			this.labelId = labelId;
			this.tooltipId = tooltipId;
			this.helpId = helpId;
			this.formatAssociated = formatAssociated;
			this.applicationPath = applicationPath;
			this.iconURI = iconURI;
		}
	}
	
	@Override
	public Iterator<ContentNodeMetadata> iterator() {
		return children.iterator();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean mounted() {
		return mounted;
	}

	@Override
	public Class<?> getType() {
		return type;
	}

	@Override
	public String getLabelId() {
		return labelId;
	}

	@Override
	public String getTooltipId() {
		return tooltipId;
	}

	@Override
	public String getHelpId() {
		return helpId;
	}

	@Override
	public FieldFormat getFormatAssociated() {
		return formatAssociated;
	}

	@Override
	public URI getLocalizerAssociated() {
		if (localizerAssociated != null) {
			return localizerAssociated;
		}
		else if (getParent() != null) {
			return getParent().getLocalizerAssociated();
		}
		else {
			return null;
		}
	}

	@Override
	public URI getApplicationPath() {
		return applicationPath;
	}

	@Override
	public URI getUIPath() {
		if (getParent() == null) {
			return URI.create(ContentMetadataInterface.UI_SCHEME+":/").resolve(getRelativeUIPath()).normalize();
		}
		else {
			final URI	parentPath = getParent().getUIPath(); 
			
			return parentPath.resolve(parentPath.getPath()+"/"+relativeUIPath).normalize();
		}
	}

	@Override
	public URI getRelativeUIPath() {
		return URI.create("./"+relativeUIPath);
	}

	@Override
	public URI getIcon() {
		return iconURI;
	}
	
	@Override
	public ContentNodeMetadata getParent() {
		return parent;
	}

	@Override
	public int getChildrenCount() {
		return children.size();
	}
	
	@Override
	public ContentNodeMetadata getChild(final int index) {
		if (index < 0 || index >= children.size()) {
			throw new IllegalArgumentException("Child index ["+index+"] out of range 0.."+(children.size()-1)); 
		}
		else {
			return children.get(index);
		}
	}

	@Override
	public ContentMetadataInterface getOwner() {
		if (owner != null) {
			return owner;
		}
		else if (parent != null) {
			return parent.getOwner();
		}
		else {
			return null;
		}
	}
	
	protected void setMounted(final boolean mounted) {
		this.mounted = mounted;
	}
	
	protected void addChild(final ContentNodeMetadata child) {
		children.add(child);
		if (child instanceof MutableContentNodeMetadata) {
			((MutableContentNodeMetadata)child).setParent(this);
		}
	}
	
	protected void removeChild(final ContentNodeMetadata child) {
		children.remove(child);
		if (child instanceof MutableContentNodeMetadata) {
			((MutableContentNodeMetadata)child).setParent(null);
		}
	}
	
	protected void setParent(final ContentNodeMetadata parent) {
		this.parent = parent;
	}

	protected void setOwner(final ContentMetadataInterface owner) {
		this.owner = owner;
	}

	@Override
	public String toString() {
		return "MutableContentNodeMetadata [name=" + name + ", type=" + type + ", labelId=" + labelId + ", tooltipId="
				+ tooltipId + ", helpId=" + helpId + ", relativeUIPath=" + relativeUIPath + ", formatAssociated="
				+ formatAssociated + ", localizerAssociated=" + localizerAssociated + ", applicationPath="
				+ applicationPath + ", iconURI=" + iconURI + "]";
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		final MutableContentNodeMetadata	clone = (MutableContentNodeMetadata)super.clone();

		clone.children.clear();
		for (ContentNodeMetadata item : this) {
			clone.addChild(((MutableContentNodeMetadata)((MutableContentNodeMetadata)item).clone()));
		}
		return clone;
	}
}
