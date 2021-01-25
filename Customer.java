package socialDistanceShopSampleSolution;

import java.awt.Color;
import java.util.Random;
import java.util.concurrent.*;

/*
 This is the basic Customer Thread class.
 */
public class Customer extends Thread {
	
	public static int IDcounter=0; //total customers created
	public static ShopGrid shop; //shared shop

	public volatile static boolean done; //add stop button
	public volatile static boolean pause; //add pause button
	public volatile static Object pauseLock;
	
	GridBlock currentBlock;
	private Random rand;
	private int movingSpeed;
	private static int maxWait=1500;
	private static int minWait=100;
	
	
	private CustomerLocation myLocation;
	private boolean inRoom;
	private boolean wantCheckout;
	private boolean wantToLeave;
	
	private PeopleCounter counter;
	private int ID; //thread ID for debugging

	
	
	Customer( int ID, PeopleCounter score, CustomerLocation loc) {
		this.counter=score;
		this.ID=ID;
		this.rand = new Random();
		
		movingSpeed=(int)(Math.random() * (maxWait-minWait)+minWait); //range of speeds for customers
		this.myLocation = loc;
		inRoom=false;
		wantCheckout=false; //need to shop first
		wantToLeave=false;
		
	}
	
	
	//getter
	public  boolean inRoom() {
		return inRoom;
	}
	
	//getter
	public   int getX() { return currentBlock.getX();}	
	
	//getter
	public   int getY() {	return currentBlock.getY();	}
	
	//getter
	public   int getSpeed() { return movingSpeed; }

	//setter
	public void updateLocation() {
		if (inRoom) {
			myLocation.setX(currentBlock.getX());
			myLocation.setY(currentBlock.getY());
			myLocation.setInRoom(inRoom);
		}
		else myLocation.setInRoom(inRoom);
		
	}
	
	//check to see if user pressed pause buttone
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
	
	
	
	//customer enters shop 
	public void enterShop() throws InterruptedException {
		currentBlock = shop.enterShop();  //enter through entrance
		inRoom=true;
		updateLocation();
		System.out.println("Thread "+this.ID + " entered shop at position: " + currentBlock.getX()  + " " +currentBlock.getY() );
		counter.personEntered(); //add to counter
		sleep(movingSpeed/2);  //wait a bit
	}
	
	//go to checkout counter
	private void headToCheckoutCounter() throws InterruptedException {
		int x_mv= rand.nextInt(3)-1;		
		currentBlock=shop.move(currentBlock,x_mv,1); //always head towrd checkout counter
		updateLocation();
		System.out.println("Thread "+this.ID + " moved to position: " + currentBlock.getX()  + " " +currentBlock.getY() );
		sleep(movingSpeed/2);  //wait a bit
	}
	
	//checkout 
	private void checkout() throws InterruptedException {
		sleep(movingSpeed*(rand.nextInt(5)+1)); //checkout takes a while
		wantCheckout=false;
		wantToLeave=true;
		currentBlock=shop.move(currentBlock,0,1); //always step away from checkout counter
		updateLocation();
		System.out.println("Thread "+this.ID + " moved to position: " + currentBlock.getX()  + " " +currentBlock.getY() );
		sleep(movingSpeed);  //wait a bit
	}

	
	//go head towards exit
	private void headTowardsExit() throws InterruptedException {
		int y_mv= rand.nextInt(2);//can't head back into shop
		int x_mv= -1; //always head toward left side of shop where exit is
		if (currentBlock.getX()==0) x_mv=0;
		currentBlock=shop.move(currentBlock,x_mv,y_mv); 
		updateLocation();
		System.out.println("Thread "+this.ID + " moved to position: " + currentBlock.getX()  + " " +currentBlock.getY() );
		sleep(movingSpeed);  //wait a bit
	}
	
	//browsing in the shop
	private void browse() throws InterruptedException {
		int x_mv= rand.nextInt(3)-1;
		int y_mv;
		if (currentBlock.getY() == (shop.checkout_y-1)) y_mv=rand.nextInt(2)-1; //not allowed at checkout yet
		else y_mv=rand.nextInt(3)-1;
		
		if (!((x_mv==0)&&(y_mv==0))) {
			currentBlock=shop.move(currentBlock,x_mv,y_mv);
			updateLocation();
			System.out.println("Thread "+this.ID + " moved to position: " + currentBlock.getX()  + " " +currentBlock.getY() );
		} 
		if (rand.nextInt(10) >8) wantCheckout = true; //randomly decide browsing is over
		sleep(movingSpeed);  //wait a bit
	}
	
	//leave shop
	private void leave() throws InterruptedException {
		shop.leaveShop(currentBlock);					
		inRoom=false;
		updateLocation();
		counter.personLeft(); //add to counter
	}
	
	public void run() {
		try {
			checkPause();
			counter.personArrived(); //add to counter of people waiting when arrived
			System.out.println("Thread "+ this.ID + " arrived at shop"); //output in standard format  - do not change this
			checkPause(); //NB need to check whether have been asked to pause
			enterShop();
			checkPause();
			headToCheckoutCounter();//move into shop quickly to avoid jamming door
			System.out.println("Thread "+this.ID + " at position: " + currentBlock.getX()  + " " +currentBlock.getY() );
			checkPause();
			sleep(movingSpeed/5);  //wait a bit		
		
			while ((!done)&&(inRoom)) {	
				checkPause();
				updateLocation();
				if (wantCheckout) {
					
					if (currentBlock.isCheckoutCounter()) {
							checkout();
							System.out.println("Thread "+this.ID + " checkout done " );
						}
					else {
						System.out.println("Thread "+this.ID + " going to checkout " );
						headToCheckoutCounter();
					}
				}
				else if (wantToLeave) {
					
					if (currentBlock.isExit()) { 
						leave();
						System.out.println("Thread "+this.ID + " left shop");

					}
					else {
						System.out.println("Thread "+this.ID + " going to exit" );
						headTowardsExit();
					}
				 
				} 
				else { //first browse
					System.out.println("Thread "+this.ID + " is browsing " );
					browse();
				}
				
				System.out.println("Thread "+this.ID + " at position: " + currentBlock.getX()  + " " +currentBlock.getY() );

			}

			System.out.println("Thread "+this.ID + " is done");

		} catch (InterruptedException e1) {
			done=true;
		}
	}
	
}
