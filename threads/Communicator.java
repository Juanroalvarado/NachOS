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
		wordLock = new Lock();
		listener = new Condition2(wordLock);
		speaker = new Condition2(wordLock);

    }

    /**a
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

		wordLock.acquire();
		speakers += 1;

		while (wordOut == true || listeners < 1) {

			speaker.sleep();
		}
		// speaker says word
		wordOut = true;
		listener.wake();
		spokenWord = word;
		speakers -= 1;

		wordLock.release();

    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */
    public int listen() {

		wordLock.acquire();
		listeners += 1;

		listening = true;

		while (wordOut == false){
			speaker.wake();
			listener.sleep();

		}

		//listener receives word
		wordOut = false;
		listening = false;
        int message = spokenWord;
		listeners -= 1;

		speaker.wake();
		wordLock.release();
		return message;
    }



    private static final char dbgThread = 't';

    private Lock wordLock;
    private Condition2 listener;
    private Condition2 speaker;
    private int listeners;
    private int speakers;
    private int spokenWord;
    private boolean wordOut = false;
    private boolean listening;
    private boolean speaking;

}
