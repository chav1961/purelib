package chav1961.purelib.ui.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.imageio.ImageIO;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.TransferHandler;
import javax.swing.TransferHandler.TransferSupport;
import javax.swing.text.JTextComponent;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.json.ColorKeeper;
import chav1961.purelib.json.ImageKeeper;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.swing.BooleanPropChangeEvent.EventChangeType;
import chav1961.purelib.ui.swing.inner.BooleanPropChangeListenerRepo;
import chav1961.purelib.ui.swing.interfaces.BooleanPropChangeListener;
import chav1961.purelib.ui.swing.interfaces.BooleanPropChangeListenerSource;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor.MonitorEvent;
import chav1961.purelib.ui.swing.useful.ComponentKeepedBorder;

public class JImageContainerWithMeta extends JComponent implements NodeMetadataOwner, LocaleChangeListener, JComponentInterface, BooleanPropChangeListenerSource {
	private static final long 			serialVersionUID = -4542351534072177624L;

	public static final String 			IMAGE_NAME = "image";
	
	private static final String 		TITLE = "JColorPicketWithMeta.chooser.title";
	private static final Class<?>[]		VALID_CLASSES = {Image.class, ImageKeeper.class};

	private final BooleanPropChangeListenerRepo	repo = new BooleanPropChangeListenerRepo();
	private final ContentNodeMetadata	metadata;
	private final Localizer 			localizer;
	private final JButton				callSelect = new JButton("...");
	private File						lastFile = new File("./");
	private Image						currentValue = null, newValue = null;
	private boolean						invalid = false;
	
	public JImageContainerWithMeta(final ContentNodeMetadata metadata, final Localizer localizer, final JComponentMonitor monitor) throws LocalizationException {
		if (metadata == null) {
			throw new NullPointerException("Metadata can't be null"); 
		}
		else if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
		}
		else if (monitor == null) {
			throw new NullPointerException("Monitor can't be null"); 
		}
		else if (!InternalUtils.checkClassTypes(metadata.getType(),VALID_CLASSES)) {
			throw new IllegalArgumentException("Invalid node type ["+metadata.getType().getCanonicalName()+"] for the given control. Only "+Arrays.toString(VALID_CLASSES)+" are available");
		}
		else {
			this.metadata = metadata;
			this.localizer = localizer;
			setFocusable(true);
			
			final String		name = URIUtils.removeQueryFromURI(metadata.getUIPath()).toString();
			final FieldFormat	format = metadata.getFormatAssociated() != null ? metadata.getFormatAssociated() : new FieldFormat(metadata.getType());
			
			InternalUtils.addComponentListener(this,()->callLoad(monitor));
			addFocusListener(new FocusListener() {
				@Override
				public void focusLost(final FocusEvent e) {
					try{if (!Objects.equals(newValue, currentValue)) {
							if (monitor.process(MonitorEvent.Validation,metadata,JImageContainerWithMeta.this) && monitor.process(MonitorEvent.Saving,metadata,JImageContainerWithMeta.this)) {
								currentValue = newValue;
							}
						}
						monitor.process(MonitorEvent.FocusLost,metadata,JImageContainerWithMeta.this);
					} catch (ContentException exc) {
						SwingUtils.getNearestLogger(JImageContainerWithMeta.this).message(Severity.error, exc,exc.getLocalizedMessage());
					}					
				}
				
				@Override
				public void focusGained(final FocusEvent e) {
					try{monitor.process(MonitorEvent.FocusGained,metadata,JImageContainerWithMeta.this);
						getActionMap().get(SwingUtils.ACTION_ROLLBACK).setEnabled(false);
					} catch (ContentException exc) {
						SwingUtils.getNearestLogger(JImageContainerWithMeta.this).message(Severity.error, exc,exc.getLocalizedMessage());
					}					
				}
			});
			SwingUtils.assignActionKey(this,WHEN_FOCUSED,SwingUtils.KS_EXIT,(e)->{
				try{if (monitor.process(MonitorEvent.Rollback,metadata,JImageContainerWithMeta.this)) {
						assignValueToComponent(currentValue);
						getActionMap().get(SwingUtils.ACTION_ROLLBACK).setEnabled(false);
					}
				} catch (ContentException exc) {
					SwingUtils.getNearestLogger(JImageContainerWithMeta.this).message(Severity.error, exc,exc.getLocalizedMessage());
				}
			}, SwingUtils.ACTION_ROLLBACK);
			setInputVerifier(new InputVerifier() {
				@Override
				public boolean verify(final JComponent input) {
					try{return monitor.process(MonitorEvent.Validation,metadata,JImageContainerWithMeta.this);
					} catch (ContentException e) {
						return false;
					}
				}
			});
			if (format != null) {
				if (format.isReadOnly(false)) {
					callSelect.setEnabled(false);
				}
			}
			callSelect.addActionListener((e)->{selectImage();});
			new ComponentKeepedBorder(0,callSelect).install(this);
			setPreferredSize(new Dimension(2*callSelect.getPreferredSize().width,callSelect.getPreferredSize().height));

			SwingUtils.assignActionKey(this, SwingUtils.KS_COPY, (e)->copyContent(), "copy");
			SwingUtils.assignActionKey(this, SwingUtils.KS_PASTE, (e)->pasteContent(), "paste");
			
			setName(name);
			callSelect.setName(name+'/'+IMAGE_NAME);
			InternalUtils.registerAdvancedTooptip(this);
			setTransferHandler(new ImageAndFileTransferHandler(this));
			fillLocalizedStrings();
		}
	}
	
	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
	}
	
	@Override
	public String getRawDataFromComponent() {
		return currentValue == null ? null : new ImageKeeper(currentValue).toString();
	}

	@Override
	public Object getValueFromComponent() {
		return currentValue;
	}

	@Override
	public Object getChangedValueFromComponent() throws SyntaxException {
		return newValue;
	}

	@Override
	public void assignValueToComponent(final Object value) {
		if (value instanceof Image) {
			newValue = (Image)value;
			repaint();
		}
		else if (value instanceof ImageKeeper) {
			newValue = ((ImageKeeper)value).getImage();
			repaint();
		}
		else if (value instanceof String) {
			newValue = new ImageKeeper(value.toString()).getImage();
			repaint();				
		}
		else if (value != null) {
			throw new IllegalArgumentException("Value can't be null and must be String, Image or ImageKeeper instance only");
		}
	}

	@Override
	public Class<?> getValueType() {
		return Image.class;
	}

	@Override
	public String standardValidation(final Object val) {
		if (val == null) {
			if (InternalUtils.checkNullAvailable(getNodeMetadata())) {
				return null;
			}
			else {
				return InternalUtils.buildStandardValidationMessage(getNodeMetadata(), InternalUtils.VALIDATION_NULL_VALUE);
			}
		}
		else {
			return null;
		}
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
	public ContentNodeMetadata getNodeMetadata() {
		return metadata;
	}

	@Override
	public void addBooleanPropChangeListener(final BooleanPropChangeListener listener) {
		repo.addBooleanPropChangeListener(listener);
	}

	@Override
	public void removeBooleanPropChangeListener(final BooleanPropChangeListener listener) {
		repo.removeBooleanPropChangeListener(listener);
	}
	
	@Override
	public void setVisible(final boolean aFlag) {
		final boolean old = isVisible();
		
		super.setVisible(aFlag);
		if (repo != null && aFlag != old) {
			repo.fireBooleanPropChange(this, EventChangeType.VISIBILE, aFlag);
		}
	}
	
	@Override
	public void setEnabled(boolean b) {
		final boolean old = isEnabled();
		
		super.setEnabled(b);
		if (repo != null && b != old) {
			repo.fireBooleanPropChange(this, EventChangeType.ENABLED, b);
		}
	}
	
	@Override
	public void paintComponent(final Graphics g) {
		final Graphics2D	g2d = (Graphics2D)g;
		final Insets		insets = getInsets();
		final Image			image = newValue != null ? newValue : (currentValue != null ? currentValue : new ImageKeeper().getImage()); 
		final int			x1 = insets.left, x2 = getWidth()-insets.right, y1 = insets.top, y2 = getHeight()-insets.bottom;

		g2d.drawImage(image, 0, 0, image.getHeight(null), image.getWidth(null), x1, y1, x2, y2, null);
	}

	protected Image chooseImage(final Localizer localizer, final Image initialImage) throws HeadlessException, LocalizationException {
		final File	choosed = chooseFile(localizer, lastFile);
		
		if (choosed != null) {
			lastFile = choosed;
			
			try{
				return ImageIO.read(choosed);
			} catch (IOException e) {
				SwingUtils.getNearestLogger(this).message(Severity.error, e, "Error loading image file");
				return initialImage;
			}
		}
		else {
			return initialImage;
		}
	}

	private File chooseFile(final Localizer localizer, final File initialFile) throws HeadlessException, LocalizationException {
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
		if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			final File	sel = chooser.getSelectedFile();  
			
			return sel != null ? sel.getAbsoluteFile() : null;
		}
		else {
			return null;
		}
	}
	
	private void copyContent() {
		final Image		img = newValue != null ? newValue : currentValue;  
		
		if (img != null) {
			final Clipboard		cb = Toolkit.getDefaultToolkit().getSystemClipboard();
			final Transferable	t = new Transferable() {
									@Override public boolean isDataFlavorSupported(DataFlavor flavor) {return flavor.equals(DataFlavor.imageFlavor);}
									@Override public DataFlavor[] getTransferDataFlavors() {return new DataFlavor[] {DataFlavor.imageFlavor};}
									@Override public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {return img;}
								};

			cb.setContents(t, (clipboard, contents)->{});
		}
	}
	
	private void pasteContent() {
		final Clipboard	cb = Toolkit.getDefaultToolkit().getSystemClipboard();
		
		if (cb.isDataFlavorAvailable(DataFlavor.imageFlavor)) {
			try{assignValueToComponent(cb.getData(DataFlavor.imageFlavor));
			} catch (UnsupportedFlavorException | IOException exc) {
				SwingUtils.getNearestLogger(this).message(Severity.error, exc, "error pasting image");
			}
		}
	}
	
	private void fillLocalizedStrings() throws LocalizationException {
		if (getNodeMetadata().getTooltipId() != null && !getNodeMetadata().getTooltipId().trim().isEmpty()) {
			setToolTipText(localizer.getValue(getNodeMetadata().getTooltipId()));
		}
	}
	
	private void selectImage() {
		try{assignValueToComponent(chooseImage(localizer,currentValue));
			getActionMap().get(SwingUtils.ACTION_ROLLBACK).setEnabled(true);
		} finally {
			requestFocus();
		}
	}

	private void callLoad(final JComponentMonitor monitor) {
		try{monitor.process(MonitorEvent.Loading,metadata,this);
			currentValue = newValue;
		} catch (ContentException exc) {
			SwingUtils.getNearestLogger(JImageContainerWithMeta.this).message(Severity.error, exc,exc.getLocalizedMessage());
		}					
	}

	private static class ImageAndFileTransferHandler extends TransferHandler {
		private static final long serialVersionUID = 608229074222584792L;

		private final JImageContainerWithMeta	owner;
		
		private ImageAndFileTransferHandler(final JImageContainerWithMeta owner) {
			super(null);
			this.owner = owner;
		}
		
		@Override
		public boolean canImport(final TransferSupport support) {
			return support.getDropAction() == COPY && (support.getDataFlavors()[0].equals(DataFlavor.javaFileListFlavor) || support.getDataFlavors()[0].equals(DataFlavor.imageFlavor)); 
		}
		
		@Override
		public boolean importData(final TransferSupport support) {
			if (canImport(support)) {
				try{Image	image = null;
					
					if (support.getDataFlavors()[0].equals(DataFlavor.javaFileListFlavor)) {
						for (File item : (List<File>)support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor)) {
							if (item.exists() && item.isFile()) {
								image = ImageIO.read(item);
								break;
							}
						}
					}
					else {
						image = (Image)support.getTransferable().getTransferData(DataFlavor.imageFlavor);
					}
					
					if (image != null) {
						owner.assignValueToComponent(image);
						return true;
					}
					else {
						return false;
					}
				
				} catch (UnsupportedFlavorException | IOException e) {
					SwingUtils.getNearestLogger(owner).message(Severity.error, e, "Error pasting files/images");
					return false;
				}
			}
			else {
				return false;
			}
		}
	}
}
