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

import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.svc.BwView;

import java.io.Serializable;
import java.util.Collection;

/** Interface for handling bedework view objects.
 *
 * @author Mike Douglass
 *
 */
public interface ViewsI extends Serializable {
  /** Add a view.
   *
   * @param  val           BwView to add
   * @param  makeDefault   boolean true for make this the default.
   * @return boolean false view not added, true - added.
   */
  boolean add(BwView val,
              boolean makeDefault);

  /** Remove the view for the owner of the object.
   *
   * @param  val     BwView
   * @return boolean false - view not found.
   */
  boolean remove(BwView val);

  /** Find the named view.
   *
   * @param  val     String view name - null means default
   * @return BwView  null view not found.
   */
  BwView find(String val);

  /** Add a collection path to the named view.
   *
   * @param  name    String view name - null means default
   * @param  path     collection path to add
   * @return boolean false view not found, true - collection path added.
   */
  boolean addCollection(String name,
                        String path);

  /** Remove a collection path from the named view.
   *
   * @param  name    String view name - null means default
   * @param  path    collection path to remove
   * @return boolean false view not found, true - collection path removed.
   */
  boolean removeCollection(String name,
                                  String path);

  /** Return the collection of views - named collections of collections
   *
   * @return collection of views
   */
  Collection<BwView> getAll();

  /** Return the collection of views - named collections of collections
   *
   * @param pr owning principal
   * @return collection of views
   */
  Collection<BwView> getAll(BwPrincipal pr);
}
