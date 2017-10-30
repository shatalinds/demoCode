import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;

import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import rx.functions.Func0;

/**
 * Created by Dmitry Shatalin
 * mailto: shatalinds@gmail.com
 */

public class ImageLoaderHelper {

    public static void load(final Context context, final String path, final Interfaces.onLoadListenerImageId callback) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Bitmap bm = null;
                try {
                    bm = Picasso.with(context).load(path).get();
                } catch (IOException e) {
                    e.printStackTrace();
                    callback.onError(e.getMessage());
                }
                callback.onLoad(bm);
                return null;
            }
        }.execute();
    }

    public static Picasso configureLoader(Context context) {
        final int memClass = ((ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE))
                .getLargeMemoryClass();
        final int cacheSize = 1024 * 1024 * memClass / 4;
        return new Picasso.Builder(context).memoryCache(new LruCache(cacheSize)).build();
    }

    public static Observable<Bitmap> load(Picasso picasso, final String path) {
        return Observable
                .defer(() -> Observable.just(
                        picasso.load(path)
                                .get()))
                .observeOn(Schedulers.newThread())
                .filter(bitmap -> bitmap != null);
    }

    public static Observable<Bitmap> load(Picasso picasso, BitmapDrawable placeholder,final String path) {
        if (placeholder != null) {
            final int width = placeholder.getBitmap().getWidth();
            final int height = placeholder.getBitmap().getHeight();
            return Observable
                    .defer(() -> Observable.just(
                            picasso.load(path)
                                    .placeholder(placeholder)
                                    .resize(width, height)
                                    .get()))
                    .observeOn(Schedulers.newThread())
                    .filter(bitmap -> bitmap != null);
        } else {
            return load(picasso, path);
        }
    }

    public static Observable<Bitmap> loadNoFade(Picasso picasso, final String path) {
        return Observable
                .defer(() -> Observable.just(
                        picasso.load(path)
                               .noFade()
                               .get()))
                .observeOn(Schedulers.newThread())
                .filter(bitmap -> bitmap != null);
    }
}
