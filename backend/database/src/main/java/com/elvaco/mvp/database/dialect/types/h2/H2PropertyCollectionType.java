package com.elvaco.mvp.database.dialect.types.h2;

import java.io.IOException;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import javax.sql.rowset.serial.SerialClob;

import com.elvaco.mvp.database.dialect.types.PropertyCollectionType;
import com.elvaco.mvp.database.entity.meter.PropertyCollection;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.SerializationException;

import static com.elvaco.mvp.database.util.Json.OBJECT_MAPPER;

public class H2PropertyCollectionType extends PropertyCollectionType {

  @Override
  public int[] sqlTypes() {
    return new int[] {Types.CLOB};
  }

  @Override
  public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object
    owner) throws HibernateException, SQLException {
    Clob value = rs.getClob(names[0]);
    if (value == null) {
      return null;
    }
    try {
      return new PropertyCollection((ObjectNode) OBJECT_MAPPER.readTree(
        value.getCharacterStream()));
    } catch (IOException e) {
      throw new SerializationException(e.getMessage(), e);
    }
  }

  @Override
  public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor
    session) throws HibernateException, SQLException {
    if (value == null || value.getClass() != PropertyCollection.class) {
      st.setNull(index, Types.OTHER);
      return;
    }
    PropertyCollection propertyCollection = (PropertyCollection) value;
    SerialClob clob = new SerialClob(propertyCollection.asJsonString().toCharArray());
    st.setObject(index, clob, Types.CLOB);
  }
}
