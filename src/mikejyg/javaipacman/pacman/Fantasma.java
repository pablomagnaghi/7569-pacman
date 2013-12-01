/**
 * Copyright (C) 1997-2010 Junyang Gu <mikejyg@gmail.com>
 * 
 * This file is part of javaiPacman.
 *
 * javaiPacman is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * javaiPacman is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with javaiPacman.  If not, see <http://www.gnu.org/licenses/>.
 */

package mikejyg.javaipacman.pacman;

import java.lang.Error;
import java.util.ArrayList;
import java.awt.*;

import javax.crypto.spec.PSource;

public class Fantasma
{
	final int IN=0;
	final int OUT=1;
	final int BLIND=2;
	final int EYE=3;

	final int[] steps=	{7, 7, 1, 1};
	final int[] frames=	{8, 8, 2, 1};

	final int INIT_BLIND_COUNT=600;	// remain blind for ??? frames
	int blindCount;

	cspeed speed=new cspeed();

	int posX, posY, direccion, iStatus;
	int iBlink, iBlindCount;

	// random calculation factors
	final int DIR_FACTOR=2;
	final int POS_FACTOR=10;

	// the applet this object is associated to
	Window applet;
	Graphics graphics;

	// the maze the ghosts knows
	Laberinto laberinto;

	// the ghost image
	Image imageGhost; 
	Image imageBlind;
	Image imageEye;
	Color color;

	Fantasma(Window a, Graphics g, Laberinto m, Color color)
	{
		applet=a;
		graphics=g;
		laberinto=m;
		this.color = color;
		imageGhost=applet.createImage(18,18);
		cimage.drawGhost(imageGhost, 0, color);

		imageBlind=applet.createImage(18,18);
		cimage.drawGhost(imageBlind,1, Color.white);

		imageEye=applet.createImage(18,18);
		cimage.drawGhost(imageEye,2, Color.lightGray);
	}

	public void start(int initialPosition, int round)
	{
		if (initialPosition>=2)
			initialPosition++;
		posX=(8+initialPosition)*16; posY=8*16;
		direccion=3;
		iStatus=IN;

		blindCount=INIT_BLIND_COUNT/((round+1)/2);

		speed.start(steps[iStatus], frames[iStatus]);
	}

	public void draw()
	{
		laberinto.DrawDot(posX/16, posY/16);
		laberinto.DrawDot(posX/16+(posX%16>0?1:0), posY/16+(posY%16>0?1:0));

		if (iStatus==BLIND && iBlink==1 && iBlindCount%32<16)
			graphics.drawImage(imageGhost, posX-1, posY-1, applet);
		else if (iStatus==OUT || iStatus==IN)
			graphics.drawImage(imageGhost, posX-1, posY-1, applet);
		else if (iStatus==BLIND)
			graphics.drawImage(imageBlind, posX-1, posY-1, applet);
		else 
			graphics.drawImage(imageEye, posX-1, posY-1, applet);
	}  

	public void move(int iPacX, int iPacY, int iPacDir)
	{
		if (iStatus==BLIND)
		{
			iBlindCount--;
			if (iBlindCount<blindCount/3)
				iBlink=1;
			if (iBlindCount==0)
				iStatus=OUT;
			if (iBlindCount%2==1)	// blind moves at 1/2 speed
			return;
		}

		if (speed.isMove()==0)
			// no move
			return;

		if (posX%16==0 && posY%16==0)
			// determine direction
		{
			switch (iStatus)
			{
			case IN:
				direccion=INSelect();
				break;
			case OUT:
				direccion=moverFantasmaFueraDeJaula(iPacX, iPacY, iPacDir);
				break;
			case BLIND:
				direccion=BLINDSelect(iPacX, iPacY, iPacDir);
				break;
			case EYE:
				direccion=EYESelect();
			}
		}

		if (iStatus!=EYE)
		{
			posX+= LaberintoUtils.direccionesEjeX[direccion];
			posY+= LaberintoUtils.direccionesEjeY[direccion];
		}
		else
		{	
			posX+=2* LaberintoUtils.direccionesEjeX[direccion];
			posY+=2* LaberintoUtils.direccionesEjeY[direccion];
		}

	}

	public int INSelect()
	// count available directions
	throws Error
	{
		int iM,i,iRand;
		int iDirTotal=0;

		for (i=0; i<4; i++)
		{
			iM=laberinto.laberinto[posY/16 + LaberintoUtils.direccionesEjeY[i]]
			              [posX/16 + LaberintoUtils.direccionesEjeX[i]];
			if (iM!=Laberinto.WALL && i != LaberintoUtils.iBack[direccion] )
			{
				iDirTotal++;
			}
		}
		// randomly select a direction
		if (iDirTotal!=0)
		{
			iRand=Utils.RandSelect(iDirTotal);
			if (iRand>=iDirTotal)
				throw new Error("iRand out of range");
			//				exit(2);
			for (i=0; i<4; i++)
			{
				iM=laberinto.laberinto[posY/16+ LaberintoUtils.direccionesEjeY[i]]
				              [posX/16+ LaberintoUtils.direccionesEjeX[i]];
				if (iM!=Laberinto.WALL && i != LaberintoUtils.iBack[direccion] )
				{
					iRand--;
					if (iRand<0)
						// the right selection
					{
						if (iM== Laberinto.DOOR)
							iStatus=OUT;
						direccion=i;	break;
					}
				}
			}
		}	
		return(direccion);	
	}

	public ArrayList<Integer> posiblesMovimientos(int pacmanPosX, int pacmanPosY, int pacmanDireccion){
		int celda,i;
		ArrayList<Integer> posibilidad = new ArrayList<Integer>();

		for (i=0; i<4; i++)
		{
			celda=laberinto.laberinto[posY/16 + LaberintoUtils.direccionesEjeY[i]]
			              [posX/16+ LaberintoUtils.direccionesEjeX[i]];
			
			if (celda!=Laberinto.WALL && i!= LaberintoUtils.iBack[direccion] && celda!= Laberinto.DOOR )
			{
				posibilidad.add(i);
			}
		}
		return posibilidad;
	}
	
	public int moverFantasmaFueraDeJaula(int pacmanPosX, int pacmanPosY, int pacmanDireccion)
	// count available directions
	throws Error
	{
		ArrayList<Integer> posibilidad = posiblesMovimientos(pacmanPosX, pacmanPosY, pacmanDireccion);
		if (posibilidad.size()>0){
			if (LaberintoUtils.politica == 1){
				Integer distanciaMin = 9999;
				Integer min = 0;		
				for (Integer i : posibilidad) {
					Integer distanciaAPacman = this.distanciaAPacman(pacmanPosX, pacmanPosY, i);
					if (distanciaAPacman < distanciaMin){
						min = i;
						distanciaMin = distanciaAPacman;
					}
				}
				direccion = min;
			}
			else if (LaberintoUtils.politica == 2){
				direccion = posibilidad.get(0);
			} else if (LaberintoUtils.politica == 3){
				int iRand= Utils.RandSelect(posibilidad.size());
				System.out.println("posi " + posibilidad.size());
				System.out.println("Rand " + iRand);
				direccion = posibilidad.get(iRand);
			} else if (LaberintoUtils.politica == 4){
				if (this.color==Color.red || this.color == Color.blue){
					direccion = posibilidad.get(0);
				} else {
					if (posibilidad.size()>1){
						direccion = posibilidad.get(1);
					} else {
						direccion = posibilidad.get(0);
					}
				}
			} 

		}
		else	
			throw new Error("iDirTotal out of range");
		return(direccion);
	}
	
	public Integer distanciaAPacman(Integer pacmanX, Integer pacmanY, Integer direccion){
		Integer nextX = posX + LaberintoUtils.direccionesEjeX[direccion];
		Integer nextY = posY + LaberintoUtils.direccionesEjeY[direccion];
		Integer distanciaX = Math.abs(pacmanX - nextX);
		Integer distanciaY = Math.abs(pacmanY - nextY);
		return distanciaX + distanciaY;
	}

	public void blind()
	{
		if (iStatus==BLIND || iStatus==OUT)
		{
			iStatus=BLIND;
			iBlindCount=blindCount;
			iBlink=0;
			// reverse
			if (posX%16!=0 || posY%16!=0)
			{
				direccion= LaberintoUtils.iBack[direccion];
				// a special condition:
				// when ghost is leaving home, it can not go back
				// while becoming blind
				int iM;
				iM=laberinto.laberinto[posY/16+ LaberintoUtils.direccionesEjeY[direccion]]
				              [posX/16+ LaberintoUtils.direccionesEjeX[direccion]];
				if (iM == Laberinto.DOOR)
					direccion=LaberintoUtils.iBack[direccion];
			}
		}
	}

	public int EYESelect()
	// count available directions
	throws Error
	{
		int iM,i,iRand;
		int iDirTotal=0;
		int [] iDirCount= new int [4];

		for (i=0; i<4; i++)
		{
			iDirCount[i]=0;
			iM=laberinto.laberinto[posY/16 + LaberintoUtils.direccionesEjeY[i]]
			              [posX/16+LaberintoUtils.direccionesEjeX[i]];
			if (iM!= Laberinto.WALL && i!= LaberintoUtils.iBack[direccion])
			{
				iDirCount[i]++;
				switch (i)
				{
				// door position 10,6
				case 0:	// right
					iDirCount[i]+=160>posX?
							POS_FACTOR:0;
					break;
				case 1:	// up
					iDirCount[i]+=96<posY?
							POS_FACTOR:0;
					break;
				case 2:	// left
					iDirCount[i]+=160<posX?
							POS_FACTOR:0;
					break;
				case 3:	// down
					iDirCount[i]+=96>posY?
							POS_FACTOR:0;
					break;
				}
				iDirTotal+=iDirCount[i];
			}	
		}
		// randomly select a direction
		if (iDirTotal!=0)
		{
			iRand= Utils.RandSelect(iDirTotal);
			if (iRand>=iDirTotal)
				throw new Error("RandSelect out of range");
			//				exit(2);
			for (i=0; i<4; i++)
			{
				iM=laberinto.laberinto[posY/16+ LaberintoUtils.direccionesEjeY[i]]
				              [posX/16+ LaberintoUtils.direccionesEjeX[i]];
				if (iM!= Laberinto.WALL && i!= LaberintoUtils.iBack[direccion])
				{
					iRand-=iDirCount[i];
					if (iRand<0)
						// the right selection
					{
						if (iM== Laberinto.DOOR)
							iStatus=IN;
						direccion=i;	break;
					}
				}
			}
		}
		else
			throw new Error("iDirTotal out of range");
		return(direccion);	
	}	

	public int BLINDSelect(int pacmanPosX, int pacmanPosY, int pacmanDireccion)
	// count available directions
	throws Error
	{
		int iM,i,iRand;
		int iDirTotal=0;
		int [] iDirCount = new int [4];

		for (i=0; i<4; i++)
		{
			iDirCount[i]=0;
			iM=laberinto.laberinto[posY/16+ LaberintoUtils.direccionesEjeY[i]][posX/16+ LaberintoUtils.direccionesEjeX[i]];
			if (iM != Laberinto.WALL && i != LaberintoUtils.iBack[direccion] && iM != Laberinto.DOOR)
				// door is not accessible for OUT
			{
				iDirCount[i]++;
				iDirCount[i]+=direccion==pacmanDireccion?
						DIR_FACTOR:0;
				switch (i)
				{
				case 0:	
					/*
					 * Si el fantasma esta mas a la derecha que el pacman, apoyamos ese movimiento,
					 * sino no (va hacia el pacman)
					 */
					iDirCount[i]+=pacmanPosX<posX?
							POS_FACTOR:0;
					break;
				case 1:	// up
					/*
					 * si la posicion del pacman esta mas abajo que el fantama, me muevo p arriba
					 */
					iDirCount[i]+=pacmanPosY>posY?
							POS_FACTOR:0;
					break;
				case 2:	// left
					/*
					 * si la posicion del pacman está mas a la derecha, tira mover a la izq
					 */
					iDirCount[i]+=pacmanPosX>posX?
							POS_FACTOR:0;
					break;
				case 3:	// down
					/*
					 * Si el pacman esta mas arriba, muevo para abajo
					 */
					iDirCount[i]+=pacmanPosY<posY?
							POS_FACTOR:0;
					break;
				}
				iDirTotal+=iDirCount[i];
			}	
		}
		// randomly select a direction
		if (iDirTotal!=0)
		{
			iRand=Utils.RandSelect(iDirTotal);
			if (iRand>=iDirTotal)
				throw new Error("RandSelect out of range");
			//				exit(2);
			for (i=0; i<4; i++)
			{	
				iM=laberinto.laberinto[posY/16+ LaberintoUtils.direccionesEjeY[i]]
				              [posX/16+ LaberintoUtils.direccionesEjeX[i]];
				if (iM!= Laberinto.WALL && i!= LaberintoUtils.iBack[direccion])
				{	
					iRand-=iDirCount[i];
					if (iRand<0)
						// the right selection
					{
						direccion=i;	break;
					}
				}
			}
		}
		else
			throw new Error("iDirTotal out of range");
		return(direccion);
	}

	// return 1 if caught the pac!
	// return 2 if being caught by pac
	int testCollision(int iPacX, int iPacY)
	{
		if (posX<=iPacX+2 && posX>=iPacX-2
				&& posY<=iPacY+2 && posY>=iPacY-2)
		{
			switch (iStatus)
			{
			case OUT:
				return(1);
			case BLIND:
				iStatus=EYE;
				posX=posX/4*4;
				posY=posY/4*4;
				return(2);
			}	
		}
		// nothing
		return(0);
	}
}


