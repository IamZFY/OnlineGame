package client;

import java.io.*;
import java.net.*;
import java.util.*;
import java.applet.*;
import globaldef.*;

public class SocketThread extends Thread
{
	private DataOutputStream os;
	private DataInputStream is;
	private MainFrame refMainFrame;

	private byte myClientId; //BYTE
	private Socket socket;

	private String host;
	
	public SocketThread(MainFrame ref, String aString)
	{
		refMainFrame =  ref;
		host = aString;
	}
	
	public void run()
	{
		//refMainFrame.debugInfo += "started";
		try
		{
			
			//refMainFrame.debugInfo += host;
			socket=new Socket(host,4700);
			os=new DataOutputStream(socket.getOutputStream());
			is=new DataInputStream(socket.getInputStream());
			
			int messageFromClient = 2;
			int leftOrRight = 2;
			int x = 10000;
			int y = 10000;
			int keyCode = 10000;
			byte id = 100; //BYTE
			int gunDegree = 10000;
						
			while(true)
			{
				// System.out.println("1");
				messageFromClient = is.readByte();
				// System.out.print("2");
				id = is.readByte(); //BYTE
				switch (messageFromClient)
				{
					case Global.CODE_MOUSE_CLICKED:
						leftOrRight = is.readByte();
						x = is.readShort();
						y = is.readShort();
						refMainFrame.setDist(x,y,id);
						break;
					case Global.CODE_KEY_TYPED: // Key typed
						keyCode = is.readInt();
						refMainFrame.setKey(keyCode,id);
						break;
					case Global.CODE_ASSIGN_ID: // Assign ID
						refMainFrame.stateInfo += "ID: " + id;
						myClientId = id;
						//refMainFrame.gameInfo = "Connected, waiting...";
						refMainFrame.gameState = GameState.CONNECTED;
						System.out.print("IDs" + id);
						break;
					case Global.CODE_POSITION_INFO: // Assign location and state
						x = is.readInt();
						y = is.readInt();
						gunDegree = is.readInt();
						if (myClientId == id)
						{
							refMainFrame.drawMe(id,x,y,gunDegree);
							System.out.println("My Id "+ myClientId + " POS " + id);							
						}
						else
						{
							if ((Global.TEAM_BATTLE)&&((id+(3*4*5)-myClientId)%Global.NUMBER_TEAM == 0))
							refMainFrame.newFriend(id,x,y,gunDegree);
							else
							refMainFrame.newChallenger(id,x,y,gunDegree);
							
							System.out.println("Other Id "+ myClientId + " POS " + id);
						}	
						break;
					case Global.CODE_START: //Start
						System.out.println("start");
						refMainFrame.startGame();
						break;
					case Global.CODE_DESTROY:
						byte targetId = is.readByte();
						refMainFrame.damage(targetId);
						break;
					case Global.CODE_SERVER_INFO:
						byte info = is.readByte();
						refMainFrame.infoFromServer(info);
						System.out.println("info " + info);
						break;
					case Global.CODE_DISCONNECTED:
							System.out.println("Disconnected id error");
							refMainFrame.playerQuit(id);
						break;
					case Global.CODE_SYNCHRONIZE:
						//int x,y, gunDegree;
						x = is.readShort();
						y = is.readShort();
						gunDegree =is.readInt();
						refMainFrame.synchronize(id,x,y,gunDegree);
						break;
					default:
						System.out.println("Wrong message ID: " +messageFromClient);
				}
			}
		}
		catch (Exception e)
		{
			System.out.println("Read error"+  e);
		}
	}

	public void disconnected()
	{
		try
		{
			synchronized(this)
			{
				os.writeByte(Global.CODE_DISCONNECTED);
				os.writeByte(myClientId); //BYTE
				socket.close();
			}
		}
		catch (Exception e)
		{
		}
		
	}

	public void setDist(int x, int y)
	{
		try
		{
			synchronized(this)
			{
				os.writeByte(Global.CODE_MOUSE_CLICKED);
				os.writeByte(myClientId); //BYTE
				os.writeByte(0);
				os.writeShort(x);
				os.writeShort(y);
			}
			// refMainFrame.debugInfo = "Mouse" + x + " " + y;
		}
		catch (Exception e)
		{}
	}

	public void setKey(int keyValue)
	{
		try
		{
			synchronized(this)
			{
			os.writeByte(Global.CODE_KEY_TYPED);
			os.writeByte(myClientId); //BYTE
			os.writeInt(keyValue);
			}
			// refMainFrame.debugInfo = "Key" + keyValue;
		}
		catch (Exception e)
		{}
	}

	public void destroyEnemy(int enemyId)
	{
		try
		{
			synchronized(this)
			{
				os.writeByte(Global.CODE_DESTROY);
				os.writeByte(myClientId); //BYTE
				os.writeByte(enemyId);
			}
			// refMainFrame.debugInfo = "Destroy" + enemyId;
		}
		catch (Exception e)
		{}
	}

	public void synchronizeMyPosition(PosInfo posInfo)
	{
		try
		{
			synchronized(this)
			{
				os.writeByte(Global.CODE_SYNCHRONIZE);
				os.writeByte(myClientId); //BYTE
				os.writeShort(posInfo.unitPosX);
				os.writeShort(posInfo.unitPosY);
				os.writeInt(posInfo.gunDegree);
			}
		}
		catch (Exception e)
		{}
	}

	public void newGame()
	{
		if(myClientId==Global.NUMBER_USER)
		{
			try
			{
				synchronized(this)
				{
					os.writeByte(Global.CODE_NEW_GAME);
					os.writeByte(myClientId); //BYTE
				}
			}
			catch (Exception e)
			{}
		}
	}
}
