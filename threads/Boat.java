package nachos.threads;
import nachos.ag.BoatGrader;

public class Boat
{
    static BoatGrader bg;

    public static void selfTest()
    {
        BoatGrader b = new BoatGrader();

        begin(3, 4, b);

//	System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
//  	begin(1, 2, b);

//  	System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
//  	begin(3, 3, b);
    }

    public static void begin( int adults, int children, BoatGrader b )
    {
        System.out.println("\n ***Testing Boats with adult "+adults+" and child "+children+"***");

        // Store the externally generated autograder in a class
        // variable to be accessible by children.
        bg = b;

        allAdults = adults;
        allChilds = children;

        // Instantiate global variables here
        int ChildrenOnOahu = 0;
        int AdultsOnOahu = 0;

        AdultsOnMolokai = 0;
        ChildrenOnMolokai = 0;

        boat = new Lock();
        AdultAtOahu = new Condition2(boat);
        ChildAtOahu = new Condition2(boat);
        ChildAtMolokai = new Condition2(boat);
        mainCondition = new Condition2(boat);


        // Create threads here. See section 3.4 of the Nachos for Java
        // Walkthrough linked from the projects page.

        Runnable AdultRunner = new Runnable() {
            public void run() {
                AdultItinerary();
            }
        };

        Runnable ChildRunner = new Runnable() {
            public void run() {
                ChildItinerary();
            }
        };

        for (int i = 1; i <= children; i++) {
            KThread Child = new KThread(ChildRunner);

            Child.setName("Child " + i).fork();
        }

        for (int i = 1; i <= adults; i++) {
            KThread Adult = new KThread(AdultRunner);
            Adult.setName("Adult " + i).fork();
        }

        boat.acquire();
        while(!done(ChildrenOnMolokai,AdultsOnMolokai)){
            mainCondition.sleep();
        }
        finished = true;
        ChildAtMolokai.wakeAll();
        ChildAtOahu.wakeAll();

        boat.release();
    }

    public static boolean done(int ChildrenOnMolokai, int AdultsOnMolokai){
        //System.out.println("Audlts arrived: " + AdultsOnMolokai + " Children arrived: " + ChildrenOnMolokai);
        if (allAdults == AdultsOnMolokai && allChilds == ChildrenOnMolokai && !atOahu)
            return true;
        else
            return false;
    }

    static void AdultItinerary()
    {

	/* This is where you should put your solutions. Make calls
	   to the BoatGrader to show that it is synchronized. For
	   example:
	       bg.AdultRowToMolokai();
	   indicates that an adult has rowed the boat across to Molokai
	*/
        /** lock en boat */
        boat.acquire();
        AdultsOnOahu += 1;

        while (!atOahu || ChildrenOnOahu != 1){
            AdultAtOahu.sleep();
        }

        AdultsOnOahu -= 1;
        System.out.println(KThread.currentThread().getName() + " rowing to molokai");
        //bg.AdultRowToMolokai();

        /** vuelve el estado de boat en false*/
        atOahu = false;
        AdultsOnMolokai += 1;
        ChildAtMolokai.wake();
        boat.release();
    }

    static void ChildItinerary()
    {

        /** lock en boat */
        boat.acquire();
        ChildrenOnOahu +=1;

        while (!finished){

            if (atOahu && ChildrenOnOahu > 1 ){

                if (travelling){

                    ChildrenOnOahu -= 2;
                    System.out.println(KThread.currentThread().getName() + " riding to Molokai as passenger");
                    //bg.ChildRideToMolokai();
                    atOahu = false;
                    ChildrenOnMolokai += 2;
                    travelling = false;
                    /** Check for finality */

                    if(done(ChildrenOnMolokai,AdultsOnMolokai)){
                        mainCondition.wake();
                        finished = true;
                    }

                } else {
                    System.out.println(KThread.currentThread().getName() + " rowing to Molokai");
                    //bg.ChildRowToMolokai();
                    travelling = true;
                    ChildAtOahu.wake();
                    ChildAtMolokai.sleep();

                }

            }
            else if (!atOahu) {

                System.out.println(KThread.currentThread().getName() + " rowing to Oahu");
                ChildrenOnMolokai -= 1;
                //bg.ChildRowToOahu();
                atOahu = true;
                ChildrenOnOahu += 1;
                //AdultAtOahu.wake();

            }
            else {
                AdultAtOahu.wake();
                ChildAtOahu.sleep();
            }
        }
        /** vuelve el estado de boat en false*/
        //AdultAtOahu.wakeAll();
        boat.release();

    }



    static void SampleItinerary()
    {
        // Please note that this isn't a valid solution (you can't fit
        // all of them on the boat). Please also note that you may not
        // have a single thread calculate a solution and then just play
        // it back at the autograder -- you will be caught.
        System.out.println("\n ***Everyone piles on the boat and goes to Molokai***");
        bg.AdultRowToMolokai();
        bg.ChildRideToMolokai();
        bg.AdultRideToMolokai();
        bg.ChildRideToMolokai();

    }


    private static Lock boat;

    private static Condition2 AdultAtOahu ;
    private static Condition2 ChildAtMolokai ;
    private static Condition2 ChildAtOahu ;
    private static Condition2 mainCondition ;

    private static int ChildrenOnOahu;
    public static int ChildrenOnMolokai;
    private static int AdultsOnOahu;
    public static int AdultsOnMolokai;

    private static boolean atOahu = true;
    private static boolean travelling = false;
    private static boolean finished = false;

    public static int allChilds;
    public static int allAdults;

}