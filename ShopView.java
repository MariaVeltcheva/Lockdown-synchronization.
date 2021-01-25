package socialDistanceShopSampleSolution;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.CountDownLatch;

import javax.swing.JButton;
import javax.swing.JPanel;

//threaded class to do the shop visualization

public class ShopView extends JPanel implements Runnable {
		public static volatile boolean done; //to signal when done
		private CustomerLocation[] customerLocations;
		private int noPeople;
		private int [][] exits;
		private int wIncr;
		private int hIncr;
		private int maxY;
		private int maxX;

		ShopGrid grid; //shared grid
		
		ShopView(CustomerLocation[] custs,  ShopGrid grid,int [][] exits) { //constructor
			this.customerLocations=custs; 
			noPeople = custs.length;
			done=false;
			this.grid = grid;
			this.exits=exits;
			this.maxY = grid.getMaxY();
		    this.maxX= grid.getMaxX();
		    int width = getWidth();
		    int height = getHeight();
		    wIncr= width/(maxX+2); //1 space on either side
		    hIncr= height/(maxY+2);//2 spaces on bottom
		}
		
		public void paintComponent(Graphics g) {
			
		    int width = getWidth();
		    int height = getHeight();
		    wIncr= width/(maxX+2); //1 space on either side
		    hIncr= height/(maxY+2);//2 spaces on bottom

		    g.clearRect(0,0,width,height);
		    g.setColor(Color.black);
		    
		    //draw grid lines
		    for (int i=1;i<=(maxX+1);i++)  //columns 
		    		g.drawLine(i*wIncr, 0, i*wIncr, maxY*hIncr); //- leave space at bottom
		    for (int i=0;i<=maxY;i++) //rows 
		    		g.drawLine(wIncr, i*hIncr, (maxX+1)*wIncr, i*hIncr); //- leave space at sides
		    
		  //highlight the entrance
		    g.setColor(Color.gray);
		    GridBlock entrance = grid.whereEntrance();
		    g.fillRect(entrance.getX()*wIncr+wIncr, entrance.getY()*hIncr, wIncr, hIncr);
		    g.drawString("Enter",entrance.getX()*wIncr+wIncr,entrance.getY()*hIncr+hIncr);
		    
		    //highlight the exit blocks
		
		    g.setFont(new Font("Helvetica", Font.BOLD, hIncr/2));
		    for (int e=0;e<exits.length;e++) {
		    	 	g.setColor(Color.pink);
		    		g.fillRect(exits[e][0]*wIncr+wIncr, exits[e][1]*hIncr, wIncr, hIncr);
		    		 g.setColor(Color.red);
		    		g.drawString("Exit",exits[e][0]*wIncr+wIncr,exits[e][1]*hIncr+hIncr);	
		    }
		    
		    		    
		    //draw and label Checkout counter
		    g.setColor(Color.lightGray);
		    g.fillRect(wIncr, (grid.checkout_y)*hIncr, wIncr*(maxX), hIncr*1);
		    g.setColor(Color.black);
		    g.setFont(new Font("Helvetica", Font.BOLD, hIncr));
		    g.drawString("Checkout",(maxX-1)*wIncr/2,grid.checkout_y*hIncr+hIncr);	
		    
		   //draw the ovals representing people in middle of grid block
			int x,y;
			 g.setFont(new Font("Helvetica", Font.BOLD, hIncr/2));
		    for (int i=0;i<noPeople;i++){	    	
		    		if (customerLocations[i].inRoom()) {
			    		g.setColor(customerLocations[i].getColor());
			    		x= (customerLocations[i].getX()+1)*wIncr;
			    		y= customerLocations[i].getY()*hIncr;
			    		g.fillOval(x+wIncr/4, y+hIncr/4 , wIncr/2, hIncr/2);
			    		g.drawString(customerLocations[i].getID()+"",x+wIncr/4, y+wIncr/4);
		    		}
		    		//else System.out.println("customer " + i+" waiting outside"); //debug
		    }
		   }
	
		public void run() {
			while (!done) {
				repaint();
			}
		}

	}


