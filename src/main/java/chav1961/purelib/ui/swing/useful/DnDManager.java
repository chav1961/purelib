package chav1961.purelib.ui.swing.useful;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
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
	
	private final TotalMouseListener	totalListener = new TotalMouseListener() {
												@Override public void mouseClicked(MouseEvent e) {}
												@Override public void mouseMoved(MouseEvent e) {}
												@Override public void mouseWheelMoved(MouseWheelEvent e) {}
										
												@Override
												public void mousePressed(MouseEvent e) {
													wasPressed = true;
												}
										
												@Override
												public void mouseReleased(MouseEvent e) {
													wasPressed = false;
													if (wasDragged && class2Process != null) {
														if (e.getComponent() != owner) {
															if (dndInterface.canReceive(currentDnDMode(),sourceComponent,sourcePoint.x,sourcePoint.x, e.getComponent(), e.getPoint().x, e.getPoint().y,class2Process)) {
																dndInterface.complete(currentDnDMode(),sourceComponent,sourcePoint.x,sourcePoint.y,e.getComponent(), e.getPoint().x, e.getPoint().y
																		,dndInterface.getSource(currentDnDMode(),sourceComponent,sourcePoint.x,sourcePoint.y,e.getComponent(),e.getPoint().x,e.getPoint().y)
																		);
															}
														}
														e.getComponent().setCursor(savedCursor);
													}
													wasDragged = false;
												}
										
												@Override
												public void mouseEntered(MouseEvent e) {
													if (wasDragged && currentDnDMode != DnDMode.NONE && class2Process != null) {
														if (e.getComponent() == owner) {
															validTarget = true;
														}
														else {
															validTarget = dndInterface.canReceive(currentDnDMode(),sourceComponent,sourcePoint.x,sourcePoint.x, e.getComponent(), e.getPoint().x, e.getPoint().y,class2Process);
														}
														savedCursor = e.getComponent().getCursor();
														e.getComponent().setCursor(currentCursor(e.getComponent()));
													}
												}

												@Override 
												public void mouseExited(MouseEvent e) {
													if (wasDragged && currentDnDMode != DnDMode.NONE && class2Process != null) {
														e.getComponent().setCursor(savedCursor);
														validTarget = true;
													}
												}
												
												@Override
												public void mouseDragged(MouseEvent e) {
													if (currentDnDMode != DnDMode.NONE) {
														if (!wasDragged) {
															class2Process = dndInterface.getSourceClass(currentDnDMode(),e.getComponent(), e.getX(), e.getY());
															sourceComponent = e.getComponent();
															sourcePoint = e.getPoint();
															wasDragged = true;
														}
														else if (wasDragged && currentDnDMode == DnDMode.LINK && class2Process != null) {
															final Point	pFrom = SwingUtilities.convertPoint(sourceComponent,sourcePoint,owner);
															final Point	pTo = SwingUtilities.convertPoint(e.getComponent(),e.getPoint(),owner);
															
															dndInterface.track(DnDMode.LINK,sourceComponent,pFrom.x,pFrom.y,e.getComponent(),pTo.x,pTo.y);
														}
													}
												}
										
											};
	private final Container				owner;
	private final DnDInterface			dndInterface;
	
	private DnDMode						currentDnDMode = DnDMode.NONE; 
	private Component					sourceComponent;
	private Point						sourcePoint;
	private boolean						wasPressed = false, wasDragged = false, validTarget = false;
	private Cursor						savedCursor = null, dragCursor = null;
	private Class<?>					class2Process = null;
	
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
	
	private Cursor currentCursor(final Component component) {
		switch (currentDnDMode()) {
			case COPY	: return validTarget ? DragSource.DefaultCopyDrop : DragSource.DefaultCopyNoDrop; 
			case LINK	: return validTarget ? DragSource.DefaultLinkDrop : DragSource.DefaultLinkNoDrop;
			case MOVE	: return validTarget ? DragSource.DefaultMoveDrop : DragSource.DefaultMoveNoDrop;
			case NONE	: return component.getCursor();
			default		: throw new UnsupportedOperationException("Drag&Drop mode ["+currentDnDMode()+"] is not supported yet"); 
		}
	}
}
