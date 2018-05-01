package nachos.threads;
import java.util.LinkedList;
import nachos.machine.*;

/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 *
 * <p>
 * You must implement this.
 *
 * @see	nachos.threads.Condition
 */
public class Condition2 {
    /**
     * Allocate a new condition variable.
     *
     * @param	conditionLock	the lock associated with this condition
     *				variable. The current thread must hold this
     *				lock whenever it uses <tt>sleep()</tt>,
     *				<tt>wake()</tt>, or <tt>wakeAll()</tt>.
     */
    public Condition2(Lock conditionLock) {
	this.conditionLock = conditionLock;
	waitQueue = new LinkedList<KThread>();
    }

    /**
     * Atomically release the associated lock and go to sleep on this condition
     * variable until another thread wakes it using <tt>wake()</tt>. The
     * current thread must hold the associated lock. The thread will
     * automatically reacquire the lock before <tt>sleep()</tt> returns.
     */
    public void sleep() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());
		/** desahbilitar interrupciones */
		Machine.interrupt().disable();

		/**  Agregar current thread al WaitQueue */
		conditionLock.release();

		waitQueue.add(KThread.currentThread());

		/**  poner thread a dormir */
		//System.out.println(KThread.currentThread().getName() + " sleeping***");
		KThread.sleep();

		conditionLock.acquire();

		/** habilitar interrupciones */
		Machine.interrupt().enable();
    }

    /**
     * Wake up at most one thread sleeping on this condition variable. The
     * current thread must hold the associated lock.
     */
    public void wake() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());

		/** deshabilitar interrupciones */
		Machine.interrupt().disable();


		if (!waitQueue.isEmpty()){
			//System.out.println(waitQueue.peek().getName() + " waking***");
			waitQueue.pop().ready();
		}

		/** habilitar interrupciones */
		Machine.interrupt().enable();
    }

    /**
     * Wake up all threads sleeping on this condition variable. The current
     * thread must hold the associated lock.
     */
    public void wakeAll() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());

	while  (!waitQueue.isEmpty()) {
		wake();
	}


    }


    private Lock conditionLock;
    private LinkedList<KThread> waitQueue;
    private KThread wakeup;
}
