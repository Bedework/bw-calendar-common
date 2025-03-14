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

import org.bedework.database.db.SchemaBuilder;
import org.bedework.calfacade.svc.CalSvcIPars;

import java.io.Serializable;

/** Interface defining svc factory
 *
 * @author Mike Douglass       douglm  rpi.edu
 */
public interface CalSvcFactory extends Serializable {
  /** Get an initialised instance of CalSvcI
   *
   * @param pars svc parameters
   * @return initialised CalSvcI instance
   */
  CalSvcI getSvc(CalSvcIPars pars);

  /** Get an initialised instance of CalSvcI
   *
   * @param loader class loader to use
   * @param pars svc parameters
   * @return initialised CalSvcI instance
   */
  CalSvcI getSvc(ClassLoader loader, CalSvcIPars pars);

  /**
   * @return schema builder
   */
  SchemaBuilder getSchemaBuilder();
}
