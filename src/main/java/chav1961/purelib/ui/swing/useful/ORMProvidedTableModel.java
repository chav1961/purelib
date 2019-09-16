package chav1961.purelib.ui.swing.useful;

import java.io.Flushable;
import java.io.IOException;
import java.sql.SQLException;
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
import chav1961.purelib.basic.exceptions.FlowException;
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
import chav1961.purelib.sql.SQLUtils;
import chav1961.purelib.sql.interfaces.ORMProvider;
import chav1961.purelib.ui.interfaces.ErrorProcessing;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.RecordFormManager;
import chav1961.purelib.ui.interfaces.RefreshMode;

public class ORMProvidedTableModel<Key,Record> extends DefaultTableModel implements LocaleChangeListener {
	private static final long 					serialVersionUID = 5818266099123995333L;
	private static final int					DEFAULT_PAGE_SIZE = 100;

	private final Localizer						localizer;
	private final LoggerFacade					logger;	
	private final ORMProvider<Key, Record>		provider;
	private final Record						instance;
	private final boolean						isReadOnly;
	private final FormManager<Key,Record> 		mgr;
	private final ColumnDescriptor[]			columnDesc;
	private final RecordCache<Record>			cache = null;

    private int									currentLine = -1, pageSize = DEFAULT_PAGE_SIZE;

	public ORMProvidedTableModel(final Localizer localizer, final ContentNodeMetadata clazzModel, final ORMProvider<Key,Record> provider, final Record instance, final String[] fields) {
		this(localizer,PureLibSettings.SYSTEM_ERR_LOGGER,clazzModel,provider,instance,fields);
	}
	
	public ORMProvidedTableModel(final Localizer localizer, final ContentNodeMetadata clazzModel, final ORMProvider<Key,Record> provider, final Record instance, final String[] fields, final FormManager<Key,Record> mgr) {
		this(localizer,PureLibSettings.SYSTEM_ERR_LOGGER,clazzModel,provider,instance,fields,mgr);
	}

	public ORMProvidedTableModel(final Localizer localizer, final LoggerFacade logger, final ContentNodeMetadata clazzModel, final ORMProvider<Key,Record> provider, final Record instance, final String[] fields) {
		this(localizer,logger,clazzModel,provider,instance,fields,true,new FormManager<Key,Record>() {
			@Override
			public RefreshMode onField(Record inst, Key id, String fieldName, Object oldValue) throws FlowException, LocalizationException {
				return RefreshMode.FIELD_ONLY;
			}

			@Override
			public LoggerFacade getLogger() {
				return logger;
			}
		});
	}	

	public ORMProvidedTableModel(final Localizer localizer, final LoggerFacade logger, final ContentNodeMetadata clazzModel, final ORMProvider<Key,Record> provider, final Record instance, final String[] fields, final FormManager<Key,Record> mgr) {
		this(localizer,logger,clazzModel,provider,instance,fields,false,mgr);
	}

	public <Err extends Enum<?>> ORMProvidedTableModel(final Localizer localizer, final LoggerFacade logger, final ContentNodeMetadata clazzModel, final ORMProvider<Key,Record> provider, final Record instance, final String[] fields, final FormManager<Key,Record> mgr, final ErrorProcessing<Record,Err> errProc) {
		this(localizer,logger,clazzModel,provider,instance,fields,false,mgr);
	}
	
	private <Err extends Enum<?>> ORMProvidedTableModel(final Localizer localizer, final LoggerFacade logger, final ContentNodeMetadata clazzModel, final ORMProvider<Key,Record> provider, final Record instance, final String[] fields, final boolean readOnly, final FormManager<Key,Record> mgr) {
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
							try{cols.add(new ColumnDescriptor(node.getName()
										,node.getLabelId(),node.getTooltipId(),node.getType()
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
	
	@SuppressWarnings("unchecked")
	@Override
	public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
		if (!isCellEditable(rowIndex,columnIndex)) {
			throw new IllegalStateException("Attempt to change value for read-only cell"); 
		}
		else {
			final GetterAndSetter	gas = columnDesc[columnIndex].gas;
			final Record			rec = cache.getRecord(rowIndex);
			final Object			oldVal = getValueAt(rowIndex,columnIndex);
			final RefreshMode		rm;
			
			try{setValueAt(instance,gas,aValue);
				try{switch (rm = mgr.onField(instance,provider.getKey(instance),columnDesc[columnIndex].fieldName,oldVal)) {
						case REJECT : case EXIT :
							setValueAt(instance,gas,oldVal);
							break;
						case DEFAULT : case FIELD_ONLY : case NONE :
							provider.update(provider.getKey(instance),instance);
							cache.markAsChanged(rowIndex);
							fireTableCellUpdated(rowIndex,columnIndex);
							break;
						case RECORD_ONLY :
							provider.update(provider.getKey(instance),instance);
							cache.markAsChanged(rowIndex);
							fireTableRowsUpdated(rowIndex,rowIndex);
							break;
						case TOTAL :
							provider.update(provider.getKey(instance),instance);
							cache.markAsChanged(rowIndex);
							fireTableDataChanged();
							break;
						default	: throw new UnsupportedOperationException("Refresh mode ["+rm+"] is not supported yet");
					}
				} catch (SQLException e) {
					mgr.getLogger().message(Severity.error,e,"Error setting cell: "+e.getLocalizedMessage());
				}
			} catch (ContentException | LocalizationException | FlowException e) {
				logger.message(Severity.error,e,"Error setting cell: "+e.getLocalizedMessage());
			}
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
	
	public void setPageSize(final int pageSize) {
		if (pageSize <= 0) {
			throw new IllegalArgumentException("Page size ["+pageSize+"] must be positive");
		}
		else {
			this.pageSize = pageSize;
		}
	}
	
	public void insert() {
		if (isReadOnly) {
			throw new IllegalStateException("Attach to call insert on read-only content");
		}
		else {
			try{
				try{final RefreshMode	rm;
					final Key 			newKey = (Key)provider.newKey();
			
					switch (rm = mgr.onRecord(RecordFormManager.Action.INSERT,null,null,instance,newKey)) {
						case REJECT : case EXIT :
							break;
						case DEFAULT : case FIELD_ONLY : case NONE : case RECORD_ONLY :
							provider.create(newKey,instance);
							reload();
							break;
						case TOTAL :
							provider.create(newKey,instance);
							reload();
							break;
						default	: throw new UnsupportedOperationException("Refresh mode ["+rm+"] is not supported yet");
					}
				} catch (SQLException e) {
					mgr.getLogger().message(Severity.error,e,"Error inserting record: "+e.getLocalizedMessage());
				}
			} catch (LocalizationException | FlowException e) {
				logger.message(Severity.error,e,"Error inserting record: "+e.getLocalizedMessage());
			}
		}
	}

	public void duplicate(int currentLine) {
		if (isReadOnly) {
			throw new IllegalStateException("Attach to call insert on read-only content");
		}
		else {
			try{
				try{final RefreshMode	rm;
					final Key 			newKey = (Key)provider.newKey();
			
					switch (rm = mgr.onRecord(RecordFormManager.Action.DUPLICATE,instance,provider.getKey(instance),instance,newKey)) {
						case REJECT : case EXIT :
							break;
						case DEFAULT : case FIELD_ONLY : case NONE : case RECORD_ONLY :
							provider.create(newKey,instance);
							reload();
							break;
						case TOTAL :
							provider.create(newKey,instance);
							reload();
							break;
						default	: throw new UnsupportedOperationException("Refresh mode ["+rm+"] is not supported yet");
					}
				} catch (SQLException e) {
					mgr.getLogger().message(Severity.error,e,"Error duplication record: "+e.getLocalizedMessage());
				}
			} catch (LocalizationException | FlowException e) {
				logger.message(Severity.error,e,"Error duplicating record: "+e.getLocalizedMessage());
			}
		}
	}

	public void delete(int currentLine) {
		if (isReadOnly) {
			throw new IllegalStateException("Attach to call delete on read-only content");
		}
		else {
			if (isReadOnly) {
				throw new IllegalStateException("Attach to call insert on read-only content");
			}
			else {
				try{
					try{final RefreshMode	rm;
				
						switch (rm = mgr.onRecord(RecordFormManager.Action.DELETE,instance,null,instance,null)) {
							case REJECT : case EXIT :
								break;
							case DEFAULT : case FIELD_ONLY : case NONE : case RECORD_ONLY :
								provider.delete(provider.getKey(instance));
								reload();
								break;
							case TOTAL :
								provider.delete(provider.getKey(instance));
								reload();
								break;
							default	: throw new UnsupportedOperationException("Refresh mode ["+rm+"] is not supported yet");
						}
					} catch (SQLException e) {
						mgr.getLogger().message(Severity.error,e,"Error deleting record: "+e.getLocalizedMessage());
					}
				} catch (LocalizationException | FlowException e) {
					logger.message(Severity.error,e,"Error duplicating record: "+e.getLocalizedMessage());
				}
			}
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

	private <T> T convert(final Object aValue, final Class<T> awaited) throws ContentException {
		return SQLUtils.convert(awaited,aValue);
	}

	private void setValueAt(final Record rec, final GetterAndSetter gas, final Object aValue) throws ContentException {
		if (gas instanceof BooleanGetterAndSetter) {
			((BooleanGetterAndSetter)gas).set(rec,convert(aValue,boolean.class));
		}
		else if (gas instanceof ByteGetterAndSetter) {
			((ByteGetterAndSetter)gas).set(rec,convert(aValue,byte.class));
		}
		else if (gas instanceof CharGetterAndSetter) {
			((CharGetterAndSetter)gas).set(rec,convert(aValue,char.class));
		}
		else if (gas instanceof DoubleGetterAndSetter) {
			((DoubleGetterAndSetter)gas).set(rec,convert(aValue,double.class));
		}
		else if (gas instanceof FloatGetterAndSetter) {
			((FloatGetterAndSetter)gas).set(rec,convert(aValue,float.class));
		}
		else if (gas instanceof IntGetterAndSetter) {
			((IntGetterAndSetter)gas).set(rec,convert(aValue,int.class));
		}
		else if (gas instanceof LongGetterAndSetter) {
			((LongGetterAndSetter)gas).set(rec,convert(aValue,long.class));
		}
		else if (gas instanceof ShortGetterAndSetter) {
			((ShortGetterAndSetter)gas).set(rec,convert(aValue,short.class));
		}
		else {
			((ObjectGetterAndSetter<Object>)gas).set(rec,aValue);
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
		final String			fieldName;
		final String			columnLabel;
		final String			columnTooltip;
		final Class<?>			columnClass;
		final GetterAndSetter	gas;
		
		private ColumnDescriptor(final String fieldName, final String columnLabel, final String columnTooltip, final Class<?> columnClass, final GetterAndSetter gas) {
			this.fieldName = fieldName;
			this.columnLabel = columnLabel;
			this.columnTooltip = columnTooltip;
			this.columnClass = columnClass;
			this.gas = gas;
		}

		@Override
		public String toString() {
			return "ColumnDescriptor [fieldName=" + fieldName + ", columnLabel=" + columnLabel + ", columnTooltip=" + columnTooltip + ", columnClass=" + columnClass + ", gas=" + gas + "]";
		}
	}
}
