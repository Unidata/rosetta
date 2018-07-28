package edu.ucar.unidata.rosetta.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class GeneralMetadata {

  private String title;
  private String description;
  private String institution;
  private String dataAuthor;
  private String version;
  private String dataSource;
  private String comment;
  private String history;
  private String references;

  private String anchor_depth_cm;
  private String anchor_dimensions_mm;
  private String anchor_material;
  private String anchor_type;
  private String ancillary_position_datetime;
  private String ancillary_position_deviceid;
  private String ancillary_position_lat;
  private String ancillary_position_lon;
  private String ancillary_position_quality;
  private String ancillary_position_source;
  private String anesthesic_product;
  private String antifouling_product;
  private String antiseptic_product;
  private String attachment_method;
  private String attachment_product;
  private String baitlure_capture;
  private String baitlure_recapture;
  private String calibration_file;
  private String code_map;
  private String condition_capture;
  private String condition_recapture;
  private String cruise_capture;
  private String cruise_recapture;
  private String date_programming;
  private String date_shipment;
  private String datetime_death;
  private String datetime_end;
  private String datetime_release;
  private String days_constantdepth;
  private String days_mission;
  private String depth_m_capture;
  private String depth_m_recapture;
  private String device_name;
  private String device_type;
  private String end_details;
  private String end_type;
  private String fate_recapture;
  private String firmware;
  private String flag_capture;
  private String float_additional;
  private String found_problem;
  private String geolocation_output;
  private String geolocation_parameters;
  private String hook_capture;
  private String hook_recapture;
  private String hours_soaktime_capture;
  private String hours_soaktime_recapture;
  private String implant_numsuture;
  private String interpolation_method;
  private String interpolation_time;
  private String lat_end;
  private String lat_release;
  private String length_capture;
  private String length_method_capture;
  private String length_method_recapture;
  private String length_recapture;
  private String length_type_capture;
  private String length_type_recapture;
  private String length_unit_capture;
  private String length_unit_recapture;
  private String lifestage_capture;
  private String lifestage_recapture;
  private String location_capture;
  private String location_recapture;
  private String locationclass_end;
  private String lon_end;
  private String lon_release;
  private String manufacturer;
  private String method_capture;
  private String method_landed;
  private String method_recapture;
  private String minutes_fighttime_capture;
  private String minutes_fighttime_recapture;
  private String minutes_operation;
  private String minutes_revival;
  private String minutes_summary;
  private String model;
  private String mount_numbolts;
  private String mount_type;
  private String othertags_capture;
  private String owner_contact;
  private String person_angler_capture;
  private String person_owner;
  private String person_programmer;
  private String person_qc;
  private String person_recapture;
  private String person_tagger_capture;
  private String person_tagger_recapture;
  private String ping_code;
  private String problem_affecteddates;
  private String problem_details;
  private String problem_numof;
  private String problem_summary;
  private String programming_report;
  private String programming_software;
  private String project;
  private String ptt;
  private String ptt_hex;
  private String release_forced;
  private String release_method;
  private String retagged_recapture;
  private String school_capture;
  private String school_recapture;
  private String seastate_capture;
  private String seastate_recapture;
  private String seconds_sampling;
  private String seconds_sampling_highfreq;
  private String seconds_writingdata;
  private String serial_number;
  private String set_float_capture;
  private String set_float_recapture;
  private String sex;
  private String speciesTSN_capture;
  private String species_capture;
  private String specs;
  private String station_capture;
  private String station_recapture;
  private String tag_placement;
  private String temp_degC_capture;
  private String temp_degC_recapture;
  private String tether_assembly;
  private String tether_length_cm;
  private String tether_material;
  private String tissue_sample_capture;
  private String tissue_sample_recapture;
  private String vessel_capture;
  private String vessel_recapture;
  private String waypoints_method;
  private String waypoints_software;
  private String waypoints_source;
  private String weight_capture;
  private String weight_method_capture;
  private String weight_method_recapture;
  private String weight_recapture;
  private String weight_type_capture;
  private String weight_type_recapture;
  private String weight_unit_capture;
  private String weight_unit_recapture;
  private String wind_knots_capture;
  private String wind_knots_recapture;

  public String getMethod_landed() {
    return method_landed;
  }

  public void setMethod_landed(String method_landed) {
    method_landed = method_landed;
  }

  public String getAnchor_depth_cm() {
    return anchor_depth_cm;
  }

  public void setAnchor_depth_cm(String anchor_depth_cm) {
    this.anchor_depth_cm = anchor_depth_cm;
  }

  public String getAnchor_dimensions_mm() {
    return anchor_dimensions_mm;
  }

  public void setAnchor_dimensions_mm(String anchor_dimensions_mm) {
    this.anchor_dimensions_mm = anchor_dimensions_mm;
  }

  public String getAnchor_material() {
    return anchor_material;
  }

  public void setAnchor_material(String anchor_material) {
    this.anchor_material = anchor_material;
  }

  public String getAnchor_type() {
    return anchor_type;
  }

  public void setAnchor_type(String anchor_type) {
    this.anchor_type = anchor_type;
  }

  public String getAncillary_position_datetime() {
    return ancillary_position_datetime;
  }

  public void setAncillary_position_datetime(String ancillary_position_datetime) {
    this.ancillary_position_datetime = ancillary_position_datetime;
  }

  public String getAncillary_position_deviceid() {
    return ancillary_position_deviceid;
  }

  public void setAncillary_position_deviceid(String ancillary_position_deviceid) {
    this.ancillary_position_deviceid = ancillary_position_deviceid;
  }

  public String getAncillary_position_lat() {
    return ancillary_position_lat;
  }

  public void setAncillary_position_lat(String ancillary_position_lat) {
    this.ancillary_position_lat = ancillary_position_lat;
  }

  public String getAncillary_position_lon() {
    return ancillary_position_lon;
  }

  public void setAncillary_position_lon(String ancillary_position_lon) {
    this.ancillary_position_lon = ancillary_position_lon;
  }

  public String getAncillary_position_quality() {
    return ancillary_position_quality;
  }

  public void setAncillary_position_quality(String ancillary_position_quality) {
    this.ancillary_position_quality = ancillary_position_quality;
  }

  public String getAncillary_position_source() {
    return ancillary_position_source;
  }

  public void setAncillary_position_source(String ancillary_position_source) {
    this.ancillary_position_source = ancillary_position_source;
  }

  public String getAnesthesic_product() {
    return anesthesic_product;
  }

  public void setAnesthesic_product(String anesthesic_product) {
    this.anesthesic_product = anesthesic_product;
  }

  public String getAntifouling_product() {
    return antifouling_product;
  }

  public void setAntifouling_product(String antifouling_product) {
    this.antifouling_product = antifouling_product;
  }

  public String getAntiseptic_product() {
    return antiseptic_product;
  }

  public void setAntiseptic_product(String antiseptic_product) {
    this.antiseptic_product = antiseptic_product;
  }

  public String getAttachment_method() {
    return attachment_method;
  }

  public void setAttachment_method(String attachment_method) {
    this.attachment_method = attachment_method;
  }

  public String getAttachment_product() {
    return attachment_product;
  }

  public void setAttachment_product(String attachment_product) {
    this.attachment_product = attachment_product;
  }

  public String getBaitlure_capture() {
    return baitlure_capture;
  }

  public void setBaitlure_capture(String baitlure_capture) {
    this.baitlure_capture = baitlure_capture;
  }

  public String getBaitlure_recapture() {
    return baitlure_recapture;
  }

  public void setBaitlure_recapture(String baitlure_recapture) {
    this.baitlure_recapture = baitlure_recapture;
  }

  public String getCalibration_file() {
    return calibration_file;
  }

  public void setCalibration_file(String calibration_file) {
    this.calibration_file = calibration_file;
  }

  public String getCode_map() {
    return code_map;
  }

  public void setCode_map(String code_map) {
    this.code_map = code_map;
  }

  public String getCondition_capture() {
    return condition_capture;
  }

  public void setCondition_capture(String condition_capture) {
    this.condition_capture = condition_capture;
  }

  public String getCondition_recapture() {
    return condition_recapture;
  }

  public void setCondition_recapture(String condition_recapture) {
    this.condition_recapture = condition_recapture;
  }

  public String getCruise_capture() {
    return cruise_capture;
  }

  public void setCruise_capture(String cruise_capture) {
    this.cruise_capture = cruise_capture;
  }

  public String getCruise_recapture() {
    return cruise_recapture;
  }

  public void setCruise_recapture(String cruise_recapture) {
    this.cruise_recapture = cruise_recapture;
  }

  public String getDate_programming() {
    return date_programming;
  }

  public void setDate_programming(String date_programming) {
    this.date_programming = date_programming;
  }

  public String getDate_shipment() {
    return date_shipment;
  }

  public void setDate_shipment(String date_shipment) {
    this.date_shipment = date_shipment;
  }

  public String getDatetime_death() {
    return datetime_death;
  }

  public void setDatetime_death(String datetime_death) {
    this.datetime_death = datetime_death;
  }

  public String getDatetime_end() {
    return datetime_end;
  }

  public void setDatetime_end(String datetime_end) {
    this.datetime_end = datetime_end;
  }

  public String getDatetime_release() {
    return datetime_release;
  }

  public void setDatetime_release(String datetime_release) {
    this.datetime_release = datetime_release;
  }

  public String getDays_constantdepth() {
    return days_constantdepth;
  }

  public void setDays_constantdepth(String days_constantdepth) {
    this.days_constantdepth = days_constantdepth;
  }

  public String getDays_mission() {
    return days_mission;
  }

  public void setDays_mission(String days_mission) {
    this.days_mission = days_mission;
  }

  public String getDepth_m_capture() {
    return depth_m_capture;
  }

  public void setDepth_m_capture(String depth_m_capture) {
    this.depth_m_capture = depth_m_capture;
  }

  public String getDepth_m_recapture() {
    return depth_m_recapture;
  }

  public void setDepth_m_recapture(String depth_m_recapture) {
    this.depth_m_recapture = depth_m_recapture;
  }

  public String getDevice_name() {
    return device_name;
  }

  public void setDevice_name(String device_name) {
    this.device_name = device_name;
  }

  public String getDevice_type() {
    return device_type;
  }

  public void setDevice_type(String device_type) {
    this.device_type = device_type;
  }

  public String getEnd_details() {
    return end_details;
  }

  public void setEnd_details(String end_details) {
    this.end_details = end_details;
  }

  public String getEnd_type() {
    return end_type;
  }

  public void setEnd_type(String end_type) {
    this.end_type = end_type;
  }

  public String getFate_recapture() {
    return fate_recapture;
  }

  public void setFate_recapture(String fate_recapture) {
    this.fate_recapture = fate_recapture;
  }

  public String getFirmware() {
    return firmware;
  }

  public void setFirmware(String firmware) {
    this.firmware = firmware;
  }

  public String getFlag_capture() {
    return flag_capture;
  }

  public void setFlag_capture(String flag_capture) {
    this.flag_capture = flag_capture;
  }

  public String getFloat_additional() {
    return float_additional;
  }

  public void setFloat_additional(String float_additional) {
    this.float_additional = float_additional;
  }

  public String getFound_problem() {
    return found_problem;
  }

  public void setFound_problem(String found_problem) {
    this.found_problem = found_problem;
  }

  public String getGeolocation_output() {
    return geolocation_output;
  }

  public void setGeolocation_output(String geolocation_output) {
    this.geolocation_output = geolocation_output;
  }

  public String getGeolocation_parameters() {
    return geolocation_parameters;
  }

  public void setGeolocation_parameters(String geolocation_parameters) {
    this.geolocation_parameters = geolocation_parameters;
  }

  public String getHook_capture() {
    return hook_capture;
  }

  public void setHook_capture(String hook_capture) {
    this.hook_capture = hook_capture;
  }

  public String getHook_recapture() {
    return hook_recapture;
  }

  public void setHook_recapture(String hook_recapture) {
    this.hook_recapture = hook_recapture;
  }

  public String getHours_soaktime_capture() {
    return hours_soaktime_capture;
  }

  public void setHours_soaktime_capture(String hours_soaktime_capture) {
    this.hours_soaktime_capture = hours_soaktime_capture;
  }

  public String getHours_soaktime_recapture() {
    return hours_soaktime_recapture;
  }

  public void setHours_soaktime_recapture(String hours_soaktime_recapture) {
    this.hours_soaktime_recapture = hours_soaktime_recapture;
  }

  public String getImplant_numsuture() {
    return implant_numsuture;
  }

  public void setImplant_numsuture(String implant_numsuture) {
    this.implant_numsuture = implant_numsuture;
  }

  public String getInterpolation_method() {
    return interpolation_method;
  }

  public void setInterpolation_method(String interpolation_method) {
    this.interpolation_method = interpolation_method;
  }

  public String getInterpolation_time() {
    return interpolation_time;
  }

  public void setInterpolation_time(String interpolation_time) {
    this.interpolation_time = interpolation_time;
  }

  public String getLat_end() {
    return lat_end;
  }

  public void setLat_end(String lat_end) {
    this.lat_end = lat_end;
  }

  public String getLat_release() {
    return lat_release;
  }

  public void setLat_release(String lat_release) {
    this.lat_release = lat_release;
  }

  public String getLength_capture() {
    return length_capture;
  }

  public void setLength_capture(String length_capture) {
    this.length_capture = length_capture;
  }

  public String getLength_method_capture() {
    return length_method_capture;
  }

  public void setLength_method_capture(String length_method_capture) {
    this.length_method_capture = length_method_capture;
  }

  public String getLength_method_recapture() {
    return length_method_recapture;
  }

  public void setLength_method_recapture(String length_method_recapture) {
    this.length_method_recapture = length_method_recapture;
  }

  public String getLength_recapture() {
    return length_recapture;
  }

  public void setLength_recapture(String length_recapture) {
    this.length_recapture = length_recapture;
  }

  public String getLength_type_capture() {
    return length_type_capture;
  }

  public void setLength_type_capture(String length_type_capture) {
    this.length_type_capture = length_type_capture;
  }

  public String getLength_type_recapture() {
    return length_type_recapture;
  }

  public void setLength_type_recapture(String length_type_recapture) {
    this.length_type_recapture = length_type_recapture;
  }

  public String getLength_unit_capture() {
    return length_unit_capture;
  }

  public void setLength_unit_capture(String length_unit_capture) {
    this.length_unit_capture = length_unit_capture;
  }

  public String getLength_unit_recapture() {
    return length_unit_recapture;
  }

  public void setLength_unit_recapture(String length_unit_recapture) {
    this.length_unit_recapture = length_unit_recapture;
  }

  public String getLifestage_capture() {
    return lifestage_capture;
  }

  public void setLifestage_capture(String lifestage_capture) {
    this.lifestage_capture = lifestage_capture;
  }

  public String getLifestage_recapture() {
    return lifestage_recapture;
  }

  public void setLifestage_recapture(String lifestage_recapture) {
    this.lifestage_recapture = lifestage_recapture;
  }

  public String getLocation_capture() {
    return location_capture;
  }

  public void setLocation_capture(String location_capture) {
    this.location_capture = location_capture;
  }

  public String getLocation_recapture() {
    return location_recapture;
  }

  public void setLocation_recapture(String location_recapture) {
    this.location_recapture = location_recapture;
  }

  public String getLocationclass_end() {
    return locationclass_end;
  }

  public void setLocationclass_end(String locationclass_end) {
    this.locationclass_end = locationclass_end;
  }

  public String getLon_end() {
    return lon_end;
  }

  public void setLon_end(String lon_end) {
    this.lon_end = lon_end;
  }

  public String getLon_release() {
    return lon_release;
  }

  public void setLon_release(String lon_release) {
    this.lon_release = lon_release;
  }

  public String getManufacturer() {
    return manufacturer;
  }

  public void setManufacturer(String manufacturer) {
    this.manufacturer = manufacturer;
  }

  public String getMethod_capture() {
    return method_capture;
  }

  public void setMethod_capture(String method_capture) {
    this.method_capture = method_capture;
  }

  public String getMethod_recapture() {
    return method_recapture;
  }

  public void setMethod_recapture(String method_recapture) {
    this.method_recapture = method_recapture;
  }

  public String getMinutes_fighttime_capture() {
    return minutes_fighttime_capture;
  }

  public void setMinutes_fighttime_capture(String minutes_fighttime_capture) {
    this.minutes_fighttime_capture = minutes_fighttime_capture;
  }

  public String getMinutes_fighttime_recapture() {
    return minutes_fighttime_recapture;
  }

  public void setMinutes_fighttime_recapture(String minutes_fighttime_recapture) {
    this.minutes_fighttime_recapture = minutes_fighttime_recapture;
  }

  public String getMinutes_operation() {
    return minutes_operation;
  }

  public void setMinutes_operation(String minutes_operation) {
    this.minutes_operation = minutes_operation;
  }

  public String getMinutes_revival() {
    return minutes_revival;
  }

  public void setMinutes_revival(String minutes_revival) {
    this.minutes_revival = minutes_revival;
  }

  public String getMinutes_summary() {
    return minutes_summary;
  }

  public void setMinutes_summary(String minutes_summary) {
    this.minutes_summary = minutes_summary;
  }

  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public String getMount_numbolts() {
    return mount_numbolts;
  }

  public void setMount_numbolts(String mount_numbolts) {
    this.mount_numbolts = mount_numbolts;
  }

  public String getMount_type() {
    return mount_type;
  }

  public void setMount_type(String mount_type) {
    this.mount_type = mount_type;
  }

  public String getOthertags_capture() {
    return othertags_capture;
  }

  public void setOthertags_capture(String othertags_capture) {
    this.othertags_capture = othertags_capture;
  }

  public String getOwner_contact() {
    return owner_contact;
  }

  public void setOwner_contact(String owner_contact) {
    this.owner_contact = owner_contact;
  }

  public String getPerson_angler_capture() {
    return person_angler_capture;
  }

  public void setPerson_angler_capture(String person_angler_capture) {
    this.person_angler_capture = person_angler_capture;
  }

  public String getPerson_owner() {
    return person_owner;
  }

  public void setPerson_owner(String person_owner) {
    this.person_owner = person_owner;
  }

  public String getPerson_programmer() {
    return person_programmer;
  }

  public void setPerson_programmer(String person_programmer) {
    this.person_programmer = person_programmer;
  }

  public String getPerson_qc() {
    return person_qc;
  }

  public void setPerson_qc(String person_qc) {
    this.person_qc = person_qc;
  }

  public String getPerson_recapture() {
    return person_recapture;
  }

  public void setPerson_recapture(String person_recapture) {
    this.person_recapture = person_recapture;
  }

  public String getPerson_tagger_capture() {
    return person_tagger_capture;
  }

  public void setPerson_tagger_capture(String person_tagger_capture) {
    this.person_tagger_capture = person_tagger_capture;
  }

  public String getPerson_tagger_recapture() {
    return person_tagger_recapture;
  }

  public void setPerson_tagger_recapture(String person_tagger_recapture) {
    this.person_tagger_recapture = person_tagger_recapture;
  }

  public String getPing_code() {
    return ping_code;
  }

  public void setPing_code(String ping_code) {
    this.ping_code = ping_code;
  }

  public String getProblem_affecteddates() {
    return problem_affecteddates;
  }

  public void setProblem_affecteddates(String problem_affecteddates) {
    this.problem_affecteddates = problem_affecteddates;
  }

  public String getProblem_details() {
    return problem_details;
  }

  public void setProblem_details(String problem_details) {
    this.problem_details = problem_details;
  }

  public String getProblem_numof() {
    return problem_numof;
  }

  public void setProblem_numof(String problem_numof) {
    this.problem_numof = problem_numof;
  }

  public String getProblem_summary() {
    return problem_summary;
  }

  public void setProblem_summary(String problem_summary) {
    this.problem_summary = problem_summary;
  }

  public String getProgramming_report() {
    return programming_report;
  }

  public void setProgramming_report(String programming_report) {
    this.programming_report = programming_report;
  }

  public String getProgramming_software() {
    return programming_software;
  }

  public void setProgramming_software(String programming_software) {
    this.programming_software = programming_software;
  }

  public String getProject() {
    return project;
  }

  public void setProject(String project) {
    this.project = project;
  }

  public String getPtt() {
    return ptt;
  }

  public void setPtt(String ptt) {
    this.ptt = ptt;
  }

  public String getPtt_hex() {
    return ptt_hex;
  }

  public void setPtt_hex(String ptt_hex) {
    this.ptt_hex = ptt_hex;
  }

  public String getRelease_forced() {
    return release_forced;
  }

  public void setRelease_forced(String release_forced) {
    this.release_forced = release_forced;
  }

  public String getRelease_method() {
    return release_method;
  }

  public void setRelease_method(String release_method) {
    this.release_method = release_method;
  }

  public String getRetagged_recapture() {
    return retagged_recapture;
  }

  public void setRetagged_recapture(String retagged_recapture) {
    this.retagged_recapture = retagged_recapture;
  }

  public String getSchool_capture() {
    return school_capture;
  }

  public void setSchool_capture(String school_capture) {
    this.school_capture = school_capture;
  }

  public String getSchool_recapture() {
    return school_recapture;
  }

  public void setSchool_recapture(String school_recapture) {
    this.school_recapture = school_recapture;
  }

  public String getSeastate_capture() {
    return seastate_capture;
  }

  public void setSeastate_capture(String seastate_capture) {
    this.seastate_capture = seastate_capture;
  }

  public String getSeastate_recapture() {
    return seastate_recapture;
  }

  public void setSeastate_recapture(String seastate_recapture) {
    this.seastate_recapture = seastate_recapture;
  }

  public String getSeconds_sampling() {
    return seconds_sampling;
  }

  public void setSeconds_sampling(String seconds_sampling) {
    this.seconds_sampling = seconds_sampling;
  }

  public String getSeconds_sampling_highfreq() {
    return seconds_sampling_highfreq;
  }

  public void setSeconds_sampling_highfreq(String seconds_sampling_highfreq) {
    this.seconds_sampling_highfreq = seconds_sampling_highfreq;
  }

  public String getSeconds_writingdata() {
    return seconds_writingdata;
  }

  public void setSeconds_writingdata(String seconds_writingdata) {
    this.seconds_writingdata = seconds_writingdata;
  }

  public String getSerial_number() {
    return serial_number;
  }

  public void setSerial_number(String serial_number) {
    this.serial_number = serial_number;
  }

  public String getSet_float_capture() {
    return set_float_capture;
  }

  public void setSet_float_capture(String set_float_capture) {
    this.set_float_capture = set_float_capture;
  }

  public String getSet_float_recapture() {
    return set_float_recapture;
  }

  public void setSet_float_recapture(String set_float_recapture) {
    this.set_float_recapture = set_float_recapture;
  }

  public String getSex() {
    return sex;
  }

  public void setSex(String sex) {
    this.sex = sex;
  }

  public String getSpeciesTSN_capture() {
    return speciesTSN_capture;
  }

  public void setSpeciesTSN_capture(String speciesTSN_capture) {
    this.speciesTSN_capture = speciesTSN_capture;
  }

  public String getSpecies_capture() {
    return species_capture;
  }

  public void setSpecies_capture(String species_capture) {
    this.species_capture = species_capture;
  }

  public String getSpecs() {
    return specs;
  }

  public void setSpecs(String specs) {
    this.specs = specs;
  }

  public String getStation_capture() {
    return station_capture;
  }

  public void setStation_capture(String station_capture) {
    this.station_capture = station_capture;
  }

  public String getStation_recapture() {
    return station_recapture;
  }

  public void setStation_recapture(String station_recapture) {
    this.station_recapture = station_recapture;
  }

  public String getTag_placement() {
    return tag_placement;
  }

  public void setTag_placement(String tag_placement) {
    this.tag_placement = tag_placement;
  }

  public String getTemp_degC_capture() {
    return temp_degC_capture;
  }

  public void setTemp_degC_capture(String temp_degC_capture) {
    this.temp_degC_capture = temp_degC_capture;
  }

  public String getTemp_degC_recapture() {
    return temp_degC_recapture;
  }

  public void setTemp_degC_recapture(String temp_degC_recapture) {
    this.temp_degC_recapture = temp_degC_recapture;
  }

  public String getTether_assembly() {
    return tether_assembly;
  }

  public void setTether_assembly(String tether_assembly) {
    this.tether_assembly = tether_assembly;
  }

  public String getTether_length_cm() {
    return tether_length_cm;
  }

  public void setTether_length_cm(String tether_length_cm) {
    this.tether_length_cm = tether_length_cm;
  }

  public String getTether_material() {
    return tether_material;
  }

  public void setTether_material(String tether_material) {
    this.tether_material = tether_material;
  }

  public String getTissue_sample_capture() {
    return tissue_sample_capture;
  }

  public void setTissue_sample_capture(String tissue_sample_capture) {
    this.tissue_sample_capture = tissue_sample_capture;
  }

  public String getTissue_sample_recapture() {
    return tissue_sample_recapture;
  }

  public void setTissue_sample_recapture(String tissue_sample_recapture) {
    this.tissue_sample_recapture = tissue_sample_recapture;
  }

  public String getVessel_capture() {
    return vessel_capture;
  }

  public void setVessel_capture(String vessel_capture) {
    this.vessel_capture = vessel_capture;
  }

  public String getVessel_recapture() {
    return vessel_recapture;
  }

  public void setVessel_recapture(String vessel_recapture) {
    this.vessel_recapture = vessel_recapture;
  }

  public String getWaypoints_method() {
    return waypoints_method;
  }

  public void setWaypoints_method(String waypoints_method) {
    this.waypoints_method = waypoints_method;
  }

  public String getWaypoints_software() {
    return waypoints_software;
  }

  public void setWaypoints_software(String waypoints_software) {
    this.waypoints_software = waypoints_software;
  }

  public String getWaypoints_source() {
    return waypoints_source;
  }

  public void setWaypoints_source(String waypoints_source) {
    this.waypoints_source = waypoints_source;
  }

  public String getWeight_capture() {
    return weight_capture;
  }

  public void setWeight_capture(String weight_capture) {
    this.weight_capture = weight_capture;
  }

  public String getWeight_method_capture() {
    return weight_method_capture;
  }

  public void setWeight_method_capture(String weight_method_capture) {
    this.weight_method_capture = weight_method_capture;
  }

  public String getWeight_method_recapture() {
    return weight_method_recapture;
  }

  public void setWeight_method_recapture(String weight_method_recapture) {
    this.weight_method_recapture = weight_method_recapture;
  }

  public String getWeight_recapture() {
    return weight_recapture;
  }

  public void setWeight_recapture(String weight_recapture) {
    this.weight_recapture = weight_recapture;
  }

  public String getWeight_type_capture() {
    return weight_type_capture;
  }

  public void setWeight_type_capture(String weight_type_capture) {
    this.weight_type_capture = weight_type_capture;
  }

  public String getWeight_type_recapture() {
    return weight_type_recapture;
  }

  public void setWeight_type_recapture(String weight_type_recapture) {
    this.weight_type_recapture = weight_type_recapture;
  }

  public String getWeight_unit_capture() {
    return weight_unit_capture;
  }

  public void setWeight_unit_capture(String weight_unit_capture) {
    this.weight_unit_capture = weight_unit_capture;
  }

  public String getWeight_unit_recapture() {
    return weight_unit_recapture;
  }

  public void setWeight_unit_recapture(String weight_unit_recapture) {
    this.weight_unit_recapture = weight_unit_recapture;
  }

  public String getWind_knots_capture() {
    return wind_knots_capture;
  }

  public void setWind_knots_capture(String wind_knots_capture) {
    this.wind_knots_capture = wind_knots_capture;
  }

  public String getWind_knots_recapture() {
    return wind_knots_recapture;
  }

  public void setWind_knots_recapture(String wind_knots_recapture) {
    this.wind_knots_recapture = wind_knots_recapture;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getInstitution() {
    return institution;
  }

  public void setInstitution(String institution) {
    this.institution = institution;
  }

  public String getDataAuthor() {
    return dataAuthor;
  }

  public void setDataAuthor(String dataAuthor) {
    this.dataAuthor = dataAuthor;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getDataSource() {
    return dataSource;
  }

  public void setDataSource(String dataSource) {
    this.dataSource = dataSource;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public String getHistory() {
    return history;
  }

  public void setHistory(String history) {
    this.history = history;
  }

  public String getReferences() {
    return references;
  }

  public void setReferences(String references) {
    this.references = references;
  }

  /**
   * String representation of this Data object.
   *
   * @return The string representation.
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

}
