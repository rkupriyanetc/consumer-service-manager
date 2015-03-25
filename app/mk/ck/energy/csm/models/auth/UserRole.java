package mk.ck.energy.csm.models.auth;

import be.objectify.deadbolt.core.models.Role;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author RVK
 */
public class UserRole implements Role {
	
	public static final String		GUESTS_ROLE_NAME	= "GUESTS";
	
	public static final String		USER_ROLE_NAME		= "USER";
	
	public static final String		OPER_ROLE_NAME		= "OPER";
	
	public static final String		ADMIN_ROLE_NAME		= "ADMIN";
	
	public static final UserRole	GUESTS						= new UserRole( GUESTS_ROLE_NAME );
	
	public static final UserRole	USER							= new UserRole( USER_ROLE_NAME );
	
	public static final UserRole	OPER							= new UserRole( OPER_ROLE_NAME );
	
	public static final UserRole	ADMIN							= new UserRole( ADMIN_ROLE_NAME );
	
	private final String					name;
	
	private UserRole( final String name ) {
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	DBObject getDBObject() {
		return new BasicDBObject( "name", name );
	}
	
	public static UserRole getInstance( final DBObject doc ) {
		final String name = ( String )doc.get( "name" );
		if ( name.compareTo( USER_ROLE_NAME ) == 0 )
			return USER;
		else
			if ( name.compareTo( OPER_ROLE_NAME ) == 0 )
				return OPER;
			else
				if ( name.compareTo( ADMIN_ROLE_NAME ) == 0 )
					return ADMIN;
				else
					return null;
	}
	
	@Override
	public boolean equals( final Object o ) {
		if ( o == null )
			return false;
		if ( o instanceof UserRole )
			return ( ( UserRole )o ).getName().equals( this.getName() );
		return false;
	}
}
