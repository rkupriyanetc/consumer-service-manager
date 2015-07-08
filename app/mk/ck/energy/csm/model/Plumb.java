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
	
	private Plumb() {}
	
	public static Plumb create() {
		return new Plumb();
	}
	
	public static Plumb create( final String number, final long installDate, final String inspector, final PlumbType type ) {
		final Plumb pl = new Plumb();
		pl.setPlumbType( type == null ? PlumbType.SECURITY : type );
		pl.setNumber( number );
		pl.setDateInstall( installDate );
		pl.setMasterName( inspector );
		pl.setDateUninstall( Meter.MAXDATE.getTime() );
		return pl;
	}
	
	/*
	 * № пломби
	 */
	public String getNumber() {
		return getString( DB_FIELD_NUMBER );
	}
	
	public void setNumber( final String number ) {
		put( DB_FIELD_NUMBER, number );
	}
	
	/*
	 * Початок дії ( дата встановлення ) пломби
	 */
	public long getDateInstall() {
		return getLong( DB_FIELD_DATE_INSTALL );
	}
	
	public void setDateInstall( final long dateInstall ) {
		put( DB_FIELD_DATE_INSTALL, dateInstall );
	}
	
	/*
	 * Закінчення дії ( дата зняття ) пломби
	 */
	public long getDateUninstall() {
		return getLong( DB_FIELD_DATE_UNINSTALL );
	}
	
	public void setDateUninstall( final long dateUninstall ) {
		put( DB_FIELD_DATE_UNINSTALL, dateUninstall );
	}
	
	/*
	 * Майстер, що встановив пломбу
	 */
	public String getMasterName() {
		return getString( DB_FIELD_MASTER_NAME );
	}
	
	public void setMasterName( final String masterName ) {
		put( DB_FIELD_MASTER_NAME, masterName );
	}
	
	/*
	 * Тип пломби
	 */
	public PlumbType getPlumbType() {
		return PlumbType.valueOf( getString( DB_FIELD_PLUMB_TYPE ) );
	}
	
	public void setPlumbType( final PlumbType type ) {
		put( DB_FIELD_PLUMB_TYPE, type.name() );
	}
	
	@Override
	public boolean equals( final Object o ) {
		if ( o == null || !( o instanceof Plumb ) )
			return false;
		final Plumb plumb = ( Plumb )o;
		return plumb.getNumber().equalsIgnoreCase( getNumber() );
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
