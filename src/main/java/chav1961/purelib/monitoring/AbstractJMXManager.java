package chav1961.purelib.monitoring;

import java.util.Map;
import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ReflectionException;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.cdb.CompilerUtils;
import chav1961.purelib.concurrent.LightWeightListenerList;
import chav1961.purelib.monitoring.interfaces.JMXItem;
import chav1961.purelib.monitoring.interfaces.MBeanListener;

class AbstractJMXManager implements DynamicMBean {
	private final LightWeightListenerList<MBeanListener>	listeners = new LightWeightListenerList<>(MBeanListener.class);
	private final SyntaxTreeInterface<ItemDesc>				tree = new AndOrTree<>();
	private final MBeanAttributeInfo[]		attrInfo;
	private final MBeanConstructorInfo[]	constrInfo;
	private final MBeanOperationInfo[]		operInfo;
			
	protected AbstractJMXManager() {
		if (!this.getClass().isAnnotationPresent(JMXItem.class)) {
			throw new IllegalStateException("Class to use as JMXManager must be annotated with @JMXItem");
		}
		else {
			final Map<String,Field>				fields = new HashMap<>();
			final Map<String,Constructor<?>>	constructors = new HashMap<>();
			final Map<String,Method>			methods = new HashMap<>();
			
			CompilerUtils.walkFields(this.getClass(), (cl,f)->{
				if (f.isAnnotationPresent(JMXItem.class)) {
					fields.putIfAbsent(f.getName(), f);
				}
			});
			this.attrInfo = buildAttrInfo(fields);

			CompilerUtils.walkConstructors(this.getClass(), (cl,c)->{
				if (c.isAnnotationPresent(JMXItem.class)) {
					constructors.putIfAbsent(this.getClass().getSimpleName()+CompilerUtils.buildConstructorSignature(c), c);
				}
			});
			this.constrInfo = buildConstrInfo(constructors);
			
			CompilerUtils.walkMethods(this.getClass(), (cl,m)->{
				if (m.isAnnotationPresent(JMXItem.class)) {
					methods.putIfAbsent(m.getName()+CompilerUtils.buildMethodSignature(m), m);
				}
			});
			this.operInfo = buildOperInfo(methods);
		}
	}

	@Override
	public Object getAttribute(final String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
		if (attribute == null || attribute.isEmpty()) {
			throw new IllegalArgumentException("Attribute name can't be null or empty");
		}
		else {
			return getAttr(attribute);
		}
	}

	@Override
	public void setAttribute(final Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
		if (attribute == null) {
			throw new NullPointerException("Attribute to set can't be null");
		}
		else if (attribute.getName() == null || attribute.getName().isEmpty()) {
			throw new IllegalArgumentException("Attribute name can't be null or empty");
		}
		else {
			setAttr(attribute.getName(),attribute.getValue());
		}
	}

	@Override
	public AttributeList getAttributes(final String[] attributes) {
		if (attributes == null || Utils.checkArrayContent4Nulls(attributes) >= 0) {
			throw new NullPointerException("Either attributes self or some items in it are null");
		}
		else {
			final AttributeList	result = new AttributeList();
			
			for (String item : attributes) {
				try{result.add(new Attribute(item,getAttribute(item)));
				} catch (AttributeNotFoundException | MBeanException | ReflectionException e) {
				}
			}
			return result;
		}
	}

	@Override
	public AttributeList setAttributes(AttributeList attributes) {
		if (attributes == null) {
			throw new NullPointerException("Attributes can't be null");
		}
		else {
			for (Object item : attributes) {
				try{setAttribute((Attribute)item);
				} catch (AttributeNotFoundException | InvalidAttributeValueException | MBeanException | ReflectionException e) {
				}
			}
		}
		return null;
	}

	@Override
	public Object invoke(final String actionName, final Object[] params, final String[] signature) throws MBeanException, ReflectionException {
		final StringBuilder	sb = new StringBuilder();
		
		sb.append(actionName).append('(');
		for (String item : signature) {
			sb.append(item);
		}
		sb.append(')');
		
		return invoke(sb.toString(),params);
	} 

	@Override
	public MBeanInfo getMBeanInfo() {
		return new MBeanInfo(this.getClass().getCanonicalName(), this.getClass().getAnnotation(JMXItem.class).value(), attrInfo, constrInfo, operInfo, null);
	}

	public void addMBeanListener(final MBeanListener listener) {
		if (listener == null) {
			throw new NullPointerException("Listener to add can't be null");
		}
		else {
			listeners.addListener(listener);
		}
	}

	public void removeMBeanListener(final MBeanListener listener) {
		if (listener == null) {
			throw new NullPointerException("Listener to remove can't be null");
		}
		else {
			listeners.removeListener(listener);
		}
	}
	
	protected synchronized Object getAttr(final String attr) throws AttributeNotFoundException, MBeanException, ReflectionException {
		// TODO Auto-generated method stub
		return null;
	}

	protected synchronized void setAttr(final String attr, final Object value) throws AttributeNotFoundException, MBeanException, ReflectionException {
		// TODO Auto-generated method stub
		final Object	oldVal = getAttr(attr);
		
		listeners.fireEvent((l)->{
			try{l.valueChanged(attr, oldVal, value);
			} catch (ContentException e) {
			}
		});
	}

	protected synchronized Object invoke(final String signature, final Object[] params) {
		// TODO Auto-generated method stub
		try {final Object	result = callMethod(signature,params);
			
			listeners.fireEvent((l)->{
				try{l.actionCalled(signature, params, result);
				} catch (ContentException e) {
				}
			});
			return result;
		} catch (final Throwable exception) {
			listeners.fireEvent((l)->{
				try{l.actionCalled(signature, params, exception);
				} catch (ContentException e) {
				}
			});
			return null;
		}
	}
	
	private Object callMethod(String signature, Object[] params) {
		// TODO Auto-generated method stub
		return null;
	}

	private static MBeanAttributeInfo[] buildAttrInfo(final Map<String, Field> fields) {
		final List<MBeanAttributeInfo>	result = new ArrayList<>();
		
		for (Entry<String, Field> item : fields.entrySet()) {
			final Field		f = item.getValue();
			
			result.add(new MBeanAttributeInfo(f.getName(),f.getType().getCanonicalName(),f.getAnnotation(JMXItem.class).value(),true,Modifier.isFinal(f.getModifiers()),false));
		}
		return result.toArray(new MBeanAttributeInfo[result.size()]);
	}
	
	private static MBeanConstructorInfo[] buildConstrInfo(final Map<String, Constructor<?>> constructors) {
		final List<MBeanConstructorInfo>	result = new ArrayList<>();
		
		for (Entry<String, Constructor<?>> item : constructors.entrySet()) {
			final Constructor<?>		c = item.getValue();
			
			result.add(new MBeanConstructorInfo(c.getName(),c.getAnnotation(JMXItem.class).value(),buildParameterInfo(c.getParameters())));
		}
		return result.toArray(new MBeanConstructorInfo[result.size()]);
	}

	private MBeanOperationInfo[] buildOperInfo(final Map<String, Method> methods) {
		final List<MBeanOperationInfo>	result = new ArrayList<>();
		
		for (Entry<String, Method> item : methods.entrySet()) {
			final Method		m = item.getValue();
			
			result.add(new MBeanOperationInfo(m.getAnnotation(JMXItem.class).value(),m));
		}
		return result.toArray(new MBeanOperationInfo[result.size()]);
	}
	
	private static MBeanParameterInfo[] buildParameterInfo(final Parameter[] parameters) {
		final List<MBeanParameterInfo>	result = new ArrayList<>();
		
		for (Parameter item : parameters) {
			result.add(new MBeanParameterInfo(item.getName(),item.getType().getCanonicalName(),item.getAnnotation(JMXItem.class).value()));
		}
		return result.toArray(new MBeanParameterInfo[result.size()]);
	}

	private static class ItemDesc {
		
	}
}
