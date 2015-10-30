package mk.ck.energy.csm.model.mongodb;

import java.util.List;
import java.util.UUID;

import org.bson.BsonArray;
import org.bson.BsonString;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoException;
import com.mongodb.MongoWriteConcernException;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;

import mk.ck.energy.csm.model.Database;

public abstract class CSMAbstractDocument< I extends Document >extends Document {
	
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
		if ( getString( DB_FIELD_ID ) == null )
			setId( UUID.randomUUID().toString().toLowerCase() );
		return id;
	}
	
	public UpdateResult update( final Bson query, final Bson updateValue ) {
		UpdateResult ur;
		try {
			// createId();
			ur = getCollection().updateOne( query, Filters.eq( "$set", updateValue ), new UpdateOptions().upsert( true ) );
			LOGGER.trace( "Saved class {}. ID is {}. UpdateResult is {}", getClass(), id, ur.isModifiedCountAvailable() );
			return ur;
		}
		catch ( final MongoWriteException mwe ) {
			ur = UpdateResult.unacknowledged();
			LOGGER.warn( "MongoWriteException {}. UpdateResult is {}", mwe, ur );
		}
		catch ( final MongoWriteConcernException mwce ) {
			ur = UpdateResult.unacknowledged();
			LOGGER.warn( "MongoWriteConcernException {}. UpdateResult is {}", mwce, ur );
		}
		catch ( final MongoException me ) {
			ur = UpdateResult.unacknowledged();
			LOGGER.warn( "MongoException {}. UpdateResult is {}", me, ur );
		}
		return ur;
	}
	
	@SuppressWarnings( "unchecked" )
	public void insertIntoDB() {
		createId();
		try {
			getCollection().insertOne( ( I )this );
		}
		catch ( final MongoWriteException mwe ) {
			LOGGER.warn( "When you add a new document an error {}", mwe );
		}
		catch ( final MongoWriteConcernException mwce ) {
			LOGGER.warn( "When you add a new document an error {}", mwce );
		}
		catch ( final MongoException me ) {
			LOGGER.warn( "When you add a new document an error {}", me );
		}
	}
	
	public I remove() {
		return getCollection().findOneAndDelete( Filters.eq( DB_FIELD_ID, id ) );
	}
	
	@SuppressWarnings( "unchecked" )
	protected List< Document > extractAsListDocuments( final Object object ) {
		return ( List< Document > )object;
	}
	
	@SuppressWarnings( "unchecked" )
	public List< String > extractAsListStringValues( final Object object ) {
		return ( List< String > )object;
	}
	
	public BsonArray listStringAsBsonArray( final List< String > list ) {
		final BsonArray array = new BsonArray();
		for ( final String str : list )
			array.add( new BsonString( str ) );
		return array;
	}
	
	public static MongoDatabase getDatabase() {
		return db;
	}
	
	protected abstract MongoCollection< I > getCollection();
}
