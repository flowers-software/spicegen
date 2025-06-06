package com.flowers.spicegen;

import com.flowers.spicegen.permissions.refs.UserRef;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HashCodeEqualsTest {

  @Test
  void hashCodeEqualsShouldBeEqual() {

    // hashCode and equals should be equal for the same object
    UserRef userRef = UserRef.of("123");
    UserRef userRef2 = UserRef.of("123");
    assertEquals(userRef.hashCode(), userRef2.hashCode(), "Hash codes should be equal");
    assertEquals(userRef, userRef2, "Objects should be equal");
    Set<UserRef> set = Set.of(userRef);
    assertTrue(set.contains(userRef2));
  }

  @Test
  void testHashCodeEqualsShouldNotMatch() {
    assertNotEquals(UserRef.of("123").hashCode(), UserRef.of("321").hashCode(),
        "Hash codes should not be equal");
    assertNotEquals(UserRef.of("321"), UserRef.of("123"), "Objects should not be equal");
  }
}

