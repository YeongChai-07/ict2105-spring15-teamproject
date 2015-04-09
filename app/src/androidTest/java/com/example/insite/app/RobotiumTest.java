package com.example.insite.app;

import android.test.ActivityInstrumentationTestCase2;

import com.robotium.solo.Solo;

/**
 * Created by Khaleef on 10/4/2015.
 */
public class RobotiumTest extends
        ActivityInstrumentationTestCase2<MainActivity> {

    private Solo solo;

    public RobotiumTest() {
        super(MainActivity.class);
    }

    public void setUp() throws Exception {
        solo = new Solo(getInstrumentation(), getActivity());
    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }

    public void testReportIssue() throws Exception {
        //Unlock the lock screen
        solo.unlockScreen();
    }

    public void testViewIssue() throws Exception {
        //Unlock the lock screen
        solo.unlockScreen();
        //Navigate tabs
        solo.clickOnText("IN PROGRESS");
        solo.clickOnText("RESOLVED");
        //Click on the first issue on the list
        solo.clickOnImage(1);
        boolean categoryFound = solo.searchText("Resolved");
        assertTrue("Resolved", categoryFound);
    }
}
