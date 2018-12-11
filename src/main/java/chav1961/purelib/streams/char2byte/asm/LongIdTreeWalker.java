package chav1961.purelib.streams.char2byte.asm;

import java.io.IOException;

import chav1961.purelib.streams.char2byte.asm.LongIdTree.LongIdTreeNode;

@FunctionalInterface
interface LongIdTreeWalker<T> {
	void process(LongIdTreeNode<T> node) throws IOException; 
}
