package chav1961.purelib.basic;

import java.awt.Color;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;

public class PureLibSettingsTest {

	@Test
	public void colorsTest() {
		Assert.assertEquals(Color.BLACK,PureLibSettings.colorByName("black",Color.BLACK));
		Assert.assertEquals(Color.GREEN,PureLibSettings.colorByName("unknown",Color.GREEN));
		Assert.assertEquals("green",PureLibSettings.nameByColor(Color.GREEN,"green"));
		Assert.assertEquals("unknown",PureLibSettings.nameByColor(new Color(1,2,3),"unknown"));
		
		try{PureLibSettings.colorByName(null,Color.gray);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{PureLibSettings.colorByName("",Color.gray);
			Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
		} catch (IllegalArgumentException exc) {
		}
		
		try{PureLibSettings.nameByColor(null,"unknown");
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}		
	}

	@Test
	public void helpTest() throws IOException, SyntaxException, ContentException {
//		try(final FileSystemInterface 		fsi = FileSystemFactory.createFileSystem(URI.create(FileSystemInterface.FILESYSTEM_URI_SCHEME+":file:./src/test/resources/chav1961/purelib/basic"))) {
//			
//			try{PureLibSettings.installHelpContent(null,fsi);
//				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
//			} catch (IllegalArgumentException exc) {			
//			}
//			try{PureLibSettings.installHelpContent("",fsi);
//				Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
//			} catch (IllegalArgumentException exc) {			
//			}
//			
//			try{PureLibSettings.installHelpContent("test",null);
//				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
//			} catch (NullPointerException exc) {			
//			}
//			
//			try{PureLibSettings.installHelpContent("test",fsi);
//				Assert.fail("Mandatory exception was not detected (help port was not defined)");
//			} catch (IllegalStateException exc) {			
//			}
//			
//			PureLibSettings.instance().setProperty(PureLibSettings.BUILTIN_HELP_PORT,"13666");
//			PureLibSettings.installHelpContent("test",fsi);
//			
//			final URL				readerUrl = new URL("http://localhost:"+PureLibSettings.instance().getProperty(PureLibSettings.BUILTIN_HELP_PORT,int.class)+"/test/resourcefile.txt");
//			
//			try(final InputStream	is = readerUrl.openStream();
//				final Reader		rdr = new InputStreamReader(is);
//				final Writer		wr = new StringWriter()) {
//				
//				Utils.copyStream(rdr,wr);
//				Assert.assertEquals("test string",wr.toString());
//			}
//			PureLibSettings.uninstallHelpContent("test");
//
//			try{PureLibSettings.uninstallHelpContent(null);
//				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
//			} catch (IllegalArgumentException exc) {			
//			}
//			try{PureLibSettings.uninstallHelpContent("");
//				Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
//			} catch (IllegalArgumentException exc) {			
//			}
//
//			try{PureLibSettings.uninstallHelpContent("test");
//				Assert.fail("Mandatory exception was not detected (plugin was not installed yer or was uninstalled earlier)");
//			} catch (IllegalArgumentException exc) {			
//			}
//		}
	}

}
