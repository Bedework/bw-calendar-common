/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.calfacade;

import org.bedework.util.misc.Util;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.component.Participant;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.ParticipantType;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.bedework.util.calendar.IcalendarUtil.fromBuilder;

/** Handle component participants
 *
 * User: mike Date: 9/10/24 Time: 13:40
 */
public class BwParticipants {
  private final BwEvent parent;

  private Set<BwParticipant> participants;

  private Set<Attendee> attendees;
  private Map<String, Attendee> attendeesMap;

  private boolean changed;

  public BwParticipants(final BwEvent parent) {
    this.parent = parent;
  }

  /**
   *
   * @return unmodifiable set of attendee objects
   */
  public Set<Attendee> getAttendees() {
    return Collections.unmodifiableSet(getAttendeesSet());
  }

  public Set<BwParticipant> getParticipants() {
    return Collections.unmodifiableSet(getParticipantsSet());
  }

  public void addParticipant(final BwParticipant part) {
    getParticipantsSet().add(part);
    markChanged();
  }

  public void removeParticipant(final BwParticipant part) {
    getParticipantsSet().remove(part);
    markChanged();
  }

  public BwParticipant findParticipant(final String calAddr) {
    for (final BwParticipant p: getParticipantsSet()) {
      if (calAddr.equals(p.getCalendarAddress())) {
        return p;
      }
    }
    return null;
  }

  public Set<String> getAttendeeeAddrs() {
    getAttendeesSet(); // Ensure populated
    return attendeesMap.keySet();
  }

  public Attendee findAttendee(final String calAddr) {
    getAttendeesSet(); // Ensure populated
    return attendeesMap.get(calAddr);
  }

  /** The new object is added to the set.
   * getParticipants must be called to get new updated set.
   *
   * @return a new BwParticipant enclosing an ical object with uid
   */
  public BwParticipant newParticipant() {
    final var p = new BwParticipant(this);
    getParticipantsSet().add(p);
    markChanged();
    return p;
  }

  /** The new object is added to the set.
   * getParticipants must be called to get new updated set.
   *
   * @return a new BwParticipant enclosing the ical object
   */
  public BwParticipant newParticipant(final Participant part) {
    final var p = new BwParticipant(this, part);
    getParticipantsSet().add(p);
    markChanged();
    return p;
  }

  public Map<String, BwParticipant> getVoters() {
    final var parts = getParticipants();

    final Map<String, BwParticipant> vals = new HashMap<>();

    if (Util.isEmpty(parts)) {
      return vals;
    }

    for (final var p: parts) {
      if (p.includesParticipantType(ParticipantType.VALUE_VOTER)) {
        vals.put(p.getCalendarAddress(), p);
      }
    }

    return vals;
  }

  public void clearParticipants() {
    participants = new HashSet<>();
    markChanged();
  }

  /** make a participant
   *
   * @param val BwAttendee to build from
   * @return new participant
   */
  public BwParticipant makeParticipant(final BwAttendee val) {
    final BwParticipant part = newParticipant();

    part.setCalendarAddress(val.getAttendeeUri());

    if (val.getRsvp()) {
      part.setExpectReply(true);
    }

    String temp = val.getCn();
    if (temp != null) {
      part.setName(temp);
    }

    //temp = val.getScheduleStatus();
    //if (temp != null) {
    //  pars.add(new ScheduleStatus(temp));
    //}

    temp = val.getCuType();
    if (temp != null) {
      part.setKind(temp);
    }

    temp = val.getEmail();
    if (temp != null) {
      part.setEmail(temp);
    }

    temp = val.getDelegatedFrom();
    if (temp != null) {
      part.setDelegatedFrom(temp);
    }

    temp = val.getDelegatedTo();
    if (temp != null) {
      part.setDelegatedTo(temp);
    }

    //temp = val.getDir();
    //if (temp != null) {
    //  pars.add(new Dir(temp));
    //}

    temp = val.getLanguage();
    if (temp != null) {
      part.setLanguage(temp);
    }

    temp = val.getMember();
    if (temp != null) {
      part.setMemberOf(temp);
    }

    temp = val.getRole();
    if (temp != null) {
      if (temp.equalsIgnoreCase("CHAIR")) {
        part.addParticipantType("CHAIR");
      } else if (temp.equalsIgnoreCase("REQ-PARTICIPANT")) {
        part.addParticipantType("ATTENDEE");
      } else if (temp.equalsIgnoreCase("OPT-PARTICIPANT")) {
        part.addParticipantType("OPTIONAL");
      } else if (temp.equalsIgnoreCase("NON-PARTICIPANT")) {
        part.addParticipantType("INFORMATIONAL");
      } else {
        part.addParticipantType(temp);
      }
    }

    //temp = val.getSentBy();
    //if (temp != null) {
    //  pars.add(new SentBy(temp));
    //}

    temp = val.getPartstat();
    if (temp != null) {
      part.setParticipationStatus(temp);
    }

    return part;
  }

  public void markChanged() {
    changed = true;
    attendees = null;
  }

  public void onSave() {
    /* Ensure x-props reflect state of participants */
    for (final BwAttendee att: parent.getAttendees()) {
      final BwParticipant p = findParticipant(att.getAttendeeUri());
      if ((p != null) &&
              (Util.compareStrings(att.getScheduleStatus(),
                                   p.getScheduleStatus()) != 0)) {
        p.setScheduleStatus(att.getScheduleStatus());
      }
    }

    if (!changed) {
      return;
    }

    final List<BwXproperty> props =
            parent.getXproperties(BwXproperty.bedeworkParticipant);

    if (!Util.isEmpty(props)) {
      for (final BwXproperty p: props) {
        parent.removeXproperty(p);
      }
    }

    for (final BwParticipant p: getParticipants()) {
      final BwXproperty xp =
              new BwXproperty(BwXproperty.bedeworkParticipant,
                              null, p.asString());
      parent.addXproperty(xp);
    }
  }

  private Set<BwParticipant> getParticipantsSet() {
    if (participants == null) {
      participants = new HashSet<>();

      final var xprops =
              parent.getXproperties(BwXproperty.bedeworkParticipant);

      if (Util.isEmpty(xprops)) {
        return participants;
      }

      // Better if ical4j supported sub-component parsing

      final StringBuilder sb = new StringBuilder(
               """
                BEGIN:VCALENDAR
                PRODID://Bedework.org//BedeWork V3.9//EN
                VERSION:2.0
                BEGIN:VEVENT
                UID:0123
                """);

      boolean found = false;
      for (final var xp: xprops) {
        if (!xp.getName().equals(BwXproperty.bedeworkParticipant)) {
          continue;
        }

        found = true;
        sb.append(xp.getValue());
      }

      if (!found) {
        return participants;
      }

      sb.append(
       """
       END:VEVENT
       END:VCALENDAR
       """);

      final Calendar ical = fromBuilder(sb.toString());

      /* Should be one event object */

      final VEvent ev = ical.getComponent(Component.VEVENT);
      for (final Component comp:
              ev.getComponents().getComponents("PARTICIPANT")) {
        participants.add(new BwParticipant(this, (Participant)comp));
      }
    }

    return participants;
  }

  private Set<Attendee> getAttendeesSet() {
    if (attendees != null) {
      return attendees;
    }

    attendees = new HashSet<>();
    attendeesMap = new HashMap<>();

    final var evatts = parent.getAttendees();
    final Map<String, BwAttendee> attMap = new HashMap<>();

    for (final BwAttendee att: evatts) {
      final var addr = att.getAttendeeUri();
      attMap.put(addr, att);
      final var part = findParticipant(addr);

      final var attendee = new Attendee(this, parent, att, part);
      attendees.add(attendee);
      attendeesMap.put(addr, attendee);
    }

    for (final BwParticipant part: getParticipantsSet()) {
      final var addr = part.getCalendarAddress();
      if ((addr != null) ||
              !part.includesParticipantType(ParticipantType.VALUE_ATTENDEE)) {
        continue;
      }

      final var att = attMap.get(addr);
      if (att == null) {
        // No associated participant
        final var attendee = new Attendee(this, parent, null, part);
        attendees.add(attendee);
        attendeesMap.put(addr, attendee);
      } else {
        attMap.remove(addr);
      }
    }

    for (final BwAttendee att: attMap.values()) {
      // Attendee with no participant
      final var attendee = new Attendee(this, parent, att, null);
      attendees.add(attendee);
      attendeesMap.put(att.getAttendeeUri(), attendee);
    }

    return attendees;
  }
}
