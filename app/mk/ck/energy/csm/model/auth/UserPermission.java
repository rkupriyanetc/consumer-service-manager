package mk.ck.energy.csm.model.auth;

import org.bson.Document;

import be.objectify.deadbolt.core.models.Permission;

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
	
	public static UserPermission getInstance( final Document doc ) {
		return new UserPermission( ( String )doc.get( DB_FIELD_VALUE ) );
	}
	
	public static UserPermission getInstance( final String value ) {
		return new UserPermission( value );
	}
	
	@Override
	public String getValue() {
		return value;
	}
	
	Document getDocument() {
		return new Document( DB_FIELD_VALUE, value );
	}
}
