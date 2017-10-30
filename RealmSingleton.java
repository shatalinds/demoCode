import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by Dmitry Shatalin
 * mailto: shatalinds@gmail.com
 */

public class RealmSingleton {
    private static RealmSingleton mInstance = null;
    private static Context mCtx = null;

    private RealmSingleton(Context context) {
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(context).deleteRealmIfMigrationNeeded().build();
        Realm.setDefaultConfiguration(realmConfig);
    }

    public static RealmSingleton getInstance(@NonNull final Context context) {
        if (mInstance == null) {
            synchronized (RealmSingleton.class) {
                if (mInstance == null) {
                    mCtx = context;
                    mInstance = new RealmSingleton(mCtx);
                }
            }
        }
        return mInstance;
    }

    public Realm getRealm() {
        return Realm.getDefaultInstance();
    }

    public <E extends RealmObject, Q extends Object> boolean restore(Class<E> cl, String field, Q value, Interfaces.onRealmCallback callback) {
        Realm realm = getRealm();
        realm.beginTransaction();

        RealmResults<E> results = addParamToQuery(realm.where(cl), field, value).findAll();

        if (results.size() > 1) {
            realm.delete(cl);
        }
        realm.commitTransaction();

        if (results.size() == 0) {
            return false;
        }

        if (results.size() == 1) {
            E result = results.first();
            callback.onRealmCallback(result);
        }
        return true;
    }

    public <E extends RealmObject> boolean restore(Class<E> cl, Interfaces.onRealmCallback callback) {
        return restore(cl, null, null, callback);
    }


    public <E extends RealmObject> boolean saveEq(Class<E> cl, String field, String value, Interfaces.onRealmCallback callback) {
        Realm realm = getRealm();
        realm.beginTransaction();
        RealmResults<E> results;

        if (field == null && value == null) {
            results = realm.where(cl).findAll();
        } else {
            results = realm.where(cl).equalTo(field, value).findAll();
        }

        E obj = null;
        if (results.size() == 1) {
            obj = results.first();
        } else if (results.size() == 0) {
            obj = realm.createObject(cl);
        }
        callback.onRealmCallback(obj);
        realm.copyToRealmOrUpdate(obj);
        realm.commitTransaction();
        return true;
    }

    public <E extends RealmObject> boolean save(Class<E> cl, Interfaces.onRealmCallback callback) {
        return saveEq(cl, null, null, callback);
    }

    public <T extends RealmObject, V> T findFirst(Class<T> cl, String field, V value) {
        Realm realm = getRealm();
        realm.beginTransaction();

        RealmResults<T> results = addParamToQuery(realm.where(cl), field, value).findAll();

        T t = null;
        if (results.size() > 0)
            t = results.first();

        realm.commitTransaction();

        return t;
    }

    private <T extends RealmObject, V> RealmQuery<T> addParamToQuery(RealmQuery<T> query, String field, V value) {
        if (value instanceof Integer) {
            Integer iValue = (Integer) value;
            return query.equalTo(field, iValue);
        } else if (value instanceof Float) {
            Float fValue = (Float) value;
            return query.equalTo(field, fValue);
        } else if (value instanceof String) {
            String sValue = (String) value;
            return query.equalTo(field, sValue);
        } else if (value instanceof Boolean) {
            Boolean bValue = (Boolean) value;
            return query.equalTo(field, bValue);
        }
        return query;
    }

    public <T extends RealmObject, V, V2> void removeFirst(Class<T> cl, String field, V value) {
        Realm realm = getRealm();
        realm.beginTransaction();
        addParamToQuery(realm.where(cl), field, value).findAll().deleteFirstFromRealm();
        realm.commitTransaction();
    }

    public <T extends RealmObject, V, V2> T findFirst(Class<T> cl, String field, V value, String field2, V2 value2) {
        Realm realm = getRealm();
        realm.beginTransaction();

        RealmResults<T> results = addParamToQuery(
                addParamToQuery(realm.where(cl), field, value),
                field2, value2).findAll();

        T t = null;
        if (results.size() > 0)
            t = results.first();

        realm.commitTransaction();

        return t;
    }

    public <T extends RealmObject, V, V2, V3> T findFirst(Class<T> cl, String field, V value,
                                                          String field2, V2 value2, String field3, V3 value3) {
        Realm realm = getRealm();
        realm.beginTransaction();

        RealmResults<T> results = addParamToQuery(
                addParamToQuery(
                        addParamToQuery(
                                realm.where(cl), field, value)
                        , field2, value2),
                field3, value3).findAll();

        T t = null;
        if (results.size() > 0)
            t = results.first();

        realm.commitTransaction();

        return t;
    }

    public <E extends RealmObject> int getNextId(Class<E> cl) {
        Number maxId = RealmSingleton.getInstance(mCtx).getRealm().where(cl).max("_id");
        return (maxId == null) ? 1 : maxId.intValue() + 1;
    }
}