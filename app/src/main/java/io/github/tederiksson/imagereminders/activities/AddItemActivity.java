package io.github.tederiksson.imagereminders.activities;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.tederiksson.imagereminders.R;
import io.github.tederiksson.imagereminders.models.ImageItem;
import io.realm.Realm;

/**
 * Created by Ted Eriksson on 30/01/16.
 */
public class AddItemActivity extends AppCompatActivity {

    static final int REQUEST_TAKE_PHOTO = 1;

    @Bind(R.id.imageView)
    ImageView imageView;

    @Bind(R.id.description)
    EditText description;

    @Bind(R.id.save)
    FloatingActionButton save;

    @Bind(R.id.textWrapper)
    TextInputLayout textWrapper;

    @Bind(R.id.blue)
    View blue;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    private File file = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Intro Animations
        save.postDelayed(() -> save.setImageDrawable(
                getResources().getDrawable(R.drawable.ic_check_white_24dp)), 150);
        save.animate()
                .setDuration(300)
                .rotation(360);
        textWrapper.setTranslationY(100);
        textWrapper.setAlpha(0);
        textWrapper.animate()
                .translationY(0)
                .alpha(1)
                .setStartDelay(300)
                .setInterpolator(new DecelerateInterpolator());
        blue.post(() -> {
            blue.setTranslationY(-blue.getHeight());
            blue.setVisibility(View.VISIBLE);
            blue.animate()
                    .translationY(0)
                    .setInterpolator(new FastOutSlowInInterpolator());
        });
    }

    @OnClick(R.id.save)
    void clickSave(View view) {
        if (file == null) {
            Snackbar.make(view, R.string.error_no_image, Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(description.getText())) {
            Snackbar.make(view, R.string.error_no_message, Snackbar.LENGTH_SHORT).show();
            return;
        }

        Realm realm = Realm.getInstance(this);

        realm.beginTransaction();
        ImageItem imageItem = realm.createObject(ImageItem.class);
        imageItem.setText(description.getText().toString());
        imageItem.setTimestamp(System.currentTimeMillis());
        imageItem.setDone(false);
        imageItem.setImageFile(file.getAbsolutePath());
        realm.commitTransaction();
        realm.close();

        Log.d("Path", "clickSave: " + file.getAbsolutePath());

        setResult(RESULT_OK);
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        // Hide keyboard
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(description.getWindowToken(), 0);

        // Exit Animations
        blue.animate()
                .translationY(-blue.getHeight())
                .setInterpolator(new FastOutSlowInInterpolator());

        imageView.animate()
                .translationY(-imageView.getHeight())
                .setInterpolator(new FastOutSlowInInterpolator());

        save.postDelayed(() -> save.setImageDrawable(
                getResources().getDrawable(R.drawable.ic_add_white_24dp)), 150);

        textWrapper.animate()
                .translationY(100)
                .alpha(0)
                .setInterpolator(new AccelerateInterpolator());

        save.animate()
                .setDuration(300)
                .rotation(0)
        .withEndAction(() -> ActivityCompat.finishAfterTransition(AddItemActivity.this));

    }

    @OnClick(R.id.picture)
    void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show();
            }
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO) {
            // Successful photo
            if (resultCode == RESULT_OK) {
                int density = (int) getResources().getDisplayMetrics().density;

                Glide.with(this).load(file).centerCrop().listener(new RequestListener<File, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, File model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, File model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            int cy = imageView.getHeight() / 2;
                            int cx = imageView.getWidth() / 2;

                            Animator animator = ViewAnimationUtils.createCircularReveal(
                                    imageView,
                                    cx,
                                    cy,
                                    0, (float) Math.hypot(cx, cy));
                            animator.start();
                        }
                        return false;
                    }
                }).into(imageView);
            } else {
                // Cancelled/Failed photo
                file = null;
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String imageFileName = "JPEG_" + System.currentTimeMillis() + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  // prefix
                ".jpg",         // suffix
                storageDir      // directory
        );

        file = image;

        return image;
    }
}
