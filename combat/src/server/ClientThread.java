package server;
import java.io.*;
import java.net.*;
import java.util.*;
import globaldef.*;

//import multiser;
class ClientThread extends Thread{

	final static int MAX_NUMBER_OF_PLAYERS = 10;
	final int numberPlayers = 2;
	static byte gClientId = 0;
	byte clientId = 0;
	static UnitTable[] unitTable = new UnitTable[MAX_NUMBER_OF_PLAYERS];
	private final int dummyValue = 10000;
	
	GameServer pMultiServer_;
	Socket clientSocket_;
	DataInputStream is_;
	DataOutputStream os_;
	ThreadGroup threadGroup;
	
	boolean running = true;
	
	public ClientThread(ThreadGroup  myTGroup, GameServer myServer, Socket mySocket,int number)
	{
		gClientId ++;
		clientId = gClientId;
		this.pMultiServer_ = myServer;
		this.clientSocket_ = mySocket;
		threadGroup = myTGroup;
	}
	
	public void run()
	{
		Thread [] threads;
		int counterThreads = 0;
		try
		{
			is_ = new DataInputStream(new BufferedInputStream(clientSocket_.getInputStream())); 
			os_ = new DataOutputStream(clientSocket_.getOutputStream());
			// String messFromClient = null;
			// String messToClient = null;
			
			int messageFromClient = dummyValue;
			// assign dummy value
			int leftOrRight = dummyValue;
			byte sourceId = 100; //BYTE
			int x = dummyValue;
			int	y = dummyValue;
			int keyCode = dummyValue;
			byte targetId = 100;
			int gunDegree = 0;
			
			System.out.println("New client:" + clientId);

			// Initial clients table 
			createNewPlayerTable();

			//assign different ID
			sendIdToNewPlayer();

			sendServerInfo();
			
			while(true)
			{

			
			// Initial clients table 
			createNewPlayerTable();

			//assign different ID
			//sendIdToNewPlayer();

			//sendServerInfo();
			
			// Broadcast the current User;
			broadcastInfoAndStart();
			loop:
			while(running)
			{
			
				messageFromClient = is_.readByte();

				counterThreads=activeCount();
				System.out.println("Clientt " + clientId + " report:  total "+ counterThreads+" threads");
				threads = new Thread [counterThreads];
				enumerate(threads);

				switch (messageFromClient)
				{
					case Global.CODE_MOUSE_CLICKED:
						
						sourceId = is_.readByte();
						leftOrRight = is_.readByte();
						x = is_.readShort();
						y = is_.readShort();

						//pMultiServer_.appendText("Mouse clicked sId,x,y " + sourceId +" " + x + " " + y);
						//System.out.println("Loop client: " + clientId + "total "+ counterThreads+" threads");
						sendMouseClicked(messageFromClient,sourceId,x,y,leftOrRight);
						break;
					case Global.CODE_KEY_TYPED:
						//pMultiServer_.appendText("Key clicked");
						sourceId = is_.readByte();
						keyCode = is_.readInt();
						sendKeyClicked(messageFromClient,sourceId,keyCode);
						break;
					case Global.CODE_DESTROY:
						sourceId = is_.readByte();
						targetId = is_.readByte();
						pMultiServer_.appendText("Destroy " + targetId + " from " + sourceId + "to all");
						sendDestroy(messageFromClient,sourceId,targetId);
						break;
					case Global.CODE_DISCONNECTED:
						sourceId = is_.readByte();
						pMultiServer_.appendText("Disconnected from: " + sourceId);
						sendDisconnected(messageFromClient,sourceId);
						break;
					case Global.CODE_SYNCHRONIZE:
						sourceId = is_.readByte();
						x = is_.readShort();
						y = is_.readShort();
						gunDegree =is_.readInt();
						sendSynchronize(messageFromClient,sourceId,x,y,gunDegree);
						pMultiServer_.appendText("Sychronize from: " + sourceId);
						break;
					case Global.CODE_NEW_GAME:
						sourceId = is_.readByte();
						pMultiServer_.appendText("Restart game from: " + sourceId);
						break loop;
					default:
						pMultiServer_.appendText("Error message type" + messageFromClient);
						System.out.println("Error message type: " + messageFromClient);

				}
				
			}
		}
			
		}
		catch(Exception ee)
		{
		
			System.out.println("Will stop in 0 second!");
			pMultiServer_.appendText("Exception Disconnected from: " + clientId);
			sendDisconnected(Global.CODE_DISCONNECTED,clientId);

			//threadGroup.stop();
		}
		
		System.out.println("Client " + clientId + " stoped " );
		//threadGroup.stop();
	}

	public void createNewPlayerTable() throws Exception
	{
		
		if(clientId == Global.NUMBER_USER)
		{

			for (int i=0; i<Global.NUMBER_USER; i++)
			{
				System.out.println("Player [" + i + "] table created");
				unitTable[i] = new UnitTable();
				unitTable[i].id = i+1;
				unitTable[i].x = (int)(Math.random()*Global.WINDOW_WIDTH);
				unitTable[i].y = (int)(Math.random()*Global.WINDOW_HIGHT);
				while(unitTable[i].x>(150-8)&&unitTable[i].x<(330+16)&&unitTable[i].y>(180-8)&&unitTable[i].y<(400+16))
				{
					unitTable[i].x = (int)(Math.random()*Global.WINDOW_WIDTH);
					unitTable[i].y = (int)(Math.random()*Global.WINDOW_HIGHT);
				}
				unitTable[i].gunDegree = (int)(Math.random()*360);
			}
		}
				
	}

	public void sendIdToNewPlayer() throws Exception
	{
		System.out.println("Assign client ID " + clientId);
		os_.writeByte(Global.CODE_ASSIGN_ID);
		os_.writeByte(clientId);
	}

	public void broadcastInfoAndStart() throws Exception
	{

		Thread [] threads;
		int counterThreads = 0;
		
		//counterThreads=activeCount();
		//threads = new Thread [counterThreads];
		//enumerate(threads);

		// System.out.println("Before broadcast, Client " + clientId + "report: total " + counterThreads + "threads, till now.");
		// System.out.println("CID" + clientId + "NUMBER_USER" + Global.NUMBER_USER);
		if(clientId == Global.NUMBER_USER)
		{

			synchronized(pMultiServer_)
			{
			for (int i=0; i<Global.NUMBER_USER; i++)
			{
				for (int j=0; j < Global.NUMBER_USER; j ++)
					{
						synchronized(pMultiServer_)
						{
							System.out.println("Client " + clientId + " send to " + i + " about " + j);
							
							pMultiServer_.clientThreadTable[i].os_.writeByte(Global.CODE_POSITION_INFO);
							pMultiServer_.clientThreadTable[i].os_.writeByte((byte)(unitTable[j].id));
							pMultiServer_.clientThreadTable[i].os_.writeInt(unitTable[j].x);
							pMultiServer_.clientThreadTable[i].os_.writeInt(unitTable[j].y);
							pMultiServer_.clientThreadTable[i].os_.writeInt(unitTable[j].gunDegree);
						}
					}
			}
			}
		
			synchronized(pMultiServer_)
			{
			
			for (int i=0; i<Global.NUMBER_USER; i++)
			{
				
					System.out.println("START send to " + i);
					pMultiServer_.clientThreadTable[i].os_.writeByte(Global.CODE_START);
					pMultiServer_.clientThreadTable[i].os_.writeByte(0); // not used
			
			}
			}
		}
	}

	public void sendServerInfo() throws Exception
	{
		synchronized(pMultiServer_)
		{
			for (int i=0; i<(clientId); i++)
			{
					if ((null != pMultiServer_.clientThreadTable[i])&& (pMultiServer_.clientThreadTable[i].running))
					{
						System.out.println("Info " + clientId + " send to " + i);
						pMultiServer_.clientThreadTable[i].os_.writeByte(Global.CODE_SERVER_INFO);
						pMultiServer_.clientThreadTable[i].os_.writeByte(0); // not used
						pMultiServer_.clientThreadTable[i].os_.writeByte(clientId);
						
					}
			}
		}
	}

	public void sendMouseClicked(int messageFromClient, byte sourceId, int x, int y,int leftOrRight) throws Exception
	{
		synchronized(pMultiServer_)
		{
			for (int i=0; i<Global.NUMBER_USER; i++)
			{
					if (pMultiServer_.clientThreadTable[i].running)
					{
						pMultiServer_.clientThreadTable[i].os_.writeByte(messageFromClient);
						//System.out.println("Mouse clicked, from ID " + id + "to thread " + i);
						pMultiServer_.clientThreadTable[i].os_.writeByte(sourceId);
						pMultiServer_.clientThreadTable[i].os_.writeByte(leftOrRight);
						pMultiServer_.clientThreadTable[i].os_.writeShort(x);
						pMultiServer_.clientThreadTable[i].os_.writeShort(y);
					}
			}
		}
	}

	public void sendKeyClicked(int messageFromClient, byte sourceId, int keyCode) throws Exception
	{
		synchronized(pMultiServer_)
		{
			for (int i=0; i<Global.NUMBER_USER; i++)
			{
				if (pMultiServer_.clientThreadTable[i].running)
				{
					pMultiServer_.clientThreadTable[i].os_.writeByte(messageFromClient);
					//System.out.println("Key clicked, from ID" + id);
					pMultiServer_.clientThreadTable[i].os_.writeByte(sourceId);
					pMultiServer_.clientThreadTable[i].os_.writeInt(keyCode);
				}
			}
		}
	}

	public void sendDestroy(int messageFromClient, byte source, byte target) throws Exception
	{
		synchronized(pMultiServer_)
		{
			for (int i=0; i<Global.NUMBER_USER; i++)
			{
				if (pMultiServer_.clientThreadTable[i].running)
				{
					//System.out.println("Destroy, from ID" + sourceId);
					pMultiServer_.clientThreadTable[i].os_.writeByte(messageFromClient);
					pMultiServer_.clientThreadTable[i].os_.writeByte(source);
					pMultiServer_.clientThreadTable[i].os_.writeByte(target);
					//pMultiServer_.clientThreadTable[i].gClientId = 0;
					//pMultiServer_.clientThreadTable[i].stop(); // Stop all
				}
			}
		}
	}

	public void sendDisconnected(int messageFromClient, byte source)
	{
		System.out.println("Disconnect , from ID" + source);
		synchronized(pMultiServer_)
		{
				
			for (int i=0; i<Global.NUMBER_USER; i++)
			{
				try
				{
					if (pMultiServer_.clientThreadTable[i].running)
					{
						System.out.println("Disconnect, from ID" + source + "to table" + i);
						pMultiServer_.clientThreadTable[i].os_.writeByte(messageFromClient);
						pMultiServer_.clientThreadTable[i].os_.writeByte(source);
					}
					//pMultiServer_.clientThreadTable[i].gClientId = 0;
					//pMultiServer_.clientThreadTable[i].stop(); // Stop all
				}
				catch (Exception e)
				{
					System.out.println("ERR of: Disconnect, from ID" + source + "to table" + i);
					System.out.println(e);
				
				}
			}
		}
	}

	public void sendSynchronize(int messageFromClient, int sourceId, int x,int y,int gunDegree)
	{
		synchronized(pMultiServer_)
		{
				
			for (int i=0; i<Global.NUMBER_USER; i++)
			{
				try
				{
					if ((null != pMultiServer_.clientThreadTable[i])&&(pMultiServer_.clientThreadTable[i].running))
					{
						pMultiServer_.clientThreadTable[i].os_.writeByte(messageFromClient);
						pMultiServer_.clientThreadTable[i].os_.writeByte(sourceId);
						pMultiServer_.clientThreadTable[i].os_.writeShort(x);
						pMultiServer_.clientThreadTable[i].os_.writeShort(y);
						pMultiServer_.clientThreadTable[i].os_.writeInt(gunDegree);
						
					}
				}
				catch (Exception e)
				{
					System.out.println(e);
				
				}
			}
		}
	
	}
}
