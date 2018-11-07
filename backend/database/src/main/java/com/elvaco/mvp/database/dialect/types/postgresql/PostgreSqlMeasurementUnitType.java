package com.elvaco.mvp.database.dialect.types.postgresql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import javax.annotation.Nullable;

import com.elvaco.mvp.database.dialect.types.MeasurementUnitType;
import com.elvaco.mvp.database.entity.measurement.MeasurementUnit;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.postgresql.util.PGobject;

@Slf4j
public class PostgreSqlMeasurementUnitType extends MeasurementUnitType {

  @Override
  public int[] sqlTypes() {
    return new int[] {Types.OTHER};
  }

  @Nullable
  @Override
  public Object nullSafeGet(
    ResultSet rs,
    String[] names,
    SharedSessionContractImplementor session,
    Object owner
  ) throws HibernateException, SQLException {
    try {
      PGobject value = (PGobject) rs.getObject(names[0]);
      if (value != null && value.getValue() != null) {
        return MeasurementUnit.from(value.getValue());
      }
    } catch (Exception ex) {
      log.warn("Unable to get value for '{}'.", names, ex);
    }
    return null;
  }

  @Override
  public void nullSafeSet(
    PreparedStatement st,
    Object value,
    int index,
    SharedSessionContractImplementor session
  ) throws HibernateException, SQLException {
    if (value == null || value.getClass() != MeasurementUnit.class) {
      st.setNull(index, Types.OTHER);
      return;
    }
    MeasurementUnit measurementUnit = (MeasurementUnit) value;
    st.setObject(index, measurementUnit.toString(), Types.OTHER);
  }
}
