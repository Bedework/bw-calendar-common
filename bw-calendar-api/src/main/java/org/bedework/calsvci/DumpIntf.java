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

import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.BwCategory;
import org.bedework.calfacade.BwContact;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.BwEventAnnotation;
import org.bedework.calfacade.BwFilterDef;
import org.bedework.calfacade.BwLocation;
import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.BwResource;
import org.bedework.calfacade.svc.BwAdminGroup;
import org.bedework.calfacade.svc.BwAuthUser;
import org.bedework.calfacade.svc.BwCalSuite;
import org.bedework.calfacade.svc.BwPreferences;
import org.bedework.calfacade.svc.EventInfo;

import java.util.Collection;
import java.util.Iterator;

/** Interface which defines the database functions needed to dump the
 * calendar data. It is intended that the implementation might cache
 * objects. Calling each method in the order below should be the most
 * efficient.
 *
 * <p>These methods return dummy objects for references. For example,
 * an event location is represented by a Location object with only the
 * id filled in.
 *
 * <p>If the implementing class discovers a discrepency, e.g. a missing
 * user entry, it is up to the caller to determine that is the case.
 *
 * <p>Error messages should be emitted by the implementing classes.
 *
 * <p>Classes to dump in the order they must appear are<ul>
 * <li>BwSystem</li>
 * <li>BwUser</li>
 * <li>BwCalendar</li>
 * <li>BwLocation</li>
 * <li>BwSponsor</li>
 * <li>BwOrganizer</li>
 * <li>BwAttendee</li>
 * <li>BwCategory</li>
 * <li>BwAuthUser</li>
 * <li>BwAuthUserPrefs</li>
 * <li>BwEvent</li>
 * <li>BwEventAnnotation</li>
 * <li>BwAdminGroup</li>
 * <li>BwPreferences + BwView</li>
 * <li>BwCalSuite</li>
 *
 * <li>BwFilter</li>
 * <li>BwUserInfo</li>
 * </ul>
 *
 * @author Mike Douglass   douglm rpi.edu
 * @version 1.0
 */
public interface DumpIntf {
  /** Will return an Iterator returning AdminGroup objects.
   *
   * @return Iterator over entities
   */
  Iterator<BwAdminGroup> getAdminGroups();

  /** Will return an Iterator returning AuthUser objects. Preferences will
   * be attached - user objects will also be attached.
   *
   * @return Iterator over entities
   */
  Iterator<BwAuthUser> getAuthUsers();

  /** Will return an Iterator returning the top level BwCalendar objects.
   *
   * @return Iterator over entities
   */
  Iterator<BwCalendar> getCalendars();

  /**
   * @param val - the collection
   * @return Children of val
   */
  Collection<BwCalendar> getChildren(BwCalendar val);

  /** Will return an Iterator returning BwCalSuite objects.
   *
   * @return Iterator over entities
   */
  Iterator<BwCalSuite> getCalSuites();

  /** Will return an Iterator returning Category objects.
   *
   * @return Iterator over entities
   */
  Iterator<BwCategory> getCategories();

  /** Will return an Iterator returning BwEvent objects.
   * All relevent objects, categories, locations, sponsors, creators will
   * be attached.
   *
   * @return Iterator - events may have overrides attached.
   */
  Iterator<BwEvent> getEvents();

  /** Will return an Iterable object returning EventInfo objects.
   * All relevent objects, categories, locations, sponsors, creators will
   * be attached.
   *
   * @param colPath for events
   * @return Iterable - events may have overrides attached.
   */
  Iterable<EventInfo> getEventInfos(String colPath);

  /** Will return an Iterator returning resource objects.
   *
   * @param colPath for resources
   * @return Iterable - resources with content.
   */
  Iterable<BwResource> getResources(String colPath);

  /** Will return an Iterator over event hrefs.
   *
   * @param start - position.
   * @return Iterator over hrefs.
   */
  Iterator<String> getEventHrefs(int start);

  /** Will return an Iterator returning BwEvent objects.
   * All relevent objects, categories, locations, sponsors, creators will
   * be attached.
   *
   * <p>Overrides are not included
   *
   * @return Iterator over entities
   */
  Iterator<BwEventAnnotation> getEventAnnotations();

  /** Will return an Iterator returning Filter objects.
   *
   * @return Iterator over entities
   */
  Iterator<BwFilterDef> getFilters();

  /** Will return an Iterator returning Location objects.
   *
   * @return Iterator over entities
   */
  Iterator<BwLocation> getLocations();

  /** Will return an Iterator returning BwPreferences objects.
   *
   * @return Iterator over entities
   */
  Iterator<BwPreferences> getPreferences();

  /** Will return an Iterator returning BwContact objects.
   *
   * @return Iterator over entities
   */
  Iterator<BwContact> getContacts();

  /** Will return an Iterator returning principal objects.
   * Subscriptions will be included
   *
   * @return Iterator over entities
   */
  Iterator<BwPrincipal<?>> getAllPrincipals();

  /** Will return an Iterator returning User resources.
   *
   * @return Iterator over entities
   */
  Iterator<BwResource> getResources();

  /** Will return the resource content for the given resource.
   *
   * @param res on return resource content object will be implanted
   */
  void getResourceContent(BwResource res);

  /** Will return an Iterator returning view objects.
   *
   * @return Iterator over entities
   */
  Iterator<?> getViews();

  /** Prepare for dumping of the given principal - used by the 
   * file dump
   * 
   * @param val the principal
   */
  void startPrincipal(BwPrincipal val);

  /** End dumping of the given principal - used by the 
   * file dump
   *
   * @param val the principal
   */
  void endPrincipal(BwPrincipal val);
}

