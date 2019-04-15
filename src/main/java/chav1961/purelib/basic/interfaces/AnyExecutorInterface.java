package chav1961.purelib.basic.interfaces;

@FunctionalInterface
public interface AnyExecutorInterface<Result,Type> {
	Result execute(Type parameter, Object... advanced) throws Exception;
}
