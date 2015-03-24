package mk.ck.energy.csm.models.auth;

import mk.ck.energy.csm.models.Database;

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
	
	private static final Logger	LOGGER						= LoggerFactory.getLogger( TokenAction.class );
	
	/**
	 * Verification time frame (until the user clicks on the link in the email) in
	 * seconds Defaults to one week
	 */
	private final static long		VERIFICATION_TIME	= 7 * 24 * 3600;
	
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
		this.token = ( String )doc.get( "token" );
		this.userId = ( String )doc.get( "user" );
		this.type = TokenType.valueOf( ( String )doc.get( "type" ) );
		this.created = ( Long )doc.get( "created" );
		this.expires = ( Long )doc.get( "expires" );
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
		final DBObject query = QueryBuilder.start( "token" ).is( token ).and( "type" ).is( type.name() ).get();
		final DBObject doc = getTokensCollection().findOne( query );
		if ( doc == null )
			throw new InvalidTokenException( type, token );
		else
			return new TokenAction( doc );
	}
	
	public static void deleteByUser( final User u, final TokenType type ) {
		final DBObject query = QueryBuilder.start( "user" ).is( u.getId() ).and( "type" ).is( type.name() ).get();
		final WriteResult removed = getTokensCollection().remove( query );
		LOGGER.debug( "Removed {}", removed );
	}
	
	private DBObject getDBObject() {
		return new BasicDBObject( "token", token )//
				.append( "user", userId )//
				.append( "type", type.name() )//
				.append( "created", created )//
				.append( "expires", expires )//
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
