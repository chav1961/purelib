package test;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.enumerations.ContinueMode;

public class NodeLoader extends JFrame {
	private static final long serialVersionUID = -842439308945374954L;

	private final JTree		navigator;
	
	public NodeLoader(final NavigationNode rootCargo) {
		final JSplitPane				jsp = new JSplitPane();
		final DefaultMutableTreeNode	root = new FileTreeNode(rootCargo);

		this.navigator = new JTree(root);
		final DefaultTreeCellRenderer	dtcr = (DefaultTreeCellRenderer)this.navigator.getCellRenderer();
		
		this.navigator.addTreeWillExpandListener(new TreeWillExpandListener() {
			@Override
			public void treeWillExpand(final TreeExpansionEvent event) throws ExpandVetoException {
				final FileTreeNode		node = (FileTreeNode)event.getPath().getLastPathComponent();
				final NavigationNode	nn = (NavigationNode)node.getUserObject();

				if (nn instanceof PackageNode) {
					for (NavigationNode item : ((PackageNode)nn).children) {
						node.add(new FileTreeNode(item));
					}
				}
				else if (nn instanceof ClassNode) {
					if (((ClassNode)nn).fields.length > 0) {
						for (NavigationNode item : ((ClassNode)nn).fields) {
							node.add(new FileTreeNode(item));
						}
					}
					if (((ClassNode)nn).methods.length > 0) {
						for (NavigationNode item : ((ClassNode)nn).methods) {
							node.add(new FileTreeNode(item));
						}
					}
					if (((ClassNode)nn).constructors.length > 0) {
						for (NavigationNode item : ((ClassNode)nn).constructors) {
							node.add(new FileTreeNode(item));
						}
					}
				}
				else if (nn instanceof FieldNode) {
				}
				else if (nn instanceof MethodNode) {
					if (((MethodNode)nn).parmRef.length > 0) {
						for (NavigationNode item : ((MethodNode)nn).parmRef) {
							node.add(new FileTreeNode(item));
						}
					}
				}
				else  if (nn instanceof RootNode) {
					node.add(new FileTreeNode(((RootNode)nn).child));
				}
				((DefaultTreeModel)navigator.getModel()).nodeStructureChanged(node);
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
				final NavigationNode	nn = (NavigationNode)((FileTreeNode)value).getUserObject();
				
				if (nn instanceof TrivialNode) {
					label.setText(((TrivialNode)nn).name);
				}
				else {
					label.setText("ROOT");
				}
				
				return label;
			}
		});
		
		jsp.setLeftComponent(this.navigator);
		getContentPane().add(new JScrollPane(this.navigator),BorderLayout.CENTER);
	}
	
	
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
		final DocumentBuilderFactory 	factory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder 			builder = factory.newDocumentBuilder();
		final Document 					doc = builder.parse(NodeLoader.class.getResourceAsStream("test.xml"));
		final Element 					root = doc.getDocumentElement();
		final List<NavigationNode>		list = new ArrayList<>();
		final NavigationNode[]			rootNode = new NavigationNode[1];
		final String[]					overview = new String[1];
		
		Utils.walkDownXML(root,(mode,node)->{
			NavigationNode	nn;
			
			switch (mode) {
				case ENTER	:
					switch (node.getTagName()) {
						case "navigation"	:
							list.add(0,new RootNode());
							break;
						case "package" 		:
							final PackageNode	pn = new PackageNode(); 
							
							pn.name = node.getAttribute("name");
							list.add(0,pn);
							break;
						case "class"		:
							final ClassNode	cn = new ClassNode(); 
							
							cn.name = node.getAttribute("name");
							list.add(0,cn);
							break;
						case "field"		:
							final FieldNode	fn = new FieldNode(); 
							
							fn.name = node.getAttribute("name");
							list.add(0,fn);
							break;
						case "method"		:
							final MethodNode	mn = new MethodNode(); 
							
							mn.name = node.getAttribute("name");
							list.add(0,mn);
							break;
						case "parameter"	:
							final FieldNode	parmn = new FieldNode(); 
							
							parmn.name = node.getAttribute("name");
							list.add(0,parmn);
							break;
						case "overview"		:
							overview[0] = node.getTextContent();
							break;
						default :
					}
					break;
				case EXIT	:
					switch (node.getTagName()) {
						case "navigation"	:
							rootNode[0] = list.remove(0);
							break;
						case "package" 		:
							nn = list.remove(0);
							if (list.get(0) instanceof PackageNode) {
								((PackageNode)list.get(0)).children = Arrays.copyOf(((PackageNode)list.get(0)).children,((PackageNode)list.get(0)).children.length+1);
								((PackageNode)list.get(0)).children[((PackageNode)list.get(0)).children.length-1] = nn;
							}
							else if (list.get(0) instanceof RootNode) {
								((RootNode)list.get(0)).child = nn;
							}
							break;
						case "class"		:
							nn = list.remove(0);
							if (list.get(0) instanceof PackageNode) {
								((PackageNode)list.get(0)).children = Arrays.copyOf(((PackageNode)list.get(0)).children,((PackageNode)list.get(0)).children.length+1);
								((PackageNode)list.get(0)).children[((PackageNode)list.get(0)).children.length-1] = nn;
							}
							break;
						case "field"		:
							nn = list.remove(0);
							if (list.get(0) instanceof ClassNode) {
								((ClassNode)list.get(0)).fields = Arrays.copyOf(((ClassNode)list.get(0)).fields,((ClassNode)list.get(0)).fields.length+1);
								((ClassNode)list.get(0)).fields[((ClassNode)list.get(0)).fields.length-1] = nn;
							}
							break;
						case "method"		:
							nn = list.remove(0);
							if (list.get(0) instanceof ClassNode) {
								((ClassNode)list.get(0)).methods = Arrays.copyOf(((ClassNode)list.get(0)).methods,((ClassNode)list.get(0)).methods.length+1);
								((ClassNode)list.get(0)).methods[((ClassNode)list.get(0)).methods.length-1] = nn;
							}
							break;
						case "parameter"	:
							nn = list.remove(0);
							if (list.get(0) instanceof MethodNode) {
								((MethodNode)list.get(0)).parmRef = Arrays.copyOf(((MethodNode)list.get(0)).parmRef,((MethodNode)list.get(0)).parmRef.length+1);
								((MethodNode)list.get(0)).parmRef[((MethodNode)list.get(0)).parmRef.length-1] = nn;
							}
							break;
						case "overview"		:
							((RootNode)list.get(0)).overview = overview[0];
							break;
						default :
					}
					break;
				default:
					break;
			}
			return ContinueMode.CONTINUE;
		});
		new NodeLoader(rootNode[0]).setVisible(true);
	}

	static void printTree(final String prefix, final NavigationNode node) {
		if (node instanceof PackageNode) {
			System.err.print(prefix);
			System.err.println("package "+((PackageNode)node).name);
			for (NavigationNode item : ((PackageNode)node).children) {
				printTree(prefix+"\t",item);
			}
		}
		else if (node instanceof ClassNode) {
			System.err.print(prefix);
			System.err.println("Class "+((ClassNode)node).name);
			if (((ClassNode)node).fields.length > 0) {
				System.err.println(prefix+"- fields:");
				for (NavigationNode item : ((ClassNode)node).fields) {
					printTree(prefix+"\t",item);
				}
			}
			if (((ClassNode)node).methods.length > 0) {
				System.err.println(prefix+"- methods:");
				for (NavigationNode item : ((ClassNode)node).methods) {
					printTree(prefix+"\t",item);
				}
			}
			if (((ClassNode)node).constructors.length > 0) {
				System.err.println(prefix+"- constructors:");
				for (NavigationNode item : ((ClassNode)node).constructors) {
					printTree(prefix+"\t",item);
				}
			}
		}
		else if (node instanceof FieldNode) {
			System.err.print(prefix);
			System.err.println("field "+((FieldNode)node).name);
		}
		else if (node instanceof MethodNode) {
			System.err.print(prefix);
			System.err.println("Method "+((MethodNode)node).name);
			if (((MethodNode)node).parmRef.length > 0) {
				System.err.println(prefix+"- parameters:");
				for (NavigationNode item : ((MethodNode)node).parmRef) {
					printTree(prefix+"\t",item);
				}
			}
		}
		else  if (node instanceof RootNode) {
			System.err.print(prefix);
			System.err.println("navigation");
			printTree(prefix+"\t",((RootNode)node).child);
		}
	}

	public static class FileTreeNode extends DefaultMutableTreeNode {
		private static final long serialVersionUID = -6014820236428705486L;

		public FileTreeNode(final NavigationNode current) {
			super(current);
		}

		@Override
		public boolean isLeaf() {
			return (getUserObject() instanceof FieldNode);
		}
	}

}
