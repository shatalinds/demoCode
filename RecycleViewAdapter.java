import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Dmitry Shatalin
 * mailto: shatalinds@gmail.com
 */

public class RecycleViewAdapter extends  RecyclerView.Adapter<RecycleViewAdapter.ViewHolder> {
    public static final String TAG = RecycleViewAdapter.class.getSimpleName();

    private List<String> mDataset;
    private Context mContext;

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.llItem) LinearLayout llItem;
        @BindView(R.id.ivItemLeft) ImageView ivItemLeft;
        @Nullable @BindView(R.id.ivItemMiddle) ImageView ivItemMiddle;
        @BindView(R.id.ivItemRight) ImageView ivItemRight;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    private int mSize;

    public RecycleViewAdapter(Context context, int size, List<String> dataset) {
        this.mDataset = dataset;
        this.mContext = context;
        this.mSize = size;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.photo_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    private boolean checkPos(int pos) {
        if (pos < mDataset.size())
            return true;
        return false;
    }

    private boolean bFirstLine = true;
    private int rightPos  = 0;
    private int middlePos = 0;
    private int leftPos   = 0;

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (!checkPos(leftPos))
            return;

        if (mContext.getResources().getBoolean(R.bool.isTablet)) {
            rightPos = bFirstLine ? position + 2 : rightPos + 3;
            if (checkPos(rightPos))
                Picasso.with(mContext)
                        .load(mDataset.get(rightPos))
                        .resize(mSize, mSize)
                        .into(holder.ivItemRight);

            middlePos = bFirstLine ? position + 1 : middlePos + 3;
            if (checkPos(middlePos))
                Picasso.with(mContext)
                        .load(mDataset.get(middlePos))
                        .resize(mSize, mSize)
                        .into(holder.ivItemMiddle);

            leftPos = bFirstLine ? position : leftPos + 3;
            if (checkPos(leftPos))
                Picasso.with(mContext)
                        .load(mDataset.get(leftPos))
                        .resize(mSize, mSize)
                        .into(holder.ivItemLeft);

        } else {
            rightPos = bFirstLine ? position + 1 : rightPos + 2;
            if (checkPos(rightPos))
                Picasso.with(mContext)
                        .load(mDataset.get(rightPos))
                        .resize(mSize, mSize)
                        .into(holder.ivItemRight);

            leftPos = bFirstLine ? position : leftPos + 2;
            if (checkPos(leftPos))
                Picasso.with(mContext)
                        .load(mDataset.get(leftPos))
                        .resize(mSize, mSize)
                        .into(holder.ivItemLeft);
        }

        bFirstLine = false;
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}