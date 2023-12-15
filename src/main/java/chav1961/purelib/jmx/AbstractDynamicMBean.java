package chav1961.purelib.jmx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;

import chav1961.purelib.basic.Utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * <p>This class is used to implements JMX Bean with {@linkplain DynamicMBean} implementation. To use this class, you must create child class from it and
 * mark it's constructors/getters/setters/actions and the self class with {@linkplain JMXItemType} annotation. All the entities marked will be included in the MBean. 
 * Annotation mast contain string descriptions for all entities to include, and type for getter/setter methods:</p>
 * 
 * <code>
 * @JMXItemType(description="My class")
 * class	ZZZ {
 * 		@JMXItemType(description="Constructor")
 * 		public ZZZ() {
 * 			. . .
 * 		}
 * 		. . .
 * 
 * 		@JMXItemType(type=JMXKind.GETTER,description="X getter")
 * 		public int getX() {
 * 			. . .
 * 		}
 * 
 * 		@JMXItemType(type=JMXKind.SETTER,description="X setter")
 * 		public void setX(int val) {
 * 			. . .
 * 		}
 * 
 * 		@JMXItemType(description="execute method")
 * 		public void execute() {
 * 			. . .
 * 		}
 * 			. . .
 * }
 * </code>
 * 
 * <p>This class can be used in multi-tasking environment</p> 
 * 
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.7
 */

public abstract class AbstractDynamicMBean implements DynamicMBean {
	/**
	 * <p>This enumeration is sued in conjunction with {@linkplain JMXItemType} annotation to mark MBEan components in your child class.</p> 
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.7
	 */
	public static enum JMXKind {
		GETTER,
		SETTER,
		OTHER
	}

	/**
	 * <p>This annotation ia used to mark getters/setters/actions and class itself to use with MBeans.</p>
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.7
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR})
	public static @interface JMXItemType {
		JMXKind type() default JMXKind.OTHER;
		String description();
	}
	
    private final MBeanAttributeInfo[]		attributes;
    private final MBeanConstructorInfo[] 	constructors;
    private final MBeanOperationInfo[] 		operations;
    private final MBeanInfo 				dMBeanInfo;
    private final MethodHandle[][]			attrMH;
    private final MethodHandle[]			operMH;
	
    /**
     * <p>Constructor of the class</p>
     * @throws NoSuchMethodException usually not throws
     * @throws IllegalAccessException usually not throws
     */
	protected AbstractDynamicMBean() throws NoSuchMethodException, IllegalAccessException {
	    if (!getClass().isAnnotationPresent(JMXItemType.class)) {
	    	throw new IllegalArgumentException("Class ["+getClass().getCanonicalName()+"] must have @JMXItemType annotation");
	    }
	    else if (getClass().getAnnotation(JMXItemType.class).type() != JMXKind.OTHER) {
	    	throw new IllegalArgumentException("Class ["+getClass().getCanonicalName()+"] must have @JMXItemType annotation with illegal type ["+getClass().getAnnotation(JMXItemType.class).type()+"]. Only ["+JMXKind.OTHER+"] is valid here");
	    }
	    else {
		    final List<MBeanAttributeInfo>		forAttr = new ArrayList<>();
		    final List<MethodHandle[]>			forAttrMH = new ArrayList<>();
		    final List<MBeanConstructorInfo>	forConstructors = new ArrayList<>();
		    final List<MBeanOperationInfo>		forOperations = new ArrayList<>();
		    final List<MethodHandle>			forOperMH = new ArrayList<>();
		    final Map<String,Method[]>			forAttrPairs = new HashMap<>();
		    final MethodHandles.Lookup 			publicLookup = MethodHandles.publicLookup();		    
		    
		    for (Constructor<?> entity : getClass().getDeclaredConstructors()) {
		    	if (entity.isAnnotationPresent(JMXItemType.class)) {
		    	    if (entity.getAnnotation(JMXItemType.class).type() != JMXKind.OTHER) {
		    	    	throw new IllegalArgumentException("Constructor ["+entity+"] has @JMXItemType annotation with illegal type ["+getClass().getAnnotation(JMXItemType.class).type()+"]. Only ["+JMXKind.OTHER+"] is valid here");
		    	    }
		    	    else {
		    	        forConstructors.add(new MBeanConstructorInfo(entity.getAnnotation(JMXItemType.class).description(), entity));
		    	    }
		    	}
		    }
		    if (forConstructors.isEmpty()) {
    	    	throw new IllegalArgumentException("At least one constructor must be marked with @JMXItemType annotation");
		    }
		    
		    for (Method entity : getClass().getMethods()) {
		    	if (entity.isAnnotationPresent(JMXItemType.class)) {
		    		switch (entity.getAnnotation(JMXItemType.class).type()) {
	    				case GETTER	:
	    					if (entity.getParameterCount() > 0) {
	    		    	    	throw new IllegalArgumentException("Getter ["+entity+"] can't have any parameters");
	    					}
	    					else if (!entity.getName().startsWith("get") && !(entity.getName().startsWith("is") && (entity.getReturnType() == boolean.class || entity.getReturnType() == Boolean.class))) {
	    		    	    	throw new IllegalArgumentException("Getter ["+entity+"] must have name 'get***()' (or 'is***()' for booleans)");
	    					}
	    					else if(entity.getReturnType() == void.class || entity.getReturnType() == Void.class) {
	    		    	    	throw new IllegalArgumentException("Getter ["+entity+"] can't return void");
	    					}
	    					else {
	    						final String	name = entity.getName().startsWith("is") ? entity.getName().substring(2) : entity.getName().substring(3);
	    						
	    						if (!forAttrPairs.containsKey(name)) {
	    							forAttrPairs.put(name, new Method[2]);
	    						}
	    						forAttrPairs.get(name)[0] = entity;
	    					}
	    					break;
	    				case SETTER	:
	    					if (!entity.getName().startsWith("set")) {
	    		    	    	throw new IllegalArgumentException("Setter ["+entity+"] must have name 'set***(...)'");
	    					}
	    					else if (entity.getParameterCount() != 1) {
	    		    	    	throw new IllegalArgumentException("Setter ["+entity+"] must have exactly one parameter");
	    					}
	    					else {
	    						final String	name = entity.getName().substring(3);
	    						
	    						if (!forAttrPairs.containsKey(name)) {
	    							forAttrPairs.put(name, new Method[2]);
	    						}
	    						forAttrPairs.get(name)[1] = entity;
	    					}
	    					break;
		    			case OTHER 	:
    		    	    	final MethodType 			mt = MethodType.methodType(entity.getReturnType(), entity.getParameterTypes());
			    	    	final MBeanParameterInfo[]	parms = new MBeanParameterInfo[entity.getParameterCount()];

			    	    	for(int index = 0; index < parms.length; index++) {
			    	    		if (entity.getParameters()[index].isAnnotationPresent(JMXItemType.class)) {
			    		    	    if (entity.getParameters()[index].getAnnotation(JMXItemType.class).type() != JMXKind.OTHER) {
			    		    	    	throw new IllegalArgumentException("Method ["+entity+"], parameter ["+index+"] has @JMXItemType annotation with illegal type ["+getClass().getAnnotation(JMXItemType.class).type()+"]. Only ["+JMXKind.OTHER+"] is valid here");
			    		    	    }
			    		    	    else {
					    	    		parms[index] = new MBeanParameterInfo(entity.getParameters()[index].getName(), entity.getParameters()[index].getType().getCanonicalName(), entity.getParameters()[index].getAnnotation(JMXItemType.class).description());
			    		    	    }
			    	    		}
			    	    		else {
				    	    		parms[index] = new MBeanParameterInfo(entity.getParameters()[index].getName(), entity.getParameters()[index].getType().getCanonicalName(), entity.getParameters()[index].getName());
			    	    		}
			    	    	}
			    	        forOperations.add(new MBeanOperationInfo(entity.getAnnotation(JMXItemType.class).description(), entity.getName(), parms, entity.getReturnType().getCanonicalName(), MBeanOperationInfo.ACTION));
			    	        forOperMH.add(publicLookup.findVirtual(getClass(), entity.getName(), mt));
			    	        break;
		    		}
		    	}
		    }

		    for(Entry<String, Method[]> entity : forAttrPairs.entrySet()) {
		    	if (entity.getValue()[0] != null && entity.getValue()[1] != null) {
		    		if (!entity.getValue()[0].getReturnType().isAssignableFrom(entity.getValue()[1].getParameters()[0].getType())) {
		    	    	throw new IllegalArgumentException("Setter ["+entity.getValue()[1]+"] has parameter type ["+entity.getValue()[1].getParameters()[0].getType()+"] incompatible with getter ["+entity.getValue()[0]+"] returned value type ["+entity.getValue()[0].getReturnType()+"]");
		    		}
		    		else {
		    	        forAttr.add(new MBeanAttributeInfo(entity.getKey(), 
		    	        				entity.getValue()[0].getReturnType().getCanonicalName(), 
		    	        				entity.getValue()[0].getAnnotation(JMXItemType.class).description(),
		    	        				true,
		    	        				true,
		    	        				entity.getValue()[0].getName().startsWith("is"))
		    	        );
		    		}
		    	}
		    	else if (entity.getValue()[1] == null) {
	    	        forAttr.add(new MBeanAttributeInfo(entity.getKey(), 
	        				entity.getValue()[0].getReturnType().getCanonicalName(), 
	        				entity.getValue()[0].getAnnotation(JMXItemType.class).description(),
	        				true,
	        				false,
	        				entity.getValue()[0].getName().startsWith("is"))
	    	        );
		    	}
		    	else {
	    	        forAttr.add(new MBeanAttributeInfo(entity.getKey(), 
	        				entity.getValue()[1].getParameters()[0].getType().getCanonicalName(), 
	        				entity.getValue()[1].getAnnotation(JMXItemType.class).description(),
	        				false,
	        				true,
	        				false)
	    	        );
		    	}
		    	final MethodHandle[]	mh = new MethodHandle[2];
		    	
		    	forAttrMH.add(mh);
		    	if (entity.getValue()[0] != null) {
		    		mh[0] = publicLookup.findVirtual(getClass(), entity.getValue()[0].getName(), MethodType.methodType(entity.getValue()[0].getReturnType()));
		    	}
		    	if (entity.getValue()[1] != null) {
		    		mh[1] = publicLookup.findVirtual(getClass(), entity.getValue()[1].getName(), MethodType.methodType(entity.getValue()[1].getReturnType(), entity.getValue()[1].getParameterTypes()));
		    	}
		    }
		    
		    if (forAttr.isEmpty() && forOperations.isEmpty()) {
    	    	throw new IllegalArgumentException("Neither any getters/setters nor any methods were marked with @JMXItemType annotation in this class");
		    }
		    else {
				this.attributes = forAttr.toArray(new MBeanAttributeInfo[forAttr.size()]);
				this.constructors = forConstructors.toArray(new MBeanConstructorInfo[forConstructors.size()]);
				this.operations = forOperations.toArray(new MBeanOperationInfo[forOperations.size()]);
				this.dMBeanInfo = new MBeanInfo(getClass().getCanonicalName(), 
									getClass().getAnnotation(JMXItemType.class).description(),
			                        attributes,
			                        constructors,
			                        operations,
			                        new MBeanNotificationInfo[0]
			                       );
				this.attrMH = forAttrMH.toArray(new MethodHandle[forAttrMH.size()][]);
				this.operMH = forOperMH.toArray(new MethodHandle[forOperMH.size()]);
		    }
	    }
	}

	@Override
	public MBeanInfo getMBeanInfo() {
		return dMBeanInfo;
	}
	
	@Override
	public Object getAttribute(final String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
		if (Utils.checkEmptyOrNullString(attribute)) {
            throw new RuntimeOperationsException(new IllegalArgumentException("Attribute name can't be null or empty"),"Cannot get attribute in the class [" + getClass().getCanonicalName() + "]");
		}
		else {
			for (int index = 0; index < attributes.length; index++) {
				if (attributes[index].getName().endsWith(attribute)) {
					if (attrMH[index][0] != null) {
						try{
							return attrMH[index][0].invokeExact(this);
						} catch (Exception e) {
							throw new ReflectionException(e);
						} catch (Throwable e) {
							throw new ReflectionException(new InvocationTargetException(e));
						} 
					}
					else {
			            throw new RuntimeOperationsException(new IllegalArgumentException("Attribute ["+attribute+"] doesn't have getter"),"Cannot get attribute in the class [" + getClass().getCanonicalName() + "]");
					}
				}
			}
			throw new AttributeNotFoundException(attribute);
		}
	}
	
	@Override
	public void setAttribute(final Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
		if (attribute == null) {
            throw new RuntimeOperationsException(new IllegalArgumentException("Attribute can't be null"),"Cannot set attribute in the class [" + getClass().getCanonicalName() + "]");
		}
		else {
			for (int index = 0; index < attributes.length; index++) {
				if (attributes[index].getName().endsWith(attribute.getName())) {
					if (attrMH[index][1] != null) {
						try{
							attrMH[index][1].invokeExact(this, attribute.getValue());
						} catch (Exception e) {
							throw new ReflectionException(e);
						} catch (Throwable e) {
							throw new ReflectionException(new InvocationTargetException(e));
						} 
					}
					else {
			            throw new RuntimeOperationsException(new IllegalArgumentException("Attribute ["+attribute.getName()+"] doesn't have setter"),"Cannot set attribute in the class [" + getClass().getCanonicalName() + "]");
					}
				}
			}
			throw new AttributeNotFoundException(attribute.getName());
		}
	}
	
	@Override
	public AttributeList getAttributes(final String[] attributes) {
		if (attributes == null || attributes.length == 0 || Utils.checkArrayContent4Nulls(attributes, true) >= 0) {
            throw new RuntimeOperationsException(new IllegalArgumentException("Attribute list is null, empty or contains nulls/empties inside"),"Cannot get attributes in the class [" + getClass().getCanonicalName() + "]");
		}
		else {
			final AttributeList	result = new AttributeList();
			
			for(String attr : attributes) {
				try {
					result.add(new Attribute(attr,getAttribute(attr)));
				} catch (AttributeNotFoundException | MBeanException | ReflectionException e) {
		            throw new RuntimeOperationsException(new IllegalArgumentException("error creating attribute list", e),"Cannot get attributes in the class [" + getClass().getCanonicalName() + "]");
				}
			}
			return result;
		}
	}
	
	@Override
	public AttributeList setAttributes(final AttributeList attributes) {
		if (attributes == null || attributes.isEmpty()) {
            throw new RuntimeOperationsException(new IllegalArgumentException("Attribute list is null or empty"),"Cannot set attributes in the class [" + getClass().getCanonicalName() + "]");
		}
		else {
			for(Object attr : attributes) {
				try {
					setAttribute((Attribute) attr);
				} catch (AttributeNotFoundException | MBeanException | ReflectionException | InvalidAttributeValueException e) {
		            throw new RuntimeOperationsException(new IllegalArgumentException("error creating attribute list", e),"Cannot get attributes in the class [" + getClass().getCanonicalName() + "]");
				}
			}
			return attributes;
		}
	}
	
	@Override
	public Object invoke(final String actionName, final Object[] params, final String[] signature) throws MBeanException, ReflectionException {
		if (Utils.checkEmptyOrNullString(actionName)) {
            throw new RuntimeOperationsException(new IllegalArgumentException("Action name can't be null or empty"),"Cannot invoke method in the class [" + getClass().getCanonicalName() + "]");
		}
		else if (params == null) {
            throw new RuntimeOperationsException(new NullPointerException("Parameters list can't be null"),"Cannot invoke method in the class [" + getClass().getCanonicalName() + "]");
		}
		else if (signature == null) {
            throw new RuntimeOperationsException(new NullPointerException("Signature list can't be null"),"Cannot invoke method in the class [" + getClass().getCanonicalName() + "]");
		}
		else if (params.length != signature.length) {
            throw new RuntimeOperationsException(new IllegalArgumentException("Parameters list size ["+params.length+"] is differ than signature list size ["+signature.length+"]"),"Cannot invoke method in the class [" + getClass().getCanonicalName() + "]");
		}
		else {
			for(int index = 0; index < operations.length; index++) {
				if (operations[index].getName().equals(actionName) && isSignatureEquals(operations[index].getSignature(), signature)) {
					final Object[]	p = new Object[params.length + 1];
					
					p[0] = this;
					System.arraycopy(params, 0, p, 1, params.length);
					
					try {
						return operMH[index].invokeExact(this, p);
					} catch (Exception e) {
						throw new ReflectionException(e);
					} catch (Throwable e) {
						throw new ReflectionException(new InvocationTargetException(e));
					}
				}
			}
            throw new RuntimeOperationsException(new NullPointerException("Method ["+actionName+"] with signature ["+Arrays.toString(signature)+"] not found"),"Cannot invoke method in the class [" + getClass().getCanonicalName() + "]");
		}
	}

	private boolean isSignatureEquals(final MBeanParameterInfo[] left, final String[] right) {
		if (left.length != right.length) {
			return false;
		}
		else {
			for(int index = 0; index < left.length; index++) {
				if (!left[index].getType().equals(right[index])) {
					return false;
				}
			}
			return true;
		}
	}
}
