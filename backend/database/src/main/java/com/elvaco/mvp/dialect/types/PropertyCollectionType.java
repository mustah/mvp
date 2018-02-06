package com.elvaco.mvp.dialect.types;

import java.io.Serializable;

import com.elvaco.mvp.entity.meter.PropertyCollection;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.hibernate.HibernateException;
import org.hibernate.type.SerializationException;
import org.hibernate.usertype.UserType;
import org.springframework.util.ObjectUtils;

public abstract class PropertyCollectionType implements UserType {

  @Override
  public Class<PropertyCollection> returnedClass() {
    return PropertyCollection.class;
  }

  @Override
  public boolean equals(Object x, Object y) throws HibernateException {
    return ObjectUtils.nullSafeEquals(x, y);
  }

  @Override
  public int hashCode(Object x) throws HibernateException {
    if (x == null) {
      return 0;
    }
    return x.hashCode();
  }

  @Override
  public boolean isMutable() {
    return true;
  }

  @Override
  public Object deepCopy(Object value) throws HibernateException {
    if (value == null) {
      return null;
    }

    if (!(value instanceof PropertyCollection)) {
      return null;
    }

    ObjectNode copyProperties = ((PropertyCollection) value).getJson().deepCopy();
    return new PropertyCollection(copyProperties);
  }

  @Override
  public Serializable disassemble(Object value) throws HibernateException {
    Object copy = deepCopy(value);
    if (copy instanceof Serializable) {
      return (Serializable) value;
    }
    throw new SerializationException(String.format("Cannot serialize '%s', %s is not Serializable"
                                                   + ".", value, value.getClass()), null);
  }

  @Override
  public Object assemble(Serializable cached, Object owner) throws HibernateException {
    return deepCopy(cached);
  }

  @Override
  public Object replace(Object original, Object target, Object owner) throws HibernateException {
    return deepCopy(original);
  }
}
