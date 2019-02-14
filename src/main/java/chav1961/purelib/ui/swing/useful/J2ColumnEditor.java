package chav1961.purelib.ui.swing.useful;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
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
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;

public class J2ColumnEditor extends JPanel implements LocaleChangeListener, JComponentMonitor {
	private static final long 				serialVersionUID = 2981133342567260172L;
	private static final int				GAP_SIZE = 5; 

	private final ContentMetadataInterface	mdi;
	private final Localizer					localizer;
	private final Set<String>				labelIds = new HashSet<>(), modifiableLabelIds = new HashSet<>();
	private final Map<String,GetterAndSetter>	accessors = new HashMap<>();	
	
	public J2ColumnEditor(final Object content) throws SyntaxException, LocalizationException, NullPointerException, PreparationException, IllegalArgumentException, ContentException, IOException {
		this(content,1);
	}
	
	public J2ColumnEditor(final Object content, final int numberOfBars) throws SyntaxException, LocalizationException, NullPointerException, PreparationException, IllegalArgumentException, ContentException, IOException {
		super(new LabelledLayout(numberOfBars, GAP_SIZE, GAP_SIZE, LabelledLayout.VERTICAL_FILLING));
		
		if (content == null) {
			throw new NullPointerException("Content to show can't be null"); 
		}
		else {
			final Class<?>	instanceClass = content.getClass();
			
			this.mdi = ContentModelFactory.forAnnotatedClass(instanceClass);
			this.localizer = LocalizerFactory.getLocalizer(mdi.getRoot().getLocalizerAssociated());
	
			mdi.walkDown((mode,applicationPath,uiPath,node)->{
				if (mode == NodeEnterMode.ENTER) {
					if (node.getApplicationPath() != null){ 
						if(node.getApplicationPath().toString().contains(ContentMetadataInterface.APPLICATION_SCHEME+":"+ContentModelFactory.APPLICATION_SCHEME_FIELD)) {
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
							} catch (LocalizationException | ContentException exc) {
							}
						}
					}
				}
				return ContinueMode.CONTINUE;
			}, mdi.getRoot().getUIPath());
		}
	}
	
	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean process(final MonitorEvent event, final ContentNodeMetadata metadata, final JComponent component, final Object... parameters) throws ContentException {
		// TODO Auto-generated method stub
		return false;
	}

	private void fillLocalizedStrings() {
		mdi.walkDown((mode,applicationPath,uiPath,node)->{
			if (mode == NodeEnterMode.ENTER) {
				if(node.getApplicationPath() != null) {
					if(node.getApplicationPath().toString().contains(ContentMetadataInterface.APPLICATION_SCHEME+":"+ContentModelFactory.APPLICATION_SCHEME_ACTION)) {
						final JButton		button = (JButton) SwingUtils.findComponentByName(this,node.getUIPath().toString());
		
						try{button.setText(localizer.getValue(node.getLabelId()));
						button.setToolTipText(localizer.getValue(node.getTooltipId()));
						} catch (LocalizationException exc) {
//							logger.message(Severity.error,exc,"Filling localized for [%1$s]: processing error %2$s",node.getApplicationPath(),exc.getLocalizedMessage());
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
