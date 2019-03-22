package chav1961.purelib.ui.swing.interfaces;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import chav1961.purelib.ui.interfaces.WizardStep;

/**
 * <p>This annotation is used to link a wizard JComponent-based fields with the data source feilds. The only parameter of the annotation is a field name in the
 * data source. On {@linkplain WizardStep#beforeShow(Object, java.util.Map, chav1961.purelib.ui.interfaces.WizardStep.ErrorProcessing) beforeShow} method call,
 * this JcComponend-based field will be filled with the appropriative field value, and on 
 * {@linkplain WizardStep#afterShow(Object, java.util.Map, chav1961.purelib.ui.interfaces.WizardStep.ErrorProcessing) afterShow} method call, data source field 
 * will be filled with the JComponent field value. </p>
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 */

@Retention(RUNTIME)
@Target(FIELD)
public @interface LinkedTo {
	String value();
}
