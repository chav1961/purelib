package chav1961.purelib.ui.swing.useful;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;

import chav1961.purelib.concurrent.LightWeightListenerList;
import chav1961.purelib.ui.swing.useful.interfaces.SelectionFrameListener;
import chav1961.purelib.ui.swing.useful.interfaces.SelectionFrameListener.SelectionStyle;

public class SelectionFrameManager {
	private static final int	STATE_INITIAL = 0;
	private static final int	STATE_SELECTION_IN_PROGRESS = 1;
	private static final int	STATE_LOST_FOCUS = 2;
	private static final int	STATE_COMPLETED = 3;

	private static final int	TERM_PRESSED = 0;
	private static final int	TERM_DRAGGED = 1;
	private static final int	TERM_RELEASED = 2;
	private static final int	TERM_EXITED = 3;
	private static final int	TERM_ENTERED = 4;
	private static final int	TERM_RESET = 5;
	
	private static final int	REFRESH_BOUND = 2;
	
	private static final Stroke	STROKE = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 5, new float[] {5}, 0);
	
	@FunctionalInterface
	private static interface AutomatCall {
		void automat(int terminal, Point parameter);
	}
	
	private final JComponent	background;
	private final boolean		keepSelectionOnMouseExit;
	private final LightWeightListenerList<SelectionFrameListener>	listeners = new LightWeightListenerList<>(SelectionFrameListener.class);
	private final Rectangle		currentFrame = new Rectangle();
	private final Rectangle		prevFrame = new Rectangle();
	private SelectionStyle		currentStyle = SelectionStyle.NONE;
	private AutomatCall			ac;	
	private boolean				enabled = false;
	private boolean 			visible = false; 
	private Point				startP, currentP;
	private int					startX, startY;
	private int					currentX, currentY;
	private int					automatState = STATE_INITIAL;
	
	public SelectionFrameManager(final JComponent component, final boolean keepSelectionOnMouseExit) {
		if (component == null) {
			throw new NullPointerException("Component can't be null"); 
		}
		else {
			this.background = component;
			this.keepSelectionOnMouseExit = keepSelectionOnMouseExit;
			this.ac = keepSelectionOnMouseExit ? this::automatNoneWithKeep : this::automatNoneWithoutKeep;
			
			this.background.addMouseListener(new MouseListener() {
				@Override public void mouseClicked(MouseEvent e) {}
				@Override public void mouseReleased(final MouseEvent e) {automat(TERM_RELEASED, e.getPoint());}
				@Override public void mousePressed(final MouseEvent e) {automat(TERM_PRESSED, e.getPoint());}
				@Override public void mouseExited(final MouseEvent e) {automat(TERM_EXITED, e.getPoint());}
				@Override public void mouseEntered(MouseEvent e) {automat(TERM_ENTERED, e.getPoint());}
				
			});
			this.background.addMouseMotionListener(new MouseMotionListener() {
				@Override public void mouseMoved(MouseEvent e) {}
				@Override public void mouseDragged(MouseEvent e) {automat(TERM_DRAGGED, e.getPoint());}
			});
		}
	}

	public SelectionStyle getSelectionStyle() {
		return currentStyle;
	}
	
	public void setSelectionStyle(final SelectionStyle style) {
		if (style == null) {
			throw new NullPointerException("Style to set can't be null"); 
		}
		else {
			ultimateAutomat(TERM_RESET, null);
			switch (currentStyle = style) {
				case NONE		:
					ac = keepSelectionOnMouseExit ? this::automatNoneWithKeep : this::automatNoneWithoutKeep;
					break;
				case PATH		:
					ac = keepSelectionOnMouseExit ? this::automatNoneWithKeep : this::automatNoneWithoutKeep;
					break;
				case POINT		:
					ac = keepSelectionOnMouseExit ? this::automatNoneWithKeep : this::automatNoneWithoutKeep;
					break;
				case RECTANGLE	:
					ac = keepSelectionOnMouseExit ? this::automatRectWithKeep : this::automatRectWithoutKeep;
					break;
				default :
					throw new UnsupportedOperationException("Selection style ["+currentStyle+"] is not supprted yet"); 
			}
		}
	}

	public void enableSelection(final boolean enable) {
		this.enabled = enable;
		background.repaint();
	}
	
	public boolean isSelectionEnabled() {
		return enabled;
	}
	
	public void setVisible(final boolean visible) {
		this.visible = visible;
		background.repaint();
	}
	
	public boolean isVisible() {
		return visible;
	}
	
	public void addSelectionFrameListener(final SelectionFrameListener l) {
		if (l == null) {
			throw new NullPointerException("Listener to add can't be null"); 
		}
		else {
			listeners.addListener(l);
		}
	}

	public void removeSelectionFrameListener(final SelectionFrameListener l) {
		if (l == null) {
			throw new NullPointerException("Listener to remove can't be null"); 
		}
		else {
			listeners.removeListener(l);
		}
	}

	public void paintSelection(final Graphics2D g2d) {
		final Stroke	oldStroke = g2d.getStroke();
		
		g2d.setStroke(STROKE);
		switch (currentStyle) {
			case NONE		:
				break;
			case PATH		:
				break;
			case POINT		:
				break;
			case RECTANGLE	:
				g2d.drawRect(currentFrame.x, currentFrame.y, currentFrame.width, currentFrame.height);
				break;
			default :
				throw new UnsupportedOperationException("Current selection style ["+currentStyle+"] is not supported yet");
		}
		g2d.setStroke(oldStroke);
	}
	
	private void automat(final int terminal, final Point pt) {
		if (isSelectionEnabled()) {
			ultimateAutomat(terminal, pt);
		}
	}

	private void ultimateAutomat(final int terminal, final Point parameter) {
		ac.automat(terminal, parameter);
	}

	private void automatNoneWithKeep(final int terminal, final Point parameter) {
	}
	
	private void automatNoneWithoutKeep(final int terminal, final Point parameter) {
	}
	
	private void automatRectWithKeep(final int terminal, final Point parameter) {
		automatRectWithoutKeep(terminal, parameter);
	}
	
	private void automatRectWithoutKeep(final int terminal, final Point parameter) {
		switch (automatState) {
			case STATE_INITIAL	:
				switch (terminal) {
					case TERM_PRESSED	:
						automatState = STATE_SELECTION_IN_PROGRESS;
						clearRectangle();
						storeInitialPoint(parameter);
						fireSelectionStarted();
						break;
					case TERM_RESET		:
						clearRectangle();
						break;
				}
				break;
			case STATE_SELECTION_IN_PROGRESS	:
				switch (terminal) {
					case TERM_RESET		:
						automatState = STATE_INITIAL;
						clearRectangle();
						fireSelectionCancelled();
						refreshContent();
						break;
					case TERM_DRAGGED	:
						storeCurrentPoint(parameter);
						resizeRectangle();
						fireSelectionChanged();
						refreshContent();
						break;
					case TERM_RELEASED	:
						automatState = STATE_INITIAL;
						storeCurrentPoint(parameter);
						resizeRectangle();
						fireSelectionCompleted();
						refreshContent();
						enableSelection(false);
						break;
				}
				break;
			case STATE_LOST_FOCUS	:
			case STATE_COMPLETED	:
			default :
				throw new UnsupportedOperationException("Automat state ["+automatState+"] is not supported yet");
		}
	}
	
	private void clearRectangle() {
		currentFrame.setBounds(0, 0, 0, 0);
	}
	
	private void storeInitialPoint(final Point p) {
		startP = currentP = p;
		startX = currentX = p.x;
		startY = currentY = p.y;
	}

	private void storeCurrentPoint(final Point p) {
		currentP = p;
		currentX = p.x;
		currentY = p.y;
	}

	private void resizeRectangle() {
		prepareRectangle();
	}
	
	private void fireSelectionStarted() {
		listeners.fireEvent((l)->l.selectionStarted(getSelectionStyle(), startP));
	}

	private void fireSelectionChanged() {
		listeners.fireEvent((l)->l.selectionChanging(getSelectionStyle(), startP, currentP, currentFrame));
	}

	private void fireSelectionCancelled() {
		listeners.fireEvent((l)->l.selectionCancelled(getSelectionStyle(), startP, currentP));
	}

	private void fireSelectionCompleted() {
		listeners.fireEvent((l)->l.selectionCompleted(getSelectionStyle(), startP, currentP, currentFrame));
	}

	private void refreshContent() {
		refresh();
	}
	
	private void prepareRectangle() {
		prevFrame.setFrame(currentFrame);
		currentFrame.setFrameFromDiagonal(Math.min(startX, currentX), Math.min(startY, currentY), Math.max(startX, currentX), Math.max(startY, currentY));
	}
	
	private void refresh() {
		final Rectangle	rect = new Rectangle();
		
		rect.setFrameFromDiagonal(Math.min(currentFrame.x, prevFrame.x)-REFRESH_BOUND, Math.min(currentFrame.y, prevFrame.y)-REFRESH_BOUND, 
								  Math.max(currentFrame.x+currentFrame.width, prevFrame.x+prevFrame.width)+REFRESH_BOUND, 
								  Math.max(currentFrame.y+currentFrame.height, prevFrame.y+prevFrame.height)+REFRESH_BOUND);
		background.repaint(rect);
	}
}
