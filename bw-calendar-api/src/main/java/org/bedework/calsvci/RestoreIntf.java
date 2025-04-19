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
import org.bedework.calfacade.BwCollection;
import org.bedework.calfacade.BwCategory;
import org.bedework.calfacade.BwContact;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwFilterDef;
import org.bedework.calfacade.BwLocation;
import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.BwResource;
import org.bedework.calfacade.BwSystem;
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
   */
  void endTransactionNow();

  /** Call to end a transaction - may be ignored if batching
   *
   */
  void endTransaction();

  /** Check for an empty system first
   *
   */
  void checkEmptySystem();

  /** Restore system pars
   *
   * @param o pars object
   */
  void restoreSyspars(BwSystem o);

  /** Restore principal
   *
   * @param o principal object
   */
  void restorePrincipal(BwPrincipal<?> o);

  /** Restore an admin group - though not the user entries nor
   * the authuser entries.
   *
   * @param o   Object to restore
   */
  void restoreAdminGroup(BwAdminGroup o);

  /**
   * @param o admin group
   * @param pr principal
   */
  void addAdminGroupMember(BwAdminGroup o,
                           BwPrincipal<?> pr);

  /** Get an admin group given it's name.
   *
   * @param name     String name of the group
   * @return BwAdminGroup
   */
  BwAdminGroup getAdminGroup(String name);

  /** Restore an auth user and preferences
   *
   * @param o   Object to restore with id set
   */
  void restoreAuthUser(BwAuthUser o);

  /** Restore an event and associated entries
   *
   * @param ei   Object to restore with id set
   */
  void restoreEvent(EventInfo ei);

  /** Get an event
   *
   * @param owner - the current user we are acting as - eg, for an annotation and
   *           fetching the master event this will be the annotation owner.
   * @param colPath collection path
   * @param recurrenceId id
   * @param uid uid of event
   * @return BwEvent
   */
  BwEvent getEvent(BwPrincipal<?> owner,
                   String colPath,
                   String recurrenceId,
                   String uid);

  /** Restore category
   *
   * @param o   Object to restore with id set
   */
  void restoreCategory(BwCategory o);

  /** Restore calendar suite
   *
   * @param o   Object to restore
   */
  void restoreCalSuite(BwCalSuite o);

  /** Restore location
   *
   * @param o   Object to restore with id set
   */
  void restoreLocation(BwLocation o);

  /** Restore contact
   *
   * @param o   Object to restore with id set
   */
  void restoreContact(BwContact o);

  /** Restore filter
   *
   * @param o   Object to restore with id set
   */
  void restoreFilter(BwFilterDef o);

  /** Restore resource
   *
   * @param o   Object to restore with id set
   */
  void restoreResource(BwResource o);

  /** Restore user prefs
   *
   * @param o   Object to restore with id set
   */
  void restoreUserPrefs(BwPreferences o);

  /* * Restore alarm - not needed - restored as part of event
   *
   * @param o   Object to restore with id set
   * @throws Throwable on fatal error
   * /
  void restoreAlarm(BwAlarm o);
  */

  /**
   * @param path of collection
   * @return BwCollection
   */
  BwCollection getCalendar(String path);

  /**
   * @param uid of entity
   * @return BwCategory
   */
  BwCategory getCategory(String uid);

  /**
   * @param uid of entity
   * @return BwContact
   */
  BwContact getContact(String uid);

  /**
   * @param uid of entity
   * @return BwLocation
   */
  BwLocation getLocation(String uid);

  /**
   * @param href of principal
   * @return BwPrincipal
   */
  BwPrincipal getPrincipal(String href);

  /** Save a single root calendar - no parent is set in the entity
   *
   * @param val root collection
   */
  void saveRootCalendar(BwCollection val);

  /** Restore a single calendar - parent is set in the entity
   *
   * @param val collection to add
   */
  void addCalendar(BwCollection val);

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
   */
  FixAliasResult fixSharee(BwCollection col,
                           String shareeHref,
                           AccessType a);
}

