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

import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.SchedulingOwner;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.util.calendar.IcalDefs;
import org.bedework.util.calendar.IcalDefs.IcalComponentType;
import org.bedework.util.calendar.ScheduleMethods;
import org.bedework.base.ToString;
import org.bedework.util.timezones.Timezones;

import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/** Class to represent an RFC icalendar object converted to an internal form.
 *
 * <p>This object acts as it's own timezone registry allowing us to handle
 * timezones defined in the event objects which we do not store in the system
 * timezone store.
 *
 * @author Mike Douglass douglm@rpi.edu
 * @version 1.0
 */
public class Icalendar implements TimeZoneRegistry, ScheduleMethods, Serializable {
  private String prodid;

  private String version;

  private String calscale;

  private String method;

  private HashMap<String, TimeZone> localTzs;

  /** We may use internal timezones or be forced to use an unknown tz shipped in
   * with an event
   *
   * @author douglm
   */
  public static class TimeZoneInfo {
    /** It's id */
    public String tzid;

    /** The actual timezone */
    public TimeZone tz;

    /** Non-null if we are using an unknown external specification */
    public String tzSpec;

    /** Constructor
     * @param tzid the id
     * @param tz it's internal representation
     * @param tzSpec the string form
     */
    public TimeZoneInfo(final String tzid,
                        final TimeZone tz,
                        final String tzSpec) {
      this.tzid = tzid;
      this.tz = tz;
      this.tzSpec = tzSpec;
    }
  }

  private Collection<TimeZoneInfo> timeZones;

  private Collection<EventInfo> components;

  private IcalComponentType componentType = IcalComponentType.none;

  /**
   * @param val product id
   */
  public void setProdid(final String val) {
    prodid = val;
  }

  /**
   * @return String
   */
  public String getProdid() {
    return prodid;
  }

  /**
   * @param val version
   */
  public void setVersion(final String val) {
    version = val;
  }

  /**
   * @return String
   */
  public String getVersion() {
    return version;
  }

  /**
   * @param val cal scale
   */
  public void setCalscale(final String val) {
    calscale = val;
  }

  /**
   * @return String
   */
  public String getCalscale() {
    return calscale;
  }

  /**
   * @param val method
   */
  public void setMethod(final String val) {
    method = val;
  }

  /**
   * @return String
   */
  public String getMethod() {
    return method;
  }

  /**
   * @return Collection of timezone info
   */
  public Collection<TimeZoneInfo> getTimeZones() {
    if (timeZones == null) {
      timeZones = new ArrayList<>();
    }
    return timeZones;
  }

  /** Add a timezone
   *
   * @param tzi  TimeZoneInfo
   */
  public void addTimeZone(final TimeZoneInfo tzi) {
    getTimeZones().add(tzi);
  }

  /**
   * @return Collection
   */
  public Collection<EventInfo> getComponents() {
    if (components == null) {
      components = new ArrayList<>();
    }
    return components;
  }

  /**
   * @param val component type for the collection
   */
  public void setComponentType(final IcalComponentType val) {
    if ((componentType == IcalComponentType.none) ||
        (componentType == val)) {
      componentType = val;
    } else {
      componentType = IcalComponentType.mixed;
    }
  }

  /**
   * @return ComponentType
   */
  public IcalComponentType getComponentType() {
    return componentType;
  }

  /**
   * @param val method type
   */
  public void setMethodType(final int val) {
    if (val == methodTypeNone) {
      setMethod(null);
      return;
    }

    setMethod(getMethodName(val));
  }

  /**
   * @return int
   */
  public int getMethodType() {
    return getMethodType(method);
  }

  /**
   * @param val   String method name
   * @return int
   */
  public static int getMethodType(final String val) {
    if (val == null) {
      return methodTypeNone;
    }

    for (int i = 1; i < methods.length; i++) {
      if (methods[i].equals(val)) {
        return i;
      }
    }

    return methodTypeUnknown;
  }

  /**
   * @param mt method index
   * @return A string value for the method
   */
  public static String getMethodName(final int mt) {
    if (mt < methods.length) {
      return methods[mt];
    }

    return "UNKNOWN";
  }

  /** An event or a free-busy request may contain an organizer. Return it if
   * it is present.
   *
   * @return SchedulingOwner object. Check noOwner() for presence.
   */
  public SchedulingOwner getOrganizer() {
    if (size() != 1) {
      return null;
    }

    final Object o = iterator().next();
    if (o instanceof final EventInfo ei) {
      return ei.getEvent().getSchedulingInfo().getSchedulingOwner();

    }

    return null;
  }

  /**
   * @return EventInfo
   */
  public EventInfo getEventInfo() {
    //if ((size() != 1) || (getComponentType() != ComponentType.event)) {
    //  throw new RuntimeException("org.bedework.icalendar.component.not.event");
    //}

    return (EventInfo)iterator().next();
  }

  /**
   * @param val the (possible) event object
   */
  public void addComponent(final EventInfo val) {
    final BwEvent ev = val.getEvent();

    if (ev.getEntityType() == IcalDefs.entityTypeEvent) {
      setComponentType(IcalComponentType.event);
    } else if (ev.getEntityType() == IcalDefs.entityTypeTodo) {
      setComponentType(IcalComponentType.todo);
    } else if (ev.getEntityType() == IcalDefs.entityTypeJournal) {
      setComponentType(IcalComponentType.journal);
    } else if (ev.getEntityType() == IcalDefs.entityTypeFreeAndBusy) {
      setComponentType(IcalComponentType.freebusy);
    } else if (ev.getEntityType() == IcalDefs.entityTypeVavailability) {
      setComponentType(IcalComponentType.vavailability);
    } else if (ev.getEntityType() == IcalDefs.entityTypeAvailable) {
      setComponentType(IcalComponentType.available);
    } else if (ev.getEntityType() == IcalDefs.entityTypeVpoll) {
      setComponentType(IcalComponentType.vpoll);
    } else {
      throw new RuntimeException("org.bedework.bad.entitytype");
    }

    getComponents().add(val);
  }

  /**
   * @return Iterator
   */
  public Iterator<?> iterator() {
    return components.iterator();
  }

  /**
   * @return int
   */
  public int size() {
    if (components == null) {
      return 0;
    }

    return components.size();
  }

  /** True for valid itip method
   *
   * @return boolean
   */
  public boolean validItipMethodType() {
    return validItipMethodType(getMethodType());
  }

  /** True for itip request type method
   *
   * @return boolean
   */
  public boolean requestMethodType() {
    return itipRequestMethodType(getMethodType());
  }

  /** True for itip reply type method
   *
   * @return boolean
   */
  public boolean replyMethodType() {
    return itipReplyMethodType(getMethodType());
  }

  /** True for itip request type method
   *
   * @param mt  method
   * @return boolean
   */
  public static boolean itipRequestMethodType(final int mt) {
    return (mt == methodTypeAdd) ||
            (mt == methodTypeCancel) ||
            (mt == methodTypeDeclineCounter) ||
            (mt == methodTypePublish) ||
            (mt == methodTypePollStatus) ||
            (mt == methodTypeRequest);
  }

  /** True for itip reply type method
   *
   * @param mt  method
   * @return boolean
   */
  public static boolean itipReplyMethodType(final int mt) {
    return (mt == methodTypeCounter) ||
            (mt == methodTypeRefresh) ||
            (mt == methodTypeReply);
  }

  /** True for valid itip method
   *
   * @param val itip method to test
   * @return boolean
   */
  public static boolean validItipMethodType(final int val) {
    if (val == methodTypeNone) {
      return false;
    }

    if (val == methodTypeUnknown) {
      return false;
    }

    return val < methods.length;
  }

  /** True for valid itip method for given component type
   *
   * @param val itip method to test
   * @param type component type
   * @return boolean
   */
  public static boolean validItipMethodType(final int val,
                                            final IcalComponentType type) {
    if (val == methodTypeNone) {
      return false;
    }

    if (val >= methods.length) {
      return false;
    }

    if ((type == IcalComponentType.todo) ||
        (type == IcalComponentType.event)) {
      return (val != methodTypePollStatus);
    }

    if (type == IcalComponentType.vpoll) {
      return true;
    }

    if (type == IcalComponentType.freebusy) {
      return (val == methodTypePublish) ||
              (val == Icalendar.methodTypeRequest) ||
              (val == Icalendar.methodTypeReply);
    }


    return true;
  }

  /** Convert to int method index
   *
   * @param val  String possible method
   * @return int
   */
  public static int findMethodType(final String val) {
    if (val == null) {
      return methodTypeNone;
    }

    for (int i = 1; i < methods.length; i++) {
      if (methods[i].equals(val)) {
        return i;
      }
    }

    return methodTypeUnknown;
  }

  /* ====================================================================
   *                      TimeZoneRegistry methods
   * ==================================================================== */

  @Override
  public void register(final TimeZone timezone) {
    try {
      final TimeZone tz = Timezones.getTz(timezone.getID());
      if (tz != null) {
        // Already three
        return;
      }

      if (localTzs == null) {
        localTzs = new HashMap<>();
      }

      localTzs.put(timezone.getID(), timezone);
    } catch (final Throwable t) {
      throw new RuntimeException(t);
    }
  }

  @Override
  public void register(final TimeZone timezone, final boolean update) {
    register(timezone);
  }

  @Override
  public void clear() {
    if (localTzs != null) {
      localTzs.clear();
    }
  }

  @Override
  public TimeZone getTimeZone(final String id) {
    try {
      final TimeZone tz = Timezones.getTz(id);
      if (tz != null) {
        return  tz;
      }

      if (localTzs == null) {
        return null;
      }

      return localTzs.get(id);
    } catch (final Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /* ====================================================================
   *                        Object methods
   * ==================================================================== */

  @Override
  public String toString() {
    final ToString ts = new ToString(this);
    ts.append("prodid", getProdid());
    ts.append("version", getVersion());
    ts.newLine();
    ts.append("method", String.valueOf(getMethod()));
    ts.append("methodType", getMethodType());
    ts.append("componentType", getComponentType());

    return ts.toString();
  }
}
