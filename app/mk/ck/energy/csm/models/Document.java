package mk.ck.energy.csm.models;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class Document {
	
	private String	passportSeries;
	
	private String	passportNumber;
	
	private String	idCode;
	
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
			doc.put( "id_code", idCode );
		if ( passportSeries != null && !passportSeries.isEmpty() )
			doc.put( "passport_series", passportSeries );
		if ( passportNumber != null && !passportNumber.isEmpty() )
			doc.put( "passport_number", passportNumber );
		if ( !doc.toMap().isEmpty() )
			return doc;
		else
			return null;
	}
}
