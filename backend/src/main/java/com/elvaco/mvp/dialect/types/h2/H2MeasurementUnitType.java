package com.elvaco.mvp.dialect.types.h2;

import com.elvaco.mvp.dialect.types.MeasurementUnitType;
import com.elvaco.mvp.entity.measurement.MeasurementUnit;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;

public class H2MeasurementUnitType extends MeasurementUnitType {
  @Override
  public int[] sqlTypes() {
    return new int[] {Types.VARCHAR};
  }

  @Override
  public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object
      owner) throws HibernateException, SQLException {
    String value = rs.getString(names[0]);
    if (value == null) {
      return null;
    }
    return new MeasurementUnit(value);
  }

  @Override
  public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor
      session) throws HibernateException, SQLException {
    if (value == null || value.getClass() != MeasurementUnit.class) {
      st.setNull(index, Types.OTHER);
      return;
    }
    MeasurementUnit measurementUnit = (MeasurementUnit) value;
    st.setString(index, measurementUnit.toString());
  }
}
