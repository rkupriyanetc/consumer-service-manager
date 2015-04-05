package mk.ck.energy.csm.model.util;

import java.util.List;

import mk.ck.energy.csm.model.Address;
import mk.ck.energy.csm.model.Meter;
import mk.ck.energy.csm.model.auth.User;

public interface AnyConsumer {
	
	String getId();
	
	User getUser();
	
	String getFullName();
	
	Address getAddress();
	
	boolean isActive();
	
	List< Meter > getMeters();
	
	String getOrganization();
}
