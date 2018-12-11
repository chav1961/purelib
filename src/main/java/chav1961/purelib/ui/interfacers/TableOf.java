package chav1961.purelib.ui.interfacers;

import java.util.Comparator;

public interface TableOf<Nested> {
	Class<Nested> getComponentType();
	int size();
	void setOrder(Comparator<Nested> comparator);
	void setFilter(Filter<Nested> filter);
	Nested insert(int index);
	Nested duplicate(int index,Nested template);
	Nested get(int index);
	Nested remove(int index);
	int indexOf(Nested item);
}
