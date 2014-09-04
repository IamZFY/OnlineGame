package client;
import java.awt.*;
//port java.awt.applet.*
public class Bullet
{
	final int dummyValue = 10000;
	int lunchPosX;
	int lunchPosY;
	int posX;
	int posY;
	int rat;
	int counter = dummyValue;
	int bulletSpeed = 3;
	Color bulletColor;
	int counteBulletLength = 75;
	
	public void launch(int x, int y, int ratio, int weapenType)
	{
		//if (counter != dummyValue)
		//{}
		//else
		if (weapenType == 1)
		{
			bulletSpeed = 3;
			bulletColor = Color.red;
			counteBulletLength = 75;
		}
		if (weapenType == 2)
		{
			bulletSpeed = 6;
			bulletColor = Color.green;
			counteBulletLength = 37;
		}

		lunchPosX = posX = x;
		lunchPosY = posY = y;
		rat = ratio;
		counter = 0;
		
	}

	public void remove()
	{
		counter = dummyValue;
		posX = posY = dummyValue;
	}

	public boolean checkResult(CombatUnit enemyRef)
	{
		if((Math.abs(posX - (enemyRef.getPos()).unitPosX)<9) &&  (Math.abs(posY - (enemyRef.getPos()).unitPosY)< 9))
		{
			posX = dummyValue;
			posY = dummyValue;
			counter = dummyValue;
			return true;
		}
		else
			return false;
	}
	
	public void updatePos()
	{
		if (counter < counteBulletLength)
		{
			posX = lunchPosX + (int)( bulletSpeed * counter * Math.sin(Math.toRadians(rat)));
			posY = lunchPosY + (int)( bulletSpeed * counter * Math.cos(Math.toRadians(rat)));
			counter ++;
			if(posX>150&&posX<330&&posY>180&&posY<400)
			
			{
				counter = dummyValue;
				posX = posY = dummyValue;
			}
		}
		else
		{
			counter = dummyValue;
			posX = posY = dummyValue;
		}
	}

	public int getX()
	{
		return posX;
	}
	
	public int getY()
	{
		return posY;
	}

	public void paint(Graphics g)
	{
		if (counter != dummyValue)
		{
			g.setColor(bulletColor);
			g.drawOval(posX-2,posY-2,4,4);
		}
		else
		{}
		
	}
}
