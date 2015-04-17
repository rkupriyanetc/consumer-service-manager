package mk.ck.energy.csm.model.auth;

import org.bson.Document;

import be.objectify.deadbolt.core.models.Role;

/**
 * @author RVK
 */
public class UserRole implements Role {
	
	public static final String	GUEST_ROLE_NAME			= "GUEST";
	
	public static final String	USER_ROLE_NAME			= "USER";
	
	public static final String	OPER_ROLE_NAME			= "OPER";
	
	public static final String	ADMIN_ROLE_NAME			= "ADMIN";
	
	public static final Role		GUEST								= new UserRole( GUEST_ROLE_NAME );
	
	public static final Role		USER								= new UserRole( USER_ROLE_NAME );
	
	public static final Role		OPER								= new UserRole( OPER_ROLE_NAME );
	
	public static final Role		ADMIN								= new UserRole( ADMIN_ROLE_NAME );
	
	private static final String	DB_FIELD_ROLE_NAME	= "name";
	
	private final String				name;
	
	private UserRole( final String name ) {
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public static Role getInstance( final Document role ) {
		return getInstance( role.getString( DB_FIELD_ROLE_NAME ) );
	}
	
	public static Role getInstance( final String roleName ) {
		switch ( roleName ) {
			case USER_ROLE_NAME :
				return USER;
			case OPER_ROLE_NAME :
				return OPER;
			case ADMIN_ROLE_NAME :
				return ADMIN;
			case GUEST_ROLE_NAME :
				return GUEST;
			default :
				throw new IllegalArgumentException( "Role name is null" );
		}
	}
	
	Document getDocument() {
		return new Document( DB_FIELD_ROLE_NAME, name );
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
