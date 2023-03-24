package chav1961.purelib.streams.char2byte.asm;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import chav1961.purelib.basic.PureLibSettings;

class DiagnosticPrinter implements Closeable {
	static enum BlockType {
		PLAIN,
		INCLUDE,
		MACRO,
		MACRO_EXTENSION
	}

	private final boolean			suppressPrint;
	private final boolean			printLines;
	private final boolean			printAddress;
	private final boolean			printCode;
	private final boolean			printSource;
	private final boolean			printMacros;
	private final boolean			printTotals;
	private final boolean			printExpanded;
	private final Writer			nested;
	private final List<BlockType>	blockType = new ArrayList<>(Arrays.asList(BlockType.PLAIN)); 
	
	DiagnosticPrinter(final Writer nested) {
		this.nested = nested;
		if (nested == null) {
			this.suppressPrint = true;
			this.printLines = false;
			this.printAddress = false;
			this.printCode = false;
			this.printSource = false;
			this.printMacros = false;
			this.printTotals = false;
			this.printExpanded = false;
		}
		else {
			this.suppressPrint = PureLibSettings.instance().getProperty(PureLibSettings.SUPPRESS_PRINT_ASSEMBLER, boolean.class, "true");
			this.printLines = PureLibSettings.instance().getProperty(PureLibSettings.PRINT_ASSEMBLER_LINES, boolean.class, "true");
			this.printAddress = PureLibSettings.instance().getProperty(PureLibSettings.PRINT_ASSEMBLER_ADDRESS, boolean.class, "false");
			this.printCode = PureLibSettings.instance().getProperty(PureLibSettings.PRINT_ASSEMBLER_CODE, boolean.class, "false");
			this.printSource = PureLibSettings.instance().getProperty(PureLibSettings.PRINT_ASSEMBLER_SOURCE, boolean.class, "true");
			this.printMacros = PureLibSettings.instance().getProperty(PureLibSettings.PRINT_ASSEMBLER_MACROS, boolean.class, "true");
			this.printTotals = PureLibSettings.instance().getProperty(PureLibSettings.PRINT_ASSEMBLER_TOTALS, boolean.class, "false");
			this.printExpanded = PureLibSettings.instance().getProperty(PureLibSettings.PRINT_EXPANDED_MACROS, boolean.class, "false");
		}
	}

	@Override
	public void close() throws IOException {
		leaveBlock();
		if (isPrintingRequired() && printTotals) {
			printTotals();
		}
		if (nested != null) {
			nested.flush();
		}
	}
	
	boolean isPrintingRequired() {
		if (suppressPrint) {
			return false;
		}
		else {
			switch (blockType.get(0)) {
				case INCLUDE			: return true;
				case MACRO				: return printMacros;
				case MACRO_EXTENSION	: return printExpanded;
				case PLAIN				: return true;
				default: throw new UnsupportedOperationException("Block type ["+blockType.get(0)+"] is not supported yet"); 
			}
		}
	}
	
	void enterBlock(final BlockType type) {
		if (blockType == null) {
			throw new NullPointerException("Block type can't be null");
		}
	}
	
	void leaveBlock() {
		if (blockType.isEmpty()) {
			throw new IllegalStateException("Block stack exhausted"); 
		}
		else {
			blockType.remove(0);
		}
	}
	
	void printBefore(final char[] content, final int from, final int to) {
		if (!suppressPrint) {
			
		}
	}

	void printAfter(final char[] content, final int from, final int to) {
		if (!suppressPrint) {
			
		}
	}

	private void printTotals() {
		// TODO Auto-generated method stub
		
	}

}
