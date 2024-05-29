package chav1961.purelib.basic;

import java.net.URI;
import java.util.Set;

import javax.swing.JEditorPane;

import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.interfaces.LoggerFacade;

public class SwingLoggerFacade extends AbstractLoggerFacade {
	public static final URI		LOGGER_URI = URI.create(LoggerFacade.LOGGER_SCHEME+":swing:/");
	
	private final JEditorPane	content;
	private final StringBuilder	sb = new StringBuilder();
	
	public SwingLoggerFacade() {
		super();
		this.content = null;
	}

	private SwingLoggerFacade(final JEditorPane content) {
		super();
		this.content = content;
	}
	
	private SwingLoggerFacade(final JEditorPane content, final String mark, final Class<?> root, final Set<Reducing> reducing) {
		super(mark, root, reducing);
		this.content = content;
	}

	@Override
	public boolean canServe(final URI resource) throws NullPointerException {
		if (resource == null) {
			throw new NullPointerException("Resource URI can't be null"); 
		}
		else {
			return URIUtils.canServeURI(resource, LOGGER_URI);
		}
	}

	@Override
	public LoggerFacade newInstance(final URI resource) throws EnvironmentException, NullPointerException, IllegalArgumentException {
		if (resource == null) {
			throw new NullPointerException("Resource URI can't be null"); 
		}
		else {
			final JEditorPane	parm = new JEditorPane("text/html","<html><body></body></html>");
			
			parm.setEditable(false);
			return new SwingLoggerFacade(parm);
		}
	}

	public JEditorPane getLoggerPane() {
		return content;
	}
	
	@Override
	protected AbstractLoggerFacade getAbstractLoggerFacade(final String mark, final Class<?> root) {
		return new SwingLoggerFacade(getLoggerPane(), mark, root, this.getReducing());
	}

	@Override
	protected void toLogger(final Severity level, final String text, final Throwable throwable) {
		switch (level) {
			case debug	:
				sb.append("<font color=gray><i>").append(escapeMessage(text)).append("</i></font>");
				break;
			case error	:
				sb.append("<font color=red>").append(escapeMessage(text)).append("</font>");
				break;
			case info	:
				sb.append("<font color=black>").append(escapeMessage(text)).append("</font>");
				break;
			case note	:
				sb.append("<font color=green>").append(escapeMessage(text)).append("</font>");
				break;
			case severe	:
				sb.append("<font color=red><b>").append(escapeMessage(text)).append("</b></font>");
				break;
			case tooltip:
				sb.append("<font color=gray>").append(escapeMessage(text)).append("</font>");
				break;
			case trace	:
				sb.append("<font color=gray><i>").append(escapeMessage(text)).append("<i></font>");
				break;
			case warning:
				sb.append("<font color=blue>").append(escapeMessage(text)).append("</font>");
				break;
			default:
				break;
		}
		if (throwable != null) {
			sb.append("<br>").append(throwable.getLocalizedMessage()+"<br>");
		}
		content.setText(content.getText().replace("</body></html>", sb.append("</body></html>").toString()));
		content.setCaretPosition(content.getDocument().getLength());
	}

	private String escapeMessage(final String text) {
		boolean			escapeRequired = false;
		StringBuilder	sb = null;
		
		for(int index = 0, maxIndex = text.length(); index < maxIndex; index++) {
			final char	symbol = text.charAt(index);
			
			if (symbol == '<' || symbol == '&' || symbol == '>') {
				sb = new StringBuilder(maxIndex + 100).append(text, 0, index);
				escapeRequired = true;
			}
			if (escapeRequired) {
				switch (symbol) {
					case '<' :
						sb.append("&lt;");
						break;
					case '&' :
						sb.append("&amp;");
						break;
					case '>' :
						sb.append("&gt;");
						break;
					default	:
						sb.append(symbol);
				}
			}
		}
		return escapeRequired ? sb.toString() : text;
	}
}
