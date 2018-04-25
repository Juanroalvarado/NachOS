package nachos.threads;
import nachos.ag.BoatGrader;

public class Boat
{
    static BoatGrader bg;

    public static void selfTest()
    {
	BoatGrader b = new BoatGrader();

	System.out.println("\n ***Testing Boats with only 2 children***");
	begin(3, 3, b);

//	System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
//  	begin(1, 2, b);

//  	System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
//  	begin(3, 3, b);
    }

    public static void begin( int adults, int children, BoatGrader b )
    {
	// Store the externally generated autograder in a class
	// variable to be accessible by children.
	    bg = b;

        allAdults = adults;
        allChilds = children;

	// Instantiate global variables here
        int ChildrenOnOahu = 0;
        int AdultsOnOahu = 0;

        int AdultsOnMolokai = 0;
        int ChildrenOnMolokai = 0;

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
        while (ChildrenOnMolokai + AdultsOnMolokai != adults + children){
            mainCondition.sleep();
        }
        boat.release();
    }

    static void AdultItinerary()
    {
        /** lock en boat */
        boat.acquire();
        AdultsOnOahu += 1;
	/* This is where you should put your solutions. Make calls
	   to the BoatGrader to show that it is synchronized. For
	   example:
	       bg.AdultRowToMolokai();
	   indicates that an adult has rowed the boat across to Molokai
	*/

        while (!atOahu || ChildrenOnOahu > 1){
            System.out.println("sleep ");
            ChildAtOahu.wake();
            AdultAtOahu.sleep();
        }

        AdultsOnOahu -= 1;
        System.out.println(KThread.currentThread().getName());
        bg.AdultRowToMolokai();

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

        while (allAdults + allChilds > ChildrenOnMolokai + AdultsOnMolokai){

            if (atOahu && ChildrenOnOahu > 1){

                if (travelling){

                    ChildrenOnOahu -= 2;

                    //System.out.println(KThread.currentThread().getName());
                    bg.ChildRideToMolokai();
                    atOahu = false;
                    ChildrenOnMolokai += 2;
                    travelling = false;

                } else {
                    //System.out.println(KThread.currentThread().getName());
                    bg.ChildRowToMolokai();

                    travelling = true;
                    ChildAtOahu.wake();
                    ChildAtMolokai.sleep();
                }

            }
            else if (!atOahu) {

                ChildrenOnMolokai -= 1;
                bg.ChildRowToOahu();
                atOahu = true;

                ChildrenOnOahu += 1;
            }
            else {
                System.out.println("wake ");
                AdultAtOahu.wake();
                ChildAtOahu.sleep();

            }
            /*
            if (ChildrenOnMolokai + AdultsOnMolokai != adults + children) {
                mainCondition.wake();
            }
            */
        }

        /** vuelve el estado de boat en false*/
        boat.release();
        System.out.println("finish ");
        return;
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
    private static int ChildrenOnMolokai;
    private static int AdultsOnOahu;
    private static int AdultsOnMolokai;

    private static boolean atOahu = true;
    private static boolean travelling = false;

    public static int allChilds;
    public static int allAdults;

}
