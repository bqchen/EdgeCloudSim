/*
 * Title:        EdgeCloudSim - EdgeVM
 * 
 * Description: 
 * EdgeVM adds vm type information over CloudSim's VM class
 *               
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2017, Bogazici University, Istanbul, Turkey
 */

package edu.boun.edgecloudsim.edge_server;

import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.Vm;

//import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.utils.SimInputConfig;

public class EdgeVM extends Vm {
	private SimInputConfig.VM_TYPES type;
	
	public EdgeVM(int id, int userId, double mips, int numberOfPes, int ram,
			long bw, long size, String vmm, CloudletScheduler cloudletScheduler) {
		super(id, userId, mips, numberOfPes, ram, bw, size, vmm, cloudletScheduler);

	}

	public void setVmType(SimInputConfig.VM_TYPES _type){
		type=_type;
	}
	
	public SimInputConfig.VM_TYPES getVmType(){
		return type;
	}
}
