# githook-maven-plugin
Maven plugin to configure and install local git hooks

## Protect your VCS
It's always a good idea to check your changes before committing them: run unit tests, perform the build, etc. However, such check-lists may be easily overlooked, especially in big projects. To get rid of the human factor, they should be somehow forced and automated. The best way is to implement such verification on the project infrastructure level. However, sometimes there's no infrastructure or it doesn't allow to implement that. For the latter there are [git client hooks](https://git-scm.com/book/en/v2/Customizing-Git-Git-Hooks).

## Listen to your VCS
Besides pre-commit and pre-push hooks there are some others which allow to handle local VCS events.

## Drawbacks of local git hooks
The main disadvantage of this approach is that hooks are kept within .git directory, which shall never come to the remote repository. Thus, each contributor will have to install them manually in his local repository, which may, again, be overlooked.

## So why should I use this plugin?
Because it deals with the problem of providing hook configuration to the repository, and automates their installation.

## Implementation
The idea is to keep somewhere a mapping between the hook name and the script, for each hook name create a respective file in .git/hooks, containing that script when the project initializes. "Initializes" -- is quite a polymorphic term, but when it's a maven project, then it likely means initial [lifecycle phase](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html). In the majority of cases, it will be enough to map the plugin on "initialize" phase, but you can still [create any other custom execution](https://maven.apache.org/guides/mini/guide-configuring-plugins.html#Using_the_executions_Tag).

## Flaws
Obviously, nothing can restrain one from the cloning of repository, and interacting with it without initial build. Also, it's always possible to delete hook files manually.

## Usage
The plugin provides the only goal "install". It's mapped on "initialize" phase by default. To use the default flow add these lines to the plugin definition:
```
<executions>
    <execution>
        <goals>
            <goal>install</goal>
        </goals>
    </execution>
</executions>
```
To configure hooks provide the following configuration for the execution:
```
<hooks>
  <hook-name>script</hook-name>
  ...
</hooks>
```
NOTE: The plugin rewrites hooks.

## Usage Example
```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>org.sandbox</groupId>
    <artifactId>githook-test</artifactId>
    <version>1.0.0</version>
    <build>
        <plugins>
            <plugin>
                <groupId>org.sandbox</groupId>
                <artifactId>githook-maven-plugin</artifactId>
                <version>1.0.0</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>install</goal>
                        </goals>
                        <configuration>
                            <hooks>
                                <pre-commit>
                                    echo running validation build
                                    exec mvn clean install
                                </pre-commit>
                            </hooks>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```
