package com.xlauncher.media;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.xlauncher.app.App;
import com.xlauncher.app.Res;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;


public class AppsDataSource extends BroadcastReceiver implements DataSource {
	private static final String TAG = "AppsDataSource";
	private Context mContext;
	private PackageManager mPackageManager;
	private List<ResolveInfo> apps;
	private MediaFeed mMediaFeed;

	public AppsDataSource(final Context context) {
		mContext = context;
	}

	@Override
	public void loadMediaSets(MediaFeed feed) {
		Log.d(TAG, "loadMediaSets");
		mMediaFeed = feed;
		final PackageManager packageManager = mPackageManager = mContext
				.getPackageManager();
		if(apps != null){
			apps.clear();
		}		
		apps = null;
		final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		apps = packageManager.queryIntentActivities(mainIntent, 0);
		
		MediaSet set = null;
		set = feed.addMediaSet(0, this); // Create dummy set.
        set.mName = "所有应用";
        set.mId = 0;
        set.setNumExpectedItems(apps.size());
        set.generateTitle(true);
        set.mPicasaAlbumId = Shared.INVALID;
        
		set = feed.addMediaSet(1, this); // Create dummy set.
        set.mName = "经常使用";
        set.mId = 1;
        set.setNumExpectedItems(1);
        set.generateTitle(true);
        set.mPicasaAlbumId = Shared.INVALID;
        

	}

	@Override
	public void loadItemsForSet(MediaFeed feed, MediaSet parentSet,
			int rangeStart, int rangeEnd) {
		if (parentSet.mNumItemsLoaded == parentSet.getNumExpectedItems()) {
			Log.d(TAG, "loadItemsForSet return");
			return;
		}
		
		int size = apps.size();
		if(parentSet.mId == 1)
			size /= 10;
		
		Log.d(TAG, "loadItemsForSet apps total = " + size);
		Log.d(TAG, "rangeStart = " + rangeStart + " rangeEnd = " + rangeEnd);
		rangeEnd = FloatUtils.clamp(rangeEnd, 0, size);
		for (int i = rangeStart; i < rangeEnd; i++) {
			ResolveInfo info = apps.get(i);
			MediaItem item = new MediaItem();
			item.mId = i;
			item.mFilePath = "" + i;
			item.drawable = getFullResIcon(info);
			item.iconID = getFullResIconID(info.activityInfo);
			final String packageName = info.activityInfo.applicationInfo.packageName;
			ComponentName componentName = new ComponentName(packageName,
					info.activityInfo.name);
			item.packageName = packageName;
			item.mCaption = info.loadLabel(mPackageManager).toString();//info.activityInfo.name;
			item.intent = setActivity(componentName,
					Intent.FLAG_ACTIVITY_NEW_TASK
							| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

			item.setMediaType(MediaItem.MEDIA_TYPE_IMAGE);
			feed.addItemToMediaSet(item, parentSet);
		}
		//parentSet.updateNumExpectedItems();
		parentSet.generateTitle(true);

	}
	
    final Intent setActivity(ComponentName className, int launchFlags)
    {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(className);
        intent.setFlags(launchFlags);
        return intent;
    }
	
    public Drawable getFullResIcon(ResolveInfo info)
    {
        return getFullResIcon(info.activityInfo);
    }

    public Drawable getFullResIcon(ActivityInfo info)
    {

        Resources resources;
        try
        {
            resources = mPackageManager
                    .getResourcesForApplication(info.applicationInfo);
        }
        catch (PackageManager.NameNotFoundException e)
        {
            resources = null;
        }
        if (resources != null)
        {
            int iconId = info.getIconResource();
            if (iconId != 0)
            {
                return getFullResIcon(resources, iconId);
            }
        }
        return getFullResDefaultActivityIcon();
    }
    
	public int getFullResIconID(ActivityInfo info) {
		Resources resources;
		int iconId = 0;
		try {
			resources = mPackageManager
					.getResourcesForApplication(info.applicationInfo);
		} catch (PackageManager.NameNotFoundException e) {
			resources = null;
		}
		if (resources != null) {
			iconId = info.getIconResource();
		}
		return iconId;
	}
    
    public Drawable getFullResIcon(Resources resources, int iconId)
    {
        Drawable d;
        try
        {
            d = resources.getDrawable(iconId);
            //resources.getdr
        }
        catch (Resources.NotFoundException e)
        {
            d = null;
        }
        catch(NoSuchMethodError e)
        {
        	d = resources.getDrawable(iconId);
        }

        return (d != null) ? d : getFullResDefaultActivityIcon();
    }
    
    public Drawable getFullResDefaultActivityIcon()
    {
//        return getFullResIcon(Resources.getSystem(),
//                android.R.mipmap.sym_def_app_icon);
        return getFullResIcon(Resources.getSystem(),
                android.R.drawable.sym_action_email);
    }

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean performOperation(int operation,
			ArrayList<MediaBucket> mediaBuckets, Object data) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public DiskCache getThumbnailCache() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getDatabaseUris() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void refresh(MediaFeed feed, String[] databaseUris) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onReceive(Context arg0, Intent intent) {
		Log.d(TAG, "onReceive");
		final String action = intent.getAction();
		 if (Intent.ACTION_PACKAGE_CHANGED.equals(action)
	                || Intent.ACTION_PACKAGE_REMOVED.equals(action)
	                || Intent.ACTION_PACKAGE_ADDED.equals(action))
	        {
			 Log.d(TAG, "onReceive ACTION_PACKAGE_CHANGED");
			 	if(mContext != null && mContext instanceof Gallery){
			 		((Gallery)(mContext)).sendInitialMessage();
			 	}
	        }
		
	}

}
