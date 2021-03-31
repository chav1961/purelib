package chav1961.purelib.model.interfaces;

import java.io.IOException;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;

public interface UIModelMapper<Data,S,T> extends ModelMapper, JComponentMonitor {
	void download(S source, Data target) throws ContentException, IOException;
	void upload(Data source, T target) throws ContentException, IOException;
}
