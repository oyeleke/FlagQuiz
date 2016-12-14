package com.example.ti.flag_quiz;
import android.view.View.OnClickListener;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    private static final String TAG = "FlagQuiz Activity";

    private  static final  int FLAGS_IN_QUIZ = 10;

    private List<String> fileNameList;
    private List<String> quizCountriesList;
    private Set<String> regionsSet;
    private  String correctAnswer;
    private int totalGuesses;
    private int correctAnswers;
    private int guessRows;
    private SecureRandom random;
    private Handler handler;
    private Animation shakeAnimation;

    private LinearLayout quizLinearLayout;
    private TextView questionNumberTextView;
    private ImageView flagImageView;
    private LinearLayout[] guessLinearLayout;
    private TextView answerTextView;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        fileNameList = new ArrayList<>();
        quizCountriesList = new ArrayList<>();
        random = new SecureRandom();
        handler = new Handler();

        // loading animation used for in correct answers
        shakeAnimation = AnimationUtils.loadAnimation(getActivity(),R.anim.incorrect_shake);
        shakeAnimation.setRepeatCount(3);

        quizLinearLayout = (LinearLayout)view.findViewById(R.id.quizLinearLayout);
        questionNumberTextView = (TextView)view.findViewById(R.id.questionNumberTextView);

        flagImageView = (ImageView)view.findViewById(R.id.imageView);
        guessLinearLayout = new LinearLayout[4];

        guessLinearLayout[0]= (LinearLayout)view.findViewById(R.id.row1LinearLayout);
        guessLinearLayout[1]= (LinearLayout)view.findViewById(R.id.row2LinearLayout);
        guessLinearLayout[2]= (LinearLayout)view.findViewById(R.id.row3LinearLayout);
        guessLinearLayout[3]= (LinearLayout)view.findViewById(R.id.row4LinearLayout);

        answerTextView = (TextView)view.findViewById(R.id.textView);

        for(LinearLayout row : guessLinearLayout){
            for (int column = 0 ; column<row.getChildCount(); column++){
                Button button = (Button)row.getChildAt(column);
                button.setOnClickListener(guessButtonListner);
            }
        }
        questionNumberTextView.setText(
                getString(R.string.question,1,FLAGS_IN_QUIZ)
        );

        return view;// return fragments view for display

    }
    public void updateGuessRows(SharedPreferences sharedPreferences) {
        // get the number of guess buttons that should be displayed
        try{
        String choices =
                sharedPreferences.getString(MainActivity.CHOICES, null);
        guessRows = Integer.parseInt(choices) / 2;}

        catch(NullPointerException e){
            Log.e(TAG , "what is wrong with you", e);
        }

        // hide all quess button LinearLayouts
        for (LinearLayout layout : guessLinearLayout)
            layout.setVisibility(View.GONE);

        // display appropriate guess button LinearLayouts
        for (int row = 0; row < guessRows; row++)
            guessLinearLayout[row].setVisibility(View.VISIBLE);
    }
    public void updateRegions(SharedPreferences sharedPreferences){
        regionsSet = sharedPreferences.getStringSet(MainActivity.REGIONS ,null);

    }

    public void resetQuiz(){
        AssetManager assets = getActivity().getAssets();
        fileNameList.clear();

        try{
            for (String region : regionsSet){
                String [] paths = assets.list(region);

                for (String path : paths)
                    fileNameList.add(path.replace(".png",""));
            }
        }
        catch (IOException exception){
            Log.e(TAG, "Error loading image file names", exception);
        }
        correctAnswers =0; // reseting total no of correct guesses made
        totalGuesses =0; // reseting total no of guesses user made
        quizCountriesList.clear();// clear prior list of quiz countries

        int flagCounter = 1;
        int numberOfFlags = fileNameList.size();

        while (flagCounter <= FLAGS_IN_QUIZ){
            int randomIndex = random.nextInt(numberOfFlags);

            String filename = fileNameList.get(randomIndex);

            if(!quizCountriesList.contains(filename)){
                quizCountriesList.add(filename);
                ++flagCounter;
            }
        }
        loadNextFlag(); // basically the guy that starts the series


    }
    public void loadNextFlag(){

        String nextImage =quizCountriesList.remove(0);
        correctAnswer = nextImage;
        answerTextView.setText(" "); //clear answer textview

        //to display current question number

        questionNumberTextView.setText(getString(R.string.question,(correctAnswers+1), FLAGS_IN_QUIZ));
        // to extract region from the next Image name
        String region =  nextImage.substring(0,nextImage.indexOf("-"));

        //using asset manager to load image from the next assets file
        AssetManager assets = getActivity().getAssets();

        try(InputStream stream = assets.open(region+ "/" + nextImage + ".png"))
        {
            Drawable flag = Drawable.createFromStream(stream, nextImage);
            flagImageView.setImageDrawable(flag);
            animate(false); // toanimate flag onto the screen
        }
        catch (IOException exception){
            Log.e(TAG , "Error loading" +nextImage ,exception);
        }
        Collections.shuffle(fileNameList); //shuffle file names

        // put the correct answer at the end of filenamelist
        int correct = fileNameList.indexOf(correctAnswer);
        fileNameList.add(fileNameList.remove(correct));

        for (int row =0; row < guessRows ; row++){
            // placing buttons in current tab row

            for (int column = 0; column <guessLinearLayout[row].getChildCount(); column++){
                Button newGuessButton = (Button)guessLinearLayout[row].getChildAt(column);
                newGuessButton.setEnabled(true);

                // getting countries name and setting it as newGuessButton's text

                String filename = fileNameList.get((row * 2) + column);
                newGuessButton.setText(getCountryName(filename));

            }
        }

        // randomlyplacing one button with the correct answer
        int row = random.nextInt(guessRows);// picking a random row
        int column = random.nextInt(2); // picking a random column
        LinearLayout randomRow = guessLinearLayout[row];

        String countryName = getCountryName(correctAnswer);
        ((Button) randomRow.getChildAt(column)).setText(countryName);

    }

    private String getCountryName(String name){
       return name.substring(name.indexOf('-')+1).replace('_', ' ');
    }
    private void animate(boolean animateOut){
        if(correctAnswers == 0)
            return;

        //caclulating centre x and y

        int centerX = (quizLinearLayout.getLeft()+quizLinearLayout.getRight()) / 2;
        int centerY = (quizLinearLayout.getTop()+quizLinearLayout.getBottom())/2;

        int radius = Math.max(quizLinearLayout.getWidth(),quizLinearLayout.getHeight());

        Animator animator ;

        // if the quiz Linear layout should animate out rather than in

        if(animateOut){
            animator = ViewAnimationUtils.createCircularReveal(quizLinearLayout, centerX ,centerY ,radius ,0);

            animator.addListener(new AnimatorListenerAdapter() {
                // what to do when the animation is finished
                @Override
                public void onAnimationEnd(Animator animation) {
                    loadNextFlag();
                }
            });
        }
        else { // if the quiz linearLayout  should animate in

            animator = ViewAnimationUtils.createCircularReveal(quizLinearLayout,centerX,centerY,0,radius);
        }
        animator.setDuration(500); // this sets the animation duration to 500 ms
        animator.start(); // starting the animation
    }

    public OnClickListener guessButtonListner = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Button guessButton = ((Button)v);
            String guess = guessButton.getText().toString();
            String answer = getCountryName(correctAnswer);
            ++totalGuesses; // incrementing number of total guesses the user has made

            if(guess.equals(answer)){ // if guess is correct
                ++correctAnswers; // increment the number of correct answers

                // to display correct answer in green
                answerTextView.setText(answer + "!");
                answerTextView.setTextColor(getResources().getColor(R.color.correct_answer, getContext().getTheme()));

                disableButtons(); // to disable all guess buttons

                if (correctAnswers == FLAGS_IN_QUIZ){
                   // create dialog to create quiz starts and start a new quiz

                    DialogFragment quizResults = new DialogFragment() {


                        // creating an alert dialog and returning it

                         @Override
                        public Dialog onCreateDialog(Bundle bundle) {

                            AlertDialog.Builder builder =
                                    new AlertDialog.Builder(
                                    getActivity());
                            builder.setMessage(
                                    getString(R.string.results, totalGuesses,
                                            (1000 / (double) totalGuesses)));
                            builder.setPositiveButton(R.string.reset_quiz,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            resetQuiz();
                                        }
                                    });
                            return builder.create();// return AlertDialog
                        }

                    };
                    // using fragment to display dialogfragment
                    quizResults.setCancelable(false);
                    quizResults.show(getFragmentManager(), "quiz results");
                }
                else { // answer is correct but quiz is not over
                    // load the next flag after a 2 second delay
                    handler.postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    animate(true);
                                }
                            }, 2000);

                }

            }
            else{ // answer incorrect;
                flagImageView.startAnimation(shakeAnimation); // play shake

                // display Incorrect in red
                answerTextView.setText(R.string.incorrect_answer);
                answerTextView.setTextColor(getResources().getColor(R.color.incorrect_answer, getContext().getTheme()));
                guessButton.setEnabled(false); // disable incorrect answer

            }

        }
    };

    //utitlity method to disable buttons

    private void disableButtons(){
        for (int row = 0; row< guessRows; row++){
            LinearLayout guessRow = guessLinearLayout[row];
            for (int i =0; i<guessRow.getChildCount(); i++)
                guessRow.getChildAt(i).setEnabled(false);
        }

    }


}
