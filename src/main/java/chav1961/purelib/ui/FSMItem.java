package chav1961.purelib.ui;

import java.util.Arrays;

public class FSMItem {
	public int 			state;
	public String		caption;
	public String		text;
	public String		image;
	public String[]		items;
	public boolean		terminal;
	public String		tooltip;
	public FSMJump[]	jumps;
	
	@Override
	public String toString() {
		return "FSMItem [state=" + state + ", caption=" + caption + ", text=" + text + ", image=" + image + ", items=" + Arrays.toString(items) + ", terminal=" + terminal + ", tooltip=" + tooltip + ", jumps=" + Arrays.toString(jumps) + "]";
	}
}
