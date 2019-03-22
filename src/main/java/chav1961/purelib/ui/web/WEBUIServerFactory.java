package chav1961.purelib.ui.web;

import java.net.URI;

import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.interfaces.SpiService;
import chav1961.purelib.ui.interfaces.UIServer;
import chav1961.purelib.ui.interfaces.UIServerFactory;

public class WEBUIServerFactory implements UIServerFactory, SpiService<UIServerFactory> {

	@Override
	public UIServer newServer(SubstitutableProperties props) throws ContentException, EnvironmentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canServe(URI resource) throws NullPointerException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public UIServerFactory newInstance(URI resource) throws EnvironmentException, NullPointerException, IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

}
