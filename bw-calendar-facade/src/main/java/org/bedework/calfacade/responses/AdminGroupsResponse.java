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

import org.bedework.calfacade.BwGroup;
import org.bedework.base.ToString;
import org.bedework.base.response.Response;

import java.util.Collection;

/** Container for fetching admin groups.
 *
 * @author Mike Douglass douglm - spherical cow
 */
public class AdminGroupsResponse
        extends Response<AdminGroupsResponse> {
  private Collection<BwGroup<?>> groups;

  /**
   *
   * @param val collection of groups
   */
  public AdminGroupsResponse setGroups(final Collection<BwGroup<?>> val) {
    groups = val;
    return this;
  }

  /**
   * @return collection of groups
   */
  public Collection<BwGroup<?>> getGroups() {
    return groups;
  }

  @Override
  public ToString toStringSegment(final ToString ts) {
    return super.toStringSegment(ts)
                .append("groups", getGroups());
  }
}
