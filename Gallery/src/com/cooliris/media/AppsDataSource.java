package com.cooliris.media;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.cooliris.app.App;
import com.cooliris.app.Res;

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


public class AppsDataSource implements DataSource {
	private static final String TAG = "AppsDataSource";
	private Context mContext;
	private PackageManager mPackageManager;

	public AppsDataSource(final Context context) {
		mContext = context;
	}

	@Override
	public void loadMediaSets(MediaFeed feed) {
		MediaSet set = null;
		set = feed.addMediaSet(0, this); // Create dummy set.
        set.mName = "all apps";
        set.mId = 0;
        set.setNumExpectedItems(1);
        set.generateTitle(true);
        set.mPicasaAlbumId = Shared.INVALID;
	}

	@Override
	public void loadItemsForSet(MediaFeed feed, MediaSet parentSet,
			int rangeStart, int rangeEnd) {
		if (parentSet.mNumItemsLoaded > 0 ) {
			Log.d(TAG, "loadItemsForSet return");
            return;
        }
		final PackageManager packageManager = mPackageManager = mContext.getPackageManager();
        List<ResolveInfo> apps = null;
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        apps = packageManager.queryIntentActivities(mainIntent, 0);
        int size = apps.size();
        Log.d(TAG, "loadItemsForSet apps total = " + size);
		for (int i = 0; i < size; i++) {
			ResolveInfo info = apps.get(i);			
			MediaItem item = new MediaItem();
            item.mId = i;
            item.mFilePath = "" + i;
            item.drawable = getFullResIcon(info);
            if(i == 0){
            	item.iconID = Res.drawable.grid_check_on;
            }else{
            	item.iconID = getFullResIconID(info.activityInfo);
                final String packageName = info.activityInfo.applicationInfo.packageName;
                ComponentName componentName = new ComponentName(packageName,
                        info.activityInfo.name);
                item.packageName = packageName;
                item.intent = setActivity(componentName, Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            }
            

            item.setMediaType(MediaItem.MEDIA_TYPE_IMAGE);
            
            feed.addItemToMediaSet(item, parentSet);

		}
        parentSet.updateNumExpectedItems();
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
            d = resources.getDrawableForDensity(iconId, App.PIXEL_DENSITY_DPI);
        }
        catch (Resources.NotFoundException e)
        {
            d = null;
        }

        return (d != null) ? d : getFullResDefaultActivityIcon();
    }
    
    public Drawable getFullResDefaultActivityIcon()
    {
        return getFullResIcon(Resources.getSystem(),
                android.R.mipmap.sym_def_app_icon);
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

}
