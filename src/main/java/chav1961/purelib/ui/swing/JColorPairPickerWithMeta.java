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

public class JColorPairPickerWithMeta extends JComponent implements NodeMetadataOwner, LocaleChangeListener, JComponentInterface {
	private static final long serialVersionUID = -4542351534072177624L;

	private static final String 		TITLE = "JColorPicketWithMeta.chooser.title";

	private final ContentNodeMetadata	metadata;
	private final JButton				callSelectF = new JButton("\u25E9");
	private final JButton				callSelectB = new JButton("\u25EA");
	private ColorPair					currentValue, newValue;
	
	public JColorPairPickerWithMeta(final ContentNodeMetadata metadata, final FieldFormat format, final JComponentMonitor monitor) throws LocalizationException {
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
					try{monitor.process(MonitorEvent.Loading,metadata,JColorPairPickerWithMeta.this);
						currentValue = newValue;
					} catch (ContentException exc) {
					}					
				}				
			});
			addFocusListener(new FocusListener() {
				@Override
				public void focusLost(final FocusEvent e) {
					try{if (newValue != null && !newValue.equals(currentValue) || newValue == null && currentValue != null) {
							monitor.process(MonitorEvent.Saving,metadata,JColorPairPickerWithMeta.this);
							currentValue = newValue;
						}
						monitor.process(MonitorEvent.FocusLost,metadata,JColorPairPickerWithMeta.this);
					} catch (ContentException exc) {
					}					
				}
				
				@Override
				public void focusGained(final FocusEvent e) {
					try{monitor.process(MonitorEvent.FocusGained,metadata,JColorPairPickerWithMeta.this);
					} catch (ContentException exc) {
					}					
				}
			});
			getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),"rollback-value");
			getActionMap().put("rollback-value", new AbstractAction(){
				private static final long serialVersionUID = -6372550433958089237L;

				@Override
				public void actionPerformed(ActionEvent e) {
					try{if (monitor.process(MonitorEvent.Rollback,metadata,JColorPairPickerWithMeta.this)) {
							assignValueToComponent(currentValue);
						}
					} catch (ContentException exc) {
					}
				}
			});
			setInputVerifier(new InputVerifier() {
				@Override
				public boolean verify(final JComponent input) {
					try{return monitor.process(MonitorEvent.Validation,metadata,JColorPairPickerWithMeta.this);
					} catch (ContentException e) {
						return false;
					}
				}
			});
			if (format.isReadOnly(false)) {
				callSelectF.setEnabled(false);
				callSelectB.setEnabled(false);
			}
			callSelectF.addActionListener((e)->{selectColorF();});
			callSelectB.addActionListener((e)->{selectColorB();});
			new ComponentKeepedBorder(0,callSelectF,callSelectB).install(this);
			setPreferredSize(new Dimension(3*callSelectF.getPreferredSize().width,callSelectF.getPreferredSize().height));
			fillLocalizedStrings();
		}
	}
	
	@Override
	public void setBorder(Border border) {
		super.setBorder(border);
		new ComponentKeepedBorder(0,callSelectF,callSelectB).install(this);
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
		if (value != null && !(value instanceof ColorPair)) {
			throw new IllegalArgumentException("Value class can be ColorPair only");
		}
		else {
			newValue = (ColorPair)value;
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

		g2d.setColor(newValue == null ? Color.WHITE : newValue.getForeground());
		g2d.fillPolygon(new int[]{0,getWidth()/3,0},new int[]{0,0,getHeight()},3);
		g2d.setColor(newValue == null ? Color.BLACK : newValue.getBackground());
		g2d.fillPolygon(new int[]{0,getWidth()/3,getWidth()/3},new int[]{getHeight(),0,getHeight()},3);
		g2d.setColor(oldColor);
	}

	private void fillLocalizedStrings() throws LocalizationException {
		try{final Localizer	localizer = LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()); 
		
			setToolTipText(localizer.getValue(getNodeMetadata().getTooltipId()));
		} catch (IOException e) {
			throw new LocalizationException(e);
		}
	}
	
	private void selectColorF() {
		try{final Localizer	localizer = LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()); 
			
			assignValueToComponent(new ColorPair(JColorChooser.showDialog(this,localizer.getValue(TITLE), newValue.getForeground()),newValue.getBackground()));
		} catch (IOException | LocalizationException e) {
			//throw new LocalizationException(e);
		}
	}

	private void selectColorB() {
		try{final Localizer	localizer = LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()); 
			
			assignValueToComponent(new ColorPair(newValue.getForeground(),JColorChooser.showDialog(this,localizer.getValue(TITLE), newValue.getBackground())));
		} catch (IOException | LocalizationException e) {
			//throw new LocalizationException(e);
		}
	}
}
