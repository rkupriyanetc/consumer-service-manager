package mk.ck.energy.csm.model;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class Plumb {
	
	private String		number;
	
	private long			installDate;
	
	private long			uninstallDate;
	
	private String		inspector;
	
	private PlumbType	type;
	
	public Plumb( final String number, final long installDate, final String inspector, final PlumbType type ) {
		if ( type == null )
			this.type = PlumbType.SECURITY;
		else
			this.type = type;
		this.number = number;
		this.installDate = installDate;
		this.inspector = inspector;
		this.uninstallDate = Meter.MAXDATE.getTime();
	}
	
	/**
	 * public Plumb( final DBObject dbo ) {
	 * this.number = ( String )dbo.get( "number" );
	 * this.installDate = ( ( Long )dbo.get( "install_date" ) ).longValue();
	 * this.uninstallDate = ( ( Long )dbo.get( "uninstall_date" ) ).longValue();
	 * this.inspector = ( String )dbo.get( "inspector" );
	 * final String type = ( String )dbo.get( "type" );
	 * if ( type != null && !type.isEmpty() )
	 * this.type = PlumbType.valueOf( type );
	 * }
	 */
	public String getNumber() {
		return number;
	}
	
	public void setNumber( final String number ) {
		this.number = number;
	}
	
	public long getInstallDate() {
		return installDate;
	}
	
	public void setInstallDate( final long installDate ) {
		this.installDate = installDate;
	}
	
	public long getUninstallDate() {
		return uninstallDate;
	}
	
	public void setUninstallDate( final long uninstallDate ) {
		this.uninstallDate = uninstallDate;
	}
	
	public String getInspector() {
		return inspector;
	}
	
	public void setInspector( final String inspector ) {
		this.inspector = inspector;
	}
	
	public PlumbType getType() {
		return type;
	}
	
	public void setType( final PlumbType type ) {
		this.type = type;
	}
	
	@Override
	public boolean equals( final Object o ) {
		if ( o == null || !( o instanceof Plumb ) )
			return false;
		final Plumb plumb = ( Plumb )o;
		return plumb.getNumber().equalsIgnoreCase( number );
	}
	
	DBObject getDBObject() {
		final DBObject doc = new BasicDBObject( "number", number );
		if ( inspector != null && !inspector.isEmpty() )
			doc.put( "inspector", inspector );
		if ( installDate > 0 )
			doc.put( "install_date", installDate );
		if ( uninstallDate > 0 )
			doc.put( "uninstall_date", uninstallDate );
		doc.put( "type", type.name() );
		return doc;
	}
}
