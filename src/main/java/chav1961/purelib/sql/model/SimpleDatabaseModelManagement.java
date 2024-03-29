package chav1961.purelib.sql.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.sql.model.interfaces.DatabaseModelManagement;

/**
 * <p>This class implements simple database model management for {@linkplain SimpleDottedVersion} enumerations.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.6
 */
public class SimpleDatabaseModelManagement implements DatabaseModelManagement<SimpleDottedVersion> {
	private static final String		DEFAULT_VERSION = "0.0";
			
	private final VersionAndModel[]	content, inversContent;
	
	/**
	 * <p>Constructor of the class.</p>
	 * @param jsonModels list of models to load. Can't be null, empty or contains nulls inside
	 * @throws EnvironmentException illegal models detected
	 * @throws NullPointerException any parameter is null
	 * @throws IllegalArgumentException json list is empty or contains nulls inside
	 */
	public SimpleDatabaseModelManagement(final URI... jsonModels) throws EnvironmentException, NullPointerException, IllegalArgumentException {
		this(PureLibSettings.CURRENT_LOGGER, jsonModels);
	}
	
	/**
	 * <p>Constructor of the class.</p>
	 * @param logger logger to print errors to. Can't be null
	 * @param jsonModels list of models to load. Can't be null, empty or contains nulls inside
	 * @throws EnvironmentException illegal models detected
	 * @throws NullPointerException any parameter is null
	 * @throws IllegalArgumentException json list is empty or contains nulls inside
	 */
	public SimpleDatabaseModelManagement(final LoggerFacade logger, final URI... jsonModels) throws EnvironmentException, NullPointerException, IllegalArgumentException {
		if (logger == null) {
			throw new NullPointerException("Logger can't be null"); 
		}
		else if (jsonModels == null || Utils.checkArrayContent4Nulls(jsonModels) >= 0) {
			throw new IllegalArgumentException("Json model list is null or contains nulls inside");
		}
		else {
			try(final LoggerFacade			trans = logger.transaction(this.getClass().getSimpleName())) {
				final List<VersionAndModel>	list = new ArrayList<>(); 
				
				for (URI item : jsonModels) {
					try(final InputStream	is = item.toURL().openStream();
						final Reader		rdr = new InputStreamReader(is, PureLibSettings.DEFAULT_CONTENT_ENCODING)) {
						final ContentMetadataInterface		mdi = ContentModelFactory.forJsonDescription(rdr);
						final SimpleDottedVersion			version = SQLModelUtils.extractVersionFromModel(mdi.getRoot(), new SimpleDottedVersion(DEFAULT_VERSION));
						
						list.add(new VersionAndModel(version, mdi));
					} catch (IOException e) {
						throw new EnvironmentException("Item ["+item+"] - I/O error: "+e.getLocalizedMessage(), e); 
					}
					
				}
				this.content = list.toArray(new VersionAndModel[list.size()]);
				this.inversContent = this.content.clone();
				
				Arrays.sort(this.content);
				Arrays.sort(this.inversContent,(o1,o2)->-o1.compareTo(o2));
				trans.rollback();
			}
		}
	}
	
	@Override
	public int size() {
		return content.length;
	}

	@Override
	public SimpleDottedVersion getVersion(final int versionNumber) {
		if (versionNumber < 0 || versionNumber >= content.length) {
			throw new IllegalArgumentException("Vesion number ["+versionNumber+"] out of range 0.."+(content.length-1));
		}
		else {
			return content[versionNumber].getVersion();
		}
	}

	@Override
	public ContentNodeMetadata getModel(int versionNumber) {
		if (versionNumber < 0 || versionNumber >= content.length) {
			throw new IllegalArgumentException("Vesion number ["+versionNumber+"] out of range 0.."+(content.length-1));
		}
		else {
			return content[versionNumber].getModel();
		}
	}

	@Override
	public Iterable<DatabaseModelContent<SimpleDottedVersion>> allAscending() {
		return Arrays.asList(content);
	}

	@Override
	public Iterable<DatabaseModelContent<SimpleDottedVersion>> allDescending() {
		return Arrays.asList(inversContent);
	}

	/**
	 * <p>Collect mode URIs from descriptor (for example, file). Each descriptor must occupy exactly one line in the content and must be an URI relative to modelListDescriptor parameter. 
	 * Empty lines and line comments (started with # sign) are supported.</p> 
	 * @param modelListDescriptor any stream contains list of model descriptors. 
	 * @return URIs list connected. Can be empty but not null
	 * @throws IOException on any I/O errors
	 * @throws link NullPointerException when parameter is null
	 */
	public static URI[] collectModels(final URI modelListDescriptor) throws IOException, NullPointerException {
		if (modelListDescriptor == null) {
			throw new NullPointerException("Model lis descriptor can't be null");
		}
		else {
			final List<URI>		result = new ArrayList<>();
			
			try(final InputStream		is = modelListDescriptor.toURL().openStream();
				final Reader			rdr = new InputStreamReader(is, PureLibSettings.DEFAULT_CONTENT_ENCODING);
				final BufferedReader	brdr = new BufferedReader(rdr)) {
				String		line;
				
				while ((line = brdr.readLine()) != null) {
					if (!line.trim().isEmpty() && !line.trim().startsWith("#")) {
						result.add(modelListDescriptor.resolve(line));
					}
				}
			}
			return result.toArray(new URI[result.size()]);
		}
	}
	
	private static class VersionAndModel implements Comparable<VersionAndModel>, DatabaseModelContent<SimpleDottedVersion> {
		private final SimpleDottedVersion		version;
		private final ContentMetadataInterface	model;
		
		public VersionAndModel(SimpleDottedVersion version, ContentMetadataInterface model) {
			this.version = version;
			this.model = model;
		}

		public SimpleDottedVersion getVersion() {
			return version;
		}

		public ContentNodeMetadata getModel() {
			return model.getRoot();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((model == null) ? 0 : model.hashCode());
			result = prime * result + ((version == null) ? 0 : version.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			VersionAndModel other = (VersionAndModel) obj;
			if (model == null) {
				if (other.model != null) return false;
			} else if (!model.equals(other.model)) return false;
			if (version == null) {
				if (other.version != null) return false;
			} else if (!version.equals(other.version)) return false;
			return true;
		}

		@Override
		public int compareTo(final VersionAndModel o) {
			if (o == null) {
				return 1;
			}
			else {
				return o.getVersion().compareTo(this.getVersion());
			}
		}

		@Override
		public String toString() {
			return "VersionAndModel [version=" + version + ", model=" + model + "]";
		}
	}
}
