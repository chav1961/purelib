package chav1961.purelib.ui;

public class FSMJump {
	public String	terminal;
	public int		newState;
	public String	actionCommand;
	
	@Override
	public String toString() {
		return "FSMJump [terminal=" + terminal + ", newState=" + newState + ", actionCommand=" + actionCommand + "]";
	}
}
