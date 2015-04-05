package mk.ck.energy.csm.service;

import mk.ck.energy.csm.model.auth.User;
import mk.ck.energy.csm.model.auth.UserNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Application;

import com.feth.play.module.pa.service.UserServicePlugin;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;

public class MyUserServicePlugin extends UserServicePlugin {
	
	private static final Logger	LOGGER	= LoggerFactory.getLogger( MyUserServicePlugin.class );
	
	public MyUserServicePlugin( final Application app ) {
		super( app );
	}
	
	@Override
	public Object save( final AuthUser authUser ) {
		final boolean isLinked = User.existsByAuthUserIdentity( authUser );
		if ( !isLinked )
			return User.create( authUser ).getId();
		else
			// we have this user already, so return null
			return null;
	}
	
	@Override
	public Object getLocalIdentity( final AuthUserIdentity identity ) {
		// For production: Caching might be a good idea here...
		// ...and dont forget to sync the cache when users get deactivated/deleted
		try {
			final User u = User.findByAuthUserIdentity( identity );
			LOGGER.trace( "Getting local identity {}", u.getId() );
			return u.getId();
		}
		catch ( final UserNotFoundException e ) {
			return null;
		}
	}
	
	@Override
	public AuthUser merge( final AuthUser newUser, final AuthUser oldUser ) {
		if ( !oldUser.equals( newUser ) )
			User.merge( oldUser, newUser );
		return oldUser;
	}
	
	@Override
	public AuthUser link( final AuthUser oldUser, final AuthUser newUser ) {
		User.addLinkedAccount( oldUser, newUser );
		return newUser;
	}
	
	@Override
	public AuthUser update( final AuthUser knownUser ) {
		LOGGER.trace( "Updating the user {}", knownUser );
		try {
			final User user = User.findByAuthUserIdentity( knownUser );
			// User logged in again, bump last login date
			user.updateLastLoginDate();
		}
		catch ( final UserNotFoundException e ) {
			LOGGER.error( "Failed to locate user {} for login update", knownUser, e );
		}
		return knownUser;
	}
}
