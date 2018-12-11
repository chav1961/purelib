package chav1961.purelib.ui.swing;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.ui.interfacers.ControllerAction;
import chav1961.purelib.ui.interfacers.FormModel;
import chav1961.purelib.ui.interfacers.FormModelProcessor;

class ComplexCallAction<Id,Instance> extends AbstractAction {
	private static final long 						serialVersionUID = 758159968615281933L;
	private final ControllerAction 					action;
	private final FormModel<Id,Instance> 			model;
	private final FormModelProcessor<Id,Instance>	inst;
	
	ComplexCallAction(final ControllerAction action, final FormModel<Id,Instance> model, final FormModelProcessor<Id,Instance> inst) {
		this.action = action;
		this.model = model;
		this.inst = inst;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		try{inst.processAction(model,action);
		} catch (ContentException exc) {
			inst.message(Severity.error,exc.getLocalizedMessage());
		}
	}
}