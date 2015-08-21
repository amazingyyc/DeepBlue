package com.amazing.view;

import java.util.ArrayList;

import com.amazing.utils.Point;
import com.amazing.utils.PopOperateMenuListener;
import com.amazing.utils.PopView;
import com.amazing.utils.PopViewStatus;
import com.amazing.utils.PopViewStatus.POP_STATUS;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

//自定义的弹出操作按钮
public class PopOperateMenu extends RelativeLayout  implements PopView
{
	private final static int BUTTON_INTER = 15;	//按钮之间的间隔单位为像素
	private final static int FAR_END_INTER = 110;	//每个弹出按钮的最远距离 和 最终距离的差值 负责显示回弹效果
	private static final int DELAYED_TIME = 30;// 延迟动画的时间

	int[] textIds;	//文字id数组
	int[] textColors;	//文字颜色
	int textViewBgId = -1;	//文字背景 只有一个
	
	int[] oeprateBgResIds;	//按钮的背景数组
	int[] operateImageResIds;	//按钮的前景图片
	
	int controlBgResId = -1;
	int controlImageResId = -1;
	
	ArrayList<OperateButton> popButtons;	//所有的按钮集合
	Button controlButton = null;	//控制按钮
	
	//布局信息
	int width;
	int left;
	int bottom;

	private PopViewStatus popViewStatus = null;
	
	//回调函数
	PopOperateMenuListener popOperateMenuListener = null;
	
	//用于动画的回调
	private Handler mHandler = new Handler();
	
	Context context = null;
	AttributeSet attrs = null;
	
	public PopOperateMenu(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		
		this.context = context;
		this.attrs   = attrs;
	}
	
	public PopView getSef()
	{
		return this;
	}

	public void initParams(int width, int left, int bottom, int textViewBgId,
			int[] textIds, int[] textColors, int[] oeprateBgResIds, int[] operateImageResIds,
			int controlBgResId, int controlImageResId,
			PopViewStatus popViewStatus)
	{
		this.width  = width;
		this.left   = left;
		this.bottom = bottom;
		
		this.textViewBgId = textViewBgId;
		this.textIds = textIds;
		this.textColors = textColors;
		this.oeprateBgResIds = oeprateBgResIds;
		this.operateImageResIds = operateImageResIds;
		this.controlBgResId = controlBgResId;
		this.controlImageResId = controlImageResId;
		
		this.popViewStatus = popViewStatus;
		
		initViews();
	}

	//初始化各种views
	private void initViews()
	{
		//初始化本view
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		lp.leftMargin = 0;
		lp.bottomMargin = 0;
		lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		
		this.setLayoutParams(lp);
		this.setClipChildren(false);
		
		int startX = left;
		int startY = bottom;
		int x = startX;
		int y = startY;
		int delta = width + BUTTON_INTER;
		
		popButtons = new ArrayList<OperateButton>();
		
		//添加各种items的按钮
		int size = textIds.length;
		for(int i = 0; i < size; ++i)
		{
			y += delta;
			
			OperateButton operateButton = new OperateButton(context, attrs);
			operateButton.setClipChildren(false);
			operateButton.initParams(width, textIds[i], textViewBgId, textColors[i], oeprateBgResIds[i], operateImageResIds[i]);
			operateButton.setPoints(new Point(startX, startY), new Point(x, y), new Point(x, y + FAR_END_INTER));
			
			//设置标记
			operateButton.operateButton.setTag(i);
			
			//按钮回调
			operateButton.operateButton.setOnClickListener(itemClickListener);
			
			addView(operateButton);
			popButtons.add(operateButton);
		}
		
		//controlbutton的初始化
		//初始化本view
		RelativeLayout.LayoutParams controlLayout = new RelativeLayout.LayoutParams(width, width);
		controlLayout.leftMargin = startX;
		controlLayout.bottomMargin = startY;
		controlLayout.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		controlLayout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		
		controlButton = new Button(context, attrs);
		controlButton.setBackgroundResource(controlBgResId);
		
		controlButton.setLayoutParams(controlLayout);
		
		controlButton.setOnClickListener(controlClickListener);
		
		addView(controlButton);		//控件中添加按钮
	}
	
	//--------------------------------------------------------------
	public void setPopOperateMenuListener(PopOperateMenuListener popOperateMenuListener)
	{
		this.popOperateMenuListener = popOperateMenuListener;
	}
	
	//各种按钮的回调
	private View.OnClickListener itemClickListener = new View.OnClickListener()
	{
		@Override
		public void onClick(View view) 
		{
			if(POP_STATUS.EXPANDED != popViewStatus.popStatus)
				return;
			
			//得到索引
			int index = Integer.parseInt(String.valueOf(view.getTag()));
			
			if(0 <= index && index < popButtons.size())
			{
				//得到按钮
				OperateButton button = popButtons.get(index);
				
				//执行回调
				if(null != popOperateMenuListener)
				{
					popOperateMenuListener.onClickItem(button, index);
				}
			}
		}
	};
	
	
	
	
	
	///////////////////////////////////////////////////////////////////
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
	
	
	
	//各种回调函数
	private View.OnClickListener controlClickListener = new View.OnClickListener() 
	{
		@Override
		public void onClick(View v)
		{
			//正在动画直接跳出
			if(POP_STATUS.SHRINKING == popViewStatus.popStatus || POP_STATUS.EXPANDING == popViewStatus.popStatus)	
				return;
			
			//根据状态设定执行的动画效果
			if(POP_STATUS.SHRINKED == popViewStatus.popStatus)
				expand();
			else if(POP_STATUS.EXPANDED == popViewStatus.popStatus)
				shrink();
		}
	};
	
	//执行扩展动画
	private void expand()
	{
		//修改状态
		popViewStatus.popStatus = POP_STATUS.EXPANDING;
		
		int size = popButtons.size();
		for(int i = size - 1; i >= 0; --i)
		{
			//延迟时间发送一个动画请求
			mHandler.postDelayed(new PopRunnable(i), DELAYED_TIME * (size - 1 - i));
		}

		//修改状态
		mHandler.postDelayed(new PopRunnable(size), DELAYED_TIME * (size - 1) + 2 * OperateButton.CONTINUE_TIME);
	}
	
	//收缩动画
	private void shrink()
	{
		//修改状态
		popViewStatus.popStatus = POP_STATUS.SHRINKING;

		int size = popButtons.size();
		for(int i = 0; i < size; ++i)
		{
			mHandler.postDelayed(new PopRunnable(i), DELAYED_TIME * i);
		}
		
		//改变状态消息
		mHandler.postDelayed(new PopRunnable(size), DELAYED_TIME * (size - 1) + OperateButton.CONTINUE_TIME);
	}
	
	//定义一个内部类 来执行动画的操作
	private class PopRunnable implements Runnable
	{
		int index;
		
		public PopRunnable(int index) 
		{
			this.index = index;
		}
		
		@Override
		public void run() 
		{
			int size = popButtons.size();
			if(index == size)
			{
				//设置状态
				if(POP_STATUS.EXPANDING == popViewStatus.popStatus)
				{
					popViewStatus.popStatus = POP_STATUS.EXPANDED;
					popViewStatus.popView = getSef();
				}
					
				else if(POP_STATUS.SHRINKING == popViewStatus.popStatus)
				{
					popViewStatus.popStatus = POP_STATUS.SHRINKED;
					popViewStatus.popView = null;
				}
					
			}
			else if(0 <= index && index < size)
			{
				OperateButton button = null;

				button = popButtons.get(index);
				
				//根据状态设置不同的动画
				if(POP_STATUS.EXPANDING == popViewStatus.popStatus)
					button.expandAnim();
				else if(POP_STATUS.SHRINKING == popViewStatus.popStatus)
					button.shrinkAnim();
			}
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
		shrink();
	}
}



















