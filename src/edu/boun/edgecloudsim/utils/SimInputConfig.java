package edu.boun.edgecloudsim.utils;

import java.util.Scanner; 

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.boun.edgecloudsim.utils.SimLogger;
import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.core.SimSettings.APP_TYPES;
import edu.boun.edgecloudsim.utils.OrderedProperties;

public class SimInputConfig {
	
	private static SimInputConfig instance = null;
	private Document edgeDevicesDoc = null;
	
	//enumarations for the VM, appplication, and place.
	//if you want to add different types on your config file,
	//you may modify current types or add new types here. 
	public static enum VM_TYPES { EDGE_VM, CLOUD_VM }
	public static enum APP_TYPES { COOPERATIVE_SURVEILLANCE, AUGMENTED_REALITY, HEALTH_APP, HEAVY_COMP_APP, INFOTAINMENT_APP }
	public static enum PLACE_TYPES { ATTRACTIVENESS_L1, ATTRACTIVENESS_L2, ATTRACTIVENESS_L3 }
	
	//predifined IDs for cloud components.
	public static int CLOUD_DATACENTER_ID = 1000;
	public static int CLOUD_HOST_ID = CLOUD_DATACENTER_ID + 1;
	public static int CLOUD_VM_ID = CLOUD_DATACENTER_ID + 2;
	
	//predifined IDs for edge devices
	public static int EDGE_ORCHESTRATOR_ID = 2000;
	public static int GENERIC_EDGE_DEVICE_ID = EDGE_ORCHESTRATOR_ID + 1;

	//delimiter for output file.
	public static String DELIMITER = ";";
	
    private double SIMULATION_TIME; //minutes unit in properties file
    private double WARM_UP_PERIOD; //minutes unit in properties file
    private double INTERVAL_TO_GET_VM_LOAD_LOG; //minutes unit in properties file
    private double INTERVAL_TO_GET_VM_LOCATION_LOG; //minutes unit in properties file
    private boolean FILE_LOG_ENABLED; //boolean to check file logging option
    private boolean DEEP_FILE_LOG_ENABLED; //boolean to check deep file logging option

    private int MIN_NUM_OF_MOBILE_DEVICES;
    private int MAX_NUM_OF_MOBILE_DEVICES;
    private int MOBILE_DEVICE_COUNTER_SIZE;
    
    private int NUM_OF_EDGE_DATACENTERS;
    private int NUM_OF_EDGE_HOSTS;
    private int NUM_OF_EDGE_VMS;
    
    private double WAN_PROPOGATION_DELAY; //seconds unit in properties file
    private double LAN_INTERNAL_DELAY; //seconds unit in properties file
    private int BANDWITH_WLAN; //Mbps unit in properties file
    private int BANDWITH_WAN; //Mbps unit in properties file
    private int BANDWITH_GSM; //Mbps unit in properties file

    private int MIPS_FOR_CLOUD; //MIPS
    
    private String[] SIMULATION_SCENARIOS;
    private String[] ORCHESTRATOR_POLICIES;
    
    // mean waiting time (minute) is stored for each place types
    private double[] mobilityLookUpTable;
    
    // following values are stored for each applications defined in applications.xml
    // [0] usage percentage (%)
    // [1] prob. of selecting cloud (%)
    // [2] poisson mean (sec)
    // [3] active period (sec)
    // [4] idle period (sec)
    // [5] avg data upload (KB)
    // [6] avg data download (KB)
    // [7] avg task length (MI)
    // [8] required # of cores
    // [9] vm utilization (%)
    private double[][] taskLookUpTable = new double[APP_TYPES.values().length][11];

	private SimInputConfig() {
	}
	
	public static SimInputConfig getInstance() {
		if(instance == null) {
			instance = new SimInputConfig();
		}
		return instance;
	}
	
	
	/**
	 * Sets config.properties file and stores information to local variables
	 * @param configPropertiesFile
	 * @return
	 * @author Beiqing Chen
	 */
	public void setConfigurationPropertiesFile(String propertiesFile) {
		boolean result = false;
		InputStream input = null;
		Scanner sc = new Scanner(System.in); 
		OutputStream fos;
		
//		File file = new File("scripts/uav_application/config/config1"+".properties");
//		File file = createFile(propertiesFile);
//		File file = new File(propertiesFile);
//		if (!file.exists()) {
//			try {
//				file.createNewFile();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
		
		
		try {
			SimLogger.printLine("Start setting configuration property file......");
			
			input = new FileInputStream(propertiesFile);
			
//			fos = new FileOutputStream("scripts/uav_application/config/config1"+".properties");
			
			// load a properties file
			OrderedProperties prop = new OrderedProperties();
			prop.load(input);
			
			
			SimLogger.print("Simulation Time (sec): ");
			String simulation_time = sc.next();
			prop.setProperty("simulation_time", simulation_time);
//			SIMULATION_TIME = (double)60 * Double.parseDouble(prop.getProperty("simulation_time"));
//			System.out.println(Double.parseDouble(prop.getProperty("simulation_time")));
//			System.out.println(SIMULATION_TIME);
			
			SimLogger.print("Warm Up Period (sec): ");
			String warm_up_period = sc.next();
			prop.setProperty("warm_up_period", warm_up_period);
			
			SimLogger.print("Interval to Get VM Load Log (sec): ");
			String interval_to_get_vm_load_log = sc.next();
			prop.setProperty("vm_load_check_interval", interval_to_get_vm_load_log);		
			
			SimLogger.print("Interval to Get VM Location Log (sec): ");
			String interval_to_get_vm_location_log = sc.next();
			prop.setProperty("vm_location_check_interval", interval_to_get_vm_location_log);
			
			SimLogger.print("File Log Enabled <t/f>: ");
			String file_log_enabled = sc.next();
			if ("t".equals(file_log_enabled)) {
				prop.setProperty("file_log_enabled", "true");
			}
			else {
				prop.setProperty("file_log_enabled", "false");
			}
			
			SimLogger.print("Deep File Log Enabled <t/f>: ");
			String deep_file_log_enabled = sc.next();
			if ("t".equals(deep_file_log_enabled)) {
				prop.setProperty("deep_file_log_enabled", "true");
			}
			else {
				prop.setProperty("deep_file_log_enabled", "false");
			}
			
			
			SimLogger.print("Number of UAVs: ");
			String num_of_uav = sc.next();
			prop.setProperty("min_number_of_mobile_devices", num_of_uav);
			prop.setProperty("max_number_of_mobile_devices", num_of_uav);
			prop.setProperty("mobile_device_counter_size", "1");
			
			
			SimLogger.print("Wan Propogation Delay (sec): ");
			String wan_propogation_delay = sc.next();
			prop.setProperty("wan_propogation_delay", wan_propogation_delay);
			
			SimLogger.print("Lan Internal Delay (sec): ");
			String lan_internal_delay = sc.next();
			prop.setProperty("lan_internal_delay", lan_internal_delay);
			
			SimLogger.print("WLAN Bandwidth (Mbps): ");
			String wlan_bandwidth = sc.next();
			prop.setProperty("wlan_bandwidth", wlan_bandwidth);
			
			SimLogger.print("WAN Bandwidth (Mbps): ");
			String wan_bandwidth = sc.next();
			prop.setProperty("wan_bandwidth", wan_bandwidth);
			
			SimLogger.print("GSM Bandwidth (Mbps): ");
			String gsm_bandwidth = sc.next();
			prop.setProperty("gsm_bandwidth", gsm_bandwidth);
			
			
			SimLogger.print("MIPS for Cloud: ");
			String mips_for_cloud = sc.next();
			prop.setProperty("mips_for_cloud", mips_for_cloud);
			
			SimLogger.print("Orchestrator Polices (use ',' for multiple values): ");
			String orchestrator_policies = sc.next();
			prop.setProperty("orchestrator_policies", orchestrator_policies);
			
			SimLogger.print("Simulation Scenarios (use ',' for multiple values): ");
			String simulation_scenarios = sc.next();
			prop.setProperty("simulation_scenarios", simulation_scenarios);
			
			
			SimLogger.print("Attractiveness L1 Mean Waiting Time (min): ");
			String attractiveness_L1_mean_waiting_time = sc.next();
			prop.setProperty("attractiveness_L1_mean_waiting_time", attractiveness_L1_mean_waiting_time);
			
			SimLogger.print("Attractiveness L2 Mean Waiting Time (min): ");
			String attractiveness_L2_mean_waiting_time = sc.next();
			prop.setProperty("attractiveness_L2_mean_waiting_time", attractiveness_L2_mean_waiting_time);
			
			SimLogger.print("Attractiveness L3 Mean Waiting Time (min): ");
			String attractiveness_L3_mean_waiting_time = sc.next();
			prop.setProperty("attractiveness_L3_mean_waiting_time", attractiveness_L3_mean_waiting_time);
			
//			prop.save(fos, "The new config file");
			fos = new FileOutputStream(propertiesFile);
			prop.store(fos, "The new config file");			
			
		}catch (IOException ex) {
			ex.printStackTrace();
		}finally {
			if (input != null) {
				try {
					input.close();
					result = true;
					SimLogger.printLine("Creating properties successfully!");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
		
	
	/**
	 * Sets application xml file and stores information to variables
	 * @param applicationXMLFile
	 * @return
	 * @author Beiqing Chen
	 */
	
	public void setApplicationXMLFile(String applicationsFile) {
		Document doc = null;
		Scanner sc = new Scanner(System.in);
		
		try {
			SimLogger.printLine("Start setting applications xml file......");
			doc = createDoc();			
			Element applications = rootElement(doc,"applications");
			
			SimLogger.print("Number of Application(s): ");
			int num_of_app = sc.nextInt();
			
			setParametersForApp(doc, applications, num_of_app, sc);
			SimInputConfig.writeXml(doc, applicationsFile);
			
		}catch (Exception ex) {
			ex.printStackTrace();
		}finally {
			try {
				SimLogger.printLine("Creating applications successfully!");
			} catch (Exception e) {
					e.printStackTrace();
					System.exit(0);
			}
		}
	}
	
	
	
	/**
	 * Sets edge device xml file and stores information to variables
	 * @param applicationXMLFile
	 * @return
	 * @author Beiqing Chen
	 */
	
	public void setEdgeDeviceXMLFile(String edgeDevicesFile) {
		Document doc = null;
		Scanner sc = new Scanner(System.in);
		
		try {
			SimLogger.printLine("Start setting edge devices xml file......");
			doc = createDoc();			
			Element applications = rootElement(doc,"edge_devices");
			
			SimLogger.print("Number of Edge Server(s): ");
			int numOfEdgeServer = sc.nextInt();
			
			SimLogger.print("Number of Host(s): ");
			int numOfHost = sc.nextInt();
			
			SimLogger.print("Number of Virtual Node(s): ");
			int numOfVirtualNode = sc.nextInt();
			
			setParametersForEdge(doc, applications, numOfEdgeServer, numOfHost, numOfVirtualNode, sc);
			SimInputConfig.writeXml(doc, edgeDevicesFile);
			
		}catch (Exception ex) {
			ex.printStackTrace();
		}finally {
			try {
				SimLogger.printLine("Creating edge devices successfully!");
			} catch (Exception e) {
					e.printStackTrace();
					System.exit(0);
			}
		}
	}
	
	
//	/**
//	 * Sets application xml file and stores information to variables
//	 * @param applicationXMLFile
//	 * @return
//	 * @author Beiqing Chen
//	 */
	
//	public void setApplicationXMLFile(String applicationsFile) {
//		Document doc = null;
//		Scanner sc = new Scanner(System.in); 
//		OutputStream fos;
//		
//		try {
//			SimLogger.printLine("Start setting applications xml file......");
//			doc = createDoc();			
//			Element applications = rootElement(doc,"applications");
//			
//			SimLogger.print("Number of Application(s): ");
//			int num_of_app = sc.nextInt();
//			
//			setParameters(doc, applications, num_of_app, sc);
			
			
			
//			for (int i = 0; i < num_of_app; i++) {
//				SimLogger.print("Creating application " + (i+1));
//				// 创建子节点，并设置属性
//				Element application = doc.createElement("application");
//				SimLogger.print("Task Name: ");
//				String task_name = sc.next();
//				application.setAttribute("name", task_name);
//				
//				// 为application添加子节点
//				Element usage_percentage = doc.createElement("usage_percentage");
//				SimLogger.print("Task Usage Percentage (%): ");
//				String usagePercentage = sc.next();
//				usage_percentage.setTextContent(usagePercentage);
//				application.appendChild(usage_percentage);		
//				
//				
//				
//				Element prob_cloud_selection = doc.createElement("prob_cloud_selection");
//				SimLogger.print("Probability of Cloud Selection (%): ");
//				String probCloudSelection = sc.next();
//				prob_cloud_selection.setTextContent(probCloudSelection);
//				application.appendChild(prob_cloud_selection);
//				
//				Element poisson_interarrival = doc.createElement("poisson_interarrival");
//				SimLogger.print("Poisson Mean (sec): ");
//				String poissonIntearrival = sc.next();
//				poisson_interarrival.setTextContent(poissonIntearrival);
//				application.appendChild(poisson_interarrival);
//				
//				Element active_period = doc.createElement("active_period");
//				SimLogger.print("Active Period (sec): ");
//				String activePeriod = sc.next();
//				active_period.setTextContent(activePeriod);
//				application.appendChild(active_period);
//				
//				Element idle_period = doc.createElement("idle_period");
//				SimLogger.print("Idle Period (sec): ");
//				String idlePeriod = sc.next();
//				idle_period.setTextContent(idlePeriod);
//				application.appendChild(idle_period);
//				
//				Element data_upload = doc.createElement("data_upload");
//				SimLogger.print("Average Data Upload (KB): ");
//				String dataUpload = sc.next();
//				data_upload.setTextContent(dataUpload);
//				application.appendChild(data_upload);
//				
//				Element data_download = doc.createElement("data_download");
//				SimLogger.print("Average Data Download (KB): ");
//				String dataDownload = sc.next();
//				data_download.setTextContent(dataDownload);
//				application.appendChild(data_download);
//				
//				Element task_length = doc.createElement("task_length");
//				SimLogger.print("Average Task Length (MI): ");
//				String taskLength = sc.next();
//				task_length.setTextContent(taskLength);
//				application.appendChild(task_length);
//				
//				Element required_core = doc.createElement("required_core");
//				SimLogger.print("Required Number of Core: ");
//				String requiredCore = sc.next();
//				required_core.setTextContent(requiredCore);
//				application.appendChild(required_core);
//				
//				Element vm_utilization = doc.createElement("vm_utilization");
//				SimLogger.print("VM Utilization (0-100): ");
//				String vmUtilization = sc.next();
//				vm_utilization.setTextContent(vmUtilization);
//				application.appendChild(vm_utilization);
//				
//				Element delay_sensitivity = doc.createElement("delay_sensitivity");
//				SimLogger.print("Delay Sensitivity (0-1): ");
//				String delaySensitivity = sc.next();
//				delay_sensitivity.setTextContent(delaySensitivity);
//				application.appendChild(delay_sensitivity);
//				
//				// 为根节点添加子节点
//				applications.appendChild(application);
//				
//			}
			
			
			
			
			/*
			 * 生成XML文件
			 */
			
//			// 创建TransformerFactory对象
//            TransformerFactory tff = TransformerFactory.newInstance();
//            // 创建Transformer对象
//            Transformer tf = tff.newTransformer();
//            
//            // 设置输出数据时换行
//            tf.setOutputProperty("indent", "yes");
//            
//            // 使用Transformer的transform()方法将DOM树转换成XML
//            tf.transform(new DOMSource(doc), new StreamResult(applicationsFile));
//			fos = new FileOutputStream(applicationsFile);
//			SimInputConfig.writeXml(doc, applicationsFile);
//			
//			
//			
//			
//		}catch(Exception e) {
//			SimLogger.printLine("Applications XML cannot be parsed! Terminating simulation...");
//			e.printStackTrace();
//			System.exit(0);
//		}
//	}
	
	
	/**
	 * Reads configuration file and stores information to local variables
	 * @param propertiesFile
	 * @return
	 */
	public boolean initialize(String propertiesFile, String edgeDevicesFile, String applicationsFile){
		boolean result = false;
		InputStream input = null;
		try {
			input = new FileInputStream(propertiesFile);

			// load a properties file
			Properties prop = new Properties();
			prop.load(input);

			SIMULATION_TIME = (double)60 * Double.parseDouble(prop.getProperty("simulation_time")); //seconds
			WARM_UP_PERIOD = (double)60 * Double.parseDouble(prop.getProperty("warm_up_period")); //seconds
			INTERVAL_TO_GET_VM_LOAD_LOG = (double)60 * Double.parseDouble(prop.getProperty("vm_load_check_interval")); //seconds
			INTERVAL_TO_GET_VM_LOCATION_LOG = (double)60 * Double.parseDouble(prop.getProperty("vm_location_check_interval")); //seconds
			FILE_LOG_ENABLED = Boolean.parseBoolean(prop.getProperty("file_log_enabled"));
			DEEP_FILE_LOG_ENABLED = Boolean.parseBoolean(prop.getProperty("deep_file_log_enabled"));
			
			MIN_NUM_OF_MOBILE_DEVICES = Integer.parseInt(prop.getProperty("min_number_of_mobile_devices"));
			MAX_NUM_OF_MOBILE_DEVICES = Integer.parseInt(prop.getProperty("max_number_of_mobile_devices"));
			MOBILE_DEVICE_COUNTER_SIZE = Integer.parseInt(prop.getProperty("mobile_device_counter_size"));
			
			WAN_PROPOGATION_DELAY = Double.parseDouble(prop.getProperty("wan_propogation_delay"));
			LAN_INTERNAL_DELAY = Double.parseDouble(prop.getProperty("lan_internal_delay"));
			BANDWITH_WLAN = 1000 * Integer.parseInt(prop.getProperty("wlan_bandwidth"));
			BANDWITH_WAN = 1000 * Integer.parseInt(prop.getProperty("wan_bandwidth"));
			BANDWITH_GSM =  1000 * Integer.parseInt(prop.getProperty("gsm_bandwidth"));

			//It is assumed that
			//-Storage and RAM are unlimited in cloud
			//-Each task is executed with maximum capacity (as if there is no task in the cloud) 
			MIPS_FOR_CLOUD = Integer.parseInt(prop.getProperty("mips_for_cloud"));

			ORCHESTRATOR_POLICIES = prop.getProperty("orchestrator_policies").split(",");
			
			SIMULATION_SCENARIOS = prop.getProperty("simulation_scenarios").split(",");
			
			//avg waiting time in a place (min)
			double place1_mean_waiting_time = Double.parseDouble(prop.getProperty("attractiveness_L1_mean_waiting_time"));
			double place2_mean_waiting_time = Double.parseDouble(prop.getProperty("attractiveness_L2_mean_waiting_time"));
			double place3_mean_waiting_time = Double.parseDouble(prop.getProperty("attractiveness_L3_mean_waiting_time"));
			
			//mean waiting time (minute)
			mobilityLookUpTable = new double[]{
				place1_mean_waiting_time, //ATTRACTIVENESS_L1
				place2_mean_waiting_time, //ATTRACTIVENESS_L2
				place3_mean_waiting_time  //ATTRACTIVENESS_L3
		    };
			

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
					result = true;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		parseApplicatinosXML(applicationsFile);
		parseEdgeDevicesXML(edgeDevicesFile);
		
		return result;
	}
	
	
	
	/**
	 * 
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	private File createFile (String filename) throws IOException{
		File file = new File(filename);
		if (!file.exists()) {
			file.createNewFile();
		}
		return file;
	}
	
	
	/**
	 * 创建DOM树部分
	 * @return document对象
	 * @throws ParserConfigurationException
	 * 
	 */
	private static Document createDoc() throws ParserConfigurationException {
		// 创建DOM解析器的工厂实例
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		// 从DOM工厂中创建DOM解析器
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.newDocument();
		// 设置XML声明中standalone为yes，即没有dtd和schema作为该XML的说明文档，且不显示该属性
        doc.setXmlStandalone(true);
		return doc;
	}
	
	
	/**
	 * 向document添加根节点
	 * 
	 * @param document
	 * @param rootName
	 *            根节点的名称
	 * @return 根节点对应的element对象
	 */
 
	private static Element rootElement(Document document, String rootName) {
		Element element = document.createElement(rootName);
		document.appendChild(element);
		return element;
	}
	
	
	/**
	 * 
	 * @param document
	 * @param element
	 * @param attrName
	 * @param attrValue
	 */
	private static void addAttrtoElement(Document document, Element element, String attrName, String attrValue) {
		Element name = document.createElement(attrName);
		name.appendChild(document.createTextNode(attrValue));
		element.appendChild(name);
	}
	
	/**
	 * 向父级element添加子element
	 * 
	 * @param document
	 * @param parentElement
	 *            父级element对象
	 * @param childName
	 *            子级element对象的<b>名称</b>
	 * @return 子级element对象
	 */
	private static void parentAddChild(Document document, Element parentElement, Element childName) {
		parentElement.appendChild(childName);
	}
	
	
	
	
	private static void writeXml(Document doc, String dest)
			throws TransformerException {
		// 创建TransformerFactory对象
		TransformerFactory tff = TransformerFactory.newInstance();
		// 创建Transformer对象
		Transformer tf = tff.newTransformer();
		// 设置输出数据时换行
		tf.setOutputProperty(OutputKeys.INDENT, "yes");
//		tf.setOutputProperty(OutputKeys.ENCODING, encoding);
		
		
		// 使用Transformer的transform()方法将DOM树转换成XML
		tf.transform(new DOMSource(doc), new StreamResult(dest));
		
//		DOMSource source = new DOMSource();
//		source.setNode(node);
//		StreamResult result = new StreamResult();
//		result.setOutputStream(os);
// 
//		tf.transform(source, result);
		
	}
	
	
	
	/**
	 * Set parameters in application xml file
	 * @param numOfApp
	 */
	private void setParametersForApp(Document doc, Element pElement, int numOfApp, Scanner sc) {
		for (int i = 0; i < numOfApp; i++) {
			SimLogger.print("Creating application " + (i+1));
			SimLogger.printLine("");
			// 创建子节点，并设置属性
			Element application = doc.createElement("application");
			SimLogger.print("Task Name: ");
			String task_name = sc.next();
			application.setAttribute("name", task_name);
			
			// 为application添加子节点
			SimLogger.print("Task Usage Percentage (%): ");
			String usagePercentage = sc.next();
			addAttrtoElement(doc, application, "usage_percentage", usagePercentage);
			
			SimLogger.print("Probability of Cloud Selection (%): ");
			String probCloudSelection = sc.next();
			addAttrtoElement(doc, application, "prob_cloud_selection", probCloudSelection);
			
			SimLogger.print("Poisson Mean (sec): ");
			String poissonIntearrival = sc.next();
			addAttrtoElement(doc, application, "poisson_interarrival", poissonIntearrival);
			
			SimLogger.print("Active Period (sec): ");
			String activePeriod = sc.next();
			addAttrtoElement(doc, application, "active_period", activePeriod);
			
			SimLogger.print("Idle Period (sec): ");
			String idlePeriod = sc.next();
			addAttrtoElement(doc, application, "idle_period", idlePeriod);
			
			SimLogger.print("Average Data Upload (KB): ");
			String dataUpload = sc.next();
			addAttrtoElement(doc, application, "data_upload", dataUpload);

			SimLogger.print("Average Data Download (KB): ");
			String dataDownload = sc.next();
			addAttrtoElement(doc, application, "data_download", dataDownload);
			
			SimLogger.print("Average Task Length (MI): ");
			String taskLength = sc.next();
			addAttrtoElement(doc, application, "task_length", taskLength);
			
			SimLogger.print("Required Number of Core: ");
			String requiredCore = sc.next();
			addAttrtoElement(doc, application, "required_core", requiredCore);
			
			SimLogger.print("VM Utilization (0-100): ");
			String vmUtilization = sc.next();
			addAttrtoElement(doc, application, "vm_utilization", vmUtilization);
			
			SimLogger.print("Delay Sensitivity (0-1): ");
			String delaySensitivity = sc.next();
			addAttrtoElement(doc, application, "delay_sensitivity", delaySensitivity);

			
			// 为根节点添加子节点
			parentAddChild(doc, pElement, application);
			
		}
	}
	
	/**
	 * Set parameters in application xml file
	 * @param numOfApp
	 */
	private void setParametersForEdge(Document doc, Element pElement, int numOfEdgeServer, int numOfHost, 
			int numOfVirtualNode, Scanner sc) {
		for (int i = 0; i < numOfEdgeServer; i++) {
			SimLogger.print("Creating edge device " + (i+1));
			SimLogger.printLine("");
			
			// 创建datacenter子节点，并设置属性
			Element edgeDevices = doc.createElement("datacenter");
			SimLogger.print("Architecture: ");
			String arch = sc.next();
			edgeDevices.setAttribute("arch", arch);
			SimLogger.print("OS: ");
			String os = sc.next();
			edgeDevices.setAttribute("os", os);
			SimLogger.print("VMM: ");
			String vmm = sc.next();
			edgeDevices.setAttribute("vmm", vmm);
			
			// 为datacenter添加子节点
			SimLogger.print("Cost Per Bandwidth: ");
			String costPerBw = sc.next();
			addAttrtoElement(doc, edgeDevices, "costPerBw", costPerBw);
			
			SimLogger.print("Cost Per Second: ");
			String costPerSec = sc.next();
			addAttrtoElement(doc, edgeDevices, "costPerSec", costPerSec);
			
			SimLogger.print("Cost Per Memory: ");
			String costPerMem = sc.next();
			addAttrtoElement(doc, edgeDevices, "costPerMem", costPerMem);
			
			SimLogger.print("Cost Per Storage: ");
			String costPerStorage = sc.next();
			addAttrtoElement(doc, edgeDevices, "costPerStorage", costPerStorage);
			
			
			// 创建location子节点
			Element deviceLocation = doc.createElement("location");
			
			// 为location添加子节点
			SimLogger.print("x Position: ");
			String x_pos = sc.next();
			addAttrtoElement(doc, deviceLocation, "x_pos", x_pos);
			
			SimLogger.print("y Position: ");
			String y_pos = sc.next();
			addAttrtoElement(doc, deviceLocation, "y_pos", y_pos);

			SimLogger.print("Wlan Id: ");
			String wlan_id = sc.next();
			addAttrtoElement(doc, deviceLocation, "wlan_id", wlan_id);
			
			SimLogger.print("Attractiveness: ");
			String attractiveness = sc.next();
			addAttrtoElement(doc, deviceLocation, "attractiveness", attractiveness);
			
			parentAddChild(doc, edgeDevices, deviceLocation);
			
			// 创建hosts子节点
			Element hosts = doc.createElement("hosts");
			
			for (int j = 0; j < numOfHost; j++) {
				// 为hosts创建host子节点
				Element host = doc.createElement("host");
				//为host创建子节点
				SimLogger.print("Number of Core(s): ");
				String core = sc.next();
				addAttrtoElement(doc, host, "core", core);
				
				SimLogger.print("Number of MIPS: ");
				String mips = sc.next();
				addAttrtoElement(doc, host, "mips", mips);
				
				SimLogger.print("RAM: ");
				String ram = sc.next();
				addAttrtoElement(doc, host, "ram", ram);
				
				SimLogger.print("Storage: ");
				String storage = sc.next();
				addAttrtoElement(doc, host, "storage", storage);
				
				
				
				// 为host创建VMs子节点
				Element VMs = doc.createElement("VMs");
				// 为VMs创建子节点
				for (int k = 0; k < numOfVirtualNode; k++) {
					Element VM = doc.createElement("VM");
					SimLogger.print("Type of Virtual Node Management: ");
					String vmmForVirtualNode = sc.next();
					VM.setAttribute("vmm", vmmForVirtualNode);
					
					SimLogger.print("Number of Core(s): ");
					String vnCore = sc.next();
					addAttrtoElement(doc, VM, "core", vnCore);
					
					SimLogger.print("Number of MIPS: ");
					String vnMips = sc.next();
					addAttrtoElement(doc, VM, "mips", vnMips);
					
					SimLogger.print("RAM: ");
					String vnRam = sc.next();
					addAttrtoElement(doc, VM, "ram", vnRam);
					
					SimLogger.print("Storage: ");
					String vnStorage = sc.next();
					addAttrtoElement(doc, VM, "storage", vnStorage);
					
					parentAddChild(doc, VMs, VM);
					parentAddChild(doc, host, VMs);
				}
				parentAddChild(doc, hosts, host);
			}
			parentAddChild(doc, edgeDevices, hosts);
			// 为根节点添加子节点
			parentAddChild(doc, pElement, edgeDevices);
			
		}
	}
	
	
	/**
	 * returns the parsed XML document for edge_devices.xml
	 */
	public Document getEdgeDevicesDocument(){
		return edgeDevicesDoc;
	}


	/**
	 * returns simulation time (in seconds unit) from properties file
	 */
	public double getSimulationTime()
	{
		return SIMULATION_TIME;
	}

	/**
	 * returns warm up period (in seconds unit) from properties file
	 */
	public double getWarmUpPeriod()
	{
		return WARM_UP_PERIOD; 
	}

	/**
	 * returns VM utilization log collection interval (in seconds unit) from properties file
	 */
	public double getVmLoadLogInterval()
	{
		return INTERVAL_TO_GET_VM_LOAD_LOG; 
	}

	/**
	 * returns VM location log collection interval (in seconds unit) from properties file
	 */
	public double getVmLocationLogInterval()
	{
		return INTERVAL_TO_GET_VM_LOCATION_LOG; 
	}

	/**
	 * returns deep statistics logging status from properties file
	 */
	public boolean getDeepFileLoggingEnabled()
	{
		return DEEP_FILE_LOG_ENABLED; 
	}

	/**
	 * returns deep statistics logging status from properties file
	 */
	public boolean getFileLoggingEnabled()
	{
		return FILE_LOG_ENABLED; 
	}
	
	/**
	 * returns WAN propogation delay (in second unit) from properties file
	 */
	public double getWanPropogationDelay()
	{
		return WAN_PROPOGATION_DELAY;
	}

	/**
	 * returns internal LAN propogation delay (in second unit) from properties file
	 */
	public double getInternalLanDelay()
	{
		return LAN_INTERNAL_DELAY;
	}

	/**
	 * returns WLAN bandwidth (in Mbps unit) from properties file
	 */
	public int getWlanBandwidth()
	{
		return BANDWITH_WLAN;
	}

	/**
	 * returns WAN bandwidth (in Mbps unit) from properties file
	 */
	public int getWanBandwidth()
	{
		return BANDWITH_WAN; 
	}

	/**
	 * returns GSM bandwidth (in Mbps unit) from properties file
	 */
	public int getGsmBandwidth()
	{
		return BANDWITH_GSM;
	}
	
	/**
	 * returns the minimum number of the mobile devices used in the simulation
	 */
	public int getMinNumOfMobileDev()
	{
		return MIN_NUM_OF_MOBILE_DEVICES;
	}

	/**
	 * returns the maximunm number of the mobile devices used in the simulation
	 */
	public int getMaxNumOfMobileDev()
	{
		return MAX_NUM_OF_MOBILE_DEVICES;
	}

	/**
	 * returns the number of increase on mobile devices
	 * while iterating from min to max mobile device
	 */
	public int getMobileDevCounterSize()
	{
		return MOBILE_DEVICE_COUNTER_SIZE;
	}

	/**
	 * returns the number of edge datacenters
	 */
	public int getNumOfEdgeDatacenters()
	{
		return NUM_OF_EDGE_DATACENTERS;
	}

	/**
	 * returns the number of edge hosts running on the datacenters
	 */
	public int getNumOfEdgeHosts()
	{
		return NUM_OF_EDGE_HOSTS;
	}

	/**
	 * returns the number of edge VMs running on the hosts
	 */
	public int getNumOfEdgeVMs()
	{
		return NUM_OF_EDGE_VMS;
	}

	/**
	 * returns MIPS of the central cloud
	 */
	public int getMipsForCloud()
	{
		return MIPS_FOR_CLOUD;
	}

	/**
	 * returns simulation screnarios as string
	 */
	public String[] getSimulationScenarios()
	{
		return SIMULATION_SCENARIOS;
	}

	/**
	 * returns orchestrator policies as string
	 */
	public String[] getOrchestratorPolicies()
	{
		return ORCHESTRATOR_POLICIES;
	}
	
	/**
	 * returns mobility characteristic within an array
	 * the result includes mean waiting time (minute) or each place type
	 */ 
	public double[] getMobilityLookUpTable()
	{
		return mobilityLookUpTable;
	}

	/**
	 * returns application characteristic within two dimensional array
	 * the result includes the following values for each application type
	 * [0] usage percentage (%)
	 * [1] prob. of selecting cloud (%)
	 * [2] poisson mean (sec)
	 * [3] active period (sec)
	 * [4] idle period (sec)
	 * [5] avg data upload (KB)
	 * [6] avg data download (KB)
	 * [7] avg task length (MI)
	 * [8] required # of cores
	 * [9] vm utilization (%)
	 */ 
	public double[][] getTaskLookUpTable()
	{
		return taskLookUpTable;
	}
	
	private void isAttribtuePresent(Element element, String key) {
        String value = element.getAttribute(key);
        if (value.isEmpty() || value == null){
        	throw new IllegalArgumentException("Attribure '" + key + "' is not found in '" + element.getNodeName() +"'");
        }
	}

	private void isElementPresent(Element element, String key) {
		try {
			String value = element.getElementsByTagName(key).item(0).getTextContent();
	        if (value.isEmpty() || value == null){
	        	throw new IllegalArgumentException("Element '" + key + "' is not found in '" + element.getNodeName() +"'");
	        }
		} catch (Exception e) {
			throw new IllegalArgumentException("Element '" + key + "' is not found in '" + element.getNodeName() +"'");
		}
	}
	
	private void parseApplicatinosXML(String filePath)
	{
		Document doc = null;
		try {	
			File devicesFile = new File(filePath);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(devicesFile);
			doc.getDocumentElement().normalize();

			NodeList appList = doc.getElementsByTagName("application");
			for (int i = 0; i < appList.getLength(); i++) {
				Node appNode = appList.item(i);
	
				Element appElement = (Element) appNode;
				isAttribtuePresent(appElement, "name");
				isElementPresent(appElement, "usage_percentage");
				isElementPresent(appElement, "prob_cloud_selection");
				isElementPresent(appElement, "poisson_interarrival");
				isElementPresent(appElement, "active_period");
				isElementPresent(appElement, "idle_period");
				isElementPresent(appElement, "data_upload");
				isElementPresent(appElement, "data_download");
				isElementPresent(appElement, "task_length");
				isElementPresent(appElement, "required_core");
				isElementPresent(appElement, "vm_utilization");

				String appName = appElement.getAttribute("name");
				SimInputConfig.APP_TYPES appType = APP_TYPES.valueOf(appName);
				double usage_percentage = Double.parseDouble(appElement.getElementsByTagName("usage_percentage").item(0).getTextContent());
				double prob_cloud_selection = Double.parseDouble(appElement.getElementsByTagName("prob_cloud_selection").item(0).getTextContent());
				double poisson_interarrival = Double.parseDouble(appElement.getElementsByTagName("poisson_interarrival").item(0).getTextContent());
				double active_period = Double.parseDouble(appElement.getElementsByTagName("active_period").item(0).getTextContent());
				double idle_period = Double.parseDouble(appElement.getElementsByTagName("idle_period").item(0).getTextContent());
				double data_upload = Double.parseDouble(appElement.getElementsByTagName("data_upload").item(0).getTextContent());
				double data_download = Double.parseDouble(appElement.getElementsByTagName("data_download").item(0).getTextContent());
				double task_length = Double.parseDouble(appElement.getElementsByTagName("task_length").item(0).getTextContent());
				double required_core = Double.parseDouble(appElement.getElementsByTagName("required_core").item(0).getTextContent());
				double vm_utilization = Double.parseDouble(appElement.getElementsByTagName("vm_utilization").item(0).getTextContent());
				double delay_sensitivity = Double.parseDouble(appElement.getElementsByTagName("delay_sensitivity").item(0).getTextContent());
				
			    taskLookUpTable[appType.ordinal()][0] = usage_percentage; //usage percentage [0-100]
			    taskLookUpTable[appType.ordinal()][1] = prob_cloud_selection; //prob. of selecting cloud [0-100]
			    taskLookUpTable[appType.ordinal()][2] = poisson_interarrival; //poisson mean (sec)
			    taskLookUpTable[appType.ordinal()][3] = active_period; //active period (sec)
			    taskLookUpTable[appType.ordinal()][4] = idle_period; //idle period (sec)
			    taskLookUpTable[appType.ordinal()][5] = data_upload; //avg data upload (KB)
			    taskLookUpTable[appType.ordinal()][6] = data_download; //avg data download (KB)
			    taskLookUpTable[appType.ordinal()][7] = task_length; //avg task length (MI)
			    taskLookUpTable[appType.ordinal()][8] = required_core; //required # of core
			    taskLookUpTable[appType.ordinal()][9] = vm_utilization; //vm utilization [0-100]
			    taskLookUpTable[appType.ordinal()][10] = delay_sensitivity; //delay_sensitivity [0-1]
			}
	
		} catch (Exception e) {
			SimLogger.printLine("Edge Devices XML cannot be parsed! Terminating simulation...");
			e.printStackTrace();
			System.exit(0);
		}
	}

	private void parseEdgeDevicesXML(String filePath)
	{
		try {	
			File devicesFile = new File(filePath);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			edgeDevicesDoc = dBuilder.parse(devicesFile);
			edgeDevicesDoc.getDocumentElement().normalize();

			NodeList datacenterList = edgeDevicesDoc.getElementsByTagName("datacenter");
			for (int i = 0; i < datacenterList.getLength(); i++) {
			    NUM_OF_EDGE_DATACENTERS++;
				Node datacenterNode = datacenterList.item(i);
	
				Element datacenterElement = (Element) datacenterNode;
				isAttribtuePresent(datacenterElement, "arch");
				isAttribtuePresent(datacenterElement, "os");
				isAttribtuePresent(datacenterElement, "vmm");
				isElementPresent(datacenterElement, "costPerBw");
				isElementPresent(datacenterElement, "costPerSec");
				isElementPresent(datacenterElement, "costPerMem");
				isElementPresent(datacenterElement, "costPerStorage");

				Element location = (Element)datacenterElement.getElementsByTagName("location").item(0);
				isElementPresent(location, "attractiveness");
				isElementPresent(location, "wlan_id");
				isElementPresent(location, "x_pos");
				isElementPresent(location, "y_pos");

				NodeList hostList = datacenterElement.getElementsByTagName("host");
				for (int j = 0; j < hostList.getLength(); j++) {
				    NUM_OF_EDGE_HOSTS++;
					Node hostNode = hostList.item(j);
					
					Element hostElement = (Element) hostNode;
					isElementPresent(hostElement, "core");
					isElementPresent(hostElement, "mips");
					isElementPresent(hostElement, "ram");
					isElementPresent(hostElement, "storage");

					NodeList vmList = hostElement.getElementsByTagName("VM");
					for (int k = 0; k < vmList.getLength(); k++) {
					    NUM_OF_EDGE_VMS++;
						Node vmNode = vmList.item(k);
						
						Element vmElement = (Element) vmNode;
						isAttribtuePresent(vmElement, "vmm");
						isElementPresent(vmElement, "core");
						isElementPresent(vmElement, "mips");
						isElementPresent(vmElement, "ram");
						isElementPresent(vmElement, "storage");
					}
				}
			}
	
		} catch (Exception e) {
			SimLogger.printLine("Edge Devices XML cannot be parsed! Terminating simulation...");
			e.printStackTrace();
			System.exit(0);
		}
	}
}
