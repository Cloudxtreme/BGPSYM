package nl.nlnetlabs.bgpsym01.primitives.timers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import nl.nlnetlabs.bgpsym01.mock.AbstractTest;
import nl.nlnetlabs.bgpsym01.mock.TimeControllerMock;
import nl.nlnetlabs.bgpsym01.primitives.timers.FlapTimer.FlapTimerType;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataInputStream;
import nl.nlnetlabs.bgpsym01.primitives.types.EDataOutputStream;

public class FlapTimerImplTest extends AbstractTest {

    private TimeControllerMock controller;
    private FlapTimerImpl timer1;

    @Override
    protected void tearDown() throws Exception {
        FlapTimerImpl.setTimeController(TimeControllerFactory.getTimeController());

    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        controller = new TimeControllerMock();
        FlapTimerImpl.setTimeController(controller);
        timer1 = getFlapTimer();
        FlapTimerImpl.setTimeController(controller);
    }

    public void testTimerType() {
        FlapTimerType cisco = FlapTimer.FlapTimerType.CISCO;
        FlapTimerImpl fi1 = new FlapTimerImpl(cisco);
        FlapTimerType juniper = FlapTimer.FlapTimerType.JUNIPER;
        FlapTimerImpl fi2 = new FlapTimerImpl(juniper);
        fi1.announce();
        fi2.announce();
        assertEquals(cisco.annPenalty, fi1.value, 0);
        assertEquals(juniper.annPenalty, fi2.value, 0);

        assertFalse(fi1.getTimerType().equals(fi2.getTimerType()));
    }

    public void testAdditive() {
        timer1.updateValue();

        double startValue = timer1.value;

        controller.currentTime = 0;
        // check that announces, reannounce and withdrawal work as they should..
        timer1.announce();
        assertEquals(startValue + timer1.getTimerType().annPenalty, timer1.value, 1.5);
        assertEquals(0, timer1.valueTime);

        timer1.announce();
        assertEquals(startValue + timer1.getTimerType().annPenalty + timer1.getTimerType().annPenalty, timer1.value, 1.5);

        controller.currentTime = 3000;
        timer1.withdraw();
        assertEquals(3, timer1.valueTime);
        assertEquals(startValue + timer1.getTimerType().annPenalty + timer1.getTimerType().annPenalty + timer1.getTimerType().withPenalty, timer1.value, 15);

    }

    public void testGoesFlapPoints() {
        assertFalse(timer1.isFlapped());
        timer1.value = timer1.getTimerType().threshold + 1;
        timer1.updateState();
        assertTrue(timer1.isFlapped());
        timer1.value = timer1.getTimerType().reuseThreshold + 1;
        assertTrue(timer1.isFlapped());
        timer1.value = timer1.getTimerType().reuseThreshold - 1;

        // no auto unflapping
        timer1.updateState();
        assertTrue(timer1.isFlapped());

        timer1.unflap(null);
        assertFalse(timer1.isFlapped());
    }

    public void testGoesUpTime() {
        assertEquals(0.0, timer1.value);

        timer1.value = timer1.getTimerType().threshold * 2;
        timer1.updateValue();
        timer1.updateState();
        System.out.println(timer1.value);
        assertTrue(timer1.isFlapped());

        long unflapTime1 = timer1.getUnflapTime();
        controller.currentTime = 1000;

        timer1.updateValue();
        assertTrue("value=" + timer1.value, timer1.value < timer1.getTimerType().threshold * 2);
        long unflapTime2 = timer1.getUnflapTime();
        controller.currentTime = 1900;
        timer1.updateValue();
        long unflapTime3 = timer1.getUnflapTime();
        controller.currentTime = unflapTime1 - 5;
        timer1.updateValue();
        long unflapTime4 = timer1.getUnflapTime();

        assertEquals(unflapTime1, unflapTime2, 2.0);
        assertEquals(unflapTime2, unflapTime3, 2.0);
        assertEquals(unflapTime3, unflapTime4, 2.0);

        controller.currentTime += 6;
        timer1.updateValue();
        timer1.updateState();
        // no auto unflapping
        assertTrue(timer1.isFlapped());

        timer1.unflap(null);
        assertFalse(timer1.isFlapped());
    }

    public void testStaysAsTimeFlows() {
        controller.currentTime = 1000000;
        FlapTimerImpl timer1 = getFlapTimer();
        timer1.announce();
        timer1.announce();
        timer1.announce();
        timer1.announce();
        timer1.announce();
        timer1.announce();
        timer1.reannounce();
        timer1.reannounce();
        timer1.reannounce();
        timer1.reannounce();
        timer1.reannounce();

        assertTrue(timer1.value + "", timer1.isFlapped());
        long v1 = timer1.getUnflapTime();
        // fail(v1 + " " + timer1.getValue());
        controller.currentTime = 2000000;
        timer1.updateValue();
        long v2 = timer1.getUnflapTime();
        assertEquals(v1, v2, 1000);

        controller.currentTime = 3000000;
        timer1.updateValue();
        long v3 = timer1.getUnflapTime();
        assertEquals(v2, v3, 1000);

        controller.currentTime = v3 - 1;
        timer1.updateValue();
        long v4 = timer1.getUnflapTime();
        assertEquals(v3, v4, 1000);

        controller.currentTime = v3 + 10;
        timer1.updateValue();
        long v5 = timer1.getUnflapTime();
        assertEquals(v4, v5, 1000);

    }

    private FlapTimerImpl getFlapTimer() {
        return new FlapTimerImpl(FlapTimerType.CISCO);
    }

    public void testMaxSuppress() {
        timer1.value = 20000;
        timer1.updateValue();
        timer1.updateState();
        assertEquals(timer1.getUnflapTime(), timer1.getTimerType().maxSuppress * 1000);

        controller.currentTime += timer1.getTimerType().maxSuppress * 1000 - 1;

        assertEquals(1, controller.getWaitingTime(timer1.getUnflapTime()));
    }

    public void testSerialize() throws IOException {
        timer1.value = 2000;
        timer1.updateValue();

        assertEquals(timer1, rewrite(timer1));

        timer1.value = 100;
        timer1.unflap(null);
        assertEquals(timer1, rewrite(timer1));
        assertNotNull(FlapTimerImpl.getTimeController());
    }

    public void testCompare() {
        FlapTimerImpl t1 = new FlapTimerImpl();
        t1.setTimerType(FlapTimerType.CISCO);
        FlapTimerImpl t2 = new FlapTimerImpl();
        t2.setTimerType(FlapTimerType.CISCO);
        FlapTimerImpl t3 = new FlapTimerImpl();
        t3.setTimerType(FlapTimerType.CISCO);

        TimeControllerMock tcm = new TimeControllerMock();
        FlapTimerImpl.setTimeController(tcm);
        FlapTimerImpl.setTimeController(tcm);
        FlapTimerImpl.setTimeController(tcm);

        t1.value = timer1.getTimerType().threshold + 100;
        t2.value = timer1.getTimerType().threshold + 26;
        t3.value = timer1.getTimerType().threshold;
        // t1.value = t1.threshold + 1;
        tcm.currentTime = 15;

        t1.updateValue();
        t1.updateState();
        assertTrue(t1.isFlapped());

        t2.updateValue();
        t2.updateState();

        assertEquals(1, t1.compareTo(t2));
        assertEquals(-1, t2.compareTo(t3));
        assertEquals(-1, t1.compareTo(t3));

    }

    public FlapTimerImpl rewrite(FlapTimer timer) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        EDataOutputStream eos = new EDataOutputStream(baos);

        timer.writeExternal(eos);
        eos.close();

        EDataInputStream eis = new EDataInputStream(new ByteArrayInputStream(baos.toByteArray()));
        FlapTimerImpl fti = new FlapTimerImpl();
        fti.readExternal(eis);
        return fti;
    }
}
