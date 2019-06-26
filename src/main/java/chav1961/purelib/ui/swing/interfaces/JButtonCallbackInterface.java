package chav1961.purelib.ui.swing.interfaces;

@FunctionalInterface
public interface JButtonCallbackInterface {
	public enum ButtonType {
		ACCEPT_BUTTON, OK_BUTTON, CANCEL_BUTTON, YES_BUTTON, NO_BUTTON, CUSTOM_BUTTON 
	}
	
	boolean process(ButtonType type, String actionString);
}
