package com.elvaco.mvp.dialect.types.postgresql;

import com.elvaco.mvp.dialect.types.PropertyCollectionType;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.SerializationException;
import org.postgresql.util.PGobject;

import com.elvaco.mvp.entity.meteringpoint.PropertyCollection;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static com.elvaco.mvp.utils.Json.OBJECT_MAPPER;

public class PostgreSQLPropertyCollectionType extends PropertyCollectionType {

  @Override
  public int[] sqlTypes() {
    return new int[]{Types.OTHER};
  }

  @Override
  public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
    PGobject value = (PGobject) rs.getObject(names[0]);
    if (value == null || value.getValue() == null) {
      return null;
    }
    try {
      return new PropertyCollection((ObjectNode) OBJECT_MAPPER.readTree(value.getValue()));
    } catch (IOException e) {
      throw new SerializationException(e.getMessage(), e);
    }
  }

  @Override
  public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {
    if (value == null || value.getClass() != PropertyCollection.class) {
      st.setNull(index, Types.OTHER);
      return;
    }
    PropertyCollection propertyCollection = (PropertyCollection) value;
    st.setObject(index, propertyCollection.asJsonString(), Types.OTHER);
  }
}
