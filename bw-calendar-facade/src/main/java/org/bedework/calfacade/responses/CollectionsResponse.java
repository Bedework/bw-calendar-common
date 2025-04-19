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
package org.bedework.calfacade.responses;

import org.bedework.calfacade.BwCollection;
import org.bedework.base.ToString;
import org.bedework.base.response.Response;

/** Container for fetching collections, e.g. calendars, virtual collections etc.
 *
 * @author Mike Douglass douglm - spherical cow
 */
public class CollectionsResponse extends Response {
  private BwCollection collections;
  private BwCollection publicCollections;
  private BwCollection userCollections;

  /**
   *
   * @param val root of collections
   */
  public void setCollections(final BwCollection val) {
    collections = val;
  }

  /**
   * @return root of collections
   */
  public BwCollection getCollections() {
    return collections;
  }

  /**
   *
   * @param val root of public collections
   */
  public void setPublicCollections(final BwCollection val) {
    publicCollections = val;
  }

  /**
   * @return root of public collections
   */
  public BwCollection getPublicCollections() {
    return publicCollections;
  }

  /**
   *
   * @param val root of user collections
   */
  public void setUserCollections(final BwCollection val) {
    userCollections = val;
  }

  /**
   * @return root of user collections
   */
  public BwCollection getUserCollections() {
    return userCollections;
  }

  @Override
  public ToString toStringSegment(final ToString ts) {
    return super.toStringSegment(ts)
                .append("collections", getCollections())
                .append("publicCollections", getPublicCollections())
                .append("userCollections", getUserCollections());
  }
}
