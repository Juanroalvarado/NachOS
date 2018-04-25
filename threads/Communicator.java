package nachos.threads;
import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
    /**
     * Allocate a new communicator.
     */
    public Communicator() {
		conditionLock = new Lock();
		listener = new Condition2(conditionLock);
		speaker = new Condition2(conditionLock);
    
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param	word	the integer to transfer.
     */
    public void speak(int word) {

		conditionLock.acquire();
		speakers += 1;

		while (boolWord == true || listeners < 1) {
            System.out.println("Speaker sleeps");
			speaker.sleep();
		}
		// speaker says word

		boolWord = true;
		listener.wakeAll();
        sound = word;
		speakers -= 1;
		conditionLock.release();

    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    public int listen() {
        int word;
		conditionLock.acquire();
		listeners += 1;

		while (boolWord == false){

			speaker.wakeAll();
			listener.sleep();
            System.out.println("Listener sleeps");
		}

		//listener receives word
		boolWord = false;
        word = sound;
		listeners -= 1;
		conditionLock.release();
		return word;
    }

	public static void selfTest() {
        Lib.debug(dbgThread, "Enter Communicator.selfTest");

		final Communicator com = new Communicator();

		KThread thread1 = new KThread(new Runnable() {
			public void run() {
				System.out.println("Thread 1 -- Start/Speaking");
				com.speak(0);
				System.out.println("Thread 1 -- Finish/Speaking");
			}
		});

		KThread thread2 = new KThread(new Runnable() {
			public void run() {
				System.out.println("Thread 2 -- Start/Listening");
				com.listen();
				System.out.println("Thread 2 -- Finish/Listening");
			}
		});

		thread1.fork();
		thread2.fork();
		//thread1.join();
		//thread2.join();
	}

    private static final char dbgThread = 't';

    private Lock conditionLock;
    private Condition2 listener;
    private Condition2 speaker;
    private int listeners;
    private int speakers;
    private int sound;
    private boolean boolWord;

}
