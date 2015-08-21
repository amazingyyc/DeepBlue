package com.amazing.view;

import com.amazing.utils.Point;
import com.amazing.utils.PopView;
import com.amazing.utils.PopViewStatus;
import com.amazing.utils.PopViewStatus.POP_STATUS;

import android.content.Context;

import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

//弹出的拖动条
public class PopSeekBar extends RelativeLayout  implements PopView
{
	//拖动快最左面个屏幕边缘的距离
	private final static int VIEW_INTER = 15;	//view之间的间隔
	private final static int SEEKBAR_HEIGHT = 500;	//bar的高度
	private final static int FAR_END_INTER = 110;	//每个弹出按钮的最远距离 和 最终距离的差值 负责显示回弹效果
	public final static int CONTINUE_TIME = 150;// 持续动画的时间

	//资源id
	private int controlResId = -1;	//控制按钮的资源id
	private int closeResId = -1;	//当按钮弹出式 显示的关闭资源id
	
	//对应的按钮
	private ImageButton controlButton = null;	//控制按钮
	private VerticalSeekBarWrapper     seekBarWrapper  = null;	//拖动条
	private VerticalSeekBar seekBar = null;

	//控制按钮的宽度
	int width;
	
	//controlButton相对于夫layout的偏移数值
	private int left = 0;
	private int bottom = 0;
	
	//用于动画的属性
	protected Point startPoint = null;	//坐标的真正含义是 相对于夫组件的偏移 即(toLeft, toBottom)(x, y)
	protected Point endPoint   = null;
	protected Point farPoint   = null;
	
	//动画
	Animation startAnim = null;
	Animation endAnim = null;
	Animation farAnim = null;
	
	//布局信息 一个start的布局信息 一个 end的布局信息
	RelativeLayout.LayoutParams startParams = null;
	RelativeLayout.LayoutParams endParams = null;
	RelativeLayout.LayoutParams farParams = null;
	
	/*
	//状态信息
	//boolean isExpand = false;
	private enum POP_STATUS
	{
		SHRINKED,	//已经收缩状态
		SHRINKING,	//正在收缩
		EXPANDED,	//已经扩展状态
		EXPANDING, 	//正在扩展状态
	};
	
	private POP_STATUS popStatus = POP_STATUS.SHRINKED;*/
	
	private PopViewStatus popViewStatus = null;
	
	//用于动画的回调
	private Handler mHandler = new Handler();
	
	private Context context = null;
	private AttributeSet attrs = null;

	public PopSeekBar(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		
		this.context = context;
		this.attrs   = attrs;
	}
	
	public PopView getSef()
	{
		return this;
	}
	
	
	////////////////////////////////////////////////////////////////////
	//监听本RelativeLayout 的触摸事件
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		if(POP_STATUS.EXPANDING == popViewStatus.popStatus || POP_STATUS.SHRINKING == popViewStatus.popStatus)
			return true;
		
		if(POP_STATUS.EXPANDED == popViewStatus.popStatus && this != popViewStatus.popView)
			return false;
		
		//如果已经有控件处于弹出状态 则
		if(POP_STATUS.EXPANDED == popViewStatus.popStatus && this == popViewStatus.popView)
		{
			//设置当前处于弹出状态的控件进行 收缩
			popViewStatus.popView.executeShrink();
			
			return true;
		}

		return super.onTouchEvent(event);
	}
	
	//返回true表示 不向 子view传递 触摸事件 交给 本onTouchEvent处理
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event)
	{
		if(POP_STATUS.EXPANDING == popViewStatus.popStatus || POP_STATUS.SHRINKING == popViewStatus.popStatus)
			return true;
		
		if(POP_STATUS.EXPANDED == popViewStatus.popStatus && this != popViewStatus.popView)
			return true;
		
		if(POP_STATUS.EXPANDED == popViewStatus.popStatus && this == popViewStatus.popView)
			return false;

		return super.onInterceptTouchEvent(event);
	}
	
	//初始化需要的参数 
	public void initParams(int width, int left, int bottom, int controlResId, int closeResId, ImageButton controlButton, VerticalSeekBarWrapper seekBarWrapper, VerticalSeekBar seekBar, PopViewStatus popViewStatus)
	{
		this.width = width;
		this.left = left;
		this.bottom = bottom; 
		
		this.controlResId = controlResId;
		this.closeResId   = closeResId;
		
		this.controlButton = controlButton;
		this.seekBarWrapper = seekBarWrapper;
		this.seekBar = seekBar;
		
		this.popViewStatus = popViewStatus;
		
		initPoints();
		initLayoutParams();
		initAnimation();
		initViews();
	}
	
	private void initPoints()
	{
		startPoint = new Point(left, -(SEEKBAR_HEIGHT - VIEW_INTER));
		endPoint   = new Point(left, bottom + width + VIEW_INTER);
		farPoint   = new Point(left, endPoint.y + FAR_END_INTER);
	}
	
	//初始化布局信息
	private void initLayoutParams()
	{
		//本RelativeLayout的布局信息
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		lp.leftMargin = 0;
		lp.bottomMargin = 0;
		lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		
		this.setLayoutParams(lp);
		
		//开始的布局信息
		startParams = new RelativeLayout.LayoutParams(width, SEEKBAR_HEIGHT);
		startParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		startParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		startParams.leftMargin = startPoint.x;
		startParams.bottomMargin = startPoint.y;
		
		//弹出后的布局信息
		endParams = new RelativeLayout.LayoutParams(width, SEEKBAR_HEIGHT);
		endParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		endParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		endParams.leftMargin   = endPoint.x;
		endParams.bottomMargin = endPoint.y;
		
		farParams = new RelativeLayout.LayoutParams(width, SEEKBAR_HEIGHT);
		farParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		farParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		farParams.leftMargin   = farPoint.x;
		farParams.bottomMargin = farPoint.y;
	}
	
	//执行扩展动画
	public void expandAnim()
	{
		seekBarWrapper.startAnimation(farAnim);
	}
	
	//收缩动画
	public void shrinkAnim()
	{
		seekBarWrapper.startAnimation(startAnim);
	}
	
	//开始从far 到end动画
	private void startFarToEndAnim()
	{
		seekBarWrapper.startAnimation(endAnim);
	}
	
	//初始化其他属性
	private void initAnimation()
	{
		//三个动画 start-far far-end end-start
		startAnim = translateAnim(startPoint, endPoint, CONTINUE_TIME);	//end - start
		endAnim   = translateAnim(endPoint, farPoint, CONTINUE_TIME);	//far - end
		farAnim   = translateAnim(farPoint, startPoint, CONTINUE_TIME);	//start - far
		
		//从end - 到start
		startAnim.setAnimationListener(new AnimationListener() 
		{
			@Override
			public void onAnimationStart(Animation arg0) 
			{
				seekBar.setEnabled(false);
			}
			
			@Override
			public void onAnimationRepeat(Animation arg0) {
			}
			
			@Override
			public void onAnimationEnd(Animation arg0) 
			{
				//动画结束后执行回调
				seekBarWrapper.clearAnimation();
				seekBarWrapper.setLayoutParams(startParams);
				seekBarWrapper.setAlpha(0.0f);
				
				//状态
				popViewStatus.popStatus = POP_STATUS.SHRINKED;
				popViewStatus.popView = null;
			}
		});
		
		endAnim.setAnimationListener(new AnimationListener() 
		{
			@Override
			public void onAnimationStart(Animation arg0) {
			}
			
			@Override
			public void onAnimationRepeat(Animation arg0) {
			}
			
			@Override
			public void onAnimationEnd(Animation arg0) 
			{
				//动画结束后执行回调
				seekBarWrapper.clearAnimation();
				seekBarWrapper.setLayoutParams(endParams);
				seekBar.setEnabled(true);
				
				popViewStatus.popStatus = POP_STATUS.EXPANDED;
				popViewStatus.popView = getSef();
			}
		});
		
		farAnim.setAnimationListener(new AnimationListener() 
		{
			@Override
			public void onAnimationStart(Animation arg0) 
			{
				seekBarWrapper.setAlpha(1.0f);
			}
			
			@Override
			public void onAnimationRepeat(Animation arg0) {
			}
			
			@Override
			public void onAnimationEnd(Animation arg0) 
			{
				//动画结束后执行回调
				seekBarWrapper.clearAnimation();
				seekBarWrapper.setLayoutParams(farParams);
				
				startFarToEndAnim();
			}
		});
	}
	
	//得到一个移动动画
	private Animation translateAnim(Point fromPoint, Point toPoint, long durationMillis) 
	{
		TranslateAnimation anTransformation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.ABSOLUTE, toPoint.x - fromPoint.x, 
																	 Animation.RELATIVE_TO_SELF, 0, Animation.ABSOLUTE, toPoint.y - fromPoint.y);
		
		//持续时间
		anTransformation.setDuration(durationMillis);
		
		/*
		 * 但是，animation只是操作View 的位图表示（bitmap representation），而不是真正的改变View的位置
			动画结束后，View回到了原来的位置，setFillAfter 和 setFillBefore 并不能解决这个问题，
			要使View保持动画结束时的状态，必须另外改变View的属性（动画并不会帮助你改变View的属性），
			setFillAfter 和 setFillBefore 只能改变动画的属性
			为什么会有setFillAfter 和 setFillBefore这两个方法：
			是因为有动画链的原因，假定你有一个移动的动画紧跟一个淡出的动画，
			如果你不把移动的动画的setFillAfter置为true，那么移动动画结束后，
			View会回到原来的位置淡出，如果setFillAfter置为true， 就会在移动动画结束的位置淡出
		 */
		anTransformation.setFillAfter(true);
		anTransformation.setFillBefore(false);
		/*
		 * 【功能说明】该方法用于设置一个动画效果执行完毕后，View对象保留在终止的位置。该方法的执行，
		 * 需要首先通过setFillEnabled方法使能填充效果，否则设置无效。
		 */
		anTransformation.setFillEnabled(true);
		
		return anTransformation;
	}
	
	//设置属性
	private void initViews()
	{
		//control的布局文件
		RelativeLayout.LayoutParams controlLayout = new RelativeLayout.LayoutParams(width, width);
		controlLayout.leftMargin = left;
		controlLayout.bottomMargin = bottom;
		controlLayout.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		controlLayout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		
		controlButton.setLayoutParams(controlLayout);
		controlButton.setOnClickListener(controlClickListener);
		
		//seekBarWrapper布局文件
		seekBarWrapper.setLayoutParams(startParams);
		seekBarWrapper.setAlpha(0.0f);
		seekBar.setEnabled(false);
	}
	
	//各种回调函数
	private View.OnClickListener controlClickListener = new View.OnClickListener() 
	{
		@Override
		public void onClick(View v)
		{
			if(POP_STATUS.EXPANDING == popViewStatus.popStatus || POP_STATUS.SHRINKING == popViewStatus.popStatus)
				return;
			
			//根据状态设定执行的动画效果
			if(POP_STATUS.EXPANDED == popViewStatus.popStatus)
			{
				//设置关闭图标
				controlButton.setImageResource(controlResId);
				
				popViewStatus.popStatus = POP_STATUS.SHRINKING;
				mHandler.post(new PopRunnable());
			}
			else if(POP_STATUS.SHRINKED == popViewStatus.popStatus)
			{
				//设置正常图标
				controlButton.setImageResource(closeResId);
				
				popViewStatus.popStatus = POP_STATUS.EXPANDING;
				mHandler.post(new PopRunnable());
			}
		}
	};
	
	//定义一个内部类 来执行动画的操作
	private class PopRunnable implements Runnable
	{
		@Override
		public void run() 
		{
			//执行动画
			if(POP_STATUS.EXPANDING == popViewStatus.popStatus)
				expandAnim();
			else if(POP_STATUS.SHRINKING == popViewStatus.popStatus)
				shrinkAnim();
		}
	}
	
	///////////////////////////////////////////////////////////////////
	///执行 扩展或者搜索 用于外界函数 调用
	@Override
	public void executeExpand()
	{
	
	}
	
	@Override
	public void executeShrink()
	{
		//设置关闭图标
		controlButton.setImageResource(controlResId);
		
		popViewStatus.popStatus = POP_STATUS.SHRINKING;
		mHandler.post(new PopRunnable());
	}

}





















