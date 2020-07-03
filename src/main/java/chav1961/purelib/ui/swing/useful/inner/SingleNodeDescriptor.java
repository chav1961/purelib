package chav1961.purelib.ui.swing.useful.inner;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.WellKnownLocalizationKeys;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.MutableContentNodeMetadata;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.Format;
import chav1961.purelib.ui.interfaces.RefreshMode;

@LocaleResourceLocation("i18n:xml:root://chav1961.purelib.ui.swing.useful.JContentMetadataEditor/chav1961/purelib/i18n/localization.xml")
@LocaleResource(value="JContentMetadataEditor.single.caption",tooltip="JContentMetadataEditor.single.caption.tt")
public class SingleNodeDescriptor implements FormManager<Object,SingleNodeDescriptor> {
	private static final URI	DEFAULT_LOCALIZER = URI.create("missing");
	private static final String	DEFAULT_TOOLTIP_ID = "<tooltip>";
	private static final String	DEFAULT_HELP_ID = "<help>";
	private static final URI	DEFAULT_APPLICATION_URI = URI.create("missing");
	private static final URI	DEFAULT_ICON_URI = URI.create("missing");

	private static final Map<String,Class<?>>	CORRECT_PRIMITIVES = new HashMap<>();
	
	static {
		CORRECT_PRIMITIVES.put("byte",byte.class);
		CORRECT_PRIMITIVES.put("short",short.class);
		CORRECT_PRIMITIVES.put("char",char.class);
		CORRECT_PRIMITIVES.put("int",int.class);
		CORRECT_PRIMITIVES.put("long",long.class);
		CORRECT_PRIMITIVES.put("float",float.class);
		CORRECT_PRIMITIVES.put("double",double.class);
		CORRECT_PRIMITIVES.put("boolean",boolean.class);
	}
	
	@LocaleResource(value="JContentMetadataEditor.single.fieldName",tooltip="JContentMetadataEditor.single.fieldName.tt")
	@Format("30ms")
	public String		fieldName = "<name>";

	@LocaleResource(value="JContentMetadataEditor.single.fieldType",tooltip="JContentMetadataEditor.single.fieldType.tt")
	@Format("30ms")
	public String		fieldClassName = "<type>";
	private Class<?>	fieldClass = null;

	@LocaleResource(value="JContentMetadataEditor.single.fieldRelUI",tooltip="JContentMetadataEditor.single.fieldRelUI.tt")
	@Format("30ms")
	public URI			fieldRelativeURI = URI.create("relativeUri");

	@LocaleResource(value="JContentMetadataEditor.single.fieldLocalizer",tooltip="JContentMetadataEditor.single.fieldLocalizer.tt")
	@Format("30s")
	public URI			fieldLocalizerURI = DEFAULT_LOCALIZER;
	
	@LocaleResource(value="JContentMetadataEditor.single.labelId",tooltip="JContentMetadataEditor.single.labelId.tt")
	@Format("30ms")
	public String		labelId = "<label>";
	
	@LocaleResource(value="JContentMetadataEditor.single.tooltipId",tooltip="JContentMetadataEditor.single.tooltipId.tt")
	@Format("30s")
	public String		tooltipId = DEFAULT_TOOLTIP_ID;

	@LocaleResource(value="JContentMetadataEditor.single.helpId",tooltip="JContentMetadataEditor.single.helpId.tt")
	@Format("30s")
	public String		helpId = DEFAULT_HELP_ID;

	@LocaleResource(value="JContentMetadataEditor.single.fieldFormat",tooltip="JContentMetadataEditor.single.fieldFormat.tt")
	@Format("30ms")
	public String		fieldFormat = "<format>";

	@LocaleResource(value="JContentMetadataEditor.single.fieldApplicationUri",tooltip="JContentMetadataEditor.single.fieldApplicationUri.tt")
	@Format("30s")
	public URI			fieldApplicationURI = DEFAULT_APPLICATION_URI;

	@LocaleResource(value="JContentMetadataEditor.single.iconUri",tooltip="JContentMetadataEditor.single.iconUri.tt")
	@Format("30s")
	public URI			fieldIconURI = DEFAULT_ICON_URI;
	
	private final LoggerFacade	logger;
	
	public SingleNodeDescriptor(final LoggerFacade logger) {
		this.logger = logger;
	}

	public void fillContent(final ContentNodeMetadata data) {
		if (data == null) {
			throw new NullPointerException("Data can't be null");
		}
		else {
			fieldName = data.getName();
			fieldClass = data.getType();
			fieldClassName = fieldClass.getCanonicalName();
			fieldRelativeURI = data.getRelativeUIPath();
			fieldLocalizerURI = data.getLocalizerAssociated() != null ? data.getLocalizerAssociated() : DEFAULT_LOCALIZER;
			labelId = data.getLabelId();
			tooltipId = data.getTooltipId() != null ? data.getTooltipId() : DEFAULT_TOOLTIP_ID;
			helpId = data.getHelpId() != null ? data.getHelpId() : DEFAULT_HELP_ID;
			fieldFormat = data.getFormatAssociated().toFormatString();
			fieldApplicationURI = data.getApplicationPath() != null ? data.getApplicationPath() : DEFAULT_APPLICATION_URI;
			fieldIconURI = data.getIcon() != null ? data.getIcon() : DEFAULT_ICON_URI;
		}
	}
	
	public MutableContentNodeMetadata getContent() {
		return new MutableContentNodeMetadata(fieldName,fieldClass,fieldRelativeURI.toString(),
				DEFAULT_LOCALIZER.equals(fieldLocalizerURI) ? null : fieldLocalizerURI,
				labelId,tooltipId.isEmpty() ? null : tooltipId,helpId.isEmpty() ? null : helpId,
				new FieldFormat(fieldClass,fieldFormat),
				DEFAULT_APPLICATION_URI.equals(fieldApplicationURI) ? null : fieldApplicationURI, 
				DEFAULT_ICON_URI.equals(fieldIconURI) ? null : fieldIconURI
		);
	}
	
	@Override
	public RefreshMode onField(final SingleNodeDescriptor inst, final Object id, final String field, final Object oldValue) throws FlowException, LocalizationException {
		switch(field) {
			case "fieldName" 			:
				if (fieldName.trim().isEmpty()) {
					getLogger().message(Severity.warning,WellKnownLocalizationKeys.CHECK_NULL_OR_EMPTY_VALUE);
					return RefreshMode.REJECT;
				}
				else {
					final String	oldName = oldValue == null ? "\uFFFF" : oldValue.toString();
					boolean			needRefreshFields = false;
					
					if (fieldRelativeURI.toString().endsWith(oldName)) {
						fieldRelativeURI = URI.create(fieldRelativeURI.toString().replace(oldName,fieldName));
						needRefreshFields = true;
					}
					if (fieldApplicationURI.toString().endsWith(oldName)) {
						fieldApplicationURI = URI.create(fieldApplicationURI.toString().replace(oldName,fieldName));
						needRefreshFields = true;
					}
					return needRefreshFields ? RefreshMode.RECORD_ONLY : RefreshMode.DEFAULT;
				}
			case "fieldClassName" 			:
				if (fieldClassName.trim().isEmpty()) {
					getLogger().message(Severity.warning,WellKnownLocalizationKeys.CHECK_NULL_OR_EMPTY_VALUE);
					return RefreshMode.REJECT;
				}
				else {
					final String	correctClassName = fieldClassName.trim(); 
					
					if (CORRECT_PRIMITIVES.containsKey(correctClassName)) {
						fieldClass = CORRECT_PRIMITIVES.get(correctClassName);
						return RefreshMode.DEFAULT;
					}
					else {
						try{fieldClass = Class.forName(correctClassName);
							return RefreshMode.DEFAULT;
						} catch (ClassNotFoundException exc) {
							getLogger().message(Severity.warning,WellKnownLocalizationKeys.CHECK_CLASS_NOT_FOUND,fieldClassName.trim());
							return RefreshMode.REJECT;
						}
					}
				}
			case "fieldRelativeURI" 	:
				if (fieldRelativeURI.isAbsolute()) {
					getLogger().message(Severity.warning,WellKnownLocalizationKeys.CHECK_RELATIVE_URI_REQUIRED);
					return RefreshMode.REJECT;
				}
				else {
					return RefreshMode.DEFAULT;
				}
			case "fieldLocalizerURI" 	:
				if (!fieldLocalizerURI.isAbsolute()) {
					if (!fieldLocalizerURI.equals(DEFAULT_LOCALIZER)) {
						getLogger().message(Severity.warning,WellKnownLocalizationKeys.CHECK_ABSOLUTE_URI_REQUIRED);
						return RefreshMode.REJECT;
					}
					else {
						return RefreshMode.DEFAULT;
					}
				}
				else if (!LocalizerFactory.hasLocalizerFor(fieldLocalizerURI)) {
					getLogger().message(Severity.warning,WellKnownLocalizationKeys.CHECK_URI_REFERS_TO_NOWHERE,fieldLocalizerURI);
					return RefreshMode.REJECT;
				}
				else {
					return RefreshMode.DEFAULT;
				}
			case "labelId" 				:
				if (labelId.trim().isEmpty()) {
					getLogger().message(Severity.warning,WellKnownLocalizationKeys.CHECK_NULL_OR_EMPTY_VALUE);
					return RefreshMode.REJECT;
				}
				else if (!validateLocalizationId(fieldLocalizerURI,labelId)) {
					getLogger().message(Severity.warning,WellKnownLocalizationKeys.CHECK_LOCALIZATION_KEY_IS_MISSING,fieldLocalizerURI,labelId);
					return RefreshMode.REJECT;
				}
				else {
					return RefreshMode.DEFAULT;
				}
			case "tooltipId" 			:
				if (!tooltipId.trim().isEmpty()) {
					if (!validateLocalizationId(fieldLocalizerURI,tooltipId)) {
						getLogger().message(Severity.warning,WellKnownLocalizationKeys.CHECK_LOCALIZATION_KEY_IS_MISSING,fieldLocalizerURI,tooltipId);
						return RefreshMode.REJECT;
					}
					else {
						return RefreshMode.DEFAULT;
					}
				}
				else {
					return RefreshMode.DEFAULT;
				}
			case "helpId" 				:
				if (!helpId.trim().isEmpty()) {
					if (!validateLocalizationId(fieldLocalizerURI,helpId)) {
						getLogger().message(Severity.warning,WellKnownLocalizationKeys.CHECK_LOCALIZATION_KEY_IS_MISSING,fieldLocalizerURI,helpId);
						return RefreshMode.REJECT;
					}
					else {
						return RefreshMode.DEFAULT;
					}
				}
				else {
					return RefreshMode.DEFAULT;
				}
			case "fieldFormat" 			:
				if (!fieldFormat.trim().isEmpty()) {
					try{new FieldFormat(fieldClass,fieldFormat.trim());
					
						return RefreshMode.FIELD_ONLY;
					} catch (IllegalArgumentException exc) {
						getLogger().message(Severity.warning,WellKnownLocalizationKeys.CHECK_ILLEGAL_FIELD_FORMAT,fieldClass,fieldFormat.trim(),exc.getLocalizedMessage());
						return RefreshMode.REJECT;
					}
				}
				else {
					return RefreshMode.DEFAULT;
				}
			case "fieldApplicationURI" 	:
				if (!fieldApplicationURI.isAbsolute()) {
					if (!fieldApplicationURI.equals(DEFAULT_APPLICATION_URI)) {
						getLogger().message(Severity.warning,WellKnownLocalizationKeys.CHECK_ABSOLUTE_URI_REQUIRED);
						return RefreshMode.REJECT;
					}
					else {
						return RefreshMode.DEFAULT;
					}
				}
				else {
					return RefreshMode.DEFAULT;
				}
			case "fieldIconURI" 		:
				if (!fieldIconURI.isAbsolute()) {
					if (!fieldApplicationURI.equals(DEFAULT_ICON_URI)) {
						getLogger().message(Severity.warning,WellKnownLocalizationKeys.CHECK_ABSOLUTE_URI_REQUIRED);
						return RefreshMode.REJECT;
					}
					else {
						return RefreshMode.DEFAULT;
					}
				}
				else {
					return RefreshMode.DEFAULT;
				}
			default  : throw new UnsupportedOperationException("Field processing for ["+field+"] is not supported yet"); 
		}
		
	}

	@Override
	public LoggerFacade getLogger() {
		return logger;
	}
	
	private static boolean validateLocalizationId(final URI localizerUri, final String key) {
		if (LocalizerFactory.hasLocalizerFor(localizerUri)) {
			try{final Localizer	localizer = LocalizerFactory.getLocalizer(localizerUri);
				
				return localizer.containsKey(key.trim());
			} catch (LocalizationException e) {
				return false;
			}
		}
		else {
			return true;
		}
	}
}