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

import org.bedework.calfacade.BwDateTime;
import org.bedework.calfacade.BwEvent;
import org.bedework.calfacade.exc.CalFacadeException;
import org.bedework.calfacade.locale.Resources;
import org.bedework.calfacade.mail.MailConfigProperties;
import org.bedework.calfacade.mail.MailerIntf;
import org.bedework.calfacade.mail.Message;
import org.bedework.calfacade.mail.ObjectAttachment;
import org.bedework.calfacade.util.BwDateTimeUtil;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;

import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/** Some useful methods used when mailing calendar objects..
 *
 * @author Mike Douglass douglm@rpi.edu
 */
public class MailUtil {
  /** make event printable
   *
   * @param event to display
   * @return printable event
   */
  public static StringBuffer displayableEvent(final BwEvent event) {
    return displayableEvent(event, new Resources());
  }

  /** make event printable using string resources
   *
   * @param event to display
   * @param rsrc localized string resources
   * @return printable event
   */
  public static StringBuffer displayableEvent(final BwEvent event,
                                              final Resources rsrc) {
    final StringBuffer sb = new StringBuffer();

    sb.append(event.getSummary());
    sb.append("\n");
    sb.append(rsrc.getString(Resources.START));
    sb.append(": ");
    sb.append(formatDate(event.getDtstart()));
    sb.append("\n");
    sb.append(rsrc.getString(Resources.END));
    sb.append(": ");
    sb.append(formatDate(event.getDtend()));
    sb.append("\n");
    sb.append(rsrc.getString(Resources.DESCRIPTION));
    sb.append(": \n");
    sb.append(event.getDescription());
    sb.append("\n");

    return sb;
  }

  /**
   * @param dt a date time object
   * @return formatted date
   */
  public static String formatDate(final BwDateTime dt) {
    final DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
    try {
      return df.format(BwDateTimeUtil.getDate(dt));
    } catch (final Throwable t) {
      return t.getMessage();
    }
  }

  /** Mail a message to somebody.
   *
   * <p>All required message fields are set. The message will be mailed via
   * the supplied mailer. If the Object is non-null it will be
   * converted to the appropriate external form and sent as an attachment.
   *
   * @param mailer   to mail it
   * @param val      Message to mail
   * @param att      String val to attach - e.g event, todo
   * @param name     name for attachment
   * @param type     mimetype for attachment
   * @param sysid    used for from address
   */
  public static void mailMessage(final MailerIntf mailer,
                                 final Message val,
                                 final String att,
                                 final String name,
                                 final String type,
                                 final String sysid) {
    final ObjectAttachment oa = new ObjectAttachment();

    oa.setOriginalName(name);
    oa.setVal(att);
    oa.setMimeType(type);

    val.addAttachment(oa);

    if (val.getFrom() == null) {
      // This should be a property
      val.setFrom("donotreply-" + sysid);
    }

    mailer.post(val);
  }

  static void setNonNull(final Properties props,
                  final String name,
                  final String val) {
    if (val == null) {
      throw new RuntimeException("Null property value for " + name);
    }

    props.setProperty(name, val);
  }

  public static Session getSession(final MailConfigProperties config) {
    final Properties props = new Properties();

    setNonNull(props, "mail.transport.protocol", config.getProtocol());
    setNonNull(props, "mail." + config.getProtocol() + ".host",
               config.getServerUri());
    if (config.getServerPort() != null) {
      props.put("mail." + config.getProtocol() + ".port",
                config.getServerPort());
    }

    props.put("mail." + config.getProtocol() + ".starttls.enable",
              String.valueOf(config.getStarttls()));

    //  add handlers for main MIME types
    final MailcapCommandMap mc = (MailcapCommandMap)CommandMap.getDefaultCommandMap();
    mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
    mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
    mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
    mc.addMailcap("text/calendar;; x-java-content-handler=com.sun.mail.handlers.text_plain");
    mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
    mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
    CommandMap.setDefaultCommandMap(mc);

    final String username;
    final String pw;
    username = config.getServerUsername();
    pw = config.getServerPassword();

    final Session sess;

    if (username != null) {
      // Authentication required.
      final var authenticator =
              new MailerAuthenticator(username, pw);
      props.put("mail." + config.getProtocol() + ".auth", "true");
      sess = Session.getInstance(props, authenticator);
    } else {
      sess = Session.getInstance(props);
    }

    return sess;
  }

  public static InternetAddress[] makeToList(final Collection<String> recipients) {
    final InternetAddress[] tos = new InternetAddress[recipients.size()];

    try {
      int i = 0;
      for (final String recip: recipients) {
        tos[i] = new InternetAddress(recip);
        i++;
      }
    } catch (final Throwable t) {
      throw new CalFacadeException(t);
    }

    return tos;
  }

  public static MimeMessage makeMimeMessage(final Session sess,
                                            final Calendar cal,
                                            final String originator,
                                            final InternetAddress[] toList,
                                            final String subject) {
    try {
      /* Create a message with the appropriate mime-type
       */
      final MimeMessage msg = new MimeMessage(sess);

      msg.setFrom(new InternetAddress(originator));

      msg.setRecipients(javax.mail.Message.RecipientType.TO, toList);

      msg.setSubject(subject);
      msg.setSentDate(new Date());

      final BodyPart messageText = new MimeBodyPart();
      messageText.setText("A vpoll message: to be filled out with details...");

      final BodyPart ics = new MimeBodyPart();
      final CalendarOutputter co = new CalendarOutputter(false);

      final Writer wtr =  new StringWriter();
      co.output(cal, wtr);
      final String content = wtr.toString();

      ics.setContent(content, "text/calendar; " +
              "charset=\"UTF-8\";" +
              "METHOD=" + cal.getMethod().getValue());

      final Multipart multipartAlt = new MimeMultipart("alternative");
//      final Multipart multipartAlt = new MimeMultipart();
      multipartAlt.addBodyPart(messageText);
      multipartAlt.addBodyPart(ics);

      final BodyPart appIcs = new MimeBodyPart();
      appIcs.setDisposition("attachment; filename=\"poll.ics\"");
      appIcs.setHeader("Content-Class", "urn:content-classes:calendarmessage");
      appIcs.setContent(content,
                        "application/ics;" +
                                " charset=\"UTF-8\";" +
                                "METHOD=" + cal.getMethod().getValue() + ";" +
                                "name=\"poll.ics\"");

      final Multipart multipart = new MimeMultipart();

      final var mbody = new MimeBodyPart();
      mbody.setContent(multipartAlt);
      multipart.addBodyPart(mbody);
      multipart.addBodyPart(appIcs);

      msg.setContent(multipart);

      return msg;
    } catch (final Throwable t) {
      throw new CalFacadeException(t);
    }

  }
}

