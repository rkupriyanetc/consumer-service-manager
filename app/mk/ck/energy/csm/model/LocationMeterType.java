package mk.ck.energy.csm.model;

public enum LocationMeterType {
	/**
	 * Встановлено в контейнері
	 */
	CONTAINER,
	/**
	 * В квартирі чи будинку
	 */
	APARTMENT,
	/**
	 * Встановлено в шкафній
	 */
	SAFE,
	/**
	 * В коридорі будинку, під'їзду
	 */
	CORRIDOR,
	/**
	 * АСКОЕ. Automated system of commercial metering electricity
	 */
	ASCME,
	/**
	 * Непристосоване приміщення
	 */
	UNCOMFORTABLE,
	/**
	 * Інше
	 */
	OTHER,
}
