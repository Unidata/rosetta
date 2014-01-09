![Alt Rosetta](http://www.unidata.ucar.edu/images2/logos/rosetta-150x150.png "Rosetta")
=======

Overview
--------

Field data obtained from dataloggers often take the form of comma separated value (CSV) ASCII text files. While ASCII
based data formats have some positive aspects, such as the ability to open them with a text editor or spreadsheet
software and "see" the data effortlessly, there are some drawbacks, especially when viewing the situation through the
lens of data interoperability and stewardship.

Issues regarding ASCII data and their integration, interoperability, and stewardship have become especially urgent for
the NSF-funded next-generation Advanced Cooperative Arctic Data and Information Service (ACADIS) project. The goal of
ACADIS is to allow scientists to more easily access, share, integrate and work with Arctic data spanning multiple
disciplines. These goals become quite challenging when one considers the large number of ASCII datasets that are either
currently part of ACADIS or are being routinely submitted to the project, as those ASCII data are stored in a multitude
of layouts, and nearly all metadata reside in non-standard README files, completely disjointed from the actual data they
describe.

The Unidata Data Transformation Tool, Rosetta, is a web-based service that provides an easy, wizard-based interface for
data collectors to transform their datalogger generated ASCII output into Climate and Forecast (CF) compliant netCDF
files, complete with metadata describing what data are contained in the file, the instruments used to collect the data,
and other critical information that otherwise may be lost in one of many dreaded README files. However, with the
understanding that the observational community appreciates the ease of use of ASCII files, methods for transforming the
netCDF back into a user defined CSV or spreadsheet formats are also built-in. We anticipate that Rosetta and the
associated services will be of value to a broader community users who have similar needs for transforming the data they
have collected or stored in non-standard formats.

Basic Architecture
------------------

Front-end user interface:

* jQuery and jQuery UI Javascript Library
* jWizard (jQuery Plugin)
* SlickGrid (jQuery Plugin)
* HTML5
* Asynchronous AJAX communication with the back-end (JQuery)

Back-end:

* Spring 3 MVC Framework
* The Common Data Model (CDM) (netCDF-Java)
* CF-1.6 Standard

More Information
Rosetta: A white paper on the challenges of sharing observational datasets ([pdf](http://www.unidata.ucar.edu/software/pzhta/files/rosetta_whitepaper.pdf))

AMS 2013 Presentation on Rosetta:

Arms, S. C., J. O. Ganter, J. Weber, and M. K. Ramamurthy, 2013: A Web-based Tool for Translating
Unstructured Data from Dataloggers into Standard Formats. 29th Conference on Environmental Information
Processing Technologies, 93rd AMS Annual Meeting, Austin, TX, J12.3. [Available online at
https://ams.confex.com/ams/93Annual/webprogram/Paper222186.html]
