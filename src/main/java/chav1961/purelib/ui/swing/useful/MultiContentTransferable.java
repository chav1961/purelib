package chav1961.purelib.ui.swing.useful;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import chav1961.purelib.basic.Utils;

/**
 * <p>This class is used to keep multi content transferrables.</p>
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.7 
 */
public class MultiContentTransferable implements Transferable, Serializable {
	private static final long serialVersionUID = -8223121510044128638L;
	
	private final Transferable[] 	content;
	private final DataFlavor[]		flavors;

	/**
	 * <p>Constructor of the class instance</p>
	 * @param content transferrable content list to keep. Can't be null, empty and can't contain nulls inside
	 * @throws IllegalArgumentException content is null, empty or contains nulls inside 
	 */
	public MultiContentTransferable(final Transferable... content) throws IllegalArgumentException {
		if (content == null || content.length == 0 || Utils.checkArrayContent4Nulls(content) >= 0) {
			throw new IllegalArgumentException("Content is null, empty or contains nulls inside");
		}
		else {
			final Set<DataFlavor>	result = new HashSet<>();
			
			this.content = content;
			for(Transferable item : content) {
				result.addAll(Arrays.asList(item.getTransferDataFlavors()));
			}
			this.flavors = result.toArray(new DataFlavor[result.size()]);
		}
	}
	
	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return flavors;
	}

	@Override
	public boolean isDataFlavorSupported(final DataFlavor flavor) {
		if (flavor == null) {
			throw new NullPointerException("Data flavor to test can't be null");
		}
		else {
			for(DataFlavor item : flavors) {
				if (item.equals(flavor)) {
					return true;
				}
			}
			return false;
		}
	}

	@Override
	public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (flavor == null) {
			throw new NullPointerException("Data flavor to test can't be null");
		}
		else if (isDataFlavorSupported(flavor)) {
			for(Transferable item : content) {
				if (item.isDataFlavorSupported(flavor)) {
					return item.getTransferData(flavor);
				}
			}
			return null;
		}
		else {
			return null;
		}
	}
}
