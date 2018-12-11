package test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.TextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.LayeredHighlighter;
import javax.swing.text.Position;
import javax.swing.text.View;
import javax.swing.text.Highlighter.Highlight;

public class SimpleEditor extends JFrame {
	private static final long 	serialVersionUID = 1L;
	private static final String	INIT_TEXT = "Trail: Creating a GUI with JFC/Swing\n"
								     + "Lesson: Learning Swing by Example\n"
								     + "This lesson explains the concepts you need to\n"
								     + " use Swing components in building a user interface.\n"
								     + " First we examine the simplest Swing application you can write.\n"
								     + " Then we present several progressively complicated examples of creating\n"
								     + " user interfaces using components in the javax.swing package.\n"
								     + " We cover several Swing components, such as buttons, labels, and text areas.\n"
								     + " The handling of events is also discussed,\n"
								     + " as are layout management and accessibility.\n"
								     + " This lesson ends with a set of questions and exercises\n"
								     + " so you can test yourself on what you've learned.\n"
								     + "https://docs.oracle.com/javase/tutorial/uiswing/learn/index.html\n";
	private static final Highlighter.HighlightPainter HIGHLIGHT_PAINTER = new DefaultHighlightPainter(Color.YELLOW,Color.CYAN);

//	Coils ring (http://rcl-radio.ru/?p=20475):
//		ω – number of coils, L— inductance µH, µ — const material, D1-outer diameter mm, D2-inner diameter mm, h-ring height mm.
//		D1/D2> 1.75	
//		ω = 100 * sqrt(L/(2*µ*h*ln(D1/D2)))
//		D1/D2< 1.75
//		ω = 100 * sqrt(L*(D1/D2)/(4*µ*h*(D1-D2)))
//
//		Inductance of square solenoid (http://rcl-radio.ru/?p=40833):
//		ω — number of coils; c — size of solenoud base m, a — solenoid length m, μ0 – constant, μ0 = 4π*10‾7; Ф - see below
//		L=µ0*ω^2*c*Ф/(4*π)
//		v=c/a
//		Ф=8.395*ln(0.544+v)+4.971
//
//		Inductance of flat coils (http://rcl-radio.ru/?p=40826):
//		ω — number of coils; c — length if the middle coil (perimeter/4) mm, r coild width mm, μ0 – constant, μ0 = 4π*10‾7; Ф - see below
//		L=(µ0*ω^2/(4*π))*(c+r)*Ф
//		v=r/c+1
//		Ф=-10.79ln(v)-3.86
//
//	
//	private double shortestDistance(float x1,float y1,float x2,float y2,float x3,float y3)
//    {
//        float px=x2-x1;
//        float py=y2-y1;
//        float temp=(px*px)+(py*py);
//        float u=((x3 - x1) * px + (y3 - y1) * py) / (temp);
//        if(u>1){
//            u=1;
//        }
//        else if(u<0){
//            u=0;
//        }
//        float x = x1 + u * px;
//        float y = y1 + u * py;
//
//        float dx = x - x3;
//        float dy = y - y3;
//        double dist = Math.sqrt(dx*dx + dy*dy);
//        return dist;
//
//    }	
	
	private final JTextArea		area = new JTextArea(); 
	
	public SimpleEditor() throws BadLocationException {
		super("TEST");
		
		area.setLineWrap(true);
		area.setText(INIT_TEXT);
		area.setPreferredSize(new Dimension(640,480));
		
		area.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				System.err.println("Remove: "+e.getOffset()+" "+e.getLength());
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				System.err.println("Insert: "+e.getOffset()+" "+e.getLength());
		        final Highlighter 	highlighter = area.getHighlighter();
		        
		        for (Highlight item : highlighter.getHighlights()) {
					System.err.println("Marks: "+item.getStartOffset()+" "+item.getEndOffset());
					if (item.getStartOffset() < e.getOffset() && e.getOffset() < item.getEndOffset()) {
				        highlighter.removeHighlight(item);
					}
		        }
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				System.err.println("Change: "+e.getOffset()+" "+e.getLength());
			}
		});
		
        final Highlighter 	highlighter = area.getHighlighter();
        final String		text = area.getDocument().getText(0,area.getDocument().getLength());
		int					index = 0;
		
		while ((index = text.indexOf("Swing",index)) >= 0) {
            highlighter.addHighlight(index,index+"Swing".length(), HIGHLIGHT_PAINTER);
            index += "Swing".length();
		}
		
		getContentPane().add(area,BorderLayout.CENTER);
	}
	

	public static void main(String[] args) throws BadLocationException {
		// TODO Auto-generated method stub
		new SimpleEditor().setVisible(true);
	}


    public static class DefaultHighlightPainter extends LayeredHighlighter.LayerPainter {
        private final Color 	foreground;
        private final Color 	background;


        /**
         * Constructs a new highlight painter. If <code>c</code> is null,
         * the JTextComponent will be queried for its selection color.
         *
         * @param c the color for the highlight
         */
        public DefaultHighlightPainter(final Color foreground, final Color background) {
            this.foreground = foreground;
            this.background = background;
        }

        /**
         * Returns the color of the highlight.
         *
         * @return the color
         */
        public Color getColor() {
            return foreground;
        }

        public Color getBackColor() {
            return background;
        }
        
        // --- HighlightPainter methods ---------------------------------------

        /**
         * Paints a highlight.
         *
         * @param g the graphics context
         * @param offs0 the starting model offset &gt;= 0
         * @param offs1 the ending model offset &gt;= offs1
         * @param bounds the bounding box for the highlight
         * @param c the editor
         */
        public void paint(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c) {
            Rectangle alloc = bounds.getBounds();
            try {
                // --- determine locations ---
                TextUI mapper = c.getUI();
                Rectangle p0 = mapper.modelToView(c, offs0);
                Rectangle p1 = mapper.modelToView(c, offs1);

                // --- render ---
                Color color = getColor();

                if (color == null) {
                    g.setColor(c.getSelectionColor());
                }
                else {
                    g.setColor(color);
                }
                if (p0.y == p1.y) {
                    // same line, render a rectangle
                    Rectangle r = p0.union(p1);
                    g.fillRect(r.x, r.y, r.width, r.height);
                } else {
                    // different lines
                    int p0ToMarginWidth = alloc.x + alloc.width - p0.x;
                    g.fillRect(p0.x, p0.y, p0ToMarginWidth, p0.height);
                    if ((p0.y + p0.height) != p1.y) {
                        g.fillRect(alloc.x, p0.y + p0.height, alloc.width,
                                   p1.y - (p0.y + p0.height));
                    }
                    g.fillRect(alloc.x, p1.y, (p1.x - alloc.x), p1.height);
                }
            } catch (BadLocationException e) {
                // can't render
            }
        }

        // --- LayerPainter methods ----------------------------
        /**
         * Paints a portion of a highlight.
         *
         * @param g the graphics context
         * @param offs0 the starting model offset &gt;= 0
         * @param offs1 the ending model offset &gt;= offs1
         * @param bounds the bounding box of the view, which is not
         *        necessarily the region to paint.
         * @param c the editor
         * @param view View painting for
         * @return region drawing occurred in
         */
        public Shape paintLayer(Graphics g, int offs0, int offs1,
                                Shape bounds, JTextComponent c, View view) {
            Color color = getColor();

            if (color == null) {
                g.setColor(c.getSelectionColor());
            }
            else {
                g.setColor(color);
            }

            Rectangle r;

            if (offs0 == view.getStartOffset() &&
                offs1 == view.getEndOffset()) {
                // Contained in view, can just use bounds.
                if (bounds instanceof Rectangle) {
                    r = (Rectangle) bounds;
                }
                else {
                    r = bounds.getBounds();
                }
            }
            else {
                // Should only render part of View.
                try {
                    // --- determine locations ---
                    Shape shape = view.modelToView(offs0, Position.Bias.Forward,
                                                   offs1,Position.Bias.Backward,
                                                   bounds);
                    r = (shape instanceof Rectangle) ?
                                  (Rectangle)shape : shape.getBounds();
                } catch (BadLocationException e) {
                    // can't render
                    r = null;
                }
            }

            if (r != null) {
                // If we are asked to highlight, we should draw something even
                // if the model-to-view projection is of zero width (6340106).
                r.width = Math.max(r.width, 1);

                g.fillRect(r.x, r.y, r.width, r.height);
            }

            return r;
        }

    }

    
//    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {                                         
//        // TODO add your handling code here:
//        StyledDocument doc = (StyledDocument) jTextPane1.getDocument();
//        int selectionEnd = jTextPane1.getSelectionEnd();
//        int selectionStart = jTextPane1.getSelectionStart();
//        if (selectionStart == selectionEnd) {
//            return;
//        }
//        Element element = doc.getCharacterElement(selectionStart);
//        AttributeSet as = element.getAttributes();
//        MutableAttributeSet asNew = new SimpleAttributeSet(as.copyAttributes());
//        StyleConstants.setBold(asNew, !StyleConstants.isBold(as));
//        doc.setCharacterAttributes(selectionStart, jTextPane1.getSelectedText().length(), asNew, true);
//        
//    }
    
//    Pipe concept:
//    	- any number of the same calculation plugins can be opened in the pipe.
//    	- any output field of the one plugin can be linked with one or more input field of any other plugins
//    	- there are set of predefined pipe-orineted plugins in the system:
//    	-- initial node (required)
//    	-- terminal node (required)
//    	-- logical blocks: (optional)
//    	--- conditional block
//    	--- repeater block
//    	--- sub-pipe caller
//    	--- message block
//    	- when all input data from the calculation plugin were gathered, automatic calculation can be started.
//    	Initial node contains a set of user-defined fields to use as pipe sources.
//    	Terminal node contains a set of user-define fields to use as pipe destination.
//    	Conditional block contains a set of user-defined fields to test and a list of conditionals. Every conditional contains JavaScript expression to calculate conditions and "connector" to pass control to.
//    	Repeater block contains two connectors to calculate sub-pipe, loop variable and loop conditions (in the JavaScript)
//    	Sub-pipe caller contains a sub-pipe location URL, and source/destination fields for it
//    	Message block a set of user-defined fields, message text (with formatters), and message terminator condition.
//    	Any pipe contains initial and at least one terminal node, can be executed. A special class, PipeExecutor, supports all calculations.
//    	Pipe configuration can be stored and loaded. Any missing plugins for pipe can be loaded thru plugin market.    

}


