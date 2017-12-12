package com.wangling.remotephone;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.util.Base64;
import br.com.voicetechnology.rtspclient.MissingHeaderException;
import br.com.voicetechnology.rtspclient.RTSPClient;
import br.com.voicetechnology.rtspclient.concepts.Client;
import br.com.voicetechnology.rtspclient.concepts.ClientListener;
import br.com.voicetechnology.rtspclient.concepts.Header;
import br.com.voicetechnology.rtspclient.concepts.Request;
import br.com.voicetechnology.rtspclient.concepts.Response;
import br.com.voicetechnology.rtspclient.headers.TransportHeader;
import br.com.voicetechnology.rtspclient.transport.PlainTCP;


public class RtspWrap implements ClientListener
{
	public static final int RTP_HEADER_LENGTH = 12;
	public static final int RTP_PAYLOAD_TYPE_G711 = 0;
	public static final int RTP_PAYLOAD_TYPE_H264 = 96;
	
	private String request_uri;
	private String controlURI;
	private String controlURI2;
	
	private RTSPClient client;
	private RtspStreamReceiver streamReceiver;
	private String username;
	private String password;
	private int retryTimes;
	private boolean bQuitRecv;
	
	private byte[] sdp_sps = null;
	private byte[] sdp_pps = null;
	
	private byte[] localSSRC = new byte[4];
	
	private long remoteSSRC_v;
	private long sequenceCycles_v;
	private long lastSequence_v;
	private long lastTimestamp_v;	
	private long remoteSSRC_a;
	private long sequenceCycles_a;
	private long lastSequence_a;
	private long lastTimestamp_a;
	
	private final List<String> resourceList;
	
	private int port;
	
	private int server_port;
	
	public  RtspWrap(RtspStreamReceiver _receiver, String _username, String _password) throws Exception
	{
		streamReceiver = _receiver;
		username = _username;
		password = _password;
		client = new RTSPClient();
		client.setTransport(new PlainTCP());
		client.setClientListener(this);
		resourceList = Collections.synchronizedList(new LinkedList<String>());
		port = 9000;
	}
	
	private RTSPClient getRTSPClient()
	{
		return client;
	}
	
	public void do_options(String uri)
	{
		request_uri = uri;
		try {
			client.options(request_uri, new URI(request_uri));
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void do_describe(String uri)
	{
		request_uri = uri;
		bQuitRecv = false;
		
		sdp_sps = null;
		sdp_pps = null;
		
		for (int i = 0; i < 4; i++)
		{
			localSSRC[i] = (byte)(Math.random() * 255f);
		}
		sequenceCycles_a = 0;
		sequenceCycles_v = 0;
		remoteSSRC_a = 0;
		remoteSSRC_v = 0;
		
		try {
			client.describe(new URI(uri));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void do_teardown()
	{
		client.teardown();
		client.setAuthentication("", false, username, password, "", "");
		bQuitRecv = true;
		retryTimes = 0;
	}
	
	public void requestFailed(Client client, Request request, Throwable cause)
	{
		System.out.println("Request failed \n" + request);
	}

	public void response(Client client, Request request, Response response)
	{
		try
		{
			System.out.println("For the request: ----------------\n" + request);
			System.out.println("Got response: +++++++++++++++++++\n" + response);
			
			if (response.getStatusCode() == 401)
			{
				String resp = response.getHeader("WWW-Authenticate").getRawValue();
				if (resp != null)
				{
					if (resp.contains("Basic realm"))
					{
						String realm = null;
						String tmp = "realm=\"";
						resp = resp.substring(resp.indexOf(tmp));
						resp = resp.substring(tmp.length());
						realm = resp.substring(0, resp.indexOf("\""));
						
						getRTSPClient().setAuthentication(request_uri, false, username, password, realm, "");
					}
					else if (resp.contains("Digest realm"))
					{
						String realm = null;
						String nonce = null;
						String tmp = "realm=\"";
						resp = resp.substring(resp.indexOf(tmp));
						resp = resp.substring(tmp.length());
						realm = resp.substring(0, resp.indexOf("\""));
						tmp = "nonce=\"";
						resp = resp.substring(resp.indexOf(tmp));
						resp = resp.substring(tmp.length());
						nonce = resp.substring(0, resp.indexOf("\""));
						
						getRTSPClient().setAuthentication(request_uri, true, username, password, realm, nonce);
					}
					
					if (retryTimes < 10)
					{
						do_describe(request_uri);
						retryTimes += 1;
					}
				}
			}
			
			if(response.getStatusCode() == 200)
			{
				switch(request.getMethod())
				{
				case DESCRIBE:
					System.out.print("resourceList:");
					System.out.println(resourceList);
					controlURI = request.getURI();
					controlURI2 = controlURI;
					
					try {
						String value = response.getHeader("Content-Base").getRawValue();
						if (value.startsWith("rtsp://")) {
							controlURI = value;
							controlURI2 = controlURI;
						}
						else if (value.startsWith("/")) {
							controlURI = controlURI.substring(0, controlURI.lastIndexOf('/')) + value;
							controlURI2 = controlURI;
						}
						else {
							controlURI2 = value;
						}
					} catch (MissingHeaderException e)	{}
					
					
//					if(resourceList.get(0).equals("*"))
//					{
//						controlURI = request.getURI();
//						resourceList.remove(0);
//					}
					if(resourceList.size() > 0)
						client.setup(new URI(controlURI), controlURI2, nextPort(), resourceList
								.remove(0));
					else
						client.setup(new URI(controlURI), controlURI2, nextPort());
					break;

				case SETUP:
					
					String sport = response.getHeader("Transport").getRawValue();
					String temp = "server_port=";
					sport = sport.substring(sport.indexOf(temp));
					sport = sport.substring(temp.length(), sport.indexOf("-"));
					server_port = Integer.parseInt(sport);
					
					
					final int client_port = port - 2;
					final boolean isVideo = (resourceList.size() > 0);
					new Thread(new Runnable() {
						
						public void run() {
							// TODO Auto-generated method stub
							String temp2 = "rtsp://";
							String server_ip = controlURI.substring(temp2.length());
							if (server_ip.contains(":")) {
								server_ip = server_ip.substring(0, server_ip.indexOf(":"));
							}
							else {
								server_ip = server_ip.substring(0, server_ip.indexOf("/"));
							}
							do_recv_rtp_stream(server_ip, server_port, client_port, isVideo);
						}
					}).start();
					new Thread(new Runnable() {
						
						public void run() {
							// TODO Auto-generated method stub
							String temp2 = "rtsp://";
							String server_ip = controlURI.substring(temp2.length());
							if (server_ip.contains(":")) {
								server_ip = server_ip.substring(0, server_ip.indexOf(":"));
							}
							else {
								server_ip = server_ip.substring(0, server_ip.indexOf("/"));
							}
							do_send_rtcp_stream(server_ip, server_port+1, client_port+1, isVideo);
						}
					}).start();
					
					//sets up next session or ends everything.
					if(resourceList.size() > 0)
					{
						client.setup(new URI(controlURI), controlURI2, nextPort(), resourceList
								.remove(0));
					}
					else
					{
						client.play(controlURI2);
					}
					
					break;
					
				case PLAY:
					
				break;
				}
			}
		} catch(Throwable t)
		{
			generalError(client, t);
		}
	}

	public void generalError(Client client, Throwable error)
	{
		error.printStackTrace();
	}

	public void mediaDescriptor(Client client, String descriptor)
	{
		// searches for control: session and media arguments.
		final String target = "trackID=";
		System.out.println("Session Descriptor\n" + descriptor);
		int position = -1;
		
		String sprop = "sprop-parameter-sets=";
		position = descriptor.indexOf(sprop);
		if (position != -1)
		{
			String tmp = descriptor.substring(position + sprop.length());
			String sps = tmp.substring(0, tmp.indexOf(","));
			sdp_sps = Base64.decode(sps, Base64.DEFAULT);
			String pps = tmp.substring(tmp.indexOf(",") + 1, tmp.indexOf("\r"));
			sdp_pps = Base64.decode(pps, Base64.DEFAULT);
		}
		
		while((position = descriptor.indexOf(target)) > -1)
		{
			descriptor = descriptor.substring(position+target.length());
			resourceList.add(target+descriptor.substring(0, descriptor.indexOf('\r')));
		}
		if (resourceList.size() == 0)
		{// For Foscam...
			resourceList.add("track1");
			resourceList.add("track2");
		}
	}
	
	private int nextPort()
	{
		return (port += 2) - 2;
	}
	
	private void do_recv_rtp_stream(String serverip, int serverport, int clientport, boolean isVideo)
	{
		if (isVideo  &&  null != sdp_sps  &&  null != sdp_pps) {
			if ((sdp_sps[0] & 0x1f) == 7 && (sdp_pps[0] & 0x1f) == 8)
			{
				if (null != streamReceiver) {
					System.out.println("Recv sdp SPS/PPS...");
					streamReceiver.recvRtpVideoData(sdp_sps, 0, sdp_sps.length, RTP_PAYLOAD_TYPE_H264);
					streamReceiver.recvRtpVideoData(sdp_pps, 0, sdp_pps.length, RTP_PAYLOAD_TYPE_H264);
				}
				sdp_sps = null;
				sdp_pps = null;
			}
		}
		
		System.out.println("Recv RTP stream: server_port=" + serverport + ", client_port=" + clientport);
		
		byte[] recv_buf = new byte[4096];
		DatagramSocket udp_socket = null;
		try {
			udp_socket = new DatagramSocket(clientport);
			udp_socket.setSoTimeout(50);
			//udp_socket.connect(InetAddress.getByName(serverip), serverport); ////Important Bug!!!
			
			byte[] _buf = new byte[4];
			_buf[0] = (byte)0xce;
			_buf[1] = (byte)0xfa;
			_buf[2] = (byte)0xed;
			_buf[3] = (byte)0xfe;
			DatagramPacket _packet = new DatagramPacket(_buf, _buf.length);
			_packet.setPort(serverport);
			_packet.setAddress(InetAddress.getByName(serverip));
			_packet.setLength(4);
			Thread.sleep(100);
			udp_socket.send(_packet);
			Thread.sleep(100);
			udp_socket.send(_packet);
			
			while (false == bQuitRecv)
			{
				//We must new DatagramPacket every loop!!!
				DatagramPacket udp_packet = new DatagramPacket(recv_buf, recv_buf.length);
				try {
					udp_socket.receive(udp_packet);
				} catch (SocketTimeoutException e) {
					continue;
				}
				if (udp_packet.getLength() > 0)
				{
					System.out.println("---------------(" + clientport + ") RTP packet length: " + udp_packet.getLength());
					byte[] data = udp_packet.getData();
					int Version = (data[0] & 0xC0) >>> 6;
					int Padding = (data[0] & 0x20) >>> 5;
					int Extension = (data[0] & 0x10) >>> 4;
					int Ptype = (data[1] & 0x7f);
					long Sequence = (long)((data[2] & 0x00ff) << 8) +  (long)(data[3] & 0x00ff);
					long Timestamp = (long)((data[4] & 0x00ff) << 24) +  (long)((data[5] & 0x00ff) << 16) +  (long)((data[6] & 0x00ff) << 8) +  (long)(data[7] & 0x00ff);
					long SSRC = (long)((data[8] & 0x00ff) << 24) +  (long)((data[9] & 0x00ff) << 16) +  (long)((data[10] & 0x00ff) << 8) +  (long)(data[11] & 0x00ff);
					System.out.println("Version:" + Version + " Padding:" + Padding + " Ext:" + Extension + " Ptype:" + Ptype);
					System.out.println("Sequence:" + Sequence + " Timestamp:" + Timestamp + " SSRC:" + SSRC);
					if (/*0 == Padding && */ 0 == Extension)
					{
						if (false == isVideo) {
							if (Sequence + 100 < lastSequence_a) {
								sequenceCycles_a += 1;
							}
							lastSequence_a = Sequence;
							lastTimestamp_a = Timestamp;
							remoteSSRC_a = SSRC;
							if (null != streamReceiver) {
								streamReceiver.recvRtpAudioData(data, RTP_HEADER_LENGTH, udp_packet.getLength() - RTP_HEADER_LENGTH, Ptype);
							}
						}
						else {
							if (Sequence + 100 < lastSequence_v) {
								sequenceCycles_v += 1;
							}
							lastSequence_v = Sequence;
							lastTimestamp_v = Timestamp;
							remoteSSRC_v = SSRC;
							if (null != streamReceiver) {
								streamReceiver.recvRtpVideoData(data, RTP_HEADER_LENGTH, udp_packet.getLength() - RTP_HEADER_LENGTH, Ptype);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (null != udp_socket) udp_socket.close();
		System.out.println("Recv RTP stream (clientport=" + clientport + ") stopped!!!");
	}
	
	private void do_send_rtcp_stream(String serverip, int serverport, int clientport, boolean isVideo)
	{
		System.out.println("Send RTCP stream: server_port=" + serverport + ", client_port=" + clientport);
		byte[] send_buf = new byte[32 + 24];
		DatagramSocket udp_socket = null;
		try
		{
			udp_socket = new DatagramSocket(clientport);
			udp_socket.setSoTimeout(50);
			udp_socket.connect(InetAddress.getByName(serverip), serverport);
			
			byte[] _buf = new byte[4];
			_buf[0] = (byte)0xce;
			_buf[1] = (byte)0xfa;
			_buf[2] = (byte)0xed;
			_buf[3] = (byte)0xfe;
			DatagramPacket _packet = new DatagramPacket(_buf, _buf.length);
			_packet.setPort(serverport);
			_packet.setAddress(InetAddress.getByName(serverip));
			_packet.setLength(4);
			Thread.sleep(100);
			udp_socket.send(_packet);
			Thread.sleep(100);
			udp_socket.send(_packet);
			
			while (false == bQuitRecv)
			{
				try {
					Thread.sleep(3000);
				} catch (Exception e) {}
				
				//Wait SSRC come...
				if ((remoteSSRC_a == 0 && isVideo == false) || (remoteSSRC_v == 0 && isVideo == true)) {
					try {
						Thread.sleep(50);
					}catch (Exception e) {}
					
					continue;
				}
				
				//Build the RR...
				send_buf[0] = (byte)0x81;
				send_buf[1] = (byte)0xc9;//RR
				send_buf[2] = (byte)0x00;
				send_buf[3] = (byte)0x07;//32 bytes
				
				send_buf[4] = localSSRC[0];
				send_buf[5] = localSSRC[1];
				send_buf[6] = localSSRC[2];
				send_buf[7] = localSSRC[3];
				
				if (isVideo) {
					send_buf[8]  = (byte) ((remoteSSRC_v & 0xff000000) >>> 24);
					send_buf[9]  = (byte) ((remoteSSRC_v & 0x00ff0000) >>> 16);
					send_buf[10] = (byte) ((remoteSSRC_v & 0x0000ff00) >>> 8);
					send_buf[11] = (byte) ((remoteSSRC_v & 0x000000ff));
				} else {
					send_buf[8]  = (byte) ((remoteSSRC_a & 0xff000000) >>> 24);
					send_buf[9]  = (byte) ((remoteSSRC_a & 0x00ff0000) >>> 16);
					send_buf[10] = (byte) ((remoteSSRC_a & 0x0000ff00) >>> 8);
					send_buf[11] = (byte) ((remoteSSRC_a & 0x000000ff));
				}
				
				send_buf[12] = (byte)0x00;
				send_buf[13] = (byte)0xff;
				send_buf[14] = (byte)0xff;
				send_buf[15] = (byte)0xff;
				
				if (isVideo) {
					send_buf[16] = (byte) ((sequenceCycles_v & 0x0000ff00) >>> 8);
					send_buf[17] = (byte) ((sequenceCycles_v & 0x000000ff));
					send_buf[18] = (byte) ((lastSequence_v & 0x0000ff00) >>> 8);
					send_buf[19] = (byte) ((lastSequence_v & 0x000000ff));
				} else {
					send_buf[16] = (byte) ((sequenceCycles_a & 0x0000ff00) >>> 8);
					send_buf[17] = (byte) ((sequenceCycles_a & 0x000000ff));
					send_buf[18] = (byte) ((lastSequence_a & 0x0000ff00) >>> 8);
					send_buf[19] = (byte) ((lastSequence_a & 0x000000ff));
				}
				
				//jitter
				send_buf[20] = (byte)0x00;
				send_buf[21] = (byte)0x00;
				send_buf[22] = (byte)0x01;
				send_buf[23] = (byte)0x20;
				
				send_buf[24] = (byte)0x00;
				send_buf[25] = (byte)0x00;
				send_buf[26] = (byte)0x00;
				send_buf[27] = (byte)0x00;
				send_buf[28] = (byte)0x00;
				send_buf[29] = (byte)0x00;
				send_buf[30] = (byte)0x00;
				send_buf[31] = (byte)0x00;
				
				//Build the SDES...
				send_buf[32+ 0] = (byte)0x81;
				send_buf[32+ 1] = (byte)0xca;//SDES
				send_buf[32+ 2] = (byte)0x00;
				send_buf[32+ 3] = (byte)0x05;//24 bytes
				
				send_buf[32+ 4] = localSSRC[0];
				send_buf[32+ 5] = localSSRC[1];
				send_buf[32+ 6] = localSSRC[2];
				send_buf[32+ 7] = localSSRC[3];
				
				send_buf[32+ 8] = 0x01;//CNAME
				send_buf[32+ 9] = 0x0c;//12 chars
				String cname = "wangling-mac";
				System.arraycopy(cname.getBytes(), 0, send_buf, 32+10, 12);
				send_buf[32+ 22] = (byte)0x00;
				send_buf[32+ 23] = (byte)0x00;
				
				
				DatagramPacket udp_packet = new DatagramPacket(send_buf, send_buf.length);
				udp_packet.setPort(serverport);
				udp_packet.setAddress(InetAddress.getByName(serverip));
				udp_packet.setLength(32 + 24);
				try {
					udp_socket.send(udp_packet);
				} catch (Exception e) {
					continue;
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (null != udp_socket) udp_socket.close();
		System.out.println("Send RTCP stream (clientport=" + clientport + ") stopped!!!");
	}
}