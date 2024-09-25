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
import net.fortuna.ical4j.model.component.Vote;
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
import net.fortuna.ical4j.model.property.SchedulingAgent;
import net.fortuna.ical4j.model.property.SchedulingDtStamp;
import net.fortuna.ical4j.model.property.SchedulingSequence;
import net.fortuna.ical4j.model.property.SchedulingStatus;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.fortuna.ical4j.model.Component.VOTE;
import static net.fortuna.ical4j.model.Property.EXPECT_REPLY;
import static net.fortuna.ical4j.model.Property.INVITED_BY;
import static net.fortuna.ical4j.model.Property.LANG;
import static net.fortuna.ical4j.model.Property.MEMBER_OF;
import static net.fortuna.ical4j.model.Property.PARTICIPATION_DELEGATED_FROM;
import static net.fortuna.ical4j.model.Property.PARTICIPATION_DELEGATED_TO;
import static net.fortuna.ical4j.model.Property.SCHEDULING_AGENT;
import static net.fortuna.ical4j.model.Property.SCHEDULING_SEQUENCE;
import static net.fortuna.ical4j.model.Property.SCHEDULING_STATUS;

/** Represent a participant component.
 *
 * These are not currently stored in the database but as x-properties.
 *
 *  @author Mike Douglass   douglm - bedework.org
 */
public class BwParticipant extends BwDbentity<BwParticipant>
         implements BwCloneable, Differable<BwParticipant> {
  private final SchedulingInfo parent;

  // Derived from the participant object.
  private String stringRepresentation;

  private final Participant participant;

  /** Constructor
   *
   */
  BwParticipant(final SchedulingInfo parent) {
    this.parent = parent;
    participant = new Participant();
    participant.getProperties().add(
            new net.fortuna.ical4j.model.property.Uid(Uid.getUid()));
  }

  /** Constructor
   *
   */
  BwParticipant(final SchedulingInfo parent,
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
    final var props = participant.getProperties();
    final var p = participant.getCalendarAddress();

    if (val == null) {
      if (p != null) {
        props.remove(p);
        changed();
      }
      return;
    }

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
    final var props = participant.getProperties();
    final var p = participant.getNameProperty();

    if (val == null) {
      if (p != null) {
        props.remove(p);
        changed();
      }
      return;
    }

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
    final var props = participant.getProperties();
    final var p = participant.getDescription();

    if (val == null) {
      if (p != null) {
        props.remove(p);
        changed();
      }
      return;
    }

    if (p == null) {
      props.add(new Description(val));
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
    final var props = participant.getProperties();
    final var p = participant.getKind();

    if (val == null) {
      if (p != null) {
        props.remove(p);
        changed();
      }
      return;
    }
    if (p == null) {
      props.add(new Kind(val));
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
    final var props = participant.getProperties();
    final var p = participant.getParticipantType();

    if (val == null) {
      if (p != null) {
        props.remove(p);
        changed();
      }
      return;
    }

    if (p == null) {
      props.add(new ParticipantType(val));
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
    final var props = participant.getProperties();
    final var p = participant.getParticipationStatus();

    if (val == null) {
      if (p != null) {
        props.remove(p);
        changed();
        return;
      }
    } else if (p == null) {
      props.add(new ParticipationStatus(val));
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
    final var props = participant.getProperties();
    final var p = (ParticipationDelegatedFrom)props
            .getProperty(PARTICIPATION_DELEGATED_FROM);

    if (val == null) {
      if (p != null) {
        props.remove(p);
        changed();
      }
      return;
    }

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
    final var props = participant.getProperties();
    final var p = (ParticipationDelegatedTo)props
            .getProperty(PARTICIPATION_DELEGATED_TO);

    if (val == null) {
      if (p != null) {
        props.remove(p);
        changed();
      }
      return;
    }

    try {
      if (p == null) {
        props.add(new ParticipationDelegatedTo(val));
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
    final var props = participant.getProperties();
    final var p = (Lang)props.getProperty(LANG);

    if (val == null) {
      if (p != null) {
        props.remove(p);
        changed();
      }
      return;
    }

    if (p == null) {
      props.add(new Lang(val));
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
    final var props = participant.getProperties();
    final var p = (MemberOf)props.getProperty(MEMBER_OF);

    if (val == null) {
      if (p != null) {
        props.remove(p);
        changed();
      }
      return;
    }

    try {
      if (p == null) {
        props.add(new MemberOf(val));
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
    final var props = participant.getProperties();
    final var p = (ExpectReply)props.getProperty(EXPECT_REPLY);
    final var sval = String.valueOf(val);

    if (p == null) {
      if (val) {
        props.add(new ExpectReply(sval));
      }
    } else if (!sval.equals(p.getValue())) {
      if (!val) {
        props.remove(p);
      } else {
        p.setValue(sval);
      }
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
    final var props = participant.getProperties();
    final var p = participant.getEmail();

    if (val == null) {
      if (p != null) {
        props.remove(p);
        changed();
      }
      return;
    }

    try {
      if (p == null) {
        props.add(new EmailAddress(val));
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

  /**
   *
   *  @param  val   String invitedBy
   */
  public void setInvitedBy(final String val) {
    final var props = participant.getProperties();
    final var p = (SchedulingSequence)props.getProperty(INVITED_BY);

    if (val == null) {
      if (p != null) {
        props.remove(p);
        changed();
      }
      return;
    }

    if (p == null) {
      props.add(new Lang(val));
    } else if (!val.equals(p.getValue())) {
      p.setValue(val);
      changed();
    }
  }

  /**
   *
   *  @return String     invitedBy
   */
  public String getInvitedBy() {
    final var p = (Lang)participant.
            getProperties().
            getProperty(INVITED_BY);
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
    final var props = participant.getProperties();
    final var p = (SchedulingSequence)props.getProperty(SCHEDULING_SEQUENCE);
    final String sval = String.valueOf(val);

    if (p == null) {
      if (val != 0) {
        props.add(new SchedulingSequence(val));
      }
    } else if (!sval.equals(p.getValue())) {
      if (val == 0) {
        props.remove(p);
      } else {
        p.setValue(sval);
      }
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
    final var props = participant.getProperties();
    final var p = participant.getSchedulingDtStamp();

    if (val == null) {
      if (p != null) {
        props.remove(p);
        changed();
      }
      return;
    }

    try {
      if (p == null) {
        props.add(new SchedulingDtStamp(val));
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

  /**
   *
   * @param val    schedule agent
   */
  public void setScheduleAgent(final String val) {
    final var props = participant.getProperties();
    final var p = (SchedulingAgent)props.getProperty(SCHEDULING_AGENT);

    if (val == null) {
      if (p != null) {
        props.remove(p);
        changed();
      }
      return;
    }

    if (p == null) {
      props.add(new SchedulingAgent(val));
    } else if (!val.equals(p.getValue())) {
      p.setValue(val);
      changed();
    }
  }

  /** Get the schedule agent
   *
   * @return String   schedule agent
   */
  public String getScheduleAgent() {
    final var p = (SchedulingAgent)participant.
            getProperties().
            getProperty(SCHEDULING_AGENT);
    if (p == null) {
      return null;
    }
    return p.getValue();
  }

  /** Set the schedule status
   *
   * @param val    schedule status
   */
  public void setScheduleStatus(final String val) {
    final var props = participant.getProperties();
    final var p = (SchedulingStatus)props.getProperty(SCHEDULING_STATUS);

    if (val == null) {
      if (p != null) {
        props.remove(p);
        changed();
      }
      return;
    }

    if (p == null) {
      props.add(new SchedulingStatus(val));
    } else if (!val.equals(p.getValue())) {
      p.setValue(val);
      changed();
    }
  }

  /**
   *
   * @return String    schedule status
   */
  public String getScheduleStatus() {
    final var p = (SchedulingStatus)participant.
            getProperties().
            getProperty(SCHEDULING_STATUS);
    if (p == null) {
      return null;
    }
    return p.getValue();
  }

  public List<BwVote> getVotes() {
    final var comps = participant.getComponents();
    final var c = comps.getComponents(VOTE);
    final var votes = new ArrayList<BwVote>();
    for (final var cv: c) {
      votes.add(new BwVote(this, (Vote)cv));
    }

    return votes;
  }

  public void setVotes(final List<BwVote> val) {
    final var comps = participant.getComponents();
    final var c = comps.getComponents(VOTE);
    comps.removeAll(c);

    for (final var v: val) {
      comps.add(v.getVote());
    }
  }

  /** Add the response - voter only
   *
   *  @param  val response for voter
   */
  public void addVote(final BwVote val) {
    final var comps = participant.getComponents();
    final var c = comps.getComponents(VOTE);
    final int id = val.getPollItemId();

    BwVote vote = null;
    for (final var cv: c) {
      final var bwv = new BwVote(this, (Vote)cv);
      if (id == bwv.getPollItemId()) {
        vote = bwv;
        break;
      }
    }
    if (vote == null) {
      comps.add(val.getVote());
      changed();

      return;
    }

    // Same id - set response.
    vote.setResponse(val.getResponse());
    changed();
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
    val.setInvitedBy(getInvitedBy());
    val.setSequence(getSequence());
    val.setSchedulingDtStamp(getSchedulingDtStamp());
    val.setScheduleAgent(getScheduleAgent());
    val.setScheduleStatus(getScheduleStatus());
    val.setVotes(getVotes());
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
                                getEmail()) != 0) ||
           (Util.compareStrings(val.getInvitedBy(),
                                getInvitedBy()) != 0) ||
           (Util.cmpObjval(val.getVotes(), getVotes()) != 0);
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
                                 getEmail()) != 0) ||
           (Util.compareStrings(val.getInvitedBy(),
                                getInvitedBy()) != 0) ||
           (Util.compareStrings(val.getScheduleAgent(), getScheduleAgent()) != 0) ||
            (Util.cmpObjval(val.getVotes(),
                            getVotes()) != 0);
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

  void changed() {
    parent.markChanged();
    stringRepresentation = null;
  }
}

