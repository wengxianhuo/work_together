package com.yarin.android.FileManager;

import java.io.File;

import android.os.Environment;
import android.os.StatFs;


public class OtherUtils {

	public static float sdRest(){
		File sdFile = Environment.getExternalStorageDirectory();
		StatFs staffs = new StatFs(sdFile.getPath());
		long size = staffs.getBlockSize();
		long available = staffs.getAvailableBlocks();
		return (float) (size*available/1024/1024/1024/1.0);
	}

	public static float all(){
		File sdFile = Environment.getExternalStorageDirectory();
		StatFs staffs = new StatFs(sdFile.getPath());
		long blockcount = staffs.getBlockCount();
		long size = staffs.getBlockSize();
		return (float) (blockcount*size/1024/1024/1024.0);
	}
	
	public static boolean newFolder(File target){
		
       return target.mkdirs();
	}
	
}
