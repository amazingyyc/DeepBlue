package com.amazing.view;

import com.amazing.deepblue.R;

import android.app.Dialog;
import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

/*
 * 等待界面 显示一个旋转的动画 表示等待
 */
public class WaitingDialog extends Dialog 
{
	//用于旋转的图片
	ImageView imageView = null;
	
	//旋转动画
	RotateAnimation animation = null;

	public WaitingDialog(Context context, int theme) 
	{
		super(context, theme);
		
		//设置布局文件
		setContentView(R.layout.waiting_dialog);
		
		//控制点击空白区域对话框是否消失
		setCanceledOnTouchOutside(false);
				
		//后者是控制点击返回键对话框是否消失
		setCancelable(false);
		
		//得到控件
		imageView = (ImageView)findViewById(R.id.waiting_image_view);
		
		//初始化动画
		animation =new RotateAnimation(0f,360f,Animation.RELATIVE_TO_SELF, 
                0.5f,Animation.RELATIVE_TO_SELF,0.5f); 
		animation.setDuration(800);//设置动画持续时间 
		animation.setRepeatCount(-1);//设置重复次数 
		animation.setFillAfter(true);//动画执行完后是否停留在执行完的状态 
	}

	@Override
	public void show()
	{
		super.show();
		
		imageView.startAnimation(animation);
	}
	
	@Override
	public void dismiss()
	{
		super.dismiss();
		
		imageView.clearAnimation();
	}
}





















