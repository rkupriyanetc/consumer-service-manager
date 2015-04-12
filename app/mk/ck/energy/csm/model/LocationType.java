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
	BOWERY;
	
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
	
	public boolean equals( final LocationType o ) {
		if ( o == null )
			return false;
		return name().equals( o.name() );
	}
	
	public String toString( final String method ) {
		return Messages.get( method + "." + name().toLowerCase() );
	}
}
