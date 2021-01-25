
import java.awt.Color;
import java.util.Random;
import java.util.concurrent.atomic.*;

public class CustomerLocation  { // this is a separate class so don't have to access thread
	
//can protect with Atomic variables or with synchronized	
	private final int ID; //total customers created
	private Color myColor;
	private volatile AtomicBoolean inRoom; //made volatile so that most recent value is always read
	private volatile AtomicInteger x; //made volatile so that most recent value is always read
	private volatile AtomicInteger y; //made volatile so that most recent value is always read
	
	CustomerLocation(int ID ) throws InterruptedException {
		Random rand = new Random();
		float c = rand.nextFloat();
		myColor = new Color(c, rand.nextFloat(), c);	//only set at beginning	by thread
		inRoom = new AtomicBoolean(false);
		this.ID=ID;
		this.x = new AtomicInteger(0);
		this.y = new AtomicInteger(0);
	}

	
	//setter
	public void  setX(int x) { this.x.set(x);}
		
	//setter
	public void  setY(int y) {	this.y.set(y);	}
	
	//setter
	public void setInRoom(boolean in) {
		this.inRoom.set(in);
}
	//getter
	public  int getX() { return x.get();}
	
	//getter
	public  int getY() {	return y.get();	}
	
	//getter
		public  int getID() {	return ID;	}

	//getter
		public  boolean inRoom() {
			return inRoom.get();
		}
	//getter
	public synchronized  Color getColor() { return myColor; }
		
}
