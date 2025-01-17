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

import org.bedework.calfacade.BwResource;
import org.bedework.calfacade.indexing.BwIndexer;

import java.io.Serializable;
import java.util.List;

/** Interface for handling bedework stored resources.
 *
 * <p>Usually this will be an attachment but it may also be an image resource or
 * some other type of downloadable resource.
 *
 * <p>Resources are stored within folders which can be located anywhere in the
 * hierarchy but MUST NOT be calendar collections. This allows us to remain
 * compliant with CalDAV.
 *
 * @author Mike Douglass
 *
 */
public interface ResourcesI extends Serializable {
  /** Save a resource. The collection MUST exist.
   * The named resource MUST NOT exist.
   *
   * @param  val       resource with attached content
   * @param returnIfExists  false to throw an exception
   * @return true if created, false if already exists
   */
  boolean save(BwResource val,
               boolean returnIfExists);

  /** Save a notification resource.
   * The collection MUST exist and MUST be a notification collection.
   * The named resource MUST NOT exist
   *
   * @param  val       resource with attached content
   * @return true if created, false if already exists
   */
  boolean saveNotification(BwResource val);

  /** Get a resource given the path - does not get content
   *
   * @param  path     String path to resource
   * @return BwResource null for unknown resource
   */
  BwResource get(String path);

  /** Retrieve resource content given the resource. It will be set in the resource
   * object
   *
   * @param  val BwResource
   */
  void getContent(BwResource val);

  /** Get resources to which this user has access - content is not fetched.
   *
   * @param  path           String path to containing collection
   * @return List     of BwResource
   */
  List<BwResource> getAll(String path);

  /** Get resources to which this user has access - content is not fetched.
   *
   * @param  path           String path to containing collection
   * @param count this many
   * @return List     of BwResource
   */
  List<BwResource> get(String path,
                       int count);

  /** Update a resource.
   *
   * @param  val          resource
   * @param updateContent if true we also update the content
   */
  void update(BwResource val,
              boolean updateContent);

  /** Delete a resource and content given the path
   *
   * @param  path     String path to resource
   */
  void delete(String path);

  /** Move or copy the given resource to the destination collection.
   *
   * @param  val BwResource
   * @param to        Path of estination collection
   * @param name      String name of new entity
   * @param copy      true for copying
   * @param overwrite destination exists
   * @return true if destination created (i.e. not updated)
   */
  boolean copyMove(BwResource val,
                   String to,
                   String name,
                   boolean copy,
                   boolean overwrite);

  class ReindexCounts {
    public long resources;
    public long resourceContents;

    public long skippedTombstonedResources;
  }

  /** Reindex current users entities
   *
   * @param indexer to use for this operation
   * @param contentIndexer to use for this operation
   * @param collectionIndexer to use for this operation
   * @return number of resources and resourcecontents reindexed
   */
  ReindexCounts reindex(BwIndexer indexer,
                BwIndexer contentIndexer,
                BwIndexer collectionIndexer);
}
