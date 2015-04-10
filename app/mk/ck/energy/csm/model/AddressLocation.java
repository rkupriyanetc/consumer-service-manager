package mk.ck.energy.csm.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import mk.ck.energy.csm.model.db.AbstractMongoDocument;

import org.bson.BsonArray;
import org.bson.BsonString;
import org.bson.BsonValue;

import com.mongodb.BasicDBList;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

public class AddressLocation extends AbstractMongoDocument< AddressLocation > {
	
	private static final long		serialVersionUID									= 1L;
	
	private static final String	COLLECTION_NAME_LOCATION_ADDRESS	= "locationAddresses";
	
	private static final String	DB_FIELD_LOCATION									= "location";
	
	private static final String	DB_FIELD_LOCATION_TYPE						= "location_type";
	
	private static final String	DB_FIELD_REF_TO_TOP_ADDRESS				= "ref_id";
	
	private AddressTop					topAddress;
	
	public AddressLocation( final AddressTop topId, final String location, final List< LocationType > locationsTypes ) {
		setLocation( location );
		setTopAddress( topId );
		setLocationType( locationsTypes );
	}
	
	public String getRefId() {
		return getString( DB_FIELD_REF_TO_TOP_ADDRESS );
	}
	
	public void setRefId( final String refId ) {
		if ( getRefId() != refId )
			try {
				topAddress = AddressTop.findById( refId );
				put( DB_FIELD_REF_TO_TOP_ADDRESS, refId );
			}
			catch ( final AddressNotFoundException anfe ) {
				remove( DB_FIELD_REF_TO_TOP_ADDRESS );
			}
	}
	
	/**
	 * Населенний пункт
	 */
	public String getLocation() {
		return getString( DB_FIELD_LOCATION );
	}
	
	public void setLocation( final String location ) {
		put( DB_FIELD_LOCATION, location );
	}
	
	/**
	 * Типи населенних пунктів: столиця, обласний центр, районний, місто, село,
	 * хутір
	 */
	public List< LocationType > getLocationType() {
		final BsonArray list = ( BsonArray )get( DB_FIELD_LOCATION_TYPE );
		final List< LocationType > lts = new ArrayList<>( list.size() );
		for ( final BsonValue key : list.getValues() ) {
			final LocationType lt = LocationType.valueOf( ( ( BsonString )key ).getValue() );
			lts.add( lt );
		}
		return lts;
	}
	
	public void setLocationsTypes( final List< LocationType > locationType ) {
		final BsonArray dbList = new BsonArray();
		for ( final LocationType lt : locationType )
			dbList.add( new BsonString( lt.name() ) );
		put( DB_FIELD_LOCATION_TYPE, dbList );
	}
	
	public AddressTop getTopAddress() {
		return topAddress;
	}
	
	public void setTopAddress( final AddressTop topAddress ) {
		this.topAddress = topAddress;
		if ( topAddress != null )
			put( DB_FIELD_REF_TO_TOP_ADDRESS, topAddress.getRefId() );
		else
			remove( DB_FIELD_REF_TO_TOP_ADDRESS );
	}
	
	public static AddressLocation findById( final String id ) throws AddressNotFoundException {
		if ( id != null && !id.isEmpty() ) {
			final AddressLocation doc = getMongoCollection().find( Filters.eq( DB_FIELD_ID, id ) ).first();
			if ( doc == null )
				throw new AddressNotFoundException( "Cannot find address-location by " + id );
			return doc;
		} else
			throw new IllegalArgumentException( "ID must be greater than zero in AddressLocation.findById( id )" );
	}
	
	public static List< AddressLocation > findByAddressTop( final AddressTop topAddr ) throws AddressNotFoundException {
		final List< AddressLocation > locations = new LinkedList<>();
		final MongoCursor< AddressLocation > cursor = getMongoCollection().find(
				Filters.eq( DB_FIELD_REF_TO_TOP_ADDRESS, topAddr.getId() ) ).iterator();
		if ( cursor == null )
			throw new AddressNotFoundException( "Cannot find address-location by " + topAddr.getId() );
		while ( cursor.hasNext() ) {
			final AddressLocation o = cursor.next();
			locations.add( o );
		}
		return locations;
	}
	
	/**
	 * public static List< AddressLocation > findByAddress( final DBObject address
	 * ) throws AddressNotFoundException {
	 * final List< AddressLocation > locations = new ArrayList< AddressLocation >(
	 * 0 );
	 * final BasicDBList lts = ( BasicDBList )address.get(
	 * DB_FIELD_LOCATIONS_TYPES );
	 * final DBObject qu = new BasicDBObject();
	 * if ( lts != null )
	 * qu.put( DB_FIELD_LOCATIONS_TYPES, new BasicDBObject( "$in", lts ) );
	 * final String name = ( String )address.get( DB_FIELD_LOCATION );
	 * if ( name != null )
	 * qu.put( DB_FIELD_LOCATION, name );
	 * qu.put( DB_FIELD_REF_TO_TOP_ADDRESS, ( ( Long )address.get(
	 * DB_FIELD_REF_TO_TOP_ADDRESS ) ).longValue() );
	 * try {
	 * final DBCursor cursor = getAddressCollection().find( qu );
	 * if ( cursor == null )
	 * throw new AddressNotFoundException( "AddressLocation by " + address +
	 * " not found" );
	 * while ( cursor.hasNext() ) {
	 * final DBObject o = cursor.next();
	 * final AddressLocation addr = AddressLocation.create( o );
	 * locations.add( addr );
	 * }
	 * return locations;
	 * }
	 * catch ( final MongoException me ) {
	 * return null;
	 * }
	 * catch ( final ImpossibleCreatingException ice ) {
	 * throw new AddressNotFoundException(
	 * "ImpossibleCreatingException in AddressLocation.findByAddress( address ) in AddressLocation.create( doc )"
	 * , ice );
	 * }
	 * }
	 */
	public static List< AddressLocation > findByLocationName( final String locationName ) throws AddressNotFoundException {
		final List< AddressLocation > locations = new LinkedList<>();
		final MongoCursor< AddressLocation > cursor = getMongoCollection().find( Filters.eq( DB_FIELD_LOCATION, locationName ) )
				.iterator();
		if ( cursor == null )
			throw new AddressNotFoundException( "Cannot find address-location by " + locationName );
		while ( cursor.hasNext() ) {
			final AddressLocation o = cursor.next();
			locations.add( o );
		}
		return locations;
	}
	
	public static void remove( final AddressLocation addr ) throws ForeignKeyException {
		if ( hasChildren( addr ) )
			throw new ForeignKeyException( "This record has dependencies" );
		else {
			final AddressLocation doc = getMongoCollection().findOneAndDelete( Filters.eq( DB_FIELD_ID, addr.getId() ) );
			LOGGER.debug( "AddressLocation was removed {}", doc );
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
	public static Map< String, String > getMap( final String refId, final int isAddrTop ) {
		final Map< String, String > references = new LinkedHashMap<>();
		final MongoCursor< AddressLocation > o = getMongoCollection().find( Filters.eq( DB_FIELD_REF_TO_TOP_ADDRESS, refId ) )
				.iterator();
		while ( o.hasNext() ) {
			final AddressLocation addr = o.next();
			final String name = choiceFromLocationsTypes( ( BasicDBList )addr.get( DB_FIELD_LOCATION_TYPE ) ) + " "
					+ addr.getString( DB_FIELD_LOCATION );
			final String _id = addr.getString( DB_FIELD_ID );
			references.put( _id, name );
		}
		if ( isAddrTop != 0 ) {
			int p = -1;
			for ( final String key : AddressTop.getMap( refId ).keySet() ) {
				references.put( new Integer( p-- ).toString(), "0" );
				references.putAll( getMap( key, 0 ) );
			}
		}
		return references;
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( choiceFromLocationsTypes( ( BasicDBList )get( DB_FIELD_LOCATION_TYPE ) ) );
		if ( sb.length() > 0 )
			sb.append( " " );
		sb.append( getString( DB_FIELD_LOCATION ) );
		return sb.toString();
	}
	
	protected static String choiceFromLocationsTypes( final BasicDBList dbList ) {
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
	
	/**
	 * @Override
	 *           private void save() throws ImpossibleCreatingException {
	 *           if ( locationsTypes.contains( LocationType.CAPITAL ) ) {
	 *           final BasicDBList dbo = new BasicDBList();
	 *           dbo.add( LocationType.CAPITAL.name() );
	 *           final Document qu = new Document( DB_FIELD_LOCATIONS_TYPES, new
	 *           Document( "$in", dbo ) );
	 *           if ( getCollection( COLLECTION_NAME_LOCATION_ADDRESS ).find( qu
	 *           ).iterator().hasNext() )
	 *           throw new ImpossibleCreatingException(
	 *           "One of the country's capital already exists in the database" );
	 *           }
	 *           super.save( COLLECTION_NAME_LOCATION_ADDRESS );
	 *           }
	 */
	public static MongoCollection< AddressLocation > getMongoCollection() {
		final MongoDatabase db = Database.getInstance().getDatabase();
		final MongoCollection< AddressLocation > collection = db.getCollection( COLLECTION_NAME_LOCATION_ADDRESS,
				AddressLocation.class );
		return collection;
	}
	
	@Override
	protected MongoCollection< AddressLocation > getCollection() {
		return getMongoCollection();
	}
}
