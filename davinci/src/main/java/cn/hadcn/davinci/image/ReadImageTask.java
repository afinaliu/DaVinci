package cn.hadcn.davinci.image;

import android.content.Context;
import android.os.Looper;
import android.widget.ImageView;

import cn.hadcn.davinci.R;
import cn.hadcn.davinci.image.base.ImageEntity;
import cn.hadcn.davinci.image.base.ImageLoader;
import cn.hadcn.davinci.image.base.Util;
import cn.hadcn.davinci.log.VinciLog;
import pl.droidsonroids.gif.GifDrawable;

/**
 * read image from any where
 * Created by 90Chris on 2016/5/5.
 */
public class ReadImageTask {
    private final int DEFAULT_IMAGE_LOADING = R.drawable.image_loading;
    private final int DEFAULT_IMAGE_ERROR = R.drawable.image_load_error;

    private ImageView mImageView;
    private String mImageUrl;
    private int mLoadingImage = DEFAULT_IMAGE_LOADING;
    private int mErrorImage = DEFAULT_IMAGE_ERROR;
    private VinciImageLoader.ImageCache mImageCache;
    private ImageLoader mImageLoader;
    private Context mContext;
    private int mMaxSize;

    public ReadImageTask(Context context, VinciImageLoader.ImageCache imageCache, ImageLoader imageLoader, String imageUrl) {
        mImageUrl = imageUrl;
        mImageCache = imageCache;
        mImageLoader = imageLoader;
        mContext = context;
    }

    public final void execute(String requestBody) {
        if ( mImageUrl == null || mImageUrl.isEmpty() || Util.generateKey(mImageUrl).isEmpty() ) {
            mImageView.setImageDrawable(mContext.getResources().getDrawable(mErrorImage));
            return;
        }

        ImageEntity entity = mImageCache.getBitmap(Util.generateKey(mImageUrl + mMaxSize));

        if ( entity != null ) {
            VinciLog.d("Load image from cache, key = " + Util.generateKey(mImageUrl + mMaxSize));

            // if it's gif, show as gif
            if ( entity.isGif() ) {
                try {
                    GifDrawable gifDrawable = new GifDrawable(entity.getBytes());
                    mImageView.setImageDrawable(gifDrawable);
                } catch (Throwable e) {
                    VinciLog.w("pl.droidsonroids.gif.GifDrawable not found");
                }
            } else {
                mImageView.setImageBitmap(entity.getBitmap());
            }
        } else if ( mImageUrl.startsWith("http") ) {
            VolleyImageListener listener = new VolleyImageListener(mContext, mImageView, mImageCache);
            listener.setDefaultImage(mLoadingImage, mErrorImage);
            listener.setMaxSize(mMaxSize);
            VinciLog.d("Load image from web, url = " + mImageUrl );
            mImageLoader.get(mImageUrl, requestBody, listener);
        } else {
            mImageView.setImageDrawable(mContext.getResources().getDrawable(mErrorImage));
        }
    }

    protected void setView(ImageView imageView, int image_loading, int image_error) {
        mImageView = imageView;
        if ( image_loading != 0 ) mLoadingImage = image_loading;
        if ( image_error != 0 ) mErrorImage = image_error;
    }

    protected void setView(ImageView imageView) {
        mImageView = imageView;
    }

    protected void setSize(int size) {
        mMaxSize = size;
    }

    private void throwIfNotOnMainThread() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalStateException("ImageLoader must be invoked from the main thread.");
        }
    }
}
