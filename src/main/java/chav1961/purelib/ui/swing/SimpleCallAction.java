package chav1961.purelib.ui.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;

import chav1961.purelib.ui.interfacers.ControllerAction;

class SimpleCallAction extends AbstractAction {
	private static final long serialVersionUID = 6194849062900094588L;

	private final ControllerAction	controllerAction;
	private final ActionListener	listener;
	
	SimpleCallAction(final ControllerAction controllerAction, final ActionListener listener) {
		this.controllerAction = controllerAction;
		this.listener = listener;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		listener.actionPerformed(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,controllerAction.toString()));
	}
}