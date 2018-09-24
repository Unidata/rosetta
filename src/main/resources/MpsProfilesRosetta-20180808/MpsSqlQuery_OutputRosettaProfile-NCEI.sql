SELECT
  public.vw_get_profilestructureattributes.profileshortname AS "metadataProfileName",
  public.vw_get_profilestructureattributes.profileversion AS "metadataProfileVersion",
  public.vw_get_profilestructureattributes.attributename AS "attibuteName",
  replace(public.vw_get_profilestructureattributes.attributename, '_', ' ') AS "displayName",
  public.vw_get_profilestructureattributes.attributedescription AS "description",
  public.vw_get_profilestructureattributes.attributevalues AS "exampleValues",
  trim(both public.vw_get_profilestructureattributes.fmt_structuretype) AS "metadataType",
  trim(both public.vw_get_profilestructureattributes.fmt_structurename_short) AS "metadataTypeStructure",
  trim(both public.vw_get_profilestructureattributes."group") AS "metadataGroup",
  public.vw_get_profilestructureattributes.attributetype AS "metadataValueType",
  public.vw_get_profilestructureattributes.attributenecessity AS "complianceLevel"
FROM
  public.vw_get_profilestructureattributes
WHERE
  public.vw_get_profilestructureattributes.idprofile = 4 AND
  public.vw_get_profilestructureattributes.attributename IS NOT NULL
ORDER BY
  public.vw_get_profilestructureattributes."order";