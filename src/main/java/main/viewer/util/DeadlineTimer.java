package main.viewer.util;

import model.CalendarWrapper;
import model.Deadline;
import main.controller.GUIController;
import org.joda.time.Period;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.LinkedList;


/**
 * This class represents the timer tasks that sets for each deadline to send notifications
 * when deadlines are approaching
 */
public class DeadlineTimer {
    private static final Integer MINUTE = 60000;
    private static final Integer HOUR = MINUTE * 60;
    private static final Integer DAY = HOUR * 60 * 24;

    // members
    private final Deadline deadline;
    private final GUIController parent;
    private LinkedList<Timer> timers;
    private boolean isRunning;

    /**
     * Constructor
     * @param deadline the deadline that the timers' schedules are based on
     * @param parent the GUIController main part that will send the notification to the user
     * @requires deadline != null; parent != null
     * @modifies deadline, timers, parent
     * @effects create a new DeadlineTimer instance
     */
    public DeadlineTimer(Deadline deadline, GUIController parent) {
        this.deadline = deadline;
        this.timers = new LinkedList<>();
        this.parent = parent;
        this.addTimers();
    }

    /**
     * This method initialize all timers
     * @requires None
     * @modifies timers
     * @effects initialize the timers
     */
    private void addTimers() {
        // notification not required for past deadlines
        if (this.shouldStop())
            return;
        // get remaining time
        Period remainingTime = deadline.getRemainPeriod(null).getKey();
        if (remainingTime.getMonths() >= 1 || remainingTime.getWeeks() * 7 + remainingTime.getDays() >= 15) {
            return;
        }
        // create timers for different intervals
        Timer Timer01 = new Timer(MINUTE, e -> sendNotification(MINUTE, e));
        Timer Timer05 = new Timer(MINUTE * 5, e -> sendNotification(MINUTE * 5, e));
        Timer Timer15 = new Timer(MINUTE * 15, e -> sendNotification(MINUTE * 15, e));
        Timer TimerHour = new Timer(HOUR, e -> sendNotification(HOUR, e));
        Timer TimerDay = new Timer(DAY, e -> sendNotification(DAY, e));
        this.timers.add(Timer01);
        this.timers.add(Timer05);
        this.timers.add(Timer15);
        this.timers.add(TimerHour);
        this.timers.add(TimerDay);
        System.out.println("DEBUG: [DeadlineTimer] {" + this.deadline + "} timer threads initialized.");
    }

    /**
     * This method will send a message to the user about deadline approaching
     * @requires None
     * @modifies parent
     * @effects send a notification
     */
    private void sendNotification() {
        String message = this.deadline.getCourse() + ": " + this.deadline.getName() + " " +
                "Due in " + " " + this.deadline.getRemainingText(CalendarWrapper.now());
        System.out.println("DEBUG: [DeadlineTimer] " + message);
    }

    /**
     * This function checks if the current timer should send a message to the user
     * If so, it will then call sendNotification() method to send the message
     * @param interval the time interval for the timer
     * @param e the timer event
     * @requires interval != null
     * @modifies None
     * @effects send a notification
     */
    private void sendNotification(Integer interval, ActionEvent e) {
        // No message should be sent after the deadline has came and gone
        if (this.shouldStop()) {
            this.stop();
        }
        Period remainingTime = deadline.getRemainPeriod(null).getKey();
        // If there is still more than one days left
        if (remainingTime.getDays() >= 1) {
            if (interval.equals(DAY)) sendNotification();
            return;
        }
        // If there is still more than one hours left
        if (remainingTime.getHours() >= 1) {
            if (interval.equals(DAY)) ((Timer)e.getSource()).stop();
            if (interval.equals(HOUR)) sendNotification();
            return;
        }
        // If there is still more than 15 minutes left
        if (remainingTime.getMinutes() >= 15) {
            if (interval.equals(HOUR)) ((Timer)e.getSource()).stop();
            if (interval.equals(MINUTE * 15)) sendNotification();
            return;
        }
        // If there is still more than 5 minutes left
        if (remainingTime.getMinutes() >= 5) {
            if (interval.equals(MINUTE * 15)) ((Timer)e.getSource()).stop();
            if (interval.equals(MINUTE * 5)) sendNotification();
            return;
        }
        // If there is still more than 1 minutes left
        if (remainingTime.getMinutes() >= 1) {
            if (interval.equals(MINUTE * 5)) ((Timer)e.getSource()).stop();
            if (interval.equals(MINUTE)) sendNotification();
            return;
        }
        ((Timer)e.getSource()).stop();
    }

    /**
     * This method will start all timers
     * @requires None
     * @modifies timers
     * @effects start all timer tasks
     */
    public void start() {
        if (this.shouldStop()) {
            return;
        }
        this.isRunning = true;
        System.out.println("DEBUG: [DeadlineTimer] {" + this.deadline + "} timer threads started.");
        for (Timer t: this.timers) {
            t.start();
        }
    }

    /**
     * This method will stop all timers
     * @requires None
     * @modifies timers
     * @effects stop all timer tasks
     */
    public void stop() {
        if (!this.isRunning) {
            return;
        }
        this.isRunning = false;
        System.out.println("DEBUG: [DeadlineTimer] {" + this.deadline + "} timer threads stopped.");
        for (Timer t: this.timers) {
            t.stop();
        }
    }

    /**
     * This method checks if the deadline is actually upcoming and not finished so that
     * a timer is needed.
     * @requires None
     * @modifies None
     * @effects check the deadline status
     * @return true if the timer should be set for the current deadline and false otherwise
     */
    private boolean shouldStop() {
        return this.deadline.isBefore(CalendarWrapper.now());
    }
}
