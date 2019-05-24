package chav1961.purelib.basic;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sun.tools.internal.xjc.model.Constructor;

import chav1961.purelib.basic.GettersAndSettersFactory.BooleanGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.ByteGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.CharGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.DoubleGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.FloatGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.Instantiator;
import chav1961.purelib.basic.GettersAndSettersFactory.IntGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.LongGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.ObjectGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.ShortGetterAndSetter;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.gettersandsetters.ArrayReferencedOwner;
import chav1961.purelib.basic.gettersandsetters.BooleanOwner;
import chav1961.purelib.basic.gettersandsetters.ByteOwner;
import chav1961.purelib.basic.gettersandsetters.CharOwner;
import chav1961.purelib.basic.gettersandsetters.DoubleOwner;
import chav1961.purelib.basic.gettersandsetters.FloatOwner;
import chav1961.purelib.basic.gettersandsetters.IntOwner;
import chav1961.purelib.basic.gettersandsetters.LongOwner;
import chav1961.purelib.basic.gettersandsetters.ReferencedOwner;
import chav1961.purelib.basic.gettersandsetters.ShortOwner;
import chav1961.purelib.basic.gettersandsetters.StaticArrayReferencedOwner;
import chav1961.purelib.basic.gettersandsetters.StaticBooleanOwner;
import chav1961.purelib.basic.gettersandsetters.StaticByteOwner;
import chav1961.purelib.basic.gettersandsetters.StaticCharOwner;
import chav1961.purelib.basic.gettersandsetters.StaticDoubleOwner;
import chav1961.purelib.basic.gettersandsetters.StaticFloatOwner;
import chav1961.purelib.basic.gettersandsetters.StaticIntOwner;
import chav1961.purelib.basic.gettersandsetters.StaticLongOwner;
import chav1961.purelib.basic.gettersandsetters.StaticReferencedOwner;
import chav1961.purelib.basic.gettersandsetters.StaticShortOwner;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;

public class GettersAndSettersFactoryTest {
	@Before
	public void prepare() {
		GettersAndSettersFactory.clearCache();
	}

	@Test
	public void uriStyledTest() throws IllegalArgumentException, NullPointerException, ContentException {
		final Object					publicBooleanObj = new BooleanOwner();
		final BooleanGetterAndSetter	booleanGS = (BooleanGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(
												URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+ContentModelFactory.APPLICATION_SCHEME_FIELD+":/"+BooleanOwner.class.getName()+"/value"));
		
		Assert.assertFalse(((BooleanOwner)publicBooleanObj).value);
		Assert.assertFalse(booleanGS.get(publicBooleanObj));
		booleanGS.set(publicBooleanObj,true);
		Assert.assertTrue(((BooleanOwner)publicBooleanObj).value);
		Assert.assertTrue(booleanGS.get(publicBooleanObj));
		
		try{GettersAndSettersFactory.buildGetterAndSetter(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {			
		}
		try{GettersAndSettersFactory.buildGetterAndSetter(URI.create("/test"));
			Assert.fail("Mandatory exception was not detected (scheme is missing)");
		} catch (IllegalArgumentException exc) {			
		}
		try{GettersAndSettersFactory.buildGetterAndSetter(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":/test"));
			Assert.fail("Mandatory exception was not detected (subscheme is missing)");
		} catch (IllegalArgumentException exc) {			
		}
		try{GettersAndSettersFactory.buildGetterAndSetter(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+ContentModelFactory.APPLICATION_SCHEME_FIELD+":/"));
			Assert.fail("Mandatory exception was not detected (illegal path format)");
		} catch (IllegalArgumentException exc) {			
		}
		try{GettersAndSettersFactory.buildGetterAndSetter(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":"+ContentModelFactory.APPLICATION_SCHEME_FIELD+":/unknown/unknown"));
			Assert.fail("Mandatory exception was not detected (class not found)");
		} catch (IllegalArgumentException exc) {			
		}
	}
	
	
	@Test
	public void primitivePublicTest() throws IllegalArgumentException, NullPointerException, ContentException {
		// boolean test
		final Object					publicBooleanObj = new BooleanOwner();
		final BooleanGetterAndSetter	booleanGS = (BooleanGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(publicBooleanObj.getClass(),"value");

		Assert.assertFalse(((BooleanOwner)publicBooleanObj).value);
		Assert.assertFalse(booleanGS.get(publicBooleanObj));
		booleanGS.set(publicBooleanObj,true);
		Assert.assertTrue(((BooleanOwner)publicBooleanObj).value);
		Assert.assertTrue(booleanGS.get(publicBooleanObj));

		final Object					publicStaticBooleanObj = new StaticBooleanOwner();
		final BooleanGetterAndSetter	booleanStaticGS = (BooleanGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(publicStaticBooleanObj.getClass(),"value");

		Assert.assertFalse(StaticBooleanOwner.value);
		Assert.assertFalse(booleanStaticGS.get(publicStaticBooleanObj));
		booleanStaticGS.set(publicStaticBooleanObj,true);
		Assert.assertTrue(StaticBooleanOwner.value);
		Assert.assertTrue(booleanStaticGS.get(publicStaticBooleanObj));

		// byte test
		final Object					publicByteObj = new ByteOwner();
		final ByteGetterAndSetter		byteGS = (ByteGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(publicByteObj.getClass(),"value");

		Assert.assertEquals(((ByteOwner)publicByteObj).value,Byte.MIN_VALUE);
		Assert.assertEquals(byteGS.get(publicByteObj),Byte.MIN_VALUE);
		byteGS.set(publicByteObj,Byte.MAX_VALUE);
		Assert.assertEquals(((ByteOwner)publicByteObj).value,Byte.MAX_VALUE);
		Assert.assertEquals(byteGS.get(publicByteObj),Byte.MAX_VALUE);

		final Object					publicStaticByteObj = new StaticByteOwner();
		final ByteGetterAndSetter		byteStaticGS = (ByteGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(publicStaticByteObj.getClass(),"value");

		Assert.assertEquals(StaticByteOwner.value,Byte.MIN_VALUE);
		Assert.assertEquals(byteStaticGS.get(publicStaticByteObj),Byte.MIN_VALUE);
		byteStaticGS.set(publicStaticByteObj,Byte.MAX_VALUE);
		Assert.assertEquals(StaticByteOwner.value,Byte.MAX_VALUE);
		Assert.assertEquals(byteStaticGS.get(publicStaticByteObj),Byte.MAX_VALUE);
		
		// char test
		final Object					publicCharObj = new CharOwner();
		final CharGetterAndSetter		charGS = (CharGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(publicCharObj.getClass(),"value");

		Assert.assertEquals(((CharOwner)publicCharObj).value,Character.MIN_VALUE);
		Assert.assertEquals(charGS.get(publicCharObj),Character.MIN_VALUE);
		charGS.set(publicCharObj,Character.MAX_VALUE);
		Assert.assertEquals(((CharOwner)publicCharObj).value,Character.MAX_VALUE);
		Assert.assertEquals(charGS.get(publicCharObj),Character.MAX_VALUE);

		final Object					publicStaticCharObj = new StaticCharOwner();
		final CharGetterAndSetter		charStaticGS = (CharGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(publicStaticCharObj.getClass(),"value");

		Assert.assertEquals(StaticCharOwner.value,Character.MIN_VALUE);
		Assert.assertEquals(charStaticGS.get(publicStaticCharObj),Character.MIN_VALUE);
		charStaticGS.set(publicStaticCharObj,Character.MAX_VALUE);
		Assert.assertEquals(StaticCharOwner.value,Character.MAX_VALUE);
		Assert.assertEquals(charStaticGS.get(publicStaticCharObj),Character.MAX_VALUE);
		
		// double test
		final Object					publicDoubleObj = new DoubleOwner();
		final DoubleGetterAndSetter		doubleGS = (DoubleGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(publicDoubleObj.getClass(),"value");

		Assert.assertEquals(((DoubleOwner)publicDoubleObj).value,Double.MIN_VALUE,0.0001);
		Assert.assertEquals(doubleGS.get(publicDoubleObj),Double.MIN_VALUE,0.0001);
		doubleGS.set(publicDoubleObj,Double.MAX_VALUE);
		Assert.assertEquals(((DoubleOwner)publicDoubleObj).value,Double.MAX_VALUE,0.0001);
		Assert.assertEquals(doubleGS.get(publicDoubleObj),Double.MAX_VALUE,0.0001);

		final Object					publicStaticDoubleObj = new StaticDoubleOwner();
		final DoubleGetterAndSetter		doubleStaticGS = (DoubleGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(publicStaticDoubleObj.getClass(),"value");

		Assert.assertEquals(StaticDoubleOwner.value,Double.MIN_VALUE,0.0001);
		Assert.assertEquals(doubleStaticGS.get(publicStaticDoubleObj),Double.MIN_VALUE,0.0001);
		doubleStaticGS.set(publicStaticDoubleObj,Double.MAX_VALUE);
		Assert.assertEquals(StaticDoubleOwner.value,Double.MAX_VALUE,0.0001);
		Assert.assertEquals(doubleStaticGS.get(publicStaticDoubleObj),Double.MAX_VALUE,0.0001);

		// float test
		final Object					publicFloatObj = new FloatOwner();
		final FloatGetterAndSetter		floatGS = (FloatGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(publicFloatObj.getClass(),"value");

		Assert.assertEquals(((FloatOwner)publicFloatObj).value,Float.MIN_VALUE,0.0001);
		Assert.assertEquals(floatGS.get(publicFloatObj),Float.MIN_VALUE,0.0001);
		floatGS.set(publicFloatObj,Float.MAX_VALUE);
		Assert.assertEquals(((FloatOwner)publicFloatObj).value,Float.MAX_VALUE,0.0001);
		Assert.assertEquals(floatGS.get(publicFloatObj),Float.MAX_VALUE,0.0001);

		final Object					publicStaticFloatObj = new StaticFloatOwner();
		final FloatGetterAndSetter		floatStaticGS = (FloatGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(publicStaticFloatObj.getClass(),"value");

		Assert.assertEquals(StaticFloatOwner.value,Float.MIN_VALUE,0.0001);
		Assert.assertEquals(floatStaticGS.get(publicStaticFloatObj),Float.MIN_VALUE,0.0001);
		floatStaticGS.set(publicStaticFloatObj,Float.MAX_VALUE);
		Assert.assertEquals(StaticFloatOwner.value,Float.MAX_VALUE,0.0001);
		Assert.assertEquals(floatStaticGS.get(publicStaticFloatObj),Float.MAX_VALUE,0.0001);

		// int test
		final Object					publicIntObj = new IntOwner();
		final IntGetterAndSetter		intGS = (IntGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(publicIntObj.getClass(),"value");

		Assert.assertEquals(((IntOwner)publicIntObj).value,Integer.MIN_VALUE);
		Assert.assertEquals(intGS.get(publicIntObj),Integer.MIN_VALUE);
		intGS.set(publicIntObj,Integer.MAX_VALUE);
		Assert.assertEquals(((IntOwner)publicIntObj).value,Integer.MAX_VALUE);
		Assert.assertEquals(intGS.get(publicIntObj),Integer.MAX_VALUE);

		final Object					publicStaticIntObj = new StaticIntOwner();
		final IntGetterAndSetter		intStaticGS = (IntGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(publicStaticIntObj.getClass(),"value");

		Assert.assertEquals(StaticIntOwner.value,Integer.MIN_VALUE);
		Assert.assertEquals(intStaticGS.get(publicStaticIntObj),Integer.MIN_VALUE);
		intStaticGS.set(publicStaticIntObj,Integer.MAX_VALUE);
		Assert.assertEquals(StaticIntOwner.value,Integer.MAX_VALUE);
		Assert.assertEquals(intStaticGS.get(publicStaticIntObj),Integer.MAX_VALUE);

		// long test
		final Object					publicLongObj = new LongOwner();
		final LongGetterAndSetter		longGS = (LongGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(publicLongObj.getClass(),"value");

		Assert.assertEquals(((LongOwner)publicLongObj).value,Long.MIN_VALUE);
		Assert.assertEquals(longGS.get(publicLongObj),Long.MIN_VALUE);
		longGS.set(publicLongObj,Long.MAX_VALUE);
		Assert.assertEquals(((LongOwner)publicLongObj).value,Long.MAX_VALUE);
		Assert.assertEquals(longGS.get(publicLongObj),Long.MAX_VALUE);

		final Object					publicStaticLongObj = new StaticLongOwner();
		final LongGetterAndSetter		longStaticGS = (LongGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(publicStaticLongObj.getClass(),"value");

		Assert.assertEquals(StaticLongOwner.value,Long.MIN_VALUE);
		Assert.assertEquals(longStaticGS.get(publicStaticLongObj),Long.MIN_VALUE);
		longStaticGS.set(publicStaticLongObj,Long.MAX_VALUE);
		Assert.assertEquals(StaticLongOwner.value,Long.MAX_VALUE);
		Assert.assertEquals(longStaticGS.get(publicStaticLongObj),Long.MAX_VALUE);

		// short test
		final Object					publicShortObj = new ShortOwner();
		final ShortGetterAndSetter		shortGS = (ShortGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(publicShortObj.getClass(),"value");

		Assert.assertEquals(((ShortOwner)publicShortObj).value,Short.MIN_VALUE);
		Assert.assertEquals(shortGS.get(publicShortObj),Short.MIN_VALUE);
		shortGS.set(publicShortObj,Short.MAX_VALUE);
		Assert.assertEquals(((ShortOwner)publicShortObj).value,Short.MAX_VALUE);
		Assert.assertEquals(shortGS.get(publicShortObj),Short.MAX_VALUE);

		final Object					publicStaticShortObj = new StaticShortOwner();
		final ShortGetterAndSetter		shortStaticGS = (ShortGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(publicStaticShortObj.getClass(),"value");

		Assert.assertEquals(StaticShortOwner.value,Short.MIN_VALUE);
		Assert.assertEquals(shortStaticGS.get(publicStaticShortObj),Short.MIN_VALUE);
		shortStaticGS.set(publicStaticShortObj,Short.MAX_VALUE);
		Assert.assertEquals(StaticShortOwner.value,Short.MAX_VALUE);
		Assert.assertEquals(shortStaticGS.get(publicStaticShortObj),Short.MAX_VALUE);
	}

	@Test
	public void referencedPublicTest() throws IllegalArgumentException, NullPointerException, ContentException {
		final Object						publicRefObj = new ReferencedOwner();
		@SuppressWarnings("unchecked")
		final ObjectGetterAndSetter<String>	refGS = (ObjectGetterAndSetter<String>) GettersAndSettersFactory.buildGetterAndSetter(publicRefObj.getClass(),"value");

		Assert.assertNull(((ReferencedOwner)publicRefObj).value);
		Assert.assertNull(refGS.get(publicRefObj));
		refGS.set(publicRefObj,"test");
		Assert.assertEquals(((ReferencedOwner)publicRefObj).value,"test");
		Assert.assertEquals(refGS.get(publicRefObj),"test");

		final Object						publicStaticRefObj = new StaticReferencedOwner();
		@SuppressWarnings("unchecked")
		final ObjectGetterAndSetter<String>	refStaticGS = (ObjectGetterAndSetter<String>) GettersAndSettersFactory.buildGetterAndSetter(publicStaticRefObj.getClass(),"value");

		Assert.assertNull(StaticReferencedOwner.value);
		Assert.assertNull(refStaticGS.get(publicStaticRefObj));
		refStaticGS.set(publicStaticRefObj,"test");
		Assert.assertEquals(StaticReferencedOwner.value,"test");
		Assert.assertEquals(refStaticGS.get(publicStaticRefObj),"test");

		final Object						publicArrayRefObj = new ArrayReferencedOwner();
		@SuppressWarnings("unchecked")
		final ObjectGetterAndSetter<int[]>	arrayRefGS = (ObjectGetterAndSetter<int[]>) GettersAndSettersFactory.buildGetterAndSetter(publicArrayRefObj.getClass(),"value");

		Assert.assertNull(((ArrayReferencedOwner)publicArrayRefObj).value);
		Assert.assertNull(arrayRefGS.get(publicArrayRefObj));
		arrayRefGS.set(publicArrayRefObj,new int[]{1,2,3});
		Assert.assertArrayEquals(((ArrayReferencedOwner)publicArrayRefObj).value,new int[]{1,2,3});
		Assert.assertArrayEquals(arrayRefGS.get(publicArrayRefObj),new int[]{1,2,3});

		final Object						publicStaticArrayRefObj = new StaticArrayReferencedOwner();
		@SuppressWarnings("unchecked")
		final ObjectGetterAndSetter<int[]>	arrayRefStaticGS = (ObjectGetterAndSetter<int[]>) GettersAndSettersFactory.buildGetterAndSetter(publicStaticArrayRefObj.getClass(),"value");

		Assert.assertNull(StaticArrayReferencedOwner.value);
		Assert.assertNull(arrayRefStaticGS.get(publicStaticArrayRefObj));
		arrayRefStaticGS.set(publicStaticArrayRefObj,new int[]{1,2,3});
		Assert.assertArrayEquals(StaticArrayReferencedOwner.value,new int[]{1,2,3});
		Assert.assertArrayEquals(arrayRefStaticGS.get(publicStaticArrayRefObj),new int[]{1,2,3});
	}

	@Test
	public void primitivePrivateTest() throws IllegalArgumentException, NullPointerException, ContentException, NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
		PureLibSettings.instance().setProperty(PureLibSettings.ALLOW_UNSAFE,"false");
		
		try{final Method	m = GettersAndSettersFactory.class.getDeclaredMethod("prepareStatic");
			m.setAccessible(true);
			m.invoke(null);
			
			// boolean test
			final Object					publicBooleanObj = new BooleanOwner();
			final BooleanGetterAndSetter	booleanGS = (BooleanGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(publicBooleanObj.getClass(),"privateValue");
	
			Assert.assertFalse(((BooleanOwner)publicBooleanObj).getPrivateValue());
			Assert.assertFalse(booleanGS.get(publicBooleanObj));
			booleanGS.set(publicBooleanObj,true);
			Assert.assertTrue(((BooleanOwner)publicBooleanObj).getPrivateValue());
			Assert.assertTrue(booleanGS.get(publicBooleanObj));
	
			final Object					publicStaticBooleanObj = new StaticBooleanOwner();
			final BooleanGetterAndSetter	booleanStaticGS = (BooleanGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(publicStaticBooleanObj.getClass(),"privateValue");
	
			Assert.assertFalse(((StaticBooleanOwner)publicStaticBooleanObj).getPrivateValue());
			Assert.assertFalse(booleanStaticGS.get(publicStaticBooleanObj));
			booleanStaticGS.set(publicStaticBooleanObj,true);
			Assert.assertTrue(((StaticBooleanOwner)publicStaticBooleanObj).getPrivateValue());
			Assert.assertTrue(booleanStaticGS.get(publicStaticBooleanObj));
	
			// byte test
			final Object					publicByteObj = new ByteOwner();
			final ByteGetterAndSetter		byteGS = (ByteGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(publicByteObj.getClass(),"privateValue");
	
			Assert.assertEquals(((ByteOwner)publicByteObj).getPrivateValue(),Byte.MIN_VALUE);
			Assert.assertEquals(byteGS.get(publicByteObj),Byte.MIN_VALUE);
			byteGS.set(publicByteObj,Byte.MAX_VALUE);
			Assert.assertEquals(((ByteOwner)publicByteObj).getPrivateValue(),Byte.MAX_VALUE);
			Assert.assertEquals(byteGS.get(publicByteObj),Byte.MAX_VALUE);
	
			final Object					publicStaticByteObj = new StaticByteOwner();
			final ByteGetterAndSetter		byteStaticGS = (ByteGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(publicStaticByteObj.getClass(),"privateValue");
	
			Assert.assertEquals(((StaticByteOwner)publicStaticByteObj).getPrivateValue(),Byte.MIN_VALUE);
			Assert.assertEquals(byteStaticGS.get(publicStaticByteObj),Byte.MIN_VALUE);
			byteStaticGS.set(publicStaticByteObj,Byte.MAX_VALUE);
			Assert.assertEquals(((StaticByteOwner)publicStaticByteObj).getPrivateValue(),Byte.MAX_VALUE);
			Assert.assertEquals(byteStaticGS.get(publicStaticByteObj),Byte.MAX_VALUE);
			
			// char test
			final Object					publicCharObj = new CharOwner();
			final CharGetterAndSetter		charGS = (CharGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(publicCharObj.getClass(),"privateValue");
	
			Assert.assertEquals(((CharOwner)publicCharObj).getPrivateValue(),Character.MIN_VALUE);
			Assert.assertEquals(charGS.get(publicCharObj),Character.MIN_VALUE);
			charGS.set(publicCharObj,Character.MAX_VALUE);
			Assert.assertEquals(((CharOwner)publicCharObj).getPrivateValue(),Character.MAX_VALUE);
			Assert.assertEquals(charGS.get(publicCharObj),Character.MAX_VALUE);
	
			final Object					publicStaticCharObj = new StaticCharOwner();
			final CharGetterAndSetter		charStaticGS = (CharGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(publicStaticCharObj.getClass(),"privateValue");
	
			Assert.assertEquals(((StaticCharOwner)publicStaticCharObj).getPrivateValue(),Character.MIN_VALUE);
			Assert.assertEquals(charStaticGS.get(publicStaticCharObj),Character.MIN_VALUE);
			charStaticGS.set(publicStaticCharObj,Character.MAX_VALUE);
			Assert.assertEquals(((StaticCharOwner)publicStaticCharObj).getPrivateValue(),Character.MAX_VALUE);
			Assert.assertEquals(charStaticGS.get(publicStaticCharObj),Character.MAX_VALUE);
			
			// double test
			final Object					publicDoubleObj = new DoubleOwner();
			final DoubleGetterAndSetter		doubleGS = (DoubleGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(publicDoubleObj.getClass(),"privateValue");
	
			Assert.assertEquals(((DoubleOwner)publicDoubleObj).getPrivateValue(),Double.MIN_VALUE,0.0001);
			Assert.assertEquals(doubleGS.get(publicDoubleObj),Double.MIN_VALUE,0.0001);
			doubleGS.set(publicDoubleObj,Double.MAX_VALUE);
			Assert.assertEquals(((DoubleOwner)publicDoubleObj).getPrivateValue(),Double.MAX_VALUE,0.0001);
			Assert.assertEquals(doubleGS.get(publicDoubleObj),Double.MAX_VALUE,0.0001);
	
			final Object					publicStaticDoubleObj = new StaticDoubleOwner();
			final DoubleGetterAndSetter		doubleStaticGS = (DoubleGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(publicStaticDoubleObj.getClass(),"privateValue");
	
			Assert.assertEquals(((StaticDoubleOwner)publicStaticDoubleObj).getPrivateValue(),Double.MIN_VALUE,0.0001);
			Assert.assertEquals(doubleStaticGS.get(publicStaticDoubleObj),Double.MIN_VALUE,0.0001);
			doubleStaticGS.set(publicStaticDoubleObj,Double.MAX_VALUE);
			Assert.assertEquals(((StaticDoubleOwner)publicStaticDoubleObj).getPrivateValue(),Double.MAX_VALUE,0.0001);
			Assert.assertEquals(doubleStaticGS.get(publicStaticDoubleObj),Double.MAX_VALUE,0.0001);
	
			// float test
			final Object					publicFloatObj = new FloatOwner();
			final FloatGetterAndSetter		floatGS = (FloatGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(publicFloatObj.getClass(),"privateValue");
	
			Assert.assertEquals(((FloatOwner)publicFloatObj).getPrivateValue(),Float.MIN_VALUE,0.0001);
			Assert.assertEquals(floatGS.get(publicFloatObj),Float.MIN_VALUE,0.0001);
			floatGS.set(publicFloatObj,Float.MAX_VALUE);
			Assert.assertEquals(((FloatOwner)publicFloatObj).getPrivateValue(),Float.MAX_VALUE,0.0001);
			Assert.assertEquals(floatGS.get(publicFloatObj),Float.MAX_VALUE,0.0001);
	
			final Object					publicStaticFloatObj = new StaticFloatOwner();
			final FloatGetterAndSetter		floatStaticGS = (FloatGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(publicStaticFloatObj.getClass(),"privateValue");
	
			Assert.assertEquals(((StaticFloatOwner)publicStaticFloatObj).getPrivateValue(),Float.MIN_VALUE,0.0001);
			Assert.assertEquals(floatStaticGS.get(publicStaticFloatObj),Float.MIN_VALUE,0.0001);
			floatStaticGS.set(publicStaticFloatObj,Float.MAX_VALUE);
			Assert.assertEquals(((StaticFloatOwner)publicStaticFloatObj).getPrivateValue(),Float.MAX_VALUE,0.0001);
			Assert.assertEquals(floatStaticGS.get(publicStaticFloatObj),Float.MAX_VALUE,0.0001);
	
			// int test
			final Object					publicIntObj = new IntOwner();
			final IntGetterAndSetter		intGS = (IntGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(publicIntObj.getClass(),"privateValue");
	
			Assert.assertEquals(((IntOwner)publicIntObj).getPrivateValue(),Integer.MIN_VALUE);
			Assert.assertEquals(intGS.get(publicIntObj),Integer.MIN_VALUE);
			intGS.set(publicIntObj,Integer.MAX_VALUE);
			Assert.assertEquals(((IntOwner)publicIntObj).getPrivateValue(),Integer.MAX_VALUE);
			Assert.assertEquals(intGS.get(publicIntObj),Integer.MAX_VALUE);
	
			final Object					publicStaticIntObj = new StaticIntOwner();
			final IntGetterAndSetter		intStaticGS = (IntGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(publicStaticIntObj.getClass(),"privateValue");
	
			Assert.assertEquals(((StaticIntOwner)publicStaticIntObj).getPrivateValue(),Integer.MIN_VALUE);
			Assert.assertEquals(intStaticGS.get(publicStaticIntObj),Integer.MIN_VALUE);
			intStaticGS.set(publicStaticIntObj,Integer.MAX_VALUE);
			Assert.assertEquals(((StaticIntOwner)publicStaticIntObj).getPrivateValue(),Integer.MAX_VALUE);
			Assert.assertEquals(intStaticGS.get(publicStaticIntObj),Integer.MAX_VALUE);
	
			// long test
			final Object					publicLongObj = new LongOwner();
			final LongGetterAndSetter		longGS = (LongGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(publicLongObj.getClass(),"privateValue");
	
			Assert.assertEquals(((LongOwner)publicLongObj).getPrivateValue(),Long.MIN_VALUE);
			Assert.assertEquals(longGS.get(publicLongObj),Long.MIN_VALUE);
			longGS.set(publicLongObj,Long.MAX_VALUE);
			Assert.assertEquals(((LongOwner)publicLongObj).getPrivateValue(),Long.MAX_VALUE);
			Assert.assertEquals(longGS.get(publicLongObj),Long.MAX_VALUE);
	
			final Object					publicStaticLongObj = new StaticLongOwner();
			final LongGetterAndSetter		longStaticGS = (LongGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(publicStaticLongObj.getClass(),"privateValue");
	
			Assert.assertEquals(((StaticLongOwner)publicStaticLongObj).getPrivateValue(),Long.MIN_VALUE);
			Assert.assertEquals(longStaticGS.get(publicStaticLongObj),Long.MIN_VALUE);
			longStaticGS.set(publicStaticLongObj,Long.MAX_VALUE);
			Assert.assertEquals(((StaticLongOwner)publicStaticLongObj).getPrivateValue(),Long.MAX_VALUE);
			Assert.assertEquals(longStaticGS.get(publicStaticLongObj),Long.MAX_VALUE);
	
			// short test
			final Object					publicShortObj = new ShortOwner();
			final ShortGetterAndSetter		shortGS = (ShortGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(publicShortObj.getClass(),"privateValue");
	
			Assert.assertEquals(((ShortOwner)publicShortObj).getPrivateValue(),Short.MIN_VALUE);
			Assert.assertEquals(shortGS.get(publicShortObj),Short.MIN_VALUE);
			shortGS.set(publicShortObj,Short.MAX_VALUE);
			Assert.assertEquals(((ShortOwner)publicShortObj).getPrivateValue(),Short.MAX_VALUE);
			Assert.assertEquals(shortGS.get(publicShortObj),Short.MAX_VALUE);
	
			final Object					publicStaticShortObj = new StaticShortOwner();
			final ShortGetterAndSetter		shortStaticGS = (ShortGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(publicStaticShortObj.getClass(),"privateValue");
	
			Assert.assertEquals(((StaticShortOwner)publicStaticShortObj).getPrivateValue(),Short.MIN_VALUE);
			Assert.assertEquals(shortStaticGS.get(publicStaticShortObj),Short.MIN_VALUE);
			shortStaticGS.set(publicStaticShortObj,Short.MAX_VALUE);
			Assert.assertEquals(((StaticShortOwner)publicStaticShortObj).getPrivateValue(),Short.MAX_VALUE);
			Assert.assertEquals(shortStaticGS.get(publicStaticShortObj),Short.MAX_VALUE);
		} finally {
			PureLibSettings.instance().setProperty(PureLibSettings.ALLOW_UNSAFE,"false");
		}
	}

	@Test
	public void referencedPrivateTest() throws IllegalArgumentException, NullPointerException, ContentException, NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
		PureLibSettings.instance().setProperty(PureLibSettings.ALLOW_UNSAFE,"false");
		
		try{final Method	m = GettersAndSettersFactory.class.getDeclaredMethod("prepareStatic");
			m.setAccessible(true);
			m.invoke(null);
			
			final Object						publicRefObj = new ReferencedOwner();
			@SuppressWarnings("unchecked")
			final ObjectGetterAndSetter<String>	refGS = (ObjectGetterAndSetter<String>) GettersAndSettersFactory.buildGetterAndSetter(publicRefObj.getClass(),"privateValue");
	
			Assert.assertNull(((ReferencedOwner)publicRefObj).getPrivateValue());
			Assert.assertNull(refGS.get(publicRefObj));
			refGS.set(publicRefObj,"test");
			Assert.assertEquals(((ReferencedOwner)publicRefObj).getPrivateValue(),"test");
			Assert.assertEquals(refGS.get(publicRefObj),"test");
	
			final Object						publicStaticRefObj = new StaticReferencedOwner();
			@SuppressWarnings("unchecked")
			final ObjectGetterAndSetter<String>	refStaticGS = (ObjectGetterAndSetter<String>) GettersAndSettersFactory.buildGetterAndSetter(publicStaticRefObj.getClass(),"privateValue");
	
			Assert.assertNull(((StaticReferencedOwner)publicStaticRefObj).getPrivateValue());
			Assert.assertNull(refStaticGS.get(publicStaticRefObj));
			refStaticGS.set(publicStaticRefObj,"test");
			Assert.assertEquals(((StaticReferencedOwner)publicStaticRefObj).getPrivateValue(),"test");
			Assert.assertEquals(refStaticGS.get(publicStaticRefObj),"test");
	
			final Object						publicArrayRefObj = new ArrayReferencedOwner();
			@SuppressWarnings("unchecked")
			final ObjectGetterAndSetter<int[]>	arrayRefGS = (ObjectGetterAndSetter<int[]>) GettersAndSettersFactory.buildGetterAndSetter(publicArrayRefObj.getClass(),"privateValue");
	 
			Assert.assertNull(((ArrayReferencedOwner)publicArrayRefObj).getPrivateValue());
			Assert.assertNull(arrayRefGS.get(publicArrayRefObj));
			arrayRefGS.set(publicArrayRefObj,new int[]{1,2,3});
			Assert.assertArrayEquals(((ArrayReferencedOwner)publicArrayRefObj).getPrivateValue(),new int[]{1,2,3});
			Assert.assertArrayEquals(arrayRefGS.get(publicArrayRefObj),new int[]{1,2,3});
	
			final Object						publicStaticArrayRefObj = new StaticArrayReferencedOwner();
			@SuppressWarnings("unchecked")
			final ObjectGetterAndSetter<int[]>	arrayRefStaticGS = (ObjectGetterAndSetter<int[]>) GettersAndSettersFactory.buildGetterAndSetter(publicStaticArrayRefObj.getClass(),"privateValue");
	
			Assert.assertNull(((StaticArrayReferencedOwner)publicStaticArrayRefObj).getPrivateValue());
			Assert.assertNull(arrayRefStaticGS.get(publicStaticArrayRefObj));
			arrayRefStaticGS.set(publicStaticArrayRefObj,new int[]{1,2,3});
			Assert.assertArrayEquals(((StaticArrayReferencedOwner)publicStaticArrayRefObj).getPrivateValue(),new int[]{1,2,3});
			Assert.assertArrayEquals(arrayRefStaticGS.get(publicStaticArrayRefObj),new int[]{1,2,3});
		} finally {
			PureLibSettings.instance().setProperty(PureLibSettings.ALLOW_UNSAFE,"false");
		}
	}

	@Test
	public void primitiveUnsafeTest() throws IllegalArgumentException, NullPointerException, ContentException, NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
		PureLibSettings.instance().setProperty(PureLibSettings.ALLOW_UNSAFE,"true");
		
		try{final Method	m = GettersAndSettersFactory.class.getDeclaredMethod("prepareStatic");
			m.setAccessible(true);
			m.invoke(null);
			
			final Object					publicBooleanObj = new BooleanOwner();
			final BooleanGetterAndSetter	booleanGS = (BooleanGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(publicBooleanObj.getClass(),"privateValue");
	
			Assert.assertFalse(((BooleanOwner)publicBooleanObj).getPrivateValue());
			Assert.assertFalse(booleanGS.get(publicBooleanObj));
			booleanGS.set(publicBooleanObj,true);
			Assert.assertTrue(((BooleanOwner)publicBooleanObj).getPrivateValue());
			Assert.assertTrue(booleanGS.get(publicBooleanObj));
	
			final Object					publicStaticBooleanObj = new StaticBooleanOwner();
			final BooleanGetterAndSetter	booleanStaticGS = (BooleanGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(publicStaticBooleanObj.getClass(),"privateValue");
	
			Assert.assertFalse(((StaticBooleanOwner)publicStaticBooleanObj).getPrivateValue());
			Assert.assertFalse(booleanStaticGS.get(publicStaticBooleanObj));
			booleanStaticGS.set(publicStaticBooleanObj,true);
			Assert.assertTrue(((StaticBooleanOwner)publicStaticBooleanObj).getPrivateValue());
			Assert.assertTrue(booleanStaticGS.get(publicStaticBooleanObj));
	
			// byte test
			final Object					publicByteObj = new ByteOwner();
			final ByteGetterAndSetter		byteGS = (ByteGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(publicByteObj.getClass(),"privateValue");
	
			Assert.assertEquals(((ByteOwner)publicByteObj).getPrivateValue(),Byte.MIN_VALUE);
			Assert.assertEquals(byteGS.get(publicByteObj),Byte.MIN_VALUE);
			byteGS.set(publicByteObj,Byte.MAX_VALUE);
			Assert.assertEquals(((ByteOwner)publicByteObj).getPrivateValue(),Byte.MAX_VALUE);
			Assert.assertEquals(byteGS.get(publicByteObj),Byte.MAX_VALUE);
	
			final Object					publicStaticByteObj = new StaticByteOwner();
			final ByteGetterAndSetter		byteStaticGS = (ByteGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(publicStaticByteObj.getClass(),"privateValue");
	
			Assert.assertEquals(((StaticByteOwner)publicStaticByteObj).getPrivateValue(),Byte.MIN_VALUE);
			Assert.assertEquals(byteStaticGS.get(publicStaticByteObj),Byte.MIN_VALUE);
			byteStaticGS.set(publicStaticByteObj,Byte.MAX_VALUE);
			Assert.assertEquals(((StaticByteOwner)publicStaticByteObj).getPrivateValue(),Byte.MAX_VALUE);
			Assert.assertEquals(byteStaticGS.get(publicStaticByteObj),Byte.MAX_VALUE);
			
			// char test
			final Object					publicCharObj = new CharOwner();
			final CharGetterAndSetter		charGS = (CharGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(publicCharObj.getClass(),"privateValue");
	
			Assert.assertEquals(((CharOwner)publicCharObj).getPrivateValue(),Character.MIN_VALUE);
			Assert.assertEquals(charGS.get(publicCharObj),Character.MIN_VALUE);
			charGS.set(publicCharObj,Character.MAX_VALUE);
			Assert.assertEquals(((CharOwner)publicCharObj).getPrivateValue(),Character.MAX_VALUE);
			Assert.assertEquals(charGS.get(publicCharObj),Character.MAX_VALUE);
	
			final Object					publicStaticCharObj = new StaticCharOwner();
			final CharGetterAndSetter		charStaticGS = (CharGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(publicStaticCharObj.getClass(),"privateValue");
	
			Assert.assertEquals(((StaticCharOwner)publicStaticCharObj).getPrivateValue(),Character.MIN_VALUE);
			Assert.assertEquals(charStaticGS.get(publicStaticCharObj),Character.MIN_VALUE);
			charStaticGS.set(publicStaticCharObj,Character.MAX_VALUE);
			Assert.assertEquals(((StaticCharOwner)publicStaticCharObj).getPrivateValue(),Character.MAX_VALUE);
			Assert.assertEquals(charStaticGS.get(publicStaticCharObj),Character.MAX_VALUE);
			
			// double test
			final Object					publicDoubleObj = new DoubleOwner();
			final DoubleGetterAndSetter		doubleGS = (DoubleGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(publicDoubleObj.getClass(),"privateValue");
	
			Assert.assertEquals(((DoubleOwner)publicDoubleObj).getPrivateValue(),Double.MIN_VALUE,0.0001);
			Assert.assertEquals(doubleGS.get(publicDoubleObj),Double.MIN_VALUE,0.0001);
			doubleGS.set(publicDoubleObj,Double.MAX_VALUE);
			Assert.assertEquals(((DoubleOwner)publicDoubleObj).getPrivateValue(),Double.MAX_VALUE,0.0001);
			Assert.assertEquals(doubleGS.get(publicDoubleObj),Double.MAX_VALUE,0.0001);
	
			final Object					publicStaticDoubleObj = new StaticDoubleOwner();
			final DoubleGetterAndSetter		doubleStaticGS = (DoubleGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(publicStaticDoubleObj.getClass(),"privateValue");
	
			Assert.assertEquals(((StaticDoubleOwner)publicStaticDoubleObj).getPrivateValue(),Double.MIN_VALUE,0.0001);
			Assert.assertEquals(doubleStaticGS.get(publicStaticDoubleObj),Double.MIN_VALUE,0.0001);
			doubleStaticGS.set(publicStaticDoubleObj,Double.MAX_VALUE);
			Assert.assertEquals(((StaticDoubleOwner)publicStaticDoubleObj).getPrivateValue(),Double.MAX_VALUE,0.0001);
			Assert.assertEquals(doubleStaticGS.get(publicStaticDoubleObj),Double.MAX_VALUE,0.0001);
	
			// float test
			final Object					publicFloatObj = new FloatOwner();
			final FloatGetterAndSetter		floatGS = (FloatGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(publicFloatObj.getClass(),"privateValue");
	
			Assert.assertEquals(((FloatOwner)publicFloatObj).getPrivateValue(),Float.MIN_VALUE,0.0001);
			Assert.assertEquals(floatGS.get(publicFloatObj),Float.MIN_VALUE,0.0001);
			floatGS.set(publicFloatObj,Float.MAX_VALUE);
			Assert.assertEquals(((FloatOwner)publicFloatObj).getPrivateValue(),Float.MAX_VALUE,0.0001);
			Assert.assertEquals(floatGS.get(publicFloatObj),Float.MAX_VALUE,0.0001);
	
			final Object					publicStaticFloatObj = new StaticFloatOwner();
			final FloatGetterAndSetter		floatStaticGS = (FloatGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(publicStaticFloatObj.getClass(),"privateValue");
	
			Assert.assertEquals(((StaticFloatOwner)publicStaticFloatObj).getPrivateValue(),Float.MIN_VALUE,0.0001);
			Assert.assertEquals(floatStaticGS.get(publicStaticFloatObj),Float.MIN_VALUE,0.0001);
			floatStaticGS.set(publicStaticFloatObj,Float.MAX_VALUE);
			Assert.assertEquals(((StaticFloatOwner)publicStaticFloatObj).getPrivateValue(),Float.MAX_VALUE,0.0001);
			Assert.assertEquals(floatStaticGS.get(publicStaticFloatObj),Float.MAX_VALUE,0.0001);
	
			// int test
			final Object					publicIntObj = new IntOwner();
			final IntGetterAndSetter		intGS = (IntGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(publicIntObj.getClass(),"privateValue");
	
			Assert.assertEquals(((IntOwner)publicIntObj).getPrivateValue(),Integer.MIN_VALUE);
			Assert.assertEquals(intGS.get(publicIntObj),Integer.MIN_VALUE);
			intGS.set(publicIntObj,Integer.MAX_VALUE);
			Assert.assertEquals(((IntOwner)publicIntObj).getPrivateValue(),Integer.MAX_VALUE);
			Assert.assertEquals(intGS.get(publicIntObj),Integer.MAX_VALUE);
	
			final Object					publicStaticIntObj = new StaticIntOwner();
			final IntGetterAndSetter		intStaticGS = (IntGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(publicStaticIntObj.getClass(),"privateValue");
	
			Assert.assertEquals(((StaticIntOwner)publicStaticIntObj).getPrivateValue(),Integer.MIN_VALUE);
			Assert.assertEquals(intStaticGS.get(publicStaticIntObj),Integer.MIN_VALUE);
			intStaticGS.set(publicStaticIntObj,Integer.MAX_VALUE);
			Assert.assertEquals(((StaticIntOwner)publicStaticIntObj).getPrivateValue(),Integer.MAX_VALUE);
			Assert.assertEquals(intStaticGS.get(publicStaticIntObj),Integer.MAX_VALUE);
	
			// long test
			final Object					publicLongObj = new LongOwner();
			final LongGetterAndSetter		longGS = (LongGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(publicLongObj.getClass(),"privateValue");
	
			Assert.assertEquals(((LongOwner)publicLongObj).getPrivateValue(),Long.MIN_VALUE);
			Assert.assertEquals(longGS.get(publicLongObj),Long.MIN_VALUE);
			longGS.set(publicLongObj,Long.MAX_VALUE);
			Assert.assertEquals(((LongOwner)publicLongObj).getPrivateValue(),Long.MAX_VALUE);
			Assert.assertEquals(longGS.get(publicLongObj),Long.MAX_VALUE);
	
			final Object					publicStaticLongObj = new StaticLongOwner();
			final LongGetterAndSetter		longStaticGS = (LongGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(publicStaticLongObj.getClass(),"privateValue");
	
			Assert.assertEquals(((StaticLongOwner)publicStaticLongObj).getPrivateValue(),Long.MIN_VALUE);
			Assert.assertEquals(longStaticGS.get(publicStaticLongObj),Long.MIN_VALUE);
			longStaticGS.set(publicStaticLongObj,Long.MAX_VALUE);
			Assert.assertEquals(((StaticLongOwner)publicStaticLongObj).getPrivateValue(),Long.MAX_VALUE);
			Assert.assertEquals(longStaticGS.get(publicStaticLongObj),Long.MAX_VALUE);
	
			// short test
			final Object					publicShortObj = new ShortOwner();
			final ShortGetterAndSetter		shortGS = (ShortGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(publicShortObj.getClass(),"privateValue");
	
			Assert.assertEquals(((ShortOwner)publicShortObj).getPrivateValue(),Short.MIN_VALUE);
			Assert.assertEquals(shortGS.get(publicShortObj),Short.MIN_VALUE);
			shortGS.set(publicShortObj,Short.MAX_VALUE);
			Assert.assertEquals(((ShortOwner)publicShortObj).getPrivateValue(),Short.MAX_VALUE);
			Assert.assertEquals(shortGS.get(publicShortObj),Short.MAX_VALUE);
	
			final Object					publicStaticShortObj = new StaticShortOwner();
			final ShortGetterAndSetter		shortStaticGS = (ShortGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(publicStaticShortObj.getClass(),"privateValue");
	
			Assert.assertEquals(((StaticShortOwner)publicStaticShortObj).getPrivateValue(),Short.MIN_VALUE);
			Assert.assertEquals(shortStaticGS.get(publicStaticShortObj),Short.MIN_VALUE);
			shortStaticGS.set(publicStaticShortObj,Short.MAX_VALUE);
			Assert.assertEquals(((StaticShortOwner)publicStaticShortObj).getPrivateValue(),Short.MAX_VALUE);
			Assert.assertEquals(shortStaticGS.get(publicStaticShortObj),Short.MAX_VALUE);
		} finally {
			PureLibSettings.instance().setProperty(PureLibSettings.ALLOW_UNSAFE,"false");
		}
	}

	@Test
	public void referencedUnsafeTest() throws IllegalArgumentException, NullPointerException, ContentException, NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
		PureLibSettings.instance().setProperty(PureLibSettings.ALLOW_UNSAFE,"true");
		
		try{final Method	m = GettersAndSettersFactory.class.getDeclaredMethod("prepareStatic");
			m.setAccessible(true);
			m.invoke(null);
			
			final Object						publicRefObj = new ReferencedOwner();
			@SuppressWarnings("unchecked")
			final ObjectGetterAndSetter<String>	refGS = (ObjectGetterAndSetter<String>) GettersAndSettersFactory.buildGetterAndSetter(publicRefObj.getClass(),"privateValue");
	
			Assert.assertNull(((ReferencedOwner)publicRefObj).getPrivateValue());
			Assert.assertNull(refGS.get(publicRefObj));
			refGS.set(publicRefObj,"test");
			Assert.assertEquals(((ReferencedOwner)publicRefObj).getPrivateValue(),"test");
			Assert.assertEquals(refGS.get(publicRefObj),"test");
	
			final Object						publicStaticRefObj = new StaticReferencedOwner();
			@SuppressWarnings("unchecked")
			final ObjectGetterAndSetter<String>	refStaticGS = (ObjectGetterAndSetter<String>) GettersAndSettersFactory.buildGetterAndSetter(publicStaticRefObj.getClass(),"privateValue");
	
			Assert.assertNull(((StaticReferencedOwner)publicStaticRefObj).getPrivateValue());
			Assert.assertNull(refStaticGS.get(publicStaticRefObj));
			refStaticGS.set(publicStaticRefObj,"test");
			Assert.assertEquals(((StaticReferencedOwner)publicStaticRefObj).getPrivateValue(),"test");
			Assert.assertEquals(refStaticGS.get(publicStaticRefObj),"test");
	
			final Object						publicArrayRefObj = new ArrayReferencedOwner();
			@SuppressWarnings("unchecked")
			final ObjectGetterAndSetter<int[]>	arrayRefGS = (ObjectGetterAndSetter<int[]>) GettersAndSettersFactory.buildGetterAndSetter(publicArrayRefObj.getClass(),"privateValue");
	 
			Assert.assertNull(((ArrayReferencedOwner)publicArrayRefObj).getPrivateValue());
			Assert.assertNull(arrayRefGS.get(publicArrayRefObj));
			arrayRefGS.set(publicArrayRefObj,new int[]{1,2,3});
			Assert.assertArrayEquals(((ArrayReferencedOwner)publicArrayRefObj).getPrivateValue(),new int[]{1,2,3});
			Assert.assertArrayEquals(arrayRefGS.get(publicArrayRefObj),new int[]{1,2,3});
	
			final Object						publicStaticArrayRefObj = new StaticArrayReferencedOwner();
			@SuppressWarnings("unchecked")
			final ObjectGetterAndSetter<int[]>	arrayRefStaticGS = (ObjectGetterAndSetter<int[]>) GettersAndSettersFactory.buildGetterAndSetter(publicStaticArrayRefObj.getClass(),"privateValue");
	
			Assert.assertNull(((StaticArrayReferencedOwner)publicStaticArrayRefObj).getPrivateValue());
			Assert.assertNull(arrayRefStaticGS.get(publicStaticArrayRefObj));
			arrayRefStaticGS.set(publicStaticArrayRefObj,new int[]{1,2,3});
			Assert.assertArrayEquals(((StaticArrayReferencedOwner)publicStaticArrayRefObj).getPrivateValue(),new int[]{1,2,3});
			Assert.assertArrayEquals(arrayRefStaticGS.get(publicStaticArrayRefObj),new int[]{1,2,3});
		} finally {
			PureLibSettings.instance().setProperty(PureLibSettings.ALLOW_UNSAFE,"false");
		}
	}

	@Test
	public void instantiatorAsmTest() throws IllegalArgumentException, NullPointerException, ContentException, NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException, InstantiationException {
		PureLibSettings.instance().setProperty(PureLibSettings.ALLOW_UNSAFE,"false");
		
		final Instantiator<String>	inst = GettersAndSettersFactory.buildInstantiator(String.class);
		
		Assert.assertNotNull(inst);
		final String				created = (String)inst.newInstance();
		
		Assert.assertNotNull(created);

	}		

	@Test
	public void instantiatorUnsafeTest() throws IllegalArgumentException, NullPointerException, ContentException, NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
		PureLibSettings.instance().setProperty(PureLibSettings.ALLOW_UNSAFE,"true");
	}		
	
}

