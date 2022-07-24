package chav1961.purelib.ui.swing.useful;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;

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
	private static final int	TERM_MOVED = 1;
	private static final int	TERM_DRAGGED = 2;
	private static final int	TERM_RELEASED = 3;
	private static final int	TERM_EXITED = 4;
	private static final int	TERM_ENTERED = 5;
	private static final int	TERM_RESET = 6;
	
	private static final int	REFRESH_BOUND = 2;
	
	private static final Stroke	STROKE = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 5, new float[] {5}, 0);
	
	@FunctionalInterface
	private static interface AutomatCall {
		void automat(int terminal, Point parameter);
	}
	
	private final JComponent	background;
	private final boolean		keepSelectionOnMouseExit;
	private final LightWeightListenerList<SelectionFrameListener>	listeners = new LightWeightListenerList<>(SelectionFrameListener.class);
	private final List<SelectionStateKeeper>	stateKeepers = new ArrayList<>();
	private final SelectionStateKeeper			ssk = new SelectionStateKeeper();
	
	public SelectionFrameManager(final JComponent component, final boolean keepSelectionOnMouseExit) {
		if (component == null) {
			throw new NullPointerException("Component can't be null"); 
		}
		else {
			this.background = component;
			this.keepSelectionOnMouseExit = keepSelectionOnMouseExit;
			this.ssk.ac = keepSelectionOnMouseExit ? this::automatNoneWithKeep : this::automatNoneWithoutKeep;
			
			this.background.addMouseListener(new MouseListener() {
				@Override public void mouseClicked(MouseEvent e) {}
				@Override public void mouseReleased(final MouseEvent e) {automat(TERM_RELEASED, e.getPoint());}
				@Override public void mousePressed(final MouseEvent e) {automat(TERM_PRESSED, e.getPoint());}
				@Override public void mouseExited(final MouseEvent e) {automat(TERM_EXITED, e.getPoint());}
				@Override public void mouseEntered(MouseEvent e) {automat(TERM_ENTERED, e.getPoint());}
				
			});
			this.background.addMouseMotionListener(new MouseMotionListener() {
				@Override public void mouseMoved(MouseEvent e) {automat(TERM_MOVED, e.getPoint());}
				@Override public void mouseDragged(MouseEvent e) {automat(TERM_DRAGGED, e.getPoint());}
			});
		}
	}

	public SelectionStyle getSelectionStyle() {
		return ssk.currentStyle;
	}
	
	public void setSelectionStyle(final SelectionStyle style) {
		if (style == null) {
			throw new NullPointerException("Style to set can't be null"); 
		}
		else {
			ultimateAutomat(TERM_RESET, null);
			setSelectionStyleInternal(style);
		}
	}
	
	public void pushSelectionStyle(final SelectionStyle style) {
		if (style == null) {
			throw new NullPointerException("Style to push can't be null"); 
		}
		else {
			ultimateAutomat(TERM_RESET, null);
			stateKeepers.add(0,new SelectionStateKeeper(ssk));
			setSelectionStyleInternal(style);
			background.repaint();
		}
	}

	public void popSelectionStyle() {
		if (stateKeepers.isEmpty()) {
			throw new IllegalStateException("Push stack is empty"); 
		}
		else {
			SelectionStateKeeper.assign(stateKeepers.remove(0), ssk);
			background.repaint();
		}
	}
	
	public void enableSelection(final boolean enable) {
		this.ssk.enabled = enable;
		background.repaint();
	}
	
	public boolean isSelectionEnabled() {
		return ssk.enabled;
	}
	
	public void setVisible(final boolean visible) {
		this.ssk.visible = visible;
		background.repaint();
	}
	
	public boolean isVisible() {
		return ssk.visible;
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
		switch (ssk.currentStyle) {
			case NONE		:
				break;
			case PATH		:
				g2d.draw(ssk.path);
				break;
			case POINT		:
				g2d.drawLine(0, ssk.currentP.y, background.getWidth(), ssk.currentP.y);
				g2d.drawLine(ssk.currentP.x, 0, ssk.currentP.x, background.getHeight());
				break;
			case LINE		:
				g2d.drawLine(ssk.startP.x, ssk.startP.y, ssk.currentP.x, ssk.currentP.y);
				break;
			case RECTANGLE	:
				g2d.drawRect(ssk.currentFrame.x, ssk.currentFrame.y, ssk.currentFrame.width, ssk.currentFrame.height);
				break;
			default :
				throw new UnsupportedOperationException("Current selection style ["+ssk.currentStyle+"] is not supported yet");
		}
		g2d.setStroke(oldStroke);
	}
	
	private void automat(final int terminal, final Point pt) {
//		System.err.println("Automat: "+terminal+", "+pt+", state="+ssk.automatState);
		if (isSelectionEnabled()) {
			ultimateAutomat(terminal, pt);
		}
	}

	private void ultimateAutomat(final int terminal, final Point pt) {
//		System.err.println("Ultimate automat: "+terminal+", "+pt+", state="+ssk.automatState);
		if (ssk.ac != null) {
			ssk.ac.automat(terminal, pt);
		}
		else {
			int x = 10;
		}
	}

	private void automatNoneWithKeep(final int terminal, final Point parameter) {
	}
	
	private void automatNoneWithoutKeep(final int terminal, final Point parameter) {
	}
	
	private void automatRectWithKeep(final int terminal, final Point parameter) {
		automatRectWithoutKeep(terminal, parameter);
	}
	
	private void automatRectWithoutKeep(final int terminal, final Point parameter) {
		switch (ssk.automatState) {
			case STATE_INITIAL	:
				switch (terminal) {
					case TERM_PRESSED	:
						ssk.automatState = STATE_SELECTION_IN_PROGRESS;
						clearSelections();
						storeInitialPoint(parameter);
						fireSelectionStarted();
						break;
					case TERM_RESET		:
						clearSelections();
						break;
				}
				break;
			case STATE_SELECTION_IN_PROGRESS	:
				switch (terminal) {
					case TERM_RESET		:
						ssk.automatState = STATE_INITIAL;
						clearSelections();
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
						ssk.automatState = STATE_INITIAL;
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
				throw new UnsupportedOperationException("Automat state ["+ssk.automatState+"] is not supported yet");
		}
	}

	private void automatLineWithKeep(final int terminal, final Point parameter) {
		automatRectWithoutKeep(terminal, parameter);
	}
	
	private void automatLineWithoutKeep(final int terminal, final Point parameter) {
		switch (ssk.automatState) {
			case STATE_INITIAL	:
				switch (terminal) {
					case TERM_PRESSED	:
						ssk.automatState = STATE_SELECTION_IN_PROGRESS;
						clearSelections();
						storeInitialPoint(parameter);
						fireSelectionStarted();
						break;
					case TERM_RESET		:
						clearSelections();
						break;
				}
				break;
			case STATE_SELECTION_IN_PROGRESS	:
				switch (terminal) {
					case TERM_RESET		:
						ssk.automatState = STATE_INITIAL;
						clearSelections();
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
						ssk.automatState = STATE_INITIAL;
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
				throw new UnsupportedOperationException("Automat state ["+ssk.automatState+"] is not supported yet");
		}
	}

	private void automatPathWithKeep(final int terminal, final Point parameter) {
		automatRectWithoutKeep(terminal, parameter);
	}
	
	private void automatPathWithoutKeep(final int terminal, final Point parameter) {
		switch (ssk.automatState) {
			case STATE_INITIAL	:
				switch (terminal) {
					case TERM_PRESSED	:
						ssk.automatState = STATE_SELECTION_IN_PROGRESS;
						clearSelections();
						storeInitialPoint(parameter);
						fireSelectionStarted();
						break;
					case TERM_RESET		:
						clearSelections();
						break;
				}
				break;
			case STATE_SELECTION_IN_PROGRESS	:
				switch (terminal) {
					case TERM_RESET		:
						ssk.automatState = STATE_INITIAL;
						clearSelections();
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
						ssk.automatState = STATE_INITIAL;
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
				throw new UnsupportedOperationException("Automat state ["+ssk.automatState+"] is not supported yet");
		}
	}

	private void automatPointWithKeep(final int terminal, final Point parameter) {
		automatRectWithoutKeep(terminal, parameter);
	}
	
	private void automatPointWithoutKeep(final int terminal, final Point parameter) {
		switch (ssk.automatState) {
			case STATE_INITIAL	:
				ssk.automatState = STATE_SELECTION_IN_PROGRESS;
				clearSelections();
				storeInitialPoint(new Point(0,0));
				fireSelectionStarted();
				break;
			case STATE_SELECTION_IN_PROGRESS	:
				switch (terminal) {
					case TERM_PRESSED	:
						ssk.automatState = STATE_INITIAL;
						storeCurrentPoint(parameter);
						resizeRectangle();
						fireSelectionCompleted();
						refreshContent();
						enableSelection(false);
						break;
					case TERM_MOVED		:
						storeCurrentPoint(parameter);
						resizeRectangle();
						fireSelectionChanged();
						refreshContent();
						break;
					case TERM_RESET		:
						ssk.automatState = STATE_INITIAL;
						fireSelectionCancelled();
						clearSelections();
						refreshContent();
						break;
				}
				break;
			default :
				throw new UnsupportedOperationException("Automat state ["+ssk.automatState+"] is not supported yet");
		}
	}
	
	private void clearSelections() {
		ssk.currentFrame.setBounds(0, 0, 0, 0);
		ssk.startP.setLocation(0, 0);
		ssk.currentP.setLocation(0, 0);
		ssk.path.reset();
	}
	
	private void storeInitialPoint(final Point p) {
		ssk.startP.setLocation(p);
		ssk.currentP.setLocation(p);
		ssk.path.moveTo(p.x, p.y);
	}

	private void storeCurrentPoint(final Point p) {
		ssk.currentP.setLocation(p);
		ssk.path.lineTo(p.x, p.y);
	}

	private void resizeRectangle() {
		prepareRectangle();
	}
	
	private void fireSelectionStarted() {
		switch (ssk.currentStyle) {
			case NONE		:
				break;
			case PATH		:
				break;
			case POINT		:
				break;
			case LINE		:
				listeners.fireEvent((l)->l.selectionStarted(getSelectionStyle(), ssk.startP));
				break;
			case RECTANGLE	:
				listeners.fireEvent((l)->l.selectionStarted(getSelectionStyle(), ssk.startP));
				break;
			default :
				throw new UnsupportedOperationException("Current selection style ["+ssk.currentStyle+"] is not supported yet");
		}
	}

	private void fireSelectionChanged() {
		switch (ssk.currentStyle) {
			case NONE		:
				break;
			case PATH		:
				break;
			case POINT		:
				break;
			case LINE		:
				listeners.fireEvent((l)->l.selectionChanging(getSelectionStyle(), ssk.startP, ssk.currentP, ssk.currentFrame));
				break;
			case RECTANGLE	:
				listeners.fireEvent((l)->l.selectionChanging(getSelectionStyle(), ssk.startP, ssk.currentP, ssk.currentFrame));
				break;
			default :
				throw new UnsupportedOperationException("Current selection style ["+ssk.currentStyle+"] is not supported yet");
		}
	}

	private void fireSelectionCancelled() {
		switch (ssk.currentStyle) {
			case NONE		:
				break;
			case PATH		:
				break;
			case POINT		:
				break;
			case LINE		:
				listeners.fireEvent((l)->l.selectionCancelled(getSelectionStyle(), ssk.startP, ssk.currentP));
				break;
			case RECTANGLE	:
				listeners.fireEvent((l)->l.selectionCancelled(getSelectionStyle(), ssk.startP, ssk.currentP));
				break;
			default :
				throw new UnsupportedOperationException("Current selection style ["+ssk.currentStyle+"] is not supported yet");
		}
	}

	private void fireSelectionCompleted() {
		switch (ssk.currentStyle) {
			case NONE		:
				break;
			case PATH		:
				listeners.fireEvent((l)->l.selectionCompleted(getSelectionStyle(), ssk.startP, ssk.currentP, ssk.path));
				break;
			case POINT		:
				listeners.fireEvent((l)->l.selectionCompleted(getSelectionStyle(), ssk.startP, ssk.currentP, ssk.currentFrame));
				break;
			case LINE		:
				listeners.fireEvent((l)->l.selectionCompleted(getSelectionStyle(), ssk.startP, ssk.currentP, ssk.currentFrame));
				break;
			case RECTANGLE	:
				listeners.fireEvent((l)->l.selectionCompleted(getSelectionStyle(), ssk.startP, ssk.currentP, ssk.currentFrame));
				break;
			default :
				throw new UnsupportedOperationException("Current selection style ["+ssk.currentStyle+"] is not supported yet");
		}
	}

	private void refreshContent() {
		final Rectangle	rect = new Rectangle();
		final int		width = background.getWidth();  
		final int		height = background.getWidth();  
		
		switch (ssk.currentStyle) {
			case NONE		:
				break;
			case PATH		:
				final Rectangle	bound = ssk.path.getBounds();
				
				rect.setBounds(bound.x - REFRESH_BOUND, bound.y - REFRESH_BOUND, bound.width + 2 * REFRESH_BOUND, bound.height + 2 * REFRESH_BOUND);
				background.repaint(rect);
				break;
			case POINT		:
				rect.setFrame(0, ssk.currentP.y - REFRESH_BOUND, width, 2 * REFRESH_BOUND);
				background.repaint(rect);
				rect.setFrame(ssk.currentP.x - REFRESH_BOUND, 0, 2 * REFRESH_BOUND, height);
				background.repaint(rect);
				break;
			case LINE : case RECTANGLE :
				rect.setFrameFromDiagonal(Math.min(ssk.currentFrame.x, ssk.prevFrame.x) - REFRESH_BOUND, Math.min(ssk.currentFrame.y, ssk.prevFrame.y) - REFRESH_BOUND, 
										  Math.max(ssk.currentFrame.x+ssk.currentFrame.width, ssk.prevFrame.x+ssk.prevFrame.width) + 2 * REFRESH_BOUND, 
										  Math.max(ssk.currentFrame.y+ssk.currentFrame.height, ssk.prevFrame.y+ssk.prevFrame.height) + 2 * REFRESH_BOUND);
				background.repaint(rect);
				break;
			default :
				throw new UnsupportedOperationException("Current selection style ["+ssk.currentStyle+"] is not supported yet");
		}
	}
	
	private void prepareRectangle() {
		ssk.prevFrame.setFrame(ssk.currentFrame);
		ssk.currentFrame.setFrameFromDiagonal(Math.min(ssk.startP.x, ssk.currentP.x), Math.min(ssk.startP.y, ssk.currentP.y), Math.max(ssk.startP.x, ssk.currentP.x), Math.max(ssk.startP.y, ssk.currentP.y));
	}

	private void setSelectionStyleInternal(final SelectionStyle style) {
		switch (ssk.currentStyle = style) {
			case NONE		:
				ssk.ac = keepSelectionOnMouseExit ? this::automatNoneWithKeep : this::automatNoneWithoutKeep;
				break;
			case PATH		:
				ssk.ac = keepSelectionOnMouseExit ? this::automatPathWithKeep : this::automatPathWithoutKeep;
				break;
			case POINT		:
				ssk.ac = keepSelectionOnMouseExit ? this::automatPointWithKeep : this::automatPointWithoutKeep;
				break;
			case LINE		:
				ssk.ac = keepSelectionOnMouseExit ? this::automatLineWithKeep : this::automatLineWithoutKeep;
				break;
			case RECTANGLE	:
				ssk.ac = keepSelectionOnMouseExit ? this::automatRectWithKeep : this::automatRectWithoutKeep;
				break;
			default :
				throw new UnsupportedOperationException("Selection style ["+style+"] is not supprted yet"); 
		}
	}
	
	private static class SelectionStateKeeper {
		private final Rectangle		currentFrame = new Rectangle();
		private final Rectangle		prevFrame = new Rectangle();
		private final Point			startP = new Point(0,0);
		private final Point			currentP = new Point(0,0);
		private final GeneralPath	path = new GeneralPath();
		private SelectionStyle		currentStyle;
		private AutomatCall			ac = (t,p)->{};
		private boolean				enabled;
		private boolean 			visible; 
		private int					automatState;
		
		private SelectionStateKeeper() {
			this.currentStyle = SelectionStyle.NONE;
			this.enabled = false;
			this.visible = false; 
			this.automatState = STATE_INITIAL;
		}
		
		private SelectionStateKeeper(final SelectionStateKeeper another) {
			assign(this,another);
		}

		@Override
		public String toString() {
			return "SelectionStateKeeper [currentFrame=" + currentFrame + ", prevFrame=" + prevFrame + ", startP="
					+ startP + ", currentP=" + currentP + ", currentStyle=" + currentStyle + ", enabled=" + enabled
					+ ", visible=" + visible + ", automatState=" + automatState + "]";
		}
		
		private static void assign(final SelectionStateKeeper from, final SelectionStateKeeper to) {
			to.currentFrame.setFrame(from.currentFrame);
			to.prevFrame.setFrame(from.prevFrame);
			to.startP.setLocation(from.startP);
			to.currentP.setLocation(from.currentP);
			to.path.reset();
			to.path.append(from.path, false);
			to.currentStyle = SelectionStyle.NONE;
			to.ac = from.ac;	
			to.enabled = from.enabled;
			to.visible = from.visible;
			to.automatState = from.automatState;
		}
	}
}
