package chav1961.purelib.sql.junit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.GettersAndSettersFactory;
import chav1961.purelib.basic.GettersAndSettersFactory.GetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.IntGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.LongGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.ObjectGetterAndSetter;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.sql.junit.interfaces.AsField;
import chav1961.purelib.sql.junit.interfaces.AsFunction;
import chav1961.purelib.sql.junit.interfaces.AsPackage;
import chav1961.purelib.sql.junit.interfaces.AsProcedure;
import chav1961.purelib.sql.junit.interfaces.AsSequence;
import chav1961.purelib.sql.junit.interfaces.AsTable;

public class JUnitDriver implements Driver {
	private static final URI					JUNIT_URI = URI.create("jdbc:junit:/");
	private static final DriverPropertyInfo[]	EMPTY_PROPERTIES = new DriverPropertyInfo[0];
	
	private final SyntaxTreeInterface<JUnitEntityImpl>	names = new AndOrTree<>();
	
	public JUnitDriver(final Object pseudoDatabase) throws ContentException {
		if (pseudoDatabase == null) {
			throw new NullPointerException("Pseudo database object can't be null");
		}
		else {
			final Class<?>	cl = pseudoDatabase.getClass();
			boolean			wasTable = false;
			
			for (Field item : cl.getFields()) {
				if (item.isAnnotationPresent(AsSequence.class)) {
					makeSequence(cl,item);
				}
				else if (item.isAnnotationPresent(AsTable.class)) {
					makeTable(cl,item);
					wasTable = false;
				}
				else if (item.isAnnotationPresent(AsPackage.class)) {
					makePackage(item);
				}
			}
			for (Method item : cl.getMethods()) {
				if (item.isAnnotationPresent(AsFunction.class)) {
					makeFunction(item);
				}
				else if (item.isAnnotationPresent(AsProcedure.class)) {
					makeProcedure(item);
				}
			}
			if (!wasTable) {
				throw new IllegalArgumentException("No any @AsTable annotations found for pseudoDatabase instance. At least one must present!");
			}
		}
	}

	@Override
	public boolean acceptsURL(final String url) throws SQLException {
		return Utils.canServeURI(URI.create(url),JUNIT_URI);
	}

	@Override
	public int getMajorVersion() {
		return 0;
	}

	@Override
	public int getMinorVersion() {
		return 0;
	}

	@Override
	public boolean jdbcCompliant() {
		return false;
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return PureLibSettings.logger;
	}
	
	@Override
	public Connection connect(final String url, final Properties info) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DriverPropertyInfo[] getPropertyInfo(final String url, final Properties info) throws SQLException {
		return EMPTY_PROPERTIES;
	}

	private void makeSequence(final Class<?> owner, final Field item) throws ContentException {
		final Class<?>	type = item.getType();
		
		if (type.isArray() || type.isEnum()) {
			throw new IllegalArgumentException("Field ["+item.getName()+"] annotated with @AsSequence can't be array or enumeration"); 
		}
		else if (type.isPrimitive()) {
			if (!(type == int.class || type == long.class)) {
				throw new IllegalArgumentException("Field ["+item.getName()+"] annotated with @AsSequence can be public int, long, AtomicInt or AtomicLong only"); 
			}
			else if (type == int.class) {
				names.placeName(item.getName(),new JUnitSequenceImpl(item.getAnnotation(AsSequence.class).value(),(IntGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(owner,item.getName())));
			}
			else {
				names.placeName(item.getName(),new JUnitSequenceImpl(item.getAnnotation(AsSequence.class).value(),(LongGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(owner,item.getName())));
			}
		}
		else if (!(AtomicInteger.class.isAssignableFrom(type) || AtomicLong.class.isAssignableFrom(type))) {
			throw new IllegalArgumentException("Field ["+item.getName()+"] annotated with @AsSequence can be public int, long, AtomicInt or AtomicLong only"); 
		}
		else if (AtomicInteger.class.isAssignableFrom(type)) {
			names.placeName(item.getName(),new JUnitSequenceImpl(item.getAnnotation(AsSequence.class).value(),(ObjectGetterAndSetter<AtomicInteger>) GettersAndSettersFactory.buildGetterAndSetter(owner,item.getName()),AtomicInteger.class));
		}
		else {
			names.placeName(item.getName(),new JUnitSequenceImpl(item.getAnnotation(AsSequence.class).value(),(ObjectGetterAndSetter<AtomicLong>) GettersAndSettersFactory.buildGetterAndSetter(owner,item.getName()),AtomicLong.class));
		}
	}

	private void makeTable(final Class<?> owner, Field item) throws ContentException {
		// TODO Auto-generated method stub
		final Class<?>	type = item.getType();
		
		if (!type.isArray() || !type.getComponentType().isPrimitive()) {
			throw new IllegalArgumentException("Field ["+item.getName()+"] annotated with @AsTable must be public array of referenced type"); 
		}
		else {
			final ObjectGetterAndSetter	gas = (ObjectGetterAndSetter) GettersAndSettersFactory.buildGetterAndSetter(owner,item.getName());	
			final Class<?>				rec = type.getComponentType();
			final List<String[]>		fieldNames = new ArrayList<>();
			final List<GetterAndSetter>	gasList = new ArrayList<>();
			boolean						wasField = false;
			
			for (Field f : rec.getFields()) {
				if (f.isAnnotationPresent(AsField.class)) {
					fieldNames.add(new String[]{f.getName(),f.getAnnotation(AsField.class).name(),f.getAnnotation(AsField.class).type()});
					gasList.add(GettersAndSettersFactory.buildGetterAndSetter(rec,f.getName()));
					wasField = true;
				}
			}
			if (!wasField) {
				throw new IllegalArgumentException("No any fields annotated with @AsField found in the class ["+type.getCanonicalName()+"] of field ["+item.getName()+"]");
			}
			else {
				
			}
		}
	}

	private void makePackage(Field item) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented yet");
	}

	private void makeFunction(Method item) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented yet");
	}

	private void makeProcedure(Method item) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not implemented yet");
	}
}
