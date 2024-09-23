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

import org.bedework.util.misc.ToString;
import org.bedework.util.misc.Util;

import net.fortuna.ical4j.model.property.ParticipantType;

/** Represent an owning participant (the organizer in iTip terms).
 *
 * These are not stored in the database.
 *
 *  @author Mike Douglass   douglm - bedework.org
 */
public class SchedulingOwner implements Comparable<SchedulingOwner> {
  private final BwParticipants parent;

  /* An owner may be indicated by a Participant object with
     participationType including owner or by a BwOrganizer object only.

      If either is non-null we'll set the values appropriately.
   */
  private final BwOrganizer organizer;

  private final BwParticipant participant;

  /** Constructor
   *
   */
  SchedulingOwner(final BwParticipants parent,
                  final BwOrganizer organizer,
                  final BwParticipant participant) {
    this.parent = parent;
    this.organizer = organizer;
    this.participant = participant;

    if (participant != null) {
      participant.addParticipantType(ParticipantType.VALUE_OWNER);
    }
  }

  /* ==============================================================
   *                      Bean methods
   * ============================================================== */

  public boolean noOwner() {
    return (organizer == null) && (participant == null);
  }

  /**
   *
   * @return organizer object. DO NOT MODIFY scheduling attributes
   * directly
   */
  public BwOrganizer getOrganizer() {
    return organizer;
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
   *  @param  val   calendar address
   */
  public void setCalendarAddress(final String val) {
    if (organizer != null) {
      organizer.setOrganizerUri(val);
    }

    if (participant != null) {
      participant.setCalendarAddress(val);
    }
  }

  /**
   *
   *  @return String     calendar address
   */
  public String getCalendarAddress() {
    if (organizer != null) {
      return organizer.getOrganizerUri();
    }

    if (participant != null) {
      return participant.getCalendarAddress();
    }

    return null;
  }

  /**
   * @param val the dtstamp
   */
  public void setSchedulingDtStamp(final String val) {
    if (organizer != null) {
      organizer.setDtstamp(val);
    }

    if (participant != null) {
      participant.setSchedulingDtStamp(val);
    }
  }

  /**
   * @return String datestamp
   */
  public String getSchedulingDtStamp() {
    if (organizer != null) {
      return organizer.getDtstamp();
    }

    if (participant != null) {
      return participant.getSchedulingDtStamp();
    }

    return null;
  }

  /** Set the schedule status
   *
   * @param val    schedule status
   */
  public void setScheduleStatus(final String val) {
    if (organizer != null) {
      organizer.setScheduleStatus(val);
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
    if (organizer != null) {
      return organizer.getScheduleStatus();
    }

    if (participant != null) {
      return participant.getScheduleStatus();
    }

    return null;
  }

  /* ==============================================================
   *                   Object methods
   * ============================================================== */

  @Override
  public int hashCode() {
    if (organizer != null) {
      return organizer.hashCode();
    }

    if (participant != null) {
      return participant.hashCode();
    }

    return -1;
  }

  @Override
  public int compareTo(final SchedulingOwner that)  {
    if (this == that) {
      return 0;
    }

    final int res = Util.cmpObjval(this.organizer, that.organizer);
    if (res != 0) {
      return res;
    }

    return Util.cmpObjval(this.participant, that.participant);
  }

  @Override
  public String toString() {
    final ToString ts = new ToString(this);

    if (organizer != null) {
      ts.append("organizer", organizer);
    }
    if (participant != null) {
      ts.append("participant", participant);
    }
    return toString();
  }
}

