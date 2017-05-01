package chav1961.purelib.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import chav1961.purelib.fsys.interfaces.FileSystemInterface;

/**
 * <p>This class allows programmer to get prepared "Look &amp; Feel change" submenu to use in the Swing applications.
 * This class is a child class of the {@link JMenu} class and can' be used everywhere the parent one is used</p>
 * 
 * <p>This class not only fills menu by existent Looks and Feels, but installs all additional L&amp;F implementation.  
 * Any vendor can add it's own Look &amp; Feel implementation to the library. To make this, simply use SPI protocol. 
 * The reference to vendor L&amp;F need be described in the <b>META-INF/services/javax.swing.LookAndFeel</b> file</p> 
 * 
 * @see javax.swing.JMenu JMenu
 * @see javax.swing Java Swing
 * @see chav1961.purelib.ui JUnit tests
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.1
 */
public class LookAndFeelMenu extends JMenu {
	private static final long 		serialVersionUID = -1451962129776742455L;

	private final Component			refreshRoot;
	private final List<JRadioButtonMenuItem>	lookAndFeels = new ArrayList<>(); 
	private final ActionListener	listener = new ActionListener(){
											@Override
											public void actionPerformed(final ActionEvent e) {
												try{UIManager.setLookAndFeel(e.getActionCommand());
													for (JRadioButtonMenuItem lf : lookAndFeels) {
														lf.setSelected(lf.getActionCommand().equals(e.getActionCommand()));
													}
													SwingUtilities.updateComponentTreeUI(refreshRoot);
												} catch (ClassNotFoundException | InstantiationException
														| IllegalAccessException | UnsupportedLookAndFeelException exc) {
													JOptionPane.showMessageDialog(null,"Can't change Look & Feel: "
																+exc.getClass().getSimpleName()+"("+exc.getMessage()+")"
																,"Error changing Look & Feel"
																,JOptionPane.OK_OPTION|JOptionPane.ERROR_MESSAGE);
												}
											}
										}; 
	
	/**
	 * <p>Create "Look &amp; Feel change" submenu</p> 
	 * @param caption menu caption in the parent menu. Can't be null or empty
	 * @param refreshRoot root of the Swing application windows to refresh it's look&amp;feel. Can't be null or empty 
	 */
	public LookAndFeelMenu(final String caption, final Component refreshRoot) {
		if (caption == null || caption.isEmpty()) {
			throw new IllegalArgumentException("Caption string can't be null or empty");
		}
		else if (refreshRoot == null) {
			throw new IllegalArgumentException("Refresh root can't be null");
		}
		else {
			setText(caption);
			
			for (LookAndFeel item : ServiceLoader.load(javax.swing.LookAndFeel.class,this.getClass().getClassLoader())){
				UIManager.installLookAndFeel(item.getName(),item.getClass().getName());
			}
			
			for (LookAndFeelInfo item : UIManager.getInstalledLookAndFeels()) {
				final JRadioButtonMenuItem	line = new JRadioButtonMenuItem(item.getName());
				
				line.setSelected(item.getClassName().equals(UIManager.getLookAndFeel().getClass().getName()));
				line.setActionCommand(item.getClassName());
				line.addActionListener(listener);
				lookAndFeels.add(line);
				add(line);
			}
			this.refreshRoot = refreshRoot; 
		}
	}
}
