package com.amazing.utils;

import android.graphics.Bitmap;

//存储需要的静态变量 
//主要是intent跳转需要的常量
public class Constant 
{
	public final static int REQUEST_GET_IMAGE_CODE = 1;	//请求得到一张图片的uri
	public final static int REQUEST_EDIT_IMAGE_CODE = 2;	//请求编辑一张图片
	
	//在editactivit 编辑好的图片引用 用于在两个activity之间传递
	private static Bitmap editBitmap = null;
	
	public static void setEditBitmap(Bitmap bitmap)
	{
		editBitmap = bitmap;
	}
	
	public static Bitmap getEditBitmap()
	{
		return editBitmap;
	}
}
