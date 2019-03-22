package chav1961.purelib.ui.interfaces;

import java.util.Set;

import chav1961.purelib.basic.exceptions.ContentException;

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
 */

public interface FormModel<Id,Instance> {
	/**
	 * <p>This enumeration describes operations supported</p> 
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.2
	 */
	public enum SupportedOperations {
		INSERT, DUPLICATE, UPDATE, DELETE
	}
	
	/**
	 * <p>Get operations supported with the given mode</p>
	 * @return operations supported. Can be empty (means the model is read-only), but not null
	 */
	Set<SupportedOperations> getOperationsSupported();
	
	/**
	 * <p>Get type of the content</p>
	 * @return content type
	 */
	Class<Instance> getInstanceType();
	
	/**
	 * <p>Create unigue identifier for new instance key</p> 
	 * @return new unique identifier
	 * @throws ContentException on any errors 
	 */
	Id createUniqueId() throws ContentException;
	
	/**
	 * <p>Create new instance</p>
	 * @param id identifier for the given instance 
	 * @return instance created
	 * @throws ContentException on any errors 
	 */
	Instance createInstance(Id id) throws ContentException;
	
	/**
	 * <p>Duplicate new instance</p>
	 * @param oldId identifier of existent instance
	 * @param newId identifier for new instance
	 * @return instance duplicated
	 * @throws ContentException on any errors 
	 */
	Instance duplicateInstance(Id oldId,Id newId) throws ContentException;
	
	/**
	 * <p>Get instance by it's key</p>
	 * @param Id identifier of the instance to get
	 * @return instance got
	 * @throws ContentException on any errors 
	 */
	Instance getInstance(Id Id) throws ContentException;
	
	/**
	 * <p>Update instance with the new one</p>
	 * @param Id identifier of the instance to update 
	 * @param inst new instance to update
	 * @return old instance 
	 * @throws ContentException on any errors 
	 */
	Instance updateInstance(Id Id, Instance inst) throws ContentException;
	
	/**
	 * <p>Remove the given instance</p>
	 * @param Id identifier of the instance to remove
	 * @return instance removed
	 * @throws ContentException on any errors 
	 */
	Instance removeInstance(Id Id) throws ContentException;

	/**
	 * <p>Refresh model content</p>
	 * @throws ContentException on any errors 
	 */
	void refresh() throws ContentException;
	
	/**
	 * <p>Undo the last operation</p>
	 * @throws ContentException on any errors 
	 */
	void undo() throws ContentException;
	
	/**
	 * <p>Get size of model</p>
	 * @return model size. 
	 */
	int size();
	
	/**
	 * <p>Get index of the current record</p> 
	 * @return index of the current record. Will be -1 on empty model
	 */
	int getCurrentIndex();
	
	/**
	 * <p>Set current record index of the model</p>
	 * @param index index to set
	 */
	void setCurrentIndex(int index);
	
	/**
	 * <p>Get index of the given Id</p>
	 * @param id id to get index for
	 * @return index of the given ID or -1 if the id not found
	 */
	int getIndexById(Id id);
	
	/**
	 * <p>Get Id from the current index</p>
	 * @return Id of current index
	 */
	Id getCurrentId();
	
	/**
	 * <p>Get id for the given index</p>
	 * @param index index to get Id for
	 * @return Id for the given index
	 */
	Id getIdByIndex(int index);
	
	/**
	 * <p>Get list of all keys in the model</p>
	 * @return list of all keys in the model. Can be empty but not null
	 * @throws ContentException on any errors 
	 */
	Iterable<Id> contentIds() throws ContentException;

	/**
	 * <p>Set filter and ordering</p>
	 * @param filter filter expression. Null means no filtering 
	 * @param ordering ordering expression. Null means no ordering
	 * @throws ContentException on any errors 
	 */
	void setFilterAndOrdering(final String filter, final String ordering) throws ContentException;
}
