package socialDistanceShopSampleSolution;

import java.awt.Color;

import javax.swing.JLabel;

//class to update the counter display
public class CounterDisplay  implements Runnable {
	private PeopleCounter score;
	JLabel waiting;
	JLabel inside;
	JLabel left;
	public static  volatile boolean done=false;
	
	CounterDisplay(JLabel w, JLabel i, JLabel l, PeopleCounter score) {
        this.waiting=w;
        this.inside = i;
        this.left = l;
        this.score=score;
    }
	
	public void run() { //this thread just updates the display of the counters
        while (!done) {
        	if (score.getMax()<score.getInside()) {
        		inside.setForeground(Color.RED);
        	}
        	else if (score.getMax()==score.getInside()) {
        		inside.setForeground(Color.ORANGE);
        	}
        	else inside.setForeground(Color.BLACK);
        	inside.setText("Inside: " + score.getInside() + "    "); 
            waiting.setText("Waiting:" +  score.getWaiting()+ "    " );
            left.setText("Left shop:" + score.getLeft()+ "    " ); 
        }
    }
}
