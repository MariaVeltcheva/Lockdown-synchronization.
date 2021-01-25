package socialDistanceShopSampleSolution;
// the main class, starts all threads
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;


import java.util.Scanner;
import java.util.concurrent.*;
//model is separate from the view.
class outOfGridBoundsException extends Exception {
	   public outOfGridBoundsException(String msg){
	      super(msg);
	   }
	}
public class SocialDistancingShop {

	static int noPeople=20;
   	static int frameX=400;
	static int frameY=500;
	static int yLimit=400;
	static int gridX=10; //number of x grids in shop - default value if not provided on command line
	static int gridY=10; //number of y grids in shop - default value if not provided on command line
	static int max=5; //max number of customers - default value if not provided on command line

	
	static Customer[] customers; // array for customer threads
	static CustomerLocation [] customerLocations;  //array to keep track of where customers are
	static Inspector inspector;
	static volatile boolean done;  //must be volatile
	static PeopleCounter tallys; //counters for number of people inside and outside shop

	static ShopView shopView; //threaded panel to display shop
	static ShopGrid shopGrid; // shop grid
	static CounterDisplay counterDisplay ; //threaded display of counters
	private static volatile Object pauseLock = new Object();

	public static void setupGUI(int frameX,int frameY,int [][] exits) {
		// Frame initialize and dimensions
    	JFrame frame = new JFrame("Shop animation"); 
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	frame.setSize(frameX, frameY);
    	
      	JPanel g = new JPanel();
        g.setLayout(new BoxLayout(g, BoxLayout.PAGE_AXIS)); 
      	g.setSize(frameX,frameY);
 	    
		shopView = new ShopView(customerLocations, shopGrid, exits);
		shopView.setSize(frameX,frameY);
	    g.add(shopView);
	    
	    
	    //add all the counters to the panel
	    JPanel txt = new JPanel();
	    txt.setLayout(new BoxLayout(txt, BoxLayout.LINE_AXIS)); 
	    JLabel maxAllowed =new JLabel("Max: " + tallys.getMax() + "    ");
	    JLabel caught =new JLabel("Inside: " + tallys.getInside() + "    ");
	    JLabel missed =new JLabel("Waiting:" + tallys.getWaiting()+ "    ");
	    JLabel scr =new JLabel("Left shop:" + tallys.getLeft()+ "    ");    
	    txt.add(maxAllowed);
	    txt.add(caught);
	    txt.add(missed);
	    txt.add(scr);
	    g.add(txt);
	    counterDisplay = new CounterDisplay(caught, missed,scr,tallys);      //thread to update score
       
	    //Add start, pause and exit buttons
	    JPanel b = new JPanel();
        b.setLayout(new BoxLayout(b, BoxLayout.LINE_AXIS)); 
        JButton startB = new JButton("Start");
        
		// add the listener to the jbutton to handle the "pressed" event
		startB.addActionListener(new ActionListener() {
			boolean started=false;
		    public void actionPerformed(ActionEvent e)  {
		    	  	if (!started) {
		    	  		started=true;
			    	  	counterDisplay.done=false;  //NB fix
			    	  	shopView.done=false;
			    	  	Customer.done=false;
			    	  	Inspector.done=false;
			    	  	done =false;
			    		startThreads(); //start threads running
		    	  	}
		    }
		   });
			
			final JButton pauseB = new JButton("Pause ");;
			
			// add the listener to the jbutton to handle the "pressed" event
			pauseB.addActionListener(new ActionListener() {
		      public void actionPerformed(ActionEvent e) {
		    	  	if (Customer.pause==false) {
		    	  		// ask threads to pause
		    	  		Customer.pause = true; //pause people
		    	  		Inspector.pause =true; //pause inspector
		    	  		pauseB.setText("Resume");
		    	  	}
		    	  	else {
		    	  		synchronized(pauseLock) {
			    	  		Customer.pause = false;
			    	  		Inspector.pause = false;
		    	  			pauseLock.notifyAll();
		    	  			pauseB.setText("Pause");
		    	  		}
		    	  	}
		      }
		    });
		JButton endB = new JButton("Quit");
				// add the listener to the jbutton to handle the "pressed" event
				endB.addActionListener(new ActionListener() {
			      public void actionPerformed(ActionEvent e) {
			    	  	// ask threads to stop
			    	  	Customer.done = true; //stop people moving
			    	  	counterDisplay.done=true; //stop counter thread 
			    	  	Inspector.done=true;
			    	  	shopView.repaint();
			    	  	shopView.done=true; //stop animator thread
			    	  	System.exit(0);
			      }
			    });

		b.add(startB);
		b.add(pauseB);
		b.add(endB);
		
		g.add(b);
    	
      	frame.setLocationRelativeTo(null);  // Center window on screen.
      	frame.add(g); //add contents to window
        frame.setContentPane(g);     
        frame.setVisible(true);	
	}
	
	
	//start all threads running - called by actionListener on Start button
	public static void startThreads() {
      	Thread t = new Thread(shopView); 
      	t.start();
      	//Start counter thread - for updating counters
      	Thread s = new Thread(counterDisplay);  
      	s.start();
      	inspector.start(); //start the inspector thread
      	for (int i=0;i<noPeople;i++) {
			customers[i].start();
		}
      	

	}
	

	public static void main(String[] args) throws InterruptedException {
		
		//deal with command line arguments if provided
		if (args.length==4) {
			noPeople=Integer.parseInt(args[0]);  //total people to enter room
			gridX=Integer.parseInt(args[1]); // No. of X grid cells  
			gridY=Integer.parseInt(args[2]); // No. of Y grid cells  
			max=Integer.parseInt(args[3]); // max people allowed in shop
		}
		//hardcoded exit doors
		int [][] exits = {{0,(int) gridY-1},
							{0,(int) gridY-2}};  //two-cell wide door on left
		
		shopGrid = new ShopGrid(gridX, gridY, exits, max); //setup shop with size and exitsand maximum limit for people    
	    Customer.shop = shopGrid; //grid shared with class
	    Customer.pauseLock = pauseLock; //create shared lock to allow all treads to pause
	    Inspector.pauseLock = pauseLock; 
	    
	    tallys = new PeopleCounter(max); //counters for people inside and outside shop
	    
	    customerLocations = new CustomerLocation[noPeople];
		customers = new Customer[noPeople];
		
        for (int i=0;i<noPeople;i++) {
        		customerLocations[i]=new CustomerLocation(i);
    			customers[i] = new Customer(i,tallys,customerLocations[i]);
    		}
		   
        inspector = new Inspector(tallys,customerLocations);
        
		setupGUI(frameX, frameY,exits);  //Start Panel thread - for drawing animation

	}

}
