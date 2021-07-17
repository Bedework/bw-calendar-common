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
package org.bedework.common.sysmon;

import org.bedework.calfacade.MonitorStat;
import org.bedework.sysevents.events.SysEventBase.SysCode;

/** Display some data as a name/value pair.
 *
 * @author douglm
 */
public class DataValue {
  private final String name;
  private final SysCode sysCode;

  /* The value at the time we last refreshed */
  private double value;

  /**
   * @param name of value
   * @param sysCode - non null if this object is associated with a code
   */
  public DataValue(final String name,
                  final SysCode sysCode) {
    super();

    this.name = name;
    this.sysCode = sysCode;
    setValue(0);
  }

  /**
   * @return syscode or null
   */
  public SysCode getSysCode() {
    return sysCode;
  }

  /**
   * @return name
   */
  public String getName() {
    return name;
  }

  /**
   * @return total
   */
  public double getValue() {
    return value;
  }

  /**
   * @param value double
   */
  public void setValue(final double value) {
    this.value = value;
  }

  /**
   * @return a statistic
   */
  public MonitorStat getStat() {
    return new MonitorStat(getName(), String.valueOf(getValue()));
  }

  /**
   *
   */
  public void inc() {
    inc(1);
  }

  /**
   * @param val double to inc by
   */
  public void inc(final double val) {
    value += val;
  }

  @Override
  public String toString() {
    return getName() + " = " + getValue() + "\n";
  }
}
