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

import org.bedework.calfacade.base.Differable;
import org.bedework.util.calendar.IcalDefs;
import org.bedework.util.misc.ToString;
import org.bedework.util.misc.Util;

import net.fortuna.ical4j.model.TextList;
import net.fortuna.ical4j.model.property.ParticipantType;
import net.fortuna.ical4j.model.property.SchedulingAgent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Represent an attending participant.
 *
 * These are not stored in the database.
 *
 *  @author Mike Douglass   douglm - bedework.org
 */
public class Participant
        implements Comparable<Participant>, Differable<Participant> {
  private final SchedulingInfo parent;

  /* Attendance may be indicated by a Participant object with
     participantType = attendee or by a BwAttendee object only.

      If either is non-null we'll set the values appropriately.
   */
  private BwAttendee attendee;

  private BwParticipant bwParticipant;

  /** Constructor
   *
   */
  Participant(final SchedulingInfo parent,
              final BwAttendee attendee,
              final BwParticipant bwParticipant) {
    this.parent = parent;
    this.attendee = attendee;
    this.bwParticipant = bwParticipant;
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
  public BwParticipant getBwParticipant() {
    return bwParticipant;
  }

  /**
   *
   *  @param  val   calendar address
   */
  public void setCalendarAddress(final String val) {
    if (attendee != null) {
      attendee.setAttendeeUri(val);
    }

    if (bwParticipant != null) {
      bwParticipant.setCalendarAddress(val);
    }
  }

  /**
   *
   *  @return String     calendar address
   */
  public String getCalendarAddress() {
    if (attendee != null) {
      return attendee.getAttendeeUri();
    }

    if (bwParticipant != null) {
      return bwParticipant.getCalendarAddress();
    }

    return null;
  }

  /**
   *
   *  @param  val   name
   */
  public void setName(final String val) {
    if (attendee != null) {
      attendee.setCn(val);
    }

    if (bwParticipant != null) {
      bwParticipant.setName(val);
    }
  }

  /**
   *
   *  @return String     name
   */
  public String getName() {
    String val = null;
    if (attendee != null) {
      val = attendee.getCn();
    }

    if ((bwParticipant != null) && (val == null)) {
      return bwParticipant.getName();
    }

    return val;
  }

  /**
   *
   *  @param  val   Language
   */
  public void setLanguage(final String val) {
    if (attendee != null) {
      attendee.setLanguage(val);
    }

    if (bwParticipant != null) {
      bwParticipant.setLanguage(val);
    }
  }

  /**
   *
   *  @return String     Language
   */
  public String getLanguage() {
    String val = null;
    if (attendee != null) {
      val = attendee.getLanguage();
    }

    if ((bwParticipant != null) && (val == null)) {
      return bwParticipant.getLanguage();
    }

    return val;
  }

  /**
   *
   *  @param  val   kind: individual, group etc
   */
  public void setKind(final String val) {
    if (attendee != null) {
      attendee.setCuType(val);
    }

    if (bwParticipant != null) {
      bwParticipant.setKind(val);
    }
  }

  /**
   *
   *  @return String     kind: individual, group etc
   */
  public String getKind() {
    if (attendee != null) {
      return attendee.getCuType();
    }

    if (bwParticipant != null) {
      return bwParticipant.getKind();
    }

    return null;
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

    if (bwParticipant != null) {
      bwParticipant.setParticipationStatus(val);
    }
  }

  /**
   *
   *  @return String     participation status
   */
  public String getParticipationStatus() {
    if (attendee != null) {
      final var s = attendee.getPartstat();
      if (s != null) {
        return s;
      }
    }

    if (bwParticipant != null) {
      final var s = bwParticipant.getParticipationStatus();
      if (s != null) {
        return s;
      }
    }

    return "needs-action";
  }

  /**
   *
   *  @param  val   String participation type as defined for
   *                PARTICIPANT
   */
  public void setParticipantType(final String val) {
    if (attendee != null) {
      attendee.setRole(iCalRole(val));
    }

    if (bwParticipant != null) {
      bwParticipant.setParticipantType(val);
    }
  }

  /**
   *
   *  @return String     participation type
   */
  public String getParticipantType() {
    if (attendee != null) {
      return jsCalRolesAsString(attendee.getRole());
    }

    if (bwParticipant != null) {
      return bwParticipant.getParticipantType();
    }

    return null;
  }

  /**
   *
   * @param val participant type
   * @return true if in list (ignoring case)
   */
  public boolean includesParticipantType(final String val) {
    if (bwParticipant != null) {
      return bwParticipant.includesParticipantType(val);
    }

    if (attendee != null) {
      return jsCalRoles(attendee.getRole())
              .contains(val.toLowerCase());
    }

    return false;
  }

  /**
   *
   * @return true if is a recipient of scheduling messages
   */
  public boolean isRecipient() {
    if (attendee != null) {
      return true;
    }

    if (bwParticipant != null) {
      return bwParticipant.includesParticipantType(ParticipantType.VALUE_ATTENDEE) ||

              bwParticipant.includesParticipantType(ParticipantType.VALUE_CHAIR) ||
              bwParticipant.includesParticipantType(ParticipantType.VALUE_VOTER);
    }

    return false;
  }


  private static final Map<String, List<String>> jscalRoles =
          new HashMap<>();
  private static final Map<String, String> jscalRolesAsString =
          new HashMap<>();
  final private static List<String> defaultRoles =
          List.of("attendee");
  static {
    jscalRoles.put("CHAIR",
                   List.of("chair"));
    jscalRoles.put("REQ-PARTICIPANT",
                   defaultRoles);
    jscalRoles.put("OPT-PARTICIPANT",
                   List.of("attendee", "optional"));
    jscalRoles.put("NON-PARTICIPANT",
                   List.of("attendee", "informational"));

    jscalRolesAsString.put("CHAIR", "chair");
    jscalRolesAsString.put("REQ-PARTICIPANT", "attendee");
    jscalRolesAsString.put("OPT-PARTICIPANT",
                           "attendee, optional");
    jscalRolesAsString.put("NON-PARTICIPANT",
                           "attendee, informational");
  }

  public static List<String> jsCalRoles(final String icalRole) {
    if (icalRole == null) {
      return defaultRoles;
    }

    final var res = jscalRoles.get(icalRole.toUpperCase());

    if (res == null) {
      return defaultRoles;
    }

    return res;
  }

  public static String jsCalRolesAsString(final String icalRole) {
    if (icalRole == null) {
      return "attendee";
    }

    final var res = jscalRolesAsString.get(icalRole.toUpperCase());

    if (res == null) {
      return "attendee";
    }

    return res;
  }

  public static String iCalRole(final String jsCalRoles) {
    final var roles = new TextList(jsCalRoles);

    if (roles.containsIgnoreCase("chair")) {
      return "CHAIR";
    }

    if (!roles.containsIgnoreCase("attendee")) {
      return jsCalRoles;
    }

    if (roles.size() == 1) {
      return "REQ-PARTICIPANT";
    }

    if (roles.containsIgnoreCase("optional")) {
      return "OPT-PARTICIPANT";
    }

    if (roles.containsIgnoreCase("informational")) {
      return "NON-PARTICIPANT";
    }

    return jsCalRoles;
  }

  /** Set the delegatedFrom
   *
   *  @param  val   String delegatedFrom
   */
  public void setDelegatedFrom(final String val) {
    if (attendee != null) {
      attendee.setDelegatedFrom(val);
    }

    if (bwParticipant != null) {
      bwParticipant.setDelegatedFrom(val);
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

    if (bwParticipant != null) {
      return bwParticipant.getDelegatedFrom();
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

    if (bwParticipant != null) {
      bwParticipant.setDelegatedTo(val);
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

    if (bwParticipant != null) {
      return bwParticipant.getDelegatedTo();
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

    if (bwParticipant != null) {
      bwParticipant.setMemberOf(val);
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

    if (bwParticipant != null) {
      return bwParticipant.getMemberOf();
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

    if (bwParticipant != null) {
      bwParticipant.setExpectReply(val);
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

    if (bwParticipant != null) {
      return bwParticipant.getExpectReply();
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

    if (bwParticipant != null) {
      bwParticipant.setEmail(val);
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

    if (bwParticipant != null) {
      return bwParticipant.getEmail();
    }

    return null;
  }

  /**
   *
   *  @param  val   String invitedBy
   */
  public void setInvitedBy(final String val) {
    if (attendee != null) {
      attendee.setSentBy(val);
    }

    if (bwParticipant != null) {
      bwParticipant.setInvitedBy(val);
    }
  }

  /**
   *
   *  @return String  invitedBy
   */
  public String getInvitedBy() {
    if (attendee != null) {
      return attendee.getSentBy();
    }

    if (bwParticipant != null) {
      return bwParticipant.getInvitedBy();
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

    if (bwParticipant != null) {
      bwParticipant.setSequence(val);
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

    if (bwParticipant != null) {
      return bwParticipant.getSequence();
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

    if (bwParticipant != null) {
      bwParticipant.setSchedulingDtStamp(val);
    }
  }

  /**
   * @return String datestamp
   */
  public String getSchedulingDtStamp() {
    if (attendee != null) {
      return attendee.getDtstamp();
    }

    if (bwParticipant != null) {
      return bwParticipant.getSchedulingDtStamp();
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

    if (bwParticipant != null) {
      bwParticipant.setScheduleAgent(val);
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

    if (bwParticipant != null) {
      final var agent = bwParticipant.getScheduleAgent();
      if (agent != null) {
        return bwParticipant.getScheduleAgent();
      }
    }

    return SchedulingAgent.VALUE_SERVER;
  }

  /** Set the schedule status
   *
   * @param val    schedule status
   */
  public void setScheduleStatus(final String val) {
    if (attendee != null) {
      attendee.setScheduleStatus(val);
    }

    if (bwParticipant != null) {
      bwParticipant.setScheduleStatus(val);
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

    if (bwParticipant != null) {
      return bwParticipant.getScheduleStatus();
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
  */

  public void copyTo(final Participant that)  {
    if (attendee != null) {
      if (that.attendee != null) {
        attendee.copyTo(that.attendee);
      } else {
        that.attendee = (BwAttendee)attendee.clone();
      }
    }

    if (bwParticipant != null) {
      if (that.bwParticipant != null) {
        that.bwParticipant.copyTo(that.bwParticipant);
      } else {
        that.bwParticipant = (BwParticipant)bwParticipant.clone();
      }
    }
  }

  /** Only true if something changes the status of, or information about, the
   * attendee.
   *
   * @param val incoming value
   * @return true for significant change
   */
  public boolean changedBy(final Participant val) {
    return changedBy(val, true);
  }

  /** Only true if something changes the status of, or information about, the
   * attendee.
   *
   * @param val incoming value
   * @param checkPartStat - true if we check the partstat
   * @return true for significant change
   */
  public boolean changedBy(final Participant val,
                           final boolean checkPartStat) {
    if (attendee != null) {
      if (val.attendee == null) {
        return true;
      }

      return attendee.changedBy(val.attendee, checkPartStat);
    }

    if (bwParticipant != null) {
      if (val.bwParticipant == null) {
        return true;
      }

      return bwParticipant.changedBy(val.bwParticipant, checkPartStat);
    }

    return (val.attendee != null) || (val.bwParticipant != null);
  }

  /* ==============================================================
   *                   Object methods
   * ============================================================== */

  @Override
  public int hashCode() {
    if (attendee != null) {
      return attendee.hashCode();
    }

    if (bwParticipant != null) {
      return bwParticipant.hashCode();
    }

    return -1;
  }

  @Override
  public int compareTo(final Participant that)  {
    if (this == that) {
      return 0;
    }

    final int res = Util.cmpObjval(this.attendee, that.attendee);
    if (res != 0) {
      return res;
    }

    return Util.cmpObjval(this.bwParticipant, that.bwParticipant);
  }

  @Override
  public String toString() {
    final ToString ts = new ToString(this);

    if (attendee != null) {
      ts.append("attendee", attendee);
    }
    if (bwParticipant != null) {
      ts.append("participant", bwParticipant);
    }

    return ts.toString();
  }

  @Override
  public boolean differsFrom(final Participant val) {
    return (Util.compareStrings(val.getParticipationStatus(),
                                getParticipationStatus()) != 0) ||
            (Util.compareStrings(val.getCalendarAddress(),
                                 getCalendarAddress()) != 0) ||
            (Util.compareStrings(val.getKind(), getKind()) != 0) ||
            (Util.compareStrings(val.getName(), getName()) != 0) ||
            (Util.compareStrings(val.getParticipantType(),
                                 getParticipantType()) != 0) ||
            (Util.compareStrings(val.getDelegatedFrom(),
                                 getDelegatedFrom()) != 0) ||
            (Util.compareStrings(val.getDelegatedTo(),
                                 getDelegatedTo()) != 0) ||
            (Util.compareStrings(val.getLanguage(),
                                 getLanguage()) != 0) ||
            (Util.compareStrings(val.getMemberOf(),
                                 getMemberOf()) != 0) ||
            (Util.cmpBoolval(val.getExpectReply(),
                             getExpectReply()) != 0) ||
            (Util.compareStrings(val.getEmail(),
                                 getEmail()) != 0) ||
           (Util.compareStrings(val.getInvitedBy(), getInvitedBy()) != 0) ||
            (Util.compareStrings(val.getScheduleAgent(), getScheduleAgent()) != 0);
  }
}

