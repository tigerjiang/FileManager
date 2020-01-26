package com.soft.TvdFileManager;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.StatFs;
import android.util.Log;

/**
 * 
 * @author chenjd 
 * @help
 * <br>1.��������ͼ�Ĺ��� 
 *   <br>getImageThumbFromDB(String path) �����ݿ��л�ȡ����ͼ,����null��ʾ���ݿ���û�����ͼƬ������ͼ����
 *   <br>getImageThumbFromMK(String path) ֱ����������ͼ�������ȵ��õ�һ�������ݿ���ȡ�����û�ҵ��ٵ����������
 *   <br>clearThumbnailData()������ݿ��б��������ͼ���ݣ������ⷽ�������ݿ��н�ֻ�������µ�MaxThumbnailNum������ͼ����
 * <br>2.�����ļ���Ŀ¼�µ��ļ��б�����
 *   <br>�����˼��ֵ��ļ�������
 *   <br>getList(String, FileFilter) �����ļ��Ĺ��������˳��ļ����ڵ��ļ��б�
 */
public class MediaProvider 
{
	public static final String RETURN = "../";
	public static final String NETWORK_NEIGHBORHOOD = "NetworkNeighborhoodList";
	public static final String NFS_SHARE = "NFSServerList";
	public static final int AUDIOTYPE = 0x01;
	public static final int IMAGETYPE = 0x02;
	public static final int VIDEOTYPE = 0x04;
	public static final int ALLTYPE	  = 0x08;
	public static final int DIRTYPE   = 0x00; 
	
	private static final int MaxThumbnailNum = 200; 
	private int filesNum = 0;
	private Context context;
	private ImageDatabase imageDB;
	private static final String TAG = "MediaProvider";
	private ArrayList<String> mlist;
	
	private ThumbnailCreator thumbCreator = null;
	public MediaProvider(Context context)
	{
		this.context = context;
		this.imageDB = new ImageDatabase(context);
		mlist = new ArrayList<String>();
	}
	
	/* ��������ʽ */
	public static final Comparator alph = new Comparator<String>()
	{
		@Override
		public int compare(String arg0, String arg1)
		{
			File f0 = new File(arg0);
			File f1 = new File(arg1);
			
			/* "����"ָʾ��Զ���ڵ�һ��  */
			if(arg0.equals(RETURN))
			{
				return -1;
			}
			if(arg1.equals(RETURN))
			{
				return 1;
			}
			
			/* �ļ�����Զ����ǰ�� */
			try
			{
				if(f0.isDirectory() && !f1.isDirectory())
				{
					return -1;
				}
				if(!f0.isDirectory() && f1.isDirectory())
				{
					return 1;
				}
			}catch(SecurityException e)
			{
				e.printStackTrace();
				return -1;
			}
			
			try
			{
				/* ��������������� */
				String str0 = arg0.substring(arg0.lastIndexOf("/") + 1);
				String str1 = arg1.substring(arg1.lastIndexOf("/") + 1);
				return str0.compareToIgnoreCase(str1);
			}catch(IndexOutOfBoundsException e)
			{
				e.printStackTrace();
				return -1;
			}
		}
	};
	
	public static final Comparator lastModified = new Comparator<String>()
	{
		@Override
		public int compare(String arg0, String arg1)
		{
			File f0 = new File(arg0);
			File f1 = new File(arg1);
			
			/* "����"ָʾ��Զ���ڵ�һ��  */
			if(arg0.equals(RETURN))
			{
				return -1;
			}
			if(arg1.equals(RETURN))
			{
				return 1;
			}
			
			try
			{
				/* �ļ�����Զ����ǰ�� */
				if(f0.isDirectory() && !f1.isDirectory())
				{
					return -1;
				}
				if(!f0.isDirectory() && f1.isDirectory())
				{
					return 1;
				}

				/* ��������������� */
				if(f0.lastModified() > f1.lastModified())
					return -1;
				else if(f0.lastModified() == f1.lastModified())
					return 0;
				else return 1;
			}catch(SecurityException e)
			{
				e.printStackTrace();
				return -1;
			}
		}
	};
	
	public static final Comparator size = new Comparator<String>()
	{
		@Override 
		public int compare(String arg0, String arg1)
		{
			File f0 = new File(arg0);
			File f1 = new File(arg1);
			if(arg0.equals(RETURN))
			{
				return -1;
			}
			if(arg1.equals(RETURN))
			{
				return 1;
			}
			try
			{
				if(f0.isDirectory() && !f1.isDirectory())
				{
					return -1;
				}
				if(!f0.isDirectory() && f1.isDirectory())
				{
					return 1;
				}
				if(f0.length() > f1.length())
					return -1;
				else if(f0.length() == f1.length())
					return 0;
				else return 1;
			}catch(SecurityException e)
			{
				e.printStackTrace();
				return -1;
			}
		}
	};
	
	/* �����ļ������� */
	public FileFilter MUSIC_FILTER = new FileFilter() {

		@Override
		public boolean accept(File pathname) {
			// TODO Auto-generated method stub
			//keep all needed files
			try{
				if(pathname.isDirectory())
				{
					filesNum ++;
					return true;
				}
			}catch(SecurityException e)
			{
				e.printStackTrace();
				return false;
			}
			
			String name = pathname.getAbsolutePath();
			
			if(TypeFilter.isMusicFile(name))
			{
				filesNum ++;
				return true;
			}
			
			return false;
		}
	};
	
	public FileFilter MOVIE_FILTER = new FileFilter() {

		@Override
		public boolean accept(File pathname) {
			// TODO Auto-generated method stub
			//keep all needed files
			try
			{
				if(pathname.isDirectory())
				{
					filesNum ++;
					return true;
				}
			}catch(SecurityException e)
			{
				e.printStackTrace();
				return false;
			}
			
			String name = pathname.getAbsolutePath();
			if(TypeFilter.isMovieFile(name))
			{
				filesNum ++;
				return true;
			}
			
			return false;
		}
	};
	
	public FileFilter PICTURE_FILTER = new FileFilter() {

		@Override
		public boolean accept(File pathname) {
			// TODO Auto-generated method stub
			//keep all needed files
			try
			{
				if(pathname.isDirectory())
				{
					filesNum ++;
					return true;
				}
			}catch(SecurityException e)
			{
				e.printStackTrace();
				return false;
			}
			
			String name = pathname.getAbsolutePath();
			if(TypeFilter.isPictureFile(name))
			{
				filesNum ++;
				return true;
			}
			
			return false;
		}
	};
	
	public FileFilter ALLTYPE_FILTER = new FileFilter()
	{
		@Override
		public boolean accept(File pathname)
		{
			filesNum ++;
			return true;
		}
	};
	
	public int getFilesNum()
	{
		return filesNum;
	}
	
	public ArrayList<String> getList(String path, FileFilter type)
	{
		filesNum = 0;
		mlist.clear();
		mlist.add(RETURN);
		File file = new File(path);
		File[] fileList = null;
		if(file.canRead())
		{
			fileList = file.listFiles(type);
		}
		DeviceManager devMng = new DeviceManager(context);
		int i = 0;
		if(fileList != null)
		{
			/* ������������� */
			if(devMng.getLocalDevicesList().contains(path) && devMng.hasMultiplePartition(path)){
				for(i = 0; i < fileList.length; i++){
					try{
						String child = fileList[i].getAbsolutePath();
						StatFs statFs = new StatFs(child);
						Log.d("chen", "child:" + child + "  block num:" + statFs.getBlockCount() + "  avail block num:" + statFs.getAvailableBlocks());
						if(statFs.getBlockCount() != 0){
							
							mlist.add(child);
						}
					}catch(Exception e){}
				}
			}else{
				for(i = 0; i < fileList.length; i++)
				{
					scanFile(fileList[i]);
					mlist.add(fileList[i].getAbsolutePath());
				}
			}
		}
		return (ArrayList<String>) mlist.clone();
	}
	
	private void scanFile(File file)
	{
		String path = file.getAbsolutePath();
		if(TypeFilter.isMusicFile(path))
		{
			/*
			 * notify the media to scan 
			 */
			Uri mUri = Uri.fromFile(file);
			Intent mIntent = new Intent();
			mIntent.setData(mUri);
			mIntent.setAction(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
			context.sendBroadcast(mIntent);
		}	
	}

	
	/**
	 * �����ݿ��в�ѯ��������ͼ
	 * @param origPath
	 * @return null�����ѯ����������ͼ�ļ�
	 */
	public Bitmap getImageThumbFromDB(String origPath)
	{
		final int ORIG_PATH_COLUMN  = 0;
		final int THUMB_PATH_COLUMN = 1;
		String[] columns = {ImageDatabase.ORIG_PATH, ImageDatabase.THUMB_PATH};
		String selection = ImageDatabase.ORIG_PATH + "=?";
		String Args[]	 = {origPath};
		Bitmap thumbnail = null;
		
		Cursor c = imageDB.query(columns, selection, Args, null);
		//��������ݿ����ҵ���ԭͼ������ͼ����ֱ��ʹ��
		if(c != null)
		{
			try
			{
				while(c.moveToNext())
				{
					String thumbPath = c.getString(THUMB_PATH_COLUMN);
					thumbnail = BitmapFactory.decodeFile(thumbPath);
					break;
				}
			}
			finally
			{
				c.close();
				c = null;
			}
			
		}
		return thumbnail;
	}
	/**
	 * 
	 * @param origPath ԴͼƬ�ļ�·��
	 * @param width ����ͼ�趨���
	 * @param height ����ͼ�趨�߶�
	 * @return ��������ͼ,ʧ�ܷ���null
	 */
	public Bitmap getImageThumbFromMK(String origPath, int width, int height)
	{
		
		//��������ͼ�����Ѹ��ļ��浽ָ��Ŀ¼�£��������ݿ���ͼƬ��Ϣ
		Bitmap thumbnail = null;
		Log.d(TAG, origPath + ":make thumbnail and insert message in database");
		ThumbnailCreator mCreator = new ThumbnailCreator(width, height);
		thumbnail = mCreator.createThumbnail(origPath);
		if(thumbnail == null) 
		{
			return null;
		}
		String name = null;
		try
		{
			name = origPath.substring(origPath.lastIndexOf("/") + 1, origPath.lastIndexOf("."));
		}catch(IndexOutOfBoundsException e)
		{
			e.printStackTrace();
			return null;
		}
		try {
			File f = new File(imageDB.getAppDir() + "/" + name);
			FileOutputStream fOut = null;
			fOut = new FileOutputStream(f);
			thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
			fOut.flush();
			fOut.close();
			imageDB.insert(origPath, f.getPath());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.d(TAG, "create temp file false");
			e.printStackTrace();
		}
		return thumbnail;
	}
	
	/**
	 * ������ݿ����ݴ������ͼ��Ϣ����Ӧ������ͼ�ļ�����Ҫ��ÿ���˳�����ǰ�����ʱ����Ϣ��ֻ����MaxThumbnailNum������ͼ��Ϣ��
	 * ��ֹ�ݴ�����������
	 */
	public void clearThumbnailData()
	{
		final int ORIG_PATH_COLUMN  = 0;
		final int THUMB_PATH_COLUMN = 1;
		final int CREATE_TIME		= 2;
		
		String[] columns = {ImageDatabase.ORIG_PATH, ImageDatabase.THUMB_PATH, ImageDatabase.CREATE_TIME};
		String sort = ImageDatabase.CREATE_TIME + " DESC";
		//�����������ѯ�Ľ��
		Cursor c = imageDB.query(columns, null, null, sort);
		int i = 0;
		if(c != null)
		{
			try
			{
				while(c.moveToNext())
				{
					i ++;
					//ɾ����MaxThumbnailNum�Ժ��ͼƬ������Ϣ
					if(i > MaxThumbnailNum)
					{
						String thumbPath  = c.getString(THUMB_PATH_COLUMN);
						String origPath   = c.getString(ORIG_PATH_COLUMN);
						File file = new File(thumbPath);
						try
						{
							if(file.delete())
							{
								imageDB.delete(origPath);
							}
						}catch(SecurityException e)
						{
						}
					}
				}
			}
			finally
			{
				c.close();
				c = null;
			}
		}
		else
		{
			Log.d(TAG, "cursor is null");
		}
	}
	
	public void closeDB()
	{
		imageDB.closeDatabase();
	}
}