package com.amazing.view;

import com.amazing.utils.Point;

import com.amazing.deepblue.R;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

//自定义的ColorButton 模拟单选按钮
public class ColorButton extends ImageButton
{
	//常量
	//private final static String STROKE_COLOR = "#FFAAAAAA";	//描边颜色
	private final static int STROKE_WIDTH = 5;	//描边宽度
	public final static int CONTINUE_TIME = 150;// 持续动画的时间
	
	///////////////////////////////////////////////////////////
	private int fillColor;	//填充颜色
	private int width;	//统一为圆形按钮 背景圆形半径
	
	/////////////////////////////////////////
	//坐标信息 用于动画操作
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

	Context context = null;
	AttributeSet attrs = null;
	
	public ColorButton(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		
		this.context = context;
		this.attrs = attrs;
	}
	
	public ColorButton(int color, int w, Context context, AttributeSet attrs) 
	{
		this(context, attrs);
		
		//设置颜色
	    fillColor = color;			//内部填充颜色
	    width = w;	//按钮的半径

	    //设置背景
	    this.setBackground(getBg());
	}
	
	//执行扩展动画
	public void expandAnim()
	{
		this.startAnimation(farAnim);
	}
	
	//收缩动画
	public void shrinkAnim()
	{
		this.startAnimation(startAnim);
	}
	
	//开始从far 到end动画
	private void startFarToEndAnim()
	{
		this.startAnimation(endAnim);
	}
	
	//清除动画
	private void clearAnim()
	{
		this.clearAnimation();
	}
	
	//设置布局信息
	private void setStartLayoutParams()
	{
		this.setLayoutParams(startParams);
	}
	
	//设置布局信息
	private void setEndLayoutParams()
	{
		this.setLayoutParams(endParams);
	}
	
	//设置布局信息
	private void setFarLayoutParams()
	{
		this.setLayoutParams(farParams);
	}
	
	//设置是否可见
	private void setVisibleOn()
	{
		this.setAlpha(1.0f);
	}
	//设置是否可见
	private void setVisibleOff()
	{
		this.setAlpha(0.0f);
	}

	//设置坐标信息
	public void setPoints(Point startPoint, Point endPoint, Point farPoint)
	{
		this.startPoint = startPoint;
		this.endPoint   = endPoint;
		this.farPoint   = farPoint;

		//初始化动画
		initLayoutParams();
		initAnimation();
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
			}
			
			@Override
			public void onAnimationRepeat(Animation arg0) {
			}
			
			@Override
			public void onAnimationEnd(Animation arg0) 
			{
				//动画结束后执行回调
				clearAnim();
				setStartLayoutParams();
				
				//不可见加不可点击
				setVisibleOff();
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
				clearAnim();
				setEndLayoutParams();
			}
		});
		
		farAnim.setAnimationListener(new AnimationListener() 
		{
			@Override
			public void onAnimationStart(Animation arg0) 
			{
				setVisibleOn();
			}
			
			@Override
			public void onAnimationRepeat(Animation arg0) {
			}
			
			@Override
			public void onAnimationEnd(Animation arg0) 
			{
				//动画结束后执行回调
				clearAnim();
				setFarLayoutParams();
				
				startFarToEndAnim();
			}
		});
	}
	
	//初始化布局信息
	private void initLayoutParams()
	{
		//开始的布局信息
		startParams = new RelativeLayout.LayoutParams(width, width);
		startParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		startParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		startParams.leftMargin = startPoint.x;
		startParams.bottomMargin = startPoint.y;
		
		//弹出后的布局信息
		endParams = new RelativeLayout.LayoutParams(width, width);
		endParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		endParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		endParams.leftMargin   = endPoint.x;
		endParams.bottomMargin = endPoint.y;
		
		farParams = new RelativeLayout.LayoutParams(width, width);
		farParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		farParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		farParams.leftMargin   = farPoint.x;
		farParams.bottomMargin = farPoint.y;
		
		//设置为开始的布局信息
		setLayoutParams(startParams);
		
		//不可见
		setVisibleOff();
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

	//得到背景GradientDrawable 
	private GradientDrawable getBg()
	{
		GradientDrawable gd = new GradientDrawable();//创建drawable
	    gd.setColor(fillColor);
	    gd.setCornerRadius(width / 2);
	    gd.setStroke(STROKE_WIDTH, context.getResources().getColor(R.color.stroke_color));
	    
	    return gd;
	}
	
	/////////////////////////////////////////
	//得到填充颜色
	public int getFillColor()
	{
		return this.fillColor;
	}
}


















