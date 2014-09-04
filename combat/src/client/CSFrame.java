package client;
import java.awt.*;
import java.awt.event.*;

public class CSFrame extends Frame
{
	MainFrame refMainFrame = null;
	public CSFrame(MainFrame aRefMainFrame)
	{
		refMainFrame = aRefMainFrame;
		//super("CS -2D");
		addWindowListener(new WindowAdapter(){
	        public void windowClosing(WindowEvent evt)
	        {
	        	refMainFrame.destroy();
	            System.exit(1);
	        }
	      }
	    );
    }
}
