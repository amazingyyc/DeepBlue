package com.amazing.utils;

import com.amazing.deepblue.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;

//将文字绘制到 图片上
public class DrawText 
{
	Context context = null;
	
	public DrawText(Context context)
	{
		this.context = context;
	}
	
	/*
	 * bitmap 图片
	 * text 文字
	 * textSize 文字大小
	 * rect裁剪区域
	 * dx 画布偏移量
	 * dy
	 */
	public Bitmap drawTextToBitmap(Bitmap bitmap, String text, int color, float textSize, int width, float dx, float dy)
	{
		//生成一张新的图片
		Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
		
		//新建画布
		Canvas canvas = new Canvas(outBitmap);
		
		//图片画笔
		Paint bitmapPaint = new Paint();
		
		//首先绘制图片
		canvas.drawBitmap(bitmap, 0, 0, bitmapPaint);
		
		//偏移量
		canvas.translate(dx, dy);
		
		//绘制文字
		TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);
		textPaint.setColor(color);
		//textPaint.setStyle(Style.FILL);
		textPaint.setTextSize(textSize);
		
		//设置自定义子体
		Typeface typeface = Typeface.createFromAsset(context.getAssets(), context.getString(R.string.font_path));
		textPaint.setTypeface(typeface);
		
		//设置文字布局
		StaticLayout staticLayout = new StaticLayout(text, textPaint, width, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
		
		//绘制文字
		staticLayout.draw(canvas);
		
		//--测试
		//File file = new File("/sdcard/namecard/yyc", "xx.png"); 
		 
		//if(file.exists())
			//file.delete();
		
		/*String fileName = "/mnt/sdcard/xx.png";
		
		 File file = new File(fileName);

         FileOutputStream out = null;
         try{
                 out = new FileOutputStream(file);
                 
                 //100表示不压缩
                 if(outBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) 
                 {
                         out.flush();
                         out.close();
                 }
         } 
         catch (FileNotFoundException e) 
         {
                 e.printStackTrace();
         } 
         catch (IOException e) 
         {
                 e.printStackTrace(); 
         }*/

		return outBitmap;
	} 
}
























