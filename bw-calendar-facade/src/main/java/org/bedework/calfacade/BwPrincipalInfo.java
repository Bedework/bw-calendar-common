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
package org.bedework.calfacade;

import org.bedework.calfacade.util.CalFacadeUtil;
import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;
import org.bedework.base.ToString;
import org.bedework.util.misc.Util;
import org.bedework.util.vcard.JsonCardBuilder;

import net.fortuna.ical4j.vcard.Property;
import net.fortuna.ical4j.vcard.Property.Id;
import net.fortuna.ical4j.vcard.VCard;
import net.fortuna.ical4j.vcard.VCardBuilder;
import net.fortuna.ical4j.vcard.property.Capacity;
import net.fortuna.ical4j.vcard.property.Categories;
import net.fortuna.ical4j.vcard.property.Kind;
import net.fortuna.ical4j.vcard.property.MaxInstances;
import net.fortuna.ical4j.vcard.property.NoCost;

import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/** This class represent directory style information for the principal. It will be
 * retrieved from a pluggable class.
 *
 * @author douglm
 *
 */
public class BwPrincipalInfo
        implements Logged, Comparable<BwPrincipalInfo>, Serializable  {
  protected String principalHref;  // Related principal

  private VCard card;

  private String cardStr;

  /* Basic info */
  private String lastname;
  private String firstname;
  private String phone;
  private String email;
  private String dept;

  private String caladruri;

  private String kind;

  private boolean hasFullAccess;
  
  private List<BwPrincipalInfo> members;

  /** Class for properties collection.
   *
   * @param <T>
   */
  public static class PrincipalProperty<T>
          implements Comparable<PrincipalProperty<T>> {
    /** Name of property */
    private final String name;
    /** Value of property */
    private final T val;

    /**
     * @param name of property
     * @param val of property
     */
    public PrincipalProperty(final String name,
                             final T val) {
      this.name = name;
      this.val = val;
    }

    /**
     * @return  String name
     */
    public String getName() {
      return name;
    }

    /**
     * @return  T val
     */
    public T getVal() {
      return val;
    }

    @Override
    @SuppressWarnings("unchecked")
    public int compareTo(final PrincipalProperty that) {
      if (this == that) {
        return 0;
      }

      final int ret = CalFacadeUtil.compareStrings(name, that.name);
      if (ret != 0) {
        return ret;
      }

      if (val instanceof Comparable) {
        return ((Comparable<Object>)val).compareTo(that.val);
      } else {
        return -1; // Never occurs
      }
    }

    @Override
    public String toString() {
      final ToString ts = new ToString(this);

      ts.append("name", getName());
      ts.append("val", getVal());

      return ts.toString();
    }
  }

  /** Class for int properties.
   *
   * @author douglm
   */
  public static class IntPrincipalProperty extends PrincipalProperty<Integer> {
    /**
     * @param name of property
     * @param val of property
     */
    public IntPrincipalProperty(final String name,
                                final Integer val) {
      super(name, val);
    }
  }

  /** Class for boolean properties.
   *
   * @author douglm
   */
  public static class BooleanPrincipalProperty extends PrincipalProperty<Boolean> {
    /**
     * @param name of property
     * @param val of property
     */
    public BooleanPrincipalProperty(final String name,
                                    final Boolean val) {
      super(name, val);
    }
  }

  /** Info about an image */
  public static class ImagePropertyVal {
    /** */
    public String url;

    /** */
    public byte[] bytes;
    /** */
    String encoding;
    /** */
    String imageType;

    ImagePropertyVal(final String url,
                     final byte[] bytes,
                     final String encoding,
                     final String imageType) {
      this.url = url;
      this.bytes = bytes;
      this.encoding = encoding;
      this.imageType = imageType;
    }
  }

  /** Class for image properties.
   *
   * @author douglm
   */
  public static class ImagePrincipalProperty extends PrincipalProperty<ImagePropertyVal> {
    /**
     * @param name of property
     * @param val of property
     */
    public ImagePrincipalProperty(final String name,
                                  final ImagePropertyVal val) {
      super(name, val);
    }
  }

  /* More info */
  private List<PrincipalProperty<?>> properties;

  /** */
  public static final int ptypeString = 0;
  /** */
  public static final int ptypeInt = 1;
  /** */
  public static final int ptypeBoolean = 2;
  /** */
  public static final int ptypeImage = 3;

  /** */
  public static final boolean isMulti = true;

  /** Info about properties */
  public static class PrincipalPropertyInfo {
    private final Property.Id vcardPname;

    /** Name of property */
    public String name;

    /** Type of property */
    public int ptype;

    /** true for multi-valued*/
    public boolean multi;

    PrincipalPropertyInfo(final Property.Id vcardPname,
                          final String name,
                          final int ptype,
                          final boolean multi) {
      this.vcardPname = vcardPname;
      this.name = name;
      this.ptype = ptype;
      this.multi = multi;
    }

    /**
     * @return  String vcardPname
     */
    public Property.Id getVcardPname() {
      return vcardPname;
    }

    /**
     * @return  String name of property
     */
    public String getName() {
      return name;
    }

    /**
     * @return  int type of property
     */
    public int getPtype() {
      return ptype;
    }

    /**
     * @return  true for multi
     */
    public boolean getMulti() {
      return multi;
    }
  }

  private static final Map<String, PrincipalPropertyInfo> pinfoMap =
    new HashMap<>();

  static {
    // Type of principal
    addPinfo(Property.Id.KIND, "kind");    // Value as for vcard
    addPinfo(null, "principal-class", ptypeInt);    // Provide finer grained classification

    // Identification of principal
    addPinfo(Property.Id.FN, "fn");      // Full name
    addPinfo(Property.Id.N, "n");       // Structured name - see vcard
    addPinfo(null, "vcard");   // vcard if available
    addPinfo(Property.Id.ORG, "ou");      // Organizational unit - vcard url?
    addPinfo(Id.EMAIL, "email");
    addPinfo(Id.CALADRURI, "caladruri");

    // Descriptive
    addPinfo(Property.Id.CATEGORIES, "category", isMulti);
    addPinfo(Property.Id.PHOTO,"image", ptypeImage, isMulti);

    // Resources
    addPinfo(Property.Id.CAPACITY, "capacity", ptypeInt);
    //addPinfo(Property.Id.RESTRICTEDACCESS,"admittance-restricted", ptypeBoolean);
    //addPinfo(Property.Id.ADMISSIONINFO, "admittance-info");            // Url to some information on entries etc
    //addPinfo(Property.Id.ACCESSABILITYINFO, "accessibility-info");         // Url to some information

    // Scheduling
    addPinfo(Property.Id.AUTOSCHEDULE, "auto-schedule");// Absent - based on kind - or approval-url
    //addPinfo(Property.Id.APPROVALINFO, "approval-url");               // No auto-accept - url of approver
    addPinfo(Property.Id.SCHEDADMININFO, "admin-url");                  // url of resource administrator
    addPinfo(Property.Id.MAXINSTANCES, "max-instances", ptypeInt);    // Max for recurring event -
                                            // absent for global limit

    /* schedule window start and end define how far in advance a meeting
     * request may be made. Both are durations
     *
     */
    addPinfo(Property.Id.BOOKINGWINDOWSTART, "schedule-window-start");       // How far in advance?
                                             // Absent - no restriction
    addPinfo(Property.Id.BOOKINGWINDOWEND, "schedule-window-end");         // How far in advance?

    addPinfo(Property.Id.NOCOST, "nocost", ptypeBoolean);        // Absent - no cost to schedule
  }

  /* ====================================================================
   *                   Constructors
   * ==================================================================== */

  /** No-arg constructor
   */
  public BwPrincipalInfo() {
  }

  /* ====================================================================
   *                   Bean methods
   * ==================================================================== */

  /**
   * @param val href
   */
  public void setPrincipalHref(final String val) {
    principalHref = val;
  }

  /**
   * @return  String Principal entry
   */
  public String getPrincipalHref() {
    return principalHref;
  }

  /**
   * @return  associated vcard
   */
  public VCard getCard() {
    return card;
  }

  /**
   * @return  associated vcard as a string
   */
  public String getCardStr() {
    return cardStr;
  }

  /**
   * @param val last name
   */
  public void setLastname(final String val) {
    lastname = val;
  }

  /**
   * @return  String last name
   */
  public String getLastname() {
    return lastname;
  }

  /**
   * @param val first name
   */
  public void setFirstname(final String val) {
    firstname = val;
  }

  /**
   * @return  String firstname
   */
  public String getFirstname() {
    return firstname;
  }

  /**
   * @param val phone
   */
  public void setPhone(final String val) {
    phone = val;
  }

  /**
   * @return  String phone
   */
  public String getPhone() {
    return phone;
  }

  /**
   * @param val email
   */
  public void setEmail(final String val) {
    email = val;
  }

  /**
   * @return  String email
   */
  public String getEmail() {
    return email;
  }

  /**
   * @param val department
   */
  public void setDept(final String val) {
    dept = val;
  }

  /**
   * @return  String dept
   */
  public String getDept() {
    return dept;
  }

  /**
   * @param val kind
   */
  public void setKind(final String val) {
    kind = val;
  }

  /**
   * @return  String kind
   */
  public String getKind() {
    return kind;
  }

  /**
   * 
   * @return true if this user has full access - even if system default is subscriptions only
   */
  public boolean getHasFullAccess() {
    return hasFullAccess;
  }
  
  /**
   * @param val caladr uri
   */
  public void setCaladruri(final String val) {
    caladruri = val;
  }

  public String getCaladruri() {
    return caladruri;
  }

  /** The properties are any other properties thought to be useful. All of type
   * PrincipalProperty.
   *
   * @param val properties
   */
  public void setProperties(final List<PrincipalProperty<?>> val) {
    properties = val;
  }

  /**
   * @return Collection of UserProperty
   */
  public List<PrincipalProperty<?>> getProperties() {
    return properties;
  }

  /** The members of the group.
   *
   * @param val list of members
   */
  public void setMembers(final List<BwPrincipalInfo> val) {
    members = val;
  }

  /**
   * @return Info on members
   */
  public List<BwPrincipalInfo> getMembers() {
    return members;
  }

  /* ====================================================================
   *                        Convenience methods
   * ==================================================================== */

  /**
   * @param val the property
   */
  public void addProperty(final PrincipalProperty<?> val) {
    List<PrincipalProperty<?>> c = getProperties();
    if (c == null) {
      c = new ArrayList<>();
      setProperties(c);
    }
    if (!c.contains(val)) {
      c.add(val);
    }
  }

  /**
   * @param name of property
   * @return first property found or null
   */
  public PrincipalProperty<?> findProperty(final String name) {
    final List<PrincipalProperty<?>> l = getProperties();
    if (l == null) {
      return null;
    }

    for (final PrincipalProperty<?> p: l) {
      if (name.equalsIgnoreCase(p.getName())) {
        return p;
      }
    }

    return null;
  }

  /**
   * @param name of property
   * @return (Possibly empty) List of PrincipalProperty values with given name
   */
  public List<PrincipalProperty<?>> getProperties(final String name) {
    final List<PrincipalProperty<?>> res = new ArrayList<>();
    final List<PrincipalProperty<?>> l = getProperties();
    if (l == null) {
      return res;
    }

    for (final PrincipalProperty<?> p: l) {
      if (name.equalsIgnoreCase(p.getName())) {
        res.add(p);
      }
    }

    return res;
  }

  /**
   * @param pi PrincipalPropertyInfo
   * @param p Property
   */
  public void addProperty(final PrincipalPropertyInfo pi,
                          final Property p) {
    switch (p.getId()) {
    case KIND:
      setKind(p.getValue());
      addProperty(new PrincipalProperty<>("kind", p.getValue()));
      // addPinfo(null, "principal-class", ptypeInt);    // Provide finer grained classification
      break;

    case FN:
      addProperty(new PrincipalProperty<>("fn", p.getValue()));
      break;

    case N:
      if (p.getValue() == null) {
        break;
      }

      final String[] split = p.getValue().split(";");
      if (split.length > 0) {
        setLastname(split[0]);
      }

      if (split.length > 1) {
        setFirstname(split[1]);
      }

      addProperty(new PrincipalProperty<>("n", p.getValue()));
      break;

    case CALADRURI:
      setCaladruri(p.getValue());
      addProperty(new PrincipalProperty<>("caladruri", p.getValue()));
      break;

    case EMAIL:
      setEmail(p.getValue());
      addProperty(new PrincipalProperty<>("email", p.getValue()));
      break;

    case ORG:
      addProperty(new PrincipalProperty<>("ou", p.getValue()));
      break;

    case CATEGORIES:
      final Iterator<String> it =
              ((Categories)p).getCategories().iterator();
      while (it.hasNext()) {
        addProperty(new PrincipalProperty<>("category",
                                            it.next()));
      }
      break;

    case PHOTO:
      /*
      Photo ph = (Photo)p;
      Parameter type = ph.getParameter("TYPE");
      ImagePropertyVal ipv = new ImagePropertyVal(ph.getUri(),
                                                  ph.getBinary(),
                                                  ?
                                                  contentType);

      addProperty(new ImagePrincipalProperty("image", ipv));
      */
      break;

    case CAPACITY:
      addProperty(new IntPrincipalProperty("capacity",
                                           ((Capacity)p).getInteger()));
      break;
/*
    case RESTRICTEDACCESS:
      addProperty(new BooleanPrincipalProperty("admittance-restricted",
                                               ((RestrictedAccess)p).getBoolean()));
      break;

    case ADMISSIONINFO:
      addProperty(new PrincipalProperty<String>("admittance-info", p.getValue()));
      break;

    case ACCESSABILITYINFO:
      addProperty(new PrincipalProperty<String>("accessibility-info", p.getValue()));
      break;
*/
    case AUTOSCHEDULE:
      addProperty(new PrincipalProperty<>("auto-schedule",
                                          p.getValue()));
      break;

 //   case APPROVALINFO:
 //     addProperty(new PrincipalProperty<String>("approval-url", p.getValue()));
 //     break;

    case SCHEDADMININFO:
      addProperty(new PrincipalProperty<>("admin-url", p.getValue()));
      break;

    case MAXINSTANCES:
      addProperty(new IntPrincipalProperty("max-instances",
                                           ((MaxInstances)p).getInteger()));
    break;

    case BOOKINGWINDOWSTART:
      addProperty(new PrincipalProperty<>("schedule-window-start",
                                          p.getValue()));
    break;

                                             // Absent - no restriction
    case BOOKINGWINDOWEND:
      addProperty(new PrincipalProperty<>("schedule-window-end",
                                          p.getValue()));
    break;

    case NOCOST:
      addProperty(new BooleanPrincipalProperty("nocost",
                                               ((NoCost)p).getBoolean()));
    break;

    }
  }

  /**
   * @return collection of PrincipalPropertyInfo
   */
  public static Collection<PrincipalPropertyInfo> getPrincipalPropertyInfoSet() {
    return pinfoMap.values();
  }

  /**
   * @param cardStr vcard as string
   */
  public void setPropertiesFromVCard(final String cardStr,
                                     final String addrDataCtype) {
    if (cardStr == null) {
      return;
    }

    this.cardStr = cardStr;
    addProperty(new PrincipalProperty<>("vcard", cardStr));

    try {
      if ("application/vcard+json".equals(addrDataCtype)) {
        card = new JsonCardBuilder(null).build(new StringReader(cardStr));
      } else {
        card = new VCardBuilder(new StringReader(cardStr)).build();
      }

      Property piprop = card.getExtendedProperty("X-BW-PRINCIPALHREF");
      if (piprop != null) {
        setPrincipalHref(piprop.getValue());
      }

      piprop = card.getExtendedProperty("X-ICAL4J-TOV3-KIND");
      if (piprop != null) {
        setKind(piprop.getValue());
      }

      if (getKind() == null) {
        // Check for member attributes
        piprop = card.getProperty(Id.MEMBER);

        if (piprop != null) {
          setKind(Kind.GROUP.getValue());
        }
      }

      for (final PrincipalPropertyInfo ppi:
              BwPrincipalInfo.getPrincipalPropertyInfoSet()) {
        final Property.Id pname = ppi.getVcardPname();
        if (pname == null) {
          // Not a vcard property
          continue;
        }

        if (!ppi.getMulti()) {
          // Single valued
          final Property prop = card.getProperty(pname);

          if (prop == null) {
            continue;
          }

          addProperty(ppi, prop);
        } else {
          final List<Property> ps = card.getProperties(pname);
          if (Util.isEmpty(ps)) {
            continue;
          }

          for (final Property prop: ps) {
            addProperty(ppi, prop);
          }
        }
      }
    } catch (final Throwable t) {
      if (debug()) {
        debug("CardStr was " + cardStr);
      }
      throw new RuntimeException(t);
    }
  }

  /* ====================================================================
   *                        Object methods
   * ==================================================================== */

  @Override
  public int compareTo(final BwPrincipalInfo that) {
    if (this == that) {
      return 0;
    }

    return getPrincipalHref().compareTo(that.getPrincipalHref());
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }

    if (!(obj instanceof BwPrincipalInfo)) {
      return false;
    }

    return compareTo((BwPrincipalInfo)obj) == 0;
  }

  @Override
  public int hashCode() {
    return 7 * getPrincipalHref().hashCode();
  }

  @Override
  public String toString() {
    final ToString ts = new ToString(this);

    ts.append("user", getPrincipalHref());
    ts.append("lastName", getLastname());
    ts.append("kind", getKind());
    ts.append("caladruri", getCaladruri());
    ts.append("properties", getProperties(), true);

    return ts.toString();
  }

  /* ====================================================================
   *                        private methods
   * ==================================================================== */

  private static void addPinfo(final Property.Id vcardPname,
                               final String name) {
    addPinfo(vcardPname, name, ptypeString, false);
  }

  private static void addPinfo(final Property.Id vcardPname,
                               final String name,
                               final boolean multi) {
    addPinfo(vcardPname, name, ptypeString, multi);
  }

  private static void addPinfo(final Property.Id vcardPname,
                               final String name,
                               final int ptype) {
    addPinfo(vcardPname,name, ptype, false);
  }

  private static void addPinfo(final Property.Id vcardPname,
                               final String name,
                               final int ptype,
                               final boolean multi) {
    final String lcname = name.toLowerCase();

    pinfoMap.put(lcname, new PrincipalPropertyInfo(vcardPname,
                                                   lcname,
                                                   ptype,
                                                   multi));
  }

  /* ====================================================================
   *                   Logged methods
   * ==================================================================== */

  private final BwLogger logger = new BwLogger();

  @Override
  public BwLogger getLogger() {
    if ((logger.getLoggedClass() == null) && (logger.getLoggedName() == null)) {
      logger.setLoggedClass(getClass());
    }

    return logger;
  }
}
