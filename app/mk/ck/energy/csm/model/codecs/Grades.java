package mk.ck.energy.csm.model.codecs;

import org.bson.BsonDocument;
import org.bson.BsonDocumentWrapper;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

public class Grades implements Bson {
	
	private ObjectId	_id;
	
	private int				studentId;
	
	private String		type;
	
	private double		score;
	
	public Grades() {}
	
	public Grades( final int id, final String type, final double score ) {
		this.studentId = id;
		this.type = type;
		this.score = score;
	}
	
	public Grades withNewObjectId() {
		setId( new ObjectId() );
		return this;
	}
	
	public ObjectId getId() {
		return _id;
	}
	
	public void setId( final ObjectId _id ) {
		this._id = _id;
	}
	
	public int getStudentId() {
		return studentId;
	}
	
	public void setStudentId( final int studentId ) {
		this.studentId = studentId;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType( final String type ) {
		this.type = type;
	}
	
	public double getScore() {
		return score;
	}
	
	public void setScore( final double score ) {
		this.score = score;
	}
	
	@Override
	public < TDocument >BsonDocument toBsonDocument( final Class< TDocument > documentClass, final CodecRegistry codecRegistry ) {
		return new BsonDocumentWrapper< Grades >( this, codecRegistry.get( Grades.class ) );
	}
	
	@Override
	public String toString() {
		return "Grades [_id=" + _id + ", studentId=" + studentId + ", type=" + type + ", score=" + score + "]";
	}
}
