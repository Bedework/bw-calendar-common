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
package org.bedework.calfacade;

import org.bedework.base.exc.BedeworkException;
import org.bedework.calfacade.BwXproperty.Xpar;
import org.bedework.calfacade.annotations.CloneForOverride;
import org.bedework.calfacade.annotations.Dump;
import org.bedework.calfacade.annotations.NoDump;
import org.bedework.calfacade.annotations.NoWrap;
import org.bedework.calfacade.annotations.Wrapper;
import org.bedework.calfacade.annotations.ical.IcalProperties;
import org.bedework.calfacade.annotations.ical.IcalProperty;
import org.bedework.calfacade.annotations.ical.Immutable;
import org.bedework.calfacade.annotations.ical.NoProxy;
import org.bedework.calfacade.base.BwShareableContainedDbentity;
import org.bedework.calfacade.base.EventEntity;
import org.bedework.calfacade.util.CalFacadeUtil;
import org.bedework.calfacade.util.ChangeTable;
import org.bedework.util.calendar.IcalDefs;
import org.bedework.util.calendar.PropertyIndex.PropertyInfoIndex;
import org.bedework.base.ToString;
import org.bedework.util.misc.Uid;
import org.bedework.util.misc.Util;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.property.Created;
import net.fortuna.ical4j.model.property.DtStamp;
import net.fortuna.ical4j.model.property.LastModified;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/** An Event in Bedework. The event class is actually used to represent most of
 * the calendaring components as they all require the same sort of searching
 * capabilities.
 *
 * <p>Proxies and annotations are used to create an overridden instance or an
 * annotated event. Pictorially...
 *
 * <pre>
 *   ***********************
 *   | Proxy     | ref |   |
 *   ***********************
 *                  |
 *                  |   **********************************
 *                  +-->| Annnotation     | target |     |
 *                      **********************************
 *                                            |
 *                                            |   ************************
 *                                            +-->| Event                |
 *                                                ************************
 * </pre>
 *
 * <p>The proxy class checks the annotation for a value and if absent, uses the
 * target - or event - value.
 *
 * <p>Immutable values cannot be overridden and need to be set in the annotation
 * object. An example is the entity type. These are usually primitive types.
 *
 * <p>Still incomplete but getting there... annotations are used to describe
 * certain aspects of events to allow the automatic generation of proxy and
 * annotation classes and of conversion classes, to and from ical and xml.
 *
 * <p>From RFC2445<pre>
 *     A "VEVENT" calendar component is defined by the
 * following notation:
 *
 *   eventc     = "BEGIN" ":" "VEVENT" CRLF
 *                eventprop *alarmc
 *                "END" ":" "VEVENT" CRLF
 *
 *   eventprop  = *(
 *
 *              ; the following are optional,
 *              ; but MUST NOT occur more than once
 *
 *              class / created / description / dtstart / geo /
 *              last-mod / location / organizer / priority /
 *              dtstamp / seq / status / summary / transp /
 *              uid / url / recurid /
 *
 *              ; either 'dtend' or 'duration' may appear in
 *              ; a 'eventprop', but 'dtend' and 'duration'
 *              ; MUST NOT occur in the same 'eventprop'
 *
 *              dtend / duration /
 *
 *              ; the following are optional,
 *              ; and MAY occur more than once
 *
 *              attach / attendee / categories / comment /
 *              contact / exdate / exrule / rstatus / related /
 *              resources / rdate / rrule / x-prop
 *              )
 *
 *         A "VTODO" calendar component is defined by the
       following notation:

         todoc      = "BEGIN" ":" "VTODO" CRLF
                      todoprop *alarmc
                      "END" ":" "VTODO" CRLF

         todoprop   = *(

                    ; the following are optional,
                    ; but MUST NOT occur more than once

                    class / completed / created / description / dtstamp /
                    dtstart / geo / last-mod / location / organizer /
                    percent / priority / recurid / seq / status /
                    summary / uid / url /

                    ; either 'due' or 'duration' may appear in
                    ; a 'todoprop', but 'due' and 'duration'
                    ; MUST NOT occur in the same 'todoprop'

                    due / duration /

                    ; the following are optional,
                    ; and MAY occur more than once
                    attach / attendee / categories / comment / contact /
                    exdate / exrule / rstatus / related / resources /
                    rdate / rrule / x-prop

                    )
        A "VJOURNAL" calendar component is defined by the
       following notation:

         journalc   = "BEGIN" ":" "VJOURNAL" CRLF
                      jourprop
                      "END" ":" "VJOURNAL" CRLF

         jourprop   = *(

                    ; the following are optional,
                    ; but MUST NOT occur more than once

                    class / created / description / dtstart / dtstamp /
                    last-mod / organizer / recurid / seq / status /
                    summary / uid / url /

                    ; the following are optional,
                    ; and MAY occur more than once

                    attach / attendee / categories / comment /
                    contact / exdate / exrule / related / rdate /
                    rrule / rstatus / x-prop
                    )

 Properties common to event, todo and journal
    attach         VEVENT VTODO VJOURNAL    n/a       n/a    VALARM
    attendee       VEVENT VTODO VJOURNAL VFREEBUSY    n/a    VALARM
    categories     VEVENT VTODO VJOURNAL
    class          VEVENT VTODO VJOURNAL
    comment        VEVENT VTODO VJOURNAL VFREEBUSY VTIMEZONE
    contact        VEVENT VTODO VJOURNAL VFREEBUSY
    created        VEVENT VTODO VJOURNAL
    description    VEVENT VTODO VJOURNAL    n/a       n/a    VALARM
    dtstamp        VEVENT VTODO VJOURNAL VFREEBUSY
    dtstart        VEVENT VTODO VJOURNAL VFREEBUSY VTIMEZONE
    exdate         VEVENT VTODO VJOURNAL
    exrule         VEVENT VTODO VJOURNAL
    lastModified   VEVENT VTODO VJOURNAL    n/a    VTIMEZONE
    organizer      VEVENT VTODO VJOURNAL VFREEBUSY
    rdate          VEVENT VTODO VJOURNAL    n/a    VTIMEZONE
    recurrenceId   VEVENT VTODO VJOURNAL    n/a    VTIMEZONE
    relatedTo      VEVENT VTODO VJOURNAL
    requestStatus  VEVENT VTODO VJOURNAL VFREEBUSY
    rrule          VEVENT VTODO VJOURNAL    n/a    VTIMEZONE
    sequence       VEVENT VTODO VJOURNAL
    status         VEVENT VTODO VJOURNAL
    summary        VEVENT VTODO VJOURNAL    n/a       n/a    VALARM
    uid            VEVENT VTODO VJOURNAL VFREEBUSY
    url            VEVENT VTODO VJOURNAL VFREEBUSY

 Properties in one or more of event, todo (all journal fields are in common set)
    completed        n/a  VTODO
    dtend          VEVENT  n/a   n/a     VFREEBUSY
    due              n/a  VTODO                            (same as dtend)
    duration       VEVENT VTODO  n/a     VFREEBUSY    n/a    VALARM
    geo            VEVENT VTODO
    location       VEVENT VTODO
    percentComplete  n/a  VTODO
    priority       VEVENT VTODO
    resources      VEVENT VTODO
    transp         VEVENT

 Alarm only:
    action           n/a   n/a   n/a        n/a       n/a    VALARM
    repeat           n/a   n/a   n/a        n/a       n/a    VALARM
    trigger          n/a   n/a   n/a        n/a       n/a    VALARM

 Freebusy only:
    freebusy         n/a   n/a   n/a     VFREEBUSY

 Timezone only:
    tzid             n/a   n/a   n/a        n/a    VTIMEZONE
    tzname           n/a   n/a   n/a        n/a    VTIMEZONE
    tzoffsetfrom     n/a   n/a   n/a        n/a    VTIMEZONE
    tzoffsetto       n/a   n/a   n/a        n/a    VTIMEZONE
    tzurl            n/a   n/a   n/a        n/a    VTIMEZONE

 In addition, events and todos may contain alarms.

 * Optional:
     class            classification
     created          created
     description      description
     dtstart          dtstart
     geo              geo
     last-mod         lastmod
     location         location
     organizer        organizerId
     priority         priority
     dtstamp          dtstamp
     seq              sequence
     status           status
     summary          summary
     transp           transparency
     uid              uid
     url              link
     recurid          recurrenceId

   One of or neither
     dtend            dtend
     duration         duration

   Optional and repeatable
      alarmc          alarms
      attach
      attendee        attendees
      categories      categories
      comment         comments
      contact         sponsor
      exdate          exdates
      exrule          exrules
      rstatus         requestStatus
      related
      resources       resources
      rdate           rdates
      rrule           rrules
      x-prop

  Extra non-rfc fields:
      private String cost;
      private UserVO creator;
      private boolean isPublic;
      private CalendarVO calendar;
      private RecurrenceVO recurrence;
      private char recurrenceStatus = 'N'; // Derived from above

 * --------------------------------------------------------------------
 *</pre>
 *
 * <p>Peruser data and overrides.</p>
 * <p>When events are in a shared collection some properties are private to each
 * user with access. These are transparency and alarms. Retrieving transparency
 * is done ona  peruser basis. An x-property holds the peruser values. Alarms
 * have an owner so we only return alarms owned by the current user. We also need
 * to be careful not to delete other users alarms on update.
 *
 * <p>Where it gets really complex is handling recurrences. If a user adds an
 * alarm to an instance, this creates an override specifically for that user.
 *
 * <p>On update a user will only send the instances they know about. We should
 * preserve all other instances for which they have no peruser data.
 *
 * <p>The owner peruser data is stored directly in the instance. If the instance
 * only differs in that respect then it is not delivered to others. Rather than
 * attempt to compare it every time, we flag it on creation with a peruser
 * x-property. If any other property is changed then we remove the peruser flag.
 *
 *  @version 1.0
 */
@Wrapper(quotas = true)
@Dump(elementName="event", keyFields={"colPath", "uid", "recurrenceId"},
      firstFields = {"ownerHref"})
public class BwEvent extends BwShareableContainedDbentity<BwEvent>
        implements EventEntity {
  /** This enum is used by annotations to index into a String containing T/F
   * characters indication the absence or presence of an override value.
   *
   * <p>This is needed to flag an override setting a value to null as distinct
   * from no override value.
   *
   * <p>DO NOT ALTER THE ORDER OF THESE. The annotation processor will flag any
   * absent values.
   *
   * @author Mike Douglass
   *
   */
  public enum ProxiedFieldIndex {
    /** 0 */
    pfiEntityType,
    /** */
    pfiName,
    /** */
    pfiClassification,
    /** */
    pfiLink,
    /** */
    pfiGeo,
    /** */
    pfiDeleted,
    /** */
    pfiStatus,
    /** */
    pfiCost,
    /** */
    pfiOrganizer,
    /** */
    pfiDtstamp,
    /** 10 */
    pfiLastmod,
    /** */
    pfiCreated,
    /** */
    pfiPriority,
    /** */
    pfiSequence,
    /** */
    pfiLocation,
    /** */
    pfiUid,
    /** */
    pfiTransparency,
    /** */
    pfiPercentComplete,
    /** */
    pfiCompleted,
    /** */
    pfiScheduleMethod,
    /** 20 */
    pfiOriginator,
    /** */
    pfiScheduleState,
    /** */
    pfiRelatedTo,
    /** */
    pfiXproperties,
    /** */
    pfiRequestStatuses,
    /** */
    pfiRecurring,
    /** */
    pfiRecurrenceId,
    /** */
    pfiRrules,
    /** */
    pfiExrules,
    /** */
    pfiRdates,
    /** 30 */
    pfiExdates,
    /** */
    pfiLatestDate,
    /** */
    pfiExpanded,
    /** */
    pfiDtstart,
    /** */
    pfiDtend,
    /** */
    pfiEndType,
    /** */
    pfiDuration,
    /** */
    pfiNoStart,
    /** */
    pfiAlarms,
    /** */
    pfiAttachments,
    /** 40 */
    pfiAttendees,
    /** */
    pfiRecipients,
    /** */
    pfiCategories,
    /** */
    pfiComments,
    /** */
    pfiContacts,
    /** */
    pfiDescriptions,
    /** */
    pfiResources,
    /** */
    pfiSummaries,
    /** */
    pfiStag,
    /** */
    pfiTombstoned,
    /** 50 */
    pfiOrganizerSchedulingObject,
    /** */
    pfiAttendeeSchedulingObject,
    /** */
    pfiCtoken,

    /** */
    pfiPollItemId,
    /** */
    pfiPollMode,
    /** */
    pfiPollProperties,
    /** */
    pfiPollAcceptResponse,
    /** */
    pfiPollCandidate,
    /** 58 */
    pfiPollWinner,
    /** 59 */
    pfiPollCompletion
  }

  private int entityType = IcalDefs.entityTypeEvent;

  private String name;

  private Set<BwString> summaries;

  private Set<BwLongString> descriptions;

  private String classification;

  private Set<BwString> comments;

  private Set<BwString> resources;

  private BwDateTime dtstart;
  private BwDateTime dtend;

  /** Duration may be calculated or specified. The end will always
         be filled in to provide the calculated end time.
   */
  private String duration;

  private Boolean noStart;

  /** This will be one of the above.
   */
  private char endType = endTypeDuration;

  private String link;

  private BwGeo geo;

  /* Status values from rfc
   *      statvalue  = "TENTATIVE"           ;Indicates event is
                                        ;tentative.
                / "CONFIRMED"           ;Indicates event is
                                        ;definite.
                / "CANCELLED"           ;Indicates event was
                                        ;cancelled.
        ;Status values for a "VEVENT"
     statvalue  =/ "NEEDS-ACTION"       ;Indicates to-do needs action.
                / "COMPLETED"           ;Indicates to-do completed.
                / "IN-PROCESS"          ;Indicates to-do in process of
                / "CANCELLED"           ;Indicates to-do was cancelled.
        ;Status values for "VTODO".

     statvalue  =/ "DRAFT"              ;Indicates journal is draft.
                / "FINAL"               ;Indicates journal is final.
                / "CANCELLED"           ;Indicates journal is removed.
        ;Status values for "VJOURNAL".
   */
  // ENUM

  /** Rfc value for a tentative meeting */
  public static final String statusTentative = "TENTATIVE";

  /** Rfc value for a confirmed meeting */
  public static final String statusConfirmed = "CONFIRMED";

  /** Rfc value for a cancelled meeting */
  public static final String statusCancelled = "CANCELLED";

  /** Rfc value for an incomplete task */
  public static final String statusNeedsAction = "NEEDS-ACTION";

  /** Rfc value for a completed task */
  public static final String statusComplete = "COMPLETE";

  /** Rfc value for an in-process task */
  public static final String statusInProcess = "IN-PROCESS";

  /** Rfc value for a draft journal */
  public static final String statusDraft = "DRAFT";

  /** Rfc value for a final? journal */
  public static final String statusFinal = "FINAL";

  /** Rfc value for an unavailable time-period */
  public static final String statusUnavailable = "BUSY-UNAVAILABLE";

  /** Non-Rfc value for a supressed master event */
  public static final String statusMasterSuppressed = "MASTER-SUPPRESSED";

  private String status;
  private String cost;

  private boolean deleted;

  private Boolean tombstoned = Boolean.FALSE;

  /** UTC datetime as specified in rfc */
  private String dtstamp;

  /** UTC datetime as specified in rfc */
  private String lastmod;

  /** UTC datetime as specified in rfc */
  private String created;

  /** As specified in caldav sched rfc */
  private String stag;

  /** RFC priority value
   */
  private Integer priority;

  /** A Set of BwCategory objects
   */
  private Set<BwCategory> categories;

  private Set<BwContact> contacts;

  private BwLocation location;

  private BwOrganizer organizer;

  private String transparency;

  /* VTODO only */
  private Integer percentComplete;

  /* VTODO only */
  private String completed;

  private Set<BwAttachment> attachments;

  private Set<BwAttendee> attendees;

  private Boolean recurring;

  /** The uid for the event. Generated by the system or by external sources when
   * imported.
   */
  private String uid;

  private String ctoken;

  private Set<BwAlarm> alarms;

  /* ------------------- RecurrenceEntity information -------------------- */

  /** recurrence-id for a specific instance.
   */
  private String recurrenceId;

  /** Set of String rrule values
   */
  private Set<String> rrules;

  /** Set of String exrule values
   */
  private Set<String> exrules;

  /** Set of BwDateTime rdate values
   */
  private Set<BwDateTime> rdates;

  /** Set of BwDateTime exdate values
   */
  private Set<BwDateTime> exdates;

  /* * Where known this will be the absolute latest date for this recurring
   * event. This field will be null for infinite events.
   * /
  private String latestDate;*/

  /* ----------------- (CalDAV) scheduling information ------------------- */

  /** RFC sequence value
   */
  private int sequence;

  // ENUM
  private int scheduleMethod;

  private String originator;

  private Set<String> recipients;

  // ENUM
  /** scheduling message has not been processed
   * <p>(CalDAV) Inbox state on arrival.
   */
  public static final int scheduleStateNotProcessed = 0;

  /** scheduling message has been processed
   * <p>(CalDAV) Inbox state after all processing complete
   */
  public static final int scheduleStateProcessed = 1;

  /** scheduling message has been sent to external users
   * <p>Outbox state after sent to all external recipients. Internal recipients
   * are handled immediately.
   */
  public static final int scheduleStateExternalDone = 2;

  private int scheduleState;

  private Set<BwRequestStatus> requestStatuses;

  private Boolean organizerSchedulingObject = Boolean.FALSE;

  private Boolean attendeeSchedulingObject = Boolean.FALSE;

  /* -------------- End of (CalDAV) scheduling information ----------------- */

  private BwRelatedTo relatedTo;

  /** Collection of BwXproperty
   */
  private List<BwXproperty> xproperties;

  /* ---------------------------- Temp --------------------------------
   * As a quick fix we will store freebusy information in one of these.
   * It avoids many changes to the searching but is not too efficient
   * space wise.
   *
   * Longer term we probably ought to base all the different types of a
   * single master class.
   */

  /** List of BwFreeBusyComponent
   */
  private List<BwFreeBusyComponent> freeBusyPeriods;

  /* ====================================================================
   *                      VAvailability fields
   * ==================================================================== */

  /** busy time */
  public static final int busyTypeBusy = 0;

  /** unavailable time */
  public static final int busyTypeBusyUnavailable = 1;

  /** tentative busy time */
  public static final int busyTypeBusyTentative = 2;

  private int busyType = busyTypeBusyUnavailable;

  /** Standard values for busyType
   *
   */
  public final static String[] busyTypeStrings = {
    "BUSY",
    "BUSY-UNAVAILABLE",
    "BUSY-TENTATIVE"
  };

  /* Uids of AVAILABILITY components */
  private Set<String> availableUids;

  /* ==============================================================
   *                      Non-db fields
   * ============================================================== */

  /* Manages participants for this component */
  private SchedulingInfo schedulingInfo;

  /** If the event is a master recurring event and we asked for the master +
   * overides or for fully expanded, this will hold all the overrides for that
   * event.
   */
  private Collection<BwEventAnnotation> overrides;

  private String color;

  private boolean significantChange = true;

  /** set true if we want to force emitted start/end as utc
   *  (used for CalDAV expanded)
   */
  boolean forceUTC;

  /* Used internally - for example to limit recurrences in an Available entity
   * to the date range of the enclosing vavailability
   */
  private BwEvent parent;

  private Set<String> categoryUids;

  private Set<String> contactHrefs;

  private String locationHref;

  private ChangeTable changeSet;

  /** Constructor
   */
  protected BwEvent() {
    super();
  }

  @NoProxy
  @Override
  public void beforeUpdate() {
    if (schedulingInfo != null) {
      schedulingInfo.onSave();
    }
  }

  /* ======================================================
   *                      Bean methods
   * ====================================================== */

  /** Set entity type defined in IcalDefs
   * @param val entity type
   */
  @Immutable
  @IcalProperties({
    @IcalProperty(pindex = PropertyInfoIndex.ENTITY_TYPE,
                  jname = "entityType",
                  required = true,
                  eventProperty = true,
                  todoProperty = true,
                  journalProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.DOCTYPE,
                  jname = "_type",
                  required = true,
                  eventProperty = true,
                  todoProperty = true,
                  journalProperty = true)}
  )
  public void setEntityType(final int val) {
    entityType = val;
  }

  /**
   * @return entity type defined in IcalDefs
   */
  public int getEntityType() {
    return entityType;
  }

  /** Set the event's name
   *
   * @param val    String event's name
   */
  @IcalProperty(pindex = PropertyInfoIndex.NAME,
                required = true,
                eventProperty = true,
                todoProperty = true,
                journalProperty = true)
  public void setName(final String val) {
    name = val;
  }

  /** Get the event's name.
   *
   * @return String   event's name
   */
  public String getName() {
    return name;
  }

  /** Set the event's classification
   *
   * @param val    String event's description
   */
  @IcalProperty(pindex = PropertyInfoIndex.CLASS,
                eventProperty = true,
                todoProperty = true,
                journalProperty = true
                )
  public void setClassification(final String val) {
    classification = val;
  }

  /** Get the event's classification
   *
   *  @return String   event's classification
   */
  public String getClassification() {
    return classification;
  }

  /** Set the event's URL
   *
   *
   * @param val   string URL
   */
  @IcalProperty(pindex = PropertyInfoIndex.URL,
                eventProperty = true,
                todoProperty = true,
                journalProperty = true,
                freeBusyProperty = true)
  public void setLink(final String val) {
    link = val;
  }

  /** Get the event's URL
   *
   * @return the event's URL
   */
  public String getLink() {
    return link;
  }

  /** Set the event's publish URL
   *
   *
   * @param val   string URL
   */
  @IcalProperty(pindex = PropertyInfoIndex.PUBLISH_URL,
          eventProperty = true,
          todoProperty = true,
          journalProperty = true,
          freeBusyProperty = true)
  @NoProxy
  public void setPublishUrl(final String val) {
    replaceXproperty(BwXproperty.bedeworkPublishUrl, val);
  }

  /**
   *
   *  @return the event's publish URL
   */ 
  @NoProxy
  @NoDump
  public String getPublishUrl() {
    return getXproperty(BwXproperty.bedeworkPublishUrl);
  }


  /** Set the event's geo
   *
   *  @param val   BwGeo
   */
  @IcalProperty(pindex = PropertyInfoIndex.GEO,
                eventProperty = true,
                todoProperty = true)
  public void setGeo(final BwGeo val) {
    geo = val;
  }

  /** Get the event's geo
   *
   * @return the event's geo
   */
  @Dump(compound = true)
  public BwGeo getGeo() {
    return geo;
  }

  /** Set the event deleted flag
   *
   *  @param val    boolean true if the event is deleted
   */
  @IcalProperty(pindex = PropertyInfoIndex.DELETED)
  public void setDeleted(final boolean val) {
    deleted = val;
  }

  /** Get the event deleted flag
   *
   *  @return boolean    true if the event is deleted
   */
  public boolean getDeleted() {
    return deleted;
  }

  /** Set the event tombstoned flag. This is distinct from the deleted flag which
   * is a user settable flag. This flag indicated the event really is deleted
   * but we want to retain information for synchronization purposes.
   *
   * <p>Unsetting this flag may cause some odd effects
   *
   *  @param val    boolean true if the event is tombstoned
   */
  @IcalProperty(pindex = PropertyInfoIndex.TOMBSTONED,
          eventProperty = true,
          todoProperty = true,
          journalProperty = true
  )
  public void setTombstoned(final Boolean val) {
    tombstoned = val;
  }

  /** Get the event tombstoned flag. This is distinct from the deleted flag which
   * is a user settable flag. This flag indicated the event really is deleted
   * but we want to retain information for synchronization purposes.
   *
   *  @return boolean    true if the event is tombstoned
   */
  public Boolean getTombstoned() {
    return tombstoned;
  }

  /** Set the event's status - must be one of
   *  CONFIRMED, TENTATIVE, or CANCELLED
   *
   *  @param val     status
   */
  @IcalProperty(pindex = PropertyInfoIndex.STATUS,
                eventProperty = true,
                todoProperty = true,
                journalProperty = true
                )
  public void setStatus(final String val) {
    status = val;
  }

  /** Get the event's status
   *
   * @return String event's status
   */
  public String getStatus() {
    return status;
  }

  /** Set the event's cost
   *
   * @param val    String event's cost
   */
  @IcalProperties({
      @IcalProperty(pindex = PropertyInfoIndex.COST,
                    eventProperty = true,
                    todoProperty = true,
                    journalProperty = true),
      @IcalProperty(pindex = PropertyInfoIndex.XBEDEWORK_COST,
                    eventProperty = true,
                    todoProperty = true,
                    journalProperty = true)}
  )
  public void setCost(final String val) {
    cost = val;
  }

  /** Get the event's cost
   *
   * @return String   the event's cost
   */
  public String getCost() {
    return cost;
  }

  /** Set the organizer
   *
   * @param val    BwOrganizer organizer
   */
  @IcalProperty(pindex = PropertyInfoIndex.ORGANIZER,
                eventProperty = true,
                todoProperty = true,
                journalProperty = true,
                freeBusyProperty = true
                )
  public void setOrganizer(final BwOrganizer val) {
    organizer = val;
  }

  /** Get the organizer
   *
   * @return BwOrganizer   the organizer
   */
  @Dump(elementName = "organizer", compound = true)
  public BwOrganizer getOrganizer() {
    return organizer;
  }

  /**
   * @param val dtstamp
   */
  @IcalProperty(pindex = PropertyInfoIndex.DTSTAMP,
                required = true,
                eventProperty = true,
                todoProperty = true,
                journalProperty = true,
                freeBusyProperty = true
                )
  public void setDtstamp(final String val) {
    dtstamp = val;
  }

  /**
   * @return String datestamp
   */
  public String getDtstamp() {
    return dtstamp;
  }

  /**
   * @param val last modified
   */
  @IcalProperty(pindex = PropertyInfoIndex.LAST_MODIFIED,
                jname = "lastModified",
                eventProperty = true,
                todoProperty = true,
                journalProperty = true,
                timezoneProperty = true)
  public void setLastmod(final String val) {
    lastmod = val;
  }

  /**
   * @return String lastmod
   */
  public String getLastmod() {
    return lastmod;
  }

  /**
   * @param val created
   */
  @IcalProperty(pindex = PropertyInfoIndex.CREATED,
                required = true,
                eventProperty = true,
                todoProperty = true,
                journalProperty = true,
                freeBusyProperty = true
                )
  public void setCreated(final String val) {
    created = val;
  }

  /**
   * @return String created
   */
  public String getCreated() {
    return created;
  }

  /**
   * @param val schedule tag
   */
  @IcalProperty(pindex = PropertyInfoIndex.SCHEDULE_TAG,
                jname = "scheduleTag",
                eventProperty = true,
                todoProperty = true)
  public void setStag(final String val) {
    stag = val;
  }

  /**
   * @return String stag
   */
  public String getStag() {
    return stag;
  }

  /** Set the rfc priority for this event
   *
   * @param val    rfc priority number
   */
  @IcalProperty(pindex = PropertyInfoIndex.PRIORITY,
                eventProperty = true,
                todoProperty = true
                )
  public void setPriority(final Integer val) {
    priority = val;
  }

  /** Get the events rfc priority
   *
   * @return Integer    the events rfc priority
   */
  public Integer getPriority() {
    return priority;
  }

  /** Set the rfc sequence for this event
   *
   * @param val    rfc sequence number
   */
  @IcalProperty(pindex = PropertyInfoIndex.SEQUENCE,
                eventProperty = true,
                todoProperty = true,
                journalProperty = true
                )
  public void setSequence(final int val) {
    sequence = val;
  }

  /** Get the events rfc sequence
   *
   * @return int    the events rfc sequence
   */
  public int getSequence() {
    return sequence;
  }

  /**
   * @param val a BwLocation object
   */
  @IcalProperties({
    @IcalProperty(pindex = PropertyInfoIndex.LOCATION,
                  reschedule = true,
                  eventProperty = true,
                  todoProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.LOCATION_STR,
                  jname = "locationStr",
                  required = true,
                  eventProperty = true,
                  todoProperty = true)}
  )
  public void setLocation(final BwLocation val) {
    location = val;
  }

  /**
   * @return  BwLocation or null
   */
  public BwLocation getLocation() {
    return location;
  }

  @NoDump
  @NoProxy
  @NoWrap
  public List<BwXproperty> getVlocations() {
    return getXproperties(BwXproperty.xBedeworkVLocation);
  }

  /** Set the uid
   *
   * @param val    String uid
   */
  @IcalProperty(pindex = PropertyInfoIndex.UID,
                required = true,
                eventProperty = true,
                todoProperty = true,
                journalProperty = true,
                freeBusyProperty = true)
  public void setUid(final String val) {
    uid = val;
  }

  /** Get the uid
   *
   * @return String   uid
   */
  public String getUid() {
    return uid;
  }

  /** Set the event's transparency
   *
   * @param val    String event's transparency
   */
  @IcalProperty(pindex = PropertyInfoIndex.TRANSP,
                jname = "transp",
                eventProperty = true)
  public void setTransparency(final String val) {
    transparency = val;
  }

  /** Get the event's transparency
   *
   * @return String   the event's transparency
   */
  public String getTransparency() {
    return transparency;
  }

  /** todo only - Set the percentComplete
   *
   * @param val    percentComplete
   */
  @IcalProperty(pindex = PropertyInfoIndex.PERCENT_COMPLETE,
                jname = "percentComplete",
                todoProperty = true)
  public void setPercentComplete(final Integer val) {
    percentComplete = val;
  }

  /** Get the percentComplete
   *
   * @return Integer    percentComplete
   */
  public Integer getPercentComplete() {
    return percentComplete;
  }

  /** todo only - UTC time completed
   * @param val UTC completed time
   */
  @IcalProperty(pindex = PropertyInfoIndex.COMPLETED,
                todoProperty = true)
  public void setCompleted(final String val) {
    completed = val;
  }

  /**
   * @return String completed
   */
  public String getCompleted() {
    return completed;
  }

  /** Set the scheduleMethod for this event. Takes methodType values defined
   * in Icalendar
   *
   * @param val    scheduleMethod
   */
  @IcalProperty(pindex = PropertyInfoIndex.SCHEDULE_METHOD,
                jname = "scheduleMethod",
                eventProperty = true,
                todoProperty = true)
  public void setScheduleMethod(final int val) {
    scheduleMethod = val;
  }

  /** Get the events scheduleMethod
   *
   * @return int    the events scheduleMethod
   */
  public int getScheduleMethod() {
    return scheduleMethod;
  }

  /** Set the event's originator
   *
   * @param val    String event's originator
   */
  @IcalProperty(pindex = PropertyInfoIndex.ORIGINATOR,
                eventProperty = true,
                todoProperty = true,
                journalProperty = true,
                freeBusyProperty = true,
                timezoneProperty = true)
  public void setOriginator(final String val) {
    originator = val;
  }

  /** Get the event's originator
   *
   * @return String   the event's originator
   */
  public String getOriginator() {
    return originator;
  }

  /** Set the scheduleState for this event
   *
   * @param val    scheduleState
   */
  @IcalProperty(pindex = PropertyInfoIndex.SCHEDULE_STATE,
                jname = "scheduleState",
                eventProperty = true,
                todoProperty = true)
  public void setScheduleState(final int val) {
    if ((val != scheduleStateNotProcessed) &&
        (val != scheduleStateProcessed) &&
        (val != scheduleStateExternalDone)) {
      throw new RuntimeException("org.bedework.badvalue");
    }

    scheduleState = val;
  }

  /** Get the events scheduleState
   *
   * @return int    the events scheduleState
   */
  public int getScheduleState() {
    return scheduleState;
  }

  /** True if this is a valid organizer scheduling object. (See CalDAV
   * scheduling specification). This can be set false (and will be on copy) to
   * suppress sending of invitations, e.g. for a draft.
   *
   * <p>When the event is added this flag will be set true if the appropriate
   * conditions are satisfied.
   * <br/>
   * NOTE: these have the NoProxy annotation as we treat them specially
   * in the proxy until we get a schema update (4.0)
   *
   * @param val Boolean  True if this is a valid organizer scheduling object
   */
  @IcalProperty(pindex = PropertyInfoIndex.ORGANIZER_SCHEDULING_OBJECT,
                jname = "organizerSchedulingObject",
                eventProperty = true,
                todoProperty = true,
                journalProperty = true,
                freeBusyProperty = true)
  @NoProxy
  public void setOrganizerSchedulingObject(final Boolean val) {
    organizerSchedulingObject = val;
  }

  /**
   *
   * NOTE: these have the NoProxy annotation as we treat them specially
   * in the proxy until we get a schema update (4.0)
   * @return Boolean
   */
  @NoProxy
  public Boolean getOrganizerSchedulingObject() {
    return organizerSchedulingObject;
  }

  /** True if this is a valid attendee scheduling object.
   * (See CalDAV scheduling specification)
   * <br/>
   * NOTE: these have the NoProxy annotation as we treat them specially
   * in the proxy until we get a schema update (4.0)
   *
   * @param val Boolean True if this is a valid attendee scheduling object
   */
  @IcalProperty(pindex = PropertyInfoIndex.ATTENDEE_SCHEDULING_OBJECT,
                jname = "attendeeSchedulingObject",
                eventProperty = true,
                todoProperty = true,
                journalProperty = true)
  @NoProxy
  public void setAttendeeSchedulingObject(final Boolean val) {
    attendeeSchedulingObject = val;
  }

  /**
   *
   * NOTE: these have the NoProxy annotation as we treat them specially
   * in the proxy until we get a schema update (4.0)
   * @return Boolean
   */
  @NoProxy
  public Boolean getAttendeeSchedulingObject() {
    return attendeeSchedulingObject;
  }

  /** Set the relatedTo property
   *
   * @param val    BwRelatedTo relatedTo property
   */
  @IcalProperty(pindex = PropertyInfoIndex.RELATED_TO,
                jname = "relatedTo",
                nested = true,
                eventProperty = true,
                todoProperty = true,
                journalProperty = true)
  public void setRelatedTo(final BwRelatedTo val) {
    relatedTo = val;
  }

  /** Get the relatedTo property
   *
   * @return BwRequestStatus   the relatedTo property
   */
  @Dump(compound = true)
  public BwRelatedTo getRelatedTo() {
    return relatedTo;
  }

  /* ====================================================================
   *                      X-prop methods
   * ==================================================================== */

  /**
   * @param val x-properties
   */
  @IcalProperty(pindex = PropertyInfoIndex.XPROP,
          jname = "xprop",
          adderName = "xproperty",
          nested = true,
          keyindex = PropertyInfoIndex.NAME,
          eventProperty = true,
          todoProperty = true,
          journalProperty = true,
          freeBusyProperty = true,
          timezoneProperty = true)
  @IcalProperties({
    @IcalProperty(pindex = PropertyInfoIndex.AFFECTS_FREE_BUSY,
            jname = "affectsFreeBusy",
            eventProperty = true,
            todoProperty = true,
            journalProperty = true,
            freeBusyProperty = true,
            timezoneProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.ALIAS_URI,
            jname = "aliasURI",
            eventProperty = true,
            todoProperty = true,
            journalProperty = true,
            freeBusyProperty = true,
            timezoneProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.CALSCALE,
            jname = "calscale",
            eventProperty = true,
            todoProperty = true,
            journalProperty = true,
            freeBusyProperty = true,
            timezoneProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.CALTYPE,
            jname = "caltype",
            eventProperty = true,
            todoProperty = true,
            journalProperty = true,
            freeBusyProperty = true,
            timezoneProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.COL_PROPERTIES,
            jname = "colProperties",
            eventProperty = true,
            todoProperty = true,
            journalProperty = true,
            freeBusyProperty = true,
            timezoneProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.COLPATH,
            jname = "colPath",
            eventProperty = true,
            todoProperty = true,
            journalProperty = true,
            freeBusyProperty = true,
            timezoneProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.CONCEPT,
            jname = "concepts",
            eventProperty = true,
            todoProperty = true,
            journalProperty = true,
            freeBusyProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.DISPLAY,
            jname = "display",
            eventProperty = true,
            todoProperty = true,
            journalProperty = true,
            freeBusyProperty = true,
            timezoneProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.EVENTREG_END,
            jname = "eventregEnd",
            eventProperty = true,
            todoProperty = true,
            journalProperty = true,
            freeBusyProperty = true,
            timezoneProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.EVENTREG_MAX_TICKETS,
            jname = "eventregMaxTickets",
            eventProperty = true,
            todoProperty = true,
            journalProperty = true,
            freeBusyProperty = true,
            timezoneProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.EVENTREG_MAX_TICKETS_PER_USER,
            jname = "eventregMaxTicketsPerUser",
            eventProperty = true,
            todoProperty = true,
            journalProperty = true,
            freeBusyProperty = true,
            timezoneProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.EVENTREG_START,
            jname = "eventregStart",
            eventProperty = true,
            todoProperty = true,
            journalProperty = true,
            freeBusyProperty = true,
            timezoneProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.EVENTREG_WAIT_LIST_LIMIT,
            jname = "eventregWaitListLimit",
            eventProperty = true,
            todoProperty = true,
            journalProperty = true,
            freeBusyProperty = true,
            timezoneProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.EXPECT_REPLY,
            jname = "expectReply",
            eventProperty = true,
            todoProperty = true,
            journalProperty = true,
            freeBusyProperty = true,
            timezoneProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.FILTER_EXPR,
            jname = "filterExpr",
            eventProperty = true,
            todoProperty = true,
            journalProperty = true,
            freeBusyProperty = true,
            timezoneProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.IGNORE_TRANSP,
            jname = "ignoreTransp",
            eventProperty = true,
            todoProperty = true,
            journalProperty = true,
            freeBusyProperty = true,
            timezoneProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.IMAGE,
            jname = "image",
            eventProperty = true,
            todoProperty = true,
            journalProperty = true,
            freeBusyProperty = true,
            timezoneProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.INSTANCE,
            jname = "instance",
            annotationRequired = true,
            eventProperty = true,
            todoProperty = true,
            journalProperty = true,
            freeBusyProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.KIND,
            jname = "kind",
            eventProperty = true,
            todoProperty = true,
            journalProperty = true,
            freeBusyProperty = true,
            timezoneProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.LAST_REFRESH,
            jname = "lastRefresh",
            eventProperty = true,
            todoProperty = true,
            journalProperty = true,
            freeBusyProperty = true,
            timezoneProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.LAST_REFRESH_STATUS,
            jname = "lastRefreshStatus",
            eventProperty = true,
            todoProperty = true,
            journalProperty = true,
            freeBusyProperty = true,
            timezoneProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.MEMBER_OF,
            jname = "memberOf",
            eventProperty = true,
            todoProperty = true,
            journalProperty = true,
            freeBusyProperty = true,
            timezoneProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.METHOD,
            jname = "method",
            eventProperty = true,
            todoProperty = true,
            journalProperty = true,
            freeBusyProperty = true,
            timezoneProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.PARAMETERS,
            jname = "pars",
            eventProperty = true,
            todoProperty = true,
            journalProperty = true,
            freeBusyProperty = true,
            timezoneProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.PARTICIPANT_TYPE,
            jname = "roles",
            eventProperty = true,
            todoProperty = true,
            journalProperty = true,
            freeBusyProperty = true,
            timezoneProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.PARTICIPATION_STATUS,
            jname = "participationStatus",
            eventProperty = true,
            todoProperty = true,
            journalProperty = true,
            freeBusyProperty = true,
            timezoneProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.PRODID,
            jname = "prodid",
            eventProperty = true,
            todoProperty = true,
            journalProperty = true,
            freeBusyProperty = true,
            timezoneProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.REFRESH_RATE,
            jname = "refreshRate",
            eventProperty = true,
            todoProperty = true,
            journalProperty = true,
            freeBusyProperty = true,
            timezoneProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.REMOTE_ID,
            jname = "remoteId",
            eventProperty = true,
            todoProperty = true,
            journalProperty = true,
            freeBusyProperty = true,
            timezoneProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.REMOTE_PW,
            jname = "remotePw",
            eventProperty = true,
            todoProperty = true,
            journalProperty = true,
            freeBusyProperty = true,
            timezoneProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.SCHEDULING_FORCE_SEND,
            jname = "scheduleForceSend",
            eventProperty = true,
            todoProperty = true,
            journalProperty = true,
            freeBusyProperty = true,
            timezoneProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.SCHEDULING_SEQUENCE,
            jname = "scheduleSequence",
            eventProperty = true,
            todoProperty = true,
            journalProperty = true,
            freeBusyProperty = true,
            timezoneProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.SCHEDULING_STATUS,
            jname = "scheduleStatus",
            eventProperty = true,
            todoProperty = true,
            journalProperty = true,
            freeBusyProperty = true,
            timezoneProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.SUGGESTED_TO,
            jname = "suggestedTo",
            eventProperty = true,
            todoProperty = true,
            journalProperty = true,
            freeBusyProperty = true,
            timezoneProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.THUMBIMAGE,
            jname = "thumbimage",
            eventProperty = true,
            todoProperty = true,
            journalProperty = true,
            freeBusyProperty = true,
            timezoneProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.TAG,
            jname = "tag",
            eventProperty = true,
            todoProperty = true,
            journalProperty = true,
            freeBusyProperty = true,
            timezoneProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.TOPICAL_AREA,
            jname = "topicalArea",
            eventProperty = true,
            todoProperty = true,
            journalProperty = true,
            freeBusyProperty = true,
            timezoneProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.UNKNOWN_PROPERTY,
            eventProperty = true,
            todoProperty = true,
            journalProperty = true,
            freeBusyProperty = true,
            timezoneProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.UNREMOVEABLE,
            jname = "unremoveable",
            eventProperty = true,
            todoProperty = true,
            journalProperty = true,
            freeBusyProperty = true,
            timezoneProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.URI,
            jname = "uri",
            eventProperty = true,
            todoProperty = true,
            journalProperty = true,
            freeBusyProperty = true,
            timezoneProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.VERSION,
            jname = "version",
            eventProperty = true,
            todoProperty = true,
            journalProperty = true,
            freeBusyProperty = true,
            timezoneProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.VIEW,
            jname = "view",
            eventProperty = true,
            todoProperty = true,
            journalProperty = true,
            freeBusyProperty = true,
            timezoneProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.VPATH,
            jname = "vpath",
            eventProperty = true,
            todoProperty = true,
            journalProperty = true,
            freeBusyProperty = true,
            timezoneProperty = true),

    @IcalProperty(pindex = PropertyInfoIndex.X_BEDEWORK_CONTACT,
                  jname = "xbwcontact",
                  adderName = "xproperty",
                  eventProperty = true,
                  todoProperty = true,
                  journalProperty = true,
                  freeBusyProperty = true,
                  timezoneProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.X_BEDEWORK_LOCATION,
                  jname = "xbwlocation",
                  adderName = "xproperty",
                  eventProperty = true,
                  todoProperty = true,
                  journalProperty = true,
                  freeBusyProperty = true,
                  timezoneProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.X_BEDEWORK_CATEGORIES,
                  jname = "xbwcategories",
                  adderName = "xproperty",
                  eventProperty = true,
                  todoProperty = true,
                  journalProperty = true,
                  freeBusyProperty = true,
                  timezoneProperty = true)}
  )
  public void setXproperties(final List<BwXproperty> val) {
    xproperties = val;
  }

  /**
   * @return List<BwXproperty>
   */
  @Dump(collectionElementName = "xproperty", compound = true)
  public List<BwXproperty> getXproperties() {
    return xproperties;
  }

  /**
   * @return int
   */
  @NoProxy
  @NoDump
  public int getNumXproperties() {
    final List<BwXproperty> c = getXproperties();
    if (c == null) {
      return 0;
    }

    return c.size();
  }

  @Override
  @NoProxy
  @NoDump
  public List<BwXproperty> getXproperties(final String val) {
    final List<BwXproperty> res = new ArrayList<>();
    final List<BwXproperty> xs = getXproperties();
    if (xs == null) {
      return res;
    }

    for (final BwXproperty x: xs) {
      if (x == null) {
        continue;
      }

      if (x.getName().equals(val)) {
        res.add(x);
      }
    }

    return res;
  }

  /** Find x-properties storing the value of the named ical property
   *
   * @param val - name to match
   * @return list of matching properties - never null
   */
  @Override
  @NoProxy
  @NoDump
  public List<BwXproperty> getXicalProperties(final String val) {
    final List<BwXproperty> icalProps = getXproperties(BwXproperty.bedeworkIcalProp);
    if (icalProps.isEmpty()) {
      return icalProps;
    }

    final List<BwXproperty> res = new ArrayList<>();

    for (final BwXproperty x: icalProps) {
      if (x == null) {
        continue;
      }

      final List<Xpar> xpars = x.getParameters();

      final Xpar xp = xpars.get(0);
      if (xp.getValue().equals(val)) {
        res.add(x);
      }
    }

    return res;
  }

  /** Remove all instances of the named property.
   *
   * @param val - name to match
   * @return number of removed proeprties
   */
  @Override
  @NoProxy
  @NoDump
  public int removeXproperties(final String val) {
    final List<BwXproperty> xs = getXproperties(val);

    if (xs.isEmpty()) {
      return 0;
    }

    for (final BwXproperty x: xs) {
      removeXproperty(x);
    }

    return xs.size();
  }

  @NoProxy
  public void addParticipant(final BwParticipant val) {
    addXproperty(new BwXproperty(BwXproperty.bedeworkParticipant,
                                 null, val.asString()));
  }

  @Override
  @NoProxy
  public void addXproperty(final BwXproperty val) {
    List<BwXproperty> c = getXproperties();
    if (c == null) {
      c = new ArrayList<>();
      setXproperties(c);
    }

    if (!c.contains(val)) {
      c.add(val);
    }
  }

  /**
   * @param val x-prop to remove
   */
  @NoProxy
  public void removeXproperty(final BwXproperty val) {
    final List<BwXproperty> c = getXproperties();
    if (c == null) {
      return;
    }

    c.remove(val);
  }

  /**
   * @return List of x-properties
   */
  @NoProxy
  public List<BwXproperty> cloneXproperty() {
    if (getNumXproperties() == 0) {
      return null;
    }
    final ArrayList<BwXproperty> xs = new ArrayList<>();

    for (final BwXproperty x: getXproperties()) {
      if (x == null) {
        continue;
      }

      xs.add((BwXproperty)x.clone());
    }

    return xs;
  }

  /** Get the single valued named property
   *
   * @param name of x-prop
   * @return value
   */
  @NoProxy
  @NoDump
  @NoWrap
  public String getXproperty(final String name) {
    final BwXproperty prop = findXproperty(name);

    if (prop == null) {
      return null;
    }

    return prop.getValue();
  }

  /**
   * @param name of x-prop
   * @return first property or null
   */
  @NoProxy
  @NoDump
  @NoWrap
  public BwXproperty findXproperty(final String name) {
    final Collection<BwXproperty> props = getXproperties();

    if (props == null) {
      return null;
    }

    for (final BwXproperty prop: props) {
      if (prop == null) {
        continue;
      }

      if (name.equals(prop.getName())) {
        return prop;
      }
    }

    return null;
  }

  /**
   * @param name of property
   * @param val its value or null to delete
   * @return true if something chnaged
   */
  @NoProxy
  public boolean replaceXproperty(final String name,
                                  final String val) {
    final BwXproperty prop = findXproperty(name);

    if (prop == null) {
      final BwXproperty xp = new BwXproperty(name, null, val);
      addXproperty(xp);

      if (changeSet != null) {
        changeSet.addValue(PropertyInfoIndex.XPROP, xp);
      }
      return true;
    }

    if (val == null) {
      removeXproperty(prop);

      return true;
    }

    if (prop.getValue().equals(val)) {
      return false;
    }

    prop.setValue(val);

    if (changeSet != null) {
      final BwXproperty xp = new BwXproperty(name, null, val);
      changeSet.changed(PropertyInfoIndex.XPROP, prop, xp);
    }
    return true;
  }

  /* ====================================================================
   *               Request status methods
   * ==================================================================== */

  /** Set the requestStatus
   *
   * @param val    BwRequestStatus requestStatus
   */
  @IcalProperty(pindex = PropertyInfoIndex.REQUEST_STATUS,
                adderName = "requestStatus",
                jname = "requestStatus",
                eventProperty = true,
                todoProperty = true,
                journalProperty = true,
                freeBusyProperty = true)
  public void setRequestStatuses(final Set<BwRequestStatus> val) {
    requestStatuses = val;
  }

  /** Get the requestStatus
   *
   * @return Set of BwRequestStatus   the requestStatus
   */
  public Set<BwRequestStatus> getRequestStatuses() {
    return requestStatuses;
  }

  /**
   * @return int
   */
  @NoProxy
  @NoDump
  public int getNumRequestStatuses() {
    final Set<BwRequestStatus> c = getRequestStatuses();
    if (c == null) {
      return 0;
    }

    return c.size();
  }

  /**
   * @param val request status
   */
  @NoProxy
  public void addRequestStatus(final BwRequestStatus val) {
    Set<BwRequestStatus> rs = getRequestStatuses();
    if (rs == null) {
      rs = new TreeSet<>();
      setRequestStatuses(rs);
    }

    rs.add(val);
  }

  /**
   * @param val request status
   * @return boolean
   */
  @NoProxy
  public boolean removeRequestStatus(final BwRequestStatus val) {
    final Set<BwRequestStatus> rs = getRequestStatuses();
    if (rs == null) {
      return false;
    }

    return rs.remove(val);
  }

  /** Return a clone of the Set
   *
   * @return Set of request status
   */
  @NoProxy
  public Set<BwRequestStatus> cloneRequestStatuses() {
    final Set<BwRequestStatus> rs = getRequestStatuses();
    if (rs == null) {
      return null;
    }

    final Set<BwRequestStatus> nrs = new TreeSet<>();

    for (final BwRequestStatus o: rs) {
      nrs.add((BwRequestStatus)o.clone());
    }

    return nrs;
  }

  /** Set the change token
   *
   * @param val change token
   */
  @IcalProperties({
    @IcalProperty(pindex = PropertyInfoIndex.CTOKEN,
                  required = true,
                  eventProperty = true,
                  todoProperty = true,
                  journalProperty = true,
                  freeBusyProperty = true
    ),
    @IcalProperty(pindex = PropertyInfoIndex.ETAG,
                  jname = "etag",
                  eventProperty = true,
                  todoProperty = true,
                  journalProperty = true,
                  freeBusyProperty = true
    ),
    @IcalProperty(pindex = PropertyInfoIndex.LASTMODSEQ,
            jname = "lastModifiedSeq",
            eventProperty = true,
            todoProperty = true,
            journalProperty = true,
            freeBusyProperty = true
    )}
  )
  public void setCtoken(final String val) {
    ctoken = val;
  }

  /**
   * @return the change token
   */
  public String getCtoken() {
    if ((ctoken == null) || (ctoken.length() == 16)) {
      /* ctoken is null or old pre-3.8 data - just return seq 0 */
      return getLastmod() + "-0000";
    }

    return ctoken;
  }

  /* ====================================================================
   *               RecurrenceEntity interface methods
   * ==================================================================== */

  @Override
  @IcalProperty(pindex = PropertyInfoIndex.RECURRING)
  public void setRecurring(final Boolean val) {
    recurring = val;
  }

  @Override
  public Boolean getRecurring() {
    return recurring;
  }

  @Override
  @IcalProperties({
          @IcalProperty(pindex = PropertyInfoIndex.RECURRENCE_ID,
                  jname = "recurrenceId",
                  eventProperty = true,
                  todoProperty = true,
                  journalProperty = true,
                  freeBusyProperty = true
          ),
          @IcalProperty(pindex = PropertyInfoIndex.RANGE)}
  )
  public void setRecurrenceId(final String val) {
    recurrenceId = val;
  }

  @Override
  public String getRecurrenceId() {
    return recurrenceId;
  }

  @Override
  @IcalProperty(pindex = PropertyInfoIndex.RRULE,
                jname = "rrule",
                adderName = "rrule",
                reschedule = true,
                eventProperty = true,
                todoProperty = true,
                journalProperty = true)
  public void setRrules(final Set<String> val) {
    rrules = val;
  }

  @Override
  @Dump(collectionElementName = "rrule")
  public Set<String> getRrules() {
    return rrules;
  }

  @Override
  @IcalProperty(pindex = PropertyInfoIndex.EXRULE,
                jname = "exrule",
                adderName = "exrule",
                reschedule = true,
                eventProperty = true,
                todoProperty = true,
                journalProperty = true)
  public void setExrules(final Set<String> val) {
    exrules = val;
  }

  @Override
  public Set<String> getExrules() {
    return exrules;
  }

  @Override
  @IcalProperty(pindex = PropertyInfoIndex.RDATE,
                jname = "rdate",
                adderName = "rdate",
                reschedule = true,
                eventProperty = true,
                todoProperty = true,
                journalProperty = true)
  public void setRdates(final Set<BwDateTime> val) {
    rdates = val;
    if (!Util.isEmpty(val)) {
      setRecurring(true);
    }
  }

  @Override
  @Dump(collectionElementName = "rdate")
  public Set<BwDateTime> getRdates() {
    return rdates;
  }

  @Override
  @IcalProperty(pindex = PropertyInfoIndex.EXDATE,
                jname = "exdate",
                adderName = "exdate",
                reschedule = true,
                eventProperty = true,
                todoProperty = true,
                journalProperty = true)
  public void setExdates(final Set<BwDateTime> val) {
    exdates = val;
  }

  @Override
  @Dump(collectionElementName = "exdate")
  public Set<BwDateTime> getExdates() {
    return exdates;
  }

  /* ====================================================================
   *                   Recurrence Helper methods
   * ==================================================================== */

  @Override
  @NoProxy
  public boolean isRecurringEntity() {
    return testRecurring() ||
           hasExdates() ||
           hasRdates() ||
           hasExrules() ||
           hasRrules();
  }

  @Override
  @NoProxy
  public boolean testRecurring() {
    if (getRecurring() == null) {
      return false;
    }

    return getRecurring();
  }

  @Override
  @NoProxy
  public boolean hasRrules() {
    return !Util.isEmpty(getRrules());
  }

  @Override
  @NoProxy
  public void addRrule(final String val) {
    Set<String> c = getRrules();

    if (c == null) {
      c = new TreeSet<>();
      setRrules(c);
    }

    if (!c.contains(val)) {
      c.add(val);
      setRecurring(true);
    }
  }

  @Override
  @NoProxy
  public boolean hasExrules() {
    return !Util.isEmpty(getExrules());
  }

  @Override
  @NoProxy
  public void addExrule(final String val) {
    Set<String> c = getExrules();

    if (c == null) {
      c = new TreeSet<>();
      setExrules(c);
    }

    c.add(val);
  }

  @Override
  @NoProxy
  public boolean hasRdates() {
    return !Util.isEmpty(getRdates());
  }

  @Override
  @NoProxy
  public void addRdate(final BwDateTime val) {
    Set<BwDateTime> c = getRdates();

    if (c == null) {
      c = new TreeSet<>();
      setRdates(c);
    }

    if (!c.contains(val)) {
      c.add(BwDateTimeRdate.make(val));
      setRecurring(true);
    }
  }

  @Override
  @NoProxy
  public boolean hasExdates() {
    return !Util.isEmpty(getExdates());
  }

  @Override
  @NoProxy
  public void addExdate(final BwDateTime val) {
    Set<BwDateTime> c = getExdates();

    if (c == null) {
      c = new TreeSet<>();
      setExdates(c);
    }

    c.add(BwDateTimeExdate.make(val));
  }

  /* ====================================================================
   *               StartEndComponent interface methods
   * NoProxy as the proxy handles these specially
   * ==================================================================== */

  @Override
  @IcalProperties({
    @IcalProperty(pindex = PropertyInfoIndex.DTSTART,
                  presenceField = "dtval",
                  required = true,
                  reschedule = true,
                  eventProperty = true,
                  todoProperty = true,
                  journalProperty = true,
                  freeBusyProperty = true,
                  timezoneProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.INDEX_START,
                  jname = "indexStart",
                  presenceField = "dtval",
                  required = true,
                  reschedule = true,
                  eventProperty = true,
                  todoProperty = true,
                  journalProperty = true,
                  freeBusyProperty = true,
                  timezoneProperty = true)}
  )
  @NoProxy
  public void setDtstart(final BwDateTime val) {
    dtstart = BwDateTimeDtStart.make(val);
  }

  @Override
  public BwDateTime getDtstart() {
    return dtstart;
  }

  @Override
  @IcalProperties({
    @IcalProperty(pindex = PropertyInfoIndex.DTEND,
                  presenceField = "dtval",
                  required = true,
                  reschedule = true,
                  eventProperty = true,
                  freeBusyProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.INDEX_END,
                  jname = "indexEnd",
                  presenceField = "dtval",
                  required = true,
                  reschedule = true,
                  eventProperty = true,
                  freeBusyProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.DUE,
                  presenceField = "dtval",
                  reschedule = true,
                  todoProperty = true)}
    )
  @NoProxy
  public void setDtend(final BwDateTime val) {
    dtend = BwDateTimeDtEnd.make(val);
  }

  @Override
  public BwDateTime getDtend() {
    return dtend;
  }

  @Override
  @NoProxy
  @IcalProperty(pindex = PropertyInfoIndex.END_TYPE,
                jname = "endType",
                required = true,
                eventProperty = true,
                todoProperty = true,
                journalProperty = true)
  public void setEndType(final char val) {
    endType = val;
  }

  @Override
  public char getEndType() {
    return endType;
  }

  @Override
  @NoProxy
  @IcalProperty(pindex = PropertyInfoIndex.DURATION,
                required = true,
                reschedule = true,
                eventProperty = true,
                todoProperty = true,
                freeBusyProperty = true
                )
  public void setDuration(final String val) {
   duration = val;
  }

  @Override
  public String getDuration() {
    return duration;
  }

  /** Set the event's publish URL
   *
   *
   * @param val   string URL
   */
  @IcalProperty(pindex = PropertyInfoIndex.ESTIMATED_DURATION,
          todoProperty = true)
  @NoProxy
  public void setEstimatedDuration(final String val) {
    replaceXproperty(BwXproperty.estimatedDuration, val);
  }

  /**
   *
   *  @return the event's publish URL
   */
  @NoProxy
  @NoDump
  public String getEstimatedDuration() {
    return getXproperty(BwXproperty.estimatedDuration);
  }

  @Override
  @NoProxy
  @IcalProperty(pindex = PropertyInfoIndex.NO_START,
                jname = "noStart",
                required = true,
                eventProperty = true,
                todoProperty = true,
                journalProperty = true)
  public void setNoStart(final Boolean val) {
    noStart = val;
  }

  @Override
  public Boolean getNoStart() {
    return noStart;
  }

  /* ====================================================================
   *               AlarmsEntity interface methods
   * ==================================================================== */

  @Override
  @IcalProperty(pindex = PropertyInfoIndex.VALARM,
          jname = "alarm",
          adderName = "alarm",
          eventProperty = true,
          todoProperty = true)
  public void setAlarms(final Set<BwAlarm> val) {
    alarms = val;
  }

  @Override
  @Dump(collectionElementName = "alarm", compound = true)
  public Set<BwAlarm> getAlarms() {
    return alarms;
  }

  @Override
  @NoProxy
  @NoDump
  public int getNumAlarms() {
    final Set<BwAlarm> c = getAlarms();
    if (c == null) {
      return 0;
    }

    return c.size();
  }

  @Override
  @NoProxy
  public void addAlarm(final BwAlarm val) {
    Set<BwAlarm> rs = getAlarms();
    if (rs == null) {
      rs = new TreeSet<>();
      setAlarms(rs);
    }

    rs.add(val);
  }

  @Override
  @NoProxy
  public boolean removeAlarm(final BwAlarm val) {
    final Set<BwAlarm> rs = getAlarms();
    if (rs == null) {
      return false;
    }

    return rs.remove(val);
  }

  /** Return a clone of the Set
   *
   * @return Set of BwAlarm
   */
  @Override
  @NoProxy
  public Set<BwAlarm> cloneAlarms() {
    final Set<BwAlarm> rs = getAlarms();
    if (rs == null) {
      return null;
    }

    final Set<BwAlarm> nrs = new TreeSet<>();

    for (final BwAlarm al: rs) {
      nrs.add((BwAlarm)al.clone());
    }

    return nrs;
  }

  /* ====================================================================
   *                   AttachmentsEntity interface methods
   * ==================================================================== */

  @Override
  @IcalProperty(pindex = PropertyInfoIndex.ATTACH,
                adderName = "attachment",
                eventProperty = true,
                todoProperty = true,
                journalProperty = true)
  public void setAttachments(final Set<BwAttachment> val) {
    attachments = val;
  }

  @Override
  @Dump(collectionElementName = "attachment", compound = true)
  public Set<BwAttachment> getAttachments() {
    return attachments;
  }

  @Override
  @NoProxy
  @NoDump
  public int getNumAttachments() {
    final Set<BwAttachment> as = getAttachments();
    if (as == null) {
      return 0;
    }

    return as.size();
  }

  @Override
  @NoProxy
  public void addAttachment(final BwAttachment val) {
    Set<BwAttachment> as = getAttachments();
    if (as == null) {
      as = new TreeSet<>();
      setAttachments(as);
    }

    as.add(val);
  }

  @Override
  @NoProxy
  public boolean removeAttachment(final BwAttachment val) {
    final Set<BwAttachment> as = getAttachments();
    if (as == null) {
      return false;
    }

    return as.remove(val);
  }

  @Override
  @NoProxy
  public Set<BwAttachment> copyAttachments() {
    if (getNumAttachments() == 0) {
      return null;
    }

    return new TreeSet<>(getAttachments());
  }

  @Override
  @NoProxy
  public Set<BwAttachment> cloneAttachments() {
    if (getNumAttachments() == 0) {
      return null;
    }
    final TreeSet<BwAttachment> ts = new TreeSet<>();

    for (final BwAttachment att: getAttachments()) {
      ts.add((BwAttachment)att.clone());
    }

    return ts;
  }

  @Override
  @NoProxy
  public void clearAttachments() {
    if (getNumAttachments() == 0) {
      return;
    }

    getAttachments().clear();
  }

  /* ====================================================================
   *                   AttendeesEntity interface methods
   * ==================================================================== */

  @Override
  @IcalProperties({
    @IcalProperty(pindex = PropertyInfoIndex.ATTENDEE,
                  jname = "attendee",
                  adderName = "attendee",
                  eventProperty = true,
                  todoProperty = true,
                  journalProperty = true,
                  freeBusyProperty = true),
    @IcalProperty(pindex = PropertyInfoIndex.VOTER,
                  jname = "voter",
                  adderName = "voter",
                  vpollProperty = true)})
  public void setAttendees(final Set<BwAttendee> val) {
    attendees = val;
  }

  @Override
  @Dump(collectionElementName = "attendee", compound = true)
  @CloneForOverride(cloneCollectionType = "TreeSet",
                    cloneElementType = "BwAttendee")
  public Set<BwAttendee> getAttendees() {
    return attendees;
  }

  @Override
  @NoProxy
  @NoDump
  public int getNumAttendees() {
    final Set<BwAttendee> as = getAttendees();
    if (as == null) {
      return 0;
    }

    return as.size();
  }

  @Override
  @NoProxy
  public void addAttendee(final BwAttendee val) {
    Set<BwAttendee> as = getAttendees();
    if (as == null) {
      as = new TreeSet<>();
      setAttendees(as);
    }

    as.add(val);
  }

  @Override
  @NoProxy
  public boolean removeAttendee(final BwAttendee val) {
    final Set<BwAttendee> as = getAttendees();
    if (as == null) {
      return false;
    }

    return as.remove(val);
  }

  @Override
  @NoProxy
  public Set<BwAttendee> copyAttendees() {
    if (getNumAttendees() == 0) {
      return null;
    }

    return new TreeSet<>(getAttendees());
  }

  @Override
  @NoProxy
  public Set<BwAttendee> cloneAttendees() {
    if (getNumAttendees() == 0) {
      return null;
    }
    final TreeSet<BwAttendee> ts = new TreeSet<>();

    for (final BwAttendee att: getAttendees()) {
      ts.add((BwAttendee)att.clone());
    }

    return ts;
  }

  private static final String mailTo = "mailto:";

  /** Find an attendee entry for the given uri (calendar address).
   *
   * @param uri to find
   * @return BwAttendee or null if none exists
   */
  @NoProxy
  public BwAttendee findAttendee(final String uri) {
    if (getNumAttendees() == 0) {
      return null;
    }

    int uriLen = uri.length();
    boolean hasMailTo = false;
    int uriStart = 0;
    if (uri.toLowerCase().startsWith(mailTo)) {
      hasMailTo = true;
      uriStart = 7;
    }

    while (uri.charAt(uriLen - 1) == '/') {
      uriLen--;
      if (uriLen <= uriStart) {
        return null;
      }
    }

    final String uriSeg = uri.substring(uriStart, uriLen);

    for (final BwAttendee att: getAttendees()) {
      final String auri = att.getAttendeeUri();
      int auriLen = auri.length();
      int auriStart = 0;

      if (hasMailTo) {
        if (!auri.toLowerCase().startsWith(mailTo)) {
          return null;
        }

        auriStart = uriStart;
      }

      while (auri.charAt(auriLen - 1) == '/') {
        auriLen--;
        if (auriLen <= auriStart) {
          return null;
        }
      }

      if ((auriLen == uriLen) && (uriSeg.regionMatches(0, auri, auriStart, uriLen - auriStart))) {
        return att;
      }
    }

    return null;
  }

  @Override
  @IcalProperty(pindex = PropertyInfoIndex.RECIPIENT,
                jname = "recipient",
                adderName = "recipient",
                eventProperty = true,
                todoProperty = true,
                journalProperty = true,
                freeBusyProperty = true)
  public void setRecipients(final Set<String> val) {
    recipients = val;
  }

  @Override
  @Dump(collectionElementName = "recipient")
  public Set<String> getRecipients() {
    return recipients;
  }

  @Override
  @NoProxy
  @NoDump
  public int getNumRecipients() {
    final Set<String> rs = getRecipients();
    if (rs == null) {
      return 0;
    }

    return rs.size();
  }

  @Override
  @NoProxy
  public void addRecipient(final String val) {
    Set<String> rs = getRecipients();
    if (rs == null) {
      rs = new TreeSet<>();
      setRecipients(rs);
    }

    rs.add(val);
  }

  @Override
  @NoProxy
  public boolean removeRecipient(final String val) {
    final Set<String> rs = getRecipients();
    if (rs == null) {
      return false;
    }

    return rs.remove(val);
  }

  /* ====================================================================
   *               Concept methods
   * ==================================================================== */

  @Override
  @NoProxy
  @NoDump
  public List<BwXproperty> getConcepts() {
    return getXicalProperties("CONCEPT");
  }

  @Override
  @NoProxy
  @NoDump
  public BwXproperty makeConcept(final String val) {
    return BwXproperty.makeIcalProperty("CONCEPT",
                                        null,
                                        val);
  }

  /* ====================================================================
   *               CategorisedEntity interface methods
   * ==================================================================== */

  @Override
  @IcalProperty(pindex = PropertyInfoIndex.CATEGORIES,
                adderName = "category",
                jname = "categories",
                eventProperty = true,
                todoProperty = true,
                journalProperty = true)
  public void setCategories(final Set<BwCategory> val) {
    categories = val;
  }

  @Override
  public Set<BwCategory> getCategories() {
    return categories;
  }

  @Override
  @NoProxy
  @NoDump
  public int getNumCategories() {
    final Set<BwCategory> c = getCategories();
    if (c == null) {
      return 0;
    }

    return c.size();
  }

  @Override
  @NoProxy
  public boolean addCategory(final BwCategory val) {
    if (val == null) {
      throw new RuntimeException("Attempting to store null");
    }

    Set<BwCategory> cats = getCategories();
    if (cats == null) {
      cats = new TreeSet<>();
      setCategories(cats);
    }

    if (!cats.contains(val)) {
      cats.add(val);
      return true;
    }
    
    return false;
  }

  @Override
  @NoProxy
  public boolean removeCategory(final BwCategory val) {
    final Set<BwCategory> cats = getCategories();
    if (cats == null) {
      return false;
    }

    return cats.remove(val);
  }

  @Override
  @NoProxy
  public boolean hasCategory(final BwCategory val) {
    final Set<BwCategory> cats = getCategories();
    if (cats == null) {
      return false;
    }

    return cats.contains(val);
  }

  @Override
  @NoProxy
  public Set<BwCategory> copyCategories() {
    if (getNumCategories() == 0) {
      return null;
    }

    return new TreeSet<>(getCategories());
  }

  @Override
  @NoProxy
  public Set<BwCategory> cloneCategories() {
    if (getNumCategories() == 0) {
      return null;
    }
    final TreeSet<BwCategory> ts = new TreeSet<>();

    for (final BwCategory cat: getCategories()) {
      ts.add((BwCategory)cat.clone());
    }

    return ts;
  }

  /* ====================================================================
   *               CommentedEntity interface methods
   * ==================================================================== */

  @Override
  @IcalProperty(pindex = PropertyInfoIndex.COMMENT,
                adderName = "comment",
                analyzed = true,
                eventProperty = true,
                todoProperty = true,
                journalProperty = true,
                freeBusyProperty = true,
                timezoneProperty = true)
  public void setComments(final Set<BwString> val) {
    comments = val;
  }

  @Override
  public Set<BwString> getComments() {
    return comments;
  }

  @Override
  @NoProxy
  @NoDump
  public int getNumComments() {
    final Set<BwString> rs = getComments();
    if (rs == null) {
      return 0;
    }

    return rs.size();
  }

  @Override
  @NoProxy
  public void addComment(final String lang, final String val) {
    addComment(new BwString(lang, val));
  }

  @Override
  @NoProxy
  public void addComment(final BwString val) {
    Set<BwString> rs = getComments();
    if (rs == null) {
      rs = new TreeSet<>();
      setComments(rs);
    }

    rs.add(val);
  }

  @Override
  @NoProxy
  public boolean removeComment(final BwString val) {
    final Set<BwString> rs = getComments();
    if (rs == null) {
      return false;
    }

    return rs.remove(val);
  }

  /* ====================================================================
   *               Contact interface methods
   * ==================================================================== */

  /** Transition method - replace Set with single value.
   *
   * @param val a contact
   */
  @NoProxy
  public void setContact(final BwContact val) {
    final Set<BwContact> c = getContacts();
    if ((c != null) && (!c.isEmpty())) {
      c.clear();
    }

    if (val != null) {
      addContact(val);
    }
  }

  /** Transition method - return first contact if any available.
   *
   * @return BwContact
   */
  @NoProxy
  @NoDump
  public BwContact getContact() {
    final Set<BwContact> c = getContacts();
    if ((c == null) || (c.isEmpty())) {
      return null;
    }

    return c.iterator().next();
  }

  @Override
  @IcalProperty(pindex = PropertyInfoIndex.CONTACT,
                adderName = "contact",
                eventProperty = true,
                todoProperty = true,
                journalProperty = true,
                freeBusyProperty = true)
  public void setContacts(final Set<BwContact> val) {
    contacts = val;
  }

  @Override
  public Set<BwContact> getContacts() {
    return contacts;
  }

  @Override
  @NoProxy
  @NoDump
  public int getNumContacts() {
    final Set<BwContact> c = getContacts();
    if (c == null) {
      return 0;
    }

    return c.size();
  }

  @Override
  @NoProxy
  public void addContact(final BwContact val) {
    Set<BwContact> cs = getContacts();
    if (cs == null) {
      cs = new TreeSet<>();
      setContacts(cs);
    }

    cs.add(val);
  }

  @Override
  @NoProxy
  public boolean removeContact(final BwContact val) {
    final Set<BwContact> cs = getContacts();
    if (cs == null) {
      return false;
    }

    return cs.remove(val);
  }

  @Override
  @NoProxy
  public boolean hasContact(final BwContact val) {
    final Set<BwContact> cs = getContacts();
    if (cs == null) {
      return false;
    }

    return cs.contains(val);
  }

  @Override
  @NoProxy
  public Set<BwContact> copyContacts() {
    if (getNumContacts() == 0) {
      return null;
    }

    return new TreeSet<>(getContacts());
  }

  @Override
  @NoProxy
  public Set<BwContact> cloneContacts() {
    if (getNumContacts() == 0) {
      return null;
    }
    final TreeSet<BwContact> ts = new TreeSet<>();

    for (final BwContact cat: getContacts()) {
      ts.add((BwContact)cat.clone());
    }

    return ts;
  }

  /* ====================================================================
   *               DescriptionEntity interface methods
   * ==================================================================== */

  @Override
  @IcalProperty(pindex = PropertyInfoIndex.DESCRIPTION,
                jname = "description",
                adderName = "description",
                analyzed = true,
                eventProperty = true,
                todoProperty = true,
                journalProperty = true)
  public void setDescriptions(final Set<BwLongString> val) {
    descriptions = val;
  }

  @Override
  @Dump(collectionElementName = "description")
  public Set<BwLongString> getDescriptions() {
    return descriptions;
  }

  @Override
  @NoProxy
  @NoDump
  public int getNumDescriptions() {
    final Set<BwLongString> rs = getDescriptions();
    if (rs == null) {
      return 0;
    }

    return rs.size();
  }

  @Override
  @NoProxy
  public void addDescription(final String lang, final String val) {
    addDescription(new BwLongString(lang, val));
  }

  @Override
  @NoProxy
  public void addDescription(final BwLongString val) {
    Set<BwLongString> rs = getDescriptions();
    if (rs == null) {
      rs = new TreeSet<>();
      setDescriptions(rs);
    }

    rs.add(val);
  }

  @Override
  @NoProxy
  public boolean removeDescription(final BwLongString val) {
    final Set<BwLongString> rs = getDescriptions();
    if (rs == null) {
      return false;
    }

    return rs.remove(val);
  }

  @Override
  @NoProxy
  public void updateDescriptions(final String lang, final String val) {
    final BwLongString s = findDescription(lang);
    if (val == null) {
      // Removing
      if (s!= null) {
        removeDescription(s);
      }
    } else if (s == null) {
      addDescription(lang, val);
    } else if ((CalFacadeUtil.cmpObjval(val, s.getValue()) != 0)) {
      // XXX Cannot change value in case this is an override collection.

      //s.setValue(val);
      removeDescription(s);
      addDescription(lang, val);
    }
  }

  @Override
  @NoProxy
  public BwLongString findDescription(final String lang) {
    return BwLongString.findLang(lang, getDescriptions());
  }

  @Override
  @NoProxy
  public void setDescription(final String val) {
    updateDescriptions(null, val);
  }

  @Override
  @NoProxy
  @NoDump
  public String getDescription() {
    final BwLongString s = findDescription(null);
    if (s == null) {
      return null;
    }
    return s.getValue();
  }

  /* ====================================================================
   *               ResourcedEntity interface methods
   * ==================================================================== */

  @Override
  @IcalProperty(pindex = PropertyInfoIndex.RESOURCES,
                adderName = "resources",
                analyzed = true,
                eventProperty = true,
                todoProperty = true)
  public void setResources(final Set<BwString> val) {
    resources = val;
  }

  @Override
  public Set<BwString> getResources() {
    return resources;
  }

  @Override
  @NoProxy
  @NoDump
  public int getNumResources() {
    final Set<BwString> rs = getResources();
    if (rs == null) {
      return 0;
    }

    return rs.size();
  }

  @Override
  @NoProxy
  public void addResource(final String lang, final String val) {
    addResource(new BwString(lang, val));
  }

  @Override
  @NoProxy
  public void addResource(final BwString val) {
    Set<BwString> rs = getResources();
    if (rs == null) {
      rs = new TreeSet<>();
      setResources(rs);
    }

    rs.add(val);
  }

  @Override
  @NoProxy
  public boolean removeResource(final BwString val) {
    final Set<BwString> rs = getResources();
    if (rs == null) {
      return false;
    }

    return rs.remove(val);
  }

  /* ====================================================================
   *               SummaryEntity interface methods
   * ==================================================================== */

  @Override
  @IcalProperty(pindex = PropertyInfoIndex.SUMMARY,
                jname = "summary",
                adderName = "summary",
                analyzed = true,
                eventProperty = true,
                todoProperty = true,
                journalProperty = true)
  public void setSummaries(final Set<BwString> val) {
    summaries = val;
  }

  @Override
  @Dump(collectionElementName = "summary")
  public Set<BwString> getSummaries() {
    return summaries;
  }

  @Override
  @NoProxy
  @NoDump
  public int getNumSummaries() {
    final Set<BwString> rs = getSummaries();
    if (rs == null) {
      return 0;
    }

    return rs.size();
  }

  @NoProxy
  public void addSummary(final String lang, final String val) {
    addSummary(new BwString(lang, val));
  }

  @Override
  @NoProxy
  public void addSummary(final BwString val) {
    Set<BwString> rs = getSummaries();
    if (rs == null) {
      rs = new TreeSet<>();
      setSummaries(rs);
    }

    rs.add(val);
  }

  @Override
  @NoProxy
  public boolean removeSummary(final BwString val) {
    final Set<BwString> c = getSummaries();
    if (c == null) {
      return false;
    }

    return c.remove(val);
  }

  @Override
  @NoProxy
  public void updateSummaries(final String lang, final String val) {
    final BwString s = findSummary(lang);
    if (val == null) {
      // Removing
      if (s!= null) {
        removeSummary(s);
      }
    } else if (s == null) {
      addSummary(new BwString(lang, val));
    } else if ((CalFacadeUtil.cmpObjval(val, s.getValue()) != 0)) {
      // XXX Cannot change value in case this is an override Set.

      //s.setValue(val);
      removeSummary(s);
      addSummary(new BwString(lang, val));
    }
  }

  @Override
  @NoProxy
  public BwString findSummary(final String lang) {
    return BwString.findLang(lang, getSummaries());
  }

  @Override
  @NoProxy
  public void setSummary(final String val) {
    updateSummaries(null, val);
  }

  @Override
  @NoProxy
  @NoDump
  public String getSummary() {
    final BwString s = findSummary(null);
    if (s == null) {
      return null;
    }
    return s.getValue();
  }

  /* ====================================================================
   *                   Free and busy methods
   * ==================================================================== */

  /** set the free busy periods
   *
   * @param val     List    of BwFreeBusyComponent
   */
  @NoProxy
  @IcalProperty(pindex = PropertyInfoIndex.FREEBUSY,
                freeBusyProperty = true)
  public void setFreeBusyPeriods(final List<BwFreeBusyComponent> val) {
    freeBusyPeriods = val;
  }

  /** Get the free busy times
   *
   * @return Set    of BwFreeBusyComponent
   */
  @NoProxy
  public List<BwFreeBusyComponent> getFreeBusyPeriods() {
    return freeBusyPeriods;
  }

  /** Add a free/busy component
   *
   * @param val free busy component
   */
  @NoProxy
  public void addFreeBusyPeriod(final BwFreeBusyComponent val) {
    List<BwFreeBusyComponent> fbps = getFreeBusyPeriods();

    if (fbps == null) {
      fbps = new ArrayList<>();
      setFreeBusyPeriods(fbps);
    }

    fbps.add(val);
  }

  /** Set the calsuite name
   *
   * @param val    name
   */
  @IcalProperty(pindex = PropertyInfoIndex.CALSUITE,
          jname = "calSuite",
          eventProperty = true,
          todoProperty = true,
          vpollProperty = true
  )
  @NoProxy
  public void setCalSuite(final String val) {
    replaceXproperty(BwXproperty.bedeworkCalsuite, val);
  }

  /** Get the calsuite name
   *
   *  @return String   name
   */
  @NoProxy
  public String getCalSuite() {
    return getXproperty(BwXproperty.bedeworkCalsuite);
  }

  /* ====================================================================
   *                   VPoll methods
   * ==================================================================== */

   /** The poll winner
   *
   * @param val    Integer id
   */
  @IcalProperty(pindex = PropertyInfoIndex.POLL_WINNER,
                jname = "pollWinner",
                vpollProperty = true
  )
  @NoDump
  public void setPollWinner(final Integer val) {
    if (val == null) {
      final BwXproperty x = findXproperty(BwXproperty.pollWinner);
      if (x != null) {
        removeXproperty(x);
      }
    } else {
      replaceXproperty(BwXproperty.pollWinner, String.valueOf(val));
    }
  }

  /** Get the winning item id
   *
   *  @return Integer   item id
   */
  public Integer getPollWinner() {
    final List<BwXproperty> props = getXproperties(BwXproperty.pollWinner);

    if (Util.isEmpty(props)) {
      return null;
    }

    if (props.size() > 1) {
      return null;
    }

    final BwXproperty p = props.get(0);
    return Integer.valueOf(p.getValue());
  }

  @IcalProperty(pindex = PropertyInfoIndex.POLL_ITEM_ID,
                eventProperty = true,
                todoProperty = true,
                journalProperty = true
  )
  @NoDump
  public void setPollItemId(final Integer val) {
    if (val == null) {
      final BwXproperty x = findXproperty(BwXproperty.pollItemId);
      if (x != null) {
        removeXproperty(x);
      }
    } else {
      replaceXproperty(BwXproperty.pollItemId, val.toString());
    }
  }

  /** Get the event's poll item id
   *
   *  @return Integer   event's poll item id
   */
  public Integer getPollItemId() {
    final List<BwXproperty> props = getXproperties(BwXproperty.pollItemId);

    if (Util.isEmpty(props)) {
      return null;
    }

    if (props.size() > 1) {
      return null;
    }

    final BwXproperty p = props.get(0);
    try {
      return Integer.valueOf(p.getValue());
    } catch (final NumberFormatException nfe) {
      return -1;
    }
  }

  /** Set the poll mode
   *
   * @param val the mode
   */
  @IcalProperty(pindex = PropertyInfoIndex.POLL_MODE,
                vpollProperty = true
  )
  public void setPollMode(final String val) {
    replaceXproperty(BwXproperty.pollMode, val);
  }

  /** Get the poll mode
   *
   *  @return String   poll mode
   */
  public String getPollMode() {
    return getXproperty(BwXproperty.pollMode);
  }

  /** Set the poll completion
   *
   * @param val the completion mode
   */
  @IcalProperty(pindex = PropertyInfoIndex.POLL_COMPLETION,
                vpollProperty = true
  )
  public void setPollCompletion(final String val) {
    replaceXproperty(BwXproperty.pollCompletion, val);
  }

  /** Get the poll Completion
   *
   *  @return String   poll Completion
   */
  public String getPollCompletion() {
    return getXproperty(BwXproperty.pollCompletion);
  }

  /** Set the poll properties
   *
   * @param val    list of interesting properties
   */
  @IcalProperty(pindex = PropertyInfoIndex.POLL_PROPERTIES,
                vpollProperty = true
  )
  public void setPollProperties(final String val) {
    replaceXproperty(BwXproperty.pollProperties, val);
  }

  /** Get the poll properties
   *
   *  @return String   list of interesting properties
   */
  public String getPollProperties() {
    return getXproperty(BwXproperty.pollProperties);
  }

  /** Set the acceptable poll component types in the reponse
   *
   * @param val    list of acceptable poll component types in the reponse
   */
  @IcalProperty(pindex = PropertyInfoIndex.ACCEPT_RESPONSE,
                vpollProperty = true
  )
  public void setPollAcceptResponse(final String val) {
    replaceXproperty(BwXproperty.pollAccceptResponse, val);
  }

  /** Get the acceptable poll component types in the reponse
   *
   *  @return String   list of acceptable poll component types in the reponse
   */
  public String getPollAcceptResponse() {
    return getXproperty(BwXproperty.pollAccceptResponse);
  }

  /* * Set the vpoll items
   *
   * @param val    Set<String>
   * /
  @IcalProperty(pindex = PropertyInfoIndex.POLL_ITEM,
                vpollProperty = true
  )
  @NoProxy
  public void setPollItems(final Set<String> val) {
    pollItems = val;
  }*/

  /** Get the vpoll items (candidates)
   *
   * @return Set<String>   candidates
   */
  @IcalProperty(pindex = PropertyInfoIndex.POLL_ITEM,
                vpollProperty = true
  )
  @NoProxy
  public Set<String> getPollItems() {
    final List<BwXproperty> props = getXproperties(BwXproperty.pollItem);

    if (Util.isEmpty(props)) {
      return null;
    }

    final Set<String> vals = new TreeSet<>();

    for (final BwXproperty p: props) {
      vals.add(p.getValue());
    }

    return vals;
  }

  /** Clear the vpoll items
   *
   */
  @NoProxy
  public void clearPollItems() {
    final List<BwXproperty> props = getXproperties(BwXproperty.pollItem);

    if (Util.isEmpty(props)) {
      return;
    }

    for (final BwXproperty p: props) {
      removeXproperty(p);
    }
  }

  /** Get the participants
   *
   * @return Set<String>   candidates
   */
  @IcalProperty(pindex = PropertyInfoIndex.PARTICIPANT,
                vpollProperty = true
  )
  @NoProxy
  public SchedulingInfo getSchedulingInfo() {
    if (schedulingInfo == null) {
      schedulingInfo = new SchedulingInfo(this);
    }

    return schedulingInfo;
  }

  /** Add vpoll item
   *
   * @param val the item
   */
  @NoProxy
  public void addPollItem(final String val) {
    final Set<String> pis = getPollItems();

    if ((pis == null) || !pis.contains(val)) {
      final BwXproperty xp = new BwXproperty(BwXproperty.pollItem, null, val);
      addXproperty(xp);

      if (changeSet != null) {
        changeSet.addValue(PropertyInfoIndex.XPROP, xp);
      }
    }
  }

  /**
   *
   * @param val    true for a poll candidate
   */
  public void setPollCandidate(final boolean val) {
    replaceXproperty(BwXproperty.pollCandidate, String.valueOf(val));
  }

  /**
   *
   *  @return true for a poll candidate
   */
  public boolean getPollCandidate() {
    return Boolean.parseBoolean(getXproperty(BwXproperty.pollCandidate));
  }

  /* ====================================================================
   *                   Available methods
   * ==================================================================== */

  /**
   * @param val int busy type
   */
  @NoProxy
  @IcalProperty(pindex = PropertyInfoIndex.BUSYTYPE,
                vavailabilityProperty = true)
  public void setBusyType(final int val) {
    busyType = val;
  }

  /**
   * @return int type of time
   */
  @NoProxy
  public int getBusyType() {
    return busyType;
  }

  /** Set the available uids
   *
   * @param val    Set<String>
   */
  @NoProxy
  public void setAvailableUids(final Set<String> val) {
    availableUids = val;
  }

  /** Get the uids
   *
   * @return Set<String>   uids
   */
  @NoProxy
  public Set<String> getAvailableUids() {
    return availableUids;
  }

  /** Add as available uid
   *
   * @param val Strign uid
   */
  @NoProxy
  public void addAvailableUid(final String val) {
    Set<String> avls = getAvailableUids();

    if (avls == null) {
      avls = new TreeSet<>();
      setAvailableUids(avls);
    }

    avls.add(val);
  }

  /* ====================================================================
   *                   Non-db methods
   * ==================================================================== */

  /** If this is the owner - set the real transparency, otherwise, if it differs
   * add an x-prop
   *
   * @param userHref user in question
   * @param val transp value
   * @return non-null if x-prop is added for this user
   */
  @NoProxy
  public BwXproperty setPeruserTransparency(final String userHref,
                                            final String val) {
    if (userHref.equals(getOwnerHref())) {
      setTransparency(val);
      return null;
    }

    BwXproperty pu = findPeruserXprop(userHref, BwXproperty.peruserPropTransp);

    if (pu == null) {
      pu = new BwXproperty(BwXproperty.peruserPropTransp,
                           BwXproperty.peruserOwnerParam + "=" + userHref,
                           val);

      addXproperty(pu);
      return pu;
    }

    pu.setValue(val);
    return null;
  }

  /** Get the event's transparency
   * @param userHref or null
   *
   * @return String   the event's transparency
   */
  @NoProxy
  public String getPeruserTransparency(final String userHref) {
    if ((userHref == null) ||
            userHref.equals(getOwnerHref())) {
      return getTransparency();
    }

    final BwXproperty pu = findPeruserXprop(userHref,
                                            BwXproperty.peruserPropTransp);

    if (pu == null) {
      return getTransparency();
    }

    return pu.getValue();
  }

  /**
   * @param userHref of user who owns property
   * @param name of property
   * @return x-prop for this user if found
   */
  @NoProxy
  public BwXproperty findPeruserXprop(final String userHref,
                                      final String name) {
    final List<BwXproperty> pus = getXproperties(name);

    for (final BwXproperty pu: pus) {
      if (userHref.equals(pu.getParam(BwXproperty.peruserOwnerParam))) {
        return pu;
      }
    }

    return null;
  }

  /* ====================================================================
   *                   Convenience methods
   * ==================================================================== */

  public static class SuggestedTo {
    private final char status;

    private final String groupHref;

    private final String suggestedByHref;

    public SuggestedTo(final char status,
                       final String groupHref,
                       final String suggestedByHref) {
      this.status = status;
      this.groupHref = groupHref;
      this.suggestedByHref = suggestedByHref;
    }

    public static SuggestedTo make(final String val) {
      if ((val.length() < 6) ||
              (val.charAt(1) != ':')) {
        return null;
      }

      try {
        return new SuggestedTo(val);
      } catch (final Throwable ignored) {
        return null;
      }
    }

    public SuggestedTo(final String val) {
      if ((val.length() > 6) &&
          (val.charAt(1) == ':')) {
        status = val.charAt(0);

        if ((status == accepted) ||
                (status == rejected) ||
                (status == pending)) {
          final String hrefs = val.substring(2);
          final int pos = hrefs.indexOf(":");

          if ((pos > 0) && (pos < hrefs.length() - 1)) {
            groupHref = hrefs.substring(0, pos);
            suggestedByHref = hrefs.substring(pos + 1);
            return;
          }
        }
      }

      throw new RuntimeException("Bad suggested value: " + val);
    }

    public static final char accepted = 'A';
    public static final char rejected = 'R';
    public static final char pending = 'P';

    public char getStatus() {
      return status;
    }

    public String getGroupHref() {
      return groupHref;
    }

    public String getSuggestedByHref() {
      return suggestedByHref;
    }

    public String toString() {
      return String.valueOf(getStatus()) +
              ':' +
              getGroupHref() +
              ':' +
              getSuggestedByHref();
    }
  }

  /** Suggested to values.
   *
   * @return list of suggested to objects
   */
  @NoProxy
  @NoDump
  @NoWrap
  public List<SuggestedTo> getSuggested() {
    final List<SuggestedTo> ss = new ArrayList<>();

    for (final BwXproperty xp: getXproperties(BwXproperty.bedeworkSuggestedTo)) {
      // Ignore bad values
      final SuggestedTo st = SuggestedTo.make(xp.getValue());

      if (st != null) {
        ss.add(st);
      }
    }

    return ss;
  }

  /** Add a suggested to value.
   *
   * @param val suggested to object
   * @return x-property added to event
   */
  @NoProxy
  @NoDump
  @NoWrap
  public BwXproperty addSuggested(final SuggestedTo val) {
    final BwXproperty res = new BwXproperty(BwXproperty.bedeworkSuggestedTo,
                                            null,
                                            val.toString());
    addXproperty(res);

    return res;
  }

  /** Scheduling assistant?
   *
   * @return boolean
   */
  @NoProxy
  @NoDump
  @NoWrap
  public boolean isSchedulingAssistant() {
    return Boolean.parseBoolean(getXproperty(BwXproperty.bedeworkSchedAssist));
  }

  /**
   * @return the String representation of the busyType
   */
  @NoProxy
  @NoDump
  @NoWrap
  public String getBusyTypeString() {
    final int b = getBusyType();

    if ((b < 0) || (b >= busyTypeStrings.length)) {
      return null;
    }

    return busyTypeStrings[b];
  }

  /**
   * @param val the String representation of the busyType
   */
  @NoProxy
  @NoWrap
  public void setBusyTypeString(final String val) {
    if (val == null) {
      return;
    }

    final String uval = val.toUpperCase();

    for (int i = 0; i < busyTypeStrings.length; i++) {
      if (uval.equals(busyTypeStrings[i])) {
        setBusyType(i);
        return;
      }
    }
  }

  /**
   * @param val the String representation of the busyType
   * @return corresponding internal value
   */
  @NoProxy
  @NoWrap
  public static int fromBusyTypeString(final String val) {
    if (val == null) {
      return BwEvent.busyTypeBusyUnavailable;
    }

    final String uval = val.toUpperCase();

    for (int i = 0; i < BwEvent.busyTypeStrings.length; i++) {
      if (uval.equals(BwEvent.busyTypeStrings[i])) {
        return i;
      }
    }
    return BwEvent.busyTypeBusyUnavailable;
  }

  /** Returns true if the event start and end dates lie within the specified
   * limits. Either or both of start and end may be null.
   *
   * @param start - UTC date/time or null
   * @param end - UTC date/time or null
   * @return true if event satisfies the limits
   */
  @NoProxy
  @NoWrap
  public boolean inDateTimeRange(final String start, final String end) {
    if ((getEntityType() == IcalDefs.entityTypeTodo)  &&
        getNoStart()) {
      // XXX Wrong? - true if start - end covers today?
      return true;
    }

    final String evStart = getDtstart().getDate();
    final String evEnd = getDtend().getDate();

    final int evstSt;

    if (end == null) {
      evstSt = -1;   // < infinity
    } else {
      evstSt = evStart.compareTo(end);
    }

    if (evstSt >= 0) {
      return false;
    }

    final int evendSt;

    if (start == null) {
      evendSt = 1;   // > infinity
    } else {
      evendSt = evEnd.compareTo(start);
    }

    return (evendSt > 0) ||
            (evStart.equals(evEnd) && (evendSt >= 0));
  }

  /**
   * @return boolean
   */
  @NoDump
  public boolean getSchedulingObject() {
    return getAttendeeSchedulingObject() || getOrganizerSchedulingObject();
  }

  /** Set the overrides collection
   *
   * @param val    Collection of overrides
   */
  @NoProxy
  @NoWrap
  public void setOverrides(final Collection<BwEventAnnotation> val) {
    overrides = val;
  }

  /** Get the overrides
   *
   *  @return Collection     overrides list
   */
  @NoProxy
  @NoWrap
  @Dump(collectionElementName = "event", compound = true)
  public Collection<BwEventAnnotation> getOverrides() {
    return overrides;
  }

  /** Set the event color property
   *
   * @param val color value
   */
  @NoProxy
  @NoWrap
  public void setColor(final String val) {
    color = val;
  }

  /** Get the event color property
   *
   * @return String event color
   */
  @NoProxy
  @NoWrap
  @NoDump
  public String getColor() {
    return color;
  }

  /** Set the significant change flag
   *
   *  @param val    boolean true if the event was changed in a significant manner
   *               (enough to mean we should update schedule-tag)
   */
  @NoProxy
  @NoWrap
  @NoDump
  public void setSignificantChange(final boolean val) {
    significantChange = val;
  }

  /** Get the significant change flag
   *
   *  @return boolean    true if the event was changed in a significant manner
   *                    (enough to mean we should update schedule-tag)
   */
  @NoProxy
  @NoWrap
  @NoDump
  public boolean getSignificantChange() {
    return significantChange;
  }

  /** Set the force UTC flag
   *
   * @param val true to force UTC
   */
  @NoProxy
  @NoWrap
  public void setForceUTC(final boolean val) {
    forceUTC = val;
  }

  /** Get the force UTC flag
   *
   * @return boolean
   */
  @NoProxy
  @NoWrap
  @NoDump
  public boolean getForceUTC() {
    return forceUTC;
  }

  /** Set parent object
   *
   * @param val BwEvent object.
   */
  @NoProxy
  @NoWrap
  @NoDump
  public void setParent(final BwEvent val) {
    parent = val;
  }

  /** Return any parent object
   *
   * @return BwEvent object.
   */
  @NoProxy
  @NoWrap
  @NoDump
  public BwEvent getParent() {
    return parent;
  }

  /** Set list of referenced categories
   *
   * @param val list of category uids
   */
  @NoProxy
  @NoWrap
  @NoDump
  @Override
  public void setCategoryHrefs(final Set<String> val) {
    categoryUids = val;
  }

  /**
   *
   * @return set of category uids.
   */
  @NoProxy
  @NoWrap
  @NoDump
  @Override
  public Set<String> getCategoryHrefs() {
    return categoryUids;
  }

  /** Set list of referenced contacts
   *
   * @param val list of contact hrefs
   */
  @NoProxy
  @NoWrap
  @NoDump
  @Override
  public void setContactHrefs(final Set<String> val) {
    contactHrefs = val;
  }

  /**
   *
   * @return set of contact uids.
   */
  @NoProxy
  @NoWrap
  @NoDump
  @Override
  public Set<String> getContactHrefs() {
    return contactHrefs;
  }

  /** Set href of referenced location
   *
   * @param val location href or null
   */
  @NoProxy
  @NoWrap
  @NoDump
  @IcalProperty(pindex = PropertyInfoIndex.LOCATION_HREF,
                jname = "locationHref",
                required = true,
                eventProperty = true,
                todoProperty = true,
                journalProperty = true)
  public void setLocationHref(final String val) {
    locationHref = val;
  }

  /**
   *
   * @return location href
   */
  @NoProxy
  @NoWrap
  @NoDump
  public String getLocationHref() {
    return locationHref;
  }

  /** Get change set for the event. The absence of a changes does not
   * mean no changes - there may be overrides to apply.
   *
   * @param userHref needed to handle per-user properties
   * @return null for no changes
   */
  @NoProxy
  @NoWrap
  @NoDump
  public ChangeTable getChangeset(final String userHref) {
    if (changeSet == null) {
      changeSet = new ChangeTable(userHref);
    }

    return changeSet;
  }

  /** Get current change set for the event.
   *
   * @return null for no changes
   */
  @NoProxy
  @NoWrap
  @NoDump
  public ChangeTable getChangeset() {
    return changeSet;
  }

  /** Will force update
   *
   */
  @NoProxy
  @NoWrap
  @NoDump
  public void clearChangeset() {
    if (changeSet == null) {
      return;
    }
    changeSet.clear();
  }

  @NoProxy
  @NoDump
  @IcalProperty(pindex = PropertyInfoIndex.HREF,
                jname = "href",
                required = true,
                eventProperty = true,
                todoProperty = true,
                journalProperty = true)
  public String getHref() {
    return Util.buildPath(false, getColPath(), "/", getName());
  }

  @NoProxy
  @NoDump
  public String getEncodedHref() {
    final String href = Util.buildPath(false, getColPath(), "/", getName());

    if (getRecurrenceId() == null) {
      return href;
    }

    return href + "%23" + getRecurrenceId();
  }

  /** Return all timezone ids this event uses. This is used when an event is
   * added by another user to ensure that the target user has a copy of user
   * specific timezones.
   *
   * @return Set of timezone ids.
   */
  @NoProxy
  @NoDump
  public Set<String> getTimeZoneIds() {
    final Set<String> ids = new TreeSet<>();

    BwDateTime dt = getDtstart();
    if ((dt != null) && (dt.getTzid() != null)) {
      ids.add(dt.getTzid());
    }

    dt = getDtend();
    if ((dt != null) && (dt.getTzid() != null)) {
      ids.add(dt.getTzid());
    }

    Set<BwDateTime> dts = getRdates();
    if (dts != null) {
      for (final BwDateTime rdt: dts) {
        if (rdt.getTzid() != null) {
          ids.add(rdt.getTzid());
        }
      }
    }

    dts = getExdates();
    if (dts != null) {
      for (final BwDateTime rdt: dts) {
        if (rdt.getTzid() != null) {
          ids.add(rdt.getTzid());
        }
      }
    }

    final List<BwFreeBusyComponent> fbcs = getFreeBusyPeriods();
    if (fbcs != null) {
      for (final BwFreeBusyComponent fbc: fbcs) {
        for (final Period p: fbc.getPeriods()) {
          DateTime fdt = p.getStart();
          if (fdt.getTimeZone() != null) {
            ids.add(fdt.getTimeZone().getID());
          }

          fdt = p.getEnd();
          if (fdt.getTimeZone() != null) {
            ids.add(fdt.getTimeZone().getID());
          }
        }
      }
    }

    return ids;
  }

  /** Set the last mod for this event.
   */
  @NoProxy
  public void updateLastmod() {
    setLastmod(new LastModified(new DateTime(true)).getValue());
  }

  /** Set the dtstamp for this event.
   */
  @NoProxy
  public void updateDtstamp() {
    setDtstamp(new DtStamp(new DateTime(true)).getValue());
  }

  /** Set the stag for this event.
   * @param val timestamp
   */
  @NoProxy
  public void updateStag(final Timestamp val) {
    final DateTime dt = new DateTime(val);
//    dt.setUtc(true);

    setStag(new LastModified(dt).getValue() + "-" +
        hex4FromNanos(val.getNanos()));
  }

  /** Set the dtstamp, lastmod and created if created is not set already.
   * @param val timestamp
   */
  @NoProxy
  public void setDtstamps(final Timestamp val) {
    final DateTime dt = new DateTime(val);
    setDtstamp(new DtStamp(dt).getValue());
    setLastmod(new LastModified(dt).getValue());
    setCtoken(getLastmod() + "-" + hex4FromNanos(val.getNanos()));

    if (getCreated() == null) {
      setCreated(new Created(dt).getValue());
    }
  }

  /** Return an object holding just enough for free busy calculation
   *
   * @return BwEvent object.
   */
  @NoProxy
  public BwEvent makeFreeBusyEvent() {
    final BwEvent res = new BwEvent();

    // Fields needed for comparison.
    res.setEntityType(getEntityType());
    res.setColPath(getColPath());
    res.setName(getName());
    res.setUid(getUid());
    res.setRecurrenceId(getRecurrenceId());
    res.setRecurring(false);
    res.setTombstoned(false);

    res.setDtend(getDtend());
    res.setDtstart(getDtstart());
    res.setDuration(getDuration());
    res.setEndType(getEndType());
    res.setTransparency(getTransparency());
    res.setStatus(getStatus());

    res.setAttendeeSchedulingObject(getAttendeeSchedulingObject());
    res.setOrganizerSchedulingObject(getOrganizerSchedulingObject());

    if (getAttendeeSchedulingObject()) {
      // XXX Need at least our attendee entry
      for (final BwAttendee att: getAttendees()) {
        res.addAttendee((BwAttendee)att.clone());
      }
    }

    res.setOwnerHref(getOwnerHref());

    if (getEntityType() == IcalDefs.entityTypeFreeAndBusy) {
      final List<BwFreeBusyComponent> fbcs = getFreeBusyPeriods();
      final List<BwFreeBusyComponent> newfbcs = new ArrayList<>();

      for (final BwFreeBusyComponent fbc: fbcs) {
        newfbcs.add((BwFreeBusyComponent)fbc.clone());
      }

      res.setFreeBusyPeriods(newfbcs);
    }

    /* Add any peruser transp */
    res.setXproperties(getXproperties(BwXproperty.peruserPropTransp));

    return res;
  }

  /** Return a BwDuration populated from the String duration.
   *
   * @return BwDuration
   */
  @NoProxy
  public BwDuration makeDurationBean() {
    return BwDuration.makeDuration(getDuration());
  }

  /** Calculate a value to be used to limit quotas.
   *
   * @return int
   */
  @NoProxy
  public int calculateByteSize() {

    int sz = 40; // Overhead for superclasses.
    sz += 4;   // int entityType = CalFacadeDefs.entityTypeEvent;
   /*
   sv += stringSize(name);        // String name;
   sv += collectionSize(nnn);     // Collection<BwString> summaries;
   sv += collectionSize(nnn);     // Collection<BwLongString> descriptions;
   sv += stringSize(classification);   // String classification;
   sv += collectionSize(nnn);     // Collection<BwString> comments;
   sv += collectionSize(nnn);     // Collection<BwString> resources;
    private BwDateTime dtstart;
    private BwDateTime dtend;
   sv += 16;   // String duration;
   sv += 4;    // Boolean noStart;
   sv += 1;    // char endType = endTypeDate;
   sv += stringSize(link);   // String link;
    private BwGeo geo;
   sv += stringSize(status);   // String status;
   sv += stringSize(cost);   // String cost;
   sv += 1;    // boolean deleted;
   sv += 16;   // String dtstamp;
   sv += 16;   // String lastmod;
   sv += 16;   // String created;
   sv += 4;    // integer priority;
    private Collection<BwCategory> categories = null;
    private Collection<BwContact> contacts;
    private BwLocation location;
   sv += stringSize(name);   // String transparency;
   sv += 4;    // integer percentComplete;
   sv += 16;   // String completed;
    private Collection<BwAttendee> attendees;
   sv += 4;   // Boolean recurring;
   sv += stringSize(uid);   // String uid;
    private Collection<BwAlarm> alarms;
   sv += 16;   // String recurrenceId;
    private Collection<String> rrules;
    private Collection<String> exrules;
    private Collection<BwDateTime> rdates;
    private Collection<BwDateTime> exdates;
   sv += 16;   // String latestDate;
   sv += 4;   // Boolean expanded;
   sv += 4;   // int sequence;
   sv += 4;   // int scheduleMethod;
   sv += stringSize(name);   // String originator;
    private Collection<String> recipients;
   sv += 4;   // int scheduleState;
    private Collection<BwRequestStatus> requestStatuses;
    private BwRelatedTo relatedTo;
   sv += 4;   // int byteSize;
    */

    return sz;
  }

  /** Add our stuff to the StringBuilder
   *
   * @param ts    ToString object for result
   */
  @Override
  @NoProxy
  protected void toStringSegment(final ToString ts) {
    super.toStringSegment(ts);
    ts.newLine();
    ts.append("entityType", getEntityType());
    ts.append("deleted", getDeleted());
    ts.newLine();
    ts.append("dtstamp", getDtstamp());
    ts.newLine();
    ts.append("dtstart", getDtstart());
    ts.newLine();
    ts.append("dtend", getDtend());

    ts.newLine();
    ts.append("status", getStatus());
    ts.newLine();
    ts.append("lastmod", getLastmod());
    ts.append("created", getCreated());
    ts.append("stag", getStag());
    ts.newLine();
    ts.append("priority", getPriority());

    if (getPercentComplete() != null) {
      ts.append("percentComplete", getPercentComplete());
    }

    if (getCompleted() != null) {
      ts.append("completed", getCompleted());
    }

    ts.append("classification", getClassification());
    if (getGeo() != null) {
      ts.append("geo", getGeo());
    }

    ts.newLine();
    ts.append("uid", getUid());
    ts.newLine();
    ts.append("name", getName());

    ts.newLine();
    ts.append("ctoken", getCtoken());

    /* ---------------- recurrence information */
    ts.newLine();
    ts.append("\n, getRecurring", getRecurring());

    if (getRecurrenceId() != null) {
      ts.append("recurrenceId", getRecurrenceId());
    } else {
      if (hasRrules()) {
        ts.append("rrules", getRrules());
      }

      if (hasExrules()) {
        ts.append("exrules", getExrules());
      }

      if (hasExdates()) {
        ts.append("exdates", getExdates());
      }

      if (hasRdates()) {
        ts.append("rdates", getRdates());
      }
    }

    ts.newLine();
    ts.append("organizer", getOrganizer());

    if (getNumRecipients() > 0) {
      ts.append("recipients", getRecipients());
    }

    if (getNumCategories() > 0) {
      ts.newLine();
      ts.append("categories", getCategories());
    }

    if (getNumComments() > 0) {
      ts.newLine();
      ts.append("comments", getComments());
    }

    if (getNumContacts() > 0) {
      ts.newLine();
      ts.append("contacts", getContacts());
    }

    if (getNumSummaries() > 0) {
      ts.newLine();
      ts.append("summary", getSummaries());
    }

    if (getNumDescriptions() > 0) {
      ts.newLine();
      ts.append("description", getDescriptions());
    }

    if (getNumResources() > 0) {
      ts.newLine();
      ts.append("resource", getResources());
    }

    if (getNumAttendees() > 0) {
      ts.newLine();
      ts.append("attendee", getAttendees(), true);
    }

    ts.newLine();
    ts.append("sequence", getSequence());
    ts.append("scheduleMethod", getScheduleMethod());
    ts.newLine();
    ts.append("originator", getOriginator());
    ts.append("scheduleState", getScheduleState());


    if (getNumRequestStatuses() > 0) {
      ts.append("requestStatuses", getRequestStatuses());
    }

    if (getRelatedTo() != null) {
      ts.append("relatedTo", getRelatedTo());
    }

    if (getNumAlarms() > 0) {
      ts.newLine();
      ts.append("alarms", getAlarms(), true);
    }

    ts.append("pollItemId", getPollItemId());
    ts.append("pollCandidate", getPollCandidate());

    if (getEntityType() == IcalDefs.entityTypeVpoll) {
      ts.append("pollWinner", getPollWinner());
      ts.append("pollMode", getPollMode());
      ts.append("pollProperties", getPollProperties());
      ts.append("pollAcceptResponse", getPollAcceptResponse());
      ts.append("pollItems", getPollItems());
    }
  }

  /** Copy this objects fields into the parameter
   *
   * @param ev - to copy to
   */
  @NoProxy
  public void copyTo(final BwEvent ev) {
    super.copyTo(ev);
    ev.setEntityType(getEntityType());
    ev.setName(getName());
    ev.setClassification(getClassification());
    ev.setDtstart(getDtstart());
    ev.setDtend(getDtend());
    ev.setEndType(getEndType());
    ev.setDuration(getDuration());
    ev.setNoStart(getNoStart());

    ev.setLink(getLink());
    ev.setGeo(getGeo());
    ev.setDeleted(getDeleted());
    ev.setStatus(getStatus());
    ev.setCost(getCost());

    ev.schedulingInfo = getSchedulingInfo().copyFor(ev);

    ev.setDtstamp(getDtstamp());
    ev.setLastmod(getLastmod());
    ev.setCreated(getCreated());
    ev.setStag(getStag());
    ev.setPriority(getPriority());
    ev.setSequence(getSequence());

    ev.setLocation(getLocation());

    ev.setUid(getUid());
    ev.setTransparency(getTransparency());
    ev.setPercentComplete(getPercentComplete());
    ev.setCompleted(getCompleted());

    ev.setCategories(copyCategories());

    ev.setContacts(copyContacts());

    ev.setCtoken(getCtoken());

    ev.setRecurrenceId(getRecurrenceId());
    ev.setRecurring(getRecurring());
    if (ev.isRecurringEntity()) {
      ev.setRrules(clone(getRrules()));
      ev.setExrules(clone(getExrules()));
      ev.setRdates(clone(getRdates()));
      ev.setExdates(clone(getExdates()));
    }

    ev.setScheduleMethod(getScheduleMethod());
    ev.setOriginator(getOriginator());

    /* Don't copy these - we always set them anyway
    if (getNumRecipients() > 0) {
      ev.setRecipients(new TreeSet<String>());

      for (String s: getRecipients()) {
        ev.addRecipient(s);
      }
    }*/

    if (getNumComments() > 0) {
      ev.setComments(null);

      for (final BwString str: getComments()) {
        ev.addComment((BwString)str.clone());
      }
    }

    if (getNumSummaries() > 0) {
      ev.setSummaries(null);

      for (final BwString str: getSummaries()) {
        ev.addSummary((BwString)str.clone());
      }
    }

    if (getNumDescriptions() > 0) {
      ev.setDescriptions(null);

      for (final BwLongString str: getDescriptions()) {
        ev.addDescription((BwLongString)str.clone());
      }
    }

    if (getNumResources() > 0) {
      ev.setResources(null);

      for (final BwString str: getResources()) {
        ev.addResource((BwString)str.clone());
      }
    }

    if (getNumXproperties() > 0) {
      ev.setXproperties(null);

      for (final BwXproperty x: getXproperties()) {
        ev.addXproperty((BwXproperty)x.clone());
      }
    }

    ev.setScheduleState(getScheduleState());

    //ev.setRequestStatuses(clone(getRequestStatuses()));

    final BwRelatedTo rt = getRelatedTo();
    if (rt != null) {
      ev.setRelatedTo((BwRelatedTo)rt.clone());
    }

    /* These are in x-props in 3.10
    ev.setPollMode(getPollMode());
    ev.setPollProperties(getPollProperties());
    ev.setPollAcceptResponse(getPollAcceptResponse());

    ev.setPollItemId(getPollItemId());
    if (!Util.isEmpty(getPollItems())) {
      for (final String s: getPollItems()) {
        ev.addPollItem(s);
      }
    }
    */

    ev.setPollCandidate(getPollCandidate());
  }

  /**
   * @return a copy suitable for tombstoning.
   */
  @NoProxy
  public BwEvent cloneTombstone() {
    final BwEvent ev = new BwEventObj();

    super.copyTo(ev);
    ev.setEntityType(getEntityType());
    ev.setName(getName());

    ev.setDtstart(getDtstart());
    ev.setDtend(getDtend());
    ev.setEndType(getEndType());
    ev.setDuration(getDuration());
    ev.setNoStart(getNoStart());

    ev.setDeleted(getDeleted());

    ev.setDtstamp(getDtstamp());
    ev.setLastmod(getLastmod());
    ev.setCreated(getCreated());
    ev.setStag(getStag());

    ev.setUid(getUid());

    ev.setRecurring(false);

    ev.setTombstoned(true);
    //ev.setDtstamps();

    return ev;
  }

  /** Check for a valid transparency - null is invalid
   *
   * @param val - possible transparency value
   * @return boolean true = it's OK
   */
  @NoProxy
  public static boolean validTransparency(final String val) {
    if (val == null) {
      /* We could argue that's valid as the default but I think that leads to
       * problems.
       */
      return false;
    }

    if (IcalDefs.transparencyOpaque.equals(val)) {
      return true;
    }

    return IcalDefs.transparencyTransparent.equals(val);
  }

  /** We are using the status to suppress master events - these are associated
   * with 'detached' instances.
   *
   * @param val true for suppressed
   */
  @NoProxy
  @NoDump
  public void setSuppressed(final boolean val) {
    if (val) {
      setStatus(statusMasterSuppressed);
    } else {
      setStatus(null);
    }
  }

  /** We are using the status to suppress master events - these are associated
   * with 'detached' instances.
   *
   * @return true if suppressed
   */
  @NoProxy
  @NoDump
  public boolean getSuppressed() {
    final String s = getStatus();

    if (s == null) {
      return false;
    }

    return s.equals(statusMasterSuppressed);
  }

  /** Assign a guid to this event. A noop if this event already has a guid.
   *
   * @param systemId for the system
   */
  @NoProxy
  public void assignGuid(final String systemId) {
    if ((getName() != null) &&
            (getUid() != null)) {
      return;
    }

    final String guidPrefix = "CAL-" + Uid.getUid();

    if (getName() == null) {
      setName(guidPrefix + ".ics");
    }

    if (getUid() != null) {
      return;
    }

    setUid(guidPrefix + systemId);
  }

  /* * Unmodified here means all but the peruser properties - transparency and
   * alarms.
   *
   * @param master
   * @return true if this is an unmodified instance of the master
   * /
  @NoProxy
  @NoDump
  public boolean instanceOf(BwEvent master) {

  }*/

  /* ====================================================================
   *                   Object methods
   * ==================================================================== */

  @Override
  @NoProxy
  public int compare(final BwEvent e1, final BwEvent e2) {
    if (e1 == e2) {
      return 0;
    }

    int res = CalFacadeUtil.cmpObjval(e1.getDtstart(), e2.getDtstart());
    if (res != 0) {
      return res;
    }

    /* I think it's OK for null calendars during addition of events. */
    res = CalFacadeUtil.cmpObjval(e1.getColPath(),
                                  e2.getColPath());
    if (res != 0) {
      return res;
    }

    /* Compare the names. For scheduling we allow multiple events with the same
     * uid etc in the same calendar.
     */
    res = CalFacadeUtil.cmpObjval(e1.getName(),
                            e2.getName());
    if (res != 0) {
      return res;
    }

    /* Only the same if the recurrence id is equal */
    res = CalFacadeUtil.cmpObjval(e1.getRecurrenceId(),
                                  e2.getRecurrenceId());
    if (res != 0) {
      return res;
    }

    /*
    int thisSeq = e1.getSequence();
    int thatSeq = e2.getSequence();

    if (thisSeq < thatSeq) {
      return -1;
    }

    if (thisSeq > thatSeq) {
      return 1;
    }*/

    return e1.getUid().compareTo(e2.getUid());
  }

  @Override
  @NoProxy
  public int compareTo(final BwEvent o2) {
    return compare(this, o2);
  }

  @Override
  @NoProxy
  public int hashCode() {
    return getUid().hashCode();
  }

  @Override
  @NoProxy
  public String toString() {
    final ToString ts = new ToString(this);

    toStringSegment(ts);

    return ts.toString();
  }

  @Override
  @NoProxy
  public Object clone() {
    final BwEvent ev = new BwEvent();

    copyTo(ev);

    return ev;
  }

  /**
   * @return a version value in microsecords.
   */
  @NoDump
  public long getMicrosecsVersion() {
    try {
      final String[] ct = getCtoken().split("-");

      return new LastModified(ct[0]).getDate().getTime() * 1000000 +
              Integer.parseInt(ct[1], 16) * 100L;
    } catch (final Throwable t) {
      throw new BedeworkException(t);
    }
  }

  /* ====================================================================
   *                   Private methods
   *  =================================================================== */

  /**
   * @param val number to convert to hex
   * @return a 4 digit hex value
   */
  @NoProxy
  public static String hex4(final int val) {
    final String formatted = Integer.toHexString(val % 32001);
    final StringBuilder buf = new StringBuilder("0000");
    buf.replace(4 - formatted.length(), 4, formatted);

    return buf.toString();
  }

  /**
   * @param val - nanoseconds
   * @return a 4 digit hex value
   */
  @NoProxy
  public static String hex4FromNanos(final int val) {
    final String formatted = Integer.toHexString(val / 100000);
    final StringBuilder buf = new StringBuilder("0000");
    buf.replace(4 - formatted.length(), 4, formatted);

    return buf.toString();
  }

  private <T extends Set<?>> T clone(final T c) {
    if (c == null) {
      return null;
    }

    return (T)new TreeSet<>((Collection<T>)c);
  }
}
