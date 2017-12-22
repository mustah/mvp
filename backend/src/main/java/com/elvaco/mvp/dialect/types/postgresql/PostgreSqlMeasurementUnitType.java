package com.elvaco.mvp.dialect.types.postgresql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.elvaco.mvp.dialect.types.MeasurementUnitType;
import com.elvaco.mvp.entity.measurement.MeasurementUnit;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.postgresql.util.PGobject;

public class PostgreSqlMeasurementUnitType extends MeasurementUnitType {
  @Override
  public int[] sqlTypes() {
    return new int[] {Types.OTHER};
  }

  @Override
  public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object
    owner) throws HibernateException, SQLException {
    PGobject value = (PGobject) rs.getObject(names[0]);
    if (value == null || value.getValue() == null) {
      return null;
    }
    return new MeasurementUnit(value.getValue());
  }

  @Override
  public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor
    session) throws HibernateException, SQLException {
    if (value == null || value.getClass() != MeasurementUnit.class) {
      st.setNull(index, Types.OTHER);
      return;
    }
    MeasurementUnit measurementUnit = (MeasurementUnit) value;
    st.setObject(index, measurementUnit.toString(), Types.OTHER);
  }
}
