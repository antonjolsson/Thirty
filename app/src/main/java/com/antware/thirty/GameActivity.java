package com.antware.thirty;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

// Class for main game UI

/**
 * Class/Activity for the main/game screen.
 */
public class GameActivity extends MusicPlayingActivity {

    public static final String SCORE_MESSAGE = "com.example.geoquiz.SCORE_MESSAGE";
    public static final String COMB_PER_ROUND_MESSAGE = "com.example.geoquiz.COMB_PER_ROUND_MESSAGE";
    public static final String SCORE_PER_ROUND_MESSAGE = "com.example.geoquiz.SCORE_PER_ROUND_MESSAGE";

    private static final String KEY_GAME = "game";

    private static final int DICE_ROLL_SOUND_DUR = 600;
    private static final int DICE_ANIMATION_FRAME_DUR = 100;
    private static final int SCORE_ANIM_FRAME_DUR = 50;
    private static final int COMB_PICKED_SOUND_DUR = 600;
    private static final float INCREASE_POINT_VOLUME = 0.15f;

    TextView roundsView, scoreView, rollsView, pickCombView;
    Button rollButton, resultButton;
    List<CardView> combViews = new ArrayList<>(); // Views for the combinations
    ImageView[] diceViews = new ImageView[6];

    Game game = new Game();
    private int diceRollSound, selectDieSound, combPickSound, increasePointsSound;
    private SoundPool soundPool;

    /**
     * Creates the Activity and inflates the layout. If savedInstanceState is not null the game is
     * being restored, so the state of the UI elements is restored as well. Else, the elements are
     * initialized. Also initializes the music logic.
     * @param savedInstanceState the saved instance state, if game is being re-created. Else, the value is null
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null){
            resumeGame(savedInstanceState);
        }
        else initGameActivity();

        bindMusicService();
    }

    /**
     * Logic for resuming the game. Must re-check if there are no rolls left, and in that case
     * execute the logic for that state
     * @param savedInstanceState the saved instance state, might be null
     */
    private void resumeGame(Bundle savedInstanceState) {
        game = savedInstanceState.getParcelable(KEY_GAME);
        initElements(false);
        setDieFaces();
        updateFigures(true);
        if (game.getRollsLeft() == 0)
            onNoRollsLeft(false);
    }

    /**
     * Overridden method to save the game state (excluding the UI) as a <code>Parcelable.</code>
     * @param outState the saved instance state, with the game state added to it
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_GAME, game);
    }

    /**
     * Method run only when the application starts or the player decides to play again, on game over.
     */
    private void initGameActivity() {
        game.initGame();
        initElements(true);
        onRollButtonPressed();
    }

    /**
     * Initializes all audio-visual elements on this screen.
     * @param rollDice whether the dice-roll sound should be instantly played
     */
    private void initElements(boolean rollDice) {
        loadSounds(rollDice);

        roundsView = findViewById(R.id.roundTextView);
        scoreView = findViewById(R.id.scoreTextView);
        setNumberInTextView(scoreView, 0);
        rollsView = findViewById(R.id.rollTextView);
        rollsView.setText(R.string.rolls_left);
        pickCombView = findViewById(R.id.pickCombView);
        rollButton = findViewById(R.id.rollButton);
        rollButton.setSoundEffectsEnabled(false);
        rollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRollButtonPressed();
            }
        });
        resultButton = findViewById(R.id.resultButton);
        resultButton.setVisibility(View.GONE);
        resultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDetailedScore();
            }
        });

        initMusicControlView();

        initCombinations();
        initDice();
    }

    /**
     * Loads all sound effects (not including the music) used in the game, in a <code>SoundPool.</code>
     * @param rollDice whether the dice-roll sound should be instantly played
     */
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

    /**
     * Initializes the views and sounds associated with the dice.
     */
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

    /**
     * Starts the result screen Activity and creates an Intent with all necessary information.
     */
    private void showDetailedScore() {
        Intent intent = new Intent(this, ScoreActivity.class);
        intent.putExtra(SCORE_MESSAGE, game.getScore());
        intent.putExtra(SCORE_PER_ROUND_MESSAGE, game.getScorePerRound());
        intent.putExtra(COMB_PER_ROUND_MESSAGE, game.getCombPerRound());
        intent.putExtra(KEY_PLAY_MUSIC, playMusic);
        // Notify this activity if the music status was changed in ScoreActivity
        startActivityForResult(intent, MESSAGE_PLAY_MUSIC);
    }

    /**
     * Method executed when returning from ScoreActivity, to decide whether to play music or not.
     * @param requestCode the request code for the result; MESSAGE_PLAY_MUSIC
     * @param resultCode the resultCode
     * @param data the music playback state in ResultActivity just before returning
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MESSAGE_PLAY_MUSIC) {
            assert data != null;
            // If music status was changed in ScoreActivity, change it in this activity accordingly
            if (playMusic != data.getBooleanExtra(KEY_PLAY_MUSIC, playMusic))
                toggleMusic();
        }
    }

    /**
     * Initializes the graphics associated with the combinations.
     */
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
                        // Only one combination can be picked per round and choice cannot be undone
                        if (game.isAnyCombPickedThisRound() || game.isCombPicked(cardNum)) return;
                        game.setCombPicked(cardNum);
                        setCombinationClicked((CardView) view, true);
                    }
                });
                setCombinationClicked(cardView, game.isCombPicked(cardNum));
            }
        }
    }

    /**
     * Marks the die graphic as picked or not.
     * @param view The CardView parent of the ImageView containing the die image.
     * @param index the index of the die in the Game object dice array
     * @param isPicked whether the die shall be marked or not
     */
    private void setDiePicked(CardView view, int index, boolean isPicked) {
        int elevation = isPicked ? R.dimen.selectedCardElev : R.dimen.nonSelectedCardElev;
        int color = isPicked ? R.color.colorAccent : R.color.transparent;
        game.setDiePicked(index, isPicked);
        setCardBackground(view, elevation, color);
    }

    /**
     * Highlight or un-highlights a die or combination.
     * @param cardView The CardView containing the graphic
     * @param elevation The new elevation of the CardView
     * @param color The new color of the CardView (may be transparent)
     */
    private void setCardBackground(CardView cardView, int elevation, int color) {
        cardView.setCardElevation(elevation);
        int bgColorId = getResources().getColor(color, null);
        cardView.setCardBackgroundColor(bgColorId);
    }

    /**
     * OnClickListener for combinations. Marks a combination as picked or not and updates figures afterwards.
     * Removes the "Pick combination!" reminder and enables rollButton and resultButton, if applicable.
     * @param cardView the CardView containing the combination
     * @param isClicked whether the die should be clicked or not
     */
    private void setCombinationClicked(CardView cardView, boolean isClicked) {
        TextView text = (TextView) cardView.getChildAt(0);
        text.setTextColor(getResources().getColor(isClicked ? R.color.blackSemiTransparent : R.color.colorAccent));
        text.setShadowLayer(isClicked ? 0 : 5, text.getShadowDx(), text.getShadowDy(),
                text.getShadowColor());
        int elevation = isClicked ? R.dimen.selectedCardElev : R.dimen.nonSelectedCardElev;
        int color = isClicked ? R.color.colorAccent : R.color.transparent;
        setCardBackground(cardView, elevation, color);
        updateFigures(false);

        if (isClicked) {
            if (game.getRollsLeft() == 0) {
                forceCombChoice(false);
                pickCombView.setVisibility(View.GONE);
                Handler handler = new Handler();
                // Delay score-increase animation until picked-combination sound has been played
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

    /**
     * Method to enable or disable rollButton and possible resultButton, depending on whether the
     * player has picked a combination this round yet and the number of rolls left.
     * @param forceChoice whether to enable or disable the buttons
     */
    private void forceCombChoice(boolean forceChoice) {
        setButtonEnabled(rollButton, !forceChoice);
        if (resultButton.getVisibility() == View.VISIBLE) {
            setButtonEnabled(resultButton, !forceChoice);
        }
    }

    /**
     * Enables/disables (own implementation) rollButton or resultButton and possibly displays the "Pick combination!"
     * message.
     * @param button the button of which to alter appearance and functionality
     * @param enable whether to set the button in enabled or disabled state
     */
    private void setButtonEnabled(final Button button, final boolean enable) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (enable) {
                    if (button.getId() == R.id.resultButton) {
                        showDetailedScore();
                    } else {
                        if (gameOver()) initGameActivity();
                        else onRollButtonPressed();
                    }
                }
                else pickCombView.setVisibility(View.VISIBLE);
            }
        });
        int bgColor = enable ? R.color.colorAccent : R.color.colorAccentSemiTransp;
        button.setBackgroundColor(ContextCompat.getColor(this, bgColor));
        int textColor = enable ? R.color.blackSemiTransparent : R.color.blackAlmostTransparent;
        button.setTextColor(ContextCompat.getColor(this, textColor));
    }

    /**
     * OnClickListener for rollButton. Could be considered "main" method of the application, along with
     * setCombinationClicked.
     */
    private void onRollButtonPressed() {
        if (game.getRollsLeft() == 0) {
            for (int i = 0; i < diceViews.length; i++) {
                setDiePicked((CardView) diceViews[i].getParent(), i, false);
            }
        }
        soundPool.play(diceRollSound, 1, 1, 0, 0, 1);
        animateDice();
        // Check if round number is the same after the dice roll
        int round = game.getRound();
        game.rollDice();
        updateFigures(false);
        if (newRound(round)) {
            resetCombPoints();
        }
        if (game.getRollsLeft() == 0)
            onNoRollsLeft(true);
        else rollButton.setText(R.string.roll_string);
    }

    /**
     * Returns whether the game has entered a new round.
     * @param oldRound the round number before game logic for a dice roll was executed
     * @return whether the game has entered a new round
     */
    private boolean newRound(int oldRound) {
        return oldRound < game.getRound();
    }

    /**
     * Hides all combinations' points, except for those picked.
     */
    private void resetCombPoints() {
        for (int i = 0; i < combViews.size(); i++) {
            if (game.isCombPicked(i)) continue;
            CardView combView = combViews.get(i);
            setCombPoints((TextView) combView.getChildAt(0), -1);
        }
    }

    /**
     * Animates random die faces in each die ImageView, except for those picked, while the
     * diceRollSound is played.
     */
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

    /**
     * Logic for handling the state of no dice rolls left.
     * @param animateScore
     */
    private void onNoRollsLeft(boolean animateScore) {
        int round = game.getRound();
        if (gameOver()) onGameOver();
        else rollButton.setText(R.string.next_round);
        if (game.getScorePerRound()[round - 1] > 0){
            if (animateScore) animateScoreIncrease();
            else setNumberInTextView(scoreView, game.getScore());
        }
        if (!game.isAnyCombPickedThisRound())
            forceCombChoice(true);
    }

    private boolean gameOver() {
        return game.getRollsLeft() == 0 && game.getRound() == game.getMaxRounds();
    }

    private void onGameOver() {
        rollsView.setText(R.string.game_over);
        rollButton.setText(R.string.new_game);
        rollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initGameActivity();
            }
        });
        setButtonEnabled(resultButton, game.isAnyCombPickedThisRound());
        resultButton.setVisibility(View.VISIBLE);
    }

    private void setDieFaces() {
        for (int i = 0; i < diceViews.length; i++) {
            int d = getDieFaceView(game.getDieFace(i));
            diceViews[i].setImageResource(d);
        }
    }

    private int getDieFaceView(int dieFace) {
        return getResources().getIdentifier("die" + dieFace, "drawable", getPackageName());
    }

    // Update all values in views (except for dice)
    private void updateFigures(boolean updateScore) {
        setNumberInTextView(rollsView, game.getRollsLeft());
        setNumberInTextView(roundsView, game.getRound());
        if (updateScore) setNumberInTextView(scoreView, game.getScore());
        for (int i = 0; i < combViews.size(); i++) {
            TextView text = (TextView) combViews.get(i).getChildAt(0);
            int points = game.isCombPicked(i) || game.isAnyCombPickedThisRound() ?
                    game.getCombPoints(i) : -1;
            setCombPoints(text, points);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        musicService.pauseMusic();
        musicService.setPlaybackPos(0);
    }
}