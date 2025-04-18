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
package org.bedework.convert;

import org.bedework.base.exc.BedeworkException;
import org.bedework.calfacade.BwCollection;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.base.StartEndComponent;
import org.bedework.calfacade.ifs.IcalCallback;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.convert.Icalendar.TimeZoneInfo;
import org.bedework.convert.ical.BwEvent2Ical;
import org.bedework.convert.ical.Ical2BwEvent;
import org.bedework.convert.ical.IcalMalformedException;
import org.bedework.convert.ical.IcalUtil;
import org.bedework.convert.ical.VFreeUtil;
import org.bedework.util.calendar.IcalDefs;
import org.bedework.util.calendar.IcalendarUtil;
import org.bedework.util.calendar.JcalCalendarBuilder;
import org.bedework.util.calendar.WsXMLTranslator;
import org.bedework.util.calendar.XmlCalendarBuilder;
import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;
import org.bedework.util.misc.Util;
import org.bedework.base.response.GetEntityResponse;
import org.bedework.util.timezones.Timezones;

import ietf.params.xml.ns.icalendar_2.BaseComponentType;
import ietf.params.xml.ns.icalendar_2.IcalendarType;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarParserImpl;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.data.UnfoldingReader;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VAvailability;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VFreeBusy;
import net.fortuna.ical4j.model.component.VPoll;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.property.TzId;

import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

import jakarta.xml.bind.JAXBElement;

import static org.bedework.calfacade.BwVersion.prodId;
import static org.bedework.util.calendar.IcalendarUtil.addIcalTimezone;
import static org.bedework.util.calendar.IcalendarUtil.newIcal;

/** Object to provide translation between a bedework entity and an Icalendar format.
 *
 * @author Mike Douglass   douglm rpi.edu
 */
public class IcalTranslator implements Logged, Serializable {
  /** We'll use this to parameterize some of the behaviour
   */
  public static class Pars {
    /** Support simple location only. Many iCalendar-aware
     * applications only support a simple string valued location.
     *
     * <p>If this value is true we only pass the address part of the location
     * and provide an altrep which will allow (some) clients to retrieve the
     * full form of a location.
     */
    public boolean simpleLocation = true;

    /** Support simple contacts only.
     */
    public boolean simpleContact = true;
  }

  protected IcalCallback cb;

  protected Pars pars = new Pars();

  /** Constructor:
   *
   * @param cb     IcalCallback object for retrieval of entities
   */
  public IcalTranslator(final IcalCallback cb) {
    this.cb = cb;
  }

  /* ====================================================================
   *                     Translation methods
   * ==================================================================== */

  /** Make a new Calendar with the freebusy object
   *
   * @param methodType scheduling method
   * @param ent freebusy object
   * @return String representation
   * @throws RuntimeException on fatal error
   */
  public static String freebusyToIcalString(final int methodType,
                                            final BwEvent ent) {
    final Calendar cal = IcalendarUtil.newIcal(methodType,
                                               prodId);

    if (ent.getEntityType() == IcalDefs.entityTypeFreeAndBusy) {
      final VFreeBusy vfreeBusy = VFreeUtil.toVFreeBusy(ent);

      cal.getComponents().add(vfreeBusy);
    } else {
      throw new RuntimeException("Unexpected entity type");
    }

    return IcalendarUtil.toIcalString(cal);
  }

  /** turn a single event with possible overrides into a calendar
   *
   * @param val event
   * @param methodType    int value fromIcalendar
   * @return Calendar
   */
  public Calendar toIcal(final EventInfo val,
                         final int methodType) {
    if (val == null) {
      return null;
    }

    try {
      final Calendar cal = newIcal(methodType, prodId);

      addToCalendar(cal, val, new TreeSet<>());

      return cal;
    } catch (final Throwable t) {
      throw new BedeworkException(t);
    }
  }

  /* * Write a collection of calendar data as iCalendar
   *
   * @param vals collection of calendar data
   * @param methodType    int value fromIcalendar
   * @param wtr Writer for output
   * /
  public void writeCalendar(final Collection vals,
                            final int methodType,
                            final Writer wtr) {
    try {
      Calendar cal = toIcal(vals, methodType);
      writeCalendar(cal, wtr);
    } catch (BedeworkException bfe) {
      throw bfe;
    } catch (Throwable t) {
      throw new BedeworkException(t);
    }
  }*/

  /** Turn a collection of events into a calendar
   *
   * @param vals          collection of events
   * @param methodType    int value fromIcalendar
   * @return Calendar
   */
  public Calendar toIcal(final Collection<EventInfo> vals,
                         final int methodType) {
    final Calendar cal = newIcal(methodType, prodId);

    if ((vals == null) || (vals.size() == 0)) {
      return cal;
    }

    final TreeSet<String> added = new TreeSet<>();

    try {
      for (final EventInfo ev: vals) {
        addToCalendar(cal, ev, added);
      }

      return cal;
    } catch (final Throwable t) {
      throw new BedeworkException(t);
    }
  }

  /** Get a TimeZone object from a VCALENDAR string definition
   *
   * @param val VCALENDAR string definition
   * @return TimeZone
   */
  public TimeZone tzFromTzdef(final String val) {
    final StringReader sr = new StringReader(val);

    final Icalendar ic = fromIcal(null, sr);

    if ((ic == null) ||
        (ic.size() != 0) || // No components other than timezones
        (ic.getTimeZones().size() != 1)) {
      if (debug()) {
        debug("Not icalendar");
      }
      throw new BedeworkException("Not icalendar");
    }

    /* This should be the only timezone in the Calendar object
     */
    return ic.getTimeZones().iterator().next().tz;
  }

  /** Convert the Icalendar reader to a Collection of Calendar objects
   *
   * @param col      collection the entities will live in - possibly null
   * @param rdr      Icalendar reader
   * @return Icalendar
   */
  public Icalendar fromIcal(final BwCollection col,
                            final Reader rdr) {
    return fromIcal(col, rdr, null,
                    false); // don't merge attendees
  }

  /** Convert the Icalendar object to a Collection of Calendar objects
   *
   * @param col      collection the entities will live in - possibly null
   * @param ical     xCal icalendar object
   * @return Icalendar
   */
  public Icalendar fromIcal(final BwCollection col,
                            final IcalendarType ical) {

    final Icalendar ic = new Icalendar();

    setSystemProperties();

    final WsXMLTranslator bldr = new WsXMLTranslator(ic);

    try {
      final Calendar cal = bldr.fromXcal(ical);

      return makeIc(col, ic, cal, false);
    } catch (final BedeworkException bfe) {
      throw bfe;
    } catch (final Throwable t) {
      throw new BedeworkException(t);
    }
  }

  /* * Convert the component to Calendar objects
   *
   * @param col      collection the entities will live in - possibly null
   * @param comp - the component
   * @param diff     True if we should assume we are updating existing events.
   * @return Icalendar
   * /
  public Icalendar fromComp(final BwCollection col,
                            final Component comp,
                            final boolean diff,
                            final boolean mergeAttendees) {
    try {
      final Icalendar ic = new Icalendar();

      setSystemProperties();

      final Calendar cal = new Calendar();

      cal.getComponents().add((CalendarComponent)comp);

      return makeIc(col, ic, cal, diff, mergeAttendees);
    } catch (final BedeworkException bfe) {
      throw bfe;
    } catch (final Throwable t) {
      throw new BedeworkException(t);
    }
  }*/

  /** Convert the Icalendar reader to a Collection of Calendar objects.
   * Handles xCal, jCal and ics
   *
   * @param col      collection the entities will live in - possibly null
   * @param rdr Icalendar reader
   * @param contentType "application/calendar+xml" etc
   * @param mergeAttendees True if we should only update our own attendee.
   * @return Icalendar
   */
  public Icalendar fromIcal(final BwCollection col,
                            final Reader rdr,
                            final String contentType,
                            final boolean mergeAttendees) {
    try {
      final Icalendar ic = new Icalendar();

      setSystemProperties();

      final Calendar cal;

      if ("application/calendar+xml".equals(contentType)) {
        final XmlCalendarBuilder bldr = new XmlCalendarBuilder(ic);

        cal = bldr.build(rdr);
      } else if ("application/calendar+json".equals(contentType)) {
        final JcalCalendarBuilder bldr = new JcalCalendarBuilder(
                ic);

        cal = bldr.build(rdr);
      } else {
        final CalendarBuilder bldr =
                new CalendarBuilder(new CalendarParserImpl(), ic);

        final UnfoldingReader ufrdr = new UnfoldingReader(rdr,
                                                          true);

        cal = bldr.build(ufrdr);
      }

      return makeIc(col, ic, cal, mergeAttendees);
    } catch (final BedeworkException bfe) {
      throw bfe;
    } catch (final ParserException pe) {
      throw new IcalMalformedException(pe);
    } catch (final Throwable t) {
      throw new BedeworkException(t);
    }
  }

  /** Add an override as part of an update
   * @param ei event
   * @param comp XML representation of override
   */
  public void addOverride(final EventInfo ei,
                          final JAXBElement<? extends BaseComponentType> comp) {
    try {
      final Calendar cal = new WsXMLTranslator(
              new Icalendar()).fromXcomp(comp);

      if (cal == null) {
        return;
      }

      final Icalendar ic = new Icalendar();

      ic.addComponent(ei);

      makeIc(null, ic, cal, false);
    } catch (final BedeworkException bfe) {
      throw bfe;
    } catch (final Throwable t) {
      throw new BedeworkException(t);
    }
  }

  private Icalendar makeIc(final BwCollection col,
                           final Icalendar ic,
                           final Calendar cal,
                           final boolean mergeAttendees) {
    try {
      if (cal == null) {
        return ic;
      }

      final var pl = cal.getProperties();
      Property prop = pl.getProperty(Property.PRODID);
      if (prop != null) {
        ic.setProdid(prop.getValue());
      }

      prop = pl.getProperty(Property.VERSION);
      if (prop != null) {
        ic.setVersion(prop.getValue());
      }

      ic.setMethod(getMethod(cal));

      prop = pl.getProperty(Property.CALSCALE);
      if (prop != null) {
        ic.setCalscale(prop.getValue());
      }

      final Collection<CalendarComponent> clist =
              orderedComponents(cal.getComponents());
      for (final CalendarComponent comp: clist) {
        if (comp instanceof VTimeZone) {
          ic.addTimeZone(doTimeZone((VTimeZone)comp));
          continue;
        }

        if ((comp instanceof VFreeBusy) ||
                (comp instanceof VEvent) ||
                (comp instanceof VToDo) ||
                (comp instanceof VPoll) ||
                (comp instanceof VAvailability)) {
          final GetEntityResponse<EventInfo> eiResp =
                  Ical2BwEvent.toEvent(cb, col, ic, comp,
                                       mergeAttendees);

          if (eiResp.isError()) {
            if (eiResp.getException() != null) {
              throw eiResp.getException();
            }
            throw new BedeworkException(eiResp.toString());
          }

          if (eiResp.isOk()) {
            ic.addComponent(eiResp.getEntity());
          }
        }
      }

      /* Prune out any overrides we didn't see */
      for (final EventInfo ei: ic.getComponents()) {
        if (!Util.isEmpty(ei.getOverrides())) {
          for (final EventInfo rei: ei.getOverrides()) {
            if (!rei.recurrenceSeen) {
              ei.removeOverride(rei.getRecurrenceId());
            }
          }
        }
      }

      return ic;
    } catch (final BedeworkException bfe) {
      throw bfe;
    } catch (final Throwable t) {
      throw new BedeworkException(t);
    }
  }

  /** Get the method for the calendar
   *
   * @param val Calendar object
   * @return String method
   */
  public static String getMethod(final Calendar val) {
    final Property prop = val.getProperties().getProperty(Property.METHOD);
    if (prop == null) {
      return null;
    }

    return prop.getValue();
  }

  /* Temp - update ical4j * /
  private static class CalBuilder extends CalendarBuilder {
    public CalBuilder(final CalendarParser parser,
                      final TimeZoneRegistry registry) {
      super(parser, registry);
    }

    @Override
    public void parameter(final String name, final String value) throws URISyntaxException {
      /* Here we look to see if the parameter is the TZID parameter. If it is and it
       * has a value then we fetch the actual timezone from our registry.
       *
       * This fetch may actually fetch a differently named timezone as we may
       * do some alias matching.
       *
       * If the fetched timezone has the same name we are done. Otherwise we
       * update the parameter value to reflect the actual fetched timezone.
       * /
      super.parameter(name, value);

      if (!name.toUpperCase().equals(Parameter.TZID)) {
        return;
      }

      Parameter tzParam = property.getParameter(Parameter.TZID);

      if (tzParam == null) {
        return;
      }

      /* Have an id wit a value * /

      TimeZone timezone = null;

      if (property  instanceof DateProperty){
        timezone = ((DateProperty)property).getTimeZone();
      } else  if (property  instanceof DateListProperty){
        timezone = ((DateListProperty) property).getTimeZone();
      }

      if (timezone == null) {
        return;
      }

      if (timezone.getID().equals(tzParam.getValue())) {
        return;
      }

      /* Fetched timezone has a different id * /

      ParameterList pl = property.getParameters();

      tzParam = ParameterFactoryImpl.getInstance().createParameter(Parameter.TZID,
                                                                   timezone.getID());
      pl.replace(tzParam);

    }
  }
  */

  /* ====================================================================
                      Private methods
     ==================================================================== */

  private void addToCalendar(final Calendar cal,
                             final EventInfo val,
                             final TreeSet<String> added) {
    String currentPrincipal = null;
    final BwPrincipal principal = cb.getPrincipal();

    if (principal != null) {
      currentPrincipal = principal.getPrincipalRef();
    }

    final BwEvent ev = val.getEvent();

    final EventTimeZonesRegistry tzreg =
            new EventTimeZonesRegistry(this, ev);

    if (!cb.getTimezonesByReference()) {
      /* Add referenced timezones to the calendar */
      addIcalTimezones(cal, ev, added, tzreg);
    }

    if (!ev.getSuppressed()) {
      /* Add it to the calendar */
      final Component comp;

      if (ev.getEntityType() == IcalDefs.entityTypeFreeAndBusy) {
        comp = VFreeUtil.toVFreeBusy(ev);
      } else {
        comp = BwEvent2Ical.convert(val, false, tzreg,
                                    currentPrincipal);
      }
      cal.getComponents().add((CalendarComponent)comp);
    }

    if (val.getNumOverrides() > 0) {
      for (final EventInfo oei: val.getOverrides()) {
        cal.getComponents().add(
                (CalendarComponent)BwEvent2Ical
                        .convert(oei, true, tzreg,
                                 currentPrincipal));
      }
    }
  }

  /* Order all the components so that they are in the following order:
   *
   * XXX should use a TreeSet with a wrapper round the object and sort them including
   * dates etc.
   *
   * 1. Timezones
   * 2. Events without recurrence id
   * 3. Events with recurrence id
   * 4. Anything else
   */
  private static Collection<CalendarComponent> orderedComponents(
          final ComponentList<?> clist) {
    final SubList<CalendarComponent> tzs = new SubList<>();
    final SubList<CalendarComponent> fbs = new SubList<>();
    final SubList<CalendarComponent> instances = new SubList<>();
    final SubList<CalendarComponent> masters = new SubList<>();

    for (final Object aClist : clist) {
      final CalendarComponent c = (CalendarComponent)aClist;

      if (c instanceof VFreeBusy) {
        fbs.add(c);
      } else if (c instanceof VTimeZone) {
        tzs.add(c);
      } else if (IcalUtil
              .getProperty(c, Property.RECURRENCE_ID) != null) {
        instances.add(c);
      } else {
        masters.add(c);
      }
    }

    final ArrayList<CalendarComponent> all = new ArrayList<>();

    tzs.appendTo(all);
    masters.appendTo(all);
    instances.appendTo(all);
    fbs.appendTo(all);

    return all;
  }

  private static class SubList<T> {
    ArrayList<T> list;

    void add(final T o) {
      if (list == null) {
        list = new ArrayList<>();
      }
      list.add(o);
    }

    void appendTo(final Collection<T> c) {
      if (list != null) {
        c.addAll(list);
      }
    }
  }

  private TimeZoneInfo doTimeZone(final VTimeZone vtz) {
    final TzId tzid = vtz.getTimeZoneId();

    if (tzid == null) {
      throw new RuntimeException("Missing tzid property");
    }

    final String id = tzid.getValue();

    //if (debug()) {
    //  debug("Got timezone: \n" + vtz.toString() + " with id " + id);
    //}

    try {
      TimeZone tz = Timezones.getTz(id);
      String tzSpec = null;

      if (tz == null) {
        tz = new TimeZone(vtz);
        tzSpec = vtz.toString();
      }

      return new TimeZoneInfo(id, tz, tzSpec);
    } catch (final Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /* If the start or end dates references a timezone, we retrieve the timezone definition
   * and add it to the calendar.
   */
  protected void addIcalTimezones(final Calendar cal,
                                  final Collection<EventInfo> vals) {
    final TreeSet<String> added = new TreeSet<>();

    for (final Object o : vals) {
      if (o instanceof EventInfo) {
        final EventInfo ei = (EventInfo)o;
        BwEvent ev = ei.getEvent();

        if (!ev.getSuppressed()) {
          /* Add referenced timezones to the calendar */
          addIcalTimezones(cal, ev, added,
                           new EventTimeZonesRegistry(this, ev));
        }

        if (ei.getNumOverrides() > 0) {
          for (final EventInfo oei : ei.getOverrides()) {
            ev = oei.getEvent();
            addIcalTimezones(cal, ev, added,
                             new EventTimeZonesRegistry(this, ev));
          }
        }
      }
    }
  }

  /* If the start or end date references a timezone, we retrieve the timezone definition
   * and add it to the calendar.
   */
  private void addIcalTimezones(final Calendar cal, final BwEvent ev,
                                final TreeSet<String> added,
                                final TimeZoneRegistry tzreg) {
    if (ev.getEntityType() == IcalDefs.entityTypeFreeAndBusy) {
      return;
    }

    if (!ev.getForceUTC()) {
      addIcalTimezone(cal,
                      ev.getDtstart().getTzid(),
                      added,
                      tzreg);

      if (ev.getEndType() == StartEndComponent.endTypeDate) {
        addIcalTimezone(cal,
                        ev.getDtend().getTzid(),
                        added,
                        tzreg);
      }

      //if (ev.getRecurrenceId() != null) {
      //  addIcalTimezone(cal, ev.getRecurrenceId().getTzid(), added, tzreg);
      //}
    }
  }

  protected static void setSystemProperties() {
    try {
      System.setProperty("ical4j.unfolding.relaxed", "true");
      System.setProperty("ical4j.parsing.relaxed", "true");
      System.setProperty("ical4j.compatibility.outlook", "true");
    } catch (final Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /* ====================================================================
   *                   Logged methods
   * ==================================================================== */

  private final BwLogger logger = new BwLogger();

  @Override
  public BwLogger getLogger() {
    if ((logger.getLoggedClass() == null) && (logger.getLoggedName() == null)) {
      logger.setLoggedClass(getClass());
    }

    return logger;
  }
}

