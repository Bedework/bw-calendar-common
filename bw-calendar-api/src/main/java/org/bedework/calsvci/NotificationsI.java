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

import org.bedework.caldav.util.notifications.NotificationType;
import org.bedework.calfacade.BwPrincipal;

import java.io.Serializable;
import java.util.List;

import javax.xml.namespace.QName;

/** Interface for handling bedework notifications - including CalDAV user
 * notification collections.
 *
 * @author Mike Douglass
 *
 */
public interface NotificationsI extends Serializable {
  /** Add the given notification to the notification collection for the
   * indicated principal.
   *
   * @param pr - target
   * @param val - the notification
   * @return false for unknown CU
   */
  boolean send(BwPrincipal pr,
               NotificationType val);

  /** Add the given notification to the notification collection for the
   * current principal. Caller should check for notifications enabled if
   * appropriate.
   *
   * @param val - the notification
   * @return false for no notification or collection
   */
  boolean add(NotificationType val);

  /** Update the given notification
   *
   * @param val - the notification
   * @return false for no notification or collection
   */
  boolean update(NotificationType val);

  /** Find a notification in the notification collection for the
   * current principal with the given name.
   *
   * @param name - of the notification
   * @return null for no notification or the notification with that name
   */
  NotificationType find(String name);

  /** Find a notification in the notification collection for the
   * given principal with the given name.
   *
   * @param principalHref - target
   * @param name - of the notification
   * @return null for no notification or the notification with that name
   */
  NotificationType find(String principalHref,
                        String name);

  /** Remove a notification in the notification collection for the
   * given principal with the given name.
   *
   * @param principalHref - target
   * @param name - of the notification
   */
  void remove(String principalHref,
              String name);

  /** Remove the given notification from the notification collection for the
   * indicated calendar user. Must have access to the collection.
   *
   * @param principalHref - target
   * @param val - the notification
   */
  void remove(String principalHref,
              NotificationType val);

  /** Remove the given notification from the notification collection for the
   * current calendar user.
   *
   * @param val - the notification
   */
  void remove(NotificationType val);

  /** Remove all the notification from the notification collection for the
   * given calendar user.
   *
   * @param principalHref - the principal
   */
  void removeAll(String principalHref);

  /**
   * @return all notifications for this user
   */
  List<NotificationType> getAll();

  /**
   * @param type of notification (null for all)
   * @return matching notifications for this user - never null
   */
  List<NotificationType> getMatching(QName type);

  /**
   * @param pr principal
   * @param type of notification (null for all)
   * @return notifications for the given principal of the given type
   */
  List<NotificationType> getMatching(BwPrincipal pr,
                                     QName type);

  /**
   * @param href principal href
   * @param type of notification (null for all)
   * @return notifications for the given principal of the given type
   */
  List<NotificationType> getMatching(String href,
                                     QName type);

  /** Subscribe to a notification service.
   *
   */
  void subscribe(String principalHref,
                 List<String> emails);

  /** Subscribe to a notification service.
   *
   */
  void unsubscribe(String principalHref,
                   List<String> emails);
}
