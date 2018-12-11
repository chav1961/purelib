package test;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class HyperlinkTest {

	public static void main(String[] args) {
		JFrame		f = new JFrame();
		
		f.setSize(200,100);
		JEditorPane	pane = new JEditorPane("text/html","<html><body><a href=\"https://mail.ru\">mail</a></body></html>");
		pane.setEditable(false);
		pane.addHyperlinkListener(new Hyperactive());
		f.getContentPane().add(pane,BorderLayout.CENTER);
		f.setVisible(true);
	}

	
	static class Hyperactive implements HyperlinkListener {
        public void hyperlinkUpdate(HyperlinkEvent e) {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            	if (e.getURL().getProtocol().startsWith("http")) {
            		try{Desktop.getDesktop().browse(e.getURL().toURI());
					} catch (IOException | URISyntaxException e1) {
						e1.printStackTrace();
					}
            	}
            	else {
                	System.err.println(e.getURL());
            	}
            }
        }
    }
}
