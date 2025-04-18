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
import org.bedework.base.response.Response;
import org.bedework.calfacade.BwAttendee;
import org.bedework.calfacade.BwCollection;
import org.bedework.calfacade.BwCategory;
import org.bedework.calfacade.BwContact;
import org.bedework.calfacade.BwDateTime;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwFreeBusyComponent;
import org.bedework.calfacade.BwGeo;
import org.bedework.calfacade.BwLocation;
import org.bedework.calfacade.BwOrganizer;
import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.BwRelatedTo;
import org.bedework.calfacade.BwRequestStatus;
import org.bedework.calfacade.BwString;
import org.bedework.calfacade.BwXproperty;
import org.bedework.calfacade.exc.CalFacadeErrorCode;
import org.bedework.calfacade.ifs.IcalCallback;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.calfacade.util.ChangeTable;
import org.bedework.convert.CnvUtil;
import org.bedework.convert.EventTimeZonesRegistry;
import org.bedework.convert.Icalendar;
import org.bedework.convert.Icalendar.TimeZoneInfo;
import org.bedework.util.calendar.IcalDefs;
import org.bedework.util.calendar.PropertyIndex;
import org.bedework.util.calendar.PropertyIndex.PropertyInfoIndex;
import org.bedework.util.calendar.ScheduleMethods;
import org.bedework.util.logging.BwLogger;
import org.bedework.util.misc.Util;
import org.bedework.util.timezones.Timezones;
import org.bedework.util.xml.tagdefs.XcalTags;

import jakarta.xml.ws.Holder;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentContainer;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.TextList;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.Available;
import net.fortuna.ical4j.model.component.Participant;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VAvailability;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VFreeBusy;
import net.fortuna.ical4j.model.component.VJournal;
import net.fortuna.ical4j.model.component.VLocation;
import net.fortuna.ical4j.model.component.VPoll;
import net.fortuna.ical4j.model.component.VResource;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.parameter.AltRep;
import net.fortuna.ical4j.model.parameter.FbType;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.parameter.XParameter;
import net.fortuna.ical4j.model.property.Attach;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.Categories;
import net.fortuna.ical4j.model.property.Concept;
import net.fortuna.ical4j.model.property.DateListProperty;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Due;
import net.fortuna.ical4j.model.property.Duration;
import net.fortuna.ical4j.model.property.FreeBusy;
import net.fortuna.ical4j.model.property.Geo;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.PercentComplete;
import net.fortuna.ical4j.model.property.PollItemId;
import net.fortuna.ical4j.model.property.PollWinner;
import net.fortuna.ical4j.model.property.Priority;
import net.fortuna.ical4j.model.property.RecurrenceId;
import net.fortuna.ical4j.model.property.RelatedTo;
import net.fortuna.ical4j.model.property.RequestStatus;
import net.fortuna.ical4j.model.property.Resources;
import net.fortuna.ical4j.model.property.Sequence;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.XProperty;
import net.fortuna.ical4j.model.property.immutable.ImmutableRelativeTo;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import static net.fortuna.ical4j.model.Property.RELATIVE_TO;
import static net.fortuna.ical4j.model.property.immutable.ImmutableRelativeTo.START;
import static org.bedework.base.response.Response.Status.failed;
import static org.bedework.base.response.Response.Status.ok;
import static org.bedework.util.calendar.PropertyIndex.PropertyInfoIndex.ATTENDEE;

/** Class to provide utility methods for translating to BwEvent from ical4j classes
 *
 * @author Mike Douglass   douglm  rpi.edu
 */
public class Ical2BwEvent extends IcalUtil {
  private final static BwLogger logger =
          new BwLogger().setLoggedClass(Ical2BwEvent.class);

  /** We are going to try to construct a BwEvent object from a VEvent. This
   * may represent a new event or an update to a pre-existing event. In any
   * case, the VEvent probably has insufficient information to completely
   * reconstitute the event object so we'll get the uid first and retrieve
   * the event if it exists.
   *
   * <p>To put it another way we're doing a diff then update.
   *
   * <p>If it doesn't exist, we'll first fill in the appropriate fields,
   * (non-public, creator, created etc) then for both cases update the
   * remaining fields from the VEvent.
   *
   * <p>Recurring events present some challenges. If there is no recurrence
   * id the vevent represents the master entity which defines the recurrence
   * rules. If a recurrence id is present then the vevent represents a
   * recurrence instance override and we should not attempt to retrieve the
   * actual object but the referenced instance.
   *
   * <p>Also, note that we sorted the components first so we get the master
   * before any instances.
   *
   * <p>If DTSTART, RRULE, EXRULE have changed (also RDATE, EXDATE?) then any
   * existing overrides are unusable. We should delete all overrides and replace
   * with new ones.
   *
   * <p>For an update we have to keep track of which fields were present in
   * the vevent and set all absent fields to null in the BwEvent.
   *
   * @param cb          IcalCallback object
   * @param cal         Needed so we can retrieve the event.
   * @param ical        Icalendar we are converting into. We check its events for
   *                    overrides.
   * @param val         VEvent object
   * @param mergeAttendees True if we should only update our own attendee.
   * @return Response with status and EventInfo object representing new entry or updated entry
   */
  public static GetEntityResponse<EventInfo> toEvent(
          final IcalCallback cb,
          final BwCollection cal,
          final Icalendar ical,
          final Component val,
          final boolean mergeAttendees) {
    final var resp = new GetEntityResponse<EventInfo>();

    if (val == null) {
      return resp.notOk(failed, "No component supplied");
    }

    String currentPrincipal = null;
    final BwPrincipal<?> principal = cb.getPrincipal();

    if (principal != null) {
      currentPrincipal = principal.getPrincipalRef();
    }

    final Holder<Boolean> hasXparams = new Holder<>(Boolean.FALSE);

    final int methodType = ical.getMethodType();

    String attUri = null;

    if (mergeAttendees) {
      // We'll need this later.
      attUri = cb.getCaladdr(cb.getPrincipal().getPrincipalRef());
    }
    
    final String colPath;
    if (cal == null) {
      colPath = null;
    } else {
      colPath = cal.getPath();
    }

    try {
      final PropertyList<Property> pl = val.getProperties();
      boolean vpoll = false;
      boolean event = false;
      boolean task = false;

      if (pl == null) {
        // Empty component
        return resp.notOk(failed, "Empty component");
      }

      final int entityType;

      if (val instanceof VEvent) {
        entityType = IcalDefs.entityTypeEvent;
        event = true;
      } else if (val instanceof VToDo) {
        entityType = IcalDefs.entityTypeTodo;
        task = true;
      } else if (val instanceof VJournal) {
        entityType = IcalDefs.entityTypeJournal;
      } else if (val instanceof VFreeBusy) {
        entityType = IcalDefs.entityTypeFreeAndBusy;
      } else if (val instanceof VAvailability) {
        entityType = IcalDefs.entityTypeVavailability;
      } else if (val instanceof Available) {
        entityType = IcalDefs.entityTypeAvailable;
      } else if (val instanceof VPoll) {
        entityType = IcalDefs.entityTypeVpoll;
        vpoll = true;
      } else {
        return resp.error("org.bedework.invalid.component.type: " +
                val.getName());
      }

      // Get the guid from the component

      String guid = null;

      final Uid uidp = pl.getProperty(Property.UID);
      if (uidp != null) {
        testXparams(uidp, hasXparams);
        guid = uidp.getValue();
      }

      if (guid == null) {
        /* XXX A guid is required - but are there devices out there without a
         *       guid - and if so how do we handle it?
         */
        return resp.notOk(failed, CalFacadeErrorCode.noGuid);
      }

      /* See if we have a recurrence id */

      BwDateTime ridObj = null;
      String rid = null;
      TimeZone ridTz = null;

      final RecurrenceId ridp = pl.getProperty(Property.RECURRENCE_ID);
      if (ridp != null) {
        testXparams(ridp, hasXparams);
        ridObj = BwDateTime.makeBwDateTime(ridp);

        if (ridObj.getRange() != null) {
          /* XXX What do I do with it? */
          logger.warn("TRANS-TO_EVENT: Got a recurrence id range");
        }

        rid = ridObj.getDate();
      }

      EventInfo masterEI = null;
      EventInfo evinfo = null;
      final BwEvent ev;

      /* If we have a recurrence id see if we already have the master (we should
       * get a master + all its overrides).
       *
       * If so find the override and use the annnotation or if no override,
       * make one.
       *
       * If no override retrieve the event, add it to our table and then locate the
       * annotation.
       *
       * If there is no annotation, create one.
       *
       * It's possible we have been sent 'detached' instances of a recurring
       * event. This may happen if we are invited to one or more instances of a
       * meeting. In this case we try to retrieve the master and if it doesn't
       * exist we manufacture one. We consider such an instance an update to
       * that instance only and leave the others alone.
       */

      /* We need this in a couple of places */
      final DtStart dtStart = pl.getProperty(Property.DTSTART);

      /*
      if (rid != null) {
        // See if we have a new master event. If so create a proxy to that event.
        masterEI = findMaster(guid, ical.getComponents());

        if (masterEI == null) {
          masterEI = makeNewEvent(cb, chg, entityType, guid, cal);
          BwEvent e = masterEI.getEvent();

          // XXX This seems bogus
          DtStart mdtStart;

          String bogusDate = "19980118T230000";

          if (dtStart.isUtc()) {
            mdtStart = new DtStart(bogusDate + "Z");
          } else if (dtStart.getTimeZone() == null) {
            mdtStart = new DtStart(bogusDate);
          } else {
            mdtStart = new DtStart(bogusDate + "Z", dtStart.getTimeZone());
          }

          setDates(e, mdtStart, null, null, chg);
          e.setRecurring(true);
          e.addRdate(ridObj);
          e.setSuppressed(true);

          ical.addComponent(masterEI);
        }

        if (masterEI != null) {
          evinfo = masterEI.findOverride(rid);
        }
      }
      */

      /* If this is a recurrence instance see if we can find the master
         We only need this because the master may follow the overrides.
       */
      if (rid != null) {
        // See if we have a new master event. If so create a proxy to this event.
        masterEI = findMaster(guid, ical.getComponents());

        if (masterEI != null) {
          evinfo = masterEI.findOverride(rid);
          evinfo.recurrenceSeen = true;
        }
      }

      if ((evinfo == null) &&
          (cal != null) &&
          (cal.getCalType() != BwCollection.calTypeInbox) &&
          (cal.getCalType() != BwCollection.calTypePendingInbox) &&
          (cal.getCalType() != BwCollection.calTypeOutbox)) {
        if (logger.debug()) {
          logger.debug("TRANS-TO_EVENT: try to fetch event with guid=" + guid);
        }

        final GetEntitiesResponse<EventInfo> eisResp =
                cb.getEvent(colPath, guid);
        if (eisResp.isError()) {
          return resp.fromResponse(eisResp);
        }

        final var eis = eisResp.getEntities();
        if (!Util.isEmpty(eis)) {
          if (eis.size() > 1) {
            // DORECUR - wrong again
            return resp.notOk(failed,
                              "More than one event returned for guid.");
          }
          evinfo = eis.iterator().next();
        }

        if (logger.debug()) {
          if (evinfo != null) {
            logger.debug("TRANS-TO_EVENT: fetched event with guid");
          } else {
            logger.debug("TRANS-TO_EVENT: did not find event with guid");
          }
        }

        if (evinfo != null) {
          if (rid != null) {
            // We just retrieved it's master
            masterEI = evinfo;
            masterEI.setInstanceOnly(true);
            evinfo = masterEI.findOverride(rid);
            evinfo.recurrenceSeen = true;
            ical.addComponent(masterEI);
          } else if (methodType == ScheduleMethods.methodTypeCancel) {
            // This should never have an rid for cancel of entire event.
            evinfo.setInstanceOnly(evinfo.getEvent().getSuppressed());
          } else {
            // Presumably sent an update for the entire event. No longer suppressed master
            evinfo.getEvent().setSuppressed(false);
          }
        } else if (rid != null) {
          /* Manufacture a master for the instance */
          masterEI = CnvUtil.makeNewEvent(cb, entityType, guid, colPath);
          final BwEvent e = masterEI.getEvent();

          // XXX This seems bogus
          final DtStart mdtStart;

          final String bogusDate = "19980118";
          final String bogusTime = "T230000";

          // Base dtstart on the recurrence id.

          final boolean isDateType = ridObj.getDateType();

          if (isDateType) {
            mdtStart = new DtStart(new Date(bogusDate));
          } else if (dtStart.isUtc()) {
            mdtStart = new DtStart(bogusDate + bogusTime + "Z");
          } else if (ridObj.getTzid() == null) {
            mdtStart = new DtStart(bogusDate + bogusTime);
          } else {
            mdtStart = new DtStart(bogusDate + bogusTime,
                                   Timezones.getTz(ridObj.getTzid()));
          }

          IcalUtil.setDates(cb.getPrincipal().getPrincipalRef(),
                            masterEI, mdtStart, null, null);
          e.setRecurring(true);
//          e.addRdate(ridObj);
          final var sum = (Summary)pl.getProperty(Property.SUMMARY);
          e.setSummary(sum.getValue());
          e.setSuppressed(true);

          ical.addComponent(masterEI);

          evinfo = masterEI.findOverride(rid);
          evinfo.recurrenceSeen = true;
          masterEI.setInstanceOnly(rid != null);
        }
      }

      if (evinfo == null) {
        evinfo = CnvUtil.makeNewEvent(cb, entityType, guid, colPath);
      } else if (evinfo.getEvent().getEntityType() != entityType) {
        return resp.notOk(failed,
                          "org.bedework.mismatched.entity.type: " +
                                  val);
      }

      final ChangeTable chg = evinfo.getChangeset(
              cb.getPrincipal().getPrincipalRef());

      if (rid != null) {
        final String evrid = evinfo.getEvent().getRecurrenceId();

        if ((evrid == null) || (!evrid.equals(rid))) {
          logger. warn("Mismatched rid ev=" + evrid + " expected " + rid);
          chg.changed(PropertyInfoIndex.RECURRENCE_ID, evrid, rid); // XXX spurious???
        }

        if (masterEI.getEvent().getSuppressed()) {
          masterEI.getEvent().addRdate(ridObj);
        }
      }

      ev = evinfo.getEvent();
      ev.setScheduleMethod(methodType);

      DtEnd dtEnd = null;

      if (entityType == IcalDefs.entityTypeTodo) {
        final Due due = pl.getProperty(Property.DUE);
        if (due != null ) {
          dtEnd = new DtEnd(due.getParameters(), due.getValue());
        }
      } else {
        dtEnd = pl.getProperty(Property.DTEND);
      }

      final Duration duration = pl.getProperty(Property.DURATION);

      IcalUtil.setDates(cb.getPrincipal().getPrincipalRef(),
                        evinfo, dtStart, dtEnd, duration);

      for (final Property prop: pl) {
        testXparams(prop, hasXparams);

        //debug("ical prop " + prop.getClass().getName());
        String pval = prop.getValue();
        if ((pval != null) && (pval.isEmpty())) {
          pval = null;
        }

        final PropertyInfoIndex pi;

        if (prop instanceof XProperty) {
          pi = PropertyInfoIndex.XPROP;
        } else {
          pi = PropertyInfoIndex.fromName(prop.getName());
        }

        if (pi == null) {
          logger.debug("Unknown property with name " + prop.getName() +
                                " class " + prop.getClass() +
                                " and value " + pval);
          continue;
        }

        chg.present(pi);

        switch (pi) {
          case ACCEPT_RESPONSE:
            /* ------------------- Accept Response -------------------- */

            String sval = prop.getValue();
            if (chg.changed(pi, ev.getPollAcceptResponse(), sval)) {
              ev.setPollAcceptResponse(sval);
            }
            break;

          case ATTACH:
            /* ------------------- Attachment -------------------- */

            chg.addValue(pi, IcalUtil.getAttachment((Attach)prop));
            break;

          case ATTENDEE:
            /* ------------------- Attendee -------------------- */

            if (vpoll) {
              // Flag this as an error?
              break;
            }

            final var attResp = doAttendee(cb, chg,
                                           evinfo, (Attendee)prop,
                                           attUri, methodType,
                                           mergeAttendees);
            if (!attResp.isOk()) {
              return resp.fromResponse(attResp);
            }

            break;

          case BUSYTYPE:
            final int ibt = BwEvent.fromBusyTypeString(pval);
            if (chg.changed(pi,
                            ev.getBusyType(),
                            ibt)) {
              ev.setBusyType(ibt);
            }

            break;

          case CATEGORIES:
            /* ------------------- Categories -------------------- */

            final Categories cats = (Categories)prop;
            final TextList cl = cats.getCategories();
            String lang = IcalUtil.getLang(cats);

            if (cl != null) {
              /* Got some categories */

              for (final String wd: cl) {
                if (wd == null) {
                  continue;
                }

                final BwString key = new BwString(lang, wd);

                final var fcResp = cb.findCategory(key);
                final BwCategory cat;

                if (fcResp.isError()) {
                  return resp.fromResponse(fcResp);
                }

                if (fcResp.isNotFound()) {
                  cat = BwCategory.makeCategory();
                  cat.setWord(key);

                  cb.addCategory(cat);
                } else {
                  cat = fcResp.getEntity();
                }

                chg.addValue(pi, cat);
              }
            }

            break;

          case CLASS:
            /* ------------------- Class -------------------- */

            if (chg.changed(pi, ev.getClassification(), pval)) {
              ev.setClassification(pval);
            }

            break;

          case COMMENT:
            /* ------------------- Comment -------------------- */

            chg.addValue(pi,
                         new BwString(null, pval));

            break;

          case COMPLETED:
            /* ------------------- Completed -------------------- */

            if (chg.changed(pi, ev.getCompleted(), pval)) {
              ev.setCompleted(pval);
            }

            break;

          case CONCEPT:
            /* ------------------- Concept -------------------- */

            final Concept c = (Concept)prop;
            final String cval = c.getValue();

            if (cval != null) {
              /* Got a concept */
              chg.addValue(PropertyInfoIndex.XPROP,
                           ev.makeConcept(cval));
            }

            break;

          case CONTACT:
            /* ------------------- Contact -------------------- */

            final String altrep = getAltRepPar(prop);
            lang = IcalUtil.getLang(prop);
            final String uid = getUidPar(prop);
            final BwString nm = new BwString(lang, pval);

            BwContact contact = null;

            if (uid != null) {
              final var fcResp = cb.getContact(uid);

              if (fcResp.isError()) {
                return resp.fromResponse(fcResp);
              }

              if (fcResp.isOk()) {
                contact = fcResp.getEntity();
              }
            }

            if (contact == null) {
              final var fcResp = cb.findContact(nm);
              if (fcResp.isError()) {
                return resp.fromResponse(fcResp);
              }

              if (fcResp.isOk()) {
                contact = fcResp.getEntity();
              }
            }

            if (contact == null) {
              contact = BwContact.makeContact();
              contact.setCn(nm);
              contact.setLink(altrep);
              cb.addContact(contact);
            } else {
              contact.setCn(nm);
              contact.setLink(altrep);
            }

            chg.addValue(pi, contact);

            break;

          case CREATED:
            /* ------------------- Created -------------------- */

            if (chg.changed(pi, ev.getCreated(), pval)) {
              ev.setCreated(pval);
            }

            break;

          case DESCRIPTION:
            /* ------------------- Description -------------------- */

            if (chg.changed(pi, ev.getDescription(), pval)) {
              ev.setDescription(pval);
            }

            break;

          case DTEND:
            /* ------------------- DtEnd -------------------- */

            break;

          case DTSTAMP:
            /* ------------------- DtStamp -------------------- */

            ev.setDtstamp(pval);

            break;

          case DTSTART:
            /* ------------------- DtStart -------------------- */

            break;

          case DUE:
            /* -------------------- Due ------------------------ */

            break;

          case DURATION:
            /* ------------------- Duration -------------------- */

            break;

          case EXDATE:
            /* ------------------- ExDate -------------------- */

            chg.addValues(pi,
                          IcalUtil.makeDateTimes((DateListProperty)prop));

            break;

          case EXRULE:
            /* ------------------- ExRule -------------------- */

            chg.addValue(pi, pval);

            break;

          case FREEBUSY:
            /* ------------------- freebusy -------------------- */

            final FreeBusy fbusy = (FreeBusy)prop;
            final PeriodList perpl = fbusy.getPeriods();
            final Parameter par = IcalUtil.getParameter(fbusy, "FBTYPE");
            final int fbtype;

            if (par == null) {
              fbtype = BwFreeBusyComponent.typeBusy;
            } else if (par.equals(FbType.BUSY)) {
              fbtype = BwFreeBusyComponent.typeBusy;
            } else if (par.equals(FbType.BUSY_TENTATIVE)) {
              fbtype = BwFreeBusyComponent.typeBusyTentative;
            } else if (par.equals(FbType.BUSY_UNAVAILABLE)) {
              fbtype = BwFreeBusyComponent.typeBusyUnavailable;
            } else if (par.equals(FbType.FREE)) {
              fbtype = BwFreeBusyComponent.typeFree;
            } else {
              if (logger.debug()) {
                logger.debug("Unsupported parameter " + par.getName());
              }

              return resp.notOk(failed,
                                    "Unsupported parameter " +
                                            par.getName());
            }

            final BwFreeBusyComponent fbc = new BwFreeBusyComponent();

            fbc.setType(fbtype);

            for (final Period per : perpl) {
              fbc.addPeriod(per);
            }

            ev.addFreeBusyPeriod(fbc);

            break;

          case GEO:
            /* ------------------- Geo -------------------- */

            final Geo g = (Geo)prop;
            final BwGeo geo = new BwGeo(g.getLatitude(),
                                        g.getLongitude());
            if (chg.changed(pi, ev.getGeo(), geo)) {
              ev.setGeo(geo);
            }

            break;

          case LAST_MODIFIED:
            /* ------------------- LastModified -------------------- */

            if (chg.changed(pi, ev.getLastmod(), pval)) {
              ev.setLastmod(pval);
            }

            break;

          case LOCATION:
            /* ------------------- Location -------------------- */
            final var plresp = processLocation(cb, ev,
                                               val, prop, pval, chg);
            if (plresp.isError()) {
              return resp.fromResponse(plresp);
            }

            break;

          case ORGANIZER:
            /* ------------------- Organizer -------------------- */

            if (vpoll) {
              // Flag this as an error?
              ev.setOrganizer(null);
              break;
            }

            final BwOrganizer org = IcalUtil.getOrganizer(cb, (Organizer)prop);
            final BwOrganizer evorg = ev.getOrganizer();
            final BwOrganizer evorgCopy;
            
            if (evorg == null) {
              evorgCopy = null;
            } else {
              evorgCopy = (BwOrganizer)evorg.clone();
            }

            if (chg.changed(pi, evorgCopy, org)) {
              if (evorg == null) {
                ev.setOrganizer(org);
              } else {
                evorg.update(org);
              }
            }

            break;

          case PERCENT_COMPLETE:
            /* ------------------- PercentComplete -------------------- */

            final Integer percentage = ((PercentComplete)prop).getPercentage();
            if (chg.changed(pi, ev.getPercentComplete(), percentage)) {
              ev.setPercentComplete(percentage);
            }

            break;

          case POLL_COMPLETION:
            /* ------------------- Poll completion --------------- */

            sval = prop.getValue();
            if (chg.changed(pi, ev.getPollCompletion(), sval)) {
              ev.setPollCompletion(sval);
            }

            break;

          case POLL_MODE:
            /* ------------------- Poll mode -------------------- */

            sval = prop.getValue();
            if (chg.changed(pi, ev.getPollMode(), sval)) {
              ev.setPollMode(sval);
            }

            break;

          case POLL_PROPERTIES:
            /* ------------------- Poll properties ---------------- */

            sval = prop.getValue();
            if (chg.changed(pi, ev.getPollProperties(), sval)) {
              ev.setPollProperties(sval);
            }

            break;

          case POLL_WINNER:
            /* ------------------- Poll winner -------------------- */

            Integer ival = ((PollWinner)prop).getPollwinner();
            if (chg.changed(pi, ev.getPollWinner(), ival)) {
              ev.setPollWinner(ival);
            }

            break;

          case PRIORITY:
            /* ------------------- Priority -------------------- */

            ival = ((Priority)prop).getLevel();
            if (chg.changed(pi, ev.getPriority(), ival)) {
              ev.setPriority(ival);
            }

            break;

          case RDATE:
            /* ------------------- RDate -------------------- */

            chg.addValues(pi,
                          IcalUtil.makeDateTimes((DateListProperty)prop));

            break;

          case RECURRENCE_ID:
            /* ------------------- RecurrenceID -------------------- */
            // Done above

            break;

          case RELATED_TO:
            /* ------------------- RelatedTo -------------------- */
            final RelatedTo irelto = (RelatedTo)prop;
            final BwRelatedTo relto = new BwRelatedTo();

            final String parval = IcalUtil.getParameterVal(irelto,
                                                           "RELTYPE");
            if (parval != null) {
              relto.setRelType(parval);
            }

            relto.setValue(irelto.getValue());

            if (chg.changed(pi, ev.getRelatedTo(), relto)) {
              ev.setRelatedTo(relto);
            }

            break;

          case REQUEST_STATUS:
            /* ------------------- RequestStatus -------------------- */
            final BwRequestStatus rs = BwRequestStatus
                    .fromRequestStatus((RequestStatus)prop);

            chg.addValue(pi, rs);

            break;

          case RESOURCES:
            /* ------------------- Resources -------------------- */

            final TextList rl = ((Resources)prop).getResources();

            if (rl != null) {
              /* Got some resources */
              lang = IcalUtil.getLang(prop);

              for (final String s: rl) {
                final BwString rsrc = new BwString(lang,
                                                   s);
                chg.addValue(pi, rsrc);
              }
            }

            break;

          case RRULE:
            /* ------------------- RRule -------------------- */

            chg.addValue(pi, pval);

            break;

          case SEQUENCE:
            /* ------------------- Sequence -------------------- */

            final int seq = ((Sequence)prop).getSequenceNo();
            if (seq != ev.getSequence()) {
              chg.changed(pi, ev.getSequence(), seq);
              ev.setSequence(seq);
            }

            break;

          case STATUS:
            /* ------------------- Status -------------------- */

            if (chg.changed(pi, ev.getStatus(), pval)) {
              ev.setStatus(pval);
            }

            break;

          case SUMMARY:
            /* ------------------- Summary -------------------- */

            if (chg.changed(pi, ev.getSummary(), pval)) {
              ev.setSummary(pval);
            }

            break;

          case TRANSP:
            /* ------------------- Transp -------------------- */

            if (chg.changed(pi,
                            ev.getPeruserTransparency(
                                    cb.getPrincipal()
                                            .getPrincipalRef()),
                            pval)) {
              final BwXproperty pu = ev.setPeruserTransparency(
                      cb.getPrincipal().getPrincipalRef(),
                      pval);
              if (pu != null) {
                chg.addValue(PropertyInfoIndex.XPROP, pu);
              }
            }

            break;

          case UID:
            /* ------------------- Uid -------------------- */

            /* We did this above */

            break;

          case URL:
            /* ------------------- Url -------------------- */

            if (chg.changed(pi, ev.getLink(), pval)) {
              ev.setLink(pval);
            }

            break;

          case XPROP:
            /* ------------------------- x-property --------------------------- */

            final String name = prop.getName();

            if (name.equalsIgnoreCase(BwXproperty.bedeworkCost)) {
              if (chg.changed(PropertyInfoIndex.COST, ev.getCost(),
                              pval)) {
                ev.setCost(pval);
              }

              break;
            }

            if (name.equalsIgnoreCase(BwXproperty.xBedeworkCategories)) {
              if (checkCategory(cb, chg, ev, null, pval)) {
                break;
              }
            }

            if (name.equalsIgnoreCase(BwXproperty.xBedeworkLocation)) {
              if (checkLocation(cb, chg, ev, prop)) {
                break;
              }
            }

            if (name.equalsIgnoreCase(BwXproperty.xBedeworkContact)) {
              if (checkContact(cb, chg, ev, null, pval)) {
                break;
              }
            }

            /* See if this is an x-category that can be
               converted to a real category
              */

            final XProperty xp = (XProperty)prop;
            chg.addValue(PropertyInfoIndex.XPROP,
                         new BwXproperty(name,
                                         xp.getParameters()
                                                 .toString(),
                                         pval));

            break;

          default:
            logger.debug("Unsupported property with index {}; " +
                                 "class {} and value {}",
                         pi, prop.getClass(), pval);
        }
      }

      /* =================== Process sub-components =============== */

      final ComponentList<Component> subComps;

      if (val instanceof ComponentContainer) {
        subComps = ((ComponentContainer<Component>)val).getComponents();
      } else {
        subComps = null;
      }

      // TODO - this looks wrong - if this is a voter response
      // we probably shouldn't be doing this ?????
      final Set<Integer> pids;
      if (vpoll) {
        pids = new TreeSet<>();
        final BwEvent vp = evinfo.getEvent();

        if (!Util.isEmpty(vp.getPollItems())) {
          vp.clearPollItems();
        }
      } else {
        pids = null;
      }

      if (!Util.isEmpty(subComps)) {
        for (final var subComp: subComps) {
          if (subComp instanceof Available) {
            if (!(val instanceof VAvailability)) {
              return resp.error("AVAILABLE only valid in VAVAILABLE");
            }
            final var avlResp = processAvailable(cb, cal,
                                                 ical,
                                                 (VAvailability)val,
                                                 (Available)subComp,
                                                 evinfo);
            if (!avlResp.isOk()) {
              return resp.fromResponse(avlResp);
            }
            continue;
          }

          if (subComp instanceof Participant) {
            ev.getSchedulingInfo()
              .addUpdateParticipant((Participant)subComp);
            continue;
          }

          if (subComp instanceof VResource) {
            logger.warn("Unimplemented VResource object");
            continue;
          }

          if (subComp instanceof VLocation) {
            logger.warn("Unimplemented VLocation object");
            continue;
          }

          if (subComp instanceof VAlarm) {
            final var aresp = VAlarmUtil.processAlarm(cb,
                                                      val,
                                                      (VAlarm)subComp,
                                                      ev,
                                                      currentPrincipal,
                                                      chg);
            if (!aresp.isOk()) {
              return resp.fromResponse(aresp);
            }
            continue;
          }

          if (vpoll &&
                  (subComp instanceof VEvent ||
                           subComp instanceof VToDo)) {
            final var vresp = processCandidate((VPoll)val,
                                               subComp,
                                               evinfo,
                                               pids,
                                               chg);
            if (!vresp.isOk()) {
              return resp.fromResponse(vresp);
            }
            continue;
          }

          logger.warn("Unimplemented Component object: " + subComp);
        }
      }

      /* Fix up timestamps. */
      if (ev.getCreated() == null) {
        if (ev.getLastmod() != null) {
          ev.setCreated(ev.getLastmod());
          chg.changed(PropertyInfoIndex.CREATED, null, ev.getCreated());
        } else {
          ev.updateDtstamp();
          chg.changed(PropertyInfoIndex.CREATED, null, ev.getCreated());
          chg.changed(PropertyInfoIndex.LAST_MODIFIED, null, ev.getLastmod());
        }
      }

      if (ev.getLastmod() == null) {
        // created cannot be null now
        ev.setLastmod(ev.getCreated());
        chg.changed(PropertyInfoIndex.LAST_MODIFIED, null, ev.getLastmod());
      }

      processTimezones(ev, ical, chg);

      /* Remove any recipients and originator
       */
      if (ev.getRecipients() != null) {
        ev.getRecipients().clear();
      }

      ev.setOriginator(null);

      if (hasXparams.value) {
        /* Save a text copy of the entire event as an x-property */

        final Component valCopy = val.copy();

        /* Remove potentially large values */
        final Description desp = valCopy.getProperty(Property.DESCRIPTION);
        if (desp != null) {
          desp.setValue(null);
        }

        final Attach attachp = valCopy.getProperty(Property.ATTACH);
        // Don't store the entire attachment - we just need the parameters.
        if (attachp != null) {
          final Value v = attachp.getParameter(Parameter.VALUE);

          if (v != null) {
            attachp.setValue(String.valueOf(attachp.getValue().hashCode()));
          }
        }

        chg.addValue(PropertyInfoIndex.XPROP,
                     new BwXproperty(BwXproperty.bedeworkIcal,
                                     null,
                                     valCopy.toString()));
      }

      chg.processChanges(ev, true, false);

      ev.setRecurring(ev.isRecurringEntity());

      if (logger.debug()) {
        logger.debug(chg.toString());
        logger.debug(ev.toString());
      }

      if (masterEI != null) {
        // Just return notfound as this event is on its override list
        return resp.notFound();
      }

      resp.setEntity(evinfo);
      return resp;
    } catch (final Throwable t) {
      if (logger.debug()) {
        logger.error(t);
      }
      return resp.error(t);
    }
  }

  private static Response<?> doAttendee(final IcalCallback cb,
                                        final ChangeTable chg,
                                        final EventInfo evinfo,
                                        final Attendee attPr,
                                        final String attUri,
                                        final int methodType,
                                        final boolean mergeAttendees) {
    if (methodType == ScheduleMethods.methodTypePublish) {
      if (cb.getStrictness() == IcalCallback.conformanceStrict) {
        return new Response<>().notOk(failed,
                                      CalFacadeErrorCode.attendeesInPublish);
      }

      //if (cb.getStrictness() == IcalCallback.conformanceWarn) {
      //  warn("Had attendees for PUBLISH");
      //}
    }

    final BwEvent ev = evinfo.getEvent();

    if (evinfo.getNewEvent() || !mergeAttendees) {
      chg.addValue(ATTENDEE, IcalUtil.getAttendee(cb, attPr));
    } else {
      final String pUri = cb.getCaladdr(attPr.getValue());

      if (pUri.equals(attUri)) {
        /* Only update for our own attendee
         * We're doing a PUT and this must be the attendee updating their
         * partstat. We don't allow them to change other attendees
         * whatever the PUT content says.
         */
        chg.addValue(ATTENDEE, IcalUtil.getAttendee(cb, attPr));
      } else {
        // Use the value we currently have
        boolean found = false;

        for (final BwAttendee att: ev.getAttendees()) {
          if (pUri.equals(att.getAttendeeUri())) {
            chg.addValue(ATTENDEE, att.clone());
            found = true;
            break;
          }
        }

        if (!found) {
          // An added attendee
          final BwAttendee att = IcalUtil
                  .getAttendee(cb, attPr);
          att.setPartstat(IcalDefs.partstatValNeedsAction);
          chg.addValue(ATTENDEE, att);
        }
      }
    }

    return new Response<>().ok();
  }

  /* Return true if value matches a category - which may be added as
   * a result
   */
  private static boolean checkCategory(final IcalCallback cb,
                                       final ChangeTable chg,
                                       final BwEvent ev,
                                       final String lang,
                                       final String val) {
    if ((val == null) || (val.isEmpty())) {
      return false;
    }

    final BwString sval = new BwString(lang, val);

    final var resp = cb.findCategory(sval);

    if (resp.getStatus() == Response.Status.notFound) {
      return false;
    }

    if (!resp.isOk()) {
      throw new RuntimeException(
              "Failed. Status: " + resp.getStatus() +
                      ", msg: " + resp.getMessage());
    }

    final Set<BwCategory> cats = ev.getCategories();

    if (cats != null) {
      for (final BwCategory c : cats) {
        if (c.getWord().equals(sval)) {
          // Already present
          return true;
        }
      }
    }

    final var cat = resp.getEntity();

    ev.addCategory(cat);

    chg.addValue(PropertyIndex.PropertyInfoIndex.CATEGORIES,
                 cat);

    return true;
  }

  /* Return true if value matches a location - which may be added as
   * a result
   */
  private static boolean checkLocation(final IcalCallback cb,
                                       final ChangeTable chg,
                                       final BwEvent ev,
                                       final Property prop) {
    final Parameter keyName =
            prop.getParameter(XcalTags.xBedeworkLocationKey.getLocalPart());
    final String val = prop.getValue();
    final BwLocation evloc = ev.getLocation();
    final BwLocation loc;

    final GetEntityResponse<BwLocation> resp;

    if (keyName == null) {
      resp = cb.fetchLocationByCombined(val, true);
    } else {
      resp = cb.fetchLocationByKey(keyName.getValue(), val);
    }

    if (resp.getStatus() != ok) {
      return false;
    }

    loc = resp.getEntity();

    ev.setLocation(loc);

    chg.changed(PropertyIndex.PropertyInfoIndex.LOCATION,
                evloc, loc);
    return true;
  }

  /* Return true if value matches a contact - which may be added as
   * a result
   */
  private static boolean checkContact(final IcalCallback cb,
                                      final ChangeTable chg,
                                      final BwEvent ev,
                                      final String lang,
                                      final String val) {
    final BwString sval = new BwString(lang, val);

    final var resp = cb.findContact(sval);

    if (resp.getStatus() == Response.Status.notFound) {
      return false;
    }

    if (!resp.isOk()) {
      throw new RuntimeException(
              "Failed. Status: " + resp.getStatus() +
                      ", msg: " + resp.getMessage());
    }

    final Set<BwContact> cs = ev.getContacts();

    final var c = resp.getEntity();

    if (cs != null) {
      for (final BwContact c1: cs) {
        if (c1.getCn().equals(sval)) {
          // Already present
          return true;
        }
      }
    }

    ev.addContact(c);

    chg.addValue(PropertyInfoIndex.CONTACT, c);

    return true;
  }

  /* ====================================================================
                      Private methods
     ==================================================================== */

  private static void testXparams(final Property p,
                           final Holder<Boolean> hasXparams) {
    if (hasXparams.value) {
      // No need to check
      return;
    }

    final ParameterList params = p.getParameters();

    for (final Parameter param: params) {
      if (!(param instanceof final XParameter xpar)) {
        continue;
      }

      if (xpar.getName().equalsIgnoreCase(BwXproperty.xparUid)) {
        continue;
      }

      hasXparams.value = true;
    }
  }

  private static Response<?> processLocation(final IcalCallback cb,
                                             final BwEvent ev,
                                             final Component val,
                                             final Property prop,
                                             final String pval,
                                             final ChangeTable chg) {
    BwLocation loc = null;

    /* See if there's a VLOCATION - if so use that
               TODO - handle multiple
     */

    // The one equivalent to our single location
    VLocation mainVloc = null;
    ev.removeXproperties(BwXproperty.xBedeworkVLocation);

    if (val instanceof ComponentContainer) {
      final var vlocs =
              ((ComponentContainer<VLocation>)val).getComponents(VLocation.VLOCATION);

      if (!Util.isEmpty(vlocs)) {
        for (final var vloc: vlocs) {
          final var relto = vloc.getProperty(RELATIVE_TO);

          if (relto != null) {
            if (relto.equals(START)) {
              mainVloc = vloc;
            } else if (relto.equals(ImmutableRelativeTo.END) &&
                    (mainVloc == null)) {
              mainVloc = vloc;
            }
          }

          chg.addValue(PropertyInfoIndex.XPROP,
                       new BwXproperty(BwXproperty.xBedeworkVLocation,
                                       null,
                                       vloc.toString()));
        }

        if (mainVloc == null) {
          // Use first or only
          mainVloc = vlocs.getFirst();
        }
      }
    }

    if (mainVloc != null) {
      final var puid = mainVloc.getUid();
      if (puid != null) {
        final var lresp = cb.getLocation(puid.getValue());
        if (lresp.isError()) {
          return lresp;
        }

        if (lresp.isNotFound()) {
          // Manufacture one
          final var addr = new BwString(null, pval);
          final var fcResp = cb.findLocation(addr);
          if (fcResp.isError()) {
            return fcResp;
          }

          if (fcResp.isOk()) {
            loc = fcResp.getEntity();
          }

          if (loc == null) {
            loc = BwLocation.makeLocation();
            loc.setAddress(addr);
            cb.addLocation(loc);
          }
        } else {
          loc = lresp.getEntity();
        }
      }
    } else {
      //String uid = getUidPar(prop);

      /* At the moment Mozilla lightning is broken and this leads to all
       * sorts of problems.
        if (uid != null) {
          loc = cb.getLocation(uid);
        }
       */
      final String lang = IcalUtil.getLang(prop);
      BwString addr;

      if (pval != null) {
        addr = new BwString(lang, pval);

        final var fcResp = cb.findLocation(addr);
        if (fcResp.isError()) {
          return fcResp;
        }

        if (fcResp.isOk()) {
          loc = fcResp.getEntity();
        }

        if (loc == null) {
          loc = BwLocation.makeLocation();
          loc.setAddress(addr);
          cb.addLocation(loc);
        }
      }
    }

    final BwLocation evloc = ev.getLocation();

    if (chg.changed(PropertyInfoIndex.LOCATION, evloc, loc)) {
      // CHGTBL - this only shows that it's a different location object
      ev.setLocation(loc);
    } else if ((loc != null) && (evloc != null)) {
      // See if the value is changed
      final String evval = evloc.getAddress().getValue();
      final String inval = loc.getAddress().getValue();
      if (!evval.equals(inval)) {
        chg.changed(PropertyInfoIndex.LOCATION, evval, inval);
        evloc.getAddress().setValue(inval);
      }
    }

    return new Response<>().ok();
  }

  private static void processTimezones(final BwEvent ev,
                                       final Icalendar ical,
                                       final ChangeTable chg) {
    for (final TimeZoneInfo tzi: ical.getTimeZones()) {
      if (tzi.tzSpec == null) {
        // System
        continue;
      }

      if (EventTimeZonesRegistry.findTzValue(ev, tzi.tzid) != null) {
        // Seen already
        continue;
      }

      chg.addValue(PropertyInfoIndex.XPROP,
                   new BwXproperty(BwXproperty.bedeworkXTimezone,
                                   null,
                                   BwXproperty.escapeSemi(tzi.tzid) + ";" +
                                   tzi.tzSpec));
    }
  }

  private static Response<?> processAvailable(
          final IcalCallback cb,
          final BwCollection cal,
          final Icalendar ical,
          final VAvailability val,
          final Available avl,
          final EventInfo vavail) {
    final var resp = new Response<>();

    final GetEntityResponse<EventInfo> availi =
            toEvent(cb, cal, ical, avl,
                    false);
    if (!resp.isOk()) {
      return resp.fromResponse(availi);
    }

    final var ei = availi.getEntity();
    ei.getEvent().setOwnerHref(
            vavail.getEvent().getOwnerHref());

    vavail.addContainedItem(ei);
    vavail.getEvent().addAvailableUid(ei.getEvent().getUid());

    return resp;
  }

  private static Response<?> processCandidate(final VPoll val,
                                              final Component comp,
                                              final EventInfo vpoll,
                                              final Set<Integer> pids,
                                              final ChangeTable changes) {
    final BwEvent event = vpoll.getEvent();

    final String pollItem = comp.toString();
    event.addPollItem(pollItem);

    changes.addValue(PropertyInfoIndex.POLL_ITEM, pollItem);

    final Property p = comp.getProperty(Property.POLL_ITEM_ID);

    if (p == null) {
      return new Response<>().error(
              "VPoll candidate - no poll item id");
    }

    final int pid = ((PollItemId)p).getPollitemid();

    if (pids.contains(pid)) {
      return new Response<>().error(
              "VPoll candidate - duplicate poll item id " + pid);
    }

    pids.add(pid);

//        EventInfo cand = toEvent(cb, cal, ical, (Component)o, true,
//                                 false);
//        cand.getEvent().setOwnerHref(vpoll.getEvent().getOwnerHref());

//        vpoll.addContainedItem(cand);
    return new Response<>().ok();
  }

  /* See if the master event is already in the collection of events
   * we've processed for this calendar. Only called if we have an event
   * with a recurrence id
   */
  private static EventInfo findMaster(final String guid,
                                      final Collection<EventInfo> evs) {
    if (evs == null) {
      return null;
    }

    for (final EventInfo ei: evs) {
      final BwEvent ev = ei.getEvent();

      if ((ev.getRecurrenceId() == null) &&
              guid.equals(ev.getUid()) /* &&
          ei.getNewEvent()  */) {
        return ei;
      }
    }

    return null;
  }

  private static String getUidPar(final Property p) {
    final ParameterList pars = p.getParameters();

    final Parameter par = pars.getParameter(BwXproperty.xparUid);

    if (par == null) {
      return null;
    }

    return par.getValue();
  }

  private static String getAltRepPar(final Property p) {
    final AltRep par = IcalUtil.getAltRep(p);

    if (par == null) {
      return null;
    }

    return par.getValue();
  }
}

