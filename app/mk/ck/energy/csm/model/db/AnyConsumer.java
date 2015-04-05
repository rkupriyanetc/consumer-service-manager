package mk.ck.energy.csm.model.db;

import java.util.List;

import mk.ck.energy.csm.model.Address;
import mk.ck.energy.csm.model.Meter;
import mk.ck.energy.csm.model.auth.User;

public interface AnyConsumer extends Identifier {
	
	User getUser();
	
	String getFullName();
	
	Address getAddress();
	
	boolean isActive();
	
	List< Meter > getMeters();
	
	String getOrganization();
}
