package chav1961.purelib.ui.swing;

import java.util.concurrent.Semaphore;

import chav1961.purelib.ui.swing.interfaces.OnAction;

class AnnotatedWithOnAction  {
	volatile Semaphore	sema = new Semaphore(0);
	volatile boolean	wasCall1 = false, wasCall2 = false, wasCall3 = false;
	
	void clear() {
		wasCall1 = wasCall2 = wasCall3 = false;
	}
	
	@OnAction("action1")
	public void call1() {
		wasCall1 = true;
	}

	@OnAction(value="action2",async=true)
	public void call2() {
		wasCall2 = true;
		sema.release();
	}

	@OnAction("action3")
	public void call3() {
		wasCall3 = true;
	}
}