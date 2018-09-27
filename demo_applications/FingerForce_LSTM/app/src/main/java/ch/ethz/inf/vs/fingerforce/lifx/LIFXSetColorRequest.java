package ch.ethz.inf.vs.fingerforce.lifx;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by wilhelmk on 01/10/15.
 */
class LIFXSetColorRequest extends LIFXRequest {
    private int hue;
    private int saturation;
    private int brightness;
    private int kelvin;

    public LIFXSetColorRequest(byte [] address, int delay, int hue, int saturation, int brightness, int kelvin) {
        super(address, delay);
        this.hue = hue;
        this.saturation = saturation;
        this.brightness = brightness;
        this.kelvin = kelvin;
    }

    public int getHue() { return hue; }
    public int getSaturation() { return saturation; }
    public int getBrightness() { return brightness; }
    public int getKelvin() { return kelvin; }

    @Override
    protected byte[] generatePayload() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(13);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        // Add reserved field
        byteBuffer.put((byte) 0x00);
        // Add color in HSBK (hue)
        byteBuffer.putShort((short) ((float) hue / 360.0 * 65535.0));
        // Add saturation
        byteBuffer.putShort((short)((float) saturation/100*65535.0));
        // Add brightness
        byteBuffer.putShort((short)((float) brightness/100*65535.0));
        // Set Kelvin
        byteBuffer.putShort((short) kelvin);
        // Set delay
        byteBuffer.putInt(delay);
        return byteBuffer.array();
    }

    @Override
    protected int getRequestType() {
        return LightMessage.SetColor;
    }
}
