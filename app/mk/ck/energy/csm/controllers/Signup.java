package mk.ck.energy.csm.controllers;

import static play.data.Form.form;
import mk.ck.energy.csm.model.auth.InvalidTokenException;
import mk.ck.energy.csm.model.auth.TokenAction;
import mk.ck.energy.csm.model.auth.TokenType;
import mk.ck.energy.csm.model.auth.User;
import mk.ck.energy.csm.model.auth.UserNotFoundException;
import mk.ck.energy.csm.providers.MyLoginUsernamePasswordAuthUser;
import mk.ck.energy.csm.providers.MyUsernamePasswordAuthProvider;
import mk.ck.energy.csm.providers.MyUsernamePasswordAuthProvider.MyIdentity;
import mk.ck.energy.csm.providers.MyUsernamePasswordAuthUser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.account.signup.exists;
import views.html.account.signup.no_token_or_invalid;
import views.html.account.signup.oAuthDenied;
import views.html.account.signup.password_forgot;
import views.html.account.signup.password_reset;
import views.html.account.signup.unverified;

import com.feth.play.module.pa.PlayAuthenticate;

public class Signup extends Controller {
	
	private static final Logger	LOGGER	= LoggerFactory.getLogger( Signup.class );
	
	public static class PasswordReset extends Account.PasswordChange {
		
		public PasswordReset() {}
		
		public PasswordReset( final String token ) {
			this.token = token;
		}
		
		private String	token;
		
		public String getToken() {
			return token;
		}
		
		public void setToken( final String token ) {
			this.token = token;
		}
	}
	
	private static final Form< PasswordReset >	PASSWORD_RESET_FORM	= form( PasswordReset.class );
	
	public static Result unverified() {
		com.feth.play.module.pa.controllers.Authenticate.noCache( response() );
		return ok( unverified.render() );
	}
	
	private static final Form< MyIdentity >	FORGOT_PASSWORD_FORM	= form( MyIdentity.class );
	
	public static Result forgotPassword( final String email ) {
		com.feth.play.module.pa.controllers.Authenticate.noCache( response() );
		Form< MyIdentity > form = FORGOT_PASSWORD_FORM;
		if ( email != null && !email.trim().isEmpty() )
			form = FORGOT_PASSWORD_FORM.fill( new MyIdentity( email ) );
		return ok( password_forgot.render( form ) );
	}
	
	public static Result doForgotPassword() {
		com.feth.play.module.pa.controllers.Authenticate.noCache( response() );
		final Form< MyIdentity > filledForm = FORGOT_PASSWORD_FORM.bindFromRequest();
		if ( filledForm.hasErrors() )
			// User did not fill in his/her email
			return badRequest( password_forgot.render( filledForm ) );
		else {
			// The email address given *BY AN UNKNWON PERSON* to the form - we
			// should find out if we actually have a user with this email
			// address and whether password login is enabled for him/her. Also
			// only send if the email address of the user has been verified.
			final String email = filledForm.get().getEmail();
			// We don't want to expose whether a given email address is signed
			// up, so just say an email has been sent, even though it might not
			// be true - that's protecting our user privacy.
			flash( Application.FLASH_MESSAGE_KEY, Messages.get( "playauthenticate.reset_password.message.instructions_sent", email ) );
			try {
				final User user = User.findByEmail( email );
				// yep, we have a user with this email that is active - we do
				// not know if the user owning that account has requested this
				// reset, though.
				final MyUsernamePasswordAuthProvider provider = MyUsernamePasswordAuthProvider.getProvider();
				// User exists
				if ( user.isEmailValidated() )
					provider.sendPasswordResetMailing( user, ctx() );
				// In case you actually want to let (the unknown person)
				// know whether a user was found/an email was sent, use,
				// change the flash message
				else {
					// We need to change the message here, otherwise the user
					// does not understand whats going on - we should not verify
					// with the password reset, as a "bad" user could then sign
					// up with a fake email via OAuth and get it verified by an
					// a unsuspecting user that clicks the link.
					flash( Application.FLASH_MESSAGE_KEY, Messages.get( "playauthenticate.reset_password.message.email_not_verified" ) );
					// You might want to re-send the verification email here...
					provider.sendVerifyEmailMailingAfterSignup( user, ctx() );
				}
			}
			catch ( final UserNotFoundException e ) {
				LOGGER.error( "Failed to fid user by email {} on forgot password", email, e );
			}
			return redirect( routes.Application.index() );
		}
	}
	
	/**
	 * Returns a token object if valid, null if not
	 * 
	 * @param token
	 * @param type
	 * @return
	 */
	private static TokenAction tokenIsValid( final String token, final TokenType type ) throws InvalidTokenException {
		final String cleanedToken = token == null || token.isEmpty() ? null : token.trim();
		if ( cleanedToken != null ) {
			final TokenAction ta = TokenAction.findByToken( cleanedToken, type );
			if ( ta.isValid() )
				return ta;
		}
		throw new InvalidTokenException( type, cleanedToken );
	}
	
	public static Result resetPassword( final String token ) {
		com.feth.play.module.pa.controllers.Authenticate.noCache( response() );
		try {
			tokenIsValid( token, TokenType.PASSWORD_RESET );
			return ok( password_reset.render( PASSWORD_RESET_FORM.fill( new PasswordReset( token ) ) ) );
		}
		catch ( final InvalidTokenException e ) {
			LOGGER.error( "Invalid token on resetPassword", e );
			return badRequest( no_token_or_invalid.render() );
		}
	}
	
	public static Result doResetPassword() {
		com.feth.play.module.pa.controllers.Authenticate.noCache( response() );
		final Form< PasswordReset > filledForm = PASSWORD_RESET_FORM.bindFromRequest();
		if ( filledForm.hasErrors() )
			return badRequest( password_reset.render( filledForm ) );
		else {
			final String token = filledForm.get().getToken();
			final String newPassword = filledForm.get().getPassword();
			try {
				final TokenAction ta = tokenIsValid( token, TokenType.PASSWORD_RESET );
				final User u = User.findById( ta.getUserId() );
				try {
					// Pass true for the second parameter if you want to
					// automatically create a password and the exception never to
					// happen
					u.resetPassword( new MyUsernamePasswordAuthUser( newPassword ), false );
				}
				catch ( final RuntimeException re ) {
					flash( Application.FLASH_MESSAGE_KEY, Messages.get( "message.reset_password.no_password_account" ) );
				}
				final boolean login = MyUsernamePasswordAuthProvider.getProvider().isLoginAfterPasswordReset();
				if ( login ) {
					// automatically log in
					flash( Application.FLASH_MESSAGE_KEY, Messages.get( "message.reset_password.success.auto_login" ) );
					return PlayAuthenticate.loginAndRedirect( ctx(), new MyLoginUsernamePasswordAuthUser( u.getEmail() ) );
				} else
					// send the user to the login page
					flash( Application.FLASH_MESSAGE_KEY, Messages.get( "message.reset_password.success.manual_login" ) );
				return redirect( routes.Application.login() );
			}
			catch ( final InvalidTokenException | UserNotFoundException e ) {
				LOGGER.error( "Invalid token on doing password reset", e );
				return badRequest( no_token_or_invalid.render() );
			}
		}
	}
	
	public static Result oAuthDenied( final String getProviderKey ) {
		com.feth.play.module.pa.controllers.Authenticate.noCache( response() );
		return ok( oAuthDenied.render( getProviderKey ) );
	}
	
	public static Result exists() {
		com.feth.play.module.pa.controllers.Authenticate.noCache( response() );
		return ok( exists.render() );
	}
	
	public static Result verify( final String token ) {
		com.feth.play.module.pa.controllers.Authenticate.noCache( response() );
		try {
			final TokenAction ta = tokenIsValid( token, TokenType.EMAIL_VERIFICATION );
			final User user = User.findById( ta.getUserId() );
			user.verify();
			flash( Application.FLASH_MESSAGE_KEY, Messages.get( "message.email_verification.succeeded", user.getEmail() ) );
			if ( User.getLocalUser( session() ) != null )
				return redirect( routes.Application.index() );
			else
				return redirect( routes.Application.login() );
		}
		catch ( final InvalidTokenException | UserNotFoundException e ) {
			LOGGER.error( "Invalid token on verification", e );
			return badRequest( no_token_or_invalid.render() );
		}
	}
}
