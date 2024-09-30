package chav1961.purelib.ui.swing.useful;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class FileTransferable implements Transferable {
    private final DataFlavor[]				flavors = {DataFlavor.javaFileListFlavor};
	private final List<JFileItemDescriptor>	files;

    public FileTransferable(final JFileItemDescriptor... files) throws IllegalArgumentException {
    	this(Arrays.asList(files));
    }	
	
    public FileTransferable(final Collection<JFileItemDescriptor> files) throws IllegalArgumentException {
    	if (files == null || files.isEmpty()) {
    		throw new IllegalArgumentException("File list can't be null or empty collection"); 
    	}
    	else {
            this.files = Collections.unmodifiableList(new ArrayList<JFileItemDescriptor>(files));
    	}
    }

    public DataFlavor[] getTransferDataFlavors() {
        return flavors;
    }

    public boolean isDataFlavorSupported(final DataFlavor flavor) {
        return DataFlavor.javaFileListFlavor.equals(flavor);
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (isDataFlavorSupported(flavor)) {
            return files;
        }
        else {
        	return null;
        }
    }

	@Override
	public String toString() {
		return "FileTransferable [flavors=" + Arrays.toString(flavors) + ", files=" + files + "]";
	}
}