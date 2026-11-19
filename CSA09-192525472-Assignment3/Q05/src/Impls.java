import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

// Every implementation has the same shape: some non-critical work (fee calculation) + a check-then-book step.
class Work {
    static volatile long ioNanos = 0;                // simulated payment-gateway wait (non-critical part)
    static double fee(int seat) {
        if (ioNanos > 0) LockSupport.parkNanos(ioNanos);
        double x = seat + 1; for (int i = 0; i < 300; i++) x = Math.sqrt(x + i); return x; }
}

class Unsafe implements Booker {
    final boolean[] booked; final boolean widen;
    Unsafe(int seats, boolean widenWindow) { booked = new boolean[seats]; widen = widenWindow; }
    public boolean book(int seat) {
        if (Work.fee(seat) < 0) return false;
        if (!booked[seat]) {                         // CHECK
            if (widen) Thread.yield();               // make the window between check and act wider
            booked[seat] = true;                     // ACT
            return true;
        }
        return false;
    }
}

class SyncMethod implements Booker {                 // lock = this, held for the WHOLE method incl. fee()
    final boolean[] booked;
    SyncMethod(int seats) { booked = new boolean[seats]; }
    public synchronized boolean book(int seat) {
        if (Work.fee(seat) < 0) return false;
        if (!booked[seat]) { booked[seat] = true; return true; }
        return false;
    }
}

class SyncBlock implements Booker {                  // lock = this, held only for the critical section
    final boolean[] booked;
    SyncBlock(int seats) { booked = new boolean[seats]; }
    public boolean book(int seat) {
        if (Work.fee(seat) < 0) return false;
        synchronized (this) {
            if (!booked[seat]) { booked[seat] = true; return true; }
            return false;
        }
    }
}

class LockBooker implements Booker {                 // alternative 1: ReentrantLock
    final boolean[] booked; final ReentrantLock lock = new ReentrantLock();
    LockBooker(int seats) { booked = new boolean[seats]; }
    public boolean book(int seat) {
        if (Work.fee(seat) < 0) return false;
        lock.lock();
        try { if (!booked[seat]) { booked[seat] = true; return true; } return false; }
        finally { lock.unlock(); }
    }
}

class CasBooker implements Booker {                  // alternative 2: AtomicBoolean[] + compareAndSet
    final AtomicBoolean[] booked;
    CasBooker(int seats) { booked = new AtomicBoolean[seats]; for (int i = 0; i < seats; i++) booked[i] = new AtomicBoolean(); }
    public boolean book(int seat) {
        if (Work.fee(seat) < 0) return false;
        return booked[seat].compareAndSet(false, true);
    }
}

class MapBooker implements Booker {                  // alternative 3: ConcurrentHashMap.putIfAbsent
    final ConcurrentHashMap<Integer, Boolean> booked = new ConcurrentHashMap<>();
    MapBooker(int seats) { }
    public boolean book(int seat) {
        if (Work.fee(seat) < 0) return false;
        return booked.putIfAbsent(seat, Boolean.TRUE) == null;
    }
}
