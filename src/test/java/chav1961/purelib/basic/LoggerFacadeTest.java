package chav1961.purelib.basic;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.LoggerCallbackInterface;
import chav1961.purelib.basic.interfaces.LoggerFacade.Reducing;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.testing.OrdinalTestCategory;

@Category(OrdinalTestCategory.class)
public class LoggerFacadeTest {
	@Test
	public void basicTest() {
		try(final LoggerFacade	lf = new AbstractLoggerFacade() {
								@Override
								public boolean canServe(URI resource) throws NullPointerException {
									return false;
								}
					
								@Override
								public LoggerFacade newInstance(URI resource) throws EnvironmentException, NullPointerException, IllegalArgumentException {
									return this;
								}
								
								@Override
								protected void toLogger(final Severity level, final String text, final Throwable throwable) {
								}
								
								@Override
								protected AbstractLoggerFacade getAbstractLoggerFacade(String mark, Class<?> root) {
									return this;
								}
							}) {
			 
			Assert.assertTrue(lf.isLoggedNow(Severity.debug));
			
			try {lf.isLoggedNow(null);				
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			
			Assert.assertEquals(0,lf.getReducing().size());
	
			lf.setReducing(Reducing.reduceCause);
			Assert.assertEquals(1,lf.getReducing().size());
			lf.setReducing(new HashSet<Reducing>(){private static final long serialVersionUID = 1L; {add(Reducing.reduceCause);}});
			Assert.assertEquals(1,lf.getReducing().size());

			try{lf.setReducing((Reducing[])null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{lf.setReducing((Reducing)null);
				Assert.fail("Mandatory exception was not detected (nulls inside 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{lf.setReducing((Set<Reducing>)null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			
			lf.pushReducing(Reducing.reduceCause,Reducing.reduceOverSubtree);
			Assert.assertEquals(2,lf.getReducing().size());
	
			lf.popReducing();
			Assert.assertEquals(1,lf.getReducing().size());
			
			try{lf.popReducing();
				Assert.fail("Mandatory exception was not detected (stack exhausted)");
			} catch (IllegalStateException exc) {
			}
		}
	}

	@Test
	public void messageTest() {
		final List<String>	messages = new ArrayList<>();
		final Throwable		t = new Throwable();
		
		try(final LoggerFacade	lf = new AbstractLoggerFacade() {
								@Override
								public boolean canServe(URI resource) throws NullPointerException {
									return false;
								}
					
								@Override
								public LoggerFacade newInstance(URI resource) throws EnvironmentException, NullPointerException, IllegalArgumentException {
									return this;
								}
			
								@Override
								protected void toLogger(final Severity level, final String text, final Throwable throwable) {
									Assert.assertEquals(Severity.error,level);
									Assert.assertTrue(throwable == null || throwable == t);
									messages.add(text);
								}
								
								@Override
								protected AbstractLoggerFacade getAbstractLoggerFacade(String mark, Class<?> root) {
									return this;
								}
							}) {

			lf.message(Severity.error,()->"test[0]");
			Assert.assertEquals("test[0]",messages.get(0));

			lf.message(Severity.error,t,()->"test[0]");
			Assert.assertEquals("test[0]",messages.get(1));
			
			lf.message(Severity.error,"test[1]");
			Assert.assertEquals("test[1]",messages.get(2));

			lf.message(Severity.error,t,"test[1]");
			Assert.assertEquals("test[1]",messages.get(3));
			
			try{lf.message(null,()->"test");
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{lf.message(Severity.error,(LoggerCallbackInterface)null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			try{lf.message(Severity.error,(String)null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}

			try{lf.message(null,t,()->"test");
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{lf.message(Severity.error,null,()->"test");
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			try{lf.message(Severity.error,t,(LoggerCallbackInterface)null);
				Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
			} catch (NullPointerException exc) {
			}
			try{lf.message(Severity.error,t,(String)null);
				Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
			} catch (NullPointerException exc) {
			}
		}
	}

	@Test
	public void transactionTest() {
		final List<String>	messages = new ArrayList<>();
		final Throwable		t = new Throwable();
		
		try(final LoggerFacade	lf = new AbstractLoggerFacade() {
								@Override
								public boolean canServe(URI resource) throws NullPointerException {
									return false;
								}
					
								@Override
								public LoggerFacade newInstance(URI resource) throws EnvironmentException, NullPointerException, IllegalArgumentException {
									return this;
								}
			
								@Override
								protected void toLogger(final Severity level, final String text, final Throwable throwable) {
								}
								
								@Override
								protected AbstractLoggerFacade getAbstractLoggerFacade(String mark, Class<?> root) {
									return new AbstractLoggerFacade(mark,root,new HashSet<>()) {
										@Override
										public boolean canServe(URI resource) throws NullPointerException {
											return false;
										}
							
										@Override
										public LoggerFacade newInstance(URI resource) throws EnvironmentException, NullPointerException, IllegalArgumentException {
											return this;
										}
										
										@Override
										protected void toLogger(Severity level, String text, Throwable throwable) {
											Assert.assertEquals(Severity.error,level);
											Assert.assertTrue(throwable == null || throwable == t);
											messages.add(text);
										}
										
										@Override
										protected AbstractLoggerFacade getAbstractLoggerFacade(String mark, Class<?> root) {
											return null;
										}
									};
								}
							}) {

			messages.clear();
			try(final LoggerFacade	inner = lf.transaction("test")) {
				inner.message(Severity.error,()->"test[0]");
				inner.message(Severity.error,t,()->"test[1]");
			}
			Assert.assertEquals("test[0]",messages.get(0));
			Assert.assertEquals("test[1]",messages.get(1));

			messages.clear();
			try(final LoggerFacade	inner = lf.transaction("test")) {
				inner.message(Severity.error,()->"test[0]");
				inner.message(Severity.error,t,()->"test[1]");
				inner.rollback();
			}
			Assert.assertEquals(0,messages.size());

			try{lf.transaction(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{lf.transaction("");
				Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{lf.transaction("test",null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
		}
	}

	@Test
	public void reducingTest() {
		final Set<Reducing>	reducing = new HashSet<>();

		// Reduce runtime exceptions:
		final RuntimeException			exc = new RuntimeException();
		final Set<StackTraceElement>	repeatables = new HashSet<>();
		
		Assert.assertTrue(AbstractLoggerFacade.needPrintException(exc, reducing, repeatables));
		Assert.assertTrue(AbstractLoggerFacade.needPrintException(exc, reducing, repeatables));
		
		reducing.add(Reducing.reduceRuntimeExceptions);
		Assert.assertTrue(AbstractLoggerFacade.needPrintException(exc, reducing, repeatables));
		Assert.assertFalse(AbstractLoggerFacade.needPrintException(exc, reducing, repeatables));

		reducing.remove(Reducing.reduceRuntimeExceptions);
		Assert.assertTrue(AbstractLoggerFacade.needPrintException(exc, reducing, repeatables));
		
		// Reduce over subtree:
		reducing.add(Reducing.reduceOverSubtree);
		Assert.assertEquals(1,AbstractLoggerFacade.convert(new Throwable(), reducing, this.getClass()).getStackTrace().length);

		reducing.remove(Reducing.reduceOverSubtree);
		Assert.assertTrue(AbstractLoggerFacade.convert(new Throwable(), reducing, this.getClass()).getStackTrace().length > 1);
 
		// Reduce JRE path:
		final Throwable	t = new Throwable();
		final int		stackSize = t.getStackTrace().length;

		Assert.assertEquals(stackSize,AbstractLoggerFacade.convert(t, reducing, this.getClass()).getStackTrace().length);
		
		reducing.add(Reducing.reduceJREPath);
		Assert.assertTrue(AbstractLoggerFacade.convert(t, reducing, this.getClass()).getStackTrace().length < stackSize);
		
		reducing.remove(Reducing.reduceJREPath);
 
		// Reduce cause:
		final Throwable	cause = new Throwable("line1");
		final Throwable	wrapper = new Throwable("line2",cause);
		
		Assert.assertEquals("line2",AbstractLoggerFacade.convert(wrapper, reducing, this.getClass()).getLocalizedMessage());
		Assert.assertNotNull(AbstractLoggerFacade.convert(wrapper, reducing, this.getClass()).getCause());

		reducing.add(Reducing.reduceCause);
		Assert.assertEquals("Throwable: line2\nThrowable: line1",AbstractLoggerFacade.convert(wrapper, reducing, this.getClass()).getLocalizedMessage());
		Assert.assertNull(AbstractLoggerFacade.convert(wrapper, reducing, this.getClass()).getCause());

		reducing.remove(Reducing.reduceCause);
	}
}
