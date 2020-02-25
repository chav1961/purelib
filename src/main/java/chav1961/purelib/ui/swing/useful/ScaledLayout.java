package chav1961.purelib.ui.swing.useful;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ScaledLayout implements LayoutManager2 {
	public enum FillPolicy {
		FILL_MAXIMUM, FILL_MINIMUM
	}

	public enum AlignmentPolicy {
		CENTER(0.5f,0.5f), NORTH(0.0f,0.5f), SOUTH(1.0f,0.5f), EAST(0.0f,0.5f), WEST(1.0f,0.5f),
		NORTH_WEST(0.0f,0.0f), NORTH_EAST(0.0f,1.0f), SOUTH_WEST(1.0f,0.0f), SOUTH_EAST(1.0f,1.0f);
		
		private final float	xAlign, yAlign;
		
		AlignmentPolicy(final float xAlign, final float yAlign) {
			this.xAlign = xAlign;
			this.yAlign = yAlign;
		}
		
		public float getXAlign() {return xAlign;}
		public float getYAlign() {return yAlign;}
	}
	
	private final int				width, height;
	private final FillPolicy		fillPolicy;
	private final AlignmentPolicy	alignmentPolicy;
	private final Map<Component,Rectangle>	cc = new HashMap<>();
	
	public ScaledLayout(final int width, final int height, final FillPolicy fillPolicy, final AlignmentPolicy alignmentPolicy) throws IllegalArgumentException, NullPointerException{
		if (width < 1) {
			throw new IllegalArgumentException("Layout width ["+width+"] must greater than 1"); 
		}
		else if (height < 1) {
			throw new IllegalArgumentException("Layout height ["+height+"] must greater than 1"); 
		}
		else if (fillPolicy == null) {
			throw new NullPointerException("Fill policy can't be null");			
		}
		else if (alignmentPolicy == null) {
			throw new NullPointerException("Alignment policy can't be null");			
		}
		else {
			this.width = width;
			this.height = height;
			this.fillPolicy = fillPolicy;
			this.alignmentPolicy = alignmentPolicy;
		}
	}
	
	@Override
	public void addLayoutComponent(final String name, final Component comp) {
		throw new UnsupportedOperationException("This layout manager doesn't support addition without explicit constraints. Use add(component,constraint) method!"); 
	}

	@Override
	public void removeLayoutComponent(final Component comp) {
		if (comp == null) {
			throw new NullPointerException("Component to remove can't be null");
		}
		else {
			cc.remove(comp);
		}
	}

	@Override
	public Dimension minimumLayoutSize(final Container parent) {
		return new Dimension(width,height);
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		switch (fillPolicy) {
			case FILL_MAXIMUM	:
				return parent.getSize();
			case FILL_MINIMUM	:
				if (parent.getWidth()/width > parent.getHeight()/height) {
					return new Dimension(parent.getHeight()*width/height,parent.getHeight());
				}
				else {
					return new Dimension(parent.getWidth(),parent.getWidth()*height/width);
				}
			default : throw new UnsupportedOperationException("Fill policy ["+fillPolicy+"] is not supported yet");
		}
	}

	@Override
	public Dimension maximumLayoutSize(final Container target) {
		return target.getSize();
	}

	
	@Override
	public void layoutContainer(final Container parent) {
		final Dimension	size = preferredLayoutSize(parent);
		final double	xScale = size.getWidth()/width, yScale = size.getHeight()/height;
		
		for (Entry<Component, Rectangle> item : cc.entrySet()) {
			final Rectangle	rect  = item.getValue();
			
			item.getKey().setBounds((int)(rect.getX()*xScale),(int)(rect.getY()*yScale),(int)(rect.getWidth()*xScale),(int)(rect.getHeight()*yScale));
		}
	}

	@Override
	public void addLayoutComponent(final Component comp, final Object constraints) {
		if (comp == null) {
			throw new NullPointerException("Component to add can't be null"); 
		}
		else if (!(constraints instanceof Rectangle)) {
			throw new IllegalArgumentException("Constraint to add can't be null and it's class must be ["+Rectangle.class+"]"); 
		}
		else {
			final Rectangle	rect = (Rectangle)constraints;
			
			if (rect.x < 0 || rect.x >= width) {
				throw new IllegalArgumentException("X-rectangle value ["+rect.x+"] out of range 0.."+(width-1)); 
			}
			else if (rect.y < 0 || rect.y >= height) {
				throw new IllegalArgumentException("Y-rectangle value ["+rect.y+"] out of range 0.."+(height-1)); 
			}
			else if (rect.x + rect.width < 0 || rect.x + rect.width >= width) {
				throw new IllegalArgumentException("X-rectangle value + width ["+(rect.x + rect.width)+"] out of range 0.."+(width-1)); 
			}
			else if (rect.y + rect.height < 0 || rect.y + rect.height >= height) {
				throw new IllegalArgumentException("Y-rectangle value + height ["+(rect.y + rect.height)+"] out of range 0.."+(height-1)); 
			}
			else {
				cc.put(comp,rect);
			}
		}
	}

	@Override
	public float getLayoutAlignmentX(final Container target) {
		return alignmentPolicy.getXAlign();
	}

	@Override
	public float getLayoutAlignmentY(final Container target) {
		return alignmentPolicy.getYAlign();
	}

	@Override
	public void invalidateLayout(final Container target) {
	}
}
