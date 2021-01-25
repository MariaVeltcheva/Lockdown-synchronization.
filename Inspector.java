package socialDistanceShopSampleSolution;

import java.awt.Color;
import java.util.Random;
import java.util.concurrent.*;

/*
 This is the basic Inspector Thread class.
 */
public class Inspector extends Thread {
	
	private CustomerLocation [] customerLocations ;
	private PeopleCounter counter;
	private int max;
	public volatile static boolean pause; //add pause button
	public volatile static Object pauseLock;
	public volatile static boolean done; //add stop button

	
	
	Inspector(PeopleCounter score, CustomerLocation [] loc) {
		this.counter=score;
		this.customerLocations= loc;
		max=customerLocations.length;
			
	}
	
	
	//check to see if user pressed pause button
	private void checkPause() {
        synchronized(pauseLock) {
            while(pause) {
                try {
                	pauseLock.wait();
                } catch(InterruptedException e) {
                    // nothing
                }
            }
        }
    }
	
	
	public void run() {
		int i=0;
		String customersInShop ="";
		while(!done) {
			if (counter.getMax()<counter.getInside()) {
				System.out.println("Inspector: VIOLATION !!!Too many people inside shop!!!");
			}
			checkPause();
			
			customersInShop ="";
			for (i=0;i<max;i++){
				if (customerLocations[i].inRoom()) {
					String cust = " "+customerLocations[i].getX()+","+customerLocations[i].getY()+" ";
					if (customersInShop.contains(cust))
						System.out.println("Inspector: SOCIAL DISTANCING VIOLATION at position "+cust );
					else customersInShop+=cust;
				}
			}
		}
	}
	
}
