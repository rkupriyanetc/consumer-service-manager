package mk.ck.energy.csm.model;

import java.util.LinkedHashMap;
import java.util.Map;

import mk.ck.energy.csm.model.db.AbstractMongoDocument;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.i18n.Messages;

import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.QueryBuilder;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class AddressPlace extends AbstractMongoDocument< AddressPlace > {
	
	private static final long		serialVersionUID							= 1L;
	
	private static final Logger	LOGGER												= LoggerFactory.getLogger( AddressPlace.class );
	
	public static final String	COLLECTION_NAME_PLACE_ADDRESS	= "placeAddresses";
	
	static final String					DB_FIELD_STREET_NAME					= "street";
	
	static final String					DB_FIELD_STREET_TYPE					= "street_type";
	
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
		addr.id = ( String )dbo.get( DB_FIELD_ID );
		return addr;
	}
	
	@Override
	public String getId() {
		return id;
	}
	
	/**
	 * Вулиця
	 */
	public String getStreet() {
		return street;
	}
	
	public void setStreet( final String street ) {
		this.street = street;
	}
	
	/**
	 * Тип вулиці
	 */
	public StreetType getStreetType() {
		return streetType;
	}
	
	public void setStreetType( final StreetType streetType ) {
		this.streetType = streetType;
	}
	
	@Override
	Document getDocument() {
		final Document doc = new Document();
		if ( street != null && !street.isEmpty() )
			doc.put( DB_FIELD_STREET_NAME, street );
		doc.put( DB_FIELD_STREET_TYPE, streetType.name() );
		return doc;
	}
	
	@Override
	public void save() {
		id = getOrCreateId();
		final Document o = new Document( DB_FIELD_ID, id );
		o.putAll( getDocument() );
		getAddressCollection().save( o );
	}
	
	public static AddressPlace find( final String streetName, final StreetType streetType ) throws AddressNotFoundException {
		try {
			final Document doc = getAddressCollection().findOne(
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
				final Document doc = getAddressCollection().findOne( QueryBuilder.start( DB_FIELD_ID ).is( id ).get() );
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
		final Document sort = new Document( DB_FIELD_STREET_TYPE, 1 );
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
	
	public static MongoCollection< AddressPlace > getMongoCollection() {
		final MongoDatabase db = Database.getInstance().getDatabase();
		final MongoCollection< AddressPlace > collection = db.getCollection( COLLECTION_NAME_PLACE_ADDRESS, AddressPlace.class );
		return collection;
	}
	
	@Override
	protected MongoCollection< AddressPlace > getCollection() {
		return getMongoCollection();
	}
}