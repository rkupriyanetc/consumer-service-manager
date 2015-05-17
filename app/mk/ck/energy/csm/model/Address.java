package mk.ck.energy.csm.model;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Address {
	
	public static final String	LOCATION_TYPE_FULLNAME				= "location.type.full";
	
	public static final String	LOCATION_TYPE_SHORTNAME				= "location.type.short";
	
	public static final String	ADMINISTRATIVE_TYPE_FULLNAME	= "administrative.type.full";
	
	public static final String	ADMINISTRATIVE_TYPE_SHORTNAME	= "administrative.type.short";
	
	public static final String	STREET_TYPE_FULLNAME					= "street.type.full";
	
	public static final String	STREET_TYPE_SHORTNAME					= "street.type.short";
	
	private static final Logger	LOGGER												= LoggerFactory.getLogger( Address.class );
	
	static final String					DB_FIELD_ADDRESS_LOCATION_ID	= "address_location_id";
	
	static final String					DB_FIELD_ADDRESS_PLACE_ID			= "address_place_id";
	
	static final String					DB_FIELD_ADDRESS_HOUSE				= "house";
	
	static final String					DB_FIELD_ADDRESS_APARTMENT		= "apartment";
	
	static final String					DB_FIELD_ADDRESS_POSTAL_CODE	= "postal_code";
	
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
	
	private Address() {}
	
	public static Address create() {
		return new Address();
	}
	
	public static Address create( final Document doc ) {
		final Address addr = new Address();
		addr.setAddressLocationId( doc.getString( DB_FIELD_ADDRESS_LOCATION_ID ) );
		addr.setAddressPlaceId( doc.getString( DB_FIELD_ADDRESS_PLACE_ID ) );
		addr.apartment = doc.getString( DB_FIELD_ADDRESS_APARTMENT );
		addr.house = doc.getString( DB_FIELD_ADDRESS_HOUSE );
		addr.postalCode = doc.getString( DB_FIELD_ADDRESS_POSTAL_CODE );
		return addr;
	}
	
	public AddressLocation getAddressLocation() {
		return addressLocation;
	}
	
	public void setAddressLocation( final AddressLocation address ) {
		if ( address != null )
			if ( !address.equals( this.addressLocation ) ) {
				this.addressLocation = address;
				// Тут тра переробити
				this.addressLocationId = address.getId();
			}
	}
	
	public String getAddressLocationId() {
		return addressLocationId;
	}
	
	public void setAddressLocationId( final String addressLocationId ) {
		if ( addressLocationId != null && !addressLocationId.isEmpty() )
			if ( !addressLocationId.equals( this.addressLocationId ) )
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
		if ( address != null )
			if ( !address.equals( this.addressPlace ) ) {
				this.addressPlace = address;
				// Тут тра переробити
				this.addressPlaceId = address.getId();
			}
	}
	
	public String getAddressPlaceId() {
		return addressPlaceId;
	}
	
	public void setAddressPlaceId( final String addressPlaceId ) {
		if ( addressPlaceId != null && !addressPlaceId.isEmpty() )
			if ( !addressPlaceId.equals( this.addressPlaceId ) )
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
	
	Document getDocument() {
		if ( addressLocationId != null && !addressLocationId.isEmpty() && addressPlaceId != null && !addressPlaceId.isEmpty() ) {
			final Document doc = new Document();
			doc.put( DB_FIELD_ADDRESS_LOCATION_ID, addressLocationId );
			doc.put( DB_FIELD_ADDRESS_PLACE_ID, addressPlaceId );
			if ( house != null && !house.isEmpty() )
				doc.put( DB_FIELD_ADDRESS_HOUSE, house );
			if ( apartment != null && !apartment.isEmpty() )
				doc.put( DB_FIELD_ADDRESS_APARTMENT, apartment );
			if ( postalCode != null && !postalCode.isEmpty() )
				doc.put( DB_FIELD_ADDRESS_POSTAL_CODE, postalCode );
			return doc;
		} else
			return null;
	}
	
	@Override
	public boolean equals( final Object o ) {
		if ( o == null )
			return false;
		if ( o instanceof Address ) {
			final Address address = Address.class.cast( o );
			return address.getAddressLocation().equals( getAddressLocation() )
					&& address.getAddressPlace().equals( getAddressPlace() )
					&& ( address.getApartment() != null && address.getApartment().equals( getApartment() ) || address.getApartment() == null
							&& getApartment() == null )
					&& ( address.getHouse() != null && address.getHouse().equals( getHouse() ) || address.getHouse() == null
							&& getHouse() == null )
					&& ( address.getPostalCode() != null && address.getPostalCode().equals( getPostalCode() ) || address.getPostalCode() == null
							&& getPostalCode() == null );
		} else
			return false;
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
			sb.append( " кв. " );
			sb.append( apartment );
			sb.append( ", " );
		}
		sb.append( addressLocation );
		sb.append( ", " );
		sb.append( addressLocation.getTopAddress().toString() );
		return sb.toString();
	}
}
