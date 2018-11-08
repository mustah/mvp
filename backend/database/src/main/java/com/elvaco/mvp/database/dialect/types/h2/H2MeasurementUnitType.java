package com.elvaco.mvp.database.dialect.types.h2;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.elvaco.mvp.core.domainmodels.MeasurementUnit;
import com.elvaco.mvp.database.dialect.types.MeasurementUnitType;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

public class H2MeasurementUnitType extends MeasurementUnitType {
  @Override
  public int[] sqlTypes() {
    return new int[] {Types.VARCHAR};
  }

  @Override
  public Object nullSafeGet(
    ResultSet rs,
    String[] names,
    SharedSessionContractImplementor session,
    Object owner
  ) throws HibernateException, SQLException {
    String value = rs.getString(names[0]);
    if (value == null) {
      return null;
    }
    return MeasurementUnit.from(value);
  }

  @Override
  public void nullSafeSet(
    PreparedStatement st, Object value, int index, SharedSessionContractImplementor
    session
  ) throws HibernateException, SQLException {
    if (value == null || value.getClass() != MeasurementUnit.class) {
      st.setNull(index, Types.OTHER);
      return;
    }
    MeasurementUnit measurementUnit = (MeasurementUnit) value;
    st.setString(index, measurementUnit.toString());
  }
}
