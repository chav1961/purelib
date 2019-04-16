package chav1961.purelib.ui.swing.useful;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import chav1961.purelib.basic.GettersAndSettersFactory;
import chav1961.purelib.basic.GettersAndSettersFactory.GetterAndSetter;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.ModelUtils;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;

public class J2ColumnEditor extends JPanel implements LocaleChangeListener, JComponentMonitor {
	private static final long 				serialVersionUID = 2981133342567260172L;
	private static final int				GAP_SIZE = 5; 

	private final Object					content;
	private final J2ColumnEditorCallback	callback;
	private final Localizer					localizer;
	private final Set<String>				labelIds = new HashSet<>(), modifiableLabelIds = new HashSet<>();
	private final Map<String,GetterAndSetter>	accessors = new HashMap<>();	
	final ContentMetadataInterface			mdi;
	
	@FunctionalInterface
	public interface J2ColumnEditorCallback {
		void process(String actionCommand);
	}	
	
	public J2ColumnEditor(final Object content) throws SyntaxException, LocalizationException, NullPointerException, PreparationException, IllegalArgumentException, ContentException, IOException {
		this(content,1);
	}

	public J2ColumnEditor(final Object content, final J2ColumnEditorCallback callback) throws SyntaxException, LocalizationException, NullPointerException, PreparationException, IllegalArgumentException, ContentException, IOException {
		this(content,1,callback);
	}
	
	public J2ColumnEditor(final Object content, final int numberOfBars) throws SyntaxException, LocalizationException, NullPointerException, PreparationException, IllegalArgumentException, ContentException, IOException {
		this(content,numberOfBars,(c)->{});
	}
	
	public J2ColumnEditor(final Object content, final int numberOfBars, final J2ColumnEditorCallback callback) throws SyntaxException, LocalizationException, NullPointerException, PreparationException, IllegalArgumentException, ContentException, IOException {
		super(new LabelledLayout(numberOfBars, GAP_SIZE, GAP_SIZE, LabelledLayout.VERTICAL_FILLING));
		
		if (content == null) {
			throw new NullPointerException("Content to show can't be null"); 
		}
		else if (callback == null) {
			throw new NullPointerException("Editor callback can't be null"); 
		}
		else {
			final Class<?>	instanceClass = content.getClass();
			
			this.content = content;
			this.callback = callback;
			this.mdi = ContentModelFactory.forAnnotatedClass(instanceClass);
			this.localizer = LocalizerFactory.getLocalizer(mdi.getRoot().getLocalizerAssociated());

			final List<JButton>	actions = new ArrayList<>();
			final JComponent[]	lastComponent = new JComponent[] {null};
			
			mdi.walkDown((mode,applicationPath,uiPath,node)->{
				if (node.getApplicationPath() != null){ 
					if (node.getApplicationPath().toString().contains(ContentMetadataInterface.APPLICATION_SCHEME+":"+ContentModelFactory.APPLICATION_SCHEME_FIELD)) {
						switch (mode) {
							case ENTER	:
								try{final JLabel		label = new JLabel();
									final FieldFormat	ff = node.getFormatAssociated();
									final JComponent 	field = SwingUtils.prepareRenderer(node, ff, this);
								
									label.setName(Utils.removeQueryFromURI(node.getUIPath()).toString()+"/label");
									add(label,LabelledLayout.LABEL_AREA);
									field.setName(Utils.removeQueryFromURI(node.getUIPath()).toString());
									add(field,LabelledLayout.CONTENT_AREA);
									labelIds.add(node.getLabelId());
									if (!ff.isReadOnly(false) && !ff.isReadOnly(true)) {
										modifiableLabelIds.add(node.getLabelId());
									}
									accessors.put(node.getUIPath().toString(),GettersAndSettersFactory.buildGetterAndSetter(instanceClass,node.getName()));
									lastComponent[0] = field;
								} catch (LocalizationException | ContentException exc) {
									exc.printStackTrace();
								}
								actions.clear();
								break;
							case EXIT	:
								if (actions.size() > 0) {
									new ComponentKeepedBorder(0,actions.toArray(new JComponent[actions.size()])).install(lastComponent[0]);
								}
								break;
							default :
								throw new UnsupportedOperationException("Node enter mode ["+mode+"] is not supported yet");
						}
					}
					else if (mode == NodeEnterMode.ENTER && node.getApplicationPath().toString().contains(ContentMetadataInterface.APPLICATION_SCHEME+":"+ContentModelFactory.APPLICATION_SCHEME_ACTION)) {
						final JButton		button = new JButton();
						
						button.setName(Utils.removeQueryFromURI(node.getUIPath()).toString());
						button.setActionCommand(node.getApplicationPath().toString());
						button.addActionListener((e)->{callback.process(e.getActionCommand());});
						actions.add(button);
					}
				}
				return ContinueMode.CONTINUE;
			}, mdi.getRoot().getUIPath());
			fillLocalizedStrings();
			bulkUpload();
		}
	}
	
	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
	}

	@Override
	public boolean process(final MonitorEvent event, final ContentNodeMetadata metadata, final JComponent component, final Object... parameters) throws ContentException {
		switch (event) {
			case Action			:
				return false;
			case FocusGained	:
				return false;
			case FocusLost		:
				return false;
			case Loading		:
				return false;
			case Rollback		:
				return false;
			case Saving			:
				return false;
			case Validation		:
				return true;
			default:
				throw new UnsupportedOperationException("Unsupported event type ["+event+"]");
		}
	}

	public void bulkDownload() {
		mdi.walkDown((mode,applicationPath,uiPath,node)->{
			if (mode == NodeEnterMode.ENTER) {
				if(node.getApplicationPath() != null) {
					if(node.getApplicationPath().toString().contains(ContentMetadataInterface.APPLICATION_SCHEME+":"+ContentModelFactory.APPLICATION_SCHEME_FIELD)) {
						final JComponent		field = (JComponent) SwingUtils.findComponentByName(this,node.getUIPath().toString());
		
						try{if (field instanceof JComponentInterface) {
								final Object	value = ((JComponentInterface)field).getValueFromComponent();
								
								ModelUtils.setValueBySetter(content, value
														, accessors.get(node.getUIPath().toString())
														, node);
							}
						} catch (ContentException exc) {
							exc.printStackTrace();
						}
					}
				}
			}
			return ContinueMode.CONTINUE;
		}, mdi.getRoot().getUIPath());
	}

	public void bulkUpload() {
		mdi.walkDown((mode,applicationPath,uiPath,node)->{
			if (mode == NodeEnterMode.ENTER) {
				if(node.getApplicationPath() != null) {
					if(node.getApplicationPath().toString().contains(ContentMetadataInterface.APPLICATION_SCHEME+":"+ContentModelFactory.APPLICATION_SCHEME_FIELD)) {
						final JComponent		field = (JComponent) SwingUtils.findComponentByName(this,node.getUIPath().toString());
		
						try{if (field instanceof JComponentInterface) {
								final Object	value = ModelUtils.getValueByGetter(content
														, accessors.get(node.getUIPath().toString())
														, node);
								
								((JComponentInterface)field).assignValueToComponent(value);
							}
						} catch (ContentException exc) {
							exc.printStackTrace();
						}
					}
				}
			}
			return ContinueMode.CONTINUE;
		}, mdi.getRoot().getUIPath());
	}
	
	private void fillLocalizedStrings() {
		mdi.walkDown((mode,applicationPath,uiPath,node)->{
			if (mode == NodeEnterMode.ENTER) {
				if(node.getApplicationPath() != null) {
					if(node.getApplicationPath().toString().contains(ContentMetadataInterface.APPLICATION_SCHEME+":"+ContentModelFactory.APPLICATION_SCHEME_ACTION)) {
						final JButton		button = (JButton) SwingUtils.findComponentByName(this,node.getUIPath().toString());
		
						if (button != null) {
							try{button.setText(localizer.getValue(node.getLabelId()));
							button.setToolTipText(localizer.getValue(node.getTooltipId()));
							} catch (LocalizationException exc) {
	//							logger.message(Severity.error,exc,"Filling localized for [%1$s]: processing error %2$s",node.getApplicationPath(),exc.getLocalizedMessage());
							}
						}
					}
					if(node.getApplicationPath().toString().contains(ContentMetadataInterface.APPLICATION_SCHEME+":"+ContentModelFactory.APPLICATION_SCHEME_FIELD)) {
						final JLabel		label = (JLabel) SwingUtils.findComponentByName(this,node.getUIPath().toString()+"/label");
						final JComponent	field = (JComponent) SwingUtils.findComponentByName(this,node.getUIPath().toString());
		
						try{label.setText(localizer.getValue(node.getLabelId()));
							field.setToolTipText(localizer.getValue(node.getTooltipId()));
						} catch (LocalizationException exc) {
//							logger.message(Severity.error,exc,"Filling localized for [%1$s]: processing error %2$s",node.getApplicationPath(),exc.getLocalizedMessage());
						}
					}
				}
			}
			return ContinueMode.CONTINUE;
		}, mdi.getRoot().getUIPath());
	}

}
