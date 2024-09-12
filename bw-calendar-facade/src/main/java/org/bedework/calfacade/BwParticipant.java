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

import org.bedework.calfacade.base.BwCloneable;
import org.bedework.calfacade.base.BwDbentity;
import org.bedework.calfacade.base.Differable;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.util.misc.ToString;
import org.bedework.util.misc.Uid;
import org.bedework.util.misc.Util;

import net.fortuna.ical4j.model.component.Participant;
import net.fortuna.ical4j.model.property.CalendarAddress;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.EmailAddress;
import net.fortuna.ical4j.model.property.ExpectReply;
import net.fortuna.ical4j.model.property.Kind;
import net.fortuna.ical4j.model.property.Lang;
import net.fortuna.ical4j.model.property.MemberOf;
import net.fortuna.ical4j.model.property.Name;
import net.fortuna.ical4j.model.property.ParticipantType;
import net.fortuna.ical4j.model.property.ParticipationDelegatedFrom;
import net.fortuna.ical4j.model.property.ParticipationDelegatedTo;
import net.fortuna.ical4j.model.property.ParticipationStatus;
import net.fortuna.ical4j.model.property.SchedulingDtStamp;
import net.fortuna.ical4j.model.property.SchedulingSequence;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;

import static net.fortuna.ical4j.model.Property.EXPECT_REPLY;
import static net.fortuna.ical4j.model.Property.LANG;
import static net.fortuna.ical4j.model.Property.MEMBER_OF;
import static net.fortuna.ical4j.model.Property.PARTICIPATION_DELEGATED_FROM;
import static net.fortuna.ical4j.model.Property.PARTICIPATION_DELEGATED_TO;
import static net.fortuna.ical4j.model.Property.SCHEDULING_SEQUENCE;

/** Represent a participant component.
 *
 * These are not currently stored in the database but as x-properties.
 *
 *  @author Mike Douglass   douglm - bedework.org
 */
public class BwParticipant extends BwDbentity<BwParticipant>
         implements BwCloneable, Differable<BwParticipant> {
  private final BwParticipants parent;

  // Derived from the participant object.
  private String stringRepresentation;

  private final Participant participant;

  /** Constructor
   *
   */
  BwParticipant(final BwParticipants parent) {
    this.parent = parent;
    participant = new Participant();
    participant.getProperties().add(
            new net.fortuna.ical4j.model.property.Uid(Uid.getUid()));
  }

  /** Constructor
   *
   */
  BwParticipant(final BwParticipants parent,
                final Participant participant) {
    this.parent = parent;
    this.participant = participant;
  }

  /* ==============================================================
   *                      Bean methods
   * ============================================================== */

  /**
   *
   * @return participant object. DO NOT MODIFY
   */
  public Participant getParticipant() {
    return participant;
  }

  /**
   *
   *  @return String     uid
   */
  public String getUid() {
    final var p = participant.getUid();
    if (p == null) {
      return null;
    }
    return p.getValue();
  }

  /** Set the calendar address
   *
   *  @param  val   String calendar address
   */
  public void setCalendarAddress(final String val) {
    final var p = participant.getCalendarAddress();
    try {
      if (p == null) {
        participant.getProperties().add(new CalendarAddress(val));
      } else if (!val.equals(p.getValue())) {
        p.setValue(val);
        changed();
      }
    } catch (final URISyntaxException e) {
      throw new CalFacadeException(e);
    }
  }

  /**
   *
   *  @return String     calendar address
   */
  public String getCalendarAddress() {
    final var p = participant.getCalendarAddress();
    if (p == null) {
      return null;
    }
    return p.getValue();
  }

  /**
   *
   *  @param  val   String name
   */
  public void setName(final String val) {
    final var p = participant.getNameProperty();
    if (p == null) {
      participant.getProperties().add(new Name(val));
    } else if (!val.equals(p.getValue())) {
      p.setValue(val);
      changed();
    }
  }

  /**
   *
   *  @return String     name
   */
  public String getName() {
    final var p = participant.getNameProperty();
    if (p == null) {
      return null;
    }
    return p.getValue();
  }

  /**
   *
   *  @param  val   String description
   */
  public void setDescription(final String val) {
    final var p = participant.getDescription();
    if (p == null) {
      participant.getProperties().add(new Description(val));
    } else if (!val.equals(p.getValue())) {
      p.setValue(val);
      changed();
    }
  }

  /**
   *
   *  @return String     Description
   */
  public String getDescription() {
    final var p = participant.getDescription();
    if (p == null) {
      return null;
    }
    return p.getValue();
  }

  /**
   *
   *  @param  val   String kind
   */
  public void setKind(final String val) {
    final var p = participant.getKind();
    if (p == null) {
      participant.getProperties().add(new Kind(val));
    } else if (!val.equals(p.getValue())) {
      p.setValue(val);
      changed();
    }
  }

  /**
   *
   *  @return String     kind
   */
  public String getKind() {
    final var p = participant.getKind();
    if (p == null) {
      return null;
    }
    return p.getValue();
  }

  /** Set the participant type
   *
   *  @param  val   String participant type
   */
  public void setParticipantType(final String val) {
    final var p = participant.getParticipantType();
    if (p == null) {
      participant.getProperties().add(new ParticipantType(val));
    } else if (!val.equals(p.getValue())) {
      p.setValue(val);
      changed();
    }
  }

  /** Get the participant type
   *
   *  @return String     participant type as comma separated list
   */
  public String getParticipantType() {
    final var p = participant.getParticipantType();
    if (p == null) {
      return null;
    }
    return p.getValue();
  }

  /** Get the participant types as a list
   *
   *  @return List&lt;String>     participant type as list
   */
  public List<String> getParticipantTypes() {
    final var p = participant.getParticipantType();
    if (p == null) {
      return Collections.EMPTY_LIST;
    }
    return p.getTypes().asList();
  }

  /** Remove the participant type
   *
   */
  public void removeParticipantType(final String val) {
    final var p = participant.getParticipantType();
    if (p != null) {
      p.getTypes().remove(val);
    }
  }

  /** Add the participant type
   *
   */
  public void addParticipantType(final String val) {
    final var p = participant.getParticipantType();
    if (p == null) {
      participant.getProperties().add(new ParticipantType(val));
    } else {
      p.getTypes().add(val);
    }
  }

  /**
   *
   * @param val participant type
   * @return true if in list (ignoring case)
   */
  public boolean includesParticipantType(final String val) {
    final var p = participant.getParticipantType();
    if (p == null) {
      return false;
    }
    return p.getTypes().containsIgnoreCase(val);
  }

  /**
   *
   *  @param  val   String participation status
   */
  public void setParticipationStatus(final String val) {
    final var p = participant.getParticipationStatus();
    if (p == null) {
      participant.getProperties().add(new ParticipationStatus(val));
    } else if (!val.equals(p.getValue())) {
      p.setValue(val);
      changed();
    }
  }

  /**
   *
   *  @return String     participation status
   */
  public String getParticipationStatus() {
    final var p = participant.getParticipationStatus();
    if (p == null) {
      return null;
    }
    return p.getValue();
  }

  /** Set the delegatedFrom
   *
   *  @param  val   String delegatedFrom
   */
  public void setDelegatedFrom(final String val) {
    final var p = (ParticipationDelegatedFrom)participant.
            getProperties().
            getProperty(PARTICIPATION_DELEGATED_FROM);
    try {
      if (p == null) {
        participant.getProperties().
                   add(new ParticipationDelegatedFrom(val));
      } else if (!val.equals(p.getValue())) {
        p.setValue(val);
        changed();
      }
    } catch (final URISyntaxException e) {
      throw new CalFacadeException(e);
    }
  }

  /** Get the delegatedFrom
   *
   *  @return String     delegatedFrom
   */
  public String getDelegatedFrom() {
    final var p = (ParticipationDelegatedFrom)participant.
            getProperties().
            getProperty(PARTICIPATION_DELEGATED_FROM);
    if (p == null) {
      return null;
    }
    return p.getValue();
  }

  /** Set the delegatedTo
   *
   *  @param  val   String delegatedTo
   */
  public void setDelegatedTo(final String val) {
    final var p = (ParticipationDelegatedTo)participant.
            getProperties().
            getProperty(PARTICIPATION_DELEGATED_TO);
    try {
      if (p == null) {
        participant.getProperties().
                   add(new ParticipationDelegatedTo(val));
      } else if (!val.equals(p.getValue())) {
        p.setValue(val);
        changed();
      }
    } catch (final URISyntaxException e) {
      throw new CalFacadeException(e);
    }
  }

  /** Get the delegatedTo
   *
   *  @return String     delegatedTo
   */
  public String getDelegatedTo() {
    final var p = (ParticipationDelegatedTo)participant.
            getProperties().
            getProperty(PARTICIPATION_DELEGATED_TO);
    if (p == null) {
      return null;
    }
    return p.getValue();
  }

  /** Set the language
   *
   *  @param  val   String language
   */
  public void setLanguage(final String val) {
    final var p = (Lang)participant.
            getProperties().
            getProperty(LANG);
    if (p == null) {
      participant.getProperties().add(new Lang(val));
    } else if (!val.equals(p.getValue())) {
      p.setValue(val);
      changed();
    }
  }

  /** Get the language
   *
   *  @return String     language
   */
  public String getLanguage() {
    final var p = (Lang)participant.
            getProperties().
            getProperty(LANG);
    if (p == null) {
      return null;
    }
    return p.getValue();
  }

  /** Set the member
   *
   *  @param  val   String member
   */
  public void setMemberOf(final String val) {
    final var p = (MemberOf)participant.
            getProperties().
            getProperty(MEMBER_OF);
    try {
      if (p == null) {
        participant.getProperties().add(new MemberOf(val));
      } else if (!val.equals(p.getValue())) {
        p.setValue(val);
        changed();
      }
    } catch (final URISyntaxException e) {
      throw new CalFacadeException(e);
    }
  }

  /** Get the member
   *
   *  @return String     member
   */
  public String getMemberOf() {
    final var p = (MemberOf)participant.getProperties().
                                       getProperty(MEMBER_OF);
    if (p == null) {
      return null;
    }
    return p.getValue();
  }

  /**
   *
   *  @param  val   boolean ExpectReply
   */
  public void setExpectReply(final boolean val) {
    final var p = (ExpectReply)participant.getProperties().
                                          getProperty(EXPECT_REPLY);
    final var sval = String.valueOf(val);
    if (p == null) {
      participant.getProperties().add(new ExpectReply(sval));
    } else if (!sval.equals(p.getValue())) {
      p.setValue(sval);
      changed();
    }
  }

  /**
   *
   *  @return boolean     ExpectReply
   */
  public boolean getExpectReply() {
    final var p = (ExpectReply)participant.getProperties().
                                          getProperty(EXPECT_REPLY);
    if (p == null) {
      return false;
    }
    return Boolean.parseBoolean(p.getValue());
  }

  /* * Set the sentBy
   *
   *  @param  val   String sentBy
   * /
  public void setSentBy(final String val) {
    assignSentByField(sentByIndex, val);
  }

  /* * Get the sentBy
   *
   *  @return String     sentBy
   * /
  public String getSentBy() {
    return fetchSentByField(sentByIndex);
    if (p == null) {
      return null;
    }
    return p.getValue();
  }

  /** Set the email param
   *
   *  @param  val   String email
   */
  public void setEmail(final String val) {
    final var p = participant.getEmail();
    try {
      if (p == null) {
        participant.getProperties().add(new EmailAddress(val));
      } else if (!val.equals(p.getValue())) {
        p.setValue(val);
        changed();
      }
    } catch (final ParseException e) {
      throw new CalFacadeException(e);
    }
  }

  /** Get the email
   *
   *  @return String  email
   */
  public String getEmail() {
    final var p = participant.getEmail();
    if (p == null) {
      return null;
    }
    return p.getValue();
  }

  /** Set the sentBy
   *
   *  @param  val   String sentBy
   * /
  public void setSentByVal(final String val) {
    sentBy = val;
  }

  /** Get the sentBy
   *
   *  @return String     sentBy
   * /
  public String getSentByVal() {
    return sentBy;
    if (p == null) {
      return null;
    }
    return p.getValue();
  }

  /**
   *
   * @param val    scheduling sequence number
   */
  public void setSequence(final int val) {
    final var p = (SchedulingSequence)participant.
            getProperties().
            getProperty(SCHEDULING_SEQUENCE);
    final String sval = String.valueOf(val);
    if (p == null) {
      participant.getProperties().add(new SchedulingSequence(val));
    } else if (!sval.equals(p.getValue())) {
      p.setValue(sval);
      changed();
    }
  }

  /**
   *
   * @return int    the events scheduling sequence
   */
  public int getSequence() {
    final var p = (SchedulingSequence)participant.
            getProperties().
            getProperty(SCHEDULING_SEQUENCE);
    if (p == null) {
      return 0;
    }
    return p.getSequenceNo();
  }

  /**
   * @param val the dtstamp
   */
  public void setSchedulingDtStamp(final String val) {
    final var p = participant.getSchedulingDtStamp();
    try {
      if (p == null) {
        participant.getProperties().add(new SchedulingDtStamp(val));
      } else if (!val.equals(p.getValue())) {
        p.setValue(val);
        changed();
      }
    } catch (final ParseException e) {
      throw new CalFacadeException(e);
    }
  }

  /**
   * @return String datestamp
   */
  public String getSchedulingDtStamp() {
    final var p = participant.getSchedulingDtStamp();
    if (p == null) {
      return null;
    }
    return p.getValue();
  }

  /** Set the schedule agent
   *
   * @param val    schedule agent
   * /
  public void setScheduleAgent(final int val) {
    scheduleAgent = val;
  }

  /** Get the schedule agent
   *
   * @return int    schedule agent
   * /
  public int getScheduleAgent() {
    return scheduleAgent;
  }

  /** Set the schedule status
   *
   * @param val    schedule status
   * /
  public void setScheduleStatus(final String val) {
    scheduleStatus = val;
  }

  /** Get the schedule status
   *
   * @return String    schedule status
   * /
  public String getScheduleStatus() {
    return scheduleStatus;
  }

  /** Set the response - voter only
   *
   *  @param  val response for voter
   * /
  public void setResponse(final int val) {
    assignSentByField(responseIndex, String.valueOf(val));
  }

  /** Get the response
   *
   *  @return int
   * /
  public int getResponse() {
    final String s = fetchSentByField(responseIndex);

    if (s == null) {
      return 0;
    }

    return Integer.parseInt(s);
  }

  /**
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

  /** Copy this objects values into the parameter
1   *
   * @param val to copy
   */
  public void copyTo(final BwParticipant val) {
    val.setCalendarAddress(getCalendarAddress());
    val.setKind(getKind());
    val.setName(getName());
    val.setParticipantType(getParticipantType());
    val.setParticipationStatus(getParticipationStatus());
    val.setDelegatedFrom(getDelegatedFrom());
    val.setDelegatedTo(getDelegatedTo());
    val.setLanguage(getLanguage());
    val.setMemberOf(getMemberOf());
    val.setExpectReply(getExpectReply());
    val.setEmail(getEmail());
//    val.setSentByVal(getSentByVal());
    val.setSequence(getSequence());
    val.setSchedulingDtStamp(getSchedulingDtStamp());
//    val.setScheduleAgent(getScheduleAgent());
//    val.setScheduleStatus(getScheduleStatus());
  }

  /** Only true if something changes the status of, or information about, the
   * attendee.
   *
   * @param val incoming value
   * @return true for significant change
   */
  public boolean changedBy(final BwParticipant val) {
    return changedBy(val, true);
  }

  /** Only true if something changes the status of, or information
   * about, the attendee.
   *
   * @param val incoming value
   * @param checkPartStat - true if we check the partstat
   * @return true for significant change
   */
  public boolean changedBy(final BwParticipant val, final boolean checkPartStat) {
    return ((checkPartStat &&
             (Util.compareStrings(val.getParticipationStatus(),
                                  getParticipationStatus()) != 0))) ||
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
            (Util.compareStrings(val.getEmail(),
                                 getEmail()) != 0);
//           (Util.compareStrings(val.getSentByVal(), getSentByVal()) != 0) ||
  }

  /* ==============================================================
   *                   Differable methods
   * ============================================================== */

  @Override
  public boolean differsFrom(final BwParticipant val) {
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
                                 getEmail()) != 0);
//           (Util.compareStrings(val.getSentByVal(), getSentByVal()) != 0) ||
//           (Util.cmpIntval(val.getScheduleAgent(), getScheduleAgent()) != 0);
  }

  /* ==============================================================
   *                   Object methods
   * ============================================================== */

  @Override
  public int hashCode() {
    final var uid = getUid();
    if (uid != null) {
      return uid.hashCode();
    }
    return getCalendarAddress().hashCode();
  }

  @Override
  public int compareTo(final BwParticipant that)  {
    if (this == that) {
      return 0;
    }

    final var uid = getUid();
    if (uid != null) {
      return uid.compareTo(that.getUid());
    }

    return getCalendarAddress().compareTo(that.getCalendarAddress());
  }

  public String asString() {
    if (stringRepresentation == null) {
      stringRepresentation = participant.toString();
    }
    return stringRepresentation;
  }

  @Override
  public String toString() {
    final ToString ts = new ToString(this);

    toStringSegment(ts);

    return ts.newLine().
             append(asString()).
             toString();
  }

  @Override
  public Object clone() {
    final BwParticipant nobj = new BwParticipant(parent);

    copyTo(nobj);

    return nobj;
  }

  private void changed() {
    parent.markChanged();
    stringRepresentation = null;
  }
}

