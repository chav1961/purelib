package test;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

public class HelloWorld {
	public enum OsType {
		Windows, Linux, MacOs, Other 
	}
	
	public static void main(String[] args) {
		final ExecutorService	es = Executors.newFixedThreadPool(2);  
		final JTextField		tf = new JTextField();
		final List<String>		history = new ArrayList<>();
		final int[]				last = {0};
		 
		tf.setColumns(20);
		tf.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),"clear");
		tf.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0),"process");
		tf.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP,0),"prev");
		tf.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,0),"next");
		tf.getActionMap().put("clear",new AbstractAction(){private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				tf.setText("");
			}
		});
		tf.getActionMap().put("prev",new AbstractAction(){private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				System.err.println("History(prev)="+history+" and "+last[0]);
				if (last[0] >= 0 && last[0] < history.size()) {
					if (last[0] < history.size() - 1) {
						last[0]++;
					}
					tf.setText(history.get(last[0]));
				}
			}
		});
		tf.getActionMap().put("next",new AbstractAction(){private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				System.err.println("History(next)="+history+" and "+last[0]);
				if (last[0] >= 0 && last[0] < history.size()) {
					if (last[0] > 0) {
						last[0]--;
					}
					tf.setText(history.get(last[0]));
				}
			}
		});
		tf.getActionMap().put("process",new AbstractAction(){private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				final String	command = tf.getText().trim();
				final String[]	items; 
				
				if (!command.isEmpty()) {
					try{final Process	p;
						
						switch (detectedOsType()) {
							case Linux:
							case MacOs:
							case Other:
							case Windows:
								items = parseCommand("cmd /s /c "+command);
								break;
							default:
								throw new UnsupportedOperationException();
						}
						p = new ProcessBuilder(items).start();
						es.submit(()->{
							try(final InputStream	is = p.getInputStream()) {
								byte[]				buffer = new byte[100];
								int					len;
								
								while((len = is.read(buffer)) > 0) {
									System.out.write(buffer,0,len);
								}
							} catch (IOException exc) {								
							}
							return null;
						});
						es.submit(()->{
							try(final InputStream	is = p.getErrorStream()) {
								byte[]				buffer = new byte[100];
								int					len;
								
								while((len = is.read(buffer)) > 0) {
									System.err.write(buffer,0,len);
								}
							} catch (IOException exc) {								
							}
							return null;
						});
						p.getOutputStream().close();
						
						p.waitFor();
						if (history.size() == 0 || !command.equals(history.get(0))) {
							history.add(0,command);
						}
						tf.setText("");
						last[0] = 0;
					} catch (IOException | InterruptedException exc) {
						exc.printStackTrace();
					}
				}
			}
		});
		
		JOptionPane.showMessageDialog(null,tf);
	}
	
	protected static String[] parseCommand(final String string) {
		return string.split(" ");
	}

	private static OsType detectedOsType() {
		return OsType.Windows;
	}
}
