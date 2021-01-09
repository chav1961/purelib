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

import chav1961.purelib.basic.GettersAndSettersFactory.GetterAndSetter;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.Constants;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.ModelUtils;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.swing.FormManagedUtils;
import chav1961.purelib.ui.swing.FormManagedUtils.FormManagerParserCallback;
import chav1961.purelib.ui.swing.JButtonWithMeta;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;

public class J2ColumnEditor extends JPanel implements LocaleChangeListener, JComponentMonitor {
	private static final long 				serialVersionUID = 2981133342567260172L;
	private static final int				GAP_SIZE = 5; 

	private final Object					content;
	private final Localizer					localizer;
	private final Set<String>				labelIds = new HashSet<>(), modifiableLabelIds = new HashSet<>();
	private final Map<String,GetterAndSetter>	accessors = new HashMap<>();	
	private final ContentMetadataInterface	metadata;
	
	@FunctionalInterface
	public interface J2ColumnEditorCallback {
		void process(String actionCommand);
	}	

	@FunctionalInterface
	public interface J2ColumnEditorFilterCallback {
		default boolean useAction(final ContentNodeMetadata metadata) {return true;}
		boolean useField(final ContentNodeMetadata metadata);
	}	
	
	public J2ColumnEditor(final ContentMetadataInterface metadata, final Object content) throws SyntaxException, LocalizationException, NullPointerException, PreparationException, IllegalArgumentException, ContentException, IOException {
		this(metadata,content,1);
	}

	public J2ColumnEditor(final ContentMetadataInterface metadata, final Object content, final J2ColumnEditorCallback callback) throws SyntaxException, LocalizationException, NullPointerException, PreparationException, IllegalArgumentException, ContentException, IOException {
		this(metadata,content,1,callback);
	}
	
	public J2ColumnEditor(final ContentMetadataInterface metadata, final Object content, final int numberOfBars) throws SyntaxException, LocalizationException, NullPointerException, PreparationException, IllegalArgumentException, ContentException, IOException {
		this(metadata,content,numberOfBars,(c)->{});
	}

	public J2ColumnEditor(final ContentMetadataInterface metadata, final Object content, final int numberOfBars, final J2ColumnEditorCallback callback) throws SyntaxException, LocalizationException, NullPointerException, PreparationException, IllegalArgumentException, ContentException, IOException {
		this(metadata,content,numberOfBars,(meta)->true,callback);
	}
	
	public J2ColumnEditor(final ContentMetadataInterface metadata, final Object content, final int numberOfBars, final J2ColumnEditorFilterCallback filter, final J2ColumnEditorCallback callback) throws SyntaxException, LocalizationException, NullPointerException, PreparationException, IllegalArgumentException, ContentException, IOException {
		super(new LabelledLayout(numberOfBars, GAP_SIZE, GAP_SIZE, LabelledLayout.VERTICAL_FILLING));
		
		if (metadata == null) {
			throw new NullPointerException("Metadata to show can't be null"); 
		}
		else if (content == null) {
			throw new NullPointerException("Content to show can't be null"); 
		}
		else if (filter == null) {
			throw new NullPointerException("Filter callback can't be null"); 
		}
		else if (callback == null) {
			throw new NullPointerException("Editor callback can't be null"); 
		}
		else {
			this.content = content;
			this.metadata = metadata;
			this.localizer = LocalizerFactory.getLocalizer(metadata.getRoot().getLocalizerAssociated());
			
			setName(metadata.getRoot().getUIPath().toString());

			final List<JButton>	actions = new ArrayList<>();
			
			FormManagedUtils.parseModel4Form(PureLibSettings.CURRENT_LOGGER,metadata,localizer,content.getClass(),this
					, new FormManagerParserCallback() {
						@Override
						public void processActionButton(final ContentNodeMetadata metadata, final JButtonWithMeta button) throws ContentException {
							if (filter.useAction(metadata)) {
								button.setName(URIUtils.removeQueryFromURI(metadata.getUIPath()).toString());
								button.setActionCommand(metadata.getApplicationPath().toString());
								button.addActionListener((e)->{callback.process(e.getActionCommand());});
								actions.add(button);
							}
						}

						@Override
						public void processField(final ContentNodeMetadata metadata, final JLabel fieldLabel, final JComponent fieldComponent, final GetterAndSetter gas, final boolean isModifiable) throws ContentException {
							if (filter.useField(metadata)) {
								final FieldFormat	ff = metadata.getFormatAssociated();
								
								fieldLabel.setName(URIUtils.removeQueryFromURI(metadata.getUIPath()).toString()+"/label");
								add(fieldLabel,LabelledLayout.LABEL_AREA);
								fieldComponent.setName(URIUtils.removeQueryFromURI(metadata.getUIPath()).toString());
								add(fieldComponent,LabelledLayout.CONTENT_AREA);
								labelIds.add(metadata.getLabelId());
								if (!ff.isReadOnly(false) && !ff.isReadOnly(true)) {
									modifiableLabelIds.add(metadata.getLabelId());
								}
								accessors.put(metadata.getUIPath().toString(),gas);
							}
						}
			});
			
			fillLocalizedStrings();
			bulkUpload();
		}
	}
	
	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
	}

	@Override
	public boolean process(final MonitorEvent event, final ContentNodeMetadata metadata, final JComponentInterface component, final Object... parameters) throws ContentException {
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
		metadata.walkDown((mode,applicationPath,uiPath,node)->{
			if (mode == NodeEnterMode.ENTER) {
				if(node.getApplicationPath() != null) {
					if(node.getApplicationPath().toString().contains(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD)) {
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
		}, metadata.getRoot().getUIPath());
	}

	public void bulkUpload() {
		metadata.walkDown((mode,applicationPath,uiPath,node)->{
			if (mode == NodeEnterMode.ENTER) {
				if(node.getApplicationPath() != null) {
					if(node.getApplicationPath().toString().contains(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD)) {
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
		}, metadata.getRoot().getUIPath());
	}

	public ContentMetadataInterface getMetadata() {
		return metadata;
	}
	
	private void fillLocalizedStrings() {
		metadata.walkDown((mode,applicationPath,uiPath,node)->{
			if (mode == NodeEnterMode.ENTER) {
				if(node.getApplicationPath() != null) {
					if(node.getApplicationPath().toString().contains(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_ACTION)) {
						final JButton		button = (JButton) SwingUtils.findComponentByName(this,node.getUIPath().toString());
		
						if (button != null) {
							try{button.setText(localizer.getValue(node.getLabelId()));
							button.setToolTipText(localizer.getValue(node.getTooltipId()));
							} catch (LocalizationException exc) {
	//							logger.message(Severity.error,exc,"Filling localized for [%1$s]: processing error %2$s",node.getApplicationPath(),exc.getLocalizedMessage());
							}
						}
					}
					if(node.getApplicationPath().toString().contains(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD)) {
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
		}, metadata.getRoot().getUIPath());
	}

}
