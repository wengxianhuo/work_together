package com.yarin.android.FileManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;

public class FileManager extends Activity implements View.OnClickListener
{
	
	private List<IconifiedText>	directoryEntries = new ArrayList<IconifiedText>();//当前目录
	private List<IconifiedText>	xuanqu = new ArrayList<IconifiedText>();//
	private File				currentDirectory = null;//当前目录
	private File 				myTmpFile 		 = null;//
	private int 				myTmpOpt		 = -1;
    private int                 sort             = -1;
    private File                sdfile           = null;
    private ListView listView = null;
    private int diaoyong = 0;
    private final String appid = "=53d90515";
    AlertDialog notice;
    private ProgressDialog  pd= null;
    private int count,zixiancheng;
    Button convertButton = null;
    GridView gridView = null;
    private static int a = 0;
    Handler handler = new Handler(){
    	public void handleMessage(Message msg) {  
            if (msg.what == 1) {
            	pd.setCancelable(true);
            new AlertDialog.Builder(FileManager.this).setTitle("您好")
            .setMessage("总共找到"+directoryEntries.size()+"个文件")
            .create().show(); 
            }
            else if(msg.what == 0){
            	
            	String data = (String)msg.obj;
            	String jiequ2 = data.substring(data.lastIndexOf("\"w\":\"")+5);
            	String operation2 = jiequ2.substring(0,jiequ2.indexOf("\""));
            	String jiequ = data.substring(data.indexOf("\"w\":\"")+5);
            	String operation = jiequ.substring(0,jiequ.indexOf("\""));
                operation +=operation2;
                int length = operation.length()/2;
                if(operation.substring(0,length).equals(operation.substring(length,operation.length()))){
                	operation = operation.substring(0,length);
                }
            	
            	if(operation.equals("图片")||operation.equals("图像")||operation.equals("相片")){
            		image_fenlei();
            	}
            	else if(operation.equals("音乐")||operation.equals("歌曲")){
            		music_fenlei();
            	}
            	else if(operation.equals("电影")||operation.equals("视频")){
            		vedio_fenlei();
            	}
            	else if(operation.equals("电子书")){
            		txt_fenlei();
            	}
            	else if(operation.equals("安装包")){
            		apk_fenlei();
            	}
            	else if(operation.length()>2){
            		Toast.makeText(FileManager.this,operation,1).show();
            	}
            	

            }
            else if(msg.what == 2){
            	Log.e("1",zixiancheng+"");
				pd.setProgress(zixiancheng);
				
            }
            else if(msg.what == 3){
            	if(gridView!=null){
            		gridView.setAdapter(new GridAdapter(FileManager.this,(ArrayList<IconifiedText>) directoryEntries));
            	}
            	else{
            	IconifiedTextListAdapter adapter= new IconifiedTextListAdapter(FileManager.this);
			    adapter.setListItems(directoryEntries);
			    listView.setAdapter(adapter);
            	}
            }
    	}

	
    };
   
    
	public void onResume(){
		if(a==0){
		String rest = (int)(OtherUtils.sdRest()*100)/100+"";
		String sum = (int)(OtherUtils.all()*100)/100+"";
		String used =(int)((OtherUtils.all()-OtherUtils.sdRest())*100)/100.0+"" ;
		new AlertDialog.Builder(this).setTitle("你好")
		.setMessage("当前sd卡剩余容量为:"+rest+"G\n当前sd卡使用容量为:"+used+"G\n当前sd卡总容量为:"+sum+"G").create().show();
		}
		a++;
		super.onResume();
	}

	
@Override
	public void onCreate(Bundle icicle)
	{
		this.setContentView(R.layout.listview);
		listView = (ListView)findViewById(R.id.listView);
		convertButton = (Button)findViewById(R.id.convertButton);
		currentDirectory = Environment.getExternalStorageDirectory();
		listView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				// 取得选中的一项的文件名
				String selectedFileString = directoryEntries.get(position).getText();
				if (selectedFileString.equals(getString(R.string.current_dir)))
				{
					//如果选中的是刷新
					browseTo(currentDirectory);
				}
				else
				{
					File clickedFile = null;
					clickedFile = directoryEntries.get(position).getFile();
					if(clickedFile != null)
						browseTo(clickedFile);
			}
			
		}});
		convertButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg));
		convertButton.setOnClickListener(this);
         
		super.onCreate(icicle);
		pd = new ProgressDialog(this);
	//语音识别初始化
		SpeechUtility.createUtility(this, SpeechConstant.APPID +appid);
		listView.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg));
		listView.setAlwaysDrawnWithCacheEnabled(true);
		listView.setCacheColorHint(Color.TRANSPARENT);
		browseTo(new File("/"));
		listView.setSelection(0);
	}
	public boolean onKeyDown(int keycode, KeyEvent event ){
		if(currentDirectory!=new File("/")&&currentDirectory.getParentFile()!=null){
		if((keycode == KeyEvent.KEYCODE_BACK) && event.getRepeatCount()==0){
			this.browseTo(this.currentDirectory.getParentFile());
			
		}
		}
		else if(keycode!=KeyEvent.KEYCODE_MENU){
			new AlertDialog.Builder(this).setTitle("温馨提示").setMessage("真的要离开吗？").setPositiveButton("确定",new AlertDialog.OnClickListener(){

				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub
					int nPid = android.os.Process.myPid();
                    android.os.Process.killProcess(nPid);
				}
				
			}).setNegativeButton("取消", new AlertDialog.OnClickListener(){

				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub
					
				}
				
			}).create().show();
			
		}
		if(keycode==KeyEvent.KEYCODE_MENU){
			openOptionsMenu();
		}
			return true;
	}

	//浏览指定的目录,如果是文件则进行打开操作
	private void browseTo(final File file)
	{
		this.setTitle(file.getAbsolutePath());
		if (file.isDirectory())
		{
			this.currentDirectory = file;
			fill(file.listFiles());
		}
		else
		{
			fileOptMenu(file);
		}
	}
	//打开指定文件
	protected void openFile(File aFile)
	{
		Intent intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_VIEW);
		File file = new File(aFile.getAbsolutePath());
		// 取得文件名
		String [] info1 = {"apk"};
		String [] info2 = {"txt","pdf"};
		String fileName = file.getName();
		// 根据不同的文件类型来打开文件
		if (checkEndsWithInStringArray(fileName, getResources().getStringArray(R.array.fileEndingImage)))
		{
			intent.setDataAndType(Uri.fromFile(file), "image/*");
		}
		else if (checkEndsWithInStringArray(fileName, getResources().getStringArray(R.array.fileEndingAudio)))
		{
			intent.setDataAndType(Uri.fromFile(file), "audio/*");
		}
		else if (checkEndsWithInStringArray(fileName, getResources().getStringArray(R.array.fileEndingVideo)))
		{
			intent.setDataAndType(Uri.fromFile(file), "video/*");
		}
		else if(checkEndsWithInStringArray(fileName,info1)){
			intent.setDataAndType(Uri.fromFile(file), "application/*");
		}
		else if(checkEndsWithInStringArray(fileName,info2)){
			intent.setDataAndType(Uri.fromFile(file), "application/*");
		}
		startActivity(intent);
	}
	//这里可以理解为设置ListActivity的源
	private void fill(File[] files)
	{
		//清空列表
		this.directoryEntries.clear();

		count =0;

		Drawable currentIcon = null;
	  
	    
		for (File currentFile : files)
		{
			//判断是一个文件夹还是一个文件
		    
			if (currentFile.isDirectory())
			{
				currentIcon = getResources().getDrawable(R.drawable.folder);
				count++;
			}
			else
			{
				//取得文件名
				String fileName = currentFile.getName();
				//根据文件名来判断文件类型，设置不同的图标
				if (checkEndsWithInStringArray(fileName, getResources().getStringArray(R.array.fileEndingImage)))
				{
					currentIcon = getResources().getDrawable(R.drawable.image);
				}
				else if (checkEndsWithInStringArray(fileName, getResources().getStringArray(R.array.fileEndingWebText)))
				{
					currentIcon = getResources().getDrawable(R.drawable.text);
				}
				else if (checkEndsWithInStringArray(fileName, getResources().getStringArray(R.array.fileEndingPackage)))
				{
					currentIcon = getResources().getDrawable(R.drawable.packed);
				}
				else if (checkEndsWithInStringArray(fileName, getResources().getStringArray(R.array.fileEndingAudio)))
				{
					currentIcon = getResources().getDrawable(R.drawable.audio);
				}
				else if (checkEndsWithInStringArray(fileName, getResources().getStringArray(R.array.fileEndingVideo)))
				{
					currentIcon = getResources().getDrawable(R.drawable.vedio);
				}
				else
				{
					currentIcon = getResources().getDrawable(R.drawable.text);
				}
			}
			//确保只显示文件名、不显示路径如：/sdcard/111.txt就只是显示111.txt
	        if(currentFile.getName().indexOf(".") != 0){
			this.directoryEntries.add(new IconifiedText(currentFile, currentIcon));
	        }
		}
		
	
			Collections.sort(this.directoryEntries);
			
		
		if(sort == 0){
			
			Collections.reverse(this.directoryEntries);
			sort = -1;
		}
		if(gridView !=null){
			gridView.setAdapter(new GridAdapter(FileManager.this,(ArrayList<IconifiedText>) directoryEntries));
		}
		else{
		IconifiedTextListAdapter itla = new IconifiedTextListAdapter(this);
		//将表设置到ListAdapter中
		itla.setListItems(this.directoryEntries);
		//为ListActivity添加一个ListAdapter
		pd.setMax(count);
		listView.setAdapter(itla);
		}
	}
	
	
	//通过文件名判断是什么类型的文件
	private boolean checkEndsWithInStringArray(String checkItsEnd, 
					String[] fileEndings)
	{
		for(String aEnd : fileEndings)
		{
			if(checkItsEnd.endsWith(aEnd))
				return true;
		}
		return false;
	}
	
	public File search(File file,String destination){
		File result = null;
	
			
			for(File currentfile:file.listFiles()){
				if(currentfile.isDirectory()&&currentfile.listFiles()!=null){
					result = search(currentfile,destination);
					if(result!=null) break;
				}
				else if(currentfile.isFile()){
					String filename = currentfile.getName();
					if(filename.indexOf(".")!=-1)
					if(filename.substring(0, filename.lastIndexOf(".")).equals(destination)){
						return currentfile;
						
					 } 
				
			    }   
		   }   
	
		return result;
	}
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);
		
		menu.add(0, 1, 0, "查找").setIcon(R.drawable.search);
		menu.add(0, 2, 0, "粘贴文件").setIcon(R.drawable.paste);
		menu.add(0, 3, 0, "根目录").setIcon(R.drawable.gen);
		menu.add(0, 4, 0, "分类显示").setIcon(R.drawable.fenlei);
		menu.add(0,5,0,"退出").setIcon(R.drawable.fail);
		menu.add(0,6,0,"语音分类").setIcon(R.drawable.jieguo);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		super.onOptionsItemSelected(item);
		
		switch (item.getItemId())
		{
			
			case 1:
				LayoutInflater factory = LayoutInflater.from(FileManager.this);
				View searchView = factory.inflate(R.layout.search, null);
				final EditText searchedittext = (EditText)searchView.findViewById(R.id.searchedittext);
				searchedittext.setText("");
				new AlertDialog.Builder(FileManager.this).setTitle("Search!").setView(searchView).setPositiveButton("当前目录查询", new AlertDialog.OnClickListener(){

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						File result = null;
						Toast.makeText(FileManager.this, "客官，请稍等", 1).show();	
						result = search(currentDirectory,searchedittext.getText().toString());
						if(result == null){
							Toast.makeText(FileManager.this, "未找到指定文件", 1).show();
						}
						else{
								
					   directoryEntries.clear();
					   currentDirectory = result.getParentFile();
					   directoryEntries.add(new IconifiedText(result,getResources().getDrawable(R.drawable.text)));
					   IconifiedTextListAdapter itla = new IconifiedTextListAdapter(FileManager.this);
						//将表设置到ListAdapter中
						itla.setListItems(directoryEntries);
						//为ListActivity添加一个ListAdapter
						listView.setAdapter(itla);
						}
					}
					
				}).create().show();
				break;
			case 2:
				MyPaste();
				break;
			case 3:
				this.browseTo(new File("/"));
				break;
			case 4:
			
				String [] menu2 = {"图片","音乐","电影","电子书","安装包"};
				
			        new AlertDialog.Builder(FileManager.this).setTitle("请选择分类类型")
					.setItems(menu2, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int which) {
						// TODO Auto-generated method stub
						pd.setCancelable(false);
					switch(which){
						case 0:
							
							image_fenlei();
							break;
						case 1:
							music_fenlei();
							break;
						case 2:
							vedio_fenlei();
							 break;
						case 3:
							txt_fenlei();
							break;
						case 4:
							apk_fenlei();
							 break;
						}
					}
				}).create().show();
				break;
			  case 5:
			        int nPid = android.os.Process.myPid();
		            android.os.Process.killProcess(nPid);
		            break;
			  case 6:
				  			
				  final SpeechRecognizer mIat= SpeechRecognizer.createRecognizer(this ,null);
				  mIat.setParameter(SpeechConstant.DOMAIN, "iat"); 
				  mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn"); 
				  mIat.setParameter(SpeechConstant.ACCENT, "mandarin ");
				  mIat.startListening(new RecognizerListener(){

					  
					@Override
					public void onBeginOfSpeech() {
					  Builder builder= new AlertDialog.Builder(FileManager.this);
					  builder.setMessage("开始录音,请说话").create();
					  notice = builder.show();
					}

					@Override
					public void onEndOfSpeech() {
						notice.cancel();
						Builder builder= new AlertDialog.Builder(FileManager.this);
						  builder.setMessage("录音结束，请等待匹配结果").create();
						  notice = builder.show();
						
					}

					@Override
					public void onError(SpeechError error) {

						error.getPlainDescription(true);
					}

					@Override
					public void onEvent(int arg0, int arg1, int arg2,
							String arg3) {
						
					}

					@Override
					public void onResult(RecognizerResult data, boolean arg1) {
						diaoyong++;
						if(diaoyong==2){
							mIat.destroy();
							diaoyong = 0;
						}
						notice.cancel();
						Message msg = new Message();
						msg.what = 0;
						msg.obj = data.getResultString();
						handler.sendMessage(msg);
					}

					@Override
					public void onVolumeChanged(int arg0) {
						
					}
					  
				  });
				  break;
		}
		return false;
	}

	private void txt_fenlei(){
		zixiancheng = 0;
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setIndeterminate(false);
		pd.setTitle("电子书搜索进度");
		pd.setProgress(0);
		pd.show();
		final String[] infos = {"txt"};
		directoryEntries.clear();
		
		new Thread(){
			public void run(){
				fenlei(currentDirectory, infos);
		    Message message1 = new Message();
		    message1.obj = zixiancheng;
		    message1.what = 3;
		    handler.sendMessage(message1);
		    pd.cancel();
		    Message msg = new Message();
	        msg.what = 1;
	        handler.sendMessage(msg);
			}
		}.start(); 
	}
	private void vedio_fenlei(){
		zixiancheng = 0;
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setIndeterminate(false);
		pd.setTitle("电影搜索进度");
		pd.setProgress(0);
		pd.show();
		directoryEntries.clear();
		
		
		new Thread(){
			public void run(){
			fenlei(currentDirectory, getResources().getStringArray(R.array.fileEndingVideo));
		    Message message1 = new Message();
		    message1.obj = zixiancheng;
		    message1.what = 3;
		    handler.sendMessage(message1);
		    pd.cancel();
		    Message msg = new Message();
	        msg.what = 1;
	        handler.sendMessage(msg);
			}
		}.start();
	}
	private void music_fenlei(){
		zixiancheng = 0;
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setIndeterminate(false);
		pd.setTitle("音乐搜索进度");
		pd.setProgress(0);
		pd.show();
		directoryEntries.clear();
		final String [] music ={"mp3"};
		
		new Thread(){
			public void run(){
				fenlei(currentDirectory,music);
		    Message message1 = new Message();
		    message1.obj = zixiancheng;
		    message1.what = 3;
		    handler.sendMessage(message1);
		    pd.cancel();
		    Message msg = new Message();
	        msg.what = 1;
	        handler.sendMessage(msg);
			}
		}.start();
	}
	private void image_fenlei() {
		zixiancheng = 0;
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setIndeterminate(false);
		pd.setTitle("图片搜索进度");
		pd.setProgress(0);
		pd.show();
		final String[] picture = { "jpg" };
		directoryEntries.clear();
		new Thread() {
			public void run() {
				fenlei(currentDirectory, picture);
				Message message1 = new Message();
				message1.obj = zixiancheng;
				message1.what = 3;
				handler.sendMessage(message1);
				pd.cancel();
				Message msg = new Message();
				msg.what = 1;
				handler.sendMessage(msg);
			}
		}.start();
	}
	private void apk_fenlei() {
		zixiancheng = 0;
		pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		pd.setIndeterminate(false);
		pd.setTitle("apk安装包搜索进度");
		pd.setProgress(0);
		pd.show();
		final String[] infos1 = { "apk" };
		directoryEntries.clear();

		new Thread() {
			public void run() {
				fenlei(currentDirectory, infos1);
				Message message1 = new Message();
				message1.obj = zixiancheng;
				message1.what = 3;
				handler.sendMessage(message1);
				pd.cancel();
				Message msg = new Message();
				msg.what = 1;
				handler.sendMessage(msg);
			}
		}.start();
	}
	public void fenlei(final File file,final String[]names){
		
	
			
		
	
		for(File currentfile:file.listFiles()){
			if(currentfile.getParentFile().equals(currentDirectory)){
				zixiancheng++;
			    Message message = new Message();
			    message.obj = zixiancheng;
			    message.what = 2;
			    handler.sendMessage(message);
			}
				if(currentfile.isFile()){
					for(String name:names){
						if(currentfile.getName().endsWith(name)){
							directoryEntries.add(new IconifiedText(currentfile,getResources().getDrawable(R.drawable.text)));
						}
				    }
				}
				
				else if(currentfile.isDirectory()&&currentfile.listFiles()!=null){
					fenlei(currentfile,names);
					
				}
			   
		}
	  
	}
	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		return super.onPrepareOptionsMenu(menu);
	}
	//粘贴操作
	public void MyPaste()
	{
		if ( myTmpFile == null )
		{
			Builder builder = new Builder(FileManager.this);
			builder.setTitle("提示");
			builder.setMessage("没有复制或剪切操作");
			builder.setPositiveButton(android.R.string.ok,
					new AlertDialog.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();	
						}
					});
			builder.setCancelable(false);
			builder.create();
			builder.show();
		}
		else
		{
			if ( myTmpOpt == 0 )//复制操作
			{
				if(new File(GetCurDirectory()+"/"+myTmpFile.getName()).exists())
				{
					Builder builder = new Builder(FileManager.this);
					builder.setTitle("粘贴提示");
					builder.setMessage("该目录有相同的文件，是否需要覆盖？");
					builder.setPositiveButton(android.R.string.ok,
							new AlertDialog.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									copyFile(myTmpFile,new File(GetCurDirectory()+"/"+myTmpFile.getName()));
									browseTo(new File(GetCurDirectory()));
								}
							});
					builder.setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									dialog.cancel();
								}
							});
					builder.setCancelable(false);
					builder.create();
					builder.show();
				}	
				else
				{
					copyFile(myTmpFile,new File(GetCurDirectory()+"/"+myTmpFile.getName()));
					browseTo(new File(GetCurDirectory()));
				}
			}
			else if(myTmpOpt == 1)//粘贴操作
			{
				if(new File(GetCurDirectory()+"/"+myTmpFile.getName()).exists())
				{
					Builder builder = new Builder(FileManager.this);
					builder.setTitle("粘贴提示");
					builder.setMessage("该目录有相同的文件，是否需要覆盖？");
					builder.setPositiveButton(android.R.string.ok,
							new AlertDialog.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									moveFile(myTmpFile.getAbsolutePath(),GetCurDirectory()+"/"+myTmpFile.getName());
									browseTo(new File(GetCurDirectory()));
								}
							});
					builder.setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									dialog.cancel();
								}
							});
					builder.setCancelable(false);
					builder.create();
					builder.show();
				}	
				else
				{
					moveFile(myTmpFile.getAbsolutePath(),GetCurDirectory()+"/"+myTmpFile.getName());
					browseTo(new File(GetCurDirectory()));	
				}
			}
		}
	}

	//新建文件夹
	public void Mynew()
	{
		final LayoutInflater factory = LayoutInflater.from(FileManager.this);
		final View dialogview = factory.inflate(R.layout.dialog, null);
		//设置TextView
		((TextView) dialogview.findViewById(R.id.TextView_PROM)).setText("请输入新建文件夹的名称！");
		//设置EditText
		((EditText) dialogview.findViewById(R.id.EditText_PROM)).setText("文件夹名称...");
		
		Builder builder = new Builder(FileManager.this);
		builder.setTitle("新建文件夹");
		builder.setView(dialogview);
		builder.setPositiveButton(android.R.string.ok,
				new AlertDialog.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						String value = ((EditText) dialogview.findViewById(R.id.EditText_PROM)).getText().toString();
						if ( newFolder(value) )
						{
							Builder builder = new Builder(FileManager.this);
							builder.setTitle("提示");
							builder.setMessage("新建文件夹成功");
							builder.setPositiveButton(android.R.string.ok,
									new AlertDialog.OnClickListener() {
										public void onClick(DialogInterface dialog, int which) {
											//点击确定按钮之后,继续执行网页中的操作
											dialog.cancel();
										}
									});
							builder.setCancelable(false);
							builder.create();
							builder.show();
						}
						else
						{
							Builder builder = new Builder(FileManager.this);
							builder.setTitle("提示");
							builder.setMessage("新建文件夹失败");
							builder.setPositiveButton(android.R.string.ok,
									new AlertDialog.OnClickListener() {
										public void onClick(DialogInterface dialog, int which) {
											//点击确定按钮之后,继续执行网页中的操作
											dialog.cancel();
										}
									});
							builder.setCancelable(false);
							builder.create();
							builder.show();	
						}
					}
				});
		builder.setNegativeButton(android.R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						dialog.cancel();
					}
				});
		builder.show();
	}
	//新建文件夹
	public boolean newFolder(String file)
	{
		File dirFile = new File(this.currentDirectory.getAbsolutePath()+"/"+file);
		try
		{
			if (!(dirFile.exists()) && !(dirFile.isDirectory()))
			{
				boolean creadok = dirFile.mkdirs();
				if (creadok)
				{
					this.browseTo(this.currentDirectory);
					return true;
				}
				else
				{
					return false;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println(e);
			return false;
		}
		return true;
	}
	//删除文件
    public boolean deleteFile(File file)
	{
		boolean result = false;
		if (file != null)
		{
			try
			{
				File file2 = file;
				file2.delete();
				result = true;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				result = false;
			}
		}
		return result;
	} 

	
	//处理文件，包括打开，重命名等操作
	public void fileOptMenu(final File file)
	{
		OnClickListener listener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which)
			{
				if (which == 0)
				{
					openFile(file);
				}
				else if (which == 1)
				{
					//自定义一个带输入的对话框由TextView和EditText构成
					final LayoutInflater factory = LayoutInflater.from(FileManager.this);
					final View dialogview = factory.inflate(R.layout.rename, null);
					//设置TextView的提示信息
					((TextView) dialogview.findViewById(R.id.TextView01)).setText("重命名");
					//设置EditText输入框初始值
					((EditText) dialogview.findViewById(R.id.EditText01)).setText(file.getAbsolutePath());
					
					Builder builder = new Builder(FileManager.this);
					builder.setTitle("重命名");
					builder.setView(dialogview);
					builder.setPositiveButton(android.R.string.ok,
							new AlertDialog.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									//点击确定之后
									String panduan = ((EditText) dialogview.findViewById(R.id.EditText01)).getText().toString();
									String value = GetCurDirectory()+"/"+panduan;
									if(new File(value).exists()&&!panduan.equals(file.getName()))
									{
										Builder builder = new Builder(FileManager.this);
										builder.setTitle("重命名");
										
										builder.setMessage("文件名重复，是否需要覆盖？");
										builder.setPositiveButton(android.R.string.ok,
												new AlertDialog.OnClickListener() {
													public void onClick(DialogInterface dialog, int which) {
														String str2 = GetCurDirectory()+"/"+((EditText) dialogview.findViewById(R.id.EditText01)).getText().toString();
														file.renameTo(new File(str2));
														
														new AlertDialog.Builder(FileManager.this).setView(factory.inflate(R.layout.dialogsucceeds,null)).create().show();
														browseTo(new File(GetCurDirectory()));
													}
												});
										builder.setNegativeButton(android.R.string.cancel,
												new DialogInterface.OnClickListener() {
													public void onClick(DialogInterface dialog, int which) {
														dialog.cancel();
													}
												});
										builder.setCancelable(false);
										builder.create();
										builder.show();
									}
									else 
									{
										//重命名
										file.renameTo(new File(value));
										new AlertDialog.Builder(FileManager.this).setView(factory.inflate(R.layout.dialogsucceeds,null)).create().show();
										browseTo(new File(GetCurDirectory()));
									}
								}
							});
					builder.setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									dialog.cancel();
								}
							});
					builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
								public void onCancel(DialogInterface dialog) {
									dialog.cancel();
								}
							});
					builder.show();
				}
				else if ( which == 2 )
				{
					Builder builder = new Builder(FileManager.this);
					builder.setTitle("删除文件");
					builder.setMessage("确定删除"+file.getName()+"？");
					builder.setPositiveButton(android.R.string.ok,
							new AlertDialog.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									if ( deleteFile(file) )
									{
										Builder builder = new Builder(FileManager.this);
										builder.setTitle("提示对话框");
										builder.setMessage("删除成功");
										builder.setPositiveButton(android.R.string.ok,
												new AlertDialog.OnClickListener() {
													public void onClick(DialogInterface dialog, int which) {
														//点击确定按钮之后
														dialog.cancel();
														browseTo(new File(GetCurDirectory()));
													}
												});
										builder.setCancelable(false);
										builder.create();
										builder.show();
									}
									else 
									{
										Builder builder = new Builder(FileManager.this);
										builder.setTitle("提示对话框");
										builder.setMessage("删除失败");
										builder.setPositiveButton(android.R.string.ok,
												new AlertDialog.OnClickListener() {
													public void onClick(DialogInterface dialog, int which) {
														//点击确定按钮之后
														dialog.cancel();
													}
												});
										builder.setCancelable(false);
										builder.create();
										builder.show();	
									}
								}
							});
					builder.setNegativeButton(android.R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									dialog.cancel();
								}
							});
					builder.setCancelable(false);
					builder.create();
					builder.show();
				}
				else if ( which == 3 )//复制
				{
					//保存我们复制的文件目录
					myTmpFile = file;
					//这里我们用0表示复制操作
					myTmpOpt = 0;
				}
				else if ( which == 4 )//剪切
				{
					//保存我们复制的文件目录
					myTmpFile = file;
					//这里我们用0表示剪切操作
					myTmpOpt = 1;	 
				}
			}
		};
		//显示操作菜单
	    String[] menu={"打开","重命名","删除","复制","剪切"};
	    new AlertDialog.Builder(FileManager.this)
	        .setTitle("请选择你要进行的操作")
	        .setItems(menu,listener)
	        .show();
	}
	//获取当前目录的绝对路径
	public String GetCurDirectory(){
		return currentDirectory.getAbsolutePath();
	}
	
	//移动文件
	public void moveFile(String source, String destination)
	{
		new File(source).renameTo(new File(destination));   
	}
	//复制文件
	public void copyFile(File src, File target)
	{
		InputStream in = null;
		OutputStream out = null;

		BufferedInputStream bin = null;
		BufferedOutputStream bout = null;
		try
		{
			in = new FileInputStream(src);
			out = new FileOutputStream(target);
			bin = new BufferedInputStream(in);
			bout = new BufferedOutputStream(out);

			byte[] b = new byte[8192];
			int len = bin.read(b);
			while (len != -1)
			{
				bout.write(b, 0, len);
				len = bin.read(b);
			}

		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (bin != null)
				{
					bin.close();
				}
				if (bout != null)
				{
					bout.close();
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}


	@Override
	public void onClick(View v) {

		switch(v.getId()){
		case R.id.convertButton:
			listView = null;
			FileManager.this.setContentView(R.layout.gridview);
			gridView = (GridView)findViewById(R.id.gridView);
			Button convert = (Button)findViewById(R.id.gridConvertButton);
			convert.setOnClickListener(this);
			convert.setBackgroundDrawable(getResources().getDrawable(R.drawable.background));
			sdfile = Environment.getExternalStorageDirectory();
			pd = new ProgressDialog(FileManager.this);
		//语音识别初始化
			SpeechUtility.createUtility(FileManager.this, SpeechConstant.APPID +appid);
			gridView.setBackgroundDrawable(getResources().getDrawable(R.drawable.background));
			gridView.setAlwaysDrawnWithCacheEnabled(true);
			gridView.setCacheColorHint(Color.TRANSPARENT);
			gridView.setBackgroundDrawable(getResources().getDrawable(R.drawable.background));
			gridView.setAlwaysDrawnWithCacheEnabled(true);
			gridView.setCacheColorHint(Color.TRANSPARENT);
			gridView.setAdapter(new GridAdapter(FileManager.this,(ArrayList<IconifiedText>) directoryEntries));
			gridView.setOnItemClickListener(new OnItemClickListener(){
		        
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						final int position, long id) {
					final Button button = (Button)findViewById(R.id.xuanqu);
					button.setOnLongClickListener(new OnLongClickListener(){

						@Override
						public boolean onLongClick(View v) {
							
							xuanqu.add(directoryEntries.get(position));
							new AlertDialog.Builder(FileManager.this).setTitle("长按屏幕").create().show();
							return false;
						}
						
					});
					button.setVisibility(Button.VISIBLE);
					
					// 取得选中的一项的文件名
					String selectedFileString = directoryEntries.get(position).getText();
					
					if (selectedFileString.equals(getString(R.string.current_dir)))
					{
						//如果选中的是刷新
						browseTo(currentDirectory);
					}
					
					else
					{
								
						File clickedFile = null;
						clickedFile = directoryEntries.get(position).getFile();
						if(clickedFile != null)
							browseTo(clickedFile);
					
				}
				
			}});
			browseTo(sdfile);
			break;
		case R.id.gridConvertButton:
			gridView = null;
			FileManager.this.setContentView(R.layout.listview);
			listView = (ListView)findViewById(R.id.listView);
			listView.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg));
			listView.setAlwaysDrawnWithCacheEnabled(true);
			listView.setCacheColorHint(Color.TRANSPARENT);
			convertButton = (Button)findViewById(R.id.convertButton);
			convertButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg));
			convertButton.setOnClickListener(this);
			sdfile = Environment.getExternalStorageDirectory();
			pd = new ProgressDialog(FileManager.this);
		//语音识别初始化
			SpeechUtility.createUtility(FileManager.this, SpeechConstant.APPID +appid);
			browseTo(sdfile);
			listView.setOnItemClickListener(new OnItemClickListener(){
		        
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					
					// 取得选中的一项的文件名
					String selectedFileString = directoryEntries.get(position).getText();
					
					if (selectedFileString.equals(getString(R.string.current_dir)))
					{
						//如果选中的是刷新
						browseTo(currentDirectory);
					}
					
					else
					{
								
						File clickedFile = null;
						clickedFile = directoryEntries.get(position).getFile();
						if(clickedFile != null)
							browseTo(clickedFile);
					
				}
				
			}});
			break;
		}
	}

public void onLongClickListener(){
	Button button = (Button)findViewById(R.id.xuanqu);
	button.setVisibility(Button.VISIBLE);
	button.setEnabled(true);
	
}


}
