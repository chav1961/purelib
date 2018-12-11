package chav1961.purelib.ui.swing;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SpringLayout;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import javax.swing.text.BadLocationException;

import chav1961.purelib.basic.FSM;
import chav1961.purelib.basic.FSM.FSMLine;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.enumerations.MarkupOutputFormat;
import chav1961.purelib.i18n.PureLibLocalizer;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.streams.char2char.CreoleWriter;
import chav1961.purelib.ui.AbstractLowLevelFormFactory;
import chav1961.purelib.ui.UIUtils;
import chav1961.purelib.ui.interfacers.ControllerAction;
import chav1961.purelib.ui.interfacers.FormManager;
import chav1961.purelib.ui.interfacers.FormModel;
import chav1961.purelib.ui.interfacers.FormModelProcessor;
import chav1961.purelib.ui.interfacers.FormRepresentation;
import chav1961.purelib.ui.interfacers.RefreshMode;

public class SimpleLowLevelFormFactory<Id,Instance> extends AbstractLowLevelFormFactory<Id,Instance> {
	private static final String 	INSERT_ICON = "insert.png";
	private static final String 	INSERT_ICON_GRAY = "insertGray.png";
	private static final String		DUPLICATE_ICON = "duplicate.png";
	private static final String		DUPLICATE_ICON_GRAY = "duplicateGray.png";
	private static final String		DELETE_ICON = "delete.png";
	private static final String		DELETE_ICON_GRAY = "deleteGray.png";
	private static final String		REFRESH_ICON = "refresh.png";
	private static final String		REFRESH_ICON_GRAY = "refreshGray.png";
	private static final String		COMMIT_ICON = "commit.png";
	private static final String		COMMIT_ICON_GRAY = "commitGray.png";
	private static final String		ROLLBACK_ICON = "rollback.png";
	private static final String		ROLLBACK_ICON_GRAY = "rollbackGray.png";
	private static final String		FILTER_ICON = "filter.png";
	private static final String		FILTER_ICON_GRAY = "filterGray.png";
	private static final String		FILTER_ICON_ON = "filterOn.png";
	private static final String		FILTER_ICON_ON_GRAY = "filterOnGray.png";
	private static final String		ORDER_ICON = "order.png";
	private static final String		ORDER_ICON_GRAY = "orderGray.png";
	private static final String		ORDER_ICON_ON = "orderOn.png";
	private static final String		ORDER_ICON_ON_GRAY = "orderOnGray.png";
	
	private static final String		MSG_INFO_FORMAT = "<html><body><font color=black>%1$s</font></body></html>";
	private static final String		MSG_WARNING_FORMAT = "<html><body><font color=red>%1$s</font></body></html>";
	private static final String		MSG_ERROR_FORMAT = "<html><body><font color=red><b>%1$s</b></font></body></html>";
	private static final String		MSG_SEVERE_FORMAT = "<html><body><font color=red><b><i>%1$s</i></b></font></body></html>";

	private static final FSMLine<FormTerminals,FormState,FormActions>[]	SINGLE_RECORD_TABLE = new FSMLine[]{
		new FSMLine<>(FormState.INITIAL,FormTerminals.NONE,FormState.INITIAL)
	};
	
	private enum FormTerminals {
		NONE
	}

	private enum FormState {
		INITIAL
	}

	private enum FormActions {
		
	}
	
	private final FSM<FormTerminals,FormState,FormActions,Integer>	formFsm;
	private final Localizer											localizer;
	
	public SimpleLowLevelFormFactory(final URI formDescription, final FormRepresentation representation, final Class<Instance> rootClass, final FormManager<Id, Instance> manager, final Localizer localizer) throws IOException, SyntaxException {
		super(formDescription, representation, rootClass, manager);
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else {
			this.localizer = localizer;
			switch (representation) {
				case SINGLE_RECORD					:
					formFsm = new FSM<>((fsm,terminal,fromState,toState,action,parameter)->{processAction(fsm,terminal,fromState,toState,action,parameter);},FormState.INITIAL,SINGLE_RECORD_TABLE);
					break;
				case SINGLE_TABBED_RECORD			:
					formFsm = new FSM<>((fsm,terminal,fromState,toState,action,parameter)->{processAction(fsm,terminal,fromState,toState,action,parameter);},FormState.INITIAL,SINGLE_RECORD_TABLE);
					break;
				case LIST							:
					formFsm = new FSM<>((fsm,terminal,fromState,toState,action,parameter)->{processAction(fsm,terminal,fromState,toState,action,parameter);},FormState.INITIAL,SINGLE_RECORD_TABLE);
					break;
				case LIST_AND_SINGLE_RECORD			:
					formFsm = new FSM<>((fsm,terminal,fromState,toState,action,parameter)->{processAction(fsm,terminal,fromState,toState,action,parameter);},FormState.INITIAL,SINGLE_RECORD_TABLE);
					break;
				case LIST_AND_SINGLE_TABBED_RECORD	:
					formFsm = new FSM<>((fsm,terminal,fromState,toState,action,parameter)->{processAction(fsm,terminal,fromState,toState,action,parameter);},FormState.INITIAL,SINGLE_RECORD_TABLE);
					break;
				default : throw new UnsupportedOperationException("Form representation ["+representation+"] is not supported yet");
			}
		}
	}
	
	public SimpleLowLevelFormFactory(final URI formDescription, final URI[] inherited, final FormRepresentation representation, final Class<Instance> rootClass, final FormManager<Id, Instance> manager, final Localizer localizer) throws IOException, SyntaxException {
		super(formDescription, inherited, representation, rootClass, manager);
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else {
			this.localizer = localizer;
			switch (representation) {
				case SINGLE_RECORD					:
					formFsm = new FSM<>((fsm,terminal,fromState,toState,action,parameter)->{processAction(fsm,terminal,fromState,toState,action,parameter);},FormState.INITIAL,SINGLE_RECORD_TABLE);
					break;
				case SINGLE_TABBED_RECORD			:
					formFsm = new FSM<>((fsm,terminal,fromState,toState,action,parameter)->{processAction(fsm,terminal,fromState,toState,action,parameter);},FormState.INITIAL,SINGLE_RECORD_TABLE);
					break;
				case LIST							:
					formFsm = new FSM<>((fsm,terminal,fromState,toState,action,parameter)->{processAction(fsm,terminal,fromState,toState,action,parameter);},FormState.INITIAL,SINGLE_RECORD_TABLE);
					break;
				case LIST_AND_SINGLE_RECORD			:
					formFsm = new FSM<>((fsm,terminal,fromState,toState,action,parameter)->{processAction(fsm,terminal,fromState,toState,action,parameter);},FormState.INITIAL,SINGLE_RECORD_TABLE);
					break;
				case LIST_AND_SINGLE_TABBED_RECORD	:
					formFsm = new FSM<>((fsm,terminal,fromState,toState,action,parameter)->{processAction(fsm,terminal,fromState,toState,action,parameter);},FormState.INITIAL,SINGLE_RECORD_TABLE);
					break;
				default : throw new UnsupportedOperationException("Form representation ["+representation+"] is not supported yet");
			}
		}
	}

	public JComponent prepareComponent(final FormModel<Id,Instance> model,final boolean readOnly) throws NullPointerException, ContentException, LocalizationException {
		if (model == null) {
			throw new NullPointerException("Model reference can't be null");
		}
		else {
			switch (representation) {
				case SINGLE_RECORD					: return prepareSingleRecord(model,readOnly);
				case SINGLE_TABBED_RECORD			: return prepareSingleTabbedRecord(model,readOnly);
				case LIST							: return prepareList(model,readOnly,true,true);
				case LIST_AND_SINGLE_RECORD			: return prepareListAndSingleRecord(model,readOnly);
				case LIST_AND_SINGLE_TABBED_RECORD	: return prepareListAndSingleTabbedRecord(model,readOnly);
				default : throw new UnsupportedOperationException("Form representation ["+representation+"] is not supported yet");
			}
		}
	}

	private JRecordPanel<Id,Instance> prepareSingleRecord(final FormModel<Id,Instance> model,final boolean readOnly) throws ContentException, LocalizationException {
		final JRecordPanel<Id,Instance>		result = new JRecordPanel<>(localizer,model,readOnly);

		result.setContent(new PageContainer<Id,Instance>(PageContainer.ContainerType.CARDS_BASED,localizer,pages,fieldNames,formManager,result,model));
		return result;
	}
	
	private JRecordPanel<Id,Instance> prepareSingleTabbedRecord(final FormModel<Id,Instance> model,final boolean readOnly) throws ContentException, LocalizationException {
		final JRecordPanel<Id,Instance>		result = new JRecordPanel<>(localizer,model,readOnly);
		
		result.setContent(new PageContainer<Id,Instance>(PageContainer.ContainerType.TABS_BASED,localizer,pages,fieldNames,formManager,result,model));
		return result;
	}
	
	private JListPanel<Id,Instance> prepareList(final FormModel<Id,Instance> model,final boolean readOnly, final boolean canEditCell, final boolean addCommit) throws ContentException, LocalizationException {
		final JListPanel<Id,Instance>	result = new JListPanel<>(localizer,model,extract4List(fieldNames),readOnly,canEditCell,addCommit);
		final JTable					table = new LocalizedTable(localizer,result,extract4List(fieldNames),readOnly,canEditCell);
		final JScrollPane				scroll = new JScrollPane(table);

		result.add(scroll,BorderLayout.CENTER);
		return result;
	}

	private JComponent prepareListAndSingleRecord(final FormModel<Id,Instance> model,final boolean readOnly) throws ContentException, LocalizationException {
		return new JCompoundPanel<Id,Instance>(localizer,prepareList(model,readOnly,false,false),prepareSingleRecord(model,readOnly));
	}
	
	private JComponent prepareListAndSingleTabbedRecord(final FormModel<Id,Instance> model,final boolean readOnly) throws ContentException, LocalizationException {
		return new JCompoundPanel<Id,Instance>(localizer,prepareList(model,readOnly,false,false),prepareSingleTabbedRecord(model,readOnly));
	}


	private FieldDescriptor[] extract4List(final FieldDescriptor[] source) throws ContentException {
		int	count = 0;
		
		for (FieldDescriptor item :source) {
			if (item.fieldFormat.isUsedInList() || item.fieldFormat.isUsedInListAnchored()) {
				count++;
			}
		}
		if (count == 0) {
			throw new ContentException("Form descriptor doesn't contain any field with 'l' or 'L' format. List can't be built");
		}
		else {
			final FieldDescriptor[] 	result = new FieldDescriptor[count];
			
			count = 0;
			for (FieldDescriptor item :source) {
				if (item.fieldFormat.isUsedInList() || item.fieldFormat.isUsedInListAnchored()) {
					result[count++] = item;
				}
			}
			return result;
		}
	}
	
	private void processAction(final FSM<FormTerminals, FormState, FormActions, Integer> fsm, final FormTerminals terminal, final FormState fromState, final FormState toState, final FormActions[] action, final Integer parameter) {
	}

	private static URL url(final String resource) {
		return SimpleLowLevelFormFactory.class.getResource(resource);	
	}
	
	static class PageContainer<Id,Instance> extends JPanel {
		private static final long 	serialVersionUID = -7458988617844120563L;

		enum ContainerType {
			CARDS_BASED, TABS_BASED
		}
		
		private final ContainerType	type;
		private final JPanel		center;
		private final JTabbedPane	pane;
		
		PageContainer(final ContainerType type, final Localizer localizer, final FormPage[] pages, final FieldDescriptor[] fields, final FormManager<Id,Instance> mgr, final FormModelProcessor<Id,Instance> processor, final FormModel<Id,Instance> model) throws ContentException {
			setLayout(new BorderLayout(2,2));
			
			switch(this.type = type) {
				case CARDS_BASED	:
					setLayout(new CardLayout(2,2));
					add(center = new JPanel(new CardLayout()),BorderLayout.CENTER);
					pane = null;
					
					int	index = 1;
					for (FormPage page : pages) {
						final JComponent	control = preparePage(page,localizer,fields,mgr,processor,model);
						
						if (page.helpId != null && !page.helpId.isEmpty()) {
							SwingUtils.assignHelpKey(control,localizer,page.helpId);
						}
						center.add(control,String.valueOf(index++));
					}
					break;
				case TABS_BASED		:
					add(pane = new JTabbedPane(),BorderLayout.CENTER);
					center = null;

					for (FormPage page : pages) {
						final JComponent	control = preparePage(page,localizer,fields,mgr,processor,model);
						
						if (page.helpId != null && !page.helpId.isEmpty()) {
							SwingUtils.assignHelpKey(control,localizer,page.helpId);
						}
						if (page.iconId != null) {
							pane.addTab(page.captionId,control);
						}
						else {
							pane.addTab(page.captionId,control);
						}
					}
					break;
				default : throw new UnsupportedOperationException("Container type ["+type+"] is not supported yet");
			}
		}

		private JComponent preparePage(final FormPage page, final Localizer localizer, final FieldDescriptor[] fields, final FormManager<Id,Instance> mgr, final FormModelProcessor<Id,Instance> processor, final FormModel<Id,Instance> model) throws ContentException {
			try{final JEditorPane	pane = new JEditorPane("text/html",UIUtils.cre2Html(page.content));
				
				pane.setEditable(false);
				
				final StringBuilder	sb = new StringBuilder(pane.getText(0,pane.getDocument().getLength()));
				final int[]			fieldPositions = new int[page.fieldNames.size()]; 
						
				int		start = 0, current, fieldIndex = 0;
				
				while ((current = sb.indexOf(FIELD_MARK_STR,start)) >= 0) {
					fieldPositions[fieldIndex++] = current;
					start = current + 1;
				}
				
				pane.setLayout(new CreoleBasedLayoutManager(pane));
				for (int index = 0; index < fieldPositions.length; index++) {
					for (FieldDescriptor item : fields) {
						if (item.field.getName().equals(page.fieldNames.get(index).name)) {
							final JComponent	input = SwingUtils.prepareInputComponent(item.fieldRepresentation,item.field.getName(),item.fieldTooltip,item.fieldLen,item.fieldFormat);
							
							assignListeners(item,input,localizer,mgr,processor,model);
							pane.add(input,new int[] {fieldPositions[index],fieldPositions[index]+item.fieldLen+1});
							input.setPreferredSize(new Dimension(page.fieldNames.get(index).format.getLen()*10,20));
						}
					}
				}
				return pane;
			} catch (IOException | BadLocationException | ParseException e) {
				throw new ContentException("Error on ["+page+"]: "+e.getLocalizedMessage(),e);
			}
		}

		private void assignListeners(final FieldDescriptor item, final JComponent component, final Localizer localizer, final FormManager<Id,Instance> mgr, final FormModelProcessor<Id,Instance> processor, final FormModel<Id,Instance> model) {
			final RefreshMode[]	refresh = new RefreshMode[1];
			final Object[]		value = new Object[1];
			
			component.getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(SwingUtils.KS_EXIT,SwingUtils.ACTION_EXIT);
			component.getActionMap().put(SwingUtils.ACTION_EXIT,new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try{processor.processAction(model,ControllerAction.EXIT);
					} catch (ContentException exc) {
						processor.message(Severity.warning,exc.getLocalizedMessage());
					}
				}
			});
			component.addFocusListener(new FocusListener() {
				@Override 
				public void focusLost(final FocusEvent e) {
					if (refresh[0] != null) {
						switch (refresh[0]) {
							case NONE			:
							case FIELD_ONLY		:
								try{item.field.set(model.getInstance(model.getCurrentId()),SwingUtils.getValueFromComponent(component));
								} catch (IllegalArgumentException | IllegalAccessException | ContentException exc) {
								}
								break;
							case RECORD_ONLY	:
								break;
							case REJECT			:
								SwingUtils.assignValueToComponent(component,value[0]);
								break;
							case TOTAL			:
								break;
							default:
								break;
						}
					}
				}
				
				@Override
				public void focusGained(final FocusEvent e) {
					refresh[0] = null;
					value[0] = SwingUtils.getValueFromComponent(component);
					if (item.fieldTooltip != null && !item.fieldTooltip.isEmpty()) {
						try{processor.message(Severity.info,localizer.getValue(item.fieldTooltip));
						} catch (LocalizationException | IllegalArgumentException exc) {
						}
					}
				}
			});
			
			component.setInputVerifier(new InputVerifier() {
				@Override
				public boolean verify(final JComponent input) {
					try{return ((refresh[0] = mgr.onField(model.getInstance(model.getCurrentId()),model.getCurrentId(),item.field.getName(),value[0]))) != RefreshMode.REJECT;
					} catch (FlowException | ContentException | LocalizationException e) {
						processor.message(Severity.warning,e.getLocalizedMessage());
						return false;
					}
				}
			});
		}
	}
	
	static class JRecordPanel<Id,Instance> extends JPanel implements LocaleChangeListener, FormModelProcessor<Id,Instance> {
		private static final long serialVersionUID = -7885687891648875337L;

		private final Localizer 				localizer; 
		private final FormModel<Id,Instance>	model;
		private final JRecordState				state = new JRecordState();
		private PageContainer<Id,Instance>		content = null;
		
		JRecordPanel(final Localizer localizer, final FormModel<Id,Instance> model, final boolean readOnly) throws LocalizationException {
			super(new BorderLayout(1,1));
			final JToolBar						toolBar = new JRecordToolBar(localizer,readOnly,model,this);
			
			this.localizer = localizer;
			this.model = model;
			add(toolBar,BorderLayout.NORTH);
			add(state,BorderLayout.SOUTH);
			
			this.getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(SwingUtils.KS_EXIT,SwingUtils.ACTION_EXIT);
			this.getActionMap().put(SwingUtils.ACTION_EXIT,new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try{processAction(model,ControllerAction.EXIT);
					} catch (ContentException exc) {
						message(Severity.error,exc.getLocalizedMessage());
					}
				}
			});
		}

		@Override
		public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
			for (Component item : getComponents()) {
				SwingUtils.refreshLocale(item,oldLocale,newLocale);
			}
		}

		@Override
		public void processAction(final FormModel<Id, Instance> model, final ControllerAction action, final Object... parameters) throws ContentException {
			
		}

		@Override
		public void message(final Severity severity, final String message, final Object... parameters) {
			message(0,severity,message,parameters);
		}

		@Override
		public void message(final int cell, final Severity severity, final String message, final Object... parameters) {
			if (cell < 0 || cell > 1) {
				throw new IllegalArgumentException("Cell number ["+cell+"] out of range 0..1");
			}
			else if (severity == null) {
				throw new NullPointerException("Message severity can't be null!");
			}
			else if (message == null) {
				throw new NullPointerException("Message string can't be null");
			}
			else {
				String	msg = parameters == null || parameters.length == 0 ? message : String.format(message,parameters); 
						
				switch (cell) {
					case 0 :
						switch (severity) {
							case trace : case debug	: break;
							case info		: msg = String.format(MSG_INFO_FORMAT,msg); break;
							case warning	: msg = String.format(MSG_WARNING_FORMAT,msg); break;
							case error		: msg = String.format(MSG_ERROR_FORMAT,msg); break;
							case severe		: msg = String.format(MSG_SEVERE_FORMAT,msg); break;
							default : throw new UnsupportedOperationException("Severity level ["+severity+"] is not supported yet");
						}
						state.message.setText(msg);
						break;
					case 1 :
						state.page.setText(msg);
						break;
					default :
				}
			}
		}

		void setContent(final PageContainer<Id,Instance> content) {
			add(this.content = content,BorderLayout.CENTER);
		}
		
		private class JRecordToolBar extends LocalizedToolBar {
			private static final long serialVersionUID = -2093323387506857589L;

			JRecordToolBar(final Localizer localizer,final boolean readOnly, final FormModel<Id,Instance> model, final FormModelProcessor<Id,Instance> inst) throws LocalizationException{
				super(localizer);
				
				add(createButton(new ComplexCallAction<>(ControllerAction.REFRESH,model,inst),url(REFRESH_ICON_GRAY),url(REFRESH_ICON),PureLibLocalizer.TOOLBAR_REFRESH),PureLibLocalizer.TOOLBAR_REFRESH);
				if (!readOnly) {
					add(createButton(new ComplexCallAction<>(ControllerAction.COMMIT,model,inst),url(COMMIT_ICON_GRAY),url(COMMIT_ICON),PureLibLocalizer.TOOLBAR_COMMIT),PureLibLocalizer.TOOLBAR_COMMIT);
					add(createButton(new ComplexCallAction<>(ControllerAction.ROLLBACK,model,inst),url(ROLLBACK_ICON_GRAY),url(ROLLBACK_ICON),PureLibLocalizer.TOOLBAR_ROLLBACK),PureLibLocalizer.TOOLBAR_ROLLBACK);
				}
				
				setMinimumSize(new Dimension(30,30));
				setPreferredSize(new Dimension(100,30));
				setMaximumSize(new Dimension(2048,30));
			}
		}
		
		private static class JRecordState extends JPanel {
			private static final long 	serialVersionUID = -1092032105395729892L;
			
			final JLabel		page = new JLabel("");
			final JLabel		message = new JLabel();
			
			JRecordState() {
				final SpringLayout	layout = new SpringLayout();
				
				setLayout(layout);
			
				message.setMinimumSize(new Dimension(100,20));
				message.setPreferredSize(new Dimension(100,20));
				message.setMaximumSize(new Dimension(2048,20));
				
				add(message);

				page.setMinimumSize(new Dimension(40,20));
				page.setPreferredSize(new Dimension(80,20));
				page.setMaximumSize(new Dimension(100,20));
				page.setBorder(new LineBorder(Color.BLACK));
				
				add(page);
				
				layout.putConstraint(SpringLayout.EAST,page,-2,SpringLayout.EAST,this);
				layout.putConstraint(SpringLayout.EAST,message,-2,SpringLayout.WEST,page);
				
				setBorder(new EtchedBorder(EtchedBorder.LOWERED));
				setMinimumSize(new Dimension(25,25));
				setPreferredSize(new Dimension(100,25));
				setMaximumSize(new Dimension(2048,25));
			}
		}
	}

	static class JListPanel<Id,Instance> extends JPanel implements TableModel, LocaleChangeListener, FormModelProcessor<Id,Instance> {
		private static final long serialVersionUID = -1359578343853287475L;
		
		private final Localizer 				localizer;
		private final FieldDescriptor[] 		columns;
		private final List<TableModelListener>	listeners = new ArrayList<>();
		private final FormModel<Id,Instance>	model;
		private final boolean 					readOnly, canEditCell;
		private final JListState				state = new JListState();
		private final JListToolBar				toolBar;
		
		JListPanel(final Localizer localizer, final FormModel<Id,Instance> model, final FieldDescriptor[] columns, final boolean readOnly, final boolean canEditCell, final boolean addCommit) throws LocalizationException {
			super(new BorderLayout(1,1));
			toolBar = new JListToolBar(localizer,readOnly,addCommit,model,this);
			
			this.localizer = localizer;
			this.columns = columns;
			this.model = model;
			this.readOnly = readOnly;
			this.canEditCell = canEditCell;
			add(toolBar,BorderLayout.NORTH);
			add(state,BorderLayout.SOUTH);
			
			this.getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(SwingUtils.KS_EXIT,SwingUtils.ACTION_EXIT);
			this.getActionMap().put(SwingUtils.ACTION_EXIT,new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try{processAction(model,ControllerAction.EXIT);
					} catch (ContentException exc) {
						message(Severity.error,exc.getLocalizedMessage());
					}
				}
			});
		}

		@Override
		public int getRowCount() {
			return model.size();
		}

		@Override
		public int getColumnCount() {
			return columns.length;
		}

		@Override
		public String getColumnName(final int columnIndex) {
			try{return columns[columnIndex].extractLocalizedFieldName(localizer);
			} catch (LocalizationException  e) {
				return columns[columnIndex].field.getName();
			}
		}

		@Override
		public Class<?> getColumnClass(final int columnIndex) {
			return columns[columnIndex].field.getType();
		}

		@Override
		public boolean isCellEditable(final int rowIndex, final int columnIndex) {
			return !readOnly && canEditCell;
		}

		@Override
		public Object getValueAt(final int rowIndex, final int columnIndex) {
			try{final Instance	inst = model.getInstance(model.getIdByIndex(rowIndex));
				
				columns[columnIndex].field.setAccessible(true);
				return inst == null ? new Object() : columns[columnIndex].field.get(inst);
			} catch (ContentException | IllegalArgumentException | IllegalAccessException e) {
				message(Severity.warning,e.getLocalizedMessage());
				return new Object();
			}
		}

		@Override
		public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
			try{final Instance	inst = model.getInstance(model.getIdByIndex(rowIndex));
				columns[columnIndex].field.set(inst,aValue);
			} catch (ContentException | IllegalArgumentException | IllegalAccessException e) {
			}
		}

		@Override
		public void addTableModelListener(final TableModelListener l) {
			listeners.add(l);
		}

		@Override
		public void removeTableModelListener(final TableModelListener l) {
			listeners.remove(l);
		}

		@Override
		public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
			for (Component item : getComponents()) {
				SwingUtils.refreshLocale(item,oldLocale,newLocale);
			}
		}

		@Override
		public void processAction(final FormModel<Id, Instance> model, final ControllerAction action, final Object... parameters) throws ContentException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void message(final Severity severity, final String message, final Object... parameters) {
			message(0,severity,message,parameters);
		}

		@Override
		public void message(final int cell, final Severity severity, final String message, final Object... parameters) {
			if (cell < 0 || cell > 1) {
				throw new IllegalArgumentException("Cell number ["+cell+"] out of range 0..1");
			}
			else if (severity == null) {
				throw new NullPointerException("Message severity can't be null!");
			}
			else if (message == null) {
				throw new NullPointerException("Message string can't be null");
			}
			else {
				String	msg = parameters == null || parameters.length == 0 ? message : String.format(message,parameters); 
						
				switch (cell) {
					case 0 :
						switch (severity) {
							case trace : case debug	: break;
							case info		: msg = String.format(MSG_INFO_FORMAT,msg); break;
							case warning	: msg = String.format(MSG_WARNING_FORMAT,msg); break;
							case error		: msg = String.format(MSG_ERROR_FORMAT,msg); break;
							case severe		: msg = String.format(MSG_SEVERE_FORMAT,msg); break;
							default : throw new UnsupportedOperationException("Severity level ["+severity+"] is not supported yet");
						}
						state.message.setText(msg);
						state.message.setToolTipText(msg);
						break;
					case 1 :
						state.xyPlace.setText(msg);
						state.xyPlace.setToolTipText(msg);
						break;
					default :
				}
			}
		}

		private class JListToolBar extends LocalizedToolBar {
			private static final long 	serialVersionUID = -6756190735378628489L;
			
			JListToolBar(final Localizer localizer, final boolean readOnly, final boolean addCommit, final FormModel<Id,Instance> model, final FormModelProcessor<Id,Instance> inst) throws LocalizationException {
				super(localizer);
				if (!readOnly) {
					add(createButton(new ComplexCallAction<>(ControllerAction.ACTION_INSERT,model,inst),url(INSERT_ICON_GRAY),url(INSERT_ICON),PureLibLocalizer.TOOLBAR_INSERT),PureLibLocalizer.TOOLBAR_INSERT);
					add(createButton(new ComplexCallAction<>(ControllerAction.ACTION_DUPLICATE,model,inst),url(DUPLICATE_ICON_GRAY),url(DUPLICATE_ICON),PureLibLocalizer.TOOLBAR_DUPLICATE),PureLibLocalizer.TOOLBAR_DUPLICATE);
					add(createButton(new ComplexCallAction<>(ControllerAction.ACTION_DELETE,model,inst),url(DELETE_ICON_GRAY),url(DELETE_ICON),PureLibLocalizer.TOOLBAR_DELETE),PureLibLocalizer.TOOLBAR_DELETE);
					addSeparator();
					add(createButton(new ComplexCallAction<>(ControllerAction.REFRESH,model,inst),url(REFRESH_ICON_GRAY),url(REFRESH_ICON),PureLibLocalizer.TOOLBAR_REFRESH),PureLibLocalizer.TOOLBAR_REFRESH);
					if (addCommit) {
						add(createButton(new ComplexCallAction<>(ControllerAction.COMMIT,model,inst),url(COMMIT_ICON_GRAY),url(COMMIT_ICON_GRAY),PureLibLocalizer.TOOLBAR_COMMIT),PureLibLocalizer.TOOLBAR_COMMIT);
						add(createButton(new ComplexCallAction<>(ControllerAction.ROLLBACK,model,inst),url(ROLLBACK_ICON_GRAY),url(ROLLBACK_ICON_GRAY),PureLibLocalizer.TOOLBAR_ROLLBACK),PureLibLocalizer.TOOLBAR_ROLLBACK);
					}
					addSeparator();
				}
				else {
					add(createButton(new ComplexCallAction<>(ControllerAction.REFRESH,model,inst),url(REFRESH_ICON_GRAY),url(REFRESH_ICON),PureLibLocalizer.TOOLBAR_REFRESH),PureLibLocalizer.TOOLBAR_REFRESH);
					addSeparator();
				}
				add(createButton(new ComplexCallAction<>(ControllerAction.FILTER_SET,model,inst),url(FILTER_ICON_GRAY),url(FILTER_ICON),url(FILTER_ICON_ON_GRAY),url(FILTER_ICON_ON),PureLibLocalizer.TOOLBAR_FILTER),PureLibLocalizer.TOOLBAR_FILTER);
				add(createButton(new ComplexCallAction<>(ControllerAction.ORDER_SET,model,inst),url(ORDER_ICON_GRAY),url(ORDER_ICON),url(ORDER_ICON_ON_GRAY),url(ORDER_ICON_ON),PureLibLocalizer.TOOLBAR_ORDER),PureLibLocalizer.TOOLBAR_ORDER);
				addSeparator();
				
				setMinimumSize(new Dimension(30,30));
				setPreferredSize(new Dimension(100,30));
				setMaximumSize(new Dimension(2048,30));
				setFloatable(false);
			}
		}

		private class JListState extends JPanel {
			private static final long 	serialVersionUID = -1092032105395729892L;
			private static final String	MESSAGE_ID = "MessageID";
			private static final String	PROGRESS_ID = "ProgressID";
			
			final JLabel		xyPlace = new JLabel();
			final JLabel		message = new JLabel();
			final JProgressBar	bar = new JProgressBar();
			final JPanel		cardPanel = new JPanel(new CardLayout(2,2));
			
			JListState() {
				final SpringLayout	layout = new SpringLayout();
				
				setLayout(layout);
			
				bar.setStringPainted(true);
				
				cardPanel.add(message,MESSAGE_ID);
				cardPanel.add(bar,PROGRESS_ID);
				cardPanel.setMinimumSize(new Dimension(100,20));
				cardPanel.setPreferredSize(new Dimension(100,20));
				cardPanel.setMaximumSize(new Dimension(2048,20));
				selectCardItem(MESSAGE_ID);

				add(cardPanel);

				xyPlace.setMinimumSize(new Dimension(40,20));
				xyPlace.setPreferredSize(new Dimension(80,20));
				xyPlace.setMaximumSize(new Dimension(100,20));
				xyPlace.setBorder(new LineBorder(Color.BLACK));
				
				add(xyPlace);
				
				layout.putConstraint(SpringLayout.EAST,xyPlace,-2,SpringLayout.EAST,this);
				layout.putConstraint(SpringLayout.EAST,cardPanel,-2,SpringLayout.WEST,xyPlace);
				layout.putConstraint(SpringLayout.WEST,cardPanel,2,SpringLayout.WEST,this);
				
				setBorder(new EtchedBorder(EtchedBorder.LOWERED));
				setMinimumSize(new Dimension(25,25));
				setPreferredSize(new Dimension(25,25));
			}
			
			public void showLine(final int x, final int y) {
				xyPlace.setText(String.format("%d of %d",x,y));
			}
			
			public void setMessage(final String msg, final Object... parameters) {
				selectCardItem(MESSAGE_ID);
				if (parameters == null || parameters.length == 0) {
					message.setText(msg);
				}
				else {
					message.setText(String.format(msg,parameters));
				}
			}
			
			public JProgressBar getProgressBar() {
				bar.setMinimum(0);
				bar.setMaximum(100);
				bar.setValue(0);
				selectCardItem(PROGRESS_ID);
				return bar;
			}
			
			private void selectCardItem(final String item) {
				((CardLayout)cardPanel.getLayout()).show(cardPanel,item);
			}
		}
	}

	static class JCompoundPanel<Id,Instance> extends JPanel implements LocaleChangeListener, FormModelProcessor<Id,Instance> {
		private static final long 				serialVersionUID = 3942110001365259731L;
		
		private final Localizer 				localizer;
		private final JListPanel<Id,Instance>	listPanel; 
		private final JRecordPanel<Id,Instance>	recordPanel; 
		
		JCompoundPanel(final Localizer localizer, final JListPanel<Id,Instance> listPanel, final JRecordPanel<Id,Instance> recordPanel) {
			super(new BorderLayout());
			final JSplitPane	splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			
			splitPane.setLeftComponent(recordPanel);
			splitPane.setRightComponent(listPanel);
			splitPane.setDividerLocation(0.5);
			add(splitPane,BorderLayout.CENTER);
			
			this.localizer = localizer;
			this.listPanel = listPanel;
			this.recordPanel = recordPanel;
		}

		@Override
		public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
			SwingUtils.refreshLocale(listPanel,oldLocale,newLocale);
			SwingUtils.refreshLocale(recordPanel,oldLocale,newLocale);
		}

		@Override
		public void processAction(final FormModel<Id, Instance> model, final ControllerAction action, final Object... parameters) throws ContentException {
			// TODO Auto-generated method stub
		}

		@Override
		public void message(final Severity severity, final String message, final Object... parameters) {
			message(0,severity,message,parameters);
		}

		@Override
		public void message(final int cell, final Severity severity, final String message, final Object... parameters) {
			// TODO Auto-generated method stub
		}
	}
}
