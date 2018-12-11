package chav1961.purelib.ui.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;

import org.junit.Test;

import chav1961.purelib.basic.SystemErrLoggerFacade;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.xsd.XSDConst;

public class XMLDescribedApplicationTest {
	@Test
	public void basicTest() throws IOException, NullPointerException, EnvironmentException {
		try(final InputStream	is = this.getClass().getResourceAsStream("Application.xml");
			final InputStream	isXSD = XSDConst.class.getResourceAsStream("XMLDescribedApplication.xsd")) {
			
			Utils.validateXMLByXSD(is,isXSD,new SystemErrLoggerFacade());
		}

		final JFrame	f = new JFrame();
		f.setPreferredSize(new Dimension(640,480));
		f.setVisible(true);
		
		try(final InputStream	is = this.getClass().getResourceAsStream("Application.xml");
			final LoggerFacade	log = new SystemErrLoggerFacade()) {
			final XMLDescribedApplication	xda = new XMLDescribedApplication(is,log); 
			
			f.getContentPane().add(xda.getEntity("mainmenu",JMenuBar.class,null),BorderLayout.NORTH);
			f.getContentPane().add(xda.getEntity("toolbar",JToolBar.class,null),BorderLayout.SOUTH);
			
			xda.getEntity("mainmenu",JPopupMenu.class,null).show(f.getContentPane(),10,10);
		}
		
		System.err.println("SDSD");
	}
}
