package chav1961.purelib.ui.swing;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DateFormatter;

import chav1961.purelib.basic.NullLoggerFacade;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;

/**
 * <p>This class is a simple Date selection dialog. It supports localization for all inner controls</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.4
 */

public class JDateSelectionPopup extends JPanel implements LocaleChangeListener, JComponentInterface, NodeMetadataOwner {
	private static final long 			serialVersionUID = -7942828154790537910L;
    private static final int 			DIALOG_WIDTH = 220;
    private static final int 			DIALOG_HEIGHT = 220;

	private static final String			YEAR_LABEL = "JDateSelectionDialog.yearLabel";
	private static final String			YEAR_TOOLTIP = "JDateSelectionDialog.year.tooltip";
	private static final String			MONTH_LABEL = "JDateSelectionDialog.monthLabel";
	private static final String			MONTH_TOOLTIP = "JDateSelectionDialog.month.tooltip";
	private static final String			DAY_NAMES = "JDateSelectionDialog.day.names";

	private static final Class<?>[]		VALID_CLASSES = {Date.class, Calendar.class};
	
	
	private final ContentNodeMetadata	metadata;
	private final Localizer				localizer;
    private final JLabel 				yearLabel = new JLabel(), monthLabel = new JLabel();
    private final JSpinner				yearSpin, monthSpin;
    private final JLabel[]				dayNames = new JLabel[7];  
    private final JButton[][] 			dayPins = new JButton[6][7];

    private DateFormatter				formatter;
    private Object						currentDate, newDate;
    private boolean						invalid = false;
	
    
	/**
	 * <p>Constructor of the class</p>
	 * @param localizer localizer to use with the class. Can't be null. It's strongly recommended to use {@linkplain PureLibSettings#PURELIB_LOCALIZER} 
	 * localizer to call the constructor</p>
	 * @param logger logger to print errors into. Can't be null. Toy can use this parameter to build total log with the rest of your application 
	 * @param initialDate initial date to fill in the control
	 * @param refresher date refresher to notify about changes
	 * @throws LocalizationException in any localization errors
	 * @throws NullPointerException if any parameter is null
	 */
	public JDateSelectionPopup(final ContentNodeMetadata metadata, final Localizer localizer, final JComponentMonitor monitor) throws LocalizationException, NullPointerException {
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
			setLayout(new BorderLayout());
            this.metadata = metadata;
			this.localizer = localizer;
			this.formatter = new DateFormatter(prepareDateFormat(getNodeMetadata().getFormatAssociated(),localizer.currentLocale().getLocale()));
            this.yearSpin = new JSpinner(new SpinnerNumberModel(2000,1900,2100, 1));
            this.monthSpin = new JSpinner(new SpinnerNumberModel(1, 1, 12, 1));

            setLayout(new BorderLayout(1,1));
            setBackground(SwingUtils.OPTIONAL_FOREGROUND);

            final JPanel 	yearAndMonth = new JPanel(new FlowLayout());
            
            yearAndMonth.setBackground(SwingUtils.MANDATORY_BACKGROUND);

            yearSpin.setPreferredSize(new Dimension(55, 20));
            yearSpin.setEditor(new JSpinner.NumberEditor(yearSpin, "####"));
            yearSpin.addChangeListener((e)->{
	            final Calendar 	cal = getCalendar();
	            
                cal.set(Calendar.YEAR, ((Integer) yearSpin.getValue()).intValue());
	            newDate = cal;
	            refreshView();
			});
            yearAndMonth.add(yearSpin);

            yearLabel.setForeground(SwingUtils.MANDATORY_FOREGROUND);
            yearAndMonth.add(yearLabel);

            monthSpin.setPreferredSize(new Dimension(35, 20));
            monthSpin.setEditor(new JSpinner.NumberEditor(monthSpin, "##"));
            monthSpin.addChangeListener((e)->{
	            final Calendar 	cal = getCalendar();
	            
                cal.set(Calendar.MONTH, ((Integer) monthSpin.getValue()).intValue() - 1);
	            newDate = cal;
	            refreshView();
			});
            yearAndMonth.add(monthSpin);

            monthLabel.setForeground(SwingUtils.MANDATORY_FOREGROUND);
            yearAndMonth.add(monthLabel);
            
            final JPanel 	daysAndWeeks = new JPanel();
            
            daysAndWeeks.setLayout(new GridLayout(7, 7));
            daysAndWeeks.setBackground(SwingUtils.MANDATORY_BACKGROUND);

            for (int col = 0; col < dayNames.length; col++) {
                final JLabel 	cell = new JLabel("",JLabel.RIGHT);
                
                daysAndWeeks.add(cell);
                dayNames[col] = cell;
            }

            for (int i = 0; i < dayPins.length; i++)
                for (int j = 0; j < dayPins[i].length; j++) {
                    final JButton 	dayButton = new JButton();
                    
                    dayButton.setBorder(new EmptyBorder(1,1,1,1));
                    dayButton.setHorizontalAlignment(SwingConstants.RIGHT);
                    dayButton.setBackground(SwingUtils.OPTIONAL_BACKGROUND);
                    
                    dayButton.addActionListener((event) -> {
                        final JButton 	source = (JButton) event.getSource();
                        
                        if (source.getText().length() != 0) {
	                        final int 		daySelected = Integer.parseInt(source.getText());
	                        final Calendar	cal = getCalendar();
	                        
	                        cal.set(Calendar.DAY_OF_MONTH, daySelected);
	    		            newDate = cal;
                        }
                    });
                    dayPins[i][j] = dayButton;
                    daysAndWeeks.add(dayButton);
                }

            add(yearAndMonth, BorderLayout.NORTH);
            add(daysAndWeeks, BorderLayout.CENTER);

            refreshView();
            
			setPreferredSize(new Dimension(DIALOG_WIDTH,DIALOG_HEIGHT));
			fillLocalizedStrings();
			setFocusable(true);
			enableEvents(AWTEvent.FOCUS_EVENT_MASK|AWTEvent.MOUSE_EVENT_MASK|AWTEvent.KEY_EVENT_MASK);
			addFocusListener(new FocusListener() {
				
				@Override
				public void focusLost(FocusEvent e) {
					// TODO Auto-generated method stub
					System.err.println("LOST");
				}
				
				@Override
				public void focusGained(FocusEvent e) {
					// TODO Auto-generated method stub
					System.err.println("GAINED");
				}
			});
			addKeyListener(new KeyListener() {
				
				@Override
				public void keyTyped(KeyEvent e) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void keyReleased(KeyEvent e) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void keyPressed(KeyEvent e) {
					// TODO Auto-generated method stub
					System.err.println("pressed");
				}
			});
		}		
	}
	
	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		formatter = new DateFormatter(prepareDateFormat(getNodeMetadata().getFormatAssociated(),newLocale));
		fillLocalizedStrings();
	}

	@Override
	public ContentNodeMetadata getNodeMetadata() {
		return metadata;
	}
	
	@Override
	public String getRawDataFromComponent() {
		try{return formatter.valueToString(getChangedValueFromComponent());
		} catch (SyntaxException | ParseException e) {
			return "";
		}
	}

	@Override
	public Object getValueFromComponent() {
		return currentDate;
	}

	@Override
	public Object getChangedValueFromComponent() throws SyntaxException {
		return newDate;
	}

	@Override
	public void assignValueToComponent(final Object value) {
		if (value == null) {
			throw new NullPointerException("Value to assign can't be null");
		}
		else if (!getValueType().isAssignableFrom(value.getClass())) {
			throw new IllegalArgumentException("Ivnalid value class to assign ["+value.getClass().getCanonicalName()+"]. Can be "+Arrays.toString(VALID_CLASSES)+" only");
		}
		else {
			newDate = value;
			refreshView();
		}
	}


	@Override
	public Class<?> getValueType() {
		return getNodeMetadata().getType();
	}

	@Override
	public String standardValidation(final String value) {
		if (value == null || value.isEmpty()) {
			return "Null or empty value is not applicable for the date";
		}
		else {
			try{formatter.stringToValue(value.trim());
				return null;
			} catch (ParseException e) {
				return e.getLocalizedMessage();
			}
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
	
	private void fillLocalizedStrings() throws LocalizationException {
		yearLabel.setText(localizer.getValue(YEAR_LABEL));
		yearSpin.setToolTipText(localizer.getValue(YEAR_TOOLTIP));
		monthLabel.setText(localizer.getValue(MONTH_LABEL));
		monthSpin.setToolTipText(localizer.getValue(MONTH_TOOLTIP));
        
		final String 	days[] = localizer.getValue(DAY_NAMES).split("\\;");
        
        for (int index = 0; index < days.length; index++) {
        	final Color		fore = days[index].startsWith("!") ? SwingUtils.DATEPICKER_WEEKEND_VALUE_COLOR : SwingUtils.DATEPICKER_DAY_VALUE_COLOR;
        	
        	dayNames[index].setForeground(fore);
        	for (JButton item : dayPins[index]) {
        		item.setForeground(fore);
        	}
        	dayNames[index].setText(days[index].replace("!",""));
        }
	}

    private Calendar getCalendar() {
        final Calendar 	calendar = Calendar.getInstance(localizer.currentLocale().getLocale());
        
    	if (currentDate instanceof Calendar) {
    		calendar.setTimeInMillis(((Calendar)newDate).getTimeInMillis());
    	}
    	else {
            calendar.setTimeInMillis(((Date)newDate).getTime());
    	}
        return calendar;
    }

	private static DateFormat prepareDateFormat(final FieldFormat format, final Locale locale) {
		if (format == null || format.getFormatMask() == null) {
			return DateFormat.getDateInstance(DateFormat.MEDIUM,locale);
		}
		else {
			return new SimpleDateFormat(format.getFormatMask(),locale);
		}
	}

	private void refreshView() {
		final Calendar	cal = getCalendar();
		
		yearSpin.setValue(cal.get(Calendar.YEAR));
		monthSpin.setValue(cal.get(Calendar.MONTH));
        cal.set(Calendar.DAY_OF_MONTH, 1);
        
        final int 	maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        for (int i = 0, minDay = 2 - cal.get(Calendar.DAY_OF_WEEK); i < dayPins.length; i++) {
            for (int j = 0; j < dayPins[i].length; j++, minDay++) {
            	if (minDay >= 1 && minDay <= maxDay) {
            		dayPins[i][j].setText(String.valueOf(minDay));
            		dayPins[i][j].setEnabled(true);
            	}
            	else {
                    dayPins[i][j].setText("");
            		dayPins[i][j].setEnabled(false);
            	}
            }
        }
	}
}
