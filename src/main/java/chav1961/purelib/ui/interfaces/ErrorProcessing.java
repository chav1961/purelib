package chav1961.purelib.ui.interfaces;

import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;

/**
 * <p>This interface describes error and warning processor</p>
 * @param <Common> data type for the information shared with all the WizardStep instances (usually some descriptor to fill with wizard) 
 * @param <ErrorType> type of the errors can be detected on the wizard execution stages
 * @author chav1961
 * @since 0.0.2
 * @lastUpdate 0.0.6
 */
@FunctionalInterface
public interface ErrorProcessing<Common, ErrorType extends Enum<?>> {
	/**
	 * <p>Process errors detected</p>
	 * @param content data type for the information shared with all the WizardStep instances (usually some descriptor to fill with wizard)
	 * @param err type of the errors can be detected on the wizard execution stages
	 * @param parameters advanced parameters
	 * @throws FlowException usually need be thrown to indicate error
	 * @throws LocalizationException when any localization errors were detected
	 */
	default void processError(Common content,ErrorType err,Object... parameters) throws FlowException, LocalizationException {
		processWarning(content, err, parameters);
	}

	/**
	 * <p>Process information detected</p>
	 * @param content data type for the information shared with all the WizardStep instances (usually some descriptor to fill with wizard)
	 * @param err type of the errors can be detected on the wizard execution stages
	 * @param parameters advanced parameters
	 * @throws FlowException usually need be thrown to indicate error
	 * @throws LocalizationException when any localization errors were detected
	 * @since 0.0.6
	 */
	default void processInfo(Common content,ErrorType err,Object... parameters) throws FlowException, LocalizationException {
		processWarning(content, err, parameters);
	}
	
	/**
	 * <p>Process warnings detected</p>
	 * @param content data type for the information shared with all the WizardStep instances (usually some descriptor to fill with wizard)
	 * @param err type of the errors can be detected on the wizard execution stages
	 * @param parameters advanced parameters
	 * @throws LocalizationException when any localization errors were detected
	 */
	void processWarning(Common content,ErrorType err,Object... parameters) throws LocalizationException;
}