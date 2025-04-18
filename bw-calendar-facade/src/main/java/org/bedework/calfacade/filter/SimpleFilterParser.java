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

import org.bedework.base.exc.BedeworkException;
import org.bedework.caldav.util.TimeRange;
import org.bedework.caldav.util.filter.AndFilter;
import org.bedework.caldav.util.filter.EntityTypeFilter;
import org.bedework.caldav.util.filter.FilterBase;
import org.bedework.caldav.util.filter.ObjectFilter;
import org.bedework.caldav.util.filter.OrFilter;
import org.bedework.caldav.util.filter.PresenceFilter;
import org.bedework.caldav.util.filter.parse.Filters;
import org.bedework.calfacade.BwCollection;
import org.bedework.calfacade.BwCategory;
import org.bedework.calfacade.exc.CalFacadeErrorCode;
import org.bedework.calfacade.ical.BwIcalPropertyInfo;
import org.bedework.calfacade.ical.BwIcalPropertyInfo.BwIcalPropertyInfoEntry;
import org.bedework.calfacade.svc.BwView;
import org.bedework.util.calendar.PropertyIndex.PropertyInfoIndex;
import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;
import org.bedework.base.ToString;
import org.bedework.base.response.GetEntityResponse;

import ietf.params.xml.ns.caldav.TextMatchType;
import net.fortuna.ical4j.model.DateTime;

import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

/** This is a simple filter parser to allow us to embed filter expressions in the
 * request stream. This is not implemented the correct way - we need a grammar
 * and allow for some greater complexity, however...
 *
 * <p>The expressions have very few token types, "(", ")", "&", "|", "=", "!=",
 * ", ", "&lt;", "&gt;", word, number and string value.
 *
 * <p>The word specifies a property.
 *
 * <p>For example
 *  <pre>entity_type="event" & owner="abcd" & (category="lecture" | category="music")
 *  </pre>
 *
 * <p>The entity_type term must be first.
 *
 * <p>This class is serially reusable but NOT thread-safe</p>
 *
 * @author Mike Douglass
 *
 */
public abstract class SimpleFilterParser implements Logged {
  private SfpTokenizer tokenizer;
  private String currentExpr;
  private String source;

  private SimpleFilterParser subParser;
  private boolean explicitSelection;

  private static class Token {
  }

  private static class OpenParenthesis extends Token {
    @Override
    public String toString() {
      return "OpenParenthesis{}";
    }
  }

  private final static Token openParen = new OpenParenthesis();

  private final static int isDefined = 0;

  private final static int notDefined = 1;

  private final static int equal = 2;

  private final static int notEqual = 3;

  private final static int like = 4;

  private final static int notLike = 5;

  private final static int greaterThan = 6;

  private final static int lessThan = 7;

  private final static int greaterThanOrEqual = 8;

  private final static int lessThanOrEqual = 9;

  private final static int inTimeRange = 10;

  private final static int andOp = 11;

  private final static int orOp = 12;

  private final static int startsWithOp = 13;

  private final static int indexedOp = 14;

  private static class Operator extends Token {
    int op;

    Operator(final int op) {
      this.op = op;
    }

    @Override
    public String toString() {
      return "Operator{op=" + op + "}";
    }
  }

  private static class LogicalOperator extends Operator {
    LogicalOperator(final int op) {
      super(op);
    }
  }

  private static final LogicalOperator andOperator = new LogicalOperator(andOp);

  private static final LogicalOperator orOperator = new LogicalOperator(orOp);

  private final Stack<Token> stack = new Stack<>();

  private final Stack<FilterBase> filterStack = new Stack<>();

  // This lets us unwind the parse
  private static class ParseFailed extends Exception {

  }

  /** */
  public static class ParseResult {
    /** true if the parse went ok */
    public boolean ok = true;

    /** Explain what went wrong */
    public String message;

    /** result of a successful parse */
    public FilterBase filter;

    /**  */
    public BedeworkException be;

    /** Result from parseSort
     *
     */
    public List<SortTerm> sortTerms;

    public ParseResult() {
    }

    public void SetFilter(final FilterBase filter) {
      this.filter = filter;
    }

    public ParseFailed fail(final String message) throws ParseFailed {
      ok = false;
      this.message = message;

      throw new ParseFailed();
    }

    public ParseFailed setBfe(final BedeworkException bfe) throws ParseFailed {
      ok = false;
      message = bfe.getMessage();
      this.be = bfe;

      throw new ParseFailed();
    }

    public ParseFailed fromPr(final ParseResult pr) throws ParseFailed {
      ok = false;
      message = pr.message;
      be = pr.be;

      throw new ParseFailed();
    }

    @Override
    public String toString() {
      final ToString ts = new ToString(this);

      ts.append("ok", ok);

      if (ok) {
        ts.append("filter", filter);
      } else {
        ts.append("errcode", be.getMessage());
      }

      return ts.toString();
    }
  }

  private final ParseResult parseResult = new ParseResult();

  /**
   *
   * @param path of collection
   * @return collection object or null.
   */
  public abstract BwCollection getCollection(String path);

  /** Attempt to get collection referenced by the alias. For an internal alias
   * the result will also be set in the aliasTarget property of the parameter.
   *
   * @param val collection
   * @param resolveSubAlias - if true and the alias points to an alias, resolve
   *                  down to a non-alias.
   * @return BwCollection
   */
  public abstract BwCollection resolveAlias(BwCollection val,
                                            boolean resolveSubAlias);

  /** Returns children of the given collection to which the current user has
   * some access.
   *
   * @param  col          parent collection
   * @return Collection   of BwCollection
   */
  public abstract Collection<BwCollection> getChildren(
          BwCollection col);

  /** An unsatisfactory approach - we'll special case categories for the moment
   * to see if this works. When using these filters we need to search for a
   * category being a member of the set of categories for the event.
   *
   * <p>This only works if the current user is the owner of the named category -
   * at least with my current implementation.
   *
   * @param name of category
   * @return category entity or null.
   */
  public abstract BwCategory getCategoryByName(String name);

  /** A slightly better approach - we'll special case categories for the moment
   * to see if this works. When using these filters we need to search for a
   * category being a member of the set of categories for the event.
   *
   * <p>method takes the uid of the category and is used by the
   * <pre>
   *   catuid=(uid1,uid2,uid3)
   * </pre>
   * construct where the list members must ALL be present.
   *
   * @param uid of the category
   * @return status and possible category entity.
   */
  public abstract GetEntityResponse<BwCategory> getCategoryByUid(String uid);

  /** Get the view given the path.
   *
   * @param path for view
   * @return view or null
   */
  public abstract BwView getView(String path);

  /** A virtual path might be for example "/user/adgrp_Eng/Lectures/Lectures"
   * which has two two components<ul>
   * <li>"/user/adgrp_Eng/Lectures" and</li>
   * <li>"Lectures"</li></ul>
   *
   * <p>
   * "/user/adgrp_Eng/Lectures" is a real path which is an alias to
   * "/public/aliases/Lectures" which is a folder containing the alias
   * "/public/aliases/Lectures/Lectures" which is aliased to the single calendar.
   *
   * @param vpath the virtual path
   * @return collection of collection objects - null for bad vpath
   */
  public abstract Collection<BwCollection> decomposeVirtualPath(final String vpath);

  /**
   *
   * @return a parser so we can parse out sub-filters
   */
  public abstract SimpleFilterParser getParser();

  /** Parse the given expression into a filter. The explicitSelection
   * flag determines whether or not we skip certain collections. For
   * example, we normally skip collections with display off or the
   * inbox. If we explicitly selct thos e items however, we want to
   * see them.
   *
   * @param expr the expression
   * @param explicitSelection true if we are explicitly selecting a
   *                          path or paths
   * @param source            Where the expression came from - for errors
   * @return ParseResult
   */
  public ParseResult parse(final String expr,
                           final boolean explicitSelection,
                           final String source) {
    this.explicitSelection = explicitSelection;
    this.source = source;
    parseResult.ok = true;

    try {
      if (debug()) {
        debug("About to parse filter expression: " + expr +
                      " from " + source);
      }

      currentExpr = expr;
      tokenizer = new SfpTokenizer(new StringReader(expr));

      doExpr();

      /* We should have 0 or 1 logical operators on the stack and a single
       * filter expression (or none for a null filter)
       */

      if (topLOp()) {
        pop();
      }

      if (!stackEmpty()) {
        throw parseResult.fail("Filter syntax: " +
                                       "source: " + source);
      }

      /* We should be left with just the filter on the stack. */

      if (filterStack.size() != 1) {
        throw parseResult.fail("Filter syntax: " +
                                       " source: " + source);
      }

      final FilterBase f = popFilters();

      if (debug()) {
        debug(f.toString());
      }

      parseResult.SetFilter(f);
    } catch (final ParseFailed ignored) {
      // Result set      
    }

    return parseResult;
  }

  /** Parse a comma list of sort terms. Each term is a property name
   * optionally followed by ":" then the terms "ASC" or "DESC". The
   * default is descending.
   *
   * <p>The property name is either a valid bedework property or the
   * word "RELEVANCE" - which is the default</p>
   *
   * @param sexpr - search expression
   * @return list of terms in order of application. Empty for no sort
   *         terms.
   */
  public ParseResult parseSort(final String sexpr) {
    parseResult.ok = true;
    parseResult.sortTerms = new ArrayList<>();

    if (sexpr == null) {
      return parseResult;
    }

    try {
      tokenizer = new SfpTokenizer(new StringReader(sexpr));
      for (; ; ) {
        int tkn = nextToken("parseSort()");

        if (tkn == StreamTokenizer.TT_EOF) {
          return parseResult;
        }

        final List<PropertyInfo> pis = getProperty(tkn);

        tkn = nextToken("parseSort() - :");

        boolean ascending = false;

        if (tkn == ':') {
          tkn = nextToken("parseSort() - asc/desc");

          if (tkn != StreamTokenizer.TT_WORD) {
            throw parseResult.fail("Expected Asc Desc: " +
                                           tkn +
                                           " source: " + source);
          }

          if ("asc".equalsIgnoreCase(tokenizer.sval)) {
            ascending = true;
          } else if ("desc".equalsIgnoreCase(tokenizer.sval)) {
            ascending = false;
          } else {
            throw parseResult.fail("Expected Asc Desc: " +
                                           tkn +
                                           " source: " + source);
          }
        } else if (tkn == StreamTokenizer.TT_EOF) {
          tokenizer.pushBack();
        } else if (tkn != ',') {
          throw parseResult.fail("Bad sort: " +
                                         tkn + " from " + sexpr +
                                         " source: " + source);
        }

        final List<PropertyInfoIndex> pixs = new ArrayList<>();

        pis.forEach(val -> pixs.add(val.pii));
        parseResult.sortTerms.add(new SortTerm(pixs, ascending));
      }
    } catch (final ParseFailed ignored) {
      return parseResult;
    }
  }

  private boolean doFactor() throws ParseFailed {
    if (debug()) {
      debug("doFactor: " + tokenizer.toString());
    }

    final int tkn = nextToken("doFactor(1)");

    if (tkn == StreamTokenizer.TT_EOF) {
      return false;
    }

    if (tkn == '(') {
      push(openParen);
      doExpr();

      if (nextToken("doFactor(2)") != ')') {
        throw parseResult.fail("Expected close paren: " +
                                       " source: " + source);
      }

      popOpenParen();
    } else {
      tokenizer.pushBack();
      if (!doPropertyComparison()) {
        return false;
      }
    }

    if (!topLOp()) {
      return true;
    }

    final FilterBase filter = popFilters();

    final FilterBase topFilter = popFilters();
    if (anding()) {
      filterStack.push(FilterBase.addAndChild(topFilter, filter));
    } else {
      filterStack.push(FilterBase.addOrChild(topFilter, filter));
    }

    pop(); // The operator

    return true;
  }

  private boolean doExpr() throws ParseFailed {
    // Don't seem quite right
    if (debug()) {
      debug("doExpr: " + tokenizer.toString());
    }
    return doTerm();
  }

  private boolean doTerm() throws ParseFailed {
    if (!doFactor()) {
      return false;
    }

    if (debug()) {
      debug("doTerm: " + tokenizer.toString());
    }

    /* If we have a logical operator next then handle that and combine the
     * two filters on top of the stack.
     */

    int tkn = nextToken("doTerm()");

    if (tkn == StreamTokenizer.TT_EOF) {
      return false;
    }

    tkn = checkLop(tkn);

    if ((tkn != '&') && (tkn != '|')) {
      tokenizer.pushBack();
      if (topLOp()) {
        final FilterBase filter = popFilters();

        final FilterBase topFilter = popFilters();
        if (anding()) {
          filterStack.push(FilterBase.addAndChild(topFilter, filter));
        } else {
          filterStack.push(FilterBase.addOrChild(topFilter, filter));
        }
        // Pop it - we used it to ensure all operators at the same level are the
        // same.
        pop();
      }
      return true;
    }

    doLop(tkn);

    doTerm();

    /* doPropertyComparison will do the anding/oring */
/*    Filter filter = popFilters();

    Filter topFilter = popFilters();
    if (anding()) {
      filterStack.push(Filter.addAndChild(topFilter, filter));
    } else {
      filterStack.push(Filter.addOrChild(topFilter, filter));
    }*/

    return true;
  }

  @SuppressWarnings("UnusedReturnValue")
  private boolean doLop(final int tkn) throws ParseFailed {
    LogicalOperator oper = null;

    if (topLOp()) {
      oper = popLOp();
    }

    if (oper != null) {
      // Must match - not allowed to mix logical operators
      if (tkn == '&') {
        if (oper.op != andOp) {
          throw parseResult.fail("Mixed Logical Operators: " +
                                         " source: " + source);
        }
      } else if (oper.op != orOp) {
        throw parseResult.fail("Mixed Logical Operators: " +
                                       " source: " + source);
      }

      push(oper);
    } else if (tkn == '&') {
      push(andOperator);
    } else {
      push(orOperator);
    }
    return true;
  }

  private int checkLop(final int tkn) {
    if (tkn == '&') {
      return tkn;
    }

    if (tkn == '|') {
      return tkn;
    }

    if (tkn != StreamTokenizer.TT_WORD) {
      return tkn;
    }

    final String pname = tokenizer.sval;

    if (pname.equalsIgnoreCase("and")) {
      return '&';
    }

    if (pname.equalsIgnoreCase("or")) {
      return '|';
    }

    return tkn;
  }

  private static class PropertyInfo {
    final PropertyInfoIndex pii;
    final Integer intKey;
    final String strKey;

    private PropertyInfo(final PropertyInfoIndex pii,
                         final Integer intKey,
                         final String strKey) {
      this.pii = pii;
      this.intKey = intKey;
      this.strKey = strKey;
    }
  }

  private List<PropertyInfo> getProperty(final int curtkn) throws ParseFailed {
    int tkn = curtkn;
    final List<PropertyInfo> pis = new ArrayList<>();

    for (;;) {
      if (tkn != StreamTokenizer.TT_WORD) {
        throw parseResult.fail("Expected Property Name: " +
                                       tkn +
                                       " source: " + source);
      }

      final String pname = tokenizer.sval;
      final String pnameUc = pname.toUpperCase();

      if ((pis.size() == 0) && pnameUc.equals("CATUID")) {
        // These are stored all over the place.

        pis.add(new PropertyInfo(PropertyInfoIndex.CATEGORIES, null, null));
        pis.add(new PropertyInfo(PropertyInfoIndex.UID, null, null));
        return pis;
      }

      final PropertyInfoIndex pi = PropertyInfoIndex.fromName(pname);
      if (pi == null) {
        throw parseResult.fail("unknown Property: " +
                                       pname + ": expr was " + currentExpr +
                                       " source: " + source);
      }

      // Check for indexed property
      Integer intIndex = null;
      String strIndex = null;

      tkn = nextToken("getProperty(index)");
      if (tkn == '[') {
        if (pis.size() > 0) {
          throw parseResult.fail("Unimplemented - indexing of nested fields: found" +
                                         tkn +
                                         " source: " + source);
        }
        // Expect an index or a quoted string 
        tkn = nextToken("getProperty(index-1)");

        if (tkn == SfpTokenizer.TT_NUMBER) {
          intIndex = (int)tokenizer.nval;
        } else if ((tkn != '"') && (tkn != '\'')) {
          throw parseResult.fail("Expected number or quoted string: found" +
                                         tkn +
                                         " source: " + source);
        } else {
          strIndex = tokenizer.sval;
        }

        tkn = nextToken("end-getProperty(index)");
        if (tkn != ']') {
          throw parseResult.fail("Expected ']': found" +
                                         tkn +
                                         " source: " + source);
        }

        tkn = nextToken("getProperty");
      }

      pis.add(new PropertyInfo(pi, intIndex, strIndex));

      if (tkn != '.') {
        tokenizer.pushBack();
        return pis;
      }

      tkn = nextToken("getProperty: pname");
    }
  }

  private boolean doPropertyComparison() throws ParseFailed {
    final List<PropertyInfo> pis = getProperty(nextToken("getProperty()"));

    final Operator oper = nextOperator();

    final FilterBase pfilter = makePropFilter(pis, oper.op);

    if (pfilter == null) {
      error(new BedeworkException(CalFacadeErrorCode.filterBadProperty,
                                   listProps(pis) +
                                           " source: " + source));
      throw parseResult.fail("Bad property: " + listProps(pis) +
                                     " source: " + source);
    }

    /* If there is a logical operator on the stack top (and/or) then we create
     * an anded or ored filter and push that.
     *
     * Otherwise we just push
     *
     * WRONG - should be done by doFactor
     */
//    if (!topLOp()) {
    filterStack.push(pfilter);
/*    } else {
      Filter topFilter = popFilters();
      if (anding()) {
        filterStack.push(Filter.addAndChild(topFilter, pfilter));
      } else {
        filterStack.push(Filter.addOrChild(topFilter, pfilter));
      }

      pop(); // The operator
    }*/

    return true;
  }

  /** Enter with either
   * ( word [, word]* ) or
   * word
   *
   * @return list of word values
   * @throws ParseFailed on error
   */
  private ArrayList<String> doWordList() throws ParseFailed {
    int tkn = nextToken("doWordList(1)");

    if (tkn == StreamTokenizer.TT_EOF) {
      throw parseResult.fail("Expected word list: found EOF" +
                                     " source: " + source);
    }

    boolean paren = false;
    final ArrayList<String> res = new ArrayList<>();

    if (tkn == '(') {
      push(openParen);
      paren = true;
    } else {
      tokenizer.pushBack();
    }

    for (;;) {
      tkn = nextToken("doWordList(2)");

      if ((tkn != '"') && (tkn != '\'')) {
        throw parseResult.fail("Expected quoted string: found" +
                                       tkn +
                                       " source: " + source);
      }

      res.add(tokenizer.sval);

      tkn = nextToken("doWordList(3)");

      if (tkn == ',') {
        if (!paren) {
          throw parseResult.fail("Bad list: " + tkn +
                                         " source: " + source);
        }
        continue;
      }

      if (paren) {
        if (tkn == ')') {
          popOpenParen();
          return res;
        }
      } else {
        tokenizer.pushBack();
        return res;
      }

      tokenizer.pushBack();
    }
  }

  private void popOpenParen() throws ParseFailed {
    final Token tkn = pop();

    if (tkn != openParen) {
      throw parseResult.fail("filterSyntax: " +
                                     "Expected openParen on stack." +
                                     " source: " + source);
    }
  }

  private FilterBase makePropFilter(final List<PropertyInfo> pis,
                                    final int oper)
          throws ParseFailed {
    final PropertyInfo pi = pis.get(0);
    FilterBase filter = null;
    final List<PropertyInfoIndex> pixs = new ArrayList<>();
    
    pis.forEach(val -> pixs.add(val.pii));
    
    final boolean exact = (oper != like) && (oper != notLike);

    if (pi.pii == PropertyInfoIndex.ENTITY_TYPE) {
      checkSub(pis, 1);
      return entityFilter(getMatch(oper).getValue());
    }
    
    if (oper == notDefined) {
      checkSub(pis, 2);
      return new PresenceFilter(null, pixs, false,
                                pi.intKey, pi.strKey);
    }

    if (oper == isDefined) {
      // Presence check
      checkSub(pis, 2);
      return new PresenceFilter(null, pixs, true,
                                pi.intKey, pi.strKey);
    }
    

    if (oper == inTimeRange) {
      checkSub(pis, 2);
      return ObjectFilter.makeFilter(null, pixs, getTimeRange(),
                                     pi.intKey, pi.strKey);
    }

    if (pi.pii == PropertyInfoIndex.VIEW) {
      checkSub(pis, 1);
      // expect list of views.
      final ArrayList<String> views = doWordList();

      for (final String view: views) {
        final FilterBase vpf = viewFilter(view);

        filter = and(null, filter, vpf);
      }

      return filter;
    }

    if (pi.pii == PropertyInfoIndex.VPATH) {
      checkSub(pis, 1);
      // expect list of virtual paths.
      final ArrayList<String> vpaths = doWordList();

      for (final String vpath: vpaths) {
        final FilterBase vpf = resolveVpath(vpath);

        filter = and(null, filter, vpf);
      }

      return filter;
    }

    if ((pi.pii == PropertyInfoIndex.CATEGORIES) &&
            (pis.size() == 2)) {
      final PropertyInfo subPi = pis.get(1);

      if (subPi.pii == PropertyInfoIndex.UID) {
        // No match and category - expect list of uids.
        final ArrayList<String> uids = doWordList();

        for (final String uid: uids) {
          final GetEntityResponse<BwCategory> cat = callGetCategory(uid);

          if (cat.isNotFound()) {
            // Deleted category?
            throw parseResult.fail("Category uid references missing category: " + uid +
                                           " Filter will always fail to match");
          }

          if (!cat.isOk()) {
            throw parseResult.fail(cat.toString());
          }

          final ObjectFilter<String> f = new ObjectFilter<String>(null,
                                                                  pixs);

          f.setEntity(uid);

          f.setExact(exact);
          f.setNot(oper == notEqual);

          filter = and(null, filter, f);
        }

        return filter;
      }

      if (subPi.pii == PropertyInfoIndex.HREF) {
        // No match and category - expect list of paths.
        final ArrayList<String> paths = doWordList();

        for (final String path: paths) {
          final ObjectFilter<String> f = new ObjectFilter<String>(null,
                                                                  pixs);
          f.setEntity(path);

          f.setCaseless(false);

          f.setExact(exact);
          f.setNot(oper == notEqual);

          filter = and(null, filter, f);
        }

        return filter;
      }
    }

    if ((pi.pii == PropertyInfoIndex.COLLECTION) ||
            (pi.pii == PropertyInfoIndex.COLPATH)) {
      checkSub(pis, 1);
      final ArrayList<String> paths = doWordList();

      for (final String path: paths) {
        final FilterBase pf = resolveColPath(path, true, true);

        if (pf == null) {
          continue;
        }

        filter = and(null, filter, pf);
      }

      return filter;
    }

    final MatchType match = getMatch(oper);

    if (pi.pii == PropertyInfoIndex.CATEGORIES) {
      checkSub(pis, 1);
      final String val = match.getValue();

      if (val.startsWith("/")) {
        pixs.add(PropertyInfoIndex.HREF);
        // Assume a path match
        final ObjectFilter<String> f = new ObjectFilter<String>(null, 
                                                                pixs);
        f.setEntity(val);

        f.setCaseless(false);

        f.setExact(exact);
        f.setNot(match.getNegateCondition().equals("yes"));

        return f;
      }

      // Try for name

      final BwCategory cat = callGetCategoryByName(val);

      if (cat == null) {
        throw parseResult.fail("Bad property: " +
                                       "category name: " + match.getValue() +
                                       " source: " + source);
      }

      pixs.add(PropertyInfoIndex.UID);
      final ObjectFilter<BwCategory> f = new BwCategoryFilter(null, 
                                                              pixs);

      f.setEntity(cat);

      f.setExact(exact);
      f.setNot(match.getNegateCondition().equals("yes"));

      return f;
    }

    checkSub(pis, 2);
    final ObjectFilter<String> f = new ObjectFilter<>(null, pixs,
                                                      pi.intKey, 
                                                      pi.strKey);
    f.setEntity(match.getValue());

    f.setCaseless(Filters.caseless(match));

    f.setExact(exact);
    f.setNot(match.getNegateCondition().equals("yes"));
    f.setPrefixMatch(match.getPrefixMatch());

    return f;
  }

  /** Check the properties to ensure we have no more than the allowed
   * number. i.e. 1 means no subfields, 2 = only subfield e.g. a.b
   * 3 means 2, a.b.c etc.
   *
   * @param pis list of PropertyInfo
   * @param depth we expect this and nothing else
   * @throws ParseFailed on error
   */
  private void checkSub(final List<PropertyInfo> pis,
                        final int depth) throws ParseFailed {
    if (depth < pis.size()) {
      throw parseResult.fail("Bad Property: " +
                                     listProps(pis) +
                                     " (exceeds allowable depth) source: " + source);
    }
  }

  private String listProps(final List<PropertyInfo> pis) {
    String delim = "";

    final StringBuilder sb = new StringBuilder();

    for (final PropertyInfo pi: pis) {
      sb.append(delim);

      final BwIcalPropertyInfoEntry ipie = BwIcalPropertyInfo.getPinfo(pi.pii);

      if (ipie == null) {
        sb.append("bad-index(").append(pi).append("(");
      } else {
        sb.append(ipie.getJname());
      }
      delim = ".";
    }

    return sb.toString();
  }
  
  private static class MatchType extends TextMatchType {
    protected boolean prefix;

    /**
     *
     * @return boolean true if this is a prefix match.
     */
    public boolean getPrefixMatch() {
      return prefix;
    }

    /**
     *
     * @param val boolean true if this is a prefix match.
     */
    public void setPrefixMatch(final boolean val) {
      prefix = val;
    }
  }

  private MatchType getMatch(final int oper) throws ParseFailed {
    final MatchType tm;

    // Expect a value
    assertString();

    tm = new MatchType();
    tm.setValue(tokenizer.sval);

    if (oper == startsWithOp) {
      tm.setPrefixMatch(true);
      // case sensitive
      tm.setCollation("i;octet");

      return tm;
    }

    if ((oper == notEqual) ||
            (oper == notLike)) {
      tm.setNegateCondition("yes");
    } else {
      tm.setNegateCondition("no");
    }

    if ((oper == notLike) || (oper == like)) {
      // case insensitive
      tm.setCollation("i;ascii-casemap");
    } else {
      // case sensitive
      tm.setCollation("i;octet");
    }

    return tm;
  }

  private FilterBase entityFilter(final String val) throws ParseFailed {
    try {
      return EntityTypeFilter.makeEntityTypeFilter(null, val, false);
    } catch (final Throwable t) {
      throw parseResult.setBfe(new BedeworkException(t));
    }
  }

  private FilterBase viewFilter(final String val) throws ParseFailed {
    final BwView view = callGetView(val);

    if (view == null) {
      throw parseResult.fail("Unknown view: " +
                                     val + " source: " + source);
    }

    FilterBase filter = view.getFilter();

    if (filter != null) {
      return filter;
    }

    for (final String vpath: view.getCollectionPaths()) {
      final FilterBase vpf = resolveVpath(vpath);

      filter = or(filter, vpf);
    }

    final BwViewFilter vf = new BwViewFilter(null);

    vf.setEntity(view);
    vf.setFilter(filter);

    view.setFilter(filter);

    return vf;
  }

  private FilterBase or(final FilterBase of,
                        final FilterBase f) {
    if (of == null) {
      return f;
    }

    if (of instanceof OrFilter) {
      of.addChild(f);
      return of;
    }

    final OrFilter nof = new OrFilter();
    nof.addChild(of);
    nof.addChild(f);

    return nof;
  }

  private FilterBase and(final String name,
                         final FilterBase af,
                         final FilterBase f) {
    final FilterBase res;

    if (af == null) {
      res = f;
    } else {
      if (af instanceof AndFilter) {
        res = af;
      } else {
        res = new AndFilter();
        res.addChild(af);
      }
      res.addChild(f);
    }

    if (name != null) {
      res.setName(name);
    }

    return res;
  }

  /** A virtual path is the apparent path for a user looking at an explorer
   * view of collections.
   *
   * <p>We might have,
   * <pre>
   *    home-->Arts-->Theatre
   * </pre>
   *
   * <p>In reality the Arts collection might be an alias to another alias which
   * is an alias to a collection containing aliases including "Theatre".
   *
   * <p>So the real picture might be something like...
   * <pre>
   *    home-->Arts             (categories="ChemEng")
   *            |
   *            V
   *           Arts             (categories="Approved")
   *            |
   *            V
   *           Arts-->Theatre   (categories="Arts" AND categories="Theatre")
   *                     |
   *                     V
   *                    MainCal
   * </pre>
   * where the vertical links are aliasing. The importance of this is that
   * each alias might introduce another filtering term, the intent of which is
   * to restrict the retrieval to a specific subset. The parenthesized terms
   * represent example filters.
   *
   * <p>The desired filter is the ANDing of all the above.
   *
   * @param  vpath  a String virtual path
   * @return FilterBase object or null for bad path
   * @throws ParseFailed on error
   */
  private FilterBase resolveVpath(final String vpath) throws ParseFailed {
    /* We decompose the virtual path into it's elements and then try to
     * build a sequence of collections that include the aliases and their
     * targets until we reach the last element in the path.
     *
     * We'll assume the path is already normalized and that no "/" are allowed
     * as parts of names.
     *
     * What we're doing here is resolving aliases to aliases and accumulating
     * any filtering that might be in place as a sequence of ANDed terms. For
     * example:
     *
     * /user/eng/Lectures has the filter cat=eng and is aliased to
     * /public/aliases/Lectures which has the filter cat=lectures and is aliased to
     * /public/cals/MainCal
     *
     * We want the filter (cat=eng) & (cat=Lectures) on MainCal.
     *
     * Below, we decompose the virtual path and we save the path to an actual
     * folder or calendar collection.
     */

    final Collection<BwCollection> cols = callDecomposeVirtualPath(vpath);

    if (cols == null) {
      throw parseResult.fail("Bad virtual path: " + vpath);
    }

    FilterBase vfilter = null;
    BwCollection vpathTarget = null;

    for (final BwCollection col: cols) {
      if (debug()) {
        debug("      vpath collection:" + col.getPath());
      }

      if (col.getFilterExpr() != null) {
        if (subParser == null) {
          subParser = callGetParser();
        }

        final ParseResult pr = subParser.parse(col.getFilterExpr(),
                                               false,
                                               col.getPath());
        if (!pr.ok) {
          throw parseResult.fromPr(pr);
        }

        if (pr.filter != null) {
          vfilter = and(null, vfilter, pr.filter);
        }
      }

      if (col.getCollectionInfo().onlyCalEntities ||
              (col.getCalType() == BwCollection.calTypeFolder)) {
        // reached an end point
        vpathTarget = col;
      }
    }

    if (vpathTarget == null) {
      throw parseResult.fail("Bad vpath - no calendar collection" +
                                     " vpath: " + vpath +
                                     " source: " + source);
    }

    final String name;
    if (vpathTarget.getAliasOrigin() != null) {
      name = vpathTarget.getAliasOrigin().getPath();
    } else {
      name = vpathTarget.getPath();
    }

    return and(name,
               vfilter,
               resolveColPath(vpathTarget.getPath(),
                              false, false));
  }

  /**
   *
   * @param path to be resolved
   * @param applyFilter - filter may already have been applied
   * @return filter or null
   * @throws ParseFailed on error
   */
  private FilterBase resolveColPath(final String path,
                                    final boolean applyFilter,
                                    final boolean explicitSelection) throws ParseFailed {
    try {
      return new FilterBuilder(this).buildFilter(path,
                                                 applyFilter,
                                                 explicitSelection);
    } catch (final Throwable t) {
      throw parseResult.setBfe(new BedeworkException(t));
    }
  }

  private TimeRange getTimeRange() throws ParseFailed {
    assertString();
    final String startStr = tokenizer.sval;

    assertToken("to");

    assertString();

    return makeTimeRange(startStr, tokenizer.sval);
  }

  private TimeRange makeTimeRange(final String startStr,
                                  final String endStr) throws ParseFailed {
    try {
      DateTime start = null;
      DateTime end = null;

      if (startStr != null) {
        start = new DateTime(startStr);
      }

      if (endStr != null) {
        end = new DateTime(endStr);
      }

      return new TimeRange(start, end);
    } catch (final Throwable t) {
      throw parseResult.setBfe(new BedeworkException(t));
    }
  }

  private Operator nextOperator() throws ParseFailed {
    int tkn = nextToken("nextOperator(1)");

    if (tkn == '[') {
      return new Operator(indexedOp);
    }

    if (tkn == '=') {
      return new Operator(equal);
    }

    if (tkn == '~') {
      return new Operator(like);
    }

    if (tkn == '!') {
      if (testToken('~')) {
        return new Operator(notLike);
      }

      assertToken('=');
      return new Operator(notEqual);
    }

    if (tkn == '>') {
      tkn = nextToken("nextOperator(2)");

      if (tkn == '=') {
        return new Operator(greaterThanOrEqual);
      }

      tokenizer.pushBack();

      return new Operator(greaterThan);
    }

    if (tkn == '<') {
      tkn = nextToken("nextOperator(3)");

      if (tkn == '=') {
        return new Operator(lessThanOrEqual);
      }

      tokenizer.pushBack();

      return new Operator(lessThan);
    }

    if (tkn != StreamTokenizer.TT_WORD) {
      throw parseResult.fail("Bad operator: " +
                                     tokenizer.sval +
                                     " source: " + source);
    }

    if (tokenizer.sval.equals("in")) {
      return new Operator(inTimeRange);
    }

    if (tokenizer.sval.equals("startsWith")) {
      return new Operator(startsWithOp);
    }

    if (tokenizer.sval.equals("isdefined")) {
      return new Operator(isDefined);
    }

    if (tokenizer.sval.equals("notdefined")) {
      return new Operator(notDefined);
    }

    throw parseResult.fail("Bad operator: " +
                                   tokenizer.sval +
                                   " source: " + source);

  }

  private boolean topLOp() {
    if (stackEmpty()) {
      return false;
    }

    return stack.peek() instanceof LogicalOperator;
  }

  private boolean anding() {
    /* If the stack is empty or it's not a logical operator we start with AND.
     * We can switch on first logical operator
     */

    if (stackEmpty()|| !topLOp()) {
      return true;
    }

    return ((LogicalOperator)stack.peek()).op == andOp;
  }

  private LogicalOperator popLOp() {
    return (LogicalOperator)stack.pop();
  }

  private void assertNotEmpty() throws ParseFailed {
    if (stack.empty()) {
      throw parseResult.fail("Filter Syntax: " +
                                     " source: " + source);
    }
  }

  private boolean stackEmpty() {
    return stack.empty();
  }

  private void push(final Token val) {
    stack.push(val);
  }

  private Token pop() throws ParseFailed {
    assertNotEmpty();
    return stack.pop();
  }

  private void assertFiltersNotEmpty() throws ParseFailed {
    if (filterStack.empty()) {
      throw parseResult.fail("FilterSyntax: " +
                                     " source: " + source);
    }
  }

  private FilterBase popFilters() throws ParseFailed {
    assertFiltersNotEmpty();
    return filterStack.pop();
  }

  private void showStack(final String tr) {
    debug("nextToken(" + tr + "): Parse stack======");
    for (int i = 0; i < stack.size(); i++) {
      debug(stack.elementAt(i).toString());
    }
  }

  private int nextToken(final String tr) throws ParseFailed {
    try {
      final int tkn = tokenizer.next();

      if (!debug()) {
        return tkn;
      }

      showStack(tr);
      //showFilterStack();

      if (tkn == StreamTokenizer.TT_WORD) {
        debug("nextToken(" + tr + ") = word: " + tokenizer.sval);
      } else if (tkn == '\'') {
        debug("nextToken(" + tr + ") = '" + tokenizer.sval + "'");
      } else if (tkn > 0) {
        debug("nextToken(" + tr + ") = " + (char)tkn);
      } else {
        debug("nextToken(" + tr + ") = " + tkn);
      }

      return tkn;
    } catch (final BedeworkException bfe) {
      throw parseResult.setBfe(bfe);
    }
  }
  
  /* Wrappers 
   */

  private void assertString() throws ParseFailed {
    try {
      tokenizer.assertString();
    } catch (final BedeworkException bfe) {
      throw parseResult.setBfe(bfe);
    }
  }

  private void assertToken(final String val) throws ParseFailed {
    try {
      tokenizer.assertToken(val);
    } catch (final BedeworkException bfe) {
      throw parseResult.setBfe(bfe);
    }
  }

  private void assertToken(final int val) throws ParseFailed {
    try {
      tokenizer.assertToken(val);
    } catch (final BedeworkException bfe) {
      throw parseResult.setBfe(bfe);
    }
  }

  private boolean testToken(final String val) throws ParseFailed {
    try {
      return tokenizer.testToken(val);
    } catch (final BedeworkException bfe) {
      throw parseResult.setBfe(bfe);
    }
  }

  private boolean testToken(final int val) throws ParseFailed {
    try {
      return tokenizer.testToken(val);
    } catch (final BedeworkException bfe) {
      parseResult.setBfe(bfe);
      return false;
    }
  }

  private BwCategory callGetCategoryByName(final String name) throws ParseFailed {
    try {
      return getCategoryByName(name);
    } catch (final BedeworkException bfe) {
      throw parseResult.setBfe(bfe);
    }
  }

  private GetEntityResponse<BwCategory> callGetCategory(final String uid) {
    return getCategoryByUid(uid);
  }

  private BwView callGetView(final String path) throws ParseFailed {
    try {
      return getView(path);
    } catch (final BedeworkException bfe) {
      throw parseResult.setBfe(bfe);
    }
  }

  private Collection<BwCollection> callDecomposeVirtualPath(final String vpath)
          throws ParseFailed {
    try {
      return decomposeVirtualPath(vpath);
    } catch (final BedeworkException bfe) {
      throw parseResult.setBfe(bfe);
    }
  }

  private SimpleFilterParser callGetParser() {
    return getParser();
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
