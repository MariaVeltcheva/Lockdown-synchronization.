// GridBlock class to represent a block in the shop.

import java.util.concurrent.atomic.*;

public class GridBlock {
	private AtomicBoolean isOccupied; //made isOccupied atomic to avoid read/write issues amongst threads
	private final boolean isExit; 
	private final boolean isCheckoutCounter;
	private AtomicInteger[] coords; // the coordinate of the block.
	private int ID;
	private volatile AtomicInteger moveCount; //added moveCount integer to keep track of how many moves a customer makes

	public static int classCounter=0;

	GridBlock(boolean exitBlock, boolean checkoutBlock) throws InterruptedException {
		isExit=exitBlock;
		isCheckoutCounter=checkoutBlock;
		isOccupied= new AtomicBoolean(false);
		moveCount = new AtomicInteger(0);
		ID=classCounter;
		classCounter++;
	}
	
	GridBlock(int x, int y, boolean exitBlock, boolean refreshBlock) throws InterruptedException {
		this(exitBlock,refreshBlock);
		coords = new AtomicInteger[] {new AtomicInteger(x),new AtomicInteger(y)};
	}
	
	//getter
	public  int getX() {return coords[0].get();}
	
	//getter
	public  int getY() {return coords[1].get();}
	
	//for customer to move to a block //made to create Atomic boolean because variable is now atomic
	public boolean get() throws InterruptedException {
		isOccupied= new AtomicBoolean(true);
		return true;
	}
		
	//for customer to leave a block //made to update Atomic boolean because variable is now atomic
	public  void release() {
		isOccupied =new AtomicBoolean(false);
	}
	
	//getter //made to use atomic method get
	public boolean occupied() {
		return isOccupied.get();
	}
	
	//getter
	public  boolean isExit() {
		return isExit;	
	}

	//getter
	public  boolean isCheckoutCounter() {
		return isCheckoutCounter;
	}
	
	//getter
	public int getID() {return this.ID;}

	//returns the moveCount of the GridBlock
	public int getMoveCount(){
		return moveCount.intValue();
	}

	//increments the moveCount variable
	public void incrMoveCount(){
		moveCount.incrementAndGet();
	}

	//decrements the moveCount variable
	public void decrMoveCount(){
		moveCount.decrementAndGet();
	}
}
