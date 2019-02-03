package chav1961.purelib.ui.swing;

import java.io.IOException;
import java.util.Locale;

import javax.swing.JLabel;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.AbstractLowLevelFormFactory.FieldDescriptor;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;

public class JLabelWithMeta extends JLabel implements NodeMetadataOwner, LocaleChangeListener, JComponentInterface {
	private static final 				long serialVersionUID = -2732968182981812331L;
	private final ContentNodeMetadata	metadata;

	public JLabelWithMeta(final ContentNodeMetadata metadata) throws LocalizationException {
		super();
		if (metadata == null) {
			throw new NullPointerException("Metadata can't be null"); 
		}
		else {
			this.metadata = metadata;
			fillLocalizedStrings();
		}
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
	}

	@Override
	public ContentNodeMetadata getNodeMetadata() {
		return metadata;
	}
	
	@Override
	public String getRawDataFromComponent() {
		return getNodeMetadata().getLabelId();
	}

	@Override
	public Object getValueFromComponent() {
		return getRawDataFromComponent();
	}

	@Override
	public Object getChangedValueFromComponent() throws SyntaxException {
		return getRawDataFromComponent();
	}

	@Override
	public void assignValueToComponent(final Object value) {
		throw new IllegalStateException("Label can't be assigned");
	}

	@Override
	public Class<?> getValueType() {
		return String.class;
	}

	@Override
	public String standardValidation(final String value) {
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
	
	private void fillLocalizedStrings() throws LocalizationException {
		try{setText(LocalizerFactory.getLocalizer(getNodeMetadata().getLocalizerAssociated()).getValue(getNodeMetadata().getLabelId()));
		} catch (IOException e) {
			throw new LocalizationException(e);
		}
	}
}