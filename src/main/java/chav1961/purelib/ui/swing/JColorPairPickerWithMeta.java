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

import javax.swing.ImageIcon;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;

import chav1961.purelib.basic.ColorUtils;
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
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.ColorPair;
import chav1961.purelib.ui.inner.InternalConstants;
import chav1961.purelib.ui.swing.BooleanPropChangeEvent.EventChangeType;
import chav1961.purelib.ui.swing.inner.BooleanPropChangeListenerRepo;
import chav1961.purelib.ui.swing.interfaces.BooleanPropChangeListener;
import chav1961.purelib.ui.swing.interfaces.BooleanPropChangeListenerSource;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor.MonitorEvent;
import chav1961.purelib.ui.swing.useful.ComponentKeepedBorder;

public class JColorPairPickerWithMeta extends JComponent implements NodeMetadataOwner, LocaleChangeListener, JComponentInterface, BooleanPropChangeListenerSource {
	private static final long serialVersionUID = -4542351534072177624L;

	public static final String 			FOREGROUND_NAME = "foreground";
	public static final String 			BACKGROUND_NAME = "background";
	
	private static final Class<?>[]		VALID_CLASSES = {ColorPair.class};

	private final BooleanPropChangeListenerRepo	repo = new BooleanPropChangeListenerRepo();
	private final ContentNodeMetadata	metadata;
	private final Localizer				localizer;
	private final JButton				callSelectF = new JButton(InternalConstants.ICON_UPPERLEFT_QUAD);
	private final JButton				callSelectB = new JButton(InternalConstants.ICON_LOWERRIGHT_QUAD);
	private ColorPair					currentValue = new ColorPair(Color.white,Color.black), newValue = currentValue;
	private boolean						invalid = false;
	
	public JColorPairPickerWithMeta(final ContentNodeMetadata metadata, final Localizer localizer, final JComponentMonitor monitor) throws LocalizationException {
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
			
			final String	name = URIUtils.removeQueryFromURI(metadata.getUIPath()).toString(); 
			
			InternalUtils.addComponentListener(this,()->callLoad(monitor));
			addFocusListener(new FocusListener() {
				@Override
				public void focusLost(final FocusEvent e) {
					try{if (!newValue.equals(currentValue)) {
							if (monitor.process(MonitorEvent.Validation,metadata,JColorPairPickerWithMeta.this) && monitor.process(MonitorEvent.Saving,metadata,JColorPairPickerWithMeta.this)) {
								currentValue = newValue;
							}
						}
						monitor.process(MonitorEvent.FocusLost,metadata,JColorPairPickerWithMeta.this);
					} catch (ContentException exc) {
						SwingUtils.getNearestLogger(JColorPairPickerWithMeta.this).message(Severity.error, exc,exc.getLocalizedMessage());
					}					
				}
				
				@Override
				public void focusGained(final FocusEvent e) {
					try{monitor.process(MonitorEvent.FocusGained,metadata,JColorPairPickerWithMeta.this);
						getActionMap().get(SwingUtils.ACTION_ROLLBACK).setEnabled(false);
					} catch (ContentException exc) {
						SwingUtils.getNearestLogger(JColorPairPickerWithMeta.this).message(Severity.error, exc,exc.getLocalizedMessage());
					}					
				}
			});
			SwingUtils.assignActionKey(this,WHEN_FOCUSED,SwingUtils.KS_EXIT,(e)->{
				try{if (monitor.process(MonitorEvent.Rollback,metadata,JColorPairPickerWithMeta.this)) {
						assignValueToComponent(currentValue);
						getActionMap().get(SwingUtils.ACTION_ROLLBACK).setEnabled(false);
					}
				} catch (ContentException exc) {
					SwingUtils.getNearestLogger(JColorPairPickerWithMeta.this).message(Severity.error, exc,exc.getLocalizedMessage());
				}
			}, SwingUtils.ACTION_ROLLBACK);
			if (!Utils.checkEmptyOrNullString(metadata.getHelpId())) {
				SwingUtils.assignActionKey(this, WHEN_FOCUSED, SwingUtils.KS_HELP, (e)->{
					try {
						SwingUtils.showCreoleHelpWindow(JColorPairPickerWithMeta.this, LocalizerFactory.getLocalizer(metadata.getLocalizerAssociated()), metadata.getHelpId());
					} catch (IOException exc) {
						SwingUtils.getNearestLogger(JColorPairPickerWithMeta.this).message(Severity.error, exc, exc.getLocalizedMessage());
					}
				},SwingUtils.ACTION_HELP);
			}
			setInputVerifier(new InputVerifier() {
				@Override
				public boolean verify(final JComponent input) {
					try{return monitor.process(MonitorEvent.Validation,metadata,JColorPairPickerWithMeta.this);
					} catch (ContentException e) {
						return false;
					}
				}
			});
			if (metadata.getFormatAssociated() != null) {
				if (metadata.getFormatAssociated().isReadOnly(false)) {
					callSelectF.setEnabled(false);
					callSelectB.setEnabled(false);
				}
			}
			callSelectF.addActionListener((e)->{selectColorF();});
			callSelectB.addActionListener((e)->{selectColorB();});
			new ComponentKeepedBorder(0,callSelectF,callSelectB).install(this);
			setPreferredSize(new Dimension(3*callSelectF.getPreferredSize().width,callSelectF.getPreferredSize().height));
			setName(name);
			callSelectF.setName(name+'/'+FOREGROUND_NAME);
			callSelectB.setName(name+'/'+BACKGROUND_NAME);
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
		return currentValue == null ? null : String.format("{#%1$02x%2$02x%3$02x,#%4$02x%5$02x%6$02x}"
				,currentValue.getForeground().getRed(),currentValue.getForeground().getGreen(),currentValue.getForeground().getBlue()
				,currentValue.getBackground().getRed(),currentValue.getBackground().getGreen(),currentValue.getBackground().getBlue());
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
		if (value instanceof ColorPair) {
			newValue = (ColorPair)value;
			repaint();
		}
		else if (value instanceof String) {
			final String	val = (String)value;

			if (val.isEmpty()) {
				throw new IllegalArgumentException("empty value is not applicable for the color pair");
			}
			else if (!(val.startsWith("{") && val.endsWith("}") && val.contains(","))) {
				throw new IllegalArgumentException("Illegal color pair format ["+value+"]. Valid format is '{foreground,background}'");
			}
			else if (val.replace(",","").length() < val.length() - 1) {
				throw new IllegalArgumentException("More than two colors in the string");
			}
			else {
				for (String item : val.substring(1,val.length()-2).split("\\,")) {
					if (ColorUtils.colorByName(item.trim(),null) == null) {
						throw new IllegalArgumentException("Unknown color name ["+item.trim()+"] in the value string");
					}
				}
			}
			
			try{newValue = new ColorPair(val);
				repaint();				
			} catch (SyntaxException e) {
				throw new IllegalArgumentException("Illegal string value ["+value+"]: "+e);
			}
		}
		else {
			throw new IllegalArgumentException("Value can't be null and must be String or ColorPair instance only");
		}
	}

	@Override
	public Class<?> getValueType() {
		return ColorPair.class;
	}

	@Override
	public String standardValidation(final Object value) {
		if (value == null) {
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
	public void setInvalid(final boolean invalid) {
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

		g2d.setColor(newValue == null ? Color.WHITE : newValue.getForeground());
		g2d.fillPolygon(new int[]{x1,x2,x1},new int[]{y1,y1,y2},3);
		g2d.setColor(newValue == null ? Color.BLACK : newValue.getBackground());
		g2d.fillPolygon(new int[]{x1,x2,x2},new int[]{y2,y1,y2},3);
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

	private void selectColorF() {
		try{assignValueToComponent(new ColorPair(chooseColor(localizer, currentValue.getForeground(), true),currentValue.getBackground()));
			getActionMap().get(SwingUtils.ACTION_ROLLBACK).setEnabled(true);
		} finally {
			requestFocus();
		}
	}

	private void selectColorB() {
		try{assignValueToComponent(new ColorPair(currentValue.getForeground(),chooseColor(localizer, currentValue.getBackground(), false)));
			getActionMap().get(SwingUtils.ACTION_ROLLBACK).setEnabled(true);
		} finally {
			requestFocus();
		}
	}

	private void callLoad(final JComponentMonitor monitor) {
		try{monitor.process(MonitorEvent.Loading,metadata,this);
			currentValue = newValue;
		} catch (ContentException exc) {
			SwingUtils.getNearestLogger(JColorPairPickerWithMeta.this).message(Severity.error, exc,exc.getLocalizedMessage());
		}					
	}
}
