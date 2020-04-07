package chav1961.purelib.ui.swing.useful;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.dnd.DragSource;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.SwingUtilities;

import chav1961.purelib.ui.swing.SwingUtils;


public class DnDManager implements AutoCloseable {
	public enum DnDMode {
		NONE, COPY, MOVE, LINK
	}
	
	public interface DnDInterface {
		Class<?> getSourceClass(final DnDMode currentMode, final Component component, final int x, final int y);
		Object getSource(final DnDMode currentMode, final Component from, final int xFrom, final int yFrom, final Component to, final int xTo, final int yTo);
		boolean canReceive(final DnDMode currentMode, final Component from, final int xFrom, final int yFrom, final Component to, final int xTo, final int yTo, final Class<?> contentClass);
		void track(final DnDMode currentMode, final Component from, final int xFromAbsolute, final int yFromAbsolute, final Component to, final int xToAbsolute, final int yToAbsolute);
		void complete(final DnDMode currentMode, final Component from, final int xFrom, final int yFrom, final Component to, final int xTo, final int yTo, final Object content);
	}

	private interface TotalMouseListener extends MouseListener, MouseMotionListener, MouseWheelListener {}

	private enum MouseAction {
		CLICKED, MOVED, WHEEL_MOVED, PRESSED, RELEASED, ENTERED, EXITED, DRAGGED, UNKNOWN
	}
	
	private final TotalMouseListener	totalListener = new TotalMouseListener() {
												@Override public void mouseClicked(final MouseEvent e) {processMouseEvent(MouseAction.CLICKED,e);}
												@Override public void mouseMoved(final MouseEvent e) {processMouseEvent(MouseAction.MOVED,e);}
												@Override public void mouseWheelMoved(final MouseWheelEvent e) {processMouseEvent(MouseAction.WHEEL_MOVED,e);}
												@Override public void mousePressed(final MouseEvent e) {processMouseEvent(MouseAction.PRESSED,e);}
												@Override public void mouseReleased(final MouseEvent e) {processMouseEvent(MouseAction.RELEASED,e);}
												@Override public void mouseEntered(final MouseEvent e) {processMouseEvent(MouseAction.ENTERED,e);}
												@Override public void mouseExited(final MouseEvent e) {processMouseEvent(MouseAction.EXITED,e);}
												@Override public void mouseDragged(final MouseEvent e) {processMouseEvent(MouseAction.DRAGGED,e);}
										
											};
	private final Container				owner;
	private final DnDInterface			dndInterface;
	
	private DnDMode						currentDnDMode = DnDMode.NONE, draggedDndMode = currentDnDMode; 
	private Component					sourceComponent, enteredComponent;
	private Point						sourcePoint;
	private boolean						allowDrag = false, cursorWasSet = false, validTarget = false;
	private Cursor						savedCursor = null, dragCursor = null;
	private Class<?>					class2Process = null;
	private int							lastX = Integer.MAX_VALUE, lastY = Integer.MAX_VALUE;
	private MouseAction					lastAction = MouseAction.UNKNOWN;
	
	public DnDManager(final Container container, final DnDInterface dndInterface) {
		if (container == null) {
			throw new NullPointerException("Container can't be null");
		}
		else if (dndInterface == null) {
			throw new NullPointerException("Drag&Drop interface can't be null");
		}
		else {
			this.owner = container;
			this.dndInterface = dndInterface;
			
			container.addContainerListener(new ContainerListener() {
				@Override
				public void componentRemoved(ContainerEvent e) {
					DnDManager.this.componentRemoved(e.getComponent());
				}
				
				@Override
				public void componentAdded(ContainerEvent e) {
					DnDManager.this.componentAdded(e.getComponent());
				}
			});
			componentAdded(container);
		}
	}

	@Override
	public void close() throws RuntimeException {
		componentRemoved(this.owner);
	}
	
	public DnDMode selectDnDMode(final DnDMode mode) {
		if (mode == null) {
			throw new NullPointerException("Mode to set can't be null");
		}
		else {
			final DnDMode	oldMode = currentDnDMode;
			
			currentDnDMode = mode;
			return oldMode;
		}
	}
	
	public DnDMode currentDnDMode() {
		return currentDnDMode;
	}
	
	private void componentAdded(final Component component) {
		component.addMouseListener(totalListener);
		component.addMouseMotionListener(totalListener);
		component.addMouseWheelListener(totalListener);
		for (Component child : SwingUtils.children(component)) {
			componentAdded(child);
		}
	}

	private void componentRemoved(final Component component) {
		for (Component child : SwingUtils.children(component)) {
			componentRemoved(child);
		}
		component.removeMouseWheelListener(totalListener);
		component.removeMouseMotionListener(totalListener);
		component.removeMouseListener(totalListener);
	}
	
	private void processMouseEvent(final MouseAction action, final MouseEvent event) {
		if (!(action == lastAction && event.getX() == lastX && event.getY() == lastY)) {
			final Component	lastComponent = event.getComponent();
			
			lastX = event.getX();
			lastY = event.getY();
			lastAction = action;
			switch (action) {
				case CLICKED		:
					break;
				case DRAGGED		:
					if (allowDrag) {
						final Point		pFrom = SwingUtilities.convertPoint(sourceComponent,sourcePoint,owner);
						final Point		pTo = SwingUtilities.convertPoint(lastComponent,event.getPoint(),owner);

						if (!cursorWasSet) {
							cursorWasSet = true;
							lastComponent.setCursor(selectCursor(lastComponent,validTarget));
						}
						
						dndInterface.track(draggedDndMode,sourceComponent,pFrom.x,pFrom.y,lastComponent,pTo.x,pTo.y);
					}
					break;
				case ENTERED		:
					if (allowDrag) {
						enteredComponent = lastComponent;
						dragCursor = lastComponent.getCursor();
						validTarget = dndInterface.canReceive(draggedDndMode,sourceComponent,sourcePoint.x,sourcePoint.x,lastComponent,lastX,lastY,class2Process);
						lastComponent.setCursor(selectCursor(lastComponent,validTarget));
					}
					break;
				case EXITED			:
					if (allowDrag) {
						enteredComponent.setCursor(dragCursor);
						enteredComponent = null; 
						validTarget = true;
					}
					break;
				case MOVED			:
					break;
				case PRESSED		:
					if (currentDnDMode != DnDMode.NONE) {
						savedCursor = dragCursor = lastComponent.getCursor();
						sourceComponent = enteredComponent = lastComponent;
						sourcePoint = event.getPoint();
						draggedDndMode = currentDnDMode;
						cursorWasSet = false;
						validTarget = false;
						allowDrag = (class2Process = dndInterface.getSourceClass(draggedDndMode,lastComponent,lastX,lastY)) != null;
					}
					break;
				case RELEASED		:
					if (allowDrag) {
						allowDrag = false;
						sourceComponent.setCursor(savedCursor);
						if (enteredComponent != null) {
							enteredComponent.setCursor(dragCursor);
							if (enteredComponent != owner) {
								final Point	enteredPoint = new Point(event.getLocationOnScreen()); 
								
								SwingUtilities.convertPointFromScreen(enteredPoint,enteredComponent);
								if (dndInterface.canReceive(draggedDndMode,sourceComponent,sourcePoint.x,sourcePoint.x,enteredComponent,enteredPoint.x,enteredPoint.y,class2Process)) {
									dndInterface.complete(draggedDndMode,sourceComponent,sourcePoint.x,sourcePoint.y,enteredComponent,enteredPoint.x,enteredPoint.y
											,dndInterface.getSource(draggedDndMode,sourceComponent,sourcePoint.x,sourcePoint.y,enteredComponent,enteredPoint.x,enteredPoint.y)
											);
								}
							}
						}
					}
					break;
				case WHEEL_MOVED	:
					break;
				case UNKNOWN		:
					break;
				default:
					throw new UnsupportedOperationException("Mouse action ["+action+"] is not supported yet");
			}
		}
	}

	private Cursor selectCursor(final Component component, final boolean validTarget) {
		final Cursor	result;
		
		switch (draggedDndMode) {
			case COPY	: result = validTarget ? DragSource.DefaultCopyDrop : DragSource.DefaultCopyNoDrop; break; 
			case LINK	: result = validTarget ? DragSource.DefaultLinkDrop : DragSource.DefaultLinkNoDrop; break;
			case MOVE	: result = validTarget ? DragSource.DefaultMoveDrop : DragSource.DefaultMoveNoDrop; break;
			case NONE	: result = component.getCursor(); break;
			default		: throw new UnsupportedOperationException("Drag&Drop mode ["+currentDnDMode()+"] is not supported yet"); 
		}
		return result;
	}
}
