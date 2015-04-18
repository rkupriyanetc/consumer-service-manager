package mk.ck.energy.csm.model;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import mk.ck.energy.csm.model.auth.User;
import mk.ck.energy.csm.model.auth.UserNotFoundException;
import mk.ck.energy.csm.model.mongodb.CSMAbstractDocument;

import org.bson.BsonArray;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

/**
 * @author RVK
 */
public class Consumer extends CSMAbstractDocument< Consumer > {
	
	private static final long		serialVersionUID									= 1L;
	
	private static final String	COLLECTION_NAME_CONSUMERS					= "consumers";
	
	public static final short		UPDATING_READING_ALL							= 0;
	
	public static final short		UPDATING_READING_USER							= 1;
	
	public static final short		UPDATING_READING_FULLNAME					= 2;
	
	public static final short		UPDATING_READING_ADDRESS_LOCATION	= 4;
	
	public static final short		UPDATING_READING_ADDRESS_PLACE		= 8;
	
	public static final short		UPDATING_READING_ADDRESS_OTHER		= 16;
	
	public static final short		UPDATING_READING_ADDRESS_FULL			= 32;
	
	public static final short		UPDATING_READING_DOCUMENTS				= 64;
	
	public static final short		UPDATING_READING_OTHER						= 128;
	
	public static final short		UPDATING_READING_ACTIVE						= 256;
	
	public static final short		READING_ID												= 16384;
	
	/**
	 * This are fields names
	 */
	static final String					DB_FIELD_ID												= "_id";
	
	static final String					DB_FIELD_USER_ID									= "user_id";
	
	static final String					DB_FIELD_FULLNAME									= "full_name";
	
	static final String					DB_FIELD_ADDRESS									= "address";
	
	/**
	 * if User authorized then active is true
	 */
	static final String					DB_FIELD_ACTIVE										= "active";
	
	/**
	 * The Consumer document about passport and ID
	 */
	static final String					DB_FIELD_DOCUMENT									= "document";
	
	static final String					DB_FIELD_CONSUMER_TYPE						= "type";
	
	static final String					DB_FIELD_STATUS_TYPE							= "status";
	
	static final String					DB_FIELD_HOUSE_TYPE								= "house_type";
	
	/**
	 * auth.User
	 */
	private User								user;
	
	/**
	 * All possible meters of consumer
	 */
	private final List< Meter >	meters;
	
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
		createId();
		this.meters = new LinkedList<>();
	}
	
	// Тут тра переробити
	public static Consumer create( final String id ) {
		LOGGER.debug( "It creates consumer {}", id );
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
	public void setUserId( final ObjectId userId ) {
		final String id = getUserId();
		if ( !userId.equals( id ) ) {
			put( DB_FIELD_USER_ID, userId );
			try {
				// Тут тра переробити
				user = User.findById( id );
			}
			catch ( final UserNotFoundException unfe ) {
				LOGGER.error( "It's a complete lie" );
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
		return ( Address )get( DB_FIELD_ADDRESS );
	}
	
	public void setAddress( final Address address ) {
		put( DB_FIELD_ADDRESS, address );
	}
	
	public boolean isActive() {
		return getBoolean( DB_FIELD_ACTIVE, false );
	}
	
	public void setActive( final boolean active ) {
		put( DB_FIELD_ACTIVE, active );
	}
	
	public List< Meter > getMeters() {
		return meters;
	}
	
	public boolean addMeters( final Meter meter ) {
		return meters.add( meter );
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
		return getMongoCollection().updateOne( Filters.eq( DB_FIELD_ID, getUserId() ),
				Filters.eq( "$addToSet", Filters.eq( "$each", houseType.name() ) ) ).isModifiedCountAvailable();
	}
	
	/**
	 * private DBObject getDBObject( final short updateSet ) {
	 * final DBObject doc = new BasicDBObject();
	 * short up = 0;
	 * if ( UPDATING_READING_ALL == updateSet || ( updateSet &
	 * UPDATING_READING_USER ) == UPDATING_READING_USER )
	 * if ( user != null )
	 * doc.put( DB_FIELD_USER_ID, userId );
	 * if ( UPDATING_READING_ALL == updateSet || ( updateSet &
	 * UPDATING_READING_FULLNAME ) == UPDATING_READING_FULLNAME )
	 * doc.put( DB_FIELD_FULLNAME, fullName );
	 * if ( UPDATING_READING_ALL == updateSet || ( updateSet &
	 * UPDATING_READING_ACTIVE ) == UPDATING_READING_ACTIVE )
	 * doc.put( DB_FIELD_ACTIVE, active );
	 * if ( UPDATING_READING_ALL == updateSet
	 * || ( updateSet & UPDATING_READING_ADDRESS_LOCATION ) ==
	 * UPDATING_READING_ADDRESS_LOCATION )
	 * up += Address.UPDATING_READING_ADDRESS_LOCATION;
	 * if ( UPDATING_READING_ALL == updateSet || ( updateSet &
	 * UPDATING_READING_ADDRESS_PLACE ) == UPDATING_READING_ADDRESS_PLACE )
	 * up += Address.UPDATING_READING_ADDRESS_PLACE;
	 * if ( UPDATING_READING_ALL == updateSet || ( updateSet &
	 * UPDATING_READING_ADDRESS_OTHER ) == UPDATING_READING_ADDRESS_OTHER )
	 * up += Address.UPDATING_READING_HOUSE + Address.UPDATING_READING_APARTMENT +
	 * Address.UPDATING_READING_POSTAL_CODE;
	 * if ( up == 31 )
	 * up = 0;
	 * if ( address != null ) {
	 * final DBObject addr = address.getDBObject( up );
	 * doc.put( DB_FIELD_ADDRESS, addr );
	 * }
	 * if ( UPDATING_READING_ALL == updateSet || ( updateSet &
	 * UPDATING_READING_DOCUMENTS ) == UPDATING_READING_DOCUMENTS )
	 * if ( document != null ) {
	 * final DBObject docs = document.getDBObject();
	 * doc.put( DB_FIELD_DOCUMENT, docs );
	 * }
	 * if ( UPDATING_READING_ALL == updateSet || ( updateSet &
	 * UPDATING_READING_OTHER ) == UPDATING_READING_OTHER ) {
	 * doc.put( DB_FIELD_CONSUMER_TYPE, consumerType.name() );
	 * if ( statusType != null )
	 * doc.put( DB_FIELD_STATUS_TYPE, statusType.name() );
	 * if ( !houseType.isEmpty() ) {
	 * final BasicDBList hType = new BasicDBList();
	 * for ( final HouseType ht : houseType )
	 * hType.add( ht.name() );
	 * doc.put( DB_FIELD_HOUSE_TYPE, hType );
	 * }
	 * }
	 * return new BasicDBObject( "$set", doc );
	 * }
	 */
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
		save();
	}
	
	public static MongoCollection< Consumer > getMongoCollection() {
		final MongoDatabase db = Database.getInstance().getDatabase();
		final MongoCollection< Consumer > collection = db.getCollection( COLLECTION_NAME_CONSUMERS, Consumer.class );
		return collection;
	}
	
	@Override
	protected MongoCollection< Consumer > getCollection() {
		return getMongoCollection();
	}
}
