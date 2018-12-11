package chav1961.purelib.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.ui.interfacers.FormModel;

public abstract class AbstractInMemoryFormModel<Id,Instance> implements FormModel<Id,Instance> {
	public static final String		ARRAY_INDEX = "@arrayIndex@";
	private static final Set<SupportedOperations>	ALL_OPERATIONS;
	
	static {
		final Set<SupportedOperations>	temp = new HashSet<>();
		
		temp.add(SupportedOperations.INSERT);
		temp.add(SupportedOperations.DUPLICATE);
		temp.add(SupportedOperations.UPDATE);
		temp.add(SupportedOperations.DELETE);
		ALL_OPERATIONS = Collections.unmodifiableSet(temp);
	}
	
	protected final Class<Id>		idClass;
	protected final Class<Instance>	instanceClass;
	protected final String			idField;
	protected final List<Instance>	content = new ArrayList<>();
	protected final MethodHandle	idGetter;
	protected final MethodHandle	idSetter; 
	protected final MethodHandle	cloner;
	protected final boolean 		useIndex;		
	protected int					currentIndex = -1;
	
	public AbstractInMemoryFormModel(final Class<Id> idClass, final Class<Instance> instanceClass, final String idField) throws NullPointerException, ContentException {
		if (idClass == null) {
			throw new NullPointerException("Id class can't be null"); 
		}
		else if (instanceClass == null) {
			throw new NullPointerException("Instance class can't be null");  
		}
		else if (idField == null || idField.isEmpty()) {
			throw new IllegalArgumentException("Id field name can't be null or empty"); 
		}
		else {
			this.idClass = idClass;
			this.instanceClass = instanceClass;
 			this.idField = idField;
			this.idGetter = buildGetter(instanceClass,idField);
			this.idSetter = buildSetter(instanceClass,idField);
			this.cloner = buildCloner(instanceClass);
			
			if (this.idGetter == null || this.idSetter == null) {
				if (!ARRAY_INDEX.equalsIgnoreCase(idField)) {
					throw new ContentException("Instance class ["+instanceClass+"] doesnt'n contain field description ["+idField+"] anywhere");
				}
				else if (!Integer.class.isAssignableFrom(idClass) && !int.class.isAssignableFrom(idClass)) {
					throw new ContentException("IdClass ["+idClass+"] is not compatible with the ["+idField+"] field");
				}
				else {
					useIndex = true;
				}
			}
			else {
				useIndex = false;
			}
		}
	}
	
	public AbstractInMemoryFormModel(final Class<Id> idClass, final Class<Instance> instanceClass, final String idField, final Instance[] content) throws NullPointerException, ContentException {
		if (idClass == null) {
			throw new NullPointerException("Id class can't be null"); 
		}
		else if (instanceClass == null) {
			throw new NullPointerException("Instance class can't be null"); 
		}
		else if (idField == null || idField.isEmpty()) {
			throw new IllegalArgumentException("Id field name can't be null or empty"); 
		}
		else if (content == null) {
			throw new NullPointerException("Content list can't be null"); 
		}
		else {
			this.idClass = idClass;
			this.instanceClass = instanceClass;
			this.idField = idField;
			this.idGetter = buildGetter(instanceClass,idField);
			this.idSetter = buildSetter(instanceClass,idField);
			this.cloner = buildCloner(instanceClass);
			
			if (this.idGetter == null || this.idSetter == null) {
				if (!ARRAY_INDEX.equalsIgnoreCase(idField)) {
					throw new ContentException("Instance class ["+instanceClass+"] doesnt'n contain field description ["+idField+"] anywhere");
				}
				else if (!Integer.class.isAssignableFrom(idClass) && !int.class.isAssignableFrom(idClass)) {
					throw new ContentException("IdClass ["+idClass+"] is not compatible with the ["+idField+"] field");
				}
				else {
					useIndex = true;
				}
			}
			else {
				useIndex = false;
			}
			
			for (int index = 0; index < content.length; index++) {
				if (content[index] == null) {
					throw new NullPointerException("Content list contains null value at index ["+index+"]"); 
				}
				else {
					this.content.add(content[index]);
				}
			}
		}
	}

	@Override public abstract Id createUniqueId() throws ContentException;
	
	@Override
	public Set<SupportedOperations> getOperationsSupported() {
		return ALL_OPERATIONS;
	}
	
	@Override 
	public Instance createInstance(final Id id) throws NullPointerException, ContentException {
		if (id == null) {
			throw new NullPointerException("Record id can't be null"); 
		}
		else if (getIndexById(id) != -1) {
			throw new ContentException("Attempt to create instance with the key ["+id+"] already exists"); 
		}
		else {
			try{final Instance	result = instanceClass.newInstance();

				if (!useIndex) {
					idSetter.invoke(result,id);
				}
				content.add(result);
				setCurrentIndex(content.size()-1);
				return result;
			} catch (Throwable e) {
				throw new ContentException(e.getLocalizedMessage(),e);
			}
		}
	}
	
	@Override 
	public Instance duplicateInstance(final Id oldId, final Id newId) throws NullPointerException, ContentException {
		if (oldId == null) {
			throw new NullPointerException("Old record id can't be null"); 
		}
		else if (newId == null) {
			throw new NullPointerException("New record id can't be null"); 
		}
		else {
			final Instance		oldInstance = getInstance(oldId);
			
			if (oldInstance == null) {
				throw new ContentException("Instance to duplicate ["+oldId+"] is not exists");
			}
			else if (getIndexById(newId) != -1) {
				throw new ContentException("New nnstance key to duplicate ["+newId+"] already exists");
			}
			else if (cloner != null) {	// Optimize using clone()
				try{final Instance	newInstance = (Instance) cloner.invoke(oldInstance);
				
					idSetter.invoke(newInstance,newId);
					content.add(newInstance);
					setCurrentIndex(content.size()-1);
					return newInstance;
				} catch (Throwable e) {
					throw new ContentException(e.getLocalizedMessage(),e);
				}
			}
			else if ((oldInstance instanceof Externalizable) || (oldInstance instanceof Serializable)) {	// Optimize using serialization
				try(final ByteArrayOutputStream		baos = new ByteArrayOutputStream()) {
					try(final ObjectOutputStream	oos = new ObjectOutputStream(baos)) {
						oos.writeObject(oldInstance);
						oos.flush();
						oos.reset();
					}
					
					try(final ByteArrayInputStream	bais = new ByteArrayInputStream(baos.toByteArray());
						final ObjectInputStream		ois = new ObjectInputStream(bais)) {
						final Instance				newInstance = (Instance)ois.readObject();
						
						idSetter.invoke(newInstance,newId);
						content.add(newInstance);
						setCurrentIndex(content.size()-1);
						return newInstance; 
					}
				} catch (Throwable e) {
					throw new ContentException(e.getLocalizedMessage(),e);
				}
			}
			else {	// General case - move values by reflection
				try{final Instance	newInstance = createInstance(newId);
	
					duplicate(instanceClass,oldInstance,newInstance);
					idSetter.invoke(newInstance,newId);
					return newInstance;
				} catch (Throwable e) {
					throw new ContentException(e.getLocalizedMessage(),e);
				}
			}
		}
	}

	@Override
	public Instance getInstance(final Id Id) throws ContentException {
		final int 	index = getIndexById(Id);
		
		if (index < 0 || index >= content.size()) {
			return null;
		}
		else {
			return content.get(index);
		}
	}

	@Override
	public Instance updateInstance(final Id Id, final Instance inst) throws ContentException {
		final int 	index = getIndexById(Id);
		
		if (index < 0 || index >= content.size()) {
			throw new ContentException("Attempt to update non-existgent element with id ["+Id+"]");
		}
		else {
			return content.set(index,inst);
		}
	}

	@Override
	public Instance removeInstance(final Id Id) throws ContentException {
		final int 	index = getIndexById(Id);
		
		if (index < 0 || index >= content.size()) {
			return null;
		}
		else if (getCurrentIndex() == content.size() - 1) {
			setCurrentIndex(getCurrentIndex()-1);
			return content.remove(index);
		}
		else {
			return content.remove(index);
		}
	}

	@Override
	public void refresh() throws ContentException {
	}

	@Override
	public void undo() throws ContentException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public int size() {
		return content.size();
	}

	@Override
	public int getCurrentIndex() {
		return currentIndex;
	}

	@Override
	public void setCurrentIndex(final int index) {
		currentIndex = index;
	}

	@Override
	public int getIndexById(final Id id) {
		for (int index = 0, maxIndex = content.size(); index < maxIndex; index++) {
			try{if (id.equals(idGetter.invoke(content.get(index)))) {
					return index;
				}
			} catch (Throwable e) {
			}
		}
		return -1;
	}

	@Override
	public Id getCurrentId() {
		return getIdByIndex(getCurrentIndex());
	}

	@Override
	public Id getIdByIndex(final int index) {
		if (index < 0 || index >= content.size()) {
			return null;
		}
		else {
			try{return (Id)idGetter.invoke(content.get(index));
			} catch (Throwable e) {
				return null;
			}
		}
	}

	@Override
	public Iterable<Id> contentIds() throws ContentException {
		final Id[]	result = (Id[]) Array.newInstance(idClass,size());
		
		for (int index = 0, maxIndex = content.size(); index < maxIndex; index++) {
			try{result[index] = (Id) idGetter.invoke(content.get(index));
			} catch (Throwable e) {
				throw new ContentException(e.getLocalizedMessage(),e);
			}
		}
		return Arrays.asList(result);
	}

	@Override
	public void setFilterAndOrdering(final String filter, final String ordering) throws ContentException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented yet");
	}

	public Instance[] toArray() {
		return content.toArray((Instance[])Array.newInstance(instanceClass,content.size()));
	}
	
	private MethodHandle buildGetter(final Class<?> instanceClass, final String idField) {
		if (instanceClass == null) {
			return null;
		}
		else {
			try{final Field	f = instanceClass.getDeclaredField(idField);
				
				f.setAccessible(true);
				return MethodHandles.lookup().unreflectGetter(f);
			} catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
				return buildGetter(instanceClass.getSuperclass(),idField);
			}
		}
	}
	
	private MethodHandle buildSetter(final Class<?> instanceClass, final String idField) {
		if (instanceClass == null) {
			return null;
		}
		else {
			try{final Field	f = instanceClass.getDeclaredField(idField);
				
				f.setAccessible(true);
				return MethodHandles.lookup().unreflectSetter(f);
			} catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
				return buildSetter(instanceClass.getSuperclass(),idField);
			}
		}
	}

	private MethodHandle buildCloner(final Class<?> instanceClass) throws ContentException {
		if (instanceClass == null) {
			return null;
		}
		else if (Cloneable.class.isAssignableFrom(instanceClass)) {
			try{final Method	m = instanceClass.getMethod("clone");
				
				return MethodHandles.lookup().unreflect(m);
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException e) {
				return null;
			}
		}
		else {
			return null;
		}
	}

	private void duplicate(final Class<?> clazz, final Instance oldInstance, final Instance newInstance) {
		if (clazz != null) {
			for (Field f : clazz.getDeclaredFields()) {
				f.setAccessible(true);
				try{f.set(newInstance,f.get(oldInstance));
				} catch (IllegalArgumentException | IllegalAccessException e) {
				}
			}
			duplicate(clazz.getSuperclass(),oldInstance,newInstance);
		}
	}
}
