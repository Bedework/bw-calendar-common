package org.bedework.calfacade;

import org.bedework.base.ToString;
import org.bedework.util.calendar.ScheduleStates;

/**
 * Result for a single recipient.
 */
public class ScheduleRecipientResult
        implements ScheduleStates,
        Comparable<ScheduleRecipientResult> {
  /**
   *
   */
  public String recipient;

  private int status = scheduleUnprocessed;

  /**
   * Set if this is the result of a freebusy request.
   */
  public BwEvent freeBusy;

  /**
   * @param val - the value
   */
  public void setStatus(final int val) {
    status = val;
  }

  /**
   * @return scheduling status
   */
  public int getStatus() {
    return status;
  }

  @Override
  public int compareTo(final ScheduleRecipientResult that) {
    return recipient.compareTo(that.recipient);
  }

  @Override
  public String toString() {
    final ToString ts = new ToString(this);

    ts.append("recipient", recipient);
    ts.append("status", status);

    return ts.toString();
  }
}
