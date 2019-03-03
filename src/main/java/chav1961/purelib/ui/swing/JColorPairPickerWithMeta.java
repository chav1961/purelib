package chav1961.purelib.ui.swing;

import java.util.Locale;

import javax.swing.JComponent;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;

public class JColorPairPickerWithMeta extends JComponent implements NodeMetadataOwner, LocaleChangeListener, JComponentInterface {
	private static final long serialVersionUID = -4542351534072177624L;

	@Override
	public String getRawDataFromComponent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getValueFromComponent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getChangedValueFromComponent() throws SyntaxException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void assignValueToComponent(Object value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Class<?> getValueType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String standardValidation(String value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setInvalid(boolean invalid) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isInvalid() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void localeChanged(Locale oldLocale, Locale newLocale) throws LocalizationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ContentNodeMetadata getNodeMetadata() {
		// TODO Auto-generated method stub
		return null;
	}


}
