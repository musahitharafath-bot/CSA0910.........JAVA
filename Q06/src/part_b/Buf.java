import java.util.*;

// Configurable bounded buffer used by the experiments: while/if, notify/notifyAll, and a size trace.
public class Buf {
    final int[] data; int count, in, out;
    final boolean useWhile, notifyAllFlag;
    final List<Integer> trace = Collections.synchronizedList(new ArrayList<>());
    volatile int minSeen = Integer.MAX_VALUE, maxSeen = Integer.MIN_VALUE;
    Buf(int cap, boolean useWhile, boolean notifyAllFlag) { data = new int[cap]; this.useWhile = useWhile; this.notifyAllFlag = notifyAllFlag; }

    private void record() { trace.add(count); minSeen = Math.min(minSeen, count); maxSeen = Math.max(maxSeen, count); }
    private void wake() { if (notifyAllFlag) notifyAll(); else notify(); }

    synchronized void put(int v) throws InterruptedException {
        if (useWhile) { while (count == data.length) wait(); } else { if (count == data.length) wait(); }
        data[in] = v; in = (in + 1) % data.length; count++; record(); wake();
    }
    synchronized int take() throws InterruptedException {
        if (useWhile) { while (count == 0) wait(); } else { if (count == 0) wait(); }
        int v = data[out]; out = (out + 1) % data.length; count--; record(); wake(); return v;
    }
}
