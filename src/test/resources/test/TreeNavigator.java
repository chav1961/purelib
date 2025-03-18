package test;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.border.LineBorder;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

public class TreeNavigator extends JFrame {
	private static final long serialVersionUID = -842439308945374954L;

	private final JTree		navigator;
	
	public TreeNavigator() {
		final JSplitPane				jsp = new JSplitPane();
		final DefaultMutableTreeNode	root = new FileTreeNode(new File("c:/"));

		this.navigator = new JTree(root);
		final DefaultTreeCellRenderer	dtcr = (DefaultTreeCellRenderer)this.navigator.getCellRenderer();
		
		this.navigator.addTreeWillExpandListener(new TreeWillExpandListener() {
			@Override
			public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
				final FileTreeNode	node = (FileTreeNode)event.getPath().getLastPathComponent();
				final File			f = (File)node.getUserObject();
				
				if (f.isDirectory()) {
					final File[]	files = f.listFiles();
					
					if (files != null) {
						for (File item : f.listFiles()) {
							node.add(new FileTreeNode(item));
						}
						((DefaultTreeModel)navigator.getModel()).nodeStructureChanged(node);
					}
				}
			}
			
			@Override
			public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
				// TODO Auto-generated method stub
			}
		});
				
		this.navigator.setCellRenderer(new TreeCellRenderer() {
			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
				final JLabel	label = new JLabel();
				
				if (selected) {
					label.setOpaque(true);
					label.setBackground(dtcr.getBackgroundSelectionColor());
					label.setForeground(dtcr.getTextSelectionColor());
				}
				if (hasFocus) {
					label.setBorder(new LineBorder(dtcr.getBorderSelectionColor()));
				}
				label.setText(((File)((FileTreeNode)value).getUserObject()).getName());
				label.setIcon(((File)((FileTreeNode)value).getUserObject()).isDirectory() 
						? (expanded ? new ImageIcon(TreeNavigator.class.getResource("dir.png")) : new ImageIcon(TreeNavigator.class.getResource("dir1.png"))) 
						: new ImageIcon(TreeNavigator.class.getResource("file.png")));
				
				return label;
			}
		});
		
		
		jsp.setLeftComponent(this.navigator);
		getContentPane().add(new JScrollPane(this.navigator),BorderLayout.CENTER);
	}
	
	public static void main(final String[] args) {
		new TreeNavigator().setVisible(true);
	}
	
	
	public static class FileTreeNode extends DefaultMutableTreeNode {
		public FileTreeNode(final File current) {
			super(current);
		}

		@Override
		public boolean isLeaf() {
			return ((File)getUserObject()).isFile();
		}
	}
}
