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
public class SchedulingInfo {
  private final BwEvent parent;

  /* Manages the owner (scheduling) for the component */
  private SchedulingOwner schedulingOwner;

  private Set<BwParticipant> bwParticipants;

  private Set<Attendee> attendees;
  private Map<String, Attendee> attendeesMap;

  private boolean changed;

  public SchedulingInfo(final BwEvent parent) {
    this.parent = parent;
  }

  public SchedulingOwner getSchedulingOwner() {
    if (schedulingOwner == null) {
      final var owners = getAttendeesWithRole(
              ParticipantType.VALUE_OWNER);
      final BwParticipant powner;
      if (owners.size() == 1) {
        powner = owners.values().iterator().next().getParticipant();
      } else {
        powner = null;
      }
      schedulingOwner = new SchedulingOwner(this,
                                            parent.getOrganizer(),
                                            powner);
    }

    return schedulingOwner;
  }

  public SchedulingOwner copySchedulingOwner(final SchedulingOwner from) {
    final BwOrganizer org;
    if (from.getOrganizer() != null) {
      org = (BwOrganizer)from.getOrganizer().clone();
    } else {
      org = null;
    }

    final BwParticipant powner;
    if (from.getParticipant() != null) {
      powner = (BwParticipant)from.getParticipant().clone();
    } else {
      powner = null;
    }

    parent.setOrganizer(org);
    parent.addParticipant(powner);

    markChanged();
    schedulingOwner = new SchedulingOwner(this,
                                          parent.getOrganizer(),
                                          powner);

    return schedulingOwner;
  }

  /**
   *
   * @return unmodifiable set of attendee objects
   */
  public Set<Attendee> getAttendees() {
    return Collections.unmodifiableSet(getAttendeesSet());
  }

  /**
   *
   * @return number of attendee objects
   */
  public int getNumAttendees() {
    return getAttendeesSet().size();
  }

  public Set<String> getAttendeeeAddrs() {
    getAttendeesSet(); // Ensure populated
    return attendeesMap.keySet();
  }

  public void clearAttendees() {
    parent.getAttendees().clear();
    getAttendeesSet().clear();


    bwParticipants = new HashSet<>();

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
    final var p = newBwParticipant();
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
      removeBwParticipant(part);
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
      getBwParticipantsSet().add(part);
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
      mPart = newBwParticipant();
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

  public void markChanged() {
    changed = true;
    attendees = null;
    bwParticipants = null;
    schedulingOwner = null;
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

    for (final var p: getBwParticipantsSet()) {
      final BwXproperty xp =
              new BwXproperty(BwXproperty.bedeworkParticipant,
                              null, p.asString());
      parent.addXproperty(xp);
    }

    changed = false;
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
        final var part = findBwParticipant(addr);

        final var attendee = new Attendee(this, att, part);
        attendees.add(attendee);
        attendeesMap.put(addr, attendee);
      }
    }

    for (final BwParticipant part: getBwParticipantsSet()) {
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

  private Set<BwParticipant> getBwParticipantsSet() {
    if (bwParticipants == null) {
      bwParticipants = new HashSet<>();

      final var xprops =
              parent.getXproperties(BwXproperty.bedeworkParticipant);

      if (Util.isEmpty(xprops)) {
        return bwParticipants;
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
        return bwParticipants;
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
        bwParticipants.add(new BwParticipant(this, (Participant)comp));
      }
    }

    return bwParticipants;
  }

  private BwParticipant findBwParticipant(final String calAddr) {
    for (final BwParticipant p: getBwParticipantsSet()) {
      if (calAddr.equals(p.getCalendarAddress())) {
        return p;
      }
    }
    return null;
  }

  private void removeBwParticipant(final BwParticipant part) {
    getBwParticipantsSet().remove(part);
    markChanged();
  }

  private BwParticipant newBwParticipant() {
    final var p = new BwParticipant(this);
    getBwParticipantsSet().add(p);
    markChanged();
    return p;
  }
}
