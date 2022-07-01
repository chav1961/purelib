package chav1961.purelib.ui.interfaces;

public interface PresentationOwner<P> {
	Class<P> getPresentationClass();
	P getPresentation();
}
