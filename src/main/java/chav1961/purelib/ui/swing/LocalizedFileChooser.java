package chav1961.purelib.ui.swing;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JToolTip;
import javax.swing.filechooser.FileFilter;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.PureLibLocalizer;
import chav1961.purelib.i18n.interfaces.Localizer;

public class LocalizedFileChooser extends JFileChooser {
	private static final long serialVersionUID = 3787155838929041708L;

	private final Localizer	localizer;
	
	public LocalizedFileChooser(final Localizer localizer) {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else {
			this.localizer = localizer;
		}
	}

	@Override
	public JToolTip createToolTip() {
		return new SmartToolTip(localizer,this);
	}

	@Override
	public String getToolTipText() {
		final String	result = super.getToolTipText();
		
		if (result != null && !result.isEmpty()) {
			try{return localizer.getValue(result);
			} catch (LocalizationException e) {
				return result;
			}
		}
		else {
			return result;
		}
	}
	
	@Override
    public void setFileFilter(final FileFilter filter) {
		if (filter == null) {
			throw new NullPointerException("File filter can't be null");
		}
		else {
			final FileFilter	localized = new FileFilter() {
									@Override
									public String getDescription() {
										final String	desc = filter.getDescription();
										
										if (desc != null && localizer != null) {
											try{return localizer.getValue(desc);
											} catch (LocalizationException | IllegalArgumentException e) {
												return filter.getDescription();
											}
										}
										else {
											return desc;
										}
									}
									
									@Override
									public boolean accept(final File f) {
										return filter.accept(f);
									}
								};
	    	super.setFileFilter(localized);
		}
    }
	
	@Override
    public String getApproveButtonText() {
		final String	result = super.getApproveButtonText();
		
		if (result != null && !result.isEmpty()) {
			try{return localizer.getValue(result);
			} catch (LocalizationException e) {
				return result;
			}
		}
		else {
			return result;
		}
    }
	
	@Override
    public String getApproveButtonToolTipText() {
		final String	result = super.getApproveButtonToolTipText();
		
		if (result != null && !result.isEmpty()) {
			try{return localizer.getValue(result);
			} catch (LocalizationException e) {
				return result;
			}
		}
		else {
			return result;
		}
    }
	
    @Deprecated
	@Override
    public void show() {
		try{switch (getDialogType()) {
				case OPEN_DIALOG	:
					setDialogTitle(localizer.getValue(PureLibLocalizer.TITLE_FILECHOOSER_OPEN));
					getApproveButtonToolTipText();
	    			break;
				case SAVE_DIALOG	:
					setDialogTitle(localizer.getValue(PureLibLocalizer.TITLE_FILECHOOSER_SAVEAS));
	    			break;
	    		case CUSTOM_DIALOG	:
	    			final String	dialogTitle = getDialogTitle();
	    			
	    			if (localizer.containsKey(dialogTitle)) {
	        			setDialogTitle(localizer.getValue(dialogTitle));
	    			}
	    			break;
	    	}
		} catch (LocalizationException e) {
		}
    	super.show();
    }
}
