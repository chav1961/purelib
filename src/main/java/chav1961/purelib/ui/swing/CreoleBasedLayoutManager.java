package chav1961.purelib.ui.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;

class CreoleBasedLayoutManager implements LayoutManager2 {
	private final JEditorPane	parentContainer;
	private final List<Content>	content = new ArrayList<>();
	private int					insideInvalidation = 0;

	CreoleBasedLayoutManager(final JEditorPane parentContainer) {
		this.parentContainer = parentContainer;
	}

	@Override
	public void addLayoutComponent(final String name, final Component comp) {
		content.add(new Content(comp.getName(),new int[] {0,0},comp));
	}

	@Override
	public void removeLayoutComponent(final Component comp) {
		for (int index = 0; index < content.size(); index++) {
			if (content.get(index).component.equals(comp)) {
				content.remove(index);
				return;
			}
		}
	}

	@Override
	public Dimension preferredLayoutSize(final Container parent) {
		return parentContainer.getPreferredSize();
	}

	@Override
	public Dimension minimumLayoutSize(final Container parent) {
		return parentContainer.getMinimumSize();
	}

	@Override
	public void layoutContainer(final Container parent) {
		invalidateLayout(parent);
	}

	@Override
	public void addLayoutComponent(final Component comp, final Object constraints) {
		content.add(new Content(comp.getName(),constraints,comp));
	}

	@Override
	public Dimension maximumLayoutSize(final Container target) {
		return parentContainer.getMaximumSize();
	}

	@Override
	public float getLayoutAlignmentX(final Container target) {
		return 0;
	}

	@Override
	public float getLayoutAlignmentY(final Container target) {
		return 0;
	}

	@Override
	public void invalidateLayout(final Container target) {
		try{if (insideInvalidation++ == 0) {
				for (int index = 0; index < content.size(); index++) {
					try{final Rectangle	rectStart = parentContainer.modelToView(((int[])content.get(index).constraint)[0]);
						final Rectangle	rectEnd = parentContainer.modelToView(((int[])content.get(index).constraint)[1]);
						
						if (rectStart != null) {
							content.get(index).component.setBounds(rectStart.x,rectStart.y,rectEnd.x-rectStart.x,rectStart.height);
						}
					} catch (BadLocationException e) {
					} 
				}
			}
		} finally {
			insideInvalidation--;
		}
	}
	
	private static class Content {
		final String	name;
		final Object	constraint;
		final Component	component;
		
		Content(String name, Object constraint, Component component) {
			this.name = name;
			this.constraint = constraint;
			this.component = component;
		}

		@Override
		public String toString() {
			return "Content [name=" + name + ", constraint=" + constraint + ", component=" + component + "]";
		}
	}
}
