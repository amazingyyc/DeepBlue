package com.amazing.deepblue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParserException;

import com.amazing.utils.BackgroundStatus;
import com.amazing.utils.Constant;
import com.amazing.utils.ContentPreference;
import com.amazing.utils.DrawText;
import com.amazing.utils.GaussBlur;
import com.amazing.utils.PopMenuListener;
import com.amazing.utils.PopOperateMenuListener;
import com.amazing.utils.PopViewStatus;
import com.amazing.utils.ViewCoord;
import com.amazing.view.AdaptEditText;
import com.amazing.view.ColorButton;
import com.amazing.view.OperateButton;
import com.amazing.view.PopMenu;
import com.amazing.view.PopOperateMenu;
import com.amazing.view.PopSeekBar;
import com.amazing.view.VerticalSeekBar;
import com.amazing.view.VerticalSeekBarWrapper;
import com.amazing.view.WaitingDialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Images.Media;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

@SuppressLint("InlinedApi")
public class MainActivity extends Activity 
{
	//-----------------------------
	//想handle发送开始或者结束动画消息
	private static final int START_WAITING_DIALOG = 1;	//显示等待 界面
	private static final int STOP_WAITING_DIALOG  = 2;	//消除等待界面
	private static final int CHANGE_IMAGE_VIEW = 3;		//设置iamgeview新的图片
	
	private static final int START_SET_WALLPAPER = 4;	//开始设置壁纸
	private static final int SUCCESS_SET_WALLPAPER = 5;	//成功设置桌面壁纸
	private static final int FAIL_SET_WALLPAPER = 6;	//设置桌面壁纸失败
	
	private static final int START_SET_LOCKPAPER = 7;	//开始设置锁屏壁纸
	private static final int SUCCESS_SET_LOCKPAPER = 8;	//成功设置锁屏壁纸
	private static final int FAIL_SET_LOCKPAPER = 9;	//设置锁屏壁纸失败
	private static final int NOT_SUPPORT_LOCKPAPER = 10;
	
	private static final int START_SAVE_BITMAP = 11;	//开始保存图片
	private static final int SUCCESS_SAVE_BITMAP = 12;	//成功保存图片
	private static final int FAIL_SAVE_BITMAP = 13;	//保存失败
	private static final int NO_SDCARD        = 14;	//没有sdcard
	
	private static final int START_SHARE_BITMAP = 15;	//开始分享图片
	private static final int SUCCESS_SHARE_BITMAP = 16;	//分享成功
	private static final int FAIL_SHARE_BITMAP = 17;	//分享失败
	private static final int NO_SHARE_SDCARD = 18;
	
	//-----------------------------------------------------
	PopMenu backgroundColorMenu = null;	//修改背景颜色的按钮
	PopMenu fontColorMenu       = null;	//修改字体颜色的按钮
	
	//高斯模糊需要的控件和相应的控制函数
	PopSeekBar             blobPopSeekBar     = null;
	VerticalSeekBarWrapper blobSeekBarWrapper = null;
	VerticalSeekBar        blobSeekBar        = null;
	ImageButton            blobControlButton  = null;
	
	//改变子体大小控件和相应的控制函数
	PopSeekBar             sizePopSeekBar     = null;
	VerticalSeekBarWrapper sizeSeekBarWrapper = null;
	VerticalSeekBar        sizeSeekBar        = null;
	ImageButton            sizeControlButton  = null;

	//操作按钮 包括分享 设置锁屏 设置桌面
	PopOperateMenu operate      = null;	
	
	ViewCoord      viewCoord    = null;	//坐标
	PopViewStatus popViewStatus = null;	//状态
	
	//文本框输入
	AdaptEditText editText = null;
	
	//高斯模糊函数
	GaussBlur gaussBlur = null;
	
	//将文字绘制到图片
	DrawText drawText = null;
	
	//显示背景图片的控件
	ImageView imageView = null;
	
	//参数 读取和存储 类
	ContentPreference contentPreference = null;
	
	//状态类
	BackgroundStatus backgroundStatus = null;
	
	//等待界面 用于显示 等待动画
	WaitingDialog waitingDialog = null;
	
	//跳转按钮 跳转到 info页面
	ImageButton infoButton = null;
	
	//----------------------------------------------
	//任务类
	BlobRunnable blobRunnable = null;	//模糊图片
	SetWallPaperRunnable setWallPaperRunnable = null;	//设置桌面壁纸
	SaveBitmapRunnable saveBitmapRunnable = null;
	SetLockWallpaperRunnable setLockWallpaperRunnable = null;
	ShareBitmapRunnable shareBitmapRunnable = null;
	
	//分享图片的uri
	Uri shareUri = null;
	
	//根控件的索引
	RelativeLayout mainLayout = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		initButtons();	//初始化info按钮
		initViewsParams();	//初始化需要的参数 不如屏幕分辨率
		initViews();	//初始化需要的按钮
		initViewsStatus();	//根据读取的状态设置 各个控件的状态
		initRunnables();
		initViewsListener();	//添加按钮监听
	}
	
	@Override
	protected void onStop()
	{
		super.onStop();
		
		//存储各种参数
		contentPreference.commitParams(backgroundColorMenu.getSelectedIndex(), fontColorMenu.getSelectedIndex(), 
						blobSeekBar.getProgress(), sizeSeekBar.getProgress());
	}
	
	//等待 窗口的显示与消失
	Handler handler = new Handler()
	{
		public void handleMessage(Message msg) 
		{   
            switch (msg.what) 
            {
            case START_WAITING_DIALOG:
            	waitingDialog.show();
            	break;
            case STOP_WAITING_DIALOG:
            	waitingDialog.dismiss();
            	break;
            case CHANGE_IMAGE_VIEW:
            	imageView.setImageBitmap(backgroundStatus.blurBitmap);
            	backgroundStatus.recycleBitmap.recycle();
            	backgroundStatus.recycleBitmap = null;
            	break;
            	
            	//设置桌面壁纸
            case START_SET_WALLPAPER:
            	waitingDialog.show();
            	break;
            case SUCCESS_SET_WALLPAPER:
            	waitingDialog.dismiss();
            	Toast.makeText(MainActivity.this, getText(R.string.success_set_wallpaper), Toast.LENGTH_SHORT).show();
            	break;
            case FAIL_SET_WALLPAPER:
            	waitingDialog.dismiss();
            	Toast.makeText(MainActivity.this, getText(R.string.fail_set_wallpaper), Toast.LENGTH_SHORT).show();
            	break;
            	
            	//设置锁屏壁纸
            case START_SET_LOCKPAPER:
            	waitingDialog.show();
            	break;
            case SUCCESS_SET_LOCKPAPER:
            	waitingDialog.dismiss();
            	
            	Toast.makeText(MainActivity.this, getText(R.string.success_set_lock_wallpaper), Toast.LENGTH_SHORT).show();
            	break;
            case FAIL_SET_LOCKPAPER:
            	waitingDialog.dismiss();
            	
            	Toast.makeText(MainActivity.this, getText(R.string.fail_set_lock_wallpaper), Toast.LENGTH_SHORT).show();
            	break;
            case NOT_SUPPORT_LOCKPAPER:
            	waitingDialog.dismiss();
            	
            	Toast.makeText(MainActivity.this, getText(R.string.not_support_set_lock_wallpaper), Toast.LENGTH_SHORT).show();
            	break;
            	
            	
            //---------------------------------------
            //图片保存
            case START_SAVE_BITMAP:
            	waitingDialog.show();
            	break;
            case SUCCESS_SAVE_BITMAP:
            	waitingDialog.dismiss();
            	String str = getText(R.string.success_save_bitmap).toString() + getText(R.string.save_bitmap_dir).toString();
            	Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
            	break;
            case FAIL_SAVE_BITMAP:
            	waitingDialog.dismiss();
            	Toast.makeText(MainActivity.this, getText(R.string.fail_save_bitmap), Toast.LENGTH_SHORT).show();
            	
            	break;
            case NO_SDCARD:
            	waitingDialog.dismiss();
            	Toast.makeText(MainActivity.this, getText(R.string.no_sdcard), Toast.LENGTH_SHORT).show();
            	
            	break;
            	
            //分享图片 文件
            case START_SHARE_BITMAP:
            	waitingDialog.show();
            	break;
            case SUCCESS_SHARE_BITMAP:
            	waitingDialog.dismiss();
            	
            	//图片保存成功 分享uri
            	Intent shareIntent = new Intent(Intent.ACTION_SEND);
            	
                shareIntent.putExtra(Intent.EXTRA_STREAM, shareUri);
                shareIntent.setType("image/png");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_extra_subject));   
                shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_extra_text));    
                shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
                
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_activity_title)));
                
            	break;
            case FAIL_SHARE_BITMAP:
            	waitingDialog.dismiss();
            	
            	Toast.makeText(MainActivity.this, getText(R.string.fail_share_bitmap), Toast.LENGTH_SHORT).show();
            	break;
            case NO_SHARE_SDCARD:
            	waitingDialog.dismiss();
            	
            	Toast.makeText(MainActivity.this, getText(R.string.no_sdcard_share), Toast.LENGTH_SHORT).show();
            	break;
            }
            
            super.handleMessage(msg);   
       }
	};
	
	
	//---------------------------------------------------------
	//根据返回的结果继不同的处理
	@Override  
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(RESULT_OK == resultCode && null != data)
		{
			if(Constant.REQUEST_GET_IMAGE_CODE == requestCode)
			{
				//得到一图片uri
	    		Uri uri = data.getData();
	    		
	    		//将uri传入到另一个activity 
	    		Intent intent = new Intent(this, EditActivity.class);
	    		intent.setData(uri);	//设置数据
	    		
	    		//等待返回数据 请求编辑activity等待返回编辑好的图片
	    		startActivityForResult(intent, Constant.REQUEST_EDIT_IMAGE_CODE);
			}
			else if(Constant.REQUEST_EDIT_IMAGE_CODE == requestCode)
			{
				//判断当前的状态
				if(BackgroundStatus.STATUS.COLOR_BG == backgroundStatus.status)
				{
					//颜色 为背景
					Bitmap tempBitmap = backgroundStatus.blurBitmap;
					
					backgroundStatus.originBitmap = Constant.getEditBitmap();
					Constant.setEditBitmap(null);
					
					//得到新的图片
					backgroundStatus.blurBitmap = gaussBlur.blurBitmap2(backgroundStatus.originBitmap, 0);
					
					backgroundStatus.status = BackgroundStatus.STATUS.BITMAP_BG;
					backgroundStatus.color = -1;
					
					blobSeekBar.setProgress(0);//模糊设为0
					imageView.setImageBitmap(backgroundStatus.blurBitmap);
					
					tempBitmap.recycle();
				}
				else if(BackgroundStatus.STATUS.BITMAP_BG == backgroundStatus.status)
				{
					//图片为背景
					Bitmap tempBitmap1 = backgroundStatus.originBitmap;
					Bitmap tempBitmap2 = backgroundStatus.blurBitmap;
					
					backgroundStatus.originBitmap = Constant.getEditBitmap();
					Constant.setEditBitmap(null);
					
					//得到新的图片
					backgroundStatus.blurBitmap = gaussBlur.blurBitmap2(backgroundStatus.originBitmap, 0);
					
					blobSeekBar.setProgress(0);//模糊设为0
					imageView.setImageBitmap(backgroundStatus.blurBitmap);
					
					tempBitmap1.recycle();
					tempBitmap2.recycle();
				}
				
				//--------------------------------------
				//背景颜色取消选择
				backgroundColorMenu.cancelSelected();
				backgroundColorMenu.executeShrink();
			}
		}
    }
	
	
	//初始化跳转按钮
	private void initButtons()
	{
		infoButton = (ImageButton)findViewById(R.id.info_button);
		
		infoButton.setOnClickListener(new View.OnClickListener() 
		{
			@Override
			public void onClick(View arg0) 
			{
				//跳转
				Intent intent = new Intent(MainActivity.this, InfoActivity.class);
				startActivity(intent);
			}
		});
	}
	
	//初始化各种坐标
	private void initViewsParams()
	{
		//状态信息
		popViewStatus = new PopViewStatus();
		
		//得到屏幕分辨率
		DisplayMetrics mDisplayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
		int w = mDisplayMetrics.widthPixels;
		int h = mDisplayMetrics.heightPixels;
		
        viewCoord = new ViewCoord(w, h);
	}
	
	private void initViews()
	{
		//得到所有控件的父控件
		mainLayout = (RelativeLayout)findViewById(R.id.main_layout);
		
		//读取颜色
		ArrayList<Integer> backgroundColors = getColorsFromXML(R.xml.bg_colors);
		ArrayList<Integer> fontColors = getColorsFromXML(R.xml.font_colors);
		
		//////////////////////////
		//得到坐标
		int[][] coords = viewCoord.getCoords();
		
		///////////////////////////////////////////////////
		backgroundColorMenu = (PopMenu)findViewById(R.id.background_color_pm);	//修改背景颜色的按钮
		fontColorMenu       = (PopMenu)findViewById(R.id.font_color_pm);	//修改字体颜色的按钮
		
		//设置参数
		backgroundColorMenu.initParams(coords[0][2], coords[0][0], coords[0][1], R.drawable.ic_action_tick, R.drawable.ic_background_color, 
				R.drawable.ic_action_cancel, R.drawable.ic_action_picture, backgroundColors, popViewStatus);
		
		fontColorMenu.initParams(coords[4][2], coords[4][0], coords[4][1], R.drawable.ic_action_tick, R.drawable.ic_font_color, 
						R.drawable.ic_action_cancel, -1, fontColors, popViewStatus);
		
		
		//高斯模糊需要的控件和相应的控制函数
		blobPopSeekBar     = (PopSeekBar)findViewById(R.id.blob_pop_seek_bar);
		blobControlButton  = (ImageButton)findViewById(R.id.blob_control_button);
		blobSeekBarWrapper = (VerticalSeekBarWrapper)findViewById(R.id.blob_bar_wrapper);
		blobSeekBar        = (VerticalSeekBar)findViewById(R.id.blob_bar);
		
		
		//改变子体大小控件和相应的控制函数
		sizePopSeekBar     = (PopSeekBar)findViewById(R.id.size_pop_seek_bar);
		sizeControlButton  = (ImageButton)findViewById(R.id.size_control_button);
		sizeSeekBarWrapper = (VerticalSeekBarWrapper)findViewById(R.id.size_bar_wrapper);
		sizeSeekBar        = (VerticalSeekBar)findViewById(R.id.size_bar);
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////
		//拖动条的参数
		blobPopSeekBar.initParams(coords[1][2], coords[1][0], coords[1][1], R.drawable.ic_background_blob, R.drawable.ic_action_cancel,
				blobControlButton, blobSeekBarWrapper, blobSeekBar, popViewStatus);
		
		sizePopSeekBar.initParams(coords[3][2], coords[3][0], coords[3][1], R.drawable.ic_font_size, R.drawable.ic_action_cancel,
				sizeControlButton, sizeSeekBarWrapper, sizeSeekBar, popViewStatus);
        

		///////////////////////////////////////////////////////////////////////////////////////////////////////
		//分享等操作
		operate = (PopOperateMenu)findViewById(R.id.operate);
		operate.initParams(coords[2][2], coords[2][0], coords[2][1], R.drawable.text_bg_selector, 
				new int[]{R.string.lock_text, R.string.table_text, R.string.save_text, R.string.share_text}, 
				new int[]{R.color.lock_text_color, R.color.table_text_color, R.color.save_text_color, R.color.share_text_color}, 
				new int[]{R.drawable.lock_bg_selector, R.drawable.table_bg_selector, R.drawable.save_bg_selector, R.drawable.share_bg_selector}, 
				new int[]{R.drawable.ic_action_lock_closed, R.drawable.ic_action_phone, R.drawable.ic_action_save, R.drawable.ic_action_share},
				R.drawable.opetate_bg_selector,
				R.drawable.ic_action_add,
				popViewStatus);
		
		//---------------------------------------------------
		//文本编辑
		editText = (AdaptEditText)findViewById(R.id.edit_text);
		editText.initParams(viewCoord.w, viewCoord.h);
		//editText.setAdaptTextSize(160);
		//---------------------------------------------
		//图片控件
		imageView = (ImageView)findViewById(R.id.image_view);
		
		//-------------------------------------------------
		//存储 参数类
		contentPreference = new ContentPreference(this, backgroundColors.size(), fontColors.size(), sizeSeekBar.getMax());
		
		//------------------------------------------
		//状态类
		backgroundStatus = new BackgroundStatus();
		
		//-----------------------------------------------
		//等待动画
		waitingDialog = new WaitingDialog(this, R.style.WaitingProgressDialog);
	}
	
	//初始化状态
	private void initViewsStatus()
	{
		//得到 颜色索引 滑动块的 大小
		int bgColorIndex = contentPreference.getBgColorIndex();
		int fontColorIndex = contentPreference.getFontColorIndex();
		int blobProgress = contentPreference.getBlobProgress();
		int sizeProgress = contentPreference.getSizeProgress();
		
		//初始化 高斯模糊函数
		gaussBlur = new GaussBlur(this, viewCoord.getW(), viewCoord.getH());
		
		//初始化绘制文字函数
		drawText = new DrawText(this);
		
		//设置状态
		backgroundStatus.status = BackgroundStatus.STATUS.COLOR_BG;
		backgroundStatus.color  = backgroundColorMenu.getColorByIndex(bgColorIndex);
		backgroundStatus.originBitmap = null;
		backgroundStatus.blurBitmap = gaussBlur.blurColor(backgroundStatus.color, 0);
		
		//设置各个控件的状态
		backgroundColorMenu.setIndexSelected(bgColorIndex);
		fontColorMenu.setIndexSelected(fontColorIndex);
		blobSeekBar.setProgress(blobProgress);
		sizeSeekBar.setProgress(sizeProgress);
		
		//设置背景
		imageView.setImageBitmap(backgroundStatus.blurBitmap);
		
		//设置颜色 字体大小
		editText.setAdaptTextSize(sizeProgress);
		editText.setTextColor(fontColorMenu.getColorByIndex(fontColorIndex));
		
		//editText.setAdaptTextSize(160);
	}
	
	//初始化任务类
	private void initRunnables()
	{
		//模糊任务
		blobRunnable = new BlobRunnable();
		setWallPaperRunnable = new SetWallPaperRunnable();
		saveBitmapRunnable = new SaveBitmapRunnable();
		setLockWallpaperRunnable = new SetLockWallpaperRunnable();
		shareBitmapRunnable = new ShareBitmapRunnable();
	}
	
	//初始化监听函数
	private void initViewsListener()
	{
		//改变背景色
		backgroundColorMenu.setPopMenuListener(new PopMenuListener() 
		{
			@Override
			public void onSelectItem(ColorButton v, int index, int color) 
			{
				//切换颜色时调用 当选择的按钮 不同时 才会调用
				if(BackgroundStatus.STATUS.COLOR_BG == backgroundStatus.status)
				{
					//当前是以颜色为 背景色时
					Bitmap tempBitmap = backgroundStatus.blurBitmap;
					
					//存储颜色 的生成一个新的图片
					backgroundStatus.color = color;
					backgroundStatus.blurBitmap = gaussBlur.blurColor(color, 0);
					
					imageView.setImageBitmap(backgroundStatus.blurBitmap);
					blobSeekBar.setProgress(0);//模糊设为0
					
					//清理内存
					tempBitmap.recycle();
				}
				else if(BackgroundStatus.STATUS.BITMAP_BG == backgroundStatus.status)
				{
					//图片为背景
					Bitmap tempBitmap1 = backgroundStatus.originBitmap;
					Bitmap tempBitmap2 = backgroundStatus.blurBitmap;
					
					//存储颜色 的生成一个新的图片
					backgroundStatus.color = color;
					backgroundStatus.blurBitmap = gaussBlur.blurColor(color, 0);
					backgroundStatus.originBitmap = null;
					backgroundStatus.status = BackgroundStatus.STATUS.COLOR_BG;
					
					imageView.setImageBitmap(backgroundStatus.blurBitmap);
					blobSeekBar.setProgress(0);//模糊设为0
					
					tempBitmap1.recycle();
					tempBitmap2.recycle();
				}
			}
			
			@Override
			public void onClickAddButton() 
			{
				//请求一张图片当点击当前按钮是 求请求一张图片REQUEST_GET_IMAGE_CODE
				Intent intent = new Intent(Intent.ACTION_PICK, Media.EXTERNAL_CONTENT_URI);
				
				//请求返回结果
				startActivityForResult(intent, Constant.REQUEST_GET_IMAGE_CODE);
			}
		});
		
		//改变字体颜色
		fontColorMenu.setPopMenuListener(new PopMenuListener() 
		{
			@Override
			public void onSelectItem(ColorButton v, int index, int color) 
			{
				//改变字体颜色
				editText.setTextColor(color);
			}
			
			@Override
			public void onClickAddButton() 
			{
				
			}
		});
		
		//字体大小
		sizeSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() 
		{
			@Override
			public void onStopTrackingTouch(SeekBar arg0) 
			{
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
			}
			
			@Override
			public void onProgressChanged(SeekBar arg0, int progress, boolean fromUser) 
			{
				//以像素的格式设置字体大小
				editText.setAdaptTextSize(progress);
			}
		});
		
		//模糊效果
		blobSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() 
		{
			@Override
			public void onStopTrackingTouch(SeekBar arg0) 
			{
				//进行模糊
				//new BlobThread(blobSeekBar.getProgress()).start();
				blobRunnable.setBlobProgress(blobSeekBar.getProgress());
				new Thread(blobRunnable).start();
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
			}
			
			@Override
			public void onProgressChanged(SeekBar arg0, int progress, boolean fromUser) 
			{
			}
		});
		
		//生成图片 进行分享 图片 设置 保存等操作
		operate.setPopOperateMenuListener(new PopOperateMenuListener() 
		{
			@Override
			public void onClickItem(OperateButton v, int index) 
			{
				switch(index)
				{
				case 0:
					//锁屏
					new Thread(setLockWallpaperRunnable).start();
					break;
				case 1:
					//设置桌面壁纸
					new Thread(setWallPaperRunnable).start();
					break;
				case 2:
					//保存
					new Thread(saveBitmapRunnable).start();
					break;
				case 3:
					//分享
					new Thread(shareBitmapRunnable).start();
					break;
				}
			}
		});
	}

	/*
	 *利用子线程 对模糊进行处理
	 */
	private class BlobRunnable implements Runnable
	{
		//模糊程度
		private int blobProgress = 0;

		public void setBlobProgress(int blobProgress)
		{
			this.blobProgress = blobProgress;
		}
		
		@Override
		public void run()
		{
			//开始动画
			handler.sendEmptyMessage(START_WAITING_DIALOG);
			
			//标准化模糊程度
			if(blobProgress < 0)
				blobProgress = 0;
			if(blobProgress > blobSeekBar.getMax())
				blobProgress = blobSeekBar.getMax();

			//针对当前状态 进行模糊 处理
			if(BackgroundStatus.STATUS.COLOR_BG == backgroundStatus.status)
			{
				//当前是以颜色为背景
				backgroundStatus.recycleBitmap = backgroundStatus.blurBitmap;
				
				//根据模糊程度 设置模糊图片
				backgroundStatus.blurBitmap = gaussBlur.blurColor(backgroundStatus.color, blobProgress);
			}
			else if(BackgroundStatus.STATUS.BITMAP_BG == backgroundStatus.status)
			{
				//当前已图片为背景
				backgroundStatus.recycleBitmap = backgroundStatus.blurBitmap;
				
				//根据模糊程度 设置模糊图片
				backgroundStatus.blurBitmap = gaussBlur.blurBitmap2(backgroundStatus.originBitmap, blobProgress);
			}
			
			//改变背景图片
			handler.sendEmptyMessage(CHANGE_IMAGE_VIEW);
			
			//结束动画
			handler.sendEmptyMessage(STOP_WAITING_DIALOG);
		}
	}
	
	//设置桌面壁纸
	private class SetWallPaperRunnable implements Runnable
	{

		@Override
		public void run() 
		{
			//发送开始设置壁纸线程
			handler.sendEmptyMessage(START_SET_WALLPAPER);
			
			Bitmap bitmap = drawText.drawTextToBitmap(
					backgroundStatus.blurBitmap, 
					editText.getText().toString(), 
					editText.getCurrentTextColor(), 
					editText.getTextSize(), 
					editText.getTextVisibleWidth(),
					editText.getDx(),
					editText.getDy());
			
			try 
			{
				//设置桌面壁纸
				WallpaperManager.getInstance(MainActivity.this).setBitmap(bitmap);
				
				//发送成功消息
				handler.sendEmptyMessage(SUCCESS_SET_WALLPAPER);
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
				
				//发送失败消息
				handler.sendEmptyMessage(FAIL_SET_WALLPAPER);
			}
			finally
			{
				//清理内存
				bitmap.recycle();
			}
		}
	}
	
	private class SaveBitmapRunnable implements Runnable
	{
		@Override
		public void run() 
		{
			//发送开始设置壁纸线程
			handler.sendEmptyMessage(START_SAVE_BITMAP);
			
			//判断是否插入sdcard
			String status = Environment.getExternalStorageState();
			
			if(!status.equals(Environment.MEDIA_MOUNTED))
			{
				//没有sdcard直接返回
				handler.sendEmptyMessage(NO_SDCARD);
				return;
			}
			
			//构建图片
			Bitmap bitmap = drawText.drawTextToBitmap(
					backgroundStatus.blurBitmap, 
					editText.getText().toString(), 
					editText.getCurrentTextColor(), 
					editText.getTextSize(), 
					editText.getTextVisibleWidth(),
					editText.getDx(),
					editText.getDy());
			
			//路径 和 文件名
			String path = MainActivity.this.getString(R.string.save_bitmap_dir);
			String name = "/" + System.currentTimeMillis() + ".png";
			
			//打开目录
			File fileDir = new File(path);
			if(!fileDir.exists())
			{
				//没有就创建
				fileDir.mkdirs();
			}
			
			//真正的文件存储
			File file = new File(path + name);

	        FileOutputStream out = null;
			
	        try 
	        {
				out = new FileOutputStream(file);
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

				//文件的关闭
				out.flush();
				out.close();
				
				//--------------------------
		        //保存成功
				handler.sendEmptyMessage(SUCCESS_SAVE_BITMAP);
			} 
	        catch (FileNotFoundException e) 
	        {
				e.printStackTrace();
				
				//保存失败
				handler.sendEmptyMessage(FAIL_SAVE_BITMAP);
			}
	        catch (IOException e) 
	        {
				e.printStackTrace();
				
				//保存失败
				handler.sendEmptyMessage(FAIL_SAVE_BITMAP);
			}
	        finally
	        {
	        	//清理内存
	        	bitmap.recycle();
	        }
		}
	}

	//保存为锁屏壁纸
	private class SetLockWallpaperRunnable implements Runnable
	{
		@Override
		public void run() 
		{
			//发送开始设置壁纸线程
			handler.sendEmptyMessage(START_SET_LOCKPAPER);
			
			//得到图片
			Bitmap bitmap = drawText.drawTextToBitmap(
					backgroundStatus.blurBitmap, 
					editText.getText().toString(), 
					editText.getCurrentTextColor(), 
					editText.getTextSize(), 
					editText.getTextVisibleWidth(),
					editText.getDx(),
					editText.getDy());
			
			WallpaperManager mWallManager = WallpaperManager.getInstance(MainActivity.this);  
	        Class class1 = mWallManager.getClass();//获取类名
	        try 
	        {
	        	//获取设置锁屏壁纸的函数 
				Method setWallPaperMethod = class1.getMethod("setBitmapToLockWallpaper", Bitmap.class);
				
				//调用锁屏壁纸的函数，并指定壁纸的路径 
				setWallPaperMethod.invoke(mWallManager, bitmap);
				
				//成功设置
		        handler.sendEmptyMessage(SUCCESS_SET_LOCKPAPER);
			} 
	        catch (NoSuchMethodException e) 
			{
				//没有此类方法
				e.printStackTrace();
				
				//不支持设置壁纸
				handler.sendEmptyMessage(NOT_SUPPORT_LOCKPAPER);
			}
			catch (IllegalAccessException e) 
			{
				e.printStackTrace();
				
				//设置失败
				handler.sendEmptyMessage(FAIL_SET_LOCKPAPER);
			} 
	        catch (IllegalArgumentException e) {
				e.printStackTrace();
				
				//设置失败
				handler.sendEmptyMessage(FAIL_SET_LOCKPAPER);
			} 
	        catch (InvocationTargetException e) {
				e.printStackTrace();
				
				//设置失败
				handler.sendEmptyMessage(FAIL_SET_LOCKPAPER);
			}
	        finally
	        {
	        	bitmap.recycle();
	        }
		}
	}
	
	//分享图片
	private class ShareBitmapRunnable implements Runnable
	{
		@Override
		public void run() 
		{
			//发送开始分享
			handler.sendEmptyMessage(START_SHARE_BITMAP);
			
			//判断是否插入sdcard
			String status = Environment.getExternalStorageState();
			
			if(!status.equals(Environment.MEDIA_MOUNTED))
			{
				//没有sdcard直接返回
				handler.sendEmptyMessage(NO_SHARE_SDCARD);
				return;
			}
			
			//得到图片
			Bitmap bitmap = drawText.drawTextToBitmap(
					backgroundStatus.blurBitmap, 
					editText.getText().toString(), 
					editText.getCurrentTextColor(), 
					editText.getTextSize(), 
					editText.getTextVisibleWidth(),
					editText.getDx(),
					editText.getDy());
			
			//将图片进行存储
			//路径 和 文件名
			String path = MainActivity.this.getString(R.string.save_bitmap_dir);
			String name = "/" + System.currentTimeMillis() + ".png";
			
			//打开目录
			File fileDir = new File(path);
			if(!fileDir.exists())
			{
				//没有就创建
				fileDir.mkdirs();
			}
			
			//真正的文件存储
			File file = new File(path + name);

	        FileOutputStream out = null;
	        
	        try 
	        {
				out = new FileOutputStream(file);
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

				//文件的关闭
				out.flush();
				out.close();
				
				//--------------------------
		        //保存成功
				//从文件创建一个uri
				shareUri = Uri.fromFile(file);
				handler.sendEmptyMessage(SUCCESS_SHARE_BITMAP);
			} 
	        catch (FileNotFoundException e) 
	        {
				e.printStackTrace();
				
				//保存失败
				handler.sendEmptyMessage(FAIL_SHARE_BITMAP);
			}
	        catch (IOException e) 
	        {
				e.printStackTrace();
				
				//保存失败
				handler.sendEmptyMessage(FAIL_SHARE_BITMAP);
			}
	        finally
	        {
	        	//清理内存
	        	bitmap.recycle();
	        }
		}
	}
	
	//从给定的文件中 读取相应的颜色 值
	private ArrayList<Integer> getColorsFromXML(int id)
	{
		//存储颜色
		ArrayList<Integer> colors = new ArrayList<Integer>();
		
		// 通过Resources，获得XmlResourceParser实例
		XmlResourceParser xrp = this.getResources().getXml(id);
		
		try 
		{
			// 如果没有到文件尾继续执行
			while (xrp.getEventType() != XmlResourceParser.END_DOCUMENT) 
			{
				if(xrp.getEventType() == XmlResourceParser.START_TAG)
				{
					if(xrp.getName().equals("color") && xrp.getAttributeName(0).equals("value"))
					{   
						//将颜色转换成int
						int color = Color.parseColor(xrp.getAttributeValue(0));
						colors.add(new Integer(color));
                    }   
				}
			
                xrp.next(); //下一个标签   
			}
		} 
		catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return colors;
	}
}



















