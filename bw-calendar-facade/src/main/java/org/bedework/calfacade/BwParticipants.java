/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.calfacade;

import org.bedework.calfacade.util.ChangeTableEntry;
import org.bedework.util.calendar.PropertyIndex;
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

/** Handle component participants.
 * Currently, we have the complication of having attendee only,
 * participant only, or both.
 * <br/>
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

  public Set<String> getAttendeeeAddrs() {
    getAttendeesSet(); // Ensure populated
    return attendeesMap.keySet();
  }

  public void clearAttendees() {
    parent.getAttendees().clear();
    getAttendeesSet().clear();


    participants = new HashSet<>();

    markChanged();
  }

  /**
   *
   * @param attendee we want left
   */
  public void setOnlyAttendee(final Attendee attendee) {
    clearAttendees();
    makeAttendee(attendee.getAttendee(), attendee.getParticipant());
  }

  public Attendee findAttendee(final String calAddr) {
    getAttendeesSet(); // Ensure populated
    return attendeesMap.get(calAddr);
  }

  public Attendee makeAttendee() {
    final var p = newParticipant();
    final var att = new BwAttendee();
    parent.addAttendee(att);

    final var a = new Attendee(this, att, p);
    getAttendeesSet().add(a);
    markChanged();

    return a;
  }

  public void removeAttendee(final Attendee val) {
    final var ctab = parent.getChangeset();
    final var att = val.getAttendee();
    if (att != null) {
      if (ctab == null) {
        parent.removeAttendee(att);
      } else {
        ctab.changed(PropertyIndex.PropertyInfoIndex.ATTENDEE,
                     att, null);
      }
    }

    final var part = val.getParticipant();
    if (part != null) {
      removeParticipant(part);
    }

    getAttendeesSet().remove(val);
    markChanged();
  }

  public Attendee addAttendee(final Attendee val) {
    final var evAtt = parent.findAttendee(val.getCalendarAddress());
    final var att = val.getAttendee();
    final var ctab = parent.getChangeset();

    if (att != null) {
      if (ctab == null) {
        parent.addAttendee(val.getAttendee());
      } else {
        final ChangeTableEntry cte = ctab.getEntry(
                PropertyIndex.PropertyInfoIndex.ATTENDEE);
        if (evAtt != null) {
          cte.addChangedValue(att);
        } else {
          cte.addAddedValue(att);
        }
      }
    }

    final var part = val.getParticipant();

    if (part != null) {
      final var xpart = new BwXproperty(BwXproperty.bedeworkParticipant, null, part.asString());
      if (ctab == null) {
        parent.addXproperty(xpart);
      } else {
        final ChangeTableEntry cte = ctab.getEntry(
                PropertyIndex.PropertyInfoIndex.XPROP);
        cte.addAddedValue(xpart);
      }
    }

    getAttendeesSet().add(val);
    markChanged();

    return val;
  }

  /** if there is no attendee with the same uri then a copy will be
   * added and returned.
   *
   * <p>Otherwise the values in the parameter will be copied to the
   * existing attendee.
   *
   * @param val attendee to copy
   * @return copied attende
   */
  public Attendee copyAttendee(final Attendee val) {
    final var ourAttendee = findAttendee(val.getCalendarAddress());
    if (ourAttendee != null) {
      val.copyTo(ourAttendee);
      final var ctab = parent.getChangeset();
      if (ctab != null) {
        final ChangeTableEntry cte = ctab.getEntry(
                PropertyIndex.PropertyInfoIndex.ATTENDEE);
        cte.addChangedValue(ourAttendee.getAttendee());
      }
      return ourAttendee;
    }

    final BwAttendee att;
    final BwParticipant part;

    if (val.getAttendee() != null) {
      att = (BwAttendee)val.getAttendee().clone();
    } else {
      att = null;
    }

    if (val.getParticipant() != null) {
      part = (BwParticipant)val.getParticipant().clone();
      getParticipantsSet().add(part);
    } else {
      part = null;
    }

    return addAttendee(new Attendee(this, att, part));
  }

  public Attendee makeAttendee(final BwAttendee att,
                               final BwParticipant part) {
    final var a = new Attendee(this, att, part);
    return addAttendee(a);
  }

  /** Create an Attendee 'like' the parameter in that, if the
   * param has a BwAttendee then so does the result. Ditto for
   * participant. No values are copied.
   *
   * @param val template Attendee
   * @return new Attendee
   */
  public Attendee makeAttendeeLike(final Attendee val) {
    final BwAttendee mAtt;
    final BwParticipant mPart;

    if (val.getAttendee() != null) {
      mAtt = new BwAttendee();
      mPart = null;
    } else {
      mPart = newParticipant();
      mAtt = null;
    }

    return makeAttendee(mAtt, mPart);
  }

  /** The new object is added to the set.
   * getAttendees must be called to get new updated set.
   *
   * @return a new Attendee enclosing the ical object
   */
  public Attendee newAttendee(final Participant part) {
    final var chg = parent.getChangeset();
    final var bwpart =  new BwParticipant(this, part);

    final var att = new Attendee(this, null, bwpart);

    chg.addValue(PropertyIndex.PropertyInfoIndex.XPROP,
                 new BwXproperty(BwXproperty.bedeworkParticipant,
                                 null,
                                 bwpart.asString()));

    return att;
  }

  public Map<String, Attendee> getAttendeesWithRole(final String role) {
    final Map<String, Attendee> vals = new HashMap<>();

    for (final var a: getAttendees()) {
      if (a.includesParticipantType(role)) {
        vals.put(a.getCalendarAddress(), a);
      }
    }

    return vals;
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

    for (final var p: getParticipants()) {
      final BwXproperty xp =
              new BwXproperty(BwXproperty.bedeworkParticipant,
                              null, p.asString());
      parent.addXproperty(xp);
    }

    changed = false;
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

    if (evatts != null) {
      for (final BwAttendee att: evatts) {
        final var addr = att.getAttendeeUri();
        attMap.put(addr, att);
        final var part = findParticipant(addr);

        final var attendee = new Attendee(this, att, part);
        attendees.add(attendee);
        attendeesMap.put(addr, attendee);
      }
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
        final var attendee = new Attendee(this, null, part);
        attendees.add(attendee);
        attendeesMap.put(addr, attendee);
      } else {
        attMap.remove(addr);
      }
    }

    for (final BwAttendee att: attMap.values()) {
      // Attendee with no participant
      final var attendee = new Attendee(this, att, null);
      attendees.add(attendee);
      attendeesMap.put(att.getAttendeeUri(), attendee);
    }

    return attendees;
  }

  private BwParticipant findParticipant(final String calAddr) {
    for (final BwParticipant p: getParticipantsSet()) {
      if (calAddr.equals(p.getCalendarAddress())) {
        return p;
      }
    }
    return null;
  }

  private void removeParticipant(final BwParticipant part) {
    getParticipantsSet().remove(part);
    markChanged();
  }

  private BwParticipant newParticipant() {
    final var p = new BwParticipant(this);
    getParticipantsSet().add(p);
    markChanged();
    return p;
  }
}
