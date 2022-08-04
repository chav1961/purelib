package chav1961.purelib.ui.swing.useful;

import java.util.HashMap;
import java.util.Map;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.event.DocumentEvent.ElementChange;
import javax.swing.event.DocumentEvent.EventType;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.Segment;

import chav1961.purelib.concurrent.LightWeightListenerList;

class PrimitiveDocument implements Document {
	final StringBuilder			sb = new StringBuilder();
	final Map<Object,Object>	props = new HashMap<>();
	final Element				root = new PrimitiveTheOnlyElement(this);
	final LightWeightListenerList<DocumentListener>	listeners = new LightWeightListenerList<>(DocumentListener.class);
	Position					start, end;

	PrimitiveDocument() {
		try{start = createPosition(0);
			end = createPosition(0);
		} catch (BadLocationException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public int getLength() {
		return sb.length();
	}

	@Override
	public void addDocumentListener(DocumentListener listener) {
		listeners.addListener(listener);
	}

	@Override
	public void removeDocumentListener(DocumentListener listener) {
		listeners.removeListener(listener);
	}

	@Override
	public void addUndoableEditListener(UndoableEditListener listener) {
	}

	@Override
	public void removeUndoableEditListener(UndoableEditListener listener) {
	}

	@Override
	public Object getProperty(final Object key) {
		return props.get(key);
	}

	@Override
	public void putProperty(final Object key, final Object value) {
		props.put(key, value);
	}

	@Override
	public void remove(final int offs, final int len) throws BadLocationException {
		final Document	d = this;
		
		sb.delete(offs, offs+len);
		start = end = createPosition(offs);
		final DocumentEvent	de = new DocumentEvent() {
								@Override public int getOffset() {return offs;}
								@Override public int getLength() {return len;}
								@Override public Document getDocument() {return d;}
								@Override public EventType getType() {return EventType.REMOVE;}
								@Override public ElementChange getChange(Element elem) {return null;}
							};
		
		listeners.fireEvent((l)->l.removeUpdate(de));
	}

	@Override
	public void insertString(final int offset, final String str, AttributeSet a) throws BadLocationException {
		final Document	d = this;
		
		sb.insert(offset, str);
		start = end = createPosition(offset+str.length());
		final DocumentEvent	de = new DocumentEvent() {
								@Override public int getOffset() {return offset;}
								@Override public int getLength() {return str.length();}
								@Override public Document getDocument() {return d;}
								@Override public EventType getType() {return EventType.INSERT;}
								@Override public ElementChange getChange(Element elem) {return null;}
							};
		
		listeners.fireEvent((l)->l.removeUpdate(de));
	}

	@Override
	public String getText(int offset, int length) throws BadLocationException {
		return sb.substring(offset, offset+length);
	}

	@Override
	public void getText(int offset, int length, Segment txt) throws BadLocationException {
		final char[]	result = new char[length];
		
		sb.getChars(length, 0, result, length);
		txt.array = result;
		txt.offset = 0;
		txt.count = length;
	}

	@Override
	public Position getStartPosition() {
		return start;
	}

	@Override
	public Position getEndPosition() {
		return end;
	}

	@Override
	public Position createPosition(final int offs) throws BadLocationException {
		return new Position() {
			@Override public int getOffset() {return offs;}
		};
	}

	@Override
	public Element[] getRootElements() {
		return new Element[] {getDefaultRootElement()};
	}

	@Override
	public Element getDefaultRootElement() {
		return root;
	}

	@Override
	public void render(Runnable r) {
		// TODO Auto-generated method stub
		
	}
	
	void insert(final char symbol) throws BadLocationException {
		removeSelected();
		insertString(getStartPosition().getOffset(), new String(new char[] {symbol}), null);
	}
	
	void left() throws BadLocationException {
		start = end = createPosition(Math.max(0, getStartPosition().getOffset() - 1));
	}

	void right() throws BadLocationException {
		start = end = createPosition(Math.min(getLength() - 1, getStartPosition().getOffset() + 1));
	}

	void home() throws BadLocationException {
		start = end = createPosition(0);
	}

	void end() throws BadLocationException {
		start = end = createPosition(getLength() - 1);
	}

	void leftSel() throws BadLocationException {
		end = createPosition(Math.max(0, getStartPosition().getOffset() - 1));
	}

	void rightSel() throws BadLocationException {
		end = createPosition(Math.min(getLength() - 1, getStartPosition().getOffset() + 1));
	}

	void homeSel() throws BadLocationException {
		end = createPosition(0);
	}

	void endSel() throws BadLocationException {
		end = createPosition(getLength() - 1);
	}
	
	void del() throws BadLocationException {
		removeSelected();
		remove(getStartPosition().getOffset(),1);
	}		

	void bksp() throws BadLocationException {
		removeSelected();
		remove(getStartPosition().getOffset(),1);
	}		

	void cut() throws BadLocationException {
		removeSelected();
	}		

	void copy() throws BadLocationException {
	}		

	void paste() throws BadLocationException {
		removeSelected();
	}
	
	void removeSelected() throws BadLocationException {
		if (getStartPosition().getOffset() != getEndPosition().getOffset()) {
			if (getStartPosition().getOffset() < getEndPosition().getOffset()) {
				remove(getStartPosition().getOffset(),getEndPosition().getOffset());
			}
			else {
				remove(getEndPosition().getOffset(),getStartPosition().getOffset());
			}
		}
	}
}