package ch.ethz.inf.vs.fingerforce.lifx;

/**
 * Created by wilhelmk on 02/10/15.
 */
public class LIFXGetPowerRequest extends LIFXRequest {

    public LIFXGetPowerRequest(byte [] address, int delay) {
        super(address, delay);
    }

    @Override
    int getRequestType() {
        return LightMessage.GetPower;
    }

    @Override
    byte[] generatePayload() {
        return new byte[0];
    }
}
