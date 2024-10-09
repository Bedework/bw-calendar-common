package org.bedework.mail;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class MailerAuthenticator extends Authenticator {
  private final PasswordAuthentication authentication;

  MailerAuthenticator(final String user, final String password) {
    authentication = new PasswordAuthentication(user, password);
  }

  protected PasswordAuthentication getPasswordAuthentication() {
    return authentication;
  }
}

