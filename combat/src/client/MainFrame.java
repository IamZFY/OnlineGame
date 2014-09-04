package client;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import globaldef.*;

public class MainFrame extends java.applet.Applet implements Runnable
{

	private CombatUnit combatUnit = null;
	private CombatUnit[] enemyUnits = new CombatUnit[10];
	//private CombatUnit enemyUnit = null;

	public String debugInfo	=	"Debug";

	public String gameInfo	=	"Welcome";

	public String stateInfo	=	"State";

	public int serverInfo = 0;

	SocketThread socketThread;

	public int gameState;

	public int enemyUnitIndex = 0;

	public int nbKilled = 0;

	public int synchronizeCounteDown = 50;

	// public int barrierX,barrierY,barrierW,barrierH;

	///////////////////////////////////////////////////////
	//            Applet overide function                //
	///////////////////////////////////////////////////////
	
	/*
	Called when applet loaded
	*/
	
	public void init(String hostAddress)
	{

		gameState = GameState.IDLE;
		
		this.addMouseListener(new MouseListen(this));
		this.addKeyListener(new KeyListen(this));
		
		new Thread(this).start();

		//socketThread= new SocketThread(this,getDocumentBase().getHost());
		socketThread= new SocketThread(this,hostAddress);
		
		socketThread.start();
		
		setBackground(Color.black);
		validate();
	}

	/*
	Called when repaint
	*/
	public void paint(Graphics g)
	{
		if(combatUnit != null)
			combatUnit.paint(g);
		for (int i=0; i< Global.NUMBER_USER; i ++)
		{
			if(enemyUnits[i]!=null)
			{
				enemyUnits[i].paint(g);
			}
		}

		
		
		g.setColor(Color.red);
		g.drawString("V 1.10",10,15);
		
		//TimesRoman
		
		Font font = new Font("TimesRoman",Font.BOLD,20);
		g.setFont(font);
		
		switch (gameState)
		{
			case GameState.IDLE:
			g.drawString("Not connected, please retry.",80,(int)(Global.WINDOW_HIGHT/2));
			break;
			case GameState.CONNECTED:
			g.drawString("Waiting other(s), don't reload.",60,(int)(Global.WINDOW_HIGHT/2));
			g.drawString("       " + serverInfo + " Player loged on", 60, (int)(Global.WINDOW_HIGHT/2)+20);
			
			break;
			case GameState.GAME_RUNNING:

			g.setColor(Color.black);
			g.fillRect(150,180,180,220);
			g.setColor(Color.gray);
			g.drawRect(150,180,180,220);
			
			// Draw player life
			if (combatUnit != null)
			{
				g.setColor(Color.green);
				
				font = new Font("TimesRoman",Font.BOLD,10);
				g.setFont(font);
				g.drawString(" " +combatUnit.getId(),Global.WINDOW_WIDTH-45, 10);

				font = new Font("TimesRoman",Font.BOLD,20);
				g.setFont(font);
				for(int i = 0; i<combatUnit.life ;i++ )
				{	
					
					g.drawString(".", Global.WINDOW_WIDTH-15-i*4,10);
				}

				// Draw Weapen

				if (combatUnit.weapenType == 1)
				{	
					g.setColor(Color.red);
				}
				if (combatUnit.weapenType == 2)
				{
					g.setColor(Color.green);
				}
				g.drawString("o", Global.WINDOW_WIDTH-55,15);
			
			}

			// Draw enamys life
			for (int i = 0; i< Global.NUMBER_USER; i ++)
			{
				g.setColor(Color.red);
				//System.out.println("Enemy" + i);
				if(enemyUnits[i]!=null)
				{
					font = new Font("TimesRoman",Font.BOLD,10);
					g.setFont(font);
					g.drawString(" " +enemyUnits[i].getId(),Global.WINDOW_WIDTH-45, i*10+10);
					
					font = new Font("TimesRoman",Font.BOLD,20);
					g.setFont(font);
					
					for(int j = 0; j<enemyUnits[i].life ;j++ )
					{
						g.drawString(".", Global.WINDOW_WIDTH-15-j*4,10+i*10);

					}
				//System.out.println("life" + enemyUnits[i].life);
				}
			}
			
			break;
			case GameState.GAME_OVER:
			g.drawString(gameInfo,100,(int)(Global.WINDOW_HIGHT/2));
			break;
			default:
			break;
		}
				
	}

	/*
	Called when window is closed.
	*/
	public void destroy()
	{
		System.out.println("Destroyed");
		socketThread.disconnected();
	}

	/////////////////////////////////////////////////
	//              Thread main function           //
	/////////////////////////////////////////////////

	public void run()
	{
		PosInfo enemyPosInfo = null;
		PosInfo combatPosInfo = null;
		while(true)
		{

			try
			{
		    	Thread.sleep(40);
		    	if(gameState == GameState.GAME_RUNNING)
		    	synchronizeCounteDown--;
		    }
		    catch(InterruptedException e)
		    {}

		    if (combatUnit != null)
		    {
				combatUnit.updatePos();
				//combatPosInfo = combatUnit.getPos();
			}

			for (int i =0; i< Global.NUMBER_USER; i ++)
			{
				if(enemyUnits[i]!=null)
				{
					enemyUnits[i].updatePos();
					if (enemyUnits[i].getType() == 1)
					checkResult(combatUnit,enemyUnits[i]);
				}
			}

			repaint();
		 	if (combatUnit != null)
		    	{
			if (synchronizeCounteDown <= 0)
			{
				socketThread.synchronizeMyPosition(combatUnit.getPos());
				synchronizeCounteDown = 100;
			}
			}

		}
	}


	/////////////////////////////////////////////////////
	//                Message From Server              //
	/////////////////////////////////////////////////////
	
	public void setDist(int x, int y, int id)
	{
		if(id == combatUnit.getId())
			combatUnit.setDist(x,y);
		else
		{
			for (int i = 0; i< Global.NUMBER_USER; i ++)
			{
				if(enemyUnits[i]!=null && enemyUnits[i].getId() == id)
				{			
					enemyUnits[i].setDist(x,y);
				}
			}
		}
		synchronizeCounteDown--;
	}

	public void setKey(int keyValue, int id)
	{
		if(id == combatUnit.getId())
			combatUnit.setKey(keyValue);
		else
		{
			for (int i = 0; i< Global.NUMBER_USER; i ++)
			{
				if(enemyUnits[i]!=null && enemyUnits[i].getId() == id)
				{			
					enemyUnits[i].setKey(keyValue);
				}
			}
		}
		synchronizeCounteDown--;
	}

	/*
	Create player
	*/
	public void drawMe(int id,int x, int y, int gunDegree)
	{
			//System.out.println ( "drawMe");
			combatUnit = new CombatUnit(id,x,y,gunDegree,0);
			//life = combatUnit.life;
			//gameState = GameState.GAME_RUNNING;
	}

	/*
	Create enemy
	*/
	public void newChallenger(int challengerId,int x, int y, int gunDegree)
	{
		debugInfo = "Enemy ID: " + challengerId;
		//if(this.challengerId != challengerId)
		//{
		//	this.challengerId = challengerId;
			System.out.println("draw Enemy " + challengerId);
			enemyUnitIndex ++;
			enemyUnits[enemyUnitIndex] = new CombatUnit(challengerId,x,y,gunDegree,1);
		//}
	}

	public void newFriend(int friendId,int x, int y, int gunDegree)
	{
		debugInfo = "Friend ID: " + friendId;
		//if(this.challengerId != challengerId)
		//{
		//	this.challengerId = challengerId;
			//System.out.println("draw Enemy " + challengerId);
			enemyUnitIndex ++;
			enemyUnits[enemyUnitIndex] = new CombatUnit(friendId,x,y,gunDegree,2);
		//}
	}
	/*
	Lost the game
	*/
	public void damage(int targetId)
	{
		boolean gameContinue = true;

		if(targetId == combatUnit.getId())
		{
			if(combatUnit.destory())
			{
				setBackground(Color.lightGray);
				gameInfo = "      You Lose!!! "+targetId;
				System.out.println("Player " + targetId + " die");
				gameState = GameState.GAME_OVER;
				
			}
		}
		else
		{
			for (int i = 0; i< Global.NUMBER_USER; i ++)
			{
				if(enemyUnits[i]!=null && enemyUnits[i].getId() == targetId)
				{
					if(enemyUnits[i].destory())
					{
						gameContinue = false;
					}
				}
				
			}

			for (int i = 0; i< Global.NUMBER_USER; i ++)
			{
				if(enemyUnits[i]!=null)
				{
					if(enemyUnits[i].getType() == 1 & enemyUnits[i].alive)
					{
						//System.out.println("ID " + combatUnit.getId() + " find " + i);
						gameContinue = true;
					}
				}
			}
			
			if(gameContinue)
			{}
			else
			{
					setBackground(Color.blue);
					gameInfo = "      You Win!!! "+ combatUnit.getId();
					System.out.println("Player  " + combatUnit.getId() +  " win");
					gameState = GameState.GAME_OVER;
					//socketThread.newGame();
			}
		}
	}

	/*
	Enemy quit the game
	*/
	public void playerQuit(int id)
	{
		System.out.println("Player Quit" + gameInfo);
		if (gameState == GameState.GAME_RUNNING)
		{
			gameInfo = "Player " + id + " left, reload";
			System.out.println(gameInfo);
			setBackground(Color.gray);
			gameState = GameState.GAME_OVER;
		}
	}


	public void synchronize(int id, int x, int y,int gunDegree)
	{
		if(id == combatUnit.getId())
			combatUnit.synchronize(x,y,gunDegree);
		else
		{
			for (int i = 0; i< Global.NUMBER_USER; i ++)
			{
				if(enemyUnits[i]!=null && enemyUnits[i].getId() == id)
				{			
					enemyUnits[i].synchronize(x,y,gunDegree);
				}
			}
		}
	}

	// Game started
	public void startGame()
	{
		setBackground(Color.black);
		gameState = GameState.GAME_RUNNING;
		enemyUnitIndex = 0;
	}

	public void infoFromServer(byte info)
	{
		serverInfo = info;
	}
	
	/////////////////////////////////////////////////////
	//                Local operation                  //
	/////////////////////////////////////////////////////
	
	/*
	Check if the enemy has been destroied
	*/
	private void checkResult(CombatUnit me, CombatUnit enemy)
	{
		
		if (gameState == GameState.GAME_RUNNING)
		{
			if(me.checkResult(enemy))
			{
				
				socketThread.destroyEnemy((byte)(enemy.getId()));
			}
		}
	}

	///////////////////////////////////////////////////////
	//              main                                 //
	///////////////////////////////////////////////////////
	public static void main(String args[])
	{
		
		MainFrame gameWindow = new MainFrame();
		CSFrame f = new CSFrame(gameWindow);
		gameWindow.init(args[0]);
		f.add("Center",gameWindow);
		//gameWindow.start();
		f.setSize(Global.WINDOW_WIDTH+4,Global.WINDOW_HIGHT+15+4);
		f.show();
	}

}

class MouseListen implements MouseListener
{

	MainFrame frameRef;
	
	public MouseListen(MainFrame aRefOfMainFrame)
	{
		frameRef = aRefOfMainFrame;
	}

	public void mousePressed(MouseEvent mEvt)
	{
		//System.out.print("<MOU");
		if(frameRef.gameState == GameState.GAME_RUNNING)
		{	
			//System.out.println("SE>");
			int x, y;
			x = mEvt.getX();
			y = mEvt.getY();
			if (x>Global.WINDOW_WIDTH)
			x = Global.WINDOW_WIDTH;
			if (y>Global.WINDOW_HIGHT)
			y =Global.WINDOW_HIGHT;
		//	if (x>200&&x<300&&y>100&&y<300)
		//	{}
		//	else
		//	{
				frameRef.socketThread.setDist(x,y);
		//	}
			
			
			
		}
	}

	public void mouseReleased(MouseEvent m){};
	public void mouseEntered(MouseEvent m){};
	public void mouseExited(MouseEvent m){};
	public void mouseClicked(MouseEvent m){};
	
}
	
class KeyListen extends KeyAdapter
{	
	private int[] keyCode = new int[2];
	
	MainFrame frameRef;
	
	public KeyListen(MainFrame aRefOfMainFrame)
	{
		frameRef = aRefOfMainFrame;
	}

	public void keyPressed(KeyEvent kEvt)
	{
		if(frameRef.gameState == GameState.GAME_RUNNING)
		{
			keyCode[0] = keyCode[1];
			keyCode[1] = kEvt.getKeyCode();

			if ((keyCode[0]!=keyCode[1])||(keyCode[1]==KeyEvent.VK_UP))
			{
				frameRef.socketThread.setKey(keyCode[1]);
			}
		}
		if(frameRef.gameState == GameState.GAME_OVER)
		{
			if(kEvt.getKeyCode()==KeyEvent.VK_F2)
			{
				frameRef.socketThread.newGame();
			}
		}
	}
}
