package chav1961.purelib.basic.interfaces;

/**
 * <p>This interface describes any SPI member factory. Every purelib SPI factory implementation <i>must</i> implements this interface (see META-INF/services for details)</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.7
 * @param <Type> any type of the SPI service
 */
public interface SpiServiceFactory<Type> extends SpiService<Type> {
	Class<Type> getSpiServiceClass();
	Class<?> getSpiServiceFactoryClass();
}
