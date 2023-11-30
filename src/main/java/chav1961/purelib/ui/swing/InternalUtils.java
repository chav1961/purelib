package chav1961.purelib.ui.swing;


import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.TimerTask;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.ToolTipManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.text.InternationalFormatter;
import javax.swing.text.JTextComponent;
import javax.swing.text.NumberFormatter;

import chav1961.purelib.basic.ColorUtils;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.swing.useful.JFileSelectionDialog;

class InternalUtils {
	static final String			VALIDATION_NULL_VALUE = "purelib.ui.validation.nullvalue";
	static final String			VALIDATION_MANDATORY = "purelib.ui.validation.mandatory";
	static final String			VALIDATION_NEITHER_TRUE_NOR_FALSE = "purelib.ui.validation.neithertruenorfalse";
	static final String			VALIDATION_ILLEGAL_TYPE = "purelib.ui.validation.illegaltype";
	static final String			VALIDATION_ILLEGAL_VALUE = "purelib.ui.validation.illegalvalue";
	static final String			VALIDATION_ILLEGAL_VALUE_SIGN = "purelib.ui.validation.illegalvaluesign";
	static final String 		CHOOSE_COLOR_TITLE = "JColorPicketWithMeta.chooser.title";

	static final BufferedImage	BUFFERED_IMAGE = new BufferedImage(100,100,BufferedImage.TYPE_INT_RGB);

	static enum IconType {
		ORDINAL, TOGGLED, PRESSED
	}
	
	interface ComponentListenerCallback {
		void process();
	}
	
	static boolean checkClassTypes(final Class<?> toTest, final Class<?>... available) {
		for (Class<?> item : available) {
			if (item.isAssignableFrom(toTest)) {
				return true;
			}
		}
		return false;
	}
	
	static boolean checkNullAvailable(final ContentNodeMetadata metadata) {
		return metadata.getFormatAssociated() != null && metadata.getFormatAssociated().isNullSupported(); 
	}

	static boolean isContentMandatory(final ContentNodeMetadata metadata) {
		return metadata.getFormatAssociated() != null && metadata.getFormatAssociated().isMandatory(); 
	}

	static void prepareMandatoryColor(final JComponent component) {
		component.setBackground(ColorUtils.defaultColorScheme().MANDATORY_BACKGROUND);
		component.setForeground(ColorUtils.defaultColorScheme().MANDATORY_FOREGROUND);
		if (component instanceof JTextComponent) {
			((JTextComponent)component).setSelectedTextColor(ColorUtils.defaultColorScheme().MANDATORY_SELECTION_FOREGROUND);
			((JTextComponent)component).setDisabledTextColor(ColorUtils.defaultColorScheme().MANDATORY_FOREGROUND.brighter());
			((JTextComponent)component).setSelectionColor(ColorUtils.defaultColorScheme().MANDATORY_SELECTION_BACKGROUND);
		}
	}
	
	static void prepareOptionalColor(final JComponent component) {
		component.setBackground(ColorUtils.defaultColorScheme().OPTIONAL_BACKGROUND);
		component.setForeground(ColorUtils.defaultColorScheme().OPTIONAL_FOREGROUND);
		if (component instanceof JTextComponent) {
			((JTextComponent)component).setSelectedTextColor(ColorUtils.defaultColorScheme().OPTIONAL_SELECTION_FOREGROUND);
			((JTextComponent)component).setDisabledTextColor(ColorUtils.defaultColorScheme().OPTIONAL_FOREGROUND.brighter());
			((JTextComponent)component).setSelectionColor(ColorUtils.defaultColorScheme().OPTIONAL_SELECTION_BACKGROUND);
		}
	}

	static void addComponentListener(final JComponent component, final ComponentListenerCallback callback) {
		component.addComponentListener(new ComponentListener() {
			private boolean	loaded = false;
			
			@Override 
			public void componentResized(ComponentEvent e) {
				if (!loaded) {
					callback.process();
					loaded = true;
				}
			}
			
			@Override 
			public void componentMoved(ComponentEvent e) {
				if (!loaded) {
					callback.process();
					loaded = true;
				}
			}
			
			@Override 
			public void componentHidden(ComponentEvent e) {
				if (!loaded) {
					callback.process();
					loaded = true;
				}
			}
			
			@Override
			public void componentShown(ComponentEvent e) {
				if (!loaded) {
					callback.process();
					loaded = true;
				}
			}				
		});
	}
	
	static void setFieldColor(final JTextComponent component, final FieldFormat format, final int signum) {
		if (format.isHighlighted(signum)) {
			if (signum < 0) {
				component.setForeground(ColorUtils.defaultColorScheme().NEGATIVEMARK_FOREGROUND);
			}
			else if (signum > 0) {
				component.setForeground(ColorUtils.defaultColorScheme().POSITIVEMARK_FOREGROUND);
			}
			else {
				component.setForeground(ColorUtils.defaultColorScheme().ZEROMARK_FOREGROUND);
			}
		}
	}
	
	static InternationalFormatter prepareNumberFormatter(final FieldFormat format, final Locale currentLocale) {
		final InternationalFormatter	formatter;
		
		if (format.getFormatMask() != null) {
			formatter = new NumberFormatter(new DecimalFormat(format.getFormatMask(),new DecimalFormatSymbols(currentLocale)));
		}
		else {
			final int 			len = format.getLength() == 0 ? 15 : format.getLength();
			final int 			frac = format.getPrecision();
			final NumberFormat	fmt = NumberFormat.getNumberInstance(currentLocale);
			
			fmt.setGroupingUsed(false);
			fmt.setMinimumIntegerDigits(1);
			fmt.setMaximumIntegerDigits(len);
			if (frac > 0) {
				fmt.setMinimumFractionDigits(frac);
				fmt.setMaximumFractionDigits(frac);
			}
			
			formatter = new NumberFormatter(fmt);
			formatter.setAllowsInvalid(false);
		}
		return formatter;
	}
	
	static String buildStandardValidationMessage(final ContentNodeMetadata metadata, final String messageId, final Object... parameters) {
		if (parameters.length == 0) {
			try{return String.format(PureLibSettings.PURELIB_LOCALIZER.getValue(messageId),
						LocalizerFactory.getLocalizer(metadata.getLocalizerAssociated()).getValue(metadata.getLabelId()),
						parameters[0]);
			} catch (LocalizationException e) {
				return PureLibSettings.PURELIB_LOCALIZER.getValue(messageId).replace("%", "%%") + ": (" + metadata.getLabelId() + ")";
			}
		}
		else {
			final List<Object>	parms = new ArrayList<>();
			
			try{parms.add(LocalizerFactory.getLocalizer(metadata.getLocalizerAssociated()).getValue(metadata.getLabelId()));
				parms.addAll(Arrays.asList(parameters));
				
				return String.format(PureLibSettings.PURELIB_LOCALIZER.getValue(messageId),parms.toArray());
			} catch (LocalizationException | IllegalArgumentException | NullPointerException e) {
				return PureLibSettings.PURELIB_LOCALIZER.getValue(messageId).replace("%", "%%") + ": (" + metadata.getLabelId() + ")";
			}
		}
	}
	
	static <T extends JComponent> void registerAdvancedTooptip(final T instance) {
		if ("advanced".equals(PureLibSettings.instance().getProperty(PureLibSettings.SWING_TOOLTIP_MODE))) {
			instance.addMouseListener(new MouseListener() {
				@Override public void mouseReleased(MouseEvent e) {}
				@Override public void mousePressed(MouseEvent e) {}
				@Override public void mouseExited(MouseEvent e) {}
				@Override public void mouseClicked(MouseEvent e) {}
				
				@Override
				public void mouseEntered(MouseEvent e) {
					internalShowTooltip(instance,e);
				}
			});
		}
	}

	static Dimension calculateFontCellSize(final Component component) {
		final Graphics2D	g2d = BUFFERED_IMAGE.createGraphics();
		final Rectangle2D	charSize = component.getFontMetrics(component.getFont()).getMaxCharBounds(g2d);
		
		return new Dimension((int)charSize.getWidth(), (int)charSize.getHeight());
	}
	
	private static <T extends JComponent> void internalShowTooltip(final T instance, final MouseEvent e) {
		final ToolTipManager	mgr = ToolTipManager.sharedInstance();
		final String			tt = instance.getToolTipText();
		final MouseEvent		me = new MouseEvent(instance, 0, 0, 0, e.getX(), e.getY(), 0, 0, 0, false, 0);
		final int				initialDelay = mgr.getInitialDelay();
		final int				dismissDelay = mgr.getDismissDelay();
		
		if (tt == null) {
			mgr.registerComponent(instance);
		}
		mgr.setInitialDelay(PureLibSettings.instance().getProperty(PureLibSettings.SWING_TOOLTIP_MODE,int.class,"1000"));
		mgr.setDismissDelay(PureLibSettings.instance().getProperty(PureLibSettings.SWING_TOOLTIP_MODE,int.class,"5000"));
		instance.setToolTipText(buildDebuggingTooltip(instance,tt));
		mgr.mouseMoved(me);
		
		PureLibSettings.COMMON_MAINTENANCE_TIMER.schedule(new TimerTask(){
			@Override
			public void run() {
				mgr.setInitialDelay(initialDelay);
				mgr.setDismissDelay(dismissDelay);
				if (tt == null) {
					mgr.unregisterComponent(instance);
				}
				else {
					instance.setToolTipText(tt);
				}
			}
		}, mgr.getDismissDelay());
	}
	
	private static <T extends JComponent> String buildDebuggingTooltip(final T instance, final String tooltip) {
		if (instance == null) {
			return "";
		}
		else {
			@SuppressWarnings("unchecked")
			final Class<T>		clazz = (Class<T>) instance.getClass();
			final StringBuilder	sb = new StringBuilder();
			
			sb.append("<html><body>");
			sb.append("<h3>Control description:</h3>");
			sb.append("<p><b>Class :</b> ").append(clazz.getCanonicalName()).append("</p>");
			if (instance.getName() != null && !instance.getName().isEmpty()) {
				sb.append("<p><b>UI name :</b> ").append(instance.getName()).append("</p>");
			}
			if (clazz.getEnclosingClass() != null) {
				sb.append("<p><b>Owned by:</b> ").append(clazz.getEnclosingClass().getCanonicalName()).append("</p>");
			}
			if (instance.getParent() != null) {
				sb.append("<p><b>Container class:</b> ").append(instance.getParent().getClass().getCanonicalName()).append("</p>");
			}
			if (instance instanceof NodeMetadataOwner) {
				final ContentNodeMetadata	meta = ((NodeMetadataOwner)instance).getNodeMetadata();
				
				if (meta != null) {
					sb.append("<p><b>Model item :</b> ").append(meta.getName()).append("</p>");
					if (meta.getApplicationPath() != null) {
						sb.append("<p><b>Model App URI:</b> ").append(meta.getApplicationPath()).append("</p>");
					}
				}
			}
			if (tooltip != null) {
				sb.append("<ht>").append("<p>").append(tooltip).append("</p>");
			}
			sb.append("</body></html>");
			return sb.toString();
		}
	}

	static void cropButtonByIcon(final JButton button) {
		final Icon	icon = button.getIcon();
		
		if (icon != null) {
			button.setPreferredSize(new Dimension(icon.getIconWidth() + 2, icon.getIconHeight() + 2));
		}
	}

	static void cropButtonByIcon(final JToggleButton button) {
		final Icon	icon = button.getIcon();
		
		if (icon != null) {
			button.setPreferredSize(new Dimension(icon.getIconWidth() + 2, icon.getIconHeight() + 2));
		}
	}
	
	static void cropToolbarButtonsByIcons(final JToolBar toolbar) {
		final int[]	widthAndHeight = new int[] {0, 0};
		
		SwingUtils.walkDown(toolbar, (mode, node)->{
			if (mode == NodeEnterMode.ENTER && (node instanceof AbstractButton)) {
				final Icon	icon = ((AbstractButton)node).getIcon();
				
				if (icon != null) {
					widthAndHeight[0] = Math.max(widthAndHeight[0], icon.getIconWidth());
					widthAndHeight[1] = Math.max(widthAndHeight[1], icon.getIconHeight());
				}
			}
			return ContinueMode.CONTINUE;
		});

		if (widthAndHeight[0] + widthAndHeight[1] > 0) {
			final Dimension	newSize = new Dimension(widthAndHeight[0] + 2, widthAndHeight[1] + 2);
			
			SwingUtils.walkDown(toolbar, (mode, node)->{
				if (mode == NodeEnterMode.ENTER && (node instanceof AbstractButton)) {
					node.setPreferredSize(newSize);
					node.setFocusable(false);
				}
				return ContinueMode.CONTINUE;
			});
		}
	}
	
	static Color chooseColor(final Component owner, final Localizer localizer, final Color initialColor, final boolean isForeground) throws HeadlessException, LocalizationException {
		return Utils.nvl(JColorChooser.showDialog(owner,PureLibSettings.PURELIB_LOCALIZER.getValue(CHOOSE_COLOR_TITLE),initialColor),initialColor);
	}
	
	static File chooseFile(final Component owner, final Localizer localizer, final File initialFile, final int options) throws HeadlessException, LocalizationException {
		final JFileChooser	chooser = new JFileChooser();
		final File			currentPath = initialFile;
		
		if (currentPath.exists()) {
			if (currentPath.isFile()) {
				chooser.setCurrentDirectory(currentPath.getParentFile());
				chooser.setSelectedFile(currentPath);
			}
			else {
				chooser.setCurrentDirectory(currentPath);
			}
		}
		if ((options & JFileSelectionDialog.OPTIONS_CAN_SELECT_FILE) != 0 && (options & JFileSelectionDialog.OPTIONS_CAN_SELECT_DIR) != 0) {
			chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		}
		else if ((options & JFileSelectionDialog.OPTIONS_CAN_SELECT_FILE) != 0) {
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		}
		else if ((options & JFileSelectionDialog.OPTIONS_CAN_SELECT_DIR) != 0) {
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		}
		if ((options & JFileSelectionDialog.OPTIONS_FOR_SAVE) != 0) {
			if (chooser.showSaveDialog(owner) == JFileChooser.APPROVE_OPTION) {
				final File	sel = chooser.getSelectedFile();  
				
				return sel != null ? sel.getAbsoluteFile() : null;
			}
			else {
				return null;
			}
		}
		else {
			if (chooser.showOpenDialog(owner) == JFileChooser.APPROVE_OPTION) {
				final File	sel = chooser.getSelectedFile();  
				
				return sel != null ? sel.getAbsoluteFile() : null;
			}
			else {
				return null;
			}
		}
	}
	
	static FileSystemInterface chooseFileSystem(final Component owner, final Localizer localizer, final FileSystemInterface initialFS, final int options) throws HeadlessException, LocalizationException {
		try{for(String item : JFileSelectionDialog.select((Dialog)null, localizer, initialFS, options)) {
				return initialFS.open(item);
			}
		} catch (IOException exc) {
			SwingUtils.getNearestLogger(owner).message(Severity.error, exc, exc.getLocalizedMessage());
		} 
		return null;
	}

	static ImageIcon loadImageIcon(final URI iconLocation, final IconType type) {
		if (iconLocation == null) {
			return null;
		}
		else {
			try{final URI							pureUri = URIUtils.removeQueryFromURI(iconLocation);
				final Hashtable<String, String[]> 	query = URIUtils.parseQuery(iconLocation);
				
				switch (type) {
					case ORDINAL	:
						return new ImageIcon(pureUri.toURL());
					case PRESSED	:
						if (query.containsKey("pressed")) {
							return new ImageIcon(replaceLastPathComponent(pureUri,query.get("pressed")[0]).toURL());
						}
						else {
							return null;
						}
					case TOGGLED	:
						if (query.containsKey("toggled")) {
							return new ImageIcon(replaceLastPathComponent(pureUri,query.get("toggled")[0]).toURL());
						}
						else {
							return null;
						}
					default:
						throw new UnsupportedOperationException("Icon type ["+type+"] is not supported yet");
				}
			} catch (IOException exc) {
				return null;
			}
		}
	}

	static Border getFocusedBorder() {
		return new LineBorder(Color.BLUE, 2);
	}
	
	private static URI replaceLastPathComponent(final URI source, final String replacement) {
		final String	result = source.toString();
		final int		index = result.lastIndexOf('/');
		
		if (index > 0) {
			return URI.create(result.substring(0,index)+'/'+replacement);
		}
		else {
			return source;
		}
	}
}
