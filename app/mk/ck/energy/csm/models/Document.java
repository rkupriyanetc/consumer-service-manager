package mk.ck.energy.csm.models;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class Document {
	
	static final String	DB_FIELD_ID_CODE					= "id_code";
	
	static final String	DB_FIELD_PASSPORT_SERIES	= "passport_series";
	
	static final String	DB_FIELD_PASSPORT_NUMBER	= "passport_number";
	
	private String			passportSeries;
	
	private String			passportNumber;
	
	private String			idCode;
	
	public Document( final String id, final String passportSeries, final String passportNumber ) {
		this.passportSeries = passportSeries;
		this.passportNumber = passportNumber;
		this.idCode = id;
	}
	
	public String getPassportSeries() {
		return passportSeries;
	}
	
	public void setPassportSeries( final String passportSeries ) {
		this.passportSeries = passportSeries;
	}
	
	public String getPassportNumber() {
		return passportNumber;
	}
	
	public void setPassportNumber( final String passportNumber ) {
		this.passportNumber = passportNumber;
	}
	
	public String getIdentificationCode() {
		return idCode;
	}
	
	public void setIdentificationCode( final String id ) {
		this.idCode = id;
	}
	
	DBObject getDBObject() {
		final DBObject doc = new BasicDBObject();
		if ( idCode != null && !idCode.isEmpty() )
			doc.put( DB_FIELD_ID_CODE, idCode );
		if ( passportSeries != null && !passportSeries.isEmpty() )
			doc.put( DB_FIELD_PASSPORT_SERIES, passportSeries );
		if ( passportNumber != null && !passportNumber.isEmpty() )
			doc.put( DB_FIELD_PASSPORT_NUMBER, passportNumber );
		return doc;
	}
}
