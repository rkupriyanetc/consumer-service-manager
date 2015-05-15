package mk.ck.energy.csm.model.auth;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import mk.ck.energy.csm.model.Consumer;
import mk.ck.energy.csm.model.ConsumerException;
import mk.ck.energy.csm.model.mongodb.CSMAbstractDocument;
import mk.ck.energy.csm.providers.MyStupidBasicAuthProvider;

import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonDocumentWrapper;
import org.bson.BsonString;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
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
import com.mongodb.client.result.UpdateResult;

/**
 * Authenticated user.
 * 
 * @author RVK
 */
public class User extends CSMAbstractDocument< User > implements Subject {
	
	private static final long				serialVersionUID					= 1L;
	
	private static final String			COLLECTION_NAME_USERS			= "users";
	
	private static final String			DB_FIELD_EMAIL						= "email";
	
	private static final String			DB_FIELD_NAME							= "name";
	
	private static final String			DB_FIELD_FIRST_NAME				= "first_name";
	
	private static final String			DB_FIELD_LAST_NAME				= "last_name";
	
	private static final String			DB_FIELD_LAST_LOGIN				= "last_login";
	
	private static final String			DB_FIELD_ACTIVE						= "active";
	
	private static final String			DB_FIELD_EMAIL_VALIDATED	= "validated";
	
	private static final String			DB_FIELD_ROLES						= "roles";
	
	private static final String			DB_FIELD_LINKED_ACCOUNTS	= "linkeds";
	
	private static final String			DB_FIELD_PERMISSIONS			= "permissions";
	
	private final List< Document >	roles;
	
	private final List< Document >	linkeds;
	
	private final List< Document >	permissions;
	
	protected User() {
		roles = new LinkedList<>();
		linkeds = new LinkedList<>();
		permissions = new LinkedList<>();
	}
	
	private User( final AuthUser authUser ) {
		roles = new LinkedList<>();
		linkeds = new LinkedList<>();
		permissions = new LinkedList<>();
		setLastLogin( System.currentTimeMillis() );
		setActive( true );
		if ( authUser.getProvider().equals( MyStupidBasicAuthProvider.GUEST_PROVIDER )
				&& authUser.getId().equals( MyStupidBasicAuthProvider.GUEST_ID ) )
			addRole( UserRole.GUEST );
		else
			addRole( UserRole.USER );
		// user.permissions = new ArrayList<UserPermission>();
		// user.permissions.add(UserPermission.findByValue("printers.edit"));
		addLinkedAccount( LinkedAccount.getInstance( authUser ) );
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
		return getBoolean( DB_FIELD_EMAIL_VALIDATED, false );
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
		if ( firstName != null && !firstName.isEmpty() )
			put( DB_FIELD_FIRST_NAME, firstName );
	}
	
	public String getLastName() {
		return getString( DB_FIELD_LAST_NAME );
	}
	
	public void setLastName( final String lastName ) {
		if ( lastName != null && !lastName.isEmpty() )
			put( DB_FIELD_LAST_NAME, lastName );
	}
	
	public long getLastLogin() {
		return getLong( DB_FIELD_LAST_LOGIN );
	}
	
	public void setLastLogin( final long lastLogin ) {
		put( DB_FIELD_LAST_LOGIN, lastLogin );
	}
	
	public boolean isActive() {
		return getBoolean( DB_FIELD_ACTIVE, false );
	}
	
	public void setActive( final boolean active ) {
		put( DB_FIELD_ACTIVE, active );
	}
	
	@Override
	public List< ? extends Role > getRoles() {
		final List< Role > rols = new LinkedList<>();
		if ( roles != null || !roles.isEmpty() )
			for ( final Document key : roles )
				rols.add( UserRole.getInstance( key ) );
		return rols;
	}
	
	/**
	 * @param listRoles
	 *          is ArrayList<Document> without prior processing
	 */
	public void setRoles( final Object listRoles ) {
		if ( listRoles != null ) {
			roles.addAll( extractAsListDocuments( listRoles ) );
			put( DB_FIELD_ROLES, listRoles );
		}
	}
	
	public boolean addRole( final Role role ) {
		final boolean bool = roles.add( ( ( UserRole )role ).getDocument() );
		put( DB_FIELD_ROLES, roles );
		return bool;
	}
	
	public List< LinkedAccount > getLinkedAccounts() {
		final List< LinkedAccount > las = new LinkedList<>();
		if ( linkeds != null || !linkeds.isEmpty() )
			for ( final Document key : linkeds )
				las.add( LinkedAccount.getInstance( key ) );
		return las;
	}
	
	/**
	 * @param listLinkedAccounts
	 *          is ArrayList<Document> without prior processing
	 */
	public void setLinkedAccounts( final Object listLinkedAccounts ) {
		if ( listLinkedAccounts != null ) {
			linkeds.addAll( extractAsListDocuments( listLinkedAccounts ) );
			put( DB_FIELD_LINKED_ACCOUNTS, listLinkedAccounts );
		}
	}
	
	public boolean addLinkedAccount( final LinkedAccount linkedAccount ) {
		final boolean bool = linkeds.add( linkedAccount.getDocument() );
		put( DB_FIELD_LINKED_ACCOUNTS, linkeds );
		return bool;
	}
	
	@Override
	public List< ? extends Permission > getPermissions() {
		final List< Permission > ps = new LinkedList<>();
		if ( permissions != null || !permissions.isEmpty() )
			for ( final Document key : permissions )
				ps.add( UserPermission.getInstance( key ) );
		return ps;
	}
	
	/**
	 * @param listPermission
	 *          is ArrayList<Document> without prior processing
	 */
	public void setPermission( final Object listPermission ) {
		if ( listPermission != null ) {
			permissions.addAll( extractAsListDocuments( listPermission ) );
			put( DB_FIELD_PERMISSIONS, listPermission );
		}
	}
	
	public UpdateResult updateRoles() {
		return update( Filters.eq( DB_FIELD_ID, getId() ), Filters.eq( "$set", Filters.eq( DB_FIELD_ROLES, roles ) ) );
	}
	
	public UpdateResult updateLinkedAccounts() {
		return update( Filters.eq( DB_FIELD_ID, getId() ), Filters.eq( "$set", Filters.eq( DB_FIELD_LINKED_ACCOUNTS, linkeds ) ) );
	}
	
	public static User create( final AuthUser authUser ) {
		final User user = new User( authUser );
		user.insertIntoDB();
		return user;
	}
	
	public static User create() {
		return new User();
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
	
	public static void addLinkedAccount( final AuthUser oldUser, final AuthUser newUser ) {
		try {
			final User u = User.findByAuthUserIdentity( oldUser );
			u.addLinkedAccount( LinkedAccount.getInstance( newUser ) );
			// Зберегти лише u.linkedAccounts
			u.updateLinkedAccounts();
		}
		catch ( final UserNotFoundException e ) {
			LOGGER.warn( "Cannot link {} to {}", newUser, oldUser );
		}
	}
	
	private static Bson getAuthUserFind( final AuthUserIdentity identity ) {
		return Filters.and( Filters.eq( DB_FIELD_ACTIVE, true ),
				Filters.elemMatch( DB_FIELD_LINKED_ACCOUNTS, LinkedAccount.getInstance( identity ).getDocument() ) );
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
				return findByUsernamePasswordIdentity( ( UsernamePasswordAuthUser )identity );
			else {
				final User doc = getMongoCollection().find( getAuthUserFind( identity ), User.class ).first();
				if ( doc == null ) {
					LOGGER.warn( "Could not find user by identity {}", identity );
					throw new UserNotFoundException();
				} else
					return doc;
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
		final User user = getMongoCollection().find( getUsernamePasswordAuthUserFind( identity ), User.class ).first();
		if ( user == null ) {
			LOGGER.warn( "Could not finr user by user and password {}", identity );
			throw new UserNotFoundException();
		} else
			return user;
	}
	
	private static Bson getUsernamePasswordAuthUserFind( final UsernamePasswordAuthUser identity ) {
		return Filters.and( getEmailUserFind( identity.getEmail() ),
				Filters.elemMatch( DB_FIELD_LINKED_ACCOUNTS, Filters.eq( LinkedAccount.DB_FIELD_PROVIDER, identity.getProvider() ) ) );
		// return getEmailUserFind( identity.getEmail() ).and(
		// DB_FIELD_LINKED_ACCOUNTS ).elemMatch( new BasicDBObject(
		// LinkedAccount.DB_FIELD_PROVIDER, identity.getProvider() ) );
	}
	
	public static List< User > findByRole( final Role role ) throws UserNotFoundException {
		final MongoCursor< User > cursor = getMongoCollection()
				.find(
						Filters.and( Filters.eq( DB_FIELD_ACTIVE, true ),
								Filters.elemMatch( DB_FIELD_ROLES, Filters.eq( DB_FIELD_ROLES, role.getName() ) ) ), User.class )
				.sort( Filters.eq( DB_FIELD_ROLES, 1 ) ).iterator();
		// QueryBuilder.start( DB_FIELD_ACTIVE ).is( true ).and( DB_FIELD_ROLES
		// ).elemMatch( role.getDBObject() ).get() ).sort( sort );
		if ( cursor == null ) {
			LOGGER.warn( "Could not find users by role {}", role );
			throw new UserNotFoundException();
		} else {
			final List< User > users = new LinkedList<>();
			while ( cursor.hasNext() )
				users.add( cursor.next() );
			return users;
		}
	}
	
	public void merge( final User otherUser ) {
		for ( final LinkedAccount acc : otherUser.getLinkedAccounts() )
			addLinkedAccount( acc );
		updateLinkedAccounts();
		// do all other merging stuff here - like resources, etc.
		if ( getEmail() == null ) {
			setEmail( otherUser.getEmail() );
			update( Filters.eq( DB_FIELD_ID, getId() ), Filters.eq( DB_FIELD_EMAIL, getEmail() ) );
		}
		final String otherName = otherUser.getName();
		if ( getName() == null && otherName != null ) {
			setName( otherName );
			update( Filters.eq( DB_FIELD_ID, getId() ), Filters.eq( DB_FIELD_NAME, otherName ) );
		}
		final String otherFName = otherUser.getFirstName();
		if ( getFirstName() == null && otherFName != null ) {
			setFirstName( otherFName );
			update( Filters.eq( DB_FIELD_ID, getId() ), Filters.eq( DB_FIELD_FIRST_NAME, otherFName ) );
		}
		final String otherLName = otherUser.getLastName();
		if ( getLastName() == null && otherLName != null ) {
			setLastName( otherLName );
			update( Filters.eq( DB_FIELD_ID, getId() ), Filters.eq( DB_FIELD_LAST_NAME, otherLName ) );
		}
		// deactivate the merged user that got added to this one
		otherUser.setActive( false );
		// Зберегти лише linkedAccounts, Email, Name. А також otherUser.Active
		otherUser.update( Filters.eq( DB_FIELD_ID, getId() ), Filters.eq( DB_FIELD_ACTIVE, false ) );
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
		final List< LinkedAccount > lla = getLinkedAccounts();
		final Set< String > providerKeys = new HashSet<>( lla.size() );
		for ( final LinkedAccount acc : lla )
			providerKeys.add( acc.getProvider() );
		return providerKeys;
	}
	
	public void updateLastLoginDate() {
		final long lastLogin = System.currentTimeMillis();
		setLastLogin( lastLogin );
		// Зберегти лише LastLogin
		update( Filters.eq( DB_FIELD_ID, getId() ), Filters.eq( DB_FIELD_LAST_LOGIN, lastLogin ) );
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
		final User doc = getMongoCollection().find( Filters.eq( DB_FIELD_ID, userId ), User.class ).first();
		if ( doc == null ) {
			LOGGER.warn( "Could not find user by id {}", userId );
			throw new UserNotFoundException();
		} else
			return doc;
	}
	
	public static User findByEmail( final String email ) throws UserNotFoundException {
		try {
			final User doc = getMongoCollection().find( getEmailUserFind( email ), User.class ).first();
			if ( doc != null )
				return doc;
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
		update( Filters.eq( DB_FIELD_ID, getId() ), Filters.eq( DB_FIELD_EMAIL_VALIDATED, true ) );
		TokenAction.deleteByUser( this, TokenType.EMAIL_VERIFICATION );
	}
	
	public void changePassword( final UsernamePasswordAuthUser authUser, final boolean create ) {
		final LinkedAccount existing = getAccountByProvider( authUser.getProvider() );
		if ( existing == null ) {
			if ( !create )
				throw new RuntimeException( "Account not enabled for password usage" );
		} else
			linkeds.remove( existing.getDocument() );
		addLinkedAccount( LinkedAccount.getInstance( authUser ) );
		// Зберегти лише LinkedAccount
		update( Filters.eq( DB_FIELD_ID, getId() ), Filters.eq( DB_FIELD_LINKED_ACCOUNTS, linkeds ) );
	}
	
	public void resetPassword( final UsernamePasswordAuthUser authUser, final boolean create ) {
		// You might want to wrap this into a transaction
		this.changePassword( authUser, create );
		TokenAction.deleteByUser( this, TokenType.PASSWORD_RESET );
	}
	
	public static User remove( final String id ) throws UserNotFoundException {
		final User doc = getMongoCollection().findOneAndDelete( Filters.eq( DB_FIELD_ID, id ) );
		if ( doc == null ) {
			LOGGER.warn( "Could not find user by id {}", id );
			throw new UserNotFoundException();
		} else
			LOGGER.debug( "User {} was removed.", id );
		return doc;
	}
	
	public Consumer getConsumer() {
		try {
			return Consumer.findByUser( this );
		}
		catch ( final ConsumerException e ) {
			LOGGER.warn( "Could not find Consumer by User {}", getEmail() );
		}
		return null;
	}
	
	public boolean isAdmin() {
		return ( ( BsonArray )get( DB_FIELD_ROLES ) ).contains( new BsonString( UserRole.ADMIN_ROLE_NAME ) );
	}
	
	public boolean isOper() {
		return ( ( BsonArray )get( DB_FIELD_ROLES ) ).contains( new BsonString( UserRole.OPER_ROLE_NAME ) );
	}
	
	@Override
	public < TDocument >BsonDocument toBsonDocument( final Class< TDocument > documentClass, final CodecRegistry codecRegistry ) {
		return new BsonDocumentWrapper< User >( this, codecRegistry.get( User.class ) );
	}
	
	public static MongoCollection< User > getMongoCollection() {
		final MongoCollection< User > collection = getDatabase().getCollection( COLLECTION_NAME_USERS, User.class );
		return collection;
	}
	
	@Override
	protected MongoCollection< User > getCollection() {
		return getMongoCollection();
	}
}
