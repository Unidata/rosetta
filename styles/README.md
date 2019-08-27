# Code Style

Rosetta intends to follow the Google Java code styles, as outlined at <https://google.github.io/styleguide/javaguide.html>, with a few exceptions:

1. Column widths are set to 120, not 100
2. Comment lines will not be joined to try to reach the 120 column width

We provide styles for both IntelliJ (intellij-style-guide.xml) and Eclipse (eclipse-style-guide.xml), which are based off of the Google Style files located at <https://github.com/google/styleguide>, with the two modification above.
This is the same style used by the other Java based THREDDS projects (i.e. netCDF-Java, THREDDS Data Server).

For those not using an IDE, we use the [Spotless](https://github.com/diffplug/spotless) gradle plugin in conjunction with the Rosetta Eclipse style file.
To check the style from the command line, run:

~~~bash
./gradlew spotlessCheck
~~~

Spotless will tell you if style issues are found.
To fix style issues, one can run:

~~~bash
./gradlew spotlessApply
~~~