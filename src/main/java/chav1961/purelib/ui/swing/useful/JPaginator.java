package chav1961.purelib.ui.swing.useful;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;

import javax.swing.FocusManager;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.KeyStroke;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.ui.swing.SwingUtils;

public class JPaginator extends JPanel implements LocaleChangeListener {
	private static final long serialVersionUID = -6595903486155330434L;
	
	private final Localizer				localizer;
	private final PageSelectCallback	callback;
	private final JComponent[]			pages;
	private final JLabel				captionLabel = new JLabel("",JLabel.CENTER);
	private final JPanel				pageContainer;
	private final CardLayout			cardLayout;
	private final JScrollBar			pager;
	private final Component[]			lastFocused;
	private final int					totalPages;
	private int							currentPage = -1;
	private String						caption, tooltip, help; 

	public enum PageMoving {
		FIRST, NEXT, PREV, LAST
	}
	
	@FunctionalInterface
	public interface PageSelectCallback {
		void selectPage(int oldPage, int newPage, int totalPages);
		default String getPageCaption(int pageNo) {return null;}
		default String getPageTooltip(int pageNo) {return null;}
		default String getPageHelp(int pageNo) {return null;}
	}

	public JPaginator(final Localizer localizer, final JComponent... pages) throws IllegalArgumentException, NullPointerException, LocalizationException {
		this(localizer,false,(a,b,c)->{},pages);
	}
	
	public JPaginator(final Localizer localizer, final boolean needCaption, final PageSelectCallback callback, final JComponent... pages) throws IllegalArgumentException, NullPointerException, LocalizationException {
		super(new BorderLayout());
		
		final int 	nullPage;
		
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (pages == null || pages.length == 0) {
			throw new IllegalArgumentException("Page list can't be null or empty array");
		}
		else if (callback == null) {
			throw new NullPointerException("Page callback can't be null");
		}
		else if ((nullPage = Utils.checkArrayContent4Nulls(pages)) >= 0) {
			throw new NullPointerException("Page list contains null at index ["+nullPage+"]");
		}
		else {
			this.localizer = localizer;
			this.callback = callback;
			this.pages = pages;
			this.totalPages = pages.length;
			this.lastFocused = new Component[totalPages];
					
			if (needCaption) {
				add(captionLabel,BorderLayout.NORTH);
			}
			if (pages.length == 1) {
				this.cardLayout = null;
				this.pageContainer = null;
				this.pager = null;
				add(pages[0],BorderLayout.CENTER);
			}
			else {
				this.cardLayout = new CardLayout();
				this.pageContainer = new JPanel(cardLayout);
				this.pager = new JScrollBar(JScrollBar.VERTICAL,1,1,1,pages.length);
				
				for (int index = 1; index <= totalPages; index++) {
					pageContainer.add(pages[index-1],"p"+index);
					prepareCrossPageMoving(pages[index-1],index == 1, index == totalPages);
				}
				add(pageContainer,BorderLayout.CENTER);
				add(pager,BorderLayout.EAST);
				SwingUtils.assignActionKey(this,JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN,0),(e)->{movePageInternal(PageMoving.valueOf(e.getActionCommand()));},PageMoving.NEXT.name());
				SwingUtils.assignActionKey(this,JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN,InputEvent.CTRL_DOWN_MASK),(e)->{movePageInternal(PageMoving.valueOf(e.getActionCommand()));},PageMoving.LAST.name());
				SwingUtils.assignActionKey(this,JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP,0),(e)->{movePageInternal(PageMoving.valueOf(e.getActionCommand()));},PageMoving.PREV.name());
				SwingUtils.assignActionKey(this,JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP,InputEvent.CTRL_DOWN_MASK),(e)->{movePageInternal(PageMoving.valueOf(e.getActionCommand()));},PageMoving.FIRST.name());
			}
			SwingUtils.assignActionKey(this,JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,SwingUtils.KS_HELP,(e)->{callHelp(help);},"");
			movePage(PageMoving.FIRST);
		}
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
		SwingUtils.refreshLocale(this,oldLocale,newLocale);
	}
	
	public void movePage(final PageMoving action) throws NullPointerException, LocalizationException {
		if (action == null) {
			throw new NullPointerException("Action can't be null");
		}
		else {
			final int	lastPageNo = currentPage;
			
			lastFocused[currentPage-1] = FocusManager.getCurrentManager().getFocusOwner();
			
			switch (action) {
				case FIRST	:
					currentPage = 1;
					break;
				case PREV	:
					if (currentPage > 1) {
						currentPage--;
					}
					break;
				case NEXT	:
					if (currentPage < totalPages) {
						currentPage++;
					}
					break;
				case LAST	:
					currentPage = totalPages;
					break;
				default : throw new UnsupportedOperationException("Moving option ["+action+"] is not supported yet"); 
			}
	
			if (cardLayout != null) {
				cardLayout.show(pageContainer,"p"+currentPage);
			}
			pager.setValue(currentPage);
			callback.selectPage(lastPageNo,currentPage,totalPages);
			caption = callback.getPageCaption(currentPage);
			tooltip = callback.getPageTooltip(currentPage);
			help = callback.getPageHelp(currentPage);
			
			if (lastFocused[currentPage-1] != null) {
				lastFocused[currentPage-1].requestFocus();
			}
			else {
				final JComponent	current = pages[currentPage-1];
				
				for (int index = 0, maxIndex = current.getComponentCount(); index < maxIndex; index++) {
					if (!(current.getComponent(index) instanceof JLabel)) {
						current.getComponent(index).requestFocus();
					}
				}
			}
			fillLocalizedStrings();
		}
	}

	protected Localizer getLocalizerAssociated() {
		return localizer;
	}

	protected void callHelp(final String helpString) {
		if (helpString != null) {
			try{SwingUtils.showCreoleHelpWindow(this,URI.create(helpString));
			} catch (IOException e) {
				PureLibSettings.CURRENT_LOGGER.message(Severity.error,e,"Help calling problems: "+e.getLocalizedMessage());
			}
		}
	}

	
	private void prepareCrossPageMoving(final JComponent page, final boolean first, final boolean last) {
		Component	current, next;
		
		if (first) {
			current = next = null;
			for (int index = 0; index < page.getComponentCount() && (current == null || next == null); index++) {
				if (page.getComponent(index).isFocusable()) {
					if (current == null) {
						current = page.getComponent(index);
					}
					else if (next == null) {
						next = page.getComponent(index);
					}
				}
			}
			if (current != null && next != null) {
				buildFocusListener(current,next,PageMoving.PREV);
			}
		}
		
		if (last) {
			current = next = null;
			for (int index = page.getComponentCount()-1; index >= 0 && (current == null || next == null); index--) {
				if (page.getComponent(index).isFocusable()) {
					if (current == null) {
						current = page.getComponent(index);
					}
					else if (next == null) {
						next = page.getComponent(index);
					}
				}
			}
			if (current != null && next != null) {
				buildFocusListener(current,next,PageMoving.NEXT);
			}
		}
	}

	private void buildFocusListener(final Component from, final Component to, final PageMoving action) {
		from.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				if (e.getOppositeComponent() != to) {
					movePageInternal(action);
				}
			}
			
			@Override public void focusGained(FocusEvent e) {}
		});
	}

	private void movePageInternal(final PageMoving action) throws NullPointerException {
		try{movePage(action);
		} catch (LocalizationException e) {
			PureLibSettings.CURRENT_LOGGER.message(Severity.error,e,"Localization problems: "+e.getLocalizedMessage());
		}
	}	
	
	private void fillLocalizedStrings() throws LocalizationException {
		captionLabel.setText(Utils.nvl(getLocalizerAssociated().getValue(caption),"p"+currentPage));
		captionLabel.setToolTipText(Utils.nvl(getLocalizerAssociated().getValue(tooltip),""));
	}
}
