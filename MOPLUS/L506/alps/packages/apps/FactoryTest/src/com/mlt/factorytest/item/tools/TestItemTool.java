package com.mlt.factorytest.item.tools;

import android.content.Context;

import com.mlt.factorytest.item.AbsHardware;
import com.mlt.factorytest.item.MyApplication;
/**
 * 
 * file name:TestItemTool.java
 * Copyright MALATA ,ALL rights reserved
 * 2015-2-10
 * author:laiyang
 * 
 * init TestItems' class,by java reflect to load these classes
 * The classes info(classes name and test item name) are saved in arrays.xml
 * 
 * Modification history
 * -------------------------------------
 * 
 * -------------------------------------
 */
public class TestItemTool {
	
	/**
	 * create object
	 * use java reflection to create Object 
	 * 
	 * @param className ClassName
	 * @param text ItemText name 
	 * @param visible the item's visibility
	 * @return
	 */
	private static AbsHardware createObj(String className,String text, boolean visible) {
		AbsHardware hw = null;
		try {
			Object o = (AbsHardware) Class.forName(className)
					.getConstructor(String.class,Boolean.class)
					.newInstance(text,visible);
			if(o instanceof AbsHardware) {
				hw = (AbsHardware) o;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(hw == null) {
			throw new NullPointerException("Create AbsHardware error!");
		}
		return hw;
	}
	
	
	public static AbsHardware getHardWares(Context context, int position) {
		String className = MyApplication.getHWClassNames().get(position);
		String text = MyApplication.getHWTexts().get(position);
		return createObj(className, text, true);
	}
}
