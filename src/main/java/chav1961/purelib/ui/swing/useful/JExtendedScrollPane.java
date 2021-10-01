package chav1961.purelib.ui.swing.useful;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;
import javax.swing.JScrollPane;

public class JExtendedScrollPane extends JScrollPane {
	private static final long 	serialVersionUID = 7351600865009840805L;
	
	private boolean		pressed = false;
	private Cursor		oldCursor = Cursor.getDefaultCursor(); 
	private Point		pressedPoint;

	public JExtendedScrollPane(final JComponent innerControl) {
		this(innerControl,false);
	}
	
	public JExtendedScrollPane(final JComponent innerControl, final boolean canDragInnerControl) {
		super(innerControl);
		
		innerControl.addMouseListener(new MouseListener() {
			@Override public void mouseExited(MouseEvent e) {}
			@Override public void mouseEntered(MouseEvent e) {}
			@Override public void mouseClicked(MouseEvent e) {}
			
			@Override
			public void mouseReleased(MouseEvent e) {
				pressed = false;
				if (canDragInnerControl) {
					setCursor(oldCursor);
				}
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				if (canDragInnerControl) {
					pressed = true;
					pressedPoint = e.getPoint();
					oldCursor = getCursor();
					setCursor(new Cursor(Cursor.HAND_CURSOR));
				}
			}
		});
		innerControl.addMouseMotionListener(new MouseMotionListener() {
			@Override public void mouseMoved(MouseEvent e) {}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				if (pressed) {
					final Point	currentVP = getViewport().getViewPosition();
					
					int 	x = e.getPoint().x-pressedPoint.x, y = e.getPoint().y-pressedPoint.y;
	
					currentVP.translate(-x,-y);
					pressedPoint = e.getPoint();
					getViewport().setViewPosition(currentVP);
				}
			}
		});
	}
}
