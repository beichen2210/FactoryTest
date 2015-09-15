package com.mlt.factorytest.item.tools;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

/** 
* @ClassName: ListView_Method 
* @Description: Mainly for listview and scrollview nesting problems
* @Function: Calculation of width and height of each items in a listview, calculated and adaptation in the scrollview
* @author:   peisaisai
* @date:     2015-02-10 15:27:37
* Copyright (c) 2015,  Malata All Rights Reserved.
*/
public class ListViewMethod {
	public static void setListViewHeightBasedOnChildren(ListView listView) {   
		
        // For ListView corresponding Adapter
        ListAdapter listAdapter = listView.getAdapter();   
        
        if (listAdapter == null) {   
            return;   
        }   
   
        int totalHeight = 0;   
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) {   
        	
            // listAdapter.getCount() Returns the number of data items
            View listItem = listAdapter.getView(i, null, listView);   
            
            // Wide high calculation item Vie 
            listItem.measure(0, 0);    
            
            // Statistics of all the items total height
            totalHeight += listItem.getMeasuredHeight();    
            
        }   
   
        ViewGroup.LayoutParams params = listView.getLayoutParams();   
        params.height = totalHeight+ (listView.getDividerHeight() * (listAdapter.getCount() - 1)); 
        
        // listView.getDividerHeight()   For children between the height of the separator to occupy
        // params.height   The height of the finally get the whole ListView display need to complete 
        listView.setLayoutParams(params);   
        
    }   
}
