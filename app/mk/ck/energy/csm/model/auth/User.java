package mk.ck.energy.csm.model.auth;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import mk.ck.energy.csm.model.mongodb.CSMAbstractDocument;
import mk.ck.energy.csm.providers.MyStupidBasicAuthProvider;

import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;

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
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;

/**
 * Authenticated user.
 * 
 * @author RVK
 */
public class User extends CSMAbstractDocument< Document > implements Subject {
	
	private static final long			serialVersionUID					= 1L;
	
	private static final String		COLLECTION_NAME_USERS			= "users";
	
	private static final String		DB_FIELD_EMAIL						= "email";
	
	private static final String		DB_FIELD_NAME							= "name";
	
	private static final String		DB_FIELD_FIRST_NAME				= "first_name";
	
	private static final String		DB_FIELD_LAST_NAME				= "last_name";
	
	private static final String		DB_FIELD_LAST_LOGIN				= "last_login";
	
	private static final String		DB_FIELD_ACTIVE						= "active";
	
	private static final String		DB_FIELD_EMAIL_VALIDATED	= "validated";
	
	private static final String		DB_FIELD_ROLES						= "roles";
	
	private static final String		DB_FIELD_LINKED_ACCOUNTS	= "linkeds";
	
	private static final String		DB_FIELD_PERMISSIONS			= "permissions";
	
	private List< Role >					roles;
	
	private List< LinkedAccount >	linkeds;
	
	private List< Permission >		permissions;
	
	private User( final AuthUser authUser ) {
		setLastLogin( System.currentTimeMillis() );
		setActive( true );
		if ( authUser.getProvider().equals( MyStupidBasicAuthProvider.GUEST_PROVIDER )
				&& authUser.getId().equals( MyStupidBasicAuthProvider.GUEST_ID ) )
			addRole( UserRole.GUEST );
		else
			addRole( UserRole.USER );
		// user.permissions = new ArrayList<UserPermission>();
		// user.permissions.add(UserPermission.findByValue("printers.edit"));
		getLinkedAccounts().add( LinkedAccount.getInstance( authUser ) );
		if ( authUser instanceof EmailIdentity ) {
			final EmailIdentity identity = ( EmailIdentity )authUser;
			// Remember, even when getting them from FB & Co., emails should be
			// verified within the application as a security breach there might
			// break your security as well!
			setEmail( identity.getEmail() );
		}
		if ( authUser instanceof NameIdentity ) {
			final NameIdentity identity = ( NameIdentity )authUser;
			final String name = identity.getName();
			if ( name != null )
				setName( name );
		}
		if ( authUser instanceof FirstLastNameIdentity ) {
			final FirstLastNameIdentity identity = ( FirstLastNameIdentity )authUser;
			final String firstName = identity.getFirstName();
			if ( firstName != null )
				setFirstName( firstName );
			final String lastName = identity.getLastName();
			if ( lastName != null )
				setLastName( lastName );
		}
	}
	
	protected User( final String id ) {
		setId( id );
	}
	
	private User( final Document doc ) {
		setId( ( String )doc.get( DB_FIELD_ID ) );
		setEmail( ( String )doc.get( DB_FIELD_EMAIL ) );
		setName( ( String )doc.get( DB_FIELD_NAME ) );
		setFirstName( ( String )doc.get( DB_FIELD_FIRST_NAME ) );
		setLastName( ( String )doc.get( DB_FIELD_LAST_NAME ) );
		setLastLogin( ( Long )doc.get( DB_FIELD_LAST_LOGIN ) );
		setActive( ( Boolean )doc.get( DB_FIELD_ACTIVE ) );
		setEmailValidated( ( Boolean )doc.get( DB_FIELD_EMAIL_VALIDATED ) );
		final BsonArray dbRoles = ( BsonArray )doc.get( DB_FIELD_ROLES );
		for ( final BsonValue elm : dbRoles )
			roles.add( UserRole.getInstance( elm.toString() ) );
		final BsonArray dbAccounts = ( BsonArray )doc.get( DB_FIELD_LINKED_ACCOUNTS );
		for ( final BsonValue elm : dbAccounts )
			linkedAccounts.add( LinkedAccount.getInstance( ( LinkedAccount )elm ) );
	}
	
	@Override
	public String getIdentifier() {
		return getId();
	}
	
	public String getEmail() {
		return getString( DB_FIELD_EMAIL );
	}
	
	public void setEmail( final String email ) {
		put( DB_FIELD_EMAIL, email );
	}
	
	public boolean isEmailValidated() {
		return getBoolean( DB_FIELD_EMAIL_VALIDATED );
	}
	
	public void setEmailValidated( final boolean emailValidated ) {
		put( DB_FIELD_EMAIL_VALIDATED, emailValidated );
	}
	
	public String getName() {
		return getString( DB_FIELD_NAME );
	}
	
	public void setName( final String name ) {
		put( DB_FIELD_NAME, name );
	}
	
	public String getFirstName() {
		return getString( DB_FIELD_FIRST_NAME );
	}
	
	public void setFirstName( final String firstName ) {
		put( DB_FIELD_FIRST_NAME, firstName );
	}
	
	public String getLastName() {
		return getString( DB_FIELD_LAST_NAME );
	}
	
	public void setLastName( final String lastName ) {
		put( DB_FIELD_LAST_NAME, lastName );
	}
	
	public long getLastLogin() {
		return getLong( DB_FIELD_LAST_LOGIN );
	}
	
	public void setLastLogin( final long lastLogin ) {
		put( DB_FIELD_LAST_LOGIN, lastLogin );
	}
	
	public boolean isActive() {
		return getBoolean( DB_FIELD_ACTIVE );
	}
	
	public void setActive( final boolean active ) {
		put( DB_FIELD_ACTIVE, active );
	}
	
	@Override
	public List< ? extends Role > getRoles() {
		if ( roles == null || roles.isEmpty() ) {
			final BsonArray list = ( BsonArray )get( DB_FIELD_ROLES );
			roles = new ArrayList<>( list.size() );
			for ( final BsonValue key : list.getValues() ) {
				final Role lt = UserRole.getInstance( ( ( BsonString )key ).getValue() );
				roles.add( lt );
			}
		}
		return roles;
	}
	
	public List< LinkedAccount > getLinkedAccounts() {
		if ( linkeds == null || linkeds.isEmpty() ) {
			final BsonArray list = ( BsonArray )get( DB_FIELD_LINKED_ACCOUNTS );
			linkeds = new ArrayList<>( list.size() );
			for ( final BsonValue key : list.getValues() ) {
				final BsonDocument bd = key.asDocument();
				for ( final Map.Entry< String, BsonValue > entry : bd.entrySet() ) {
					final String k = entry.getKey();
					final String v = entry.getValue().asString().getValue();
					final LinkedAccount la = LinkedAccount.getInstance( k, v );
					linkeds.add( la );
				}
			}
		}
		return linkeds;
	}
	
	@Override
	public List< ? extends Permission > getPermissions() {
		if ( permissions == null || permissions.isEmpty() ) {
			final BsonArray list = ( BsonArray )get( DB_FIELD_PERMISSIONS );
			permissions = new ArrayList<>( list.size() );
			for ( final BsonValue key : list.getValues() ) {
				final Permission lt = UserPermission.getInstance( ( ( BsonString )key ).getValue() );
				permissions.add( lt );
			}
		}
		return permissions;
	}
	
	public static boolean existsByAuthUserIdentity( final AuthUserIdentity identity ) {
		if ( identity instanceof UsernamePasswordAuthUser ) {
			final Bson doc = getUsernamePasswordAuthUserFind( ( UsernamePasswordAuthUser )identity );
			return getMongoCollection().count( doc ) > 0;
		} else {
			final Bson doc = getAuthUserFind( identity );
			return getMongoCollection().count( doc ) > 0;
		}
	}
	
	private static Bson getAuthUserFind( final AuthUserIdentity identity ) {
		final Bson active = Filters.eq( DB_FIELD_ACTIVE, true );
		final Bson linkeds = LinkedAccount.getInstance( identity ).getFilters();
		final Bson match = Filters.elemMatch( DB_FIELD_LINKED_ACCOUNTS, linkeds );
		return Filters.and( active, match );
		// return QueryBuilder.start( DB_FIELD_ACTIVE ).is( true ).and(
		// DB_FIELD_LINKED_ACCOUNTS ).elemMatch( LinkedAccount.getInstance( identity
		// ).getDBObject() );
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
				return User.class.cast( findByUsernamePasswordIdentity( ( UsernamePasswordAuthUser )identity ) );
			else {
				final Document doc = getMongoCollection().find( getAuthUserFind( identity ) ).first();
				if ( doc == null ) {
					LOGGER.warn( "Could not find user by identity {}", identity );
					throw new UserNotFoundException();
				} else
					return User.class.cast( doc );
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
		final Document doc = getMongoCollection().find( getUsernamePasswordAuthUserFind( identity ) ).first();
		if ( doc == null ) {
			LOGGER.warn( "Could not finr user by user and password {}", identity );
			throw new UserNotFoundException();
		} else
			return User.class.cast( doc );
	}
	
	private static Bson getUsernamePasswordAuthUserFind( final UsernamePasswordAuthUser identity ) {
		final Bson email = getEmailUserFind( identity.getEmail() );
		final Bson linkProvider = Filters.eq( LinkedAccount.DB_FIELD_PROVIDER, identity.getProvider() );
		final Bson match = Filters.elemMatch( DB_FIELD_LINKED_ACCOUNTS, linkProvider );
		return Filters.and( email, match );
		// return getEmailUserFind( identity.getEmail() ).and(
		// DB_FIELD_LINKED_ACCOUNTS ).elemMatch( new BasicDBObject(
		// LinkedAccount.DB_FIELD_PROVIDER, identity.getProvider() ) );
	}
	
	public static List< User > findByRole( final Role role ) throws UserNotFoundException {
		final MongoCursor< Document > cursor = getMongoCollection()
				.find(
						Filters.and( Filters.eq( DB_FIELD_ACTIVE, true ),
								Filters.elemMatch( DB_FIELD_ROLES, Filters.eq( DB_FIELD_ROLES, role.getName() ) ) ) )
				.sort( Filters.eq( DB_FIELD_ROLES, 1 ) ).iterator();
		// QueryBuilder.start( DB_FIELD_ACTIVE ).is( true ).and( DB_FIELD_ROLES
		// ).elemMatch( role.getDBObject() ).get() ).sort( sort );
		if ( cursor == null ) {
			LOGGER.warn( "Could not find users by role {}", role );
			throw new UserNotFoundException();
		} else {
			final List< User > users = new LinkedList<>();
			while ( cursor.hasNext() )
				users.add( User.class.cast( cursor.next() ) );
			return users;
		}
	}
	
	/**
	 * private Document getDocument() {
	 * final BasicDBList dbRoles = new BasicDBList();
	 * for ( final UserRole role : roles )
	 * dbRoles.add( role.getDBObject() );
	 * final BasicDBList dbAccounts = new BasicDBList();
	 * for ( final LinkedAccount acc : linkedAccounts )
	 * dbAccounts.add( acc.getDBObject() );
	 * final DBObject doc = new BasicDBObject( DB_FIELD_ID, getOrCreateId() );
	 * if ( email != null )
	 * doc.put( DB_FIELD_EMAIL, email );
	 * if ( firstName != null )
	 * doc.put( DB_FIELD_FIRST_NAME, firstName );
	 * if ( name != null )
	 * doc.put( DB_FIELD_NAME, name );
	 * if ( lastName != null )
	 * doc.put( DB_FIELD_LAST_NAME, lastName );
	 * doc.put( DB_FIELD_ACTIVE, active );
	 * doc.put( DB_FIELD_EMAIL_VALIDATED, emailValidated );
	 * if ( !dbRoles.isEmpty() )
	 * doc.put( DB_FIELD_ROLES, dbRoles );
	 * if ( !dbAccounts.isEmpty() )
	 * doc.put( DB_FIELD_LINKED_ACCOUNTS, dbAccounts );
	 * doc.put( DB_FIELD_LAST_LOGIN, lastLogin );
	 * return doc;
	 * }
	 */
	public void merge( final User otherUser ) {
		for ( final LinkedAccount acc : otherUser.getLinkedAccounts() )
			this.getLinkedAccounts().add( LinkedAccount.getInstance( acc ) );
		// do all other merging stuff here - like resources, etc.
		if ( getEmail() == null )
			setEmail( otherUser.getEmail() );
		if ( getName() == null )
			setName( otherUser.getName() );
		// deactivate the merged user that got added to this one
		otherUser.setActive( false );
		// Зберегти лише linkedAccounts, Email, Name. А також otherUser.Active
		save();
		otherUser.save();
	}
	
	public static User create( final AuthUser authUser ) {
		final User user = new User( authUser );
		return User.class.cast( user.save() );
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
		final Set< String > providerKeys = new HashSet< String >( getLinkedAccounts().size() );
		for ( final LinkedAccount acc : getLinkedAccounts() )
			providerKeys.add( acc.getProvider() );
		return providerKeys;
	}
	
	public static void addLinkedAccount( final AuthUser oldUser, final AuthUser newUser ) {
		try {
			final User u = User.findByAuthUserIdentity( oldUser );
			u.getLinkedAccounts().add( LinkedAccount.getInstance( newUser ) );
			// Зберегти лише u.linkedAccounts
			u.save();
		}
		catch ( final UserNotFoundException e ) {
			LOGGER.warn( "Cannot link {} to {}", newUser, oldUser );
		}
	}
	
	public void addRole( final Role role ) {
		( ( BsonArray )get( DB_FIELD_ROLES ) ).add( new BsonString( role.getName() ) );
		// Зберегти лише roles
		save();
	}
	
	public void updateLastLoginDate() {
		setLastLogin( System.currentTimeMillis() );
		// Зберегти лише LastLogin
		save();
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
		final Document doc = getMongoCollection().find( Filters.eq( DB_FIELD_ID, userId ) ).first();
		if ( doc == null ) {
			LOGGER.warn( "Could not find user by id {}", userId );
			throw new UserNotFoundException();
		} else
			return User.class.cast( doc );
	}
	
	public static User findByEmail( final String email ) throws UserNotFoundException {
		// there is out RuntimeException
		try {
			final Document doc = getMongoCollection().find( getEmailUserFind( email ) ).first();
			if ( doc != null )
				return User.class.cast( doc );
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
	
	private static Bson getEmailUserFind( final String email ) {
		return Filters.and( Filters.eq( DB_FIELD_ACTIVE, true ), Filters.eq( DB_FIELD_EMAIL, email ) );
		// return QueryBuilder.start( DB_FIELD_ACTIVE ).is( true ).and(
		// DB_FIELD_EMAIL ).is( email );
	}
	
	public LinkedAccount getAccountByProvider( final String providerKey ) {
		for ( final LinkedAccount acc : getLinkedAccounts() )
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
		setEmailValidated( true );
		// Зберегти лише EmailValidated
		save();
		TokenAction.deleteByUser( this, TokenType.EMAIL_VERIFICATION );
	}
	
	public void changePassword( final UsernamePasswordAuthUser authUser, final boolean create ) {
		final LinkedAccount existing = getAccountByProvider( authUser.getProvider() );
		if ( existing == null ) {
			if ( !create )
				throw new RuntimeException( "Account not enabled for password usage" );
		} else
			getLinkedAccounts().remove( existing );
		getLinkedAccounts().add( LinkedAccount.getInstance( authUser ) );
		// Зберегти лише LinkedAccount
		save();
	}
	
	public void resetPassword( final UsernamePasswordAuthUser authUser, final boolean create ) {
		// You might want to wrap this into a transaction
		this.changePassword( authUser, create );
		TokenAction.deleteByUser( this, TokenType.PASSWORD_RESET );
	}
	
	public static User remove( final String id ) throws UserNotFoundException {
		final Document doc = getMongoCollection().findOneAndDelete( new Document( DB_FIELD_ID, id ) );
		if ( doc == null ) {
			LOGGER.warn( "Could not find user by id {}", id );
			throw new UserNotFoundException();
		} else
			LOGGER.debug( "User {} was removed.", id );
		return User.class.cast( doc );
	}
	
	public boolean isAdmin() {
		return ( ( BsonArray )get( DB_FIELD_ROLES ) ).contains( new BsonString( UserRole.ADMIN_ROLE_NAME ) );
	}
	
	public boolean isOper() {
		return ( ( BsonArray )get( DB_FIELD_ROLES ) ).contains( new BsonString( UserRole.OPER_ROLE_NAME ) );
	}
	
	public static MongoCollection< Document > getMongoCollection() {
		final MongoCollection< Document > collection = getDatabase().getCollection( COLLECTION_NAME_USERS, Document.class );
		return collection;
	}
	
	@Override
	protected MongoCollection< Document > getCollection() {
		return getMongoCollection();
	}
}
