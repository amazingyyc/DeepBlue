package com.amazing.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Matrix;

/*
 * 负责对图片进行高斯模糊
 */
public class GaussBlur 
{
	//缩放参数
	//private final static float SCALE = 4.0f;
	private final static int RENDER_BITMAP_MAX_RADIUS = 200;
	private final static int MAX_RADIUS = 100;	//颜色高斯模糊的最大半径 和 最小半径
	private final static int MIN_RADIUS = 1;
	
	private final static int RENDER_MAX_RADIUS = 25;
	private final static int RENDER_MIN_RADIUS = 1;
	
	Context context = null;
	
	//屏幕的宽与高
	int screenWidth;
	int screenHeight;
	
	//高 宽 比
	float scale;
	
	public GaussBlur(Context context, int screenWidth, int screenHeight)
	{
		this.context = context;
		
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		
		this.scale = 1.0f * screenHeight / screenWidth;
	}
	
	//传入一个颜色值 对这个纯色图片进行高斯模糊 边缘部分用白色补全
	public Bitmap blurColor(int color, int radius)
	{
		//模糊半径为0 直接返回一张图片
		if(0 == radius)
		{
			//直接返回一个纯色图片
			int[] pix = new int[screenWidth * screenHeight];
			int n = screenWidth * screenHeight;
			for(int i = 0; i < n; ++i)
				pix[i] = color;
			
			Bitmap bitmap = Bitmap.createBitmap(pix, screenWidth, screenHeight, Config.ARGB_8888);
			
			return bitmap;
		}
		
		//判断边界条件
		if(radius > MAX_RADIUS)	radius = MAX_RADIUS;
		if(radius < MIN_RADIUS)	radius = MIN_RADIUS;
		
		int w;
		int h;
		int n;
		
		if(screenHeight > screenWidth)
		{
			//高大于宽
			w = 2 * (MAX_RADIUS + radius);
			h = (int)(scale * w);
			n = w * h;
		}
		else
		{
			h = 2 * (MAX_RADIUS + radius);
			w = (int)(h / scale);
			n = w * h;
		}

		int[] pix = new int[n];
		
		//背景设为白色
		for(int i = 0; i < n; ++i)
			pix[i] = 0xFFFFFFFF;
		
		//中间的矩形设置为 既定颜色
		for(int i = radius; i < w - radius; ++i)
		for(int j = radius; j < h - radius; ++j)
			pix[j * w + i] = color;
		
		//进行高斯模糊
		initCBlur(pix, w ,h, radius);
		
		//制作一张图片
		Bitmap intBitmap = Bitmap.createBitmap(pix, w, h, Config.ARGB_8888);
		
		//对图片进行裁剪
		w -= 2 * radius;
		h -= 2 * radius;
		Matrix matrix = new Matrix();
		matrix.postScale(1.0f * screenWidth / w, 1.0f * screenHeight / h);
		Bitmap blurBitmap = Bitmap.createBitmap(intBitmap, radius, radius, w, h, matrix, true);
		
		//清理内存
		intBitmap.recycle();
		
		return blurBitmap;
	}
	
	public Bitmap blurBitmap2(Bitmap bitmap, int radius)
	{
		if(0 == radius)
		{
			//返回一个相同的图片
			//Bitmap outBitmap = Bitmap.createBitmap(bitmap);
			int w = bitmap.getWidth();
			int h = bitmap.getHeight();
			int[] pix = new int[w * h];
			
			//写入数组数据
			bitmap.getPixels(pix, 0, w, 0, 0, w, h);
			
			Bitmap outBitmap = Bitmap.createBitmap(pix, w, h, Bitmap.Config.ARGB_8888);
			
			return outBitmap;
		}
		
		//防止越界
		if(radius > MAX_RADIUS)	radius = MAX_RADIUS;
		if(radius < MIN_RADIUS) radius = MIN_RADIUS;
		
		//对图片进行缩放
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		
		float ss;	//缩放比例
		
		if(h > w)	ss = 1.0f * 2 * RENDER_BITMAP_MAX_RADIUS / w;
		else 		ss = 1.0f * 2 * RENDER_BITMAP_MAX_RADIUS / h;
		
		Matrix smallMatrix = new Matrix();
		smallMatrix.postScale(ss, ss);
		Bitmap inBitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, smallMatrix, true);
		
		int inW = inBitmap.getWidth();
		int inH = inBitmap.getHeight();
		int[] pix = new int[inW * inH];
		
		//写入数据
		inBitmap.getPixels(pix, 0, inW, 0, 0, inW, inH);
		
		//进行模糊
		initCBlur(pix, inW, inH, radius);
		
		//写入数据
		inBitmap.setPixels(pix, 0, inW, 0, 0, inW, inH);
		
		//放大
		//放大
        ss = 1.0f / ss;
        Matrix bigMatrix = new Matrix();
        bigMatrix.postScale(ss, ss);
        Bitmap outBitmap = Bitmap.createBitmap(inBitmap, 0, 0, inW, inH, bigMatrix, true);
        
        inBitmap.recycle();
        
        return outBitmap;
	}
	
	//利用android自带的算法进行 模糊操作
	/*public Bitmap blurBitmap(Bitmap bitmap, int radius)
	{  
		if(0 == radius)
		{
			//返回一个相同的图片
			//Bitmap outBitmap = Bitmap.createBitmap(bitmap);
			int w = bitmap.getWidth();
			int h = bitmap.getHeight();
			int[] pix = new int[w * h];
			
			//写入数组数据
			bitmap.getPixels(pix, 0, w, 0, 0, w, h);
			
			Bitmap outBitmap = Bitmap.createBitmap(pix, w, h, Bitmap.Config.ARGB_8888);
			
			return outBitmap;
		}
		
		//防止越界
		if(radius > RENDER_MAX_RADIUS)	radius = RENDER_MAX_RADIUS;
		if(radius < RENDER_MIN_RADIUS)  radius = RENDER_MIN_RADIUS;
		
		//对图片进行缩放
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		
		float ss;	//缩放比例
		
		if(h > w)	ss = 1.0f * 2 * RENDER_BITMAP_MAX_RADIUS / w;
		else 		ss = 1.0f * 2 * RENDER_BITMAP_MAX_RADIUS / h;
		
		Matrix smallMatrix = new Matrix();
		smallMatrix.postScale(ss, ss);
		Bitmap inBitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, smallMatrix, true);
		
        //Let's create an empty bitmap with the same size of the bitmap we want to blur  
        Bitmap midBitmap = Bitmap.createBitmap(inBitmap.getWidth(), inBitmap.getHeight(), Config.ARGB_8888);  
          
        //Instantiate a new Renderscript  
        RenderScript rs = RenderScript.create(context.getApplicationContext());  
          
        //Create an Intrinsic Blur Script using the Renderscript  
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));  
          
        //Create the Allocations (in/out) with the Renderscript and the in/out bitmaps  
        Allocation allIn = Allocation.createFromBitmap(rs, inBitmap);  
        Allocation allOut = Allocation.createFromBitmap(rs, midBitmap);  
          
        //Set the radius of the blur  
        blurScript.setRadius(radius);
          
        //Perform the Renderscript  
        blurScript.setInput(allIn);  
        blurScript.forEach(allOut);  
          
        //Copy the final bitmap created by the out Allocation to the outBitmap  
        allOut.copyTo(midBitmap);  
         
        //放大
        ss = 1.0f / ss;
        Matrix bigMatrix = new Matrix();
        bigMatrix.postScale(ss, ss);
        Bitmap outBitmap = Bitmap.createBitmap(midBitmap, 0, 0, midBitmap.getWidth(), midBitmap.getHeight(), bigMatrix, true);
        
        //After finishing everything, we destroy the Renderscript.  
        inBitmap.recycle();
        midBitmap.recycle();
        rs.destroy();
        
          
        return outBitmap;  
    }*/
	
	//传入一个颜色值 对这个纯色图片进行高斯模糊 边缘部分用白色补全
	/*public Bitmap blurColor(int color, int radius)
	{
		//模糊半径为0 直接返回一张图片
		if(0 == radius)
		{
			return null;
		}
		
		//判断边界条件
		if(radius > MAX_RADIUS)	radius = MAX_RADIUS;
		else if(radius < MIN_RADIUS)	radius = MIN_RADIUS;
		
		int w;
		int h;
		int n;
		
		if(screenHeight > screenWidth)
		{
			//高大于宽
			w = 2 * (MAX_RADIUS + radius);
			h = (int)(scale * w);
			n = w * h;
		}
		else
		{
			h = 2 * (MAX_RADIUS + radius);
			w = (int)(h / scale);
			n = w * h;
		}

		int[] pix = new int[n];
		
		//背景设为白色
		for(int i = 0; i < n; ++i)
			pix[i] = 0xFFFFFFFF;
		
		//中间的矩形设置为 既定颜色
		for(int i = radius; i < w - radius; ++i)
		for(int j = radius; j < h - radius; ++j)
			pix[j * w + i] = color;
		
		//进行高斯模糊
		initCBlur2(pix, w ,h, radius);
		
		//制作一张图片
		Bitmap intBitmap = Bitmap.createBitmap(pix, w, h, Config.ARGB_8888);
		
		//对图片进行裁剪
		w -= 2 * radius;
		h -= 2 * radius;
		Matrix matrix = new Matrix();
		matrix.postScale(1.0f * screenWidth / w, 1.0f * screenHeight / h);
		Bitmap blurBitmap = Bitmap.createBitmap(intBitmap, radius, radius, w, h, matrix, false);
		
		//清理内存
		intBitmap.recycle();
		
		return blurBitmap;
	}*/
	
	//使用c++计算高斯模糊
	/*
	 * pix 像素数组
	 * w 行像素个数
	 * h 列数
	 * radius 模糊半径
	 */
	private native void initCBlur(int[] pix, int w ,int h, int r);
	//private native void initCBlur2(int[] pix, int w ,int h, int r);
	
	//加载native模块
	static
	{
		System.loadLibrary("blur"); 
	};
}


















