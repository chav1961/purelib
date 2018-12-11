package chav1961.purelib.ui.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.ToolTipManager;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.SystemErrLoggerFacade;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.i18n.DebuggingLocalizer;
import chav1961.purelib.i18n.PureLibLocalizer;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.ui.AbstractInMemoryFormModel;
import chav1961.purelib.ui.SingleClass;
import chav1961.purelib.ui.interfacers.FormManager;
import chav1961.purelib.ui.interfacers.FormRepresentation;
import chav1961.purelib.ui.interfacers.RefreshMode;

public class SimpleLowLevelFormFactoryTest {
	private static final Map<String,SubstitutableProperties>	map = new HashMap<>();
	private static final Map<String,String>						help = new HashMap<>();
	
	static {
		final SubstitutableProperties	props = new SubstitutableProperties();
		
		props.putAll(Utils.mkProps("help1","uri(help)","help2","uri(help2)","field1","FIELD1 NAME","field2Name","FIELD2 NAME","field2Tooltip","FIELD2 TOOLTIP"));
		map.put("ru",props);
		map.put("en",props);
		
		try{help.put("help",Utils.fromResource(SimpleHelpComponentTest.class.getResource("helpcontent.cre")));
			help.put("help2",Utils.fromResource(SimpleHelpComponentTest.class.getResource("referencedhelpcontent.cre")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	@Test
	public void listPresentationTest() throws IOException, SyntaxException, URISyntaxException, ContentException, LocalizationException, NullPointerException, InterruptedException {
		final FormManager<Integer,SingleClass>			mgr = new FormManager<Integer,SingleClass>(){
															final LoggerFacade logger = new SystemErrLoggerFacade();
			
															@Override
															public RefreshMode onRecord(Action action, SingleClass oldRecord, Integer oldId, SingleClass newRecord, Integer newId) throws FlowException {
																return null;
															}
												
															@Override
															public RefreshMode onField(SingleClass inst, Integer id, String fieldName, Object oldValue) throws FlowException {
																return null;
															}
												
															@Override
															public RefreshMode onAction(SingleClass inst, Integer id, String actionName, Object parameter) throws FlowException {
																return null;
															}

															@Override
															public LoggerFacade getLogger() {
																return logger;
															}
														};
		final SingleClass[]								modelContent = new SingleClass[] {new SingleClass()};  
		final AbstractInMemoryFormModel<Integer,SingleClass>		model = new AbstractInMemoryFormModel<Integer,SingleClass>(Integer.class,SingleClass.class,"id",modelContent){
																			int	id = 0;
																			@Override
																			public Integer createUniqueId() throws ContentException {
																				return ++id;
																			}
																			@Override
																			public Class<SingleClass> getInstanceType() {
																				return SingleClass.class;
																			}
																	};
		final Localizer									root = new PureLibLocalizer();

		ToolTipManager.sharedInstance().setLightWeightPopupEnabled(true);
		
		try(final Localizer	localizer = new DebuggingLocalizer(map,help)) {
			
			root.push(localizer);
			final SimpleLowLevelFormFactory<Integer,SingleClass>	f = new SimpleLowLevelFormFactory<Integer,SingleClass>(SingleClass.class.getResource("singleform.txt").toURI(),FormRepresentation.LIST,SingleClass.class,mgr,localizer);
		
			final JFrame	frame = new JFrame();
			
			frame.setSize(new Dimension(640,480));
			frame.setMinimumSize(new Dimension(640,480));
			frame.setPreferredSize(new Dimension(640,480));
			frame.getContentPane().add(f.prepareComponent(model,false),BorderLayout.CENTER);
			frame.pack();
			frame.setVisible(true);
			Thread.sleep(100000);
			System.err.println("SD");
			frame.setVisible(false);
		} finally {
			root.pop();
		}
	}
	
	
//	@Test
	public void complexTest() throws IOException, SyntaxException, URISyntaxException, ContentException, LocalizationException, NullPointerException, InterruptedException {
		final FormManager<Integer,SingleClass>			mgr = new FormManager<Integer,SingleClass>(){
															final LoggerFacade logger = new SystemErrLoggerFacade();
			
															@Override
															public RefreshMode onRecord(Action action, SingleClass oldRecord, Integer oldId, SingleClass newRecord, Integer newId) throws FlowException {
																return null;
															}
												
															@Override
															public RefreshMode onField(SingleClass inst, Integer id, String fieldName, Object oldValue) throws FlowException {
																return null;
															}
												
															@Override
															public RefreshMode onAction(SingleClass inst, Integer id, String actionName, Object parameter) throws FlowException {
																return null;
															}

															@Override
															public LoggerFacade getLogger() {
																return logger;
															}
														};
		final SingleClass[]								modelContent = new SingleClass[] {new SingleClass()};  
		final AbstractInMemoryFormModel<Integer,SingleClass>		model = new AbstractInMemoryFormModel<Integer,SingleClass>(Integer.class,SingleClass.class,"id",modelContent){
																			int	id = 0;
																			@Override
																			public Integer createUniqueId() throws ContentException {
																				return ++id;
																			}
																			@Override
																			public Class<SingleClass> getInstanceType() {
																				return SingleClass.class;
																			}
																	};
		final Localizer									root = new PureLibLocalizer();
									
		try(final Localizer	localizer = new DebuggingLocalizer(map,help)) {
			
			root.push(localizer);
			for (FormRepresentation item : FormRepresentation.values()) {
				final SimpleLowLevelFormFactory<Integer,SingleClass>	f = new SimpleLowLevelFormFactory<Integer,SingleClass>(SingleClass.class.getResource("singleform.txt").toURI(),item,SingleClass.class,mgr,localizer);
			
				final JFrame	frame = new JFrame();
				
				frame.setSize(new Dimension(640,480));
				frame.setMinimumSize(new Dimension(640,480));
				frame.setPreferredSize(new Dimension(640,480));
				frame.getContentPane().add(f.prepareComponent(model,false),BorderLayout.CENTER);
				frame.pack();
				frame.setVisible(true);
				Thread.sleep(10000);
				System.err.println("SD");
				frame.setVisible(false);
			}
		} finally {
			root.pop();
		}
	}
}
