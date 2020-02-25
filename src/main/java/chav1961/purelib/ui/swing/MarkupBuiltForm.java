package chav1961.purelib.ui.swing;

import java.awt.Rectangle;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.LineByLineProcessor;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.swing.interfaces.JComponentMonitor;

public class MarkupBuiltForm<T> extends JPanel implements LocaleChangeListener, AutoCloseable, JComponentMonitor {
	private static final long 				serialVersionUID = -1828992791881237479L;
	
	private final Localizer					localizer;
	private final LoggerFacade				logger;
	private final ContentMetadataInterface	metadata;
	private final T							instance;
	private final FormManager<Object,T>		formMgr;
	private final boolean					tooltipsOnFocus;
	private final PresentationDescriptor	desc;
	
	public MarkupBuiltForm(final Localizer localizer, final LoggerFacade logger, final ContentMetadataInterface metadata, final String markupDescriptor, final T instance, final FormManager<Object,T> formMgr, final boolean tooltipsOnFocus) throws NullPointerException, IllegalArgumentException, SyntaxException, LocalizationException, ContentException {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (logger == null) {
			throw new NullPointerException("Logger can't be null");
		}
		else if (metadata == null) {
			throw new NullPointerException("Metadata can't be null");
		}
		else if (markupDescriptor == null || markupDescriptor.isEmpty()) {
			throw new IllegalArgumentException("Markup descriptor can't be null");
		}
		else if (instance == null) {
			throw new NullPointerException("Instance to edit can't be null");
		}
		else if (formMgr == null) {
			throw new NullPointerException("Form manager to edit can't be null");
		}
		else {
			this.localizer = localizer;
			this.logger = logger;
			this.metadata = metadata;
			this.instance = instance;
			this.formMgr = formMgr;
			this.tooltipsOnFocus = tooltipsOnFocus;
			this.desc = buildPresentation(metadata, markupDescriptor, instance.getClass());
			fillLocalizedStrings();
		}
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() throws RuntimeException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean process(final MonitorEvent event, final ContentNodeMetadata metadata, final JComponent component, final Object... parameters) throws ContentException {
		// TODO Auto-generated method stub
		return false;
	}

	private void fillLocalizedStrings() {
		// TODO Auto-generated method stub
		
	}

	private static <T> PresentationDescriptor buildPresentation(final ContentMetadataInterface metadata, final String markupDescriptor, final  Class<T> clazz) {
		// TODO Auto-generated method stub
		final List<JComponent>	components = new ArrayList<>();
		final List<Rectangle>	constraints = new ArrayList<>();
		final int[]				lastLineNo = new int[0];
		
		try(final LineByLineProcessor	lblp = new LineByLineProcessor((displacement, lineNo, data, from, length)-> {
				if (data[from] == '>' && data[from+1] == '>') {
					processPage(displacement,lineNo,data,from,length,components,constraints);
					lastLineNo[0] = lineNo;
				}
				else {
					processLine(displacement,lineNo-lastLineNo[0],data,from,length,components,constraints);
				}
			})) {
			
			lblp.write(markupDescriptor.toCharArray(),0,markupDescriptor.length());
		} catch (IOException | SyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	private static void processPage(final long displacement, final int lineNo, final char[] data, int from, final int length, final List<JComponent> components, final List<Rectangle> constraints) throws IOException, SyntaxException {
		final int	begin = from, to = from + length; 
	}

	
	private static void processLine(final long displacement, final int lineNo, final char[] data, int from, final int length, final List<JComponent> components, final List<Rectangle> constraints) throws IOException, SyntaxException {
		final int	begin = from, to = from + length; 
		int 		start, result[] = new int[2], size[] = new int[1], location = 0;
		
		for (;;) {
			start = from;
			
			while (data[from] != '$' && data[from] != '\n') {	// Parse constant
				from++;
			}
			components.add(new JLabel(new String(data,start,from-start)));
			constraints.add(new Rectangle(lineNo,location,from-start,1));
			location += from - start;
			
			if (data[from] == '$' && data[from+1] == '{') {		// Parse field description
				from = CharUtils.parseName(data,from+2, result);
				if (data[from] == ':') {
					from = CharUtils.parseInt(data,from+1,size,true);
					if (data[from] == '}') {
						final String	name = new String(data,result[0],result[1]-result[0]);
						
						components.add(new JLabel(new String(data,start,from-start)));
						constraints.add(new Rectangle(lineNo,location,size[0],1));
						location += size[0];
						from++;
					}
					else {
						throw new SyntaxException(lineNo,from-begin,"Missing '}'"); 
					}
				}
			}
			else {
				break;
			}
		}
	}


	
	
	private static class PageDescriptor extends JPanel {
		private static final long serialVersionUID = 5773037141534896393L;
		
		PageDescriptor() {
			
		}		
	}

	private static class PresentationDescriptor {
		private final int				width, height;
		private final PageDescriptor[]	pages;
		
		PresentationDescriptor(final int width, final int height, final PageDescriptor... pages) {
			this.width = width;
			this.height = height;
			this.pages = pages;
		}
		
		public int getWidth() {
			return width;
		}

		public int getHeight() {
			return height;
		}
		
		public int getPageCount() {
			return pages.length;
		}
		
		public PageDescriptor getPage(final int index) {
			return pages[index];
		}
	}
}
