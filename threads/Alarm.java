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

	while(!waitQueue.isEmpty() && waitQueue.peek().time <= curTime){
		if (waitQueue.peek().time <= curTime){
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
	
	/** 
	* Crear un nuevo WaitThread con el currentThread y el X (waittime)
	* agregar a priority Queue
	*/
	long upTime = Machine.timer().getTime() + x;

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
		    if (time < toOccur.time)
		    return -1;
		    else if (time > toOccur.time)
		    return 1;
		    else
			return thread.compareTo(toOccur.thread);        
		}

    long time;
    KThread thread;

    }

    private static class AlarmTest implements Runnable {
	AlarmTest(long x) {
	    this.time = x;
	}
	
	public void run() {

        System.out.print(KThread.currentThread().getName() + " alarm\n");	
        ThreadedKernel.alarm.waitUntil(time);
        System.out.print(KThread.currentThread().getName() + " woken up \n");	

	}

    private long  time; 
    }

    public static void selfTest() {

    System.out.print("Enter Alarm.selfTest\n");	

	Runnable r = new Runnable() {
	    public void run() {
                KThread t[] = new KThread[10];

                for (int i=0; i<10; i++) {
                     t[i] = new KThread(new AlarmTest(160 + i*20));
                     t[i].setName("Thread" + i).fork();
                }
                for (int i=0; i<10000; i++) {
                    KThread.yield();
                }
            }
    };

    KThread t = new KThread(r);
    t.setName("Alarm SelfTest");
    t.fork();
    KThread.yield();

    t.join();

    System.out.print("Leave Alarm.selfTest\n");	

    }

   private PriorityQueue<WaitingThread> waitQueue = new PriorityQueue<WaitingThread>();
}
