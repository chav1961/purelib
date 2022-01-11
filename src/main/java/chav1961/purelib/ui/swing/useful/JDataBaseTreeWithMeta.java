package chav1961.purelib.ui.swing.useful;

import java.sql.ResultSet;
import java.util.Locale;

import javax.swing.JTree;
import javax.swing.tree.TreeModel;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.interfaces.FormManager;

public class JDataBaseTreeWithMeta extends JTree implements NodeMetadataOwner, LocaleChangeListener {
	private static final long serialVersionUID = -834136905259412041L;

	private final ContentNodeMetadata	meta;
	private final Localizer				localizer;
	
	
	public JDataBaseTreeWithMeta(final ContentNodeMetadata meta, final Localizer localizer) throws NullPointerException {
		super();
		// TODO Auto-generated constructor stub
		if (meta == null) {
			throw new NullPointerException("Metadata can't be null"); 
		}
		else if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
		}
		else {
			this.meta = meta;
			this.localizer = localizer;
		}
	}
	
	public JDataBaseTreeWithMeta(final ContentNodeMetadata meta, final Localizer localizer, final TreeModel newModel) throws NullPointerException {
		super(newModel);
		// TODO Auto-generated constructor stub
		if (meta == null) {
			throw new NullPointerException("Metadata can't be null"); 
		}
		else if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
		}
		else {
			this.meta = meta;
			this.localizer = localizer;
		}
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		// TODO Auto-generated method stub
		
	}
	
	public <K,V> void assignResultSetAndFormManager(final ResultSet rs, final String currentKey, final String parentKey, final FormManager<K,V> mgr) throws NullPointerException, IllegalArgumentException {
		// TODO Auto-generated method stub
		if (rs == null) {
			throw new NullPointerException("Result set can't be null");
		}
		else if (currentKey == null || currentKey.isEmpty()) {
			throw new IllegalArgumentException("Current key can't be null or empty");
		}
		else if (parentKey == null || parentKey.isEmpty()) {
			throw new IllegalArgumentException("Current key can't be null or empty");
		}
	}

	public void resetResultSetAndFormManager() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public ContentNodeMetadata getNodeMetadata() {
		return meta;
	}

}
