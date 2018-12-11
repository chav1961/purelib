package chav1961.purelib.ui.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolTip;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.Localizer;

public class StyledButton extends JButton {
	private static final long 		serialVersionUID = -8550176610468018240L;
	private static final int		DEFAULT_BUTTON_SIZE = 24;
	private static final Border		PRESSED = new LineBorder(PureLibSettings.instance().getProperty(PureLibSettings.UI_SWING_TOOLBAR_CLICK_BORDER_COLOR,Color.class,"red"),1,true);
	private static final Border		NOT_PRESSED = new EmptyBorder(1,1,1,1);

	private final Localizer	localizer;

	public StyledButton(final Localizer localizer, final String tooltipId, final URL iconWhiteURL) {
		this(localizer,tooltipId,iconWhiteURL,iconWhiteURL);
	}
	
	public StyledButton(final Localizer localizer, final String tooltipId, final URL iconWhiteURL, final URL iconGrayURL) {
		this(localizer,tooltipId,iconWhiteURL,iconGrayURL,iconWhiteURL,iconGrayURL);
	}
	
	public StyledButton(final Localizer localizer, final String tooltipId, final URL iconWhiteURL, final URL iconGrayURL, final URL iconSelectedWhiteURL, final URL iconSelectedGrayURL) {
		this(localizer,tooltipId,URL2Icon(iconWhiteURL,"iconWhiteURL"),URL2Icon(iconGrayURL,"iconGrayURL"),URL2Icon(iconSelectedWhiteURL,"iconSelectedWhiteURL"),URL2Icon(iconSelectedGrayURL,"iconSelectedGrayURL"));
	}
	
	public StyledButton(final Localizer localizer, final String tooltipId, final Icon iconWhite) {
		this(localizer,tooltipId,iconWhite,iconWhite);
	}
	
	public StyledButton(final Localizer localizer, final String tooltipId, final Icon iconWhite, final Icon iconGray) {
		this(localizer,tooltipId,iconWhite,iconGray,iconWhite,iconGray);
	}
	
	public StyledButton(final Localizer localizer, final String tooltipId, final Icon iconWhite, final Icon iconGray, final Icon iconSelectedWhite, final Icon iconSelectedGray) {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (tooltipId == null || tooltipId.isEmpty()) {
			throw new IllegalArgumentException("Tooltip id can't be null or empty");
		}
		else if (iconWhite == null) {
			throw new NullPointerException("White icon can't be null");
		}
		else if (iconGray == null) {
			throw new NullPointerException("Gray icon can't be null");
		}
		else if (iconSelectedWhite == null) {
			throw new NullPointerException("White selected icon can't be null");
		}
		else if (iconSelectedGray == null) {
			throw new NullPointerException("Gray selected icon can't be null");
		}
		else {
			final Dimension	buttonSize;
			
			if (iconWhite.getIconWidth() > 0 && iconWhite.getIconHeight() > 0) {
				buttonSize = new Dimension(iconWhite.getIconWidth()+2,iconWhite.getIconHeight()+2);
				
			}
			else {
				buttonSize = new Dimension(DEFAULT_BUTTON_SIZE+2,DEFAULT_BUTTON_SIZE+2);
			}
			this.localizer = localizer;
			
			addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(final ChangeEvent e) {
					if (isSelected()) {
						if (getModel().isRollover() && isEnabled() && (getAction() == null || getAction().isEnabled())) {
							setIcon(iconSelectedWhite);
						}
						else {
							setIcon(iconSelectedGray);
						}
					}
					else {
						if (getModel().isRollover() && isEnabled() && (getAction() == null || getAction().isEnabled())) {
							setIcon(iconWhite);
						}
						else {
							setIcon(iconGray);
						}
					}
					setBorder(getModel().isPressed() ? PRESSED : NOT_PRESSED);
				}
			});
	
			setIcon(iconGray);
			setToolTipText(tooltipId);		
			setBorder(NOT_PRESSED);
			setMinimumSize(buttonSize);
			setPreferredSize(buttonSize);
			setMaximumSize(buttonSize);
		}
	}

	@Override
	public String getToolTipText() {
		final String	toolTip = super.getToolTipText();
		
		if (toolTip != null) {
			try{return localizer.getValue(toolTip);
			} catch (LocalizationException e) {
				return super.getToolTipText();
			}
		}
		else {
			return toolTip;
		}
	}
	
	@Override
	public JToolTip createToolTip() {
		return new SmartToolTip(localizer,this);
	}
	
	private static Icon URL2Icon(final URL iconURL, final String parameterName) {
		if (iconURL == null) {
			throw new NullPointerException(parameterName+" can't be null");
		}
		else {
			return new ImageIcon(iconURL);
		}
	}
}
