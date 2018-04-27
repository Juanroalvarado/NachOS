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
		listener = new Condition(conditionLock);
		speaker = new Condition(conditionLock);
    
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
            //System.out.println("Speaker sleeps");
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
            //System.out.println("Listener sleeps");
		}

		//listener receives word
		boolWord = false;
        word = sound;
		listeners -= 1;
		conditionLock.release();
		return word;
    }


    private static final char dbgThread = 't';

    private Lock conditionLock;
    private Condition listener;
    private Condition speaker;
    private int listeners;
    private int speakers;
    private int sound;
    private boolean boolWord;

}
