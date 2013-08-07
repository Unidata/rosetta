.. Rosetta documentation master file, created by
   sphinx-quickstart on Sat Aug  3 08:41:51 2013.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

Welcome to Rosetta's documentation!
===================================

.. toctree::
   :maxdepth: 2

Overview
++++++++

The Unidata Data Translation Tool, Rosetta, is a web-based service that
provides an easy, wizard-based interface for data collectors to transform their
datalogger generated ASCII output into Climate and Forecast (CF) compliant
netCDF files, complete with metadata describing what data are contained in the
file, the instruments used to collect the data, and other critical information
that otherwise may be lost in one of many dreaded README files. The use of CF
compliant netCDF files allows the use of standard services to achieve the many
goals of the ACADIS project. However, with the understanding that the
observational community appreciates the ease of use of ASCII files, methods for
transforming the netCDF back into a user defined CSV or spreadsheet format are
also built-in. We anticipate that Rosetta and the associated services will
be of value to a broader community users who have similar needs for
transforming the data they have collected or stored in non-standard formats.

Architecture
++++++++++++

**Front-end user interface:**

* `jQuery <http://jquery.com/>`_ and `Query UI <http://jqueryui.com/>`_ Javascript Library
* `jWizard <https://github.com/dominicbarnes/jWizard>`_ (jQuery Plugin)
* `SlickGrid <https://github.com/mleibman/SlickGrid/wik>`_ (jQuery Plugin)
* `HTML5 <http://dev.w3.org/html5/spec/single-page.html>`_
* Asynchronous `AJAX <http://api.jquery.com/category/ajax/>`_ communication with the back-end (JQuery)

**Back-end:**

* `Spring 3 MVC Framework <http://static.springsource.org/spring/docs/3.1.x/spring-framework-reference/html/mvc.html>`_
* The Common Data Model (CDM) (`netCDF-Java <http://www.unidata.ucar.edu/software/netcdf-java/>`_)
* `CF-1.6 Standard <http://cf-pcmdi.llnl.gov/documents/cf-conventions/1.6/cf-conventions.html>`_

More Information
++++++++++++++++

Rosetta: A white paper on the challenges of sharing observational datasets
(`pdf <http://www.unidata.ucar.edu/software/pzhta/files/rosetta_whitepaper.pdf>`_)


AMS 2013 Presentation on Rosetta\:

| Arms, S. C., J. O. Ganter, J. Weber, and M. K. Ramamurthy, 2013: *A Web-based*
|   *Tool for Translating Unstructured Data from Dataloggers into Standard* 
|   *Formats.* 29th Conference on Environmental Information Processing 
|   Technologies, 93rd AMS Annual Meeting, Austin, TX, J12.3. Available online 
|   at `https://ams.confex.com/ams/93Annual/webprogram/Paper222186.html <https://ams.confex.com/ams/93Annual/webprogram/Paper222186.html>`_

`Presentation <_static/presentations/AMS_2013_pzhta_pptx2pdf.pdf>`_

.. Indices and tables
.. ==================
..
.. * :ref:`genindex`
.. * :ref:`modindex`
.. * :ref:`search`

