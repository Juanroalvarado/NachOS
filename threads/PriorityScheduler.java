package nachos.threads;

import nachos.machine.*;

import java.util.*;
import java.util.Comparator;
import java.util.PriorityQueue;


/**
 * A scheduler that chooses threads based on their priorities.
 * <p/>
 * <p/>
 * A priority scheduler associates a priority with each thread. The next thread
 * to be dequeued is always a thread with priority no less than any other
 * waiting thread's priority. Like a round-robin scheduler, the thread that is
 * dequeued is, among all the threads of the same (highest) priority, the
 * thread that has been waiting longest.
 * <p/>
 * <p/>
 * Essentially, a priority scheduler gives access in a round-robin fassion to
 * all the highest-priority threads, and ignores all other threads. This has
 * the potential to
 * starve a thread if there's always a thread waiting with higher priority.
 * <p/>
 * <p/>
 * A priority scheduler must partially solve the priority inversion problem; in
 * particular, priority must be donated through locks,` and through joins.
 */
public class PriorityScheduler extends Scheduler {
    /**
     * Allocate a new priority scheduler.
     */
    public PriorityScheduler() {
    }

    /**
     * Allocate a new priority thread queue.
     *
     * @param transferPriority <tt>true</tt> if this queue should
     *                         transfer priority from waiting threads
     *                         to the owning thread.
     * @return a new priority thread queue.
     */
    public ThreadQueue newThreadQueue(boolean transferPriority) {
        return new PriorityQueue(transferPriority);
    }

    public int getPriority(KThread thread) {
        Lib.assertTrue(Machine.interrupt().disabled());

        return getThreadState(thread).priority;
    }

    public int getEffectivePriority(KThread thread) {
        Lib.assertTrue(Machine.interrupt().disabled());

        return getThreadState(thread).getEffectivePriority();
    }

    public void setPriority(KThread thread, int priority) {
        Lib.assertTrue(Machine.interrupt().disabled());

        Lib.assertTrue(priority >= priorityMinimum &&
                priority <= priorityMaximum);

        getThreadState(thread).setPriority(priority);
    }

    public boolean increasePriority() {
        boolean intStatus = Machine.interrupt().disable();

        KThread thread = KThread.currentThread();

        int priority = getPriority(thread);
        if (priority == priorityMaximum)
            return false;

        setPriority(thread, priority + 1);

        Machine.interrupt().restore(intStatus);
        return true;
    }

    public boolean decreasePriority() {
        boolean intStatus = Machine.interrupt().disable();

        KThread thread = KThread.currentThread();

        int priority = getPriority(thread);
        if (priority == priorityMinimum)
            return false;

        setPriority(thread, priority - 1);

        Machine.interrupt().restore(intStatus);
        return true;
    }

    /**
     * The default priority for a new thread. Do not change this value.
     */
    public static final int priorityDefault = 1;
    /**
     * The minimum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMinimum = 0;
    /**
     * The maximum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMaximum = 7;

    /**
     * Return the scheduling state of the specified thread.
     *
     * @param thread the thread whose scheduling state to return.
     * @return the scheduling state of the specified thread.
     */
    protected ThreadState getThreadState(KThread thread) {
        if (thread.schedulingState == null)
            thread.schedulingState = new ThreadState(thread);

        return (ThreadState) thread.schedulingState;
    }



    public static void selfTest(){
        /*
         * Creates 3 threads with different priorities and runs them
         */
        System.out.println("Priority TEST #2: START");
        KThread thread1 = new KThread(new Runnable(){
            public void run(){

                //KThread.yield();

                System.out.println("Im first to run");
            }
        }).setName("Thread1");
        KThread thread2 = new KThread(new Runnable(){
            public void run(){
                System.out.println("Im Second to run");
            }
        }).setName("Thread2");
        KThread thread3 = new KThread(new Runnable(){

            public void run(){

                //KThread.yield();
                System.out.println("Im Third to run");
            }
        }).setName("Thread3");
        Machine.interrupt().disable();
        ThreadedKernel.scheduler.setPriority(3);
        ThreadedKernel.scheduler.setPriority(thread1, 5);
        ThreadedKernel.scheduler.setPriority(thread2, 7);
        ThreadedKernel.scheduler.setPriority(thread3, 6);

        Machine.interrupt().enable();
        thread1.fork();
        thread2.fork();
        thread3.fork();

        KThread.yield();
        System.out.println("Priority TEST #2: END");
    }


    /**
     * A <tt>ThreadQueue</tt> that sorts threads by priority.
     */



    protected class PriorityQueue extends ThreadQueue {

        PriorityQueue(boolean transferPriority) {
            this.transferPriority = transferPriority;
            /** Added priorityQueue for waitingThreads */
            this.threadsWaiting = new java.util.PriorityQueue<ThreadState>();
        }

        public void waitForAccess(KThread thread) {
            Lib.assertTrue(Machine.interrupt().disabled());

            final ThreadState ts = getThreadState(thread);
            this.threadsWaiting.add(ts);
            ts.waitForAccess(this);
        }


        public void acquire(KThread thread) {
            Lib.assertTrue(Machine.interrupt().disabled());
            final ThreadState ts = getThreadState(thread);
            if (this.owner != null) {
                this.owner.release(this);
            }
            this.owner = ts;
            ts.acquire(this);
        }


        public KThread nextThread() {
            Lib.assertTrue(Machine.interrupt().disabled());

            // Pick the next thread
            final ThreadState nextThread = this.pickNextThread();

            if (nextThread == null) return null;

            // Remove the next thread from the queue
            this.threadsWaiting.remove(nextThread);

            // Give nextThread the resource
            this.acquire(nextThread.getThread());

            return nextThread.getThread();
        }
        /**
         * Return the next thread that <tt>nextThread()</tt> would return,
         * without modifying the state of this queue.
         *
         * @return the next thread that <tt>nextThread()</tt> would
         *         return.
         */
        protected ThreadState pickNextThread() {
            boolean intStatus = Machine.interrupt().disable();
            Machine.interrupt().restore(intStatus);
            return this.threadsWaiting.peek();
        }

        /**
         * This method returns the effectivePriority of this PriorityQueue.C
         * The return value is cached for as long as possible. If the cached value
         * has been invalidated, this method will spawn a series of mutually
         * recursive calls needed to recalculate effectivePriorities across the
         * entire resource graph.
         * @return
         */
        public int getEffectivePriority() {
            if (!this.transferPriority) {
                return priorityMinimum;
            } else if (this.priorityChange) {
                /*
                this.effectivePriority = priorityMinimum;
                for (final ThreadState curr : this.threadsWaiting) {
                    this.effectivePriority = Math.max(this.effectivePriority, curr.getEffectivePriority());
                } */
                this.effectivePriority = this.threadsWaiting.peek().getEffectivePriority();
                this.priorityChange = false;
            }
            return effectivePriority;
        }

        public void print() {
            Lib.assertTrue(Machine.interrupt().disabled());
            // implement me ( if you want )
        }

        private void invalidateCachedPriority() {
            if (!this.transferPriority) return;

            this.priorityChange = true;

            if (this.owner != null) {
                owner.invalidateCachedPriority();
            }
        }

        /**
           The priorityQueue of threads  waiting.
         */
        protected final java.util.PriorityQueue<ThreadState> threadsWaiting;
        protected ThreadState owner = null;
        protected int effectivePriority = priorityMinimum;
        protected boolean priorityChange = false;
        public boolean transferPriority;

    }

    /**
     * The scheduling state of a thread. This should include the thread's
     * priority, its effective priority, any objects it owns, and the queue
     * it's waiting for, if any.
     *
     * @see nachos.threads.KThread#schedulingState
     */
    protected class ThreadState implements Comparable<ThreadState> {
        /**
         * Allocate a new <tt>ThreadState</tt> object and associate it with the
         * specified thread.
         *
         * @param thread the thread this state belongs to.
         */
        public ThreadState(KThread thread) {
            this.thread = thread;

            this.heldQueues = new LinkedList<PriorityQueue>();
            this.wantedQueues = new LinkedList<PriorityQueue>();
            setPriority(priorityDefault);

        }

        /**
         * Return the effective priority of the associated thread.
         *
         * @return the effective priority of the associated thread.
         */
        public int getEffectivePriority() {

            if (this.heldQueues.isEmpty()) {
                return this.priority;
            } else if (this.priorityChange) {
                this.effectivePriority = this.priority;
                for (final PriorityQueue pq : this.heldQueues) {
                    if (this.effectivePriority < pq.getEffectivePriority()){
                        this.effectivePriority = pq.getEffectivePriority();
                    }
                }
                this.priorityChange = false;
            }
            return this.effectivePriority;
        }

        /**
         * Set the priority of the associated thread to the specified value.
         *
         * @param priority the new priority.
         */
        public void setPriority(int priority) {
            if (this.priority == priority)
                return;
            this.priority = priority;
            // force priority invalidation
            for (final PriorityQueue pq : wantedQueues) {
                pq.invalidateCachedPriority();
            }
        }

        /**
         * Called when <tt>waitForAccess(thread)</tt> (where <tt>thread</tt> is
         * the associated thread) is invoked on the specified priority queue.
         * The associated thread is therefore waiting for access to the
         * resource guarded by <tt>waitQueue</tt>. This method is only called
         * if the associated thread cannot immediately obtain access.
         *
         * @param waitQueue the queue that the associated thread is
         *                  now waiting on.
         * @see nachos.threads.ThreadQueue#waitForAccess
         */
        public void waitForAccess(PriorityQueue waitQueue) {
            this.life = Machine.timer().getTime();
            this.wantedQueues.add(waitQueue);
            this.heldQueues.remove(waitQueue);
            waitQueue.invalidateCachedPriority();
        }

        /**
         * Called when the associated thread has acquired access to whatever is
         * guarded by <tt>waitQueue</tt>. This can occur either as a result of
         * <tt>acquire(thread)</tt> being invoked on <tt>waitQueue</tt> (where
         * <tt>thread</tt> is the associated thread), or as a result of
         * <tt>nextThread()</tt> being invoked on <tt>waitQueue</tt>.
         *
         * @see nachos.threads.ThreadQueue#acquire
         * @see nachos.threads.ThreadQueue#nextThread
         */
        public void acquire(PriorityQueue waitQueue) {
            this.heldQueues.add(waitQueue);
            this.wantedQueues.remove(waitQueue);
            this.invalidateCachedPriority();
        }

        /**
         * Called when the associated thread has relinquished access to whatever
         * is guarded by waitQueue.
          * @param waitQueue The waitQueue corresponding to the relinquished resource.
         */
        public void release(PriorityQueue waitQueue) {
            this.heldQueues.remove(waitQueue);
            this.invalidateCachedPriority();
        }

        public KThread getThread() {
            return thread;
        }

        private void invalidateCachedPriority() {
            if (this.priorityChange) return;
            this.priorityChange = true;
            for (final PriorityQueue pq : this.wantedQueues) {
                pq.invalidateCachedPriority();
            }
        }

        public int compareTo(ThreadState secondThreadState){
            if (this.getEffectivePriority() < secondThreadState.getEffectivePriority()){
                return 1;
            }
            else if (this.getEffectivePriority() > secondThreadState.getEffectivePriority()) {
                return -1;
            }
            else {
                if (this.life > secondThreadState.life){
                    return 1;
                }
                else if (this.life > secondThreadState.life){
                    return -1;
                }
            }
            return 0;
        }

        /**
         * The thread with which this object is associated.
         */
        protected KThread thread;
        /**
         * The priority of the associated thread.
         */
        protected int priority;

        protected boolean priorityChange = false;
        protected int effectivePriority = priorityMinimum;
        protected final List<PriorityQueue> heldQueues;
        protected final List<PriorityQueue> wantedQueues;
        public long life = Machine.timer().getTime();
    }
}
