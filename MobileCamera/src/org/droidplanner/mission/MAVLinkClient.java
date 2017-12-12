package org.droidplanner.mission;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;


// provide a common class for some ease of use functionality
public class MAVLinkClient {

	Context parent;
	private OnMavlinkClientListener listener;
	private Timer timeOutTimer;
	private int timeOutCount;
	private long timeOut;
	private int timeOutRetry;

	public interface OnMavlinkClientListener {
		boolean notifyTimeOut(int timeOutCount);
	}

	public MAVLinkClient(Context context, OnMavlinkClientListener listener) {
		parent = context;
		this.listener = listener;
	}

	public void setTimeOutValue(long timeout_ms) {
		this.timeOut = timeout_ms;
	}

	public long getTimeOutValue() {
		if (this.timeOut <= 0)
			return 3000; // default value

		return this.timeOut;
	}

	public void setTimeOutRetry(int timeout_retry) {
		this.timeOutRetry = timeout_retry;
	}

	public int getTimeOutRetry() {
		if (this.timeOutRetry <= 0)
			return 3; // default value

		return this.timeOutRetry;
	}

	public synchronized void resetTimeOut() {
		if (timeOutTimer != null) {
			timeOutTimer.cancel();
			timeOutTimer = null;
			/*
			 * Log.d("TIMEOUT", "reset " + String.valueOf(timeOutTimer));
			 */
		}
	}

	public void setTimeOut() {
		setTimeOut(this.timeOut, true);
	}

	public void setTimeOut(boolean resetTimeOutCount) {
		setTimeOut(this.timeOut, resetTimeOutCount);
	}

	public synchronized void setTimeOut(long timeout_ms,
			boolean resetTimeOutCount) {
		/*
		 * Log.d("TIMEOUT", "set " + String.valueOf(timeout_ms));
		 */
		resetTimeOut();
		if (resetTimeOutCount)
			timeOutCount = 0;

		if (timeOutTimer == null) {
			timeOutTimer = new Timer();
			timeOutTimer.schedule(new TimerTask() {
				public void run() {
					if (timeOutTimer != null) {
						resetTimeOut();
						timeOutCount++;

						/*
						 * Log.d("TIMEOUT", "timed out");
						 */

						listener.notifyTimeOut(timeOutCount);
					}
				}
			}, timeout_ms); // delay in milliseconds
		}
	}

}
