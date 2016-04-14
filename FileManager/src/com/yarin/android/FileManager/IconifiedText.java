package com.yarin.android.FileManager;

import java.io.File;

import android.graphics.drawable.Drawable;
import android.widget.Toast;

public class IconifiedText implements Comparable<IconifiedText>
{
	/* �ļ��� */
	private File	file	= null;
	/* �ļ���ͼ��ICNO */
	private Drawable	mIcon		= null;
	/* �ܷ�ѡ�� */
	private boolean	mSelectable	= true;
	public IconifiedText(File file, Drawable bullet)
	{
		mIcon = bullet;
		this.file = file;
	}
	//�Ƿ����ѡ��
	public boolean isSelectable()
	{
		return mSelectable;
	}
	//�����Ƿ����ѡ��
	public void setSelectable(boolean selectable)
	{
		mSelectable = selectable;
	}
	//�õ��ļ���
	public String getText()
	{
		return file.getName();
	}
    public File getFile(){
    	return file;
    }
	//�����ļ���
	public void setFile(File thefile)
	{
		file = thefile;
	}
	//����ͼ��
	public void setIcon(Drawable icon)
	{
		mIcon = icon;
	}
	//�õ�ͼ��
	public Drawable getIcon()
	{
		return mIcon;
	}
	//�Ƚ��ļ����Ƿ���ͬ
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
