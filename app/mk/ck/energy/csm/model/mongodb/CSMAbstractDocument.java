package mk.ck.energy.csm.model.mongodb;

import java.util.UUID;

import mk.ck.energy.csm.model.Database;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoException;
import com.mongodb.MongoWriteConcernException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;

public abstract class CSMAbstractDocument< I extends Document > extends Document {
	
	private static final MongoDatabase	db								= Database.getInstance().getDatabase();
	
	private static final long						serialVersionUID	= 1L;
	
	protected static final Logger				LOGGER						= LoggerFactory.getLogger( CSMAbstractDocument.class );
	
	protected static final String				DB_FIELD_ID				= "_id";
	
	private String											id;
	
	public String getId() {
		return id;
	}
	
	public void setId( final String id ) {
		this.id = id;
		put( DB_FIELD_ID, id );
	}
	
	public String createId() {
		if ( id == null )
			setId( UUID.randomUUID().toString().toLowerCase() );
		return id;
	}
	
	public I save() {
		try {
			createId();
			getCollection().updateOne( Filters.eq( DB_FIELD_ID, id ), new Document( "$set", this ), new UpdateOptions().upsert( true ) );
		}
		catch ( final MongoWriteException mwe ) {}
		catch ( final MongoWriteConcernException mwce ) {}
		catch ( final MongoException me ) {}
		LOGGER.trace( "Saved class {}. ID is {}", getClass(), id );
		return ( I )this;
	}
	
	public static MongoDatabase getDatabase() {
		return db;
	}
	
	protected abstract MongoCollection< I > getCollection();
}
