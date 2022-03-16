package chav1961.purelib.ui.interfaces;

import java.util.Map;

import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.PreparationException;

/**
 * <p>This interface describes any entity that can be used as a step in the different wizard steps chain. The life cycle of the wizard step is:</p>
 * <ul>
 * <li>{@linkplain #prepare(Object, Map)} - the same first call from the wizard</li>
 * <li>{@linkplain #beforeShow(Object, Map, ErrorProcessing)} - call before showing the wizard step in the wizard</li>
 * <li>{@linkplain #validate(Object, Map, ErrorProcessing)} - call before attempt to go to next wizard step</li>
 * <li>{@linkplain #afterShow(Object, Map, ErrorProcessing)} - call after hiding the wizard step in the wizard</li>
 * <li>{@linkplain #unprepare(Object, Map)} - the same last call from the wizard</li>
 * </ul>
 * <p>Internal group of life cycle can be repeated many times. Any wizards must support the life cycle and must guarantee this calling sequence</p>
 * <p>Wizard step must be designed for correct subsequential usage in the other wizards after calling {@linkplain #unprepare(Object, Map)}</p>
 * <p>Internal structure Map&lt;String,Object&gt; in {@linkplain #beforeShow(Object, Map, ErrorProcessing)}, {@linkplain #validate(Object, Map, ErrorProcessing)}
 * and {@linkplain #afterShow(Object, Map, ErrorProcessing)} methods is a temporary store to share with all Wizard steps and can be used for data interchange
 * between them. Content of the store are unpredictable and implementation-specific before first calling these methods, and you need prepare it by self.</p>
 * <p>Wizard step is not thread-safe.</p>
 * @see chav1961.purelib.streams.charsource
 * @param <Common> data type for the information shared with all the WizardStep instances (usually some descriptor to fill with wizard) 
 * @param <ErrorType> type of the errors can be detected on the wizard execution stages. Any enumeration can be used
 * @param <Content> content type of the wizard step (for example, usually JComponent for the Swing application)
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 * @lastUpdate 0.0.5
 */

public interface WizardStep<Common,ErrorType extends Enum<?>, Content> {
	/**
	 * <p>This enumeration describes the step type. It can be:</p>
	 * <ul>
	 * <li>THE_ONLY - initial and terminal step. No any other steps will be presented in the wizard</li>
	 * <li>INITIAL - any step from which wizard starts. At least one INITIAL wizard step must present in any wizard</li>
	 * <li>TERM_SUCCESS - any terminal step with successful finishing. At least one TERM_SUCCESS wizard step must present in any wizard</li>
	 * <li>TERM_FAILURE - any terminal step with failure.</li>
	 * <li>ORDINAL - any step in the chain.</li>
	 * <li>PROCESSING - any step in the chain, that doesn't need human interaction. Except any other types, this type can't be navigated manually</li>
	 * </ul>
	 * @author chav1961
	 * @since 0.0.2
	 * @lastUpdate 0.0.6
	 */
	public enum StepType {
		THE_ONLY, INITIAL, TERM_SUCCESS, TERM_FAILURE, ORDINAL, PROCESSING
	}

	/**
	 * <p>Get current step id</p>
	 * @return any non-null and non-empty string. Need be unique in the given wizard steps chain
	 */
	String getStepId();
	
	/**
	 * <p>Get next step id</p>
	 * @return id of the next step in the given wizard steps chain, or null if the next step appropriates to the natural step order in the given wizard steps chain. Can be changed under conditions
	 * @lastUpdate 0.0.5  
	 */
	default String getNextStep() {return null;}
	
	/**
	 * <p>Get previous step id</p>
	 * @return id of the previous step in the given wizard steps chain, or null if the previous step appropriates to the natural step order in the given wizard steps chain. Can be changed under conditions  
	 * @lastUpdate 0.0.5  
	 */
	default String getPrevStep() {return null;}
	
	/**
	 * <p>Get step type.</p>
	 * @return step type. Can't be null
	 */
	StepType getStepType();
	
	/**
	 * <p>Get tab name in the tab collection for the given step. Tab name appears at the left list of the wizard.</p>
	 * @return any non-null and non-empty string. Can be identical with {@linkplain #getCaption()}
	 * @lastUpdate 0.0.5  
	 */
	default String getTabName() {return getCaption();}
	
	/**
	 * <p>Get any caption for the given step. Caption appears at top of the wizard</p>
	 * @return any non-null and non-empty string.
	 */
	String getCaption();
	
	/**
	 * <p>Get human-readable description of the step. DEsription appears before input form in the wizard</p>
	 * @return any string. Can be identical with {@linkplain #getCaption()}
	 */
	String getDescription();
	
	/**
	 * <p>Get help id associated with the step.</p>
	 * @return any non-null and non-empty string. Can be identical with {@linkplain #getStepId()}
	 */
	String getHelpId();
	
	/**
	 * <p>Get content kept inside the step. Method is called only once after calling {@linkplain #prepare(Object, Map)} method </p>
	 * @return any kind of content. Can't be null. Interpretation of the content is a scope of the wizard, not wizard step
	 */
	Content getContent();
	
	/**
	 * <p>Prepare wizard step to use in the wizard</p>
	 * @param content data type for the information shared with all the WizardStep instances (usually some descriptor to fill with wizard) 
	 * @param temporary temporary map to share with all the wizard steps in the given wizard. Every wizard step can use it for any purposes 
	 * @throws PreparationException when any problems on the preparation stage were detected
	 * @throws LocalizationException when any localization problems were detected 
	 * @throws NullPointerException if any of parameters are null
	 * @lastUpate 0.0.5
	 */
	default void prepare(Common content, Map<String,Object> temporary) throws PreparationException, LocalizationException, NullPointerException {}
	
	/**
	 * <p>Prepare wizard step before showing it in the wizard</p>
	 * @param content data type for the information shared with all the WizardStep instances (usually some descriptor to fill with wizard) 
	 * @param temporary temporary map to share with all the wizard steps in the given wizard. Every wizard step can use it for any purposes 
	 * @param err error processor
	 * @throws FlowException when any problems on the stage were detected
	 * @throws LocalizationException when any localization problems were detected 
	 * @throws NullPointerException if any of parameters are null
	 */
	void beforeShow(Common content, Map<String,Object> temporary, ErrorProcessing<Common,ErrorType> err) throws FlowException, LocalizationException, NullPointerException;
	
	/**
	 * <p>Validate content of the wizard step. If your step is {@linkplain StepType#PROCESSING} step, this method will be called asynchronously. You need
	 * implement all the functionality of the {@linkplain StepType#PROCESSING} step in this method only. Returned value for this call is treated as jump to the next step on true and 
	 * cancel wizard on false. Any exceptions thrown are treated as cancel wizard. To prevent this behavior, you can catch all exceptions inside the method and
	 * modify {@linkplain #getNextStep()} method to jump to different steps in the success or failure cases.</p>
	 * <p>This method is called with any wizards before either jumping to the next step or finishing wizard only. Neither cancellation, nor returning to the previous steps never 
	 * calls this method.</p>  
	 * @param content data type for the information shared with all the WizardStep instances (usually some descriptor to fill with wizard) 
	 * @param temporary temporary map to share with all the wizard steps in the given wizard. Every wizard step can use it for any purposes 
	 * @param err error processor
	 * @return true if content is valid and pass to the next step is available
	 * @throws FlowException when any problems on the stage were detected
	 * @throws LocalizationException when any localization problems were detected 
	 * @throws NullPointerException if any of parameters are null
	 */
	boolean validate(Common content, Map<String,Object> temporary, ErrorProcessing<Common,ErrorType> err) throws FlowException, LocalizationException, NullPointerException;
	
	/**
	 * <p>Process wizard step after showing it in the wizard</p>
	 * @param content data type for the information shared with all the WizardStep instances (usually some descriptor to fill with wizard) 
	 * @param temporary temporary map to share with all the wizard steps in the given wizard. Every wizard step can use it for any purposes 
	 * @param err error processor
	 * @throws FlowException when any problems on the stage were detected
	 * @throws LocalizationException when any localization problems were detected 
	 * @throws NullPointerException if any of parameters are null
	 */
	void afterShow(Common content, Map<String,Object> temporary, ErrorProcessing<Common,ErrorType> err) throws FlowException, LocalizationException, NullPointerException;
	
	/**
	 * <p>Unprepare wizard step after showing it in the wizard</p>
	 * @param content data type for the information shared with all the WizardStep instances (usually some descriptor to fill with wizard) 
	 * @param temporary temporary map to share with all the wizard steps in the given wizard. Every wizard step can use it for any purposes 
	 * @throws LocalizationException when any localization problems were detected 
	 * @throws NullPointerException if any of parameters are null
	 * @lastUpdate 0.0.5
	 */
	default void unprepare(Common content, Map<String,Object> temporary) throws LocalizationException, NullPointerException {}
}
