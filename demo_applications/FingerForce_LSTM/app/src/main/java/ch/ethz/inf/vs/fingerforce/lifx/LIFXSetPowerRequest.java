package ch.ethz.inf.vs.fingerforce.lifx;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by wilhelmk on 01/10/15.
 */
class LIFXSetPowerRequest extends LIFXRequest {
    private boolean powerLevel;

    public LIFXSetPowerRequest (byte [] address, int delay, boolean powerLevel) {
        super(address, delay);
        this.powerLevel = powerLevel;
    }

    protected byte[] generatePayload() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(6);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        if (powerLevel) {
            byteBuffer.putShort((short) 65535);
        } else {
            byteBuffer.putShort((short) 0);
        }
        // Set delay
        byteBuffer.putInt(delay);
        return byteBuffer.array();
    }

    @Override
    protected int getRequestType() {
        return LightMessage.SetPower;
    }

}
