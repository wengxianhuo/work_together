package com.yarin.android.FileManager;

import java.io.File;

import android.graphics.drawable.Drawable;
import android.widget.Toast;

public class IconifiedText implements Comparable<IconifiedText>
{
	/* 文件名 */
	private File	file	= null;
	/* 文件的图标ICNO */
	private Drawable	mIcon		= null;
	/* 能否选中 */
	private boolean	mSelectable	= true;
	public IconifiedText(File file, Drawable bullet)
	{
		mIcon = bullet;
		this.file = file;
	}
	//是否可以选中
	public boolean isSelectable()
	{
		return mSelectable;
	}
	//设置是否可用选中
	public void setSelectable(boolean selectable)
	{
		mSelectable = selectable;
	}
	//得到文件名
	public String getText()
	{
		return file.getName();
	}
    public File getFile(){
    	return file;
    }
	//设置文件名
	public void setFile(File thefile)
	{
		file = thefile;
	}
	//设置图标
	public void setIcon(Drawable icon)
	{
		mIcon = icon;
	}
	//得到图标
	public Drawable getIcon()
	{
		return mIcon;
	}
	//比较文件名是否相同
	public int compareTo(IconifiedText other)
	{
	 if(other.file.isDirectory()&&this.file.isDirectory()){
		
		 return this.getText().compareTo(other.getText());
	 }
	 else if(other.file.isDirectory()&&this.file.isFile()){
		 return 1;
	 }
	 else if(other.file.isFile()&&this.file.isDirectory()){
		 return -1;
	 }
	 else {
		
		 return this.getText().compareTo(other.getText());
		 
	 }
	 
	}
}
