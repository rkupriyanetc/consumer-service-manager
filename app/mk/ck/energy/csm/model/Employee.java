package mk.ck.energy.csm.model;

public class Employee {
	
	public static final String	XLS_FILENAME_OUTPUT	= "Employees.xls";
	
	private String							remId;
	
	private String							fullname;
	
	private String							address;
	
	private String							phoneBusiness;
	
	private String							phoneHome;
	
	private String							phoneMobile;
	
	private String							position;
	
	private String							meterName;
	
	private String							meterNumber;
	
	private byte								meterPhase;
	
	private short								meterCurrent;
	
	private String							meterPlumb;
	
	private String							latterFigure;
	
	private String							birthday;
	
	private String							employmentDay;
	
	private boolean							toPhoneBook;
	
	public Employee() {}
	
	public String getRemId() {
		return remId;
	}
	
	public void setRemId( final String remId ) {
		this.remId = remId;
	}
	
	public String getFullname() {
		return fullname;
	}
	
	public void setFullname( final String fullname ) {
		this.fullname = fullname;
	}
	
	public String getAddress() {
		return address;
	}
	
	public void setAddress( final String address ) {
		this.address = address;
	}
	
	public String getPhoneBusiness() {
		return phoneBusiness;
	}
	
	public void setPhoneBusiness( final String phoneBusiness ) {
		this.phoneBusiness = phoneBusiness;
	}
	
	public String getPhoneHome() {
		return phoneHome;
	}
	
	public void setPhoneHome( final String phoneHome ) {
		this.phoneHome = phoneHome;
	}
	
	public String getPhoneMobile() {
		return phoneMobile;
	}
	
	public void setPhoneMobile( final String phoneMobile ) {
		this.phoneMobile = phoneMobile;
	}
	
	public String getPosition() {
		return position;
	}
	
	public void setPosition( final String position ) {
		this.position = position;
	}
	
	public String getMeterName() {
		return meterName;
	}
	
	public void setMeterName( final String meterName ) {
		this.meterName = meterName;
	}
	
	public String getMeterNumber() {
		return meterNumber;
	}
	
	public void setMeterNumber( final String meterNumber ) {
		this.meterNumber = meterNumber;
	}
	
	public byte getMeterPhase() {
		return meterPhase;
	}
	
	public void setMeterPhase( final byte meterPhase ) {
		this.meterPhase = meterPhase;
	}
	
	public short getMeterCurrent() {
		return meterCurrent;
	}
	
	public void setMeterCurrent( final short meterCurrent ) {
		this.meterCurrent = meterCurrent;
	}
	
	public String getMeterPlumb() {
		return meterPlumb;
	}
	
	public void setMeterPlumb( final String meterPlumb ) {
		this.meterPlumb = meterPlumb;
	}
	
	public String getLatterFigure() {
		return latterFigure;
	}
	
	public void setLatterFigure( final String latterFigure ) {
		this.latterFigure = latterFigure;
	}
	
	public String getBirthday() {
		return birthday;
	}
	
	public void setBirthday( final String birthday ) {
		this.birthday = birthday;
	}
	
	public String getEmploymentDay() {
		return employmentDay;
	}
	
	public void setEmploymentDay( final String employmentDay ) {
		this.employmentDay = employmentDay;
	}
	
	public boolean isToPhoneBook() {
		return toPhoneBook;
	}
	
	public void setToPhoneBook( final boolean toPhoneBook ) {
		this.toPhoneBook = toPhoneBook;
	}
}
