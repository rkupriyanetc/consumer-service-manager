package mk.ck.energy.csm.model.auth;

import mk.ck.energy.csm.model.Database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import com.mongodb.WriteResult;

/**
 * @author KYL
 */
public class TokenAction {
	
	private static final Logger	LOGGER								= LoggerFactory.getLogger( TokenAction.class );
	
	/**
	 * Verification time frame (until the user clicks on the link in the email) in
	 * seconds Defaults to one week
	 */
	private final static long		VERIFICATION_TIME			= 7 * 24 * 3600;
	
	static final String					DB_FIELD_TOKEN				= "token";
	
	static final String					DB_FIELD_USER_ID			= "user_id";
	
	static final String					DB_FIELD_TOKEN_TYPE		= "type";
	
	static final String					DB_FIELD_DATE_CREATED	= "created";
	
	static final String					DB_FIELD_DATE_EXPIRES	= "expires";
	
	private final String				token;
	
	private final String				userId;
	
	private final TokenType			type;
	
	private final long					created;
	
	private final long					expires;
	
	private TokenAction( final TokenType type, final String token, final String userId ) {
		this.token = token;
		this.userId = userId;
		this.type = type;
		this.created = System.currentTimeMillis();
		this.expires = this.created + VERIFICATION_TIME * 1000;
	}
	
	private TokenAction( final DBObject doc ) {
		this.token = ( String )doc.get( DB_FIELD_TOKEN );
		this.userId = ( String )doc.get( DB_FIELD_USER_ID );
		this.type = TokenType.valueOf( ( String )doc.get( DB_FIELD_TOKEN_TYPE ) );
		this.created = ( Long )doc.get( DB_FIELD_DATE_CREATED );
		this.expires = ( Long )doc.get( DB_FIELD_DATE_EXPIRES );
	}
	
	public String getToken() {
		return token;
	}
	
	public String getTargetUser() {
		return userId;
	}
	
	public TokenType getTokenType() {
		return type;
	}
	
	public double getCreated() {
		return created;
	}
	
	public boolean isValid() {
		return this.expires > System.currentTimeMillis();
	}
	
	public static TokenAction findByToken( final String token, final TokenType type ) throws InvalidTokenException {
		final DBObject query = QueryBuilder.start( DB_FIELD_TOKEN ).is( token ).and( DB_FIELD_TOKEN_TYPE ).is( type.name() ).get();
		final DBObject doc = getTokensCollection().findOne( query );
		if ( doc == null )
			throw new InvalidTokenException( type, token );
		else
			return new TokenAction( doc );
	}
	
	public static void deleteByUser( final User u, final TokenType type ) {
		final DBObject query = QueryBuilder.start( DB_FIELD_USER_ID ).is( u.getId() ).and( DB_FIELD_TOKEN_TYPE ).is( type.name() )
				.get();
		final WriteResult removed = getTokensCollection().remove( query );
		LOGGER.debug( "Removed {}", removed );
	}
	
	private DBObject getDBObject() {
		return new BasicDBObject( DB_FIELD_TOKEN, token )//
				.append( DB_FIELD_USER_ID, userId )//
				.append( DB_FIELD_TOKEN_TYPE, type.name() )//
				.append( DB_FIELD_DATE_CREATED, created )//
				.append( DB_FIELD_DATE_EXPIRES, expires )//
		;
	}
	
	public static TokenAction create( final TokenType type, final String token, final User targetUser ) {
		final TokenAction ua = new TokenAction( type, token, targetUser.getId() );
		final WriteResult saved = getTokensCollection().save( ua.getDBObject() );
		LOGGER.debug( "Saved token {}", saved );
		return ua;
	}
	
	private static DBCollection getTokensCollection() {
		return Database.getInstance().getDatabase().getCollection( "tokens" );
	}
}
