package mk.ck.energy.csm.model;

import org.bson.Document;

public class Documents {
	
	static final String	DB_FIELD_ID_CODE					= "id_code";
	
	static final String	DB_FIELD_PASSPORT_SERIES	= "passport_series";
	
	static final String	DB_FIELD_PASSPORT_NUMBER	= "passport_number";
	
	private String			passportSeries;
	
	private String			passportNumber;
	
	private String			idCode;
	
	private Documents() {}
	
	private Documents( final String id, final String passportSeries, final String passportNumber ) {
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
	
	Document getDocument() {
		final Document doc = new Document();
		if ( idCode != null && !idCode.isEmpty() )
			doc.put( DB_FIELD_ID_CODE, idCode );
		if ( passportSeries != null && !passportSeries.isEmpty() )
			doc.put( DB_FIELD_PASSPORT_SERIES, passportSeries );
		if ( passportNumber != null && !passportNumber.isEmpty() )
			doc.put( DB_FIELD_PASSPORT_NUMBER, passportNumber );
		return doc;
	}
	
	@Override
	public boolean equals( final Object o ) {
		if ( o == null )
			return false;
		if ( o instanceof Documents ) {
			final Documents documents = Documents.class.cast( o );
			return ( documents.getIdentificationCode() != null && documents.getIdentificationCode().equals( getIdentificationCode() ) || documents
					.getIdentificationCode() == null && getIdentificationCode() == null )
					&& ( documents.getPassportNumber() != null && documents.getPassportNumber().equals( getPassportNumber() ) || documents
							.getPassportNumber() == null && getPassportNumber() == null )
					&& ( documents.getPassportSeries() != null && documents.getPassportNumber().equals( getPassportNumber() ) || documents
							.getPassportNumber() == null && getPassportNumber() == null );
		} else
			return false;
	}
	
	public static Documents create() {
		return new Documents();
	}
	
	public static Documents create( final String id, final String passportSeries, final String passportNumber ) {
		return new Documents( id, passportSeries, passportNumber );
	}
	
	public static Documents create( final Document doc ) {
		return new Documents( doc.getString( DB_FIELD_ID_CODE ), doc.getString( DB_FIELD_PASSPORT_SERIES ),
				doc.getString( DB_FIELD_PASSPORT_NUMBER ) );
	}
}
