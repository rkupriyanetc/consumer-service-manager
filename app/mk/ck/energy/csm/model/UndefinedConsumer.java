package mk.ck.energy.csm.model;

import mk.ck.energy.csm.model.mongodb.CSMAbstractDocument;

import org.bson.BsonDocument;
import org.bson.BsonDocumentWrapper;
import org.bson.codecs.configuration.CodecRegistry;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

/**
 * @author RVK
 *         Designed to export data of consumers from another database
 */
public class UndefinedConsumer extends CSMAbstractDocument< UndefinedConsumer > {
	
	private static final long		serialVersionUID										= 1L;
	
	private static final String	COLLECTION_NAME_CONSUMERS_UNDEFINED	= "consumersUndefined";
	
	private static final String	DB_FIELD_UNDEFINED_CONSUMER_TYPE		= "type";
	
	private UndefinedConsumer() {}
	
	public static UndefinedConsumer create() {
		return new UndefinedConsumer();
	}
	
	public static UndefinedConsumer create( final String consumerId, final UndefinedConsumerType undefinedType ) {
		final UndefinedConsumer uc = new UndefinedConsumer();
		uc.setId( consumerId );
		uc.setUndefinedType( undefinedType );
		return uc;
	}
	
	public UndefinedConsumerType getUndefinedType() {
		return UndefinedConsumerType.valueOf( getString( DB_FIELD_UNDEFINED_CONSUMER_TYPE ) );
	}
	
	public void setUndefinedType( final UndefinedConsumerType undefinedType ) {
		put( DB_FIELD_UNDEFINED_CONSUMER_TYPE, undefinedType.name() );
	}
	
	public void save() {
		final UndefinedConsumer uConsumer = getCollection().find( Filters.eq( DB_FIELD_ID, getId() ), UndefinedConsumer.class )
				.first();
		if ( uConsumer == null )
			insertIntoDB();
		else {
			final String consumerName = this.toString();
			LOGGER.warn( "Cannot save Meter. Meter already exists: {}", consumerName );
		}
	}
	
	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer( "Ос. рахунок: " );
		sb.append( getId() );
		sb.append( " UT: " );
		sb.append( getString( DB_FIELD_UNDEFINED_CONSUMER_TYPE ) );
		return sb.toString();
	}
	
	@Override
	public < TDocument >BsonDocument toBsonDocument( final Class< TDocument > documentClass, final CodecRegistry codecRegistry ) {
		return new BsonDocumentWrapper< UndefinedConsumer >( this, codecRegistry.get( UndefinedConsumer.class ) );
	}
	
	public static MongoCollection< UndefinedConsumer > getMongoCollection() {
		final MongoCollection< UndefinedConsumer > collection = getDatabase().getCollection( COLLECTION_NAME_CONSUMERS_UNDEFINED,
				UndefinedConsumer.class );
		return collection;
	}
	
	@Override
	protected MongoCollection< UndefinedConsumer > getCollection() {
		return getMongoCollection();
	}
}
