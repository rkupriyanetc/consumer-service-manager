package mk.ck.energy.csm.model.auth;

import org.bson.Document;
import org.bson.conversions.Bson;

import be.objectify.deadbolt.core.models.Permission;

import com.mongodb.client.model.Filters;

/**
 * Initial version based on work by Steve Chaloner (steve@objectify.be) for
 * Deadbolt2
 */
public class UserPermission implements Permission {
	
	private static final String	DB_FIELD_VALUE	= "value";
	
	private final String				value;
	
	private UserPermission( final String value ) {
		this.value = value;
	}
	
	public static Permission getInstance( final String value ) {
		return new UserPermission( value );
	}
	
	public static Permission getInstance( final Document document ) {
		return new UserPermission( document.getString( DB_FIELD_VALUE ) );
	}
	
	@Override
	public String getValue() {
		return value;
	}
	
	Bson getDocument() {
		return Filters.eq( DB_FIELD_VALUE, value );
	}
}
