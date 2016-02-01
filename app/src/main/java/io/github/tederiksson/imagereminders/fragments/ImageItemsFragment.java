package io.github.tederiksson.imagereminders.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.github.tederiksson.imagereminders.R;
import io.github.tederiksson.imagereminders.adapters.ImageItemAdapter;
import io.github.tederiksson.imagereminders.models.ImageItem;
import io.realm.Realm;
import io.realm.Sort;

/**
 * Created by Ted Eriksson on 30/01/16.
 */
public class ImageItemsFragment extends Fragment implements ImageItemAdapter.OnItemClickListener {

    public static final String ARG_DONE = "DONE";

    @Bind(R.id.recyclerView)
    RecyclerView recyclerView;

    private boolean showDone;
    private ImageItemAdapter imageItemAdapter;

    private OnListChangedListener onListChangedListener = null;

    public ImageItemsFragment() {
    }

    public static ImageItemsFragment newInstance(boolean showDone) {

        Bundle args = new Bundle();
        args.putBoolean(ARG_DONE, showDone);

        ImageItemsFragment fragment = new ImageItemsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnListChangedListener(OnListChangedListener onListChangedListener) {
        this.onListChangedListener = onListChangedListener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_items, container, false);
        ButterKnife.bind(this, view);

        Realm realm = Realm.getInstance(getActivity());

        showDone = getArguments().getBoolean(ARG_DONE, false);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        imageItemAdapter = new ImageItemAdapter(getActivity(), realm, realm.where(ImageItem.class).equalTo("done", showDone).findAllSorted("timestamp", Sort.DESCENDING));

        imageItemAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(imageItemAdapter);

        return view;
    }

    @Override
    public void onItemClick(View view, ImageItem imageItem, int position) {
        Realm realm = Realm.getInstance(getActivity());

        realm.beginTransaction();
        imageItem.setDone(!imageItem.isDone());
        imageItem.setTimestamp(System.currentTimeMillis());
        realm.commitTransaction();

        imageItemAdapter.notifyItemRemoved(position);

        if (onListChangedListener != null) {
            onListChangedListener.onListChanged();
        }
    }

    public interface OnListChangedListener {
        void onListChanged();
    }
}
