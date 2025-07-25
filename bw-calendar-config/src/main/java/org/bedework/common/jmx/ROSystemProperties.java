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
package org.bedework.common.jmx;

import org.bedework.calfacade.configs.SystemProperties;
import org.bedework.util.config.ConfInfo;

import java.util.List;

/**
 * @author douglm
 *
 */
public final class ROSystemProperties implements SystemProperties {
  /* Wrapper to make System properties read only. */

  private final SystemProperties cfg;

  private SystemProperties getConfig() {
    return cfg;
  }

  /**
   * @param cfg - system properties
   */
  ROSystemProperties(final SystemProperties cfg) {
    this.cfg = cfg;
  }

  /* ========================================================================
   * Attributes
   * ======================================================================== */

  @Override
  public void setTzid(final String val) {
    throw new RuntimeException("Immutable"); // getConfig().setTzid(val);
  }

  @Override
  public String getTzid() {
    return getConfig().getTzid();
  }

  @Override
  public void setTzServeruri(final String val) {
    throw new RuntimeException("Immutable"); // getConfig().setTzServeruri(val);
  }

  @Override
  public String getTzServeruri() {
    return getConfig().getTzServeruri();
  }

  @Override
  public void setSystemid(final String val) {
    throw new RuntimeException("Immutable"); // getConfig().setSystemid(val);
  }

  @Override
  public String getSystemid() {
    return getConfig().getSystemid();
  }

  @Override
  public void setRootUsers(final String val) {
    throw new RuntimeException("Immutable");
  }

  @Override
  public String getRootUsers() {
    return getConfig().getRootUsers();
  }

  @Override
  public void setFeatureFlags(final String val) {
    throw new RuntimeException("Immutable");
  }

  @Override
  public String getFeatureFlags() {
    return getConfig().getFeatureFlags();
  }

  @Override
  public void setAdminContact(final String val) {
    throw new RuntimeException("Immutable"); // getConfig().setAdminContact(val);
  }

  @Override
  public String getAdminContact() {
    return getConfig().getAdminContact();
  }

  @Override
  public void setIscheduleURI(final String val) {
    throw new RuntimeException("Immutable"); // getConfig().setIscheduleURI(val);
  }

  @Override
  public String getIscheduleURI() {
    return getConfig().getIscheduleURI();
  }

  @Override
  public void setFburlServiceURI(final String val) {
    throw new RuntimeException("Immutable"); // getConfig().setFburlServiceURI(val);
  }

  @Override
  public String getFburlServiceURI() {
    return getConfig().getFburlServiceURI();
  }

  /** Set the web calendar service uri - null for no web calendar service
   *
   * @param val    String
   */
  @Override
  public void setWebcalServiceURI(final String val) {
    throw new RuntimeException("Immutable"); // getConfig().setWebcalServiceURI(val);
  }

  /** get the web calendar service uri - null for no web calendar service
   *
   * @return String
   */
  @Override
  public String getWebcalServiceURI() {
    return getConfig().getWebcalServiceURI();
  }

  /** Set the calws soap web service uri - null for no service
   *
   * @param val    String
   */
  @Override
  public void setCalSoapWsURI(final String val) {
    throw new RuntimeException("Immutable"); // getConfig().setCalSoapWsURI(val);
  }

  /** Get the calws soap web service uri - null for no service
   *
   * @return String
   */
  @Override
  public String getCalSoapWsURI() {
    return getConfig().getCalSoapWsURI();
  }

  /** Set the calws soap web service WSDL uri - null for no service
   *
   * @param val    String
   */
  @Override
  public void setCalSoapWsWSDLURI(final String val) {
    throw new RuntimeException("Immutable"); // getConfig().setCalSoapWsWSDLURI(val);
  }

  @Override
  public String getCalSoapWsWSDLURI() {
    return getConfig().getCalSoapWsWSDLURI();
  }

  @Override
  public void setTimezonesByReference(final boolean val) {
    throw new RuntimeException("Immutable"); // getConfig().setTimezonesByReference(val);
  }

  @Override
  public boolean getTimezonesByReference() {
    return getConfig().getTimezonesByReference();
  }

  @Override
  public void setUserauthClass(final String val) {
    throw new RuntimeException("Immutable"); // getConfig().setUserauthClass(val);
  }

  @Override
  public String getUserauthClass() {
    return getConfig().getUserauthClass();
  }

  @Override
  public void setMailerClass(final String val) {
    throw new RuntimeException("Immutable"); // getConfig().setMailerClass(val);
  }

  /**
   *
   * @return String
   */
  @Override
  public String getMailerClass() {
    return getConfig().getMailerClass();
  }

  /** Set the admingroups class
   *
   * @param val    String admingroups class
   */
  @Override
  public void setAdmingroupsClass(final String val) {
    throw new RuntimeException("Immutable"); // getConfig().setAdmingroupsClass(val);
  }

  @Override
  public String getAdmingroupsClass() {
    return getConfig().getAdmingroupsClass();
  }

  @Override
  public void setUsergroupsClass(final String val) {
    throw new RuntimeException("Immutable"); // getConfig().setUsergroupsClass(val);
  }

  @Override
  public String getUsergroupsClass() {
    return getConfig().getUsergroupsClass();
  }

  @Override
  public void setLocaleList(final String val) {
    throw new RuntimeException("Immutable");
  }

  @Override
  public String getLocaleList() {
    return getConfig().getLocaleList();
  }

  @Override
  public void setSocketToken(final String val) {
    throw new RuntimeException("Immutable");
  }

  @Override
  public String getSocketToken() {
    return getConfig().getSocketToken();
  }

  @Override
  public void setSynchMaxMinutes(final int val) {
    getConfig().setSynchMaxMinutes(val);
  }

  @Override
  public int getSynchMaxMinutes() {
    return getConfig().getSynchMaxMinutes();
  }

  @Override
  public void setEventregAdminToken(final String val) {
    throw new RuntimeException("Immutable"); // getConfig().setEventregAdminToken(val);
  }

  @Override
  public String getEventregAdminToken() {
    return getConfig().getEventregAdminToken();
  }

  @Override
  public void setEventregUrl(final String val) {
    throw new RuntimeException("Immutable"); // getConfig().setEventregUrl(val);
  }

  @Override
  public String getEventregUrl() {
    return getConfig().getEventregUrl();
  }

  @Override
  public void setEventregWSUrl(final String val) {
    throw new RuntimeException("Immutable");
  }

  @Override
  public String getEventregWSUrl() {
    return getConfig().getEventregWSUrl();
  }

  @Override
  public void setEventregAdminUrl(final String val) {
    throw new RuntimeException("Immutable");
  }

  @Override
  public String getEventregAdminUrl() {
    return getConfig().getEventregAdminUrl();
  }

  @Override
  public void setCacheUrlPrefix(final String val) {
    throw new RuntimeException("Immutable");
  }

  @Override
  public String getCacheUrlPrefix() {
    return getConfig().getCacheUrlPrefix();
  }

  @Override
  public void setAutoKillMinutes(final int val) {
    throw new RuntimeException("Immutable");
  }

  @Override
  public int getAutoKillMinutes() {
    return getConfig().getAutoKillMinutes();
  }

  @Override
  public void setSuggestionEnabled(final boolean val) {
    throw new RuntimeException("Immutable");
  }

  @Override
  public boolean getSuggestionEnabled() {
    return getConfig().getSuggestionEnabled();
  }

  @Override
  public void setWorkflowEnabled(final boolean val) {
    throw new RuntimeException("Immutable");
  }

  @Override
  public boolean getWorkflowEnabled() {
    return getConfig().getWorkflowEnabled();
  }

  @Override
  public void setWorkflowRoot(final String val) {
    throw new RuntimeException("Immutable");
  }

  @Override
  public String getWorkflowRoot() {
    return getConfig().getWorkflowRoot();
  }

  @Override
  public void setSubmissionRoot(final String val) {
    throw new RuntimeException("Immutable");
  }

  @Override
  public String getSubmissionRoot() {
    return getConfig().getSubmissionRoot();
  }

  @Override
  public void setUserSubscriptionsOnly(final boolean val) {
    throw new RuntimeException("Immutable");
  }

  @Override
  public boolean getUserSubscriptionsOnly() {
    return getConfig().getUserSubscriptionsOnly();
  }

  @Override
  public void setVpollMaxItems(final Integer val) {
    throw new RuntimeException("Immutable");
  }

  @Override
  public Integer getVpollMaxItems() {
    return getConfig().getVpollMaxItems();
  }

  @Override
  public void setVpollMaxActive(final Integer val) {
    throw new RuntimeException("Immutable");
  }

  @Override
  public Integer getVpollMaxActive() {
    return getConfig().getVpollMaxActive();
  }

  @Override
  public void setVpollMaxVoters(final Integer val) {
    throw new RuntimeException("Immutable");
  }

  @Override
  public Integer getVpollMaxVoters() {
    return getConfig().getVpollMaxVoters();
  }

  @Override
  public void setSyseventsProperties(final List<String> val) {
    throw new RuntimeException("Immutable");
  }

  @Override
  @ConfInfo(collectionElementName = "syseventsProperty")
  public List<String> getSyseventsProperties() {
    return getConfig().getSyseventsProperties();
  }

  @Override
  public void addSyseventsProperty(final String name,
                                   final String val) {
    throw new RuntimeException("Immutable");
  }

  @Override
  public String getSyseventsProperty(final String name) {
    return getConfig().getSyseventsProperty(name);
  }

  @Override
  public void removeSyseventsProperty(final String name) {
    throw new RuntimeException("Immutable");
  }

  @Override
  public void setSyseventsProperty(final String name,
                                   final String val) {
    throw new RuntimeException("Immutable");
  }

  @Override
  public void setTestMode(final boolean val) {
    throw new RuntimeException("Immutable");
  }

  @Override
  public boolean getTestMode() {
    return getConfig().getTestMode();
  }

  @Override
  public SystemProperties cloneIt() {
    return this;
  }
}
