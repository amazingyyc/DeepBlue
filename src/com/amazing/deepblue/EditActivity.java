package com.amazing.deepblue;

import java.io.FileNotFoundException;

import com.amazing.utils.Constant;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.ImageView;

//图片编辑的activity 传入一章图片 用户编辑好尺寸后 然后返回
public class EditActivity extends Activity 
{
	//存储屏幕的分辨率
	int screenWidth;
	int screenHeight;
	
	Bitmap bitmap = null;	//对应的图片
	float scale;	//缩放比例
	
	float MIN_LEFT;	//最大的偏移量
	float MIN_TOP;
	
	float left = 0;	//图片相对于控件的左 上 的偏移量
	float top = 0;
	
	Matrix originMatrix = null;	//原始的变换矩阵
	Matrix currentMatrix = null;	//随着操作的变换矩阵 用于时时移动
	
	//接受触屏操作
	float lastX;
	float lastY;
	
	ImageView editImageView     = null;
	ImageButton editImageButton = null;
	
	 @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        
        //得到屏幕分辨率
  		DisplayMetrics mDisplayMetrics = new DisplayMetrics();
  		getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
  		screenWidth  = mDisplayMetrics.widthPixels;
  		screenHeight = mDisplayMetrics.heightPixels;
        
        //得到控件
        editImageView = (ImageView)findViewById(R.id.edit_image_view);
        editImageButton = (ImageButton)findViewById(R.id.edit_image_button);
        
        //根据传入的inten得到一张图片
        ContentResolver cr = this.getContentResolver();	//内容提供者
        Intent intent      = this.getIntent();
        
        Uri uri = intent.getData();	//图片uri
               
        //读取图片
        try
        {
        	bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri)); 
		}
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}
        
        //对图片进行旋转 
        int degree = getGegree(uri);
        if(-1 != degree)
        	bitmap = rotateBitmap(bitmap, degree);
        
        
        //根据屏幕大小缩放图片 使之铺满整个屏幕
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        
        if((1.0f * w / h) > (1.0f * screenWidth / screenHeight))
        	//图片的宽高比 大于 屏幕的宽高比
        	scale = 1.0f * screenHeight / h;
        else
        	//图片的宽高比 小于 屏幕的
        	scale = 1.0f * screenWidth / w;
        
        //初始化最大的偏移量
        MIN_LEFT = screenWidth - scale * w;
        MIN_TOP  = screenHeight - scale * h;
        
        //原始的缩放矩阵
        originMatrix = new Matrix();
        originMatrix.postScale(scale, scale);
        
        //需要移动的缩放矩阵
        currentMatrix = new Matrix(originMatrix);
        
        //设置图片
        editImageView.setImageMatrix(currentMatrix);
        editImageView.setImageBitmap(bitmap);
        
        //设置触摸监听 用户 移动图片
        editImageView.setOnTouchListener(movingEventListener);  
        
        //进行裁剪
        editImageButton.setOnClickListener(editClickListener);
    }
	 
	 /*
	  * 一般的思路都是从Bitmap或者Exif信息的方向入手，可是这些都不太完美，
	  * 因为Exif信息可以抹除，实际上我们在拍照的时候，
	  * 方向的信息已经被应用记录在数据库里面了，我们只需要从数据库获取即可。
	  * 可能你会问，如果是别人发给你的呢？这里不必担心，因为别人发给你的，
	  * 肯定是他希望你所看的形式所展现的，如果是以文件发给你也没关系，
	  * 因为有Exif的信息情况下，能够被系统应用保存起来的图片，
	  * 那么它的方向也会被写入到数据库
	  */
	 private int getGegree(Uri uri)
	 {
		 int degree = 0;
		 
		 //进行查询
		 Cursor cursor = this.getContentResolver().query(uri, new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);
	     if (cursor != null) 
	     {
	         if (cursor.getCount() != 1) 
	            return -1;

	         cursor.moveToFirst();
	         degree = cursor.getInt(0);
	         cursor.close();
	     }
	     
	     return degree;
	 }
	 
	 private Bitmap rotateBitmap(Bitmap bitmap, int degree)
	 {
		 if(0 != degree)
		 {
			 Matrix matrix = new Matrix();
			 matrix.postRotate(degree);
			 
			 return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		 }
		 
		 return bitmap;
	 }
	 
	 private OnClickListener editClickListener = new OnClickListener() 
	 {
		@Override
		public void onClick(View arg0) 
		{
			//裁剪图片
			int x = (int) Math.floor(-left / scale);
			int y = (int) Math.floor(-top / scale);
			int width = (int) Math.ceil(screenWidth / scale);
			int height = (int) Math.ceil(screenHeight / scale);
			Matrix m = new Matrix();
			m.postScale(scale, scale);
			
			//得到处理后的图片
			Bitmap resultBitmap = Bitmap.createBitmap(bitmap, x, y, width, height, m, true);
			//将图片设置到静态区域
			Constant.setEditBitmap(resultBitmap);
			
			//返回到mainactivity
			Intent intent = new Intent();//数据是使用Intent返回
			setResult(RESULT_OK, intent);//设置返回数据
			
			finish();//关闭Activity
		}
	};
	 
	 private OnTouchListener movingEventListener = new OnTouchListener()
	 {
		@Override
		public boolean onTouch(View view, MotionEvent event) 
		{
			switch(event.getAction() & MotionEvent.ACTION_MASK)
			{
			 case MotionEvent.ACTION_DOWN:
				 //得到初始坐标
				 lastX = event.getX();
				 lastY = event.getY();
				 break;
			 case MotionEvent.ACTION_MOVE:
				 //当前正在移动的坐标
				 float curX = event.getX();
				 float curY = event.getY();

				 left += curX - lastX;
				 top  += curY - lastY;
				 
				 if(left > 0)left = 0;
				 if(left < MIN_LEFT) left = MIN_LEFT;
				 if(top > 0) top = 0;
				 if(top < MIN_TOP) top = MIN_TOP;
				 
				 currentMatrix.set(originMatrix);
				 currentMatrix.postTranslate(left, top);

				 editImageView.setImageMatrix(currentMatrix);
				 
				 lastX = curX;
				 lastY = curY;
				 break;
			 default:
				 break;
			}
			
			return true;
		}
	 };
}

























