package com.antware.thirty;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import static com.antware.thirty.GameActivity.COMB_PER_ROUND_MESSAGE;
import static com.antware.thirty.GameActivity.SCORE_MESSAGE;
import static com.antware.thirty.GameActivity.SCORE_PER_ROUND_MESSAGE;

public class ScoreActivity extends AppCompatActivity {

    private final static int SCORE_VIEW_INDEX = 2;
    private static final int COMB_VIEW_INDEX = 3;
    private static final int POINTS_ROW_START_INDEX = 2;

    TextView scoreView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        scoreView = findViewById(R.id.totalScoreView);

        int totalScore = getIntent().getIntExtra(SCORE_MESSAGE, 0);
        int[] scorePerRound = getIntent().getIntArrayExtra(SCORE_PER_ROUND_MESSAGE);
        int[] combPerRound = getIntent().getIntArrayExtra(COMB_PER_ROUND_MESSAGE);

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

    }

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