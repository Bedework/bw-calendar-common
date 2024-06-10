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
import org.bedework.calfacade.synch.BwSynchInfo;
import org.bedework.calsvci.CalendarsI.CheckSubscriptionResult;
import org.bedework.calsvci.CalendarsI.SynchStatusResponse;
import org.bedework.util.misc.response.Response;

import java.io.Serializable;

/** Interface for handling interactions with the synch engine from within
 * bedework.
 *
 * @author Mike Douglass
 *
 */
public interface SynchI extends Serializable {
  /** Represents a synch connection - an opaque object managed by the
   * implementation of this interface
   *
   * @author douglm
   *
   */
  interface Connection {
  }

  /** Is synchronization active?
   *
   * @return true if synchronization active.
   */
  boolean getActive();

  /** Get a connection to the synch server.
   *
   * @return null if synch not active otherwise a connection.
   */
  Connection getSynchConnection();

  /** Make a default file subscription for the given collection.
   *
   * @param val the collection representing the subscription
   * @return true if subscribed OK.
   */
  boolean subscribe(BwCalendar val);

  /**
   *
   * @param val Collection
   * @return status - never null.
   */
  SynchStatusResponse getSynchStatus(BwCalendar val);

  /** Check the subscription if this is an external subscription. Will contact
   * the synch server and check the validity. If there is no subscription
   * on the synch server will attempt to resubscribe.
   *
   * @param val the collection representing the subscription
   * @return result of call
   */
  CheckSubscriptionResult checkSubscription(BwCalendar val);

  /** Remove a subscription for the given collection.
   *
   * @param val the collection representing the subscription
   * @param forDelete - we're deleting the collection - use Oracle workround
   * @return true if unsubscribed OK.
   */
  boolean unsubscribe(BwCalendar val,
                      boolean forDelete);

  /** Refresh the subscription for the given collection.
   *
   * @param val the collection representing the subscription
   * @return Response showing status.
   */
  Response refresh(BwCalendar val);

  /** Returns the synch service information.
   *
   * @return full synch info
   */
  BwSynchInfo getSynchInfo();
}
