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

import org.bedework.base.ToString;
import org.bedework.base.exc.BedeworkException;
import org.bedework.calfacade.annotations.Dump;
import org.bedework.calfacade.annotations.NoDump;
import org.bedework.calfacade.base.BwDbentity;
import org.bedework.calfacade.util.CalFacadeUtil;
import org.bedework.util.misc.Util;

import org.apache.commons.codec.binary.Base64OutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/** Represent the content for a resource stored within the system, e.g an attachment or an
 * image. The actual content is stored in a BwResourceContent object to allow us
 * to put off retrieval of content - or maybe even store outside of the db.
 *
 *  @author Mike Douglass   douglm - rpi.edu
 */
@Dump(elementName="resourceContent", keyFields={"colPath", "name", "encodedContent"})
public class BwResourceContent extends BwDbentity<BwResourceContent> {
  /* The collection this belongs to
   */
  private String colPath;

  private String name;

  private byte[] byteValue;

  private int hash;
  private boolean hashZero;

  /** Constructor
   *
   */
  public BwResourceContent() {
  }

  /* ====================================================================
   *                      Bean methods
   * ==================================================================== */

  /** Set the object's collection path
   *
   * @param val    String path
   */
  public void setColPath(final String val) {
    colPath = val;
  }

  /** Get the object's collection path
   *
   * @return String   path
   */
  public String getColPath() {
    return colPath;
  }

  /** Set the name
   *
   * @param val    String name
   */
  public void setName(final String val) {
    name = val;
  }

  /** Get the name
   *
   * @return String   name
   */
  public String getName() {
    return name;
  }

  /** Set the byte array value
   *
   * @param val    byte array value
   */
  public void setByteValue(final byte[] val) {
    byteValue = val;
    hashZero = false;
    hash = 0;
  }

  /** Get the byte array value
   *
   * @return byte array or null
   */
  public byte[] getByteValue() {
    return byteValue;
  }

  /* ==============================================================
   *                   db entity methods
   * ============================================================== */

  /** Set the href - ignored
   *
   * @param val    String href
   */
  public void setHref(final String val) { }

  public String getHref() {
    return Util.buildPath(false, getColPath(),
                          "/",
                          getName());
  }

  /* ==============================================================
   *                   non-db methods
   * ============================================================== */

  public InputStream getBinaryStream() {
    if (byteValue != null) {
      return new ByteArrayInputStream(byteValue);
    }

    return null;
  }

  /**
   * @return base64 encoded value
   */
  public String getEncodedContent() {
    final Base64OutputStream b64out;

    try {
      final InputStream str = getBinaryStream();
      if (str == null) {
        return null;
      }

      int len;
      final int chunkSize = 1024;

      final byte[] buffer = new byte[chunkSize];

      final ByteArrayOutputStream baos = new ByteArrayOutputStream();
      b64out = new Base64OutputStream(baos);

      while ((len = str.read(buffer)) != -1) {
        b64out.write(buffer, 0, len);
      }
      b64out.close();

      return baos.toString();
    } catch (final Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /**
   * @return String value
   */
  @NoDump
  public String getStringContent() {
    try {
      final InputStream str = getBinaryStream();
      if (str == null) {
        return null;
      }

      int len;
      final int chunkSize = 1024;

      final byte[] buffer = new byte[chunkSize];

      final ByteArrayOutputStream baos = new ByteArrayOutputStream();

      while ((len = str.read(buffer)) != -1) {
        baos.write(buffer, 0, len);
      }

      return baos.toString();
    } catch (final Throwable t) {
      throw new BedeworkException(t);
    }
  }

  /** Copy this objects values into the parameter
   *
   * @param val resource content
   */
  public void copyTo(final BwResourceContent val) {
    val.setColPath(getColPath());
    val.setName(getName());
    val.setByteValue(getByteValue());
  }

  /* ==============================================================
   *                   Object methods
   * ============================================================== */

  @Override
  public int hashCode() {
    final var strVal = getStringContent();
    if (strVal == null) {
      return 0;
    }

    int h = hash;
    if ((h == 0) && !hashZero) {
      // Not calculated
      h = strVal.hashCode();
      if (h == 0) {
        hashZero = true;
      }

      hash = h;
    }

    return h;
  }

  @Override
  public int compareTo(final BwResourceContent that)  {
    if (this == that) {
      return 0;
    }

    return CalFacadeUtil.cmpObjval(getColPath(), that.getColPath());
    /*
    int res = CalFacadeUtil.cmpObjval(getColPath(), that.getColPath());
    if (res != 0) {
      return res;
    }

    byte[] thisone = getValue();
    byte[] thatone = that.getValue();

    if (thisone == null) {
      if (thatone == null) {
        return 0;
      }

      return -1;
    }

    if (thatone == null) {
      return 1;
    }

    if (thisone.length < thatone.length) {
      return -1;
    }

    if (thatone.length < thisone.length) {
      return 1;
    }

    for (int i = 0; i < thisone.length; i++) {
      byte thisbyte = thisone[i];
      byte thatbyte = thatone[i];

      if (thisbyte < thatbyte) {
        return -1;
      }

      if (thatbyte < thisbyte) {
        return 1;
      }
    }

    return 0;
    */
  }

  @Override
  public String toString() {
    final ToString ts = new ToString(this);

    toStringSegment(ts);
    ts.append("path", getColPath());
    ts.append("name", getName());

    return ts.toString();
  }

  @Override
  public Object clone() {
    final BwResourceContent nobj = new BwResourceContent();
    copyTo(nobj);

    return nobj;
  }
}

