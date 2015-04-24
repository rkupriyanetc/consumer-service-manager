package mk.ck.energy.csm.model;

import java.util.LinkedHashMap;
import java.util.Map;

import play.i18n.Messages;

public enum AdministrativeCenterType {
	/**
	 * Столиця
	 */
	CAPITAL,
	/**
	 * Обласний центр
	 */
	REGIONAL,
	/**
	 * Районний центр
	 */
	DISTRICT,
	/**
	 * Невизначений тип адміністративного центру
	 */
	UNSPECIFIED, ;
	
	public static Map< String, String > optionsFullname() {
		final Map< String, String > vals = new LinkedHashMap<>();
		for ( final AdministrativeCenterType lType : AdministrativeCenterType.values() )
			vals.put( lType.name(), Messages.get( Address.ADMINISTRATIVE_TYPE_FULLNAME + "." + lType.name().toLowerCase() ) );
		return vals;
	}
	
	public static Map< String, String > optionsShortname() {
		final Map< String, String > vals = new LinkedHashMap<>();
		for ( final AdministrativeCenterType lType : AdministrativeCenterType.values() )
			vals.put( lType.name(), Messages.get( Address.ADMINISTRATIVE_TYPE_SHORTNAME + "." + lType.name().toLowerCase() ) );
		return vals;
	}
	
	public static Map< String, String > optionsValues() {
		final Map< String, String > vals = new LinkedHashMap<>();
		for ( final AdministrativeCenterType lType : AdministrativeCenterType.values() )
			vals.put( lType.name(), lType.name() );
		return vals;
	}
	
	public String toString( final String method ) {
		return Messages.get( method + "." + name().toLowerCase() );
	}
}
