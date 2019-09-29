package model;

/**
 * @overview
 * This class represents the interface of a Deadline object
 *
 * @mutator
 *
 * @observers
 */
public interface DeadlineInterface extends Cloneable {
    /**
     * This function returns the minute of the deadline due time
     *
     * @requires None
     * @modifies None
     * @effects None
     * @return a number represents current minute. Starts from 0 to 59
     */
    int getMinute();

    /**
     * This function returns the hour of the deadline due time
     *
     * @requires None
     * @modifies None
     * @effects None
     * @return a number represents the deadline due time hour.
     *         It is used for the 24-hour clock and starts from 0 to 23
     */
    int getHour();

    /**
     * This function returns the date of the deadline due date
     *
     * @requires None
     * @modifies None
     * @effects None
     * @return a number represents date of the deadline due date.
     *         The first day of the month has value 1.
     */
    int getDay();

    /**
     * This function returns the month of the deadline due date
     *
     * @requires None
     * @modifies None
     * @effects None
     * @return the month number of the month number of the deadline due date.
     *         The first month of the year starts from 1 (Jan)
     */
    int getMonth();

    /**
     * This function returns the year of the deadline due date
     *
     * @requires None
     * @modifies None
     * @effects None
     * @return a number represents the year number of the deadline due date
     */
    int getYear();

    /**
     * This function returns the name of the course this deadline belongs to
     * @requires None
     * @modifies None
     * @effects None
     * @return the course name
     */
    String getCourse();

    /**
     * This function returns the date of this deadline
     * @requires None
     * @modifies None
     * @effects None
     * @return the name of this deadline
     */
    String getName();

    /**
     * Creates and returns a copy of this object. The precise meaning
     * of "copy" may depend on the class of the object.
     * @requires None
     * @modifies None
     * @effects make a clone
     * @return     a clone of this instance.
     * @throws  CloneNotSupportedException  if the object's class does not
     *               support the {@code Cloneable} interface. Subclasses
     *               that override the {@code clone} method can also
     *               throw this exception to indicate that an instance cannot
     *               be cloned.
     * @see java.lang.Cloneable
     */
    Object clone() throws CloneNotSupportedException;
}
