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
package org.bedework.convert.xcal;

import org.bedework.calfacade.BwDateTime;
import org.bedework.calfacade.BwXproperty;
import org.bedework.calfacade.BwXproperty.Xpar;
import org.bedework.calfacade.base.BwStringBase;
import org.bedework.util.calendar.ScheduleMethods;
import org.bedework.util.calendar.XcalUtil;
import org.bedework.util.logging.BwLogger;

import ietf.params.xml.ns.icalendar_2.AltrepParamType;
import ietf.params.xml.ns.icalendar_2.ArrayOfParameters;
import ietf.params.xml.ns.icalendar_2.ArrayOfProperties;
import ietf.params.xml.ns.icalendar_2.BaseComponentType;
import ietf.params.xml.ns.icalendar_2.BasePropertyType;
import ietf.params.xml.ns.icalendar_2.DateDatetimePropertyType;
import ietf.params.xml.ns.icalendar_2.IcalendarType;
import ietf.params.xml.ns.icalendar_2.LanguageParamType;
import ietf.params.xml.ns.icalendar_2.MethodPropType;
import ietf.params.xml.ns.icalendar_2.ObjectFactory;
import ietf.params.xml.ns.icalendar_2.ProdidPropType;
import ietf.params.xml.ns.icalendar_2.TzidParamType;
import ietf.params.xml.ns.icalendar_2.VcalendarType;
import ietf.params.xml.ns.icalendar_2.VersionPropType;
import ietf.params.xml.ns.icalendar_2.XBedeworkExsynchEndtzidPropType;
import ietf.params.xml.ns.icalendar_2.XBedeworkExsynchLastmodPropType;
import ietf.params.xml.ns.icalendar_2.XBedeworkExsynchStarttzidPropType;
import ietf.params.xml.ns.icalendar_2.XBedeworkMaxTicketsPerUserPropType;
import ietf.params.xml.ns.icalendar_2.XBedeworkMaxTicketsPropType;
import ietf.params.xml.ns.icalendar_2.XBedeworkRegistrationEndPropType;
import ietf.params.xml.ns.icalendar_2.XBedeworkRegistrationStartPropType;
import ietf.params.xml.ns.icalendar_2.XBedeworkUidParamType;
import ietf.params.xml.ns.icalendar_2.XBedeworkWaitListLimitPropType;
import ietf.params.xml.ns.icalendar_2.XBedeworkWrappedNameParamType;
import ietf.params.xml.ns.icalendar_2.XBedeworkWrapperPropType;
import ietf.params.xml.ns.icalendar_2.XBwCategoriesPropType;
import ietf.params.xml.ns.icalendar_2.XBwContactPropType;
import ietf.params.xml.ns.icalendar_2.XBwLocationPropType;
import net.fortuna.ical4j.model.NumberList;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import jakarta.xml.bind.JAXBElement;

/** Class to provide utility methods for translating  between XML and Bedework
 * alarm representations
 *
 * @author Mike Douglass   douglm  rpi.edu
 */
public class Xutil {
  private static final BwLogger logger =
          new BwLogger().setLoggedClass(Xutil.class);

  protected static ObjectFactory of = new ObjectFactory();

  protected static void listFromNumberList(
          final List<String> l,
          final NumberList nl) {
    if (nl == null) {
      return;
    }

    for (final var o: nl) {
      l.add(o.toString());
    }
  }

  protected static void intlistFromNumberList(
          final List<Integer> l,
          final NumberList nl) {
    if (nl == null) {
      return;
    }

    l.addAll(nl);
  }

  protected static void bigintlistFromNumberList(
          final List<BigInteger> l,
          final NumberList nl) {
    if (nl == null) {
      return;
    }

    for (final Integer o: nl) {
      l.add(BigInteger.valueOf(o.longValue()));
    }
  }

  protected static BasePropertyType tzidProp(final BasePropertyType prop,
                                             final String val) {
    if (val == null) {
      return prop;
    }

    final ArrayOfParameters pars = getAop(prop);

    final TzidParamType tzid = new TzidParamType();
    tzid.setText(val);
    final JAXBElement<TzidParamType> t = of.createTzid(tzid);
    pars.getBaseParameter().add(t);

    return prop;
  }

  protected static BasePropertyType altrepProp(final BasePropertyType prop,
                                               final String val) {
    if (val == null) {
      return prop;
    }

    final ArrayOfParameters pars = getAop(prop);

    final AltrepParamType a = new AltrepParamType();
    a.setUri(val);
    final JAXBElement<AltrepParamType> param = of.createAltrep(a);
    pars.getBaseParameter().add(param);

    return prop;
  }

  protected static BasePropertyType uidProp(final BasePropertyType prop,
                                            final String uid) {
    if (uid == null) {
      return prop;
    }

    final ArrayOfParameters pars = getAop(prop);

    final XBedeworkUidParamType x = new XBedeworkUidParamType();
    x.setText(uid);
    final JAXBElement<XBedeworkUidParamType> param = of.createXBedeworkUid(
            x);
    pars.getBaseParameter().add(param);

    return prop;
  }

  protected static BasePropertyType langProp(final BasePropertyType prop,
                                             final BwStringBase<?> s) {
    final String lang = s.getLang();

    if (lang == null) {
      return prop;
    }

    final ArrayOfParameters pars = getAop(prop);

    final LanguageParamType l = new LanguageParamType();
    l.setText(lang);

    final JAXBElement<LanguageParamType> param = of.createLanguage(l);
    pars.getBaseParameter().add(param);

    return prop;
  }

  protected static ArrayOfParameters getAop(final BasePropertyType prop) {
    ArrayOfParameters pars = prop.getParameters();

    if (pars == null) {
      pars = new ArrayOfParameters();
      prop.setParameters(pars);
    }

    return pars;
  }

  protected static DateDatetimePropertyType makeDateDatetime(
          final DateDatetimePropertyType p,
          final BwDateTime dt,
          final boolean forceUTC) {
    final String dtval;
    if (forceUTC) {
      dtval = dt.getDate();
    } else if (!dt.getDateType()) {
      dtval = dt.getDtval();
    } else {
      dtval = dt.getDtval().substring(0, 8);
    }

    XcalUtil.initDt(p, dtval, dt.getTzid());

    return p;
  }

  public static IcalendarType initCalendar(final String prodId,
                                           final int methodType) {
    final IcalendarType ical = new IcalendarType();
    final VcalendarType vcal = new VcalendarType();

    ical.getVcalendar().add(vcal);

    vcal.setProperties(new ArrayOfProperties());
    final List<JAXBElement<? extends BasePropertyType>> pl =
            vcal.getProperties().getBasePropertyOrTzid();

    final ProdidPropType prod = new ProdidPropType();
    prod.setText(prodId);
    pl.add(of.createProdid(prod));

    final VersionPropType vers = new VersionPropType();
    vers.setText("2.0");
    pl.add(of.createVersion(vers));

    if ((methodType > ScheduleMethods.methodTypeNone) &&
            (methodType < ScheduleMethods.methodTypeUnknown)) {
      final MethodPropType m = new MethodPropType();

      m.setText(ScheduleMethods.methods[methodType]);
      pl.add(of.createMethod(m));
    }

    return ical;
  }

  /**
   * @param pl properties list
   * @param xprops x-properties
   * @param pattern to allow skipping
   * @param masterClass we're building
   * @param wrapXprops wrap x-properties in bedework object - allows
   *                   us to push them through soap
   */
  @SuppressWarnings("deprecation")
  public static void xpropertiesToXcal(
          final List<JAXBElement<? extends BasePropertyType>> pl,
          final List<BwXproperty> xprops,
          final BaseComponentType pattern,
          final Class<?> masterClass,
          final boolean wrapXprops) {
    for (final BwXproperty x: xprops) {
      // Skip any timezone we saved in the event.
      final String xname = x.getName();
      final String val = x.getValue();

      if (xname.startsWith(BwXproperty.bedeworkTimezonePrefix)) {
        continue;
      }

      switch (xname) {
        case BwXproperty.bedeworkExsynchEndtzid -> {
          if (!emit(pattern, masterClass,
                    XBedeworkExsynchEndtzidPropType.class)) {
            continue;
          }

          final XBedeworkExsynchEndtzidPropType p =
                  new XBedeworkExsynchEndtzidPropType();
          p.setText(val);

          pl.add(of.createXBedeworkExsynchEndtzid(p));
          continue;
        }
        case BwXproperty.bedeworkExsynchLastmod -> {
          if (!emit(pattern, masterClass,
                    XBedeworkExsynchLastmodPropType.class)) {
            continue;
          }

          final XBedeworkExsynchLastmodPropType p =
                  new XBedeworkExsynchLastmodPropType();
          p.setText(val);

          pl.add(of.createXBedeworkExsynchLastmod(p));
          continue;
        }
        case BwXproperty.bedeworkExsynchOrganizer -> {
          continue;
        }
        case BwXproperty.bedeworkExsynchStarttzid -> {
          if (!emit(pattern, masterClass,
                    XBedeworkExsynchStarttzidPropType.class)) {
            continue;
          }

          final XBedeworkExsynchStarttzidPropType p =
                  new XBedeworkExsynchStarttzidPropType();
          p.setText(val);

          pl.add(of.createXBedeworkExsynchStarttzid(p));
          continue;
        }
        case BwXproperty.bedeworkEventRegStart -> {
          if (!emit(pattern, masterClass,
                    XBedeworkRegistrationStartPropType.class)) {
            continue;
          }

          final XBedeworkRegistrationStartPropType p =
                  new XBedeworkRegistrationStartPropType();
          String tzid = null;
          for (final Xpar xp: x.getParameters()) {
            if (xp.getName().equalsIgnoreCase("TZID")) {
              tzid = xp.getValue();
              break;
            }
          }

          XcalUtil.initDt(p, val, tzid);

          pl.add(of.createXBedeworkRegistrationStart(p));
          continue;
        }
        case BwXproperty.bedeworkEventRegEnd -> {
          if (!emit(pattern, masterClass,
                    XBedeworkRegistrationEndPropType.class)) {
            continue;
          }

          final XBedeworkRegistrationEndPropType p =
                  new XBedeworkRegistrationEndPropType();
          String tzid = null;
          for (final Xpar xp: x.getParameters()) {
            if (xp.getName().equalsIgnoreCase("TZID")) {
              tzid = xp.getValue();
              break;
            }
          }

          XcalUtil.initDt(p, val, tzid);

          pl.add(of.createXBedeworkRegistrationEnd(p));
          continue;
        }
        case BwXproperty.bedeworkEventRegMaxTickets -> {
          if (!emit(pattern, masterClass,
                    XBedeworkMaxTicketsPropType.class)) {
            continue;
          }

          final XBedeworkMaxTicketsPropType p =
                  new XBedeworkMaxTicketsPropType();

          p.setInteger(
                  BigInteger.valueOf(Long.parseLong(val.trim())));

          pl.add(of.createXBedeworkMaxTickets(p));
          continue;
        }
        case BwXproperty.bedeworkEventRegMaxTicketsPerUser -> {
          if (!emit(pattern, masterClass,
                    XBedeworkMaxTicketsPerUserPropType.class)) {
            continue;
          }

          final XBedeworkMaxTicketsPerUserPropType p =
                  new XBedeworkMaxTicketsPerUserPropType();

          p.setInteger(
                  BigInteger.valueOf(Long.parseLong(val.trim())));

          pl.add(of.createXBedeworkMaxTicketsPerUser(p));
          continue;
        }
        case BwXproperty.bedeworkEventRegWaitListLimit -> {
          if (!emit(pattern, masterClass,
                    XBedeworkWaitListLimitPropType.class)) {
            continue;
          }

          final XBedeworkWaitListLimitPropType p =
                  new XBedeworkWaitListLimitPropType();
          p.setText(val);

          pl.add(of.createXBedeworkWaitListLimit(p));
          continue;
        }
        case BwXproperty.xBedeworkCategories -> {
          if (!emit(pattern, masterClass,
                    XBwCategoriesPropType.class)) {
            continue;
          }

          final XBwCategoriesPropType p = new XBwCategoriesPropType();

          p.getText().add(val);

          pl.add(of.createXBedeworkCategories(p));
          continue;
        }
        case BwXproperty.xBedeworkContact -> {
          if (!emit(pattern, masterClass, XBwContactPropType.class)) {
            continue;
          }

          final XBwContactPropType p = new XBwContactPropType();

          p.setText(val);

          pl.add(of.createXBedeworkContact(p));
          continue;
        }
        case BwXproperty.xBedeworkLocation -> {
          if (!emit(pattern, masterClass,
                    XBwLocationPropType.class)) {
            continue;
          }

          final XBwLocationPropType p = new XBwLocationPropType();

          p.setText(val);

          pl.add(of.createXBedeworkLocation(p));
          continue;
        }
      }

      if (!wrapXprops) {
        if (logger.debug()) {
          logger.warn("Not handing x-property " + xname);
        }
        continue;
      }

      final XBedeworkWrapperPropType wrapper =
              new XBedeworkWrapperPropType();

      if (x.getParameters() != null) {
        for (final Xpar xp : x.getParameters()) {
          xparam(wrapper, xp);
        }
      }

      final XBedeworkWrappedNameParamType wnp =
              new XBedeworkWrappedNameParamType();
      wnp.setText(x.getName());
      if (wrapper.getParameters() == null) {
        wrapper.setParameters(new ArrayOfParameters());
      }
      wrapper.getParameters().getBaseParameter().add(
              of.createXBedeworkWrappedName(wnp));
      wrapper.setText(val);

      pl.add(of.createXBedeworkWrapper(wrapper));
    }
  }

  /** Convert a parameter
   * @param prop - parameters go here
   * @param xp - a parameter
   */
  protected static void xparam(final BasePropertyType prop,
                               final Xpar xp) {
    final ArrayOfParameters aop = getAop(prop);

    if (xp.getName().equalsIgnoreCase("tzid")) {
      final TzidParamType tz = new TzidParamType();
      tz.setText(xp.getValue());

      aop.getBaseParameter().add(of.createTzid(tz));
      return;
    }

    if (xp.getName().equalsIgnoreCase("altrep")) {
      altrepProp(prop, xp.getValue());

      //return;
    }
  }

  protected static boolean emit(final BaseComponentType pattern,
                                final Class<?> compCl,
                                final Class<?>... cl) {
    if (pattern == null) {
      return true;
    }

    if (!compCl.getName().equals(pattern.getClass().getName())) {
      return false;
    }

    if ((cl == null) || (cl.length == 0)) {
      // Any property
      return true;
    }

    final String className = cl[0].getName();

    if (BasePropertyType.class.isAssignableFrom(cl[0])) {
      if (pattern.getProperties() == null) {
        return false;
      }

      final List<JAXBElement<? extends BasePropertyType>> patternProps =
         pattern.getProperties().getBasePropertyOrTzid();

      for (final JAXBElement<? extends BasePropertyType> jp: patternProps) {
        if (jp.getValue().getClass().getName().equals(className)) {
          return true;
        }
      }

      return false;
    }

    final List<JAXBElement<? extends BaseComponentType>> patternComps =
      XcalUtil.getComponents(pattern);

    if (patternComps == null) {
      return false;
    }

    // Check for component

    for (final var jp: patternComps) {
      if (jp.getValue().getClass().getName().equals(className)) {
        return emit(pattern, cl[0], Arrays.copyOfRange(cl, 1, cl.length));
      }
    }

    return false;
  }
}
