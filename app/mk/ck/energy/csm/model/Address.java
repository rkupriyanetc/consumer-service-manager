package mk.ck.energy.csm.model;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Address {
	
	public static final short		UPDATING_READING_ALL							= 0;
	
	public static final short		UPDATING_READING_ADDRESS_LOCATION	= 1;
	
	public static final short		UPDATING_READING_ADDRESS_PLACE		= 2;
	
	public static final short		UPDATING_READING_HOUSE						= 4;
	
	public static final short		UPDATING_READING_APARTMENT				= 8;
	
	public static final short		UPDATING_READING_POSTAL_CODE			= 16;
	
	public static final String	LOCATION_TYPE_FULLNAME						= "location.type.full";
	
	public static final String	LOCATION_TYPE_SHORTNAME						= "location.type.short";
	
	public static final String	ADMINISTRATIVE_TYPE_FULLNAME			= "administrative.type.full";
	
	public static final String	ADMINISTRATIVE_TYPE_SHORTNAME			= "administrative.type.short";
	
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
	
	private String							addressLocationId;
	
	/**
	 * Деяка частина адреси: вулиця.
	 */
	private AddressPlace				addressPlace;
	
	private String							addressPlaceId;
	
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
	
	public Address( final Document doc ) {
		setAddressLocationId( ( String )doc.get( DB_FIELD_ADDRESS_LOCATION_ID ) );
		setAddressPlaceId( ( String )doc.get( DB_FIELD_ADDRESS_PLACE_ID ) );
		apartment = ( String )doc.get( DB_FIELD_ADDRESS_APARTMENT );
		house = ( String )doc.get( DB_FIELD_ADDRESS_HOUSE );
		postalCode = ( String )doc.get( DB_FIELD_ADDRESS_POSTAL_CODE );
	}
	
	public AddressLocation getAddressLocation() {
		return addressLocation;
	}
	
	public void setAddressLocation( final AddressLocation address ) {
		if ( !this.addressLocation.equals( address ) ) {
			this.addressLocation = address;
			// Тут тра переробити
			this.addressLocationId = address.getId().toHexString();
		}
	}
	
	public String getAddressLocationId() {
		return addressLocationId;
	}
	
	public void setAddressLocationId( final String addressLocationId ) {
		if ( !this.addressLocationId.equals( addressLocationId ) )
			try {
				this.addressLocation = AddressLocation.findById( addressLocationId );
				this.addressLocationId = addressLocationId;
			}
			catch ( final AddressNotFoundException anfe ) {
				this.addressLocationId = null;
				LOGGER.warn( "Sorry. Cannot find address location by {}", addressLocationId );
			}
	}
	
	public AddressPlace getAddressPlace() {
		return addressPlace;
	}
	
	public void setAddressPlace( final AddressPlace address ) {
		if ( !this.addressPlace.equals( address ) ) {
			this.addressPlace = address;
			// Тут тра переробити
			this.addressPlaceId = address.getId().toHexString();
		}
	}
	
	public String getAddressPlaceId() {
		return addressPlaceId;
	}
	
	public void setAddressPlaceId( final String addressPlaceId ) {
		if ( !this.addressPlaceId.equals( addressPlaceId ) )
			try {
				this.addressPlace = AddressPlace.findById( addressPlaceId );
				this.addressPlaceId = addressPlaceId;
			}
			catch ( final AddressNotFoundException anfe ) {
				this.addressPlaceId = null;
				LOGGER.warn( "Sorry. Cannot find address place by {}", addressPlaceId );
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
	
	Document getDocument( final short updateSet ) {
		final Document doc = new Document();
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
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		if ( postalCode != null && postalCode.isEmpty() ) {
			sb.append( postalCode );
			sb.append( ", " );
		}
		sb.append( addressPlace );
		sb.append( ", " );
		if ( house != null && !house.isEmpty() ) {
			sb.append( house );
			sb.append( ", " );
		}
		if ( apartment != null && !apartment.isEmpty() ) {
			sb.append( apartment );
			sb.append( ", " );
		}
		sb.append( addressLocation );
		sb.append( ", " );
		sb.append( addressLocation.getTopAddress().toString() );
		return sb.toString();
	}
}
