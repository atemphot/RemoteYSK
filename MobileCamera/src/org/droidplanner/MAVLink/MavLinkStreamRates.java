package org.droidplanner.MAVLink;

import org.droidplanner.connection.MAVLinkConnection;

import com.MAVLink.Messages.ardupilotmega.msg_request_data_stream;
import com.MAVLink.Messages.enums.MAV_DATA_STREAM;

public class MavLinkStreamRates {

	public static void setupStreamRates(MAVLinkConnection conn,
			int extendedStatus, int extra1, int extra2, int extra3,
			int position, int rcChannels, int rawSensors, int rawControler) {
		requestMavlinkDataStream(conn,
				MAV_DATA_STREAM.MAV_DATA_STREAM_EXTENDED_STATUS, extendedStatus);
		requestMavlinkDataStream(conn,
				MAV_DATA_STREAM.MAV_DATA_STREAM_EXTRA1, extra1);
		requestMavlinkDataStream(conn,
				MAV_DATA_STREAM.MAV_DATA_STREAM_EXTRA2, extra2);
		requestMavlinkDataStream(conn,
				MAV_DATA_STREAM.MAV_DATA_STREAM_EXTRA3, extra3);
		requestMavlinkDataStream(conn,
				MAV_DATA_STREAM.MAV_DATA_STREAM_POSITION, position);
		requestMavlinkDataStream(conn,
				MAV_DATA_STREAM.MAV_DATA_STREAM_RAW_SENSORS, rawSensors);
		requestMavlinkDataStream(conn,
				MAV_DATA_STREAM.MAV_DATA_STREAM_RAW_CONTROLLER, rawControler);
		requestMavlinkDataStream(conn,
				MAV_DATA_STREAM.MAV_DATA_STREAM_RC_CHANNELS, rcChannels);
	}

	private static void requestMavlinkDataStream(MAVLinkConnection conn,
			int stream_id, int rate) {
		msg_request_data_stream msg = new msg_request_data_stream();
		msg.target_system = 1;
		msg.target_component = 1;

		msg.req_message_rate = (short) rate;
		msg.req_stream_id = (byte) stream_id;

		if (rate > 0) {
			msg.start_stop = 1;
		} else {
			msg.start_stop = 0;
		}
		conn.sendMavPacket(msg.pack());
	}
}
