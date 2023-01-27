package chav1961.purelib.ui.swing.useful;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.TransferHandler;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.ui.swing.SwingUtils;

public abstract class JDropTargetPlaceholder extends JLabel implements LocaleChangeListener {
	private static final long serialVersionUID = 1L;
	
	private final Localizer		localizer;
	private final String		placeholderTooltip;
	private final DataFlavor[]	flavorsSupported;
	
	public JDropTargetPlaceholder(final Localizer localizer, final String placeholderTooltip, final DataFlavor... flavorsSupported) {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
		}
		else if (placeholderTooltip == null || placeholderTooltip.isEmpty()) {
			throw new IllegalArgumentException("Placeholder tooltip can't be null or empty"); 
		}
		else if (flavorsSupported == null || flavorsSupported.length == 0 || Utils.checkArrayContent4Nulls(flavorsSupported) >= 0) {
			throw new IllegalArgumentException("Data flavors is null, empty or contains nulls inside"); 
		}
		else {
			this.localizer = localizer;
			this.placeholderTooltip = placeholderTooltip;
			this.flavorsSupported = flavorsSupported.clone();
			
			setFocusable(true);
			SwingUtils.assignActionKey(this, SwingUtils.KS_PASTE, (e)->pasteClipboard(), "paste");
			setTransferHandler(new InnerTransferHandler());
			fillLocalizedStrings();
		}
	}

	protected abstract boolean processDropOperation(final DataFlavor flavor, final Object content) throws ContentException, IOException ;
	
	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
	}

	private void pasteClipboard() {
		final Clipboard	cb = Toolkit.getDefaultToolkit().getSystemClipboard();
		
		for (DataFlavor item : flavorsSupported) {
			if (cb.isDataFlavorAvailable(item)) {
				try{processDropOperation(item, cb.getData(item));
				} catch (ContentException | IOException | UnsupportedFlavorException exc) {
					SwingUtils.getNearestLogger(JDropTargetPlaceholder.this).message(Severity.error, exc, "Error processing paste: "+exc.getLocalizedMessage());
				}
			}
		}
	}

	private void fillLocalizedStrings() {
		setText(localizer.getValue(placeholderTooltip));
	}
	
	private class InnerTransferHandler extends TransferHandler {
		private static final long serialVersionUID = 9157341018090529555L;

		@Override
		public boolean canImport(final TransferSupport support) {
			if (support.isDrop() && (support.getSourceDropActions() & COPY) == COPY && isAnyAvailableFlavorSupported(support)) {
		        support.setDropAction(COPY);
		        return true;
			}
			else {
				return false;
			}
		}
		
		@Override
		public boolean importData(final TransferSupport support) {
			if (!canImport(support)) {
	            return false;
	        }
			else {
				boolean	result = false;
				
				for (DataFlavor item : support.getTransferable().getTransferDataFlavors()) {
					try{
						result |= processDropOperation(item, support.getTransferable().getTransferData(item));
					} catch (UnsupportedFlavorException | IOException | ContentException exc) {
						SwingUtils.getNearestLogger(JDropTargetPlaceholder.this).message(Severity.error, exc, "Error processing drop: "+exc.getLocalizedMessage());
						return false;
					}
				}
				return result;
			}
		}
		
		private boolean isAnyAvailableFlavorSupported(final TransferSupport support) {
			for (DataFlavor item :flavorsSupported) {
				if (support.getTransferable().isDataFlavorSupported(item)) {
					return true;
				}
			}
			return false;
		}
	}
}
