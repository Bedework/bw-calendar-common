/* ********************************************************************
    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.
*/
package org.bedework.calsvci;

import org.bedework.calfacade.Participant;
import org.bedework.calfacade.BwAttendee;
import org.bedework.calfacade.BwCollection;
import org.bedework.calfacade.BwDateTime;
import org.bedework.calfacade.BwDuration;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwOrganizer;
import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.ScheduleResult;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.calfacade.util.EventPeriod;
import org.bedework.util.calendar.ScheduleStates;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Mike Douglass
 *
 */
public interface SchedulingI extends Serializable {
  /** We have detected changes to an event that require a schedule. This method
   * will set up the event ready for a call to send out the REQUEST. This will
   * involve at least incrementing th esequence and setting PARTSTAT to
   * NEEDS-ACTION
   *
   * @param ei event to be rescheduled
   */
  void setupReschedule(EventInfo ei);

  /** Schedule a meeting or publish an event. The event object must have the organizer
   * and attendees and possibly recipients set according to itip + caldav.
   *
   * <p>The event will be added to the users outbox which will trigger the send
   * of requests to other users inboxes. For users within this system the
   * request will be immediately addded to the recipients inbox. For external
   * users they are sent via ischedule or mail.
   *
   * @param ei         EventInfo object containing event with
   *                   method=REQUEST, CANCEL,
   *                              ADD, DECLINECOUNTER or PUBLISH
   * @param recipient - non null to send to this recipient only (for REFRESH)
   * @param fromAttUri attendee uri
   * @param iSchedule  true if it's an iSchedule request.
   * @return ScheduleResult
   */
  ScheduleResult<?> schedule(EventInfo ei,
                             String recipient,
                             String fromAttUri,
                             boolean iSchedule,
                             ScheduleResult<?> res);

  /**
   * @param ei event
   * @param comment - optional comment
   * @param fromAtt attendee
   * @return ScheduleResult
   */
  ScheduleResult<?> declineCounter(EventInfo ei,
                                   String comment,
                                   BwAttendee fromAtt);

  /** Attendee wants a refresh
   *
   * @param ei event which is probably in a calendar.
   * @param comment - optional comment
   * @return   ScheduleResult
   */
  ScheduleResult<?> requestRefresh(EventInfo ei,
                                   String comment);

  /** Attendee wants to send a reply
   *
   * @param ei event which is probably in a calendar.
   * @param partstat - valid partstat.
   * @param comment - optional comment
   * @return   ScheduleResult
   */
  ScheduleResult<?> sendReply(EventInfo ei,
                              int partstat,
                              String comment);

  /** An attendee responds to a request.
   *
   * @param ei  The stored updated copy of the meeting
   * @param method - the scheduling method
   * @return   ScheduleResult
   */
  ScheduleResult<?> attendeeRespond(EventInfo ei,
                                    int method,
                                    ScheduleResult<?> res);

  /* * Handle replies to scheduling requests - that is the schedule
   * method was REPLY. We, as an organizer (or their delegate) are going to
   * use the reply to update the original invitation.
   *
   * @param ei - the incoming event in the inbox
   * @return ScheduleResult showing how things went.
   * /
  ScheduleResult processResponse(EventInfo ei);
  */

  /* * The organizer has canceled the meeting - or taken us (the attendee) off
   * the list.
   *
   * <p>We will look for the event in the users calendar(s) and either remove it
   * or set the status to canceled.
   *
   * @param ei            EventInfo object with method=CANCEL
   * @return ScheduleResult
   * /
  ScheduleResult processCancel(EventInfo ei);
  */

  /** Respond to a scheduling request. The event object must have the organizer
   * and a single attendee and possibly recipient set according to itip + caldav.
   *
   * <p>The event will be added to the users outbox which will trigger the send
   * of a request to the organizers inbox. For an organizer within this system the
   * request will be immediately addded to the recipients inbox. For an external
   * organizer it is sent via mail.
   *
   * @param ei         EventInfo object with event with method=REPLY, COUNTER or
   *                    REFRESH
   * @return ScheduleResult
   */
  ScheduleResult<?> scheduleResponse(EventInfo ei,
                                     ScheduleResult<?> res);

  /** Get the free busy for the given principal as a list of busy periods.
   *
   * @param fbset  Collections for which to provide free-busy. Null for the
   *               for default collection (as specified by user).
   *               Used for local access to a given calendar via e.g. caldav
   * @param who    If cal is null get the info for this user, otherwise
   *               this is used as the free/busy result owner
   * @param start time for period start
   * @param end time for period end
   * @param org - needed to make the result compliant
   * @param uid - uid of requesting component or null for no request
   * @param exceptUid if non-null omit this uid from the freebusy calculation
   * @return BwEvent
   */
  BwEvent getFreeBusy(Collection<BwCollection> fbset,
                      BwPrincipal<?> who,
                      BwDateTime start,
                      BwDateTime end,
                      BwOrganizer org,
                      String uid,
                      String exceptUid);

  /** Used for user interface. Result of dividing VFREEBUSY into equal sized
   * chunks.
   *
   * @author Mike Douglass
   *
   */
  class FbGranulatedResponse implements Serializable {
    private BwDateTime start;
    private BwDateTime end;

    /** How did it go? Status from scheduling request */
    public int respCode;

    /** */
    public boolean noResponse;

    /** Who for */
    public BwAttendee attendee;

    private String recipient;

    /** Collection of Granulator.EventPeriod */
    public Collection<EventPeriod> eps = new ArrayList<>();

    /**
     * @param val start of period
     */
    public void setStart(final BwDateTime val) {
      start = val;
    }

    /**
     * @return BwDateTime start
     */
    public BwDateTime getStart() {
      return start;
    }

    /**
     * @param val end of peiod
     */
    public void setEnd(final BwDateTime val) {
      end = val;
    }

    /**
     * @return BwDateTime end
     */
    public BwDateTime getEnd() {
      return end;
    }

    /**
     * @param val int response code
     */
    public void setRespCode(final int val) {
      respCode = val;
    }

    /**
     * @return int
     */
    public int getRespCode() {
      return respCode;
    }

    /**
     * @param val true if there was no response
     */
    public void setNoResponse(final boolean val) {
      noResponse = val;
    }

    /**
     * @return boolean
     */
    public boolean getNoResponse() {
      return noResponse;
    }

    /**
     * @param val a recipient
     */
    public void setRecipient(final String val) {
      recipient = val;
    }
    /**
     * @return String
     */
    public String getRecipient() {
      return recipient;
    }

    /**
     * @param val the attendee
     */
    public void setAttendee(final BwAttendee val) {
      attendee = val;
    }
    /**
     * @return BwAttendee
     */
    public BwAttendee getAttendee() {
      return attendee;
    }

    /**
     * @return boolean
     */
    public boolean okResponse() {
      return (respCode == ScheduleStates.scheduleOk) &&
             !noResponse;
    }
  }

  /**
   * @author douglm
   *
   */
  class FbResponses implements Serializable {
    private Collection<FbGranulatedResponse> responses;

    private FbGranulatedResponse aggregatedResponse;

    /**
     * @param val All responses with status
     */
    public void setResponses(final Collection<FbGranulatedResponse> val) {
      responses = val;
    }

    /**
     *
     * @return Collection of FbResponse
     */
    public Collection<FbGranulatedResponse> getResponses() {
      return responses;
    }

    /**
     *
     * @param val Aggregated response
     */
    public void setAggregatedResponse(final FbGranulatedResponse val) {
      aggregatedResponse = val;
    }

    /** Aggregated response
     *
     * @return FbResponse
     */
    public FbGranulatedResponse getAggregatedResponse() {
      return aggregatedResponse;
    }
  }

  /** Get calendar collections which affect freebusy.
   *
   * @return Collection of calendars.
   */
  Collection<BwCollection> getFreebusySet();

  /** Get aggregated free busy for a ScheduleResult.
   *
   * @param sr ScheduleResult
   * @param start     start from original request
   * @param end       end from original request
   * @param granularity as a duration
   * @return FbResponses
   */
  FbResponses aggregateFreeBusy(ScheduleResult<?> sr,
                                BwDateTime start, BwDateTime end,
                                BwDuration granularity);

  /** Granulate (divide into equal chunks and return the result. The response
   * from the original request may be for a different time range than requested.
   *
   * @param fb event
   * @param start     start from original request
   * @param end       end from original request
   * @param granularity as a duration
   * @return FbResponse
   */
  FbGranulatedResponse granulateFreeBusy(BwEvent fb,
                                         BwDateTime start,
                                         BwDateTime end,
                                         BwDuration granularity);

  /** Return the users copy of the active meeting with the
   * same uid as that given.
   *
   * @param ev event to locate
   * @return possibly null meeting
   */
  EventInfo getStoredMeeting(BwEvent ev);

  /** Find the attendee in this event which corresponds to the current user
   *
   * @param ei to search
   * @return attendee or null.
   */
  Participant findUserAttendee(EventInfo ei);
}
