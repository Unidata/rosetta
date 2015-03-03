Build Rosetta using gradle
==========================

Use the gradle wrapper (`gradlew`, `gradlew.bat`) included in this repository for building Rosetta.

Clean-up previous builds
------------------------
To clean-up any previous builds of rosetta, run:

`./gradle2 clean`

Build and skip tests
--------------------

To compile and package the rosetta war file while skipping the tests, run:

`./gradlew build -x test`

This will result in the creation of the rosetta war file in the build/lib/ directory.

Build the docs
--------------

To build the rosetta documentation, the following packages must be installed on your system:

 * [graphviz](http://www.graphviz.org)
 * [doxygen](http://www.stack.nl/~dimitri/doxygen/)

To install these dependencies on a Mac using [homebrew](http://brew.sh): 

`brew install graphviz doxygen`

change directories to docs and execute:

`bash buildDocs.sh`

Note that the documentation for Rosetta is available online: [http://www.unidata.ucar.edu/software/rosetta/dox/html/](http://www.unidata.ucar.edu/software/rosetta/dox/html/)
