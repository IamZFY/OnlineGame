package server;

import java.io.*;
import java.net.*;
import java.util.*;
import globaldef.Global;
class GameServer implements Runnable{
	
	//class variable definition
	static InfoWin infoWindow;
	Thread mainServerThread_ = null;
	ThreadGroup clientThreadGroup_;
 	ServerSocket mainServer_=null;

 	ClientThread[] clientThreadTable = new ClientThread[Global.NUMBER_USER];
	
	//function definition
	public void start()
	{
		infoWindow = new InfoWin();
		infoWindow.setSize(500,300);
		infoWindow.infoBox.append("Game Server is running." + "\n");
		infoWindow.show();
		clientThreadGroup_ = new ThreadGroup("MyThreadGroup");
		mainServerThread_ = new Thread (this);
		mainServerThread_.start();
	}
	
	/*public void stop()
	{
		mainServerThread_.stop();
		mainServerThread_ = null;
		clientThreadGroup_.stop();
	}
	*/
	public void run()
	{
		try
		{
			mainServer_=new ServerSocket(4700);
		}
		catch(Exception e)
		{
			System.out.println("Can not create main server:"+e);
		}
		
		Socket mainSocket=null;
		
		try
		{
			int i = 0;
			do
			{
				if (i == 0)
				clientThreadTable[i].gClientId = 0;
				mainSocket=mainServer_.accept();
				
				System.out.println(mainSocket);
				clientThreadTable[i] = new  ClientThread(clientThreadGroup_,this,mainSocket,1);
				clientThreadTable[i].start();
				i++;
				
				if (i == Global.NUMBER_USER)
				i = 0;
				
				
			}while(true);
		}
		catch(Exception e)
		{
			System.out.println("Can not create main socket"+e);
		}
	}

	public synchronized void appendText(String message)
	{
   		infoWindow.infoBox.append(message+"\n");
	}

	public static void main(String[] args)
	{
		GameServer gameServer = new GameServer();
		gameServer.start();
	}
}

