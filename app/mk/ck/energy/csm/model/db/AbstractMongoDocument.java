package mk.ck.energy.csm.model.db;

import java.util.UUID;

import org.bson.Document;

import com.mongodb.MongoException;
import com.mongodb.MongoWriteConcernException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;

public abstract class AbstractMongoDocument< I extends Document > extends Document {
	
	private static final long			serialVersionUID	= 1L;
	
	protected static final String	DB_FIELD_ID				= "_id";
	
	public String getId() {
		return getString( DB_FIELD_ID );
	}
	
	public void setId( final String id ) {
		put( DB_FIELD_ID, id );
	}
	
	protected String getOrCreateId() {
		String id = getId();
		if ( id == null ) {
			id = UUID.randomUUID().toString().toLowerCase();
			setId( id );
		}
		return id;
	}
	
	public I save() {
		getOrCreateId();
		try {
			getCollection().insertOne( ( I )this );
		}
		catch ( final MongoWriteException mwe ) {}
		catch ( final MongoWriteConcernException mwce ) {}
		catch ( final MongoException me ) {}
		return ( I )this;
	}
	
	protected abstract MongoCollection< I > getCollection();
}
