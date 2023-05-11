package chav1961.purelib.ui.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.ColorKeeper;
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

public class JColorPickerWithMeta extends JComponent implements NodeMetadataOwner, LocaleChangeListener, JComponentInterface, BooleanPropChangeListenerSource {
	private static final long 			serialVersionUID = -4542351534072177624L;

	public static final String 			COLOR_NAME = "color";
	
	private static final Class<?>[]		VALID_CLASSES = {Color.class, ColorKeeper.class};

	private final BooleanPropChangeListenerRepo	repo = new BooleanPropChangeListenerRepo();
	private final ContentNodeMetadata	metadata;
	private final Localizer 			localizer;
	private final JButton				callSelect = new JButton("...");
	private Color						currentValue = Color.black, newValue = Color.black;
	private boolean						invalid = false;
	
	public JColorPickerWithMeta(final ContentNodeMetadata metadata, final Localizer localizer, final JComponentMonitor monitor) throws LocalizationException {
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
			
			final String		name = URIUtils.removeQueryFromURI(metadata.getUIPath()).toString();
			final FieldFormat	format = metadata.getFormatAssociated() != null ? metadata.getFormatAssociated() : new FieldFormat(metadata.getType());
			
			InternalUtils.addComponentListener(this,()->callLoad(monitor));
			addFocusListener(new FocusListener() {
				@Override
				public void focusLost(final FocusEvent e) {
					try{if (!newValue.equals(currentValue)) {
							if (monitor.process(MonitorEvent.Validation,metadata,JColorPickerWithMeta.this) && monitor.process(MonitorEvent.Saving,metadata,JColorPickerWithMeta.this)) {
								currentValue = newValue;
							}
						}
						monitor.process(MonitorEvent.FocusLost,metadata,JColorPickerWithMeta.this);
					} catch (ContentException exc) {
						SwingUtils.getNearestLogger(JColorPickerWithMeta.this).message(Severity.error, exc,exc.getLocalizedMessage());
					}					
				}
				
				@Override
				public void focusGained(final FocusEvent e) {
					try{monitor.process(MonitorEvent.FocusGained,metadata,JColorPickerWithMeta.this);
						getActionMap().get(SwingUtils.ACTION_ROLLBACK).setEnabled(false);
					} catch (ContentException exc) {
						SwingUtils.getNearestLogger(JColorPickerWithMeta.this).message(Severity.error, exc,exc.getLocalizedMessage());
					}					
				}
			});
			SwingUtils.assignActionKey(this,WHEN_FOCUSED,SwingUtils.KS_EXIT,(e)->{
				try{if (monitor.process(MonitorEvent.Rollback,metadata,JColorPickerWithMeta.this)) {
						assignValueToComponent(currentValue);
						getActionMap().get(SwingUtils.ACTION_ROLLBACK).setEnabled(false);
					}
				} catch (ContentException exc) {
					SwingUtils.getNearestLogger(JColorPickerWithMeta.this).message(Severity.error, exc,exc.getLocalizedMessage());
				}
			}, SwingUtils.ACTION_ROLLBACK);
			if (!Utils.checkEmptyOrNullString(metadata.getHelpId())) {
				SwingUtils.assignActionKey(this, WHEN_FOCUSED, SwingUtils.KS_HELP, (e)->{
					try {
						SwingUtils.showCreoleHelpWindow(JColorPickerWithMeta.this, LocalizerFactory.getLocalizer(metadata.getLocalizerAssociated()), metadata.getHelpId());
					} catch (IOException exc) {
						SwingUtils.getNearestLogger(JColorPickerWithMeta.this).message(Severity.error, exc, exc.getLocalizedMessage());
					}
				},SwingUtils.ACTION_HELP);
			}
			setInputVerifier(new InputVerifier() {
				@Override
				public boolean verify(final JComponent input) {
					try{return monitor.process(MonitorEvent.Validation,metadata,JColorPickerWithMeta.this);
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
			callSelect.addActionListener((e)->{selectColor();});
			new ComponentKeepedBorder(0,callSelect).install(this);
			setPreferredSize(new Dimension(2*callSelect.getPreferredSize().width,callSelect.getPreferredSize().height));
			
			setName(name);
			callSelect.setName(name+'/'+COLOR_NAME);
			InternalUtils.registerAdvancedTooptip(this);
			fillLocalizedStrings();
		}
	}
	
	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
	}
	
	@Override
	public String getRawDataFromComponent() {
		return currentValue == null ? null : String.format("#%1$02x%2$02x%3$02x",currentValue.getRed(),currentValue.getGreen(),currentValue.getBlue());
	}

	@Override
	public Object getValueFromComponent() {
		if (metadata.getType().isAssignableFrom(ColorKeeper.class)) {
			return new ColorKeeper(currentValue);
		}
		else {
			return currentValue;
		}
	}

	@Override
	public Object getChangedValueFromComponent() throws SyntaxException {
		if (metadata.getType().isAssignableFrom(ColorKeeper.class)) {
			return new ColorKeeper(newValue);
		}
		else {
			return newValue;
		}
	}

	@Override
	public void assignValueToComponent(final Object value) {
		if (value instanceof Color) {
			newValue = (Color)value;
			repaint();
		}
		else if (value instanceof ColorKeeper) {
			newValue = ((ColorKeeper)value).toColor();
			repaint();
		}
		else if (value instanceof String) {
			final String 	val = value.toString();
			
			if (val.isEmpty()) {
				throw new IllegalArgumentException("Null or empty value is not applicable for the color");
			}
			else if (PureLibSettings.colorByName(val.trim(),null) == null) {
				throw new IllegalArgumentException("Unknown color name ["+val.trim()+"] in the value string");
			}
			
			newValue = PureLibSettings.colorByName(value.toString(),Color.black);
			repaint();				
		}
		else {
			throw new IllegalArgumentException("Value can't be null and must be String or Color instance only");
		}
	}

	@Override
	public Class<?> getValueType() {
		return Color.class;
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
	public boolean isEnabled() {
		if (getParent() != null) {
			return super.isEnabled() && getParent().isEnabled();
		}
		else {
			return super.isEnabled();
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
		final Color			oldColor = g2d.getColor();
		final Insets		insets = getInsets();
		final int			x1 = insets.left, x2 = getWidth()-insets.right, y1 = insets.top, y2 = getHeight()-insets.bottom;   

		g2d.setColor(newValue == null ? Color.WHITE : newValue);
		g2d.fillRect(x1,y1,x2-x1,y2-y1);
		g2d.setColor(oldColor);
	}

	protected Color chooseColor(final Localizer localizer, final Color initialColor, final boolean isForeground) throws HeadlessException, LocalizationException {
		return InternalUtils.chooseColor(this, localizer, initialColor, isForeground);
	}
	
	private void fillLocalizedStrings() throws LocalizationException {
		if (getNodeMetadata().getTooltipId() != null && !getNodeMetadata().getTooltipId().trim().isEmpty()) {
			setToolTipText(localizer.getValue(getNodeMetadata().getTooltipId()));
		}
	}
	
	private void selectColor() {
		try{assignValueToComponent(chooseColor(localizer, currentValue, false));
			getActionMap().get(SwingUtils.ACTION_ROLLBACK).setEnabled(true);
		} finally {
			requestFocus();
		}
	}

	private void callLoad(final JComponentMonitor monitor) {
		try{monitor.process(MonitorEvent.Loading,metadata,this);
			currentValue = newValue;
		} catch (ContentException exc) {
			SwingUtils.getNearestLogger(JColorPickerWithMeta.this).message(Severity.error, exc,exc.getLocalizedMessage());
		}					
	}
}
