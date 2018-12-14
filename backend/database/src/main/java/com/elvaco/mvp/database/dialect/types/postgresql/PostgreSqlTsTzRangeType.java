package com.elvaco.mvp.database.dialect.types.postgresql;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.elvaco.mvp.core.domainmodels.PeriodRange;
import com.elvaco.mvp.database.util.PeriodRangeParser;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;
import org.postgresql.util.PGobject;
import org.springframework.util.ObjectUtils;

@Slf4j
public class PostgreSqlTsTzRangeType implements UserType {
  @Override
  public int[] sqlTypes() {
    return new int[] {Types.OTHER};
  }

  @Override
  public Class<?> returnedClass() {
    return PeriodRange.class;
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
  public Object nullSafeGet(
    ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner
  ) throws HibernateException, SQLException {
    try {
      PGobject value = (PGobject) rs.getObject(names[0]);
      if (value != null && value.getValue() != null) {
        return PeriodRangeParser.parse(value.getValue());
      } else {
        return null;
      }
    } catch (HibernateException | SQLException ex) {
      log.warn("Unable to get value for '{}'.", names, ex);
      throw ex;
    }
  }

  @Override
  public void nullSafeSet(
    PreparedStatement st, Object value, int index, SharedSessionContractImplementor session
  ) throws HibernateException, SQLException {
    if (value == null || value.getClass() != PeriodRange.class) {
      st.setNull(index, Types.OTHER);
      return;
    }
    PeriodRange periodRange = (PeriodRange) value;
    st.setObject(index, PeriodRangeParser.format(periodRange), Types.OTHER);
  }

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
