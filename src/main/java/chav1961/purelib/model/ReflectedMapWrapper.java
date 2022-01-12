package chav1961.purelib.model;


import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import chav1961.purelib.basic.CharUtils.CharSubstitutionSource;
import chav1961.purelib.basic.CharUtils.SubstitutionSource;
import chav1961.purelib.basic.GettersAndSettersFactory;
import chav1961.purelib.basic.GettersAndSettersFactory.BooleanGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.ByteGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.CharGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.DoubleGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.FloatGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.IntGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.LongGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.ShortGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.ObjectGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.GetterAndSetter;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.cdb.CompilerUtils;

public class ReflectedMapWrapper extends MappedAdamClass<String, Object> implements SubstitutionSource, CharSubstitutionSource {
	private final Object			instance;
	private final LoggerFacade		logger;
	private final BiFunction<String, Object, String>	convertor;
	private final String[]			names;
	private final Object[]			values;
	private final GetterAndSetter[]	gas;

	public ReflectedMapWrapper(final Object instance) {
		this(instance, PureLibSettings.CURRENT_LOGGER, (name, value) -> value == null ? "" : value.toString());
	}

	public ReflectedMapWrapper(final Object instance, final BiFunction<String, Object, String> convertor) {
		this(instance, PureLibSettings.CURRENT_LOGGER, convertor);
	}
	
	public ReflectedMapWrapper(final Object instance, final LoggerFacade logger, final BiFunction<String, Object, String> convertor) {
		if (instance == null) {
			throw new NullPointerException("Instance can't be null");
		}
		else if (logger == null) {
			throw new NullPointerException("Logger can't be null");
		}
		else if (convertor == null) {
			throw new NullPointerException("Convertor function can't be null");
		}
		else {
			this.instance = instance;
			this.logger = logger;
			this.convertor = convertor;

			try(final LoggerFacade	trans = logger.transaction(this.getClass().getName())) {
				final Map<String,GetterAndSetter>	fields = new HashMap<>();
				
				CompilerUtils.walkFields(instance.getClass(), (cl, f)->{
					if (!Modifier.isStatic(f.getModifiers())) {
						try{if (!fields.containsKey(f.getName())) {
								if (instance instanceof ModuleAccessor) {
									fields.put(f.getName(), GettersAndSettersFactory.buildGetterAndSetter(f.getDeclaringClass(), f.getName(), (ModuleAccessor)instance));
								}
								else {
									fields.put(f.getName(), GettersAndSettersFactory.buildGetterAndSetter(f.getDeclaringClass(), f.getName()));
								}
							}
						} catch (ContentException e) {
							trans.message(Severity.error, e, e.getLocalizedMessage());
						}
					}
				});
				
				this.names = fields.keySet().toArray(new String[fields.size()]);
				this.values = new Object[fields.size()];
				this.gas = new GetterAndSetter[fields.size()];
				for (int index= 0; index < names.length; index++) {
					gas[index] = fields.get(names[index]);
				}
				fillValues(trans);
				trans.rollback();
			}
			
		}
	}

	@Override
	public String getValue(final String key) {
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("Key can't be null or empty"); 
		}
		else if (containsKey(key)) {
			return convertor.apply(key, get(key));
		}
		else {
			return key;
		}
	}

	@Override
	public char[] getValue(final char[] data, final int from, final int to) {
		if (data == null || data.length == 0) {
			throw new IllegalArgumentException("Key data can't be null or empty array"); 
		}
		else if (from < 0 || from >= data.length) {
			throw new IllegalArgumentException("From index ["+from+"] out of range 0.."+(data.length-1)); 
		}
		else if (to < 0 || to >= data.length) {
			throw new IllegalArgumentException("To index ["+to+"] out of range 0.."+(data.length-1)); 
		}
		else if (to <= from) {
			throw new IllegalArgumentException("To index ["+to+"] must be greater than from index ["+from+"]"); 
		}
		else {
			return getValue(new String(data, from, to-from)).toCharArray();
		}
	}
	
	@Override
	protected String[] getKeys() {
		return names;
	}

	@Override
	protected Object[] getValues() {
		return values;
	}

	@Override
	protected Object setValue(final int index, final Object value) {
		Object	result = null;
		
		try{switch (gas[index].getClassType()) {
				case CompilerUtils.CLASSTYPE_BOOLEAN 	:
					result = ((BooleanGetterAndSetter)gas[index]).get(instance);
					((BooleanGetterAndSetter)gas[index]).set(instance, (Boolean)value);
					break;
				case CompilerUtils.CLASSTYPE_BYTE		:
					result = ((ByteGetterAndSetter)gas[index]).get(instance);
					((ByteGetterAndSetter)gas[index]).set(instance, (Byte)value);
					break;
				case CompilerUtils.CLASSTYPE_CHAR		:
					result = ((CharGetterAndSetter)gas[index]).get(instance);
					((CharGetterAndSetter)gas[index]).set(instance, (Character)value);
					break;
				case CompilerUtils.CLASSTYPE_DOUBLE		:
					result = ((DoubleGetterAndSetter)gas[index]).get(instance);
					((DoubleGetterAndSetter)gas[index]).set(instance, (Double)value);
					break;
				case CompilerUtils.CLASSTYPE_FLOAT		:
					result = ((FloatGetterAndSetter)gas[index]).get(instance);
					((FloatGetterAndSetter)gas[index]).set(instance, (Float)value);
					break;
				case CompilerUtils.CLASSTYPE_INT		:
					result = ((IntGetterAndSetter)gas[index]).get(instance);
					((IntGetterAndSetter)gas[index]).set(instance, (Integer)value);
					break;
				case CompilerUtils.CLASSTYPE_LONG		:
					result = ((LongGetterAndSetter)gas[index]).get(instance);
					((LongGetterAndSetter)gas[index]).set(instance, (Long)value);
					break;
				case CompilerUtils.CLASSTYPE_SHORT		:
					result = ((ShortGetterAndSetter)gas[index]).get(instance);
					((ShortGetterAndSetter)gas[index]).set(instance, (Short)value);
					break;
				case CompilerUtils.CLASSTYPE_REFERENCE	:
					result = ((ObjectGetterAndSetter<Object>)gas[index]).get(instance);
					((ObjectGetterAndSetter<Object>)gas[index]).set(instance, value);
					break;
				default :
					throw new UnsupportedOperationException("Class type ["+gas[index].getClassType()+"] is not supported yet");
			}
			values[index] = value;
		} catch (ContentException e) {
			logger.message(Severity.error, e, e.getLocalizedMessage());
		}
		return result;
	}

	private void fillValues(final LoggerFacade logger) {
		for(int index=0; index < values.length; index++) {
			try{switch (gas[index].getClassType()) {
					case CompilerUtils.CLASSTYPE_BOOLEAN 	:
						values[index] = ((BooleanGetterAndSetter)gas[index]).get(instance);
						break;
					case CompilerUtils.CLASSTYPE_BYTE		:
						values[index] = ((ByteGetterAndSetter)gas[index]).get(instance);
						break;
					case CompilerUtils.CLASSTYPE_CHAR		:
						values[index] = ((CharGetterAndSetter)gas[index]).get(instance);
						break;
					case CompilerUtils.CLASSTYPE_DOUBLE		:
						values[index] = ((DoubleGetterAndSetter)gas[index]).get(instance);
						break;
					case CompilerUtils.CLASSTYPE_FLOAT		:
						values[index] = ((FloatGetterAndSetter)gas[index]).get(instance);
						break;
					case CompilerUtils.CLASSTYPE_INT		:
						values[index] = ((IntGetterAndSetter)gas[index]).get(instance);
						break;
					case CompilerUtils.CLASSTYPE_LONG		:
						values[index] = ((LongGetterAndSetter)gas[index]).get(instance);
						break;
					case CompilerUtils.CLASSTYPE_SHORT		:
						values[index] = ((ShortGetterAndSetter)gas[index]).get(instance);
						break;
					case CompilerUtils.CLASSTYPE_REFERENCE	:
						values[index] = ((ObjectGetterAndSetter<?>)gas[index]).get(instance);
						break;
					default :
						throw new UnsupportedOperationException("Class type ["+gas[index].getClassType()+"] is not supported yet");
				}
			} catch (ContentException e) {
				logger.message(Severity.error, e, e.getLocalizedMessage());
			}
		}
	}
	
	@Override
	public String toString() {
		return "ReflectedMapWrapper [instance=" + instance + ", names=" + Arrays.toString(names) + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((instance == null) ? 0 : instance.hashCode());
		result = prime * result + Arrays.hashCode(names);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ReflectedMapWrapper other = (ReflectedMapWrapper) obj;
		if (instance == null) {
			if (other.instance != null) return false;
		} else if (!instance.equals(other.instance)) return false;
		if (!Arrays.equals(names, other.names)) return false;
		return true;
	}
}
