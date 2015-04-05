package mk.ck.energy.csm.model;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * @author RVK
 *         Designed to export data of consumers from another database
 */
public class UndefinedConsumer {
	
	private final Consumer							consumer;
	
	private final UndefinedConsumerType	undefinedType;
	
	public UndefinedConsumer( final Consumer consumer, final UndefinedConsumerType undefinedType ) {
		this.consumer = consumer;
		this.undefinedType = undefinedType;
		save();
	}
	
	public Consumer consumer() {
		return consumer;
	}
	
	public UndefinedConsumerType undefinedType() {
		return undefinedType;
	}
	
	private DBObject getDBObject() {
		final DBObject doc = new BasicDBObject( "_id", consumer.getId() );
		doc.put( "type", undefinedType.name() );
		return doc;
	}
	
	public void save() {
		getUndefinedConsumerCollection().save( getDBObject() );
	}
	
	private static DBCollection getUndefinedConsumerCollection() {
		return Database.getInstance().getDatabase().getCollection( "consumersUndefined" );
	}
}
