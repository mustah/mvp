package com.elvaco.mvp.dialect.types;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.SerializationException;
import org.postgresql.util.PGobject;

import com.elvaco.mvp.entity.meteringpoint.MvpPropertyCollection;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static com.elvaco.mvp.utils.Json.OBJECT_MAPPER;

public class PostgreSQLMvpPropertyCollectionType extends MvpPropertyCollectionType {

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
      return new MvpPropertyCollection((ObjectNode) OBJECT_MAPPER.readTree(value.getValue()));
    } catch (IOException e) {
      throw new SerializationException(e.getMessage(), e);
    }
  }

  @Override
  public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {
    if (value == null || value.getClass() != MvpPropertyCollection.class) {
      st.setNull(index, Types.OTHER);
      return;
    }
    MvpPropertyCollection propertyCollection = (MvpPropertyCollection) value;
    st.setObject(index, propertyCollection.asJsonString(), Types.OTHER);
  }
}
