package chav1961.purelib.ui;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import chav1961.purelib.ui.interfacers.ContentMetadataInterface.ContentNodeMetadata;

public class MutableContentNodeMetadata implements ContentNodeMetadata {
	private final List<ContentNodeMetadata>	children = new ArrayList<>();
	private final String		name;
	private final Class<?>		type;
	private final String		labelId, tooltipId, helpId, formatAssociated, relativeUIPath;
	private final URI			localizerAssociated, applicationPath;

	private ContentNodeMetadata	parent = null;
	private boolean 			mounted = false;	
	
	public MutableContentNodeMetadata(final String name, final Class<?> type, final String relativeUIPath, final URI localizerAssociated, final String labelId, final String tooltipId, final String helpId, final String formatAssociated, final URI applicationPath) {
		if (name == null || name.isEmpty()) {
			throw new NullPointerException("Node name can't be null or empty");
		}
		else if (type == null) {
			throw new NullPointerException("Node type can't be null");
		}
		else if (relativeUIPath == null || relativeUIPath.isEmpty()) {
			throw new NullPointerException("Relative UI path can't be null or empty");
		}
		else if (labelId == null || labelId.isEmpty()) {
			throw new NullPointerException("Label identifier can't be null or empty");
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
	public String getFormatAssociated() {
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
			return URI.create("ui:/").resolve("./"+relativeUIPath);
		}
		else {
			return getParent().getUIPath().resolve("./"+relativeUIPath);
		}
	}

	@Override
	public URI getRelativeUIPath() {
		return URI.create("./"+relativeUIPath);
	}
	
	@Override
	public ContentNodeMetadata getParent() {
		return parent;
	}

	@Override
	public int getChildCount() {
		return children.size();
	}
	
	protected void setMounted(final boolean mounted) {
		this.mounted = mounted;
	}
	
	protected void addChild(final ContentNodeMetadata child) {
		children.add(child);
	}
	
	protected void removeChild(final ContentNodeMetadata child) {
		children.remove(child);
	}
	
	protected void setParent(final ContentNodeMetadata parent) {
		this.parent = parent;
	}
}