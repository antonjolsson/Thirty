package com.antware.thirty;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class GameActivity extends AppCompatActivity {

    public static final String SCORE_MESSAGE = "com.example.geoquiz.SCORE_MESSAGE";
    public static final String COMB_PER_ROUND_MESSAGE = "com.example.geoquiz.COMB_PER_ROUND_MESSAGE";
    public static final String SCORE_PER_ROUND_MESSAGE = "com.example.geoquiz.SCORE_PER_ROUND_MESSAGE";

    private static final String KEY_GAME = "game";
    private static final String KEY_PLAY_MUSIC = "playMusic";

    private static final int DICE_ROLL_SOUND_DUR = 600;
    private static final int DICE_ANIMATION_FRAME_DUR = 100;
    private static final int SCORE_ANIM_FRAME_DUR = 50;
    private static final int COMB_PICKED_SOUND_DUR = 600;
    private static final float INCREASE_POINT_VOLUME = 0.15f;
    private static final float LARGE_TEXT_SIZE = 24;
    private static final float SMALL_TEXT_SIZE = 18;

    TextView roundsView, scoreView, throwsView, musicControlView;
    Button throwButton, resultButton;
    List<CardView> combViews = new ArrayList<>();
    ImageView[] diceViews = new ImageView[6];

    Game game = new Game();
    private int diceRollSound, selectDieSound, combPickSound, increasePointsSound;
    private SoundPool soundPool;

    Intent musicIntent;
    boolean playMusic = true;
    boolean serviceBound = false;
    MusicService musicService;
    private ServiceConnection serviceConnection;
    private boolean launchingScoreActivity;

    //private boolean orientationChanged;
    private int lastOrientation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null){
            resumeGame(savedInstanceState);
        }
        else initGameActivity();

        lastOrientation = getResources().getConfiguration().orientation;
    }

    private void resumeGame(Bundle savedInstanceState) {
        game = savedInstanceState.getParcelable(KEY_GAME);
        playMusic = savedInstanceState.getBoolean(KEY_PLAY_MUSIC);
        initServiceConnection();
        bindMusicService();
        //initMusic();
        initElements(false);
        setDieFaces();
        updateFigures();
        if (game.getThrowsLeft() == 0)
            onNoThrowsLeft(false);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_GAME, game);
        outState.putBoolean(KEY_PLAY_MUSIC, playMusic);
    }

    private void initGameActivity() {
        game.initGame();
        initMusic();
        initElements(true);
        onThrowButtonPressed();
    }

    private void initMusic() {
        initServiceConnection();
        bindMusicService();
        musicIntent = new Intent();
        musicIntent.setClass(this, MusicService.class);
        if (playMusic)
            startService(musicIntent);
    }

    private void initElements(boolean rollDice) {
        loadSounds(rollDice);

        roundsView = findViewById(R.id.roundTextView);
        scoreView = findViewById(R.id.scoreTextView);
        setNumberInTextView(scoreView, 0);
        throwsView = findViewById(R.id.throwTextView);
        throwsView.setText(R.string.throws_left);
        throwButton = findViewById(R.id.throwButton);
        throwButton.setSoundEffectsEnabled(false);
        throwButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onThrowButtonPressed();
            }
        });
        resultButton = findViewById(R.id.resultButton);
        resultButton.setVisibility(View.GONE);
        resultButton.setEnabled(false);
        resultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDetailedScore();
            }
        });
        musicControlView = findViewById(R.id.playView);
        musicControlView.setText(playMusic ? R.string.pause : R.string.play);
        musicControlView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleMusic();
            }
        });

        initCombinations();
        initDice();
    }

    private void toggleMusic() {
        if (playMusic) {
            musicService.pauseMusic();
            musicControlView.setText(R.string.play);
            musicControlView.setTextSize(LARGE_TEXT_SIZE);
            playMusic = false;
        }
        else {
            musicService.resumeMusic();
            musicControlView.setText(R.string.pause);
            musicControlView.setTextSize(SMALL_TEXT_SIZE);
            playMusic = true;
        }
    }

    private void loadSounds(final boolean rollDice) {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(audioAttributes)
                .build();
        diceRollSound = soundPool.load(this, R.raw.dice_roll_board_game_amp, 1);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                if (sampleId == diceRollSound && rollDice)
                    soundPool.play(diceRollSound, 1.0f, 1.0f, 0, 0, 1.0f);
            }
        });
        selectDieSound = soundPool.load(this, R.raw.browse_menu, 0);
        combPickSound = soundPool.load(this, R.raw.lock, 1);
        increasePointsSound = soundPool.load(this, R.raw.point, 0);

    }

    private void initDice() {
        TableLayout table = findViewById(R.id.diceTable);
        for (int i = 0; i < table.getChildCount(); i++) {
            TableRow row = (TableRow) table.getChildAt(i);
            for (int j = 0; j < row.getChildCount(); j++) {
                CardView cardView = (CardView) row.getChildAt(j);
                cardView.setSoundEffectsEnabled(false);
                ImageView diceView = (ImageView) cardView.getChildAt(0);
                final int index = i * row.getChildCount() + j;
                diceViews[index] = diceView;
                setDiePicked(cardView, index, game.isDiePicked(index));
                cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        soundPool.play(selectDieSound, 1, 1, 0, 0, 1);
                        setDiePicked((CardView) view, index, !game.isDiePicked(index));
                    }
                });
            }
        }
    }

    private void showDetailedScore() {
        Intent intent = new Intent(this, ScoreActivity.class);
        intent.putExtra(SCORE_MESSAGE, game.getScore());
        intent.putExtra(SCORE_PER_ROUND_MESSAGE, game.getScorePerRound());
        intent.putExtra(COMB_PER_ROUND_MESSAGE, game.getCombPerRound());
        launchingScoreActivity = true;
        startActivity(intent);
    }

    private void initCombinations() {
        combViews.clear();
        TableLayout table = findViewById(R.id.combTable);
        for (int i = 0; i < table.getChildCount(); i++) {
            final TableRow row = (TableRow) table.getChildAt(i);
            for (int j = 0; j < row.getChildCount(); j++) {
                CardView cardView = (CardView) row.getChildAt(j);
                combViews.add(cardView);
                final int cardNum = i * row.getChildCount() + j;
                cardView.setSoundEffectsEnabled(false);
                cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (game.isAnyCombPicked()) return;
                        game.setPickedComb(cardNum);
                        setCombinationClicked((CardView) view, true);
                    }
                });
                setCombinationClicked(cardView, game.isCombPicked(cardNum));
            }
        }
    }

    private void setDiePicked(CardView view, int index, boolean isPicked) {
        int elevation = isPicked ? R.dimen.selectedCardElev : R.dimen.nonSelectedCardElev;
        int color = isPicked ? R.color.colorAccent : R.color.transparent;
        game.setDiePicked(index, isPicked);
        setCardBackground(view, elevation, color);
    }

    private void setCardBackground(CardView cardView, int elevation, int color) {
        cardView.setCardElevation(elevation);
        int bgColorId = getResources().getColor(color, null);
        cardView.setCardBackgroundColor(bgColorId);
    }

    private void setCombinationClicked(CardView cardView, boolean isClicked) {

        TextView text = (TextView) cardView.getChildAt(0);
        text.setTextColor(getResources().getColor(isClicked ? R.color.blackSemiTransparent : R.color.colorAccent));
        text.setShadowLayer(isClicked ? 0 : 5, text.getShadowDx(), text.getShadowDy(),
                text.getShadowColor());
        int elevation = isClicked ? R.dimen.selectedCardElev : R.dimen.nonSelectedCardElev;
        int color = isClicked ? R.color.colorAccent : R.color.transparent;
        setCardBackground(cardView, elevation, color);
        updateFigures();

        if (isClicked) {
            if (game.getThrowsLeft() == 0) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        animateScoreIncrease();
                    }
                }, COMB_PICKED_SOUND_DUR);
            }
            soundPool.play(combPickSound, 1, 1, 0, 0, 1);
        }
    }

    private void onThrowButtonPressed() {
        soundPool.play(diceRollSound, 1, 1, 0, 0, 1);
        animateDice();
        if (game.getThrowsLeft() == 0) {
            for (int i = 0; i < diceViews.length; i++) {
                setDiePicked((CardView) diceViews[i].getParent(), i, false);
            }
        }
        int round = game.getRound();
        game.throwDice();
        updateFigures();
        if (round < game.getRound()) {
            for (CardView combView : combViews)
                setCombinationClicked(combView, false);
        }
        if (game.getThrowsLeft() == 0)
            onNoThrowsLeft(true);
        else throwButton.setText(R.string.throw_string);
    }

    private void animateDice() {
        for (int i = 0; i < diceViews.length; i++) {
            if (game.isDiePicked(i)) continue;
            diceViews[i].setImageResource(R.drawable.dice_animation);
            Random random = new Random();
            final AnimationDrawable animation = (AnimationDrawable) diceViews[i].getDrawable();
            for (int j = 0; j < DICE_ROLL_SOUND_DUR / DICE_ANIMATION_FRAME_DUR; j++) {
                int dieFace = random.nextInt(6) + 1;
                animation.addFrame(Objects.requireNonNull(getDrawable(getDieFaceView(dieFace))), DICE_ANIMATION_FRAME_DUR);
            }
            Handler animationHandler = new Handler();
            animationHandler.post(new Runnable() {
                @Override
                public void run() {
                    animation.start();
                }
            });
            animationHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    animation.stop();
                    setDieFaces();
                }
            }, DICE_ROLL_SOUND_DUR);
        }
    }

    private void onNoThrowsLeft(boolean animateScore) {
        if (game.getRound() == game.getMaxRounds())
            onGameOver();
        else throwButton.setText(R.string.next_round);
        if (game.isAnyCombPicked()){
            if (animateScore) animateScoreIncrease();
            else setNumberInTextView(scoreView, game.getScore());
        }
    }

    private void onGameOver() {
        throwsView.setText(R.string.game_over);
        throwButton.setText(R.string.new_game);
        throwButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initGameActivity();
            }
        });
        resultButton.setEnabled(true);
        resultButton.setVisibility(View.VISIBLE);
    }

    private void setDieFaces() {
        for (int i = 0; i < diceViews.length; i++) {
            int d = getDieFaceView(game.getDieFace(i));
            diceViews[i].setImageResource(d);
        }
    }

    private int getDieFaceView(int dieFace) {
        return getResources().getIdentifier("die" + dieFace + "grad", "drawable", getPackageName());
    }

    private void updateFigures() {
        setNumberInTextView(throwsView, game.getThrowsLeft());
        setNumberInTextView(roundsView, game.getRound());
        for (int i = 0; i < combViews.size(); i++) {
            TextView text = (TextView) combViews.get(i).getChildAt(0);
            setCombPoints(text, game.isAnyCombPicked() ? game.getCombPoints(i) : -1);
        }
    }

    private void animateScoreIncrease() {
        final int oldScore = Integer.parseInt(scoreView.getText().toString().
                replaceAll("[^\\d]", ""));

        for (int i = 1; i <= game.getScore() - oldScore; i++) {
            Handler animationHandler = new Handler();
            final int finalI = i;
            animationHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setNumberInTextView(scoreView, oldScore + finalI);
                    soundPool.play(increasePointsSound, INCREASE_POINT_VOLUME,
                            INCREASE_POINT_VOLUME, finalI, 0, 1);
                    Log.d("GameActivity", scoreView.getText().toString());
                }
            }, SCORE_ANIM_FRAME_DUR * i);
        }
    }

    private void setNumberInTextView(TextView textView, int number) {
        String newText = textView.getText().toString().replaceAll("(\\d)+",
                String.valueOf(number));
        textView.setText(newText);
    }

    private void setCombPoints(TextView text, int points) {
        String pointStr = "\n" + ((points >= 0) ? points + " P" : "");
        String newText = text.getText().toString().replaceFirst("\\n.*", pointStr);
        text.setText(newText);
    }

    private void initServiceConnection() {
        serviceConnection = new ServiceConnection(){

            public void onServiceConnected(ComponentName name, IBinder binder) {
                musicService = ((MusicService.ServiceBinder) binder).getService();
            }

            public void onServiceDisconnected(ComponentName name) {
                musicService = null;
            }
        };
    }

    void bindMusicService(){
        bindService(new Intent(this, MusicService.class), serviceConnection,
                Context.BIND_AUTO_CREATE);
        serviceBound = true;
    }

    void unbindMusicService()
    {
        if(serviceBound)
        {
            unbindService(serviceConnection);
            serviceBound = false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        /*if (!launchingScoreActivity && getResources().getConfiguration().orientation == lastOrientation)
            musicService.pauseMusic();*/
    }
}