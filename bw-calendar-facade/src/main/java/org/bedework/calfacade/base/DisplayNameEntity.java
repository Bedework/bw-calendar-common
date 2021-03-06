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
package org.bedework.calfacade.base;

import org.bedework.calfacade.BwString;

import java.util.Collection;

/** An entity that can have one or more display names will implement this interface.
 *
 * @author douglm
 */
public interface DisplayNameEntity {

  /** Set the display names collection
   *
   * @param val    Collection of (BwString)display names
   */
  public void setDisplayNames(Collection<BwString> val);

  /** Get the display names
   *
   *  @return Collection     display names set
   */
  public Collection<BwString> getDisplayNames();

  /**
   * @return int number of display names.
   */
  public int getNumDisplayNames();

  /**
   * @param lang
   * @param val
   */
  public void addDisplayName(String lang, String val);

  /** If display name with given lang is present updates the value, otherwise adds it.
   * @param lang
   * @param val
   */
  public void updateDisplayNames(String lang, String val);

  /**
   * @param lang
   * @return BwString with language code or default
   */
  public BwString findDisplayName(String lang);

  /**
   * @param val  String default
   * @deprecated
   */
  public void setDisplayName(String val);

  /**
   * @return String default
   * @deprecated
   */
  public String getDisplayName();

  /**
   * @param val
   */
  public void addDisplayName(BwString val);

  /**
   * @param val
   * @return boolean true if removed.
   */
  public boolean removeDisplayName(BwString val);
}
