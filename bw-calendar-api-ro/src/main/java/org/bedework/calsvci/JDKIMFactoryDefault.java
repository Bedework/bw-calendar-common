package org.bedework.calsvci;

import org.bedework.base.exc.BedeworkException;

import org.apache.james.jdkim.api.JDKIM;

public class JDKIMFactoryDefault implements JDKIMFactory {
  private static final String defaultJDKIMClass = "org.apache.james.jdkim.api.JDKIM";

  @Override
  public JDKIM getJDKIMImpl() {
    /* should determine the class from configs and load it.
     */
    return (JDKIM)loadInstance(
            Thread.currentThread().getContextClassLoader(),
            defaultJDKIMClass,
            JDKIM.class);
  }

  private static Object loadInstance(final ClassLoader loader,
                                     final String cname,
                                     final Class<?> interfaceClass) {
    try {
      final Class<?> cl = loader.loadClass(cname);

      if (cl == null) {
        throw new BedeworkException("Class " + cname + " not found");
      }

      final Object o = cl.getDeclaredConstructor().newInstance();

      if (!interfaceClass.isInstance(o)) {
        throw new BedeworkException("Class " + cname +
                                             " is not a subclass of " +
                                             interfaceClass.getName());
      }

      return o;
    } catch (final Throwable t) {
      t.printStackTrace();
      throw new BedeworkException(t);
    }
  }
}
