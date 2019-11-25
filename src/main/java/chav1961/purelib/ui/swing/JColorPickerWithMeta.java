package chav1961.purelib.ui.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.border.Border;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor.MonitorEvent;
import chav1961.purelib.ui.swing.useful.ComponentKeepedBorder;

public class JColorPickerWithMeta extends JComponent implements NodeMetadataOwner, LocaleChangeListener, JComponentInterface {
	private static final long 			serialVersionUID = -4542351534072177624L;
	private static final String 		TITLE = "JColorPicketWithMeta.chooser.title";

	private final ContentNodeMetadata	metadata;
	private final JButton				callSelect = new JButton("...");
	private Color						currentValue, newValue;
	
	public JColorPickerWithMeta(final ContentNodeMetadata metadata, final FieldFormat format, final JComponentMonitor monitor) throws LocalizationException {
		if (metadata == null) {
			throw new NullPointerException("Metadata can't be null"); 
		}
		else if (format == null) {
			throw new NullPointerException("Format can't be null"); 
		}
		else if (monitor == null) {
			throw new NullPointerException("Monitor can't be null"); 
		}
		else {
			this.metadata = metadata;
			addComponentListener(new ComponentListener() {
				@Override public void componentResized(ComponentEvent e) {}
				@Override public void componentMoved(ComponentEvent e) {}
				@Override public void componentHidden(ComponentEvent e) {}
				
				@Override
				public void componentShown(ComponentEvent e) {
					try{monitor.process(MonitorEvent.Loading,metadata,JColorPickerWithMeta.this);
						currentValue = newValue;
					} catch (ContentException exc) {
					}					
				}				
			});
			addFocusListener(new FocusListener() {
				@Override
				public void focusLost(final FocusEvent e) {
					try{if (newValue != null && !newValue.equals(currentValue) || newValue == null && currentValue != null) {
							monitor.process(MonitorEvent.Saving,metadata,JColorPickerWithMeta.this);
							currentValue = newValue;
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
			getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),"rollback-value");
			getActionMap().put("rollback-value", new AbstractAction(){
				private static final long serialVersionUID = -6372550433958089237L;

				@Override
				public void actionPerformed(ActionEvent e) {
					try{if (monitor.process(MonitorEvent.Rollback,metadata,JColorPickerWithMeta.this)) {
							assignValueToComponent(currentValue);
						}
					} catch (ContentException exc) {
					}
				}
			});
			setInputVerifier(new InputVerifier() {
				@Override
				public boolean verify(final JComponent input) {
					try{return monitor.process(MonitorEvent.Validation,metadata,JColorPickerWithMeta.this);
					} catch (ContentException e) {
						return false;
					}
				}
			});
			if (format.isReadOnly(false)) {
				callSelect.setEnabled(false);
			}
			callSelect.addActionListener((e)->{selectColor();});
			new ComponentKeepedBorder(0,callSelect).install(this);
			setPreferredSize(new Dimension(2*callSelect.getPreferredSize().width,callSelect.getPreferredSize().height));
			fillLocalizedStrings();
		}
	}
	
	@Override
	public void setBorder(Border border) {
		super.setBorder(border);
		if (!(border instanceof ComponentKeepedBorder)) {
			new ComponentKeepedBorder(0,callSelect).install(this);
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
		if (value != null && !(value instanceof Color)) {
			throw new IllegalArgumentException("Value class can be Color only");
		}
		else {
			newValue = (Color)value;
			repaint();
		}
	}

	@Override
	public Class<?> getValueType() {
		return Color.class;
	}

	@Override
	public String standardValidation(final String value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setInvalid(boolean invalid) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isInvalid() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ContentNodeMetadata getNodeMetadata() {
		return metadata;
	}
	
	@Override
	public void paintComponent(final Graphics g) {
		final Graphics2D	g2d = (Graphics2D)g;
		final Color			oldColor = g2d.getColor();

		g2d.setColor(newValue == null ? Color.BLACK : newValue);
		g2d.fillRect(0,0,getWidth(),getHeight());
		g2d.setColor(oldColor);
	}

	private void fillLocalizedStrings() throws LocalizationException {
		try{final Localizer	localizer = LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()); 
		
			if (getNodeMetadata().getTooltipId() != null) {
				setToolTipText(localizer.getValue(getNodeMetadata().getTooltipId()));
			}
		} catch (IOException e) {
			throw new LocalizationException(e);
		}
	}
	
	private void selectColor() {
		try{final Localizer	localizer = LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()); 
			
			assignValueToComponent(JColorChooser.showDialog(this,localizer.getValue(TITLE), newValue));
		} catch (IOException | LocalizationException e) {
			//throw new LocalizationException(e);
		}
	}
}
