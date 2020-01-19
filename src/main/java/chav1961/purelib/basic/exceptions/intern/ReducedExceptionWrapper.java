package chav1961.purelib.basic.exceptions.intern;

public class ReducedExceptionWrapper extends Throwable {
	private static final long serialVersionUID = 3973043354968237478L;

	private final Throwable	content;
	
	public ReducedExceptionWrapper(final String message, final Throwable t) {
	    super(message, null, true, false);
	    this.content = t;
	}
	
	public Throwable getWrappedException() {
		return content;
	}
}
