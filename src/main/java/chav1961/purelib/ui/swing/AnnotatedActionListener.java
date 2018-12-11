package chav1961.purelib.ui.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.ui.swing.interfaces.OnAction;

/**
 * <p>This class is a wrapper to any object, annotated with {@linkplain OnAction}, to process action listeners calls. Constructor of the class
 * finds all annotated methods in required class and builds code to call them when P{@linkplain ActionEvent} is fired.</p>
 * <p>Method annotated can have <i>parameterized</i> annotation content. Parameterized annotation has regular expression instead of exact value in
 * it's body. When any action is firing, it's <i>actionCommand</i> string is matching sequentially with the annotation value using {@linkplain Pattern}
 * and {@linkplain Matcher} classes. On successful matching, Matcher instance will be passed to calling method as it's parameter, so method template 
 * must be:</p>
 * <code>&lt;anyVisibility&gt; void &lt;anyMethodName&gt;(Matcher matcher){...}</code>   
 * <p>Method template for non-parameterized annotation must be:</p>      
 * <code>&lt;anyVisibility&gt; void &lt;anyMethodName&gt;(){...}</code>   
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.2
 */

public class AnnotatedActionListener<T> implements ActionListener {
	private final Map<String,MethodHandle>	actions = new HashMap<>(); 
	private final T							toCall;
	
	public AnnotatedActionListener(final T toCall) throws NullPointerException, EnvironmentException {
		if (toCall == null) {
			throw new NullPointerException("Object to call can't be null"); 
		}
		else {
			this.toCall = toCall;
			try{collectAnnotatedMethods(toCall.getClass(),actions);
			} catch (IllegalAccessException e) {
				throw new EnvironmentException(e.getLocalizedMessage(),e);
			}
			if (actions.size() == 0) {
				throw new IllegalArgumentException("No one method in the class ["+toCall.getClass()+"] is marked with the ["+OnAction.class+"] annotation"); 
			}
		}
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		final String		actionCommand = e.getActionCommand();
		final MethodHandle	handle = actions.get(actionCommand);
		
		if (handle == null) {
			for (Entry<String, MethodHandle> item : actions.entrySet()) {
				final Pattern	pattern = Pattern.compile(item.getKey());
				final Matcher	matcher = pattern.matcher(actionCommand);
				
				if (matcher.matches()) {
					try{item.getValue().invoke(toCall,matcher);
					} catch (Throwable t) {
						t.printStackTrace();
					}
					return;
				}
			}
			throw new IllegalArgumentException("Instance of the class ["+toCall.getClass()+"] not processes action command ["+e.getActionCommand()+"]. Place appropriative @"+OnAction.class+" annotation in the class descriptor"); 
		}
		else {
			try{handle.invoke(toCall);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	private void collectAnnotatedMethods(final Class<?> clazz, final Map<String, MethodHandle> actions) throws IllegalAccessException {
		if (clazz != null) {
			final Set<String>	currentLevelValues = new HashSet<>(); 
			
			for (Method m : clazz.getDeclaredMethods()) {
				if (m.isAnnotationPresent(OnAction.class)) {
					final OnAction	ann = m.getAnnotation(OnAction.class);
					
					if (currentLevelValues.contains(ann.value())) {
						throw new IllegalArgumentException("More than one method in the class has ["+OnAction.class+"] annotation with the same value ["+ann.value()+"]");
					}
					else {
						currentLevelValues.add(ann.value());
						m.setAccessible(true);
						actions.putIfAbsent(ann.value(),MethodHandles.lookup().unreflect(m));
					}
				}
			}
			collectAnnotatedMethods(clazz.getSuperclass(),actions);
		}
	}
}
