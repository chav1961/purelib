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
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.useful.interfaces.FileContentKeeper;

class FileTransferHandler extends TransferHandler {
	private static final long serialVersionUID = 109812767523913960L;

	@Override
	public int getSourceActions(final JComponent source) {
		return COPY;
	}

	@Override
	protected Transferable createTransferable(final JComponent source) throws IllegalArgumentException {
		final Transferable[]	result = new Transferable[1];
		
		findFileContentKeeper(source, (item)->{
			final Collection<JFileItemDescriptor> content = ((FileContentKeeper)item).getSelectedFileContent();
			
			if (content != null && !content.isEmpty()) {
				result[0] = new FileTransferable(content);
			}
		});
		return result[0];
	}
	
	public static void prepare4DroppingFiles(final JComponent target) throws NullPointerException, IllegalArgumentException {
		if (target == null) {
			throw new NullPointerException("Target control can't be null"); 
		}
		else {
			findFileContentKeeper(target,(item)->{
				item.setDropTarget(new DropTarget(target, DnDConstants.ACTION_MOVE, null, true) {
					private static final long serialVersionUID = 4186663174291662900L;

					@Override
		            public synchronized void drop(final DropTargetDropEvent evt) {
	                	if ((evt.getDropAction() & DnDConstants.ACTION_COPY_OR_MOVE) != 0 && evt.getTransferable().isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
		                	evt.acceptDrop(DnDConstants.ACTION_COPY);
		        			findFileContentKeeper(target,(item)->{
			                    try{final List<JFileItemDescriptor>	descList = new ArrayList<>();
			                    	
			                    	for (File f : (List<File>)evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor)) {
			                    		descList.add(JFileItemDescriptor.of(f));
			                    	}
			                    	
			                    	((FileContentKeeper)item).placeFileContent(evt.getLocation(), descList);
								} catch (IOException | UnsupportedFlavorException exc) {
				                	SwingUtils.getNearestLogger(target).message(Severity.error, exc, exc.getLocalizedMessage());
								}
		        			});
	                	}
	                	else {
		                	evt.rejectDrop();
	                	}
		            }
		        });
			});
		}
	}
	
	private static void findFileContentKeeper(final JComponent component, final Consumer<JComponent> callback) {
		Component	current = component;
		
		while (current != null) {
			if (current instanceof FileContentKeeper) {
				callback.accept((JComponent)current);
				return;
			}
			else {
				current = current.getParent();
			}
		}
		throw new IllegalArgumentException("Neither target control nor one of it's parents don't implement FileContentKeeper interface");
	}
	
	private class FileTransferable implements Transferable {
	    private final DataFlavor[]				flavors = {DataFlavor.javaFileListFlavor};
		private final List<JFileItemDescriptor>	files;

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
}
