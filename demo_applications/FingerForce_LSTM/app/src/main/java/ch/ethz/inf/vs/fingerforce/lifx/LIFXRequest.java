package ch.ethz.inf.vs.fingerforce.lifx;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by wilhelmk on 01/10/15.
 */
abstract class LIFXRequest {

    // Light Message Codes
    protected class LightMessage {
        final static int Get = 101;
        final static int SetColor = 102;
        final static int State = 107;
        final static int GetPower = 116;
        final static int SetPower = 117;
        final static int StatePower = 118;
    }

    private final byte[] address;
    protected final int delay;

    public LIFXRequest(byte [] address, int delay) {
        this.address = address;
        this.delay = delay;
    }

    private byte[] generateHeader() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(36);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        //---------------------------------------------------------//
        // Frame                                                   //
        //---------------------------------------------------------//
        // Write empty in first two bytes
        byteBuffer.put(new byte[] {(byte)0x00,(byte)0x00});
        // Set Origin, Tagged, Addressable and Protocol
        //byteBuffer.put(new byte[]{(byte) 0x00, (byte) 0x34}); // For all addressing
        byteBuffer.put(new byte[]{(byte) 0x00, (byte) 0x14}); // For individual addressing
        // Set source (all zeros in this case)
        byteBuffer.put(new byte[] {(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00});
        //---------------------------------------------------------//
        // Frame address                                           //
        //---------------------------------------------------------//
        // Set target address (all zeros addresses all lights)
        byteBuffer.put(address);
        // Reserved field with zero padding
        byteBuffer.put(new byte[] {(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00});
        // Set ack_required and res_required to zero
        byteBuffer.put((byte)0x00);
        // Set sequence number to zero (we are not processing and creating responses)
        byteBuffer.put((byte)0x00);
        //---------------------------------------------------------//
        // Add protocol header (...starting with 8 bytes of zeros) //
        //---------------------------------------------------------//
        byteBuffer.put(new byte[]{(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00});
        // Set message type
        byteBuffer.putInt(getRequestType());
        return byteBuffer.array();
    }

    abstract int getRequestType();

    abstract byte[] generatePayload();

    public byte[] generatePacket() {
        byte[] header = generateHeader();
        byte[] payload = generatePayload();
        byte[] packet = new byte[header.length+payload.length];
        System.arraycopy(header, 0, packet, 0, header.length);
        System.arraycopy(payload, 0, packet, header.length, payload.length);
        return ByteBuffer.wrap(packet).order(ByteOrder.LITTLE_ENDIAN).putShort(0, (short)packet.length).array();
    }
}
