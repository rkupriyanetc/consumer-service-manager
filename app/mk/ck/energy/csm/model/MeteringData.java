package mk.ck.energy.csm.model;

import java.util.HashMap;
import java.util.Map;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class MeteringData {
	
	enum TransferDataType {
		/**
		 * Контрольний огляд лічильника
		 */
		CONTROL,
		/**
		 * Повідомлення показів по телефону або усно
		 */
		PHONE,
		/**
		 * Повідомлення показів на квитанції
		 */
		RECEIPT,
		/**
		 * Повідомлення показів через вебсайт
		 */
		WEBSITE,
		/**
		 * Запис показників при монтажі лічильника
		 */
		INSTALLATION,
		/**
		 * Запис показників при демонтажі лічильника
		 */
		DEINSTALLATION,
	}
	
	enum MultipleData {
		DAY, NIGHT, PEAK,
	}
	
	private String									meterDeviceId;
	
	private MeterDevice							meterDevice;
	
	private long										dateMetering;
	
	private String									inspector;
	
	private TransferDataType				transferDataType;
	
	private Map< String, Integer >	values;
	
	public MeteringData( final MeterDevice meterDevice, final long dateMetering, final String inspector,
			final TransferDataType transferDataType ) {
		this.meterDevice = meterDevice;
		this.meterDeviceId = meterDevice.getId();
		this.dateMetering = dateMetering;
		this.inspector = inspector;
		this.transferDataType = transferDataType == null ? TransferDataType.WEBSITE : transferDataType;
		this.values = new HashMap< String, Integer >( 0 );
	}
	
	public MeterDevice getMeterDevice() {
		return meterDevice;
	}
	
	public void setMeterDevice( final MeterDevice meterDevice ) {
		this.meterDevice = meterDevice;
		this.meterDeviceId = meterDevice.getId();
	}
	
	public Map< String, Integer > getValues() {
		return values;
	}
	
	public void setValues( final Map< String, Integer > values ) {
		this.values = values;
	}
	
	public long getDateMetering() {
		return dateMetering;
	}
	
	public void setDateMetering( final long dateMetering ) {
		this.dateMetering = dateMetering;
	}
	
	public String getInspector() {
		return inspector;
	}
	
	public void setInspector( final String inspector ) {
		this.inspector = inspector;
	}
	
	public TransferDataType getTransferDataType() {
		return transferDataType;
	}
	
	public void setTransferDataType( final TransferDataType transferDataType ) {
		this.transferDataType = transferDataType;
	}
	
	DBObject getDBObject() {
		final DBObject doc = new BasicDBObject( "meter_device_id", meterDeviceId );
		doc.put( "date_metering", dateMetering );
		if ( inspector != null )
			doc.put( "inspector", inspector );
		doc.put( "transfer_data_type", transferDataType.name() );
		if ( !values.isEmpty() )
			doc.put( "values", values );
		return doc;
	}
	
	public void save() {
		final DBObject query = new BasicDBObject( "meter_device_id", meterDeviceId );
		query.put( "date_metering", dateMetering );
		final DBObject doc = getDBObject();
		getMeteringDataCollection().update( query, new BasicDBObject( "$set", doc ), true, false );
	}
	
	private static DBCollection getMeteringDataCollection() {
		return null;// Database.getInstance().getDatabase().getCollection(
								// "meteringData" );
	}
}
