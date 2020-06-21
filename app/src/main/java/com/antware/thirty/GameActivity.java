package com.antware.thirty;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class GameActivity extends AppCompatActivity {

    public static final String SCORE_MESSAGE = "com.example.geoquiz.SCORE_MESSAGE";
    public static final String COMB_PER_ROUND_MESSAGE = "com.example.geoquiz.COMB_PER_ROUND_MESSAGE";
    public static final String SCORE_PER_ROUND_MESSAGE = "com.example.geoquiz.SCORE_PER_ROUND_MESSAGE";

    private static final int DEBUG_COMB = 8;

    TextView roundsView, scoreView, throwsView;
    Button throwButton, resultButton;
    List<CardView> combViews = new ArrayList<>();
    ImageView[] diceViews = new ImageView[6];

    Game game = new Game();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initGameUI();
    }

    private void initGameUI() {
        game.initGame();
        initElements();

        onThrowButtonPressed();
        updateFigures();
    }

    private void initElements() {
        roundsView = findViewById(R.id.roundTextView);
        scoreView = findViewById(R.id.scoreTextView);
        throwsView = findViewById(R.id.throwTextView);
        throwsView.setText(R.string.throws_left);
        throwButton = findViewById(R.id.throwButton);
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

        initCombinations();
        initDices();
    }

    private void initDices() {
        TableLayout table = findViewById(R.id.diceTable);
        for (int i = 0; i < table.getChildCount(); i++) {
            TableRow row = (TableRow) table.getChildAt(i);
            for (int j = 0; j < row.getChildCount(); j++) {
                CardView cardView = (CardView) row.getChildAt(j);
                ImageView diceView = (ImageView) cardView.getChildAt(0);
                final int index = i * row.getChildCount() + j;
                diceViews[index] = diceView;
                setCardBackground(cardView, R.dimen.nonSelectedCardElev, R.color.transparent);
                cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (game.isDiePicked(index)) {
                            setDiePicked((CardView) view, index, false);
                        }
                        else {
                            setDiePicked((CardView) view, index, true);
                        }
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
                cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (game.isCombPicked()) return;
                        game.setPickedComb(cardNum);
                        setCombinationClicked((CardView) view, true);
                    }
                });
                setCombinationClicked(cardView, false);
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
    }

    private void onThrowButtonPressed() {
        if (game.getThrowsLeft() == 0) {
            for (int i = 0; i < diceViews.length; i++) {
                setDiePicked((CardView) diceViews[i].getParent(), i, false);
            }
        }
        int round = game.getRound();
        game.throwDice();
        setDiceFaces();
        updateFigures();
        if (round < game.getRound()) {
            for (CardView combView : combViews)
                setCombinationClicked(combView, false);
        }
        if (game.getThrowsLeft() == 0){
            if (game.getRound() == game.getMaxRounds())
                onGameOver();
            else throwButton.setText(R.string.next_round);}
        else throwButton.setText(R.string.throw_string);
    }

    private void onGameOver() {
        throwsView.setText(R.string.game_over);
        throwButton.setText(R.string.new_game);
        throwButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initGameUI();
            }
        });
        resultButton.setEnabled(true);
        resultButton.setVisibility(View.VISIBLE);
    }

    private void setDiceFaces() {
        for (int i = 0; i < diceViews.length; i++) {
            int d = 0;
            switch (game.getDieFace(i)) {
                case 1: d = R.drawable.die1; break;
                case 2: d = R.drawable.die2; break;
                case 3: d = R.drawable.die3; break;
                case 4: d = R.drawable.die4; break;
                case 5: d = R.drawable.die5; break;
                case 6: d = R.drawable.die6; break;
            }
            diceViews[i].setImageResource(d);
        }
    }

    private void updateFigures() {
        setNumberInTextView(throwsView, game.getThrowsLeft());
        setNumberInTextView(scoreView, game.getScore());
        setNumberInTextView(roundsView, game.getRound());
        for (int i = 0; i < combViews.size(); i++) {
            TextView text = (TextView) combViews.get(i).getChildAt(0);
            setCombPoints(text, game.isCombPicked() ? game.getCombPoints(i) : -1);
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
}