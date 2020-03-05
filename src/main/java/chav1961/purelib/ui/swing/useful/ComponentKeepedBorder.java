package chav1961.purelib.ui.swing.useful;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

public class ComponentKeepedBorder implements Border {
	private JComponent 			parent;
	private final JComponent[] 	components;
	private int 				gap = 0;
	private Insets 				borderInsets = new Insets(0, 0, 0, 0);
	private ComponentListener	cl = new ComponentListener() {
									@Override public void componentShown(ComponentEvent e) {resizeOwner(e.getComponent());}
									@Override public void componentResized(ComponentEvent e) {resizeOwner(e.getComponent());}
									@Override public void componentMoved(ComponentEvent e) {resizeOwner(e.getComponent());}
									@Override public void componentHidden(ComponentEvent e) {resizeOwner(e.getComponent());}
								};
	
	public ComponentKeepedBorder(final int gap, final JComponent... components) {
		this.gap = gap;
		this.components = components;
	}

	@Override
	public Insets getBorderInsets(final Component c) {
		if (c instanceof JComponent) {
			determineInsetsAndAlignment((JComponent)c);
		}
		return borderInsets;
	}

	@Override
	public boolean isBorderOpaque() {
		return false;
	}

	@Override
	public void paintBorder(final Component c, final Graphics g, final int x, final int y, final int width, final int height) {
		
//		final Insets 	parentInsets = getBorderInsets(c);
//		
//		System.err.println("X="+x+", Y="+y+", W="+width+", H="+height);
//		for (int index = 0, start = x + width - components.length * height; index < components.length; index++, start += height) {
//			components[components.length-1-index].setLocation(start, parentInsets.top);
//			components[components.length-1-index].setSize(height-2,height-2);
//			System.err.println("Border["+index+"]="+components[components.length-1-index].getBounds()+", "+components[components.length-1-index].isVisible());
//		}
	}
	

	public void install(final JComponent parent) {
		if (this.parent != null) {
			this.parent.removeComponentListener(cl);
		}
		this.parent = parent;
//		determineInsetsAndAlignment(parent);

		final Border current = parent.getBorder();

		if (current == null) {
			parent.setBorder(this);
		}
		else if (current instanceof CompoundBorder) {	// Protection against recursive nesting
			if (((CompoundBorder)current).getOutsideBorder() != this) {
				final CompoundBorder 	compound = new CompoundBorder(current, this);
				parent.setBorder(compound);
			}
		}
		else {
			final CompoundBorder 	compound = new CompoundBorder(current, this);
			parent.setBorder(compound);
		}
		for (int index = 0; index < components.length; index++) {
			parent.add(components[index]);
		}
		parent.addComponentListener(cl);
	}

	private void determineInsetsAndAlignment(final JComponent parent) {
		final Insets 	parentInsets = (parent.getBorder() instanceof ComponentKeepedBorder) ? new Insets(0,0,0,0) : parent.getInsets();
		final int 		parentHeight = parent.getHeight() - parentInsets.top - parentInsets.bottom;
		
		borderInsets = new Insets(0, 0, 0, 0);
		
		for (int index = 0; index < components.length; index++) {
			borderInsets.right += parentHeight + gap;
			components[index].setAlignmentX(1.0f);
			components[index].setAlignmentY(0.5f);
			components[index].setSize(parentHeight,parentHeight);
		}
	}

	protected void resizeOwner(final Component parent) {
		final Rectangle	area = parent.getBounds();
		final Insets 	parentInsets = getBorderInsets(parent);
		
		for (int index = 0, start = area.x + area.width - components.length * (area.height + gap); index < components.length; index++, start += area.height  + gap) {
			components[components.length-1-index].setLocation(start, parentInsets.top);
			components[components.length-1-index].setSize(area.height-2,area.height-2);
			System.err.println("Border["+index+"]="+components[components.length-1-index].getBounds()+", "+components[components.length-1-index].isVisible());
		}
	}

}
