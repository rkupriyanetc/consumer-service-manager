package mk.ck.energy.csm.models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mk.ck.energy.csm.models.auth.User;
import mk.ck.energy.csm.models.auth.UserNotFoundException;

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
	
	public static final short		UPDATING_READING_DOCUMENTS				= 16;
	
	public static final short		UPDATING_READING_ACTIVE						= 32;
	
	public static final short		UPDATING_READING_METERS						= 64;
	
	public static final short		UPDATING_READING_OTHER						= 128;
	
	public static final short		UPDATING_READING_ADDRESS_OTHER		= 256;
	
	public static final short		READING_ID												= 512;
	
	private static final Logger	LOGGER														= LoggerFactory.getLogger( User.class );
	
	/**
	 * Customer's account from Paked soft: Особовий рахунок
	 */
	private String							id;
	
	/**
	 * auth.User.id
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
	private final List< Meter >	meters;
	
	private Document						document;
	
	private ConsumerType				consumerType;
	
	private ConsumerStatusType	statusType;
	
	private Set< HouseType >		houseType;
	
	private Consumer( final String id, final String userId ) throws ImpossibleCreatingException {
		try {
			this.user = User.findById( userId );
			this.userId = user.getId();
		}
		catch ( final UserNotFoundException unfe ) {
			this.user = null;
			this.consumerType = null;
			LOGGER.warn( "Sorry. Consumer user can not be created. Could not find auth user by id {}.", userId );
			throw new ImpossibleCreatingException();
		}
		finally {
			this.id = id;
			this.active = false;
			this.consumerType = ConsumerType.INDIVIDUAL;
			this.meters = new ArrayList< Meter >( 0 );
			this.houseType = new HashSet< HouseType >( 0 );
		}
	}
	
	private Consumer( final String id ) {
		this.id = id;
		this.active = false;
		this.consumerType = ConsumerType.INDIVIDUAL;
		this.user = null;
		this.meters = new ArrayList< Meter >( 0 );
		this.houseType = new HashSet< HouseType >( 0 );
	}
	
	public static Consumer create( final String id, final String userId ) throws ImpossibleCreatingException {
		return new Consumer( id, userId );
	}
	
	public static Consumer create( final String id ) {
		return new Consumer( id );
	}
	
	public static Consumer create( final DBObject dbo, final short readingSet ) {
		if ( dbo == null )
			return null;
		final String id = ( String )dbo.get( "_id" );
		final String userid = ( String )dbo.get( "user_id" );
		Consumer consumer = null;
		if ( userid != null && !userid.isEmpty() )
			try {
				consumer = new Consumer( id, userid );
			}
			catch ( final ImpossibleCreatingException ice ) {}
		else
			consumer = new Consumer( id );
		if ( readingSet < READING_ID )
			if ( consumer != null ) {
				consumer.fullName = ( String )dbo.get( "full_name" );
				consumer.active = ( ( Boolean )dbo.get( "active" ) ).booleanValue();
				final long idLocation = ( ( Long )dbo.get( "address.addressLocation_id" ) ).longValue();
				final long idPlace = ( ( Long )dbo.get( "address.addressPlace_id" ) ).longValue();
				final Address address = new Address();
				try {
					address.setAddressLocation( AddressLocation.findById( idLocation ) );
					address.setAddressPlace( AddressPlace.findById( idPlace ) );
				}
				catch ( final AddressNotFoundException anfe ) {
					LOGGER.error( "Can not find address" );
				}
				address.setHouse( ( String )dbo.get( "address.house" ) );
				address.setApartment( ( String )dbo.get( "address.apartment" ) );
				address.setPostalCode( ( String )dbo.get( "address.postal_code" ) );
				consumer.setAddress( address );
				final Document docum = new Document( ( String )dbo.get( "document.id_code" ),
						( String )dbo.get( "document.passport_series" ), ( String )dbo.get( "document.passport_number" ) );
				consumer.setDocuments( docum );
				consumer.consumerType = ( ConsumerType )dbo.get( "consumer_type" );
				consumer.statusType = ConsumerStatusType.valueOf( ( String )dbo.get( "status" ) );
				final BasicDBList getList = ( BasicDBList )dbo.get( "meters" );
				for ( final Object o : getList )
					consumer.meters.add( ( Meter )o );
			}
		return consumer;
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
				doc.put( "user_id", user.getId() );
		if ( UPDATING_READING_ALL == updateSet || ( updateSet & UPDATING_READING_FULLNAME ) == UPDATING_READING_FULLNAME )
			doc.put( "full_name", fullName );
		if ( UPDATING_READING_ALL == updateSet || ( updateSet & UPDATING_READING_ACTIVE ) == UPDATING_READING_ACTIVE )
			doc.put( "active", active );
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
			doc.put( "address", addr );
		}
		if ( UPDATING_READING_ALL == updateSet || ( updateSet & UPDATING_READING_METERS ) == UPDATING_READING_METERS )
			if ( !meters.isEmpty() ) {
				final BasicDBList met = new BasicDBList();
				for ( final Meter meter : meters )
					met.add( meter.getDBObject( Meter.UPDATING_READING_ALL ) );
				doc.put( "meters", met );
			}
		if ( UPDATING_READING_ALL == updateSet || ( updateSet & UPDATING_READING_DOCUMENTS ) == UPDATING_READING_DOCUMENTS )
			if ( document != null ) {
				final DBObject docs = document.getDBObject();
				doc.put( "document", docs );
			}
		if ( UPDATING_READING_ALL == updateSet || ( updateSet & UPDATING_READING_OTHER ) == UPDATING_READING_OTHER ) {
			doc.put( "consumer_type", consumerType.name() );
			if ( statusType != null )
				doc.put( "status", statusType.name() );
			if ( !houseType.isEmpty() ) {
				final BasicDBList hType = new BasicDBList();
				for ( final HouseType ht : houseType )
					hType.add( ht.name() );
				doc.put( "house_type", hType );
			}
		}
		return new BasicDBObject( "$set", doc );
	}
	
	public static Consumer findById( final String id, final short updateSet ) throws ConsumerException {
		if ( id == null || !id.isEmpty() )
			try {
				final DBObject doc = getConsumerCollection().findOne( QueryBuilder.start( "_id" ).is( id ).get() );
				final Consumer consumer = Consumer.create( doc, updateSet );
				return consumer;
			}
			catch ( final MongoException me ) {
				throw new ConsumerException( "Exception in Consumer.findById( id ) of DBCollection", me );
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
		final DBObject query = new BasicDBObject( "_id", id );
		getConsumerCollection().update( query, getDBObject( updateSet ), true, false );
	}
	
	private static DBCollection getConsumerCollection() {
		return Database.getInstance().getDatabase().getCollection( "consumers" );
	}
}
