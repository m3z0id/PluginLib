# PluginLib ![maven](https://img.shields.io/github/v/release/Darrionat/PluginLib)

A project that aims to make the creation of plugins a faster and easier process. This project supports Minecraft
1.14-26.x.

## Maven ![maven](https://img.shields.io/github/v/release/Darrionat/PluginLib)

To add this project to your Maven/Gradle project make sure you have the following repository and dependency.

### Repository
#### Maven
```xml

<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```
#### Gradle
##### Groovy
```groovy
repositories {
    maven {
        url = uri("https://jitpack.io")
    }
}
```
##### Kotlin
```kotlin
repositories {
    maven("https://jitpack.io")
}
```

### Dependency
#### Maven
```xml
<dependency>
    <groupId>com.github.darrionat</groupId>
    <artifactId>pluginlib</artifactId>
    <version>version</version>
</dependency>
```
#### Gradle
##### Groovy
```groovy
dependencies {
    implementation 'com.github.darrionat:pluginlib:version'
}
```
##### Kotlin
```kotlin
dependencies {
    implementation("com.github.darrionat:pluginlib:version")
}
```

### Shading
#### Maven
```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>3.1.0</version>
            <configuration>
                <relocations>
                    <relocation>
                        <pattern>me.darrionat.pluginlib</pattern>
                        <!-- Make sure to change the package below -->
                        <shadedPattern>your.plugin.destination</shadedPattern>
                    </relocation>
                </relocations>
            </configuration>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>shade</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```
#### Gradle
First, you need to add the [shadow](https://gradleup.com/shadow/) plugin. Make sure to change `<version>` to the actual version.
##### Groovy
```groovy
plugins {
    id 'com.gradleup.shadow' version '<version>'
}
```
##### Kotlin
```kotlin
plugins {
    id("com.gradleup.shadow") version "<version>"
}
```
Then, you need to relocate this library. Make sure to change `your.plugin.destination` to the actual destination.
##### Groovy
```groovy
shadowJar {
    relocate 'me.darrionat.pluginlib', 'your.plugin.destination'
}
```
##### Kotlin
```kotlin
shadowJar {
    relocate("me.darrionat.pluginlib", "your.plugin.destination")
}
```


## Documentation [![Website](https://img.shields.io/website?label=wiki&url=https%3A%2F%2Fwiki.darrionatplugins.com%2F)](https://wiki.darrionatplugins.com/libraries/pluginlib)

The [Wiki][wiki] provides detailed information about the API. The JavaDocs are also detailed, please read them before
all uses.

<!-- Links -->

[wiki]: https://wiki.darrionatplugins.com/libraries/pluginlib