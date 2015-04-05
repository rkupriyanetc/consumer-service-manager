package mk.ck.energy.csm.security;

import mk.ck.energy.csm.model.auth.User;
import mk.ck.energy.csm.model.auth.UserNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.libs.F;
import play.libs.F.Promise;
import play.mvc.Http;
import play.mvc.Http.Context;
import play.mvc.Result;
import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.java.AbstractDeadboltHandler;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUserIdentity;

public class MyDeadboltHandler extends AbstractDeadboltHandler {
	
	private static final Logger	LOGGER	= LoggerFactory.getLogger( MyDeadboltHandler.class );
	
	@Override
	public F.Promise< Result > beforeAuthCheck( final Http.Context context ) {
		if ( PlayAuthenticate.isLoggedIn( context.session() ) )
			// user is logged in
			return F.Promise.pure( null );
		else {
			LOGGER.trace( "User is not logged in" );
			// user is not logged in
			// call this if you want to redirect your visitor to the page that
			// was requested before sending him to the login page
			// if you don't call this, the user will get redirected to the page
			// defined by your resolver
			final String originalUrl = PlayAuthenticate.storeOriginalUrl( context );
			context.flash().put( "error", "You need to log in first, to view '" + originalUrl + "'" );
			return F.Promise.promise( ( ) -> redirect( PlayAuthenticate.getResolver().login() ) );
		}
	}
	
	@Override
	public Promise< Subject > getSubject( final Context context ) {
		final AuthUserIdentity u = PlayAuthenticate.getUser( context );
		if ( u == null )
			return F.Promise.pure( null );
		else {
			// Caching might be a good idea here
			LOGGER.info( "Need to find user by identity {}", u );
			return F.Promise.promise( ( ) -> getSubject( u ) );
		}
	}
	
	protected Subject getSubject( final AuthUserIdentity identity ) {
		LOGGER.debug( "Finding subject by identity {}", identity );
		try {
			// The user implementation class should be Subject
			return User.findByAuthUserIdentity( identity );
		}
		catch ( final UserNotFoundException e ) {
			LOGGER.error( "Could not get subject from context identity {}. Exc: {}", identity, e );
			return null;
		}
	}
	
	@Override
	public F.Promise< Result > onAuthFailure( final Http.Context context, final String content ) {
		// if the user has a cookie with a valid user and the local user has
		// been deactivated/deleted in between, it is possible that this gets
		// shown. You might want to consider to sign the user out in this case.
		return F.Promise.promise( ( ) -> forbidden( "Forbidden" ) );
	}
}
