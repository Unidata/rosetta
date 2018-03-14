# Contributing

## Introduction
First off, thank you for considering contributing to Rosetta. Rosetta is community-driven
project, so it's people like you that make Rosetta useful and successful.

Following these guidelines helps to communicate that you respect the time of the
developers managing and developing this open source project. In return, they
should reciprocate that respect in addressing your issue, assessing changes, and
helping you finalize your pull requests.

We love contributions from community members, just like you! There are many ways
to contribute, from writing tutorial material to improvements
to the documentation, submitting bug report and feature requests, or even writing
code which can be incorporated into Rosetta for everyone to use. If you get stuck at
any point you can create an [issue on GitHub](https://github.com/Unidata/rosetta/issues).

For more information on contributing to open source projects,
[GitHub's own guide](https://guides.github.com/activities/contributing-to-open-source/)
is a great starting point.

## Ground Rules
The goal is to maintain a diverse community that's pleasant for everyone. Please
be considerate and respectful of others. Other items:

* Each pull request should consist of a logical collection of changes. You can
  include multiple bug fixes in a single pull request, but they should be related.
  For unrelated changes, please submit multiple pull requests.
* Do not commit changes to files that are irrelevant to your feature or bugfix
  (eg: .gitignore).
* Be willing to accept criticism and work on improving your code; we don't want
  to break other users' code, so care must be taken not to introduce bugs.
* Be aware that the pull request review process is not immediate, and is
  generally proportional to the size of the pull request.

## Reporting a bug
When creating a new issue, please be as specific as possible. Include the version
of the code you were using, as well as what operating system you are running.
If possible, include complete, minimal example code that reproduces the problem.

## Pull Requests
**Working on your first Pull Request?** You can learn how from this *free* series [How to Contribute to an Open Source Project on GitHub](https://egghead.io/series/how-to-contribute-to-an-open-source-project-on-github)

We love pull requests from everyone. Fork, then clone the repo:

    git clone git@github.com:your-username/rosetta.git

Make your change. Add tests for your change. (Note: still working on good example tests for Rosetta)

Commit the changes you made. Chris Beams has written a [guide](http://chris.beams.io/posts/git-commit/) on how to write good commit messages.

Push to your fork and [submit a pull request][pr].

[pr]: https://github.com/Unidata/rosetta/compare/

For the Pull Request to be accepted, you need to agree to the
[UCAR/Unidata Contributor License Agreement (CLA)](https://www.clahub.com/agreements/Unidata/rosetta).
See [here](https://github.com/Unidata/rosetta/blob/master/CLA.md) for more
explanation and rationale behind Rosetta’s CLA.

As part of the Pull Request, be sure to add yourself to the
[list of contributors](https://github.com/Unidata/rosetta/blob/master/CONTRIBUTORS.md).
We want to make sure we acknowledge the hard work you've generously contributed
here. Note that the difference between "developer" and "contributor" is in most cases
small, as all play an importiant role in the development of Rosetta. The people listed 
specifically under "developers" have github permission to merge pull requests.

## Code Review
Once you've submitted a Pull Request, at this point you're waiting on us. You
should expect to hear at least a comment within a couple of days. We may suggest 
some changes or improvements or alternatives. We work on several different 
projects, and sometimes our response time is slower than we'd like. Feel free to 
ping us on the pull request or issue if we've been silent.

Some things that will increase the chance that your pull request is accepted:

* Write tests.
* Apply one of the Google styles for code style issues (note: Rosetta uses four spaces instead of two). 
* Write a [good commit message](http://tbaggery.com/2008/04/19/a-note-about-git-commit-messages.html).
