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

import org.bedework.base.exc.BedeworkException;
import org.bedework.calfacade.BwStats;
import org.bedework.calfacade.configs.AuthProperties;
import org.bedework.calfacade.configs.CardDavInfo;
import org.bedework.calfacade.configs.CmdUtilProperties;
import org.bedework.calfacade.configs.Configurations;
import org.bedework.calfacade.configs.DirConfigProperties;
import org.bedework.calfacade.configs.DumpRestoreProperties;
import org.bedework.calfacade.configs.IndexProperties;
import org.bedework.calfacade.configs.NotificationProperties;
import org.bedework.calfacade.configs.SynchConfig;
import org.bedework.calfacade.configs.SystemProperties;
import org.bedework.calfacade.mail.MailConfigProperties;
import org.bedework.calfacade.svc.CalSvcIPars;
import org.bedework.calfacade.util.Sysprop;
import org.bedework.calsvci.CalSvcFactoryDefault;
import org.bedework.calsvci.CalSvcI;
import org.bedework.common.sysmon.BwSysMonitor;
import org.bedework.sysevents.listeners.BwSysevLogger;
import org.bedework.util.config.ConfInfo;
import org.bedework.util.config.ConfigBase;
import org.bedework.util.config.ConfigurationStore;
import org.bedework.util.http.service.HttpConfig;
import org.bedework.util.http.service.HttpOut;
import org.bedework.util.jmx.BaseMBean;
import org.bedework.util.jmx.ConfBase;
import org.bedework.util.jmx.ConfigHolder;
import org.bedework.util.security.keys.GenKeys;
import org.bedework.util.servlet.io.PooledBuffers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.ObjectName;

/** All the configuration objects used by CalSvc and its callers.
 *
 */
public final class ConfigurationsImpl
        extends ConfBase<SystemPropertiesImpl>
        implements SystemConfMBean, Configurations,
        ConfigHolder<SystemPropertiesImpl> {
  private AutoKiller autoKiller;

  private String rootUser;

  private static boolean configured;

  private static SystemProperties sysProperties;

  private static AuthProperties authProperties;

  private static AuthProperties unAuthProperties;

  private static MailConfigProperties mailProps;

  private static NotificationProperties notificationProps;

  private static SynchConfig synchProps;

  private static CardDavInfo unauthCardDavInfo;

  private static CardDavInfo authCardDavInfo;

  private static HttpConfig httpConfig;

  private static PooledBuffers pooledBuffers;

  private final Map<String, DirConfigProperties> dirConfigs = new HashMap<>();

  private final boolean readOnlySystem =
          Sysprop.readOnlySystem();

  private static final String cmdUtilClass =
          "org.bedework.tools.cmdutil.CmdUtil";

  private static final String dumpRestoreClass =
          "org.bedework.dumprestore.BwDumpRestore";

  private static DumpRestoreProperties dumpRestoreProperties;

  private static CmdUtilProperties cmdUtilProperties;

  private static final String indexerCtlClass =
          "org.bedework.indexer.BwIndexCtl";

  private static IndexProperties indexProperties;

  private static final String chgnoteClass =
          "org.bedework.chgnote.BwChgNote";

  private static final String inoutClass =
          "org.bedework.inoutsched.BwInoutSched";

  /** Name of the property holding the location of the config data */
  private static final String confDirName = "bwengine";

  /** This class acts as the mbean for the system properties
   *
   */
  public ConfigurationsImpl() {
    super("org.bedework.bwengine:service=system",
          confDirName,
          Configurations.systemPropsNamePart);

    try {
      checkMbeansInstalled();

      if (!readOnlySystem) {
        authProperties = new ROAuthProperties(getAuthProps(true));
      }

      unAuthProperties = new ROAuthProperties(getAuthProps(false));
    } catch (final BedeworkException bfe) {
      throw bfe;
    } catch (final Throwable t) {
      throw new BedeworkException(t);
    }
  }

  @Override
  public String loadConfig() {
    return null;
  }

  @Override
  public AuthProperties getAuthenticatedAuthProperties() {
    return authProperties;
  }

  @Override
  public AuthProperties getUnauthenticatedAuthProperties() {
    return unAuthProperties;
  }

  @Override
  public SystemProperties getSystemProperties() {
    return sysProperties;
  }

  @Override
  public HttpConfig getHttpConfig() {
    return httpConfig;
  }

  @Override
  public DumpRestoreProperties getDumpRestoreProperties() {
    return dumpRestoreProperties;
  }

  @Override
  public IndexProperties getIndexProperties() {
    return indexProperties;
  }

  @Override
  public MailConfigProperties getMailConfigProperties() {
    return mailProps;
  }

  @Override
  public NotificationProperties getNotificationProps() {
    return notificationProps;
  }

  @Override
  public SynchConfig getSynchConfig() {
    return synchProps;
  }

  @Override
  public DirConfigProperties getDirConfig(final String name) {
    return dirConfigs.get(name);
  }

  @Override
  public CardDavInfo getCardDavInfo(final boolean auth) {
    if (auth) {
      return authCardDavInfo;
    }

    return unauthCardDavInfo;
  }

  /**
   * @param name of config
   * @return service name for the mbean with this name
   */
  public static String getServiceName(final String name) {
    return "org.bedework.bwengine:service=" + name;
  }

  /**
   * @return name for unauthenticated properties mbean
   */
  public static ObjectName getUnauthpropsName() {
    try {
      return new ObjectName(getServiceName(
              Configurations.unauthPropsNamePart));
    } catch (final Throwable t) {
      throw new BedeworkException(t);
    }
  }

  /**
   * @return name for authenticated properties mbean
   */
  public static ObjectName getAuthpropsName() {
    try {
      return new ObjectName(getServiceName(
              Configurations.authPropsNamePart));
    } catch (final Throwable t) {
      throw new BedeworkException(t);
    }
  }

  /**
   * @return name for system properties mbean
   */
  public static ObjectName getSyspropsName() {
    try {
      return new ObjectName(getServiceName(
              Configurations.systemPropsNamePart));
    } catch (final Throwable t) {
      throw new BedeworkException(t);
    }
  }

  private synchronized void checkMbeansInstalled() {
    if (configured) {
      return;
    }

    try {
      /* ------------- System properties -------------------- */
      ConfBase.getManagementContext().start();
      register(getSyspropsName(), this);

      loadConfig(SystemPropertiesImpl.class);

      cfg.setTestMode(Boolean.getBoolean("org.bedework.testmode"));

      sysProperties = new ROSystemProperties(cfg);
      getRootUser();

      /* ------------- Auth properties -------------------- */
      AuthConf conf = new AuthConf(Configurations.unauthPropsNamePart);
      register(getUnauthpropsName(), conf);
      conf.loadConfig();

      if (!readOnlySystem) {
        conf = new AuthConf(Configurations.authPropsNamePart);
        register(getAuthpropsName(), conf);
        conf.loadConfig();
      }

      /* ------------- System properties -------------------- */
      //final SystemConf sconf = new SystemConf(systemPropsNamePart);
      //register(getSyspropsName(), sconf);
      //sconf.loadConfig();

      /* ------------- Notification properties -------------------- */
      final NotificationConf nc = new NotificationConf();
      register(new ObjectName(nc.getServiceName()), nc);
      nc.loadConfig();
      notificationProps = nc.getConfig();

      /* ------------- Mailer properties -------------------- */
      if (!readOnlySystem) {
        final MailerConf mc = new MailerConf();
        register(new ObjectName(mc.getServiceName()), mc);
        mc.loadConfig();
        mailProps = mc.getConfig();
      }

      /* ------------- Http properties -------------------- */
      final HttpOut ho =
              new HttpOut("bwengine", "httpConfig");
      register(new ObjectName(ho.getServiceName()), ho);
      ho.loadConfig();
      httpConfig = ho.getConfig();

      /* ------------- Sysevents -------------------- */
      final BwSysevLogger sysev = new BwSysevLogger();
      register(new ObjectName(BwSysevLogger.serviceName), sysev);
      sysev.start();

      /* ------------- Monitor -------------------- */
      final BwSysMonitor sysmon = new BwSysMonitor();
      register(new ObjectName(BwSysMonitor.serviceName), sysmon);
      sysmon.start();

      /* ------------- GenKeys -------------------- */
      if (!readOnlySystem) {
        final GenKeys gk = new GenKeys("bwengine");
        register(new ObjectName(GenKeys.serviceName), gk);
        gk.loadConfig();
      }

      /* ------------- Pooled buffers -------------------- */
      final PooledBuffers pb = new PooledBuffers();
      register(new ObjectName(pb.getServiceName()), pb);

      /* ------------- Synch properties -------------------- */
      if (!readOnlySystem) {
        final SynchConf sc = new SynchConf();
        register(new ObjectName(sc.getServiceName()), sc);
        sc.loadConfig();
        synchProps = sc.getConfig();
      }

      /* ------------- Directory interface properties ------------- */
      loadDirConfigs();

      /* At this point we can call ourselves usable */
      configured = true;

      /* ------------- Change notifications  -------------------- */
      if (!readOnlySystem) {
        startChgNote();
      }

      /* ------------- Carddav ------------------------------------ */
      loadCardDav();

      /* ------------- CmdUtil --------------------- */
      if (!readOnlySystem) {
        loadCmdUtil();
      }

      /* ------------- DumpRestore properties --------------------- */
      if (!readOnlySystem) {
        loadDumpRestore();
      }

      /* ------------- Indexer properties ------------------------- */
      startIndexing();

      /* ------------- InoutSched --------------------------------- */
      if (!readOnlySystem) {
        startScheduling();
      }

      /* ------------- Autokiller --------------------------------- */
      startAutoKiller();
    } catch (final Throwable t) {
      throw new BedeworkException(t);
    } finally {
      if (!configured) {
        stop();
      }
    }
  }

  private void startChgNote() throws Throwable {
    final ConfBase<?> cb = loadInstance(chgnoteClass);

    register(new ObjectName(cb.getServiceName()), cb);
    cb.start();
  }

  private void loadCardDav() throws Throwable {
    unauthCardDavInfo = (CardDavInfo)load(
            new CardDavInfoConf(
                    Configurations.unauthCardDavInfoNamePart), false);

    authCardDavInfo = (CardDavInfo)load(
            new CardDavInfoConf(Configurations.authCardDavInfoNamePart), false);
  }

  private void loadCmdUtil() throws Throwable {
    cmdUtilProperties = (CmdUtilProperties)load(
            loadInstance(cmdUtilClass), false);
  }

  private void loadDumpRestore() throws Throwable {
    dumpRestoreProperties = (DumpRestoreProperties)load(
            loadInstance(dumpRestoreClass), false);
  }

  private void startIndexing() throws Throwable {
    indexProperties = (IndexProperties)load(
            loadInstance(indexerCtlClass), true);
  }

  private void startScheduling() throws Throwable {
    load(loadInstance(inoutClass), true);
  }

  private void startAutoKiller() {
    try {
      autoKiller = new AutoKiller(getSystemProps());
      autoKiller.start();
    } catch (final Throwable t) {
      error(t);
      error("Unable to start autokill process");
    }
  }

  private ConfigBase<?> load(final ConfBase<?> cb,
                             final boolean start) throws Throwable {
    register(new ObjectName(cb.getServiceName()), cb);

    cb.loadConfig();

    if (start) {
      cb.start();
    }

    return cb.getConfig();
  }

  private void loadDirConfigs() throws Throwable {
    final ConfigurationStore cs = getStore().getStore("dirconfigs");

    final List<String> names = cs.getConfigs();

    for (final String dn: names) {
      final ObjectName objectName = createObjectName("dirconfig", dn);

      /* Read the config so we can get the mbean class name. */

      final DirConfigPropertiesImpl dCfg = (DirConfigPropertiesImpl)cs.getConfig(dn);

      if (dCfg == null) {
        error("Unable to read directory configuration " + dn);
        continue;
      }

      String mbeanClassName = dCfg.getMbeanClassName();

      if (mbeanClassName == null) {
        error("Must set the mbean class name for connector " + dn);
        error("Falling back to base class for " + dn);

        mbeanClassName = DirConf.class.getCanonicalName();
      }

      @SuppressWarnings("unchecked")
      final DirConf<DirConfigPropertiesImpl> dc =
              (DirConf<DirConfigPropertiesImpl>)ConfBase
                      .makeObject(
                              mbeanClassName,
                              objectName.toString(),
                              cs,
                              dn);

      if (dc == null) {
        error("Unable to create mbean class: " + mbeanClassName);
        error("Skipping");
        continue;
      }

      dc.setConfig(dCfg);

      dirConfigs.put(dn, dc);

      register(new ObjectName(dc.getServiceName()), dc);
    }
  }

  private AuthProperties getAuthProps(final boolean auth) {
    try {
      final ObjectName mbeanName;

      if (!auth) {
        mbeanName = getUnauthpropsName();
      } else {
        mbeanName = getAuthpropsName();
      }

      return (AuthProperties)ConfBase.getManagementContext().getAttribute(mbeanName, "Config");
    } catch (final BedeworkException bfe) {
      throw bfe;
    } catch (final Throwable t) {
      throw new BedeworkException(t);
    }
  }

  private static ConfBase<?> loadInstance(final String cname) {
    try {
      final ClassLoader loader = Thread.currentThread().getContextClassLoader();
      final Class<?> cl = loader.loadClass(cname);

      if (cl == null) {
        throw new BedeworkException("Class " + cname + " not found");
      }

      final Object o = cl.getDeclaredConstructor().newInstance();

      return (ConfBase<?>)o;
    } catch (final Throwable t) {
      t.printStackTrace();
      throw new RuntimeException(t);
    }
  }

  private SystemProperties getSystemProps() {
    return sysProperties;
  }

  @Override
  public void stop() {
    for (final Object o: getRegisteredMBeans()) {
      if (o instanceof BaseMBean) {
        try {
          ((BaseMBean)o).stop();
        } catch (final Throwable t){
          t.printStackTrace();
        }
      }
    }

    try {
      ConfBase.getManagementContext().stop();
    } catch (final Throwable t){
      t.printStackTrace();
    }
  }

  private void getRootUser() {
    final String[] rootUsers = getSystemProperties()
            .getRootUsers()
            .split(",");

    if ((rootUsers.length > 0) && (rootUsers[0] != null)) {
      rootUser = rootUsers[0];
    }
  }

  /* ===============================================================
      SystemProperties attributes
   */

  @Override
  public void setTzid(final String val) {
    getConfig().setTzid(val);
  }

  @Override
  public String getTzid() {
    return getConfig().getTzid();
  }

  @Override
  public void setTzServeruri(final String val) {
    getConfig().setTzServeruri(val);
  }

  @Override
  public String getTzServeruri() {
    return getConfig().getTzServeruri();
  }

  @Override
  public void setSystemid(final String val) {
    getConfig().setSystemid(val);
  }

  @Override
  public String getSystemid() {
    return getConfig().getSystemid();
  }

  @Override
  public void setRootUsers(final String val) {
    getConfig().setRootUsers(val);
  }

  @Override
  public String getRootUsers() {
    return getConfig().getRootUsers();
  }

  @Override
  public void setFeatureFlags(final String val) {
    getConfig().setFeatureFlags(val);
  }

  @Override
  public String getFeatureFlags() {
    return getConfig().getFeatureFlags();
  }

  @Override
  public void setAdminContact(final String val) {
    getConfig().setAdminContact(val);
  }

  @Override
  public String getAdminContact() {
    return getConfig().getAdminContact();
  }

  @Override
  public void setIscheduleURI(final String val) {
    getConfig().setIscheduleURI(val);
  }

  @Override
  public String getIscheduleURI() {
    return getConfig().getIscheduleURI();
  }

  @Override
  public void setFburlServiceURI(final String val) {
    getConfig().setFburlServiceURI(val);
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
    getConfig().setWebcalServiceURI(val);
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
    getConfig().setCalSoapWsURI(val);
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
    getConfig().setCalSoapWsWSDLURI(val);
  }

  @Override
  public String getCalSoapWsWSDLURI() {
    return getConfig().getCalSoapWsWSDLURI();
  }

  @Override
  public void setTimezonesByReference(final boolean val) {
    getConfig().setTimezonesByReference(val);
  }

  @Override
  public boolean getTimezonesByReference() {
    return getConfig().getTimezonesByReference();
  }

  @Override
  public void setUserauthClass(final String val) {
    getConfig().setUserauthClass(val);
  }

  @Override
  public String getUserauthClass() {
    return getConfig().getUserauthClass();
  }

  @Override
  public void setMailerClass(final String val) {
    getConfig().setMailerClass(val);
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
    getConfig().setAdmingroupsClass(val);
  }

  @Override
  public String getAdmingroupsClass() {
    return getConfig().getAdmingroupsClass();
  }

  @Override
  public void setUsergroupsClass(final String val) {
    getConfig().setUsergroupsClass(val);
  }

  @Override
  public String getUsergroupsClass() {
    return getConfig().getUsergroupsClass();
  }

  @Override
  public void setLocaleList(final String val) {
    getConfig().setLocaleList(val);
  }

  @Override
  public String getLocaleList() {
    return getConfig().getLocaleList();
  }

  @Override
  public void setSocketToken(final String val) {
    getConfig().setSocketToken(val);
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
    getConfig().setEventregAdminToken(val);
  }

  @Override
  public String getEventregAdminToken() {
    return getConfig().getEventregAdminToken();
  }

  @Override
  public void setEventregUrl(final String val) {
    getConfig().setEventregUrl(val);
  }

  @Override
  public String getEventregUrl() {
    return getConfig().getEventregUrl();
  }

  @Override
  public void setEventregWSUrl(final String val) {
    getConfig().setEventregWSUrl(val);
  }

  @Override
  public String getEventregWSUrl() {
    return getConfig().getEventregWSUrl();
  }

  @Override
  public void setEventregAdminUrl(final String val) {
    getConfig().setEventregAdminUrl(val);
  }

  @Override
  public String getEventregAdminUrl() {
    return getConfig().getEventregAdminUrl();
  }

  @Override
  public void setCacheUrlPrefix(final String val) {
    getConfig().setCacheUrlPrefix(val);
  }

  @Override
  public String getCacheUrlPrefix() {
    return getConfig().getCacheUrlPrefix();
  }

  @Override
  public void setAutoKillMinutes(final int val) {
    getConfig().setAutoKillMinutes(val);
  }

  @Override
  public int getAutoKillMinutes() {
    return getConfig().getAutoKillMinutes();
  }

  @Override
  public void setSuggestionEnabled(final boolean val) {
    getConfig().setSuggestionEnabled(val);
  }

  @Override
  public boolean getSuggestionEnabled() {
    return getConfig().getSuggestionEnabled();
  }

  @Override
  public void setWorkflowEnabled(final boolean val) {
    getConfig().setWorkflowEnabled(val);
  }

  @Override
  public boolean getWorkflowEnabled() {
    return getConfig().getWorkflowEnabled();
  }

  @Override
  public void setWorkflowRoot(final String val) {
    getConfig().setWorkflowRoot(val);
  }

  @Override
  public String getWorkflowRoot() {
    return getConfig().getWorkflowRoot();
  }

  @Override
  public void setSubmissionRoot(final String val) {
    getConfig().setSubmissionRoot(val);
  }

  @Override
  public String getSubmissionRoot() {
    return getConfig().getSubmissionRoot();
  }

  @Override
  public void setUserSubscriptionsOnly(final boolean val) {
    getConfig().setUserSubscriptionsOnly(val);
  }

  @Override
  public boolean getUserSubscriptionsOnly() {
    return getConfig().getUserSubscriptionsOnly();
  }

  @Override
  public int getAutoKillTerminated() {
    return autoKiller.getTerminated();
  }

  @Override
  public int getAutoKillFailedTerminations() {
    return autoKiller.getFailedTerminations();
  }

  @Override
  public void setVpollMaxItems(final Integer val) {
    getConfig().setVpollMaxItems(val);
  }

  @Override
  public Integer getVpollMaxItems() {
    return getConfig().getVpollMaxItems();
  }

  @Override
  public void setVpollMaxActive(final Integer val) {
    getConfig().setVpollMaxActive(val);
  }

  @Override
  public Integer getVpollMaxActive() {
    return getConfig().getVpollMaxActive();
  }

  @Override
  public void setVpollMaxVoters(final Integer val) {
    getConfig().setVpollMaxVoters(val);
  }

  @Override
  public Integer getVpollMaxVoters() {
    return getConfig().getVpollMaxVoters();
  }

  @Override
  public void setSyseventsProperties(final List<String> val) {
    getConfig().setSyseventsProperties(val);
  }

  @Override
  @ConfInfo(collectionElementName = "syseventsProperty"
  )
  public List<String> getSyseventsProperties() {
    return getConfig().getSyseventsProperties();
  }

  @Override
  public void addSyseventsProperty(final String name,
                                   final String val) {
    getConfig().addSyseventsProperty(name, val);
  }

  @Override
  public String getSyseventsProperty(final String name) {
    return getConfig().getSyseventsProperty(name);
  }

  @Override
  public void removeSyseventsProperty(final String name) {
    getConfig().removeSyseventsProperty(name);
  }

  @Override
  public void setSyseventsProperty(final String name,
                                   final String val) {
    getConfig().setSyseventsProperty(name, val);
  }

  @Override
  public void setTestMode(final boolean val) {
    getConfig().setTestMode(val);
  }

  @Override
  public boolean getTestMode() {
    return getConfig().getTestMode();
  }

  @Override
  public void setDbStatsEnabled(final boolean enable) {
    try {
      getSvci();

      if (svci != null) {
        svci.setDbStatsEnabled(enable);
      }
    } catch (final Throwable t) {
      error(t);
    } finally {
      closeSvci();
    }
  }

  @Override
  public boolean getDbStatsEnabled() {
    try {
      getSvci();

      return svci != null && svci.getDbStatsEnabled();
    } catch (final Throwable t) {
      error(t);
      return false;
    } finally {
      closeSvci();
    }
  }

  /* ========================================================================
   * Operations
   * ======================================================================== */

  @Override
  public BwStats getStats()  {
    try {
      getSvci();

      if (svci == null) {
        return null;
      }

      return svci.getStats();
    } catch (final Throwable t) {
      error(t);
      return null;
    } finally {
      closeSvci();
    }
  }

  @Override
  public void dumpDbStats() {
    try {
      getSvci();

      if (svci != null) {
        svci.dumpDbStats();
      }
    } catch (final Throwable t) {
      error(t);
    } finally {
      closeSvci();
    }
  }

  @Override
  public String listOpenIfs() {
    return autoKiller.listOpenIfs();
  }

  private CalSvcI svci;

  private CalSvcI getSvci() {
    if (getConfig() == null) {
      return null;
    }

    if ((svci != null) && svci.isOpen()) {
      return svci;
    }

    final CalSvcIPars pars = CalSvcIPars.getServicePars(getServiceName(),
                                                        rootUser,
                                                        true,// publicAdmin
                                                        true);   // Allow super user
    svci = new CalSvcFactoryDefault().getSvc(pars);

    svci.open();
    svci.beginTransaction();

    return svci;
  }

  /**
   */
  private void closeSvci() {
    if ((svci == null) || !svci.isOpen()) {
      return;
    }

    try {
      svci.endTransaction();
    } catch (final Throwable t) {
      try {
        svci.close();
      } catch (final Throwable ignored) {
      }
    }

    try {
      svci.close();
    } catch (final Throwable ignored) {
    }
  }

  @Override
  public void putConfig() {
    saveConfig();
  }

  @Override
  public SystemProperties cloneIt() {
    return null;
  }
}