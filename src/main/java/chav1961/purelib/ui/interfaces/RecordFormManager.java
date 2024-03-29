package chav1961.purelib.ui.interfaces;

import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;

/**
 * <p>This interface describes actions for record processing.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 * @param <Id> record identifier type. Use Object type if not required
 * @param <Instance> instance type.
 */
public interface RecordFormManager<Id,Instance> {
	/**
	 * <p>This enumeration describes action on the UI screen for processing callback</p> 
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.2
	 * @last.update 0.0.7
	 */
	public static enum RecordAction {
		INSERT, DUPLICATE, UPDATE, DELETE, CHECK, REFRESH
	}

	/**
	 * <p>Processing record-scope actions on the UI screen</p>
	 * @param action record-scope action to process
	 * @param oldRecord old record instance (will be null for {@linkplain RecordAction#INSERT}) 
	 * @param oldId primary key of the old record instance (will be null for {@linkplain RecordAction#INSERT})
	 * @param newRecord new record instance (will be null for {@linkplain RecordAction#DELETE})
	 * @param newId primary key of the new record instance (will be null for {@linkplain RecordAction#DELETE})
	 * @return refresh mode after processing
	 * @throws FlowException on any errors or on action cancelling  
	 * @throws LocalizationException on any errors in the localization  
	 */
	default RefreshMode onRecord(final RecordAction action, final Instance oldRecord, final Id oldId, final Instance newRecord, final Id newId) throws FlowException, LocalizationException {
		return RefreshMode.DEFAULT;
	}
}
