package ch.ethz.inf.vs.fingerforce.lifx;

public class LIFXClient {

    private static String[] macAddresses = {"D0:73:D5:00:30:EE"}; //"D0:73:D5:00:30:8D"

    public static LIFXBulb[] getLights() {
        LIFXBulb [] lights = new LIFXBulb[macAddresses.length];
        for (int i=0; i<macAddresses.length; i++) {
            lights[i] = new LIFXBulb(macAddresses[i]);
        }
        return lights;
    }

}
