package mk.ck.energy.csm.model.db;

import java.util.UUID;

import mk.ck.energy.csm.model.Database;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;

public abstract class AbstractMongoCollection implements Identifier {
	
	public static final String	DB_FIELD_ID												= "_id";
	
	public static final String	COLLECTION_NAME_TOP_ADDRESS				= "topAddresses";
	
	public static final String	COLLECTION_NAME_LOCATION_ADDRESS	= "locationAddresses";
	
	public static final String	COLLECTION_NAME_PLACE_ADDRESS			= "placeAddresses";
	
	private String							id;
	
	@Override
	public String getId() {
		return id;
	}
	
	public void setId( final String id ) {
		this.id = id;
	}
	
	protected String getOrCreateId() {
		if ( id == null )
			id = UUID.randomUUID().toString().toLowerCase();
		return id;
	}
	
	public void save( final String coolectionName ) {
		id = getOrCreateId();
		getCollection( coolectionName )
				.updateOne( new Document( DB_FIELD_ID, id ), getDocument(), new UpdateOptions().upsert( true ) );
	}
	
	public static MongoCollection< Document > getCollection( final String coolectionName ) {
		return Database.getInstance().getDatabase().getCollection( coolectionName );
	}
}
