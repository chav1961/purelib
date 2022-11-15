package chav1961.purelib.ui.swing.useful;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;

import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicSliderUI;

import chav1961.purelib.ui.swing.useful.DnDManager.DnDInterface;

/**
 * <p>This class implements range slider.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.6
 */
public class JRangeSlider extends JSlider {
	private static final long serialVersionUID = 5770775931471559072L;

	private Color	lowerThumbColor = Color.RED;
	private Color	upperThumbColor = Color.GREEN;
	private Color	selectionColor = Color.BLUE;

	/**
	 * <p>Constructor of the class. Create slider with (0, 100) range</p> 
	 */
    public JRangeSlider() {
        this(0,100);
    }

    /**
     * <p>Constructor of the class.</p>
     * @param min minimal value.
     * @param max maximal value. Can't be less than minimal value
     * @throws IllegalArgumentException when minimal value is greater than maximal one 
     */
    public JRangeSlider(final int min, final int max) throws IllegalArgumentException {
        super(min, max);
        if (min > max) {
        	throw new IllegalArgumentException("Min value ["+min+"] can't be greater than max value ["+max+"]"); 
        }
    }

    @Override
    public void updateUI() {
        setUI(new RangeSliderUI(this));
        updateLabelUIs();
    }

    /**
     * <p>Set (lower) value
     * @param value value to set. If value is outside of the range minimum..upperValue, will be truncated 
     * @see #setLowerValue(int)
     */
    @Override
    public void setValue(final int value) {
        final int 	oldValue = getValue();
        
        if (oldValue != value) {
	        final int 	oldExtent = getExtent();
	        final int 	newValue = Math.min(Math.max(getMinimum(), value), oldValue + oldExtent);
	        final int 	newExtent = oldExtent + oldValue - newValue;
	
	        getModel().setRangeProperties(newValue, newExtent, getMinimum(), getMaximum(), getValueIsAdjusting());
        }
    }

    /**
     * <p>Get lower value of the range</p>
     */
    public int getLowerValue() {
        return getValue();
    }

    /**
     * <p>Set lower value of the range</p>
     * @param value
     */
    public void setLowerValue(final int value) {
    	setValue(value);
    }
    
    /**
     * <p>Get upper value of the range</p>
     */
    public int getUpperValue() {
        return getValue() + getExtent();
    }

    /**
     * <p>Set upper value of the range</p>
     * @param value upper value to set. If value is outside the lowerValue..maximum, will be trucated  
     */
    public void setUpperValue(final int value) {
    	final int	oldValue = getUpperValue();
    	
    	if (value != oldValue) {
            final int 	lowerValue = getLowerValue();
            final int 	newExtent = Math.min(Math.max(0, value - lowerValue), getMaximum() - lowerValue);
            
            setExtent(newExtent);
    	}
    }

    /**
     * <p>Get range interval as long. Minimal value is upper part of the long, and maximal value is lower part of the long</p>
     * @return range value as long
     */
    public long getRangeValueAsLong() {
    	return ((long)getLowerValue() << 32) | getUpperValue();
    }
    
    /**
     * <p>Set range value</p>
     * @param lower lower range value.
     * @param upper upper range value
     * @see #setLowerValue(int)
     * @see #setUpperValue(int)
     */
    public void setRangeValue(final int lower, final int upper) {
    	setLowerValue(lower);
    	setUpperValue(upper);
    }
    
    /**
     * <p>Set range value from long. Lower value must be placed into the upper part of the long, and upper value must be placed into the lower part of the long</p>  
     * @param value range value to set.
     * @see #setLowerValue(int)
     * @see #setUpperValue(int)
     * @see #getRangeValueAsLong()
     */
    public void setRangeValueAsLong(final long value) {
    	setRangeValue((int)((value >> 32) & 0xFFFFFFFF), (int)(value & 0xFFFFFFFF));
    }

    /**
     * <p>Get color for lower thumb</p>
     * @return color for lower thumb. Can't be null
     */
    public Color getLowerThumbColor() {
		return lowerThumbColor;
	}
    
	/**
	 * <p>Set color for lower thumb</p> 
	 * @param lowerThumbColor lower thumb color to set. Can't be null
	 * @throws NullPointerException when color to set is null
	 */
	public void setLowerThumbColor(final Color lowerThumbColor) throws NullPointerException {
		if (lowerThumbColor == null) {
			throw new NullPointerException("Color to set can't be null");
		}
		else {
			this.lowerThumbColor = lowerThumbColor;
		}
	}

	/**
     * <p>Get color for upper thumb</p>
     * @return color for upper thumb. Can't be null
	 */
	public Color getUpperThumbColor() {
		return upperThumbColor;
	}

	/**
	 * <p>Set color for upper thumb</p> 
	 * @param lowerThumbColor upper thumb color to set. Can't be null
	 * @throws NullPointerException when color to set is null
	 */
	public void setUpperThumbColor(final Color upperThumbColor) throws NullPointerException {
		this.upperThumbColor = upperThumbColor;
	}

	/**
	 * <p>Get range selection color</p>
	 * @return range selection color
	 */
	public Color getSelectionColor() {
		return selectionColor;
	}

	/**
	 * <p>Set color for selection range</p> 
	 * @param lowerThumbColor selection range color to set. Can't be null
	 * @throws NullPointerException when color to set is null
	 */
	public void setSelectionColor(Color selectionColor) throws NullPointerException {
		this.selectionColor = selectionColor;
	}

	private static class RangeSliderUI extends BasicSliderUI {
        private Rectangle	upperThumbRect = new Rectangle();
        private boolean 	upperThumbSelected = false;
        private boolean 	lowerDragging = false;
        private boolean 	upperDragging = false;
        
        private RangeSliderUI(final JRangeSlider owner) {
            super(owner);
        }
        
        @Override
        protected TrackListener createTrackListener(final JSlider slider) {
            return new RangeTrackListener();
        }

        @Override
        protected void calculateThumbSize() {
            super.calculateThumbSize();
            upperThumbRect.setSize(thumbRect.width, thumbRect.height);	// Sync thumbs size
        }
        
        @Override
        protected void calculateThumbLocation() {
            super.calculateThumbLocation();
            
            if (slider.getSnapToTicks()) {
                final int	upperValue = ((JRangeSlider)slider).getUpperValue();
                final int	minorTickSpacing = slider.getMinorTickSpacing();
                final int	majorTickSpacing = slider.getMajorTickSpacing();
                
                int	snappedValue = upperValue; 
                int	tickSpacing = 0;
                
                if (minorTickSpacing > 0) {
                    tickSpacing = minorTickSpacing;
                } else if (majorTickSpacing > 0) {
                    tickSpacing = majorTickSpacing;
                }

                if (tickSpacing != 0) {
                    if ((upperValue - slider.getMinimum()) % tickSpacing != 0) {
                        final float 	temp = (float)(upperValue - slider.getMinimum()) / (float)tickSpacing;
                        
                        snappedValue = slider.getMinimum() + (Math.round(temp) * tickSpacing);
                    }

                    if (snappedValue != upperValue) { 
                        slider.setExtent(snappedValue - slider.getValue());
                    }
                }
            }
            
            if (slider.getOrientation() == JSlider.HORIZONTAL) {
                int upperPosition = xPositionForValue(((JRangeSlider)slider).getUpperValue());
                
                upperThumbRect.x = upperPosition - (upperThumbRect.width / 2);
                upperThumbRect.y = trackRect.y;
                
            } else {
                int upperPosition = yPositionForValue(((JRangeSlider)slider).getUpperValue());
                
                upperThumbRect.x = trackRect.x;
                upperThumbRect.y = upperPosition - (upperThumbRect.height / 2);
            }
        }
        
        @Override
        protected Dimension getThumbSize() {
            return slider.getOrientation() == JSlider.HORIZONTAL ? new Dimension(8, 12) : new Dimension(12, 8);
        }

        @Override
        public void paintTrack(final Graphics g) {
        	final Graphics2D		g2d = (Graphics2D)g;
        	final Stroke			oldStroke = g2d.getStroke();
        	final Color				oldColor = g2d.getColor();
            final Color				rangeColor = ((JRangeSlider)slider).getSelectionColor();
            final Rectangle 		trackBounds = trackRect;
        	
            super.paintTrack(g);
            
            g2d.setColor(rangeColor);
            g2d.setStroke(new BasicStroke(4));
            if (slider.getOrientation() == JSlider.HORIZONTAL) {
                final int 	lowerX = thumbRect.x + (thumbRect.width / 2);
                final int 	upperX = upperThumbRect.x + (upperThumbRect.width / 2);
                final int	cy = trackBounds.height / 2;

                g2d.drawLine(lowerX, trackBounds.y + cy, upperX, trackBounds.y + cy);
            } else {
                final int 	lowerY = thumbRect.x + (thumbRect.width / 2);
                final int 	upperY = upperThumbRect.x + (upperThumbRect.width / 2);
                final int 	cx = trackBounds.width / 2;

                g.drawLine(trackBounds.x + cx, lowerY, trackBounds.x + cx, upperY);
            }
            g2d.setColor(oldColor);
            g2d.setStroke(oldStroke);
        }
        
        @Override
        public void paintThumb(final Graphics g) {
            final Rectangle 	clipRect = g.getClipBounds();
            
            if (upperThumbSelected) {
                if (clipRect.intersects(thumbRect)) {
                	paintThumb((Graphics2D)g, thumbRect, Color.BLACK, ((JRangeSlider)slider).getLowerThumbColor());
                }
                if (clipRect.intersects(upperThumbRect)) {
                	paintThumb((Graphics2D)g, upperThumbRect, Color.BLACK, ((JRangeSlider)slider).getUpperThumbColor());
                }
                
            } else {
                if (clipRect.intersects(upperThumbRect)) {
                	paintThumb((Graphics2D)g, upperThumbRect, Color.BLACK, ((JRangeSlider)slider).getUpperThumbColor());
                }
                if (clipRect.intersects(thumbRect)) {
                	paintThumb((Graphics2D)g, thumbRect, Color.BLACK, ((JRangeSlider)slider).getLowerThumbColor());
                }
            }
        }

        @Override
        public void scrollByBlock(final int direction) {
            synchronized (slider) {
                final int 	blockIncrement = Math.max(1,(slider.getMaximum() - slider.getMinimum()) / 10);
                final int	delta = blockIncrement * (direction > 0 ? POSITIVE_SCROLL : NEGATIVE_SCROLL);
                
                if (upperThumbSelected) {
                    int oldValue = ((JRangeSlider) slider).getUpperValue();
                    
                    ((JRangeSlider) slider).setUpperValue(oldValue + delta);
                } else {
                    int oldValue = ((JRangeSlider) slider).getLowerValue();
                    
                    ((JRangeSlider) slider).setLowerValue(oldValue + delta);
                }
            }
        }
        
        @Override
        public void scrollByUnit(final int direction) {
            synchronized (slider) {
                final int 	delta = direction > 0 ? POSITIVE_SCROLL : NEGATIVE_SCROLL;
                
                if (upperThumbSelected) {
                    int oldValue = ((JRangeSlider)slider).getUpperValue();
                    
                    ((JRangeSlider)slider).setUpperValue(oldValue + delta);
                } else {
                    int oldValue = ((JRangeSlider)slider).getLowerValue();
                    
                    ((JRangeSlider)slider).setLowerValue(oldValue + delta);
                }
            }       
        }
        
        private void paintThumb(final Graphics2D g2d, final Rectangle thumbRect, final Color drawColor, final Color fillColor) {
            final AffineTransform	oldAt = g2d.getTransform();
            final Color				oldColor = g2d.getColor();
        	final int 				w = thumbRect.width;
            final int 				h = thumbRect.height;      
            final Shape 			thumbShape = createThumbShape(w - 1, h - 1);

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.translate(thumbRect.x, thumbRect.y);

            g2d.setColor(fillColor);
            g2d.fill(thumbShape);
            g2d.setColor(drawColor);
            g2d.draw(thumbShape);

            g2d.setColor(oldColor);
            g2d.setTransform(oldAt);
        }
        
        private Shape createThumbShape(final int width, final int height) {
        	if (width > height) {
        		final GeneralPath	gp = new GeneralPath();
        		final int			h2 = height/2;
        		
        		gp.moveTo(0, 0);
        		gp.lineTo(width-h2, 0);
        		gp.lineTo(width, h2);
        		gp.lineTo(width-h2, height);
        		gp.lineTo(0, height);
        		gp.closePath();
        		return gp;
        	}
        	else {
        		final GeneralPath	gp = new GeneralPath();
        		final int			w2 = width/2;
        		
        		gp.moveTo(0, 0);
        		gp.lineTo(0, height-w2);
        		gp.lineTo(w2, height);
        		gp.lineTo(width, height-w2);
        		gp.lineTo(width, 0);
        		gp.closePath();
        		return gp;
        	}
        }
        
        private void setUpperThumbLocation(int x, int y) {
            final Rectangle 	upperUnionRect = new Rectangle();
            
            upperUnionRect.setBounds(upperThumbRect);
            upperThumbRect.setLocation(x, y);
            SwingUtilities.computeUnion(upperThumbRect.x, upperThumbRect.y, upperThumbRect.width, upperThumbRect.height, upperUnionRect);
            slider.repaint(upperUnionRect.x, upperUnionRect.y, upperUnionRect.width, upperUnionRect.height);
        }
        
        private class RangeTrackListener extends TrackListener {
            @Override
            public void mousePressed(MouseEvent e) {
                if (slider.isEnabled()) {
	                boolean lowerPressed = false;
	                boolean upperPressed = false;
	                
	                currentMouseX = e.getX();
	                currentMouseY = e.getY();
	                if (slider.isRequestFocusEnabled()) {
	                    slider.requestFocusInWindow();
	                }
	                
	                if (upperThumbSelected) {
	                    if (upperThumbRect.contains(currentMouseX, currentMouseY)) {
	                        upperPressed = true;
	                    } else if (thumbRect.contains(currentMouseX, currentMouseY)) {
	                        lowerPressed = true;
	                    }
	                } else {
	                    if (thumbRect.contains(currentMouseX, currentMouseY)) {
	                        lowerPressed = true;
	                    } else if (upperThumbRect.contains(currentMouseX, currentMouseY)) {
	                        upperPressed = true;
	                    }
	                }
	
	                if (lowerPressed) {
	                    switch (slider.getOrientation()) {
		                    case JSlider.VERTICAL	:
		                        offset = currentMouseY - thumbRect.y;
		                        break;
		                    case JSlider.HORIZONTAL	:
		                        offset = currentMouseX - thumbRect.x;
		                        break;
	                    }
	                    upperThumbSelected = false;
	                    lowerDragging = true;
	                }
	                else if (upperPressed) {
	                    switch (slider.getOrientation()) {
		                    case JSlider.VERTICAL	:
		                        offset = currentMouseY - upperThumbRect.y;
		                        break;
		                    case JSlider.HORIZONTAL	:
		                        offset = currentMouseX - upperThumbRect.x;
		                        break;
	                    }
	                    upperThumbSelected = true;
	                    upperDragging = true;
	                }
	                else {
		                lowerDragging = false;
		                upperDragging = false;
	                }
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                lowerDragging = false;
                upperDragging = false;
                slider.setValueIsAdjusting(false);
                super.mouseReleased(e);
            }
            
            @Override
            public void mouseDragged(MouseEvent e) {
                if (slider.isEnabled()) {
		            currentMouseX = e.getX();
		            currentMouseY = e.getY();
		
		            if (lowerDragging) {
		                slider.setValueIsAdjusting(true);
		                moveLowerThumb();
		                
		            } else if (upperDragging) {
		                slider.setValueIsAdjusting(true);
		                moveUpperThumb();
		            }
                }
            }
            
            @Override
            public boolean shouldScroll(int direction) {
                return false;
            }

            private void moveLowerThumb() {
                int thumbMiddle = 0;
                
                switch (slider.getOrientation()) {
	                case JSlider.VERTICAL	:      
	                    int halfThumbHeight = thumbRect.height / 2;
	                    int thumbTop = currentMouseY - offset;
	                    int trackTop = trackRect.y;
	                    int trackBottom = trackRect.y + (trackRect.height - 1);
	                    int vMax = yPositionForValue(((JRangeSlider)slider).getUpperValue());
	
	                    // Apply bounds to thumb position.
	                    if (drawInverted()) {
	                        trackBottom = vMax;
	                    } else {
	                        trackTop = vMax;
	                    }
	                    thumbTop = Math.max(thumbTop, trackTop - halfThumbHeight);
	                    thumbTop = Math.min(thumbTop, trackBottom - halfThumbHeight);
	
	                    setThumbLocation(thumbRect.x, thumbTop);
	
	                    // Update slider value.
	                    thumbMiddle = thumbTop + halfThumbHeight;
	                    slider.setValue(valueForYPosition(thumbMiddle));
	                    break;
	                    
	                case JSlider.HORIZONTAL	:
	                    int halfThumbWidth = thumbRect.width / 2;
	                    int thumbLeft = currentMouseX - offset;
	                    int trackLeft = trackRect.x;
	                    int trackRight = trackRect.x + (trackRect.width - 1);
	                    int hMax = xPositionForValue(((JRangeSlider)slider).getUpperValue());
	
	                    // Apply bounds to thumb position.
	                    if (drawInverted()) {
	                        trackLeft = hMax;
	                    } else {
	                        trackRight = hMax;
	                    }
	                    thumbLeft = Math.max(thumbLeft, trackLeft - halfThumbWidth);
	                    thumbLeft = Math.min(thumbLeft, trackRight - halfThumbWidth);
	
	                    setThumbLocation(thumbLeft, thumbRect.y);
	
	                    // Update slider value.
	                    thumbMiddle = thumbLeft + halfThumbWidth;
	                    slider.setValue(valueForXPosition(thumbMiddle));
	                    break;
                }
            }

            /**
             * Moves the location of the upper thumb, and sets its corresponding 
             * value in the slider.
             */
            private void moveUpperThumb() {
                int thumbMiddle = 0;
                
                switch (slider.getOrientation()) {
                
                case JSlider.VERTICAL:      
                    int halfThumbHeight = thumbRect.height / 2;
                    int thumbTop = currentMouseY - offset;
                    int trackTop = trackRect.y;
                    int trackBottom = trackRect.y + (trackRect.height - 1);
                    int vMin = yPositionForValue(slider.getValue());

                    // Apply bounds to thumb position.
                    if (drawInverted()) {
                        trackTop = vMin;
                    } else {
                        trackBottom = vMin;
                    }
                    thumbTop = Math.max(thumbTop, trackTop - halfThumbHeight);
                    thumbTop = Math.min(thumbTop, trackBottom - halfThumbHeight);

                    setUpperThumbLocation(thumbRect.x, thumbTop);

                    // Update slider extent.
                    thumbMiddle = thumbTop + halfThumbHeight;
                    slider.setExtent(valueForYPosition(thumbMiddle) - slider.getValue());
                    break;
                    
                case JSlider.HORIZONTAL:
                    int halfThumbWidth = thumbRect.width / 2;
                    int thumbLeft = currentMouseX - offset;
                    int trackLeft = trackRect.x;
                    int trackRight = trackRect.x + (trackRect.width - 1);
                    int hMin = xPositionForValue(slider.getValue());

                    // Apply bounds to thumb position.
                    if (drawInverted()) {
                        trackRight = hMin;
                    } else {
                        trackLeft = hMin;
                    }
                    thumbLeft = Math.max(thumbLeft, trackLeft - halfThumbWidth);
                    thumbLeft = Math.min(thumbLeft, trackRight - halfThumbWidth);

                    setUpperThumbLocation(thumbLeft, thumbRect.y);
                    
                    // Update slider extent.
                    thumbMiddle = thumbLeft + halfThumbWidth;
                    slider.setExtent(valueForXPosition(thumbMiddle) - slider.getValue());
                    break;
                    
                default:
                    return;
                }
            }
        }
    }

}
