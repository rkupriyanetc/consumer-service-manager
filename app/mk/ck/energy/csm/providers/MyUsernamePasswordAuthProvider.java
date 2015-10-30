package mk.ck.energy.csm.providers;

import static play.data.Form.form;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.feth.play.module.mail.Mailer.Mail.Body;
import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import com.google.inject.Inject;

import mk.ck.energy.csm.controllers.routes;
import mk.ck.energy.csm.model.auth.LinkedAccount;
import mk.ck.energy.csm.model.auth.TokenAction;
import mk.ck.energy.csm.model.auth.TokenType;
import mk.ck.energy.csm.model.auth.User;
import mk.ck.energy.csm.model.auth.UserNotFoundException;
import play.Application;
import play.data.Form;
import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Required;
import play.i18n.Lang;
import play.i18n.Messages;
import play.mvc.Call;
import play.mvc.Http.Context;

public class MyUsernamePasswordAuthProvider extends
		UsernamePasswordAuthProvider< String, MyLoginUsernamePasswordAuthUser, MyUsernamePasswordAuthUser, MyUsernamePasswordAuthProvider.MyLogin, MyUsernamePasswordAuthProvider.MySignup > {
		
	private static final Logger	LOGGER																			= LoggerFactory
			.getLogger( MyUsernamePasswordAuthProvider.class );
			
	private static final String	SETTING_KEY_VERIFICATION_LINK_SECURE				= SETTING_KEY_MAIL + "." + "verificationLink.secure";
																																					
	private static final String	SETTING_KEY_PASSWORD_RESET_LINK_SECURE			= SETTING_KEY_MAIL + "." + "passwordResetLink.secure";
																																					
	private static final String	SETTING_KEY_LINK_LOGIN_AFTER_PASSWORD_RESET	= "loginAfterPasswordReset";
																																					
	private static final String	EMAIL_TEMPLATE_FALLBACK_LANGUAGE						= "en";
																																					
	@Override
	protected List< String > neededSettingKeys() {
		final List< String > needed = new ArrayList< String >( super.neededSettingKeys() );
		needed.add( SETTING_KEY_VERIFICATION_LINK_SECURE );
		needed.add( SETTING_KEY_PASSWORD_RESET_LINK_SECURE );
		needed.add( SETTING_KEY_LINK_LOGIN_AFTER_PASSWORD_RESET );
		return needed;
	}
	
	public static MyUsernamePasswordAuthProvider getProvider() {
		return ( MyUsernamePasswordAuthProvider )PlayAuthenticate.getProvider( UsernamePasswordAuthProvider.PROVIDER_KEY );
	}
	
	public static class MyIdentity {
		
		public MyIdentity() {}
		
		public MyIdentity( final String email ) {
			this.setEmail( email );
		}
		
		@Required
		@Email
		private String email;
		
		public String getEmail() {
			return email;
		}
		
		public void setEmail( final String email ) {
			this.email = email;
		}
	}
	
	public static class MyLogin extends MyIdentity
			implements com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider.UsernamePassword {
			
		@Required
		@MinLength( 3 )
		private String password;
		
		@Override
		public String getPassword() {
			return password;
		}
		
		public void setPassword( final String password ) {
			this.password = password;
		}
	}
	
	public static class MySignup extends MyLogin {
		
		@Required
		@MinLength( 3 )
		private String	repeatPassword;
										
		@Required
		private String	name;
										
		public String getName() {
			return name;
		}
		
		public void setName( final String name ) {
			this.name = name;
		}
		
		public String getRepeatPassword() {
			return repeatPassword;
		}
		
		public void setRepeatPassword( final String repeatPassword ) {
			this.repeatPassword = repeatPassword;
		}
		
		public String validate() {
			try {
				final User user = User.findByEmail( getEmail() );
				LOGGER.error( "User {} already exists!", user.getEmail() );
				return Messages.get( "playauthenticate.email.signup.error.email_exists" );
			}
			catch ( final UserNotFoundException unfe ) {
				if ( getPassword() == null || !getPassword().equals( repeatPassword ) )
					return Messages.get( "playauthenticate.password.signup.error.passwords_not_same" );
			}
			return null;
		}
	}
	
	public static final Form< MySignup >	SIGNUP_FORM	= form( MySignup.class );
																										
	public static final Form< MyLogin >		LOGIN_FORM	= form( MyLogin.class );
																										
	@Inject
	public MyUsernamePasswordAuthProvider( final Application app ) {
		super( app );
	}
	
	@Override
	protected Form< MySignup > getSignupForm() {
		return SIGNUP_FORM;
	}
	
	@Override
	protected Form< MyLogin > getLoginForm() {
		return LOGIN_FORM;
	}
	
	@Override
	protected com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider.SignupResult signupUser(
			final MyUsernamePasswordAuthUser user ) {
		try {
			final User u = User.findByUsernamePasswordIdentity( user );
			if ( u.isEmailValidated() )
				// This user exists, has its email validated and is active
				return SignupResult.USER_EXISTS;
			else
				// this user exists, is active but has not yet validated its
				// email
				return SignupResult.USER_EXISTS_UNVERIFIED;
		}
		catch ( final UserNotFoundException e ) {
			// The user either does not exist or is inactive - create a new one
			final User newUser = User.create( user );
			LOGGER.debug( "New user created {}", newUser );
			// Usually the email should be verified before allowing login,
			// however
			// if you return
			// return SignupResult.USER_CREATED;
			// then the user gets logged in directly
			return SignupResult.USER_CREATED_UNVERIFIED;
		}
	}
	
	@Override
	protected com.feth.play.module.pa.providers.password.UsernamePasswordAuthProvider.LoginResult loginUser(
			final MyLoginUsernamePasswordAuthUser authUser ) {
		try {
			final User u = User.findByUsernamePasswordIdentity( authUser );
			if ( !u.isEmailValidated() )
				return LoginResult.USER_UNVERIFIED;
			else {
				for ( final LinkedAccount acc : u.getLinkedAccounts() )
					if ( getKey().equals( acc.getProvider() ) )
						if ( authUser.checkPassword( acc.getUserId(), authUser.getPassword() ) )
							// Password was correct
							return LoginResult.USER_LOGGED_IN;
						else
							// if you don't return here,
							// you would allow the user to have
							// multiple passwords defined
							// usually we don't want this
							return LoginResult.WRONG_PASSWORD;
				return LoginResult.WRONG_PASSWORD;
			}
		}
		catch ( final UserNotFoundException e ) {
			return LoginResult.NOT_FOUND;
		}
	}
	
	@Override
	protected Call userExists( final UsernamePasswordAuthUser authUser ) {
		return routes.Signup.exists();
	}
	
	@Override
	protected Call userUnverified( final UsernamePasswordAuthUser authUser ) {
		return routes.Signup.unverified();
	}
	
	@Override
	protected MyUsernamePasswordAuthUser buildSignupAuthUser( final MySignup signup, final Context ctx ) {
		return new MyUsernamePasswordAuthUser( signup );
	}
	
	@Override
	protected MyLoginUsernamePasswordAuthUser buildLoginAuthUser( final MyLogin login, final Context ctx ) {
		return new MyLoginUsernamePasswordAuthUser( login.getPassword(), login.getEmail() );
	}
	
	@Override
	protected MyLoginUsernamePasswordAuthUser transformAuthUser( final MyUsernamePasswordAuthUser authUser,
			final Context context ) {
		return new MyLoginUsernamePasswordAuthUser( authUser.getEmail() );
	}
	
	@Override
	protected String getVerifyEmailMailingSubject( final MyUsernamePasswordAuthUser user, final Context ctx ) {
		return Messages.get( "playauthenticate.password.verify_signup.subject" );
	}
	
	@Override
	protected String onLoginUserNotFound( final Context context ) {
		context.flash().put( mk.ck.energy.csm.controllers.Application.FLASH_ERROR_KEY,
				Messages.get( "playauthenticate.password.login.unknown_user_or_pw" ) );
		return super.onLoginUserNotFound( context );
	}
	
	@Override
	protected Body getVerifyEmailMailingBody( final String token, final MyUsernamePasswordAuthUser user, final Context ctx ) {
		final boolean isSecure = getConfiguration().getBoolean( SETTING_KEY_VERIFICATION_LINK_SECURE );
		final String url = routes.Signup.verify( token ).absoluteURL( ctx.request(), isSecure );
		final Lang lang = Lang.preferred( ctx.request().acceptLanguages() );
		final String langCode = lang.code();
		final String html = getEmailTemplate( "views.html.account.signup.email.verify_email", langCode, url, token, user.getName(),
				user.getEmail() );
		final String text = getEmailTemplate( "views.txt.account.signup.email.verify_email", langCode, url, token, user.getName(),
				user.getEmail() );
		return new Body( text, html );
	}
	
	private static String generateToken() {
		return UUID.randomUUID().toString();
	}
	
	@Override
	protected String generateVerificationRecord( final MyUsernamePasswordAuthUser user ) {
		try {
			final User u = User.findByAuthUserIdentity( user );
			return generateVerificationRecord( u );
		}
		catch ( final UserNotFoundException e ) {
			LOGGER.error( "Could not generate verification record for {}", user, e );
			return generateToken();
		}
	}
	
	protected String generateVerificationRecord( final User user ) {
		final String token = generateToken();
		// Do database actions, etc.
		TokenAction.create( TokenType.EMAIL_VERIFICATION, token, user );
		return token;
	}
	
	protected String generatePasswordResetRecord( final User u ) {
		final String token = generateToken();
		TokenAction.create( TokenType.PASSWORD_RESET, token, u );
		return token;
	}
	
	protected String getPasswordResetMailingSubject( final User user, final Context ctx ) {
		return Messages.get( "playauthenticate.password.reset_email.subject" );
	}
	
	protected Body getPasswordResetMailingBody( final String token, final User user, final Context ctx ) {
		final boolean isSecure = getConfiguration().getBoolean( SETTING_KEY_PASSWORD_RESET_LINK_SECURE );
		final String url = routes.Signup.resetPassword( token ).absoluteURL( ctx.request(), isSecure );
		final Lang lang = Lang.preferred( ctx.request().acceptLanguages() );
		final String langCode = lang.code();
		final String html = getEmailTemplate( "views.html.account.email.password_reset", langCode, url, token, user.getName(),
				user.getEmail() );
		final String text = getEmailTemplate( "views.txt.account.email.password_reset", langCode, url, token, user.getName(),
				user.getEmail() );
		return new Body( text, html );
	}
	
	public void sendPasswordResetMailing( final User user, final Context ctx ) {
		final String token = generatePasswordResetRecord( user );
		final String subject = getPasswordResetMailingSubject( user, ctx );
		final Body body = getPasswordResetMailingBody( token, user, ctx );
		sendMail( subject, body, getEmailName( user ) );
	}
	
	public boolean isLoginAfterPasswordReset() {
		return getConfiguration().getBoolean( SETTING_KEY_LINK_LOGIN_AFTER_PASSWORD_RESET );
	}
	
	protected String getVerifyEmailMailingSubjectAfterSignup( final User user, final Context ctx ) {
		return Messages.get( "playauthenticate.password.verify_email.subject" );
	}
	
	protected String getEmailTemplate( final String template, final String langCode, final String url, final String token,
			final String name, final String email ) {
		Class< ? > cls = null;
		String ret = null;
		try {
			cls = Class.forName( template + "_" + langCode );
		}
		catch ( final ClassNotFoundException e ) {
			LOGGER.warn( "Template: '{}_{}' was not found! Trying to use English fallback template instead.", template, langCode );
		}
		if ( cls == null )
			try {
				cls = Class.forName( template + "_" + EMAIL_TEMPLATE_FALLBACK_LANGUAGE );
			}
			catch ( final ClassNotFoundException e ) {
				LOGGER.error( "Fallback template: '{}_{}' was not found either!", template, EMAIL_TEMPLATE_FALLBACK_LANGUAGE );
			}
		if ( cls != null ) {
			Method htmlRender = null;
			try {
				htmlRender = cls.getMethod( "render", String.class, String.class, String.class, String.class );
				ret = htmlRender.invoke( null, url, token, name, email ).toString();
			}
			catch ( final NoSuchMethodException e ) {
				e.printStackTrace();
			}
			catch ( final IllegalAccessException e ) {
				e.printStackTrace();
			}
			catch ( final InvocationTargetException e ) {
				e.printStackTrace();
			}
		}
		return ret;
	}
	
	protected Body getVerifyEmailMailingBodyAfterSignup( final String token, final User user, final Context ctx ) {
		final boolean isSecure = getConfiguration().getBoolean( SETTING_KEY_VERIFICATION_LINK_SECURE );
		final String url = routes.Signup.verify( token ).absoluteURL( ctx.request(), isSecure );
		final Lang lang = Lang.preferred( ctx.request().acceptLanguages() );
		final String langCode = lang.code();
		final String html = getEmailTemplate( "views.html.account.email.verify_email", langCode, url, token, user.getName(),
				user.getEmail() );
		final String text = getEmailTemplate( "views.txt.account.email.verify_email", langCode, url, token, user.getName(),
				user.getEmail() );
		return new Body( text, html );
	}
	
	public void sendVerifyEmailMailingAfterSignup( final User user, final Context ctx ) {
		final String subject = getVerifyEmailMailingSubjectAfterSignup( user, ctx );
		final String token = generateVerificationRecord( user );
		final Body body = getVerifyEmailMailingBodyAfterSignup( token, user, ctx );
		sendMail( subject, body, getEmailName( user ) );
	}
	
	private String getEmailName( final User user ) {
		return getEmailName( user.getEmail(), user.getName() );
	}
}
