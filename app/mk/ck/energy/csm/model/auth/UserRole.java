package mk.ck.energy.csm.model.auth;

import org.bson.Document;

import be.objectify.deadbolt.core.models.Role;

/**
 * @author RVK
 */
public class UserRole implements Role {
	
	private static final String		DB_FIELD_ROLE_NAME	= "name";
	
	public static final String		GUEST_ROLE_NAME			= "GUEST";
	
	public static final String		USER_ROLE_NAME			= "USER";
	
	public static final String		OPER_ROLE_NAME			= "OPER";
	
	public static final String		ADMIN_ROLE_NAME			= "ADMIN";
	
	public static final UserRole	GUEST								= new UserRole( GUEST_ROLE_NAME );
	
	public static final UserRole	USER								= new UserRole( USER_ROLE_NAME );
	
	public static final UserRole	OPER								= new UserRole( OPER_ROLE_NAME );
	
	public static final UserRole	ADMIN								= new UserRole( ADMIN_ROLE_NAME );
	
	private final String					name;
	
	private UserRole( final String name ) {
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	Document getDocument() {
		return new Document( DB_FIELD_ROLE_NAME, name );
	}
	
	public static UserRole getInstance( final Document doc ) {
		final String name = ( String )doc.get( DB_FIELD_ROLE_NAME );
		if ( name.compareTo( USER_ROLE_NAME ) == 0 )
			return USER;
		else
			if ( name.compareTo( OPER_ROLE_NAME ) == 0 )
				return OPER;
			else
				if ( name.compareTo( ADMIN_ROLE_NAME ) == 0 )
					return ADMIN;
				else
					if ( name.compareTo( GUEST_ROLE_NAME ) == 0 )
						return GUEST;
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
