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
package org.bedework.convert.ical;

import org.bedework.base.exc.BedeworkException;
import org.bedework.calfacade.BwAlarm;
import org.bedework.calfacade.BwAlarm.TriggerVal;
import org.bedework.calfacade.BwAttendee;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwXproperty;
import org.bedework.calfacade.ifs.IcalCallback;
import org.bedework.calfacade.util.ChangeTable;
import org.bedework.util.calendar.PropertyIndex.PropertyInfoIndex;
import org.bedework.base.response.Response;

import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.parameter.Related;
import net.fortuna.ical4j.model.property.Action;
import net.fortuna.ical4j.model.property.Attach;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Duration;
import net.fortuna.ical4j.model.property.Repeat;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Trigger;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.XProperty;

import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/** Class to provide utility methods for handline VAlarm ical4j classes
 *
 * @author Mike Douglass   douglm rpi.edu
 */
public class VAlarmUtil extends IcalUtil {
  /** If there are any alarms for this component add them to the events alarm
   * collection
   *
   * @param cb          IcalCallback object
   * @param val the parent component
   * @param va the alarm
   * @param ev bedework event object
   * @param currentPrincipal - href for current authenticated user
   * @param chg change table
   */
  public static Response<?> processAlarm(final IcalCallback cb,
                                         final Component val,
                                         final VAlarm va,
                                         final BwEvent ev,
                                         final String currentPrincipal,
                                         final ChangeTable chg) {
    final PropertyList<Property> pl = va.getProperties();

    if (pl == null) {
      // Empty VAlarm
      return new Response<>().error("Invalid alarm list");
    }

    Property prop;
    final BwAlarm al;

    /* XXX Handle mozilla alarm stuff in a way that might work better with other clients.
         *
         */

    prop = pl.getProperty("X-MOZ-LASTACK");
    final boolean mozlastAck = prop != null;

    String mozSnoozeTime = null;
    if (mozlastAck) {
      prop = pl.getProperty("X-MOZ-SNOOZE-TIME");

      if (prop == null) {
        // lastack and no snooze - presume dismiss so delete alarm
        return new Response<>().ok();
      }

      mozSnoozeTime = prop.getValue(); // UTC time
    }

    // All alarm types require action and trigger

    prop = pl.getProperty(Property.ACTION);
    if (prop == null) {
      return new Response<>().error("Invalid alarm");
    }

    final String actionStr = prop.getValue();

    final TriggerVal tr = getTrigger(pl, "NONE".equals(actionStr));

    if (mozSnoozeTime != null) {
      tr.trigger = mozSnoozeTime;
      tr.triggerDateTime = true;
      tr.triggerStart = false;
    }

    final DurationRepeat dr = getDurationRepeat(pl);

    switch (actionStr) {
      case "EMAIL" -> {
        al = BwAlarm.emailAlarm(ev.getCreatorHref(),
                                tr,
                                dr.duration, dr.repeat,
                                getOptStr(pl, "ATTACH"),
                                getReqStr(pl, "DESCRIPTION"),
                                getReqStr(pl, "SUMMARY"),
                                null);

        final Iterator<?> atts = getReqStrs(pl, "ATTENDEE");

        while (atts.hasNext()) {
          al.addAttendee(getAttendee(cb, (Attendee)atts.next()));
        }
      }
      case "AUDIO" -> al = BwAlarm.audioAlarm(ev.getCreatorHref(),
                                              tr,
                                              dr.duration, dr.repeat,
                                              getOptStr(pl,
                                                        "ATTACH"));
      case "DISPLAY" -> al = BwAlarm.displayAlarm(ev.getCreatorHref(),
                                                  tr,
                                                  dr.duration,
                                                  dr.repeat,
                                                  getReqStr(pl,
                                                            "DESCRIPTION"));
      case "PROCEDURE" ->
              al = BwAlarm.procedureAlarm(ev.getCreatorHref(),
                                          tr,
                                          dr.duration, dr.repeat,
                                          getReqStr(pl, "ATTACH"),
                                          getOptStr(pl,
                                                    "DESCRIPTION"));
      case "NONE" -> al = BwAlarm.noneAlarm(ev.getCreatorHref(),
                                            tr,
                                            dr.duration, dr.repeat,
                                            getOptStr(pl,
                                                      "DESCRIPTION"));
      case null, default ->
              al = BwAlarm.otherAlarm(ev.getCreatorHref(),
                                      actionStr,
                                      tr,
                                      dr.duration, dr.repeat,
                                      getOptStr(pl, "DESCRIPTION"));
    }

    /* Mozilla is add xprops to the containing event to set the snooze time.
         * Seems wrong - there could be multiple alarms.
         *
         * We possibly want to try this sort of trick..

        prop = pl.getProperty("X-MOZ-LASTACK");
        boolean mozlastAck = prop != null;

        String mozSnoozeTime = null;
        if (mozlastAck) {
          prop = pl.getProperty("X-MOZ-SNOOZE-TIME");

          if (prop == null) {
            // lastack and no snooze - presume dismiss so delete alarm
            continue;
          }

          mozSnoozeTime = prop.getValue(); // UTC time
        }
        ...

        TriggerVal tr = getTrigger(pl);

        if (mozSnoozeTime != null) {
          tr.trigger = mozSnoozeTime;
          tr.triggerDateTime = true;
          tr.triggerStart = false;
        }

         */

    for (final Property property: pl) {
      prop = property;

      if (prop instanceof final XProperty xp) {
        /* ------------------------- x-property --------------------------- */

        al.addXproperty(new BwXproperty(xp.getName(),
                                        xp.getParameters()
                                          .toString(),
                                        xp.getValue()));
        continue;
      }

      if (prop instanceof final Uid p) {
        al.addXproperty(BwXproperty.makeIcalProperty(p.getName(),
                                                     p.getParameters()
                                                      .toString(),
                                                     p.getValue()));
        //continue;
      }
    }

    al.setOwnerHref(currentPrincipal);
    chg.addValue(PropertyInfoIndex.VALARM, al);

    return new Response<>().ok();
  }

  /** Process any alarms.
   *
   * @param ev the event
   * @param comp representing the event
   * @param currentPrincipal - href for current authenticated user
   */
  public static void processEventAlarm(final BwEvent ev,
                                       final Component comp,
                                       final String currentPrincipal) {
    if (currentPrincipal == null) {
      // No alarms for unauthenticated users.
      return;
    }

    final Collection<BwAlarm> als = ev.getAlarms();
    if ((als == null) || als.isEmpty()) {
      return;
    }

    final ComponentList<Component> vals;

    if (comp instanceof VEvent) {
      vals = ((VEvent)comp).getComponents();
    } else if (comp instanceof VToDo) {
      vals = ((VToDo)comp).getComponents();
    } else {
      throw new RuntimeException("org.bedework.invalid.component.type " +
                                   comp.getName());
    }

    for (final BwAlarm alarm: als) {
      /* Only add alarms for the current authenticated user */
      if (!currentPrincipal.equals(alarm.getOwnerHref())) {
        continue;
      }

      vals.add(setAlarm(ev, alarm));
    }
  }

  private static VAlarm setAlarm(final BwEvent ev,
                                 final BwAlarm val) {
    try {
      final VAlarm alarm = new VAlarm();

      final int atype = val.getAlarmType();
      final String action;

      if (atype != BwAlarm.alarmTypeOther) {
        action = BwAlarm.alarmTypes[atype];
      } else {
        final List<BwXproperty> xps = val.getXicalProperties("ACTION");

        action = xps.getFirst().getValue();
      }

      addProperty(alarm, new Action(action));

      if (val.getTriggerDateTime()) {
        final DateTime dt = new DateTime(val.getTrigger());
        addProperty(alarm, new Trigger(dt));
      } else {
        final Trigger tr = new Trigger(new Dur(val.getTrigger()));
        if (!val.getTriggerStart()) {
          addParameter(tr, Related.END);
        } else {
          // Not required - it's the default - but we fail some Cyrus tests otherwise
          // Apparently Cyrus now handles the default state correctly
          addParameter(tr, Related.START);
        }
        addProperty(alarm, tr);
      }

      if (val.getDuration() != null) {
        addProperty(alarm, new Duration(new Dur(val.getDuration())));
        addProperty(alarm, new Repeat(val.getRepeat()));
      }

      if (atype == BwAlarm.alarmTypeAudio) {
        if (val.getAttach() != null) {
          addProperty(alarm, new Attach(new URI(val.getAttach())));
        }
      } else if (atype == BwAlarm.alarmTypeDisplay) {
        /* This is required but somehow we got a bunch of alarms with no description
         * Is it possibly because of the rollback issue I (partially) fixed?
         */
        //checkRequiredProperty(val.getDescription(), "alarm-description");
        if (val.getDescription() != null) {
          addProperty(alarm, new Description(val.getDescription()));
        } else {
          addProperty(alarm, new Description(ev.getSummary()));
        }
      } else if (atype == BwAlarm.alarmTypeEmail) {
        if (val.getAttach() != null) {
          addProperty(alarm, new Attach(new URI(val.getAttach())));
        }
        checkRequiredProperty(val.getDescription(), "alarm-description");
        addProperty(alarm, new Description(val.getDescription()));
        checkRequiredProperty(val.getSummary(), "alarm-summary");
        addProperty(alarm, new Summary(val.getSummary()));

        if (val.getNumAttendees() > 0) {
          for (final BwAttendee att: val.getAttendees()) {
            addProperty(alarm, setAttendee(att));
          }
        }
      } else if (atype == BwAlarm.alarmTypeProcedure) {
        checkRequiredProperty(val.getAttach(), "alarm-attach");
        addProperty(alarm, new Attach(new URI(val.getAttach())));

        if (val.getDescription() != null) {
          addProperty(alarm, new Description(val.getDescription()));
        }
      } else {
        if (val.getDescription() != null) {
          addProperty(alarm, new Description(val.getDescription()));
        }
      }

      if (val.getNumXproperties() > 0) {
        /* This event has x-props */

        xpropertiesToIcal(alarm.getProperties(),
                          val.getXproperties());
      }

      return alarm;
    } catch (final BedeworkException bfe) {
      throw bfe;
    } catch (final Throwable t) {
      throw new BedeworkException(t);
    }
  }

  private static void checkRequiredProperty(final String val,
                                            final String name) {
    if (val == null) {
      throw new BedeworkException("org.bedework.icalendar.missing.required.property",
                                   name);
    }
  }
}
