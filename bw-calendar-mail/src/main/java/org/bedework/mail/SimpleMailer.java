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

package org.bedework.mail;

import org.bedework.base.exc.BedeworkException;
import org.bedework.calfacade.BwCollection;
import org.bedework.calfacade.BwPrincipal;
import org.bedework.calfacade.mail.MailConfigProperties;
import org.bedework.calfacade.mail.MailerIntf;
import org.bedework.calfacade.mail.Message;
import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;

import net.fortuna.ical4j.model.Calendar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import jakarta.mail.Message.RecipientType;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

/** A mailer which provides some minimal functionality.
 * We do not consider many issues such as spam prevention, efficiency in
 * mailing to large lists, etc.
 *
 * @author  Mike Douglass douglm@rpi.edu
 */
public class SimpleMailer implements Logged, MailerIntf {
  private MailConfigProperties config;

  private Session sess;

  @Override
  public void init(final MailConfigProperties config) {
    this.config = config;

    sess = MailUtil.getSession(config);

    sess.setDebug(debug());
  }

  @Override
  public boolean mailEntity(final Calendar cal,
                            final String originator,
                            final Collection<String>recipients,
                            String subject) {
    if (debug()) {
      debug("mailEntity called with " + cal);
    }

    if (config.getDisabled()) {
      return false;
    }

    var orig = originator;
    if (orig == null) {
      orig = config.getFrom();
    }

    var sub = subject;
    if (sub == null) {
      sub = config.getSubject();
    }

    final var toList = MailUtil.makeToList(recipients);
    final var msg =
            MailUtil.makeMimeMessage(sess,
                                     cal,
                                     orig,
                                     toList,
                                     sub);

    try {
      final Transport tr = sess.getTransport(config.getProtocol());

      tr.connect();
      tr.sendMessage(msg, toList);

      return true;
    } catch (final Throwable t) {
      if (debug()) {
        error(t);
      }

      throw new BedeworkException(t);
    }
  }

  @Override
  public void addList(final BwCollection cal) {
    debug("addList called with " + cal.getName());
  }

  @Override
  public void deleteList(final BwCollection cal) {
    debug("deleteList called with " + cal.getName());
  }

  @Override
  public Collection<String> listLists() {
    debug("listLists called");
    return new ArrayList<>();
  }

  @Override
  public boolean checkList(final BwCollection cal) {
    debug("checkList called with " + cal.getName());
    return true;
  }

  @Override
  public void postList(final BwCollection cal, final Message val) {
    debug("postList called with " + cal.getName() + " and message:");
    debug(val.toString());
  }

  @Override
  public void addMember(final BwCollection cal,
                        final BwPrincipal<?> member) {
    debug("addUser called with " + cal.getName() + " and member " +
             member.getAccount());
  }

  @Override
  public void removeMember(final BwCollection cal,
                           final BwPrincipal<?> member) {
    debug("removeUser called with " + cal.getName() + " and member " +
             member.getAccount());
  }

  @Override
  public boolean checkMember(final BwCollection cal,
                             final BwPrincipal<?> member) {
    debug("checkUser called with " + cal.getName() + " and member " +
             member.getAccount());
    return true;
  }

  @Override
  public void updateMember(final BwCollection cal,
                           final BwPrincipal<?> member,
                           final String newEmail)
        {
    debug("updateUser called with " + cal.getName() + " and member " +
             member.getAccount() + " and new email " + newEmail);
  }

  @Override
  public Collection<BwPrincipal<?>> listMembers(final BwCollection cal) {
    debug("listUsers called with " + cal.getName());
    return new ArrayList<>();
  }

  @Override
  public void post(final Message val) {
    debug("Mailer called with:");
    debug(val.toString());

    if (config.getDisabled()) {
      return;
    }

    try {
      /* Create a message with the appropriate mime-type
       */
      final MimeMessage msg = new MimeMessage(sess);

      msg.setFrom(new InternetAddress(val.getFrom()));

      final InternetAddress[] tos = new InternetAddress[val.getMailTo().length];

      int i = 0;
      for (final String recip: val.getMailTo()) {
        tos[i] = new InternetAddress(recip);
        i++;
      }

      msg.setRecipients(RecipientType.TO, tos);

      msg.setSubject(val.getSubject());
      msg.setSentDate(new Date());

      msg.setContent(val.getContent(), "text/plain");

      final Transport tr = sess.getTransport(config.getProtocol());

      tr.connect();
      tr.sendMessage(msg, tos);
    } catch (final Throwable t) {
      if (debug()) {
        error(t);
      }

      throw new BedeworkException(t);
    }
  }

  /* ==============================================================
   *                   Logged methods
   * ============================================================== */

  private final BwLogger logger = new BwLogger();

  @Override
  public BwLogger getLogger() {
    if ((logger.getLoggedClass() == null) && (logger.getLoggedName() == null)) {
      logger.setLoggedClass(getClass());
    }

    return logger;
  }
}
