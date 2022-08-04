package chav1961.purelib.ui.swing.useful;

import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.Element;

class PrimitiveTheOnlyElement implements Element {
	private final Document	doc;
	
	PrimitiveTheOnlyElement(final Document doc) {
		this.doc = doc;
	}

	@Override
	public Document getDocument() {
		return doc;
	}

	@Override
	public Element getParentElement() {
		return null;
	}

	@Override
	public String getName() {
		return "root";
	}

	@Override
	public AttributeSet getAttributes() {
		return null;
	}

	@Override
	public int getStartOffset() {
		return 0;
	}

	@Override
	public int getEndOffset() {
		return getDocument().getLength();
	}

	@Override
	public int getElementIndex(int offset) {
		return 0;
	}

	@Override
	public int getElementCount() {
		return 1;
	}

	@Override
	public Element getElement(int index) {
		return this;
	}

	@Override
	public boolean isLeaf() {
		return true;
	}
}