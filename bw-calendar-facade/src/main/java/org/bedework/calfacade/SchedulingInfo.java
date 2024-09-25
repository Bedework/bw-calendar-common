/* ********************************************************************
    Appropriate copyright notice
*/
package org.bedework.calfacade;

import org.bedework.calfacade.util.ChangeTableEntry;
import org.bedework.util.calendar.PropertyIndex;
import org.bedework.util.misc.Util;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.ParticipantType;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.bedework.util.calendar.IcalDefs.entityTypeVpoll;
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

  private Set<Participant> participants;
  private Map<String, Participant> participantMap;

  private boolean changed;

  public SchedulingInfo(final BwEvent parent) {
    this.parent = parent;
  }

  public SchedulingOwner getSchedulingOwner() {
    if (schedulingOwner == null) {
      final var owners = getParticipantsWithRoles(
              ParticipantType.VALUE_OWNER);
      final BwParticipant powner;
      if (owners.size() == 1) {
        powner = owners.values().iterator().next().getBwParticipant();
      } else {
        powner = null;
      }
      schedulingOwner = new SchedulingOwner(this,
                                            parent.getOrganizer(),
                                            powner);
    }

    return schedulingOwner;
  }

  public SchedulingOwner newSchedulingOwner() {
    final BwParticipant powner;
    if (parent.getEntityType() == entityTypeVpoll) {
      powner = new BwParticipant(this);
    } else {
      powner = null;
    }

    final BwOrganizer organizer;
    if (powner == null) {
      organizer = new BwOrganizer();
    } else {
      organizer = null;
    }
    schedulingOwner = new SchedulingOwner(this,
                                          organizer,
                                          powner);
    markChanged();

    return schedulingOwner;
  }

  public SchedulingOwner newSchedulingOwner(final BwOrganizer organizer,
                                            final BwParticipant powner) {
    schedulingOwner = new SchedulingOwner(this,
                                          organizer,
                                          powner);

    return schedulingOwner;
  }

  public SchedulingOwner copySchedulingOwner(final SchedulingOwner from) {
    final BwOrganizer org;
    final BwParticipant powner;

    if (from.getOrganizer() != null) {
      // Assume no participants.
      org = (BwOrganizer)from.getOrganizer().clone();
      parent.setOrganizer(org);
      schedulingOwner = newSchedulingOwner(org, null);
      return schedulingOwner;
    }

    if (from.getParticipant() == null) {
      // Should not happen
      return null;
    }

    // Look for an existing participant
    final Participant p = findParticipant(from.getCalendarAddress());

    if (p == null) {
      // Just add a new copy with role set.
      powner = (BwParticipant)from.getParticipant().clone();
      powner.addParticipantType(ParticipantType.VALUE_OWNER);
      schedulingOwner = newSchedulingOwner(null, powner);
      return schedulingOwner;
    }

    // Update

    powner = p.getBwParticipant();
    from.getParticipant().copyTo(powner);
    powner.addParticipantType(ParticipantType.VALUE_OWNER);
    schedulingOwner = newSchedulingOwner(null, powner);
    return schedulingOwner;
  }

  /**
   *
   * @return unmodifiable set of all Participant objects
   */
  public Set<Participant> getParticipants() {
    return Collections.unmodifiableSet(getParticipantsSet());
  }

  /**
   *
   * @return number of participant objects
   */
  public int getNumParticipants() {
    return getParticipantsSet().size();
  }

  public Set<String> getParticipantAddrs() {
    getParticipantsSet(); // Ensure populated
    return participantMap.keySet();
  }

  public void clearParticipants() {
    parent.getAttendees().clear();
    getParticipantsSet().clear();


    bwParticipants = new HashSet<>();

    markChanged();
  }

  /**
   *
   * @param participant we want left
   */
  public void setOnlyParticipant(final Participant participant) {
    clearParticipants();
    makeParticipant(participant.getAttendee(), participant.getBwParticipant());
  }

  public Participant findParticipant(final String calAddr) {
    getParticipantsSet(); // Ensure populated
    return participantMap.get(calAddr);
  }

  public Participant makeParticipant() {
    final var p = newBwParticipant();
    final var att = new BwAttendee();
    parent.addAttendee(att);

    final var a = new Participant(this, att, p);
    getParticipantsSet().add(a);
    markChanged();

    return a;
  }

  public void removeParticipant(final Participant val) {
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

    final var part = val.getBwParticipant();
    if (part != null) {
      removeBwParticipant(part);
    }

    getParticipantsSet().remove(val);
    markChanged();
  }

  public Participant addParticipant(final Participant val) {
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

    final var part = val.getBwParticipant();

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

    getParticipantsSet().add(val);
    markChanged();

    return val;
  }

  /** if there is no participant with the same uri then a copy will be
   * added and returned.
   *
   * <p>Otherwise the values in the parameter will be copied to the
   * existing participant.
   *
   * @param val participant to copy
   * @return copied attende
   */
  public Participant copyParticipant(final Participant val) {
    final var ourParticipant = findParticipant(val.getCalendarAddress());
    if (ourParticipant != null) {
      val.copyTo(ourParticipant);
      final var ctab = parent.getChangeset();
      if (ctab != null) {
        final ChangeTableEntry cte = ctab.getEntry(
                PropertyIndex.PropertyInfoIndex.ATTENDEE);
        cte.addChangedValue(ourParticipant.getAttendee());
      }
      return ourParticipant;
    }

    final BwAttendee att;
    final BwParticipant part;

    if (val.getAttendee() != null) {
      att = (BwAttendee)val.getAttendee().clone();
    } else {
      att = null;
    }

    if (val.getBwParticipant() != null) {
      part = (BwParticipant)val.getBwParticipant().clone();
      getBwParticipantsSet().add(part);
    } else {
      part = null;
    }

    return addParticipant(new Participant(this, att, part));
  }

  public Participant makeParticipant(final BwAttendee att,
                                     final BwParticipant part) {
    final var a = new Participant(this, att, part);
    return addParticipant(a);
  }

  /** Create a Participant 'like' the parameter in that, if the
   * param has a BwAttendee then so does the result. Ditto for
   * participant. No values are copied.
   *
   * @param val template Participant
   * @return new Participant
   */
  public Participant makeParticipantLike(final Participant val) {
    final BwAttendee mAtt;
    final BwParticipant mPart;

    if (val.getAttendee() != null) {
      mAtt = new BwAttendee();
      mPart = null;
    } else {
      mPart = newBwParticipant();
      mAtt = null;
    }

    return makeParticipant(mAtt, mPart);
  }

  /** The new object is added to the set.
   * getParticipants must be called to get new updated set.
   *
   * @return a new Participant enclosing the ical object
   */
  public Participant newParticipant(final net.fortuna.ical4j.model.component.Participant part) {
    final var chg = parent.getChangeset();
    final var bwpart =  new BwParticipant(this, part);

    final var participant = new Participant(this, null, bwpart);

    chg.addValue(PropertyIndex.PropertyInfoIndex.XPROP,
                 new BwXproperty(BwXproperty.bedeworkParticipant,
                                 null,
                                 bwpart.asString()));

    return participant;
  }

  public Map<String, Participant> getParticipantsWithRoles(
          final String... roles) {
    final Map<String, Participant> vals = new HashMap<>();

    for (final var p: getParticipants()) {
      for (final var r: roles) {
        if (p.includesParticipantType(r)) {
          vals.put(p.getCalendarAddress(), p);
          break;
        }
      }
    }

    return Collections.unmodifiableMap(vals);
  }

  /**
   *
   * @return unmodifiable set of Participant objects that should
   * receive scheduling messages
   */
  public Map<String, Participant> getRecipientParticipants() {
    return getParticipantsWithRoles(ParticipantType.VALUE_ATTENDEE,
                                    ParticipantType.VALUE_CHAIR,
                                    ParticipantType.VALUE_VOTER);
  }

  public void markChanged() {
    changed = true;
    participants = null;
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

  private Set<Participant> getParticipantsSet() {
    if (participants != null) {
      return participants;
    }

    participants = new HashSet<>();
    participantMap = new HashMap<>();

    final var evatts = parent.getAttendees();
    final Map<String, BwAttendee> attMap = new HashMap<>();

    if (evatts != null) {
      for (final BwAttendee att: evatts) {
        final var addr = att.getAttendeeUri();
        attMap.put(addr, att);
        final var part = findBwParticipant(addr);

        final var participant = new Participant(this, att, part);
        participants.add(participant);
        participantMap.put(addr, participant);
      }
    }

    for (final BwParticipant part: getBwParticipantsSet()) {
      final var addr = part.getCalendarAddress();
      if (addr == null) {
        continue;
      }

      final var att = attMap.get(addr);
      if (att == null) {
        // No associated participant
        final var participant = new Participant(this, null, part);
        participants.add(participant);
        participantMap.put(addr, participant);
      } else {
        attMap.remove(addr);
      }
    }

    for (final BwAttendee att: attMap.values()) {
      // Attendee with no bwparticipant
      final var participant = new Participant(this, att, null);
      participants.add(participant);
      participantMap.put(att.getAttendeeUri(), participant);
    }

    return participants;
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
        bwParticipants.add(new BwParticipant(this, (net.fortuna.ical4j.model.component.Participant)comp));
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
