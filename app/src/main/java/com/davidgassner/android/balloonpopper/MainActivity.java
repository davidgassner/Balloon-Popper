package com.davidgassner.android.balloonpopper;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.davidgassner.android.balloonpopper.dialogs.MyAlertDialog;
import com.davidgassner.android.balloonpopper.utils.PreferencesHelper;
import com.davidgassner.android.balloonpopper.utils.SoundHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity
        implements Balloon.BalloonListener {

    private static final String TAG = "MainActivity";

    private static final int BALLOONS_PER_LEVEL = 10;
    private static final int NUMBER_OF_PINS = 5;

    private static final int MIN_ANIMATION_DELAY = 500;
    private static final int MAX_ANIMATION_DELAY = 1500;
    private static final int MIN_ANIMATION_DURATION = 1000;
    private static final int MAX_ANIMATION_DURATION = 8000;
    private static final String ACTION_NEXT_LEVEL = "action_next_level";
    private static final String ACTION_RESTART_GAME = "action_restart_game";

    private ViewGroup mContentView;
    private SoundHelper mSoundHelper;
    private List<ImageView> mPinImages = new ArrayList<>();
    private List<Balloon> mBalloons = new ArrayList<>();
    private TextView mScoreDisplay, mLevelDisplay;
    private Button mGoButton;
    private String mNextAction = ACTION_RESTART_GAME;
    private boolean mPlaying;
    private int[] mBalloonColors = new int[3];
    private int mNextColor, mBalloonsPopped,
            mScreenWidth, mScreenHeight,
            mPinsUsed = 0,
            mScore = 0, mLevel = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setBackgroundDrawableResource(R.drawable.modern_background);

//      Load the activity layout, which is an empty canvas
        setContentView(R.layout.activity_main);

//      Get background reference.
        mContentView = (ViewGroup) findViewById(R.id.content_view);
        if (mContentView == null) throw new AssertionError();
        mContentView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    setToFullScreen();
                }
                return false;
            }
        });
        setToFullScreen();

//      After the layout is complete, get screen dimensions from the layout.
        ViewTreeObserver viewTreeObserver = mContentView.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mContentView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mScreenWidth = mContentView.getWidth();
                    mScreenHeight = mContentView.getHeight();
                }
            });
        }

//      Initialize sound helper class that wraps SoundPool for audio effects
        mSoundHelper = new SoundHelper(this);
        mSoundHelper.prepareMusicPlayer(this);

//      Initialize display elements
        mPinImages.add((ImageView) findViewById(R.id.pushpin1));
        mPinImages.add((ImageView) findViewById(R.id.pushpin2));
        mPinImages.add((ImageView) findViewById(R.id.pushpin3));
        mPinImages.add((ImageView) findViewById(R.id.pushpin4));
        mPinImages.add((ImageView) findViewById(R.id.pushpin5));
        mScoreDisplay = (TextView) findViewById(R.id.score_display);
        mLevelDisplay = (TextView) findViewById(R.id.level_display);

//      Display current level and score
        updateDisplay();

//      Initialize balloon colors: red, white and blue
        mBalloonColors[0] = Color.argb(255, 255, 0, 0);
        mBalloonColors[1] = Color.argb(255, 0, 255, 0);
        mBalloonColors[2] = Color.argb(255, 0, 0, 255);

//      Get button references
        mGoButton = (Button) findViewById(R.id.go_button);

//      Handle button click
        if (mGoButton == null) throw new AssertionError();
        mGoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlaying) {
                    stopGame();
                } else {
                    switch (mNextAction) {
                        case ACTION_RESTART_GAME:
                            startGame();
                            break;
                        case ACTION_NEXT_LEVEL:
                            startLevel();
                            break;
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        stopGame();
        super.onBackPressed();
    }

    private void setToFullScreen() {

        //      Set full screen mode
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    private void startGame() {

        setToFullScreen();

//      Reset score and level
        mScore = 0;
        mLevel = 1;

//      Update display
        updateDisplay();
        mGoButton.setText(R.string.stop_game);

//      Reset pins
        mPinsUsed = 0;
        for (ImageView pin : mPinImages) {
            pin.setImageResource(R.drawable.pin);
        }

//      Start the first level
        startLevel();

        mSoundHelper.playMusic();
    }

    private void stopGame() {
        mGoButton.setText(R.string.play_game);
        mPlaying = false;
        gameOver(false);
    }

    private void startLevel() {

//      Display the current level and score
        updateDisplay();
        mGoButton.setText(R.string.stop_game);

//      Reset flags for new level
        mPlaying = true;
        mBalloonsPopped = 0;

//      integer arg for BalloonLauncher indicates the level
        BalloonLauncher mLauncher = new BalloonLauncher();
        mLauncher.execute(mLevel);

    }

    private void finishLevel() {
        PreferencesHelper.setCurrentScore(this, mScore);
        PreferencesHelper.setCurrentLevel(this, mLevel);
        Toast.makeText(MainActivity.this,
                String.format(getString(R.string.you_finished_level_n), mLevel),
                Toast.LENGTH_LONG).show();

        mPlaying = false;
        mLevel++;
        mGoButton.setText(String.format("Start level %s", mLevel));
        mNextAction = ACTION_NEXT_LEVEL;
    }

    private void updateDisplay() {
        mScoreDisplay.setText(String.valueOf(mScore));
        mLevelDisplay.setText(String.valueOf(mLevel));
    }

    private void launchBalloon(int x) {

//      Balloon is launched from activity upon progress update from the AsyncTask
//      Create new imageview and set its tint color
        Balloon balloon = new Balloon(this, mBalloonColors[mNextColor], 150, mLevel);
        mBalloons.add(balloon);

//      Reset color for next balloon
        if (mNextColor + 1 == mBalloonColors.length) {
            mNextColor = 0;
        } else {
            mNextColor++;
        }

//      Set balloon vertical position and dimensions, add to container
        balloon.setX(x);
        balloon.setY(mScreenHeight + balloon.getHeight());
        mContentView.addView(balloon);

//      Let 'er fly
        int duration = Math.max(MIN_ANIMATION_DURATION, MAX_ANIMATION_DURATION - (mLevel * 1000));
        balloon.releaseBalloon(mScreenHeight, duration);

    }

    @Override
    public void popBalloon(Balloon balloon, boolean userTouch) {

//      Play sound, make balloon go away
        mSoundHelper.playSound(balloon);
        mContentView.removeView(balloon);
        mBalloons.remove(balloon);
        mBalloonsPopped++;

//      If balloon pop was caused by user, it's a point; otherwise,
//      a balloon hit the top of the screen and it's a life lost
        if (userTouch) {
            mScore++;
        } else {
            mPinsUsed++;
            if (mPinsUsed <= mPinImages.size()) {
                mPinImages.get(mPinsUsed - 1)
                        .setImageResource(R.drawable.pin_off);
            }
            if (mPinsUsed == NUMBER_OF_PINS) {
                gameOver(true);
                return;
            } else {
                Toast.makeText(MainActivity.this,
                        R.string.missed_that_one, Toast.LENGTH_SHORT).show();
            }
        }
        updateDisplay();
        if (mBalloonsPopped == BALLOONS_PER_LEVEL) {
            finishLevel();
        }
    }

    private void gameOver(boolean allPinsUsed) {

        Toast.makeText(MainActivity.this, R.string.game_over, Toast.LENGTH_LONG).show();
        mSoundHelper.stopMusic();

//      Clean up balloons
        for (Balloon balloon : mBalloons) {
            balloon.setPopped(true);
        }

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                for (Balloon balloon : mBalloons) {
                    mContentView.removeView(balloon);
                }
                mBalloons.clear();
            }
        }, 2000);

//      Reset for a new game
        mPlaying = false;
        mPinsUsed = 0;
        mGoButton.setText(R.string.play_game);
        mNextAction = ACTION_RESTART_GAME;

        if (allPinsUsed) {

//          Manage high score locally
            if (PreferencesHelper.isTopScore(this, mScore)) {
                String message = String.format(getString(R.string.your_top_score_is), mScore);
                PreferencesHelper.setTopScore(this, mScore);
                MyAlertDialog dialog = MyAlertDialog.newInstance(
                        getString(R.string.new_top_score),
                        message);
                dialog.show(getSupportFragmentManager(), null);
            }

            int completedLevel = mLevel - 1;
            if (PreferencesHelper.isMostLevels(this, completedLevel)) {
                PreferencesHelper.setPrefMostLevels(this, completedLevel);
                MyAlertDialog dialog = MyAlertDialog.newInstance(
                        getString(R.string.more_levels_than_ever),
                        String.format(getString(R.string.you_completed_n_levels), completedLevel));
                dialog.show(getSupportFragmentManager(), null);
            }

        }

    }

    private class BalloonLauncher extends AsyncTask<Integer, Integer, Void> {

        @Override
        protected Void doInBackground(Integer... params) {

            if (params.length != 1) {
                throw new AssertionError(
                        "Expected 1 param for current level");
            }

            int level = params[0];

//          level 1 = max delay; each ensuing level reduces delay by 500 ms
//            min delay is 250 ms
            int maxDelay = Math.max(MIN_ANIMATION_DELAY, (MAX_ANIMATION_DELAY - ((level - 1) * 500)));
            int minDelay = maxDelay / 2;

//          Keep on launching balloons until either
//              1) we run out or 2) the mPlaying flag is set to false
            int balloonsLaunched = 0;
            while (mPlaying && balloonsLaunched < BALLOONS_PER_LEVEL) {

//              Get a random horizontal position for the next balloon
                Random random = new Random(new Date().getTime());
                int xPosition = random.nextInt(mScreenWidth - 200);
                publishProgress(xPosition);
                balloonsLaunched++;

//              Wait a random number of milliseconds before looping
                int delay = random.nextInt(minDelay) + minDelay;
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return null;

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
//          This runs on the UI thread, so we can launch a balloon
//            at the randomized horizontal position
            int xPosition = values[0];
            launchBalloon(xPosition);
        }

    }
}

