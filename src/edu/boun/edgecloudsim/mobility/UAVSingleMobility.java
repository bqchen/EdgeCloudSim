package edu.boun.edgecloudsim.mobility;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.utils.Location;
import edu.boun.edgecloudsim.utils.SimInputConfig;
import edu.boun.edgecloudsim.utils.SimLogger;
import edu.boun.edgecloudsim.utils.SimUtils;

public class UAVSingleMobility extends MobilityModel {
private List<TreeMap<Double, Location>> treeMapArray;
	
	public UAVSingleMobility(int _numberOfMobileDevices, double _simulationTime) {
		super(_numberOfMobileDevices, _simulationTime);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void initialize() {
		treeMapArray = new ArrayList<TreeMap<Double, Location>>();
		
		ExponentialDistribution[] expRngList = new ExponentialDistribution[SimInputConfig.getInstance().getNumOfEdgeDatacenters()];

		//create random number generator for each place
		Document doc = SimInputConfig.getInstance().getEdgeDevicesDocument();
		NodeList datacenterList = doc.getElementsByTagName("datacenter");
		for (int i = 0; i < datacenterList.getLength(); i++) {
			Node datacenterNode = datacenterList.item(i);
			Element datacenterElement = (Element) datacenterNode;
			Element location = (Element)datacenterElement.getElementsByTagName("location").item(0);
			String attractiveness = location.getElementsByTagName("attractiveness").item(0).getTextContent();
			SimInputConfig.PLACE_TYPES placeType = SimUtils.stringToPlace(attractiveness);
			
			expRngList[i] = new ExponentialDistribution(SimInputConfig.getInstance().getMobilityLookUpTable()[placeType.ordinal()]);
//			SimLogger.printLine("createSuccess");
		}
		
		//initialize tree maps and position of mobile devices
		for(int i=0; i<numberOfMobileDevices; i++) {
			treeMapArray.add(i, new TreeMap<Double, Location>());
			
			int randDatacenterId = SimUtils.getRandomNumber(0, SimInputConfig.getInstance().getNumOfEdgeDatacenters()-1);
			Node datacenterNode = datacenterList.item(randDatacenterId);
			Element datacenterElement = (Element) datacenterNode;
			Element location = (Element)datacenterElement.getElementsByTagName("location").item(0);
			String attractiveness = location.getElementsByTagName("attractiveness").item(0).getTextContent();
			SimInputConfig.PLACE_TYPES placeType = SimUtils.stringToPlace(attractiveness);
			int wlan_id = Integer.parseInt(location.getElementsByTagName("wlan_id").item(0).getTextContent());
			int x_pos = Integer.parseInt(location.getElementsByTagName("x_pos").item(0).getTextContent());
			int y_pos = Integer.parseInt(location.getElementsByTagName("y_pos").item(0).getTextContent());
			//start locating user from 10th seconds
			treeMapArray.get(i).put((double)10, new Location(placeType, wlan_id, x_pos, y_pos));
//			SimLogger.printLine("createSuccess");
		}
		
		for(int i=0; i<numberOfMobileDevices; i++) {
			TreeMap<Double, Location> treeMap = treeMapArray.get(i);

			while(treeMap.lastKey() < SimInputConfig.getInstance().getSimulationTime()) {				
				boolean placeFound = false;
				int currentLocationId = treeMap.lastEntry().getValue().getServingWlanId();
//				SimLogger.printLine("currentLocation:" + currentLocationId);
//				SimLogger.printLine("instance:"+(SimSettings.getInstance().getNumOfEdgeDatacenters()));
				double waitingTime = expRngList[currentLocationId].sample();
				
				while(placeFound == false){
					int newDatacenterId = SimUtils.getRandomNumber(0,SimInputConfig.getInstance().getNumOfEdgeDatacenters()-1);
//					SimLogger.printLine("createSuccess: " + newDatacenterId);
//					if(newDatacenterId != currentLocationId){
						placeFound = true;
						Node datacenterNode = datacenterList.item(newDatacenterId);
						Element datacenterElement = (Element) datacenterNode;
						Element location = (Element)datacenterElement.getElementsByTagName("location").item(0);
						String attractiveness = location.getElementsByTagName("attractiveness").item(0).getTextContent();
						SimInputConfig.PLACE_TYPES placeType = SimUtils.stringToPlace(attractiveness);
						int wlan_id = Integer.parseInt(location.getElementsByTagName("wlan_id").item(0).getTextContent());
						int x_pos = Integer.parseInt(location.getElementsByTagName("x_pos").item(0).getTextContent());
						int y_pos = Integer.parseInt(location.getElementsByTagName("y_pos").item(0).getTextContent());
//						SimLogger.printLine("mobility1...");
						treeMap.put(treeMap.lastKey()+waitingTime, new Location(placeType, wlan_id, x_pos, y_pos));
//					}
				}
				if(!placeFound){
					SimLogger.printLine("impossible is occured! location cannot be assigned to the device!");
			    	System.exit(0);
				}
			}
		}

	}

	@Override
	public Location getLocation(int deviceId, double time) {
		TreeMap<Double, Location> treeMap = treeMapArray.get(deviceId);
		
		Entry<Double, Location> e = treeMap.floorEntry(time);
	    
	    if(e == null){
	    	SimLogger.printLine("impossible is occured! no location is found for the device!");
	    	System.exit(0);
	    }
		return e.getValue();
	}
}
