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
import org.bedework.calfacade.util.FieldSplitter;
import org.bedework.util.calendar.IcalDefs;
import org.bedework.base.ToString;
import org.bedework.util.misc.Util;

/** Represent an attendee. An attendee entry is associated with a single event
 * and gives the participation status of the attendee for that event.
 *
 * <p>For 3.10 the sentBy column is used to store the stay-informed,
 * response and sent-by parameters. If the first char is not ":" or "\t"
 * it's all sentBy</p>
 *
 * <p>If it is ":" - it has form<ul>
 *    <li>":"</li>
 *    <li>"F" or "T"</li>
 *    <li>int string value</li>
 *    <li>":"</li>
 *    <li>Remainder is sent-by</li>
 *    </ul>
 *
 *  <p>Additionally we assumed that the presence of the ":" indicated
 *  a voter.
 *
 *  <p>If it starts with "\t" it's using the FieldSplitter
 *
 *  @author Mike Douglass   douglm - rpi.edu
 */
public class BwAttendee extends BwDbentity<BwAttendee>
         implements BwCloneable, Differable<BwAttendee> {
  /** The default type of entity */
  public final static int typeAttendee = 0;

  /** Could be a voter */
  public final static int typeVoter = 1;

  public static final String fieldDelimiter = "\t";
  // Index starts at 1 to ensure leading delimiter
  private static final int stayInformedIndex = 1;
  private static final int responseIndex = 2;
  private static final int typeIndex = 3;
  private static final int emailIndex = 4;
  private static final int sentByIndex = 5;

  private FieldSplitter sentBySplit;

  /* Params fields */

  private String cn;
  private String cuType;
  private String delegatedFrom;
  private String delegatedTo;
  private String dir;
  private String language;
  private String member;
  private String sentBy;
  private boolean rsvp;
  private String role;

  /*     partstatparam      = "PARTSTAT" "="
                         ("NEEDS-ACTION"        ; Event needs action
                        / "ACCEPTED"            ; Event accepted
                        / "DECLINED"            ; Event declined
                        / "TENTATIVE"           ; Event tentatively
                                                ; accepted
                        / "DELEGATED"           ; Event delegated
                        / x-name                ; Experimental status
                        / iana-token)           ; Other IANA registered
                                                ; status
     ; These are the participation statuses for a "VEVENT". Default is
     ; NEEDS-ACTION
     partstatparam      /= "PARTSTAT" "="
                         ("NEEDS-ACTION"        ; To-do needs action
                        / "ACCEPTED"            ; To-do accepted
                        / "DECLINED"            ; To-do declined
                        / "TENTATIVE"           ; To-do tentatively
                                                ; accepted
                        / "DELEGATED"           ; To-do delegated
                        / "COMPLETED"           ; To-do completed.
                                                ; COMPLETED property has
                                                ;date/time completed.
                        / "IN-PROCESS"          ; To-do in process of
                                                ; being completed
                        / x-name                ; Experimental status
                        / iana-token)           ; Other IANA registered
                                                ; status
     ; These are the participation statuses for a "VTODO". Default is
     ; NEEDS-ACTION

     partstatparam      /= "PARTSTAT" "="
                         ("NEEDS-ACTION"        ; Journal needs action
                        / "ACCEPTED"            ; Journal accepted
                        / "DECLINED"            ; Journal declined
                        / x-name                ; Experimental status
                        / iana-token)           ; Other IANA registered
                                                ; status
     ; These are the participation statuses for a "VJOURNAL". Default is
     ; NEEDS-ACTION
   */
  private String partstat = IcalDefs.partstatValNeedsAction;

  // ENUM
  /* The uri */
  private String attendeeUri;

  /** RFC sequence value - needed to preserve last sequence # we saw from this
   * attendee
   */
  private int sequence;

  /** UTC datetime as specified in rfc - from replies */
  private String dtstamp;

  /* CalDAV implicit scheduling parameters */
  private int scheduleAgent;

  private String scheduleStatus;

  /* Non-db values */

  /** Constructor
   *
   */
  public BwAttendee() {
  }

  /* ====================================================================
   *                      Bean methods
   * ==================================================================== */

  /** Set the type
   *
   *  @param  val the type
   */
  public void setType(final int val) {
    assignSentByField(typeIndex, String.valueOf(val));
  }

  /** Get the type - attendee/voter
   *
   *  @return int
   */
  public int getType() {
    final String s = fetchSentByField(typeIndex);

    if (s == null) {
      return typeAttendee;
    }

    return Integer.parseInt(s);
  }

  /** Set the cn
   *
   *  @param  val   String cn
   */
  public void setCn(final String val) {
    cn = val;
  }

  /** Get the cn
   *
   *  @return String     cn
   */
  public String getCn() {
    return cn;
  }

  /** Set the cuType
   *
   *  @param  val   String cuType
   */
  public void setCuType(final String val) {
    cuType = val;
  }

  /** Get the cuType
   *
   *  @return String     cuType
   */
  public String getCuType() {
    return cuType;
  }

  /** Set the delegatedFrom
   *
   *  @param  val   String delegatedFrom
   */
  public void setDelegatedFrom(final String val) {
    delegatedFrom = val;
  }

  /** Get the delegatedFrom
   *
   *  @return String     delegatedFrom
   */
  public String getDelegatedFrom() {
    return delegatedFrom;
  }

  /** Set the delegatedTo
   *
   *  @param  val   String delegatedTo
   */
  public void setDelegatedTo(final String val) {
    delegatedTo = val;
  }

  /** Get the delegatedTo
   *
   *  @return String     delegatedTo
   */
  public String getDelegatedTo() {
    return delegatedTo;
  }

  /** Set the dir
   *
   *  @param  val   String dir
   */
  public void setDir(final String val) {
    dir = val;
  }

  /** Get the dir
   *
   *  @return String     dir
   */
  public String getDir() {
    return dir;
  }

  /** Set the language
   *
   *  @param  val   String language
   */
  public void setLanguage(final String val) {
    language = val;
  }

  /** Get the language
   *
   *  @return String     language
   */
  public String getLanguage() {
    return language;
  }

  /** Set the member
   *
   *  @param  val   String member
   */
  public void setMember(final String val) {
    member = val;
  }

  /** Get the member
   *
   *  @return String     member
   */
  public String getMember() {
    return member;
  }

  /** Set the rsvp
   *
   *  @param  val   boolean rsvp
   */
  public void setRsvp(final boolean val) {
    rsvp = val;
  }

  /** Get the rsvp
   *
   *  @return boolean     rsvp
   */
  public boolean getRsvp() {
    return rsvp;
  }

  /** Set the role
   *
   *  @param  val   String role
   */
  public void setRole(final String val) {
    role = val;
  }

  /** Get the role
   *
   *  @return String     role
   */
  public String getRole() {
    return role;
  }

  /** Set the partstat
   *
   *  @param  val   String partstat
   */
  public void setPartstat(final String val) {
    partstat = val;
  }

  /** Get the partstat
   *
   *  @return String     partstat
   */
  public String getPartstat() {
    return partstat;
  }

  /** Set the sentBy
   *
   *  @param  val   String sentBy
   */
  public void setSentBy(final String val) {
    assignSentByField(sentByIndex, val);
  }

  /** Get the sentBy
   *
   *  @return String     sentBy
   */
  public String getSentBy() {
    return fetchSentByField(sentByIndex);
  }

  /** Set the email param
   *
   *  @param  val   String email
   */
  public void setEmail(final String val) {
    assignSentByField(emailIndex, val);
  }

  /** Get the email
   *
   *  @return String  email
   */
  public String getEmail() {
    return fetchSentByField(emailIndex);
  }

  /** Set the sentBy
   *
   *  @param  val   String sentBy
   */
  public void setSentByVal(final String val) {
    sentBy = val;
  }

  /** Get the sentBy
   *
   *  @return String     sentBy
   */
  public String getSentByVal() {
    return sentBy;
  }

  /** Set the attendeeUri
   *
   *  @param  val   String attendeeUri
   */
  public void setAttendeeUri(final String val) {
    attendeeUri = val;
  }

  /** Get the attendeeUri
   *
   *  @return String     attendeeUri
   */
  public String getAttendeeUri() {
    return attendeeUri;
  }

  /** Set the rfc sequence for this event
   *
   * @param val    rfc sequence number
   */
  public void setSequence(final int val) {
    sequence = val;
  }

  /** Get the events rfc sequence
   *
   * @return int    the events rfc sequence
   */
  public int getSequence() {
    return sequence;
  }

  /**
   * @param val the dtstamp
   */
  public void setDtstamp(final String val) {
    dtstamp = val;
  }

  /**
   * @return String datestamp
   */
  public String getDtstamp() {
    return dtstamp;
  }

  /** Set the schedule agent
   *
   * @param val    schedule agent
   */
  public void setScheduleAgent(final int val) {
    scheduleAgent = val;
  }

  /** Get the schedule agent
   *
   * @return int    schedule agent
   */
  public int getScheduleAgent() {
    return scheduleAgent;
  }

  /** Set the schedule status
   *
   * @param val    schedule status
   */
  public void setScheduleStatus(final String val) {
    scheduleStatus = val;
  }

  /** Get the schedule status
   *
   * @return String    schedule status
   */
  public String getScheduleStatus() {
    return scheduleStatus;
  }

  /** Set the response - voter only
   *
   *  @param  val response for voter
   */
  public void setResponse(final int val) {
    assignSentByField(responseIndex, String.valueOf(val));
  }

  /** Get the response
   *
   *  @return int
   */
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
   */
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
   */
  public boolean getStayInformed() {
    return "T".equals(fetchSentByField(stayInformedIndex));
  }

  /** Copy this objects values into the parameter
1   *
   * @param val to copy
   */
  public void copyTo(final BwAttendee val) {
    val.setCn(getCn());
    val.setCuType(getCuType());
    val.setDelegatedFrom(getDelegatedFrom());
    val.setDelegatedTo(getDelegatedTo());
    val.setDir(getDir());
    val.setLanguage(getLanguage());
    val.setMember(getMember());
    val.setRsvp(getRsvp());
    val.setRole(getRole());
    val.setPartstat(getPartstat());
    val.setSentByVal(getSentByVal());
    val.setAttendeeUri(getAttendeeUri());
    val.setSequence(getSequence());
    val.setScheduleAgent(getScheduleAgent());
    val.setScheduleStatus(getScheduleStatus());
    val.setDtstamp(getDtstamp());
  }

  /** Only true if something changes the status of, or information about, the
   * attendee.
   *
   * @param val incoming value
   * @return true for significant change
   */
  public boolean changedBy(final BwAttendee val) {
    return changedBy(val, true);
  }

  /** Only true if something changes the status of, or information about, the
   * attendee.
   *
   * @param val incoming value
   * @param checkPartStat - true if we check the partstat
   * @return true for significant change
   */
  public boolean changedBy(final BwAttendee val, final boolean checkPartStat) {
    return ((checkPartStat &&
             (Util.compareStrings(val.getPartstat(), getPartstat()) != 0))) ||
           (Util.compareStrings(val.getCn(), getCn()) != 0) ||
           (Util.compareStrings(val.getCuType(), getCuType()) != 0) ||
           (Util.compareStrings(val.getDelegatedFrom(), getDelegatedFrom()) != 0) ||
           (Util.compareStrings(val.getDelegatedTo(), getDelegatedTo()) != 0) ||
           (Util.compareStrings(val.getDir(), getDir()) != 0) ||
           (Util.compareStrings(val.getLanguage(), getLanguage()) != 0) ||
           (Util.compareStrings(val.getMember(), getMember()) != 0) ||
           (Util.compareStrings(val.getRole(), getRole()) != 0) ||
           (Util.compareStrings(val.getSentByVal(), getSentByVal()) != 0) ||
           (Util.compareStrings(val.getAttendeeUri(), getAttendeeUri()) != 0);
  }

  private void assignSentByField(final int index, final String val) {
    fixSentBy();
    storeSentByField(index, val);
  }

  private String fetchSentByField(final int index) {
    fixSentBy();
    return fetchSentBySplit().getFld(index);
  }

  private void fixSentBy() {
    final String sb = getSentByVal();

    if ((sb == null) || sb.startsWith(fieldDelimiter)) {
      return;
    }

    if (sb.startsWith(":")) {
      // Old packed form - type voter
      final var fs = sb.split(":");

      fixSentByField(fs, 1, stayInformedIndex);
      fixSentByField(fs, 2, responseIndex);
      fixSentByField(fs, 3, sentByIndex);
      storeSentByField(typeIndex, String.valueOf(typeVoter));
    } else {
      // Unpacked sentBy

      storeSentByField(sentByIndex, sb);
    }
  }

  private void fixSentByField(final String[] fields,
                              final int oldIndex,
                              final int splitterIndex) {
    if (oldIndex >= fields.length) {
      return;
    }

    final var f = fields[oldIndex];
    if ((f == null) || (f.length() == 0)) {
      return;
    }

    storeSentByField(splitterIndex, f);
  }

  private void storeSentByField(final int index, final String val) {
    fetchSentBySplit().setFld(index, val);
    setSentByVal(fetchSentBySplit().getCombined());
  }

  private FieldSplitter fetchSentBySplit() {
    if (sentBySplit == null) {
      sentBySplit = new FieldSplitter(fieldDelimiter);
      sentBySplit.setVal(getSentByVal());
    }

    return sentBySplit;
  }

  /* ====================================================================
   *                   Differable methods
   * ==================================================================== */

  @Override
  public boolean differsFrom(final BwAttendee val) {
    return (Util.compareStrings(val.getPartstat(), getPartstat()) != 0) ||
           (Util.compareStrings(val.getCn(), getCn()) != 0) ||
           (Util.compareStrings(val.getCuType(), getCuType()) != 0) ||
           (Util.compareStrings(val.getDelegatedFrom(), getDelegatedFrom()) != 0) ||
           (Util.compareStrings(val.getDelegatedTo(), getDelegatedTo()) != 0) ||
           (Util.compareStrings(val.getDir(), getDir()) != 0) ||
           (Util.compareStrings(val.getLanguage(), getLanguage()) != 0) ||
           (Util.compareStrings(val.getMember(), getMember()) != 0) ||
           (Util.cmpBoolval(val.getRsvp(), getRsvp()) != 0) ||
           (Util.compareStrings(val.getRole(), getRole()) != 0) ||
           (Util.compareStrings(val.getSentByVal(), getSentByVal()) != 0) ||
           (Util.compareStrings(val.getAttendeeUri(), getAttendeeUri()) != 0) ||
           (Util.cmpIntval(val.getScheduleAgent(), getScheduleAgent()) != 0);
  }

  /* ====================================================================
   *                   Object methods
   * ==================================================================== */

  @Override
  public int hashCode() {
    return getAttendeeUri().hashCode();
  }

  @Override
  public int compareTo(final BwAttendee that)  {
    if (this == that) {
      return 0;
    }

    return getAttendeeUri().compareTo(that.getAttendeeUri());
  }

  @Override
  public String toString() {
    final ToString ts = new ToString(this);

    toStringSegment(ts);

    ts.append("type", getType());
    ts.append("cn", getCn());
    ts.append("cuType", getCuType());
    ts.append("delegatedFrom", getDelegatedFrom());
    ts.append("delegatedTo", getDelegatedTo());

    ts.newLine();
    ts.append("dir", getDir());
    ts.append("language", getLanguage());
    ts.append("member", getMember());
    ts.append("rsvp", getRsvp());

    ts.newLine();
    ts.append("role", getRole());
    ts.append("partstat", getPartstat());
    ts.append("sentBy", getSentBy());
    ts.append("attendeeUri", getAttendeeUri());

    ts.newLine();
    ts.append("sequence", getSequence());
    ts.append("dtstamp", getDtstamp());

    ts.newLine();
    ts.append("scheduleAgent", getScheduleAgent());
    ts.append("scheduleStatus", getScheduleStatus());

    if (getType() == typeVoter) {
      ts.newLine();
      ts.append("response", getResponse());
      ts.append("stayInformed", getStayInformed());
    }

    return ts.toString();
  }

  @Override
  public Object clone() {
    final BwAttendee nobj = new BwAttendee();

    copyTo(nobj);

    return nobj;
  }
}

