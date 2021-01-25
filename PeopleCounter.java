
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.Semaphore;

//class to keep track of people inside and outside and left shop
public class PeopleCounter {
	private volatile AtomicInteger peopleOutSide; //counter for people arrived but not yet in the building // made Atomic and volatile to avoid read/write issues amongst threads
	private volatile AtomicInteger peopleInside; //people inside the shop //made Atomic and volatile to avoid read/write issues amongst threads
	private volatile AtomicInteger peopleLeft; //people left the shop //made Atomic and volatile to avoid read/write issues amongst threads
	private int maxPeople; //maximum for lockdown rules
	private Semaphore available; //Sephamore created to ensure no more than max num of people in the store
	
	PeopleCounter(int max) {
		peopleOutSide = new AtomicInteger(0);
		peopleInside = new AtomicInteger(0);
		peopleLeft = new AtomicInteger(0);
		maxPeople = max; //made maxPeople = max so that maxPeople reflects the maximum specified
		available = new Semaphore(maxPeople,true); //initialized to maxPeople, set fairness to true to ensure FIFO
	}
		
	//getter //changed to use Atomic methods because variables are now atomic
	public int getWaiting() {
		return peopleOutSide.intValue();
	}

	//getter //changed to use Atomic methods because variables are now atomic
	public int getInside() {
		return peopleInside.intValue();
	}
	
	//getter
	/*public int getTotal() {
		return (peopleOutSide+peopleInside+peopleLeft);
	}*/

	//getter
	public AtomicInteger getLeft() {
		return peopleLeft;
	}
	
	//getter
	public int getMax() {
		return maxPeople;
	}
	
	//getter //changed to use Atomic methods because variables are now atomic
	public void personArrived() throws InterruptedException {
		peopleOutSide.incrementAndGet();
	}
	
	//update counters for a person entering the shop //made to use Atomic methods because variables are now atomic
	public void personEntered() throws InterruptedException {
		available.acquire(); //made to acquire the lock
		peopleOutSide.decrementAndGet();
		peopleInside.incrementAndGet();
	}

	//update counters for a person exiting the shop //made to use Atomic methods because variables are now atomic
	public void personLeft() throws InterruptedException {
		peopleInside.decrementAndGet();
		peopleLeft.incrementAndGet();
		available.release(); //made to release the lock
		
	}

	//reset - not really used //made to initilaise atomic integers because variables are now atomic
	synchronized public void resetScore() {
		peopleInside = new AtomicInteger(0);
		peopleOutSide = new AtomicInteger(0);
		peopleLeft = new AtomicInteger(0);
	}
}
