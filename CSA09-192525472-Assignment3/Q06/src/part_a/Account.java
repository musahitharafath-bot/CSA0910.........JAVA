import java.util.concurrent.locks.ReentrantLock;

public class Account {
    final int id; int balance;
    final ReentrantLock lock = new ReentrantLock();      // used only by the tryLock version
    Account(int id, int balance) { this.id = id; this.balance = balance; }
    public String toString() { return "Acc" + id + "(" + balance + ")"; }
}
