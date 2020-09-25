package chav1961.purelib.ui.swing;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.tree.DefaultMutableTreeNode;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.json.JsonRPCFactory.Transport;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.swing.useful.JStateString;

public class Client implements LocaleChangeListener {
	private final JFrame				frame;
	private final ContentNodeMetadata	root;
	private final Localizer				localizer;
	private final Transport				transport;
	private final SimpleMenuBar			menu;
	private final SimpleNavigatorTree	nav;
	private final JStateString			stateString;
	
	public Client(final ContentMetadataInterface mdi, final Transport transport) throws LocalizationException, NullPointerException {
		if (mdi == null) {
			throw new NullPointerException("Metadata interface can't be null");
		}
		else if (transport == null) {
			throw new NullPointerException("Transport can't be null");
		}
		else {
			this.root = mdi.getRoot();
			this.frame = new JFrame();
			this.transport = transport;
			this.localizer = LocalizerFactory.getLocalizer(mdi.getRoot().getLocalizerAssociated());
			this.stateString = new JStateString(this.localizer,10,true);
			
			stateString.setAutomaticClearTime(Severity.error,1,TimeUnit.MINUTES);
			stateString.setAutomaticClearTime(Severity.warning,15,TimeUnit.SECONDS);
			stateString.setAutomaticClearTime(Severity.info,5,TimeUnit.SECONDS);
			frame.getContentPane().add(this.stateString,BorderLayout.SOUTH);

			final ContentNodeMetadata	menuRoot = mdi.byUIPath(URI.create("ui:/model/navigation.top.mainmenu"));
			final ContentNodeMetadata	navRoot = mdi.byUIPath(URI.create("ui:/model/navigation.top.navigator"));
			
			if (menuRoot != null) {
				this.menu = new SimpleMenuBar(localizer,menuRoot);
				frame.getContentPane().add(this.menu,BorderLayout.NORTH);
				menu.addActionListener((e)->actionPerformed(e));
			}
			else {
				this.menu = null;
			}
			
			if (navRoot != null) {
				this.nav = new SimpleNavigatorTree(localizer,navRoot) {
								private static final long serialVersionUID = 1L;
								@Override
								protected void appendNodes(final ContentNodeMetadata submenu, final DefaultMutableTreeNode node) {
//									final String	namePrefix = submenu.getName()+'.';
									
//									for (PluginInterface<?> item : ServiceLoader.load(PluginInterface.class)) {
//										if (item.getPluginName().startsWith(namePrefix)) {
//											node.add(new DefaultMutableTreeNode(item.getMetadata(),false));
//										}
//									}
								}
							};
				frame.getContentPane().add(this.nav,BorderLayout.WEST);
				nav.addActionListener((e)->actionPerformed(e));
			}
			else {
				this.nav = null;
			}

			PureLibSettings.PURELIB_LOCALIZER.push(this.localizer);
			localizer.addLocaleChangeListener(this);
			fillLocalizedStrings(localizer.currentLocale().getLocale());
			
			if (root.getHelpId() != null) {
				SwingUtils.assignActionKey((JComponent)frame.getContentPane(),SwingUtils.KS_HELP,(e)->
				   {try {
						SwingUtils.showCreoleHelpWindow(frame,URI.create(""));
					} catch (NullPointerException | IOException e1) {
					}
				},"help");
			}
		}
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings(newLocale);
	}
	
	protected void actionPerformed(final ActionEvent e) {
		
	}

	private void fillLocalizedStrings(final Locale locale) throws LocalizationException {
		if (root.getLabelId() != null) {
			frame.setTitle(localizer.getValue(root.getLabelId()));
		}
		stateString.localeChanged(locale,locale);
		if (menu != null) {
			menu.localeChanged(locale,locale);
		}
		if (nav != null) {
			nav.localeChanged(locale,locale);
		}
	}
}
