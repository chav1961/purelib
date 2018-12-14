package chav1961.purelib.ui.swing;

import java.awt.BorderLayout;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.interfacers.Recommendations;

public class Interpreter extends JFrame implements LocaleChangeListener {
	private static final long serialVersionUID = -2536520199081216211L;

	private final ContentMetadataInterface	mdi;
	private final Recommendations 			recommendations;
	
	public Interpreter(final ContentMetadataInterface mdi, final Recommendations recommendations) {
		super();
		
		if (mdi == null) {
			throw new NullPointerException("Metadata descriptor can't be null"); 
		}
		else if (recommendations == null) {
			throw new NullPointerException("Recommendations can't be null"); 
		}
		else {
			this.mdi = mdi;
			this.recommendations = recommendations;
			mdi.walkDown((mode,applicationPath,uiPath,node)->{return buildContent(mdi,mode,applicationPath,uiPath,node);},mdi.getRoot().getUIPath());
		}
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		// TODO Auto-generated method stub
		
	}
	
	private ContinueMode buildContent(final ContentMetadataInterface mdi, final NodeEnterMode mode, final URI applicationPath, final URI uiPath, final ContentNodeMetadata node) {
		if (node.getRelativeUIPath().getPath().startsWith("./navigation.top")) {
			final List<JComponent>	menus = new ArrayList<>();
			final JMenuBar			bar = new JMenuBar();

			bar.setName(node.getName());
			menus.add(bar);
			mdi.walkDown((innerMode,innerApplicationPath,innerUiPath,innerNode)->{
				try{if (node.getRelativeUIPath().getPath().startsWith("./navigation.leaf")) {
						final Localizer		localizer = LocalizerFactory.getLocalizer(innerNode.getLocalizerAssociated());
						final JMenuItem		item;
						
						switch (mode) {
							case ENTER	:
								item = new JMenuItem(localizer.getValue(innerNode.getLabelId()));
								
								item.setName(innerNode.getName());
								item.setToolTipText(localizer.getValue(innerNode.getLabelId()));
								item.addActionListener((e)->{processAction(innerNode.getApplicationPath());});
								menus.add(0,item);
								break;
							case EXIT	:
								item = (JMenuItem) menus.remove(0);
								if (menus.get(0) instanceof JMenu) {
									((JMenu)menus.get(0)).add(item);
								}
								else if (menus.get(0) instanceof JMenuBar) {
									((JMenuBar)menus.get(0)).add(item);
								}
								break;
							default:
								break;
						}
					}
					else if (node.getRelativeUIPath().getPath().startsWith("./navigation.node")) {
						final Localizer		localizer = LocalizerFactory.getLocalizer(innerNode.getLocalizerAssociated());
						final JMenu			item;
						
						switch (mode) {
							case ENTER	:
								item = new JMenu(localizer.getValue(innerNode.getLabelId()));
								
								item.setName(innerNode.getName());
								item.setToolTipText(localizer.getValue(innerNode.getLabelId()));
								menus.add(0,item);
								break;
							case EXIT	:
								item = (JMenu) menus.remove(0);
								if (menus.get(0) instanceof JMenu) {
									((JMenu)menus.get(0)).add(item);
								}
								else if (menus.get(0) instanceof JMenuBar) {
									((JMenuBar)menus.get(0)).add(item);
								}
								break;
							default:
								break;
						}
					}
					return ContinueMode.CONTINUE;
				} catch (IOException | LocalizationException e) {
					return ContinueMode.STOP;
				}				
				}
			,uiPath);
			getContentPane().add(menus.remove(0),BorderLayout.NORTH);
			return ContinueMode.SKIP_CHILDREN; 
		}
		else {
			return ContinueMode.SKIP_CHILDREN;
		}
	}

	private void processAction(final URI action) {
		
	}
}
