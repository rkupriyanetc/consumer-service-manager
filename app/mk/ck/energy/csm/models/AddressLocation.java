package mk.ck.energy.csm.models;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.QueryBuilder;
import com.mongodb.WriteResult;

public class AddressLocation {
	
	private static final Logger		LOGGER											= LoggerFactory.getLogger( AddressLocation.class );
	
	static final String						DB_FIELD_ID									= "_id";
	
	static final String						DB_FIELD_LOCATIONS_TYPES		= "locations_types";
	
	static final String						DB_FIELD_LOCATION						= "location";
	
	static final String						DB_FIELD_REF_TO_TOP_ADDRESS	= "ref_id";
	
	private long									id;
	
	private long									refId;
	
	/**
	 * Типи населенних пунктів: столиця, обласний центр, районний, місто, село,
	 * хутір
	 */
	private List< LocationType >	locationsTypes;
	
	/**
	 * Населенний пункт
	 */
	private String								location;
	
	private AddressTop						topAddress;
	
	private AddressLocation() {}
	
	public static AddressLocation create( final AddressTop topId, final String location, final List< LocationType > locationsTypes ) {
		final AddressLocation addr = new AddressLocation();
		addr.setLocation( location );
		addr.setTopAddress( topId );
		addr.setLocationsTypes( locationsTypes );
		try {
			addr.save();
		}
		catch ( final ImpossibleCreatingException ice ) {
			LOGGER.debug( "Create error in AddressLocation.create( addr, location, type ). {}", ice );
		}
		return addr;
	}
	
	public static AddressLocation create( final DBObject dbo ) throws ImpossibleCreatingException {
		if ( dbo == null )
			return null;
		final AddressLocation addr = new AddressLocation();
		addr.locationsTypes = new ArrayList< LocationType >( 0 );
		addr.location = ( String )dbo.get( DB_FIELD_LOCATION );
		final BasicDBList getList = ( BasicDBList )dbo.get( DB_FIELD_LOCATIONS_TYPES );
		for ( final Object o : getList )
			addr.locationsTypes.add( LocationType.valueOf( ( String )o ) );
		addr.id = ( Long )dbo.get( DB_FIELD_ID );
		addr.refId = ( Long )dbo.get( DB_FIELD_REF_TO_TOP_ADDRESS );
		try {
			addr.topAddress = AddressTop.findById( addr.refId );
		}
		catch ( final AddressNotFoundException anfe ) {
			throw new ImpossibleCreatingException( "Cannot AddressLocation.create( DBObject dbo ) because could not find AddressTop" );
		}
		return addr;
	}
	
	public long getId() {
		return id;
	}
	
	public void setId( final long id ) {
		this.id = id;
	}
	
	public long getRefId() {
		return refId;
	}
	
	public String getLocation() {
		return location;
	}
	
	public void setLocation( final String location ) {
		this.location = location;
	}
	
	public List< LocationType > getLocationsTypes() {
		return locationsTypes;
	}
	
	public void setLocationsTypes( final List< LocationType > locationsTypes ) {
		this.locationsTypes = locationsTypes;
	}
	
	public AddressTop getTopAddress() {
		return topAddress;
	}
	
	public void setTopAddress( final AddressTop topAddress ) {
		this.topAddress = topAddress;
		if ( topAddress != null )
			this.refId = topAddress.getId();
		else
			this.refId = 0;
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
		}
		catch ( final MongoException me ) {
			LOGGER.debug( "Cannon find ID in AddressLocation.getOrCreateId(). {}", me );
		}
		catch ( final NullPointerException npe ) {
			LOGGER.debug( "Cannon find ID in AddressLocation.getOrCreateId(). {}", npe );
		}
		return id;
	}
	
	DBObject getDBObject() {
		final DBObject doc = new BasicDBObject();
		if ( location != null && !location.isEmpty() )
			doc.put( DB_FIELD_LOCATION, location );
		final BasicDBList dbTypes = new BasicDBList();
		for ( final LocationType type : locationsTypes )
			dbTypes.add( type.name() );
		if ( !dbTypes.isEmpty() )
			doc.put( DB_FIELD_LOCATIONS_TYPES, dbTypes );
		doc.put( DB_FIELD_REF_TO_TOP_ADDRESS, refId );
		return doc;
	}
	
	public void save() throws ImpossibleCreatingException {
		if ( locationsTypes.contains( LocationType.CAPITAL ) ) {
			final BasicDBList dbo = new BasicDBList();
			dbo.add( LocationType.CAPITAL.name() );
			final DBObject qu = new BasicDBObject( DB_FIELD_LOCATIONS_TYPES, new BasicDBObject( "$in", dbo ) );
			if ( getAddressCollection().find( qu ).hasNext() )
				throw new ImpossibleCreatingException( "One of the country's capital already exists in the database" );
		}
		id = getOrCreateId();
		final DBObject o = new BasicDBObject( DB_FIELD_ID, id );
		o.putAll( getDBObject() );
		getAddressCollection().save( o );
	}
	
	public static AddressLocation findById( final long id ) throws AddressNotFoundException {
		if ( id > 0 )
			try {
				final DBObject doc = getAddressCollection().findOne( QueryBuilder.start( DB_FIELD_ID ).is( id ).get() );
				try {
					final AddressLocation addr = AddressLocation.create( doc );
					return addr;
				}
				catch ( final ImpossibleCreatingException ice ) {
					return null;
				}
			}
			catch ( final MongoException me ) {
				throw new AddressNotFoundException( "Exception in AddressLocation.findById( id ) of DBCollection", me );
			}
		else
			throw new AddressNotFoundException( "ID must be greater than zero in AddressLocation.findById( id )" );
	}
	
	public static List< AddressLocation > findByAddressTop( final AddressTop topAddr ) throws AddressNotFoundException {
		final List< AddressLocation > locations = new ArrayList< AddressLocation >( 0 );
		try {
			final DBCursor cur = getAddressCollection().find( new BasicDBObject( DB_FIELD_REF_TO_TOP_ADDRESS, topAddr.getId() ) );
			if ( cur == null )
				throw new AddressNotFoundException( "Addresses in the " + topAddr + " not found" );
			while ( cur.hasNext() ) {
				final DBObject o = cur.next();
				final AddressLocation addr = AddressLocation.create( o );
				locations.add( addr );
			}
			return locations;
		}
		catch ( final MongoException me ) {
			return null;
		}
		catch ( final ImpossibleCreatingException ice ) {
			throw new AddressNotFoundException(
					"ImpossibleCreatingException in AddressLocation.findByAddressTop( addressTop ) in AddressLocation.create( doc )", ice );
		}
	}
	
	public static List< AddressLocation > findByAddress( final DBObject address ) throws AddressNotFoundException {
		final List< AddressLocation > locations = new ArrayList< AddressLocation >( 0 );
		final BasicDBList lts = ( BasicDBList )address.get( DB_FIELD_LOCATIONS_TYPES );
		final DBObject qu = new BasicDBObject();
		if ( lts != null )
			qu.put( DB_FIELD_LOCATIONS_TYPES, new BasicDBObject( "$in", lts ) );
		final String name = ( String )address.get( DB_FIELD_LOCATION );
		if ( name != null )
			qu.put( DB_FIELD_LOCATION, name );
		qu.put( DB_FIELD_REF_TO_TOP_ADDRESS, ( ( Long )address.get( DB_FIELD_REF_TO_TOP_ADDRESS ) ).longValue() );
		try {
			final DBCursor cursor = getAddressCollection().find( qu );
			if ( cursor == null )
				throw new AddressNotFoundException( "AddressLocation by " + address + " not found" );
			while ( cursor.hasNext() ) {
				final DBObject o = cursor.next();
				final AddressLocation addr = AddressLocation.create( o );
				locations.add( addr );
			}
			return locations;
		}
		catch ( final MongoException me ) {
			return null;
		}
		catch ( final ImpossibleCreatingException ice ) {
			throw new AddressNotFoundException(
					"ImpossibleCreatingException in AddressLocation.findByAddress( address ) in AddressLocation.create( doc )", ice );
		}
	}
	
	public static List< AddressLocation > findByLocationName( final String locationName ) throws AddressNotFoundException {
		final List< AddressLocation > locations = new ArrayList< AddressLocation >( 0 );
		try {
			final DBCursor cursor = getAddressCollection().find( QueryBuilder.start( DB_FIELD_LOCATION ).is( locationName ).get() );
			if ( cursor == null )
				throw new AddressNotFoundException( "Location by " + locationName + " not found" );
			while ( cursor.hasNext() ) {
				final DBObject o = cursor.next();
				final AddressLocation addr = AddressLocation.create( o );
				locations.add( addr );
			}
			return locations;
		}
		catch ( final MongoException me ) {
			return null;
		}
		catch ( final ImpossibleCreatingException ice ) {
			throw new AddressNotFoundException(
					"ImpossibleCreatingException in AddressLocation.findByLocationName( locationName ) in AddressLocation.create( doc )",
					ice );
		}
	}
	
	public static void remove( final AddressLocation addr ) throws AddressNotFoundException, ForeignKeyException {
		if ( hasChildren( addr ) )
			throw new ForeignKeyException( "This record has dependencies" );
		else
			try {
				final DBObject doc = getAddressCollection().findOne( QueryBuilder.start( DB_FIELD_ID ).is( addr.id ).get() );
				final WriteResult wr = getAddressCollection().remove( doc );
				LOGGER.debug( "AddressLocation object removed {}", wr );
			}
			catch ( final MongoException me ) {
				throw new AddressNotFoundException( "AddressTop not found" );
			}
	}
	
	private static boolean hasChildren( final AddressLocation addr ) {
		return false;
	}
	
	/**
	 * @param refId
	 *          Indicates reference id, who have to get the select to map
	 * @param isAddrTop
	 *          If equals zero then does not participate
	 * @return
	 */
	public static Map< String, String > getMap( final long refId, final int isAddrTop ) {
		final Map< String, String > references = new LinkedHashMap< String, String >();
		for ( final DBObject o : getAddressCollection().find( new BasicDBObject( DB_FIELD_REF_TO_TOP_ADDRESS, refId ) ) ) {
			final String name = choiceFromLocationsTypes( ( BasicDBList )o.get( DB_FIELD_LOCATIONS_TYPES ) ) + " "
					+ ( String )o.get( DB_FIELD_LOCATION );
			final String _id = ( ( Long )o.get( DB_FIELD_ID ) ).toString();
			references.put( _id, name );
		}
		if ( isAddrTop != 0 ) {
			int p = -1;
			for ( final String keys : AddressTop.getMap( refId ).keySet() ) {
				references.put( new Integer( p-- ).toString(), "0" );
				references.putAll( getMap( new Long( keys ), 0 ) );
			}
		}
		return references;
	}
	
	public static List< AddressLocation > asClassType( final List< DBObject > all ) {
		final List< AddressLocation > result = new ArrayList< AddressLocation >();
		for ( final DBObject dbo : all )
			try {
				result.add( AddressLocation.create( dbo ) );
			}
			catch ( final ImpossibleCreatingException ice ) {
				LOGGER.debug( "Error in converter AddressLocation {}", ice );
			}
		return result;
	}
	
	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer();
		sb.append( choiceFromLocationsTypes( new ArrayList< Object >( locationsTypes ) ) );
		if ( sb.length() > 0 )
			sb.append( " " );
		sb.append( location );
		return sb.toString();
	}
	
	protected static String choiceFromLocationsTypes( final ArrayList< Object > dbList ) {
		String strRet = "";
		for ( final Object typeO : dbList ) {
			final String typeStr = ( String )typeO;
			final LocationType type = LocationType.valueOf( typeStr );
			switch ( type ) {
				case CITY :
					strRet = LocationType.CITY.toString( Address.LOCATION_TYPE_SHORTNAME );
					break;
				case TOWNSHIP :
					strRet = LocationType.TOWNSHIP.toString( Address.LOCATION_TYPE_SHORTNAME );
					break;
				case VILLAGE :
					strRet = LocationType.VILLAGE.toString( Address.LOCATION_TYPE_SHORTNAME );
					break;
				case HAMLET :
					strRet = LocationType.HAMLET.toString( Address.LOCATION_TYPE_SHORTNAME );
					break;
				case BOWERY :
					strRet = LocationType.BOWERY.toString( Address.LOCATION_TYPE_SHORTNAME );
					break;
				default :
					break;
			}
		}
		return strRet;
	}
	
	public static DBCollection getAddressCollection() {
		return Database.getInstance().getDatabase().getCollection( "locationAddresses" );
	}
}
