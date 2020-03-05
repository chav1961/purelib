package chav1961.purelib.testing;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SwingTestingUtils {

	/**
	 * <p>Request focus for the given component and wait until the request has been completed</p>
	 * @param component component to request focus for
	 * @throws IllegalArgumentException when component is not or is not focusable
	 * @throws InterruptedException interrupted or timeout exception
	 */
	public static void syncRequestFocus(final Component component) throws IllegalArgumentException, InterruptedException {
		if (component == null || !component.isFocusable()) {
			throw new IllegalArgumentException("Component to set focus is null or is not focusable");
		}
		else {
			final CountDownLatch	cdl = new CountDownLatch(1);
			final FocusListener		fl = new FocusListener() {
										@Override public void focusLost(FocusEvent e) {}
									
										@Override
										public void focusGained(FocusEvent e) {
											cdl.countDown();
										}
									};
			
			component.addFocusListener(fl);
			component.requestFocus();
			
			try{if (!cdl.await(1,TimeUnit.SECONDS)) {
					throw new InterruptedException();
				}
			} finally {
				component.removeFocusListener(fl);
			}
		}
	}
}
