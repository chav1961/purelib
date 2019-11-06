import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Test2 {
	
	public static void parse() throws XPathExpressionException {
		final XPathFactory 	xPathfactory = XPathFactory.newInstance();
		final XPath 		xpath = xPathfactory.newXPath();
		
		final XPathExpression expr = xpath.compile("");	
	}

	static class ParsedContent {
		final XPathExpression		xpath;
		final Map<String,String>	content;
		
		ParsedContent(final XPathExpression xPathExpression, final String contentAssociated) {
			this.xpath = xPathExpression;
			this.content = disassemble(contentAssociated);
		}
		
		public XPathExpression getXPathExpression() {
			return xpath;
		}
		
		public Iterable<Node> filter(final Node node) throws XPathExpressionException {
			final NodeList	list = (NodeList) xpath.evaluate(node, XPathConstants.NODESET);
			
			return new Iterable<Node>() {
				@Override
				public Iterator<Node> iterator() {
					return new Iterator<Node>() {
						int	currentIndex = 0, maxIndex = list.getLength();

						@Override
						public boolean hasNext() {
							return currentIndex < maxIndex;
						}

						@Override
						public Node next() {
							return list.item(currentIndex++);
						}
					};
				}
			};
		}
		
		public String joinContent(final CharSequence toJoinFor) {
			final Map<String,String>	temp = new HashMap<>();
			
			temp.putAll(disassemble(toJoinFor));
			temp.putAll(content);
			return assemble(temp);
		}
		
		public static Map<String,String> disassemble(final CharSequence content) {
			final Map<String,String>	result = new HashMap<>();
			int							index = 0, maxIndex = content.length(), startName, endName, startValue, endValue, nesting;
			char						symbol = 0;
			
			while (index < maxIndex) {
				while (index < maxIndex && (symbol = content.charAt(index)) <= ' ') {
					index++;
				}
				startName = index;
				while (index < maxIndex && (Character.isJavaIdentifierPart(symbol) || symbol == '-')) {
					index++;
				}
				endName = index;
				while (index < maxIndex && (symbol = content.charAt(index)) <= ' ') {
					index++;
				}
				if (symbol == ':') {
					index++;
					while (index < maxIndex && (symbol = content.charAt(index)) <= ' ') {
						index++;
					}
					startValue = index;
					nesting = 0;
					while (index < maxIndex && !((symbol = content.charAt(index)) == ';' && nesting == 0)) {
						if (symbol == '\"') {
							nesting = 1 - nesting;
						}
						index++;
					}
					endValue = index;
					result.put(content.subSequence(startName,endName).toString(),content.subSequence(startValue,endValue).toString());
					if (symbol == ';') {
						index++;
					}
				}
			}
			return result;
		}

		public static String assemble(final Map<String,String> content) {
			final StringBuilder sb = new StringBuilder();
			
			for (Entry<String, String> item : content.entrySet()) {
				sb.append(item.getKey()).append(':').append(item.getValue()).append(';');
			}
			return sb.toString();
		}
	}
}
