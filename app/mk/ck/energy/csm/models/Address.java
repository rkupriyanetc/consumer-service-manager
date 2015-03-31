package mk.ck.energy.csm.models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class Address {
	
	public static final short		UPDATING_READING_ALL							= 0;
	
	public static final short		UPDATING_READING_ADDRESS_LOCATION	= 1;
	
	public static final short		UPDATING_READING_ADDRESS_PLACE		= 2;
	
	public static final short		UPDATING_READING_HOUSE						= 4;
	
	public static final short		UPDATING_READING_APARTMENT				= 8;
	
	public static final short		UPDATING_READING_POSTAL_CODE			= 16;
	
	public static final String	LOCATION_TYPE_FULLNAME						= "location.type.full";
	
	public static final String	LOCATION_TYPE_SHORTNAME						= "location.type.short";
	
	public static final String	STREET_TYPE_FULLNAME							= "street.type.full";
	
	public static final String	STREET_TYPE_SHORTNAME							= "street.type.short";
	
	private static final Logger	LOGGER														= LoggerFactory.getLogger( Address.class );
	
	static final String					DB_FIELD_ADDRESS_LOCATION_ID			= "address_location_id";
	
	static final String					DB_FIELD_ADDRESS_PLACE_ID					= "address_place_id";
	
	static final String					DB_FIELD_ADDRESS_HOUSE						= "house";
	
	static final String					DB_FIELD_ADDRESS_APARTMENT				= "apartment";
	
	static final String					DB_FIELD_ADDRESS_POSTAL_CODE			= "postal_code";
	
	/**
	 * Деяка частина адреси: населений пункт.
	 */
	private AddressLocation			addressLocation;
	
	private long								addressLocationId;
	
	/**
	 * Деяка частина адреси: вулиця.
	 */
	private AddressPlace				addressPlace;
	
	private long								addressPlaceId;
	
	/**
	 * Будинок
	 */
	private String							house;
	
	/**
	 * Квартира
	 */
	private String							apartment;
	
	/**
	 * Postal code of
	 */
	private String							postalCode;
	
	public Address() {}
	
	public Address( final DBObject doc ) {
		setAddressLocationId( ( ( Long )doc.get( DB_FIELD_ADDRESS_LOCATION_ID ) ).longValue() );
		setAddressPlaceId( ( ( Long )doc.get( DB_FIELD_ADDRESS_PLACE_ID ) ).longValue() );
		apartment = ( String )doc.get( DB_FIELD_ADDRESS_APARTMENT );
		house = ( String )doc.get( DB_FIELD_ADDRESS_HOUSE );
		postalCode = ( String )doc.get( DB_FIELD_ADDRESS_POSTAL_CODE );
	}
	
	public AddressLocation getAddressLocation() {
		return addressLocation;
	}
	
	public void setAddressLocation( final AddressLocation address ) {
		this.addressLocation = address;
		this.addressLocationId = address.getId();
	}
	
	public long getAddressLocationId() {
		return addressLocationId;
	}
	
	public void setAddressLocationId( final long addressLocationId ) {
		if ( this.addressLocationId != addressLocationId )
			try {
				this.addressLocation = AddressLocation.findById( addressLocationId );
				this.addressLocationId = addressLocationId;
			}
			catch ( final AddressNotFoundException anfe ) {
				this.addressLocationId = 0;
				LOGGER.trace( "Sorry. Cannot find address location by {}", addressLocationId );
			}
	}
	
	public AddressPlace getAddressPlace() {
		return addressPlace;
	}
	
	public void setAddressPlace( final AddressPlace address ) {
		this.addressPlace = address;
		this.addressPlaceId = address.getId();
	}
	
	public long getAddressPlaceId() {
		return addressPlaceId;
	}
	
	public void setAddressPlaceId( final long addressPlaceId ) {
		if ( this.addressPlaceId != addressPlaceId )
			try {
				this.addressPlace = AddressPlace.findById( addressPlaceId );
				this.addressPlaceId = addressPlaceId;
			}
			catch ( final AddressNotFoundException anfe ) {
				this.addressPlaceId = 0;
				LOGGER.trace( "Sorry. Cannot find address place by {}", addressPlaceId );
			}
	}
	
	public String getHouse() {
		return house;
	}
	
	public void setHouse( final String house ) {
		this.house = house;
	}
	
	public String getApartment() {
		return apartment;
	}
	
	public void setApartment( final String apartment ) {
		this.apartment = apartment;
	}
	
	public String getPostalCode() {
		return postalCode;
	}
	
	public void setPostalCode( final String postalCode ) {
		this.postalCode = postalCode;
	}
	
	DBObject getDBObject( final short updateSet ) {
		final DBObject doc = new BasicDBObject();
		try {
			if ( UPDATING_READING_ALL == updateSet
					|| ( updateSet & UPDATING_READING_ADDRESS_LOCATION ) == UPDATING_READING_ADDRESS_LOCATION ) {
				addressLocation.save();
				doc.put( DB_FIELD_ADDRESS_LOCATION_ID, addressLocation.getId() );
			}
			if ( UPDATING_READING_ALL == updateSet || ( updateSet & UPDATING_READING_ADDRESS_PLACE ) == UPDATING_READING_ADDRESS_PLACE ) {
				addressPlace.save();
				doc.put( DB_FIELD_ADDRESS_PLACE_ID, addressPlace.getId() );
			}
			if ( UPDATING_READING_ALL == updateSet || ( updateSet & UPDATING_READING_HOUSE ) == UPDATING_READING_HOUSE )
				if ( house != null && !house.isEmpty() )
					doc.put( DB_FIELD_ADDRESS_HOUSE, house );
			if ( UPDATING_READING_ALL == updateSet || ( updateSet & UPDATING_READING_APARTMENT ) == UPDATING_READING_APARTMENT )
				if ( apartment != null && !apartment.isEmpty() )
					doc.put( DB_FIELD_ADDRESS_APARTMENT, apartment );
			if ( UPDATING_READING_ALL == updateSet || ( updateSet & UPDATING_READING_POSTAL_CODE ) == UPDATING_READING_POSTAL_CODE )
				if ( postalCode != null && postalCode.isEmpty() )
					doc.put( DB_FIELD_ADDRESS_POSTAL_CODE, postalCode );
			return doc;
		}
		catch ( final ImpossibleCreatingException ice ) {
			LOGGER.error( "Can not write locationAdress in a global address" );
			return null;
		}
	}
	
	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer();
		if ( postalCode != null && postalCode.isEmpty() ) {
			sb.append( postalCode );
			sb.append( ", " );
		}
		sb.append( addressLocation.getTopAddress().toString() );
		sb.append( ", " );
		sb.append( addressLocation );
		sb.append( ", " );
		sb.append( addressPlace );
		if ( house != null && !house.isEmpty() ) {
			sb.append( ", " );
			sb.append( house );
		}
		if ( apartment != null && !apartment.isEmpty() ) {
			sb.append( ", " );
			sb.append( apartment );
		}
		return sb.toString();
	}
}
