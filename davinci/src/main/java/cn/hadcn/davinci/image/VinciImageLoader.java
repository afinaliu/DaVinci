package cn.hadcn.davinci.image;

import android.content.Context;
import android.widget.ImageView;

import java.io.File;

import cn.hadcn.davinci.image.base.ImageEntity;
import cn.hadcn.davinci.image.base.ImageLoader;
import cn.hadcn.davinci.image.base.Util;
import cn.hadcn.davinci.image.cache.DiskLruImageCache;
import cn.hadcn.davinci.log.VinciLog;
import cn.hadcn.davinci.volley.RequestQueue;


/**
 * DaImageLoader
 * Created by 90Chris on 2015/9/11.
 */
public class VinciImageLoader {
    private String mCacheDir = null;
    private ImageCache mImageCache;
    private ImageLoader mImageLoader;
    private Context mContext;
    private int mMaxSize = 0;
    private final static int CACHE_SIZE = 1024 * 1024 * 50;
    private ReadImageTask mReadImageTask;

    /**
     * Simple cache adapter interface. If provided to the ImageLoader, it
     * will be used as an L1 cache before dispatch to Volley. Implementations
     * must not block. Implementation with an LruCache is recommended.
     */
    public interface ImageCache {
        ImageEntity getBitmap(String url);
        void putBitmap(String url, byte[] data);
    }

    public VinciImageLoader(Context context, RequestQueue requestQueue) {
        mCacheDir = getDiskCacheDir(context);
        mContext = context;
        mImageCache = new DiskLruImageCache(mCacheDir, CACHE_SIZE);
        mImageLoader = new ImageLoader(requestQueue);
    }

    private String getDiskCacheDir(Context context) {
        final String CACHE_DIR_NAME = "imgCache";
        final String cachePath = context.getCacheDir().getPath();
        return cachePath + File.separator + CACHE_DIR_NAME;
    }

    public String getAbsolutePath( String fileName ) {
        return mCacheDir + File.separator + Util.generateKey(fileName) + ".0";
    }

    public ImageEntity getImage(String name) {
        String key = Util.generateKey(name);
        if ( key.isEmpty() ) throw new RuntimeException("key is invalid");

        try {
            return mImageCache.getBitmap(key);
        } catch (NullPointerException e) {
            VinciLog.w("Get Image failed, name = " + key);
            return null;
        }
    }

    public void putImage(String name, byte[] bitmap) {
        String key = Util.generateKey(name);
        if ( key.isEmpty() ) throw new RuntimeException("key is invalid");
        try {
            mImageCache.putBitmap(key, bitmap);
        } catch (NullPointerException e) {
            VinciLog.e("Put Image failed, name cannot be null", e);
        }
    }

    public VinciImageLoader load(String url) {
        mReadImageTask = new ReadImageTask(mContext, mImageCache, mImageLoader, url);
        return this;
    }

    private String gBody = null;
    /**
     * set image load global body, post way
     * @param body post body, if null, change to get way
     */
    public void gBody(String body) {
        gBody = body;
    }

    private String mBody = null;
    /**
     * load image using post way, pass body part
     * @param body post body
     * @return this
     */
    public VinciImageLoader body(String body) {
        mBody = body;
        return this;
    }

    public void into(ImageView imageView) {
        into(imageView, 0, 0);
    }

    public void into(ImageView imageView, int loadingImage, int errorImage) {
        mReadImageTask.setView(imageView, loadingImage, errorImage);
        mReadImageTask.setSize(mMaxSize);
        if ( mBody != null ) mReadImageTask.execute(mBody);
        else mReadImageTask.execute(gBody);
        mBody = null;
    }

    /**
     * limit the max size of an image will be displayed, height and width are both shorter than maxPix
     * @param maxPix max pixels of height and width
     * @return DaImageLoader instance
     */
    public VinciImageLoader resize(int maxPix) {
        mMaxSize = maxPix;
        return this;
    }
}
