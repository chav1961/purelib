package chav1961.purelib.ui.swing;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JComponent;

class InternalUtils {
	interface ComponentListenerCallback {
		void process();
	}
	
	static boolean checkClassTypes(final Class<?> toTest, final Class<?>... available) {
		for (Class<?> item : available) {
			if (item.isAssignableFrom(toTest)) {
				return true;
			}
		}
		return false;
	}
	
	static void addComponentListener(final JComponent component, final ComponentListenerCallback callback) {
		component.addComponentListener(new ComponentListener() {
			private boolean	loaded = false;
			
			@Override 
			public void componentResized(ComponentEvent e) {
				if (!loaded) {
					callback.process();
					loaded = true;
				}
			}
			
			@Override 
			public void componentMoved(ComponentEvent e) {
				if (!loaded) {
					callback.process();
					loaded = true;
				}
			}
			
			@Override 
			public void componentHidden(ComponentEvent e) {
				if (!loaded) {
					callback.process();
					loaded = true;
				}
			}
			
			@Override
			public void componentShown(ComponentEvent e) {
				if (!loaded) {
					callback.process();
					loaded = true;
				}
			}				
		});
	}
}
