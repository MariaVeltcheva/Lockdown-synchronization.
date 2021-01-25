//Class to represent the shop, as a grid of gridblocks

import javax.swing.text.BadLocationException;
import java.util.concurrent.Semaphore;

public class ShopGrid {
	private GridBlock [][] Blocks;
	private final int x;
	private final int y;
	public final int checkout_y;
	private final static int minX =5;//minimum x dimension
	private final static int minY =5;//minimum y dimension
	private Semaphore mutex; //added Semaphore for use later on to ensure only one customer enters at a time
	private static volatile GridBlock entrance; //made entrance global, static and volatile to avoid read issues amongst threads
	
	
	ShopGrid() throws InterruptedException {
		this.x=20;
		this.y=20;
		this.checkout_y=y-3;
		Blocks = new GridBlock[x][y];
		int [] [] dfltExit= {{10,10}};
		//mutex = new Semaphore(1);
		this.initGrid(dfltExit);
	}
	
	ShopGrid(int x, int y, int [][] exitBlocks,int maxPeople) throws InterruptedException {
		if (x<minX) x=minX; //minimum x
		if (y<minY) y=minY; //minimum x
		this.x=x;
		this.y=y;
		this.checkout_y=y-3;
		Blocks = new GridBlock[x][y];
		//NOTE: if customer is at the entrance they are not counted as being in the room yet
		mutex = new Semaphore(1); //initialize Semaphore to 1 because only one customer can be in the entrance at a time
		this.initGrid(exitBlocks);
	}
	
	private  void initGrid(int [][] exitBlocks) throws InterruptedException {
		for (int i=0;i<x;i++) {
			for (int j=0;j<y;j++) {
				boolean exit=false;
				boolean checkout=false;
				for (int e=0;e<exitBlocks.length;e++)
						if ((i==exitBlocks[e][0])&&(j==exitBlocks[e][1])) 
							exit=true;
				if (j==(y-3)) {
					checkout=true; 
				}//checkout is hardcoded two rows before  the end of the shop
				Blocks[i][j]=new GridBlock(i,j,exit,checkout);
			}
		}
	}
	
	//get max X for grid
	public  int getMaxX() {
		return x;
	}
	
	//get max y  for grid
	public int getMaxY() {
		return y;
	}

	public GridBlock whereEntrance() { //hard coded entrance
		return Blocks[getMaxX()/2][0];
	}

	//is a position a valid grid position?
	public  boolean inGrid(int i, int j) {
		if ((i>=x) || (j>=y) ||(i<0) || (j<0)) 
			return false;
		return true;
	}
	
	//called by customer when entering shop
	public GridBlock enterShop() throws InterruptedException  {
		mutex.acquire();//acquire Semaphore
		whereEntrance().release();
		entrance = whereEntrance();
		boolean b = entrance.get();//set occupied to true
		return entrance;

	}
		
	//called when customer wants to move to a location in the shop
	public GridBlock move(GridBlock currentBlock,int step_x, int step_y) throws InterruptedException {  
		//try to move in

		int c_x= currentBlock.getX();
		int c_y= currentBlock.getY();
		
		int new_x = c_x+step_x; //new block x coordinates
		int new_y = c_y+step_y; // new block y  coordinates
		
		//restrict i an j to grid
		if (!inGrid(new_x,new_y)) {
			//Invalid move to outside shop - ignore
			//mutex.release();
			return currentBlock;
			
		}

		if ((new_x==currentBlock.getX())&&(new_y==currentBlock.getY())) {//not actually moving
			return currentBlock;
		}

		GridBlock newBlock = Blocks[new_x][new_y];
		//GridBlock newBlock = Blocks[0][0]; //for testing

		//check if block occupied. if so, return current block. this is to ensure customers don't occupy the same block
		if (newBlock.occupied()){
			return currentBlock;
		}

			if (newBlock.get())  {  //get successful because block not occupied
					currentBlock.release(); //must release current block
			}
			else {
				newBlock=currentBlock;
				///Block occupied - giving up
			}

			//make sure customer doesn't move back into entrance block because only one customer allowed there at a time
			entrance = whereEntrance();
			if(newBlock.getX()==entrance.getX() && newBlock.getY()==entrance.getY()){
				return currentBlock;
			}

			//if in entrance update entrance moveCount
			if(currentBlock.getX()==entrance.getX() && currentBlock.getY()==entrance.getY()){
				entrance.incrMoveCount();
			}
			//if customer moved out of entrance release semaphore
			if(entrance.getMoveCount()==1 /*&& entrance.occupied()==false*/){
				entrance.decrMoveCount();
				mutex.release();
				return newBlock;
			}

		return newBlock;
	} 
	
	//called by customer to exit the shop
	public void leaveShop(GridBlock currentBlock)   {
		currentBlock.release();
	}

}


	

	

