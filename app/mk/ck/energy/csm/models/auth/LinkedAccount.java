package mk.ck.energy.csm.models.auth;

import com.feth.play.module.pa.user.AuthUserIdentity;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author KYL
 */
public class LinkedAccount {
	
	static final String		DB_FIELD_PROVIDER	= "provider";
	
	static final String		DB_FIELD_USER_ID	= "user_id";
	
	private final String	provider;
	
	private final String	userId;
	
	private LinkedAccount( final String provider, final String userId ) {
		this.provider = provider;
		this.userId = userId;
	}
	
	public static LinkedAccount getInstance( final AuthUserIdentity authUserIdentity ) {
		return new LinkedAccount( authUserIdentity.getProvider(), authUserIdentity.getId() );
	}
	
	public static LinkedAccount getInstance( final LinkedAccount linkedAccount ) {
		return new LinkedAccount( linkedAccount.provider, linkedAccount.userId );
	}
	
	public static LinkedAccount getInstance( final DBObject doc ) {
		return new LinkedAccount( ( String )doc.get( DB_FIELD_PROVIDER ), ( String )doc.get( DB_FIELD_USER_ID ) );
	}
	
	public String getProvider() {
		return provider;
	}
	
	public String getUserId() {
		return userId;
	}
	
	DBObject getDBObject() {
		return new BasicDBObject( DB_FIELD_PROVIDER, provider ).append( DB_FIELD_USER_ID, userId );
	}
}