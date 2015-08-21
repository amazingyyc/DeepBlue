package com.amazing.utils;

//根据分辨率计算 对应的坐标
public class ViewCoord 
{
	private final static int STANDARD_WIDTH = 800;
	private final static int STANDARD_SMALL_WIDTH = 100;
	private final static int STANDARD_BIG_WIDTH = 160;
	private final static int STANDARD_VIEW_INTER = 40;
	private final static int STANDARD_SMALL_BOTTOM = 40;
	private final static int STANDARD_BIG_BOTTOM = 10;
	
	//屏幕分辨率
	public int w;
	public int h;
	
	private int smallWidth;
	private int bigWidth;
	private int inter;
	private int smallBottom;
	private int bigBottom;
	
	private int[][] coords = new int[5][3];
	
	public ViewCoord(int w, int h)
	{
		this.w = w;
		this.h = h;
		
		initCoords();
	}
	
	public int[][] getCoords()
	{
		return coords;
	}
	
	private void initCoords()
	{
		double rate = 1.0 * w / STANDARD_WIDTH;
		
		smallWidth  = (int) (rate * STANDARD_SMALL_WIDTH);
		bigWidth    = (int) (rate * STANDARD_BIG_WIDTH);
		inter       = (int) (rate * STANDARD_VIEW_INTER);
		smallBottom      = (int) (rate * STANDARD_SMALL_BOTTOM);
		bigBottom      = (int) (rate * STANDARD_BIG_BOTTOM);
		
		coords[0][0] = inter;
		coords[0][1] = smallBottom;
		coords[0][2] = smallWidth;
		
		coords[1][0] = 2 * inter + smallWidth;
		coords[1][1] = smallBottom;
		coords[1][2] = smallWidth;
		
		coords[2][0] = 3 * inter + 2 * smallWidth;
		coords[2][1] = bigBottom;
		coords[2][2] = bigWidth;
		
		coords[3][0] = 4 * inter + 2 * smallWidth + bigWidth;
		coords[3][1] = smallBottom;
		coords[3][2] = smallWidth;
		
		coords[4][0] = 5 * inter + 3 * smallWidth + bigWidth;
		coords[4][1] = smallBottom;
		coords[4][2] = smallWidth;
	}
	
	public int getW()
	{
		return w;
	}
	
	public int getH()
	{
		return h;
	}
}



















