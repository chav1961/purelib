package chav1961.purelib.ui.swing;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JTextField;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.DebuggingException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.testing.OrdinalTestCategory;
import chav1961.purelib.testing.SwingTestingUtils;
import chav1961.purelib.testing.SwingUnitTest;
import chav1961.purelib.testing.UITestCategory;

public class SimpleNavigatorTreeTest {
	final JFrame		root = new JFrame();
	final JTextField	text = new JTextField();
	final Locale		en = Locale.forLanguageTag("en");
	final Locale		ru = Locale.forLanguageTag("ru");

	volatile boolean	action = false;
	volatile String		actionCommand = null;
	
	@Before
	public void prepare() {
		root.getContentPane().setLayout(new BorderLayout());
		text.setName("TEXT");
		root.getContentPane().add(text,BorderLayout.NORTH);
		SwingUtils.centerMainWindow(root,0.2f);
	}

	@After
	public void unprepare() {
		root.dispose();
	}
	
	@Category(OrdinalTestCategory.class)
	@Test
	public void basicTest() throws EnvironmentException {
		final ContentMetadataInterface	mdi = ContentModelFactory.forXmlDescription(this.getClass().getResourceAsStream("Application.xml"));
		final ContentNodeMetadata		metadata = mdi.byUIPath(URI.create("ui:/model/navigation.top.navigatorTree"));
		final ContentNodeMetadata		clicked = mdi.byUIPath(URI.create("ui:/model/navigation.top.navigatorTree/navigation.leaf.menu.project.new"));
		final SimpleNavigatorTree		tree = new SimpleNavigatorTree(PureLibSettings.PURELIB_LOCALIZER,metadata);
		final ActionListener			listener = (e)->{action = true; actionCommand = e.getActionCommand();}; 
		
		Assert.assertEquals(metadata,tree.getNodeMetadata());
		tree.addActionListener(listener);
		
		try{tree.addActionListener(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		Assert.assertTrue(tree.findAndSelect(clicked.getUIPath()));
		Assert.assertEquals(clicked,tree.getSelectedNodeMetadata());
		
		Assert.assertFalse(tree.findAndSelect(URI.create("unknown:/")));
		Assert.assertNull(tree.getSelectedNodeMetadata());
		
		Assert.assertFalse(action);
		Assert.assertNull(actionCommand);
		
		tree.findAndDoubleClick(clicked.getUIPath());
		
		Assert.assertTrue(action);
		Assert.assertEquals(clicked.getApplicationPath().toString(),actionCommand);
		
		tree.removeActionListener(listener);
		try{tree.removeActionListener(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		try{new SimpleNavigatorTree(null,metadata);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{new SimpleNavigatorTree(PureLibSettings.PURELIB_LOCALIZER,null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
		try{new SimpleNavigatorTree(PureLibSettings.PURELIB_LOCALIZER,mdi.getRoot());
			Assert.fail("Mandatory exception was not detected (1-st argument is not a navigator reference)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Category(UITestCategory.class)
	@Test
	public void uiTest() throws NullPointerException, EnvironmentException, InterruptedException, DebuggingException {
		final ContentMetadataInterface	mdi = ContentModelFactory.forXmlDescription(this.getClass().getResourceAsStream("Application.xml"));
		final ContentNodeMetadata		metadata = mdi.byUIPath(URI.create("ui:/model/navigation.top.navigatorTree"));
		final ContentNodeMetadata		clicked = mdi.byUIPath(URI.create("ui:/model/navigation.top.navigatorTree/navigation.leaf.menu.project.new"));
		final SimpleNavigatorTree		tree = new SimpleNavigatorTree(PureLibSettings.PURELIB_LOCALIZER,metadata);
		final ActionListener			listener = (e)->{action = true; actionCommand = e.getActionCommand();};
		final SwingUnitTest				sut = new SwingUnitTest(root);

		tree.addActionListener(listener);
		
		root.getContentPane().add(tree,BorderLayout.CENTER);
		root.setVisible(true);
		SwingTestingUtils.syncRequestFocus(root);
	
		Assert.assertFalse(action);
		sut.select(URIUtils.removeQueryFromURI(metadata.getUIPath()).toString()).move(5,5).click(MouseEvent.BUTTON1,2);
		Assert.assertTrue(action);
		
		Assert.assertEquals(clicked,tree.getSelectedNodeMetadata());
		
		tree.removeActionListener(listener);
		root.setVisible(false);
	}
}
