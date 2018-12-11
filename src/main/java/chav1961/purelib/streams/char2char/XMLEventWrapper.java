package chav1961.purelib.streams.char2char;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

class XMLEventWrapper implements XMLEventWriter {
	private final List<String>		stack = new ArrayList<>();
	private final XMLEventWriter 	nested;
	
	public XMLEventWrapper(final XMLEventWriter nested) throws NullPointerException {
		if (nested == null) {
			throw new NullPointerException("Nested writer can't be null");
		}
		else {
			this.nested = nested;
		}
	}

	@Override
	public void flush() throws XMLStreamException {
		nested.flush();
	}

	@Override
	public void close() throws XMLStreamException {
		nested.close();
	}

	@Override
	public void add(final XMLEvent event) throws XMLStreamException {
		if (event instanceof StartElement) {
			stack.add(0,((StartElement)event).getName().toString());
		}
		else if (event instanceof EndElement) {
			if (stack.get(0).equals(((EndElement)event).getName().toString())) {
				stack.remove(0);
			}
			else {
				throw new XMLStreamException("Illegal tag sequence: endElement ["+((EndElement)event).getName()+"] doesn't match awaited end element ["+stack.get(0)+"]");
			}
		}
		nested.add(event);
	}

	@Override
	public void add(XMLEventReader reader) throws XMLStreamException {
		nested.add(reader);
	}

	@Override
	public String getPrefix(final String uri) throws XMLStreamException {
		return nested.getPrefix(uri);
	}

	@Override
	public void setPrefix(final String prefix, final String uri) throws XMLStreamException {
		nested.setPrefix(prefix, uri);
	}

	@Override
	public void setDefaultNamespace(final String uri) throws XMLStreamException {
		nested.setDefaultNamespace(uri);
	}

	@Override
	public void setNamespaceContext(final NamespaceContext context) throws XMLStreamException {
		nested.setNamespaceContext(context);
	}

	@Override
	public NamespaceContext getNamespaceContext() {
		return nested.getNamespaceContext();
	}
}
