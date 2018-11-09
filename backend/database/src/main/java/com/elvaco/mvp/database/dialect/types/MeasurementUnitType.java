package com.elvaco.mvp.database.dialect.types;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.elvaco.mvp.core.domainmodels.MeasurementUnit;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;
import org.springframework.util.ObjectUtils;

public abstract class MeasurementUnitType implements UserType {

  @Override
  public abstract int[] sqlTypes();

  @Override
  public Class<?> returnedClass() {
    return MeasurementUnit.class;
  }

  @Override
  public boolean equals(Object x, Object y) throws HibernateException {
    return ObjectUtils.nullSafeEquals(x, y);
  }

  @Override
  public int hashCode(Object x) throws HibernateException {
    if (x == null) {
      return 0;
    }
    return x.hashCode();
  }

  @Override
  public abstract Object nullSafeGet(
    ResultSet rs,
    String[] names,
    SharedSessionContractImplementor session,
    Object owner
  ) throws HibernateException, SQLException;

  @Override
  public abstract void nullSafeSet(
    PreparedStatement st, Object value, int index,
    SharedSessionContractImplementor session
  ) throws HibernateException, SQLException;

  @Override
  public Object deepCopy(Object value) throws HibernateException {
    return value;
  }

  @Override
  public boolean isMutable() {
    return false;
  }

  @Override
  public Serializable disassemble(Object value) throws HibernateException {
    return null;
  }

  @Override
  public Object assemble(Serializable cached, Object owner) throws HibernateException {
    return null;
  }

  @Override
  public Object replace(Object original, Object target, Object owner) throws HibernateException {
    return deepCopy(original);
  }
}
