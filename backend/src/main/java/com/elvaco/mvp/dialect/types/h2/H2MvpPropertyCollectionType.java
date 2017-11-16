package com.elvaco.mvp.dialect.types.h2;

import com.elvaco.mvp.dialect.types.MvpPropertyCollectionType;
import java.io.IOException;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import javax.sql.rowset.serial.SerialClob;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.SerializationException;

import com.elvaco.mvp.entity.meteringpoint.MvpPropertyCollection;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static com.elvaco.mvp.utils.Json.OBJECT_MAPPER;

public class H2MvpPropertyCollectionType extends MvpPropertyCollectionType {

  @Override
  public int[] sqlTypes() {
    return new int[]{Types.CLOB};
  }

  @Override
  public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
    Clob value = rs.getClob(names[0]);
    if (value == null) {
      return null;
    }
    try {
      return new MvpPropertyCollection((ObjectNode) OBJECT_MAPPER.readTree(value.getCharacterStream()));
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
    SerialClob clob = new SerialClob(propertyCollection.asJsonString().toCharArray());
    st.setObject(index, clob, Types.CLOB);
  }
}
