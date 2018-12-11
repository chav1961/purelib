package chav1961.purelib.ui.swing;



import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Polygon;
import java.io.IOException;
import java.net.URI;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JToolTip;
import javax.swing.border.Border;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.ui.UIUtils;

public class SmartToolTip extends JToolTip {
	private static final long 				serialVersionUID = -1468648719021872001L;
	
	private static final AnchorProcessor	DUMMY = new AnchorProcessor(){
												@Override
												public void processAnchor(final URI anchor) {
												}
											};

	public interface AnchorProcessor {
		void processAnchor(URI anchor);
	}
	
	private final JEditorPane		pane = new JEditorPane("text/html",null);
	
	public SmartToolTip(final Localizer localizer, final JComponent comp) {
		this(localizer,comp,DUMMY);
	}
	
	public SmartToolTip(final Localizer localizer, final JComponent comp, final AnchorProcessor ap) {
		setOpaque(false);
		setBorder(new InternalBorders(1));
		setLayout(new BorderLayout());
		pane.setEditable(false);
		pane.addHyperlinkListener(new HyperlinkListener() {
				@Override
				public void hyperlinkUpdate(final HyperlinkEvent e) {
					if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
						ap.processAnchor(URI.create(e.getDescription()));
					}
				}
		});
		add(pane,BorderLayout.CENTER);
		if (comp.getToolTipText() != null && !comp.getToolTipText().isEmpty()) {
			setTipText(comp.getToolTipText());
		}
	}
	
	@Override
	public void setTipText(final String tooltip) {
		super.setTipText(tooltip);
		if (tooltip != null) {
			try{this.pane.setText(UIUtils.cre2Html(tooltip));
				setPreferredSize(new Dimension(this.pane.getPreferredSize().width+2,this.pane.getPreferredSize().height+2));
			} catch (IOException e) {
				this.pane.setText(tooltip);
			}
		}
		else {
			this.pane.setText("");
		}
	}

	private class InternalBorders implements Border {  
		private final int 	top;  
		private final int 	left = 1;   
		private final int 	bottom = 1;    
		private final int 	right = 1;
		  
		public InternalBorders(final int topHeight) {  
			this.top = topHeight;   
		}
  
		@Override
		public void paintBorder(final Component c, final Graphics g, final int x, final int y, final int width, final int height) {
			final Insets 	insets = getBorderInsets(c);
			final Polygon 	p = new Polygon();

			p.addPoint(x+insets.left-1,y+insets.top-1);
			p.addPoint(x+insets.left-1,y+height-insets.bottom);
			p.addPoint(x+width-insets.right,y+height-insets.bottom);
			p.addPoint(x+width-insets.right,y+insets.top-1);
			p.addPoint(x+insets.left+this.top,y+insets.top-1);
			p.addPoint(x+insets.left+this.top/2,y);
			p.addPoint(x+insets.left+this.top/2,y+insets.top-1);
			p.addPoint(x+insets.left-1,y+insets.top-1);
			     
			g.setColor(pane.getBackground());
			g.fillPolygon(p); 
			g.setColor(SwingUtils.TOOLTIP_BORDER_COLOR);
			g.drawPolygon(p); 
		}   
  
		@Override
		public Insets getBorderInsets(Component c) {  
			return new Insets(top, left, bottom, right);  
		}
  
		@Override
		public boolean isBorderOpaque() {   
			return true;  
		}   
	}
}
