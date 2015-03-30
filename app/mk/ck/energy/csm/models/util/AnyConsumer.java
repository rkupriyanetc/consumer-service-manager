package mk.ck.energy.csm.models.util;

import java.util.List;

import mk.ck.energy.csm.models.Address;
import mk.ck.energy.csm.models.Meter;
import mk.ck.energy.csm.models.auth.User;

public interface AnyConsumer {
	
	String getId();
	
	User getUser();
	
	String getFullName();
	
	Address getAddress();
	
	boolean isActive();
	
	List< Meter > getMeters();
	
	String getOrganization();
}
