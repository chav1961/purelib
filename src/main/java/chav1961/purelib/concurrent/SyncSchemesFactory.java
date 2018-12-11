package chav1961.purelib.concurrent;

import java.util.function.Consumer;

class SyncSchemesFactory {
	@FunctionalInterface
	public interface Producer<T> {
		T generate();
	}

	@FunctionalInterface
	public interface Transform<S,T> {
		T process(S content);
	}
	
	@FunctionalInterface
	public interface ProducerFactory<T> {
		Producer<T> createProducer();
		default void dropProducer(Producer<T> producer){}
	}

	@FunctionalInterface
	public interface TransformFactory<S,T> {
		Transform<S,T> createTransform();
		default void dropTransform(Transform<S,T> transform){}
	}
	
	@FunctionalInterface
	public interface ConsumerFactory<T> {
		Consumer<T> createConsumer();
		default void dropConsumer(Consumer<T> consumer){}
	}	
	
	public static <T> ProducerAndConsumer buildProducerAndConsumerPair(final ProducerFactory<T> producerFactory, final ConsumerFactory<T> consumerFactory) {
		return null;
	}

	public static <S,T> ProducerConsumerPipe buildProducerConsumerPipe(final ProducerFactory<S> producerFactory, final ConsumerFactory<T> consumerFactory, final TransformFactory<?,?>... convertors) {
		return null;
	}

	public static <S,T> MapperReducer<S,T> buildMapperReducer(final TransformFactory<S,T> convertor) {
		return null;
	}
}
