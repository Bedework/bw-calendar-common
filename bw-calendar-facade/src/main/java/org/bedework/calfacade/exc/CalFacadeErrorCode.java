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
package org.bedework.calfacade.exc;

/** Codes for use in the calendar facade
 *
 * @author Mike Douglass douglm
 */
public interface CalFacadeErrorCode {
  /** Not allowed to set values */
  String envCannotSetValues =
      "org.bedework.exception.envcannotsetvalues";

  /** Multiple getters for property xxx */
  String envMultipleGetters =
      "org.bedework.exception.envmultiplegetters";

  /** No getters for property xxx */
  String envNoGetters =
      "org.bedework.exception.envnogetters";

  /* ********************** dump/restore **************************** */

  /** No key fields defined */
  String noKeyFields =
      "org.bedework.exception.nokeyfields";

  /* ********************** security related **************************** */

  /** Bad stored password format */
  String badPwFormat =
      "org.bedework.exception.security.badpwformat";

  /* ********************** config app ********************************** */

  /** No optiones defined */
  String configNoOptions =
      "org.bedework.exception.confignooptions";

  /** Missing configurations */
  String noconfigs =
      "org.bedework.config.error.noconfigs";

  /** No metadata */
  String noMetadata =
      "org.bedework.config.error.nometadata";

  /** Missing required value */
  String configMissingValue =
      "org.bedework.config.error.missingvalue";

  /* ****************** principals and ids ****************************** */

  /** principal does not exist */
  String principalNotFound =
      "org.bedework.exception.principalnotfound";

  /** unknown principal type */
  String unknownPrincipalType =
      "org.bedework.exception.unknownprincipaltype";

  /** bad calendar user address */
  String badCalendarUserAddr =
    "org.bedework.caladdr.bad";

  /** null calendar user address */
  String nullCalendarUserAddr =
      "org.bedework.caladdr.null";

  /* ****************** domains ****************************** */

  /** no default domain defined */
  String noDefaultDomain =
      "org.bedework.domains.nodefault";

  /* ****************** (Admin) groups ****************************** */

  /** The admin group already exists */
  String duplicateAdminGroup =
      "org.bedework.exception.duplicateadmingroup";

  /** The group is already on the path to the root (makes a loop) */
  String alreadyOnGroupPath =
      "org.bedework.exception.alreadyonagrouppath";

  /** Group g does not exist */
  String groupNotFound =
      "org.bedework.exception.groupnotfound";

  /* ****************** Calendar suites ****************************** */

  /** The calendar suite does not exist */
  String unknownCalsuite =
      "org.bedework.svci.unknown.calsuite";

  /** */
  String duplicateCalsuite =
      "org.bedework.svci.duplicate.calsuite";

  /** */
  String calsuiteGroupNameTooLong =
      "org.bedework.svci.calsuite.group.name.too.long";

  /** */
  String calsuiteUnknownRootCollection =
      "org.bedework.svci.calsuite.unknown.root.collection";

  /** */
  String calsuiteUnknownSubmissionsCollection =
      "org.bedework.svci.calsuite.unknown.submissions.collection";

  /** No calendar suite defined for this group */
  String noCalsuite =
      "org.bedework.svci.no.calsuite";

  /** Missing or invalid group owner */
  String calsuiteBadowner =
      "org.bedework.svci.calsuite.badowner";

  /** group already assigned to (another) calsuite */
  String calsuiteGroupAssigned =
      "org.bedework.svci.calsuite.group.assigned";

  /** group has no home */
  String missingGroupOwnerHome =
      "org.bedework.svci.calsuite.missing.group.owner.home";

  /** No calendar suite resource collection defined */
  String noCalsuiteResCol =
      "org.bedework.svci.no.calsuite.resource.collection";

  /* ****************** Filters ****************************** */

  /** Duplicate filter */
  String duplicateFilter =
      "org.bedework.exception.duplicate.filter";

  /** The filter does not exist */
  String unknownFilter =
      "org.bedework.exception.unknown.filter";

  /** Unexpected eof while parsing */
  String unexpectedFilterEof =
      "org.bedework.exception.filter.unexpected.eof";

  /** expected uid */
  String filterBadList =
      "org.bedework.exception.filter.bad.list";

  /** expected a word */
  String filterExpectedWord =
      "org.bedework.exception.filter.expected.word";

  /** expected open paren */
  String filterExpectedOpenParen =
      "org.bedework.exception.filter.expected.openparen";

  /** expected close paren */
  String filterExpectedCloseParen =
      "org.bedework.exception.filter.expected.closeparen";

  /** expected property name */
  String filterExpectedPropertyName =
      "org.bedework.exception.filter.expected.propertyname";

  /** expected ascending/descending */
  String filterExpectedAscDesc =
          "org.bedework.exception.filter.expected.ascdesc";

  /** Bad sort expression */
  String filterBadSort =
          "org.bedework.exception.filter.badsort";

  /** expected uid */
  String filterExpectedUid =
      "org.bedework.exception.filter.expected.uid";

  /** Cannot mix logical operators */
  String filterMixedLogicalOperators =
      "org.bedework.exception.filter.mixedlogicaloperators";

  /** Bad property value */
  String filterBadProperty =
      "org.bedework.exception.filter.badproperty";

  /** Bad operator */
  String filterBadOperator =
      "org.bedework.exception.filter.badoperator";

  /** Syntax error */
  String filterSyntax =
      "org.bedework.exception.filter.syntax";

  /** Type requires andop */
  String filterTypeNeedsAnd =
      "org.bedework.exception.filter.typeneedsand";

  /** Bad type value */
  String filterBadType =
      "org.bedework.exception.filter.badtype";

  /** Type must come first */
  String filterTypeFirst =
      "org.bedework.exception.filter.typefirst";

  /** unknown property */
  String filterUnknownView =
          "org.bedework.exception.filter.unknown.view";

  /* ****************** Resources ****************************** */

  /** Missing resource content */
  String missingResourceContent =
      "org.bedework.exception.missing.resource.content";

  /** Duplicate resource */
  String duplicateResource =
      "org.bedework.exception.duplicate.resource";

  /** The resource does not exist */
  String unknownResource =
      "org.bedework.exception.unknown.resource";

  /* ****************** Calendars ****************************** */

  /** Couldn't find calendar */
  String collectionNotFound =
      "org.bedework.exception.calendarnotfound";

  /** Somebody tried to create a duplicate calendar */
  String duplicateCalendar =
      "org.bedework.exception.duplicatecalendar";

  /** Somebody tried to delete a collection with children */
  String collectionNotEmpty =
      "org.bedework.exception.calendarnotempty";

  /** */
  String illegalCalendarCreation =
      "org.bedework.exception.illegalcalendarcreation";

  /** */
  String cannotDeleteCalendarRoot =
      "org.bedework.exception.cannotdeletecalendarroot";

  /** */
  String cannotDeleteDefaultCalendar =
      "org.bedework.exception.cannotdeletedefaultcalendar";

  /* ****************** Subscriptions ****************************** */

  /** Somebody tried to create a duplicate subscription */
  String duplicateSubscription =
      "org.bedework.exception.duplicatesubscription";

  /** Subscription chain has a loop */
  String subscriptionLoopDetected =
      "org.bedework.exception.subscriptionloop";

  /** Subscription failed */
  String subscriptionFailed =
      "org.bedework.exception.subscriptionfailed";

  /* ****************** Ical translation **************************** */

  /** Tried to specify attendees for publish */
  String attendeesInPublish =
      "org.bedework.exception.ical.attendeesinpublish";

  /** Tried to specify end and duration for an event */
  String endAndDuration =
      "org.bedework.exception.ical.endandduration";

  /** Must have a guid */
  String noGuid =
      "org.bedework.exception.ical.noguid";

  /* ****************** Users ****************************** */

  /** No such account */
  String noSuchAccount =
      "org.bedework.exception.nosuchaccount";

  /* ****************** Events ****************************** */

  /** unknown property */
  String unknownProperty =
      "org.bedework.exception.unknown.property";

  /** No calendar for this event */
  String noEventCalendar =
      "org.bedework.exception.noeventcalendar";

  /** No guid for this event */
  String noEventGuid =
      "org.bedework.exception.noeventguid";

  /** Missing required property for this event */
  String missingEventProperty =
      "org.bedework.exception.missingeventproperty";

  /** No name for this event */
  String noEventName =
      "org.bedework.exception.noeventname";

  /** The guid for this event already exists in this collection */
  String duplicateGuid =
      "org.bedework.exception.duplicateguid";

  /** The name for this event already exists in this collection */
  String duplicateName =
      "org.bedework.exception.duplicatename";

  /** Cannot locate instances for ... */
  String cannotLocateInstance =
      "org.bedework.exception.cannotlocateinstance";

  /** There are no instances for this recurring event. */
  String noRecurrenceInstances =
      "org.bedework.exception.norecurrenceinstances";

  /** There is no instances for this override. */
  String invalidOverride =
      "org.bedework.error.invalid.override";

  /** Cannot supply overrides for nonrecurring event. */
  String overridesForNonRecurring =
      "org.bedework.exception.overridesfornonrecurring";

  /* ****************** Scheduling ****************************** */

  /** Access is disallowed to any attendee. */
  String schedulingAttendeeAccessDisallowed =
      "org.bedework.error.scheduling.attendeeaccessdisallowed";

  /** Too many attendees */
  String schedulingTooManyAttendees =
          "org.bedework.error.scheduling.toomany.attendees";

  /** Attendee bad */
  String schedulingBadAttendees =
      "org.bedework.error.scheduling.bad.attendees";

  /** Entity had a  bad method set */
  String schedulingBadMethod =
      "org.bedework.error.scheduling.badmethod";

  /** A bad response method was attempted */
  String schedulingBadResponseMethod =
      "org.bedework.error.scheduling.badresponsemethod";

  /** event is not in inbox */
  String schedulingBadSourceCalendar =
      "org.bedework.error.scheduling.badsourcecalendar";

  /** invalid destination calendar for event */
  String schedulingBadDestinationCalendar =
      "org.bedework.error.scheduling.baddestinationcalendar";

  /** Duplicate uid found in the target calendar  */
  String schedulingDuplicateUid =
      "org.bedework.error.scheduling.duplicateuid";

  /** Expected exactly one attendee for reply. */
  String schedulingExpectOneAttendee =
      "org.bedework.error.scheduling.expectoneattendee";

  /** Attendee partStat is bad. */
  String schedulingInvalidPartStatus =
      "org.bedework.error.scheduling.invalidpartstatus";

  /** Multiple events were found in the target calendar  */
  String schedulingMultipleEvents =
      "org.bedework.error.scheduling.multipleevents";

  /** You are not an attendee of the meeting. */
  String schedulingNotAttendee =
      "org.bedework.error.scheduling.notattendee";

  /** Entity required attendees but had none. */
  String schedulingNoAttendees =
      "org.bedework.error.scheduling.noattendees";

  /** Entity required originator but had none. */
  String schedulingNoOriginator =
      "org.bedework.error.scheduling.noOriginator";

  /** Entity required recipients but had none. */
  String schedulingNoRecipients =
      "org.bedework.error.scheduling.norecipients";

  /** Attendee for reply not in event. */
  String schedulingUnknownAttendee =
      "org.bedework.error.scheduling.unknownattendee";

  /** Unknown event - organizer possibly deleted it?. */
  String schedulingUnknownEvent =
      "org.bedework.error.scheduling.unknownevent";

  /** Invalid scheduling response. */
  String schedulingBadResponse =
      "org.bedework.error.scheduling.badresponse";

  /** Invalid scheduling action. */
  String schedulingBadAction =
      "org.bedework.error.scheduling.badaction";

  /** Invalid recipients for scheduling request. */
  String schedulingBadRecipients =
      "org.bedework.error.scheduling.badrecipients";

  /** System error: Invalid freebusy granulator date limit - Must be DATETIME. */
  String schedulingBadGranulatorDt =
      "org.bedework.error.scheduling.badgranulatordt";

  /** No default scheduling calendar. */
  String schedulingNoCalendar =
      "org.bedework.error.scheduling.nocalendar";

  /* ****************** Timezones ****************************** */

  /** Unknown timezones */
  String unknownTimezone =
      "org.bedework.error.unknown.timezone";

  /** Bad date */
  String badDate =
      "org.bedework.error.bad.date";

  /** No thread local timezones set */
  String noThreadLocalTimezones =
      "org.bedework.error.nothreadlocaltimezones";

  /* ****************** Indexing ****************************** */

  /** */
  String unindexableObjectClass =
      "org.bedework.exception.unindexableobjectclass";

  /** */
  String notIndexPrincipal =
      "org.bedework.error.indexing.notprincipal";

  /* ****************** Notifications ********************* */

  /** */
  String noInvite =
      "org.bedework.notification.noinvite";

  /** */
  String noInviteeInUsers =
      "org.bedework.notification.noinviteeinusers";

  /** */
  String shareTargetNotFound =
      "org.bedework.notification.sharetargetnotfound";

  /* ****************** Misc ****************************** */

  /** */
  String badSystemLocaleList =
      "org.bedework.exception.badsystemlocalelist";

  /** */
  String badLocale =
      "org.bedework.exception.badlocale";

  /** */
  String badRootUsersList =
      "org.bedework.exception.badrootuserslist";

  /** */
  String illegalObjectClass =
      "org.bedework.exception.illegalobjectclass";

  /** */
  String targetExists =
      "org.bedework.exception.targetexists";

  /** */
  String badRequest = "org.bedework.exception.badrequest";

  /** */
  String badResponse = "org.bedework.exception.badresponse";

  /** Used to indicate something you're not allowed to do -
   * not an access exception
   */
  String forbidden = "org.bedework.exception.forbidden";

  /** */
  String staleState =
      "org.bedework.exception.stalestate";
}
