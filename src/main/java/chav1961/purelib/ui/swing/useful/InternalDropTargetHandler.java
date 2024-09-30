package chav1961.purelib.ui.swing.useful;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.IOException;
import java.util.function.BiPredicate;

import javax.swing.JComponent;

class InternalDropTargetHandler implements DropTargetListener {
	static interface Acceptor {
		boolean accept(Transferable t) throws IOException;
	}
	
    private final JComponent panel;
    private final BiPredicate<Transferable, DataFlavor[]>	support;
    private final InternalDropTargetHandler.Acceptor					accept;

    public InternalDropTargetHandler(final JComponent panel, final BiPredicate<Transferable, DataFlavor[]>	support, final InternalDropTargetHandler.Acceptor accept) {
        this.panel = panel;
        this.support = support;
        this.accept = accept;
    }

    public void dragEnter(final DropTargetDragEvent dtde) {
    	if (support.test(dtde.getTransferable(), dtde.getTransferable().getTransferDataFlavors())) {
            dtde.acceptDrag(DnDConstants.ACTION_COPY);
    	}
    	else {
            dtde.rejectDrag();
    	}
    }

    public void dragOver(final DropTargetDragEvent dtde) {
    }

    public void dragExit(final DropTargetEvent dte) {
    }

    public void dropActionChanged(final DropTargetDragEvent dtde) {
    }

    public void drop(DropTargetDropEvent dtde) {
    	if (support.test(dtde.getTransferable(), dtde.getTransferable().getTransferDataFlavors())) {
    		try {
			    dtde.acceptDrop(DnDConstants.ACTION_COPY);
			    
				if (!accept.accept(dtde.getTransferable())) {
				    dtde.rejectDrop();
				}
			} catch (IOException e) {
			    dtde.rejectDrop();
			}
        }
    }
}