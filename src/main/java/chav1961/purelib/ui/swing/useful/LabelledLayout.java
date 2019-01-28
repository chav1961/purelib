package chav1961.purelib.ui.swing.useful;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;

public class LabelledLayout implements LayoutManager2, Serializable {
	public static final String		LABEL_AREA = "labelArea";
	public static final String		CONTENT_AREA = "contentArea";
	public static final int			HORIZONTAL_FILLING = 1;
	public static final int			VERTICAL_FILLING = 2;
	
	private static final int		MIN_CONTENT_WIDTH = 10;
	
	private static final long 		serialVersionUID = 5377169415875489416L;
	
	private final int				numberOfBars, hGap, vGap, filling;
	private final List<Component>	labels = new ArrayList<>();
	private final List<Component>	content = new ArrayList<>();

	public LabelledLayout() {
		this(1,0,0,VERTICAL_FILLING);
	}
	
	public LabelledLayout(final int numberOfBars, final int hGap, final int vGap, final int filling) throws IllegalArgumentException {
		if (numberOfBars <= 0) {
			throw new IllegalArgumentException("Number of bars ["+numberOfBars+"] must be positive"); 
		}
		else if (hGap < 0) {
			throw new IllegalArgumentException("Horizontal gap ["+hGap+"] can't be negative"); 
		}
		else if (vGap < 0) {
			throw new IllegalArgumentException("Vertical gap ["+vGap+"] can't be negative"); 
		}
		else if (filling != HORIZONTAL_FILLING && filling != VERTICAL_FILLING) {
			throw new IllegalArgumentException("Illegal filling ["+vGap+"]. Only HORIZONTAL_FILLING or VERTICAL_FILLING area available"); 
		}
		else {
			this.numberOfBars = numberOfBars;
			this.hGap = hGap;
			this.vGap = vGap;
			this.filling = filling;
		}
	}
	
	@Override
	public void addLayoutComponent(final String name, final Component comp) {
		if (name == null) {
			throw new NullPointerException("Constraint name can't be null. Use either 'LABEL_AREA' or 'CONTENT_AREA'");
		}
		else if (comp == null) {
			throw new NullPointerException("Component to add can't be null");
		}
		else {
			switch (name) {
				case LABEL_AREA 	:
					labels.add(comp);
					break;
				case CONTENT_AREA	:
					content.add(comp);
					break;
				default : throw new UnsupportedOperationException("Unknown constraint name ["+name+"]. Only 'LABEL_AREA' or 'CONTENT_AREA' are available");
			}
			invalidateLayout(comp.getParent());
		}
	}

	@Override
	public void addLayoutComponent(final Component comp, final Object constraints) {
		if (comp == null) {
			throw new NullPointerException("Component to add can't be null");
		}
		else if (constraints instanceof String) {
			addLayoutComponent((String)constraints,comp);
		}
		else {
			throw new IllegalArgumentException("Invalid constraint for component. Use 'LABEL_AREA' or 'CONTENT_AREA'");
		}
	}

	@Override
	public void removeLayoutComponent(final Component comp) {
		if (comp == null) {
			throw new NullPointerException("Component to remove can't be null");
		}
		else {
			labels.remove(comp);
			content.remove(comp);
			invalidateLayout(comp.getParent());
		}
	}

	@Override
	public Dimension minimumLayoutSize(final Container parent) {
		if (parent == null) {
			throw new NullPointerException("Parent container can't be null");
		}
		else {
			int 	labelWidth = Integer.MAX_VALUE, labelHeight = 0;
			int 	contentWidth = Integer.MAX_VALUE, contentHeight = 0;
			
			for (Component item : labels) {
				if (item.getMinimumSize() != null) {
					labelWidth = Math.min(labelWidth, item.getMinimumSize().width);
					labelHeight += item.getMinimumSize().height;
				}
			}
			for (Component item : content) {
				if (item.getMinimumSize() != null) {
					contentWidth = Math.min(labelWidth, item.getMinimumSize().width);
					contentHeight += item.getMinimumSize().height;
				}
			}
			
			return addInsets(labelWidth + contentWidth,Math.max(labelHeight, contentHeight), parent.getInsets()); 
		}
	}

	@Override
	public Dimension preferredLayoutSize(final Container parent) {
		if (parent == null) {
			throw new NullPointerException("Parent container can't be null");
		}
		else {
			int 	labelWidth = 0, labelHeight = 0;
			int 	contentWidth = 0, contentHeight = 0;
			
			for (Component item : labels) {
				if (item.getMinimumSize() != null) {
					labelWidth = Math.max(labelWidth, item.getMaximumSize().width);
					labelHeight += item.getMaximumSize().height;
				}
			}
			for (Component item : content) {
				if (item.getMinimumSize() != null) {
					contentWidth = Math.max(contentWidth, item.getMaximumSize().width);
					contentHeight += item.getMaximumSize().height;
				}
			}
			
			return addInsets(labelWidth + contentWidth,Math.max(labelHeight, contentHeight), parent.getInsets()); 
		}
	}

	@Override
	public Dimension maximumLayoutSize(final Container parent) {
		if (parent == null) {
			throw new NullPointerException("Parent container can't be null");
		}
		else {
			int 	labelWidth = 0, labelHeight = 0;
			int 	contentWidth = 0, contentHeight = 0;
			
			for (Component item : labels) {
				if (item.getMinimumSize() != null) {
					labelWidth = Math.max(labelWidth, item.getMaximumSize().width);
					labelHeight += item.getMaximumSize().height;
				}
			}
			for (Component item : content) {
				if (item.getMinimumSize() != null) {
					contentWidth = Math.max(contentWidth, item.getMaximumSize().width);
					contentHeight += item.getMaximumSize().height;
				}
			}
			
			return addInsets(labelWidth + contentWidth,Math.max(labelHeight, contentHeight), parent.getInsets()); 
		}
	}
	
	@Override
	public void layoutContainer(final Container parent) {
		if (parent == null) {
			throw new NullPointerException("Parent container can't be null");
		}
		else {
			final List<Component>[][]	splittedContent = splitContent(numberOfBars,filling,labels,content);
			final Dimension[][]			sizes = new Dimension[numberOfBars][];
			final int					minContentWidth = calculateMinContentWidth(content);
			
	        for (int index = 0; index < splittedContent.length; index++) {
	        	sizes[index] = new Dimension[]{calculateAreaSize(splittedContent[0][0]), calculateAreaSize(splittedContent[0][1])};
	        }
	        int		totalWidth = 0, totalTruncedWidth = 0, totalLabelWidth = 0, totalHeight = 0;
	        double	xScale = 1.0, xContentScale = 1.0, yScale = 1.0;
	        
	        for (Dimension[] item : sizes) {
	        	totalWidth += item[0].width + item[1].width;
	        	totalLabelWidth += item[0].width;
	        	totalTruncedWidth += item[0].width + minContentWidth;
	        	totalHeight = Math.max(Math.max(totalHeight,item[0].height),item[1].height);
	        }
	        
	        final Dimension				alloc = parent.getSize();
	        final Insets 				in = parent.getInsets();
	        
	        alloc.width -= in.left + in.right;
	        alloc.height -= in.top + in.bottom;
			
	        if (alloc.height < totalHeight) {
	        	yScale = 1.0 * alloc.height / totalHeight;  
	        }
	        if (alloc.width < totalWidth) {
	        	if (alloc.width < totalTruncedWidth) {
	        		xScale = 1.0 * alloc.width / totalWidth; 
	        	}
	        	else {
	        		xContentScale = 1.0 * (alloc.width - totalLabelWidth) / (totalWidth - totalLabelWidth);
	        	}
	        }
	        else {
        		xContentScale = 1.0 * (alloc.width - totalLabelWidth) / (totalWidth - totalLabelWidth);
	        }
	        
//	        for (int i = 0; i < nChildren; i++) {
//	            final Component 	c = parent.getComponent(i);
//	            
//	            c.setBounds((int) Math.min((long) in.left + (long) xOffsets[i], Integer.MAX_VALUE),
//	                        (int) Math.min((long) in.top + (long) yOffsets[i], Integer.MAX_VALUE),
//	                        xSpans[i], ySpans[i]);
//	
//	        }
	
		}
	}

	private static List<Component>[][] splitContent(final int numberOfBars, final int filling, final List<Component> labels, final List<Component> content) {
		final List<Component>[][]	result = new List[numberOfBars][];

		for (int index = 0; index < result.length; index++) {
			result[index] = new List[]{new ArrayList<>(),new ArrayList<>()};
		}
		if (filling == HORIZONTAL_FILLING) {
			for (int index = 0, maxIndex = Math.max(labels.size(), content.size()); index < maxIndex; index++) {
				if (index < labels.size()) {
					result[index % numberOfBars][0].add(labels.get(index));
				}
				else {
					result[index % numberOfBars][0].add(new JLabel(""));
				}
				if (index < content.size()) {
					result[index % numberOfBars][0].add(content.get(index));
				}
				else {
					result[index % numberOfBars][0].add(new JLabel(""));
				}
			}
		}
		else {
			for (int index = 0, maxIndex = Math.max(labels.size(), content.size()); index < maxIndex; index++) {
				if (index < labels.size()) {
					result[index / numberOfBars][0].add(labels.get(index));
				}
				else {
					result[index / numberOfBars][0].add(new JLabel(""));
				}
				if (index < content.size()) {
					result[index / numberOfBars][0].add(content.get(index));
				}
				else {
					result[index / numberOfBars][0].add(new JLabel(""));
				}
			}
		}
		return result;
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
	public synchronized void invalidateLayout(final Container target) {
		// TODO Auto-generated method stub
		
	}

	private Dimension addInsets(final int width, final int height, final Insets insets) {
		return new Dimension((int)Math.min((long)width + insets.left + insets.right, Integer.MAX_VALUE)
							,(int)Math.min((long)height + insets.top + insets.bottom, Integer.MAX_VALUE)
						);
	}
	
	private static Dimension extractSize(final Component comp) {
		if (comp.getPreferredSize() != null) {
			return comp.getPreferredSize(); 
		}
		else if (comp.getMinimumSize() != null) {
			return comp.getMinimumSize(); 
		}
		else if (comp.getMaximumSize() != null) {
			return comp.getMaximumSize(); 
		}
		else {
			return new Dimension(0,0);
		}
	}
	
	private static int calculateMinContentWidth(final List<Component> content) {
		int 	minHeight = Integer.MAX_VALUE;
		
		for (Component item : content) {
			final Dimension	itemSize = extractSize(item);
			
			if (itemSize.height > 0) {
				minHeight = Math.min(minHeight, itemSize.height);
			}
		}
		if (minHeight == Integer.MAX_VALUE) {
			return MIN_CONTENT_WIDTH; 
		}
		else {
			return minHeight;
		}
	}
	
	
//	U+2500 - U+257F - символы рамок
//	U+2580 - U+259F - символы заполнения
	
	private static Dimension calculatePairSize(final Component label, final Component content, final int minContentWidth) {
		final Dimension	labelSize = extractSize(label), contentSize = extractSize(content);
		int				totalHeight = Math.max(labelSize.height,contentSize.height);
		
		if (contentSize.width == 0) {
			contentSize.width = minContentWidth;
		}
		if (labelSize.width == 0) {
			labelSize.width = minContentWidth;
		}
		if (totalHeight == 0) {
			totalHeight = minContentWidth;
		}
		return new Dimension(labelSize.width+contentSize.width, totalHeight);
	}
	
	private static final Dimension calculateAreaSize(final List<Component> list) {
		int		listWidth = 0, listHeight = 0;
		
		for (Component item : list) {
			final Dimension	itemSize = extractSize(item);
			
			listWidth = Math.max(listWidth,itemSize.width);
			listHeight += itemSize.width;
		}
		return new Dimension(listWidth,listHeight);
	}
	
	
}
