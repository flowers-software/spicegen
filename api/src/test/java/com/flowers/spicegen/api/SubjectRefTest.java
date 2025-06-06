package com.flowers.spicegen.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class SubjectRefTest {

  @Test
  void ofWithRelation() {
    var user = SubjectRef.ofObjectWithRelation(ObjectRef.of("user", "123"), "reader");

    assertEquals("123", user.id());
    assertEquals("user", user.kind());
    assertEquals("reader", user.relation());
  }

  @Test
  void ofUser() {
    var uuid = UUID.fromString("20162b05-fbc5-4567-853f-7ad90fc29d25");
    var user = SubjectRef.ofObject(ObjectRef.of("user", uuid.toString()));

    assertEquals(user.id(), uuid.toString());
    assertEquals("user", user.kind());
  }

  @Test
  void ofUuid() {
    var namespace = "tenant";
    var uuid = UUID.fromString("c0fe2b05-fbc5-4567-853f-7ad90fc29d25");
    var user = SubjectRef.ofObject(ObjectRef.of(namespace, uuid.toString()));

    assertEquals(user.id(), uuid.toString());
    assertEquals(user.kind(), namespace);
  }

  @Test
  void ofUuid_nullId() {
    assertThrows(
        IllegalArgumentException.class, () -> SubjectRef.ofObject(ObjectRef.of("anotherns", null)));
  }

  @Test
  void of() {
    var namespace = "tenant";
    var id = "9392";
    var user = SubjectRef.ofObject(ObjectRef.of(namespace, id));

    assertEquals(user.id(), id);
    assertEquals(user.kind(), namespace);
  }

  @Test
  void of_nullId() {
    assertThrows(
        IllegalArgumentException.class, () -> SubjectRef.ofObject(ObjectRef.of("somens", null)));
  }

  @Test
  void of_nullNamespace() {
    assertThrows(
        IllegalArgumentException.class, () -> SubjectRef.ofObject(ObjectRef.of(null, "32")));
  }

  @Test
  void equals() {
    var a = SubjectRef.ofObject(ObjectRef.of("a", "1"));
    var b = SubjectRef.ofObject(ObjectRef.of("a", "1"));

    assertEquals(a, b);
  }
}
