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
package org.bedework.calfacade.filter;

import org.bedework.base.exc.BedeworkBadRequest;
import org.bedework.base.exc.BedeworkException;
import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.regex.Pattern;

/**
 * @author douglm
 *
 */
public class SfpTokenizer extends StreamTokenizer implements Logged {
  private static final int WORD_CHAR_START = 32;

  private static final int WORD_CHAR_END = 255;

  private static final int WHITESPACE_CHAR_START = 0;

  private static final int WHITESPACE_CHAR_END = ' ';

  private static final Pattern quotePattern = Pattern.compile("([\"'])");

  /**
   * @param rdr
   */
  public SfpTokenizer(final Reader rdr) {
    super(rdr);

    lowerCaseMode(false);
    wordChars(WORD_CHAR_START, WORD_CHAR_END);
    whitespaceChars(WHITESPACE_CHAR_START,
                              WHITESPACE_CHAR_END);
    ordinaryChar('.');
    ordinaryChar(':');
    ordinaryChar(';');
    ordinaryChar(',');
    ordinaryChar('~');
    ordinaryChar('=');
    ordinaryChar('!');
    ordinaryChar('>');
    ordinaryChar('<');
    ordinaryChar('&');
    ordinaryChar('|');
    ordinaryChar('(');
    ordinaryChar(')');
    ordinaryChar('[');
    ordinaryChar(']');
    ordinaryChar('\t');
    eolIsSignificant(false);
    whitespaceChars(0, 0);

    quoteChar('"');
    quoteChar('\'');
  }

  /**
   * @return int
   */
  public int next() {
    try {
      return nextToken();
    } catch (IOException e) {
      throw new BedeworkException(e);
    }
  }

  /**
   * Asserts that the next token in the stream matches the specified token.
   *
   * @param token expected token
   */
  public void assertToken(final int token) {
    try {
      if (nextToken() != token) {
        throw new BedeworkBadRequest("Expected [" + token + "], read [" +
                                             ttype + "] at " + lineno());
      }

      if (debug()) {
        if (token > 0) {
          debug("[" + (char)token + "]");
        } else {
          debug("[" + token + "]");
        }
      }
    } catch (IOException e) {
      throw new BedeworkException(e);
    }
  }

  /**
   */
  public void assertWord() {
    assertToken(StreamTokenizer.TT_WORD);
  }

  /**
   */
  public void assertString() {
    if (testToken('"') || testToken('\'')) {
      return;
    }

    throw new BedeworkBadRequest("Expected <quoted-string>, read [" +
                                  ttype + "] at " + lineno());
  }

  /**
   * @return true if it's a quoted string
   */
  public boolean testString() {
    return testToken('"') || testToken('\'');
  }

  /**
   * Asserts that the next token in the stream matches the specified token.
   * This method is case-sensitive.
   *
   * @param token
   */
  public void assertToken(final String token) {
    assertToken(token, false);
  }

  /**
   * Asserts that the next token in the stream matches the specified token.
   *
   * @param token expected token
   * @param ignoreCase
   */
  public void assertToken(final String token, final boolean ignoreCase) {
    // ensure next token is a word token..
    assertWord();

    if (ignoreCase) {
      if (!token.equalsIgnoreCase(sval)) {
        throw new BedeworkBadRequest("Expected [" + token + "], read [" +
                                  sval + "] at " + lineno());
      }
    } else if (!token.equals(sval)) {
      throw new BedeworkBadRequest( "Expected [" + token + "], read [" +
                                sval + "] at " + lineno());
    }

    if (debug()) {
      debug("[" + token + "]");
    }
  }

  /**
   * @return boolean true if eof flagged
   */
  public boolean atEof() {
    return ttype == StreamTokenizer.TT_EOF;
  }

  /**
   * Tests that the next token in the stream matches the specified token.
   * This method is case-sensitive.
   *
   * @param token
   * @return boolean
   */
  public boolean testToken(final int token) {
    try {
      boolean res = nextToken() == token;

      if (!res) {
        pushBack();
        return false;
      }

      return true;
    } catch (IOException e) {
      throw new BedeworkException(e);
    }
  }

  /**
   * Tests if the next token in the stream matches the specified token.
   *
   * @param token expected token
   * @return int
   */
  public boolean testToken(final String token) {
    return testToken(token, true);
  }

  /**
   * Tests if the next token in the stream matches the specified token.
   *
   * @param token expected token
   * @param ignoreCase
   * @return boolean
   */
  public boolean testToken(final String token, final boolean ignoreCase) {
    // ensure next token is a word token..
    if (!testToken(StreamTokenizer.TT_WORD)) {
      return false;
    }

    if (ignoreCase) {
      if (!token.equalsIgnoreCase(sval)) {
        pushBack();
        return false;
      }
    } else if (!token.equals(sval)) {
      pushBack();
      return false;
    }

    return true;
  }

  /**
   * Absorbs extraneous newlines.
   */
  public void skipWhitespace() {
    while (true) {
      assertToken(StreamTokenizer.TT_EOL);
    }
  }

  public static String escapeQuotes(String s) {
         return quotePattern.matcher(s).replaceFirst("\\\\$1");
  }

  /* ====================================================================
   *                   Logged methods
   * ==================================================================== */

  private BwLogger logger = new BwLogger();

  @Override
  public BwLogger getLogger() {
    if ((logger.getLoggedClass() == null) && (logger.getLoggedName() == null)) {
      logger.setLoggedClass(getClass());
    }

    return logger;
  }
}
