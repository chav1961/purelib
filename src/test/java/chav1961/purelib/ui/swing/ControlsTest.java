package chav1961.purelib.ui.swing;



import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JFrame;

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
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.ui.AbstractInMemoryFormModel;
import chav1961.purelib.ui.interfacers.Action;
import chav1961.purelib.ui.interfacers.FormManager;
import chav1961.purelib.ui.interfacers.FormModel;
import chav1961.purelib.ui.interfacers.RefreshMode;
import chav1961.purelib.ui.swing.MicroTableEditor.EditorRepresentation;

public class ControlsTest {
	private static final Map<String,SubstitutableProperties>	map = new HashMap<>();
	private static final Map<String,String>						help = new HashMap<>();
	
	static {
		final SubstitutableProperties	props = new SubstitutableProperties();
		
		props.putAll(Utils.mkProps("help","uri(help)","help2","uri(help2)"
						,"name1","Localized NAME1","tooltip1","Localized TOOLTIP1"
						,"name2","Localized --- NAME2","tooltip2","Localized --- TOOLTIP2"
						,"action1","Action1","tooltipAction1","Action tooltip 1"
						,"action2","Action2","tooltipAction2","Action tooltip 2"));
		map.put("ru",props);
		map.put("en",props);
		
		try{help.put("help",Utils.fromResource(SimpleHelpComponentTest.class.getResource("helpcontent.cre")));
			help.put("help2",Utils.fromResource(SimpleHelpComponentTest.class.getResource("referencedhelpcontent.cre")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

//	@Test
//	public void microTableEditorTest() throws LocalizationException, InterruptedException, NullPointerException, ContentException, SyntaxException {
//		final FormManager<String,AutoBuildContent>		mgr = new FormManager<String,AutoBuildContent>(){
//															final LoggerFacade logger = new SystemErrLoggerFacade();
//												
//															@Override
//															public RefreshMode onRecord(final Action action, final AutoBuildContent oldRecord, final String oldId, final AutoBuildContent newRecord, final String newId) throws FlowException {
//																return RefreshMode.TOTAL;
//															}
//												
//															@Override
//															public RefreshMode onField(final AutoBuildContent inst, final String id, final String fieldName, final Object oldValue) throws FlowException {
//																System.err.println("Name="+fieldName);
//																return fieldName.equals("field1") ? RefreshMode.FIELD_ONLY : RefreshMode.REJECT;
//															}
//												
//															@Override
//															public RefreshMode onAction(final AutoBuildContent inst, final String id, final String actionName, final Object parameter) throws FlowException {
//																System.err.println("Action="+actionName);
//																inst.field2 = 200;
//																return RefreshMode.RECORD_ONLY;
//															}
//												
//															@Override
//															public LoggerFacade getLogger() {
//																return logger;
//															}
//														};
//		final FormModel<Integer,String[]>				model = new AbstractInMemoryFormModel<Integer,String[]>(Integer.class,String[].class,AbstractInMemoryFormModel.ARRAY_INDEX) {
//															int	id = 0;
//															
//															@Override
//															public Integer createUniqueId() throws ContentException {
//																return id++;
//															}
//
//															@Override
//															public Class<String[]> getInstanceType() {
//																return String[].class;
//															}
//														};
//		
//		try(final Localizer	root = new PureLibLocalizer();
//			final Localizer	localizer = new DebuggingLocalizer(map,help)) {
//			
//			root.push(localizer);
//			
//			for (EditorRepresentation item : new EditorRepresentation[]{EditorRepresentation.MARKED_LIST}/*EditorRepresentation.values()*/) {
//				final MicroTableEditor	mte = new MicroTableEditor(localizer,item);
//				
//				final JFrame	frame = createFrame(mte.build(mgr,model,new String[] {"item"}
//										,nullString[].class,new String[]{"item1","item2"}
//										,new String[]{"item1","item2","item3","item4","item5"}));
//
//				frame.setVisible(true);
//				Thread.sleep(20000);
//				
//				Assert.assertArrayEquals(null,new String[]{"item1","item2"});
//				System.err.println("Next microTableEditorTest");
//				frame.setVisible(false);
//			}
//		}
//	}
	
//	@Test
	public void autoBuiltFormTest() throws LocalizationException, InterruptedException, SyntaxException, NullPointerException, IllegalArgumentException, ContentException {
		final FormManager<Object,AutoBuildContent>		mgr = new FormManager<Object,AutoBuildContent>(){
															final LoggerFacade logger = new SystemErrLoggerFacade();
			
															@Override
															public RefreshMode onRecord(final Action action, final AutoBuildContent oldRecord, final Object oldId, final AutoBuildContent newRecord, final Object newId) throws FlowException {
																return RefreshMode.TOTAL;
															}
												
															@Override
															public RefreshMode onField(final AutoBuildContent inst, final Object id, final String fieldName, final Object oldValue) throws FlowException {
																System.err.println("Name="+fieldName);
																return fieldName.equals("field1") ? RefreshMode.FIELD_ONLY : RefreshMode.REJECT;
															}
												
															@Override
															public RefreshMode onAction(final AutoBuildContent inst, final Object id, final String actionName, final Object parameter) throws FlowException {
																System.err.println("Action="+actionName);
																inst.field2 = 200;
																return RefreshMode.RECORD_ONLY;
															}

															@Override
															public LoggerFacade getLogger() {
																return logger;
															}
														};
		final AutoBuildContent							abc = new AutoBuildContent(); 
		
		try(final Localizer	root = new PureLibLocalizer();
			final Localizer	localizer = new DebuggingLocalizer(map,help)) {
			
			root.push(localizer);

			final AutoBuiltForm<AutoBuildContent>	form = new AutoBuiltForm<AutoBuildContent>(localizer,abc,mgr);
			final JFrame							frame = createFrame(form);
			
			frame.setVisible(true);
			Thread.sleep(10000);
			System.err.println("Next AutoBuiltFormTest");
			frame.setVisible(false);
			
			LocalizedDialog.askParameters(localizer,(AutoBuiltForm<AutoBuildContent>)form);
		}
	}
	
//	@Test
	public void styledProgressBarTest() throws LocalizationException, InterruptedException, SyntaxException, NullPointerException, IllegalArgumentException, ContentException {
		final StyledProgressBar	bar = new StyledProgressBar();
		final JFrame			frame = createFrame(bar);
		
		bar.setMinimum(0);
		bar.setMaximum(100);
		bar.setStringPainted(true);
		frame.setVisible(true);
		for (int index = 0; index <= 100; index++) {
			bar.setValue(index);
			Thread.sleep(50);
		}
		frame.setVisible(false);
	}	
	
	@Test
	public void empty() {
	}
	
	public static JFrame createFrame(final JComponent inner) {
		final JFrame	frame = new JFrame();
		
		frame.setSize(new Dimension(640,480));
		frame.setMinimumSize(new Dimension(640,480));
		frame.setPreferredSize(new Dimension(640,480));
		frame.getContentPane().add(inner,BorderLayout.CENTER);
		frame.pack();
		return frame;
	}
}

@Action(resource=@LocaleResource(value="action1",tooltip="tooltipAction1"),actionString="str1")
@Action(resource=@LocaleResource(value="action2",tooltip="tooltipAction2"),actionString="str2")
class AutoBuildContent {
	@LocaleResource(value="name1",tooltip="tooltip1")	
	String	field1 = "empty";	
	@LocaleResource(value="name2",tooltip="tooltip2")	
	long	field2 = 100;
}