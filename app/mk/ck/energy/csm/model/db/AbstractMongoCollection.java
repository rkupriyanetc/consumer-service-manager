package mk.ck.energy.csm.model.db;

import java.util.UUID;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;

public abstract class AbstractMongoCollection implements Identifier {
	
	public static final String	DB_FIELD_ID	= "_id";
	
	private String							id;
	
	@Override
	public String getId() {
		return id;
	}
	
	public void setId( final String id ) {
		this.id = id;
	}
	
	private String getOrCreateId() {
		if ( id == null )
			id = UUID.randomUUID().toString().toLowerCase();
		return id;
	}
	
	public void save() {
		id = getOrCreateId();
		getCollection().updateOne( new Document( DB_FIELD_ID, id ), getDocument(), new UpdateOptions().upsert( true ) );
	}
	
	public abstract MongoCollection< Document > getCollection();
}
