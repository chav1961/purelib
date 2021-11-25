package chav1961.purelib.ui.interfaces;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import chav1961.purelib.basic.Utils;

public interface ItemAndSelection<T> {
	boolean isSelected();
	void setSelected(boolean selected);
	T getItem();
	void setItem(T item);
	
	static <T> ItemAndSelection<T>[] of(final T... content) {
		if (content == null || Utils.checkArrayContent4Nulls(content) >= 0) {
			throw new NullPointerException("Content is null or contains nulls inside");
		}
		else {
			return of(Arrays.asList(content));
		}
	}

	static <T> ItemAndSelection<T>[] of(final Iterable<T> content) {
		if (content == null) {
			throw new NullPointerException("Content is null");
		}
		else {
			final List<ItemAndSelection<T>> result = new ArrayList<>();

			for (T item : content) {
				final T	currentItem = item;
				
				result.add(new ItemAndSelection<T>() {
					boolean	isSelected = false;
					T		item = currentItem;

					@Override
					public boolean isSelected() {
						return isSelected;
					}

					@Override
					public void setSelected(final boolean selected) {
						this.isSelected = selected;
					}

					@Override
					public T getItem() {
						return item;
					}

					@Override
					public void setItem(final T item) {
						if (item == null) {
							throw new NullPointerException("Item to set can't be null"); 
						}
						else {
							this.item = item;
						}
					}
					
					@Override
					public String toString() {
						return "ItemAndSelection {selected="+isSelected+", item="+item+"}";
					}
				}); 
			}
			return result.toArray(new ItemAndSelection[result.size()]);
		}
	}
	
	static <T> T[] extract(final boolean selection, final ItemAndSelection<T>... content) {
		if (content == null || Utils.checkArrayContent4Nulls(content) >= 0) {
			throw new NullPointerException("Content is null or contains nulls inside");
		}
		else {
			final Set<Class<?>>	classes = new HashSet<>();
			int					counter = 0;
			
			for (ItemAndSelection<T> item : content) {
				if (item.isSelected() == selection) {
					classes.add(item.getItem().getClass());
					counter++;
				}
			}
			
			Class<?>	topMost = null;
			for (Class<?> item : classes) {
				if (topMost == null) {
					topMost = item;
				}
				if (item.isAssignableFrom(topMost)) {
					topMost = item;
				}
			}
			
			final T[]	array = (T[])Array.newInstance(topMost, counter);
			
			counter = 0;
			for (ItemAndSelection<T> item : content) {
				if (item.isSelected() == selection) {
					Array.set(array, counter++, item.getItem());
				}
			}
			return array;
		}
	}

	static <T> Iterable<T> extract(final boolean selection, final Iterable<ItemAndSelection<T>> content) {
		if (content == null || Utils.checkArrayContent4Nulls(content) >= 0) {
			throw new NullPointerException("Content is null or contains nulls inside");
		}
		else {
			final List<T>		result = new ArrayList<>();
			
			for (ItemAndSelection<T> item : content) {
				if (item.isSelected() == selection) {
					result.add(item.getItem());
				}
			}
			
			return result;
		}
	}
}
