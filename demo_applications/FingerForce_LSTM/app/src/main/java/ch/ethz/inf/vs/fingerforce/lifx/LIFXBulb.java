package ch.ethz.inf.vs.fingerforce.lifx;

import android.graphics.Color;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class LIFXBulb {

    private static final String IP_ADDRESS = "192.168.3.255";
    //private static final String IP_ADDRESS = "192.168.1.28";
    private byte [] address;
    private String label;

    public LIFXBulb(String macAddress) {
        label = macAddress;
        // Convert to big-endian address
        String[] macAddressParts = macAddress.split(":");
        byte[] bigEndianAddress = new byte[8];
        Short byteComponent;
        for(int i=0; i<6; i++) {
            byteComponent = Short.parseShort(macAddressParts[i], 16);
            bigEndianAddress[i] = byteComponent.byteValue();
        }
        bigEndianAddress[6] = (byte) 0x00;
        bigEndianAddress[7] = (byte) 0x00;
        address = bigEndianAddress;

//        // Convert to little-endian address
//        ByteBuffer bigEndianAddressBuffer = ByteBuffer.wrap(bigEndianAddress);
//        ByteBuffer littleEndianAddressBuffer = ByteBuffer.allocate(bigEndianAddress.length);
//        littleEndianAddressBuffer.order( ByteOrder.LITTLE_ENDIAN);
//        int element;
//        while (bigEndianAddressBuffer.hasRemaining()) {
//            element = bigEndianAddressBuffer.getInt();
//            littleEndianAddressBuffer.putInt(element);
//        }
//        address = littleEndianAddressBuffer.array();
    }

    public String toString () { return label; }

    public String getLabel () { return label; }

    public void setColor(int color, int kelvin, int delay) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        setColor((int)hsv[0], (int)(hsv[1]*100), (int)(hsv[2]*100), kelvin, delay);
    }

    public void setColor(int hue, int saturation, int brightness, int kelvin, int delay) {
        try {
            byte [] messageData = new LIFXSetColorRequest(address, delay, hue, saturation, brightness, kelvin).generatePacket();
            StringBuilder sb = new StringBuilder();
            for (byte byteValue : messageData) {
                sb.append(String.format("%02x ", byteValue));
            }
            String message = sb.toString();
            InetAddress ipAddress = InetAddress.getByName(IP_ADDRESS);
            new MessageSendTask().execute(new Message(messageData, ipAddress, 56700));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setPower(boolean power, int delay) {
        try {
            byte [] messageData = new LIFXSetPowerRequest(address, delay, power).generatePacket();
            StringBuilder sb = new StringBuilder();
            for (byte byteValue : messageData) {
                sb.append(String.format("%02x ", byteValue));
            }
            String message = sb.toString();

            InetAddress ipAddress = InetAddress.getByName(IP_ADDRESS);
            new MessageSendTask().execute(new Message(messageData, ipAddress, 56700));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getPower() {
        try {
            byte [] messageData = new LIFXGetPowerRequest(address, 0).generatePacket();
            StringBuilder sb = new StringBuilder();
            for (byte byteValue : messageData) {
                sb.append(String.format("%02x ", byteValue));
            }
            String message = sb.toString();
            //InetAddress ipAddress = InetAddress.getByName("192.168.3.255");
            InetAddress ipAddress = InetAddress.getByName(IP_ADDRESS);
            //new MessageSendTask().execute(new Message(messageData, ipAddress, 56700));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
