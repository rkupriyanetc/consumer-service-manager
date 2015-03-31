package mk.ck.energy.csm.models;

import java.util.LinkedHashMap;
import java.util.Map;

import play.i18n.Messages;

public enum LocationType {
	/**
	 * Столиця
	 */
	CAPITAL( 0 ),
	/**
	 * Обласний центр
	 */
	REGIONAL( 1 ),
	/**
	 * Районний центр
	 */
	DISTRICT( 2 ),
	/**
	 * Місто
	 */
	CITY( 3 ),
	/**
	 * Невелике місто, селище, селище міського типу
	 */
	TOWNSHIP( 4 ),
	/**
	 * Село
	 */
	VILLAGE( 5 ),
	/**
	 * Хутір
	 */
	HAMLET( 6 ),
	/**
	 * Окрема садиба
	 */
	BOWERY( 7 );
	
	private int	id;
	
	LocationType() {}
	
	LocationType( final int id ) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public static Map< String, String > optionsFullname() {
		final Map< String, String > vals = new LinkedHashMap< String, String >();
		for ( final LocationType lType : LocationType.values() )
			vals.put( lType.name(), Messages.get( Address.LOCATION_TYPE_FULLNAME + "." + lType.name().toLowerCase() ) );
		return vals;
	}
	
	public static Map< String, String > optionsShortname() {
		final Map< String, String > vals = new LinkedHashMap< String, String >();
		for ( final LocationType lType : LocationType.values() )
			vals.put( lType.name(), Messages.get( Address.LOCATION_TYPE_SHORTNAME + "." + lType.name().toLowerCase() ) );
		return vals;
	}
	
	public static Map< String, String > optionsValues() {
		final Map< String, String > vals = new LinkedHashMap< String, String >();
		for ( final LocationType lType : LocationType.values() )
			vals.put( lType.name(), lType.name() );
		return vals;
	}
	
	public String renamingLocationType( final String method ) {
		return Messages.get( method + "." + name().toLowerCase() );
	}
}
