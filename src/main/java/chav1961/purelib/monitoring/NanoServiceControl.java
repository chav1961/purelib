package chav1961.purelib.monitoring;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.ReflectionException;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.monitoring.interfaces.JMXItem;

@JMXItem("librarySettings")
public class NanoServiceControl extends AbstractJMXManager {
	public NanoServiceControl() {
	}
}