package chav1961.purelib.ui.swing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import chav1961.purelib.ui.swing.useful.JFileSelectionDialog;
import chav1961.purelib.ui.swing.useful.JFileSelectionDialog.FilterCallback;

class FileWizardOptions {
	final int				options;
	final FilterCallback[]	filters;
	
	FileWizardOptions(final int options, final FilterCallback... filters) {
		this.options = options;
		this.filters = filters;
	}

	@Override
	public String toString() {
		return "WizardOptions [options=" + options + ", filters=" + Arrays.toString(filters) + "]";
	}

	static FileWizardOptions of(final String source) {
		final List<FilterCallback>	filters = new ArrayList<>();
		int	result = 0;
		
		for (String item : source.split("\\s*;\\s*")) {
			final String[]	keyValue = item.split("\\s*=\\s*");

			if (keyValue.length != 2) {
				throw new IllegalArgumentException("Illegal wizard options ["+source+"]: must be <key1>=<value1>;...");
			}
			else {
				switch (keyValue[0]) {
					case "selectFile"	:
						if (Boolean.valueOf(keyValue[1])) {
							result |= JFileSelectionDialog.OPTIONS_CAN_SELECT_FILE;
						}
						break;
					case "selectDir"	:
						if (Boolean.valueOf(keyValue[1])) {
							result |= JFileSelectionDialog.OPTIONS_CAN_SELECT_DIR;
						}
						break;
					case "mustExists"	:
						if (Boolean.valueOf(keyValue[1])) {
							result |= JFileSelectionDialog.OPTIONS_FILE_MUST_EXISTS;
						}
						break;
					case "forOpen"		:
						if (Boolean.valueOf(keyValue[1])) {
							result |= JFileSelectionDialog.OPTIONS_FOR_OPEN;
						}
						break;
					case "forSave"		:
						if (Boolean.valueOf(keyValue[1])) {
							result |= JFileSelectionDialog.OPTIONS_FOR_SAVE;
						}
						break;
					case "filter"		:	// type:filter_id;...
						for (String filter : keyValue[1].split("\\s*,\\s*")) {
							final String[]	components = filter.split("\\s*:\\s*");
							
							if (components.length != 2) {
								throw new IllegalArgumentException("Illegal wizard options ["+source+"]: filter description must be <mask1>[|<mask2>...<maskN>]:<title1>,...");
							}
							else {
								filters.add(FilterCallback.of(components[1], components[0].split("\\s*\\|\\s*")));
							}
						}
					default :
						throw new IllegalArgumentException("Illegal wizard options ["+source+"]: unknown option ["+keyValue[0]+"]");
				}
			}
		}
		if (result == 0) {
			result = JFileSelectionDialog.OPTIONS_FOR_OPEN | JFileSelectionDialog.OPTIONS_CAN_SELECT_FILE;
		}
		if (filters.isEmpty()) {
			filters.add(JFileSelectionDialog.ALL_FILES);
		}
		return new FileWizardOptions(result, filters.toArray(new FilterCallback[filters.size()]));
	}

}