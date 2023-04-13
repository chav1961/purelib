package chav1961.purelib.concurrent.interfaces;

import chav1961.purelib.concurrent.LightWeightFuture;

/**
 * <p>This interface describes a request to process.</p>  
 * @param <F> type of source data to process
 * @param <T> type of result processed
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.7
 */
public interface RequestProcessor<F,T> {
	/**
	 * <p>Take content to process.</p>
	 * @return content to process. Can't be null
	 */
	F take();
	
	/**
	 * <p>Complete processing and return results. Can be call at once only, subsequent calls will have no effect.</p> 
	 * @param result result to return. Can't be null
	 * @see #fail(Throwable)
	 * @see #reject()
	 */
	void complete(T result);

	/**
	 * <p>Complete processing as failed. Can be call at once only, subsequent calls will have no effect.</p>
	 * @param exception exception to mark failure. Can't be null
	 * @see #complete(Object)
	 * @see #reject()
	 */
	void fail(Throwable exception);
	
	/**
	 * <p>Reject processing. Can be call at once only, subsequent calls will have no effect. Future will be marked as 'cancelled'</p>
	 * @see #complete(Object)
	 * @see LightWeightFuture#fail(Throwable)
	 */
	void reject();
}
