package chav1961.purelib.ui.swing.useful;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

public class ComponentKeepedBorder implements Border {
	private JComponent 			parent;
	private final JComponent[] 	components;
	private int 				gap = 0;
	private Insets 				borderInsets = new Insets(0, 0, 0, 0);
	
	public ComponentKeepedBorder(final int gap, final JComponent... components) {
		this.gap = gap;
		this.components = components;
	}

	@Override
	public Insets getBorderInsets(final Component c) {
		return borderInsets;
	}

	@Override
	public boolean isBorderOpaque() {
		return false;
	}

	@Override
	public void paintBorder(final Component c, final Graphics g, final int x, final int y, final int width, final int height) {
		final Insets 	parentInsets = parent.getInsets();
		
		for (int index = 0; index < components.length; index++) {
			final float 	xComponent = (width  - components[components.length-1-index].getWidth() - index * gap) + x;
			
			components[components.length-1-index].setLocation((int)xComponent, parentInsets.top);
		}
	}

	public void install(final JComponent parent) {
		this.parent = parent;
		determineInsetsAndAlignment();

		final Border current = parent.getBorder();

		if (current == null) {
			parent.setBorder(this);
		}
		else {
			final CompoundBorder 	compound = new CompoundBorder(current, this);
			parent.setBorder(compound);
		}
		for (JComponent item : components) {
			parent.add(item);
		}
	}

	private void determineInsetsAndAlignment() {
		final Insets 	parentInsets = parent.getInsets();
		final int 		parentHeight = parent.getPreferredSize().height - parentInsets.top - parentInsets.bottom;
		
		borderInsets = new Insets(0, 0, 0, 0);
		
		for (int index = 0; index < components.length; index++) {
			borderInsets.right += parentHeight + gap;
			components[index].setAlignmentX(1.0f);
			components[index].setAlignmentY(0.5f);
			components[index].setSize(parentHeight,parentHeight);
		}
	}
}
