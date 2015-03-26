package mk.ck.energy.csm.models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mk.ck.energy.csm.models.auth.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.QueryBuilder;

/**
 * @author RVK
 */
public class Consumer {
	
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
	
	private static final Logger	LOGGER														= LoggerFactory.getLogger( User.class );
	
	/**
	 * This are fields names
	 */
	static final String					DB_FIELD_ID												= "_id";
	
	static final String					DB_FIELD_USER_ID									= "user_id";
	
	static final String					DB_FIELD_FULLNAME									= "full_name";
	
	static final String					DB_FIELD_ADDRESS									= "address";
	
	static final String					DB_FIELD_ACTIVE										= "active";
	
	static final String					DB_FIELD_DOCUMENT									= "document";
	
	static final String					DB_FIELD_CONSUMER_TYPE						= "type";
	
	static final String					DB_FIELD_STATUS_TYPE							= "status";
	
	static final String					DB_FIELD_HOUSE_TYPE								= "house_type";
	
	/**
	 * Customer's account from Paked soft: Особовий рахунок
	 */
	private final String				id;
	
	/**
	 * auth.User
	 */
	private User								user;
	
	private String							userId;
	
	private String							fullName;
	
	private Address							address;
	
	/**
	 * if User authorized then active is true
	 */
	private boolean							active;
	
	/**
	 * All possible meters of consumer
	 */
	private List< Meter >				meters;
	
	private Document						document;
	
	private ConsumerType				consumerType;
	
	private ConsumerStatusType	statusType;
	
	private Set< HouseType >		houseType;
	
	private Consumer( final String id, final User user, final String fullName, final Address address, final boolean active,
			final ConsumerStatusType statusType, final ConsumerType consumerType ) {
		if ( user != null ) {
			this.user = user;
			this.userId = user.getId();
		}
		this.id = id;
		this.active = active;
		this.consumerType = consumerType == null ? ConsumerType.INDIVIDUAL : consumerType;
		this.statusType = statusType == null ? ConsumerStatusType.TEMPORARILY : statusType;
		this.fullName = fullName;
		this.address = address;
		this.meters = new ArrayList< Meter >( 0 );
		this.houseType = new HashSet< HouseType >( 0 );
	}
	
	private Consumer( final String id ) {
		this.id = id;
		this.active = false;
		this.consumerType = ConsumerType.INDIVIDUAL;
		this.user = null;
		this.meters = new ArrayList< Meter >( 0 );
		this.houseType = new HashSet< HouseType >( 0 );
	}
	
	public static Consumer create( final String id ) {
		return new Consumer( id );
	}
	
	private Consumer( final DBObject dbo, final short readingSet ) throws ImpossibleCreatingException {
		if ( dbo != null ) {
			id = ( String )dbo.get( DB_FIELD_ID );
			userId = ( String )dbo.get( DB_FIELD_USER_ID );
			if ( UPDATING_READING_ALL == readingSet || ( readingSet & UPDATING_READING_FULLNAME ) == UPDATING_READING_FULLNAME )
				fullName = ( String )dbo.get( DB_FIELD_FULLNAME );
			if ( UPDATING_READING_ALL == readingSet || ( readingSet & UPDATING_READING_ACTIVE ) == UPDATING_READING_ACTIVE )
				active = ( ( Boolean )dbo.get( DB_FIELD_ACTIVE ) ).booleanValue();
			if ( UPDATING_READING_ALL == readingSet || ( readingSet & UPDATING_READING_ADDRESS_FULL ) == UPDATING_READING_ADDRESS_FULL )
				address = ( Address )dbo.get( DB_FIELD_ADDRESS );
			/**
			 * final long idPlace = ( ( Long )dbo.get( "address.addressPlace_id" )
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
			 */
			if ( UPDATING_READING_ALL == readingSet || ( readingSet & UPDATING_READING_DOCUMENTS ) == UPDATING_READING_DOCUMENTS )
				document = ( Document )dbo.get( DB_FIELD_DOCUMENT );
			// ( String )dbo.get( "document.passport_series" ), ( String )dbo.get(
			// "document.passport_number" ) );
			if ( UPDATING_READING_ALL == readingSet || ( readingSet & UPDATING_READING_OTHER ) == UPDATING_READING_OTHER ) {
				consumerType = ConsumerType.valueOf( ( String )dbo.get( DB_FIELD_CONSUMER_TYPE ) );
				statusType = ConsumerStatusType.valueOf( ( String )dbo.get( DB_FIELD_STATUS_TYPE ) );
			}
		} else
			throw new ImpossibleCreatingException( "The parameter dbObject should not be null in create Consumer" );
	}
	
	public String getId() {
		return id;
	}
	
	public User getUser() {
		return user;
	}
	
	public String getUserId() {
		return userId;
	}
	
	public String getFullName() {
		return fullName;
	}
	
	public void setFullName( final String fullName ) {
		this.fullName = fullName;
	}
	
	public Address getAddress() {
		return address;
	}
	
	public void setAddress( final Address address ) {
		this.address = address;
	}
	
	public boolean isActive() {
		return active;
	}
	
	public void setActive( final boolean active ) {
		this.active = active;
	}
	
	public List< Meter > getMeters() {
		return meters;
	}
	
	public boolean addMeters( final Meter meter ) {
		return meters.add( meter );
	}
	
	public Document getDocument() {
		return document;
	}
	
	public void setDocuments( final Document document ) {
		this.document = document;
	}
	
	public ConsumerType getConsumerType() {
		return consumerType;
	}
	
	public void setConsumerType( final ConsumerType consumerType ) {
		this.consumerType = consumerType;
	}
	
	public ConsumerStatusType getStatusType() {
		return statusType;
	}
	
	public void setStatusType( final ConsumerStatusType statusType ) {
		this.statusType = statusType;
	}
	
	public Set< HouseType > getHouseType() {
		return houseType;
	}
	
	public boolean addHouseType( final HouseType houseType ) {
		return this.houseType.add( houseType );
	}
	
	private DBObject getDBObject( final short updateSet ) {
		final DBObject doc = new BasicDBObject();
		short up = 0;
		if ( UPDATING_READING_ALL == updateSet || ( updateSet & UPDATING_READING_USER ) == UPDATING_READING_USER )
			if ( user != null )
				doc.put( DB_FIELD_USER_ID, userId );
		if ( UPDATING_READING_ALL == updateSet || ( updateSet & UPDATING_READING_FULLNAME ) == UPDATING_READING_FULLNAME )
			doc.put( DB_FIELD_FULLNAME, fullName );
		if ( UPDATING_READING_ALL == updateSet || ( updateSet & UPDATING_READING_ACTIVE ) == UPDATING_READING_ACTIVE )
			doc.put( DB_FIELD_ACTIVE, active );
		if ( UPDATING_READING_ALL == updateSet
				|| ( updateSet & UPDATING_READING_ADDRESS_LOCATION ) == UPDATING_READING_ADDRESS_LOCATION )
			up += Address.UPDATING_READING_ADDRESS_LOCATION;
		if ( UPDATING_READING_ALL == updateSet || ( updateSet & UPDATING_READING_ADDRESS_PLACE ) == UPDATING_READING_ADDRESS_PLACE )
			up += Address.UPDATING_READING_ADDRESS_PLACE;
		if ( UPDATING_READING_ALL == updateSet || ( updateSet & UPDATING_READING_ADDRESS_OTHER ) == UPDATING_READING_ADDRESS_OTHER )
			up += Address.UPDATING_READING_HOUSE + Address.UPDATING_READING_APARTMENT + Address.UPDATING_READING_POSTAL_CODE;
		if ( up == 31 )
			up = 0;
		if ( address != null ) {
			final DBObject addr = address.getDBObject( up );
			doc.put( DB_FIELD_ADDRESS, addr );
		}
		if ( UPDATING_READING_ALL == updateSet || ( updateSet & UPDATING_READING_DOCUMENTS ) == UPDATING_READING_DOCUMENTS )
			if ( document != null ) {
				final DBObject docs = document.getDBObject();
				doc.put( DB_FIELD_DOCUMENT, docs );
			}
		if ( UPDATING_READING_ALL == updateSet || ( updateSet & UPDATING_READING_OTHER ) == UPDATING_READING_OTHER ) {
			doc.put( DB_FIELD_CONSUMER_TYPE, consumerType.name() );
			if ( statusType != null )
				doc.put( DB_FIELD_STATUS_TYPE, statusType.name() );
			if ( !houseType.isEmpty() ) {
				final BasicDBList hType = new BasicDBList();
				for ( final HouseType ht : houseType )
					hType.add( ht.name() );
				doc.put( DB_FIELD_HOUSE_TYPE, hType );
			}
		}
		return new BasicDBObject( "$set", doc );
	}
	
	public static Consumer findById( final String id, final short updateSet ) throws ConsumerException {
		if ( id == null || !id.isEmpty() )
			try {
				final DBObject doc = getConsumerCollection().findOne( QueryBuilder.start( DB_FIELD_ID ).is( id ).get() );
				final Consumer consumer = new Consumer( doc, updateSet );
				return consumer;
			}
			catch ( final MongoException me ) {
				throw new ConsumerException( "Exception in Consumer.findById( id ) of DBCollection", me );
			}
			catch ( final ImpossibleCreatingException ice ) {
				throw new ConsumerException( "The Consumer was found by " + id + ". Error creating consumer!" );
			}
		else
			throw new ConsumerException( "Id should not be null or empty in Consumer.findById( id )" );
	}
	
	public void joinConsumerElectricity( final User user ) {
		this.user = user;
		this.userId = user.getId();
		this.active = true;
		save( ( short )( UPDATING_READING_ACTIVE + UPDATING_READING_USER ) );
	}
	
	public void save( final short updateSet ) {
		final DBObject query = new BasicDBObject( DB_FIELD_ID, id );
		getConsumerCollection().update( query, getDBObject( updateSet ), true, false );
	}
	
	private static DBCollection getConsumerCollection() {
		return Database.getInstance().getDatabase().getCollection( "consumers" );
	}
}
