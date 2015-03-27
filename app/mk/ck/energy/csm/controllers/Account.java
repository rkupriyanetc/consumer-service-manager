package mk.ck.energy.csm.controllers;

import static play.data.Form.form;

import java.util.List;

import mk.ck.energy.csm.models.auth.User;
import mk.ck.energy.csm.models.auth.UserRole;
import mk.ck.energy.csm.providers.MyUsernamePasswordAuthProvider;
import mk.ck.energy.csm.providers.MyUsernamePasswordAuthUser;
import play.data.Form;
import play.data.format.Formats.NonEmpty;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Required;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.account.ask_link;
import views.html.account.ask_merge;
import views.html.account.joinConsumer;
import views.html.account.link;
import views.html.account.password_change;
import views.html.account.unverified;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUser;

public class Account extends Controller {
	
	public static class Accept {
		
		@Required
		@NonEmpty
		private Boolean	accept;
		
		public Boolean getAccept() {
			return accept;
		}
		
		public void setAccept( final Boolean accept ) {
			this.accept = accept;
		}
	}
	
	public static class PasswordChange {
		
		@MinLength( 3 )
		@Required
		private String	password;
		
		@MinLength( 3 )
		@Required
		private String	repeatPassword;
		
		public String getPassword() {
			return password;
		}
		
		public void setPassword( final String password ) {
			this.password = password;
		}
		
		public String getRepeatPassword() {
			return repeatPassword;
		}
		
		public void setRepeatPassword( final String repeatPassword ) {
			this.repeatPassword = repeatPassword;
		}
		
		public String validate() {
			if ( password == null || !password.equals( repeatPassword ) )
				return Messages.get( "playauthenticate.change_password.error.passwords_not_same" );
			return null;
		}
	}
	
	public static class AppendConsumer {
		
		@MinLength( 5 )
		@Required
		private String					id;
		
		@Required
		private String					fullName;
		
		@Required
		private List< String >	topAddress;
		
		@Required
		private List< String >	locationAddress;
		
		@Required
		private List< String >	placeAddress;
		
		@Required
		private String					house;
		
		@Required
		private String					apartment;
		
		public AppendConsumer() {}
		
		public String getId() {
			return id;
		}
		
		public void setId( final String id ) {
			this.id = id;
		}
		
		public String getFullName() {
			return fullName;
		}
		
		public void setFullName( final String fullName ) {
			this.fullName = fullName;
		}
		
		public List< String > getTopAddress() {
			return topAddress;
		}
		
		public void setTopAddress( final List< String > topAddress ) {
			this.topAddress = topAddress;
		}
		
		public List< String > getLocationAddress() {
			return locationAddress;
		}
		
		public void setLocationAddress( final List< String > locationAddress ) {
			this.locationAddress = locationAddress;
		}
		
		public List< String > getPlaceAddress() {
			return placeAddress;
		}
		
		public void setPlaceAddress( final List< String > placeAddress ) {
			this.placeAddress = placeAddress;
		}
		
		public String getHouse() {
			return house;
		}
		
		public void setHouse( final String house ) {
			this.house = house;
		}
		
		public String getApartment() {
			return apartment;
		}
		
		public void setApartment( final String apartment ) {
			this.apartment = apartment;
		}
	}
	
	private static final Form< Accept >					ACCEPT_FORM						= form( Accept.class );
	
	private static final Form< PasswordChange >	PASSWORD_CHANGE_FORM	= form( PasswordChange.class );
	
	private static final Form< AppendConsumer >	APPEND_CONSUMER_FORM	= form( AppendConsumer.class );
	
	@SubjectPresent
	public static Result link() {
		com.feth.play.module.pa.controllers.Authenticate.noCache( response() );
		return ok( link.render() );
	}
	
	@SubjectPresent
	public static Result verifyEmail() {
		com.feth.play.module.pa.controllers.Authenticate.noCache( response() );
		final User user = User.getLocalUser( session() );
		if ( user.isEmailValidated() )
			// E-Mail has been validated already
			flash( Application.FLASH_MESSAGE_KEY, Messages.get( "playauthenticate.verify_email.error.already_validated" ) );
		else
			if ( user.getEmail() != null && !user.getEmail().trim().isEmpty() ) {
				flash( Application.FLASH_MESSAGE_KEY,
						Messages.get( "playauthenticate.verify_email.message.instructions_sent", user.getEmail() ) );
				MyUsernamePasswordAuthProvider.getProvider().sendVerifyEmailMailingAfterSignup( user, ctx() );
			} else
				flash( Application.FLASH_MESSAGE_KEY,
						Messages.get( "playauthenticate.verify_email.error.set_email_first", user.getEmail() ) );
		return redirect( routes.Application.profile() );
	}
	
	@SubjectPresent
	public static Result changePassword() {
		com.feth.play.module.pa.controllers.Authenticate.noCache( response() );
		final User u = User.getLocalUser( session() );
		if ( !u.isEmailValidated() )
			return ok( unverified.render() );
		else
			return ok( password_change.render( PASSWORD_CHANGE_FORM ) );
	}
	
	@SubjectPresent
	public static Result doChangePassword() {
		com.feth.play.module.pa.controllers.Authenticate.noCache( response() );
		final Form< Account.PasswordChange > filledForm = PASSWORD_CHANGE_FORM.bindFromRequest();
		if ( filledForm.hasErrors() )
			// User did not select whether to link or not link
			return badRequest( password_change.render( filledForm ) );
		else {
			final User user = User.getLocalUser( session() );
			final String newPassword = filledForm.get().getPassword();
			user.changePassword( new MyUsernamePasswordAuthUser( newPassword ), true );
			flash( Application.FLASH_MESSAGE_KEY, Messages.get( "playauthenticate.change_password.success" ) );
			return redirect( routes.Application.profile() );
		}
	}
	
	@SubjectPresent
	public static Result askLink() {
		com.feth.play.module.pa.controllers.Authenticate.noCache( response() );
		final AuthUser u = PlayAuthenticate.getLinkUser( session() );
		if ( u == null )
			// account to link could not be found, silently redirect to login
			return redirect( routes.Application.index() );
		return ok( ask_link.render( ACCEPT_FORM, u ) );
	}
	
	@SubjectPresent
	public static Result doLink() {
		com.feth.play.module.pa.controllers.Authenticate.noCache( response() );
		final AuthUser u = PlayAuthenticate.getLinkUser( session() );
		if ( u == null )
			// account to link could not be found, silently redirect to login
			return redirect( routes.Application.index() );
		final Form< Accept > filledForm = ACCEPT_FORM.bindFromRequest();
		if ( filledForm.hasErrors() )
			// User did not select whether to link or not link
			return badRequest( ask_link.render( filledForm, u ) );
		else {
			// User made a choice :)
			final boolean link = filledForm.get().getAccept();
			if ( link )
				flash( Application.FLASH_MESSAGE_KEY, Messages.get( "playauthenticate.accounts.link.success" ) );
			return PlayAuthenticate.link( ctx(), link );
		}
	}
	
	@SubjectPresent
	public static Result askMerge() {
		com.feth.play.module.pa.controllers.Authenticate.noCache( response() );
		// this is the currently logged in user
		final AuthUser aUser = PlayAuthenticate.getUser( session() );
		// this is the user that was selected for a login
		final AuthUser bUser = PlayAuthenticate.getMergeUser( session() );
		if ( bUser == null )
			// user to merge with could not be found, silently redirect to login
			return redirect( routes.Application.index() );
		// You could also get the local user object here via
		// User.findByAuthUserIdentity(newUser)
		return ok( ask_merge.render( ACCEPT_FORM, aUser, bUser ) );
	}
	
	@SubjectPresent
	public static Result doMerge() {
		com.feth.play.module.pa.controllers.Authenticate.noCache( response() );
		// this is the currently logged in user
		final AuthUser aUser = PlayAuthenticate.getUser( session() );
		// this is the user that was selected for a login
		final AuthUser bUser = PlayAuthenticate.getMergeUser( session() );
		if ( bUser == null )
			// user to merge with could not be found, silently redirect to login
			return redirect( routes.Application.index() );
		final Form< Accept > filledForm = ACCEPT_FORM.bindFromRequest();
		if ( filledForm.hasErrors() )
			// User did not select whether to merge or not merge
			return badRequest( ask_merge.render( filledForm, aUser, bUser ) );
		else {
			// User made a choice :)
			final boolean merge = filledForm.get().getAccept();
			if ( merge )
				flash( Application.FLASH_MESSAGE_KEY, Messages.get( "playauthenticate.accounts.merge.success" ) );
			return PlayAuthenticate.merge( ctx(), merge );
		}
	}
	
	@Restrict( @Group( UserRole.USER_ROLE_NAME ) )
	public static Result joinConsumerElectricity() {
		com.feth.play.module.pa.controllers.Authenticate.noCache( response() );
		final Form< AppendConsumer > filledForm = APPEND_CONSUMER_FORM.bindFromRequest();
		final AppendConsumer ac = new AppendConsumer();
		return ok( joinConsumer.render( filledForm.fill( ac ) ) );
	}
	
	@Restrict( @Group( UserRole.USER_ROLE_NAME ) )
	public static Result doJoinConsumerElectricity() {
		com.feth.play.module.pa.controllers.Authenticate.noCache( response() );
		final Form< AppendConsumer > filledForm = APPEND_CONSUMER_FORM.bindFromRequest();
		if ( filledForm.hasErrors() )
			// User did not select whether to link or not link
			return badRequest( joinConsumer.render( filledForm ) );
		else {
			final AppendConsumer u = filledForm.get();
			return Application.profile();
		}
	}
}
