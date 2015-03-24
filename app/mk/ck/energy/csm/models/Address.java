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
	
	/**
	 * Деяка частина адреси: населений пункт.
	 */
	private AddressLocation			addressLocation;
	
	/**
	 * Деяка частина адреси: вулиця.
	 */
	private AddressPlace				addressPlace;
	
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
	
	public AddressLocation getAddressLocation() {
		return addressLocation;
	}
	
	public void setAddressLocation( final AddressLocation address ) {
		this.addressLocation = address;
	}
	
	public AddressPlace getAddressPlace() {
		return addressPlace;
	}
	
	public void setAddressPlace( final AddressPlace address ) {
		this.addressPlace = address;
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
				doc.put( "addressLocation_id", addressLocation.getId() );
			}
			if ( UPDATING_READING_ALL == updateSet || ( updateSet & UPDATING_READING_ADDRESS_PLACE ) == UPDATING_READING_ADDRESS_PLACE ) {
				addressPlace.save();
				doc.put( "addressPlace_id", addressPlace.getId() );
			}
			if ( UPDATING_READING_ALL == updateSet || ( updateSet & UPDATING_READING_HOUSE ) == UPDATING_READING_HOUSE )
				if ( house != null && !house.isEmpty() )
					doc.put( "house", house );
			if ( UPDATING_READING_ALL == updateSet || ( updateSet & UPDATING_READING_APARTMENT ) == UPDATING_READING_APARTMENT )
				if ( apartment != null && !apartment.isEmpty() )
					doc.put( "apartment", apartment );
			if ( UPDATING_READING_ALL == updateSet || ( updateSet & UPDATING_READING_POSTAL_CODE ) == UPDATING_READING_POSTAL_CODE )
				if ( postalCode != null && postalCode.isEmpty() )
					doc.put( "postal_code", postalCode );
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
