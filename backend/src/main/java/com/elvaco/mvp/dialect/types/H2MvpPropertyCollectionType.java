package com.elvaco.mvp.dialect.types;

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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode json;
    try {
      json = (ObjectNode) mapper.readTree(value.getCharacterStream());
    } catch (IOException e) {
      throw new SerializationException(e.getMessage(), e);
    }
    return new MvpPropertyCollection(json);
  }

  @Override
  public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {
    if (value == null || value.getClass() != MvpPropertyCollection.class) {
      st.setNull(index, Types.OTHER);
      return;
    }

    MvpPropertyCollection propertyCollection = (MvpPropertyCollection) value;
    String json = propertyCollection.getJson().toString();
    SerialClob clob = new SerialClob(json.toCharArray());
    st.setObject(index, clob, Types.CLOB);
  }
}
