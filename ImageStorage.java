import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.ByteArrayOutputStream;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Dmitry Shatalin
 * mailto: shatalinds@gmail.com
 */

public class ImageStorage extends RealmObject {
    @PrimaryKey
    private int _id;


    @SerializedName("id")
    @Expose
    private int id;


    @SerializedName("isPost")
    @Expose
    private boolean isPost;


    @SerializedName("isPostAva")
    @Expose
    private boolean isPostAva;


    @SerializedName("isAva")
    @Expose
    private boolean isAva;


    @SerializedName("isAvaBlur")
    @Expose
    private boolean isAvaBlur;


    @SerializedName("groupId")
    @Expose
    private int groupId;


    @SerializedName("sector")
    @Expose
    private int sector;


    @SerializedName("bitmapData")
    @Expose
    protected byte[] bitmapData;



    public void set_id(int _id) {
        this._id = _id;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getSector() {
        return sector;
    }

    public void setSector(int sector) {
        this.sector = sector;
    }


    public boolean isAva() {
        return isAva;
    }

    public void setAva(boolean ava) {
        isAva = ava;
    }


    public boolean isAvaBlur() {
        return isAvaBlur;
    }

    public void setAvaBlur(boolean avaBlur) {
        isAvaBlur = avaBlur;
    }


    public boolean isPost() {
        return isPost;
    }

    public void setPost(boolean post) {
        isPost = post;
    }


    public boolean isPostAva() {
        return isPostAva;
    }

    public void setPostAva(boolean postAva) {
        isPostAva = postAva;
    }


    public Bitmap getBitmap() {
        Bitmap bm = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
        return bitmapData == null ? null : bm;
    }

    public void setBitmap(Bitmap bmp) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        bitmapData = stream.toByteArray();
    }
}
