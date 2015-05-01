package mk.ck.energy.csm.model;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import mk.ck.energy.csm.model.auth.User;
import mk.ck.energy.csm.model.auth.UserNotFoundException;
import mk.ck.energy.csm.model.mongodb.CSMAbstractDocument;

import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonDocumentWrapper;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

/**
 * @author RVK
 */
public class Consumer extends CSMAbstractDocument< Consumer > {
	
	private static final long		serialVersionUID					= 1L;
	
	private static final String	COLLECTION_NAME_CONSUMERS	= "consumers";
	
	private static final String	DB_FIELD_USER_ID					= "user_id";
	
	private static final String	DB_FIELD_FULLNAME					= "full_name";
	
	private static final String	DB_FIELD_ADDRESS					= "address";
	
	/**
	 * if User authorized then active is true
	 */
	private static final String	DB_FIELD_ACTIVE						= "active";
	
	/**
	 * The Consumer document about passport and ID
	 */
	private static final String	DB_FIELD_DOCUMENTS				= "documents";
	
	private static final String	DB_FIELD_CONSUMER_TYPE		= "type";
	
	private static final String	DB_FIELD_STATUS_TYPE			= "status";
	
	private static final String	DB_FIELD_HOUSE_TYPE				= "house_type";
	
	/**
	 * auth.User
	 */
	private User								user;
	
	/**
	 * All possible meters of consumer
	 */
	private List< Meter >				meters;
	
	/**
	 * private Consumer( final String id, final User user, final String fullName,
	 * final Address address, final boolean active,
	 * final ConsumerStatusType statusType, final ConsumerType consumerType ) {
	 * if ( user != null ) {
	 * this.user = user;
	 * this.userId = user.getId();
	 * }
	 * this.id = id;
	 * this.active = active;
	 * this.consumerType = consumerType == null ? ConsumerType.INDIVIDUAL :
	 * consumerType;
	 * this.statusType = statusType == null ? ConsumerStatusType.TEMPORARILY :
	 * statusType;
	 * this.fullName = fullName;
	 * this.address = address;
	 * this.meters = new ArrayList< Meter >( 0 );
	 * this.houseType = new HashSet< HouseType >( 0 );
	 * }
	 */
	// Тут тра переробити
	private Consumer( final String id ) {
		setId( id );
		this.meters = new LinkedList<>();
	}
	
	// Тут тра переробити
	public static Consumer create( final String id ) {
		return new Consumer( id );
	}
	
	public User getUser() {
		return user;
	}
	
	public String getUserId() {
		return getString( DB_FIELD_USER_ID );
	}
	
	public void setUserId( final String userId ) {
		if ( userId != null && !userId.isEmpty() ) {
			final String id = getUserId();
			if ( !userId.equals( id ) )
				try {
					user = User.findById( userId );
					put( DB_FIELD_USER_ID, userId );
				}
				catch ( final UserNotFoundException unfe ) {
					LOGGER.warn( "It's a complete lie" );
					remove( DB_FIELD_USER_ID );
					throw new IllegalArgumentException( "It's a complete lie! ID User is : " + userId );
				}
		} else
			remove( DB_FIELD_USER_ID );
	}
	
	public String getFullName() {
		return getString( DB_FIELD_FULLNAME );
	}
	
	public void setFullName( final String fullName ) {
		put( DB_FIELD_FULLNAME, fullName );
	}
	
	public Address getAddress() {
		return Address.create( ( Document )get( DB_FIELD_ADDRESS ) );
	}
	
	public void setAddress( final Address address ) {
		put( DB_FIELD_ADDRESS, address.getDocument() );
	}
	
	public boolean isActive() {
		return getBoolean( DB_FIELD_ACTIVE, false );
	}
	
	public void setActive( final boolean active ) {
		put( DB_FIELD_ACTIVE, active );
	}
	
	public Documents getDocuments() {
		return ( Documents )get( DB_FIELD_DOCUMENTS );
	}
	
	public void setDocuments( final Documents documents ) {
		put( DB_FIELD_DOCUMENTS, documents );
	}
	
	public ConsumerType getConsumerType() {
		return ConsumerType.valueOf( getString( DB_FIELD_CONSUMER_TYPE ) );
	}
	
	public void setConsumerType( final ConsumerType consumerType ) {
		put( DB_FIELD_CONSUMER_TYPE, consumerType.name() );
	}
	
	public ConsumerStatusType getStatusType() {
		return ConsumerStatusType.valueOf( getString( DB_FIELD_STATUS_TYPE ) );
	}
	
	public void setStatusType( final ConsumerStatusType statusType ) {
		put( DB_FIELD_STATUS_TYPE, statusType.name() );
	}
	
	public Set< HouseType > getHouseType() {
		final BsonArray list = ( BsonArray )get( DB_FIELD_HOUSE_TYPE );
		final Set< HouseType > hts = new LinkedHashSet<>();
		for ( final BsonValue key : list.getValues() ) {
			final HouseType ht = HouseType.valueOf( ( ( BsonString )key ).getValue() );
			hts.add( ht );
		}
		return hts;
	}
	
	public boolean addHouseType( final HouseType houseType ) {
		return update( Filters.eq( DB_FIELD_ID, getId() ), Filters.eq( "$addToSet", Filters.eq( "$each", houseType.name() ) ) )
				.isModifiedCountAvailable();
	}
	
	public List< Meter > getMeters() {
		try {
			if ( meters.isEmpty() )
				meters = Meter.findByConsumerId( getId() );
		}
		catch ( final MeterNotFoundException mnfe ) {}
		return meters;
	}
	
	public boolean addMeters( final Meter meter ) {
		return meters.add( meter );
	}
	
	public static Consumer findById( final String id ) throws ConsumerException {
		if ( id != null && !id.isEmpty() ) {
			final Consumer doc = getMongoCollection().find( Filters.eq( DB_FIELD_ID, id ) ).first();
			if ( doc == null )
				throw new ConsumerException( "The Consumer was found by " + id );
			return doc;
		} else
			throw new IllegalArgumentException( "ID should not be empty in AddressTop.findById( id )" );
	}
	
	public void joinConsumerElectricity( final User user ) {
		setUserId( user.getId() );
		setActive( true );
		update( Filters.eq( DB_FIELD_ID, getId() ),
				Filters.and( Filters.eq( DB_FIELD_ACTIVE, true ), Filters.eq( DB_FIELD_USER_ID, user.getId() ) ) );
	}
	
	@Override
	public < TDocument >BsonDocument toBsonDocument( final Class< TDocument > documentClass, final CodecRegistry codecRegistry ) {
		return new BsonDocumentWrapper< Consumer >( this, codecRegistry.get( Consumer.class ) );
	}
	
	public static MongoCollection< Consumer > getMongoCollection() {
		final MongoCollection< Consumer > collection = getDatabase().getCollection( COLLECTION_NAME_CONSUMERS, Consumer.class );
		return collection;
	}
	
	@Override
	protected MongoCollection< Consumer > getCollection() {
		return getMongoCollection();
	}
}
