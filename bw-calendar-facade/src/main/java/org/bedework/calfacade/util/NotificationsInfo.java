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
package org.bedework.calfacade.util;

import org.bedework.caldav.util.notifications.CalendarChangesType;
import org.bedework.caldav.util.notifications.ChangedByType;
import org.bedework.caldav.util.notifications.ChangedPropertyType;
import org.bedework.caldav.util.notifications.ChangesType;
import org.bedework.caldav.util.notifications.CreatedType;
import org.bedework.caldav.util.notifications.DeletedDetailsType;
import org.bedework.caldav.util.notifications.DeletedType;
import org.bedework.caldav.util.notifications.NotificationType;
import org.bedework.caldav.util.notifications.RecurrenceType;
import org.bedework.caldav.util.notifications.ResourceChangeType;
import org.bedework.caldav.util.notifications.UpdatedType;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwLocation;
import org.bedework.calfacade.BwLongString;
import org.bedework.calfacade.BwString;
import org.bedework.calfacade.BwXproperty;
import org.bedework.util.calendar.IcalDefs;
import org.bedework.util.misc.Util;

import java.util.Collection;

import static org.bedework.util.calendar.PropertyIndex.PropertyInfoIndex;
import static org.bedework.util.dates.DateFormatter.webDateTimeFormat;

/** Generate change notification messages from event and other information.
 * Output is an XML object following the Apple extensions.
 *
 * <p>Call open first, then one or more of the methods describing changes,
 * followed by a call to close which returns the entire XML body.
 *
 * @author douglm
 */
public class NotificationsInfo {
  /**
   * @param currentAuth authenticated principal
   * @param ev the event
   * @return Info for single added event.
   */
  public static String added(final String currentAuth,
                             final BwEvent ev) {
    final var note = getNotification();

    note.setNotification(getAdded(currentAuth, ev));

    return note.toXml(true);
  }

  /**
   * @param currentAuth authenticated principal
   * @param ev the event
   * @return Info for single deleted event.
   */
  public static String deleted(final String currentAuth,
                               final BwEvent ev) {
    final var note = getNotification();

    note.setNotification(getDeleted(currentAuth, ev));

    return note.toXml(true);
  }

  /**
   * @param currentAuth authenticated principal
   * @param ev the event
   * @return Info for single updated event.
   */
  public static String updated(final String currentAuth,
                               final BwEvent ev) {
    final var rc = getUpdated(currentAuth, ev);

    if (rc == null) {
      return null;
    }

    final var note = getNotification();

    note.setNotification(rc);

    return note.toXml(true);
  }

  /** Call for a deleted event
   *
   * @param currentAuth authenticated principal
   * @param ev the event
   * @return resource deleted notification.
   */
  public static ResourceChangeType getDeleted(final String currentAuth,
                                              final BwEvent ev) {
    final var rc = new ResourceChangeType();

    final var del = new DeletedType();

    del.setHref(getHref(ev));

    del.setChangedBy(getChangedBy(currentAuth));

    final var dd = new DeletedDetailsType();

    dd.setDeletedComponent(getType(ev));
    dd.setDeletedSummary(ev.getSummary());
    //if (ev.isRecurringEntity()) {
    // TODO: Set these correctly.
    //dd.setDeletedNextInstance(val);
    //dd.setDeletedNextInstanceTzid(val);
    //dd.setDeletedHadMoreInstances(val);
    //}

    if (ev.getDtstart() != null) {
      final var start = new ChangedPropertyType();
      start.setName(PropertyInfoIndex.DTSTART.name());
      start.setDataFrom(String.valueOf(ev.getDtstart()));
      dd.getDeletedProps().add(start);
    }

    if (ev.getDtend() != null) {
      final var end = new ChangedPropertyType();
      end.setName(PropertyInfoIndex.DTEND.name());
      end.setDataFrom(String.valueOf(ev.getDtend()));
      dd.getDeletedProps().add(end);
    }

    if (ev.getDuration() != null && !ev.getDuration().isEmpty()) {
      final var dur = new ChangedPropertyType();
      dur.setName(PropertyInfoIndex.DURATION.name());
      dur.setDataFrom(ev.getDuration());
      dd.getDeletedProps().add(dur);
    }

    if (ev.getLocation() != null) {
      final var loc = new ChangedPropertyType();
      loc.setName(PropertyInfoIndex.LOCATION.name());
      loc.setDataFrom(ev.getLocation().getAddress().getValue());
      dd.getDeletedProps().add(loc);
    }

    if (ev.getDescription() != null) {
      final var desc = new ChangedPropertyType();
      desc.setName(PropertyInfoIndex.DESCRIPTION.name());
      desc.setDataFrom(ev.getDescription());
      dd.getDeletedProps().add(desc);
    }

    del.setDeletedDetails(dd);

    rc.setDeleted(del);

    return rc;
  }

  /** Call for an added event
   *
   * @param currentAuth authenticated principal
   * @param ev the event
   * @return resource created notification.
   */
  public static ResourceChangeType getAdded(final String currentAuth,
                                            final BwEvent ev) {
    final var rc = new ResourceChangeType();

    final var cre = new CreatedType();

    cre.setHref(getHref(ev));

    cre.setChangedBy(getChangedBy(currentAuth));

    rc.setCreated(cre);

    return rc;
  }

  /** Call for an updated event.
   *
   * @param currentAuth authenticated principal
   * @param ev the event
   * @return resource updated notification.
   */
  public static ResourceChangeType getUpdated(
      final String currentAuth,
      final BwEvent ev) {
    final var changes = ev.getChangeset(currentAuth);

    if (changes.isEmpty()) {
      return null;
    }

    final var rc = new ResourceChangeType();

    final var upd = new UpdatedType();

    upd.setHref(getHref(ev));

    upd.setChangedBy(getChangedBy(currentAuth));

    upd.getCalendarChanges().add(instanceChanges(currentAuth, ev));

    rc.addUpdate(upd);

    return rc;
  }

  /* ===========================================================
                      Private methods
     =========================================================== */

  private NotificationsInfo() {}

  private static ChangedByType getChangedBy(final String currentAuth) {
    final var cb = new ChangedByType();
    // firstName
    // lastName
    cb.setCommonName(currentAuth); // XXX - need real name(s)
    cb.setDtstamp(webDateTimeFormat.fromDate());
    cb.setHref(currentAuth);

    return cb;
  }

  private static NotificationType getNotification() {
    final var note = new NotificationType();

    note.setDtstamp(webDateTimeFormat.fromDate());

    return note;
  }

  private static CalendarChangesType instanceChanges(
      final String currentAuth,
      final BwEvent ev) {
    final var cc = new CalendarChangesType();
    final var r = new RecurrenceType();

    r.setRecurrenceid(ev.getRecurrenceId());

    final var c = new ChangesType();

    for (final var cte: ev.getChangeset(currentAuth).getEntries()) {
      if (!cte.getChanged()) {
        continue;
      }

      if (cte.getIndex() == PropertyInfoIndex.XPROP) {
        /* Reflected a a set of removes and adds. */
        if (!Util.isEmpty(cte.getRemovedValues())) {
          for (final var xp: ((Collection<BwXproperty>)cte.getRemovedValues())) {
            final var cp = new ChangedPropertyType();
            cp.setName(xp.getName());

            cp.setDataFrom(String.valueOf(xp));

            c.getChangedProperty().add(cp);
          }
        }

        if (!Util.isEmpty(cte.getAddedValues())) {
          for (final var xp: ((Collection<BwXproperty>)cte.getAddedValues())) {
            final var cp = new ChangedPropertyType();
            cp.setName(xp.getName());

            cp.setDataTo(String.valueOf(xp));

            c.getChangedProperty().add(cp);
          }
        }
      } else {
        final var cp = new ChangedPropertyType();

        cp.setName(cte.getIndex().name());

        cp.setDataFrom(getDataFrom(cte));
        cp.setDataTo(getDataTo(cte));

        c.getChangedProperty().add(cp);
      }
    }

    r.getChanges().add(c);

    cc.getRecurrence().add(r);

    return cc;
  }

  private static String getDataFrom(final ChangeTableEntry cte) {
    return getData(cte, cte.getOldVal());
  }

  private static String getDataTo(final ChangeTableEntry cte) {
    return getData(cte, cte.getNewVal());
  }

  private static String getData(final ChangeTableEntry cte,
                                final Object o) {
    if (o == null) {
      return null;
    }

    if (!cte.getIndex().getDbMultiValued()) {
      return switch (o) {
        case final BwString bwString -> bwString.getValue();
        case final BwLongString bwLongString -> bwLongString.getValue();
        case final BwLocation bwLocation ->
            bwLocation.getAddress().getValue();
        case final BwXproperty bwXproperty -> bwXproperty.getValue();
        default -> String.valueOf(o);
      };
    }

    return switch (o) {
      case final BwString bwString -> bwString.getValue();
      case final BwLongString bwLongString -> bwLongString.getValue();
      case final BwLocation bwLocation ->
          bwLocation.getAddress().getValue();
      case final BwXproperty bwXproperty -> bwXproperty.getValue();
      default -> String.valueOf(o);
    };
  }

  private static String getType(final BwEvent ev) {
    try {
      return IcalDefs.entityTypeIcalNames[ev.getEntityType()];
    } catch (final Throwable t) {
      return "X";
    }
  }

  private static String getHref(final BwEvent ev) {
    return Util.buildPath(false, ev.getColPath(), "/", ev.getName());
  }
}
