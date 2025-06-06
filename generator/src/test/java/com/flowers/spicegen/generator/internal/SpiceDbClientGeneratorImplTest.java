package com.flowers.spicegen.generator.internal;

import com.flowers.spicegen.generator.Options;
import com.flowers.spicegen.model.Schema;
import com.flowers.spicegen.parser.SpiceDbSchemaParser;
import java.nio.file.Path;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SpiceDbClientGeneratorImplTest {

  private static final String SOURCE_DIRECTORY = "./out/src/main/java";
  private static final String SOURCE_PACKAGE_NAME = "com.flowers.spicegen";

  @ParameterizedTest
  @ValueSource(strings = {"files"})
  void test(String schemaName) {

    var generator =
        new SpiceDbClientGeneratorImpl(new Options(SOURCE_DIRECTORY, SOURCE_PACKAGE_NAME));
    var schema = loadSchema(schemaName);
    generator.generate(schema);
  }

  private Schema loadSchema(String name) {

    var astInput = Path.of("./src/test/resources/%s.zed".formatted(name));
    var parser = new SpiceDbSchemaParser();

    return parser.parse(astInput);
  }
}
