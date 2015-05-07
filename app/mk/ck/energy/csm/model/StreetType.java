package mk.ck.energy.csm.model;

import java.util.LinkedHashMap;
import java.util.Map;

import play.i18n.Messages;

public enum StreetType {
	/**
	 * Не визначений тип вулиці. Коли неправильно описаний тип: будь що, що не
	 * входить в стандартний набір ( пл., бул., прсп., прс., вул., прв., пров.,
	 * туп., узв., прз., прзд., наб.. "." та "" - це тип UNCERTAIN )
	 */
	UNSPECIFIED( 0 ),
	/**
	 * Тип вулиці без типу. Наприклад: Карпати, Центр, Лісництво. Це вулиці без
	 * підпису.
	 */
	UNCERTAIN( 1 ),
	/**
	 * Площа. Такі як: Вокзальна площа, Площа Катедральна (Львів), площа Старий
	 * Ринок та інші.
	 */
	AREA( 2 ),
	/**
	 * <a href=
	 * "http://uk.wikipedia.org/wiki/%D0%91%D1%83%D0%BB%D1%8C%D0%B2%D0%B0%D1%80"
	 * >Бульвар</a> — вулиця, яка має розміщену вздовж її осі (зазвичай
	 * посередині) широку обсаджену деревами алею з лавами для відпочинку.
	 */
	BOULEVARD( 3 ),
	/**
	 * <a href=
	 * "http://uk.wikipedia.org/wiki/%D0%9F%D1%80%D0%BE%D1%81%D0%BF%D0%B5%D0%BA%D1%82"
	 * >Проспект</a> — пряма, довга, широка вулиця з твердим покриттям та
	 * зеленими насадженнями вздовж вулиці.
	 */
	AVENUE( 4 ),
	/**
	 * <a
	 * href="http://uk.wikipedia.org/wiki/%D0%92%D1%83%D0%BB%D0%B8%D1%86%D1%8F">
	 * Вулиця</a> — обмежений принаймні з одного боку рядом будинків простір у
	 * межах міста або іншого населеного пункту, призначений для проїзду
	 * транспорту та ходіння.
	 */
	STREET( 5 ),
	/**
	 * <a href=
	 * "http://uk.wikipedia.org/wiki/%D0%9F%D1%80%D0%BE%D0%B2%D1%83%D0%BB%D0%BE%D0%BA"
	 * >Провулок</a> — невеличка вулиця, що з'єднує дві більших.
	 */
	LANE( 6 ),
	/**
	 * Тупик (укр. сліпа вулиця) — вулиця або провулок, що не мають наскрізного
	 * проходу, проїзду.
	 */
	CUL_DE_SAC( 7 ),
	/**
	 * <a href="http://uk.wikipedia.org/wiki/%D0%A3%D0%B7%D0%B2%D1%96%D0%B7">
	 * Узвіз</a> — вулиця, що має крутий підйом. (наприклад, Андріївський узвіз у
	 * Києві).
	 */
	DESCENT( 8 ),
	/**
	 * Проїзд
	 */
	PASSAGE( 9 ),
	/**
	 * <a
	 * href="http://uk.wikipedia.org/wiki/%D0%92%D1%83%D0%BB%D0%B8%D1%86%D1%8F">
	 * Набережна</a> — вулиця вздовж річки чи великої водойми (озеро, море,
	 * океан).
	 */
	QUAY( 10 ), ;
	
	private int	id;
	
	StreetType() {}
	
	StreetType( final int id ) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public static Map< String, String > optionsFullname() {
		final Map< String, String > vals = new LinkedHashMap< String, String >();
		for ( final StreetType sType : StreetType.values() )
			vals.put( sType.name(), Messages.get( Address.STREET_TYPE_FULLNAME + "." + sType.name().toLowerCase() ) );
		return vals;
	}
	
	public static Map< String, String > optionsShortname() {
		final Map< String, String > vals = new LinkedHashMap< String, String >();
		for ( final StreetType sType : StreetType.values() )
			vals.put( sType.name(), Messages.get( Address.STREET_TYPE_SHORTNAME + "." + sType.name().toLowerCase() ) );
		return vals;
	}
	
	public static Map< String, String > optionsValues() {
		final Map< String, String > vals = new LinkedHashMap< String, String >();
		for ( final StreetType lType : StreetType.values() )
			vals.put( lType.name(), lType.name() );
		return vals;
	}
	
	public static StreetType abbreviationToStreetType( final String abbreviation ) {
		StreetType st;
		switch ( abbreviation ) {
			case "пл." :
				st = StreetType.AREA;
				break;
			case "бул." :
				st = StreetType.BOULEVARD;
				break;
			case "прсп." :
				st = StreetType.AVENUE;
				break;
			case "прс." :
				st = StreetType.AVENUE;
				break;
			case "вул." :
				st = StreetType.STREET;
				break;
			case "прв." :
				st = StreetType.LANE;
				break;
			case "пров." :
				st = StreetType.LANE;
				break;
			case "туп." :
				st = StreetType.CUL_DE_SAC;
				break;
			case "узв." :
				st = StreetType.DESCENT;
				break;
			case "прз." :
				st = StreetType.PASSAGE;
				break;
			case "прзд." :
				st = StreetType.PASSAGE;
				break;
			case "наб." :
				st = StreetType.PASSAGE;
				break;
			case "" :
				st = StreetType.UNCERTAIN;
				break;
			case "." :
				st = StreetType.UNCERTAIN;
				break;
			default :
				st = StreetType.UNSPECIFIED;
				break;
		}
		return st;
	}
	
	public boolean equals( final StreetType o ) {
		if ( o == null )
			return false;
		return name().equals( o.name() );
	}
	
	public String toString( final String method ) {
		return Messages.get( method + "." + name().toLowerCase() );
	}
}
