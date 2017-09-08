package com.elvaco.mvp.dialect.types;

import com.elvaco.mvp.entity.meteringpoint.MvpPropertyCollection;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.SerializationException;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class PostgreSQLMvpPropertyCollectionType extends MvpPropertyCollectionType {
  private static final Logger logger = LoggerFactory.getLogger(PostgreSQLMvpPropertyCollectionType.class);
  @Override
  public int[] sqlTypes() {
    return new int[] {Types.OTHER} ;
  }

  @Override
  public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
    PGobject value = (PGobject) rs.getObject(names[0]);
    if (value == null  || value.getValue() == null) {
      return null;
    }
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode json;
    try {
       json = (ObjectNode) mapper.readTree(value.getValue());
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
    st.setObject(index, json, Types.OTHER);
  }
}
