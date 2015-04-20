package mk.ck.energy.csm.model.auth;

import mk.ck.energy.csm.model.Database;
import mk.ck.energy.csm.model.mongodb.CSMAbstractDocument;

import org.bson.BsonDocument;
import org.bson.BsonDocumentWrapper;
import org.bson.codecs.configuration.CodecRegistry;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

/**
 * @author KYL
 */
public class TokenAction extends CSMAbstractDocument< TokenAction > {
	
	private static final long		serialVersionUID				= 1L;
	
	private static final String	COLLECTION_NAME_TOKENS	= "tokens";
	
	/**
	 * Verification time frame (until the user clicks on the link in the email) in
	 * seconds Defaults to one week
	 */
	private final static long		VERIFICATION_TIME				= 7 * 24 * 3600;
	
	static final String					DB_FIELD_TOKEN					= "token";
	
	static final String					DB_FIELD_USER_ID				= "user_id";
	
	static final String					DB_FIELD_TOKEN_TYPE			= "type";
	
	static final String					DB_FIELD_DATE_CREATED		= "created";
	
	static final String					DB_FIELD_DATE_EXPIRES		= "expires";
	
	private TokenAction() {}
	
	private TokenAction( final TokenType type, final String token, final String userId ) {
		setToken( token );
		setUserId( userId );
		setTokenType( type );
		setCreated( System.currentTimeMillis() );
		setExpires( getCreated() + VERIFICATION_TIME * 1000 );
	}
	
	public String getToken() {
		return getString( DB_FIELD_TOKEN );
	}
	
	public void setToken( final String token ) {
		put( DB_FIELD_TOKEN, token );
	}
	
	public String getUserId() {
		return getString( DB_FIELD_USER_ID );
	}
	
	public void setUserId( final String userId ) {
		put( DB_FIELD_USER_ID, userId );
	}
	
	public TokenType getTokenType() {
		return TokenType.valueOf( getString( DB_FIELD_TOKEN_TYPE ) );
	}
	
	public void setTokenType( final TokenType type ) {
		put( DB_FIELD_TOKEN_TYPE, type.name() );
	}
	
	public long getCreated() {
		return getLong( DB_FIELD_DATE_CREATED );
	}
	
	public void setCreated( final long created ) {
		put( DB_FIELD_DATE_CREATED, created );
	}
	
	public long getExpires() {
		return getLong( DB_FIELD_DATE_EXPIRES );
	}
	
	public void setExpires( final long expires ) {
		put( DB_FIELD_DATE_EXPIRES, expires );
	}
	
	public boolean isValid() {
		return getExpires() > System.currentTimeMillis();
	}
	
	public static TokenAction create() {
		return new TokenAction();
	}
	
	public static TokenAction create( final TokenType type, final String token, final User targetUser ) {
		final TokenAction ua = new TokenAction( type, token, targetUser.getId() );
		ua.insertIntoDB();
		return ua;
	}
	
	public static TokenAction findByToken( final String token, final TokenType type ) throws InvalidTokenException {
		final TokenAction doc = getMongoCollection().find(
				Filters.and( Filters.eq( DB_FIELD_TOKEN, token ), Filters.eq( DB_FIELD_TOKEN_TYPE, type.name() ) ), TokenAction.class )
				.first();
		if ( doc == null )
			throw new InvalidTokenException( type, token );
		else
			return doc;
	}
	
	public static void deleteByUser( final User u, final TokenType type ) {
		final TokenAction removed = getMongoCollection().findOneAndDelete(
				Filters.and( Filters.eq( DB_FIELD_USER_ID, u.getId() ), Filters.eq( DB_FIELD_TOKEN_TYPE, type.name() ) ) );
		LOGGER.debug( "Removed {}", removed );
	}
	
	@Override
	public < TDocument >BsonDocument toBsonDocument( final Class< TDocument > documentClass, final CodecRegistry codecRegistry ) {
		return new BsonDocumentWrapper< TokenAction >( this, codecRegistry.get( TokenAction.class ) );
	}
	
	public static MongoCollection< TokenAction > getMongoCollection() {
		final MongoDatabase db = Database.getInstance().getDatabase();
		final MongoCollection< TokenAction > collection = db.getCollection( COLLECTION_NAME_TOKENS, TokenAction.class );
		return collection;
	}
	
	@Override
	protected MongoCollection< TokenAction > getCollection() {
		return getMongoCollection();
	}
}
