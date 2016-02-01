package io.github.tederiksson.imagereminders.activities;

import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.tederiksson.imagereminders.R;

/**
 * Created by Ted Eriksson on 31/01/16.
 */
public class ViewImageActivity extends AppCompatActivity {

    @Bind(R.id.imageView)
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);
        ButterKnife.bind(this);

        final int screenWidth = getResources().getDisplayMetrics().widthPixels;
        imageView.getLayoutParams().width = ((int) (screenWidth * 0.9f));
        imageView.getLayoutParams().height = ((int) (screenWidth * 0.9f));
        imageView.requestLayout();

        String file = getIntent().getStringExtra("file");

        Glide.with(this).load(file).centerCrop().dontAnimate().into(imageView);
    }

    @OnClick(R.id.container)
    void clickContainer() {
        ActivityCompat.finishAfterTransition(this);
    }
}
