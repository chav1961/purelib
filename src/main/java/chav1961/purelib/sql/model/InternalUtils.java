package chav1961.purelib.sql.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import chav1961.purelib.basic.GettersAndSettersFactory;
import chav1961.purelib.basic.SimpleURLClassLoader;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.sql.interfaces.DomainType;

class InternalUtils {

	static FieldAccessor[] buildFieldAccessorList(final ContentNodeMetadata databaseMeta, final ContentNodeMetadata classMeta, final SimpleURLClassLoader loader) throws ContentException {
		final Map<String,ContentNodeMetadata[]>	pairs = new HashMap<>();
		
		for (ContentNodeMetadata item : databaseMeta) {
			if (!item.getName().endsWith("/primaryKey")) {
				final String	upperName = item.getName().toUpperCase();
				
				pairs.put(upperName, new ContentNodeMetadata[] {item, null});
			}
		}
		for (ContentNodeMetadata item : classMeta) {
			final String	upperName = item.getName().toUpperCase();
			
			if (pairs.containsKey(upperName)) {
				pairs.get(upperName)[1] = item;
			}
		}

		final List<FieldAccessor>	result = new ArrayList<>();

		for (Entry<String, ContentNodeMetadata[]> item : pairs.entrySet()) {
			if (item.getValue()[1] != null) {
				final Map<String,String[]>	parameters = URIUtils.parseQuery(URIUtils.extractQueryFromURI(item.getValue()[0].getApplicationPath())); 
				
				result.add(new FieldAccessor(item.getValue()[0], 
									item.getValue()[1], 
									GettersAndSettersFactory.buildGetterAndSetter(item.getValue()[1].getApplicationPath(), loader), 
									DomainType.valueOf(parameters.get("type")[0]), 
									item.getValue()[0].getFormatAssociated() != null && item.getValue()[0].getFormatAssociated().isMandatory(), 
									parameters.containsKey("pkSeq") ? Integer.valueOf(parameters.get("pkSeq")[0]) : 0)
				);
			}
			else if (item.getValue()[0].getFormatAssociated() != null && item.getValue()[0].getFormatAssociated().isMandatory()) {
				throw new ContentException("Field name ["+item.getKey()+"] is mandatory in the database table ["+databaseMeta.getName()+"], but doesn't have appropriative field in the class ["+classMeta.getType().getCanonicalName()+"]");
			}
		}
		
		return result.toArray(new FieldAccessor[result.size()]);
	}
}
