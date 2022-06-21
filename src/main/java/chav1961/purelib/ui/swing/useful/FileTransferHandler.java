package chav1961.purelib.ui.swing.useful;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.useful.interfaces.FileContentKeeper;

class FileTransferHandler extends TransferHandler {
	private static final long serialVersionUID = 109812767523913960L;

	@Override
	public int getSourceActions(final JComponent source) {
		return COPY_OR_MOVE;
	}

	@Override
	protected Transferable createTransferable(final JComponent source) throws IllegalArgumentException {
		if (!(source instanceof FileContentKeeper)) {
			throw new IllegalArgumentException("Source control doesn't implements FileContentKeeper interface");
		}
		else {
			return new FileTransferable(((FileContentKeeper)source).getSelectedFileContent());
		}
	}
	
	public static void prepare4DroppingFiles(final JComponent target) throws NullPointerException, IllegalArgumentException {
		if (target == null) {
			throw new NullPointerException("Target control can't be null"); 
		}
		else {
			Component	current = target;
			
			while (current != null) {
				if (current instanceof FileContentKeeper) {
					current.setDropTarget(new DropTarget() {
						private static final long serialVersionUID = 4186663174291662900L;

						@Override
			            public synchronized void drop(final DropTargetDropEvent evt) {
			                try{
			                	if ((evt.getDropAction() & DnDConstants.ACTION_COPY) != 0 && evt.getTransferable().isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				                	evt.acceptDrop(DnDConstants.ACTION_COPY);
				                    ((FileContentKeeper)target).placeFileContent((Iterable<File>)evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor));
			                	}
			                	else {
				                	evt.acceptDrop(DnDConstants.ACTION_NONE);
			                	}
			                } catch (Exception exc) {
			                	SwingUtils.getNearestLogger(target).message(Severity.error, exc, exc.getLocalizedMessage());
			                }
			            }
			        });
					return;
				}
				else {
					current = current.getParent();
				}
			}
			throw new IllegalArgumentException("Neither target control nor one of it's parents don't implement FileContentKeeper interface");
		}
	}
	
	private class FileTransferable implements Transferable {
	    private final DataFlavor[]	flavors = {DataFlavor.javaFileListFlavor};
		private final List<File>	files;

	    public FileTransferable(final Collection<File> files) throws IllegalArgumentException {
	    	if (files == null || files.isEmpty()) {
	    		throw new IllegalArgumentException("File list can't be null or empty collection"); 
	    	}
	    	else {
	            this.files = Collections.unmodifiableList(new ArrayList<File>(files));
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
}
