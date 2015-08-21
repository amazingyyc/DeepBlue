package com.amazing.view;

import java.util.ArrayList;

import com.amazing.deepblue.R;
import com.amazing.utils.Point;
import com.amazing.utils.PopMenuListener;
import com.amazing.utils.PopView;
import com.amazing.utils.PopViewStatus;
import com.amazing.utils.PopViewStatus.POP_STATUS;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

//自定义菜单类
public class PopMenu extends RelativeLayout implements PopView
{
	private final static int BUTTON_INTER = 6;	//按钮之间的间隔单位为像素
	private final static int FAR_END_INTER = 110;	//每个弹出按钮的最远距离 和 最终距离的差值 负责显示回弹效果
	private static final int DELAYED_TIME = 20;// 延迟动画的时间
	
	//自定义控件：当前控件必须包含一个控制按钮 控制其他按钮是否显现 而且 包含一个add按钮 此按钮负责调用其他操作
	//单选组合：负责选择操作 可以同时选择或者只能选择一个
	
	private int selectResId = -1;	//选择按钮 当某一个按钮被选择是 就设置显示资源selectResId
	private int controlResId = -1;	//控制按钮的资源id
	private int closeResId = -1;	//当按钮弹出式 显示的关闭资源id
	private int addResId = -1;	//添加按钮的资源id
	
	private ArrayList<Integer> colors = null;
	private Context context = null;
	private AttributeSet attrs = null;
	
	//各种按钮
	private ImageButton controlButton = null;	
	private ColorButton addButton = null;	//设置其他操作
	private ArrayList<ColorButton> popButtons = null;	//单选按钮 要么选择一个 要吗都不选
	
	private int width;	//所有的按钮均为圆形 width表示为宽度 和 高度
	
	//RelativeLayout相对于父控件的布局
	private int left;
	private int bottom;
	
	//执行各种回调 和 选择状态
	private int selectIndex = -1;	//0,size-1表示 选择对应的按钮 -1表示没有任何选择
	private PopMenuListener popMenuListener = null;

	private PopViewStatus popViewStatus = null;
	
	//用于动画的回调
	private Handler mHandler = new Handler();

	public PopMenu(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		
		this.context = context;
		this.attrs   = attrs;
	}
	
	public PopView getSef()
	{
		return this;
	}
	
	//宽度 控制按钮索引 添加按钮索引 颜色
	public void initParams(int width, int left, int bottom, int selectResId, int controlResId, int closeResId, int addResId, ArrayList<Integer> colors, PopViewStatus popViewStatus)
	{
		this.width        = width;
		this.left         = left;
		this.bottom       = bottom;
		
		this.selectResId  = selectResId;
		this.controlResId = controlResId;
		this.closeResId   = closeResId;
		this.addResId     = addResId;
		this.colors       = colors;
		
		this.popViewStatus = popViewStatus;
		
		initLayoutParams();
		addButtons();
	}
	
	//根据传入的left bottom设置本空间
	private void initLayoutParams()
	{
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		lp.leftMargin = 0;
		lp.bottomMargin = 0;
		lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		
		this.setLayoutParams(lp);
	}

	//初始化各种参数 并添加按钮
	private void addButtons()
	{
		//坐标信息
		int startX = left;
		int startY = bottom;
		int x = startX;
		int y = startY;
		int delta = width + BUTTON_INTER;
		
		//首先初始化addButton
		//布局参数
		if(-1 != addResId)
		{
			addButton = new ColorButton(Color.WHITE, width, context, attrs);
			addButton.setBackgroundResource(R.drawable.add_button_bg_selector);
			
			y += delta;
			addButton.setPoints(new Point(startX, startY), new Point(x, y), new Point(x, y + FAR_END_INTER));
			addButton.setScaleType(ImageButton.ScaleType.CENTER_INSIDE);
			addButton.setImageResource(addResId);
			
			//设置回调
			addButton.setOnClickListener( addClickListener );
			
			//设置标记
			addButton.setTag(-1);
			
			//添加按钮
			addView(addButton);
		}
		
		//弹出按钮
		popButtons = new ArrayList<ColorButton>();
		
		int size = colors.size();
		for(int i = 0; i < size; ++i)
		{
			//颜色按钮
			ColorButton popButton = new ColorButton(colors.get(i).intValue(), width, context, attrs);
			
			y += delta;
			popButton.setPoints(new Point(startX, startY), new Point(x, y), new Point(x, y + FAR_END_INTER));
			popButton.setScaleType(ImageButton.ScaleType.CENTER_INSIDE);
			
			//设置标记
			popButton.setTag(i);
			addView(popButton);
			
			//设置回调函数
			popButton.setOnTouchListener(itemTouchListener);
			
			//添加到链表
			popButtons.add(popButton);
		}
		
		//控制按钮的初始化
		RelativeLayout.LayoutParams controlParams = new RelativeLayout.LayoutParams(width, width);
		controlParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		controlParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		controlParams.leftMargin   = startX;
		controlParams.bottomMargin = startY;

		controlButton = new ImageButton(context, attrs);
		controlButton.setScaleType(ImageButton.ScaleType.CENTER_INSIDE);
		controlButton.setImageResource(controlResId);
		controlButton.setLayoutParams(controlParams);
		controlButton.setBackgroundResource(R.drawable.button_bg_selector);
		
		//回调函数
		controlButton.setOnClickListener(controlClickListener);	
		
		addView(controlButton);		//控件中添加按钮
	}
	
	//--------------------------------------------------------
	//根据索引返回颜色值
	public int getColorByIndex(int index)
	{
		if(index < 0 || index >= popButtons.size())
			index = 0;
		
		return popButtons.get(index).getFillColor();
	}
	
	//-------------------------------------
	//设置当前的索引
	public void setIndexSelected(int index)
	{
		//如果与当前的选择索引相同直接返回
		if(index == selectIndex) return;
		
		if(0 <= index && index <= popButtons.size())
		{
			//得到索引
			ColorButton button = popButtons.get(index);
			
			//已经选中一个按钮
			if(0 <= selectIndex && selectIndex < popButtons.size())
			{
				//设置前景不不显示
				popButtons.get(selectIndex).setImageAlpha(0);
			}
			
			selectIndex = index;
			button.setImageAlpha(255);
			
			if(Color.WHITE == button.getFillColor())
				button.setImageResource(R.drawable.ic_action_tick_black);
			else
				button.setImageResource(selectResId);
		}
	}
	
	//取消选择 即 什么都不选
	public void cancelSelected()
	{
		//已经选中一个按钮
		if(0 <= selectIndex && selectIndex < popButtons.size())
		{
			//设置前景不不显示
			popButtons.get(selectIndex).setImageAlpha(0);
			
			selectIndex = -1;
		}
	}
	
	//得到当前选择
	public int getSelectedIndex()
	{
		return selectIndex;
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
			{
				//设置关闭图标
				controlButton.setImageResource(closeResId);
				
				expand();
			}
				
			else if(POP_STATUS.EXPANDED == popViewStatus.popStatus)
			{
				//设置正常图标
				controlButton.setImageResource(controlResId);
				
				shrink();
			}
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

		//发送一个-1消息 addButton的动画效果
		if(null != addButton)
		{
			mHandler.postDelayed(new PopRunnable(-1), DELAYED_TIME * size);
			
			//发送一个size消息表示 改变状态
			mHandler.postDelayed(new PopRunnable(size), DELAYED_TIME * size + 2 * ColorButton.CONTINUE_TIME);
		}
		else
		{
			//发送一个size消息表示 改变状态
			mHandler.postDelayed(new PopRunnable(size), DELAYED_TIME * (size - 1) + 2 * ColorButton.CONTINUE_TIME);
		}
		
		
	}
	
	//收缩动画
	private void shrink()
	{
		//修改状态
		popViewStatus.popStatus = POP_STATUS.SHRINKING;
		
		long delayed_time = 0;
		if(null != addButton)
		{
			mHandler.postDelayed(new PopRunnable(-1), 0);
			delayed_time += DELAYED_TIME;
		}
		
		int size = popButtons.size();
		for(int i = 0; i < size; ++i)
		{
			mHandler.postDelayed(new PopRunnable(i), DELAYED_TIME * i + delayed_time);
		}
		
		//改变状态消息
		mHandler.postDelayed(new PopRunnable(size), DELAYED_TIME * (size - 1) + delayed_time + ColorButton.CONTINUE_TIME);
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
			else if(-1 <= index && index < size)
			{
				ColorButton button = null;
				
				if(-1 == index)
					button = addButton;
				else
					button = popButtons.get(index);
				
				//根据状态设置不同的动画
				if(POP_STATUS.EXPANDING == popViewStatus.popStatus)
					button.expandAnim();
				else if(POP_STATUS.SHRINKING == popViewStatus.popStatus)
					button.shrinkAnim();
			}
		}
	}
	
	///////////////////////////////////////////////////
	//设置监听接口
	public void setPopMenuListener(PopMenuListener popMenuListener)
	{
		this.popMenuListener = popMenuListener;
	}
	
	//添加按钮的回回调
	private View.OnClickListener addClickListener = new View.OnClickListener() 
	{
		@Override
		public void onClick(View arg0)
		{
			// TODO Auto-generated method stub
			//执行回调
			if(null != popMenuListener)
				popMenuListener.onClickAddButton();
		}
	};
	
	//选择按钮的回调
	private View.OnTouchListener itemTouchListener = new View.OnTouchListener()
	{
		@Override
		public boolean onTouch(View view, MotionEvent event) 
		{
			if(POP_STATUS.EXPANDED != popViewStatus.popStatus)
				return false;
			
			if(event.getAction() != MotionEvent.ACTION_DOWN)
				return false;
			
			//得到索引
			int index = Integer.parseInt(String.valueOf(view.getTag()));
			
			//如果与当前的选择索引相同直接返回
			if(index == selectIndex)	return false;
			
			if(0 <= index && index <= popButtons.size())
			{
				//得到索引
				ColorButton button = popButtons.get(index);
				
				//已经选中一个按钮
				if(0 <= selectIndex && selectIndex < popButtons.size())
				{
					//设置前景不不显示
					popButtons.get(selectIndex).setImageAlpha(0);
				}
				
				selectIndex = index;
				button.setImageAlpha(255);
				
				if(Color.WHITE == button.getFillColor())
					button.setImageResource(R.drawable.ic_action_tick_black);
				else
					button.setImageResource(selectResId);

				//执行回调
				if(null != popMenuListener)
					popMenuListener.onSelectItem(button, index, button.getFillColor());
			}
			
			return false;
		}
	};
		
	
	///////////////////////////////////////////////////////////////////
	///执行 扩展或者搜索 用于外界函数 调用
	@Override
	public void executeExpand()
	{
		
	}
	
	@Override
	public void executeShrink()
	{
		//设置正常图标
		controlButton.setImageResource(controlResId);
		
		shrink();
	}

}
















