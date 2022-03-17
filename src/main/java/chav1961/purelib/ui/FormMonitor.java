package chav1961.purelib.ui;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Map;

import chav1961.purelib.basic.GettersAndSettersFactory.GetterAndSetter;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.model.ModelUtils;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.sql.SQLUtils;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.interfaces.RefreshMode;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;

public abstract class FormMonitor<T> implements JComponentMonitor {
	private final Localizer					localizer;
	private final LoggerFacade				logger;
	private final T							instance;
	private final FormManager<?,T>			formMgr;
	private final Map<URI,GetterAndSetter>	accessors;
	private final boolean					tooltipsOnFocus;
	
	public FormMonitor(final Localizer localizer, final LoggerFacade logger, final T instance, final FormManager<?,T> formMgr, final Map<URI,GetterAndSetter> accessors, final boolean tooltipsOnFocus) throws NullPointerException {
		if (localizer == null) {
			throw new NullPointerException("Localizer associated can't be null");
		}
		else if (logger == null) {
			throw new NullPointerException("Logger can't be null");
		}
		else if (instance == null) {
			throw new NullPointerException("Instance to monitor can't be null");
		}
		else if (formMgr == null) {
			throw new NullPointerException("Form manager can't be null");
		}
		else if (accessors == null) {
			throw new NullPointerException("Accessor map can't be null");
		}
		else {
			this.localizer = localizer;
			this.logger = logger;
			this.instance = instance;
			this.formMgr = formMgr;
			this.accessors = accessors;
			this.tooltipsOnFocus = tooltipsOnFocus;
		}
	}

	protected abstract JComponentInterface findComponentByName(final URI uiPath) throws ContentException;
	
	@Override
	public boolean process(final MonitorEvent event, final ContentNodeMetadata metadata, final JComponentInterface component, final Object... parameters) throws ContentException {
		System.err.println("Event="+event+",meta="+metadata.getName());
		switch (event) {
			case Action:
				if (metadata.getApplicationPath().toString().contains("().")) {
					try{switch (processRefreshMode(seekAndCall(instance,metadata.getApplicationPath()), event, metadata, component, parameters)) {
							case REJECT : case FIELD_ONLY : case DEFAULT : case NONE :
								break;
							case TOTAL : case RECORD_ONLY :
								for (ContentNodeMetadata item : metadata.getParent()) {
									process(MonitorEvent.Loading,item,findComponentByName(item.getUIPath()));
								}
								break;
							case EXIT :
								return process(MonitorEvent.Exit,metadata,component,parameters);
							default	:
								break;
						}
					} catch (Exception exc) {
						getLogger().message(Severity.error,exc,"Action [%1$s]: processing error %2$s",metadata.getApplicationPath(),exc.getLocalizedMessage());
					}
				}
				else {
					try{switch (processRefreshMode(formMgr.onAction(instance, null, metadata.getApplicationPath().toString(), parameters), event, metadata, component, parameters)) {
							case REJECT : case FIELD_ONLY : case DEFAULT : case NONE :
								break;
							case TOTAL : case RECORD_ONLY :
								for (ContentNodeMetadata item : metadata.getParent()) {
									process(MonitorEvent.Loading,item,findComponentByName(item.getUIPath()));
								}
								break;
							case EXIT :
								return process(MonitorEvent.Exit,metadata,component,parameters);
							default	:
								break;
						}
					} catch (LocalizationException | FlowException exc) {
						getLogger().message(Severity.error,exc,"Action [%1$s]: processing error %2$s",metadata.getApplicationPath(),exc.getLocalizedMessage());
					}
				}
				break;
			case FocusGained:
				try{if (tooltipsOnFocus) {
						getLogger().message(Severity.tooltip,SwingUtils.prepareHtmlMessage(Severity.trace, getLocalizerAssociated().getValue(metadata.getTooltipId())));
					}
				} catch (LocalizationException  exc) {
					getLogger().message(Severity.error,exc,"FocusGained for [%1$s]: processing error %2$s",metadata.getApplicationPath(),exc.getLocalizedMessage());
					if (tooltipsOnFocus) {
						getLogger().message(Severity.tooltip,"a");
					}
				}
				break;
			case FocusLost:
				getLogger().message(Severity.tooltip,"a");
				break;
			case Loading:
				final GetterAndSetter	gas = accessors.get(metadata.getUIPath());
				
				if (gas == null) {
					// TODO:
				}
				else {
					final Object			value = ModelUtils.getValueByGetter(instance, gas, metadata);
					
					if (value == null || component == null) {
						// TODO:
					}
					((JComponentInterface)component).assignValueToComponent(value);
				}
				break;
			case Rollback:
				getLogger().message(Severity.tooltip,"");
				break;
			case Saving:
				try{final Object	oldValue = ((JComponentInterface)component).getValueFromComponent();
				
					ModelUtils.setValueBySetter(instance, SQLUtils.convert(metadata.getType(),((JComponentInterface)component).getChangedValueFromComponent()), accessors.get(metadata.getUIPath()), metadata);
					switch (processRefreshMode(formMgr.onField(instance,null,metadata.getName(),oldValue,false), event, metadata, component, parameters)) {
						case FIELD_ONLY : case DEFAULT : case NONE :
							break;
						case TOTAL : case RECORD_ONLY :
							for (ContentNodeMetadata item : metadata.getParent()) {
								process(MonitorEvent.Loading,item,findComponentByName(item.getUIPath()));
							}
							break;
						case REJECT		:
							ModelUtils.setValueBySetter(instance, SQLUtils.convert(metadata.getType(),oldValue), accessors.get(metadata.getUIPath()), metadata);
							((JComponentInterface)component).assignValueToComponent(oldValue);
							break;
						case EXIT :
							return process(MonitorEvent.Exit,metadata,component,parameters);
						default	:
							break;
					}
				} catch (FlowException | ContentException | RuntimeException exc) {
					getLogger().message(Severity.error,exc,"Saving for [%1$s]: processing error %2$s",metadata.getApplicationPath(),exc.getLocalizedMessage());
				}
				break;
			case Validation:
				final Object	changed = ((JComponentInterface)component).getChangedValueFromComponent(); 
				final String	error = ((JComponentInterface)component).standardValidation(changed);
				
				if (error != null) {
					getLogger().message(Severity.error,SwingUtils.prepareHtmlMessage(Severity.error, error));
					return false;
				}
				else {
					getLogger().message(Severity.tooltip,"");
					return true;
				}
			case FinalValidation :
				final Object	fvChanged = ((JComponentInterface)component).getChangedValueFromComponent(); 
				final String	fvError = ((JComponentInterface)component).standardValidation(fvChanged);
				
				if (fvError != null) {
					getLogger().message(Severity.error,SwingUtils.prepareHtmlMessage(Severity.error, fvError));
					return false;
				}
				else {
					try{if (formMgr.onField(instance,null,metadata.getName(),fvChanged,true) != RefreshMode.REJECT) {
							return true;
						}
					} catch (LocalizationException | FlowException e) {
						getLogger().message(Severity.error,SwingUtils.prepareHtmlMessage(Severity.error, e.getLocalizedMessage()));
					}
					return false;
				}
			case Exit :
				return processExit(metadata,component,parameters);
			default:
				break;
		}
		return true;
	}

	protected RefreshMode processRefreshMode(final RefreshMode mode, final MonitorEvent event, final ContentNodeMetadata metadata, final JComponentInterface component, final Object... parameters) throws ContentException {
		return mode;
	}
	
	protected LoggerFacade getLogger() {
		return logger;
	}
	
	protected Localizer getLocalizerAssociated() {
		return localizer;
	}
	
	protected boolean processExit(final ContentNodeMetadata metadata, final JComponentInterface component, final Object... parameters) {
		return true;
	}

	static <T> RefreshMode seekAndCall(final T instance, final URI appPath) throws Exception {
		final String[]		parts = URI.create(appPath.getSchemeSpecificPart()).getPath().split("/");
		Class<?>			cl = instance.getClass();
		
		while (cl != null && parts.length >= 3) {
			for (Method m : cl.getDeclaredMethods()) {
				if (m.getParameterCount() == 0 && parts[2].startsWith(m.getName()+"()")) {
					m.setAccessible(true);
					
					try{if (m.getReturnType() == void.class) {
							m.invoke(instance);
							return RefreshMode.DEFAULT;
						}
						else if (RefreshMode.class.isAssignableFrom(m.getReturnType())) {
							return (RefreshMode)m.invoke(instance);
						}
						else {
							throw new IllegalArgumentException("Method ["+m+"] returns neither void nor RefreshMode type");
						}
					} catch (InvocationTargetException exc) {	// unwrap source exception
						final Throwable	t = exc.getTargetException(); 
						
						if (t instanceof Exception) {
							throw (Exception)t; 
						}
						else {
							throw exc;
						}
					}
				}
			}
			cl = cl.getSuperclass();
		}
		return RefreshMode.DEFAULT;
	}
}
