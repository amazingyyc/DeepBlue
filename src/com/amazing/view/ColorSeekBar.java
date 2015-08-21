package com.amazing.view;

import com.amazing.utils.Point;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

public class ColorSeekBar extends SeekBar
{
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

	public ColorSeekBar(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
	}
}




















