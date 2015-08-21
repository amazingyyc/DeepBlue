package com.amazing.utils;

//状态  5个按钮 只有一个状态 
public class PopViewStatus 
{
	static public enum POP_STATUS
	{
		SHRINKED,	//已经收缩状态
		SHRINKING,	//正在收缩
		EXPANDED,	//已经扩展状态
		EXPANDING, 	//正在扩展状态
	};
	
	public POP_STATUS popStatus = POP_STATUS.SHRINKED;
	public PopView popView = null;
}
