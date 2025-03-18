package chav1961.purelib.ui.swing;


import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

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
import chav1961.purelib.testing.OrdinalTestCategory;
import chav1961.purelib.ui.interfaces.Action;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.interfaces.RefreshMode;
import chav1961.purelib.ui.swing.FormManagedUtils.FormManagerParserCallback;
import chav1961.purelib.ui.swing.FormManagedUtils.MarkupParserCallback;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;

@Tag("OrdinalTestCategory")
public class FormManagedUtilsTest {
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
												Assert.assertEquals("testSet1",metadata.getLabelId());
												Assert.assertTrue(isModifiable);
											}
										};
		 
		FormManagedUtils.parseModel4Form(PureLibSettings.CURRENT_LOGGER,mdi,PureLibSettings.PURELIB_LOCALIZER,SeekAndCallTest.class,mon,callback);
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
		Assert.assertEquals(new PlainTextKeeper(2,1,1,1,false,false,false,"A"),plain.get(0));

		// Plain text and caption
		Arrays.fill(called,false);
		plain.clear();
		separator.clear();
		field.clear();
		content.clear();
		
		FormManagedUtils.parseMarkup("== Caption \nPlain text",callback);
		
		Assert.assertArrayEquals(new boolean[] {true,false,false,false,false},called);
		Assert.assertEquals(2,plain.size());
		Assert.assertEquals(new PlainTextKeeper(2,1,7,1,true,false,true,"Caption"),plain.get(0));
		Assert.assertEquals(new PlainTextKeeper(1,2,10,1,false,false,false,"Plain text"),plain.get(1));

		// Bold and italic
		Arrays.fill(called,false);
		plain.clear();
		separator.clear();
		field.clear();
		content.clear();
		
		FormManagedUtils.parseMarkup("plain **bold** plain //italic// plain **//bold italic//** plain",callback);
		
		Assert.assertArrayEquals(new boolean[] {true,false,false,false,false},called);
		Assert.assertEquals(7,plain.size());
		Assert.assertEquals(new PlainTextKeeper(1,1,5,1,false,false,false,"plain"),plain.get(0));
		Assert.assertEquals(new PlainTextKeeper(7,1,4,1,true,false,false,"bold"),plain.get(1));
		Assert.assertEquals(new PlainTextKeeper(12,1,5,1,false,false,false,"plain"),plain.get(2));
		Assert.assertEquals(new PlainTextKeeper(18,1,6,1,false,true,false,"italic"),plain.get(3));
		Assert.assertEquals(new PlainTextKeeper(25,1,5,1,false,false,false,"plain"),plain.get(4));
		Assert.assertEquals(new PlainTextKeeper(31,1,11,1,true,true,false,"bold italic"),plain.get(5));
		Assert.assertEquals(new PlainTextKeeper(43,1,5,1,false,false,false,"plain"),plain.get(6));

		Arrays.fill(called,false);
		plain.clear();
		separator.clear();
		field.clear();
		content.clear();
		
		FormManagedUtils.parseMarkup("plain **bold\nplain",callback);
		
		Assert.assertArrayEquals(new boolean[] {true,false,false,false,false},called);
		Assert.assertEquals(3,plain.size());
		Assert.assertEquals(new PlainTextKeeper(1,1,5,1,false,false,false,"plain"),plain.get(0));
		Assert.assertEquals(new PlainTextKeeper(7,1,4,1,true,false,false,"bold"),plain.get(1));
		Assert.assertEquals(new PlainTextKeeper(1,2,5,1,false,false,false,"plain"),plain.get(2));
		
		Arrays.fill(called,false);
		plain.clear();
		separator.clear();
		field.clear();
		content.clear();
		
		FormManagedUtils.parseMarkup("plain //italic\nplain",callback);
		
		Assert.assertArrayEquals(new boolean[] {true,false,false,false,false},called);
		Assert.assertEquals(3,plain.size());
		Assert.assertEquals(new PlainTextKeeper(1,1,5,1,false,false,false,"plain"),plain.get(0));
		Assert.assertEquals(new PlainTextKeeper(7,1,6,1,false,true,false,"italic"),plain.get(1));
		Assert.assertEquals(new PlainTextKeeper(1,2,5,1,false,false,false,"plain"),plain.get(2));
		
		// Substitutions
		Arrays.fill(called,false);
		plain.clear();
		separator.clear();
		field.clear();
		content.clear();
		
		FormManagedUtils.parseMarkup("plain ${var:10=100} plain ${var:10} plain ${var:10='test'} plain",callback);
		
		Assert.assertArrayEquals(new boolean[] {true,false,true,false,false},called);
		Assert.assertEquals(4,plain.size());
		Assert.assertEquals(new PlainTextKeeper(1,1,5,1,false,false,false,"plain"),plain.get(0));
		Assert.assertEquals(new PlainTextKeeper(18,1,5,1,false,false,false,"plain"),plain.get(1));
		Assert.assertEquals(new PlainTextKeeper(35,1,5,1,false,false,false,"plain"),plain.get(2));
		Assert.assertEquals(new PlainTextKeeper(52,1,5,1,false,false,false,"plain"),plain.get(3));
		Assert.assertEquals(3,field.size());
		Assert.assertEquals(new FieldKeeper(7,1,10,1,"var","100"),field.get(0));
		Assert.assertEquals(new FieldKeeper(24,1,10,1,"var",null),field.get(1));
		Assert.assertEquals(new FieldKeeper(41,1,10,1,"var","test"),field.get(2));
		
		try {FormManagedUtils.parseMarkup("${:10=100}",callback);
			Assert.fail("Mandatory exception was not detected (missing field name)");
		} catch (SyntaxException exc) {
		}
		try {FormManagedUtils.parseMarkup("${var 10=100}",callback);
			Assert.fail("Mandatory exception was not detected (missing colon)");
		} catch (SyntaxException exc) {
		}
		try {FormManagedUtils.parseMarkup("${var:=100}",callback);
			Assert.fail("Mandatory exception was not detected (missing field length)");
		} catch (SyntaxException exc) {
		}
		try {FormManagedUtils.parseMarkup("${var:10",callback);
			Assert.fail("Mandatory exception was not detected (missing '}')");
		} catch (SyntaxException exc) {
		}
		try {FormManagedUtils.parseMarkup("${var:10=",callback);
			Assert.fail("Mandatory exception was not detected (missing '}')");
		} catch (SyntaxException exc) {
		}
		try {FormManagedUtils.parseMarkup("${var:10='}",callback);
			Assert.fail("Mandatory exception was not detected (unpaired quote)");
		} catch (SyntaxException exc) {
		}

		// Separators
		Arrays.fill(called,false);
		plain.clear();
		separator.clear();
		field.clear();
		content.clear();
		
		FormManagedUtils.parseMarkup("plain ----- plain",callback);
		
		Assert.assertArrayEquals(new boolean[] {true,true,false,false,false},called);
		Assert.assertEquals(2,plain.size());
		Assert.assertEquals(new PlainTextKeeper(1,1,5,1,false,false,false,"plain"),plain.get(0));
		Assert.assertEquals(new PlainTextKeeper(13,1,5,1,false,false,false,"plain"),plain.get(1));
		Assert.assertEquals(1,separator.size());
		Assert.assertEquals(new SeparatorKeeper(7,1,5,1),separator.get(0));

		Arrays.fill(called,false);
		plain.clear();
		separator.clear();
		field.clear();
		content.clear();
		
		FormManagedUtils.parseMarkup("plain | plain\nplain | plain\r\nplain | plain\n",callback);
		
		Assert.assertArrayEquals(new boolean[] {true,true,false,false,false},called);
		Assert.assertEquals(6,plain.size());
		Assert.assertEquals(new PlainTextKeeper(1,1,5,1,false,false,false,"plain"),plain.get(0));
		Assert.assertEquals(new PlainTextKeeper(9,1,5,1,false,false,false,"plain"),plain.get(1));
		Assert.assertEquals(new PlainTextKeeper(1,2,5,1,false,false,false,"plain"),plain.get(2));
		Assert.assertEquals(new PlainTextKeeper(9,2,5,1,false,false,false,"plain"),plain.get(3));
		Assert.assertEquals(new PlainTextKeeper(1,3,5,1,false,false,false,"plain"),plain.get(4));
		Assert.assertEquals(new PlainTextKeeper(9,3,5,1,false,false,false,"plain"),plain.get(5));
		Assert.assertEquals(1,separator.size());
		Assert.assertEquals(new SeparatorKeeper(7,1,1,3),separator.get(0));
		
		// Escapes
		Arrays.fill(called,false);
		plain.clear();
		separator.clear();
		field.clear();
		content.clear();
		
		FormManagedUtils.parseMarkup("plai\\n **bol\\d** plai\\n",callback);
		
		Assert.assertArrayEquals(new boolean[] {true,false,false,false,false},called);
		Assert.assertEquals(3,plain.size());
		Assert.assertEquals(new PlainTextKeeper(1,1,5,1,false,false,false,"plain"),plain.get(0));
		Assert.assertEquals(new PlainTextKeeper(7,1,4,1,true,false,false,"bold"),plain.get(1));
		Assert.assertEquals(new PlainTextKeeper(12,1,5,1,false,false,false,"plain"),plain.get(2));

		// Frames
		Arrays.fill(called,false);
		plain.clear();
		separator.clear();
		field.clear();
		content.clear();
		
		FormManagedUtils.parseMarkup("before1 +--------+ after1\nbefore2 | inside | after2\nbefore3 +--------+ after3\n",callback);
		
		Assert.assertArrayEquals(new boolean[] {true,false,false,true,true},called);
		Assert.assertEquals(7,plain.size());
		Assert.assertEquals(new PlainTextKeeper(1,1,7,1,false,false,false,"before1"),plain.get(0));
		Assert.assertEquals(new PlainTextKeeper(2,1,6,1,false,false,false,"inside"),plain.get(1));
		Assert.assertEquals(new PlainTextKeeper(20,1,6,1,false,false,false,"after1"),plain.get(2));
		Assert.assertEquals(new PlainTextKeeper(1,2,7,1,false,false,false,"before2"),plain.get(3));
		Assert.assertEquals(new PlainTextKeeper(20,2,6,1,false,false,false,"after2"),plain.get(4));
		Assert.assertEquals(new PlainTextKeeper(1,3,7,1,false,false,false,"before3"),plain.get(5));
		Assert.assertEquals(new PlainTextKeeper(20,3,6,1,false,false,false,"after3"),plain.get(6));

		Arrays.fill(called,false);
		plain.clear();
		separator.clear();
		field.clear();
		content.clear();
		
		FormManagedUtils.parseMarkup("before1 +--------+ after1\nbefore2 | ${var:6} | after2\nbefore3 +--------+ after3\n",callback);
		
		Assert.assertArrayEquals(new boolean[] {true,false,true,true,true},called);
		Assert.assertEquals(6,plain.size());
		Assert.assertEquals(new PlainTextKeeper(1,1,7,1,false,false,false,"before1"),plain.get(0));
		Assert.assertEquals(new PlainTextKeeper(20,1,6,1,false,false,false,"after1"),plain.get(1));
		Assert.assertEquals(new PlainTextKeeper(1,2,7,1,false,false,false,"before2"),plain.get(2));
		Assert.assertEquals(new PlainTextKeeper(20,2,6,1,false,false,false,"after2"),plain.get(3));
		Assert.assertEquals(new PlainTextKeeper(1,3,7,1,false,false,false,"before3"),plain.get(4));
		Assert.assertEquals(new PlainTextKeeper(20,3,6,1,false,false,false,"after3"),plain.get(5));

		try {FormManagedUtils.parseMarkup("+--\n",callback);
			Assert.fail("Mandatory exception was not detected (non-rectangle area - missing top-right)");
		} catch (SyntaxException exc) {
		}
		try {FormManagedUtils.parseMarkup("+--+\n+-+",callback);
			Assert.fail("Mandatory exception was not detected (non-rectangle area - missing bottom-left or bottom-right)");
		} catch (SyntaxException exc) {
		}
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

@LocaleResourceLocation(Localizer.LOCALIZER_SCHEME+":xml:root://chav1961.purelib.ui.swing.FormManagedUtilsTest/chav1961/purelib/i18n/test.xml")
@LocaleResource(value="testSet1",tooltip="testSet2",help="testSet3")
@Action(resource=@LocaleResource(value="testSet1",tooltip="testSet1"),actionString="press",simulateCheck=true) 
class SeekAndCallTest {
	@LocaleResource(value="testSet1",tooltip="testSet1")
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
