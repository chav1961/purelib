package chav1961.purelib.sql.content;

import java.io.IOException;
import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.ServiceLoader;

import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.sql.AbstractContent;
import chav1961.purelib.sql.AbstractReadOnlyResultSet;
import chav1961.purelib.sql.AbstractResultSetMetaData;
import chav1961.purelib.sql.RsMetaDataElement;
import chav1961.purelib.sql.SQLUtils;
import chav1961.purelib.sql.interfaces.ResultSetContentParser;

public class ResultSetFactory {
	public static final String			RESULTSET_PARSERS_SCHEMA = "rsps";
	
	public static ResultSet buildResultSet(final Statement owner, final URI resource, final int resultSetType) throws IOException {
		final String	query;
		
		if (resource == null) {
			throw new NullPointerException("Resource URI can't be null");
		}
		else if (!RESULTSET_PARSERS_SCHEMA.equals(resource.getScheme())) {
			throw new IllegalArgumentException("Resource scheme ["+resource.getScheme()+"] is not ["+RESULTSET_PARSERS_SCHEMA+"]");
		}
		else if ((query = resource.getRawQuery()) == null) {
			throw new IllegalArgumentException("Resource ["+resource+"]: query string is missing in the URI!");
		}
		else {
			try{final URI		source = URI.create(resource.getRawSchemeSpecificPart());
			
				for (ResultSetContentParser item : ServiceLoader.load(ResultSetContentParser.class)) {
					if (item.canServe(source)) {
						final Hashtable<String,String[]>	content = Utils.parseQuery(query);
						final RsMetaDataElement[]			fields = buildMetadataFromQueryString(query,item.filter(content));
						final ResultSetContentParser		parser = item.newInstance(URI.create(source.getRawSchemeSpecificPart()).toURL()
																	,resultSetType
																	,fields
																	,buildOptions(content,item.filter(content)));
	
						return new AbstractReadOnlyResultSet(
								new AbstractResultSetMetaData(fields,true) {
									@Override public String getTableName(int column) throws SQLException {return null;}
									@Override public String getSchemaName(int column) throws SQLException {return null;}
									@Override public String getCatalogName(int column) throws SQLException {return null;}
								},resultSetType) {
							
							@Override
							public Statement getStatement() throws SQLException {
								return owner;
							}
							
							@Override
							protected AbstractContent getContent() {
								return parser.getAccessContent();
							}
						};
					}
				}
				throw new IOException("No any parser were found for ["+resource+"] request");
			} catch (SyntaxException e) {
				throw new IOException(e.getMessage(),e);
			}
		}
	}
	
	static RsMetaDataElement[] buildMetadataFromQueryString(final String query, final Hashtable<String,String[]> content) throws SyntaxException {
		final List<String>					names = new ArrayList<>();
		final RsMetaDataElement[]			result;
		
		for (String item : CharUtils.split(query,'&')) {
			if (content.containsKey(item)) {
				names.add(CharUtils.split(item.trim(),'=')[0].trim());
			}
		}
		
		result = new RsMetaDataElement[names.size()];
		for (int index = 0; index < result.length; index++) {
			String	type = content.get(names.get(index))[0].trim();
			int		location, typeId;
			
			if ((location = type.indexOf('(')) == -1) {
				typeId = SQLUtils.typeIdByTypeName(type);
				
				if (typeId == SQLUtils.UNKNOWN_TYPE) {
					throw new SyntaxException(0,0,"Unknown data type ["+type+"] for field ["+names.get(index)+"]"); 
				}
				else {
					result[index] = new RsMetaDataElement(names.get(index),"",type,typeId,0,0);
				}
			}
			else {
				String	tail = type.substring(location+1);
				int		len, frac;
				
				type = type.substring(0,location);
				if (tail.charAt(tail.length()-1) != ')') {
					throw new SyntaxException(0,0,"Missing close bracket in the data type for field ["+names.get(index)+"]"); 
				}
				else {
					tail = tail.substring(0,tail.length()-1);
				}
				
				typeId = SQLUtils.typeIdByTypeName(type.trim());
				
				if (typeId == SQLUtils.UNKNOWN_TYPE) {
					throw new SyntaxException(0,0,"Unknown data type ["+type+"] for field ["+names.get(index)+"]"); 
				}
				if ((location = tail.indexOf(',')) == -1) {
					try{len = Integer.valueOf(tail.trim());
						frac = 0;
					} catch (NumberFormatException exc) {
						throw new SyntaxException(0,0,"Illegal number ["+tail.trim()+"] in the length for field ["+names.get(index)+"]");
					}
				}
				else {
					try{final String[] parts = CharUtils.split(tail.trim(),',');
						len = Integer.valueOf(parts[0].trim());
						frac =  Integer.valueOf(parts[1].trim());
					} catch (NumberFormatException exc) {
						throw new SyntaxException(0,0,"Illegal number(s) ["+tail.trim()+"] in the length and/or fractional for field ["+names.get(index)+"]");
					}
				}
				result[index] = new RsMetaDataElement(names.get(index),"",type,SQLUtils.typeIdByTypeName(type),len,frac);
			}
		}
		return result;
	}
	
	static SubstitutableProperties buildOptions(final Hashtable<String,String[]> source, final Hashtable<String,String[]> excludes) {
		final Properties	props = new Properties();
		
		for (Entry<String, String[]> item : source.entrySet()) {
			if (!excludes.containsKey(item.getKey())) {
				props.setProperty(item.getKey(),item.getValue()[0]);
			}
		}
		return new SubstitutableProperties(props);
	}
}
