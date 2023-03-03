package chav1961.purelib.ui.swing.useful;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;

import javax.swing.JPanel;
import javax.swing.KeyStroke;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.swing.AutoBuiltForm;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.useful.JCloseableTab;
import chav1961.purelib.ui.swing.useful.JCloseableTabbedPane;

public class JTabbedEditor extends JCloseableTabbedPane {
	private static final long serialVersionUID = 6429947271830580356L;
	private static final int[]			KEYS = {KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_3, KeyEvent.VK_4, KeyEvent.VK_5, KeyEvent.VK_6, KeyEvent.VK_7, KeyEvent.VK_8, KeyEvent.VK_9};

	private final Localizer				localizer;
	private final FormManager[]			content;
	private final AutoBuiltForm<?,?>[]	forms;
	private final JPanel[]				blanks;
	
	public JTabbedEditor(final Localizer localizer, final FormManager... content) throws ContentException {
		if (localizer == null) {
			throw new NullPointerException("Localzier can't be null");
		}
		else if (content == null || content.length == 0 || Utils.checkArrayContent4Nulls(content) >= 0) {
			throw new IllegalArgumentException("Component list is null, empty o contains sulls inside");
		}
		else {
			this.localizer = localizer;
			this.content = content;
			this.forms = new AutoBuiltForm[content.length];
			this.blanks = new JPanel[content.length];
			int	maxX = 0, maxY = 0;
			
			for(int index = 0; index < forms.length; index++) {
				final int						selectedIndex = index;
				final Object					inst = content[index];
				final ContentMetadataInterface	mdi = ContentModelFactory.forAnnotatedClass(inst.getClass());
				final AutoBuiltForm				abf = new AutoBuiltForm(mdi, localizer, PureLibSettings.INTERNAL_LOADER, inst, (FormManager)content[index]);
				final JCloseableTab				tabLabel = new JCloseableTab(localizer, mdi.getRoot(), false);
				final JPanel					blank = new JPanel();
				final JPanel					tab = new JPanel(new BorderLayout());
				
				((ModuleAccessor)inst).allowUnnamedModuleAccess(abf.getUnnamedModules());
				forms[index] = abf;
				blanks[index] = blank;
				
				maxX = Math.max(maxX, abf.getPreferredSize().width);
				maxY = Math.max(maxY, abf.getPreferredSize().height);
				tab.add(abf, BorderLayout.CENTER);
				tab.add(blank, BorderLayout.SOUTH);
				addTab("", tab);
				setTabComponentAt(getTabCount()-1, tabLabel);
				tabLabel.associate(this, tab);
				tabLabel.setToolTipText(mdi.getRoot().getTooltipId());
				
				if (index < KEYS.length) {
					SwingUtils.assignActionKey(this, WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, KeyStroke.getKeyStroke(KEYS[index], KeyEvent.CTRL_DOWN_MASK), (e)->setSelectedIndex(selectedIndex), "select "+index);
				}
			}
			
			for(int index = 0; index < forms.length; index++) {
				final AutoBuiltForm	abf = forms[index];
				final JPanel		blank = blanks[index];
				final Dimension		dim = new Dimension(maxX, maxY - abf.getPreferredSize().height);
				
				blank.setMinimumSize(dim);
				blank.setPreferredSize(dim);
			}			
		}
	}

	@Override
	public void close() throws RuntimeException {
		super.close();
	}
}
