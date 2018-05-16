package com.diapearl.lint.geoquiz;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;

public class QuizActivity extends AppCompatActivity {
    private static final String TAG = "QuizActivity";
    private static final String KEY_INDEX = "index";
    private static final String KEY_ISCHEATER = "ischeater";
    private static final int REQUEST_CODE_CHEAT = 0;

    private Button mTrueButton;
    private Button mFalseButton;
    private ImageButton mNextButton;
    private ImageButton mPrevButton;

    private Button mCheatButton;

    private TextView mQuestionTextView;

    private Question[] mQuestionsBank = new Question[] {
            new Question(R.string.question_australia, true),
            new Question(R.string.question_oceans, true),
            new Question(R.string.question_mideast, false),
            new Question(R.string.question_africa, false),
            new Question(R.string.question_americas, true),
            new Question(R.string.question_asia, true),
    };

    private int mCurrentIndex = 0;
    private boolean mIsCheater;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_CODE_CHEAT) {
            if (data == null) {
                return ;
            }

            mIsCheater = CheatActivity.wasAnswerShown(data);

            if (mIsCheater) {
                mQuestionsBank[mCurrentIndex].setCheated(true);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(TAG, "onSaveInstanceState");
        outState.putInt(KEY_INDEX, mCurrentIndex);
        outState.putBoolean(KEY_ISCHEATER, mIsCheater);
    }

    // lint: not work in save state, should use onSaveInstanceState(Bundle outState)
    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        Log.d(TAG, "onSaveInstanceState");
        outState.putInt(KEY_INDEX, mCurrentIndex);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate(Bundle) called");

        setContentView(R.layout.activity_quiz);

        if (savedInstanceState != null) {
            mCurrentIndex = savedInstanceState.getInt(KEY_INDEX, 0);
            mIsCheater = savedInstanceState.getBoolean(KEY_ISCHEATER, false);
        }

        mQuestionTextView = findViewById(R.id.question_text_view);
        updateQuestion();

        mQuestionTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextQuestion();
            }
        });

        mTrueButton = findViewById(R.id.true_button);
        mTrueButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mQuestionsBank[mCurrentIndex].isAnswered()) {
                    Toast.makeText(QuizActivity.this, R.string.answered_toast, Toast.LENGTH_SHORT).show();
                } else {
                    checkAnswer(true);
                }
            }
        });

        mFalseButton = findViewById(R.id.false_button);
        mFalseButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mQuestionsBank[mCurrentIndex].isAnswered()) {
                    Toast.makeText(QuizActivity.this, R.string.answered_toast, Toast.LENGTH_SHORT).show();
                } else {
                    checkAnswer(false);
                }
            }
        });

        mCheatButton= findViewById(R.id.cheat_button);
        mCheatButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                boolean answerIsTrue = mQuestionsBank[mCurrentIndex].isAnswerTrue();
                Intent intent = CheatActivity.newIntent(QuizActivity.this, answerIsTrue);
                startActivityForResult(intent, REQUEST_CODE_CHEAT);
            }
        });

        mNextButton = findViewById(R.id.next_button);
        mNextButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                nextQuestion();
            }
        });

        mPrevButton= findViewById(R.id.previous_button);
        mPrevButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                prevQuestion();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onResume() called");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
    }

    private void updateQuestion() {
        int question = mQuestionsBank[mCurrentIndex].getTextResId();
        mQuestionTextView.setText(question);
    }

    private void nextQuestion() {
        mCurrentIndex = (mCurrentIndex + 1) % mQuestionsBank.length;
        mIsCheater = false;
        updateQuestion();
    }

    private void prevQuestion() {
        if (mCurrentIndex == 0) {
            mCurrentIndex = mQuestionsBank.length - 1;
        } else {
            mCurrentIndex = (mCurrentIndex - 1) % mQuestionsBank.length;
        }

        mIsCheater = false;
        updateQuestion();
    }

    private void checkAnswer(boolean userPressedTrue) {
        boolean answerIsTrue = mQuestionsBank[mCurrentIndex].isAnswerTrue();

        int messageResId = 0;

        boolean questionIsCheated = mQuestionsBank[mCurrentIndex].isCheated();

        if (mIsCheater | questionIsCheated) {
            messageResId = R.string.judgment_toast;
        } else {
            if (userPressedTrue == answerIsTrue) {
                messageResId = R.string.correct_toast;
                mQuestionsBank[mCurrentIndex].setAnswerCorrect(true);
            } else {
                messageResId = R.string.incorrect_toast;
                mQuestionsBank[mCurrentIndex].setAnswerCorrect(false);
            }
        }

        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show();

        mQuestionsBank[mCurrentIndex].setAnswered(true);

        getScore();
    }

    private void getScore() {
        int numOfCorrect = 0;
        int numOfAnswered = 0;


        for (int i = 0; i < mQuestionsBank.length; i++) {
            if (mQuestionsBank[i].isAnswered()) {
                numOfAnswered += 1;
                if (mQuestionsBank[i].isAnswerCorrect()) {
                    numOfCorrect += 1;
                }
            }
        }

        NumberFormat nf = NumberFormat.getPercentInstance();
        double correctPercent = (double)numOfCorrect/numOfAnswered;

        if (numOfAnswered == mQuestionsBank.length) {
            Log.d(TAG, "Answered: " + numOfAnswered + "Correct: " + numOfCorrect + "Percent: " + correctPercent);
            Toast toast= Toast.makeText(this, "Score by Percent: "+nf.format(correctPercent),
                        Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 0, 0);
            toast.show();
        }
    }
}
