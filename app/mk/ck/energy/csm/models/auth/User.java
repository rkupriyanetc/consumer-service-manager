package mk.ck.energy.csm.models.auth;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

import mk.ck.energy.csm.models.Database;
import mk.ck.energy.csm.providers.MyStupidBasicAuthProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.cache.Cache;
import play.mvc.Http;
import play.mvc.Http.Session;
import be.objectify.deadbolt.core.models.Permission;
import be.objectify.deadbolt.core.models.Role;
import be.objectify.deadbolt.core.models.Subject;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;
import com.feth.play.module.pa.user.EmailIdentity;
import com.feth.play.module.pa.user.FirstLastNameIdentity;
import com.feth.play.module.pa.user.NameIdentity;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.QueryBuilder;

/**
 * Authenticated user.
 * 
 * @author KYL
 */
public class User implements Subject {
	
	private static final Logger						LOGGER										= LoggerFactory.getLogger( User.class );
	
	static final String										DB_FIELD_ID								= "_id";
	
	static final String										DB_FIELD_EMAIL						= "email";
	
	static final String										DB_FIELD_NAME							= "name";
	
	static final String										DB_FIELD_FIRST_NAME				= "first_name";
	
	static final String										DB_FIELD_LAST_NAME				= "last_name";
	
	static final String										DB_FIELD_LAST_LOGIN				= "last_login";
	
	static final String										DB_FIELD_ACTIVE						= "active";
	
	static final String										DB_FIELD_EMAIL_VALIDATED	= "validated";
	
	static final String										DB_FIELD_ROLES						= "roles";
	
	static final String										DB_FIELD_LINKED_ACCOUNTS	= "linkeds";
	
	private String												id;
	
	private String												email;
	
	private String												name;
	
	private String												firstName;
	
	private String												lastName;
	
	private long													lastLogin;
	
	private boolean												active;
	
	private boolean												emailValidated;
	
	private final List< UserRole >				roles											= new ArrayList< UserRole >( 0 );
	
	private final List< LinkedAccount >		linkedAccounts						= new ArrayList< LinkedAccount >( 0 );
	
	private final List< UserPermission >	permissions								= new ArrayList< UserPermission >( 0 );
	
	private User( final AuthUser authUser ) {
		lastLogin = System.currentTimeMillis();
		active = true;
		if ( authUser.getProvider().equals( MyStupidBasicAuthProvider.GUEST_PROVIDER )
				&& authUser.getId().equals( MyStupidBasicAuthProvider.GUEST_ID ) )
			roles.add( UserRole.GUEST );
		else
			roles.add( UserRole.USER );
		// user.permissions = new ArrayList<UserPermission>();
		// user.permissions.add(UserPermission.findByValue("printers.edit"));
		linkedAccounts.add( LinkedAccount.getInstance( authUser ) );
		if ( authUser instanceof EmailIdentity ) {
			final EmailIdentity identity = ( EmailIdentity )authUser;
			// Remember, even when getting them from FB & Co., emails should be
			// verified within the application as a security breach there might
			// break your security as well!
			email = identity.getEmail();
		}
		if ( authUser instanceof NameIdentity ) {
			final NameIdentity identity = ( NameIdentity )authUser;
			final String name = identity.getName();
			if ( name != null )
				this.name = name;
		}
		if ( authUser instanceof FirstLastNameIdentity ) {
			final FirstLastNameIdentity identity = ( FirstLastNameIdentity )authUser;
			final String firstName = identity.getFirstName();
			if ( firstName != null )
				this.firstName = firstName;
			final String lastName = identity.getLastName();
			if ( lastName != null )
				this.lastName = lastName;
		}
	}
	
	protected User( final String id ) {
		this.id = id;
	}
	
	private User( final DBObject doc ) {
		this.id = ( String )doc.get( DB_FIELD_ID );
		this.email = ( String )doc.get( DB_FIELD_EMAIL );
		this.name = ( String )doc.get( DB_FIELD_NAME );
		this.firstName = ( String )doc.get( DB_FIELD_FIRST_NAME );
		this.lastName = ( String )doc.get( DB_FIELD_LAST_NAME );
		this.lastLogin = ( Long )doc.get( DB_FIELD_LAST_LOGIN );
		this.active = ( Boolean )doc.get( DB_FIELD_ACTIVE );
		this.emailValidated = ( Boolean )doc.get( DB_FIELD_EMAIL_VALIDATED );
		final BasicDBList dbRoles = ( BasicDBList )doc.get( DB_FIELD_ROLES );
		for ( final Object elm : dbRoles )
			roles.add( UserRole.getInstance( ( DBObject )elm ) );
		final BasicDBList dbAccounts = ( BasicDBList )doc.get( DB_FIELD_LINKED_ACCOUNTS );
		for ( final Object elm : dbAccounts )
			linkedAccounts.add( LinkedAccount.getInstance( ( DBObject )elm ) );
	}
	
	@Override
	public String getIdentifier() {
		return getId();
	}
	
	public String getId() {
		return id;
	};
	
	public String getEmail() {
		return email;
	}
	
	public boolean isEmailValidated() {
		return emailValidated;
	}
	
	public String getName() {
		return name;
	}
	
	public String getFirstName() {
		return firstName;
	}
	
	public String getLastName() {
		return lastName;
	}
	
	@Override
	public List< ? extends Role > getRoles() {
		return roles;
	}
	
	public List< LinkedAccount > getLinkedAccounts() {
		return linkedAccounts;
	}
	
	@Override
	public List< ? extends Permission > getPermissions() {
		return permissions;
	}
	
	public static boolean existsByAuthUserIdentity( final AuthUserIdentity identity ) {
		if ( identity instanceof UsernamePasswordAuthUser )
			return getUsersCollection().count( getUsernamePasswordAuthUserFind( ( UsernamePasswordAuthUser )identity ).get() ) > 0;
		else
			return getUsersCollection().count( getAuthUserFind( identity ).get() ) > 0;
	}
	
	private static QueryBuilder getAuthUserFind( final AuthUserIdentity identity ) {
		return QueryBuilder.start( DB_FIELD_ACTIVE ).is( true ).and( DB_FIELD_LINKED_ACCOUNTS )
				.elemMatch( LinkedAccount.getInstance( identity ).getDBObject() );
	}
	
	private static class ByIdentityFinder implements Callable< User > {
		
		private final AuthUserIdentity	identity;
		
		private ByIdentityFinder( final AuthUserIdentity identity ) {
			this.identity = identity;
			LOGGER.trace( "Finder for User : {} created", identity );
		}
		
		@Override
		public User call() throws Exception {
			if ( identity instanceof UsernamePasswordAuthUser )
				return findByUsernamePasswordIdentity( ( UsernamePasswordAuthUser )identity );
			else {
				final DBObject doc = getUsersCollection().findOne( getAuthUserFind( identity ).get() );
				if ( doc == null ) {
					LOGGER.warn( "Could not find user by identity {}", identity );
					throw new UserNotFoundException();
				} else
					return new User( doc );
			}
		}
	}
	
	public static User findByAuthUserIdentity( final AuthUserIdentity identity ) throws UserNotFoundException {
		if ( identity == null ) {
			LOGGER.error( "Tried to find user by null identity" );
			throw new UserNotFoundException();
		}
		final String identityKey = "user-" + identity.toString();
		try {
			return Cache.getOrElse( identityKey, new ByIdentityFinder( identity ), 60 );
		}
		catch ( final UserNotFoundException e ) {
			throw e;
		}
		catch ( final Exception e ) {
			LOGGER.error( "Could not find user for identity {}", identity, e );
			throw new UserNotFoundException();
		}
	}
	
	public static User findByUsernamePasswordIdentity( final UsernamePasswordAuthUser identity ) throws UserNotFoundException {
		final DBObject doc = getUsersCollection().findOne( getUsernamePasswordAuthUserFind( identity ).get() );
		if ( doc == null ) {
			LOGGER.warn( "Could not finr user by user and password {}", identity );
			throw new UserNotFoundException();
		} else
			return new User( doc );
	}
	
	private static QueryBuilder getUsernamePasswordAuthUserFind( final UsernamePasswordAuthUser identity ) {
		return getEmailUserFind( identity.getEmail() ).and( DB_FIELD_LINKED_ACCOUNTS ).elemMatch(
				new BasicDBObject( LinkedAccount.DB_FIELD_PROVIDER, identity.getProvider() ) );
	}
	
	public static List< User > findByRole( final UserRole role ) throws UserNotFoundException {
		final DBObject sort = new BasicDBObject();
		sort.put( DB_FIELD_ROLES, 1 );
		final DBCursor cursor = getUsersCollection().find(
				QueryBuilder.start( DB_FIELD_ACTIVE ).is( true ).and( DB_FIELD_ROLES ).elemMatch( role.getDBObject() ).get() )
				.sort( sort );
		if ( cursor == null ) {
			LOGGER.warn( "Could not find users by role {}", role );
			throw new UserNotFoundException();
		} else {
			final List< User > users = new ArrayList<>( 0 );
			while ( cursor.hasNext() ) {
				final DBObject o = cursor.next();
				users.add( new User( o ) );
			}
			return users;
		}
	}
	
	private DBObject getDBObject() {
		final BasicDBList dbRoles = new BasicDBList();
		for ( final UserRole role : roles )
			dbRoles.add( role.getDBObject() );
		final BasicDBList dbAccounts = new BasicDBList();
		for ( final LinkedAccount acc : linkedAccounts )
			dbAccounts.add( acc.getDBObject() );
		final DBObject doc = new BasicDBObject( DB_FIELD_ID, getOrCreateId() );
		if ( email != null )
			doc.put( DB_FIELD_EMAIL, email );
		if ( firstName != null )
			doc.put( DB_FIELD_FIRST_NAME, firstName );
		if ( name != null )
			doc.put( DB_FIELD_NAME, name );
		if ( lastName != null )
			doc.put( DB_FIELD_LAST_NAME, lastName );
		doc.put( DB_FIELD_ACTIVE, active );
		doc.put( DB_FIELD_EMAIL_VALIDATED, emailValidated );
		if ( !dbRoles.isEmpty() )
			doc.put( DB_FIELD_ROLES, dbRoles );
		if ( !dbAccounts.isEmpty() )
			doc.put( DB_FIELD_LINKED_ACCOUNTS, dbAccounts );
		doc.put( DB_FIELD_LAST_LOGIN, lastLogin );
		return doc;
	}
	
	private String getOrCreateId() {
		if ( id == null )
			id = UUID.randomUUID().toString().toLowerCase();
		return id;
	}
	
	public void merge( final User otherUser ) {
		for ( final LinkedAccount acc : otherUser.linkedAccounts )
			this.linkedAccounts.add( LinkedAccount.getInstance( acc ) );
		// do all other merging stuff here - like resources, etc.
		if ( email == null )
			email = otherUser.email;
		if ( name == null )
			name = otherUser.name;
		// deactivate the merged user that got added to this one
		otherUser.active = false;
		final DBCollection users = getUsersCollection();
		users.save( getDBObject() );
		users.update( new BasicDBObject( DB_FIELD_ID, otherUser.getId() ), otherUser.getDBObject() );
	}
	
	public static User create( final AuthUser authUser ) {
		final User user = new User( authUser );
		getUsersCollection().save( user.getDBObject() );
		return user;
	}
	
	public static void merge( final AuthUser oldAuthUser, final AuthUser newAuthUser ) {
		try {
			final User oldUser = User.findByAuthUserIdentity( oldAuthUser );
			final User newUser = User.findByAuthUserIdentity( newAuthUser );
			oldUser.merge( newUser );
		}
		catch ( final UserNotFoundException e ) {
			LOGGER.warn( "Cannot merge {} with {}", oldAuthUser, newAuthUser, e );
		}
	}
	
	public Set< String > getProviders() {
		final Set< String > providerKeys = new HashSet< String >( linkedAccounts.size() );
		for ( final LinkedAccount acc : linkedAccounts )
			providerKeys.add( acc.getProvider() );
		return providerKeys;
	}
	
	public static void addLinkedAccount( final AuthUser oldUser, final AuthUser newUser ) {
		try {
			final User u = User.findByAuthUserIdentity( oldUser );
			u.linkedAccounts.add( LinkedAccount.getInstance( newUser ) );
			getUsersCollection().save( u.getDBObject() );
		}
		catch ( final UserNotFoundException e ) {
			LOGGER.warn( "Cannot link {} to {}", newUser, oldUser );
		}
	}
	
	public void addRole( final UserRole role ) {
		try {
			roles.add( role );
		}
		catch ( final UnsupportedOperationException uoe ) {
			LOGGER.warn( "Exception: {}. ", uoe );
		}
		getUsersCollection().save( getDBObject() );
	}
	
	public void updateLastLoginDate() {
		lastLogin = System.currentTimeMillis();
		getUsersCollection().save( getDBObject() );
	}
	
	public static String getCollectorId() {
		final Session session = Http.Context.current().session();
		final AuthUser currentAuthUser = PlayAuthenticate.getUser( session );
		if ( currentAuthUser != null )
			try {
				final User collector = User.findByAuthUserIdentity( currentAuthUser );
				return collector.getId();
			}
			catch ( final UserNotFoundException e ) {
				LOGGER.warn( "Could not find user by identity {}", currentAuthUser );
			}
		return null;
	}
	
	public static User findById( final String userId ) throws UserNotFoundException {
		final DBObject doc = getUsersCollection().findOne( QueryBuilder.start( DB_FIELD_ID ).is( userId ).get() );
		if ( doc == null ) {
			LOGGER.warn( "Could not find user by id {}", userId );
			throw new UserNotFoundException();
		} else
			return new User( doc );
	}
	
	public static User findByEmail( final String email ) throws UserNotFoundException {
		// there is out RuntimeException
		try {
			final DBObject doc = getUsersCollection().findOne( getEmailUserFind( email ).get() );
			if ( doc != null )
				return new User( doc );
			else {
				LOGGER.warn( "Could not find user by email {}", email );
				throw new UserNotFoundException();
			}
		}
		catch ( final RuntimeException re ) {
			LOGGER.warn( "Could not find user by email {}", email );
			throw new UserNotFoundException();
		}
	}
	
	private static QueryBuilder getEmailUserFind( final String email ) {
		return QueryBuilder.start( DB_FIELD_ACTIVE ).is( true ).and( DB_FIELD_EMAIL ).is( email );
	}
	
	public LinkedAccount getAccountByProvider( final String providerKey ) {
		for ( final LinkedAccount acc : linkedAccounts )
			if ( acc.getProvider().equals( providerKey ) )
				return acc;
		LOGGER.warn( "Could not find account by provider {}", providerKey );
		return null;
	}
	
	public static User getLocalUser( final Session session ) {
		final AuthUser currentAuthUser = PlayAuthenticate.getUser( session );
		if ( currentAuthUser != null )
			try {
				return User.findByAuthUserIdentity( currentAuthUser );
			}
			catch ( final UserNotFoundException e ) {
				LOGGER.warn( "Could not find user by identity {}", currentAuthUser );
			}
		return null;
	}
	
	public void verify() {
		// You might want to wrap this into a transaction
		this.emailValidated = true;
		getUsersCollection().save( getDBObject() );
		TokenAction.deleteByUser( this, TokenType.EMAIL_VERIFICATION );
	}
	
	public void changePassword( final UsernamePasswordAuthUser authUser, final boolean create ) {
		final LinkedAccount existing = this.getAccountByProvider( authUser.getProvider() );
		if ( existing == null ) {
			if ( !create )
				throw new RuntimeException( "Account not enabled for password usage" );
		} else
			linkedAccounts.remove( existing );
		linkedAccounts.add( LinkedAccount.getInstance( authUser ) );
		getUsersCollection().save( getDBObject() );
	}
	
	public void resetPassword( final UsernamePasswordAuthUser authUser, final boolean create ) {
		// You might want to wrap this into a transaction
		this.changePassword( authUser, create );
		TokenAction.deleteByUser( this, TokenType.PASSWORD_RESET );
	}
	
	public static User remove( final String id ) throws UserNotFoundException {
		final DBObject doc = getUsersCollection().findOne( QueryBuilder.start( DB_FIELD_ID ).is( id ).get() );
		if ( doc == null ) {
			LOGGER.warn( "Could not find user by id {}", id );
			throw new UserNotFoundException();
		} else {
			try {
				getUsersCollection().remove( doc );
			}
			catch ( final MongoException me ) {
				LOGGER.warn( "Could not remove user. Id is {}", id );
			}
			return new User( doc );
		}
	}
	
	public boolean isAdmin() {
		for ( final UserRole r : roles )
			if ( r.equals( UserRole.ADMIN ) )
				return true;
		return false;
	}
	
	public boolean isOper() {
		for ( final UserRole r : roles )
			if ( r.equals( UserRole.OPER ) )
				return true;
		return false;
	}
	
	private static DBCollection getUsersCollection() {
		return Database.getInstance().getDatabase().getCollection( "users" );
	}
}
