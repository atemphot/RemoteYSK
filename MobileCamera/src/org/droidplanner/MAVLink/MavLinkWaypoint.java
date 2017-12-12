package org.droidplanner.MAVLink;

import org.droidplanner.connection.MAVLinkConnection;

import com.MAVLink.Messages.ardupilotmega.msg_mission_ack;
import com.MAVLink.Messages.ardupilotmega.msg_mission_count;
import com.MAVLink.Messages.ardupilotmega.msg_mission_request;
import com.MAVLink.Messages.ardupilotmega.msg_mission_request_list;
import com.MAVLink.Messages.ardupilotmega.msg_mission_set_current;
import com.MAVLink.Messages.enums.MAV_MISSION_RESULT;

public class MavLinkWaypoint {

	public static void sendAck(MAVLinkConnection conn) {
		msg_mission_ack msg = new msg_mission_ack();
		msg.target_system = 1;
		msg.target_component = 1;
		msg.type = MAV_MISSION_RESULT.MAV_MISSION_ACCEPTED;
		conn.sendMavPacket(msg.pack());

	}

	public static void requestWayPoint(MAVLinkConnection conn, int index) {
		msg_mission_request msg = new msg_mission_request();
		msg.target_system = 1;
		msg.target_component = 1;
		msg.seq = (short) index;
		conn.sendMavPacket(msg.pack());
	}

	public static void requestWaypointsList(MAVLinkConnection conn) {
		msg_mission_request_list msg = new msg_mission_request_list();
		msg.target_system = 1;
		msg.target_component = 1;
		conn.sendMavPacket(msg.pack());
	}

	public static void sendWaypointCount(MAVLinkConnection conn, int count) {
		msg_mission_count msg = new msg_mission_count();
		msg.target_system = 1;
		msg.target_component = 1;
		msg.count = (short) count;
		conn.sendMavPacket(msg.pack());
	}

	public static void sendSetCurrentWaypoint(MAVLinkConnection conn, short i) {
		msg_mission_set_current msg = new msg_mission_set_current();
		msg.target_system = 1;
		msg.target_component = 1;
		msg.seq = i;
		conn.sendMavPacket(msg.pack());
	}

}
