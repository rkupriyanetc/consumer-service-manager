package mk.ck.energy.csm.model;

import mk.ck.energy.csm.model.mongodb.CSMAbstractDocument;

import org.bson.BsonDocument;
import org.bson.BsonDocumentWrapper;
import org.bson.codecs.configuration.CodecRegistry;

import com.mongodb.client.MongoCollection;

public class Plumb extends CSMAbstractDocument< Plumb > {
	
	private static final long		serialVersionUID				= 1L;
	
	private static final String	COLLECTION_NAME_PLUMBS	= "plumbs";
	
	private static final String	DB_FIELD_NUMBER					= "number";
	
	private static final String	DB_FIELD_DATE_INSTALL		= "date_install";
	
	private static final String	DB_FIELD_DATE_UNINSTALL	= "date_uninstall";
	
	private static final String	DB_FIELD_MASTER_NAME		= "master_name";
	
	private static final String	DB_FIELD_PLUMB_TYPE			= "plumb_type";
	
	public enum PlumbType {
		SECURITY, IMS, STICKER, ;
		
		public boolean equals( final PlumbType o ) {
			if ( o == null )
				return false;
			return name().equals( o.name() );
		}
	}
	
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
	
	/*
	 * DBObject getDBObject() {
	 * final DBObject doc = new BasicDBObject( "number", number );
	 * if ( inspector != null && !inspector.isEmpty() )
	 * doc.put( "inspector", inspector );
	 * if ( installDate > 0 )
	 * doc.put( "install_date", installDate );
	 * if ( uninstallDate > 0 )
	 * doc.put( "uninstall_date", uninstallDate );
	 * doc.put( "type", type.name() );
	 * return doc;
	 * }
	 */
	@Override
	public < TDocument >BsonDocument toBsonDocument( final Class< TDocument > documentClass, final CodecRegistry codecRegistry ) {
		return new BsonDocumentWrapper< Plumb >( this, codecRegistry.get( Plumb.class ) );
	}
	
	public static MongoCollection< Plumb > getMongoCollection() {
		final MongoCollection< Plumb > collection = getDatabase().getCollection( COLLECTION_NAME_PLUMBS, Plumb.class );
		return collection;
	}
	
	@Override
	protected MongoCollection< Plumb > getCollection() {
		return getMongoCollection();
	}
}
