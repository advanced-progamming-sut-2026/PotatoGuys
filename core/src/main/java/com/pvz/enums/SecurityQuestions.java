package com.pvz.enums;

import java.util.ArrayList;

public class SecurityQuestions {
    public static final ArrayList<String> QUESTIONS = new ArrayList<String>() {
        {
            add("What was the name of your first pet?");
            add("In what city or town did your parents meet?");
            add("What was your favorite food as a child?");
            add("What was the name of your first school?");
            add("What is the name of your favorite childhood friend?");
            add("What was the make and model of your first car?");
            add("In what city was your first job?");
            add("What is your maternal grandmother's maiden name?");
            add("What is the name of the street you grew up on?");
            add("What is your favorite movie or book of all time?");
        }
    };

    public static String getQuestionByNumber(int number) {
        if (number < 1 || number > QUESTIONS.size()) {
            return null;
        }
        return QUESTIONS.get(number - 1);
    }

}
