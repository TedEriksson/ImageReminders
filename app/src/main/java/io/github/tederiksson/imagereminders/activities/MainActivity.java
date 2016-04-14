package io.github.tederiksson.imagereminders.activities;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.tederiksson.imagereminders.R;
import io.github.tederiksson.imagereminders.adapters.ImageItemsPagerAdapter;
import io.github.tederiksson.imagereminders.fragments.ImageItemsFragment;
import io.github.tederiksson.imagereminders.models.ImageItem;
import io.realm.Realm;

public class MainActivity extends AppCompatActivity implements ImageItemsFragment.OnListChangedListener {

    public static final int REQUEST_ADD_ITEM = 1;

    @Bind(R.id.container)
    CoordinatorLayout container;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.tabLayout)
    TabLayout tabLayout;

    @Bind(R.id.viewpager)
    ViewPager viewPager;

    @Bind(R.id.emptyListContainer)
    FrameLayout emptyListContainer;

    @Bind(R.id.emptyListText)
    TextView emptyListText;

    @Bind(R.id.fab)
    FloatingActionButton floatingActionButton;

    boolean isAnimatingIn = false;
    boolean isAnimatingOut = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        // Set no items text
        String text = getString(R.string.no_items_text);
        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(new RelativeSizeSpan(0.8f), text.indexOf('\n'), text.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        emptyListText.setText(spannableString);

        // Setup Tabs and fragments
        ImageItemsPagerAdapter imageItemsPagerAdapter = new ImageItemsPagerAdapter(this, getSupportFragmentManager());
        imageItemsPagerAdapter.setOnListChangedListener(this);
        viewPager.setAdapter(imageItemsPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position == 0 && positionOffset == 0) {
                    animateEnterNothingHere();
                } else if (position != 0) {
                    // Prevents view from every being visible outside of page 0
                    emptyListContainer.setAlpha(0);
                } else {
                    // Prevents view scrolling into done column
                    emptyListContainer.setTranslationX(-positionOffsetPixels);
                    animateExitNothingHere();
                }
            }
        });

        // On initial load, if items exist, hide empty text
        Realm realm = Realm.getInstance(this);
        if (realm.where(ImageItem.class).equalTo("done", false).count() != 0) {
            emptyListContainer.setAlpha(0);
        }
        realm.close();

        // Animate FAB in
        container.post(() -> {
            floatingActionButton.setScaleX(0);
            floatingActionButton.setScaleY(0);
            floatingActionButton.animate()
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .setStartDelay(600)
                    .scaleX(1)
                    .scaleY(1);
        });
    }

    @OnClick(R.id.fab)
    void clickFloatingActionButton() {
        viewPager.setCurrentItem(0, true);

        floatingActionButton.postDelayed(() -> {
            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(MainActivity.this,
                            floatingActionButton, "fab");

            Intent intent = new Intent(MainActivity.this, AddItemActivity.class);

            ActivityCompat.startActivityForResult(MainActivity.this, intent, REQUEST_ADD_ITEM, optionsCompat.toBundle());

            floatingActionButton.postDelayed(() -> {
                emptyListContainer.setAlpha(0);
                floatingActionButton.setTranslationY(0);
                floatingActionButton.setTranslationX(0);
            }, 250);
        }, 250);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ADD_ITEM) {
            onListChanged();
        }
    }

    /**
     * Called when returning to page 0,
     * will do nothing if ACTIVE items != 0
     */
    public void animateEnterNothingHere() {
        if (isAnimatingIn) {
            return;
        }

        Realm realm = Realm.getInstance(this);
        if (realm.where(ImageItem.class).equalTo("done", false).count() != 0) {
            animateExitNothingHere();
            realm.close();
            return;
        }
        realm.close();

        isAnimatingIn = true;

        container.post(() -> {
            emptyListContainer.animate()
                    .alpha(1);


            float width = container.getWidth();
            float height = container.getHeight();

            float density = getResources().getDisplayMetrics().density;

            Animator animX = ObjectAnimator.ofFloat(floatingActionButton, "translationX", floatingActionButton.getTranslationX(), (-width / 2) + (floatingActionButton.getWidth() / 2) + (density * 16));
            animX.setInterpolator(new DecelerateInterpolator());

            Animator animY = ObjectAnimator.ofFloat(floatingActionButton, "translationY", floatingActionButton.getTranslationY(), (-height / 2) + (floatingActionButton.getHeight()) + (density * 16));
            animY.setInterpolator(new AccelerateDecelerateInterpolator());

            AnimatorSet animSet = new AnimatorSet();
            animSet.playTogether(animX, animY);

            animSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    isAnimatingIn = false;
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }

            });

            animSet.setStartDelay(200);
            animSet.start();
        });
    }

    /**
     * Hides the nothing here text
     */
    public void animateExitNothingHere() {
        if (isAnimatingOut) {
            return;
        }

        isAnimatingOut = true;

        floatingActionButton.post(() -> {
            emptyListContainer.animate()
                    .alpha(0);

            Animator animX = ObjectAnimator.ofFloat(floatingActionButton, "translationX", floatingActionButton.getTranslationX(), 0);
            animX.setInterpolator(new DecelerateInterpolator());

            Animator animY = ObjectAnimator.ofFloat(floatingActionButton, "translationY", floatingActionButton.getTranslationY(), 0);
            animY.setInterpolator(new AccelerateDecelerateInterpolator());

            AnimatorSet animSet = new AnimatorSet();
            animSet.playTogether(animX, animY);

            animSet.addListener(new Animator.AnimatorListener() {

                @Override
                public void onAnimationEnd(Animator animator) {
                    isAnimatingOut = false;
                }

                @Override
                public void onAnimationStart(Animator animator) {
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                }
            });

            animSet.setStartDelay(0);
            animSet.start();
        });
    }

    @Override
    public void onListChanged() {
        if (viewPager.getCurrentItem() == 0) {
            animateEnterNothingHere();
        } else {
            animateExitNothingHere();
        }
    }
}
