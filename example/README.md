# Spicegen Example

## Code
See [ExampleTest](./src/test/java/com/flowers/spicegen/example/ExampleTest.java).


## Maven Setup
Example [pom.xml](./pom.xml)
```xml
<project>


    <dependencies>
        <dependency>
            <groupId>com.flowers.spicegen</groupId>
            <artifactId>api</artifactId>
            <version>...</version>
        </dependency>
        <dependency>
            <groupId>com.flowers.spicegen</groupId>
            <artifactId>spicedb-binding</artifactId>
            <version>...</version>
        </dependency>
    </dependencies>

    <!-- ... -->

    <build>
        <plugins>
            <plugin>
                <groupId>com.flowers.spicegen</groupId>
                <artifactId>spicegen-maven-plugin</artifactId>
                <version>${project.version}</version>
                <executions>
                    <execution>
                        <configuration>
                            <schemaPath>${project.basedir}/src/test/resources/files.zed</schemaPath>
                            <packageName>${project.groupId}.permissions</packageName>
                            <outputDirectory>${project.basedir}/target/generated-sources/src/main/java</outputDirectory>
                        </configuration>
                        <goals>
                            <goal>spicegen</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```
