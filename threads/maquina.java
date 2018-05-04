package nachos.threads;
import java.util.Random;

public class maquina
        {

public static void selfTest() {


        llave = new Lock();
        camionCoca = new Condition2(llave);
        consumidor = new Condition2(llave);
        //int maquina = 0;
        //int limite = 5;
        int n = 10;
        Random rand = new Random();

        Runnable depositar = new Runnable() {
public void run() {
        for (int i = 0; i <= n; i++) {
                coca();
                System.out.println(maquina);
                int tiemp = rand.nextInt(100);
                ThreadedKernel.alarm.waitUntil(tiemp);
        }
        }
        };
        Runnable sacar = new Runnable() {
public void run() {

        for(int i = 0; i <= n; i++) {
                System.out.println(maquina);
                cons();
        }
        }
        };

        KThread speak = new KThread(depositar);

        KThread listen = new KThread(sacar);


        speak.setName("Coca").fork();
        listen.setName("Consumidor").fork();

        speak.join();
        listen.join();

        };




//private Lock newLock;
//private Condition2 coca(newLock);
//private Condition2 cons(newLock);

static void coca(){
                llave.acquire();
        if(maquina < limite) {
        maquina += 1;
        System.out.println("Deposito 1 lata");

        }
        else {
                consumidor.wake();
        camionCoca.sleep();
        }
                KThread.yield();
                llave.release();
        };

static void cons(){


                llave.acquire();
        if(maquina > 0 ) {
        maquina -= 1;
        System.out.println("Saco 1 lata");
                KThread.yield();
        }
        else {
        camionCoca.wake();
        consumidor.sleep();
        }

                llave.release();

        };

public Random rand = new Random();

public static int limite = 5;
public static int maquina = 0;
public static int n = 10;
private static Lock llave;
private static Condition2 camionCoca;
private static Condition2 consumidor;



        }