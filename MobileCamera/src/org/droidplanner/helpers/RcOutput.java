package org.droidplanner.helpers;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.droidplanner.MAVLink.MavLinkRC;
import org.droidplanner.connection.MAVLinkConnection;

import android.content.Context;


public class RcOutput {
	private static final int DISABLE_OVERRIDE = 0;
	private static final int RC_TRIM = 1500;
	private static final int RC_RANGE = 550;
	private Context parrentContext;
	private ScheduledExecutorService scheduleTaskExecutor;
	private MAVLinkConnection mavConn;
	public int[] rcOutputs = new int[8];

	public static final int AILERON = 0;
	public static final int ELEVATOR = 1;
	public static final int TROTTLE = 2;
	public static final int RUDDER = 3;

	public static final int RC5 = 4;
	public static final int RC6 = 5;
	public static final int RC7 = 6;
	public static final int RC8 = 7;

	public RcOutput(MAVLinkConnection conn, Context context) {
		this.mavConn = conn;
		parrentContext = context;
	}

	public void disableRcOverride() {
		if (isRcOverrided()) {
			scheduleTaskExecutor.shutdownNow();
			scheduleTaskExecutor = null;
		}
		Arrays.fill(rcOutputs, DISABLE_OVERRIDE);	// Start with all channels disabled, external callers can enable them as desired
		MavLinkRC.sendRcOverrideMsg(mavConn, rcOutputs); // Just to be sure send 3
														// disable
		MavLinkRC.sendRcOverrideMsg(mavConn, rcOutputs);
		MavLinkRC.sendRcOverrideMsg(mavConn, rcOutputs);
	}

	public void enableRcOverride() {
		if (!isRcOverrided()) {
			Arrays.fill(rcOutputs, DISABLE_OVERRIDE);
			MavLinkRC.sendRcOverrideMsg(mavConn, rcOutputs); // Just to be sure send 3
			MavLinkRC.sendRcOverrideMsg(mavConn, rcOutputs);
			MavLinkRC.sendRcOverrideMsg(mavConn, rcOutputs);
			Arrays.fill(rcOutputs, DISABLE_OVERRIDE);
			scheduleTaskExecutor = Executors.newScheduledThreadPool(5);
			scheduleTaskExecutor.scheduleWithFixedDelay(new Runnable() {
				@Override
				public void run() {
					MavLinkRC.sendRcOverrideMsg(mavConn, rcOutputs);
				}
			}, 0, getRcOverrideDelayMs(), TimeUnit.MILLISECONDS);
		}
	}

	private int getRcOverrideDelayMs() {
		return 200;
	}

	public boolean isRcOverrided() {
		return (scheduleTaskExecutor != null);
	}

	public void setRcChannel(int ch, double value) {
		if (value > +1)
			value = +1;
		if (value < -1)
			value = -1;
		rcOutputs[ch] = (int) (value * RC_RANGE + RC_TRIM);
	}

}
