package chav1961.purelib.ui.swing.useful;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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

import chav1961.purelib.basic.NullLoggerFacade;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.ui.swing.SwingUtils;

/**
 * <p>This class is a simple Date selection dialog. It supports localization for all inner controls</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.4
 */

public class JDateSelectionDialog extends JPanel implements LocaleChangeListener {
	private static final long 			serialVersionUID = -7942828154790537910L;
    private static final int 			DIALOG_WIDTH = 220;
    private static final int 			DIALOG_HEIGHT = 220;

	private static final String			YEAR_LABEL = "JDateSelectionDialog.yearLabel";
	private static final String			YEAR_TOOLTIP = "JDateSelectionDialog.year.tooltip";
	private static final String			MONTH_LABEL = "JDateSelectionDialog.monthLabel";
	private static final String			MONTH_TOOLTIP = "JDateSelectionDialog.month.tooltip";
	private static final String			DAY_NAMES = "JDateSelectionDialog.day.names";

	private static final DateRefresher	NULL_REFRESHER = new DateRefresher() {
											@Override
											public void refresh(Date newDate, boolean exitNow) {
//												System.err.println("Date="+newDate+", exit="+exitNow);
											}
										};	
    
	@FunctionalInterface
	public interface DateRefresher {
		void refresh(Date newDate, boolean exitNow);
	}
	
	private final Localizer			localizer;
	@SuppressWarnings("unused")
	private final LoggerFacade		logger;
	private final DateRefresher		refresher;
    private final JLabel 			yearLabel = new JLabel(), monthLabel = new JLabel();
    private final JSpinner			yearSpin, monthSpin;
    private final JLabel[]			dayNames = new JLabel[7];  
    private final JButton[][] 		dayPins = new JButton[6][7];

    private Date					currentDate;
	
    
    /**
	 * <p>Constructor of the class</p>
	 * @param localizer localizer to use with the class. Can't be null. It's strongly recommended to use {@linkplain PureLibSettings#PURELIB_LOCALIZER} 
	 * localizer to call the constructor</p>
	 * @throws LocalizationException in any localization errors
	 * @throws NullPointerException if any parameter is null
	 */
	public JDateSelectionDialog(final Localizer localizer) throws LocalizationException {
		this(localizer,new NullLoggerFacade(),new Date(System.currentTimeMillis()),NULL_REFRESHER);
	}

	/**
	 * <p>Constructor of the class</p>
	 * @param localizer localizer to use with the class. Can't be null. It's strongly recommended to use {@linkplain PureLibSettings#PURELIB_LOCALIZER} 
	 * @param initialDate initial date to fill in the control
	 * @param refresher date refresher to notify about changes
	 * @throws LocalizationException in any localization errors
	 * @throws NullPointerException if any parameter is null
	 */
	public JDateSelectionDialog(final Localizer localizer, final Date initialDate, final DateRefresher refresher) throws LocalizationException, NullPointerException {
		this(localizer,new NullLoggerFacade(),initialDate,refresher);
	}
	
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
	public JDateSelectionDialog(final Localizer localizer, final LoggerFacade logger, final Date initialDate, final DateRefresher refresher) throws LocalizationException, NullPointerException {
		super(new BorderLayout());
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (logger == null) {
			throw new NullPointerException("Logger facade can't be null");
		}
		else if (initialDate == null) {
			throw new NullPointerException("Initial date can't be null");
		}
		else if (refresher == null) {
			throw new NullPointerException("Date refresher can't be null");
		}
		else {
			final Calendar			current = Calendar.getInstance();

			current.setTimeInMillis(System.currentTimeMillis());
            this.yearSpin = new JSpinner(new SpinnerNumberModel(current.get(Calendar.YEAR), current.get(Calendar.YEAR) - 100, current.get(Calendar.YEAR) + 100, 1));
            this.monthSpin = new JSpinner(new SpinnerNumberModel(current.get(Calendar.MONTH) + 1, 1, 12, 1));
			this.localizer = localizer;
			this.logger = logger;
			this.currentDate = initialDate;
			this.refresher = refresher;

            setLayout(new BorderLayout(1,1));
            setBackground(SwingUtils.OPTIONAL_FOREGROUND);

            final JPanel 	yearAndMonth = new JPanel(new FlowLayout());
            
            yearAndMonth.setBackground(SwingUtils.MANDATORY_BACKGROUND);

            yearSpin.setPreferredSize(new Dimension(55, 20));
            yearSpin.setEditor(new JSpinner.NumberEditor(yearSpin, "####"));
            yearSpin.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
		            final Calendar 	cal = getCalendar();
		            
	                cal.set(Calendar.YEAR, getSelectedYear());
		            changeDate(cal.getTime());
		            fillDays();
				}
			});
            yearAndMonth.add(yearSpin);

            yearLabel.setForeground(SwingUtils.MANDATORY_FOREGROUND);
            yearAndMonth.add(yearLabel);

            monthSpin.setPreferredSize(new Dimension(35, 20));
            monthSpin.setEditor(new JSpinner.NumberEditor(monthSpin, "##"));
            monthSpin.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
		            final Calendar 	cal = getCalendar();
		            
	                cal.set(Calendar.MONTH, getSelectedMonth() - 1);
		            changeDate(cal.getTime());
		            fillDays();
				}
			});
            yearAndMonth.add(monthSpin);

            monthLabel.setForeground(SwingUtils.MANDATORY_FOREGROUND);
            yearAndMonth.add(monthLabel);
            
            final JPanel 	daysAndWeeks = new JPanel();
            
            daysAndWeeks.setLayout(new GridLayout(7, 7));
            daysAndWeeks.setBackground(SwingUtils.MANDATORY_BACKGROUND);

            for (int col = 0; col < dayNames.length; col++) {
                final JLabel 	cell = new JLabel("",JLabel.RIGHT);
                
                cell.setForeground(col == 0 || col == 6 
                		? SwingUtils.DATEPICKER_WEEKEND_NAME_COLOR 
                		: SwingUtils.DATEPICKER_DAY_NAME_COLOR);
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
                        JButton source = (JButton) event.getSource();
                        if (source.getText().length() == 0) {
                            return;
                        }
                        else {
	                        final int 		daySelected = Integer.parseInt(source.getText());
	                        final Calendar	cal = getCalendar();
	                        
	                        cal.set(Calendar.DAY_OF_MONTH, daySelected);
	    		            changeDate(cal.getTime());
                        }
                    });

                    dayButton.setForeground(j == 0 || j == 6 
	                    		? SwingUtils.DATEPICKER_WEEKEND_VALUE_COLOR 
	                    		: SwingUtils.DATEPICKER_DAY_VALUE_COLOR);
                    dayPins[i][j] = dayButton;
                    daysAndWeeks.add(dayButton);
                }

            add(yearAndMonth, BorderLayout.NORTH);
            add(daysAndWeeks, BorderLayout.CENTER);

            fillDays();
            
			setPreferredSize(new Dimension(DIALOG_WIDTH,DIALOG_HEIGHT));
			SwingUtils.assignActionKey(this, WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, SwingUtils.KS_EXIT, (e)->refresher.refresh(currentDate,true),SwingUtils.ACTION_EXIT);
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
		fillLocalizedStrings();
	}

	public Date getDateSelected() {
		return currentDate;
	}	
	
	private void fillLocalizedStrings() throws LocalizationException {
		yearLabel.setText(localizer.getValue(YEAR_LABEL));
		yearSpin.setToolTipText(localizer.getValue(YEAR_TOOLTIP));
		monthLabel.setText(localizer.getValue(MONTH_LABEL));
		monthSpin.setToolTipText(localizer.getValue(MONTH_TOOLTIP));
        
		final String days[] = localizer.getValue(DAY_NAMES).split("\\;");
        
        for (int index = 0; index < days.length; index++) {
        	dayNames[index].setText(days[index]);
        }
	}

	private void changeDate(final Date newDate) {
		currentDate = newDate;
		refresher.refresh(newDate,false);
	}
	
    private Calendar getCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentDate.getTime());
        return calendar;
    }

    private int getSelectedYear() {
        return ((Integer) yearSpin.getValue()).intValue();
    }

    private int getSelectedMonth() {
        return ((Integer) monthSpin.getValue()).intValue();
    }

    private void fillDays() {
        final Calendar 	cal = getCalendar();
        
        cal.set(Calendar.DAY_OF_MONTH, 1);
        
        final int 	maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        for (int i = 0, minDay = 2 - cal.get(Calendar.DAY_OF_WEEK); i < dayPins.length; i++) {
            for (int j = 0; j < dayPins[i].length; j++, minDay++) {
                dayPins[i][j].setText(minDay >= 1 && minDay <= maxDay ? String.valueOf(minDay) : "");
            }
        }
    }
}
