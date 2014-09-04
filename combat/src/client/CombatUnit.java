package client;
import java.awt.*;
import java.awt.event.*;

public class CombatUnit{

	public String DebugInfo = "";

	private int type;

	boolean alive = true;

	private int unitSize = 16;

	private int myId = 1000;

	//Scope
	public int radarDegree = 0;
	private int radarSpeed = -2; // do not remove "-"
	private int radarLength = 200;
	private final int scopeLength = 160;

	//Position
	private int oldX = 0;
	private int oldY = 0;

	private int lunchX = 0;
	private int lunchY = 0;
	
	private int newX = 0;
	private int newY = 0;

	private double ratX = 0;
	private double ratY = 0;

	private PosInfo posInfo = new PosInfo();

	private int stepCounter = 0;

	//Weapon
	public int weapenType = 1;
	public final int gunLength = 20;
	private int gunDegree = 90;
	private int gunSpeed = 1;
	private int gunMethod = KeyEvent.VK_DOWN;
	
	//Bullet
	private static final int MAX_NB_BULLETS = 10;
	private int nbBullets = 7;
	private Bullet[] bullets = new Bullet[MAX_NB_BULLETS];
	private int bulletIndex = 0;

	//Score
	private int nbWin;
	private int nbLose;

	public int life = 5;
	
	public int damageCountDown = 0;
	
	public CombatUnit(int aMyId, int x, int y, int degree, int type)
	{
		myId = aMyId;
		oldX=newX=x;
		oldY=newY=y;
		gunDegree = degree;
		life = 5;
		for (int i =0; i< MAX_NB_BULLETS; i++)
		{
			bullets[i] = new Bullet();
		}
		this.type = type;
	}

	public void synchronize(int x, int y, int degree)
	{

		if (Math.abs(oldX - x) > 3)
		{
			oldX=x;
			oldY=y;
		}
		if (Math.abs(gunDegree - degree) > 5)
		gunDegree = degree;
	}

	public void setDist(int x, int y)
	{
		stepCounter = 0;
		lunchX = oldX;
		lunchY = oldY;
		newX = x;
		newY = y;
		calcRat();
	}

	public void setKey(int keyValue)
	{
		switch (keyValue)
		{
			case KeyEvent.VK_1:
				weapenType = 1;
				nbBullets = 7;
				bulletIndex = 0;
				for (int i = nbBullets; i<MAX_NB_BULLETS; i++)
				{
					bullets[i].remove();
				}
				break;
			case KeyEvent.VK_2:
				weapenType = 2;
				nbBullets = 2;
				bulletIndex = 0;
				for (int i = nbBullets; i<MAX_NB_BULLETS; i++)
				{
					bullets[i].remove();
				}
				break;
			default:
				gunMethod = keyValue;
				break;
		}
	}

	public PosInfo getPos()
	{
		posInfo.unitPosX = oldX;
		posInfo.unitPosY = oldY;
		//posInfo.bulletPosX = bullet.getX();
		//posInfo.bulletPosY = bullet.getY();
		posInfo.gunDegree = gunDegree;
		return posInfo;
	}

	public int getId()
	{
		return myId;
	}

	public int getType()
	{
		return type;
	}
		
	public boolean updatePos()
	{
		boolean needUpdate = false;
		if(alive == true)
		{
			
			if(Math.abs(newX-oldX)> 3 || Math.abs(newY-oldY)> 3)
			{
			
				//System.out.print("XY" + ratX + " " + ratY);
				stepCounter ++;
				int tX, tY;
				
				tX = (int)(lunchX + ratX * (2 * stepCounter));
				tY = (int)(lunchY + ratY * (2 * stepCounter));
				
				if (tX>(150-8)&&tX<(330+16)&&tY>(180-8)&&tY<(400+16))
				{
					stepCounter --;//150,180,180,220
				}
				else
				{
					oldX = tX;
					oldY = tY;
				}
				
				needUpdate = true;
				
			}
			else
			{
			
				if (oldX!=newX||oldY!=newY)
				{
					if (newX>200&&newX<300&&newY>100&&newY<300)
					{
					
					}
					else
					{
						oldX = newX;
						oldY = newY;
						needUpdate = true;
					}
				}
			}
			needUpdate = updateGun()||needUpdate;

			
		}
		
		return needUpdate;
	}

	public boolean updateGun()
	{
		boolean needUpdate = true;
		switch (gunMethod)
		{
			case KeyEvent.VK_LEFT:
			gunDegree +=gunSpeed;
			break;
			case KeyEvent.VK_RIGHT:
			gunDegree -=gunSpeed;
			break;
			case KeyEvent.VK_UP:
			bullets[bulletIndex].launch(oldX, oldY, gunDegree, weapenType);
			bulletIndex ++;
			
			if (bulletIndex >= nbBullets)
			{
				bulletIndex = 0;
			}
			
			gunMethod = KeyEvent.VK_DOWN;
			break;
			default:
			needUpdate = false;
			break;
		}
		for (int i = 0; i<nbBullets; i++)
		{
			bullets[i].updatePos();
		}
		return needUpdate;
	}

	public boolean checkResult(CombatUnit enemyRef)
	{
		boolean returnValue = false;
		if (enemyRef.alive)
		{
			for (int i = 0; i<nbBullets; i++)
			{
				if(bullets[i].checkResult(enemyRef))
				{
					returnValue = true;
					break;
				}
			}
		}
		return returnValue;
	}

	public boolean destory()
	{
		if(alive)
		{
			//nbLose ++;
			damageCountDown = 3;
			life --;
			if(life == 0)
			{
				alive = false;
				return true;
			}
		}
		return false;
		
	}

	public void win()
	{
		//nbWin ++;
	}

	
	public void paint(Graphics g)
	{
		if(alive)
		{
		if (type == 0)
		{
		// Draw scope
		g.setColor(Color.gray);

		g.fillArc(oldX-radarLength,oldY-radarLength,radarLength*2,radarLength*2,(gunDegree-40-90 + radarDegree)%360,50);
		
		if ((radarDegree == 30)||(radarDegree == 0))
		{
			radarSpeed = -1*radarSpeed;
		}
		radarDegree += radarSpeed;

		g.setColor(Color.orange);
		
		//g.drawLine(oldX,oldY,oldX+(int)(gunLength*Math.sin(Math.toRadians(gunDegree+40))*10) , oldY+(int)(gunLength*Math.cos(Math.toRadians(gunDegree+40))*10));
		//g.drawLine(oldX,oldY,oldX+(int)(gunLength*Math.sin(Math.toRadians(gunDegree-40))*10) , oldY+(int)(gunLength*Math.cos(Math.toRadians(gunDegree-40))*10));
		g.drawLine(oldX,oldY,
		           oldX+(int)(gunLength*Math.sin(Math.toRadians(gunDegree))*(8.5)),
		           oldY+(int)(gunLength*Math.cos(Math.toRadians(gunDegree))*(8.5))
		           );

		g.drawArc(oldX-scopeLength,oldY-scopeLength,scopeLength*2,scopeLength*2,(gunDegree-4-90)%360,8);
		
		g.setColor(Color.green);
		}
		else
		{
			if (type == 2)
			g.setColor(Color.green);
			if (type == 1)
			g.setColor(Color.black);
		}
		
		// Draw unit
		if (damageCountDown > 0)
		{
			g.setColor(Color.red);
			g.fillOval(oldX-(unitSize/2),oldY-(unitSize/2),unitSize,unitSize);
			damageCountDown --;
		}
		else
		{
			g.drawOval(oldX-(unitSize/2),oldY-(unitSize/2),unitSize,unitSize);
		}
		//g.fillOval(oldX-(unitSize/4),oldY-(unitSize/4),unitSize/2,unitSize/2);
		
		g.drawLine(oldX,oldY,oldX+(int)(gunLength*Math.sin(Math.toRadians(gunDegree))) , oldY+(int)(gunLength*Math.cos(Math.toRadians(gunDegree))));

		//g.drawString("O",oldX,oldY);
		// Draw bullets
		for (int i =0; i<nbBullets; i++)
		{
			bullets[i].paint(g);
		}
		}
		else
		{
			if (type == 0)
			{
			g.setColor(Color.gray);

			g.fillArc(oldX-radarLength,oldY-radarLength,radarLength*2,radarLength*2,(gunDegree-40-90 + radarDegree)%360,50);

			g.fillArc(oldX-radarLength,oldY-radarLength,radarLength*2,radarLength*2,(gunDegree-40-90 + radarDegree)%360,50);
			
			//if ((radarDegree == 30)||(radarDegree == 0))
			//{
			//	radarSpeed = -1*radarSpeed;
			//}
			radarDegree = 0;
			
			if(radarLength >5)
			{
				radarLength -= 2;
			}
			else
			{
				radarLength = 5;
				g.fillArc(oldX-radarLength-5,oldY-radarLength-3,radarLength*2,radarLength*2,(gunDegree-40-30 + radarDegree)%360,50);
				g.fillArc(oldX-radarLength-5,oldY-radarLength+2,radarLength*2,radarLength*2,(gunDegree-40-120 + radarDegree)%360,50);
			
			
			}

			g.setColor(Color.green);
			}
			else
			{
				if(type == 2)
				g.setColor(Color.green);
				if(type == 1)
				g.setColor(Color.black);
			}
			
			g.drawOval(oldX-(unitSize/2),oldY-(unitSize/2),unitSize,unitSize);
			if(unitSize >1)
			{
				unitSize -= 1;
			}
			else
			{
				unitSize = 0;
			}

		}
		
	}

	private void calcRat()
	{
		double l;
		
		l = Math.sqrt((newX-oldX)*(newX-oldX) + (newY-oldY)*(newY-oldY));
		if(l!=0)
		{
			ratX = (newX - oldX)/l;
			ratY = (newY - oldY)/l;
		}
	
	}
}
