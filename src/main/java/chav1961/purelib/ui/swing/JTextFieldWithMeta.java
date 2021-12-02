package chav1961.purelib.ui.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Locale;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.URIUtils;
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

public class JTextFieldWithMeta extends JTextField implements NodeMetadataOwner, LocaleChangeListener, JComponentInterface {
	private static final long 	serialVersionUID = -7990739033479280548L;

	private static final Class<?>[]		VALID_CLASSES = {String.class, URL.class, URI.class};
	private static final int			TRIANGLE_WIDTH = 10;
	
	private final ContentNodeMetadata	metadata;
	private String						currentValue, newValue;
	private boolean						invalid = false;
	
	public JTextFieldWithMeta(final ContentNodeMetadata metadata, final JComponentMonitor monitor) throws LocalizationException {
		if (metadata == null) {
			throw new NullPointerException("Metadata can't be null"); 
		}
		else if (monitor == null) {
			throw new NullPointerException("Monitor can't be null"); 
		}
		else if (!InternalUtils.checkClassTypes(metadata.getType(),VALID_CLASSES)) {
			throw new IllegalArgumentException("Invalid node type ["+metadata.getType().getCanonicalName()+"] for the given control. Only "+Arrays.toString(VALID_CLASSES)+" are available");
		}
		else {
			this.metadata = metadata;

			final String		name = URIUtils.removeQueryFromURI(metadata.getUIPath()).toString();
			final FieldFormat	format = metadata.getFormatAssociated() != null ? metadata.getFormatAssociated() : new FieldFormat(metadata.getType());
			
			InternalUtils.addComponentListener(this,()->callLoad(monitor));
			addFocusListener(new FocusListener() {
				@Override
				public void focusLost(final FocusEvent e) {
					try{if (newValue != currentValue && newValue != null && !newValue.equals(currentValue)) {
							monitor.process(MonitorEvent.Saving,metadata,JTextFieldWithMeta.this);
						}
						monitor.process(MonitorEvent.FocusLost,metadata,JTextFieldWithMeta.this);
					} catch (ContentException exc) {
					}					
				}
				
				@Override
				public void focusGained(final FocusEvent e) {
					try{
						monitor.process(MonitorEvent.FocusGained,metadata,JTextFieldWithMeta.this);
					} catch (ContentException exc) {
					}					
					SwingUtilities.invokeLater(()->{
						if (format.needSelectOnFocus()) {
							selectAll();
						}
					});
				}
			});
			SwingUtils.assignActionKey(this,WHEN_FOCUSED,SwingUtils.KS_EXIT,(e)->{
				try{if (monitor.process(MonitorEvent.Rollback,metadata,JTextFieldWithMeta.this)) {
						assignValueToComponent(currentValue);
					}
				} catch (ContentException exc) {
				} finally {
					JTextFieldWithMeta.this.requestFocus();
				}
			},"rollback-value");
			setInputVerifier(new InputVerifier() {
				@Override
				public boolean verify(final JComponent input) {
					try{if (monitor.process(MonitorEvent.Validation,metadata,JTextFieldWithMeta.this)) {
							newValue = (String)getChangedValueFromComponent();
							return true;
						}
						else {
							return false;
						}
					} catch (ContentException e) {
						return false;
					}
				}
			});

			if (InternalUtils.checkMandatory(metadata)) {
				InternalUtils.prepareMandatoryColor(this);
			}
			else {
				InternalUtils.prepareOptionalColor(this);
			}
			switch (format.getAlignment()) {
				case CenterAlignment: setAlignmentX(JTextField.CENTER_ALIGNMENT); break;
				case LeftAlignment	: setAlignmentX(JTextField.LEFT_ALIGNMENT); break;
				case RightAlignment	: setAlignmentX(JTextField.RIGHT_ALIGNMENT); break;
				default: break;
			}
			if (format.isOutput()) {
				setFocusable(false);
			}
			if (format.getLength() != 0) {
				setColumns(format.getLength());
			}
			
			setName(name);
			InternalUtils.registerAdvancedTooptip(this);
			fillLocalizedStrings();
		}		
	}

	@Override
	public ContentNodeMetadata getNodeMetadata() {
		return metadata;
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
	}
	
	@Override
	public String getRawDataFromComponent() {
		return currentValue;
	}

	@Override
	public Object getValueFromComponent() {
		return currentValue;
	}

	@Override
	public Object getChangedValueFromComponent() throws SyntaxException {
		return getText();
	}

	@Override
	public void assignValueToComponent(final Object value) {
		if (value == null) {
			setText("");
			newValue = null;
		}
		else {
			setText(newValue = value.toString());
		}
	}

	@Override
	public Class<?> getValueType() {
		return String.class;
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
		else if (InternalUtils.checkMandatory(getNodeMetadata())) {
			if (val.toString().trim().isEmpty()) {
				return InternalUtils.buildStandardValidationMessage(getNodeMetadata(), InternalUtils.VALIDATION_MANDATORY);
			}
			else {
				return null;
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
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if (getNodeMetadata().getFormatAssociated() != null && getNodeMetadata().getFormatAssociated().hasLocalEditor()) {
			final Graphics2D	g2d = (Graphics2D)g;
			final Color			oldColor = g2d.getColor();
			final int[]			x = new int[] {getWidth(), getWidth(), getWidth() - TRIANGLE_WIDTH};
			final int[]			y = new int[] {getHeight() - TRIANGLE_WIDTH, getHeight(), getHeight()};
			
			g2d.setColor(Color.BLUE);
			g2d.fillPolygon(x, y, x.length);
			g2d.setColor(oldColor);
		}
	}
	
	private void fillLocalizedStrings() throws LocalizationException {
		final Localizer	localizer = LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()); 
		
		if (getNodeMetadata().getTooltipId() != null && !getNodeMetadata().getTooltipId().trim().isEmpty()) {
			setToolTipText(localizer.getValue(getNodeMetadata().getTooltipId()));
		}
	}

	
	private void callLoad(final JComponentMonitor monitor) {
		try{monitor.process(MonitorEvent.Loading,metadata,this);
			currentValue = newValue;
		} catch (ContentException exc) {
		}					
	}
}
