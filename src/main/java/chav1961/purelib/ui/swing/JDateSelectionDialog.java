package chav1961.purelib.ui.swing;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.DateFormatter;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor.MonitorEvent;

/**
 * <p>This class is a simple Date selection dialog. It supports localization for all inner controls</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.4
 */

public class JDateSelectionDialog extends JComponent implements LocaleChangeListener, JComponentInterface, NodeMetadataOwner {
	private static final long 			serialVersionUID = -7942828154790537910L;
    private static final int 			DIALOG_WIDTH = 250;
    private static final int 			DIALOG_HEIGHT = 200;
    private static final int 			CELL_SIZE = 25;

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
    private final DaysModel				days = new DaysModel();
    private final JTable				daysTable = new JTable(days);
    private final JScrollPane			dayScroll = new JScrollPane(daysTable,JScrollPane.VERTICAL_SCROLLBAR_NEVER,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    private DateFormatter				formatter;
    private Object						currentValue, newValue = new Date();
    private boolean						invalid = false;
	
    
	/**
	 * <p>Constructor of the class</p>
     * @param metadata metadata of the date field
	 * @param localizer localizer to use with the class. Can't be null. It's strongly recommended to use {@linkplain PureLibSettings#PURELIB_LOCALIZER} 
	 * localizer to call the constructor</p>
     * @param monitor callback to process all actions in the control
	 * @throws LocalizationException in any localization errors
	 * @throws NullPointerException if any parameter is null
	 */
	public JDateSelectionDialog(final ContentNodeMetadata metadata, final Localizer localizer, final JComponentMonitor monitor) throws LocalizationException, NullPointerException {
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
            setBackground(PureLibSettings.defaultColorScheme().OPTIONAL_FOREGROUND);

            final JPanel 	yearAndMonth = new JPanel(new FlowLayout());
            
            yearAndMonth.setBackground(PureLibSettings.defaultColorScheme().MANDATORY_BACKGROUND);
            
            yearSpin.setEditor(new JSpinner.NumberEditor(yearSpin, "####"));
            yearSpin.addChangeListener((e)->{
	            final Calendar 	cal = getCalendar();
	            
                cal.set(Calendar.YEAR, ((Integer) yearSpin.getValue()).intValue());
	            storeNewValue(cal);
	            refreshView();
			});
            yearAndMonth.add(yearSpin);

            yearLabel.setForeground(PureLibSettings.defaultColorScheme().MANDATORY_FOREGROUND);
            yearAndMonth.add(yearLabel);

            monthSpin.setEditor(new JSpinner.NumberEditor(monthSpin, "##"));
            monthSpin.addChangeListener((e)->{
	            final Calendar 	cal = getCalendar();
	            
                cal.set(Calendar.MONTH, ((Integer) monthSpin.getValue()).intValue() - 1);
	            storeNewValue(cal);
	            refreshView();
			});
            yearAndMonth.add(monthSpin);

            monthLabel.setForeground(PureLibSettings.defaultColorScheme().MANDATORY_FOREGROUND);
            yearAndMonth.add(monthLabel);
            
            daysTable.setDefaultRenderer(String.class,days);
            daysTable.getTableHeader().setDefaultRenderer((table,value,isSelected,hasFocus,row,column)->{
    			final JLabel	label = new JLabel(value == null ? "" : value.toString());
    			
    			label.setForeground(days.weekends[column] ? Color.RED : Color.BLACK);
    			label.setBorder(new LineBorder(Color.BLACK));
    			label.setOpaque(true);
            	return label;
            });
            daysTable.getTableHeader().setReorderingAllowed(false);
            daysTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            daysTable.setRowSelectionAllowed(false);
            daysTable.setColumnSelectionAllowed(false);
            daysTable.setCellSelectionEnabled(true);
            daysTable.setRowHeight(CELL_SIZE);
            for (int index = 0, maxIndex = daysTable.getColumnCount(); index < maxIndex; index++) {
            	final TableColumn col = daysTable.getColumnModel().getColumn(index);
            	col.setWidth(CELL_SIZE);
            	col.setMinWidth(CELL_SIZE);
            	col.setPreferredWidth(CELL_SIZE);
            	col.setMaxWidth(CELL_SIZE);
            	col.setResizable(false);
            }
            daysTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e) {
					final int		colIndex = daysTable.getSelectedColumn(), rowIndex = daysTable.getSelectedRow();
					final Calendar	cal = getCalendar();
					final Object	day = daysTable.getModel().getValueAt(rowIndex, colIndex);
						
					if ((day instanceof String) && !((String)day).isEmpty()) {
		                cal.set(Calendar.DAY_OF_MONTH, Integer.valueOf(day.toString()));
			            storeNewValue(cal);
					}
				}
			});
            
            add(yearAndMonth, BorderLayout.NORTH);
            add(dayScroll, BorderLayout.CENTER);

			setMinimumSize(new Dimension(DIALOG_WIDTH,DIALOG_HEIGHT));
			setPreferredSize(new Dimension(DIALOG_WIDTH,DIALOG_HEIGHT));
			
			setFocusable(true);
			enableEvents(AWTEvent.COMPONENT_EVENT_MASK|AWTEvent.FOCUS_EVENT_MASK|AWTEvent.MOUSE_EVENT_MASK|AWTEvent.KEY_EVENT_MASK);
			setFocusCycleRoot(true);
			setFocusTraversalPolicyProvider(true);

			InternalUtils.addComponentListener(this,()->callLoad(monitor));
			addFocusListener(new FocusListener() {
				@Override
				public void focusLost(final FocusEvent e) {
					try{monitor.process(MonitorEvent.Saving,metadata,JDateSelectionDialog.this);
						monitor.process(MonitorEvent.FocusLost,metadata,JDateSelectionDialog.this);
					} catch (ContentException exc) {
					}
				}
				
				@Override
				public void focusGained(final FocusEvent e) {
					try{
						monitor.process(MonitorEvent.FocusGained,metadata,JDateSelectionDialog.this);
					} catch (ContentException exc) {
					}					
				}
			});
			setInputVerifier(new InputVerifier() {
				@Override
				public boolean verify(final JComponent input) {
					try{return monitor.process(MonitorEvent.Validation,metadata,JDateSelectionDialog.this);
					} catch (ContentException e) {
						return false;
					}
				}
			});
			setName(URIUtils.removeQueryFromURI(metadata.getUIPath()).toString());
			
			fillLocalizedStrings();
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
		return currentValue;
	}

	@Override
	public Object getChangedValueFromComponent() throws SyntaxException {
		return newValue;
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
			newValue = value;
			refreshView();
		}
	}

	@Override
	public Class<?> getValueType() {
		return getNodeMetadata().getType();
	}

	@Override
	public String standardValidation(final Object val) {
		if (SwingUtils.inAllowedClasses(val,VALID_CLASSES)) {
			return null;
		}
		else if (val instanceof String) {
			final String	 value = val.toString();
			
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
		else {
			return "Illegal value type to validate";
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
	
	protected void storeNewValue(final Object value) {
		newValue = value;
	}
	
	private void fillLocalizedStrings() throws LocalizationException {
		yearLabel.setText(localizer.getValue(YEAR_LABEL));
		yearSpin.setToolTipText(localizer.getValue(YEAR_TOOLTIP));
		monthLabel.setText(localizer.getValue(MONTH_LABEL));
		monthSpin.setToolTipText(localizer.getValue(MONTH_TOOLTIP));
		days.refreshDayNames(localizer);
	}

    private Calendar getCalendar() {
        final Calendar 	calendar = Calendar.getInstance(localizer.currentLocale().getLocale());
        
    	if (newValue instanceof Calendar) {
    		calendar.setTimeInMillis(((Calendar)newValue).getTimeInMillis());
    	}
    	else {
            calendar.setTimeInMillis(((Date)newValue).getTime());
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

	private boolean 	recursionProtect = false;
	
	private void refreshView() {
		if (!recursionProtect) {
			recursionProtect = true;
			final Calendar	cal = getCalendar();
			final int		year = cal.get(Calendar.YEAR);
			final int		month = cal.get(Calendar.MONTH)+1;
			
			yearSpin.setValue(year);
			monthSpin.setValue(month);
			days.refreshDays(cal);
			recursionProtect = false;
		}
	}
	
	private static class DaysModel extends DefaultTableModel implements TableCellRenderer {
		private static final long 	serialVersionUID = 1L;
		private static final int	ROWS = 6;
		private static final int	COLUMNS = 7;

		private final boolean[]		weekends = new boolean[COLUMNS];
		private final String[]		labels = new String[COLUMNS];
		private final String[]		days = new String[ROWS*COLUMNS];

		
		@Override
		public int getRowCount() {
			return ROWS;
		}

		@Override
		public int getColumnCount() {
			return COLUMNS;
		}


		@Override
		public String getColumnName(final int columnIndex) {
			return labels[columnIndex];
		}


		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}


		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return days[rowIndex*COLUMNS+columnIndex];
		}

		public void refreshDayNames(final Localizer localizer) throws LocalizationException {
			final String 	dayNames[] = localizer.getValue(DAY_NAMES).split("\\;");
			final Calendar	cal = Calendar.getInstance(localizer.currentLocale().getLocale());
	        
	        for (int index = 0; index < dayNames.length; index++) {
	        	final int	dayIndex = (8 - cal.getFirstDayOfWeek() + index) % 7;
	        	
	        	weekends[dayIndex] = dayNames[index].startsWith("!");
	        	labels[dayIndex] = dayNames[index].replace("!","");
	        }
	        fireTableStructureChanged();
		}		
		
		public void refreshDays(final Calendar cal) {
			final Calendar	tmp = (Calendar)cal.clone();
			
			Arrays.fill(days,"");
			tmp.set(Calendar.DAY_OF_MONTH,1);
			
			final int 	delta = (cal.getFirstDayOfWeek() + tmp.get(Calendar.DAY_OF_WEEK) + 5) % 7; 
			for (int index = 0, maxIndex = tmp.getActualMaximum(Calendar.DAY_OF_MONTH); index < maxIndex; index++) {
				days[index + delta] = String.valueOf(index+1);
			}
	        fireTableDataChanged();
		}

		@Override
		public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, int column) {
			final JLabel	label = new JLabel(value == null ? "" : value.toString());

			label.setForeground(weekends[column] ? Color.RED : Color.BLACK);
			if (hasFocus) {
				label.setBorder(new LineBorder(Color.BLUE));
			}
			label.setOpaque(true);
			label.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
			return label;
		}
	}

	private void callLoad(final JComponentMonitor monitor) {
		try{monitor.process(MonitorEvent.Loading,metadata,this);
			currentValue = newValue;
			refreshView();
		} catch (ContentException exc) {
		}					
	}
}
