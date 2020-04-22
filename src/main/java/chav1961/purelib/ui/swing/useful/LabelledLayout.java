package chav1961.purelib.ui.swing.useful;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.SizeRequirements;

import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;

public class LabelledLayout implements LayoutManager2, Serializable {
	public static final String		LABEL_AREA = "labelArea";
	public static final String		CONTENT_AREA = "contentArea";
	public static final int			HORIZONTAL_FILLING = 1;
	public static final int			VERTICAL_FILLING = 2;

	private static final long 		serialVersionUID = 5377169415875489416L;
	private static final String		ABSTRACT_TEXT = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final JLabel		NULL_LABEL = new JLabel(); 
	
	private final int				numberOfBars, hGap, vGap, filling;
	private final List<Component>	labels = new ArrayList<>();
	private final List<Component>	content = new ArrayList<>();

	public LabelledLayout() {
		this(1,0,0,VERTICAL_FILLING);
	}

	public LabelledLayout(final int hGap, final int vGap) {
		this(1,hGap,vGap,VERTICAL_FILLING);
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
				default : throw new IllegalArgumentException("Unknown constraint name ["+name+"]. Only 'LABEL_AREA' or 'CONTENT_AREA' are available");
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
		else if (parent.getComponentCount() == 0) {
			return new Dimension(0,0);
		}
		else if (numberOfBars == 1) {
			final SizeRequirements[]	size = calculateAreaSize(toPairs(labels,content));
			
			return addInsets(Math.min(Short.MAX_VALUE,size[0].minimum+3*hGap),Math.min(Short.MAX_VALUE,size[1].minimum+vGap*(size.length+1)),parent.getInsets());
		}
		else {
			final SizeRequirements[]	size = calculateAreaSize(split(toPairs(labels,content),numberOfBars,filling));
			
			return addInsets(Math.min(Short.MAX_VALUE,size[0].minimum+2*numberOfBars*hGap),Math.min(Short.MAX_VALUE,size[1].minimum+2*numberOfBars*vGap),parent.getInsets());
		}
	}

	@Override
	public Dimension preferredLayoutSize(final Container parent) {
		if (parent == null) {
			throw new NullPointerException("Parent container can't be null");
		}
		else if (parent.getComponentCount() == 0) {
			return new Dimension(0,0);
		}
		else if (numberOfBars == 1) {
			final SizeRequirements[]	size = calculateAreaSize(toPairs(labels,content));
			
			return addInsets(Math.min(Short.MAX_VALUE,size[0].preferred+2*hGap),Math.min(Short.MAX_VALUE,size[1].preferred+vGap*(size.length+1)),parent.getInsets());
		}
		else {
			final SizeRequirements[]	size = calculateAreaSize(split(toPairs(labels,content),numberOfBars,filling));
			
			return addInsets(Math.min(Short.MAX_VALUE,size[0].preferred+2*numberOfBars*hGap),Math.min(Short.MAX_VALUE,size[1].preferred+2*numberOfBars*vGap),parent.getInsets());
		}
	}

	@Override
	public Dimension maximumLayoutSize(final Container parent) {
		if (parent == null) {
			throw new NullPointerException("Parent container can't be null");
		}
		else if (parent.getComponentCount() == 0) {
			return new Dimension(0,0);
		}
		else if (numberOfBars == 1) {
			final SizeRequirements[]	size = calculateAreaSize(toPairs(labels,content));
			
			return addInsets(Math.min(Short.MAX_VALUE,size[0].maximum+2*hGap),Math.min(Short.MAX_VALUE,size[1].maximum+vGap*(size.length+1)),parent.getInsets());
		}
		else {
			final SizeRequirements[]	size = calculateAreaSize(split(toPairs(labels,content),numberOfBars,filling));
			
			return addInsets(Math.min(Short.MAX_VALUE,size[0].maximum+2*numberOfBars*hGap),Math.min(Short.MAX_VALUE,size[1].maximum+2*numberOfBars*vGap),parent.getInsets());
		}
	}
	
	@Override
	public void layoutContainer(final Container parent) {
		if (parent == null) {
			throw new NullPointerException("Parent container can't be null");
		}
		else if (numberOfBars == 1) {
			final Dimension	parentSize = parent.getSize();
			final Dimension preferredSize = preferredLayoutSize(parent);
			final Pair[]	pairs = toPairs(labels,content);
			final Insets	ins = parent.getInsets();
			
			parentSize.width -= ins.left + ins.right + (2 * hGap);
			parentSize.height -= ins.top + ins.bottom + ((pairs.length + 1)* vGap);
			
			final float		yScale = preferredSize.height < parentSize.height 
								? 1.0f * parentSize.height / preferredSize.height 
								: 1.0f; 
			final float		xScale = preferredSize.width < parentSize.width 
								? 1.0f * parentSize.width / preferredSize.width
								: 1.0f;
			int		maxLabelWidth = 0, minContentWidth = parentSize.width;
			
			for (int index = 0; index < pairs.length; index++) {
				final int 	labelWidth = pairs[index].label.getPreferredSize().width > parentSize.width 
									? (int)(xScale * pairs[index].label.getPreferredSize().width)
									: pairs[index].label.getPreferredSize().width;
				final int	contentWidth = parentSize.width - labelWidth;
				
				maxLabelWidth = Math.max(maxLabelWidth,labelWidth);
				minContentWidth = Math.min(minContentWidth, contentWidth);
			}

			for (int index = 0, yPos = vGap; index < pairs.length; index++) {
				final int 	labelHeight = (int) (yScale * pairs[index].label.getPreferredSize().height);
				final int	contentHeight = (int) (yScale * pairs[index].content.getPreferredSize().height);
				final int	cellHeight = Math.max(labelHeight, contentHeight);
				
				pairs[index].label.setBounds(hGap,yPos,maxLabelWidth,cellHeight);
				pairs[index].content.setBounds(2*hGap+maxLabelWidth,yPos,minContentWidth,cellHeight);
				yPos += cellHeight + vGap;
			}
		}
		else {
			final Dimension	parentSize = parent.getSize();
			final Dimension preferredSize = preferredLayoutSize(parent);
			final Pair[]	pairs = toPairs(labels,content);
			final Pair[][]	splits = split(pairs, numberOfBars, filling);
			final Insets	ins = parent.getInsets();
			final int		barWidth = parentSize.width /numberOfBars; 
			
			parentSize.width -= ins.left + ins.right + (2 * (splits.length + 1) * hGap);
			parentSize.height -= ins.top + ins.bottom + ((pairs.length + 1)* vGap / numberOfBars);
			
			final float		yScale = preferredSize.height > parentSize.height 
								? 1.0f * parentSize.height / preferredSize.height 
								: 1.0f; 
			final float		xScale = preferredSize.width > barWidth 
								? 1.0f * parentSize.width / barWidth
								: 1.0f;
			int		maxLabelWidth = 0, minContentWidth = parentSize.width;
			
			for (int index = 0; index < pairs.length; index++) {
				final int 	labelWidth = pairs[index].label.getPreferredSize().width > barWidth 
									? (int)(xScale * pairs[index].label.getPreferredSize().width)
									: pairs[index].label.getPreferredSize().width;
				final int	contentWidth = barWidth - labelWidth;
				
				maxLabelWidth = Math.max(maxLabelWidth,labelWidth);
				minContentWidth = Math.min(minContentWidth, contentWidth);
			}

			for (int bar = 0; bar < splits.length; bar++) {
				for (int index = 0, yPos = vGap; index < splits[bar].length; index++) {
					final int 	labelHeight = (int) (yScale * splits[bar][index].label.getPreferredSize().height);
					final int	contentHeight = (int) (yScale * splits[bar][index].content.getPreferredSize().height);
					final int	cellHeight = Math.max(labelHeight, contentHeight);
					
					splits[bar][index].label.setBounds(bar*barWidth+hGap,yPos,maxLabelWidth,cellHeight);
					splits[bar][index].content.setBounds(bar*barWidth+2*hGap+maxLabelWidth,yPos,minContentWidth,cellHeight);
					yPos += cellHeight + vGap;
				}
			}
		}
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
	}

	private Dimension addInsets(final int width, final int height, final Insets insets) {
		return new Dimension((int)Math.min((long)width + insets.left + insets.right, Integer.MAX_VALUE)
							,(int)Math.min((long)height + insets.top + insets.bottom, Integer.MAX_VALUE)
						);
	}

	private static final SizeRequirements[] calculateAreaSize(final Pair[] list) {
		final SizeRequirements[]	x = new SizeRequirements[list.length], y = new SizeRequirements[list.length]; 
		
		for (int index = 0; index < list.length; index++) {
			x[index] = list[index].totalX;
			y[index] = list[index].totalY;
		}
		
		return new SizeRequirements[]{SizeRequirements.getAlignedSizeRequirements(x),SizeRequirements.getTiledSizeRequirements(y)};
	}

	private static final SizeRequirements[] calculateAreaSize(final Pair[][] list) {
		final SizeRequirements[]	totalX = new SizeRequirements[list.length], totalY = new SizeRequirements[list.length];
		
		for (int bar = 0; bar < list.length; bar++) {
			final SizeRequirements[]	x = new SizeRequirements[list[bar].length], y = new SizeRequirements[list[bar].length]; 
			
			for (int index = 0; index < list[bar].length; index++) {
				x[index] = list[bar][index].totalX;
				y[index] = list[bar][index].totalY;
			}
			
			totalX[bar] = SizeRequirements.getAlignedSizeRequirements(x);
			totalY[bar] = SizeRequirements.getTiledSizeRequirements(y);
		}
		return new SizeRequirements[]{SizeRequirements.getAlignedSizeRequirements(totalX),SizeRequirements.getTiledSizeRequirements(totalY)};
	}
	
	private static SizeRequirements toXSizeRequirements(final Component comp) {
		final Dimension		min = comp.getMinimumSize(), pref = comp.getPreferredSize(), max = comp.getMaximumSize();
		final int			preferredSize = calcXSizeByFormat(comp);
		
		if (comp.isVisible()) {
			if (preferredSize > 0) {
				return new SizeRequirements(preferredSize,preferredSize,preferredSize,comp.getAlignmentX());
			}
			else {
				return new SizeRequirements(min != null ? min.width : 0
						,pref != null ? pref.width : calcXSizeByFormat(comp)
						,max != null ? max.width : calcXSizeByFormat(comp)
						,comp.getAlignmentX());
			}
		}
		else {
			return new SizeRequirements(0,0,0,comp.getAlignmentX());
		}
	}

	private static SizeRequirements toYSizeRequirements(final Component comp) {
		final Dimension		min = comp.getMinimumSize(), pref = comp.getPreferredSize(), max = comp.getMaximumSize();
		
		if (comp.isVisible()) {
			return new SizeRequirements(min != null ? min.height : 0
					,pref != null ? pref.height : 0
					,max != null ? max.height : 0
					,comp.getAlignmentY());
		}
		else {
			return new SizeRequirements(0,0,0,comp.getAlignmentY());
		}
	}

	private static Pair[] toPairs(final List<Component> labels, final List<Component> content) {
		final int		maxSize = Math.max(labels.size(), content.size());
		final Pair[]	result = new Pair[maxSize];
		
		for (int index = 0; index < maxSize; index++) {
			result[index] = new Pair(index < labels.size() ? labels.get(index) : NULL_LABEL
									, index < content.size() ? content.get(index) : NULL_LABEL);
		}
		return result;
	}
	
	private static Pair[][] split(Pair[] source, int numberOfBars, int filling) {
		final Pair[][]	result = new Pair[numberOfBars][];
		final int		pieceSize =  (source.length + numberOfBars - 1) / numberOfBars; 
		
		if (filling == HORIZONTAL_FILLING) {
			for (int index = 0, tail = source.length; index < result.length; index++, tail -= pieceSize) {
				result[index] = new Pair[Math.min(pieceSize, tail)];
			}
			for (int index = 0; index < source.length; index++) {
				result[index % numberOfBars][index / numberOfBars] = source[index];
			}
		}
		else {
			for (int index = 0, tail = source.length; index < result.length; index++, tail -= pieceSize) {
				result[index] = Arrays.copyOfRange(source,index*pieceSize,index*pieceSize+Math.min(pieceSize, tail));
			}
		}
		return result;
	}

	private static int calcXSizeByFormat(final Component comp) {
		if (comp instanceof NodeMetadataOwner) {
			final ContentNodeMetadata	meta = ((NodeMetadataOwner)comp).getNodeMetadata();
			
			if (meta.getFormatAssociated() != null) {
				final int				requiredSize = meta.getFormatAssociated().getLength();
				final Font				font = comp.getFont();
				final FontRenderContext frc = new FontRenderContext(null, false, false);
				final Rectangle2D 		boundingBox = font.getStringBounds(ABSTRACT_TEXT.substring(0,Math.min(requiredSize,ABSTRACT_TEXT.length())), frc);

				return boundingBox.getBounds().width;
			}
			else {
				return 0;
			}
		}
		else {
			return 0;
		}
	}

	private static class Pair {
		final Component		label;
		final Component		content;
		SizeRequirements	labelX;
		SizeRequirements	labelY;
		SizeRequirements	contentX;
		SizeRequirements	contentY;
		SizeRequirements	totalX;
		SizeRequirements	totalY;
		
		public Pair(final Component label, final Component content) {
			this.label = label;
			this.content = content;
			this.labelX = toXSizeRequirements(label); 
			this.labelY = toYSizeRequirements(label); 
			this.contentX = toXSizeRequirements(content); 
			this.contentY = toYSizeRequirements(content);
			this.totalX = SizeRequirements.getTiledSizeRequirements(new SizeRequirements[]{this.labelX,this.contentX}); 
			this.totalY = SizeRequirements.getAlignedSizeRequirements(new SizeRequirements[]{this.labelY,this.contentY}); 
		}
	}
}
