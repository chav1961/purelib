package chav1961.purelib.concurrent.interfaces;

import java.util.concurrent.Future;

/**
 * <p>THis interface is an extension of {@linkplain Future} interface for multi-staged processing.</p> 
 * @param <Stage> processing stage
 * @param <T> processing result
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.7
 */
public interface StagedFuture<Stage extends Enum<?>,T> extends Future<T> {
	Stage currentStage();
}
