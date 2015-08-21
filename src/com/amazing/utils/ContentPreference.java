package com.amazing.utils;

import java.util.Random;

import android.content.Context;
import android.content.SharedPreferences;

//内容读取与写入 类
//包括 读取 各种参数 和 存储各种参数
public class ContentPreference 
{
	//
	private final static String DATA_FILE_NAME = "lff"; //存储的文件名
	private final static String BG_COLOR_KEY = "bgcolorindex";
	private final static String FONT_COLOR_KEY = "fontcolorindex";
	private final static String SIZE_PROGRESS_KEY = "sizeprogress";
	
	private int bgColorIndexMax;	//最大的颜色索引
	private int fontColorIndexMax;//最大的字体颜色 索引
	private int sizeProgressMax;	//子体的最大大小
	
	private int bgColorIndex;	//背景颜色索引
	private int fontColorIndex;	//字体颜色索引
	private int blobProgress;	//模糊
	private int sizeProgress;	//字体的大小
	
	//
	private SharedPreferences sharePreferences = null;
	
	//随机数
	Random random = null;
	
	public ContentPreference(Context context, int bgColorIndexMax, int fontColorIndexMax, int sizeProgressMax)
	{
		this.bgColorIndexMax = bgColorIndexMax;
		this.fontColorIndexMax = fontColorIndexMax;
		this.sizeProgressMax = sizeProgressMax;
		
		//读取内容
		sharePreferences = context.getSharedPreferences(DATA_FILE_NAME, 0);
		
		//随机数
		random = new Random();
		
		readParams();
	}
	


	public int getBgColorIndex() {
		return bgColorIndex;
	}

	public int getFontColorIndex() {
		return fontColorIndex;
	}
	
	public int getBlobProgress() {
		return blobProgress;
	}

	public int getSizeProgress() {
		return sizeProgress;
	}



	public void setSizeProgress(int sizeProgress) {
		this.sizeProgress = sizeProgress;
	}

	//存储参数
	public void commitParams(int bgColorIndex, int fontColorIndex, int blobProgress, int sizeProgress)
	{
		SharedPreferences.Editor editor = sharePreferences.edit();
		editor.putInt(BG_COLOR_KEY, bgColorIndex);  
		editor.putInt(FONT_COLOR_KEY, fontColorIndex);  
		editor.putInt(SIZE_PROGRESS_KEY, sizeProgress);  
		editor.commit();  
	}
	
	//读取各种参数
	private void readParams()
	{
		bgColorIndex = sharePreferences.getInt(BG_COLOR_KEY, -1);
		if(0 > bgColorIndex || bgColorIndex >= bgColorIndexMax)
			bgColorIndex = Math.abs( random.nextInt() ) % bgColorIndexMax;
		
		//---------------------------------
		//System.out.println("bgColorIndex：" + bgColorIndex);
		
		fontColorIndex = sharePreferences.getInt(FONT_COLOR_KEY, -1);
		if(0 > fontColorIndex || fontColorIndex >= fontColorIndexMax)
			fontColorIndex = Math.abs( random.nextInt() ) % fontColorIndexMax;
		
		sizeProgress = sharePreferences.getInt(SIZE_PROGRESS_KEY, 25);
		if(0 > sizeProgress || sizeProgress > sizeProgressMax)
			sizeProgress = 25;
		
		//模糊程度默认是0
		blobProgress = 0;
	}
}























