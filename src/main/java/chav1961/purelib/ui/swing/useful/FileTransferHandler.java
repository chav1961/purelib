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
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

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
			findFileContentKeeper(target, (item)->{
				item.setDropTarget(new DropTarget(target, DnDConstants.ACTION_MOVE, null, true) {
					private static final long serialVersionUID = 4186663174291662900L;

					@Override
		            public synchronized void drop(final DropTargetDropEvent evt) {
	                	if ((evt.getDropAction() & DnDConstants.ACTION_COPY_OR_MOVE) != 0 && evt.getTransferable().isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
		                	evt.acceptDrop(DnDConstants.ACTION_COPY);
		        			findFileContentKeeper(target,(item)->{
			                    try{final List<JFileItemDescriptor>	descList = new ArrayList<>();
			                    	
			                    	for (Object f : (List<?>)evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor)) {
			                    		if (f instanceof JFileItemDescriptor) {
				                    		descList.add((JFileItemDescriptor)f);
			                    		}
			                    		else if (f instanceof File) {
				                    		descList.add(JFileItemDescriptor.of((File)f));
			                    		}
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
}
