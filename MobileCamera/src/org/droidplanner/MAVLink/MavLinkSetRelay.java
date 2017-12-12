package org.droidplanner.MAVLink;

import org.droidplanner.connection.MAVLinkConnection;

import com.MAVLink.Messages.ardupilotmega.msg_command_long;
import com.MAVLink.Messages.enums.MAV_CMD;
import com.MAVLink.Messages.enums.MAV_COMPONENT;

public class MavLinkSetRelay {

	public static void sendSetRelayMessage(MAVLinkConnection conn, float number, boolean isOn) {
		msg_command_long msg = new msg_command_long();
		msg.target_system = 1;
		msg.target_component = 1;//(byte) MAV_COMPONENT.MAV_COMP_ID_SYSTEM_CONTROL

		msg.command = MAV_CMD.MAV_CMD_DO_SET_RELAY;
		msg.param1 = number;
		msg.param2 = isOn?1:0;
		msg.param3 = 0;
		msg.param4 = 0;
		msg.param5 = 0;
		msg.param6 = 0;
		msg.param7 = 0;
		msg.confirmation = 0;
		conn.sendMavPacket(msg.pack());
	}

}