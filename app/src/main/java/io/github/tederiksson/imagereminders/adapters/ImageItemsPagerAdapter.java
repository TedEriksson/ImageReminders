package io.github.tederiksson.imagereminders.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

import io.github.tederiksson.imagereminders.R;
import io.github.tederiksson.imagereminders.fragments.ImageItemsFragment;

/**
 * Created by Ted Eriksson on 30/01/16.
 */
public class ImageItemsPagerAdapter extends FragmentPagerAdapter implements ImageItemsFragment.OnListChangedListener {

    private Context context;
    private List<Fragment> fragments = new ArrayList<>();

    private ImageItemsFragment.OnListChangedListener onListChangedListener;

    public ImageItemsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);

        this.context = context;

        ImageItemsFragment active = ImageItemsFragment.newInstance(false);
        active.setOnListChangedListener(this);
        fragments.add(active);

        ImageItemsFragment done = ImageItemsFragment.newInstance(true);
        done.setOnListChangedListener(this);
        fragments.add(done);
    }

    public void setOnListChangedListener(ImageItemsFragment.OnListChangedListener onListChangedListener) {
        this.onListChangedListener = onListChangedListener;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return context.getResources().getString(R.string.active);
            case 1:
                return context.getResources().getString(R.string.done);
            default:
                return "NULL";
        }
    }

    @Override
    public void onListChanged() {
        if (onListChangedListener != null) {
            onListChangedListener.onListChanged();
        }
    }
}
