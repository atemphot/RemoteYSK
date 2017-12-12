package com.MAVLink.Messages.ardupilotmega;

import com.MAVLink.Messages.MAVLinkMessage;
import com.MAVLink.Messages.MAVLinkPayload;
import com.MAVLink.Messages.MAVLinkPacket;
//import android.util.Log;


public class msg_gps_rtcm_data extends MAVLinkMessage{

	public static final int MAVLINK_MSG_ID_GPS_RTCM_DATA = 233;
	public static final int MAVLINK_MSG_LENGTH = 182;
	private static final long serialVersionUID = MAVLINK_MSG_ID_GPS_RTCM_DATA;
	

	public byte flags; 
	public byte len; 
	public byte[] data; 

	/**
	 * Generates the payload for a mavlink message for a message of this type
	 * @return
	 */
	public MAVLinkPacket pack(){
		MAVLinkPacket packet = new MAVLinkPacket();
		packet.len = MAVLINK_MSG_LENGTH;
		packet.sysid = 255;
		packet.compid = 190;
		packet.msgid = MAVLINK_MSG_ID_GPS_RTCM_DATA;
		packet.payload.putByte(flags);
		packet.payload.putByte(len);
		for (int i = 0; i < 180; i++) {
			packet.payload.putByte(data[i]);
		}
		return packet;		
	}
	
	@Override
	public void unpack(MAVLinkPayload payload) {
		// TODO Auto-generated method stub
        payload.resetIndex();
	    flags = payload.getByte();
	    len = payload.getByte();
	    for (int i = 0; i < 180; i++) {
	    	data[i] = payload.getByte(); 
	    }
	}

     /**
     * Constructor for a new message, just initializes the msgid
     */
    public msg_gps_rtcm_data(){
    	msgid = MAVLINK_MSG_ID_GPS_RTCM_DATA;
    	data = new byte[180];
    }
      
    /**
     * Returns a string with the MSG name and data
     */
    public String toString(){
    	return "MAVLINK_MSG_ID_GPS_RTCM_DATA -"+" flags:"+flags+" len:"+len+"";
    }
}
