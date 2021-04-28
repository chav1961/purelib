package chav1961.purelib.ui;

import java.io.Serializable;

public class AnchorPointPair implements Serializable, Cloneable {
	private static final long serialVersionUID = -5691082930281975410L;
	
	private AnchorPoint		first = new AnchorPoint();
	private AnchorPoint		second = new AnchorPoint();

	public AnchorPointPair() {
	}	
	
	public AnchorPointPair(final AnchorPoint first, final AnchorPoint second) throws NullPointerException {
		if (first == null) {
			throw new NullPointerException("First anchor point can't be null"); 
		}
		else if (second == null) {
			throw new NullPointerException("Second anchor point can't be null"); 
		}
		else {
			this.first = first;
			this.second = second;
		}
	}

	public AnchorPoint getFirst() {
		return first;
	}
	
	public void setFirst(final AnchorPoint first) throws NullPointerException {
		if (first == null) {
			throw new NullPointerException("First anchor point can't be null"); 
		}
		else {
			this.first = first;
		}
	}
	
	public AnchorPoint getSecond() {
		return second;
	}
	
	public void setSecond(final AnchorPoint second) throws NullPointerException {
		if (second == null) {
			throw new NullPointerException("Second anchor point can't be null"); 
		}
		else {
			this.second = second;
		}
	}
	
	@Override
	public AnchorPointPair clone() throws CloneNotSupportedException {
		final AnchorPointPair	result = (AnchorPointPair)super.clone();
		
		result.setFirst(first.clone());
		result.setSecond(second.clone());
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		AnchorPointPair other = (AnchorPointPair) obj;
		if (first == null) {
			if (other.first != null) return false;
		} else if (!first.equals(other.first)) return false;
		if (second == null) {
			if (other.second != null) return false;
		} else if (!second.equals(other.second)) return false;
		return true;
	}

	@Override
	public String toString() {
		return "AnchorPointPair [first=" + first + ", second=" + second + "]";
	}
}
