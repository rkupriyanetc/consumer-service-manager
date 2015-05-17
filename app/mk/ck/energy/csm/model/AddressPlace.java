package mk.ck.energy.csm.model;

import java.util.LinkedHashMap;
import java.util.Map;

import mk.ck.energy.csm.model.mongodb.CSMAbstractDocument;

import org.bson.BsonDocument;
import org.bson.BsonDocumentWrapper;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import play.i18n.Messages;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;

public class AddressPlace extends CSMAbstractDocument< AddressPlace > {
	
	private static final long		serialVersionUID							= 1L;
	
	private static final String	COLLECTION_NAME_PLACE_ADDRESS	= "placeAddresses";
	
	private static final String	DB_FIELD_STREET_NAME					= "street";
	
	private static final String	DB_FIELD_STREET_TYPE					= "street_type";
	
	private AddressPlace() {}
	
	public static AddressPlace create() {
		return new AddressPlace();
	}
	
	public static AddressPlace create( final StreetType streetType, final String street ) {
		final AddressPlace addr = new AddressPlace();
		addr.setStreet( street );
		addr.setStreetType( streetType );
		return addr;
	}
	
	/**
	 * Вулиця
	 */
	public String getStreet() {
		return getString( DB_FIELD_STREET_NAME );
	}
	
	public void setStreet( final String street ) {
		put( DB_FIELD_STREET_NAME, street );
	}
	
	/**
	 * Тип вулиці
	 */
	public StreetType getStreetType() {
		return StreetType.valueOf( getString( DB_FIELD_STREET_TYPE ) );
	}
	
	public void setStreetType( final StreetType streetType ) {
		put( DB_FIELD_STREET_TYPE, streetType.name() );
	}
	
	public void save() throws ImpossibleCreatingException {
		final Bson value = Filters.and( Filters.eq( DB_FIELD_STREET_NAME, getStreet() ),
				Filters.eq( DB_FIELD_STREET_TYPE, getString( DB_FIELD_STREET_TYPE ) ) );
		final AddressPlace addr = getCollection().find( value, AddressPlace.class ).first();
		if ( addr == null )
			insertIntoDB();
		else {
			final String street = this.toString();
			LOGGER.warn( "Cannot save AddressPlace. Place address already exists: {}", street );
			throw new ImpossibleCreatingException( "Place address already exists " + street );
		}
	}
	
	public static AddressPlace find( final String streetName, final StreetType streetType ) throws AddressNotFoundException {
		final AddressPlace doc = getMongoCollection().find(
				Filters.and( Filters.eq( DB_FIELD_STREET_NAME, streetName ), Filters.eq( DB_FIELD_STREET_TYPE, streetType.name() ) ) )
				.first();
		if ( doc == null )
			throw new AddressNotFoundException( StreetType.optionsShortname().get( streetType.name() ) + " " + streetName
					+ " not found" );
		return doc;
	}
	
	public static AddressPlace findById( final String id ) throws AddressNotFoundException {
		if ( id != null && !id.isEmpty() ) {
			final AddressPlace doc = getMongoCollection().find( Filters.eq( DB_FIELD_ID, id ) ).first();
			if ( doc == null )
				throw new AddressNotFoundException( "Cannot find address-place by " + id );
			return doc;
		} else
			throw new IllegalArgumentException( "ID must be greater than zero in AddressPlace.findById( id )" );
	}
	
	public static Map< String, String > getMap() {
		final Map< String, String > references = new LinkedHashMap< String, String >( 0 );
		final Bson sort = Filters.eq( DB_FIELD_STREET_NAME, 1 );
		final MongoCursor< AddressPlace > cursor = getMongoCollection().find().sort( sort ).iterator();
		while ( cursor.hasNext() ) {
			final AddressPlace place = cursor.next();
			final String name = Messages.get( Address.STREET_TYPE_SHORTNAME + "."
					+ place.getString( DB_FIELD_STREET_TYPE ).toLowerCase() )
					+ " " + place.getString( DB_FIELD_STREET_NAME );
			final String _id = place.getString( DB_FIELD_ID );
			references.put( _id, name );
		}
		return references;
	}
	
	@Override
	public boolean equals( final Object o ) {
		if ( o == null )
			return false;
		final AddressPlace ap = ( AddressPlace )o;
		return getStreet().equals( ap.getStreet() ) && getStreetType().equals( ap.getStreetType() );
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder( Messages.get( Address.STREET_TYPE_SHORTNAME + "."
				+ getStreetType().name().toLowerCase() ) );
		if ( sb.length() > 0 )
			sb.append( " " );
		sb.append( getStreet() );
		return sb.toString();
	}
	
	@Override
	public < TDocument >BsonDocument toBsonDocument( final Class< TDocument > documentClass, final CodecRegistry codecRegistry ) {
		return new BsonDocumentWrapper< AddressPlace >( this, codecRegistry.get( AddressPlace.class ) );
	}
	
	public static MongoCollection< AddressPlace > getMongoCollection() {
		final MongoCollection< AddressPlace > collection = getDatabase().getCollection( COLLECTION_NAME_PLACE_ADDRESS,
				AddressPlace.class );
		return collection;
	}
	
	@Override
	protected MongoCollection< AddressPlace > getCollection() {
		return getMongoCollection();
	}
}