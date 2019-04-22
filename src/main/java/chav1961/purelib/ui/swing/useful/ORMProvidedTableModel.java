package chav1961.purelib.ui.swing.useful;

import java.io.Flushable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.swing.table.DefaultTableModel;

import chav1961.purelib.basic.GettersAndSettersFactory;
import chav1961.purelib.basic.GettersAndSettersFactory.BooleanGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.ByteGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.CharGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.DoubleGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.FloatGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.GetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.IntGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.LongGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.ObjectGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.ShortGetterAndSetter;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.SimpleContentMetadata;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.sql.interfaces.ORMProvider;
import chav1961.purelib.ui.interfaces.FormManager;

public class ORMProvidedTableModel<Record> extends DefaultTableModel implements LocaleChangeListener {
	private static final long serialVersionUID = 5818266099123995333L;

	private final Localizer						localizer;
	private final LoggerFacade					logger;	
	private final ORMProvider<Record, Record>	provider;
	private final Record						instance;
	private final boolean						isReadOnly;
	private final FormManager<Object,Record> 	mgr;
	private final ColumnDescriptor[]			columnDesc;
	private final RecordCache<Record>			cache = null;
	private int									currentLine = -1;

	public ORMProvidedTableModel(final Localizer localizer, final ContentNodeMetadata clazzModel, final ORMProvider<Record,Record> provider, final Record instance, final String[] fields) {
		this(localizer,PureLibSettings.SYSTEM_ERR_LOGGER,clazzModel,provider,instance,fields);
	}
	
	public ORMProvidedTableModel(final Localizer localizer, final ContentNodeMetadata clazzModel, final ORMProvider<Record,Record> provider, final Record instance, final String[] fields, final FormManager<Object,Record> mgr) {
		this(localizer,PureLibSettings.SYSTEM_ERR_LOGGER,clazzModel,provider,instance,fields,mgr);
	}

	public ORMProvidedTableModel(final Localizer localizer, final LoggerFacade logger, final ContentNodeMetadata clazzModel, final ORMProvider<Record,Record> provider, final Record instance, final String[] fields) {
		this(localizer,logger,clazzModel,provider,instance,fields,true,null);
	}	

	public ORMProvidedTableModel(final Localizer localizer, final LoggerFacade logger, final ContentNodeMetadata clazzModel, final ORMProvider<Record,Record> provider, final Record instance, final String[] fields, final FormManager<Object,Record> mgr) {
		this(localizer,logger,clazzModel,provider,instance,fields,false,mgr);
	}
	
	private ORMProvidedTableModel(final Localizer localizer, final LoggerFacade logger, final ContentNodeMetadata clazzModel, final ORMProvider<Record,Record> provider, final Record instance, final String[] fields, final boolean readOnly, final FormManager<Object,Record> mgr) {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (logger == null) {
			throw new NullPointerException("Logger can't be null");
		}
		else if (clazzModel == null) {
			throw new NullPointerException("Class model can't be null");
		}
		else if (provider == null) {
			throw new NullPointerException("Provider can't be null");
		}
		else if (instance == null) {
			throw new NullPointerException("Record instance can't be null");
		}
		else if (fields == null || fields.length == 0) {
			throw new IllegalArgumentException("Field list can' be null or empty array");
		}
		else if (Utils.checkArrayContent4Nulls(fields) >= 0) {
			throw new IllegalArgumentException("Some fields in the field list contain null: "+Arrays.toString(fields));
		}
		else if (!readOnly && mgr == null) {
			throw new NullPointerException("Form manager can't be null");
		}
		else {
			this.localizer = localizer;
			this.logger = logger;
			this.provider = provider;
			this.instance = instance;
			this.isReadOnly = readOnly;
			this.mgr = mgr;
			
			final List<ColumnDescriptor>	cols = new ArrayList<>();
			final ContentMetadataInterface	metadata = new SimpleContentMetadata(clazzModel);
			
			for (String field : fields) {
				metadata.walkDown((mode,appPath,uiPath,node)->{
					if (mode == NodeEnterMode.ENTER && Utils.hasSubScheme(appPath,ContentModelFactory.APPLICATION_SCHEME_FIELD)) {
						if (node.getName().equalsIgnoreCase(field)) {
							try{cols.add(new ColumnDescriptor(node.getLabelId(),node.getTooltipId(),node.getType()
										,GettersAndSettersFactory.buildGetterAndSetter(appPath)));
							} catch (ContentException  e) {
								logger.message(Severity.error,e,"Error building getter/setter for ["+appPath+"]: "+e.getLocalizedMessage());
							}
						}
					}
					return ContinueMode.CONTINUE;
				}
				, clazzModel.getUIPath());
			}
			this.columnDesc = cols.toArray(new ColumnDescriptor[cols.size()]);
			this.cache.refresh();
		}
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fireTableStructureChanged();
	}

	@Override
	public Class<?> getColumnClass(final int columnIndex) {
		return columnDesc[columnIndex].columnClass;
	}
	
	@Override
	public int	getColumnCount() {
		return columnDesc.length;		
	}
	
	@Override
	public String getColumnName(final int columnIndex) {
		try{return localizer.getValue(columnDesc[columnIndex].columnLabel);
		} catch (LocalizationException e) {
			logger.message(Severity.error,e,"Error getting column name: "+e.getLocalizedMessage());
			return columnDesc[columnIndex].columnLabel;
		}
	}
	
	@Override
	public int getRowCount() {
		return cache.getSize();
	}	
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		final GetterAndSetter	gas = columnDesc[columnIndex].gas;
		final Record			rec = cache.getRecord(rowIndex);
		
		try{if (gas instanceof BooleanGetterAndSetter) {
				return Boolean.valueOf(((BooleanGetterAndSetter)gas).get(rec));
			}
			else if (gas instanceof ByteGetterAndSetter) {
				return Byte.valueOf(((ByteGetterAndSetter)gas).get(rec));
			}
			else if (gas instanceof CharGetterAndSetter) {
				return Character.valueOf(((CharGetterAndSetter)gas).get(rec));
			}
			else if (gas instanceof DoubleGetterAndSetter) {
				return Double.valueOf(((DoubleGetterAndSetter)gas).get(rec));
			}
			else if (gas instanceof FloatGetterAndSetter) {
				return Float.valueOf(((FloatGetterAndSetter)gas).get(rec));
			}
			else if (gas instanceof IntGetterAndSetter) {
				return Integer.valueOf(((IntGetterAndSetter)gas).get(rec));
			}
			else if (gas instanceof LongGetterAndSetter) {
				return Long.valueOf(((LongGetterAndSetter)gas).get(rec));
			}
			else if (gas instanceof ShortGetterAndSetter) {
				return Short.valueOf(((ShortGetterAndSetter)gas).get(rec));
			}
			else {
				return ((ObjectGetterAndSetter<?>)gas).get(rec);
			}
		} catch (ContentException e) {
			logger.message(Severity.error,e,"Error getting cell: "+e.getLocalizedMessage());
			return null;
		}
	}
	
	@Override
	public boolean isCellEditable(final int rowIndex, final int columnIndex) {
		return isReadOnly;
	}
	
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (!isCellEditable(rowIndex,columnIndex)) {
			throw new IllegalStateException("Attempt to change value for read-only cell"); 
		}
		else {
			// TODO:
		}
	}
	
	public void setFilter(final String filter) {
		
	}

	public void setOrdering(final String ordering) {
		
	}

	public int getCurrentLine() {
		return currentLine;
	}

	public void setCurrentLine(final int currentRecord) {
		currentLine = currentRecord;
	}
	
	public Record getCurrentRecord() {
		if (getCurrentLine() < 0 || getCurrentLine() >= cache.getSize()) {
			throw new IllegalStateException("Current record ["+getCurrentLine()+"] out of content range 0.."+(cache.getSize()-1));
		}
		else {
			return cache.getRecord(getCurrentLine());
		}
	}

	public Record getRecord(int rowIndex) {
		if (rowIndex < 0 || rowIndex >= cache.getSize()) {
			throw new IllegalStateException("Row index ["+rowIndex+"] out of content range 0.."+(cache.getSize()-1));
		}
		else {
			return cache.getRecord(getCurrentLine());
		}
	}
	
	public void setPageSize(int pageSize) {
		
	}
	
	public void insert() {
		if (isReadOnly) {
			throw new IllegalStateException("Attach to call insert on read-only content");
		}
		else {
			// TODO:
		}
	}

	public void duplicate(int currentLine) {
		if (isReadOnly) {
			throw new IllegalStateException("Attach to call duplicate on read-only content");
		}
		else {
			// TODO:
		}
	}

	public void delete(int currentLine) {
		if (isReadOnly) {
			throw new IllegalStateException("Attach to call delete on read-only content");
		}
		else {
			// TODO:
		}
	}
	
	public void reload() {
		try{cache.flush();
			cache.refresh();
			fireTableDataChanged();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static class RecordCache<Record> implements Flushable {
		@Override
		public void flush() throws IOException {
			// TODO Auto-generated method stub
			
		}
		
		public int getSize() {
			return 0;
		}
		
		public Record getRecord(int rowNumber) {
			return null;
		}
		
		public void setCurrentRow(int rowNumber) {
			
		}
		
		public void markAsChanged(int rowNumber) {
			
		}
		
		public void refresh() {
			
		}

	}
	
	private static class ColumnDescriptor {
		final String			columnLabel;
		final String			columnTooltip;
		final Class<?>			columnClass;
		final GetterAndSetter	gas;
		
		private ColumnDescriptor(final String columnLabel, final String columnTooltip, final Class<?> columnClass, final GetterAndSetter gas) {
			this.columnLabel = columnLabel;
			this.columnTooltip = columnTooltip;
			this.columnClass = columnClass;
			this.gas = gas;
		}

		@Override
		public String toString() {
			return "ColumnDescriptor [columnLabel=" + columnLabel + ", columnTooltip=" + columnTooltip + ", columnClass=" + columnClass + "]";
		}
	}
}
