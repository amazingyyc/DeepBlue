package com.amazing.utils;

import android.graphics.Bitmap;

//mainActivity状态 表明当前是 颜色背景 还是 图片背景
public class BackgroundStatus 
{
	static public enum STATUS
	{
		COLOR_BG,	//以颜色为背景
		BITMAP_BG,	//以图片 为背景
	}
	
	public STATUS status;	//对应的状态
	
	public int color;	//如果是颜色状态 存储当前颜色
	
	public Bitmap originBitmap = null;	//如果是图片状态 存储原始图片
	public Bitmap blurBitmap = null;	//存储已经设为背景的图片 
	
	//需要清理的图片
	public Bitmap recycleBitmap = null;
}
