package com.amazing.utils;

import com.amazing.view.ColorButton;

//用户PopMenu的回调
public interface PopMenuListener 
{
	//当某个选择按钮被选择是 执行 view 索引 和 颜色
	public void onSelectItem(ColorButton v, int index, int color);
	
	//当添加按钮被点击时执行
	public void onClickAddButton();
}
