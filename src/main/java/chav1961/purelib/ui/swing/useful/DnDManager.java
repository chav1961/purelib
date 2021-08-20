package chav1961.purelib.ui.swing.useful;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Objects;

import javax.swing.SwingUtilities;

import chav1961.purelib.ui.swing.SwingUtils;

/**
 * <p>This class is a Drag&amp;Drop manager, that unifies drag&amp;drop operations inside one swing container. It implements {@linkplain AutoCloseable} 
 * interface and can be used in the <b>try-with-resource</b> operators. The constructor of the class passes container to support drag&amp;drop
 * and callback interface {@linkplain DnDInterface} to process drag&amp;drop events. Details about lifecycle of drag&amp;drop are described in the
 * {@linkplain DnDInterface}. Using this class allow you to avoid adding listeners to all of the container content.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @see DnDInterface
 * @since 0.0.4
 * @lastUpate 0.0.5
 */
public class DnDManager implements AutoCloseable {
	/**
	 * <p>This anumerations describes current Drag&amp;drop mode in the swing container.</p> 
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.4
	 * @lastUpate 0.0.5
	 */
	public static enum DnDMode {
		NONE(DnDConstants.ACTION_NONE, null, null), 
		COPY(DnDConstants.ACTION_COPY, DragSource.DefaultCopyDrop, DragSource.DefaultCopyNoDrop), 
		MOVE(DnDConstants.ACTION_MOVE, DragSource.DefaultMoveDrop, DragSource.DefaultMoveNoDrop),
		LINK(DnDConstants.ACTION_LINK, DragSource.DefaultLinkDrop, DragSource.DefaultLinkNoDrop);
		
		private final int		mode;
		private final Cursor	enabledCursor;
		private final Cursor	disabledCursor;
		
		DnDMode(final int mode, final Cursor enabledCursor, final Cursor disabledCursor) {
			this.mode = mode;
			this.enabledCursor = enabledCursor;
			this.disabledCursor = disabledCursor;
		}

		/**
		 * <p>Get Drag&Drop mode as {@linkplain DnDConstants} constant</p>
		 * @return mode returned
		 */
		public int getMode() {
			return mode;
		}
		
		/**
		 * <p>Get cursor for enabled operation of the give type</p>
		 * @return cursor for enabled operation of null for NONE mode
		 */
		public Cursor getEnabledCursor() {
			return enabledCursor;
		}

		/**
		 * <p>Get cursor for disabled operation of the give type</p>
		 * @return cursor for disabled operation of null for NONE mode
		 */
		public Cursor getDisabledCursor() {
			return disabledCursor;
		}
		
		/**
		 * <p>Get Drag&Drop mode by {@linkplain DnDConstants} constants</p>
		 * @param dndConstant constant to get mode for
		 * @return mode for constant
		 * @throws IllegalArgumentException if constant to get mode for is illegal
		 */
		public static DnDMode valueOf(final int dndConstant) throws IllegalArgumentException {
			if ((dndConstant & DnDConstants.ACTION_COPY) != 0) {
				return COPY;
			}
			else if ((dndConstant & DnDConstants.ACTION_MOVE) != 0) {
				return MOVE;
			}
			else if ((dndConstant & DnDConstants.ACTION_LINK) != 0) {
				return LINK;
			}
			else if (dndConstant == 0) {
				return NONE;
			}
			else {
				throw new IllegalArgumentException("Illegal value for constant [" +  dndConstant+ "], only DnDConstants.ACTION_NONE, DnDConstants.ACTION_COPY, DnDConstants.ACTION_MOVE and DnDConstants.ACTION_LINK are available");
			}
		}
	}
	
	/**
	 * <p>This interface is a set of callbacks to support drag&amp;drop operations in conjunction with {@linkplain DnDManager} class.
	 * The lifecycle of the callbacks is:</p>
	 * <ul>
	 * <li>{@linkplain #getSourceContent(DnDMode, Component, int, int, Component, int, int)} calls at the beginning of the drag operation. If it returns non-null value,
	 * drag operation starts. Calls only once for each dedicated drag&amp;drop operation</li>
	 * <li>{@linkplain #canReceive(DnDMode, Component, int, int, Component, int, int, Class)} calls on every mouse moving during drag&amp;drop.</li>
	 * <li>{@linkplain #track(DnDMode, Component, int, int, Component, int, int)} calls on every mouse moving during drag&amp;drop.</li>
	 * <li>{@linkplain #getSourceContent(DnDMode, Component, int, int, Component, int, int)} calls at the end of the drag operation, but before {@linkplain #complete(DnDMode, Component, int, int, Component, int, int, Object)} method call. Calls only once for each dedicated drag&amp;drop operation</li>
	 * <li>{@linkplain #complete(DnDMode, Component, int, int, Component, int, int, Object)} the same last call at the lifecycle. Calls only once for each dedicated drag&amp;drop operation</li>
	 * </ul>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.4
	 */
	public interface DnDInterface {
		/**
		 * <p>Get source content class. This method returns type of the content, located at requested point.</p>
		 * @param currentMode current drag&drop mode. Can't be null
		 * @param component current component. Can't be null
		 * @param x current x-coordinate related to upper-left corner of the component
		 * @param y current y-coordinate related to upper-left corner of the component
		 * @return content type to drag. Null cancels drag operation
		 */
		Class<?> getSourceContentClass(final DnDMode currentMode, final Component component, final int x, final int y);

		/**
		 * <p>Get source content to pass it to {@linkplain #complete(DnDMode, Component, int, int, Component, int, int, Object)} method.</p>
		 * @param currentMode current drag&drop mode. Can't be null
		 * @param from source component. Can't be null
		 * @param xFrom x-coordinate related to upper-left corner of the source component
		 * @param yFrom y-coordinate related to upper-left corner of the source component
		 * @param to target component. Can't be null
		 * @param xTo x-coordinate related to upper-left corner of the target component
		 * @param yTo y-coordinate related to upper-left corner of the target component
		 * @return content to drag&drop. Null cancels drop operation
		 */
		Object getSourceContent(final DnDMode currentMode, final Component from, final int xFrom, final int yFrom, final Component to, final int xTo, final int yTo);
		
		/**
		 * <p>Test the control can receive dragged data.</p>
		 * @param currentMode current drag&drop mode. Can't be null
		 * @param from source component. Can't be null
		 * @param xFrom x-coordinate related to upper-left corner of the source component
		 * @param yFrom y-coordinate related to upper-left corner of the source component
		 * @param to target component. Can't be null
		 * @param xTo x-coordinate related to upper-left corner of the target component
		 * @param yTo y-coordinate related to upper-left corner of the target component
		 * @param contentClass class of the content dragged. Can't be null
		 * @return true if the content can be dropped to the given target control
		 */
		boolean canReceive(final DnDMode currentMode, final Component from, final int xFrom, final int yFrom, final Component to, final int xTo, final int yTo, final Class<?> contentClass);
		
		/**
		 * <p>Track drag&drop operation. Can be used for visualization purposes.</p>
		 * @param currentMode current drag&drop mode. Can't be null
		 * @param from source component. Can't be null
		 * @param xFromAbsolute staring x-coordinate related to upper-left corner of the screen
		 * @param yFromAbsolute staring y-coordinate related to upper-left corner of the screen
		 * @param to target component. Can't be null
		 * @param xToAbsolute current x-coordinate related to upper-left corner of the screen
		 * @param yToAbsolute current y-coordinate related to upper-left corner of the screen
		 */
		void track(final DnDMode currentMode, final Component from, final int xFromAbsolute, final int yFromAbsolute, final Component to, final int xToAbsolute, final int yToAbsolute);
		
		/**
		 * <p>Complete drop operation.</p>
		 * @param currentMode current drag&drop mode. Can't be null
		 * @param from source component. Can't be null
		 * @param xFrom x-coordinate related to upper-left corner of the source component
		 * @param yFrom y-coordinate related to upper-left corner of the source component
		 * @param to target component. Can't be null
		 * @param xTo x-coordinate related to upper-left corner of the target component
		 * @param yTo y-coordinate related to upper-left corner of the target component
		 * @param content content returned by {@linkplain #getSourceContent(DnDMode, Component, int, int, Component, int, int)} method
		 */
		void complete(final DnDMode currentMode, final Component from, final int xFrom, final int yFrom, final Component to, final int xTo, final int yTo, final Object content);
	}

	private interface TotalMouseListener extends MouseListener, MouseMotionListener, MouseWheelListener {}

	public static enum MouseAction {
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
	
	/**
	 * <p>Constructor of the class</p>
	 * @param container container to manage drag&drip inside. Can't be null
	 * @param dndInterface callback interface to support drag&drop lifecycle. Can't be null 
	 * @throws NullPointerException if any parameter is null
	 */
	public DnDManager(final Container container, final DnDInterface dndInterface) throws NullPointerException {
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
	
	/**
	 * <p>Select current Drag&drop mode inside the container.</p>
	 * @param mode mode to select. Can't be null 
	 * @return previous drag&drop mode. Can't be null
	 * @throws NullPointerException if any parameter is null
	 */
	public DnDMode selectDnDMode(final DnDMode mode) throws NullPointerException {
		if (mode == null) {
			throw new NullPointerException("Mode to set can't be null");
		}
		else {
			final DnDMode	oldMode = currentDnDMode;
			
			currentDnDMode = mode;
			return oldMode;
		}
	}
	
	/**
	 * <p>Get current drag&drop mode</p>
	 * @return current drag&drop mode. Can't be null
	 */
	public DnDMode currentDnDMode() {
		return currentDnDMode;
	}
	
	
	public void cut(final Component from) {
		
	}
	
	public void cut(final Component from, final int x, final int y) {
		
	}
	
	public void copy(final Component from) {
		
	}
	
	public void copy(final Component from, final int x, final int y) {
		
	}

	public void paste(final Component to) {
		
	}
	
	public void paste(final Component to, final int x, final int y) {
		
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
						allowDrag = (class2Process = dndInterface.getSourceContentClass(draggedDndMode,lastComponent,lastX,lastY)) != null;
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
									final Object 	src = dndInterface.getSourceContent(draggedDndMode,sourceComponent,sourcePoint.x,sourcePoint.y,enteredComponent,enteredPoint.x,enteredPoint.y);

									if (src != null) {
										dndInterface.complete(draggedDndMode,sourceComponent,sourcePoint.x,sourcePoint.y,enteredComponent,enteredPoint.x,enteredPoint.y,src);
									}
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
		return Objects.requireNonNullElseGet(validTarget ? draggedDndMode.getEnabledCursor() : draggedDndMode.getDisabledCursor(), ()-> component.getCursor()); 
	}
}
