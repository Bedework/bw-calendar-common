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

import org.bedework.base.response.GetEntitiesResponse;
import org.bedework.base.response.GetEntityResponse;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwLocation;
import org.bedework.calfacade.BwXproperty;
import org.bedework.schemaorg.impl.SOMapper;
import org.bedework.schemaorg.model.SOTypes;
import org.bedework.schemaorg.model.values.SOGeoCoordinates;
import org.bedework.schemaorg.model.values.SOPlace;
import org.bedework.schemaorg.model.values.SOPostalAddress;
import org.bedework.util.misc.Util;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentContainer;
import net.fortuna.ical4j.model.component.VLocation;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.parameter.Schema;
import net.fortuna.ical4j.model.property.LocationType;
import net.fortuna.ical4j.model.property.StructuredData;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Url;
import net.fortuna.ical4j.model.property.immutable.ImmutableRelativeTo;

import java.net.URI;
import java.util.Collection;

import static org.bedework.util.calendar.IcalendarUtil.fromBuilder;

/** Class to provide utility methods for ical4j classes
 *
 * @author Mike Douglass   douglm    rpi.edu
 */
public class VLocationUtil {
  private final SOMapper somapper = new SOMapper();

  public enum Relto {
    start,
    end,
    none
  }

  /**
   * @param loc the location
   * @return A VLOCATION object.
   */
  public GetEntityResponse<VLocation> toVlocation(
          final BwLocation loc,
          final Relto relTo) {
    final GetEntityResponse<VLocation> resp = new GetEntityResponse<>();
    final VLocation vloc = new VLocation();

    try {
      final var plist = vloc.getProperties();
      final SOPlace pl =
              (SOPlace)somapper.getJFactory().newValue(SOTypes.typePlace);

      final SOPostalAddress pa =
              (SOPostalAddress)somapper.getJFactory()
                                       .newValue(SOTypes.typePostalAddress);

      plist.add(new Uid(loc.getUid()));
      pa.setIdentifier(loc.getUid());

      if (relTo == Relto.start) {
        plist.add(ImmutableRelativeTo.START);
      } else if (relTo == Relto.end) {
        plist.add(ImmutableRelativeTo.END);
      }

      if (loc.getGeouri() != null) {
        final var geouri = new URI(loc.getGeouri());
        plist.add(new Url(geouri));
        final SOGeoCoordinates geo =
                (SOGeoCoordinates)somapper.getJFactory()
                                          .newValue(SOTypes.typeGeoCoordinates);

        geo.setURI(geouri);
        pl.setGeo(geo);
      }

      if (loc.getLoctype() != null) {
        plist.add(new LocationType(loc.getLoctype()));
      }

      pa.setName(loc.getAddressField());
      pa.setStreetAddress(loc.getStreet());
      pa.setAddressLocality(loc.getCity());
      pa.setAddressRegion(loc.getState());
      pa.setAddressCountry(loc.getCountry());
      pa.setPostalCode(loc.getZip());

      pl.setAddress(pa);

      final var sdata = new StructuredData(
              pl.writeValueAsStringFormatted(somapper));
      sdata.getParameters().add(new Schema(somapper.getSchema("Place")));

      plist.add(sdata);
    } catch (final Throwable t) {
      return resp.error(t);
    }

    return resp.setEntity(vloc).ok();
  }

  public static GetEntitiesResponse<VLocation> getVlocations(
          final BwEvent ev) {
    final var geresp = new GetEntitiesResponse<VLocation>();
    final var xlocs = ev.getXproperties(BwXproperty.xBedeworkVLocation);

    if (Util.isEmpty(xlocs)) {
      return geresp.notFound();
    }

    // Better if ical4j supported sub-component parsing

    final StringBuilder sb = new StringBuilder(
            """
              BEGIN:VCALENDAR
              PRODID://Bedework.org//BedeWork V3.9//EN
              VERSION:2.0
              BEGIN:VTODO
              UID:0123
              """);

    for (final var xloc: xlocs) {
      sb.append(xloc.getValue());
    }

    sb.append(
            """
             END:VTODO
             END:VCALENDAR
             """);

    final Calendar ical = fromBuilder(sb.toString());

    final VToDo comp = ical.getComponent(Component.VTODO);

    final var vlocs = ((ComponentContainer<?>)comp).getComponents(Component.VLOCATION);
//    for (final var o: vlocs) {
 //     geresp.addEntity((VLocation)o);
  //  }

    return geresp.addAll((Collection<VLocation>)vlocs);
  }
}

