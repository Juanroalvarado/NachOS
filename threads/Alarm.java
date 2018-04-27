package nachos.threads;
import java.util.PriorityQueue;
import nachos.machine.*;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     *
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */
    public Alarm() {
	Machine.timer().setInterruptHandler(new Runnable() {
		public void run() { timerInterrupt(); }
	    });
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    public void timerInterrupt() {
		Lib.assertTrue(Machine.interrupt().disabled());

		long curTime = Machine.timer().getTime();
		System.out.println("check time:" + curTime);
		while(!waitQueue.isEmpty() && waitQueue.peek().time <= curTime){

			if (waitQueue.peek().time <= curTime){
				System.out.println("waking:" + curTime);
				waitQueue.poll().thread.ready();
			} else {
				break;
			}

		}

		Machine.interrupt().enable();

		KThread.currentThread().yield();
    }

    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param	x	the minimum number of clock ticks to wait.
     *
     * @see	nachos.machine.Timer#getTime()
     */
    public void waitUntil(long x) {
		Machine.interrupt().disable();
		//System.out.println("Sleep Tick:" + Machine.timer().getTime());
		/**
		* Crear un nuevo WaitThread con el currentThread y el X (waittime)
		* agregar a priority Queue
		*/
		long upTime = Machine.timer().getTime() + x;
		System.out.println("uptime :" + upTime);
		WaitingThread wThread = new WaitingThread(KThread.currentThread(), upTime);

		waitQueue.add(wThread);

		// dormir thread
		KThread.sleep();

		Machine.interrupt().enable();

    }
    
    private class WaitingThread implements Comparable {

	WaitingThread(KThread thread,long time) {  
		this.thread = thread;
		this.time = time;
		
	    }

		public int compareTo(Object o) {
		    WaitingThread toOccur = (WaitingThread) o;

		    // can't return 0 for unequal objects, so check all fields
		    if (time < toOccur.time){
				return -1;
			}
		    
		    else if (time > toOccur.time){
				return 1;
			}
		    
		    else {
				return thread.compareTo(toOccur.thread);
			}  
		}

    long time;
    KThread thread;

    }

    private static class PingTest implements Runnable {
	PingTest(int which, int time) {
	    this.which = which;
	    this.time = time;
	}
	
	public void run() {
	    ThreadedKernel.alarm.waitUntil(time);
	    for (int i=0; i<5; i++) {
		System.out.println("*** thread " + which + " looped "
				   + i + " times");

		KThread.yield();
	    }
	}

	private int which;
	private int time;
    }

    public static void selfTest() {
		Lib.debug(dbgThread, "Enter Alarm.selfTest");
		System.out.println(" Alarm TEST: START");
		// Aqui le agregamos una variable al ping test con el Wait time,
		// podemos ver que hara el ping test en orden de menor timepo a mayor tiempo
		KThread t1 = new KThread(new PingTest(1,1000));
		KThread t2 = new KThread(new PingTest(2,50));
		KThread t3 = new KThread(new PingTest(3,500));

		t1.fork();
		t2.fork();
		t3.fork();
    }
	
   private static final char dbgThread = 't';

   private PriorityQueue<WaitingThread> waitQueue = new PriorityQueue<WaitingThread>();
}
