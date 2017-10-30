import android.app.Application;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.SqlBrite;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by Dmitry Shatalin
 * mailto: shatalinds@gmail.com
 */

@Module
public final class DataModule {

    private static final int DISK_CACHE_SIZE = 100 * 1024 * 1024; // 100MB
    private static final int CONNECTION_TIMEOUT_SECONDS = 20;

    public DataModule() {}

    @Provides
    @Singleton SharedPreferences sharedPreferences(final Application application) {
        return PreferenceManager.getDefaultSharedPreferences(application);
    }

    @Provides
    @Singleton AuthenticationInterceptor provideInterceptor(final Application application) {
        return new AuthenticationInterceptor(application);
    }

    @Provides
    @Singleton PrefsHelper prefsHelper(final SharedPreferences preferences) {
        return new PrefsHelper(preferences);
    }

    @Provides
    @Singleton UserHelper userHelper(final Application application,
                                     final PrefsHelper prefsHelper,
                                     final AuthenticationInterceptor authInterceptor,
                                     final MessengerRepository messengerRepository) {
        return new UserHelper(application, prefsHelper, authInterceptor, messengerRepository);
    }

    @Provides
    @Singleton Gson gson() {
        return new GsonBuilder()
                .serializeNulls()
                .registerTypeAdapter(Calendar.class, new CalendarSerializer())
                .registerTypeAdapter(Calendar.class, new CalendarDeserializer())
                .registerTypeAdapter(Date.class, new DateSerializer())
                .registerTypeAdapter(Date.class, new DateDeserializer())
                .registerTypeAdapterFactory(CustomAdapterFactory.create())
                .setLenient()
                .create();
    }

    @Provides
    @Singleton Cache okHttpCache(final Application application) {
        File cacheDir = new File(application.getCacheDir(), "http");
        return new Cache(cacheDir, DISK_CACHE_SIZE);
    }

    @Provides
    @Singleton OkHttpClient okHttpClient(final Cache cache,
                                         final AuthenticationInterceptor authInterceptor) {

        final HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(BuildConfig.DEBUG
                ? HttpLoggingInterceptor.Level.HEADERS
                : HttpLoggingInterceptor.Level.NONE);

        final OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        builder.readTimeout(CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        builder.cache(cache);

        builder.addInterceptor(logging);
        builder.addInterceptor(authInterceptor);
        builder.addInterceptor(new ContentTypeInterceptor());
        builder.addInterceptor(new LocalizationInterceptor());

        return builder.build();
    }

    @Provides
    @Singleton IntellectMoneyApi intellectMoneyApi(OkHttpClient client) {
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(IntellectMoneyApi.BASE_URL)
                .client(client)
                .build();
        return retrofit.create(IntellectMoneyApi.class);
    }

    @Provides
    @Singleton KnowledgeBaseApi knowledgeBaseApi(final Gson gson,
                                                 final OkHttpClient client) {
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addConverterFactory(new GsonStringConverterFactory(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(KnowledgeBaseApi.BASE_URL)
                .client(client)
                .build();
        return retrofit.create(KnowledgeBaseApi.class);
    }
}
