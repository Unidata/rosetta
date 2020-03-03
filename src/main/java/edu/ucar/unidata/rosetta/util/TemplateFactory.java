/*
 * Copyright (c) 2012-2020 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import edu.ucar.unidata.rosetta.domain.RosettaGlobalAttribute;
import edu.ucar.unidata.rosetta.domain.Template;

public class TemplateFactory {

  private static String nameValueSep = ":";
  private static String groupNameSep = "\\.";

  /**
   * Convert a line from a .metadata file into a RosettaGlobalAttribute
   *
   * @param line one entry from a .metadata file
   * @return a RosettaGlobalAttribute representation of a single .metadata file entry
   */
  private static RosettaGlobalAttribute convertMetadataFileLine(String line) {
    String group = "root";
    String name = "";
    String value = "";

    String[] groupNameAndValue = line.split(nameValueSep, 2);
    if (groupNameAndValue.length > 1) {
      value = groupNameAndValue[groupNameAndValue.length - 1].trim();
      String[] groupAndName = groupNameAndValue[0].split(groupNameSep, 2);
      if (groupAndName.length == 2) {
        group = groupAndName[0].trim();
        name = groupAndName[1].trim();
      } else {
        name = groupAndName[0].trim();
      }
    } else {
      // error - no metadata pair found
    }

    return new RosettaGlobalAttribute(name, value, "STRING", group);
  }

  /**
   * Construct a template object from a .metadata file
   *
   * @param metadataFile the .metadata file
   * @return a template representation of the .metadata file
   */
  public static Template makeTemplateFromMetadataFile(Path metadataFile) throws IOException {
    Template template = new Template();
    List<RosettaGlobalAttribute> globalAttrs = new ArrayList<>();

    try (Stream<String> stream = Files.lines(metadataFile)) {
      globalAttrs = stream.map(line -> convertMetadataFileLine(line)).collect(Collectors.toList());

    }

    template.setGlobalMetadata(globalAttrs);
    return template;
  }

  /**
   * Construct a template object from a JSON representation of a template
   *
   * @param jsonFile the json template file
   * @return a template object based on the json file
   */
  public static Template makeTemplateFromJsonFile(Path jsonFile) throws IOException {
    ObjectMapper templateMapper = new ObjectMapper();

    Template template;
    try (FileReader templateFileReader = new FileReader(jsonFile.toFile())) {
      template = templateMapper.readValue(templateFileReader, Template.class);
    }

    return template;
  }

}
