package chav1961.purelib.ui.interfaces;

import java.net.URI;

public interface ReferenceAndComment {
	URI getReference();
	void setReference(URI reference);
	String getComment();
	void setComment(String comment);

	static ReferenceAndComment of(final URI reference, final String comment) {
		return new ReferenceAndCommentImpl(reference, comment); 
	}
}
