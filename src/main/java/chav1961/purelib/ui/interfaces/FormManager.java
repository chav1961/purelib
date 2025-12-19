package chav1961.purelib.ui.interfaces;

import chav1961.purelib.basic.interfaces.LoggerFacadeOwner;

/**
 * <p>This class is an abstract class for implementing low-level UI form management. This class supports:</p> 
 * <ul>
 * <li>Creole-styled multi-paged UI form description</li>
 * <li>UI form inheritance</li>
 * <li>a lot of form representation on the UI screen</li>
 * <li>a lot of field representation on the UI screen</li>
 * <li>supporting callback (see {@linkplain FormManager}) for the form management</li>
 * </ul>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 * @last.update 0.0.7
 * @param <Id> primary key Id type for the form manager instance
 * @param <Instance> content for the form manager instance
 */

public interface FormManager<Id,Instance> extends RecordFormManager<Id,Instance>, FieldFormManager<Id,Instance>, ActionFormManager<Id,Instance> {
}
