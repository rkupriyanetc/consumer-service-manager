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
	private static final String	DB_FIELD_DOCUMENT					= "document";
	
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
	
	/**
	 * private Consumer( final DBObject dbo, final short readingSet ) throws
	 * ImpossibleCreatingException {
	 * if ( dbo != null ) {
	 * id = ( String )dbo.get( DB_FIELD_ID );
	 * userId = ( String )dbo.get( DB_FIELD_USER_ID );
	 * if ( UPDATING_READING_ALL == readingSet || ( readingSet &
	 * UPDATING_READING_FULLNAME ) == UPDATING_READING_FULLNAME )
	 * fullName = ( String )dbo.get( DB_FIELD_FULLNAME );
	 * if ( UPDATING_READING_ALL == readingSet || ( readingSet &
	 * UPDATING_READING_ACTIVE ) == UPDATING_READING_ACTIVE )
	 * active = ( ( Boolean )dbo.get( DB_FIELD_ACTIVE ) ).booleanValue();
	 * if ( UPDATING_READING_ALL == readingSet || ( readingSet &
	 * UPDATING_READING_ADDRESS_FULL ) == UPDATING_READING_ADDRESS_FULL )
	 * address = ( Address )dbo.get( DB_FIELD_ADDRESS ); final long idPlace = ( (
	 * Long )dbo.get( "address.addressPlace_id" )
	 * ).longValue();
	 * final Address address = new Address();
	 * try {
	 * address.setAddressLocation( AddressLocation.findById( idLocation ) );
	 * address.setAddressPlace( AddressPlace.findById( idPlace ) );
	 * }
	 * catch ( final AddressNotFoundException anfe ) {
	 * LOGGER.error( "Can not find address" );
	 * }
	 * address.setHouse( ( String )dbo.get( "address.house" ) );
	 * address.setApartment( ( String )dbo.get( "address.apartment" ) );
	 * address.setPostalCode( ( String )dbo.get( "address.postal_code" ) );
	 * if ( UPDATING_READING_ALL == readingSet || ( readingSet &
	 * UPDATING_READING_DOCUMENTS ) == UPDATING_READING_DOCUMENTS )
	 * document = ( Documents )dbo.get( DB_FIELD_DOCUMENT );
	 * // ( String )dbo.get( "document.passport_series" ), ( String )dbo.get(
	 * // "document.passport_number" ) );
	 * if ( UPDATING_READING_ALL == readingSet || ( readingSet &
	 * UPDATING_READING_OTHER ) == UPDATING_READING_OTHER ) {
	 * consumerType = ConsumerType.valueOf( ( String )dbo.get(
	 * DB_FIELD_CONSUMER_TYPE ) );
	 * statusType = ConsumerStatusType.valueOf( ( String )dbo.get(
	 * DB_FIELD_STATUS_TYPE ) );
	 * }
	 * } else
	 * throw new ImpossibleCreatingException(
	 * "The parameter dbObject should not be null in create Consumer" );
	 * }
	 */
	public User getUser() {
		return user;
	}
	
	public String getUserId() {
		return getString( DB_FIELD_USER_ID );
	}
	
	// Тут тра переробити
	public void setUserId( final String userId ) {
		final String id = getUserId();
		if ( !userId.equals( id ) ) {
			put( DB_FIELD_USER_ID, userId );
			try {
				// Тут тра переробити
				user = User.findById( userId );
				update( Filters.eq( DB_FIELD_ID, getId() ), Filters.eq( DB_FIELD_USER_ID, userId ) );
			}
			catch ( final UserNotFoundException unfe ) {
				LOGGER.warn( "It's a complete lie" );
			}
		}
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
	
	public Documents getDocument() {
		return ( Documents )get( DB_FIELD_DOCUMENT );
	}
	
	public void setDocuments( final Documents document ) {
		put( DB_FIELD_DOCUMENT, document );
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
	
	public static Consumer findById( final String id, final short updateSet ) throws ConsumerException {
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
