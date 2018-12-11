package chav1961.purelib.ui.swing;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.enumerations.MarkupOutputFormat;
import chav1961.purelib.i18n.PureLibLocalizer;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.streams.char2char.CreoleWriter;

public class SimpleHelpComponent extends JPanel implements LocaleChangeListener {
	
	private static final long serialVersionUID = 1268197778682457474L;

	private final Localizer			localizer;
	private final InnerToolBar		toolBar;
	private final JPanel			center = new JPanel(new CardLayout());
	private final Action			forwardAction = new AbstractAction() {private static final long serialVersionUID = 1L;
										@Override
										public void actionPerformed(ActionEvent e) {
											forward();
										}
									}; 
	private final Action			backwardAction = new AbstractAction() {private static final long serialVersionUID = 1L;
										@Override
										public void actionPerformed(ActionEvent e) {
											backward();
										}
									};
	private final Map<String,JEditorPane>	localizedPages = new HashMap<>();
	private final List<JEditorPane>			plainPages = new ArrayList<>();
	private int						currentPage = 0, totalPage = 1;
	
	public SimpleHelpComponent(final Localizer localizer, final String helpId) throws NullPointerException, IllegalArgumentException, ContentException, LocalizationException {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
		}
		else if (helpId == null || helpId.isEmpty()) {
			throw new IllegalArgumentException("Help id can't be null or empty"); 
		}
		else if (!localizer.containsKey(helpId)) {
			throw new ContentException("Help id ["+helpId+"] is missing in the localizer"); 
		}
		else {
			this.setLayout(new BorderLayout());
			this.toolBar = new InnerToolBar(localizer,forwardAction,backwardAction);
			
			final JEditorPane	pane = buildPane(localizer,helpId);
			final JScrollPane	scroll = new JScrollPane(pane);
			
			localizedPages.put(helpId,pane);
			plainPages.add(pane);
		
			center.setBorder(new EtchedBorder(EtchedBorder.RAISED));
			add(toolBar,BorderLayout.NORTH);
			center.add(scroll,String.valueOf(currentPage));
			add(center,BorderLayout.CENTER);
			this.localizer = localizer;
			
			assignMovingKeys(this);
			this.setMinimumSize(new Dimension(100,100));
		}
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		toolBar.localeChanged(oldLocale, newLocale);
		try{for (Entry<String, JEditorPane> item : localizedPages.entrySet()) {
				item.getValue().setText(getPageContent(localizer,item.getKey()));
			}
		} catch (ContentException e) {
			throw new LocalizationException(e.getLocalizedMessage(),e);
		}
	}
	
	private void gotoLink(final URI link) throws ContentException, IOException {
		final String		ref = link.getSchemeSpecificPart();
		final JEditorPane	pane;
		
		currentPage = totalPage++;
		if (ref.contains("examplewiki.com")) {
			final String		helpId = ref.substring(ref.lastIndexOf('/')+1);
			
			pane = buildPane(localizer,helpId);
			localizedPages.put(helpId,pane);
		}
		else if ("http".equals(link.getScheme()) || "https".equals(link.getScheme())) {
			Desktop.getDesktop().browse(link);
			return;
		}
		else {
			pane = buildPane(new String(Utils.loadCharsFromURI(link,"UTF8")));
		}
		
		assignMovingKeys(pane);
		center.add(new JScrollPane(pane),String.valueOf(currentPage));
		((CardLayout)center.getLayout()).show(center,String.valueOf(currentPage));
		plainPages.add(pane);
		pane.requestFocusInWindow();
	}
	
	private void forward() {
		if (currentPage < totalPage-1) {
			((CardLayout)center.getLayout()).show(center,String.valueOf(++currentPage));
			plainPages.get(currentPage).requestFocusInWindow();
		}
	}

	private void backward() {
		if (currentPage > 0) {
			((CardLayout)center.getLayout()).show(center,String.valueOf(--currentPage));
			plainPages.get(currentPage).requestFocusInWindow();
		}
	}
	
	private JEditorPane buildPane(final Localizer localizer, final String helpId) throws ContentException {
		return buildPane(getPageContent(localizer,helpId));
	}

	private JEditorPane buildPane(final String content) throws ContentException {
		final JEditorPane	pane = new JEditorPane("text/html",content);
		
		pane.setEditable(false);
		pane.addHyperlinkListener(new HyperlinkListener(){
								@Override
								public void hyperlinkUpdate(final HyperlinkEvent e) {
									if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
										try{gotoLink(e.getURL().toURI());
										} catch (URISyntaxException | ContentException | IOException exc) {
											exc.printStackTrace();
										}
									}
								}
							});
		return pane;
	}

	private String getPageContent(final Localizer localizer, final String helpId) throws ContentException {
		try(final Writer		wr = new StringWriter()) {
			try(final Writer	cre = new CreoleWriter(wr,MarkupOutputFormat.XML2HTML)) {
				cre.write(localizer.getValue(helpId));
				cre.flush();
			}
			return wr.toString();
		} catch (IOException | LocalizationException e) {
			e.printStackTrace();
			throw new ContentException("I/O error preparing help window: "+e.getLocalizedMessage(),e); 
		}
	}
	
	private void assignMovingKeys(final JComponent component) {
		component.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(SwingUtils.KS_BACKWARD,SwingUtils.ACTION_BACKWARD);
		component.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(SwingUtils.KS_FORWARD,SwingUtils.ACTION_FORWARD);
		component.getActionMap().put(SwingUtils.ACTION_FORWARD,forwardAction);
		component.getActionMap().put(SwingUtils.ACTION_BACKWARD,backwardAction);
	}
	
	private static class InnerToolBar extends LocalizedToolBar {
		private static final long serialVersionUID = -7054690647150329911L;
		
		InnerToolBar(final Localizer localizer, final Action forwardAction, final Action backwardAction) throws LocalizationException, IllegalArgumentException {
			super(localizer);
			add(createButton(backwardAction,this.getClass().getResource("backwardGray.png"),this.getClass().getResource("backward.png"),PureLibLocalizer.TOOLBAR_HISTORY_BACKWARD),PureLibLocalizer.TOOLBAR_HISTORY_BACKWARD);
			add(createButton(forwardAction,this.getClass().getResource("forwardGray.png"),this.getClass().getResource("forward.png"),PureLibLocalizer.TOOLBAR_HISTORY_FORWARD),PureLibLocalizer.TOOLBAR_HISTORY_FORWARD);
		}
	}
}
