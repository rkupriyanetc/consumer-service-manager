package mk.ck.energy.csm.model;

import java.util.LinkedList;
import java.util.List;

import mk.ck.energy.csm.model.mongodb.CSMAbstractDocument;

import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonDocumentWrapper;
import org.bson.BsonString;
import org.bson.BsonValue;
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
	
	private static final String	DB_FIELD_UNDEFINED_CONSUMER_TYPES		= "types";
	
	private static final String	DB_FIELD_UNDEFINED_STRING						= "error";
	
	private final BsonArray			types;
	
	private boolean							isRegisteredTypes;
	
	private UndefinedConsumer() {
		types = new BsonArray();
	}
	
	public static UndefinedConsumer create() {
		return new UndefinedConsumer();
	}
	
	public static UndefinedConsumer create( final String consumerId, final UndefinedConsumerType undefinedType, final String error ) {
		final UndefinedConsumer uc = new UndefinedConsumer();
		uc.setId( consumerId );
		uc.addUndefinedConsumerType( undefinedType );
		uc.setError( error );
		return uc;
	}
	
	public String getError() {
		return getString( DB_FIELD_UNDEFINED_STRING );
	}
	
	public void setError( final String error ) {
		if ( error != null && !error.isEmpty() )
			put( DB_FIELD_UNDEFINED_STRING, error );
	}
	
	/**
	 * Типи неірного створення особового рахунку
	 */
	public List< UndefinedConsumerType > getUndefinedConsumerTypes() {
		final List< UndefinedConsumerType > typ = new LinkedList<>();
		if ( types != null && !types.isEmpty() )
			for ( final BsonValue value : types.getValues() )
				typ.add( UndefinedConsumerType.valueOf( value.asString().getValue() ) );
		return typ;
	}
	
	public void setUndefinedConsumerTypes( final Object undefinedTypes ) {
		if ( undefinedTypes != null ) {
			types.addAll( ( BsonArray )undefinedTypes );
			if ( !types.isEmpty() ) {
				if ( !isRegisteredTypes ) {
					put( DB_FIELD_UNDEFINED_CONSUMER_TYPES, types );
					isRegisteredTypes = true;
				}
			} else {
				remove( DB_FIELD_UNDEFINED_CONSUMER_TYPES );
				isRegisteredTypes = false;
			}
		} else {
			remove( DB_FIELD_UNDEFINED_CONSUMER_TYPES );
			isRegisteredTypes = false;
		}
	}
	
	public boolean addUndefinedConsumerType( final UndefinedConsumerType value ) {
		final boolean bool = types.add( new BsonString( value.name() ) );
		if ( !isRegisteredTypes ) {
			put( DB_FIELD_UNDEFINED_CONSUMER_TYPES, types );
			isRegisteredTypes = true;
		}
		return bool;
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
		sb.append( get( DB_FIELD_UNDEFINED_CONSUMER_TYPES ) );
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
