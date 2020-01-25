# Contributing
## Introduction
First off, thank you for considering contributing to Rosetta.
Rosetta is community-driven project, so it's people like you that make Rosetta useful and successful.

Following these guidelines helps to communicate that you respect the time of the developers managing and developing this open source project.
In return, they should reciprocate that respect in addressing your issue, assessing changes, and helping you finalize your pull requests.

We love contributions from community members, just like you!
There are many ways to contribute, from writing tutorial material to improvements to the documentation, submitting bug report and feature requests, or even writing code which can be incorporated into Rosetta for everyone to use.
If you get stuck at any point you can create an [issue on GitHub](https://github.com/Unidata/rosetta/issues).

For more information on contributing to open source projects, [GitHub's own guide](https://guides.github.com/activities/contributing-to-open-source/) is a great starting point.

## Ground Rules
The goal is to maintain a diverse community that's pleasant for everyone.
Please be considerate and respectful of others.
The Rosetta project follows Unidata's [Contributor Code of Conduct](https://github.com/Unidata/.github/blob/master/CODE_OF_CONDUCT.md).
Other items:

* Each pull request should consist of a logical collection of changes.
  You can include multiple bug fixes in a single pull request, but they should be related.
  For unrelated changes, please submit multiple pull requests.
* Do not commit changes to files that are irrelevant to your feature or bugfix (eg: `.gitignore`).
* Be willing to accept criticism and work on improving your code; we don't want to break other users' code, so care must be taken not to introduce bugs.
* Be aware that the pull request review process is not immediate, and is generally proportional to the size of the pull request.
* Follow the Code Style

## Reporting a bug
When creating a new issue, please be as specific as possible.
Include the version of the code you were using, as well as what operating system you are running.
If possible, include complete, minimal example code that reproduces the problem.

## Pull Requests
**Working on your first Pull Request?** You can learn how from this *free* series [How to Contribute to an Open Source Project on GitHub](https://egghead.io/series/how-to-contribute-to-an-open-source-project-on-github)

We love pull requests from everyone.
Fork, then clone the repo:

~~~bash
git clone git@github.com:your-username/rosetta.git
~~~

Make your change.
Add tests for your change (note: still working on good example tests for Rosetta).

Commit the changes you made. 
hris Beams has written a [guide](http://chris.beams.io/posts/git-commit/) on how to write good commit messages.

Push to your fork and [submit a pull request][pr].

[pr]: https://github.com/Unidata/rosetta/compare/

For the Pull Request to be accepted, you need to agree to the [UCAR/Unidata Contributor License Agreement (CLA)](https://www.clahub.com/agreements/Unidata/rosetta).
See [here](https://github.com/Unidata/rosetta/blob/master/CLA.md) for more explanation and rationale behind Rosetta’s CLA.

As part of the Pull Request, be sure to add yourself to the [list of contributors](https://github.com/Unidata/rosetta/blob/master/CONTRIBUTORS.md).
We want to make sure we acknowledge the hard work you've generously contributed here.
Note that the difference between "developer" and "contributor" is in most cases small, as all play an important role in the development of Rosetta.
The people listed  specifically under "developers" have github permission to merge pull requests.

## Code Review
Once you've submitted a Pull Request, at this point you're waiting on us.
You should expect to hear at least a comment within a couple of days.
We may suggest  some changes or improvements or alternatives.
We work on several different projects, and sometimes our response time is slower than we'd like.
Feel free to ping us on the pull request or issue if we've been silent.

Some things that will increase the chance that your pull request is accepted:

* Write tests.
* Apply one of the Google styles for code style issues (note: Rosetta uses four spaces instead of two). 
* Write a [good commit message](http://tbaggery.com/2008/04/19/a-note-about-git-commit-messages.html).

## Style Guidelines
We are using the [https://google.github.io/styleguide/javaguide.html](Google Java Style Guide) with these exceptions and augmentations:

1. The recommended line width is 120, not 100.
   Modern screens are much wider than tall, so having wider lines allows more code to fit on a screen.
1. As a rule, don't add empty javadocs that have no information.
   For example, do not do this:

   ~~~java
   /**
    * _more_
    *
    * @param nds _more_
    * @param v   _more_
    * @return _more_
    * @throws IOException
    */
   protected Foo makeFoo(NetcdfDataset nds, VariableSimpleIF v) throws IOException
   ~~~

   Better to not have any javadoc then to have empty javadoc.
   Of course, best is to put actual, useful comments to help others understand your API.

To assist in following the style, we provide both IntelliJ and Eclipse style files.
These files can be found in the root directory of the repository.
If you are not using an IDE, you can check the style using:

~~~bash
./gradlew spotlessCheck
~~~

and you can apply the style using:

~~~bash
./gradlew spotlessApply
~~~

It can be easy to forget to run this command before pushing your changes to github.
For that, we have created git pre-commit hook scripts for you to use.
The pre-commit hook will run the spotlessApply task before the commit is made, ensuring that you have the proper code format. 
To install the pre-commit hook in a *nix environment (linux, *BSD, MacOS, Cygwin, etc.), copy the file `hooks/pre-commit-nix` to `.git/hooks/pre-commit` and make it executable.
On windows, copy the file `hooks/pre-commit-windows` to `.git/hooks/pre-commit`.