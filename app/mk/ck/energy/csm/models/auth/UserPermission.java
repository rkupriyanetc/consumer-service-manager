package mk.ck.energy.csm.models.auth;

import be.objectify.deadbolt.core.models.Permission;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * Initial version based on work by Steve Chaloner (steve@objectify.be) for
 * Deadbolt2
 */
public class UserPermission implements Permission {
	
	private final String	value;
	
	private UserPermission( final String value ) {
		this.value = value;
	}
	
	public static UserPermission getInstance( final DBObject doc ) {
		return new UserPermission( ( String )doc.get( "value" ) );
	}
	
	public static UserPermission getInstance( final String value ) {
		return new UserPermission( value );
	}
	
	@Override
	public String getValue() {
		return value;
	}
	
	DBObject getDBObject() {
		return new BasicDBObject( "value", value );
	}
}
