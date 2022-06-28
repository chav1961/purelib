package chav1961.purelib.ui.swing.inner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import chav1961.purelib.ui.interfaces.LongItemAndReference;
import chav1961.purelib.ui.interfaces.LongItemAndReferenceList;

public class LongItemAndReferenceListImpl<T> implements LongItemAndReferenceList<T> {
	private final List<LongItemAndReference<T>>	delegate = new ArrayList<>();
	
	public LongItemAndReferenceListImpl() {}
	
	@Override public int size() {return delegate.size();}
	@Override public boolean isEmpty() {return delegate.isEmpty();}
	@Override public boolean contains(Object o) {return delegate.contains(o);}
	@Override public Iterator<LongItemAndReference<T>> iterator() {return delegate.iterator();}
	@Override public Object[] toArray() {return delegate.toArray();}
	@Override public <T1> T1[] toArray(T1[] a) {return delegate.toArray(a);}
	@Override public boolean add(LongItemAndReference<T> e) {return delegate.add(e);}
	@Override public boolean remove(Object o) {return delegate.remove(o);}
	@Override public boolean containsAll(Collection<?> c) {return delegate.containsAll(c);}
	@Override public boolean addAll(Collection<? extends LongItemAndReference<T>> c) {return delegate.addAll(c);}
	@Override public boolean addAll(int index, Collection<? extends LongItemAndReference<T>> c) {return delegate.addAll(index, c);}
	@Override public boolean removeAll(Collection<?> c) {return delegate.removeAll(c);}
	@Override public boolean retainAll(Collection<?> c) {return delegate.retainAll(c);}
	@Override public void clear() {delegate.clear();}
	@Override public LongItemAndReference<T> get(int index) {return delegate.get(index);}
	@Override public LongItemAndReference<T> set(int index, LongItemAndReference<T> element) {return delegate.set(index, element);}
	@Override public void add(int index, LongItemAndReference<T> element) {delegate.add(index, element);}
	@Override public LongItemAndReference<T> remove(int index) {return delegate.remove(index);}
	@Override public int indexOf(Object o) {return delegate.indexOf(o);}
	@Override public int lastIndexOf(Object o) {return delegate.lastIndexOf(o);}
	@Override public ListIterator<LongItemAndReference<T>> listIterator() {return delegate.listIterator();}
	@Override public ListIterator<LongItemAndReference<T>> listIterator(int index) {return delegate.listIterator(index);}
	@Override public List<LongItemAndReference<T>> subList(int fromIndex, int toIndex) {return delegate.subList(fromIndex, toIndex);}
}
