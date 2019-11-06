package chav1961.purelib.ui.swing.useful;

import java.awt.Component;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Locale;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;

public class JNavigatorTree<Area extends Enum<Area>> extends JTree implements LocaleChangeListener {
	private static final long 				serialVersionUID = 1606564401519679798L;
	private static final String				NAVIGATION_ROOT = "NAVIGATION.ROOT";

	private final Class<Area>				areaClass;
	private final Localizer					localizer;
	private final DefaultMutableTreeNode	root;
	private final ContentMetadataInterface	rootContent;
	
	public interface WalkCallback {
		ContinueMode walk(NodeEnterMode mode, DefaultMutableTreeNode node);
	}

	public interface ActionProcessor {
		void processAction(DefaultMutableTreeNode node, URI action);
	}
	
	public JNavigatorTree(final Class<Area> areaClass, final Localizer localizer) throws LocalizationException, SyntaxException, ContentException {
		if (areaClass == null) {
			throw new NullPointerException("Area class can't be null");
		}
		else if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else {
			this.areaClass = areaClass;
			this.localizer = localizer;
			this.rootContent = ContentModelFactory.forAnnotatedClass(areaClass);
			this.root = new DefaultMutableTreeNode(localizer.getValue(NAVIGATION_ROOT),true);
			
			for (Area item : areaClass.getEnumConstants()) {
				root.add(new DefaultMutableTreeNode(item,true));
			}
			this.setCellRenderer((tree,value,selected,expanded,leaf,row,hasFocus)->{
				return makeCellRenderer(tree,value,selected,expanded,leaf,row,hasFocus);
			});
		}
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		// TODO Auto-generated method stub
	}
	
	public void load(final Area root, final InputStream settings, final boolean ReadOnly) {
		
	}

	public void load(final Area root, final ContentNodeMetadata settings, final boolean ReadOnly) {
		
	}
	
	public void save(final Area root, final OutputStream settings) {
		
	}
	
	public void walkDown(final Area root, final WalkCallback callback) {
		
	}

	private Component makeCellRenderer(final JTree tree, final Object value, final boolean selected, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
		// TODO Auto-generated method stub
		if (value == null) {
			
		}
		else {
			if (areaClass.isAssignableFrom(value.getClass())) {
				
			}
			else {
				
			}
		}
		return null;
	}

}
