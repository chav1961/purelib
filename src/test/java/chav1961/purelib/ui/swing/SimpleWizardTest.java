package chav1961.purelib.ui.swing;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.awt.Dialog.ModalityType;
import java.net.URI;
import java.awt.Window;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.ui.AbstractWizardStep;
import chav1961.purelib.ui.interfacers.WizardStep;
import chav1961.purelib.ui.swing.SimpleWizard.ActionButton;

public class SimpleWizardTest {
	public static final String		KEY_PREPARE = "prepare";
	public static final String		KEY_BEFORE_SHOW = "beforeShow";
	public static final String		KEY_VALIDATE = "validate";
	public static final String		KEY_AFTER_SHOW = "afterShow";
	public static final String		KEY_UNPREPARE = "unprepare";
	public static final String		KEY_LIFECYCLE = "lifecycle";

	public static final String		CTRL_STEP2_VALIDATION = "step2Validation";
	public static final String		CTRL_STEP4_VALIDATION = "step4Validation";
	public static final String		CTRL_STEP1_PREV = "step1Prev";
	public static final String		CTRL_STEP1_NEXT = "step1Next";
	public static final String		CTRL_STEP2_PREV = "step2Prev";
	public static final String		CTRL_STEP2_NEXT = "step2Next";
	public static final String		CTRL_STEP3_PREV = "step3Prev";
	public static final String		CTRL_STEP3_NEXT = "step3Next";
	
//	@Test
	public void lifeCycleTest() throws LocalizationException, InterruptedException, TimeoutException {
		final Properties									props = new Properties();
		final HashMap<String,Object>						ctrl = new HashMap<String,Object>();
		final WizardStep<Properties,TestError,JComponent>	ws1 = new WizardStep1(ctrl), ws2 = new WizardStep2(ctrl), ws3 = new WizardStep3(ctrl); 

		// Successful life cycle 
		try(final PseudoWizard wiz = new PseudoWizard(null,"caption",ModalityType.DOCUMENT_MODAL,new HashMap<String,Object>(),ws1,ws2,ws3)) {
			final BlockingQueue<ActionButton>	ex = new ArrayBlockingQueue<>(1000);
			final boolean[]					results = new boolean[]{false,false};
			final Thread					t = new Thread(new Runnable(){
													@Override
													public void run() {
														try{results[0] = wiz.animate(props,ex);
														} catch (LocalizationException | PreparationException | FlowException | InterruptedException e) {
															e.printStackTrace();
															results[1] = true;
														}
													}
												}
											);
			
			t.setDaemon(true);				t.start();
			
			wiz.accessProperties().put(CTRL_STEP2_VALIDATION,false);	// Simulate Step2 validation false			
			ex.put(ActionButton.NEXT);
			ex.put(ActionButton.PREV);
			ex.put(ActionButton.NEXT);
			ex.put(ActionButton.NEXT);
			Thread.sleep(1000);	// Prevent too quick key refreshing after previous exchange because of multithreading...	
			wiz.accessProperties().put(CTRL_STEP2_VALIDATION,true);		// Simulate Step2 validation true
			ex.put(ActionButton.NEXT);
			ex.put(ActionButton.FINISH);
			t.join(5000);
			Assert.assertTrue(results[0]);
			Assert.assertFalse(results[1]);
			Assert.assertEquals(props.getProperty(KEY_PREPARE,""),"123");
			Assert.assertEquals(props.getProperty(KEY_BEFORE_SHOW,""),"12123");
			Assert.assertEquals(props.getProperty(KEY_VALIDATE,""),"11223");
			Assert.assertEquals(props.getProperty(KEY_AFTER_SHOW,""),"12123");
			Assert.assertEquals(props.getProperty(KEY_UNPREPARE,""),"123");
			Assert.assertEquals(props.getProperty(KEY_LIFECYCLE,""),"P1P2P3B1V1A1B2A2B1V1A1B2V2V2A2B3V3A3U1U2U3");
		}
		
		props.clear();
		
		// Failed life cycle 
		try(final PseudoWizard wiz = new PseudoWizard(null,"caption",ModalityType.DOCUMENT_MODAL,new HashMap<String,Object>(),ws1,ws2,ws3)) {
			final BlockingQueue<ActionButton>	ex = new ArrayBlockingQueue<>(1000);
			final boolean[]					results = new boolean[]{false,false};
			final Thread					t = new Thread(new Runnable(){
													@Override
													public void run() {
														try{results[0] = wiz.animate(props,ex);
														} catch (LocalizationException | PreparationException | FlowException | InterruptedException e) {
															e.printStackTrace();
															results[1] = true;
														}
													}
												}
											);
			
			t.setDaemon(true);				t.start();

			ctrl.put(CTRL_STEP1_NEXT,ws2.getClass().getSimpleName());
			ctrl.put(CTRL_STEP2_NEXT,ws3.getClass().getSimpleName());
			ctrl.put(CTRL_STEP2_PREV,ws1.getClass().getSimpleName());
			ctrl.put(CTRL_STEP3_PREV,ws2.getClass().getSimpleName());
			
			wiz.accessProperties().put(CTRL_STEP2_VALIDATION,false);	// Simulate Step2 validation false			
			ex.put(ActionButton.NEXT);
			ex.put(ActionButton.PREV);
			ex.put(ActionButton.NEXT);
			ex.put(ActionButton.NEXT);
			Thread.sleep(1000);	// Prevent too quick key refreshing after previous exchange because of multithreading...	
			wiz.accessProperties().put(CTRL_STEP2_VALIDATION,true);		// Simulate Step2 validation true
			ex.put(ActionButton.NEXT);
			ex.put(ActionButton.CANCEL);
			t.join(5000);
			Assert.assertFalse(results[0]);
			Assert.assertFalse(results[1]);
			Assert.assertEquals(props.getProperty(KEY_PREPARE,""),"123");
			Assert.assertEquals(props.getProperty(KEY_BEFORE_SHOW,""),"12123");
			Assert.assertEquals(props.getProperty(KEY_VALIDATE,""),"1122");
			Assert.assertEquals(props.getProperty(KEY_AFTER_SHOW,""),"12123");
			Assert.assertEquals(props.getProperty(KEY_UNPREPARE,""),"123");
			Assert.assertEquals(props.getProperty(KEY_LIFECYCLE,""),"P1P2P3B1V1A1B2A2B1V1A1B2V2V2A2B3A3U1U2U3");
		}
	}
	
//	@Test
	public void localizationTest() throws LocalizationException, InterruptedException, TimeoutException, IOException {
		final Properties									props = new Properties();
		final HashMap<String,Object>						ctrl = new HashMap<String,Object>();
		final Localizer										l = LocalizerFactory.getLocalizer(URI.create(Localizer.LOCALIZER_SCHEME+":prop:chav1961/purelib/i18n/test"));
		final WizardStep<Properties,TestError,JComponent>	ws1 = new WizardStep1(ctrl), ws3 = new WizardStep3(ctrl); 

		ctrl.put(SimpleWizard.PROP_LOCALIZER,l);
		l.setCurrentLocale(new Locale("en"));

		try(final PseudoWizard wiz = new PseudoWizard(null,"key1",ModalityType.DOCUMENT_MODAL,ctrl,ws1,ws3)) {
			Assert.assertEquals(wiz.getTitle(),"value1");
			l.setCurrentLocale(new Locale("ru"));
			Assert.assertEquals(wiz.getTitle(),"значение1");
			l.setCurrentLocale(new Locale("en"));
			
			final BlockingQueue<ActionButton>	ex = new ArrayBlockingQueue<>(1000);
			final boolean[]					results = new boolean[]{false,false};
			final Thread					t = new Thread(new Runnable(){
													@Override
													public void run() {
														try{results[0] = wiz.animate(props,ex);
														} catch (LocalizationException | PreparationException | FlowException | InterruptedException e) {
															e.printStackTrace();
															results[1] = true;
														}
													}
												}
											);
			
			t.setDaemon(true);				t.start();
			
			ex.put(ActionButton.NEXT);
			Thread.sleep(500);
			Assert.assertEquals(((WizardStep3)ws3).f.getText(),"value1");
			Assert.assertEquals(((WizardStep3)ws3).f.getToolTipText(),"value2");
			l.setCurrentLocale(new Locale("ru"));
			Assert.assertEquals(((WizardStep3)ws3).f.getText(),"значение1");
			Assert.assertEquals(((WizardStep3)ws3).f.getToolTipText(),"значение2");
			ex.put(ActionButton.CANCEL);
			t.join(5000);
			Assert.assertFalse(results[0]);
			Assert.assertFalse(results[1]);
		}

		props.clear();
		ctrl.put(SimpleWizard.PROP_SHOW_LOCALIZATION_BOX,true);
		l.setCurrentLocale(new Locale("en"));
		
		try(final PseudoWizard wiz = new PseudoWizard(null,"key1",ModalityType.DOCUMENT_MODAL,ctrl,ws1,ws3)) {
			Assert.assertEquals(wiz.getTitle(),"value1");
			l.setCurrentLocale(new Locale("ru"));
			Assert.assertEquals(wiz.getTitle(),"значение1");
			l.setCurrentLocale(new Locale("en"));
			
			final BlockingQueue<ActionButton>	ex = new ArrayBlockingQueue<>(1000);
			final boolean[]					results = new boolean[]{false,false};
			final Thread					t = new Thread(new Runnable(){
													@Override
													public void run() {
														try{results[0] = wiz.animate(props,ex);
														} catch (LocalizationException | PreparationException | FlowException | InterruptedException e) {
															e.printStackTrace();
															results[1] = true;
														}
													}
												}
											);
			
			t.setDaemon(true);				t.start();
			
			ex.put(ActionButton.NEXT);
			Thread.sleep(500);
			Assert.assertEquals(((WizardStep3)ws3).f.getText(),"value1");
			Assert.assertEquals(((WizardStep3)ws3).f.getToolTipText(),"value2");
			l.setCurrentLocale(new Locale("ru"));
			Assert.assertEquals(((WizardStep3)ws3).f.getText(),"значение1");
			Assert.assertEquals(((WizardStep3)ws3).f.getToolTipText(),"значение2");
			ex.put(ActionButton.CANCEL);
			t.join(5000);
			Assert.assertFalse(results[0]);
			Assert.assertFalse(results[1]);
		}
	}

//	@Test
	public void processingTest() throws LocalizationException, InterruptedException, TimeoutException, IOException {
		final Properties									props = new Properties();
		final HashMap<String,Object>						ctrl = new HashMap<String,Object>();
		final WizardStep<Properties,TestError,JComponent>	ws1 = new WizardStep1(ctrl), ws4 = new WizardStep4(ctrl), ws3 = new WizardStep3(ctrl);

		try(final PseudoWizard wiz = new PseudoWizard(null,"caption",ModalityType.DOCUMENT_MODAL,ctrl,ws1,ws4,ws3)) {
			final BlockingQueue<ActionButton>	ex = new ArrayBlockingQueue<>(1000);
			final boolean[]					results = new boolean[]{false,false};
			final Thread					t = new Thread(new Runnable(){
													@Override
													public void run() {
														try{results[0] = wiz.animate(props,ex);
														} catch (LocalizationException | PreparationException | FlowException | InterruptedException e) {
															e.printStackTrace();
															results[1] = true;
														}
													}
												}
											);
			
			t.setDaemon(true);				t.start();

			ex.put(ActionButton.NEXT);
			Thread.sleep(1000);
			ex.put(ActionButton.FINISH);
			t.join(5000);
			Assert.assertTrue(results[0]);
			Assert.assertFalse(results[1]);
		}
		
		try(final PseudoWizard wiz = new PseudoWizard(null,"caption",ModalityType.DOCUMENT_MODAL,ctrl,ws1,ws4,ws3)) {
			final BlockingQueue<ActionButton>	ex = new ArrayBlockingQueue<>(1000);
			final boolean[]					results = new boolean[]{false,false};
			final Thread					t = new Thread(new Runnable(){
													@Override
													public void run() {
														try{results[0] = wiz.animate(props,ex);
														} catch (LocalizationException | PreparationException | FlowException | InterruptedException e) {
															e.printStackTrace();
															results[1] = true;
														}
													}
												}
											);
			
			t.setDaemon(true);				t.start();

			wiz.accessProperties().put(CTRL_STEP4_VALIDATION,false);	// Simulate Step4 validation false			
			ex.put(ActionButton.NEXT);
			t.join(5000);
			Assert.assertFalse(results[0]);
			Assert.assertFalse(results[1]);
		}
	}
	
//	@Test
	public void exceptionsTest() throws LocalizationException, InterruptedException, TimeoutException, IOException {
		final Properties									props = new Properties();
		final HashMap<String,Object>						ctrl = new HashMap<String,Object>();
		final Localizer										l = LocalizerFactory.getLocalizer(URI.create(Localizer.LOCALIZER_SCHEME+":prop:chav1961/purelib/i18n/test"));
		final WizardStep<Properties,TestError,JComponent>	ws1 = new WizardStep1(ctrl), ws4 = new WizardStep4(ctrl), ws3 = new WizardStep3(ctrl);
		
		try{new PseudoWizard(null,null,ModalityType.DOCUMENT_MODAL,ctrl,ws1,ws4,ws3);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new PseudoWizard(null,"caption",null,ctrl,ws1,ws4,ws3);
			Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
		} catch (NullPointerException exc) {
		}
		try{new PseudoWizard(null,"caption",ModalityType.DOCUMENT_MODAL,null,ws1,ws4,ws3);
			Assert.fail("Mandatory exception was not detected (null 4-th argument)");
		} catch (NullPointerException exc) {
		}
		try{new PseudoWizard(null,"caption",ModalityType.DOCUMENT_MODAL,ctrl);
			Assert.fail("Mandatory exception was not detected (null or empty 5-th argument)");
		} catch (IllegalArgumentException exc) {
		}
		try{new PseudoWizard(null,"caption",ModalityType.DOCUMENT_MODAL,ctrl,ws4);
			Assert.fail("Mandatory exception was not detected (no initial step)");
		} catch (IllegalArgumentException exc) {
		}
		try{new PseudoWizard(null,"caption",ModalityType.DOCUMENT_MODAL,ctrl,null,ws4);
			Assert.fail("Mandatory exception was not detected (null steps inside the list)");
		} catch (NullPointerException exc) {
		}
		try{new PseudoWizard(null,"caption",ModalityType.DOCUMENT_MODAL,ctrl,ws1,ws4);
			Assert.fail("Mandatory exception was not detected (no terminal step)");
		} catch (IllegalArgumentException exc) {
		}
		
		ctrl.put(SimpleWizard.PROP_LOCALIZER, new Object());
		try{new PseudoWizard(null,"caption",ModalityType.DOCUMENT_MODAL,ctrl,ws1,ws3);
			Assert.fail("Mandatory exception was not detected (illegal localizer key)");
		} catch (IllegalArgumentException exc) {
		}
		
		ctrl.put(SimpleWizard.PROP_LOCALIZER,l);
		ctrl.put(SimpleWizard.PROP_SHOW_LOCALIZATION_BOX,new Object());
		try{new PseudoWizard(null,"caption",ModalityType.DOCUMENT_MODAL,ctrl,ws1,ws3);
			Assert.fail("Mandatory exception was not detected (illegal localization box key)");
		} catch (IllegalArgumentException exc) {
		}
		
		ctrl.put(SimpleWizard.PROP_SHOW_LOCALIZATION_BOX,true);
		ctrl.remove(SimpleWizard.PROP_LOCALIZER);
		try{new PseudoWizard(null,"caption",ModalityType.DOCUMENT_MODAL,ctrl,ws1,ws3);
			Assert.fail("Mandatory exception was not detected (localization box key without localizer key)");
		} catch (IllegalArgumentException exc) {
		}
	}	

//	@Test
	public void complexTest() throws NullPointerException, IOException, LocalizationException, PreparationException, FlowException, InterruptedException {
		final Properties									props = new Properties();
		final HashMap<String,Object>						ctrl = new HashMap<String,Object>();
		final Localizer										l = LocalizerFactory.getLocalizer(URI.create(Localizer.LOCALIZER_SCHEME+":prop:chav1961/purelib/i18n/test"));
		final WizardStep<Properties,TestError,JComponent>	ws1 = new WizardStep1(ctrl), ws2 = new WizardStep2(ctrl), ws3 = new WizardStep3(ctrl);

		ctrl.put(SimpleWizard.PROP_LOCALIZER,l);
		ctrl.put(SimpleWizard.PROP_SHOW_LOCALIZATION_BOX,true);
		l.setCurrentLocale(new Locale("en"));

		try(final SimpleWizard<Properties,TestError> wiz = new SimpleWizard(null,"key1",ModalityType.DOCUMENT_MODAL,ctrl,ws1,ws2,ws3)) {
			System.err.println("------------------------------------------");
			wiz.animate(props);
		}
	}
	
	@Test
	public void empty() {
	}
	
}

enum TestError {
	ERROR1, ERROR2
}

class PseudoWizard extends SimpleWizard<Properties,TestError> {
	private static final long serialVersionUID = 1L;
	
	public PseudoWizard(Window parent, String caption, ModalityType modality, Map<String, Object> properties, WizardStep<Properties, TestError, JComponent>... steps) throws LocalizationException {
		super(parent, caption, modality, properties, steps);
	}
	
	public boolean animate(final Properties cargo, final BlockingQueue<ActionButton> stepper) throws PreparationException, FlowException, InterruptedException, LocalizationException {
		return animate(0,cargo,stepper);
	}
	
	@Override
	public void setVisible(boolean visibility) {
	}
	
	public Map<String,Object> accessProperties() {
		return properties;
	}
}


class WizardStep1 extends AbstractWizardStep<Properties,TestError,JComponent> {
	private final HashMap<String, Object>	ctrl;
	
	public WizardStep1(HashMap<String, Object> ctrl) {
		this.ctrl = ctrl;
	}

	@Override
	public StepType getStepType() {
		return StepType.INITIAL;
	}

	@Override
	public String getCaption() {
		return "step1";
	}

	@Override
	public JComponent getContent() {
		return new JLabel(getCaption());
	}

	@Override
	public String getNextStep() {
		return ctrl.containsKey(SimpleWizardTest.CTRL_STEP1_NEXT) ? ctrl.get(SimpleWizardTest.CTRL_STEP1_NEXT).toString() : null;
	}

	@Override
	public String getPrevStep() {
		return ctrl.containsKey(SimpleWizardTest.CTRL_STEP1_PREV) ? ctrl.get(SimpleWizardTest.CTRL_STEP1_PREV).toString() : null;
	}
	
	@Override
	public void prepare(final Properties content, final Map<String, Object> temporary) throws PreparationException {
		content.setProperty(SimpleWizardTest.KEY_PREPARE,content.getProperty(SimpleWizardTest.KEY_PREPARE,"")+"1");
		content.setProperty(SimpleWizardTest.KEY_LIFECYCLE,content.getProperty(SimpleWizardTest.KEY_LIFECYCLE,"")+"P1");
	}

	@Override
	public void beforeShow(final Properties content, Map<String, Object> temporary, ErrorProcessing<Properties, TestError> err) throws FlowException {
		content.setProperty(SimpleWizardTest.KEY_BEFORE_SHOW,content.getProperty(SimpleWizardTest.KEY_BEFORE_SHOW,"")+"1");
		content.setProperty(SimpleWizardTest.KEY_LIFECYCLE,content.getProperty(SimpleWizardTest.KEY_LIFECYCLE,"")+"B1");
	}

	@Override
	public boolean validate(final Properties content, final Map<String, Object> temporary, final ErrorProcessing<Properties, TestError> err) throws FlowException {
		content.setProperty(SimpleWizardTest.KEY_VALIDATE,content.getProperty(SimpleWizardTest.KEY_VALIDATE,"")+"1");
		content.setProperty(SimpleWizardTest.KEY_LIFECYCLE,content.getProperty(SimpleWizardTest.KEY_LIFECYCLE,"")+"V1");
		return true;
	}
	
	@Override
	public void afterShow(Properties content, Map<String, Object> temporary, ErrorProcessing<Properties, TestError> err) throws FlowException {
		content.setProperty(SimpleWizardTest.KEY_AFTER_SHOW,content.getProperty(SimpleWizardTest.KEY_AFTER_SHOW,"")+"1");
		content.setProperty(SimpleWizardTest.KEY_LIFECYCLE,content.getProperty(SimpleWizardTest.KEY_LIFECYCLE,"")+"A1");
	}

	@Override
	public void unprepare(final Properties content, final Map<String, Object> temporary) {
		content.setProperty(SimpleWizardTest.KEY_UNPREPARE,content.getProperty(SimpleWizardTest.KEY_UNPREPARE,"")+"1");
		content.setProperty(SimpleWizardTest.KEY_LIFECYCLE,content.getProperty(SimpleWizardTest.KEY_LIFECYCLE,"")+"U1");
	}
}

class WizardStep2 extends AbstractWizardStep<Properties,TestError,JComponent> {
	private final HashMap<String, Object>	ctrl;

	public WizardStep2(HashMap<String, Object> ctrl) {
		this.ctrl = ctrl;
	}

	@Override
	public StepType getStepType() {
		return StepType.ORDINAL;
	}

	@Override
	public String getCaption() {
		return "step2";
	}

	@Override
	public JComponent getContent() {
		return new JLabel(getCaption());
	}

	@Override
	public String getNextStep() {
		return ctrl.containsKey(SimpleWizardTest.CTRL_STEP2_NEXT) ? ctrl.get(SimpleWizardTest.CTRL_STEP2_NEXT).toString() : null;
	}

	@Override
	public String getPrevStep() {
		return ctrl.containsKey(SimpleWizardTest.CTRL_STEP2_PREV) ? ctrl.get(SimpleWizardTest.CTRL_STEP2_PREV).toString() : null;
	}
	
	@Override
	public void prepare(final Properties content, final Map<String, Object> temporary) throws PreparationException {
		content.setProperty(SimpleWizardTest.KEY_PREPARE,content.getProperty(SimpleWizardTest.KEY_PREPARE,"")+"2");
		content.setProperty(SimpleWizardTest.KEY_LIFECYCLE,content.getProperty(SimpleWizardTest.KEY_LIFECYCLE,"")+"P2");
	}

	@Override
	public void beforeShow(final Properties content, Map<String, Object> temporary, ErrorProcessing<Properties, TestError> err) throws FlowException {
		content.setProperty(SimpleWizardTest.KEY_BEFORE_SHOW,content.getProperty(SimpleWizardTest.KEY_BEFORE_SHOW,"")+"2");
		content.setProperty(SimpleWizardTest.KEY_LIFECYCLE,content.getProperty(SimpleWizardTest.KEY_LIFECYCLE,"")+"B2");
		
		try{err.processError(content,TestError.ERROR1);
		} catch (LocalizationException | FlowException e) {
		}
		try{err.processWarning(content,TestError.ERROR2,"1");
		} catch (LocalizationException e) {
		}
	}

	@Override
	public boolean validate(final Properties content, final Map<String, Object> temporary, final ErrorProcessing<Properties, TestError> err) throws FlowException {
		content.setProperty(SimpleWizardTest.KEY_VALIDATE,content.getProperty(SimpleWizardTest.KEY_VALIDATE,"")+"2");
		content.setProperty(SimpleWizardTest.KEY_LIFECYCLE,content.getProperty(SimpleWizardTest.KEY_LIFECYCLE,"")+"V2");
		return temporary.containsKey(SimpleWizardTest.CTRL_STEP2_VALIDATION) ? ((Boolean)temporary.get(SimpleWizardTest.CTRL_STEP2_VALIDATION)).booleanValue() : true;
	}
	
	@Override
	public void afterShow(Properties content, Map<String, Object> temporary, ErrorProcessing<Properties, TestError> err) throws FlowException {
		content.setProperty(SimpleWizardTest.KEY_AFTER_SHOW,content.getProperty(SimpleWizardTest.KEY_AFTER_SHOW,"")+"2");
		content.setProperty(SimpleWizardTest.KEY_LIFECYCLE,content.getProperty(SimpleWizardTest.KEY_LIFECYCLE,"")+"A2");
	}

	@Override
	public void unprepare(final Properties content, final Map<String, Object> temporary) {
		content.setProperty(SimpleWizardTest.KEY_UNPREPARE,content.getProperty(SimpleWizardTest.KEY_UNPREPARE,"")+"2");
		content.setProperty(SimpleWizardTest.KEY_LIFECYCLE,content.getProperty(SimpleWizardTest.KEY_LIFECYCLE,"")+"U2");
	}
}

@LocaleResourceLocation(Localizer.LOCALIZER_SCHEME+":prop:chav1961/purelib/i18n/test")
class WizardStep3 extends AbstractWizardStep<Properties,TestError,JComponent> {
	private final HashMap<String, Object>	ctrl;

@LocaleResource(value="key1",tooltip="key2")
	public final JTextField	f = new JTextField();
	
	public WizardStep3(HashMap<String, Object> ctrl) {
		this.ctrl = ctrl;
	}

	@Override
	public StepType getStepType() {
		return StepType.TERM_SUCCESS;
	}

	@Override
	public String getCaption() {
		return "step3";
	}

	@Override
	public JComponent getContent() {
		return f;
	}

	@Override
	public String getNextStep() {
		return ctrl.containsKey(SimpleWizardTest.CTRL_STEP3_NEXT) ? ctrl.get(SimpleWizardTest.CTRL_STEP3_NEXT).toString() : null;
	}

	@Override
	public String getPrevStep() {
		return ctrl.containsKey(SimpleWizardTest.CTRL_STEP3_PREV) ? ctrl.get(SimpleWizardTest.CTRL_STEP3_PREV).toString() : null;
	}
	
	@Override
	public void prepare(final Properties content, final Map<String, Object> temporary) throws PreparationException {
		content.setProperty(SimpleWizardTest.KEY_PREPARE,content.getProperty(SimpleWizardTest.KEY_PREPARE,"")+"3");
		content.setProperty(SimpleWizardTest.KEY_LIFECYCLE,content.getProperty(SimpleWizardTest.KEY_LIFECYCLE,"")+"P3");
	}

	@Override
	public void beforeShow(final Properties content, Map<String, Object> temporary, ErrorProcessing<Properties, TestError> err) throws FlowException {
		content.setProperty(SimpleWizardTest.KEY_BEFORE_SHOW,content.getProperty(SimpleWizardTest.KEY_BEFORE_SHOW,"")+"3");
		content.setProperty(SimpleWizardTest.KEY_LIFECYCLE,content.getProperty(SimpleWizardTest.KEY_LIFECYCLE,"")+"B3");
	}

	@Override
	public boolean validate(final Properties content, final Map<String, Object> temporary, final ErrorProcessing<Properties, TestError> err) throws FlowException {
		content.setProperty(SimpleWizardTest.KEY_VALIDATE,content.getProperty(SimpleWizardTest.KEY_VALIDATE,"")+"3");
		content.setProperty(SimpleWizardTest.KEY_LIFECYCLE,content.getProperty(SimpleWizardTest.KEY_LIFECYCLE,"")+"V3");
		return true;
	}
	
	@Override
	public void afterShow(Properties content, Map<String, Object> temporary, ErrorProcessing<Properties, TestError> err) throws FlowException {
		content.setProperty(SimpleWizardTest.KEY_AFTER_SHOW,content.getProperty(SimpleWizardTest.KEY_AFTER_SHOW,"")+"3");
		content.setProperty(SimpleWizardTest.KEY_LIFECYCLE,content.getProperty(SimpleWizardTest.KEY_LIFECYCLE,"")+"A3");
	}

	@Override
	public void unprepare(final Properties content, final Map<String, Object> temporary) {
		content.setProperty(SimpleWizardTest.KEY_UNPREPARE,content.getProperty(SimpleWizardTest.KEY_UNPREPARE,"")+"3");
		content.setProperty(SimpleWizardTest.KEY_LIFECYCLE,content.getProperty(SimpleWizardTest.KEY_LIFECYCLE,"")+"U3");
	}
}

class WizardStep4 extends AbstractWizardStep<Properties,TestError,JComponent> {
	private final HashMap<String, Object>	ctrl;
	
	public WizardStep4(HashMap<String, Object> ctrl) {
		this.ctrl = ctrl;
	}

	@Override
	public StepType getStepType() {
		return StepType.PROCESSING;
	}

	@Override
	public String getCaption() {
		return "step4";
	}

	@Override
	public JComponent getContent() {
		return new JLabel(getCaption());
	}

	@Override
	public String getNextStep() {
		return null;
	}

	@Override
	public String getPrevStep() {
		return null;
	}
	
	@Override
	public void prepare(final Properties content, final Map<String, Object> temporary) throws PreparationException {
		content.setProperty(SimpleWizardTest.KEY_PREPARE,content.getProperty(SimpleWizardTest.KEY_PREPARE,"")+"4");
		content.setProperty(SimpleWizardTest.KEY_LIFECYCLE,content.getProperty(SimpleWizardTest.KEY_LIFECYCLE,"")+"P4");
	}

	@Override
	public void beforeShow(final Properties content, Map<String, Object> temporary, ErrorProcessing<Properties, TestError> err) throws FlowException {
		content.setProperty(SimpleWizardTest.KEY_BEFORE_SHOW,content.getProperty(SimpleWizardTest.KEY_BEFORE_SHOW,"")+"4");
		content.setProperty(SimpleWizardTest.KEY_LIFECYCLE,content.getProperty(SimpleWizardTest.KEY_LIFECYCLE,"")+"B4");
	}

	@Override
	public boolean validate(final Properties content, final Map<String, Object> temporary, final ErrorProcessing<Properties, TestError> err) throws FlowException {
		content.setProperty(SimpleWizardTest.KEY_VALIDATE,content.getProperty(SimpleWizardTest.KEY_VALIDATE,"")+"4");
		content.setProperty(SimpleWizardTest.KEY_LIFECYCLE,content.getProperty(SimpleWizardTest.KEY_LIFECYCLE,"")+"V4");
		return temporary.containsKey(SimpleWizardTest.CTRL_STEP4_VALIDATION) ? ((Boolean)temporary.get(SimpleWizardTest.CTRL_STEP4_VALIDATION)).booleanValue() : true;
	}
	
	@Override
	public void afterShow(Properties content, Map<String, Object> temporary, ErrorProcessing<Properties, TestError> err) throws FlowException {
		content.setProperty(SimpleWizardTest.KEY_AFTER_SHOW,content.getProperty(SimpleWizardTest.KEY_AFTER_SHOW,"")+"4");
		content.setProperty(SimpleWizardTest.KEY_LIFECYCLE,content.getProperty(SimpleWizardTest.KEY_LIFECYCLE,"")+"A4");
	}

	@Override
	public void unprepare(final Properties content, final Map<String, Object> temporary) {
		content.setProperty(SimpleWizardTest.KEY_UNPREPARE,content.getProperty(SimpleWizardTest.KEY_UNPREPARE,"")+"4");
		content.setProperty(SimpleWizardTest.KEY_LIFECYCLE,content.getProperty(SimpleWizardTest.KEY_LIFECYCLE,"")+"U4");
	}
}
