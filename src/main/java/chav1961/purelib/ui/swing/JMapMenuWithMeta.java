package chav1961.purelib.ui.swing;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.net.URI;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;


import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.ToolTipManager;

import chav1961.purelib.basic.CSSUtils;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor.MonitorEvent;
import chav1961.purelib.ui.swing.useful.svg.SVGUtils;

public class JMapMenuWithMeta extends JComponent implements NodeMetadataOwner, LocaleChangeListener, JComponentInterface {
	private static final long serialVersionUID = 7000659520053155494L;
	
	private static final String			SIZE_PROP = "size";
	private static final String			AUTO_SHAPE_PROP = "autoShape";
	private static final String			DEFAULT_AUTO_SHAPE_PROP = "true";
	private static final String			SELECTED_COLOR_PROP = "selectedColor";
	private static final Color			DEFAULT_SELECTED_COLOR = Color.DARK_GRAY;
	private static final String			FOCUSED_COLOR_PROP = "focusedColor";
	private static final Color			DEFAULT_FOCUSED_COLOR = Color.BLACK;
	private static final String			TRANSFORM_PROP_SUFFIX = ".transform";

	private final ContentNodeMetadata	metadata;
	private final Image					icon;
	private final double				menuWidth;
	private final double				menuHeight;
	private final MetadataShapeAndTransform[]	areas;
	private final ImageObserver			observer = new ImageObserver() {
											@Override
											public boolean imageUpdate(final Image img, int infoflags, int x, int y, int width, int height) {
												return true;
											}
										};
	private final Color					selectedColor, focusedColor;
	private final Map<URI,Image>		icons = new HashMap<>();
	
	private boolean 					invalid = false;
	private Object						value;
	private volatile MetadataShapeAndTransform	inside = null;
	private volatile MetadataShapeAndTransform	selected = null;

	public JMapMenuWithMeta(final ContentNodeMetadata metadata, final JComponentMonitor monitor, final Properties areaDescriptors) throws LocalizationException, SyntaxException {
		if (metadata == null) {
			throw new NullPointerException("Metadata can't be null"); 
		}
		else if (monitor == null) {
			throw new NullPointerException("Monitor can't be null"); 
		}
		else if (areaDescriptors == null) {
			throw new NullPointerException("Menu areas can't be null"); 
		}
		else if (metadata.getIcon() == null) {
			throw new IllegalArgumentException("Metadata doesn't contain mandatory icon reference!"); 
		}
		else {
			final double[]	value = new double[1];
			int				from = 0;
			
			this.metadata = metadata;
			
			if (areaDescriptors.containsKey(SIZE_PROP)) {
				final char[]	content = CharUtils.terminateAndConvert2CharArray(areaDescriptors.getProperty(SIZE_PROP), '\n');
				
				try{from = CharUtils.parseDouble(content, CharUtils.skipBlank(content, from, true), value, true);
					menuWidth = value[0];
					if (menuWidth == 0) {
						throw new SyntaxException(0, from, "Illegal format of ["+SIZE_PROP+"] property: menu width can't be 0");
					}
					
					from = CharUtils.skipBlank(content, from, true);
					if (content[from] == 'x' || content[from] == 'X') {
						from = CharUtils.parseDouble(content, CharUtils.skipBlank(content, from + 1, true), value, true);
						menuHeight = value[0];
						if (menuHeight == 0) {
							throw new SyntaxException(0, from, "Illegal format of ["+SIZE_PROP+"] property: menu height can't be 0");
						}
						else {
							from = CharUtils.skipBlank(content, from, true);
							if (content[from] != '\n') {
								throw new SyntaxException(0, from, "Illegal format of ["+SIZE_PROP+"] property: unparsed tail after second number");
							}
						}
					}
					else {
						throw new SyntaxException(0, from, "Illegal format of ["+SIZE_PROP+"] property ('x' is missing)");
					}
				} catch (IllegalArgumentException exc) {
					throw new SyntaxException(0, from, "Illegal format of ["+SIZE_PROP+"] property (illegal number)");
				}
			}
			else {
				menuWidth = 1.0;
				menuHeight = 1.0;
			}

			try{this.icon = ImageIO.read(metadata.getIcon().toURL());
			} catch (IOException e) {
				throw new SyntaxException(0, 0, "I/O error loading image ["+metadata.getIcon()+"]: "+e.getLocalizedMessage());
			}
			
			if (areaDescriptors.containsKey(SELECTED_COLOR_PROP)) {
				this.selectedColor = PureLibSettings.colorByName(areaDescriptors.getProperty(SELECTED_COLOR_PROP), DEFAULT_SELECTED_COLOR);
			}
			else {
				this.selectedColor = DEFAULT_SELECTED_COLOR;
			}
			if (areaDescriptors.containsKey(FOCUSED_COLOR_PROP)) {
				this.focusedColor = PureLibSettings.colorByName(areaDescriptors.getProperty(FOCUSED_COLOR_PROP), DEFAULT_FOCUSED_COLOR);
			}
			else {
				this.focusedColor = DEFAULT_FOCUSED_COLOR;
			}
			
			final List<MetadataShapeAndTransform>	temp = new ArrayList<>();
			final boolean					autoShape = Boolean.valueOf(areaDescriptors.getProperty(AUTO_SHAPE_PROP, DEFAULT_AUTO_SHAPE_PROP)); 
					
			for (ContentNodeMetadata item : metadata) {
				final AffineTransform	at;
				if (!areaDescriptors.containsKey(item.getName()+TRANSFORM_PROP_SUFFIX)) {
					at = CSSUtils.asTransform(areaDescriptors.getProperty(item.getName()+TRANSFORM_PROP_SUFFIX));
				}
				else {
					at = new AffineTransform();
				}
				if (!areaDescriptors.containsKey(item.getName())) {
					if (autoShape && item.getIcon() != null) {
						final Image		image = extractImage(item.getIcon());
						
						temp.add(new MetadataShapeAndTransform(item, SwingUtils.buildContour((BufferedImage)image),at));
					}
					else {
						throw new SyntaxException(0, 0, "Mandatory name ["+item.getName()+"] is missing in the areas");
					}
				}
				else {
					temp.add(new MetadataShapeAndTransform(item, SVGUtils.extractCommands(areaDescriptors.getProperty(item.getName())),at));
				}
			}
			if (temp.isEmpty()) {
				throw new SyntaxException(0, 0, "No any areas in the properties");
			}
			else {
				this.areas = temp.toArray(new MetadataShapeAndTransform[temp.size()]);
			}
			
			addMouseListener(new MouseListener() {
				@Override public void mouseReleased(MouseEvent e) {}
				@Override public void mousePressed(MouseEvent e) {}
				@Override public void mouseExited(MouseEvent e) {}
				@Override public void mouseEntered(MouseEvent e) {}
				
				@Override 
				public void mouseClicked(final MouseEvent e) {
					final MetadataShapeAndTransform	temp = inside;
					
					if (e.getButton() == MouseEvent.BUTTON1 && e.getModifiersEx() == 0) {
						if (temp != null) {
							selected = inside;
							if (e.getClickCount() > 1) {
								try{monitor.process(MonitorEvent.Action, temp.meta, JMapMenuWithMeta.this);
								} catch (ContentException exc) {
								}
							}
						}
						else {
							selected = null;
						}
						repaint();
					}
				}
			});
			addMouseMotionListener(new MouseMotionListener() {
				@Override public void mouseDragged(MouseEvent e) {}
				
				@Override
				public void mouseMoved(final MouseEvent e) {
					final ContentNodeMetadata	meta = getNodeMetadata(e.getX(), e.getY());
					
					if (meta != null) {
						if (inside == null) {
							processFocusGained(meta);
						}
						else if (inside.meta != meta) {
							processFocusLost();
							processFocusGained(meta);
						}
					}
					else {
						if (inside != null) {
							processFocusLost();
						}
					}
				}
				
				private void processFocusGained(final ContentNodeMetadata meta) {
					for (MetadataShapeAndTransform item : JMapMenuWithMeta.this.areas) {
						if (item.meta == meta) {
							inside = item;
							
							repaint();
							
							try{monitor.process(MonitorEvent.FocusGained, meta, JMapMenuWithMeta.this, inside.shape);
							} catch (ContentException exc) {
							}
							break;
						}
					}
				}
				
				private void processFocusLost() {
					final MetadataShapeAndTransform	mas = inside; 
					
					repaint();
					
					try{monitor.process(MonitorEvent.FocusLost, mas.meta, JMapMenuWithMeta.this, mas.shape);
					} catch (ContentException exc) {
					}
					inside = null;
				}
			});

			setFocusable(true);
			ToolTipManager.sharedInstance().registerComponent(this);
			fillLocalizedStrings();
		}
	}	
	
	@Override
	public String getRawDataFromComponent() {
		return value == null ? "null" : value.toString();
	}

	@Override
	public Object getValueFromComponent() {
		return value;
	}

	@Override
	public Object getChangedValueFromComponent() throws SyntaxException {
		return value;
	}

	@Override
	public void assignValueToComponent(final Object value) throws ContentException {
		this.value = value;
	}

	@Override
	public Class<?> getValueType() {
		return value == null ? Object.class : value.getClass();
	}

	@Override
	public String standardValidation(Object value) {
		return null;
	}

	@Override
	public void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}

	@Override
	public boolean isInvalid() {
		return invalid;
	}

	@Override
	public void localeChanged(Locale oldLocale, Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
	}

	@Override
	public ContentNodeMetadata getNodeMetadata() {
		return metadata;
	}
	
	@Override
	public ContentNodeMetadata getNodeMetadata(final int x, final int y) {
		final double	kx = menuWidth/getWidth(), ky = menuHeight/getHeight();
		final double	scaledX = x * kx, scaledY = y * ky;
		
		for (MetadataShapeAndTransform item : areas) {
			if (item.shape.contains(scaledX, scaledY)) {
				return item.meta;
			}
		}
		return null;
	}

	@Override
	public String getToolTipText(final MouseEvent event) {
		final Point		p = event.getPoint();
		final double	kx = menuWidth/getWidth(), ky = menuHeight/getHeight();
		final double	scaledX = p.x * kx, scaledY = p.y * ky;
		
		for (MetadataShapeAndTransform item : areas) {
			if (item.shape.contains(scaledX, scaledY)) {
				return extractTooltip(item.meta);
			}
		}
		return super.getToolTipText(event);
	}
	
	@Override
	public String getToolTipText() {
		final String	tooltipId = extractTooltip(getNodeMetadata());
		
		if (tooltipId == null || tooltipId.isEmpty()) {
			return super.getToolTipText();
		}
		else {
			return tooltipId;
		}
	}
	
	@Override
	protected void paintComponent(final Graphics g) {
		final Graphics2D		g2d = (Graphics2D)g;
		final AffineTransform	oldAt = g2d.getTransform();
		final AffineTransform	newAt = new AffineTransform();
		final AffineTransform	imageAt = new AffineTransform();
		
		imageAt.translate(0,0);
		imageAt.scale(1.0f*getWidth()/icon.getWidth(observer), 1.0f*getHeight()/icon.getHeight(observer));
		g2d.drawImage(icon, imageAt, observer);
		
		newAt.translate(0,0);
		newAt.scale(getWidth()/menuWidth, getHeight()/menuHeight);
		g2d.setTransform(newAt);
	
		if (inside != null) {
			final Stroke		oldStroke = g2d.getStroke();
			final Color			oldColor = g2d.getColor();
			
			g2d.setStroke(new BasicStroke((float) (0.005*Math.min(menuWidth, menuHeight))));
			for (MetadataShapeAndTransform item : areas) {
				final boolean 	isFocused = item == inside;
				final boolean 	isSelected = item == selected;
				
				paintItem(g2d, item.meta, imageAt, item.shape, isFocused, isSelected);
			}
			g2d.setStroke(oldStroke);
			g2d.setColor(oldColor);
		}
		g2d.setTransform(oldAt);
	}
	
	protected void paintItem(final Graphics2D g2d, final ContentNodeMetadata metadata, final AffineTransform at, Shape shape, final boolean focused, final boolean selected) {
		final Color	oldColor = g2d.getColor();
		
		if (metadata.getIcon() != null) {
			final Image	image = extractImage(metadata.getIcon());
			
			if (image !=  null) {
				g2d.drawImage(icon, at, observer);
			}
		}
		if (selected && !focused) {
			g2d.setColor(selectedColor);
			g2d.draw(shape);
		}
		else if (focused) {
			g2d.setColor(focusedColor);
			g2d.draw(shape);
		}
		g2d.setColor(oldColor);
	}
	
	private String extractTooltip(final ContentNodeMetadata meta) {
		final String	tooltipId = meta.getTooltipId();
		
		if (tooltipId == null || tooltipId.isEmpty()) {
			return null;
		}
		else {
			try{
				return LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()).getValue(tooltipId);
			} catch (LocalizationException e) {
				return tooltipId;
			}
		}
	}
	
	private Image extractImage(final URI imageUri) {
		if (!icons.containsKey(imageUri)) {
			try{icons.put(imageUri, ImageIO.read(metadata.getIcon().toURL()));
			} catch (IOException e) {
				try{icons.put(imageUri, ImageIO.read(this.getClass().getResource("notfound.png")));
				} catch (IOException e1) {
				}
			}
		}
		return icons.get(imageUri); 
	}
	
	private void fillLocalizedStrings() {
		
	}
	
	private static class MetadataShapeAndTransform {
		private final ContentNodeMetadata	meta;
		private final Shape					shape;
		private final AffineTransform		transform;
		
		private MetadataShapeAndTransform(final ContentNodeMetadata meta, final Shape shape, final AffineTransform transform) {
			this.meta = meta;
			this.shape = shape;
			this.transform = transform;
		}

		@Override
		public String toString() {
			return "MetadataAndShape [meta=" + meta + ", shape=" + shape + "]";
		}
	}
}
