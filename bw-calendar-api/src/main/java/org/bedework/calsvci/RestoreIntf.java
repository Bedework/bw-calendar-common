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

import org.bedework.caldav.util.sharing.AccessType;
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwCategory;
import org.bedework.calfacade.BwContact;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwFilterDef;
import org.bedework.calfacade.BwLocation;
import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.BwResource;
import org.bedework.calfacade.BwSystem;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.svc.BwAdminGroup;
import org.bedework.calfacade.svc.BwAuthUser;
import org.bedework.calfacade.svc.BwCalSuite;
import org.bedework.calfacade.svc.BwPreferences;
import org.bedework.calfacade.svc.EventInfo;
import org.bedework.util.logging.Logged;

/** Interface which defines the database functions needed to restore the
 * calendar database. The methods need to be called in the order defined
 * below.
 *
 * @author Mike Douglass   douglm rpi.edu
 * @version 1.0
 */
public interface RestoreIntf {
  /**
   * @param val Logged object
   */
  void setLogger(Logged val);

  /** Allow transactions to span many updates. May not work well.
   *
   * @param val batch size
   */
  void setBatchSize(int val);

  /** Call to end a transaction - even if batched
   *
   * @throws Throwable on fatal error
   */
  void endTransactionNow() throws Throwable;

  /** Call to end a transaction - may be ignored if batching
   *
   * @throws Throwable on fatal error
   */
  void endTransaction() throws Throwable;

  /** Check for an empty system first
   *
   * @throws Throwable on fatal error
   */
  void checkEmptySystem() throws Throwable;

  /** Restore system pars
   *
   * @param o pars object
   * @throws Throwable on fatal error
   */
  void restoreSyspars(BwSystem o) throws Throwable;

  /** Restore principal
   *
   * @param o principal object
   * @throws Throwable on fatal error
   */
  void restorePrincipal(BwPrincipal<?> o) throws Throwable;

  /** Restore an admin group - though not the user entries nor
   * the authuser entries.
   *
   * @param o   Object to restore
   * @throws Throwable on fatal error
   */
  void restoreAdminGroup(BwAdminGroup o) throws Throwable;

  /**
   * @param o admin group
   * @param pr principal
   * @throws Throwable on fatal error
   */
  void addAdminGroupMember(BwAdminGroup o,
                           BwPrincipal<?> pr) throws Throwable;

  /** Get an admin group given it's name.
   *
   * @param name     String name of the group
   * @return BwAdminGroup
   * @throws Throwable on fatal error
   */
  BwAdminGroup getAdminGroup(String name) throws Throwable;

  /** Restore an auth user and preferences
   *
   * @param o   Object to restore with id set
   * @throws Throwable on fatal error
   */
  void restoreAuthUser(BwAuthUser o) throws Throwable;

  /** Restore an event and associated entries
   *
   * @param ei   Object to restore with id set
   * @throws Throwable on fatal error
   */
  void restoreEvent(EventInfo ei) throws Throwable;

  /** Get an event
   *
   * @param owner - the current user we are acting as - eg, for an annotation and
   *           fetching the master event this will be the annotation owner.
   * @param colPath collection path
   * @param recurrenceId id
   * @param uid uid of event
   * @return BwEvent
   * @throws Throwable on fatal error
   */
  BwEvent getEvent(BwPrincipal<?> owner,
                   String colPath,
                   String recurrenceId,
                   String uid) throws Throwable;

  /** Restore category
   *
   * @param o   Object to restore with id set
   * @throws Throwable on fatal error
   */
  void restoreCategory(BwCategory o) throws Throwable;

  /** Restore calendar suite
   *
   * @param o   Object to restore
   * @throws Throwable on fatal error
   */
  void restoreCalSuite(BwCalSuite o) throws Throwable;

  /** Restore location
   *
   * @param o   Object to restore with id set
   * @throws Throwable on fatal error
   */
  void restoreLocation(BwLocation o) throws Throwable;

  /** Restore contact
   *
   * @param o   Object to restore with id set
   * @throws Throwable on fatal error
   */
  void restoreContact(BwContact o) throws Throwable;

  /** Restore filter
   *
   * @param o   Object to restore with id set
   * @throws Throwable on fatal error
   */
  void restoreFilter(BwFilterDef o) throws Throwable;

  /** Restore resource
   *
   * @param o   Object to restore with id set
   * @throws Throwable on fatal error
   */
  void restoreResource(BwResource o) throws Throwable;

  /** Restore user prefs
   *
   * @param o   Object to restore with id set
   * @throws Throwable on fatal error
   */
  void restoreUserPrefs(BwPreferences o) throws Throwable;

  /* * Restore alarm - not needed - restored as part of event
   *
   * @param o   Object to restore with id set
   * @throws Throwable on fatal error
   * /
  void restoreAlarm(BwAlarm o) throws Throwable;
  */

  /**
   * @param path of collection
   * @return BwCalendar
   * @throws Throwable on fatal error
   */
  BwCalendar getCalendar(String path) throws Throwable;

  /**
   * @param uid of entity
   * @return BwCategory
   * @throws Throwable on fatal error
   */
  BwCategory getCategory(String uid) throws Throwable;

  /**
   * @param uid of entity
   * @return BwContact
   * @throws Throwable on fatal error
   */
  BwContact getContact(String uid) throws Throwable;

  /**
   * @param uid of entity
   * @return BwLocation
   * @throws Throwable on fatal error
   */
  BwLocation getLocation(String uid) throws Throwable;

  /**
   * @param href of principal
   * @return BwPrincipal
   * @throws CalFacadeException on fatal error
   */
  BwPrincipal getPrincipal(String href) throws CalFacadeException;

  /** Save a single root calendar - no parent is set in the entity
   *
   * @param val root collection
   * @throws Throwable on fatal error
   */
  void saveRootCalendar(BwCalendar val) throws Throwable;

  /** Restore a single calendar - parent is set in the entity
   *
   * @param val collection to add
   * @throws Throwable on fatal error
   */
  void addCalendar(BwCalendar val) throws Throwable;

  /** */
  enum FixAliasResult {
    /** No action was required */
    ok,

    /** No access to target collection */
    noAccess,

    /** Wrong access to target collection */
    wrongAccess,

    /** No such target collection */
    notFound,

    /** Part of or points to a circular chain */
    circular,

    /** Broken chain */
    broken,

    /** reshared */
    reshared,

    /** failed */
    failed
  }

  /** Restore sharing for the given principal href
   *
   * @param col - the target collection
   * @param shareeHref - the sharee
   * @param a          - access
   * @return indication of how it went
   * @throws CalFacadeException on fatal error
   */
  FixAliasResult fixSharee(BwCalendar col,
                           String shareeHref,
                           AccessType a) throws CalFacadeException;
}

