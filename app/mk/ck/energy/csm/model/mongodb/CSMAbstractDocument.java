package mk.ck.energy.csm.model.mongodb;

import java.util.UUID;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoException;
import com.mongodb.MongoWriteConcernException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;

public abstract class CSMAbstractDocument< I extends CSMAbstractDocument > extends Document {
	
	private static final long			serialVersionUID	= 1L;
	
	protected static final Logger	LOGGER						= LoggerFactory.getLogger( CSMAbstractDocument.class );
	
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
		LOGGER.trace( "Saved class {}. ID is {}", getClass(), getId() );
		return ( I )this;
	}
	
	protected abstract MongoCollection< I > getCollection();
}
