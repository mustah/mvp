package com.elvaco.mvp.database.dialect.types.postgresql;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.elvaco.mvp.database.dialect.types.JsonFieldType;
import com.elvaco.mvp.database.entity.meter.JsonField;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.SerializationException;
import org.postgresql.util.PGobject;

import static com.elvaco.mvp.database.util.Json.OBJECT_MAPPER;

public class PostgreSqlJsonFieldType extends JsonFieldType {

  @Override
  public int[] sqlTypes() {
    return new int[] {Types.OTHER};
  }

  @Override
  public Object nullSafeGet(
    ResultSet rs,
    String[] names,
    SharedSessionContractImplementor session,
    Object owner
  ) throws HibernateException, SQLException {
    PGobject value = (PGobject) rs.getObject(names[0]);
    if (value == null || value.getValue() == null) {
      return null;
    }
    try {
      return new JsonField((ObjectNode) OBJECT_MAPPER.readTree(value.getValue()));
    } catch (IOException e) {
      throw new SerializationException(e.getMessage(), e);
    }
  }

  @Override
  public void nullSafeSet(
    PreparedStatement st,
    Object value,
    int index,
    SharedSessionContractImplementor session
  ) throws HibernateException, SQLException {
    if (value == null || value.getClass() != JsonField.class) {
      st.setNull(index, Types.OTHER);
      return;
    }
    JsonField jsonField = (JsonField) value;
    st.setObject(index, jsonField.asJsonString(), Types.OTHER);
  }
}
