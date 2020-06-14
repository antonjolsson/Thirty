package com.antware.thirty;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class GameActivity extends AppCompatActivity {

    final static int SELECTED_CARD_ELEV = 2;
    final static int UNSELECTED_CARD_ELEV = 0;

    TextView roundsView, scoreView, throwsView;
    Button throwButton;
    List<CardView> cardViews = new ArrayList<>();


    Game game = new Game();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initElements();
        initUI();
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

        TableLayout table = findViewById(R.id.combTable);
        for (int i = 0; i < table.getChildCount(); i++) {
            TableRow row = (TableRow) table.getChildAt(i);
            for (int j = 0; j < row.getChildCount(); j++) {
                cardViews.add((CardView) row.getChildAt(j));
            }
        }
    }

    private void throwDice() {
        //game.throwDice();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initUI() {
        game.initGame();
        setNumberInTextView(throwsView, game.getThrowsLeft());
        setNumberInTextView(scoreView, game.getScore());
        setNumberInTextView(roundsView, game.getRound());
        for (int i = 0; i < cardViews.size(); i++) {
            Combination comb = game.getCombs()[i];
            TextView text = (TextView) cardViews.get(i).getChildAt(0);
            setCombPoints(text, comb);
            if (comb.isPicked()) {
                text.setTextColor(getResources().getColor(R.color.blackSemiTransparent));
                text.setShadowLayer(0, text.getShadowDx(), text.getShadowDy(),
                        text.getShadowColor());
                cardViews.get(i).setCardElevation(2);
                int bgColorId = getResources().getColor(R.color.colorAccent);
                cardViews.get(i).setCardBackgroundColor(bgColorId);
            }
            else {
                text.setTextColor(getResources().getColor(R.color.colorAccent));
                text.setShadowLayer(5, text.getShadowDx(), text.getShadowDy(),
                        text.getShadowColor());
                cardViews.get(i).setCardElevation(0);
                int bgColorId = getResources().getColor(R.color.transparent);
                cardViews.get(i).setCardBackgroundColor(bgColorId);
            }
        }
    }

    private void setNumberInTextView(TextView textView, int number) {
        String newText = textView.getText().toString().replaceAll("(\\d)+",
                String.valueOf(number));
        textView.setText(newText);
    }

    private void setCombPoints(TextView text, Combination comb) {
        String pointStr = comb.getPoints() + " P";
        String newText = text.getText().toString().replaceAll("(\\d)+\\sP", pointStr);
        text.setText(newText);
    }
}