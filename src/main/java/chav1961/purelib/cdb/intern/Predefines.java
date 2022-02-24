package chav1961.purelib.cdb.intern;

import chav1961.purelib.basic.interfaces.ModuleAccessor;

public enum Predefines implements ModuleAccessor {
	Empty, Name, FixedNumber, FloatNumber, QuotedString, DoubleQuotedString;

	@Override
	public void allowUnnamedModuleAccess(final Module... unnamedModules) {
		for (Module item : unnamedModules) {
			this.getClass().getModule().addExports(this.getClass().getPackageName(),item);
		}
	}
}