package ch.ethz.inf.vs.fingerforce.lifx;

import java.net.InetAddress;

// Message class
class Message {
	private byte [] messageData;
	private InetAddress ipAddress;
	private int port;
	
	public Message(byte [] messageData, InetAddress ipAddress, int port) {
		this.messageData = messageData;
		this.ipAddress = ipAddress;
		this.port = port;
	}
	
	public byte[] getMessageData() {
		return messageData;
	}
	public void setMessageData(byte[] messageData) {
		this.messageData = messageData;
	}
	public InetAddress getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(InetAddress ipAddress) {
		this.ipAddress = ipAddress;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
}