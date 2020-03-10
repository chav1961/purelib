package chav1961.purelib.ui.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
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
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor.MonitorEvent;
import chav1961.purelib.ui.swing.useful.ComponentKeepedBorder;

public class JColorPickerWithMeta extends JComponent implements NodeMetadataOwner, LocaleChangeListener, JComponentInterface {
	private static final long 			serialVersionUID = -4542351534072177624L;

	public static final String 			COLOR_NAME = "color";
	
	private static final String 		TITLE = "JColorPicketWithMeta.chooser.title";
	private static final Class<?>[]		VALID_CLASSES = {Color.class};

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
			throw new IllegalArgumentException("Invalid node type for the given control. Only "+Arrays.toString(VALID_CLASSES)+" are available");
		}
		else {
			this.metadata = metadata;
			this.localizer = localizer;
			
			final String		name = URIUtils.removeQueryFromURI(metadata.getUIPath()).toString();
			final FieldFormat	format = metadata.getFormatAssociated();
			
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
					}					
				}
				
				@Override
				public void focusGained(final FocusEvent e) {
					try{monitor.process(MonitorEvent.FocusGained,metadata,JColorPickerWithMeta.this);
					} catch (ContentException exc) {
					}					
				}
			});
			SwingUtils.assignActionKey(this,WHEN_FOCUSED,SwingUtils.KS_EXIT,(e)->{
				try{if (monitor.process(MonitorEvent.Rollback,metadata,JColorPickerWithMeta.this)) {
					assignValueToComponent(currentValue);
				}
				} catch (ContentException exc) {
				}
			},"rollback-value");
			setInputVerifier(new InputVerifier() {
				@Override
				public boolean verify(final JComponent input) {
					try{return monitor.process(MonitorEvent.Validation,metadata,JColorPickerWithMeta.this);
					} catch (ContentException e) {
						return false;
					}
				}
			});
			if (format != null && format.isReadOnly(false)) {
				callSelect.setEnabled(false);
			}
			callSelect.addActionListener((e)->{selectColor();});
			new ComponentKeepedBorder(0,callSelect).install(this);
			setPreferredSize(new Dimension(2*callSelect.getPreferredSize().width,callSelect.getPreferredSize().height));
			
			setName(name);
			callSelect.setName(name+'/'+COLOR_NAME);
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
		return currentValue;
	}

	@Override
	public Object getChangedValueFromComponent() throws SyntaxException {
		return newValue;
	}

	@Override
	public void assignValueToComponent(final Object value) {
		if (value instanceof Color) {
			newValue = (Color)value;
			repaint();
		}
		else if (value instanceof String) {
			final String	val = (String)value;
			
			if (standardValidation(val) == null) {
				newValue = PureLibSettings.colorByName(value.toString(),Color.black);
				repaint();				
			}
			else {
				throw new IllegalArgumentException("Illegal string value ["+value+"]: "+standardValidation(val));
			}
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
	public String standardValidation(final String value) {
		if (value == null || value.isEmpty()) {
			return "Null or empty value is not applicable for the color";
		}
		else if (PureLibSettings.colorByName(value.trim(),null) == null) {
			return "Unknown color name ["+value.trim()+"] in the value string";
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
		return Utils.nvl(JColorChooser.showDialog(this,localizer.getValue(TITLE),initialColor),initialColor);
	}
	
	private void fillLocalizedStrings() throws LocalizationException {
		if (getNodeMetadata().getTooltipId() != null) {
			setToolTipText(localizer.getValue(getNodeMetadata().getTooltipId()));
		}
	}
	
	private void selectColor() {
		try{assignValueToComponent(chooseColor(localizer,currentValue,false));
		} catch (LocalizationException e) {
			e.printStackTrace();
		} finally {
			requestFocus();
		}
	}

	private void callLoad(final JComponentMonitor monitor) {
		try{monitor.process(MonitorEvent.Loading,metadata,this);
			currentValue = newValue;
		} catch (ContentException exc) {
		}					
	}
}
