package com.antware.thirty;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

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

    TextView roundsView, scoreView, throwsView;
    Button throwButton;
    List<CardView> combViews = new ArrayList<>();
    ImageView[] diceViews = new ImageView[6];

    Game game = new Game();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initElements();
        game.initGame();
        updateFigures();
    }

    private void initElements() {
        roundsView = findViewById(R.id.roundTextView);
        scoreView = findViewById(R.id.scoreTextView);
        throwsView = findViewById(R.id.throwTextView);
        throwButton = findViewById(R.id.throwButton);
        throwButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                throwDice();
            }
        });

        initCombinations();

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

    private void initCombinations() {
        TableLayout table = findViewById(R.id.combTable);
        for (int i = 0; i < table.getChildCount(); i++) {
            final TableRow row = (TableRow) table.getChildAt(i);
            for (int j = 0; j < row.getChildCount(); j++) {
                CardView cardView = (CardView) row.getChildAt(j);
                combViews.add(cardView);
                final int finalI = i;
                final int finalJ = j;
                cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        cardViewClicked((CardView) view, finalI * row.getChildCount() + finalJ);
                    }
                });
                setCardViewNotClicked(cardView);
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
        int bgColorId = getResources().getColor(color);
        cardView.setCardBackgroundColor(bgColorId);
    }

    private void setCardViewNotClicked(CardView cardView) {
        TextView text = (TextView) cardView.getChildAt(0);
        text.setTextColor(getResources().getColor(R.color.colorAccent));
        text.setShadowLayer(5, text.getShadowDx(), text.getShadowDy(),
                text.getShadowColor());
        setCardBackground(cardView, R.dimen.nonSelectedCardElev, R.color.transparent);
    }

    private void cardViewClicked(CardView cardView, int cardNum) {
        if (game.isCombPicked()) return;
        game.setPickedComb(cardNum);
        TextView text = (TextView) cardView.getChildAt(0);
        text.setTextColor(getResources().getColor(R.color.blackSemiTransparent));
        text.setShadowLayer(0, text.getShadowDx(), text.getShadowDy(),
                text.getShadowColor());
        setCardBackground(cardView, R.dimen.selectedCardElev, R.color.colorAccent);
        updateFigures();
    }

    private void throwDice() {
        int round = game.getRound();
        game.throwDice();
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
        updateFigures();
        if (round < game.getRound()) {
            for (CardView cardView : combViews)
                setCardViewNotClicked(cardView);
            for (int i = 0; i < diceViews.length; i++) {
                ImageView diceView = diceViews[i];
                setDiePicked((CardView) diceView.getParent(), i, false);
            }
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