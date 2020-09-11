package chav1961.purelib.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import chav1961.purelib.basic.GettersAndSettersFactory;
import chav1961.purelib.basic.GettersAndSettersFactory.BooleanGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.ByteGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.CharGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.DoubleGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.FloatGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.GetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.IntGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.LongGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.ObjectGetterAndSetter;
import chav1961.purelib.basic.GettersAndSettersFactory.ShortGetterAndSetter;
import chav1961.purelib.basic.SimpleURLClassLoader;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.ContentNodeFilter;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.sql.interfaces.ORMProvider3;
import chav1961.purelib.streams.char2byte.CompilerUtils;

public abstract class SimpleResultSetProvider implements ORMProvider3, ModuleAccessor {
	private final ResultSet			rs;
	private final GetterAndSetter[]	gas;
	
	public SimpleResultSetProvider(final ResultSet rs, final SimpleURLClassLoader loader) throws ContentException, SQLException {
		if (rs != null) {
			throw new NullPointerException("Result set can't be null");
		}
		else if (loader != null) {
			throw new NullPointerException("Loader can't be null");
		}
		else {
			
			try{final Set<String>			names = SQLUtils.getResutSetColumnNames(rs,true);
				final ContentNodeMetadata	classMeta = new ContentNodeFilter(ContentModelFactory.forOrdinalClass(this.getClass()).getRoot(),(n)->names.contains(n.getName().toUpperCase()));
				final List<GetterAndSetter>	list = new ArrayList<>();

				for (ContentNodeMetadata item : classMeta) {
					GettersAndSettersFactory.buildGetterAndSetter(this.getClass(),item.getName(),this,loader);
				}
				this.rs = rs;
				this.gas = list.toArray(new GetterAndSetter[list.size()]);
			} catch (LocalizationException e) {
				throw new ContentException(e.getLocalizedMessage(),e);
			}
		}
	}

	@Override
	public abstract void allowUnnamedModuleAccess(Module... unnamedModules);
	
	@Override
	public void close() throws SQLException {
		rs.close();
	}

	@Override
	public void insert() throws SQLException {
		rs.insertRow();
		rs.moveToInsertRow();
		update();
		rs.moveToCurrentRow();
	}

	@Override
	public void update() throws SQLException {
		try{
			for (int index = 0; index < gas.length; index++) {
				switch (gas[index].getClassType()) {
					case CompilerUtils.CLASSTYPE_BOOLEAN 	:
						rs.updateBoolean(index+1,((BooleanGetterAndSetter)gas[index]).get(this));
						break;
					case CompilerUtils.CLASSTYPE_BYTE 		:
						rs.updateByte(index+1,((ByteGetterAndSetter)gas[index]).get(this));
						break;
					case CompilerUtils.CLASSTYPE_CHAR 		:
						rs.updateInt(index+1,((CharGetterAndSetter)gas[index]).get(this));
						break;
					case CompilerUtils.CLASSTYPE_DOUBLE 	:
						rs.updateDouble(index+1,((DoubleGetterAndSetter)gas[index]).get(this));
						break;
					case CompilerUtils.CLASSTYPE_FLOAT 		:
						rs.updateFloat(index+1,((FloatGetterAndSetter)gas[index]).get(this));
						break;
					case CompilerUtils.CLASSTYPE_INT 		:
						rs.updateInt(index+1,((IntGetterAndSetter)gas[index]).get(this));
						break;
					case CompilerUtils.CLASSTYPE_LONG 		:
						rs.updateLong(index+1,((LongGetterAndSetter)gas[index]).get(this));
						break;
					case CompilerUtils.CLASSTYPE_SHORT 		:	
						rs.updateShort(index+1,((ShortGetterAndSetter)gas[index]).get(this));
						break;
					case CompilerUtils.CLASSTYPE_REFERENCE	:
						rs.updateObject(index+1,((ObjectGetterAndSetter)gas[index]).get(this));
						break;
					default :
						throw new UnsupportedOperationException("Class type ["+gas[index].getClassType()+"] is not supported yet"); 
				}
			}
			rs.updateRow();
		} catch (ContentException e) {
			throw new SQLException(e.getLocalizedMessage(),e);
		}
	}

	@Override
	public void delete() throws SQLException {
		rs.deleteRow();
	}

	@Override
	public void refresh() throws SQLException {
		try{
			for (int index = 0; index < gas.length; index++) {
				switch (gas[index].getClassType()) {
					case CompilerUtils.CLASSTYPE_BOOLEAN 	:
						((BooleanGetterAndSetter)gas[index]).set(this,rs.getBoolean(index+1));
						break;
					case CompilerUtils.CLASSTYPE_BYTE 		:
						((ByteGetterAndSetter)gas[index]).set(this,rs.getByte(index+1));
						break;
					case CompilerUtils.CLASSTYPE_CHAR 		:
						((CharGetterAndSetter)gas[index]).set(this,(char)rs.getInt(index+1));
						break;
					case CompilerUtils.CLASSTYPE_DOUBLE 	:
						((DoubleGetterAndSetter)gas[index]).set(this,rs.getDouble(index+1));
						break;
					case CompilerUtils.CLASSTYPE_FLOAT 		:
						((FloatGetterAndSetter)gas[index]).set(this,rs.getFloat(index+1));
						break;
					case CompilerUtils.CLASSTYPE_INT 		:
						((IntGetterAndSetter)gas[index]).set(this,rs.getInt(index+1));
						break;
					case CompilerUtils.CLASSTYPE_LONG 		:
						((LongGetterAndSetter)gas[index]).set(this,rs.getLong(index+1));
						break;
					case CompilerUtils.CLASSTYPE_SHORT 		:	
						((ShortGetterAndSetter)gas[index]).set(this,rs.getShort(index+1));
						break;
					case CompilerUtils.CLASSTYPE_REFERENCE	:
						((ObjectGetterAndSetter)gas[index]).set(this,rs.getObject(index+1));
						break;
					default :
						throw new UnsupportedOperationException("Class type ["+gas[index].getClassType()+"] is not supported yet"); 
				}
			}
		} catch (ContentException e) {
			throw new SQLException(e.getLocalizedMessage(),e);
		}
	}

	@Override
	public boolean next() throws SQLException {
		if (rs.next()) {
			refresh();
			return true;
		}
		else {
			return false;
		}
	}
}
