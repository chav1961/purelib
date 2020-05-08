package chav1961.purelib.ui.swing.useful;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.KeyStroke;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.ui.swing.SwingUtils;

public class JMultipageGallery extends JPanel {
	private static final long 		serialVersionUID = 9173511881414823318L;
	
	private static final String		PAGE_NEXT = "pageNext";
	private static final String		PAGE_PREV = "pagePrev";
	private static final String		PAGE_FIRST = "pageFirst";
	private static final String		PAGE_LAST = "pageLast";
	
	private final CardLayout		card = new CardLayout();; 
	private final JPanel			innerPanel = new JPanel(card);
	private final JScrollBar		bar;
	private final Icon[]			content;
	private final ActionListener	al = (e)->processAction(e.getActionCommand());
	private int						currentPage = 0;
	
	public JMultipageGallery(final Icon... content) {
		if (content == null || content.length == 0) {
			throw new IllegalArgumentException("Content can't be null or empty list");
		}
		else if (Utils.checkArrayContent4Nulls(content) >= 0) {
			throw new NullPointerException("Nulls inside content list");
		}
		else {
			int	xMax = 0, yMax = 0;
			
			this.bar = new JScrollBar(JScrollBar.VERTICAL,0,10,0,content.length);
			this.content = content.clone();
			
			for (int index = 0, maxIndex = content.length; index < maxIndex; index++) {
				innerPanel.add(new JLabel(content[index]),"label"+index);
				xMax = Math.max(xMax,content[index].getIconWidth());
				yMax = Math.max(yMax,content[index].getIconHeight());
			}
			innerPanel.setPreferredSize(new Dimension(xMax,yMax));
			
			setFocusable(true);
			SwingUtils.assignActionKey(this,WHEN_FOCUSED,KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN,0),al,PAGE_NEXT);
			SwingUtils.assignActionKey(this,WHEN_FOCUSED,KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN,KeyEvent.CTRL_DOWN_MASK),al,PAGE_LAST);
			SwingUtils.assignActionKey(this,WHEN_FOCUSED,KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP,0),al,PAGE_PREV);
			SwingUtils.assignActionKey(this,WHEN_FOCUSED,KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP,KeyEvent.CTRL_DOWN_MASK),al,PAGE_FIRST);
			
			setLayout(new BorderLayout());
			add(innerPanel,BorderLayout.CENTER);
			if (content.length > 1) {
				add(bar,BorderLayout.EAST);
			}
			setPage(0);
		}
	}
	
	public int getNumberOfPages() {
		return content.length;
	}
	
	public int getCurrentPage() {
		return currentPage;
	}
	
	public void setCurrentPage(final int page) throws ArrayIndexOutOfBoundsException {
		if (page < 0 || page >= getNumberOfPages()) {
			throw new ArrayIndexOutOfBoundsException("Page to set ["+page+"] must be in range 0.."+(getNumberOfPages()-1));
		}
		else {
			
		}
	}

	private void processAction(final String action) {
		switch (action) {
			case PAGE_NEXT	:
				if (getCurrentPage() < getNumberOfPages()) {
					setCurrentPage(getCurrentPage()+1);
				}
				break;
			case PAGE_PREV	:
				if (getCurrentPage() > 0) {
					setCurrentPage(getCurrentPage()-1);
				}
				break;
			case PAGE_FIRST	:
				setCurrentPage(0);
				break;
			case PAGE_LAST	:
				setCurrentPage(getNumberOfPages()-1);
				break;
		}
	}
	
	private void setPage(final int page) {
		card.show(innerPanel,"label"+page);
		bar.setValue(page);
		currentPage = page;
	}
}
