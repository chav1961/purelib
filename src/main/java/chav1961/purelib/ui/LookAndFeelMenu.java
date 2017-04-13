package chav1961.purelib.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * <p>This class allows programmer to get prepared "Look & Feel change" submenu to use in the Swing applications.
 * This class is a child class of the {@link JMenu} class and can' be used everywhere the parent one is used</p>
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
	 * <p>Create "Look & Feel change" submenu</p> 
	 * @param caption menu caption in the parent menu. Can't be null or empty
	 * @param refreshRoot root of the Swing application windows to refresh it's look&feel. Can't be null or empty 
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
