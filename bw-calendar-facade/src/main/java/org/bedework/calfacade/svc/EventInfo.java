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
package org.bedework.calfacade.svc;

import org.bedework.access.CurrentAccess;
import org.bedework.calfacade.BwAlarm;
import org.bedework.calfacade.BwAttendee;
import org.bedework.calfacade.BwDateTime;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwEventAnnotation;
import org.bedework.calfacade.BwEventProxy;
import org.bedework.calfacade.BwParticipant;
import org.bedework.calfacade.BwXproperty;
import org.bedework.calfacade.ScheduleResult;
import org.bedework.calfacade.SchedulingOwner;
import org.bedework.calfacade.base.BwUnversionedDbentity;
import org.bedework.calfacade.util.ChangeTable;
import org.bedework.util.calendar.PropertyIndex.PropertyInfoIndex;
import org.bedework.base.ToString;
import org.bedework.util.misc.Util;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/** This class provides information about an event for a specific user and
 * session.
 *
 * @author Mike Douglass       douglm  rpi.edu
 */
public class EventInfo extends BwUnversionedDbentity<EventInfo>
      implements Comparable<EventInfo>, Comparator<EventInfo>, Serializable {
  /** This class allows add and update event to signal changes back to the
   * caller.
   */
  public static class UpdateResult extends ScheduleResult<UpdateResult> {
    /** False if the update method(s) could find no changes */
    public boolean hasChanged;

    /** True if the entity was scheduled */
    public boolean wasScheduled;

    /** False if the sequence doesn't need updating */
    public boolean sequenceChange;

    /** true if we need to reschedule after add/update
     * (Handled by add/update)
     */
    public boolean doReschedule;

    /** true for adding event, false for updating */
    public boolean adding;
    /** true for deleting event */
    public boolean deleting;

    /** True for attendee replying */
    public boolean reply;

    /** */
    public int locationsAdded;
    /** */
    public int locationsRemoved;

    /** */
    public int contactsAdded;
    /** */
    public int sponsorsRemoved;

    /** */
    public int categoriesAdded;
    /** */
    public int categoriesRemoved;

    /** null or overrides that didn't get added */
    public Collection<BwEventProxy> failedOverrides;

    /** Recurrence ids of deleted instances */
    public List<String> deletedInstances;

    /** */
    public Collection<BwAttendee> deletedAttendees;

    /** */
    public Collection<BwParticipant> deletedParticipants;

    /** The attendee who was responding
     */
    public String fromAttUri;

    /** Marked entity */
    public Integer pollWinner;
  }

  private EventInfo retrievedEvent;

  private EventInfo parent;

  protected BwEvent event;

  /** editable is set at retrieval to indicate an event owned by the current
   * user. This only has significance for the personal calendar.
   */
  protected boolean editable;

  protected boolean fromRef;

  /**
   * Use this to mark an override we saw in the ical.
   */
  public boolean recurrenceSeen;

  /* ENUM
   * XXX these need changing
   */

  /** actual event entry */
  public final static int kindEntry = 0;
  /** 'added' event - from a reference */
  public final static int kindAdded = 1;
  /** from a subscription */
  public final static int kindUndeletable = 2;

  private int kind;

  private static final String[] kindStr = {
    "entry",
    "reffed",
    "subscribed",
  };

  private boolean newEvent;

  /* True if we were only sent the instance. Don't delete other overrides. */
  private boolean instanceOnly;

  private String prevStag;

  private String prevCtoken;

  /** A Collection of related BwAlarm objects. These may just be the alarms
   * defined in an ical calendar or all alarms for the given event.
   *
   * <p>These are not fetched while fetching the event. Call getAlarms()
   */
  private Collection<BwAlarm> alarms = null;

  /** If the event is a master recurring event and we asked for the master +
   * overrides or for fully expanded, this will hold all the overrides for that
   * event in the form of EventInfo objects referencing a BwProxyEvent.
   */
  private Set<EventOverride> overrides;

  /** This is a copy of overrides at the point an event is retrieved. It allows
   * us to compare the state of overrides after modifications.
   */
  private Set<EventOverride> retrievedOverrides;

  private Set<String> overrideIds;

  /* * At the start of an update we set this to the full set of overrides.
   * At the end it will be the set of overrides to delete
   */
  //private Set<EventInfo> deletedOverrides;

  /* * This is where we put the overrides when we are updating the event
   */
  //private Map<String, EventInfo> overrideMap;

  /* Collection of EventInfo representing contained items. For
   * entityTypeVavailability this would be AVAILABLE components. For Vpoll it
   * will be candidates.
   */
  private Set<EventInfo> containedItems;

  /* This object contains information giving the current users access rights to
   * the entity.
   */
  private CurrentAccess currentAccess;

  /* Fields set when we are doing a scheduling operation. They indicate the
   * attendee who requested the operation and the name of the inbox event from
   * that attendee.
   */
  private String replyAttendeeURI;
  private String inboxEventName;

  private UpdateResult updResult;

  private boolean replyUpdate;

  /**
   * @param event the contained event
   */
  public EventInfo(final BwEvent event) {
    setEvent(event);
  }

  /**
   * @param event the contained event
   * @param overrides set of overrides
   */
  public EventInfo(final BwEvent event,
                   final Set<EventInfo> overrides) {
    setEvent(event);

    this.overrides = new TreeSet<>();

    for (final EventInfo oei: overrides) {
      if (oei.getEvent().getRecurrenceId() == null) {
        throw new RuntimeException("No recurrence id in override");
      }

      this.overrides.add(new EventOverride(oei));
      oei.parent = this;
    }

    retrievedOverrides = new TreeSet<>(this.overrides);
  }

  @Override
  public void setId(final int val) {
    getEvent().setId(val);
  }

  @Override
  @JsonIgnore
  public int getId() {
    return getEvent().getId();
  }

  @Override
  public void setHref(final String val) {
    getEvent().setHref(val);
  }

  @Override
  public String getHref() {
    return getEvent().getHref();
  }

  /**
   * @return BwEvent associated with this object
   */
  public BwEvent getEvent() {
    return event;
  }

  public EventInfo getParent() {
    return parent;
  }

  public static EventInfo makeProxy(final BwEvent master,
                                    final BwDateTime start,
                                    final BwDateTime end,
                                    final String recurrenceId,
                                    final boolean isOverride) {
    final BwEventAnnotation ann = new BwEventAnnotation();

    // Fields that must be the same as master
    ann.setColPath(master.getColPath());
    ann.setName(master.getName());
    ann.setUid(master.getUid());
    ann.setOwnerHref(master.getOwnerHref());

    ann.setDtstart(start);
    ann.setDtend(end);
    ann.setRecurrenceId(recurrenceId);
    ann.setOwnerHref(master.getOwnerHref());
    ann.setOverride(isOverride);
    ann.setTombstoned(false);
    ann.setName(master.getName());
    ann.setUid(master.getUid());
    ann.setTarget(master);
    ann.setMaster(master);

    return new EventInfo(new BwEventProxy(ann));
  }

  /** The full event as retrieved. This allows us to (re)index after
   * changes to a single instance for example
   *
   * @param val the full retrieved copy
   */
  public void setRetrievedEvent(final EventInfo val) {
    retrievedEvent = val;
  }

  /** The full event as retrieved. This allows us to (re)index after
   * changes to a single instance for example
   *
   * @return as retrieved
   */
  public EventInfo getRetrievedEvent() {
    return retrievedEvent;
  }

  /** editable is set at retrieval to indicate an event owned by the current
   * user. This only has significance for the personal calendar.
   * <br/>
   * XXX - not applicable in a shared world?
   *
   * @param val true if object is considered editable
   */
  public void setEditable(final boolean val) {
    editable = val;
  }

  /**
   * @return true if object is considered editable
   */
  public boolean getEditable() {
    return editable;
  }

  /** Return true if this event is included as a reference
   *
   * @return true if object is from a ref
   */
  public boolean getFromRef() {
    return fromRef;
  }

  /**
   * @param val int kind of event
   */
  public void setKind(final int val) {
    kind = val;
  }

  /**
   * @return int kind of event
   */
  public int getKind() {
    return kind;
  }

  /** This field is set by those input methods which might need to retrieve
   * an event for update, for example the icalendar translators.
   *
   * <p>They retrieve the event based on the guid. If the guid is not found
   * then we assume a new event. Otherwise this flag is set false.
   *
   *  @param  val    boolean true if a new event
   */
  public void setNewEvent(final boolean val) {
    newEvent = val;
  }

  /** Is the event new?
   *
   *  @return boolean    true if the event is new
   */
  public boolean getNewEvent() {
    return newEvent;
  }

  public int getMaxAttendees() {
    int maxAttendees = 0;
    if (!getEvent().getSuppressed()) {
      maxAttendees = getEvent().getSchedulingInfo().getRecipientParticipants().size();
    }

    if (getNumOverrides() > 0) {
      for (final EventInfo oei: getOverrides()) {
        final BwEvent oev = oei.getEvent();
        maxAttendees = Math.max(maxAttendees,
                                oev.getSchedulingInfo()
                                   .getRecipientParticipants().size());
      }
    }

    return maxAttendees;
  }

  public SchedulingOwner getSchedulingOwner() {
    SchedulingOwner sowner = null;
    if (!getEvent().getSuppressed()) {
      sowner = getEvent().getSchedulingInfo().getSchedulingOwner();
    }

    if (sowner != null) {
      return sowner;
    }

    if (getNumOverrides() > 0) {
      for (final EventInfo oei: getOverrides()) {
        final BwEvent oev = oei.getEvent();
        sowner = oev.getSchedulingInfo()
                    .getSchedulingOwner();
        if (sowner != null) {
          return sowner;
        }
      }
    }

    return null;
  }

  /** This field is set when the organizers copy is being updated as the result
   * of a reply. We flag this in the attendees inbox witha  private xprop which
   * allows us to deleet the trivial updates.
   *
   *  @param  val    boolean true if a reply update
   */
  public void setReplyUpdate(final boolean val) {
    replyUpdate = val;
  }

  /**
   *  @return boolean    true if this is a replay update
   */
  public boolean getReplyUpdate() {
    return replyUpdate;
  }

  /** This field is set when we are sent one or more instances and no master.
   * In this case we don't delete other overrides.
   *
   *  @param  val    boolean true if instanceOnly
   */
  public void setInstanceOnly(final boolean val) {
    instanceOnly = val;
  }

  /** Sent instances only?
   *
   *  @return boolean
   */
  public boolean getInstanceOnly() {
    return instanceOnly;
  }

  /** Set the event's previous schedule-tag - used to allow if none match
   *
   *  @param val     stag
   */
  public void setPrevStag(final String val) {
    prevStag = val;
  }

  /** Get the event's previous schedule-tag - used to allow if none match
   *
   * @return the event's lastmod
   */
  public String getPrevStag() {
    return prevStag;
  }

  /** Set the event's previous ctoken - used to allow if none match
   *
   *  @param val     lastmod
   */
  public void setPrevCtoken(final String val) {
    prevCtoken = val;
  }

  /** Get the event's previous ctoken - used to allow if none match
   *
   * @return the event's lastmod
   */
  public String getPrevCtoken() {
    return prevCtoken;
  }

  /** Get the event's recurrence id
   *
   * @return the event's recurrence id
   */
  public String getRecurrenceId() {
    if (event == null) {
      return null;
    }
    return event.getRecurrenceId();
  }

  /** Set the current users access rights.
   *
   * @param val  CurrentAccess
   */
  public void setCurrentAccess(final CurrentAccess val) {
    currentAccess = val;
  }

  /** Get the current users access rights.
   *
   * @return  CurrentAccess
   */
  public CurrentAccess getCurrentAccess() {
    return currentAccess;
  }

  /** Get change set for the event. The absence of a changes does not
   * mean no changes - there may be overrides to apply.
   *
   * @param userHref these apply to
   * @return null for no changes
   */
  public ChangeTable getChangeset(final String userHref) {
    if (event == null) {
      return null;
    }
    return event.getChangeset(userHref);
  }

  /** Will force update
   * 
   */
  public void clearChangeset() {
    if (event == null) {
      return;
    }
    event.clearChangeset();
  }

  /**
   * @return UpdateResult or null
   */
  public UpdateResult getUpdResult() {
    if (updResult == null) {
      updResult = new UpdateResult();
    }

    return updResult;
  }

  /**
   * @return true if the overrides have been added or deleted
   */
  public boolean getOverridesChanged() {
    if ((overrides == null) && (retrievedOverrides == null)) {
      return false;
    }

    if (getNumOverrides() != retrievedOverrides.size()) {
      return true;
    }

    if (getNumOverrides() == 0) {
      return false;
    }

    for (final EventOverride oe: overrides) {
      if (!retrievedOverrides.contains(oe)) {
        return true;
      }
    }

    return false;
  }

  /* ==============================================================
   *                   Alarms methods
   * ============================================================== */

  /**
   * @param val collection of alarms
   */
  public void setAlarms(final Collection<BwAlarm> val) {
    alarms = val;
  }

  /**
   * @return Collection of alarms
   */
  public Collection<BwAlarm> getAlarms() {
    return alarms;
  }

  /**
   * @return int number of alarms.
   */
  public int getNumAlarms() {
    final Collection<BwAlarm> as = getAlarms();
    if (as == null) {
      return 0;
    }

    return as.size();
  }

  /** clear the event's alarms
   */
  public void clearAlarms() {
    final Collection<BwAlarm> as = getAlarms();
    if (as != null) {
      as.clear();
    }
  }

  /**
   * @param val an alarm
   */
  public void addAlarm(final BwAlarm val) {
    Collection<BwAlarm> as = getAlarms();
    if (as == null) {
      as = new TreeSet<>();
    }

    if (!as.contains(val)) {
      as.add(val);
    }
  }

  /* ==============================================================
   *                   Overrides methods
   * ============================================================== */

  /**
   *
   * @param val Set of override ids
   */
  public void setOverrideIds(final Set<String> val) {
    overrideIds = val;
  }

  /** Get the override recurrence ids. Provided by the indexer
   *
   *  @return Set of override ids
   */
  public Set<String> getOverrideIds() {
    return overrideIds;
  }

  /** Get the overrides
   *
   *  @return Set     overrides list
   */
  public Set<EventInfo> getOverrides() {
    if (overrides == null) {
      return null;
    }

    final Set<EventInfo> eis = new TreeSet<>();

    for (final EventOverride eo: overrides) {
      eis.add(eo.getEventInfo());
    }

    return eis;
  }

  /**
   * @return int number of overrides.
   */
  public int getNumOverrides() {
    final Set<EventInfo> os = getOverrides();
    if (os == null) {
      return 0;
    }

    return os.size();
  }

  /**
   * @param val an override
   */
  public void addOverride(final EventInfo val) {
    if (val.getEvent().getRecurrenceId() == null) {
      throw new RuntimeException("No recurrence id in override");
    }

    if (overrides == null) {
      overrides = new TreeSet<>();
    }

    final EventOverride eo = new EventOverride(val);

    overrides.add(eo);
    val.parent = this;
  }

  /* *
   * @param val
   * @return boolean true if removed.
   * /
  public boolean removeOverride(final BwEventAnnotation val) {
    Collection<EventInfo> os = getOverrides();
    if (os == null) {
      return false;
    }

    return os.remove(val);
  }*/

  /**
   * @return Collection of override proxy events or null
   */
  public Collection<BwEventProxy> getOverrideProxies() {
    if (getNumOverrides() == 0) {
      return null;
    }

    final TreeSet<BwEventProxy> proxies = new TreeSet<>();

    for (final EventInfo ei: getOverrides()) {
      final BwEventProxy proxy = (BwEventProxy)ei.getEvent();
      proxies.add(proxy);
    }

    return proxies;
  }

  /**
   * @param userHref
   * @return Collection of deleted override proxy events or null
   */
  public Collection<BwEventProxy> getDeletedOverrideProxies(final String userHref) {
    final TreeSet<BwEventProxy> proxies = new TreeSet<>();

    if ((retrievedOverrides == null) || instanceOnly) {
      return proxies;
    }

    for (final EventOverride eo: retrievedOverrides) {
      if (overrides.contains(eo)) {
        continue;
      }

      final BwEventProxy proxy = (BwEventProxy)eo.getEvent();

      if (proxy.getRef().unsaved()) {
        throw new RuntimeException("Unsaved override in delete list");
      }

      /* If the override is not for per user data then we remove it
       *
       * If it is for peruser data then we first remove any peruser data
       * for the current user. If there is no peruser data left then we remove
       * the instance.
       */

      if (proxy.getXproperty(BwXproperty.peruserInstance) == null) {
        /* Not peruser - remove it. */
        proxies.add(proxy);
        continue;
      }

      final BwXproperty pu =
              proxy.findPeruserXprop(userHref,
                                     BwXproperty.peruserPropTransp);

      if (pu != null) {
        /* remove it */
        proxy.removeXproperty(pu);
      }

      /* Remove any alarm(s) */
      final List<BwAlarm> toRemove = new ArrayList<>();
      for (final BwAlarm a: proxy.getAlarms()) {
        if (a.getOwnerHref().equals(userHref)) {
          toRemove.add(a);
        }
      }

      for (final BwAlarm a: toRemove) {
        proxy.removeAlarm(a);
      }

      if (Util.isEmpty(proxy.getXproperties(BwXproperty.peruserPropTransp)) &&
          Util.isEmpty(proxy.getAlarms())) {
        /* No more peruser data - add to remove list */
        proxies.add(proxy);
      }

      /* Update the changes */
      final ChangeTable chg = getChangeset(userHref);

      if (pu != null) {
        chg.addValue(PropertyInfoIndex.XPROP, pu);
      }

      if (!Util.isEmpty(toRemove)) {
        for (final BwAlarm a: toRemove) {
          chg.addValue(PropertyInfoIndex.VALARM, a);
        }
      }
    }

    return proxies;
  }

  /** See if the master event has an override with the given recurrence id.
   * If not create one.
   *
   * @param rid recurrence id to find
   * @return EventInfo for override
   */
  public EventInfo findOverride(final String rid) {
    return findOverride(rid, true);
  }

  /** See if the master event has an override with the given recurrence id.
   * If not optionally create one.
   *
   * @param rid recurrence id to find
   * @param create - true to create missing override.
   * @return EventInfo for override
   */
  public EventInfo findOverride(final String rid,
                                final boolean create) {
    if (overrides != null) {
      for (final EventOverride eo: overrides) {
        if (eo.getEvent().getRecurrenceId().equals(rid)) {
          return eo.getEventInfo();
        }
      }
    }

    if (!create) {
      return null;
    }

    final BwEventProxy proxy =
            BwEventProxy.makeAnnotation(getEvent(), null, true);
    proxy.setRecurring(Boolean.FALSE);
    final EventInfo oei = new EventInfo(proxy);
    proxy.setRecurrenceId(rid);

    oei.setNewEvent(true);

    addOverride(oei);

    return oei;
  }
  
  public boolean removeOverride(final String rid) {
    EventOverride ov = null;
    if (Util.isEmpty(overrides)) {
      return false;
    }
    
    for (final EventOverride eo: overrides) {
      if (eo.getEvent().getRecurrenceId().equals(rid)) {
        ov = eo;
        break;
      }
    }

    if (ov == null) {
      return false;
    }
    
    overrides.remove(ov);
    return true;
  }

  /** An attendee we need to send a reply to
   *
   *  @param val     uri
   */
  public void setReplyAttendeeURI(final String val) {
    replyAttendeeURI = val;
  }

  /** An attendee we need to send a reply to
   *
   * @return uri
   */
  public String getReplyAttendeeURI() {
    return replyAttendeeURI;
  }

  /**
   *  @param val     event name
   */
  public void setInboxEventName(final String val) {
    inboxEventName = val;
  }

  /**
   * @return inbox event name
   */
  public String getInboxEventName() {
    return inboxEventName;
  }

  /* ==============================================================
   *                   Contained item methods
   * ============================================================== */

  /** set the contained items
   *
   * @param val     Collection    of EventInfo
   */
  public void setContainedItems(final Set<EventInfo> val) {
    containedItems = val;
  }

  /** Get the contained items
   *
   * @return Collection    of EventInfo
   */
  public Set<EventInfo> getContainedItems() {
    return containedItems;
  }

  /** Add a contained item
   *
   * @param val contained event
   */
  public void addContainedItem(final EventInfo val) {
    Set<EventInfo> cis = getContainedItems();

    if (cis == null) {
      cis = new TreeSet<>();
      setContainedItems(cis);
    }

    cis.add(val);
    val.parent = this;
  }

  /**
   * @return int number of contained items.
   */
  public int getNumContainedItems() {
    final Set<EventInfo> cis = getContainedItems();
    if (cis == null) {
      return 0;
    }

    return cis.size();
  }

  /* ==============================================================
   *                   Object methods
   * ============================================================== */

  @Override
  public int compare(final EventInfo e1, final EventInfo e2) {
    if (e1 == e2) {
      return 0;
    }

    return e1.getEvent().compare(e1.getEvent(), e2.getEvent());
    /*BwEvent ev1 = e1.getEvent();
    BwEvent ev2 = e2.getEvent();

    int res = ev1.getUid().compareTo(ev2.getUid());

    if (res != 0) {
      return res;
    }

    return Util.compareStrings(ev1.getRecurrenceId(), ev2.getRecurrenceId());*/
  }

  @Override
  public int compareTo(final EventInfo that) {
    return compare(this, that);
  }

  @Override
  public int hashCode() {
    return getEvent().hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }

    if (!(obj instanceof EventInfo)) {
      return false;
    }

    return compareTo((EventInfo)obj) == 0;
  }

  @Override
  public String toString() {
    final ToString ts = new ToString(this);

    if (getEvent() == null) {
      ts.append("eventId", (Integer)null);
    } else {
      ts.append("eventId", getEvent().getId());
    }

    ts.append("editable", getEditable());
    ts.append("kind", kindStr[getKind()]);

    if (getAlarms() != null) {
      for (final BwAlarm alarm: getAlarms()) {
        ts.append("alarm", alarm.toString());
      }
    }
    ts.append("recurrenceId", getEvent().getRecurrenceId());

    return ts.toString();
  }

  /* ==============================================================
   *                   Private methods
   * ============================================================== */

  /**
   * @param val the event
   */
  private void setEvent(final BwEvent val) {
    event = val;
    fromRef = val instanceof BwEventAnnotation;
    setPrevStag(val.getStag());
    setPrevCtoken(val.getCtoken());
  }
}

