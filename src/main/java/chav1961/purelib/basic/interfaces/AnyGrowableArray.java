package chav1961.purelib.basic.interfaces;

/**
 * <p>This interface describes any growable arrays in the Pure Library.</p>
 * @since 0.0.7
 * @author Alexander Chernomyrdin aka chav1961
 */
public interface AnyGrowableArray {
	/**
	 * <p>Get component type for the given array</p>
	 * @return component type. Can't be null. Arrays of primitive types should return primitive class descriptors, not wrappers
	 */
	Class<?> getComponentType();
}
