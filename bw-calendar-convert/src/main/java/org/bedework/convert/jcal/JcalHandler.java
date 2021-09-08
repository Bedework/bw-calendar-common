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
package org.bedework.convert.jcal;

import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwVersion;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.convert.EventTimeZonesRegistry;
import org.bedework.convert.ical.BwEvent2Ical;
import org.bedework.convert.ical.VFreeUtil;
import org.bedework.util.calendar.IcalDefs;
import org.bedework.util.calendar.ScheduleMethods;
import org.bedework.util.logging.BwLogger;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentContainer;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;

import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;

/** Class to handle jcal.
 *
 * @author Mike Douglass douglm rpi.edu
 * @version 1.0
 */
public class JcalHandler implements Serializable {
  private static final BwLogger logger =
          new BwLogger().setLoggedClass(JcalHandler.class);

  private final static JsonFactory jsonFactory;

  static {
    jsonFactory = new JsonFactory();
    jsonFactory.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
    jsonFactory.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
  }

  public static String toJcal(final Collection<EventInfo> vals,
                              final int methodType,
                              final String currentPrincipal,
                              final EventTimeZonesRegistry tzreg) throws CalFacadeException {
    final StringWriter sw = new StringWriter();

    outJcal(sw, vals, methodType, currentPrincipal, tzreg);

    return sw.toString();
  }

  public static String toJcal(final Calendar cal) throws CalFacadeException {
    final StringWriter sw = new StringWriter();

    outJcal(sw, cal);

    return sw.toString();
  }

  public static void outJcal(final Writer wtr,
                             final Calendar cal) throws CalFacadeException {
    try {
      final JsonGenerator jgen = jsonFactory.createGenerator(wtr);

      if (logger.debug()) {
        jgen.useDefaultPrettyPrinter();
      }

      jgen.writeStartArray(); // for vcalendar

      jgen.writeString("vcalendar");
      jgen.writeStartArray();

      for (final Property p: cal.getProperties()) {
        JsonProperty.addFields(jgen, p);
      }

      jgen.writeEndArray(); // End event properties

      /* Output subcomponents
       */

      jgen.writeStartArray(); // for components

      for (final Component comp: cal.getComponents()) {
        outComp(jgen, comp);
      }

      jgen.writeEndArray(); // for components

      jgen.writeEndArray(); // for vcalendar

      jgen.flush();
    } catch (final CalFacadeException cfe) {
      throw cfe;
    } catch (final Throwable t) {
      throw new CalFacadeException(t);
    }
  }

  public static void outJcal(final Writer wtr,
                             final Collection<EventInfo> vals,
                             final int methodType,
                             final String currentPrincipal,
                             final EventTimeZonesRegistry tzreg) throws CalFacadeException {
    try {
      final JsonGenerator jgen = jsonFactory.createGenerator(wtr);

      if (logger.debug()) {
        jgen.useDefaultPrettyPrinter();
      }

      jgen.writeStartArray();

      calendarProps(jgen, methodType);

      jgen.writeStartArray(); // for components

      for (final EventInfo ei: vals) {
        final BwEvent ev = ei.getEvent();

        final Component comp;
        if (ev.getEntityType() == IcalDefs.entityTypeFreeAndBusy) {
          comp = VFreeUtil.toVFreeBusy(ev);
        } else {
          comp = BwEvent2Ical.convert(ei, false, tzreg,
                                      currentPrincipal);
        }

        outComp(jgen, comp);

        if (ei.getNumOverrides() > 0) {
          for (final EventInfo oei: ei.getOverrides()) {
            outComp(jgen, BwEvent2Ical.convert(oei,
                                               true,
                                               tzreg,
                                               currentPrincipal));
          }
        }
      }

      jgen.writeEndArray(); // for components

      jgen.writeEndArray();

      jgen.flush();
    } catch (final CalFacadeException cfe) {
      throw cfe;
    } catch (final Throwable t) {
      throw new CalFacadeException(t);
    }
  }

  private static void outComp(final JsonGenerator jgen,
                              final Component comp) throws CalFacadeException {
    try {
      jgen.writeStartArray();

      jgen.writeString(comp.getName().toLowerCase());
      jgen.writeStartArray();

      for (final Property p: comp.getProperties()) {
        JsonProperty.addFields(jgen, p);
      }

      jgen.writeEndArray(); // End event properties

      /* Output subcomponents
       */
      jgen.writeStartArray();

      if (comp instanceof ComponentContainer) {
        final ComponentList<Component> cl = ((ComponentContainer<Component>)comp).getComponents();

        if (cl != null) {
          for (final Component c: cl) {
            outComp(jgen, c);
          }
        }
      }

      jgen.writeEndArray(); // end subcomponents

      jgen.writeEndArray(); // end event
    } catch (final Throwable t) {
      throw new CalFacadeException(t);
    }
  }

  private static void calendarProps(final JsonGenerator jgen,
                                    final int methodType) throws CalFacadeException {
    try {
      jgen.writeString("vcalendar");

      jgen.writeStartArray();

      JsonProperty.addFields(jgen, new ProdId(BwVersion.prodId));
      JsonProperty.addFields(jgen, Version.VERSION_2_0);

      if ((methodType > ScheduleMethods.methodTypeNone) &&
              (methodType < ScheduleMethods.methodTypeUnknown)) {
        JsonProperty.addFields(jgen, new Method(
                ScheduleMethods.methods[methodType]));
      }

      jgen.writeEndArray();
    } catch (final Throwable t) {
      throw new CalFacadeException(t);
    }
  }
}
