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
package org.bedework.calfacade;

import org.bedework.util.calendar.IcalDefs;
import org.bedework.util.misc.ToString;
import org.bedework.util.misc.Util;

/** Represent an attending participant.
 *
 * These are not stored in the database.
 *
 *  @author Mike Douglass   douglm - bedework.org
 */
public class Attendee implements Comparable<Attendee> {
  private final BwParticipants parent;

  private final BwEvent event;

  /* Attendance may be indicated by a Participant object with
     participantType = attendee or by a BwAttendee object only.

      If this is non-null we'll set the values appropriately.
   */
  private final BwAttendee attendee;

  private final BwParticipant participant;

  /** Constructor
   *
   */
  Attendee(final BwParticipants parent,
           final BwEvent event,
           final BwAttendee attendee,
           final BwParticipant participant) {
    this.parent = parent;
    this.event = event;
    this.attendee = attendee;
    this.participant = participant;
  }

  /* ==============================================================
   *                      Bean methods
   * ============================================================== */

  /**
   *
   * @return attendee object. DO NOT MODIFY scheduling attributes
   * directly
   */
  public BwAttendee getAttendee() {
    return attendee;
  }

  /**
   *
   * @return participant object. DO NOT MODIFY scheduling attributes
   * directly
   */
  public BwParticipant getParticipant() {
    return participant;
  }

  /**
   *
   *  @param  val   String participation status as defined for
   *                PARTICIPANT
   */
  public void setParticipationStatus(final String val) {
    if (attendee != null) {
      attendee.setPartstat(val);
    }

    if (participant != null) {
      participant.setParticipationStatus(val);
    }
  }

  /**
   *
   *  @return String     participation status
   */
  public String getParticipationStatus() {
    if (attendee != null) {
      return attendee.getPartstat();
    }

    if (participant != null) {
      return participant.getParticipationStatus();
    }

    return null;
  }

  /** Set the delegatedFrom
   *
   *  @param  val   String delegatedFrom
   */
  public void setDelegatedFrom(final String val) {
    if (attendee != null) {
      attendee.setDelegatedFrom(val);
    }

    if (participant != null) {
      participant.setDelegatedFrom(val);
    }
  }

  /** Get the delegatedFrom
   *
   *  @return String     delegatedFrom
   */
  public String getDelegatedFrom() {
    if (attendee != null) {
      return attendee.getDelegatedFrom();
    }

    if (participant != null) {
      return participant.getDelegatedFrom();
    }

    return null;
  }

  /** Set the delegatedTo
   *
   *  @param  val   String delegatedTo
   */
  public void setDelegatedTo(final String val) {
    if (attendee != null) {
      attendee.setDelegatedTo(val);
    }

    if (participant != null) {
      participant.setDelegatedTo(val);
    }
  }

  /** Get the delegatedTo
   *
   *  @return String     delegatedTo
   */
  public String getDelegatedTo() {
    if (attendee != null) {
      return attendee.getDelegatedTo();
    }

    if (participant != null) {
      return participant.getDelegatedTo();
    }

    return null;
  }

  /** Set the member
   *
   *  @param  val   String member
   */
  public void setMemberOf(final String val) {
    if (attendee != null) {
      attendee.setMember(val);
    }

    if (participant != null) {
      participant.setMemberOf(val);
    }
  }

  /** Get the member
   *
   *  @return String     member
   */
  public String getMemberOf() {
    if (attendee != null) {
      return attendee.getMember();
    }

    if (participant != null) {
      return participant.getMemberOf();
    }

    return null;
  }

  /**
   *
   *  @param  val   boolean ExpectReply
   */
  public void setExpectReply(final boolean val) {
    if (attendee != null) {
      attendee.setRsvp(val);
    }

    if (participant != null) {
      participant.setExpectReply(val);
    }
  }

  /**
   *
   *  @return boolean     ExpectReply
   */
  public boolean getExpectReply() {
    if (attendee != null) {
      return attendee.getRsvp();
    }

    if (participant != null) {
      return participant.getExpectReply();
    }

    return false;
  }

  /** Set the email param
   *
   *  @param  val   String email
   */
  public void setEmail(final String val) {
    if (attendee != null) {
      attendee.setEmail(val);
    }

    if (participant != null) {
      participant.setEmail(val);
    }
  }

  /** Get the email
   *
   *  @return String  email
   */
  public String getEmail() {
    if (attendee != null) {
      return attendee.getEmail();
    }

    if (participant != null) {
      return participant.getEmail();
    }

    return null;
  }

  /**
   *
   * @param val    scheduling sequence number
   */
  public void setSequence(final int val) {
    if (attendee != null) {
      attendee.setSequence(val);
    }

    if (participant != null) {
      participant.setSequence(val);
    }
  }

  /**
   *
   * @return int    the events scheduling sequence
   */
  public int getSequence() {
    if (attendee != null) {
      return attendee.getSequence();
    }

    if (participant != null) {
      return participant.getSequence();
    }

    return 0;
  }

  /**
   * @param val the dtstamp
   */
  public void setSchedulingDtStamp(final String val) {
    if (attendee != null) {
      attendee.setDtstamp(val);
    }

    if (participant != null) {
      participant.setSchedulingDtStamp(val);
    }
  }

  /**
   * @return String datestamp
   */
  public String getSchedulingDtStamp() {
    if (attendee != null) {
      return attendee.getDtstamp();
    }

    if (participant != null) {
      return participant.getSchedulingDtStamp();
    }

    return null;
  }

  /**
   *
   * @param val    schedule agent
   */
  public void setScheduleAgent(final String val) {
    if (attendee != null) {
      for (int i = 0; i <= IcalDefs.scheduleAgents.length; i++) {
        if (IcalDefs.scheduleAgents[i].equalsIgnoreCase(val)) {
          attendee.setScheduleAgent(i);
          break;
        }
      }
    }

    if (participant != null) {
      participant.setScheduleAgent(val);
    }
  }

  /** Get the schedule agent
   *
   * @return String   schedule agent
   */
  public String getScheduleAgent() {
    if (attendee != null) {
      final var i = attendee.getScheduleAgent();
      if ((i >= 0) && (i < IcalDefs.scheduleAgents.length)) {
        return IcalDefs.scheduleAgents[i];
      }
    }

    if (participant != null) {
      return participant.getScheduleAgent();
    }

    return null;
  }

  /** Set the schedule status
   *
   * @param val    schedule status
   */
  public void setScheduleStatus(final String val) {
    if (attendee != null) {
      attendee.setScheduleStatus(val);
    }

    if (participant != null) {
      participant.setScheduleStatus(val);
    }
  }

  /**
   *
   * @return String    schedule status
   */
  public String getScheduleStatus() {
    if (attendee != null) {
      return attendee.getScheduleStatus();
    }

    if (participant != null) {
      return participant.getScheduleStatus();
    }

    return null;
  }

  /* *
   *
   *  @param  val true/false for stay-informed
   * /
  public void setStayInformed(final boolean val) {
    final String sival;

    if (val) {
      sival = "T";
    } else {
      sival = "F";
    }

    assignSentByField(stayInformedIndex, sival);
  }

  /**
   *
   *  @return boolean
   * /
  public boolean getStayInformed() {
    return "T".equals(fetchSentByField(stayInformedIndex));
  }

  /* ==============================================================
   *                   Object methods
   * ============================================================== */

  @Override
  public int hashCode() {
    if (attendee != null) {
      return attendee.hashCode();
    }

    if (participant != null) {
      return participant.hashCode();
    }

    return -1;
  }

  @Override
  public int compareTo(final Attendee that)  {
    if (this == that) {
      return 0;
    }

    final int res = Util.cmpObjval(this.attendee, that.attendee);
    if (res != 0) {
      return res;
    }

    return Util.cmpObjval(this.participant, that.participant);
  }

  @Override
  public String toString() {
    final ToString ts = new ToString(this);

    if (attendee != null) {
      ts.append("attendee", attendee);
    }
    if (participant != null) {
      ts.append("participant", participant);
    }
    return toString();
  }
}

