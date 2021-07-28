package chav1961.purelib.ui.swing.useful;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.ui.interfaces.PureLibStandardIcons;

public abstract class JFileListOld extends JList<JFileItemDescriptor> {
	private static final long 			serialVersionUID = 6388035688220928716L;
	private static final Icon			DIR_ICON = PureLibStandardIcons.DIRECTORY.getIcon();
	private static final Icon			FILE_ICON = PureLibStandardIcons.FILE.getIcon();

	public enum JFileListSelectionType {
		NONE, 
		EXACTLY_ONE_DIRECTORY,
		EXACTLY_ONE_FILE,
		DIRECTORIES_ONLY,
		FILES_ONLY,
		MIX
	}
	
	private final LoggerFacade			logger;
	private final FileSystemInterface	fsi;
	private final boolean				insertParent;
	
	private String						currentLocation;
	
	public JFileListOld(final LoggerFacade logger, final FileSystemInterface fsi, final boolean insertParent, final boolean useMultiselection, final boolean horizontalPlacement) throws IOException, NullPointerException {
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
			
			setModel(new DefaultListModel<JFileItemDescriptor>());
			setLayoutOrientation(horizontalPlacement ? JList.HORIZONTAL_WRAP : JList.VERTICAL_WRAP);
			setVisibleRowCount(-1);
			setCellRenderer(new ListCellRenderer<JFileItemDescriptor>() {
				@Override
				public Component getListCellRendererComponent(final JList<? extends JFileItemDescriptor> list, final JFileItemDescriptor value, final int index, final boolean isSelected, final boolean cellHasFocus) {
					final JLabel	result = new JLabel();
					final String	val = URI.create(value.getPath()).getRawSchemeSpecificPart();
						
					try{
						result.setText(CharUtils.fillInto(URLDecoder.decode(value.getName(), PureLibSettings.DEFAULT_CONTENT_ENCODING),10,false,CharUtils.FillingAdjstment.LEFT));
						result.setToolTipText(URLDecoder.decode(value.getPath(), PureLibSettings.DEFAULT_CONTENT_ENCODING));
					} catch (UnsupportedEncodingException e) {
						result.setToolTipText(value.getPath());					
					}
					if (value.isDirectory()) {
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
							
							try (final FileSystemInterface	fsiNew = fsi.clone().open(getModel().getElementAt(index).getPath())) {
								
								if (fsiNew.isDirectory()) {
									newLocation = fsiNew.getPath();
								}
								else {
									setSelectedIndex(index);
									selectAndAccept(getSelectedValue().getPath());
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
						final JFileItemDescriptor	value = getSelectedValue();
						
						if (value != null) {
							try(final FileSystemInterface	current = fsi.clone().open(value.getPath())) {
								if (current.isDirectory()) {
									open(value.getPath());
								}
								else {
									selectAndAccept(value.getPath());
								}
							} catch (IOException exc) {
								logger.message(Severity.error,exc,"Error selection file system node ["+value+"]: "+exc.getLocalizedMessage());
							}
						}
					}
				}
			});
			setSelectionMode(useMultiselection ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION : ListSelectionModel.SINGLE_SELECTION);
			open(fsi.getPath());
		}
	}

	protected abstract void selectAndAccept(final String path);

	public String getCurrentLocation() {
		return currentLocation;
	}

	public String getCurrentSelection() {
		if (getSelectedIndex() >= 0) {
			return getSelectedValue().getPath();
		}
		else {
			return null;
		}
	}
	
	public void refresh() throws IOException {
		open(getCurrentLocation());
	}

	public JFileListSelectionType getSelectionType() {
		final int[]	indices = getSelectedIndices();
		
		if (indices == null || indices.length == 0) {
			return JFileListSelectionType.NONE;
		}
		else if (indices.length == 1) {
			return getModel().getElementAt(indices[0]).isDirectory() ? JFileListSelectionType.EXACTLY_ONE_DIRECTORY : JFileListSelectionType.EXACTLY_ONE_FILE; 
		}
		else {
			boolean	filePresent = false, directoryPresent = false;
			
			for (int index : indices) {
				if (getModel().getElementAt(index).isDirectory()) {
					directoryPresent = true;
				}
				else {
					filePresent = true;
				}
			}
			if (directoryPresent && filePresent) {
				return JFileListSelectionType.MIX;
			}
			else {
				return directoryPresent ? JFileListSelectionType.DIRECTORIES_ONLY : JFileListSelectionType.FILES_ONLY;
			}
		}
	}

	public Set<String> getSelectedExtensions() {
		final Set<String>	result = new HashSet<>();
		final int[]			indices = getSelectedIndices();
		
		if (indices != null) {
			for (int index : indices) {
				final String	path = getModel().getElementAt(index).getPath();
				final String	name = path.endsWith("/") ? path.substring(path.lastIndexOf('/',path.lastIndexOf('/')) + 1) : path.substring(path.lastIndexOf('/') + 1);
				
				if (name.contains(".")) {
					result.add(name.substring(name.lastIndexOf('.')+1));
				}
			}
		}
		return result;
	}
	
	private void open(final String newLocation) throws IOException {
		final DefaultListModel<JFileItemDescriptor>	model = ((DefaultListModel<JFileItemDescriptor>)getModel()); 
		
		model.removeAllElements();
		if (insertParent && !"/".equals(newLocation)) {
			try(final FileSystemInterface current = fsi.clone().open(newLocation)) {
				final String	parentPath = current.open("..").getPath(); 
				
				model.addElement(new JFileItemDescriptor("..", parentPath, true, 0, new Date(0)));
			}
		}
		try(final FileSystemInterface current = fsi.clone().open(newLocation)) {
			final List<JFileItemDescriptor>	dirs = new ArrayList<>();
			final List<JFileItemDescriptor>	files = new ArrayList<>();
			
			current.list((s)-> {
				if (s.isDirectory()) {
					dirs.add(new JFileItemDescriptor(s.getName(), s.getPath(), true, s.size(), new Date(s.lastModified())));
				}
				else {
					files.add(new JFileItemDescriptor(s.getName(), s.getPath(), false, s.size(), new Date(s.lastModified())));
				}
				return ContinueMode.CONTINUE;
			});
			dirs.sort((o1,o2)->o1.getName().compareTo(o2.getName()));
			files.sort((o1,o2)->o1.getName().compareTo(o2.getName()));
			for (JFileItemDescriptor item : dirs) {
				model.addElement(item);
			}
			for (JFileItemDescriptor item : files) {
				model.addElement(item);
			}
		}
		currentLocation = newLocation;
	}
}
