package com.antware.thirty;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import static com.antware.thirty.GameActivity.COMB_PER_ROUND_MESSAGE;
import static com.antware.thirty.GameActivity.SCORE_MESSAGE;
import static com.antware.thirty.GameActivity.SCORE_PER_ROUND_MESSAGE;

/**
 * Class for displaying score per round.
 * @author Anton J Olsson
 */
public class ScoreActivity extends MusicPlayingActivity {

    private final static int SCORE_VIEW_INDEX = 2; // Index of score View in TableLayout
    private static final int COMB_VIEW_INDEX = 3; // Index of chosen combination  View in TableLayout
    private static final int POINTS_ROW_START_INDEX = 2; // Starting row in TableLayout for displaying points

    /**
     * Creates the UI from Intent created in GameActivity.
     * @param savedInstanceState the saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        TextView scoreView = findViewById(R.id.totalScoreView);

        int totalScore = getIntent().getIntExtra(SCORE_MESSAGE, 0);
        int[] scorePerRound = getIntent().getIntArrayExtra(SCORE_PER_ROUND_MESSAGE);
        int[] combPerRound = getIntent().getIntArrayExtra(COMB_PER_ROUND_MESSAGE);

        // If activity was just started from GameActivity, decide from Intent whether to play music
        if (savedInstanceState == null)
            playMusic = getIntent().getBooleanExtra(KEY_PLAY_MUSIC, false);
        initMusicControlView();

        scoreView.setText(String.valueOf(totalScore));
        TableLayout scoreTable = findViewById(R.id.scoreTable);
        assert scorePerRound != null && combPerRound != null;
        for (int i = 0; i < scorePerRound.length; i++) {
            TableRow row = (TableRow) scoreTable.getChildAt(i + POINTS_ROW_START_INDEX);
            TextView score = (TextView) row.getChildAt(SCORE_VIEW_INDEX);
            TextView comb = (TextView) row.getChildAt(COMB_VIEW_INDEX);
            score.setText(String.valueOf(scorePerRound[i]));
            comb.setText(getCombName(combPerRound[i]));
        }

        bindMusicService();
    }

    /**
     * Notices GameActivity if music playback status was changed in this activity
     */
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(KEY_PLAY_MUSIC, playMusic);
        setResult(MESSAGE_PLAY_MUSIC, intent);
        super.onBackPressed();
    }

    /**
     * Gets a combination name as a String from its integer name.
     * @param combAsInt the integer name.
     * @return the name as a String
     */
    private String getCombName(int combAsInt) {
        switch (combAsInt) {
            case 0:
                return "NONE";
            case 1:
                return "LOW";
            default:
                return String.valueOf(combAsInt);
        }
    }

}