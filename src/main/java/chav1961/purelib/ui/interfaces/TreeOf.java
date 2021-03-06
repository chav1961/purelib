package chav1961.purelib.ui.interfaces;

import java.util.Comparator;

import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;

public interface TreeOf<Nested> {
	@FunctionalInterface
	public interface Walker<Nested> {
		ContinueMode process(NodeEnterMode mode, Nested node, int... pathFromRoot);
	}
	
	Class<Nested> getComponentType();
	int size();
	void setOrder(Comparator<Nested> comparator);
	void setFilter(Filter<Nested> filter);
	Nested insert(int index);
	Nested duplicate(int index,Nested template);
	Nested get(int index);
	Nested remove(int index);
	int indexOf(Nested item);
	void walkDown(int index, Walker<Nested> walker);
	void walkUp(int index, Walker<Nested> walker);
}

