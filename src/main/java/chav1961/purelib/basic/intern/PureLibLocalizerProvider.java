package chav1961.purelib.basic.intern;

import java.net.URI;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.i18n.interfaces.DefaultLocalizerProvider;
import chav1961.purelib.i18n.interfaces.Localizer;

public class PureLibLocalizerProvider implements DefaultLocalizerProvider {
	private static final String	SUBSCHEME = "root";
	private static final URI	SERVE = URI.create(DefaultLocalizerProvider.LOCALIZER_PROVIDER_SCHEME+":"+SUBSCHEME+":/");
	
	public PureLibLocalizerProvider() {
	}

	@Override
	public Localizer getLocalizer() {
		return PureLibSettings.PURELIB_LOCALIZER;
	}

	@Override
	public boolean canServe(final URI resource) {
		if (resource == null) {
			throw new NullPointerException("Resource URI can't be null");
		}
		else {
			return URIUtils.canServeURI(resource, SERVE);
		}
	}

	@Override
	public DefaultLocalizerProvider newInstance(final URI resource) {
		if (canServe(resource)) {
			return this;
		}
		else {
			throw new EnvironmentException("Resource URI ["+resource+"] is not supported for this provider");
		}
	}

	@Override
	public Module getModule() {
		return getClass().getModule();
	}
}
