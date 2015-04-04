package mk.ck.energy.csm.models;

import java.util.LinkedHashMap;
import java.util.Map;

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
	
	private static final Logger	LOGGER								= LoggerFactory.getLogger( AddressPlace.class );
	
	static final String					DB_FIELD_ID						= "_id";
	
	static final String					DB_FIELD_STREET_NAME	= "street";
	
	static final String					DB_FIELD_STREET_TYPE	= "street_type";
	
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
		addr.save();
		return addr;
	}
	
	public static AddressPlace create( final DBObject dbo ) {
		if ( dbo == null )
			return null;
		final AddressPlace addr = new AddressPlace();
		addr.streetType = StreetType.valueOf( ( String )dbo.get( DB_FIELD_STREET_TYPE ) );
		addr.street = ( String )dbo.get( DB_FIELD_STREET_NAME );
		addr.id = ( Long )dbo.get( DB_FIELD_ID );
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
				id = ( Long )rec.get( DB_FIELD_ID );
			else {
				final DBCursor cursor = getAddressCollection().find().sort( new BasicDBObject( DB_FIELD_ID, -1 ) ).limit( 1 );
				if ( cursor.hasNext() ) {
					final Object o = cursor.next().get( DB_FIELD_ID );
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
			doc.put( DB_FIELD_STREET_NAME, street );
		doc.put( DB_FIELD_STREET_TYPE, streetType.name() );
		return doc;
	}
	
	public void save() {
		id = getOrCreateId();
		final DBObject o = new BasicDBObject( DB_FIELD_ID, id );
		o.putAll( getDBObject() );
		getAddressCollection().save( o );
	}
	
	public static AddressPlace find( final String streetName, final StreetType streetType ) throws AddressNotFoundException {
		try {
			final DBObject doc = getAddressCollection().findOne(
					QueryBuilder.start( DB_FIELD_STREET_NAME ).is( streetName ).and( DB_FIELD_STREET_TYPE ).is( streetType.name() ).get() );
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
				final DBObject doc = getAddressCollection().findOne( QueryBuilder.start( DB_FIELD_ID ).is( id ).get() );
				final AddressPlace addr = AddressPlace.create( doc );
				return addr;
			}
			catch ( final MongoException me ) {
				throw new AddressNotFoundException( "Exception in AddressPlace.findById( id ) of DBCollection", me );
			}
		else
			throw new AddressNotFoundException( "ID must be greater than zero in AddressPlace.findById( id )" );
	}
	
	public static Map< String, String > getMap() {
		final Map< String, String > references = new LinkedHashMap< String, String >( 0 );
		final DBObject sort = new BasicDBObject( DB_FIELD_STREET_TYPE, 1 );
		sort.put( DB_FIELD_STREET_NAME, 1 );
		final DBCursor cursor = getAddressCollection().find().sort( sort );
		for ( final DBObject o : cursor ) {
			final String name = Messages.get( Address.STREET_TYPE_SHORTNAME + "."
					+ ( ( String )o.get( DB_FIELD_STREET_TYPE ) ).toLowerCase() )
					+ " " + ( String )o.get( DB_FIELD_STREET_NAME );
			final String _id = ( ( Long )o.get( DB_FIELD_ID ) ).toString();
			references.put( _id, name );
		}
		return references;
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