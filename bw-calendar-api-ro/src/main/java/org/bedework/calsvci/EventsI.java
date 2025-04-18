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
package org.bedework.calsvci;

import org.bedework.caldav.util.filter.FilterBase;
import org.bedework.calfacade.BwCollection;
import org.bedework.calfacade.BwCategory;
import org.bedework.calfacade.BwDateTime;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.RecurringRetrievalMode;
import org.bedework.calfacade.base.CategorisedEntity;
import org.bedework.calfacade.ical.BwIcalPropertyInfo.BwIcalPropertyInfoEntry;
import org.bedework.calfacade.indexing.BwIndexer.DeletedState;
import org.bedework.calfacade.requests.GetInstancesRequest;
import org.bedework.calfacade.responses.InstancesResponse;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.calfacade.svc.EventInfo.UpdateResult;
import org.bedework.calfacade.svc.RealiasResult;
import org.bedework.calfacade.util.ChangeTable;
import org.bedework.base.response.Response;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/** Interface for handling bedework event objects.
 *
 * @author Mike Douglass
 *
 */
public interface EventsI extends Serializable {
  /** Return one or more events for the current user using the calendar, guid
   * and the recurrence id as a key.
   *
   * <p>For non-recurring events, in normal calendar collections, one and only
   * one event should be returned.
   *
   * <p>For non-recurring events, in special calendar collections, more than
   * one event might be returned if the guid uniqueness requirement is relaxed,
   * for example, in the inbox.
   * <br/>
   * For recurring events, the 'master' event defining the rules together
   * with any exceptions should be returned.
   *
   * @param   colPath   String collection path
   * @param   guid      String guid for the event
   * @param   recurrenceId String recurrence id or null
   * @param recurRetrieval How recurring event is returned.
   * @return  Collection of EventInfo objects representing event(s).

   */
  Collection<EventInfo> getByUid(String colPath,
                                 String guid,
                                 String recurrenceId,
                                 RecurringRetrievalMode recurRetrieval);

  /** Get events given the calendar and String name. Return null for not
   * found. There should be only one event or none. For recurring, the
   * overrides and possibly the instances will be attached.
   * <br/>
   * NOTE: this does not provide alias filtering. 
   *
   * @param  colPath   String collection path fully resolved to target
   * @param name       String possible name
   * @return EventInfo or null
   */
  EventInfo get(String colPath,
                       String name);

  /** Get events given the calendar and String name. Return null for not
   * found. There should be only one event or none.
   *
   * <p>For a recurring event and no supplied recurrence id the
   * overrides will be attached.
   *
   * <p>For a recurring event and a supplied recurrence id the
   * expanded instance only will be returned.
   * <br/>
   * NOTE: this does not provide alias filtering. 
   *
   * @param  colPath   String collection path
   * @param name       String possible name
   * @param recurrenceId non-null for single instance
   * @return EventInfo or null
   */
  EventInfo get(String colPath,
                       String name,
                       String recurrenceId);

  /** Get events given the calendar and String name. Return null for not
   * found. There should be only one event or none. For recurring, the
   * overrides and possibly the instances will be attached.
   * <br/>
   * This does provide alias filtering. 
   *
   * @param col   Collection - possibly a filtered alias
   * @param name  String name
   * @param recurrenceId non-null for single instance
   * @param retrieveList List of properties to retrieve or null for a full event.
   * @return EventInfo or null
   */
  EventInfo get(BwCollection col,
                String name,
                String recurrenceId,
                List<String> retrieveList);

  /** Return the events for the current user within the given date and time
   * range. If retrieveList is supplied only those fields (and a few required
   * fields) will be returned.
   *
   * @param cal          BwCollection object - non-null means limit to given calendar
   *                     null is limit to current user
   * @param filter       BwFilter object restricting search or null.
   * @param startDate    BwDateTime start - may be null
   * @param endDate      BwDateTime end - may be null.
   * @param retrieveList List of properties to retrieve or null for a full event.
   * @param recurRetrieval How recurring event is returned.
   * @return Collection  populated event value objects
   */
  Collection<EventInfo> getEvents(BwCollection cal,
                                  FilterBase filter,
                                  BwDateTime startDate,
                                  BwDateTime endDate,
                                  List<BwIcalPropertyInfoEntry> retrieveList,
                                  DeletedState delState,
                                  RecurringRetrievalMode recurRetrieval);

  /** Delete an event.
   *
   * @param ei                 BwEvent object to be deleted
   * @param sendSchedulingMessage   Send a declined or cancel scheduling message
   * @return Response with status ok if event deleted
   */
  Response<?> delete(EventInfo ei,
                     boolean sendSchedulingMessage);

  /** Method which allows us to flag it as a scheduling action
   *
   * @param ei event info
   * @param scheduling true for scheduling
   * @param sendSchedulingReply true if we need a reply
   * @return Response with status
   */
  Response<?> delete(EventInfo ei,
                     boolean scheduling,
                     boolean sendSchedulingReply);

  /** Add an event and ensure its location and contact exist. The calendar path
   * must be set in the event.
   *
   * <p>For public events some calendar implementors choose to allow the
   * dynamic creation of locations and contacts. For each of those, if we have
   * an id, then the object represents a preexisting database item.
   *
   * <p>Otherwise the client has provided information which will be used to
   * locate an already existing location or contact. Failing that we use the
   * information to create a new entry.
   *
   * <p>For user clients, we generally assume no contact and the location is
   * optional. However, both conditions are enforced at the application level.
   *
   * <p>On return the event object will have been updated. In addition the
   * location and contact may have been updated.
   *
   * <p>If this is a scheduling event and noInvites is set to false then
   * invitations wil be sent out to the attendees.
   *
   * <p>The event to be added may be a reference to another event. In this case
   * a number of fields should have been copied from that event. Other fields
   * will come from the target.
   *
   * @param ei           EventInfo object to be added
   * @param noInvites    True for don't send invitations.
   * @param scheduling   True if this is to be added to an inbox - affects required
   *                     access.
   * @param autoCreateCollection - true if we should add a missing collection
   * @param rollbackOnError true to roll back if we get an error
   * @return UpdateResult Status and counts of changes.
   */
  UpdateResult add(EventInfo ei,
                          boolean noInvites,
                          boolean scheduling,
                          boolean autoCreateCollection,
                          boolean rollbackOnError);

  /** Reindex an event.
   *
   * @param ei           EventInfo object to be reindexed
   */
  void reindex(EventInfo ei);

  /** Update an event. Any changeset should be embedded in the event
   * info object. This method should only be used for an update by a
   * client. Implicit scheduling etc should use the other methods.
   *
   * @param ei           EventInfo object to be added
   * @param noInvites    True for don't send invitations.
   * @return UpdateResult Counts of changes.
   */
  UpdateResult update(EventInfo ei,
                      boolean noInvites);

  /** Update an event in response to an attendee. Exactly as normal update if
   * fromAtt is null. Otherwise no status update is sent to the given attendee
   *
   * <p>  Any changeset should be embedded in the event info object.
   *
   * @param ei           EventInfo object to be added
   * @param noInvites    True for don't send invitations.
   * @param fromAttUri   attendee responding
   * @return UpdateResult Counts of changes.
   */
  UpdateResult update(EventInfo ei,
                      boolean noInvites,
                      String fromAttUri,
                      boolean autoCreateCollection);

  /** Update an event in response to an attendee. Exactly as normal update if
   * fromAtt is null. Otherwise no status update is sent to the given attendee
   *
   * <p>  Any changeset should be embedded in the event info object.
   *
   * @param ei           EventInfo object to be added
   * @param noInvites    True for don't send invitations.
   * @param fromAttUri   attendee responding
   * @param alwaysWrite  write and reindex whatever changetable says
   * @param clientUpdate true if this is a client updating the event.
   * @return UpdateResult Counts of changes.
   */
  UpdateResult update(EventInfo ei,
                      boolean noInvites,
                      String fromAttUri,
                      boolean alwaysWrite,
                      boolean clientUpdate,
                      boolean autoCreateCollection);

  /** For an event to which we have write access we simply mark it deleted.
   *
   * <p>Otherwise we add an annotation maarking the event as deleted.
   *
   * @param event the event
   */
  void markDeleted(BwEvent event);

  /** Copy or move the given named entity to the destination calendar and give it
   * the supplied name.
   *
   * @param from      Source named entity
   * @param to        Destination calendar
   * @param name      String name of new entity
   * @param copy      true for copying
   * @param overwrite if destination exists replace it.
   * @param newGuidOK   set a new guid if needed (e.g. copy in same collection)
   * @return Response with status
   */
  Response<?> copyMoveNamed(EventInfo from,
                            BwCollection to,
                            String name,
                            boolean copy,
                            boolean overwrite,
                            boolean newGuidOK);

  /** Claim ownership of this event
   *
   * @param ev  event
   */
  void claim(BwEvent ev);

  /** Realias the event - set categories according to the set of aliases.
   * 
   * <p>This is a bedework function in which we specify which set of aliases
   * we used to add the event. Aliases are used to filter the data and provide a
   * view for users, e.g category="Films"
   *
   * <p>We need these aliases to provide a way of informing the user what they can
   * subscribe to in order to see the events of interest.
   *
   * <p>We also use them to set and unset categories, allowing event submitters to
   * consider only topic areas and leave it up to system administrators to
   * define which categories get set
   *
   * <p>Each alias is a virtual path. For example "/user/adgrp_Eng/Lectures/Lectures"
   * might be a real path with two components<br/>
   * "/user/adgrp_Eng/Lectures" and<br/>
   * "Lectures"
   *
   * <p>"/user/adgrp_Eng/Lectures" is aliased to "/public/aliases/Lectures" which
   * is a folder containing the alias "/public/aliases/Lectures/Lectures" which
   * is aliased to the single calendar.
   *
   * @param ev  event
   * @return Response containing set of categories referenced by the aliases. Status unknown for unknown alias(es)

   */
  RealiasResult reAlias(BwEvent ev);

  /** Return the instances for a given combination of start date, rrule,
   * exdates and rdates. A date/time window may be supplied to limit the
   * result.
   * <br/>
   * Note: this is currently unused but I believe the intent was to
   * allow a web client to request a range of instances from the server
   * to avoid the necessity of calculating them locally.
   *
   * @param req parameters for the method
   * @return instances or error response
   */
  InstancesResponse getInstances(GetInstancesRequest req);

  class SetEntityCategoriesResult {
    /** rc */
    public static final int success = 0;
    
    public int rcode = -1;

    /** Number of BwCategory created */
    public int numCreated;

    /** Number of BwCategory added */
    public int numAdded;

    /** Number of BwCategory removed */
    public int numRemoved;
  }

  /** Set the entity categories based on multivalued request parameter "categoryKey".
   *
   * <p>We build a list of categories then update the membership of the entity
   * category collection to correspond.
   *
   * @param ent categorised entity to be adjusted
   * @param extraCats Categories to add as a result of other operations
   * @param defCatUids uids of default categories for current user
   * @param allDefCatUids uids of all public default categories
   * @param strCatUids Categories to add from request
   * @param changes a change table
   * @return setEventCategoriesResult
   */
  SetEntityCategoriesResult setEntityCategories(CategorisedEntity ent,
                                                Set<BwCategory> extraCats,
                                                Set<String> defCatUids,
                                                Set<String> allDefCatUids,
                                                Collection<String> strCatUids,
                                                ChangeTable changes);
}
