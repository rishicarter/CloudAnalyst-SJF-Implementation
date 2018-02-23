package cloudsim.ext.datacenter;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cloudsim.Cloudlet;
import cloudsim.ext.Constants;
import cloudsim.ext.event.CloudSimEvent;
import cloudsim.ext.event.CloudSimEventListener;
import cloudsim.ext.event.CloudSimEvents;

public class SJF_VmLoadBalance extends VmLoadBalancer implements CloudSimEventListener {

	private Map<Integer, Integer> currentAllocationCounts;
	private static List<Cloudlet> cloudletlist, cloudletlistsjf;
	private Map<Integer, VirtualMachineState> vmStatesList;
	private int listsize,min;
	
	public SJF_VmLoadBalance(DatacenterController dcb){
		dcb.addCloudSimEventListener(this);
		this.vmStatesList = dcb.getVmStatesList();
		this.currentAllocationCounts = Collections.synchronizedMap(new HashMap<Integer, Integer>());
		this.cloudletlist = dcb.getCloudletList();
		this.cloudletlistsjf = new LinkedList<>();
		cloudletlistsjf = cloudletlist;
	}

	/**
	 * @return The VM id of a VM so that the number of active tasks on each VM is kept
	 * 			evenly distributed among the VMs.
	 */
	@Override
	public int getNextAvailableVm(){
		int vmId = -1;
		
		//Find the vm with least number of allocations
		
		//If all available vms are not allocated, allocated the new ones
		/*if (currentAllocationCounts.size() < vmStatesList.size()){
			for (int availableVmId : vmStatesList.keySet()){
				if (!currentAllocationCounts.containsKey(availableVmId)){
					vmId = availableVmId;
					break;
				}				
			}
		} else {
			int currCount;
			int minCount = Integer.MAX_VALUE;
			
			for (int thisVmId : currentAllocationCounts.keySet()){
				currCount = currentAllocationCounts.get(thisVmId);
				if (currCount < minCount){
					minCount = currCount;
					vmId = thisVmId;
				}
			}
		}*/
		
		if (vmStatesList.size() > 0) {
			int temp = getVmfromCloudlet();
			VirtualMachineState state = vmStatesList.get(temp); //System.out.println(temp + " state is " + state + " total vms " + vmStatesList.size());
			if (state.equals(VirtualMachineState.AVAILABLE)){
				vmId = temp;
			}
			// TODO : Else condition when VMState is BUSY.
		}
		
		
		allocatedVm(vmId);
		
		return vmId;
		
	}
	
	private int getVmfromCloudlet() {
		// TODO : Code does not consider an empty cloudletlist
		//Step 1 : Calculate min job length.
		min = 0;
		for (int i=0;i<cloudletlistsjf.size();i++){
			if (cloudletlistsjf.get(i).getCloudletLength() < cloudletlistsjf.get(min).getCloudletLength())
				min = i;
		}
		//Step 2 : Identify Vm with the shortest job length
		int temp_vmID = cloudletlistsjf.get(min).getVmId();
		//Step 3 : Remove that cloudlet so that same VmID not selected again and again.
		cloudletlistsjf.remove(min);
		return temp_vmID;
	}

	public void cloudSimEventFired(CloudSimEvent e) {
		if (e.getId() == CloudSimEvents.EVENT_CLOUDLET_ALLOCATED_TO_VM){
			int vmId = (Integer) e.getParameter(Constants.PARAM_VM_ID);
			vmStatesList.put(vmId, VirtualMachineState.BUSY);
		} else if (e.getId() == CloudSimEvents.EVENT_VM_FINISHED_CLOUDLET){
			int vmId = (Integer) e.getParameter(Constants.PARAM_VM_ID);
			vmStatesList.put(vmId, VirtualMachineState.AVAILABLE);
		}
	}
	

	
}
