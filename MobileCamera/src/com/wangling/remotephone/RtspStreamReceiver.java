package com.wangling.remotephone;

public interface RtspStreamReceiver
{
	void recvRtpAudioData(byte[] data, int offset, int len, int ptype);

	void recvRtpVideoData(byte[] data, int offset, int len, int ptype);
}
