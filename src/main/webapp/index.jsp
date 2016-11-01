<!DOCTYPE HTML>
<%@ page import="edu.ucar.unidata.rosetta.service.ServerInfoBean" %>
<html>
<head>
    <style>
        h1 {
            font-size: 250%;
        }

        h2 {
            font-size: 200%;
        }

        td {
            text-align: left;
            vertical-align: top;
            padding: 15px;
            font-size: 150%;
        }

        img {
            padding-top: 25pt;
        }
    </style>
    <title>Rosetta</title>
</head>
<body>
<table>
    <tr>
        <td>
            <img src="resources/img/logo/rosetta-150x150.png" alt="Rosetta">
        </td>
        <td>
            <p>
            <h1>Rosetta Landing Page</h1>
            </p>
            <p>
                <b><i>Rosetta is Beta software under active development, use at your own
                    risk.</b></i>
            </p>
            <p>
                Welcome to Rosetta, a data transformation tool. Rosetta is a web-based service that
                provides an easy, wizard-based interface for data collectors to transform their
                datalogger generated ASCII output into Climate and Forecast (CF) compliant netCDF
                files. These files will contain the metadata describing what data is contained in
                the file, the instruments used to collect the data, and other critical information
                that otherwise may be lost in one of many dreaded README files.
            </p>
            <p>
                In addition, with the understanding that the observational community does appreciate
                the ease of use of ASCII files, methods for transforming the netCDF back into a user
                defined CSV or spreadsheet formats are also incorporated into Rosetta.
            </p>
            <p>
                We hope that Rosetta will be of value to the science community users who have needs
                for transforming the data they have collected or stored in non-standard formats.
            </p>
            <p>
                Rosetta is currently under continued further development, and ready for beta
                testing.
            </p>
            <p>
            <h2>What would you like to do?</h2>
            </p>
            <a href="create"><img src="resources/img/add.png" alt="Works!"/></a>
            Create a new template
            <br>
            <a href="restore"><img src="resources/img/add.png" alt="Works!"/></a>
            Upload, modify, and use an existing template
            <br>
            <a href="autoConvert"><img src="resources/img/add.png" alt="Works!"/></a>
            Autoconvert a new data file and transform automatically
            <br>
            <img src="resources/img/remove.png" alt="Future Work."/>
            Upload template and new data file, transform automatically
            </p>
        </td>
    </tr>
    <tr>
        <td colspan="2">
            <i>
                Version : <%=ServerInfoBean.getVersion()%>
                <br>
                Build Date: <%=ServerInfoBean.getBuildDate()%>
            </i>
        </td>
    </tr>
</table>
</body>
</html>
