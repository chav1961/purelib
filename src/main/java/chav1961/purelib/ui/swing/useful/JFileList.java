package chav1961.purelib.ui.swing.useful;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.model.FieldFormat;

public abstract class JFileList extends JList<JFileListItemDescriptor> {
	private static final long 			serialVersionUID = 6388035688220928716L;
	private static final Icon			DIR_ICON = new ImageIcon(JFileSelectionDialog.class.getResource("directory.png"));
	private static final Icon			FILE_ICON = new ImageIcon(JFileSelectionDialog.class.getResource("file.png"));

	private final LoggerFacade			logger;
	private final FileSystemInterface	fsi;
	private final boolean				insertParent;
	
	public JFileList(final LoggerFacade logger, final FileSystemInterface fsi, final boolean insertParent, final boolean useMultiselection, final boolean horizontalPlacement) throws IOException {
		if (logger == null) {
			throw new NullPointerException("Logger facade can't be null"); 
		}
		else if (fsi == null) {
			throw new NullPointerException("File system interface can't be null"); 
		}
		else {
			this.logger = logger;
			this.fsi = fsi;
			this.insertParent = insertParent;
			
			setModel(new DefaultListModel<JFileListItemDescriptor>());
			setLayoutOrientation(horizontalPlacement ? JList.HORIZONTAL_WRAP : JList.VERTICAL_WRAP);
			setVisibleRowCount(-1);
			setCellRenderer(new ListCellRenderer<JFileListItemDescriptor>() {
				@Override
				public Component getListCellRendererComponent(final JList<? extends JFileListItemDescriptor> list, final JFileListItemDescriptor value, final int index, final boolean isSelected, final boolean cellHasFocus) {
					final JLabel	result = new JLabel();
					final String	val = URI.create(value.path).getRawSchemeSpecificPart();
						
					result.setText(CharUtils.fillInto(URLDecoder.decode(value.name),10,false,CharUtils.FillingAdjstment.LEFT));
					result.setToolTipText(URLDecoder.decode(value.path));
					if (value.isDirectory) {
						result.setIcon(DIR_ICON);
						if (isSelected) {
							result.setOpaque(true);
							result.setForeground(list.getSelectionForeground());
							result.setBackground(list.getSelectionBackground());
						}
					}
					else {
						result.setIcon(FILE_ICON);
						if (isSelected) {
							result.setOpaque(true);
							result.setForeground(list.getSelectionForeground());
							result.setBackground(list.getSelectionBackground());
						}
					}
					if (cellHasFocus) {
						result.setBorder(new LineBorder(Color.BLACK));
					}
					
					return result;
				}
			});
			addMouseListener(new MouseListener() {
				@Override public void mouseReleased(MouseEvent e) {}
				@Override public void mousePressed(MouseEvent e) {}
				@Override public void mouseExited(MouseEvent e) {}
				@Override public void mouseEntered(MouseEvent e) {}
				
				@Override 
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() > 1) {
						final int	index = locationToIndex(e.getPoint());
						
						if (index >= 0) {
							String	newLocation = null;
							
							try (final FileSystemInterface	fsiNew = fsi.clone().open(getModel().getElementAt(index).path)) {
								
								if (fsiNew.isDirectory()) {
									newLocation = fsiNew.getPath();
								}
								else {
									setSelectedIndex(index);
									selectAndAccept(getSelectedValue().path);
								}
							} catch (IOException e1) {
								logger.message(Severity.error,e1,"Error querying file system node ["+getModel().getElementAt(index)+"]: "+e1.getLocalizedMessage());
							}
							if (newLocation != null) {
								try{open(newLocation);
								} catch (IOException e1) {
									logger.message(Severity.error,e1,"Error opening file system node ["+newLocation+"]: "+e1.getLocalizedMessage());
								}
							}
						}
					}
				}
			});
			addKeyListener(new KeyListener() {
				@Override public void keyTyped(final KeyEvent e) {}
				@Override public void keyPressed(final KeyEvent e) {}
				
				@Override
				public void keyReleased(final KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						final JFileListItemDescriptor	value = getSelectedValue();
						
						try(final FileSystemInterface	current = fsi.clone().open(value.path)) {
							if (current.isDirectory()) {
								open(value.path);
							}
							else {
								selectAndAccept(value.path);
							}
						} catch (IOException exc) {
							logger.message(Severity.error,exc,"Error selection file system node ["+value+"]: "+exc.getLocalizedMessage());
						}
					}
				}
			});
			setSelectionMode(useMultiselection ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION : ListSelectionModel.SINGLE_SELECTION);
			open(fsi.getPath());
		}
	}

	protected abstract void selectAndAccept(final String path);

	private void open(final String newLocation) throws IOException {
		final DefaultListModel<JFileListItemDescriptor>	model = ((DefaultListModel<JFileListItemDescriptor>)getModel()); 
		
		model.removeAllElements();
		if (insertParent && !"/".equals(newLocation)) {
			try(final FileSystemInterface current = fsi.clone().open(newLocation)) {
				final String	parentPath = current.open("..").getPath(); 
				
				model.addElement(new JFileListItemDescriptor("..",parentPath,true));
			}
		}
		try(final FileSystemInterface current = fsi.clone().open(newLocation)) {
			final List<JFileListItemDescriptor>	dirs = new ArrayList<>();
			final List<JFileListItemDescriptor>	files = new ArrayList<>();
			
			current.list((s)-> {
				if (s.isDirectory()) {
					dirs.add(new JFileListItemDescriptor(s.getName(),s.getPath(),true));
				}
				else {
					files.add(new JFileListItemDescriptor(s.getName(),s.getPath(),false));
				}
			});
			dirs.sort((o1,o2)->o1.name.compareTo(o2.name));
			files.sort((o1,o2)->o1.name.compareTo(o2.name));
			for (JFileListItemDescriptor item : dirs) {
				model.addElement(item);
			}
			for (JFileListItemDescriptor item : files) {
				model.addElement(item);
			}
		}
	}
}
