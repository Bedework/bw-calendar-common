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

import org.bedework.calfacade.AliasesInfo;
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwCategory;
import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.CollectionAliases;
import org.bedework.synch.wsmessages.SubscriptionStatusResponseType;
import org.bedework.base.response.GetEntityResponse;
import org.bedework.base.response.Response;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

/** This is the calendars interface for the  bedework system.
 * Calendars is something of a misnomer - we're really talking collections here.
 * The type property defines which type of collection we are dealing with.
 * The CalDAV spec defines what is allowable, e.g. no collections
 * inside a calendar collection.
 *
 * <p>To allow us to enforce access checks we wrap the object inside a wrapper
 * class which blocks access to the getChildren method. To retrieve the children
 * of a calendar object call the getCalendars(BwCalendar) method. The resulting
 * collection is a set of access checked, wrapped objects. Only accessible
 * children will be returned.
 *
 * @author Mike Douglass
 */
public interface CalendarsI extends Serializable {
  /** Returns the href of the root of the public calendars tree.
   *
   * @return String   root path
   */
  String getPublicCalendarsRootPath();

  /** Returns the root of the tree of public calendars. This is NOT a
   * live hibernate object.
   *
   * @return BwCalendar   root
   */
  BwCalendar getPublicCalendars();

  /**
   * @return the public calendar flagged as the primary collection
   */
  BwCalendar getPrimaryPublicPath();

  /** Returns root path of calendars owned by the current user. For
   * unauthenticated this will be the public calendar root.
   *
   * @return String principal home.
   */
  String getHomePath();

  /** Returns root of calendars owned by the current user.
   *
   * <p>For authenticated, personal access this always returns the user
   * entry in the /user calendar tree, e.g. for user smithj it would return
   * an entry /user/smithj
   *
   * @return BwCalendar   user home.
   */
  BwCalendar getHome();

  /** Returns root of calendars owned by the given principal.
   *
   * <p>For authenticated, personal access this always returns the user
   * entry in the /user calendar tree, e.g. for user smithj it would return
   * an entry smithj with path /user/smithj
   *
   * Note: the returned object is NOT a live hibernate object.
   *
   * @param  principal whose home we want
   * @param freeBusy      true if this is for freebusy access
   * @return BwCalendar   user home.
   */
  BwCalendar getHome(BwPrincipal principal,
                     boolean freeBusy);

  /** Returns root of calendars owned by the given principal.
   *
   * <p>For authenticated, personal access this always returns the user
   * entry in the /user calendar tree, e.g. for user smithj it would return
   * an entry smithj with path /user/smithj
   *
   * Note: the returned object is a live hibernate object.
   *
   * @param  principal whose home we want
   * @param freeBusy      true if this is for freebusy access
   * @return BwCalendar   user home.
   */
  BwCalendar getHomeDb(BwPrincipal principal,
                       boolean freeBusy);

  /** A virtual path might be for example "/user/adgrp_Eng/Lectures/Lectures"
   * which has two two components<ul>
   * <li>"/user/adgrp_Eng/Lectures" and</li>
   * <li>"Lectures"</li></ul>
   *
   * <p>
   * "/user/adgrp_Eng/Lectures" is a real path which is an alias to
   * "/public/aliases/Lectures" which is a folder containing the alias
   * "/public/aliases/Lectures/Lectures" which is aliased to the single calendar.
   *
   * @param vpath A virtual path
   * @return collection of collection objects - null for bad vpath
   */
  Collection<BwCalendar> decomposeVirtualPath(String vpath);

  /** Returns children of the given calendar to which the current user has
   * some access.
   *
   * @param  cal          parent calendar
   * @return Collection   of BwCalendar (empty if no children)
   */
  Collection<BwCalendar> getChildren(BwCalendar cal);

  /** Returns children of the given collection to which the current user has
   * some access.
   * 
   * <p>The returned objects are from the indexer and are not live 
   * hibernate objects</p>
   *
   * @param  col          parent collection
   * @return Collection   of BwCalendar
   */
  Collection<BwCalendar> getChildrenIdx(BwCalendar col);

  /** Return a list of calendars in which calendar objects can be
   * placed by the current user.
   *
   * <p>Caldav currently does not allow collections inside collections so that
   * calendar collections are the leaf nodes only.
   *
   * @param includeAliases - true to include aliases - for public admin we don't
   *                    want aliases
   * @return Set   of BwCalendar
   */
  Set<BwCalendar> getAddContentCollections(boolean includeAliases,
                                           boolean isApprover);

  /** Check to see if a collection is empty. A collection is not empty if it
   * contains other collections or calendar entities.
   *
   * @param val      BwCalendar object to check
   * @return boolean true if the calendar is empty
   */
  boolean isEmpty(BwCalendar val);

  /** Get a calendar given the path. If the path is that of a 'special'
   * calendar, for example the deleted calendar, it may not exist if it has
   * not been used.
   *
   * @param  path          String path of calendar
   * @return BwCalendar null for unknown calendar
   */
  BwCalendar get(String path);

  /** Get a calendar given the path. If the path is that of a 'special'
   * calendar, for example the deleted calendar, it may not exist if it has
   * not been used. Returns the non-persisted version from the index
   *
   * @param  path          String path of calendar
   * @return BwCalendar null for unknown calendar
   */
  BwCalendar getIdx(String path);

  /** Get a special calendar (e.g. Notifications) for the current user. If it does not
   * exist and is supported by the target system it will be created.
   *
   * @param  calType   int special calendar type.
   * @param  create    true if we should create it if non-existent.
   * @return BwCalendar null for unknown calendar
   */
  BwCalendar getSpecial(int calType,
                        boolean create);

  /** Get a special calendar (e.g. Notifications) for the given user. If it does not
   * exist and is supported by the target system it will be created.
   *
   * @param  principal the principal href.
   * @param  calType   int special calendar type.
   * @param  create    true if we should create it if non-existent.
   * @return BwCalendar null for unknown calendar
   */
  BwCalendar getSpecial(String principal,
                        int calType,
                        boolean create);

  /** set the default calendar for the current user.
   *
   * @param  val    BwCalendar
   */
  void setPreferred(BwCalendar  val);

  /** Get the default calendar for the current user for the given entity type.
   *
   * @param entityType to search for
   * @return path or null for unknown calendar
   */
  String getPreferred(String entityType);

  /** Add a calendar object
   *
   * <p>The new calendar object will be added to the db. If the indicated parent
   * is null it will be added as a root level calendar.
   *
   * <p>Certain restrictions apply, mostly because of interoperability issues.
   * A calendar cannot be added to another calendar which already contains
   * entities, e.g. events etc.
   *
   * <p>Names cannot contain certain characters - (complete this)
   *
   * <p>Name must be unique at this level, i.e. all paths must be unique
   *
   * @param  val     BwCalendar new object
   * @param  parentPath  String path to parent.
   * @return BwCalendar object as added. Parameter val MUST be discarded
   */
  BwCalendar add(BwCalendar val,
                 String parentPath);

  /** Change the name (path segment) of a calendar object.
   *
   * @param  val         BwCalendar object
   * @param  newName     String name
   */
  void rename(BwCalendar val,
              String newName);

  /** Move a calendar object from one parent to another
   *
   * @param  val         BwCalendar object
   * @param  newParent   BwCalendar potential parent
   */
  void move(BwCalendar val,
            BwCalendar newParent);

  /** Update a calendar object
   *
   * @param  val     BwCalendar object
   */
  void update(BwCalendar val);

  /** Delete a calendar. Also remove it from the current user preferences (if any).
   *
   * @param val      BwCalendar calendar
   * @param emptyIt  true to delete contents
   * @param sendSchedulingMessage  true if we should send cancels
   * @return boolean  true if it was deleted.
   *                  false if it didn't exist
   */
  boolean delete(BwCalendar val,
                 boolean emptyIt,
                 boolean sendSchedulingMessage);

  /** Return true if cal != null and it represents a (local) user root
   *
   * @param cal the collection
   * @return boolean
   */
  boolean isUserRoot(BwCalendar cal);

  /** Attempt to get calendar referenced by the alias. For an internal alias
   * the result will also be set in the aliasTarget property of the parameter.
   *
   * @param val the alias
   * @param resolveSubAlias - if true and the alias points to an alias, resolve
   *                  down to a non-alias.
   * @param freeBusy to determine trequired access
   * @return a BwCalendar object
   */
  BwCalendar resolveAlias(BwCalendar val,
                          boolean resolveSubAlias,
                          boolean freeBusy);

  /** Attempt to get calendar referenced by the alias. For an internal alias
   * the result will also be set in the aliasTarget property of the parameter.
   * 
   * <p>This uses the index only. The returned object will not be a 
   * live hibernate object</p>
   *
   * @param val the alias
   * @param resolveSubAlias - if true and the alias points to an alias, resolve
   *                  down to a non-alias.
   * @param freeBusy to determine trequired access
   * @return a BwCalendar object
   */
  BwCalendar resolveAliasIdx(BwCalendar val,
                             boolean resolveSubAlias,
                             boolean freeBusy);

  /**
   * @param val a collection to check
   * @return response with status and info.
   */
  GetEntityResponse<CollectionAliases> getAliasInfo(BwCalendar val);

  /** */
  enum CheckSubscriptionResult {
    /** No action was required */
    ok,

    /** No such collection */
    notFound,

    /** Not external subscription */
    notExternal,

    /** no subscription id */
    notsubscribed,

    /** resubscribed */
    resubscribed,

    /** Synch service is unavailable */
    noSynchService,

    /** failed */
    failed
  }

  class SynchStatusResponse {
    public CheckSubscriptionResult requestStatus;

    public SubscriptionStatusResponseType subscriptionStatus;

    public CheckSubscriptionResult getRequestStatus() {
      return requestStatus;
    }

    public SubscriptionStatusResponseType getSubscriptionStatus() {
      return subscriptionStatus;
    }
  }

  /** Called to get information about aliases to a collection 
   * containing the named entity. 
   *
   * @param collectionHref the collection
   * @param entityName the entity
   * @return the information
   */
  AliasesInfo getAliasesInfo(String collectionHref,
                             String entityName);

  /**
   *
   * @param path to collection
   * @return never null - requestStatus set for not an external subscription.
   */
  SynchStatusResponse getSynchStatus(String path);

  /** Check the subscription if this is an external subscription. Will contact
   * the synch server and check the validity. If there is no subscription
   * onthe synch server will attempt to resubscribe.
   *
   * @param path to collection
   * @return result of call
   */
  CheckSubscriptionResult checkSubscription(String path);

  /** Refresh the subscription if this is an external subscription. Will contact
   * the synch server.
   *
   * @param val the collection
   * @return result of call
   */
  Response refreshSubscription(BwCalendar val);

  /** Return the value to be used as the sync-token property for the given path.
   * This is effectively the max sync-token of the collection and any child
   * collections.
   *
   * @param path to collection
   * @return a sync-token
   */
  String getSyncToken(String path);

  /** Return true if the value represents a valid sync-token for
   * the given path.
   *
   * For bedework the token is effectively the max sync-token of
   * the collection and any child.
   *
   * We consider it invalid if it's not a valid date or too old
   * or an exception occurs.
   *
   * @param token to test
   * @param path to collection
   * @return true for a valid sync-token
   */
  boolean getSyncTokenIsValid(String token,
                              String path);

  Set<BwCategory> getCategorySet(String href);

  BwCalendar getSpecial(BwPrincipal owner,
                        int calType,
                        boolean create,
                        int access);
}
