package com.wangling.remotephone;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.UUID;

import android.os.Handler;
import android.util.Base64;


public class OnvifProto {
	
	private static final String TAG = "OnvifProto";
	
	private static final int WS_DISCOVERY_PORT = 3702;
	private static final String ONVIF_DEVICE_NAMESPACE = "http://www.onvif.org/ver10/device/wsdl";
	private static final String ONVIF_EVENTS_NAMESPACE_SUB = "/events/wsdl";
	private static final String ONVIF_MEDIA_NAMESPACE_SUB = "/media/wsdl";
	private static final String ONVIF_PTZ_NAMESPACE_SUB = "/ptz/wsdl";
	
	
	private static final String str_probe_req = ""
		+"<?xml version=\"1.0\" encoding=\"utf-8\"?>"
		+"<Envelope xmlns:dn=\"http://www.onvif.org/ver10/network/wsdl\" xmlns=\"http://www.w3.org/2003/05/soap-envelope\">"
		+"<Header>"
		+"<wsa:MessageID xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">uuid:%s</wsa:MessageID>"
		+"<wsa:To xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">urn:schemas-xmlsoap-org:ws:2005:04:discovery</wsa:To>"
		+"<wsa:Action xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\">http://schemas.xmlsoap.org/ws/2005/04/discovery/Probe</wsa:Action>"
		+"</Header>"
		+"<Body>"
		+"<Probe xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns=\"http://schemas.xmlsoap.org/ws/2005/04/discovery\">"
		+"<Types>dn:NetworkVideoTransmitter</Types>"
		+"<Scopes />"
		+"</Probe>"
		+"</Body>"
		+"</Envelope>";


	public class OnvifNode
	{
		public String strIpAddress = null;
		public String strDeviceServiceUrl = null;
		
		public String strMediaServiceUrl = null;
		public String strPTZServiceUrl = null;
		public String strEventsServiceUrl = null;
		public String strMediaServiceNamespace = null;
		public String strPTZServiceNamespace = null;
		public String strEventsServiceNamespace = null;
		
		public String strProfileToken = null;
		public String strStreamUri = null;
	}
	
	public class OnvifNodeComparator implements Comparator
	{
		private long ip2long(String strIp) {
			String[] arr = strIp.split(".");
			if (arr.length != 4) {
				return 0;
			}
			long[] ip = new long[4];
			for (int i = 0; i < 4; i++)
			{
				ip[i] = Long.parseLong(arr[i]);
			}
			return (ip[0] << 24) + (ip[1] << 16) + (ip[2] << 8) + (ip[3]);
		}
		
		public int compare(Object o1, Object o2) {
			OnvifNode p1= (OnvifNode)o1;
			OnvifNode p2= (OnvifNode)o2; 
			if(ip2long(p1.strIpAddress) < ip2long(p2.strIpAddress))
				return 1;
			else
				return 0;
		}
	}
	
	
	private Handler mWorkerHandler = null;
	private RtspWrap rtspWrap = null;
	private ArrayList<OnvifNode> onvifNodeList = null;
	private int currIndex;
	
	private String username = "admin";
	private String password = "admin";
	private String time_str;
	private String nonce;
	private String digest;
	
	public OnvifProto(RtspStreamReceiver receiver) throws Exception
	{
		Worker worker = new Worker("OnvifProto Worker");
        mWorkerHandler = new Handler(worker.getLooper());
        
		rtspWrap = new RtspWrap(receiver, username, password);
		onvifNodeList = new ArrayList<OnvifNode>();
		currIndex = -1;
	}
	
	public int getNumOfOnvifNodes()
	{
		onvifNodeList.clear();
		wsDiscovery();
		Collections.sort(onvifNodeList, new OnvifNodeComparator());
		int count = onvifNodeList.size();
		return count;
	}
	
	public void switchToOnvifNode(int index)
	{
		rtspWrap.do_teardown();
		
		int count = onvifNodeList.size();
		if (count == 0) {
			return;
		}
		
		if (index >= count) {
			index = count - 1;
		}
		
		OnvifNode node = onvifNodeList.get(index);
		if (false == doGetServices(node)) {
			return;
		}
		if (false == doGetProfiles(node)) {
			return;
		}
		if (false == doGetStreamUri(node)) {
			return;
		}
		
		rtspWrap.do_describe(node.strStreamUri);
		currIndex = index;
	}
	
	public void leaveOnvifNode()
	{
		rtspWrap.do_teardown();
		currIndex = -1;
	}
	
	private byte[] SHA1(String s) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(s.getBytes());
            byte[] messageDigest = digest.digest();
            return messageDigest;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
	
	private void calPasswordDigest()
	{
		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sTimeFormat = new SimpleDateFormat("HH:mm:ss");
    	time_str = sDateFormat.format(new java.util.Date()) + "T" + sTimeFormat.format(new java.util.Date()) + "Z";
    	String nonce_orig = time_str.substring(6) + (int)(Math.random() * 10) + (int)(Math.random() * 10);
    	nonce = Base64.encodeToString(nonce_orig.getBytes()/* 16 bytes */, Base64.NO_WRAP);
    	digest = Base64.encodeToString(SHA1(nonce_orig + time_str + password), Base64.NO_WRAP);
	}
	
	private String[] strParseTag(String str, String tag)
	{
		String strTemp = str;
		String tag1 = "<" + tag + ">";
		String tag2 = "</" + tag + ">";
		int count = 0;
		
		while (true)
		{
			tag1 = "<" + tag + ">";
			int index = strTemp.indexOf(tag1);
			if (index == -1)
			{
				index = strTemp.indexOf("<" + tag);
				if (index == -1) {
					break;
				}
				tag1 = strTemp.substring(index);
				tag1 = tag1.substring(0, tag1.indexOf(">") + 1);
			}
			
			count += 1;
			
			strTemp = strTemp.substring(index + tag1.length());
		}
		
		if (count == 0) {
			return null;
		}
		
		String[] retArray = new String[count];
		for (int i = 0; i < count; i++)
		{
			tag1 = "<" + tag + ">";
			int index2 = str.indexOf(tag1);
			if (index2 == -1)
			{
				index2 = str.indexOf("<" + tag);
				tag1 = str.substring(index2);
				tag1 = tag1.substring(0, tag1.indexOf(">") + 1);
			}
			str = str.substring(index2);
			index2 = str.indexOf(tag2);
			if (index2 == -1) {
				continue;
			}
			String temp = str.substring(tag1.length(), index2);
			retArray[i] = temp.trim();
			str = str.substring(index2);
		}
		return retArray;
	}
	
	private String[] strParseTagEx(String str, String tag, String propName, String[] propValues)
	{
		String strTemp = str;
		String tag1 = "<" + tag + ">";
		String tag2 = "</" + tag + ">";
		int count = 0;
		
		while (true)
		{
			tag1 = "<" + tag + ">";
			int index = strTemp.indexOf(tag1);
			if (index == -1)
			{
				index = strTemp.indexOf("<" + tag);
				if (index == -1) {
					break;
				}
				tag1 = strTemp.substring(index);
				tag1 = tag1.substring(0, tag1.indexOf(">") + 1);
			}
			
			count += 1;
			
			strTemp = strTemp.substring(index + tag1.length());
		}
		
		if (count == 0) {
			System.out.println("strParseTagEx(" + tag + ") return null");
			return null;
		}
		
		String[] retArray = new String[count];
		for (int i = 0; i < count; i++)
		{
			tag1 = "<" + tag + ">";
			int index2 = str.indexOf(tag1);
			if (index2 == -1)
			{
				index2 = str.indexOf("<" + tag);
				tag1 = str.substring(index2);
				tag1 = tag1.substring(0, tag1.indexOf(">") + 1);
				//Get prop value...
				String p = propName + "=\"";
				String propValue = tag1.substring(tag1.indexOf(p) + p.length());
				propValues[i] = propValue.substring(0, propValue.indexOf("\""));
			}
			str = str.substring(index2);
			index2 = str.indexOf(tag2);
			if (index2 == -1) {
				continue;
			}
			String temp = str.substring(tag1.length(), index2);
			retArray[i] = temp.trim();
			str = str.substring(index2);
		}
		return retArray;
	}
	
	private boolean isNodeExists(String strIp)
	{
		int count = onvifNodeList.size();
		for (int i = 0; i < count; i++)
		{
			OnvifNode node = onvifNodeList.get(i);
			if (node.strIpAddress.equals(strIp))
			{
				return true;
			}
		}
		return false;
	}
	
	private void wsDiscovery()
	{
		DatagramSocket udp_socket = null;
		DatagramPacket udp_packet = null;
		byte[] recv_buf = new byte[4096];
		int loop = 10;
		try {
			udp_socket = new DatagramSocket(WS_DISCOVERY_PORT);
			udp_socket.setBroadcast(true);
			udp_socket.setSoTimeout(20);
			
			//Send...
			UUID uuid = UUID.randomUUID();
			String strSendData = String.format(str_probe_req, uuid.toString());
			byte[] sendData = strSendData.getBytes("UTF-8");
			
			udp_packet = new DatagramPacket(sendData, sendData.length);
			udp_packet.setPort(WS_DISCOVERY_PORT);
			InetAddress broadcastAddr = InetAddress.getByName("255.255.255.255");
			udp_packet.setAddress(broadcastAddr);
			
			udp_socket.send(udp_packet);
			Thread.sleep(20);
			udp_socket.send(udp_packet);
			
			while (loop >  0)
			{
				udp_packet = new DatagramPacket(recv_buf, recv_buf.length);
				try {
					udp_socket.receive(udp_packet);
				} catch (Exception e) {
					e.printStackTrace();
					
					//Send Probe again...
					udp_packet = new DatagramPacket(sendData, sendData.length);
					udp_packet.setPort(WS_DISCOVERY_PORT);
					udp_packet.setAddress(broadcastAddr);
					udp_socket.send(udp_packet);
					
					loop -= 1;
					continue;
				}
				if (udp_packet.getLength() > 0)
				{
					System.out.println("Packet length: " + udp_packet.getLength());
					InetAddress addr = udp_packet.getAddress();
					byte[] returned = new byte[udp_packet.getLength()];
					System.arraycopy(udp_packet.getData(), 0, returned, 0, udp_packet.getLength());
					String response = new String(returned, "UTF-8");
					
					if (false == response.contains("<wsa:RelatesTo>uuid:" + uuid.toString() + "</wsa:RelatesTo>")
							&& false == response.contains("<wsadis:RelatesTo>uuid:" + uuid.toString() + "</wsadis:RelatesTo>")  )
					{
						System.out.println("Packet not related!");
						continue;
					}
					
					String[] tempArr = null;
					tempArr = strParseTag(response, "d:XAddrs");
					if (null == tempArr) {
						tempArr = strParseTag(response, "wsdd:XAddrs");
						if (null == tempArr) {
							continue;
						}
					}
					response = tempArr[0];
					//支持IPv6的情况：http://192.168.8.169/onvif/device_service http://[fe80::4ebd:8fff:fedf:234f]/onvif/device_service
					if (response.contains(" ") && response.contains("::"))
					{
						String[] arrIPv4v6 = response.split("\\s+");//使用一个或多个空格分割字符串
						if (arrIPv4v6.length >= 1 && arrIPv4v6[0].contains("::") == false) {
							response = arrIPv4v6[0];
						}
						else if (arrIPv4v6.length >= 2 && arrIPv4v6[1].contains("::") == false) {
							response = arrIPv4v6[1];
						}
					}
					
					if (false == response.contains(addr.getHostAddress()))
					{
						String temp = "http://";
						response = response.substring(temp.length());
						if (-1 == response.indexOf(":"))
						{
							response = response.substring(response.indexOf("/"));
						}
						else {
							response = response.substring(response.indexOf(":"));
						}
						response = temp + addr.getHostAddress() + response;
					}
					if (false == isNodeExists(addr.getHostAddress()))
					{
						OnvifNode theOnvifNode = new OnvifNode();
						theOnvifNode.strIpAddress = addr.getHostAddress();
						theOnvifNode.strDeviceServiceUrl = response;
						onvifNodeList.add(theOnvifNode);
					}
					
					System.out.println("Packet wanted: " + response);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (udp_socket != null) udp_socket.close();
	}
	
	private String GetHttpPostReturn(String serverIp, String strUrl, StringBuilder param)
	{
		String responseString = "";
		try {
			byte[] bs = param.toString().getBytes("UTF-8");
			
			URL url = new URL(strUrl);
			
			if (false == serverIp.equals(url.getHost()))
			{
				int nPort = url.getPort();
				if (nPort == -1) {
					nPort = url.getDefaultPort();
				}
				if (nPort == -1) {
					nPort = 80;
				}
				String strUrl2 = url.getProtocol() + "://" + serverIp + ":" + nPort + url.getPath();
				url = new URL(strUrl2);
			}
			
			HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
			urlConn.setDoOutput(true);
			urlConn.setDoInput(true);
			urlConn.setRequestMethod("POST");
			
			urlConn.setUseCaches(false);
			urlConn.setInstanceFollowRedirects(true);
			
			urlConn.setRequestProperty("Host", serverIp);
			urlConn.setRequestProperty("Content-Type", "application/soap+xml; charset=utf-8");
			urlConn.setRequestProperty("Content-Length", "" + bs.length + "");
			
			String tmp = username + ":" + password;
			String authHeader = "Basic " + Base64.encodeToString(tmp.getBytes(), Base64.NO_WRAP);
			urlConn.setRequestProperty("Authorization", authHeader);
			
			urlConn.connect();
			
			
			DataOutputStream out = new DataOutputStream(urlConn.getOutputStream());
			for (int i = 0; i < bs.length; i++)
			{
				out.writeByte(bs[i]);
			}
			out.flush();
			
			InputStreamReader in = new InputStreamReader(urlConn.getInputStream());
			BufferedReader buffer = new BufferedReader(in);
			String inputLine = null;
			while (((inputLine = buffer.readLine()) != null))
            {
				responseString += inputLine + "\r\n";
            }
			in.close();
			out.close();
			urlConn.disconnect();
		}
		catch (Exception e) {  
			e.printStackTrace();  
		}
		
		return responseString;
	}
	
	private boolean doSystemReboot(OnvifNode onvifNode)
	{
		if (onvifNode.strDeviceServiceUrl == null) {
			return false;
		}
		
		StringBuilder postBody = new StringBuilder();
		postBody.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		postBody.append("<s:Envelope xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\">");
		
		calPasswordDigest();
		postBody.append("<s:Header>");
		postBody.append("<wsse:Security xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">");
		postBody.append("<wsse:UsernameToken>");
		postBody.append("<wsse:Username>" + username + "</wsse:Username>");
		postBody.append("<wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest\">" + digest + "</wsse:Password>");
		postBody.append("<wsse:Nonce>" + nonce + "</wsse:Nonce>");
		postBody.append("<wsu:Created>" + time_str + "</wsu:Created>");
		postBody.append("</wsse:UsernameToken>");
		postBody.append("</wsse:Security>");
		postBody.append("</s:Header>");
		
		postBody.append("<s:Body xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">");
		postBody.append("<SystemReboot xmlns=\"" + ONVIF_DEVICE_NAMESPACE + "\">");
		postBody.append("</SystemReboot>");
		postBody.append("</s:Body>");
		postBody.append("</s:Envelope>");
		
		String result = GetHttpPostReturn(onvifNode.strIpAddress, onvifNode.strDeviceServiceUrl, postBody);
		System.out.println("SOAP result chars: " + result.length());
		
		String[] temp = null;
		if ((temp = strParseTag(result, "tds:SystemRebootResponse")) != null)
		{
			return true;
		}
		return false;
	}
	
	private boolean doGetServices(OnvifNode onvifNode)
	{
		if (onvifNode.strDeviceServiceUrl == null) {
			return false;
		}
		
		StringBuilder postBody = new StringBuilder();
		postBody.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		postBody.append("<s:Envelope xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\">");
		
		calPasswordDigest();
		postBody.append("<s:Header>");
		postBody.append("<wsse:Security xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">");
		postBody.append("<wsse:UsernameToken>");
		postBody.append("<wsse:Username>" + username + "</wsse:Username>");
		postBody.append("<wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest\">" + digest + "</wsse:Password>");
		postBody.append("<wsse:Nonce>" + nonce + "</wsse:Nonce>");
		postBody.append("<wsu:Created>" + time_str + "</wsu:Created>");
		postBody.append("</wsse:UsernameToken>");
		postBody.append("</wsse:Security>");
		postBody.append("</s:Header>");
		
		postBody.append("<s:Body xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">");
		postBody.append("<GetServices xmlns=\"" + ONVIF_DEVICE_NAMESPACE + "\">");
		postBody.append("<IncludeCapability>true</IncludeCapability>");
		postBody.append("</GetServices>");
		postBody.append("</s:Body>");
		postBody.append("</s:Envelope>");
		
		
		String result = GetHttpPostReturn(onvifNode.strIpAddress, onvifNode.strDeviceServiceUrl, postBody);
		System.out.println("doGetServices, SOAP result chars: " + result.length());
		
		String[] temp = null;
		if ((temp = strParseTag(result, "tds:GetServicesResponse")) != null)
		{
			result = temp[0];
			String[] services = strParseTag(result, "tds:Service");
			if (services != null)
			{
				for (int i = 0; i < services.length; i++)
				{
					if (services[i].contains(ONVIF_EVENTS_NAMESPACE_SUB))
					{
						temp = strParseTag(services[i], "tds:Namespace");
						if (temp != null) {
							onvifNode.strEventsServiceNamespace = temp[0];
							System.out.println("" + temp[0]);
						}
						temp = strParseTag(services[i], "tds:XAddr");
						if (temp != null) {
							onvifNode.strEventsServiceUrl = temp[0];
							System.out.println("" + temp[0]);
						}
					}
					else if (services[i].contains(ONVIF_MEDIA_NAMESPACE_SUB) && services[i].contains("/ver10/"))
					{
						temp = strParseTag(services[i], "tds:Namespace");
						if (temp != null) {
							onvifNode.strMediaServiceNamespace = temp[0];
							System.out.println("" + temp[0]);
						}
						temp = strParseTag(services[i], "tds:XAddr");
						if (temp != null) {
							onvifNode.strMediaServiceUrl = temp[0];
							System.out.println("" + temp[0]);
						}
					}
					else if (services[i].contains(ONVIF_PTZ_NAMESPACE_SUB))
					{
						temp = strParseTag(services[i], "tds:Namespace");
						if (temp != null) {
							onvifNode.strPTZServiceNamespace = temp[0];
							System.out.println("" + temp[0]);
						}
						temp = strParseTag(services[i], "tds:XAddr");
						if (temp != null) {
							onvifNode.strPTZServiceUrl = temp[0];
							System.out.println("" + temp[0]);
						}
					}
				}
				
				//For”君永安“摄像头
				if (onvifNode.strMediaServiceUrl.endsWith("/onvif")) {
					onvifNode.strMediaServiceUrl = onvifNode.strDeviceServiceUrl;
				}
				if (onvifNode.strPTZServiceUrl.endsWith("/onvif")) {
					onvifNode.strPTZServiceUrl = onvifNode.strMediaServiceUrl;
				}
				return true;
			}
		}
		
		return false;
	}
	
	private boolean doGetProfiles(OnvifNode onvifNode)
	{
		if (onvifNode.strMediaServiceNamespace == null || 
				onvifNode.strMediaServiceUrl == null ) {
			return false;
		}
		
		StringBuilder postBody = new StringBuilder();
		postBody.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		postBody.append("<s:Envelope xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\">");
		
		calPasswordDigest();
		postBody.append("<s:Header>");
		postBody.append("<wsse:Security xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">");
		postBody.append("<wsse:UsernameToken>");
		postBody.append("<wsse:Username>" + username + "</wsse:Username>");
		postBody.append("<wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest\">" + digest + "</wsse:Password>");
		postBody.append("<wsse:Nonce>" + nonce + "</wsse:Nonce>");
		postBody.append("<wsu:Created>" + time_str + "</wsu:Created>");
		postBody.append("</wsse:UsernameToken>");
		postBody.append("</wsse:Security>");
		postBody.append("</s:Header>");
		
		postBody.append("<s:Body xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">");
		postBody.append("<GetProfiles xmlns=\"" + onvifNode.strMediaServiceNamespace + "\">");
		postBody.append("</GetProfiles>");
		postBody.append("</s:Body>");
		postBody.append("</s:Envelope>");
		
		String result = GetHttpPostReturn(onvifNode.strIpAddress, onvifNode.strMediaServiceUrl, postBody);
		System.out.println("doGetProfiles, SOAP result chars: " + result.length());
		System.out.println(result);////Debug
		
		String[] temp = null;
		String[] temp1 = null;
		String[] temp2 = null;
		if ((temp = strParseTag(result, "trt:GetProfilesResponse")) != null)
		{
			result = temp[0];
			String[] tokenNames = new String[16];
			String[] profiles = strParseTagEx(result, "trt:Profiles", "token", tokenNames);
			if (profiles != null)
			{
				System.out.println("doGetProfiles, profiles.length= " + profiles.length);////Debug
				for (int i = 0; i < profiles.length; i++)
				{
					temp = strParseTag(profiles[i], "tt:VideoEncoderConfiguration");
					if (temp != null)
					{
						String videoEncoderConfiguration = temp[0];
						
						temp = strParseTag(videoEncoderConfiguration, "tt:Encoding");
						if (temp != null && (temp[0].equalsIgnoreCase("H264") || temp[0].equalsIgnoreCase("H.264")))
						{
							System.out.println("Encoding: " + temp[0]);
							temp = strParseTag(videoEncoderConfiguration, "tt:Resolution");
							if (temp != null)
							{
								temp1 = strParseTag(temp[0], "tt:Width");
								temp2 = strParseTag(temp[0], "tt:Height");
								if (temp1 != null && temp2 != null)
								{
									int w = Integer.parseInt(temp1[0]);
									int h = Integer.parseInt(temp2[0]);
									System.out.println("Resolution: " + w + "x" + h);
									if (w <= 640 && h <= 480)
									{
										onvifNode.strProfileToken = tokenNames[i];
										System.out.println("Choose ProfileToken: " + onvifNode.strProfileToken);
										return true;
									}
								}
							}
						}
					}
				}//for
			}
		}
		
		return false;
	}
	
	private boolean doGetStreamUri(OnvifNode onvifNode)
	{
		if (onvifNode.strMediaServiceNamespace == null || 
				onvifNode.strMediaServiceUrl == null || 
				onvifNode.strProfileToken == null) {
			return false;
		}
		
		StringBuilder postBody = new StringBuilder();
		postBody.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		postBody.append("<s:Envelope xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\">");
		
		calPasswordDigest();
		postBody.append("<s:Header>");
		postBody.append("<wsse:Security xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">");
		postBody.append("<wsse:UsernameToken>");
		postBody.append("<wsse:Username>" + username + "</wsse:Username>");
		postBody.append("<wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest\">" + digest + "</wsse:Password>");
		postBody.append("<wsse:Nonce>" + nonce + "</wsse:Nonce>");
		postBody.append("<wsu:Created>" + time_str + "</wsu:Created>");
		postBody.append("</wsse:UsernameToken>");
		postBody.append("</wsse:Security>");
		postBody.append("</s:Header>");
		
		postBody.append("<s:Body xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">");
		postBody.append("<GetStreamUri xmlns=\"" + onvifNode.strMediaServiceNamespace + "\">");
		postBody.append("<StreamSetup>");
		postBody.append("<Stream xmlns=\"http://www.onvif.org/ver10/schema\">RTP-Unicast</Stream>");
		postBody.append("<Transport xmlns=\"http://www.onvif.org/ver10/schema\"><Protocol>UDP</Protocol></Transport>");
		postBody.append("</StreamSetup>");
		postBody.append("<ProfileToken>" + onvifNode.strProfileToken + "</ProfileToken>");
		postBody.append("</GetStreamUri>");
		postBody.append("</s:Body>");
		postBody.append("</s:Envelope>");
		
		String result = GetHttpPostReturn(onvifNode.strIpAddress, onvifNode.strMediaServiceUrl, postBody);
		System.out.println("doGetStreamUri, SOAP result chars: " + result.length());
		
		String[] temp = null;
		if ((temp = strParseTag(result, "trt:GetStreamUriResponse")) != null)
		{
			result = temp[0];
			temp = strParseTag(result, "trt:MediaUri");
			if (temp != null)
			{
				result = temp[0];
				temp = strParseTag(result, "tt:Uri");
				if (temp != null)
				{
					onvifNode.strStreamUri = temp[0];
					System.out.println("Get StreamUri -->   " + temp[0]);
					return true;
				}
			}
		}
		
		return false;
	}
	
	private boolean doPTZContinuousMove(OnvifNode onvifNode, float x, float y, float secTimeout)
	{
		if (onvifNode.strPTZServiceNamespace == null 
				|| onvifNode.strPTZServiceUrl == null
				|| onvifNode.strProfileToken == null) {
			return false;
		}
		
		StringBuilder postBody = new StringBuilder();
		postBody.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		postBody.append("<s:Envelope xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\">");
		
		calPasswordDigest();
		postBody.append("<s:Header>");
		postBody.append("<wsse:Security xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">");
		postBody.append("<wsse:UsernameToken>");
		postBody.append("<wsse:Username>" + username + "</wsse:Username>");
		postBody.append("<wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest\">" + digest + "</wsse:Password>");
		postBody.append("<wsse:Nonce>" + nonce + "</wsse:Nonce>");
		postBody.append("<wsu:Created>" + time_str + "</wsu:Created>");
		postBody.append("</wsse:UsernameToken>");
		postBody.append("</wsse:Security>");
		postBody.append("</s:Header>");
		
		postBody.append("<s:Body xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">");
		postBody.append("<ContinuousMove xmlns=\"" + onvifNode.strPTZServiceNamespace + "\">");
		postBody.append("<ProfileToken>" + onvifNode.strProfileToken + "</ProfileToken>");
		postBody.append("<Velocity>");
		postBody.append("<PanTilt x=\"" + ((x < 0.1 && x > -0.1) ? "0" : x) + "\" y=\"" + ((y < 0.1 && y > -0.1) ? "0" : y) + "\" space=\"http://www.onvif.org/ver10/tptz/PanTiltSpaces/VelocityGenericSpace\" xmlns=\"http://www.onvif.org/ver10/schema\"/>");
		postBody.append("</Velocity>");
		postBody.append("<Timeout>PT" + secTimeout + "S</Timeout>");
		postBody.append("</ContinuousMove>");
		postBody.append("</s:Body>");
		postBody.append("</s:Envelope>");
		
		String result = GetHttpPostReturn(onvifNode.strIpAddress, onvifNode.strPTZServiceUrl, postBody);
		System.out.println("SOAP result chars: " + result.length());
		
		String[] temp = null;
		if ((temp = strParseTag(result, "tptz:ContinuousMoveResponse")) != null)
		{
			System.out.println("PTZ ContinuousMove OK!");
			return true;
		}
		return false;
	}
	
	private boolean doPTZStop(OnvifNode onvifNode)
	{
		if (onvifNode.strPTZServiceNamespace == null 
				|| onvifNode.strPTZServiceUrl == null
				|| onvifNode.strProfileToken == null) {
			return false;
		}
		
		StringBuilder postBody = new StringBuilder();
		postBody.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		postBody.append("<s:Envelope xmlns:s=\"http://www.w3.org/2003/05/soap-envelope\">");
		
		calPasswordDigest();
		postBody.append("<s:Header>");
		postBody.append("<wsse:Security xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">");
		postBody.append("<wsse:UsernameToken>");
		postBody.append("<wsse:Username>" + username + "</wsse:Username>");
		postBody.append("<wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest\">" + digest + "</wsse:Password>");
		postBody.append("<wsse:Nonce>" + nonce + "</wsse:Nonce>");
		postBody.append("<wsu:Created>" + time_str + "</wsu:Created>");
		postBody.append("</wsse:UsernameToken>");
		postBody.append("</wsse:Security>");
		postBody.append("</s:Header>");
		
		postBody.append("<s:Body xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">");
		postBody.append("<Stop xmlns=\"" + onvifNode.strPTZServiceNamespace + "\">");
		postBody.append("<ProfileToken>" + onvifNode.strProfileToken + "</ProfileToken>");
		postBody.append("<PanTilt>true</PanTilt>");
		postBody.append("<Zoom>false</Zoom>");
		postBody.append("</Stop>");
		postBody.append("</s:Body>");
		postBody.append("</s:Envelope>");
		
		String result = GetHttpPostReturn(onvifNode.strIpAddress, onvifNode.strPTZServiceUrl, postBody);
		System.out.println("SOAP result chars: " + result.length());
		
		String[] temp = null;
		if ((temp = strParseTag(result, "tptz:StopResponse")) != null)
		{
			System.out.println("PTZ Stop OK!");
			return true;
		}
		return false;
	}
	
	
	final Runnable auto_ptz_stop_runnable = new Runnable() {
		public void run() {
			if (onvifNodeList.size() > 0 && currIndex >= 0) {
				doPTZStop(onvifNodeList.get(currIndex));
			}
		}
	};
	
	public void ptzTurnLeft()
	{
		if (currIndex >= 0)
		{
			doPTZContinuousMove(onvifNodeList.get(currIndex), 0.5f, 0f, 0.5f);
			mWorkerHandler.removeCallbacks(auto_ptz_stop_runnable);
			mWorkerHandler.postDelayed(auto_ptz_stop_runnable, 500);
		}
	}
	
	public void ptzTurnRight()
	{
		if (currIndex >= 0)
		{
			doPTZContinuousMove(onvifNodeList.get(currIndex), -0.5f, 0f, 0.5f);
			mWorkerHandler.removeCallbacks(auto_ptz_stop_runnable);
			mWorkerHandler.postDelayed(auto_ptz_stop_runnable, 500);
		}
	}
	
	public void ptzTurnUp()
	{
		if (currIndex >= 0)
		{
			doPTZContinuousMove(onvifNodeList.get(currIndex), 0f, -0.5f, 0.5f);
			mWorkerHandler.removeCallbacks(auto_ptz_stop_runnable);
			mWorkerHandler.postDelayed(auto_ptz_stop_runnable, 500);
		}
	}
	
	public void ptzTurnDown()
	{
		if (currIndex >= 0)
		{
			doPTZContinuousMove(onvifNodeList.get(currIndex), 0f, 0.5f, 0.5f);
			mWorkerHandler.removeCallbacks(auto_ptz_stop_runnable);
			mWorkerHandler.postDelayed(auto_ptz_stop_runnable, 500);
		}
	}
}
