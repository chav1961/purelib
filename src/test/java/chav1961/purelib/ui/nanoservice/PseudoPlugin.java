package chav1961.purelib.ui.nanoservice;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stax.StAXResult;
import javax.xml.transform.stax.StAXSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;
import chav1961.purelib.streams.StreamsUtil;
import chav1961.purelib.streams.char2char.CreoleWriter;
import chav1961.purelib.streams.interfaces.CharacterSource;
import chav1961.purelib.streams.interfaces.CharacterTarget;
import chav1961.purelib.ui.nanoservice.interfaces.FromBody;
import chav1961.purelib.ui.nanoservice.interfaces.FromHeader;
import chav1961.purelib.ui.nanoservice.interfaces.FromPath;
import chav1961.purelib.ui.nanoservice.interfaces.FromQuery;
import chav1961.purelib.ui.nanoservice.interfaces.Path;
import chav1961.purelib.ui.nanoservice.interfaces.QueryType;
import chav1961.purelib.ui.nanoservice.interfaces.RootPath;
import chav1961.purelib.ui.nanoservice.interfaces.ToBody;
import chav1961.purelib.ui.nanoservice.interfaces.ToHeader;

@RootPath("/root")
public class PseudoPlugin {
	@Path("/test")
	public int ping(@ToBody(mimeType="text/plain") final OutputStream os) throws IOException {
		os.write("test string".getBytes());
		os.flush();
		return 200;
	}
	
	/*
	 *		Test ToBody annotation 
	 */

	//	--------------------------- text/plain
	
	@Path(value="/get/body/OutputStream",type={QueryType.GET,QueryType.HEAD,QueryType.DELETE})
	public int call(@ToBody(mimeType="text/plain") final OutputStream os) throws IOException {
		os.write("test string".getBytes());
		os.flush();
		return 200;
	}

	@Path(value="/get/body/Writer",type={QueryType.GET,QueryType.HEAD,QueryType.DELETE})
	public int call(@ToBody(mimeType="text/plain") final Writer os) throws IOException {
		os.write("test string");
		os.flush();
		return 200;
	}

	@Path(value="/get/body/CreoleWriter",type={QueryType.GET,QueryType.HEAD,QueryType.DELETE})
	public int call(@ToBody(mimeType="text/plain") final CreoleWriter os) throws IOException {
		os.write("test string");
		os.flush();
		return 200;
	}
	
	@Path(value="/get/body/CharTarget",type={QueryType.GET,QueryType.HEAD,QueryType.DELETE})
	public int call(@ToBody(mimeType="text/plain") final CharacterTarget target) throws IOException, PrintingException {
		target.put("test string");
		return 200;
	}

	@Path(value="/get/body/StringBuilder",type={QueryType.GET,QueryType.HEAD,QueryType.DELETE})
	public int call(@ToBody(mimeType="text/plain") final StringBuilder target) throws IOException, PrintingException {
		target.append("test string");
		return 200;
	}

	//	--------------------------- text/html
	
	@Path(value="/get/body/OutputStream",type={QueryType.GET,QueryType.HEAD,QueryType.DELETE})
	public int callHtml(@ToBody(mimeType="text/html") final OutputStream os) throws IOException {
		os.write("html test string".getBytes());
		os.flush();
		return 200;
	}

	@Path(value="/get/body/Writer",type={QueryType.GET,QueryType.HEAD,QueryType.DELETE})
	public int callHtml(@ToBody(mimeType="text/html") final Writer os) throws IOException {
		os.write("html test string");
		os.flush();
		return 200;
	}

	@Path(value="/get/body/CreoleWriter",type={QueryType.GET,QueryType.HEAD,QueryType.DELETE})
	public int callHtml(@ToBody(mimeType="text/html") final CreoleWriter os) throws IOException {
		os.write("html test string");
		os.flush();
		return 200;
	}
	
	@Path(value="/get/body/CharTarget",type={QueryType.GET,QueryType.HEAD,QueryType.DELETE})
	public int callHtml(@ToBody(mimeType="text/html") final CharacterTarget target) throws IOException, PrintingException {
		target.put("html test string");
		return 200;
	}

	@Path(value="/get/body/StringBuilder",type={QueryType.GET,QueryType.HEAD,QueryType.DELETE})
	public int callHtml(@ToBody(mimeType="text/html") final StringBuilder target) throws IOException, PrintingException {
		target.append("html test string");
		return 200;
	}
	
	//	--------------------------- text/xml
	
	@Path(value="/get/body/OutputStream",type={QueryType.GET,QueryType.HEAD,QueryType.DELETE})
	public int callXml(@ToBody(mimeType="text/xml") final OutputStream os) throws IOException {
		os.write("xml test string".getBytes());
		os.flush();
		return 200;
	}

	@Path(value="/get/body/Writer",type={QueryType.GET,QueryType.HEAD,QueryType.DELETE})
	public int callXml(@ToBody(mimeType="text/xml") final Writer os) throws IOException {
		os.write("xml test string");
		os.flush();
		return 200;
	}

	@Path(value="/get/body/CreoleWriter",type={QueryType.GET,QueryType.HEAD,QueryType.DELETE})
	public int callXml(@ToBody(mimeType="text/xml") final CreoleWriter os) throws IOException {
		os.write("xml test string");
		os.flush();
		return 200;
	}
	
	@Path(value="/get/body/CharTarget",type={QueryType.GET,QueryType.HEAD,QueryType.DELETE})
	public int callXml(@ToBody(mimeType="text/xml") final CharacterTarget target) throws IOException, PrintingException {
		target.put("xml test string");
		return 200;
	}

	@Path(value="/get/body/StringBuilder",type={QueryType.GET,QueryType.HEAD,QueryType.DELETE})
	public int callXml(@ToBody(mimeType="text/xml") final StringBuilder target) throws IOException, PrintingException {
		target.append("xml test string");
		return 200;
	}
	
	@Path(value="/get/body/XMLStreamWriter",type={QueryType.GET,QueryType.HEAD,QueryType.DELETE})
	public int callXml(@ToBody(mimeType="text/xml") final XMLStreamWriter target) throws IOException, PrintingException, XMLStreamException {
		target.writeStartDocument();
		target.setPrefix("test","http://test");
		target.setDefaultNamespace("http://test");
		target.writeStartElement("http://test","a");
		target.writeAttribute("b","xml test string");
		target.writeCharacters("xml test string");
		target.writeEndElement();
		return 200;
	}

	@Path(value="/get/body/Document",type={QueryType.GET,QueryType.HEAD,QueryType.DELETE})
	public int callXml(@ToBody(mimeType="text/xml") final Document target) throws IOException, PrintingException {
        final Element 	rootElement = target.createElementNS("https://www.test", "Test");
        final Element 	subElement = target.createElement("Content");;
        final Text 		item = target.createTextNode("xml test string");
        
        subElement.appendChild(item);
        rootElement.appendChild(subElement);
        target.appendChild(rootElement);
		return 200;
	}

	//	--------------------------- application/json
	
	@Path(value="/get/body/OutputStream",type={QueryType.GET,QueryType.HEAD,QueryType.DELETE})
	public int callJson(@ToBody(mimeType="application/json") final OutputStream os) throws IOException {
		os.write("json test string".getBytes());
		os.flush();
		return 200;
	}

	@Path(value="/get/body/Writer",type={QueryType.GET,QueryType.HEAD,QueryType.DELETE})
	public int callJson(@ToBody(mimeType="application/json") final Writer os) throws IOException {
		os.write("json test string");
		os.flush();
		return 200;
	}

	@Path(value="/get/body/CharTarget",type={QueryType.GET,QueryType.HEAD,QueryType.DELETE})
	public int callJson(@ToBody(mimeType="application/json") final CharacterTarget target) throws IOException, PrintingException {
		target.put("json test string");
		return 200;
	}

	@Path(value="/get/body/StringBuilder",type={QueryType.GET,QueryType.HEAD,QueryType.DELETE})
	public int callJson(@ToBody(mimeType="application/json") final StringBuilder target) throws IOException, PrintingException {
		target.append("json test string");
		return 200;
	}

	@Path(value="/get/body/JsonStaxPrinter",type={QueryType.GET,QueryType.HEAD,QueryType.DELETE})
	public int callJson(@ToBody(mimeType="application/json") final JsonStaxPrinter target) throws IOException, PrintingException {
		target.value("json test string");
		return 200;
	}

	@Path(value="/get/body/JsonSerializer",type={QueryType.GET,QueryType.HEAD,QueryType.DELETE})
	public int callJson(@ToBody(mimeType="application/json") final ForJson target) throws IOException, PrintingException {
		target.content = "json test string";
		return 200;
	}

	//	--------------------------- application/octet-stream
	
	@Path(value="/get/body/OutputStream",type={QueryType.GET,QueryType.HEAD,QueryType.DELETE})
	public int callOctetStream(@ToBody(mimeType="application/octet-stream") final OutputStream os) throws IOException {
		os.write("octet test string".getBytes());
		os.flush();
		return 200;
	}

	/*
	 *		Test FromPath annotation 
	 */

	@Path("/get/path/{parm1}/parm2/parm3")
	public int callPath1(@FromPath("parm1") String parm1, @ToBody(mimeType="text/plain") final Writer os) throws IOException {
		os.write(parm1 == null ? "NULL" : parm1);
		os.flush();
		return 200;
	}

	@Path("/get/path/parm1/{parm2}/parm3")
	public int callPath2(@FromPath("parm2") String parm2, @ToBody(mimeType="text/plain") final Writer os) throws IOException {
		os.write(parm2);
		os.flush();
		return 200;
	}

	@Path("/get/path/parm1/parm2/{parm3}")
	public int callPath3(@FromPath("parm3") String parm3, @ToBody(mimeType="text/plain") final Writer os) throws IOException {
		os.write(parm3);
		os.flush();
		return 200;
	}

	@Path("/get/path1/*")
	public int callPath4(@FromPath("*") String parm, @ToBody(mimeType="text/plain") final Writer os) throws IOException {
		os.write("asterisk "+parm);
		os.flush();
		return 200;
	}

	/*
	 *		Test FromQuery annotation 
	 */

	@Path("/get/query")
	public int callQuery(@FromQuery("parm1") String parm1, @FromQuery("parm2") String parm2, @FromQuery("parm3") String parm3, @ToBody(mimeType="text/plain") final Writer os) throws IOException {
		os.write(parm1 == null ? "NULL" : parm1);
		os.write(parm2 == null ? "NULL" : parm2);
		os.write(parm3 == null ? "NULL" : parm3);
		os.flush();
		return 200;
	}
	
	/*
	 *		Test FromHeader annotation 
	 */

	@Path("/get/header")
	public int callRequestHead(@FromHeader("parm1") String parm1, @FromHeader("parm2") String parm2, @FromHeader("parm3") String parm3, @ToBody(mimeType="text/plain") final Writer os) throws IOException {
		os.write(parm1 == null ? "NULL" : parm1);
		os.write(parm2 == null ? "NULL" : parm2);
		os.write(parm3 == null ? "NULL" : parm3);
		os.flush();
		return 200;
	}	

	/*
	 *		Test ToHeader annotation 
	 */

	@Path("/get/responseheader")
	public int callResponseHead(@ToHeader("parm1") StringBuilder sb, @ToHeader("parm2") List<String> list, @ToHeader("@ForJson") ForJson json, @ToBody(mimeType="text/plain") final Writer os) throws IOException {
		list.add("URA!");
		os.flush();
		return 200;
	}	

	/*
	 *		Test FromBody annotation 
	 */

	//	--------------------------- text/plain
	
	@Path(value="/post/body/InputStream",type={QueryType.POST,QueryType.PUT})
	public int call(@FromBody(mimeType="text/plain") final InputStream is, @ToBody(mimeType="text/plain") final OutputStream os) throws IOException {
		Utils.copyStream(is, os);
		return 200;
	}

	@Path(value="/post/body/Reader",type={QueryType.POST,QueryType.PUT})
	public int call(@FromBody(mimeType="text/plain") final Reader is, @ToBody(mimeType="text/plain") final Writer os) throws IOException {
		Utils.copyStream(is, os);
		return 200;
	}

	@Path(value="/post/body/CharSource",type={QueryType.POST,QueryType.PUT})
	public int call(@FromBody(mimeType="text/plain") final CharacterSource source, @ToBody(mimeType="text/plain") final CharacterTarget target) throws PrintingException, ContentException {
		Utils.copyStream(source,target);
		return 200;
	}

	@Path(value="/post/body/String",type={QueryType.POST,QueryType.PUT})
	public int call(@FromBody(mimeType="text/plain") final String source, @ToBody(mimeType="text/plain") final StringBuilder target) throws IOException, PrintingException {
		target.append(source);
		return 200;
	}

	//	--------------------------- text/xml
	
	@Path(value="/post/body/InputStream",type={QueryType.POST,QueryType.PUT})
	public int callXml(@FromBody(mimeType="text/xml") final InputStream is, @ToBody(mimeType="text/xml") final OutputStream os) throws IOException {
		Utils.copyStream(is, os);
		return 200;
	}

	@Path(value="/post/body/Reader",type={QueryType.POST,QueryType.PUT})
	public int callXml(@FromBody(mimeType="text/xml") final Reader is, @ToBody(mimeType="text/xml") final Writer os) throws IOException {
		Utils.copyStream(is, os);
		return 200;
	}

	@Path(value="/post/body/CharSource",type={QueryType.POST,QueryType.PUT})
	public int callXml(@FromBody(mimeType="text/xml") final CharacterSource source, @ToBody(mimeType="text/xml") final CharacterTarget target) throws PrintingException, ContentException {
		Utils.copyStream(source, target);
		return 200;
	}

	@Path(value="/post/body/String",type={QueryType.POST,QueryType.PUT})
	public int callXml(@FromBody(mimeType="text/xml") final String source, @ToBody(mimeType="text/xml") final StringBuilder target) throws IOException, PrintingException {
		target.append(source);
		return 200;
	}
	
	@Path(value="/post/body/XMLStreamReader",type={QueryType.POST,QueryType.PUT})
	public int callXml(@FromBody(mimeType="text/xml") final XMLStreamReader source, @ToBody(mimeType="text/xml") final XMLStreamWriter target) throws IOException, PrintingException, XMLStreamException, TransformerException {
		final TransformerFactory 	tf = TransformerFactory.newInstance();
		final Transformer 			t = tf.newTransformer();
		final StAXSource 			from = new StAXSource(source);
		final StAXResult 			to = new StAXResult(target);
		
		t.transform(from,to);
		return 200;
	}

	@Path(value="/post/body/Document",type={QueryType.POST,QueryType.PUT})
	public int callXml(@FromBody(mimeType="text/xml") final Document source, @ToBody(mimeType="text/xml") final Document target) throws IOException, PrintingException, TransformerException {
		final TransformerFactory 	tf = TransformerFactory.newInstance();
		final Transformer 			t = tf.newTransformer();
		final DOMSource 			from = new DOMSource(source);
		final DOMResult 			to = new DOMResult(target);
		
		t.transform(from,to);
		return 200;
	}

	//	--------------------------- application/json
	
	@Path(value="/post/body/InputStream",type={QueryType.POST,QueryType.PUT})
	public int callJson(@FromBody(mimeType="application/json") final InputStream is, @ToBody(mimeType="application/json") final OutputStream os) throws IOException {
		Utils.copyStream(is, os);
		return 200;
	}

	@Path(value="/post/body/Reader",type={QueryType.POST,QueryType.PUT})
	public int callJson(@FromBody(mimeType="application/json") final Reader is, @ToBody(mimeType="application/json") final Writer os) throws IOException {
		Utils.copyStream(is, os);
		return 200;
	}

	@Path(value="/post/body/CharSource",type={QueryType.POST,QueryType.PUT})
	public int callJson(@FromBody(mimeType="application/json") final CharacterSource source, @ToBody(mimeType="application/json") final CharacterTarget target) throws PrintingException, ContentException {
		Utils.copyStream(source,target);
		return 200;
	}

	@Path(value="/post/body/String",type={QueryType.POST,QueryType.PUT})
	public int callJson(@FromBody(mimeType="application/json") final String source, @ToBody(mimeType="application/json") final StringBuilder target) throws IOException, PrintingException {
		target.append(source);
		return 200;
	}

	@Path(value="/post/body/JsonStaxParser",type={QueryType.POST,QueryType.PUT})
	public int callJson(@FromBody(mimeType="application/json") final JsonStaxParser source, @ToBody(mimeType="application/json") final JsonStaxPrinter target) throws IOException, PrintingException, SyntaxException {
		StreamsUtil.copyStax(source, target);
		return 200;
	}

	@Path(value="/post/body/JsonSerializer",type={QueryType.POST,QueryType.PUT})
	public int callJson(@FromBody(mimeType="application/json") final ForJson source, @ToBody(mimeType="application/json") final ForJson target) throws IOException, PrintingException {
		target.content = source.content;
		target.message = source.message;
		return 200;
	}

	//	--------------------------- application/octet-stream
	
	@Path(value="/post/body/InputStream",type={QueryType.POST,QueryType.PUT})
	public int callOctetStream(@FromBody(mimeType="application/octet-stream") final InputStream is, @ToBody(mimeType="application/octet-stream") final OutputStream os) throws IOException {
		Utils.copyStream(is, os);
		return 200;
	}
}