package com.elvaco.mvp.entities.user;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Access(AccessType.FIELD)
public class User {

   @Id
   @GeneratedValue
   public Long id;
   public String firstName;
   public String lastName;
   /**
    * Company associated with this user.
    */
   public String company; // TODO : should be an instance of Company once we have such an object!

   public User() {}

   public User(String firstName, String lastName) {
      this.firstName = firstName;
      this.lastName = lastName;
   }
}
