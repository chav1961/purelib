package chav1961.purelib.ui.swing;


import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.Set;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.PureLibLocalizer;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.ui.interfacers.ControllerAction;
import chav1961.purelib.ui.interfacers.FormModel;
import chav1961.purelib.ui.interfacers.FormModel.SupportedOperations;

class MicroTableEditorEditedToolBar extends LocalizedToolBar {
	private static final long 		serialVersionUID = 5650103363252521014L;
	private static final String		INSERT_ICON = "microEditInsert.png";
	private static final String		INSERT_ICON_GRAY = "microEditInsertGray.png";
	private static final String		DUPLICATE_ICON = "microEditDuplicate.png";
	private static final String		DUPLICATE_ICON_GRAY = "microEditDuplicateGray.png";
	private static final String		DELETE_ICON = "microEditDelete.png";
	private static final String		DELETE_ICON_GRAY = "microEditDeleteGray.png";
	private static final String		COMMIT_ICON = "microEditCommit.png";
	private static final String		COMMIT_ICON_GRAY = "microEditCommitGray.png";
	private static final String		EXIT_ICON = "microEditExit.png";
	private static final String		EXIT_ICON_GRAY = "microEditExitGray.png";

	MicroTableEditorEditedToolBar(final Localizer localizer, final ActionListener parent, final Set<SupportedOperations> operations, final boolean reducedOperations) throws LocalizationException, IllegalArgumentException {
		super(localizer);

		if (operations.contains(SupportedOperations.INSERT)) {
			add(createButton(new SimpleCallAction(ControllerAction.ACTION_INSERT,parent),SwingUtils.url(INSERT_ICON_GRAY),SwingUtils.url(INSERT_ICON),PureLibLocalizer.TOOLBAR_INSERT),PureLibLocalizer.TOOLBAR_INSERT);
		}
		if (operations.contains(SupportedOperations.DUPLICATE)) {
			add(createButton(new SimpleCallAction(ControllerAction.ACTION_DUPLICATE,parent),SwingUtils.url(DUPLICATE_ICON_GRAY),SwingUtils.url(DUPLICATE_ICON),PureLibLocalizer.TOOLBAR_DUPLICATE),PureLibLocalizer.TOOLBAR_DUPLICATE);
		}
		if (operations.contains(SupportedOperations.DELETE)) {
			add(createButton(new SimpleCallAction(ControllerAction.ACTION_DELETE,parent),SwingUtils.url(DELETE_ICON_GRAY),SwingUtils.url(DELETE_ICON),PureLibLocalizer.TOOLBAR_DELETE),PureLibLocalizer.TOOLBAR_DELETE);
		}
		if (!reducedOperations) {
			addSeparator();
			add(createButton(new SimpleCallAction(ControllerAction.COMMIT,parent),SwingUtils.url(COMMIT_ICON_GRAY),SwingUtils.url(COMMIT_ICON),PureLibLocalizer.TOOLBAR_COMMIT),PureLibLocalizer.TOOLBAR_COMMIT);
			add(createButton(new SimpleCallAction(ControllerAction.EXIT,parent),SwingUtils.url(EXIT_ICON_GRAY),SwingUtils.url(EXIT_ICON),PureLibLocalizer.TOOLBAR_EXIT),PureLibLocalizer.TOOLBAR_EXIT);
		}
		
		setMinimumSize(new Dimension(16,16));
		setPreferredSize(new Dimension(100,16));
	}
}