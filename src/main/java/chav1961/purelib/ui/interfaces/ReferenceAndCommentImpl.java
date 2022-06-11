package chav1961.purelib.ui.interfaces;

import java.net.URI;
import java.util.Objects;

class ReferenceAndCommentImpl implements ReferenceAndComment {
	private URI		reference = URI.create("http:/");
	private String	comment = "";

	ReferenceAndCommentImpl(final URI reference, final String comment) {
		if (reference == null) {
			throw new NullPointerException("Reference can't be null"); 
		}
		else if (comment == null) {
			throw new NullPointerException("Comment can't be null"); 
		}
		else {
			this.reference = reference;
			this.comment = comment;
		}
	}
	
	@Override
	public URI getReference() {
		return reference;
	}

	@Override
	public void setReference(final URI reference) {
		if (reference == null) {
			throw new NullPointerException("Reference can't be null"); 
		}
		else {
			this.reference = reference;
		}
	}

	@Override
	public String getComment() {
		return comment;
	}

	@Override
	public void setComment(String comment) {
		if (comment == null) {
			throw new NullPointerException("Comment can't be null"); 
		}
		else {
			this.comment = comment;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(comment, reference);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ReferenceAndComment other = (ReferenceAndCommentImpl) obj;
		return Objects.equals(comment, other.getComment()) && Objects.equals(reference, other.getReference());
	}

	@Override
	public String toString() {
		return "ReferenceAndCommentImpl [reference=" + reference + ", comment=" + comment + "]";
	}
}
