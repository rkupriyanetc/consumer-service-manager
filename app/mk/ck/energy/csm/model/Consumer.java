package mk.ck.energy.csm.model;

import java.util.LinkedList;
import java.util.List;

import mk.ck.energy.csm.controllers.Account.AppendConsumer;
import mk.ck.energy.csm.model.auth.User;
import mk.ck.energy.csm.model.auth.UserNotFoundException;
import mk.ck.energy.csm.model.mongodb.CSMAbstractDocument;

import org.bson.BsonDocument;
import org.bson.BsonDocumentWrapper;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.UpdateResult;

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
	
	private Consumer( final String id ) {
		setId( id );
		this.meters = new LinkedList<>();
	}
	
	public static Consumer create( final String id ) {
		return new Consumer( id );
	}
	
	public User getUser() {
		return user;
	}
	
	public String getUserId() {
		return getString( DB_FIELD_USER_ID );
	}
	
	public void setUser( final User user ) {
		if ( user != null ) {
			if ( !user.getId().equals( getUserId() ) ) {
				this.user = user;
				put( DB_FIELD_USER_ID, user.getId() );
			}
		} else {
			this.user = null;
			remove( DB_FIELD_USER_ID );
		}
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
		final Object o = get( DB_FIELD_ADDRESS );
		if ( o != null )
			return Address.create( ( Document )o );
		else
			return null;
	}
	
	public void setAddress( final Address address ) {
		if ( address != null ) {
			final Document doc = address.getDocument();
			if ( doc != null && !doc.isEmpty() )
				put( DB_FIELD_ADDRESS, doc );
		}
	}
	
	public boolean isActive() {
		return getBoolean( DB_FIELD_ACTIVE, false );
	}
	
	public void setActive( final boolean active ) {
		put( DB_FIELD_ACTIVE, active );
	}
	
	public Documents getDocuments() {
		final Object o = get( DB_FIELD_DOCUMENTS );
		if ( o != null )
			return Documents.create( ( Document )o );
		else
			return null;
	}
	
	public void setDocuments( final Documents documents ) {
		if ( documents != null ) {
			final Document doc = documents.getDocument();
			if ( doc != null && !doc.isEmpty() )
				put( DB_FIELD_DOCUMENTS, doc );
		}
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
	
	public HouseType getHouseType() {
		return HouseType.valueOf( getString( DB_FIELD_HOUSE_TYPE ) );
	}
	
	public void setHouseType( final HouseType houseType ) {
		put( DB_FIELD_HOUSE_TYPE, houseType.name() );
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
				throw new ConsumerException( "The Consumer was not found by " + id );
			return doc;
		} else
			throw new IllegalArgumentException( "ID should not be empty in Consumer.findById( id )" );
	}
	
	public static Consumer findByUser( final User user ) throws ConsumerException {
		if ( user != null ) {
			final Consumer doc = getMongoCollection().find(
					Filters.and( Filters.eq( DB_FIELD_USER_ID, user.getId() ), Filters.eq( DB_FIELD_ACTIVE, true ) ) ).first();
			if ( doc == null )
				throw new ConsumerException( "The Consumer was not found by " + user.getEmail() );
			return doc;
		} else
			throw new IllegalArgumentException( "User should not be empty in Consumer.findByUser( User )" );
	}
	
	public static Bson makeFilterToId( final String value ) {
		return Filters.eq( DB_FIELD_ID, value );
	}
	
	public static Bson makeFilterToFullName( final String value ) {
		return Filters.eq( DB_FIELD_FULLNAME, value );
	}
	
	public static Bson makeFilterToAddress( final Address value ) {
		if ( value != null )
			return Filters.eq( DB_FIELD_ADDRESS, value.getDocument() );
		else
			return null;
	}
	
	public static Bson makeFilterToDocuments( final Documents value ) {
		if ( value != null )
			return Filters.eq( DB_FIELD_DOCUMENTS, value.getDocument() );
		else
			return null;
	}
	
	public static Bson makeFilterToConsumerType( final ConsumerType value ) {
		if ( value != null )
			return Filters.eq( DB_FIELD_CONSUMER_TYPE, value.name() );
		else
			return null;
	}
	
	public static Bson makeFilterToHouseType( final HouseType value ) {
		if ( value != null )
			return Filters.eq( DB_FIELD_HOUSE_TYPE, value.name() );
		else
			return null;
	}
	
	public static Bson makeFilterToStatusType( final ConsumerStatusType value ) {
		if ( value != null )
			return Filters.eq( DB_FIELD_STATUS_TYPE, value.name() );
		else
			return null;
	}
	
	public static Bson makeFilterToActive( final String value ) {
		return Filters.eq( DB_FIELD_ACTIVE, value );
	}
	
	public static Bson makeFilterToUserId( final String value ) {
		return Filters.eq( DB_FIELD_USER_ID, value );
	}
	
	public void save() throws ImpossibleCreatingException {
		final Consumer consumer = getCollection().find( Filters.eq( DB_FIELD_ID, getId() ), Consumer.class ).first();
		if ( consumer == null )
			insertIntoDB();
		else {
			final String consumerId = getId();
			LOGGER.warn( "Cannot save Consumer. Consumer already exists: {}", consumerId );
		}
	}
	
	@Override
	public boolean equals( final Object o ) {
		if ( o == null )
			return false;
		if ( o instanceof Consumer ) {
			final Consumer consumer = Consumer.class.cast( o );
			return consumer.getId().equals( getId() ) && consumer.getFullName().equals( getFullName() )
					&& consumer.getAddress().equals( getAddress() );
		} else
			if ( o instanceof AppendConsumer ) {
				final AppendConsumer consumer = AppendConsumer.class.cast( o );
				return consumer.getId().equals( getId() )
						&& consumer.getFullName().equals( getFullName() )
						&& ( consumer.getApartment() != null && consumer.getApartment().equals( getAddress().getApartment() ) || ( consumer
								.getApartment() == null || consumer.getApartment().isEmpty() )
								&& ( getAddress().getApartment() == null || getAddress().getApartment().isEmpty() ) )
						&& ( consumer.getHouse() != null && consumer.getHouse().equals( getAddress().getHouse() ) || ( consumer.getHouse() == null || consumer
								.getHouse().isEmpty() ) && ( getAddress().getHouse() == null || getAddress().getHouse().isEmpty() ) );
			}
		return false;
	}
	
	public boolean joinConsumerElectricity( final User user ) {
		// Consumer already be stored in the database
		if ( isActive() )
			return false; // Consumer are joined
		setUser( user );
		setActive( true );
		final UpdateResult ur = update( Filters.eq( DB_FIELD_ID, getId() ),
				Filters.and( Filters.eq( DB_FIELD_ACTIVE, true ), Filters.eq( DB_FIELD_USER_ID, user.getId() ) ) );
		final long n = ur.getModifiedCount();
		LOGGER.trace( "Consumer {} join a user {}. Modified {} document(s) in consumers.", getId(), user.getEmail(), n );
		return n > 0;
	}
	
	public boolean unjoinConsumerElectricity() {
		// Consumer no already be stored in the database
		if ( !isActive() )
			return false; // Consumer are unjoined
		final User user = getUser();
		setUser( null );
		setActive( false );
		final UpdateResult ur = update( Filters.eq( DB_FIELD_ID, getId() ),
				Filters.and( Filters.eq( DB_FIELD_ACTIVE, false ), Filters.eq( DB_FIELD_USER_ID, null ) ) );
		final long n = ur.getModifiedCount();
		LOGGER.trace( "Consumer {} unjoin a user {}. Modified {} document(s) in consumers.", getId(), user.getEmail(), n );
		return n > 0;
	}
	
	public Consumer copyInstance() {
		final Consumer c = Consumer.create( getId() );
		c.setUserId( getUserId() );
		c.setFullName( getFullName() );
		c.setAddress( getAddress() );
		c.setActive( isActive() );
		c.setDocuments( getDocuments() );
		c.setConsumerType( getConsumerType() );
		c.setStatusType( getStatusType() );
		c.setHouseType( getHouseType() );
		return c;
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
