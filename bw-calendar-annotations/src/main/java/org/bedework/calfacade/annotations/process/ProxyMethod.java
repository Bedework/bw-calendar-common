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
package org.bedework.calfacade.annotations.process;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.tools.Diagnostic.Kind;

/** We create a list of these as we process the event. We tie together the
 * setter and getter methods so that only the setter needs to be annotated
 *
 * @author Mike Douglass
 */
public class ProxyMethod extends MethodHandler<ProxyMethod> {
  /**
   * @param env our processing environment
   * @param annUtil useful routines
   * @param d an executable element
   */
  public ProxyMethod(final ProcessingEnvironment env,
                     final AnnUtil annUtil,
                     final ExecutableElement d,
                     final ProcessState pstate) {
    super(env, annUtil, d, pstate);
  }

  /**
   */
  @Override
  public void generateGet() {
    final String typeStr = annUtil.getClassName(returnType);

    /* check the corresponding setter to see if this is immutable */
    if ((setGet != null) && setGet.immutable) {
      annUtil.println("    return ", makeCallGetter("getTarget()"), ";");
      annUtil.prntlns("  }",
                      "");

      return;
    }

    if (!annUtil.isCollection(returnType)) {
      if (!(returnType.getKind().isPrimitive())) {
        annUtil.println("   final var val = ", makeCallGetter("ref"), ";")
               .prntlns("    if (val != null) {",
                        "      return val;",
                        "    }",
                        "")
               .println("    if (", makeGetEmptyFlag("ref"), ") {")
               .prntlns("      return null;",
                        "    }",
                        "");
      }

      annUtil.println("    return ", makeCallGetter("getTarget()"), ";")
             .prntlns("  }",
                      "");

      return;
    }

    annUtil.println("    var c = super.", methName, "();")
           .println("    if (c == null) {")
           .println("      c = new Override", typeStr,
                               "(")
           .println("              BwEvent.ProxiedFieldIndex.pfi",
                               ucFieldName, ",")
           .println("              ref, this) {")
           .println("        public void setOverrideCollection(final ", typeStr, " val) {")
           .println("          ", makeCallSetter("ref", "val"), ";")
           .prntlns("          setChangeFlag(true);",
                    "        }",
                    "")
           .println("        public ", typeStr, " getOverrideCollection() {")
           .println("          return ", makeCallGetter("ref"), ";")
           .prntlns("        }",
                    "")
           .println("        public void copyIntoOverrideCollection() {")
           .println("          final var mstr = getMasterCollection();")
           .println(" ")
           .println("          if (mstr != null) {")
           .println("            final var over = getOverrideCollection();");
    if (cloneForOverride) {
      annUtil.println("            for (final var el: mstr) {")
             .println("              over.add((", cloneElementType, ")el.clone());")
             .println("            }");
    } else {
      annUtil.println("            over.addAll(mstr);");
    }
    annUtil.prntlns("          }",
                    "        }",
                    "");


    /*
     * From ClassType
     * Collection<TypeMirror> getActualTypeArguments()
     * Needed to build TreeSet decl below.
     */
    String typePar = null;
    final var returnEl = annUtil.asTypeElement(returnType);
    if (returnEl.getKind() == ElementKind.CLASS) {
      final var tps =  returnEl.getTypeParameters();

      typePar = tps.getFirst().toString();
    } else if (returnEl.getKind() == ElementKind.INTERFACE) {
      final var tps =  returnEl.getTypeParameters();

      typePar = tps.getFirst().toString();
    } else {
      final Messager msg = env.getMessager();
      msg.printMessage(Kind.WARNING,
                       "Unhandled returnType: " + returnType);
    }

    typePar = AnnUtil.fixName(typePar);

    // XXX Having done all that we didn't use typePar

    annUtil.println("        public ", typeStr, " getMasterCollection() {");
    annUtil.println("          return ", makeCallGetter("getTarget()"), ";");
    annUtil.prntlns("        }",
                    "      };",
                    "");
    annUtil.println("      ", makeCallSetter("super", "c"), ";");
    annUtil.prntlns("    }",
                    "",
                    "    return c;",
                    "  }",
                    "");
  }

  /**
   */
  @Override
  public void generateSet() {
    if (basicType) {
      annUtil.println("    if (", makeCallGetter("ref"), " != val) {");

      if (immutable) {
        annUtil.println("      throw new RuntimeException(\"Immutable\");");
      } else {
        annUtil.println("      ", makeCallSetter("ref", "val"), ";")
               .println("      setChangeFlag(true);");
      }

      annUtil.prntlns("    }",
                      "  }",
                      "");

      return;
    }

    String valName = "val";

    if (annUtil.isCollection(fieldType)) {
      final String fieldTypeStr = annUtil.getClassName(fieldType);
      annUtil.println("    var par = val;")
             .println("    if (val instanceof Override", AnnUtil.nonGeneric(fieldTypeStr),
                                        ") {")
             .println("      par = ((Override", fieldTypeStr, ")val).getOverrideCollection();")
             .println("    }");
      valName = "par";
    }

    annUtil.println("    final int res = doSet(", makeFieldIndex(), ", ",
                                         String.valueOf(immutable), ",")
           .println("                          ", makeCallGetter("getTarget()"), ",")
           .println("                          ", makeCallGetter("ref"), ", " + valName + ");")
           .println("    if (res == setRefNull) {")
           .println("      ", makeCallSetter("ref", null), ";")
           .prntlns("    }",
                    "",
                    "    if (res == setRefVal) {")
           .println("      ", makeCallSetter("ref", valName), ";")
           .prntlns("    }",
                    "  }",
                    "");
  }

  /**
   */
  @Override
  public void generateMethod() {
    env.getMessager()
       .printMessage(Kind.ERROR,
                     "Proxy should only do set/get, found: " +
                             methName);
  }

  private String makeGetEmptyFlag(final String objRef) {
    return new StringBuilder(objRef)
            .append(".")
            .append("getEmptyFlag(")
            .append(makeFieldIndex())
            .append(")")
            .toString();
  }

  private String makeFieldIndex() {
    return "ProxiedFieldIndex.pfi" + ucFieldName;
  }
}
