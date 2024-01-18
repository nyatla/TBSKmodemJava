# Ubuntu

## Setup

https://qiita.com/fsdg-adachi_h/items/e35d5a237a2ef278ae0b

```
$ sudo add-apt-repository ppa:cwchien/gradle

$ sudo apt update
$ sudo apt install gradle
```

## 

```
$ gradle init

Select type of project to generate:
  1: basic
  2: application
  3: library
  4: Gradle plugin
Enter selection (default: basic) [1..4] 3

Select implementation language:
  1: C++
  2: Groovy
  3: Java
  4: Kotlin
  5: Scala
  6: Swift
Enter selection (default: Java) [1..6] 3

Select build script DSL:
  1: Kotlin
  2: Groovy
Enter selection (default: Kotlin) [1..2] 1

Select test framework:
  1: JUnit 4
  2: TestNG
  3: Spock
  4: JUnit Jupiter
Enter selection (default: JUnit Jupiter) [1..4] 1

...
```

```
./gradlew build
```
