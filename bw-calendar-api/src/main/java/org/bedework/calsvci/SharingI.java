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

import org.bedework.caldav.util.sharing.InviteReplyType;
import org.bedework.caldav.util.sharing.InviteType;
import org.bedework.caldav.util.sharing.ShareResultType;
import org.bedework.caldav.util.sharing.ShareType;
import org.bedework.calfacade.BwCalendar;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.svc.SharingReplyResult;
import org.bedework.calfacade.svc.SubscribeResult;

import java.io.Serializable;

/** Interface for handling bedework sharing - looks like Apple sharing.
 *
 * @author Mike Douglass
 *
 */
public interface SharingI extends Serializable {
  /**
   * @param principalHref share as this user.
   * @param col MUST be a sharable collection
   * @param share the request
   * @return list of ok and !ok sharees
   * @throws CalFacadeException on fatal error
   */
  ShareResultType share(String principalHref,
                        BwCalendar col,
                        ShareType share) throws CalFacadeException;

  /**
   * @param col MUST be a sharable collection
   * @param share the request
   * @return list of ok and !ok sharees
   * @throws CalFacadeException on fatal error
   */
  ShareResultType share(BwCalendar col,
                        ShareType share) throws CalFacadeException;

  /**
   * @param col MUST be current sharees home
   * @param reply the request
   * @return a ReplyResult object.
   * @throws CalFacadeException on fatal error
   */
  SharingReplyResult reply(BwCalendar col,
                           InviteReplyType reply) throws CalFacadeException;

  /**
   * @param col to check
   * @return current invitations
   * @throws CalFacadeException on fatal error
   */
  InviteType getInviteStatus(BwCalendar col) throws CalFacadeException;

  /** Do any cleanup necessary for a collection delete.
   *
   * @param col to delete
   * @param sendNotifications true to notify sharees
   * @throws CalFacadeException on fatal error
   */
  void delete(BwCalendar col,
              boolean sendNotifications) throws CalFacadeException;

  /** Publish the collection - that is make it available for subscriptions.
   *
   * @param col to publish
   * @throws CalFacadeException on fatal error
   */
  void publish(BwCalendar col) throws CalFacadeException;

  /** Unpublish the collection - that is make it unavailable for subscriptions
   * and remove any existing subscriptions.
   *
   * @param col to unpublish
   * @throws CalFacadeException on fatal error
   */
  void unpublish(BwCalendar col) throws CalFacadeException;

  /** Subscribe to the collection - must be a published collection.
   *
   * @param colPath of collection
   * @param subscribedName name for new alias
   * @return path of new alias and flag
   * @throws CalFacadeException on fatal error
   */
  SubscribeResult subscribe(String colPath,
                            String subscribedName) throws CalFacadeException;

  /** Subscribe to an external url.
   *
   * @param extUrl external url
   * @param subscribedName name for new alias
   * @param refresh - refresh rate in minutes <= 0 for default
   * @param remoteId - may be null
   * @param remotePw  - may be null
   * @return path of new alias and flag
   * @throws CalFacadeException on fatal error
   */
  SubscribeResult subscribeExternal(String extUrl,
                                    String subscribedName,
                                    int refresh,
                                    String remoteId,
                                    String remotePw) throws CalFacadeException;

  /** Unsubscribe the collection - that is col MUST be an alias to
   * another collection. Update any existing invite status for the
   * current principal.
   *
   * @param col alias to unsubscribe
   * @throws CalFacadeException on fatal error
   */
  void unsubscribe(BwCalendar col) throws CalFacadeException;
}
