package mk.ck.energy.csm.model;

import java.util.LinkedHashMap;
import java.util.Map;

import play.i18n.Messages;

public enum LocationType {
	/**
	 * Місто
	 */
	CITY,
	/**
	 * Невелике місто, селище, селище міського типу
	 */
	TOWNSHIP,
	/**
	 * Село
	 */
	VILLAGE,
	/**
	 * Хутір
	 */
	HAMLET,
	/**
	 * Окрема садиба
	 */
	BOWERY,
	/**
	 * Невизначений тип населенного пункту
	 */
	UNSPECIFIED, ;
	
	public static Map< String, String > optionsFullname() {
		final Map< String, String > vals = new LinkedHashMap<>();
		for ( final LocationType lType : LocationType.values() )
			vals.put( lType.name(), Messages.get( Address.LOCATION_TYPE_FULLNAME + "." + lType.name().toLowerCase() ) );
		return vals;
	}
	
	public static Map< String, String > optionsShortname() {
		final Map< String, String > vals = new LinkedHashMap<>();
		for ( final LocationType lType : LocationType.values() )
			vals.put( lType.name(), Messages.get( Address.LOCATION_TYPE_SHORTNAME + "." + lType.name().toLowerCase() ) );
		return vals;
	}
	
	public static Map< String, String > optionsValues() {
		final Map< String, String > vals = new LinkedHashMap<>();
		for ( final LocationType lType : LocationType.values() )
			vals.put( lType.name(), lType.name() );
		return vals;
	}
	
	public static LocationType abbreviationToLocationType( final String abbreviation ) {
		LocationType lt;
		switch ( abbreviation ) {
			case "с." :
				lt = LocationType.VILLAGE;
				break;
			case "м." :
				lt = LocationType.CITY;
				break;
			case "смт." :
				lt = LocationType.TOWNSHIP;
				break;
			case "х." :
				lt = LocationType.HAMLET;
				break;
			case "сад." :
				lt = LocationType.BOWERY;
				break;
			default :
				lt = LocationType.UNSPECIFIED;
				break;
		}
		return lt;
	}
	
	public boolean equals( final LocationType o ) {
		if ( o == null )
			return false;
		return name().equals( o.name() );
	}
	
	public String toString( final String method ) {
		return Messages.get( method + "." + name().toLowerCase() );
	}
}
