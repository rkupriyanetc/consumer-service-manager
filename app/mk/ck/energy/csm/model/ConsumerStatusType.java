package mk.ck.energy.csm.model;

public enum ConsumerStatusType {
	/**
	 * Активний абонент: постійно користується електроенергією
	 */
	ACTIVE( "actv" ),
	/**
	 * Не завжди користується електроенергією, дачник та т.і.
	 */
	TEMPORARILY( "temp" ),
	/**
	 * Мале використання
	 */
	LITTLE_USE( "litt" ),
	/**
	 * Споживач відсутні більше року
	 */
	UNAVAILABLE_MORE_ONE_YEAR( "unmy" ),
	/**
	 * Попереджений на відключення
	 */
	WARNED( "warn" ),
	/**
	 * Відключений на Р.Щ.
	 */
	OFF_ON_PANEL( "opan" ),
	/**
	 * Відключений біля лічильника
	 */
	OFF_ON_METER( "omet" ),
	/**
	 * Відключений на опорі
	 */
	OFF_ON_PILLAR( "opil" ),
	/**
	 * Закритий рахунок споживача
	 */
	CLOSED( "clos" ), ;
	
	private String	name;
	
	ConsumerStatusType() {}
	
	ConsumerStatusType( final String name ) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
