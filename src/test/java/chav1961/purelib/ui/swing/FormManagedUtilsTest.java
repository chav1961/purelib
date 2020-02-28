package chav1961.purelib.ui.swing;

import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.GettersAndSettersFactory.GetterAndSetter;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.exceptions.TestException;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.interfaces.Action;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.interfaces.RefreshMode;
import chav1961.purelib.ui.swing.FormManagedUtils.FormManagerParserCallback;
import chav1961.purelib.ui.swing.FormManagedUtils.MarkupParserCallback;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;

public class FormManagedUtilsTest {
	@Test
	public void seekAndCallTest() throws Exception {
		final SeekAndCallTest	sact = new SeekAndCallTest();
		
		try{FormManagedUtils.seekAndCall(sact,URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":/"+SeekAndCallTest.class.getCanonicalName()+"/callMethod1().test"));
			Assert.fail("Mandatory exception was not detected (Test exception awaited)");
		} catch (TestException  exc) {
		}
		
		Assert.assertEquals(RefreshMode.DEFAULT,FormManagedUtils.seekAndCall(sact,URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":/"+SeekAndCallTest.class.getCanonicalName()+"/callMethod2().test")));
		Assert.assertEquals(RefreshMode.EXIT,FormManagedUtils.seekAndCall(sact,URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":/"+SeekAndCallTest.class.getCanonicalName()+"/callMethod3().test")));
		Assert.assertEquals(RefreshMode.DEFAULT,FormManagedUtils.seekAndCall(sact,URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":/"+SeekAndCallTest.class.getCanonicalName()+"/unknown().test")));

		Assert.assertEquals(RefreshMode.DEFAULT,FormManagedUtils.seekAndCall(new String(),URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":/"+SeekAndCallTest.class.getCanonicalName()+"/unknown().test")));
		try{FormManagedUtils.seekAndCall(sact,URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":/"+SeekAndCallTest.class.getCanonicalName()+"/callMethod4().test"));
			Assert.fail("Mandatory exception was not detected (Returned type of the methos is neither void nor RefreshMode)");
		} catch (IllegalArgumentException exc) {
		}
	}

	@Test
	public void parseModel4FormsTest() throws SyntaxException, LocalizationException, ContentException {
		final ContentMetadataInterface	mdi = ContentModelFactory.forAnnotatedClass(SeekAndCallTest.class);
		final JComponentMonitor			mon = (JComponentMonitor) Proxy.newProxyInstance(this.getClass().getClassLoader(),new Class<?>[]{JComponentMonitor.class},(a,b,c)->null);
		final FormManagerParserCallback	callback = new FormManagerParserCallback() {
											@Override
											public void processActionButton(final ContentNodeMetadata metadata, final JButtonWithMeta button) throws ContentException {
												Assert.assertEquals("press",metadata.getName());
											}
								
											@Override
											public void processField(final ContentNodeMetadata metadata, final JLabel fieldLabel, final JComponent fieldComponent, final GetterAndSetter gas, final boolean isModifiable) throws ContentException {
												// TODO Auto-generated method stub
												Assert.assertEquals("testSet1",metadata.getName());
												Assert.assertEquals("key1",metadata.getLabelId());
												Assert.assertTrue(isModifiable);
											}
										};
		 
		FormManagedUtils.parseModel4Form(PureLibSettings.CURRENT_LOGGER,mdi,SeekAndCallTest.class,mon,callback);
	}


	@Test
	public void parseMarkUpTest() throws SyntaxException, LocalizationException, ContentException {
		final boolean[]				called = new boolean[5];
		final List<PlainTextKeeper>	plain = new ArrayList<>();
		final List<SeparatorKeeper>	separator = new ArrayList<>();
		final List<FieldKeeper>		field = new ArrayList<>();
		final List<ContentKeeper>	content = new ArrayList<>();
		final MarkupParserCallback	callback = new MarkupParserCallback() {
										@Override
										public void placePlainText(int x, int y, int width, int height, boolean bold, boolean italic, boolean caption, String content) throws ContentException {
											plain.add(new PlainTextKeeper(x, y, width, height, bold, italic, caption, content));
											called[0] = true;
										}
							
										@Override
										public void placeSeparator(int x, int y, int width, int height) throws ContentException {
											separator.add(new SeparatorKeeper(x, y, width, height));
											called[1] = true;
										}
							
										@Override
										public void placeField(int x, int y, int width, int height, String componentName, String initialValue) throws ContentException {
											field.add(new FieldKeeper(x, y, width, height, componentName, initialValue));
											called[2] = true;
										}
							
										@Override
										public void pushContent(int x, int y, int width, int height, String caption) throws ContentException {
											content.add(new ContentKeeper(x, y, width, height, caption));
											called[3] = true;
										}
							
										@Override
										public void popContent() throws ContentException {
											called[4] = true;
										}
									};
		
		// Empty content
		Arrays.fill(called,false);
		plain.clear();
		separator.clear();
		field.clear();
		content.clear();
		
		FormManagedUtils.parseMarkup("   ",callback);
		
		Assert.assertArrayEquals(new boolean[] {false,false,false,false,false},called);
		
		// Plain text only
		Arrays.fill(called,false);
		plain.clear();
		separator.clear();
		field.clear();
		content.clear();
		
		FormManagedUtils.parseMarkup(" A ",callback);
		
		Assert.assertArrayEquals(new boolean[] {true,false,false,false,false},called);
		Assert.assertEquals(1,plain.size());
		Assert.assertEquals(new PlainTextKeeper(2,0,1,1,false,false,false,"A"),plain.get(0));
		
	}
	
	private static class PlainTextKeeper {
		final int 		x, y, width, height;
		final boolean	bold, italic, caption;
		final String	text;

		public PlainTextKeeper(final int x, final int y, final int width, final int height, final boolean bold, final boolean italic, final boolean caption, final String text) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.bold = bold;
			this.italic = italic;
			this.caption = caption;
			this.text = text;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (bold ? 1231 : 1237);
			result = prime * result + (caption ? 1231 : 1237);
			result = prime * result + height;
			result = prime * result + (italic ? 1231 : 1237);
			result = prime * result + ((text == null) ? 0 : text.hashCode());
			result = prime * result + width;
			result = prime * result + x;
			result = prime * result + y;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			PlainTextKeeper other = (PlainTextKeeper) obj;
			if (bold != other.bold) return false;
			if (caption != other.caption) return false;
			if (height != other.height) return false;
			if (italic != other.italic) return false;
			if (text == null) {
				if (other.text != null) return false;
			} else if (!text.equals(other.text)) return false;
			if (width != other.width) return false;
			if (x != other.x) return false;
			if (y != other.y) return false;
			return true;
		}

		@Override
		public String toString() {
			return "PlainTextKeeper [x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + ", bold=" + bold + ", italic=" + italic + ", caption=" + caption + ", text=" + text + "]";
		}
	}
	
	private static class SeparatorKeeper {
		final int	x, y, width, height;

		SeparatorKeeper(final int x, final int y, final int width, final int height) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + height;
			result = prime * result + width;
			result = prime * result + x;
			result = prime * result + y;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			SeparatorKeeper other = (SeparatorKeeper) obj;
			if (height != other.height) return false;
			if (width != other.width) return false;
			if (x != other.x) return false;
			if (y != other.y) return false;
			return true;
		}

		@Override
		public String toString() {
			return "SeparatorKeeper [x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + "]";
		}
	}
	
	private static class FieldKeeper {
		final int		x, y, width, height;
		final String	name, initial;
		
		FieldKeeper(final int x, final int y, final int width, final int height, final String name, final String initial) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.name = name;
			this.initial = initial;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + height;
			result = prime * result + ((initial == null) ? 0 : initial.hashCode());
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + width;
			result = prime * result + x;
			result = prime * result + y;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			FieldKeeper other = (FieldKeeper) obj;
			if (height != other.height) return false;
			if (initial == null) {
				if (other.initial != null) return false;
			} else if (!initial.equals(other.initial)) return false;
			if (name == null) {
				if (other.name != null) return false;
			} else if (!name.equals(other.name)) return false;
			if (width != other.width) return false;
			if (x != other.x) return false;
			if (y != other.y) return false;
			return true;
		}

		@Override
		public String toString() {
			return "FieldKeeper [x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + ", name=" + name + ", initial=" + initial + "]";
		}
	}
	
	private static class ContentKeeper {
		final int		x, y, width, height;
		final String	caption;
		
		ContentKeeper(final int x, final int y, final int width, final int height, final String caption) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.caption = caption;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((caption == null) ? 0 : caption.hashCode());
			result = prime * result + height;
			result = prime * result + width;
			result = prime * result + x;
			result = prime * result + y;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			ContentKeeper other = (ContentKeeper) obj;
			if (caption == null) {
				if (other.caption != null) return false;
			} else if (!caption.equals(other.caption)) return false;
			if (height != other.height) return false;
			if (width != other.width) return false;
			if (x != other.x) return false;
			if (y != other.y) return false;
			return true;
		}

		@Override
		public String toString() {
			return "ContentKeeper [x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + ", caption=" + caption + "]";
		}
	}
}

@LocaleResourceLocation(Localizer.LOCALIZER_SCHEME+":xml:file:./src/test/resources/chav1961/purelib/i18n/test.xml")
@LocaleResource(value="key1",tooltip="key2",help="key3")
@Action(resource=@LocaleResource(value="key2",tooltip="key2"),actionString="press",simulateCheck=true) 
class SeekAndCallTest {
	@LocaleResource(value="key1",tooltip="key2")
	@Format("10.3mpzn")
		private float 	testSet1 = 0.0f;
	

	void callMethod1() throws TestException {
		throw new TestException(); 
	}

	void callMethod2() {}
	
	RefreshMode callMethod3() {
		return RefreshMode.EXIT;
	}

	int callMethod4() {
		return 0;
	}
}