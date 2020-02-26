package chav1961.purelib.ui.swing;

import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;

import chav1961.purelib.basic.GettersAndSettersFactory;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.GettersAndSettersFactory.GetterAndSetter;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.model.Constants;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.interfaces.RefreshMode;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor.MonitorEvent;
import chav1961.purelib.ui.swing.useful.LabelledLayout;

class FormManagedUtils {
	static <T> RefreshMode seekAndCall(final T instance, final URI appPath) throws Exception {
		final String[]		parts = URI.create(appPath.getSchemeSpecificPart()).getPath().split("/");
		Class<?>			cl = instance.getClass();
		
		while (cl != null && parts.length >= 3) {
			for (Method m : cl.getDeclaredMethods()) {
				if (parts[2].startsWith(m.getName()+"().")) {
					m.setAccessible(true);
					
					if (m.getReturnType() == void.class) {
						m.invoke(instance);
						return RefreshMode.DEFAULT;
					}
					else if (RefreshMode.class.isAssignableFrom(m.getReturnType())) {
						return (RefreshMode)m.invoke(instance);
					}
					else {
						throw new IllegalArgumentException("Method ["+m+"] returns neither void nor RefreshMode type");
					}
				}
			}
			cl = cl.getSuperclass();
		}
		return RefreshMode.DEFAULT;
	}
	
	interface FormManagerParserCallback {
		void processActionButton(final ContentNodeMetadata metadata, final JButtonWithMeta button) throws ContentException;
		void processField(final ContentNodeMetadata metadata, final JLabel fieldLabel, final JComponent fieldComponent, final GetterAndSetter gas, final boolean isModifiable) throws ContentException;
	}
	
	static <T> void parseModel4Form(final LoggerFacade logger, final ContentMetadataInterface mdi, final Class<T> instanceClass, final JComponentMonitor monitor, final FormManagerParserCallback callback) {
		try(final LoggerFacade	trans = logger.transaction("parseModel")) {
			
			mdi.walkDown((mode,applicationPath,uiPath,node)->{
				if (mode == NodeEnterMode.ENTER) {
					if (node.getApplicationPath() != null){
						try{if(node.getApplicationPath().toString().contains(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_ACTION)) {
								final JButtonWithMeta		button = new JButtonWithMeta(node,monitor);
								
								button.setName(URIUtils.removeQueryFromURI(node.getUIPath()).toString());
								trans.message(Severity.trace,"Process button [%1$s]",node.getApplicationPath());
		
								button.setActionCommand(node.getApplicationPath().toString());
								callback.processActionButton(node,button);							
							}
							else if(node.getApplicationPath().toString().contains(ContentMetadataInterface.APPLICATION_SCHEME+":"+Constants.MODEL_APPLICATION_SCHEME_FIELD)) {
								final JLabel			label = new JLabel();
								final FieldFormat		ff = node.getFormatAssociated();
								final JComponent 		field = SwingUtils.prepareRenderer(node, ff, monitor);
								final GetterAndSetter	gas = GettersAndSettersFactory.buildGetterAndSetter(instanceClass,node.getName());
							
								label.setName(URIUtils.removeQueryFromURI(node.getUIPath()).toString()+"/label");
								field.setName(URIUtils.removeQueryFromURI(node.getUIPath()).toString());
								trans.message(Severity.trace,"Process control [%1$s] type [%2$s]",node.getUIPath(),field.getClass().getCanonicalName());

								callback.processField(node,label,field,gas,!ff.isReadOnly(false) && !ff.isReadOnly(true));
							}
						} catch (LocalizationException | ContentException exc) {
							logger.message(Severity.error,exc,"Control [%1$s]: processing error %2$s",node.getApplicationPath(),exc.getLocalizedMessage());
						}
					}
				}
				return ContinueMode.CONTINUE;
			}, mdi.getRoot().getUIPath());
			
			trans.rollback();	// All ok, remove trace from logger			
		}
	}
}
