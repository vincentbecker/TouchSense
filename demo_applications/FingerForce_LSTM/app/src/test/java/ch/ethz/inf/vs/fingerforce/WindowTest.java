package ch.ethz.inf.vs.fingerforce;

import org.junit.Before;
import org.junit.Test;
import ch.ethz.inf.vs.fingerforce.machinelearning.Windows;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class WindowTest {

    private Windows windows;
    private TestCallback callback;

    @Before
    public void before() {
        callback = new TestCallback();
        windows = new Windows(callback, 2, 4, 10);
    }

    @Test
    public void addData_test4() throws Exception {
        for (int i = 0; i < 4; i++) {
            byte num = (byte) i;
            float[] datapoint = {num, (byte) (num + 10), (byte) (num + 20), (byte) (num + 30)};
            windows.addData(datapoint);
        }
        assertEquals(0, callback.getCounter());
    }

    @Test
    public void addData_test5() throws Exception {
        for (int i = 0; i < 5; i++) {
            byte num = (byte) i;
            float[] datapoint = {num, (byte) (num + 10), (byte) (num + 20), (byte) (num + 30)};
            windows.addData(datapoint);
        }
        assertEquals(0, callback.getCounter());
    }

    @Test
    public void addData_test6() throws Exception {
        for (int i = 0; i < 6; i++) {
            byte num = (byte) i;
            float[] datapoint = {num, (byte) (num + 10), (byte) (num + 20), (byte) (num + 30)};
            windows.addData(datapoint);
        }
        assertEquals(0, callback.getCounter());
    }

    @Test
    public void addData_test9() throws Exception {
        for (int i = 0; i < 9; i++) {
            byte num = (byte) i;
            float[] datapoint = {num, (byte) (num + 10), (byte) (num + 20), (byte) (num + 30)};
            windows.addData(datapoint);
        }
        assertEquals(0, callback.getCounter());
    }

    @Test
    public void addData_test14() throws Exception {
        for (int i = 0; i < 14; i++) {
            byte num = (byte) i;
            float[] datapoint = {num, (byte) (num + 10), (byte) (num + 20), (byte) (num + 30)};
            windows.addData(datapoint);
        }
        assertEquals(1, callback.getCounter());
    }

    // Initialization: first 10 data points, then first window full.
    // Fill second window with the next 5 points (overlap is 5).
    @Test
    public void addData_test15() throws Exception {
        for (int i = 0; i < 15; i++) {
            byte num = (byte) i;
            float[] datapoint = {num, (byte) (num + 10), (byte) (num + 20), (byte) (num + 30)};
            windows.addData(datapoint);
        }
        for (int j = 0; j < callback.getData().length; ++j)
            for (int i = 0; i < callback.getData()[0].length; ++i)
                System.out.println(callback.getData()[j][i]);
        assertEquals(2, callback.getCounter());
    }

    // Initialization, then fill second and third windows with the following 10 points.
    @Test
    public void addData_test20() throws Exception {
        for (int i = 0; i < 20; i++) {
            byte num = (byte) i;
            float[] datapoint = {num, (byte) (num + 10), (byte) (num + 20), (byte) (num + 30)};
            windows.addData(datapoint);
        }
        assertEquals(3, callback.getCounter());
    }

    @Test
    public void addData_test25() throws Exception {
        for (int i = 0; i < 25; i++) {
            byte num = (byte) i;
            float[] datapoint = {num, (byte) (num + 10), (byte) (num + 20), (byte) (num + 30)};
            windows.addData(datapoint);
        }
        assertEquals(4, callback.getCounter());
    }

    @Test
    public void addData_test30() throws Exception {
        for (int i = 0; i < 30; i++) {
            byte num = (byte) i;
            float[] datapoint = {num, (byte) (num + 10), (byte) (num + 20), (byte) (num + 30)};
            windows.addData(datapoint);
        }
        assertEquals(5, callback.getCounter());
    }

    @Test
    public void addData_test35() throws Exception {
        for (int i = 0; i < 35; i++) {
            byte num = (byte) i;
            float[] datapoint = {num, (byte) (num + 10), (byte) (num + 20), (byte) (num + 30)};
            windows.addData(datapoint);
        }
        assertEquals(6, callback.getCounter());
    }
}