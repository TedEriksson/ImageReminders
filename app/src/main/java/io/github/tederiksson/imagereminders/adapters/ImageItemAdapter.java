package io.github.tederiksson.imagereminders.adapters;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;
import io.github.tederiksson.imagereminders.R;
import io.github.tederiksson.imagereminders.activities.ViewImageActivity;
import io.github.tederiksson.imagereminders.models.ImageItem;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Created by Ted Eriksson on 30/01/16.
 */
public class ImageItemAdapter extends RecyclerView.Adapter<ImageItemAdapter.ViewHolder> {

    RealmResults<ImageItem> realmResults;

    private final Activity activity;
    private final RealmChangeListener listener;

    private OnItemClickListener onItemClickListener;

    private int oldResultsSize = 0;

    public ImageItemAdapter(Activity activity, Realm realm, final RealmResults<ImageItem> realmResults) {
        this.activity = activity;
        this.realmResults = realmResults;

        this.listener = () -> {
            int newResultsSize = realmResults.size();

            if (newResultsSize > oldResultsSize) {
                for (int i = 0; i < (newResultsSize - oldResultsSize); i++) {
                    notifyItemInserted(0);
                }
            }

            oldResultsSize = newResultsSize;
        };

        if (realmResults != null) {
            oldResultsSize = realmResults.size();
            realm.addChangeListener(listener);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_image_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final ImageItem imageItem = realmResults.get(position);
        holder.itemView.setEnabled(true);
        holder.checkBox.setClickable(true);
        holder.textView.setText(imageItem.getText());
        holder.checkBox.setChecked(imageItem.isDone());

        Glide.with(holder.itemView.getContext()).load(imageItem.getImageFile()).into(holder.imageView);

        holder.itemView.setOnClickListener(view -> {
            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, holder.imageView ,"image");

            Intent intent = new Intent(view.getContext(), ViewImageActivity.class);
            intent.putExtra("file", imageItem.getImageFile());
            ActivityCompat.startActivity(activity, intent, optionsCompat.toBundle());
        });

        holder.checkBox.setOnClickListener(view -> {
            holder.itemView.setEnabled(false);
            holder.checkBox.setClickable(false);
            new Handler().postDelayed(() -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(view, imageItem, holder.getAdapterPosition());
                }
            }, 200);
        });
    }

    @Override
    public int getItemCount() {
        if (realmResults == null) {
            return 0;
        }

        return realmResults.size();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView imageView;
        TextView textView;
        CheckBox checkBox;

        public ViewHolder(View itemView) {
            super(itemView);

            imageView = (CircleImageView) itemView.findViewById(R.id.image);
            textView = (TextView) itemView.findViewById(R.id.text);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkbox);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, ImageItem imageItem, int position);
    }
}
