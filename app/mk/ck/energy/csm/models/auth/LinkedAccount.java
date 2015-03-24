package mk.ck.energy.csm.models.auth;

import com.feth.play.module.pa.user.AuthUserIdentity;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author KYL
 */
public class LinkedAccount {
	
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
		return new LinkedAccount( ( String )doc.get( "provider" ), ( String )doc.get( "userId" ) );
	}
	
	public String getProvider() {
		return provider;
	}
	
	public String getUserId() {
		return userId;
	}
	
	DBObject getDBObject() {
		return new BasicDBObject( "provider", provider ).append( "userId", userId );
	}
}