package mk.ck.energy.csm.models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.i18n.Messages;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.QueryBuilder;

public class AddressPlace {
	
	private static final Logger	LOGGER	= LoggerFactory.getLogger( AddressPlace.class );
	
	private long								id;
	
	/**
	 * Вулиця
	 */
	private String							street;
	
	/**
	 * Тип вулиці
	 */
	private StreetType					streetType;
	
	private AddressPlace() {}
	
	public static AddressPlace create( final StreetType streetType, final String street ) {
		final AddressPlace addr = new AddressPlace();
		addr.setStreet( street );
		addr.setStreetType( streetType );
		return addr;
	}
	
	public static AddressPlace create( final DBObject dbo ) {
		if ( dbo == null )
			return null;
		final AddressPlace addr = new AddressPlace();
		addr.streetType = StreetType.valueOf( ( String )dbo.get( "streetType" ) );
		addr.street = ( String )dbo.get( "street" );
		addr.id = ( ( Long )dbo.get( "_id" ) ).longValue();
		return addr;
	}
	
	public long getId() {
		return id;
	}
	
	public String getStreet() {
		return street;
	}
	
	public void setStreet( final String street ) {
		this.street = street;
	}
	
	public StreetType getStreetType() {
		return streetType;
	}
	
	public void setStreetType( final StreetType streetType ) {
		this.streetType = streetType;
	}
	
	private long getOrCreateId() {
		long id = 1;
		try {
			final DBObject doc = getDBObject();
			final DBObject rec = getAddressCollection().find( doc ).one();
			if ( rec != null && !rec.toMap().isEmpty() )
				id = ( ( Long )rec.get( "_id" ) ).intValue();
			else {
				final DBCursor cursor = getAddressCollection().find().sort( new BasicDBObject( "_id", -1 ) ).limit( 1 );
				if ( cursor.hasNext() ) {
					final Object o = cursor.next().get( "_id" );
					final Long i = ( Long )o;
					id = i.longValue() + 1;
				}
			}
			LOGGER.debug( "Finding ID in TRY block {}", id );
		}
		catch ( final MongoException me ) {
			LOGGER.debug( "MongoException. Cannon finding ID in try block {}", me );
		}
		catch ( final NullPointerException npe ) {
			LOGGER.debug( "NullPointerException. Cannon finding ID in try block {}", npe );
		}
		return id;
	}
	
	DBObject getDBObject() {
		final DBObject doc = new BasicDBObject();
		if ( street != null && !street.isEmpty() )
			doc.put( "street", street );
		doc.put( "streetType", streetType.name() );
		return doc;
	}
	
	public void save() {
		final DBObject o = getDBObject();
		if ( getAddressCollection().find( o ).count() < 1 ) {
			id = getOrCreateId();
			o.put( "_id", id );
			getAddressCollection().save( o );
		}
	}
	
	public static AddressPlace find( final String streetName, final StreetType streetType ) throws AddressNotFoundException {
		try {
			final DBObject doc = getAddressCollection().findOne(
					QueryBuilder.start( "street" ).is( streetName ).and( "streetType" ).is( streetType.name() ).get() );
			if ( doc == null )
				throw new AddressNotFoundException( StreetType.optionsShortname().get( streetType.name() ) + " " + streetName
						+ " not found" );
			final AddressPlace addr = AddressPlace.create( doc );
			return addr;
		}
		catch ( final MongoException me ) {
			return null;
		}
	}
	
	public static AddressPlace findById( final long id ) throws AddressNotFoundException {
		if ( id > 0 )
			try {
				final DBObject doc = getAddressCollection().findOne( QueryBuilder.start( "_id" ).is( id ).get() );
				final AddressPlace addr = AddressPlace.create( doc );
				return addr;
			}
			catch ( final MongoException me ) {
				throw new AddressNotFoundException( "Exception in AddressPlace.findById( id ) of DBCollection", me );
			}
		else
			throw new AddressNotFoundException( "ID must be greater than zero in AddressPlace.findById( id )" );
	}
	
	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer(
				Messages.get( Address.STREET_TYPE_SHORTNAME + "." + streetType.name().toLowerCase() ) );
		if ( sb.length() > 0 )
			sb.append( " " );
		sb.append( street );
		return sb.toString();
	}
	
	public static DBCollection getAddressCollection() {
		return Database.getInstance().getDatabase().getCollection( "placeAddresses" );
	}
}