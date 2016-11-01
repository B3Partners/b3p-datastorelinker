package nl.b3p.geotools.data.linker;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import nl.b3p.datastorelinker.entity.Inout;
import nl.b3p.geotools.data.linker.blocks.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Create actionBlocks for given Classname and parameters (properties)
 *
 * @author Boy de Wit
 */
public class ActionFactory {

    public static final String ATTRIBUTE_ID = "attribute_id";
    public static final String ATTRIBUTE_ID_X = "attribute_id_x";
    public static final String ATTRIBUTE_ID_Y = "attribute_id_y";
    public static final String ATTRIBUTE_CLASS = "attribute_class";
    public static final String NEW_ATTRIBUTE_CLASS = "new_attribute_class";
    public static final String ATTRIBUTE_NAME = "attribute_name";
    public static final String ATTRIBUTE_NAME_X = "attribute_name_x";
    public static final String ATTRIBUTE_NAME_Y = "attribute_name_y";
    public static final String NEW_ATTRIBUTE_NAME = "new_attribute_name";
    public static final String NEW_TYPENAME = "new_typename";
    public static final String PARAMS = "params";
    public static final String APPEND = "append";
    public static final String DROPFIRST = "drop";
    //Property may be true or false. Set to true if you want to polygonize all the lines in the RESULTING LINE TABLE
    public static final String POLYGONIZE = "polygonize";
    //Set the attribute which you want to use to classificate the lines. Lines with same value will be polygonized.
    public static final String POLYGONIZE_CLASSIFICATION_ATTRIBUTE = "polygonize_classification_attribute";
    //Set the beginning of the value that is used to classificate the line. (substring begin)
    public static final String POLYGONIZE_CLASSIFICATION_BEGIN = "polygonize_classification_begin";
    //Set the end of the value that is used to classificate the line. (substring end)
    public static final String POLYGONIZE_CLASSIFICATION_END = "polygonize_classification_end";
    //Default: true. Set to true if you want load every classification seperate in the memory. (takes longer, but less mem usages)
    public static final String POLYGONIZE_ONECLASSINMEMORY = "polygonize_oneClassInMemory";
    public static final String POLYGONIZEWITHATTR = "polygonizeWithAttr";
    public static final String POLYGONIZEWITHATTR_CQLFILTER_ATTRIBUTE = "polygonizeWithAttr_cqlfilter";
    public static final String POLYGONIZEWITHATTR_ATTRIBUTEFEATURENAME_ATTRIBUTE = "polygonizeWithAttr_attributeFeatureName";
    public static final String POLYGONIZEWITHATTR_LINEFEATURENAME_ATTRIBUTE = "polygonizeWithAttr_lineFeatureName";
    public static final String POLYGONIZEWITHATTR_LINECLOSETOLERANCE_ATTRIBUTE = "polygonizeWithAttr_lineCloseTolerance";
    public static final String POLYGONIZESUFLKI = "polygonizeSufLki";
    public static final String POSTPOINTWITHINPOLYGON = "postPointWithinPolygon";
    public static final String TRYCAST = "trycast";
    public static final String OBJECT_FIND = "object_find";
    public static final String OBJECT_REPLACE = "object_replace";
    public static final String BUFFERSIZE = "buffersize";
    public static final String UPPERCASE = "uppercase";
    public static final String BEGIN_INDEX = "beginIndex";
    public static final String END_INDEX = "endIndex";
    public static final String COMPARE_TYPE = "compare";
    public static final String LENGTH = "length";
    public static final String REVERSE = "reverse";
    public static final String SRS = "srs";
    public static final String URL = "url";
    public static final String SKIPFAILURES = "skip_failures";
    public static final String REGEX = "regex";
    public static final String REPLACEMENT = "replacement";
    public static final String SCALE = "scale";
    public static final String FLOAT_PRECISION = "float_precision";
    public static final String PROPERTY_NAME1 = "property_name1";
    public static final String PROPERTY_NAME2 = "property_name2";
    public static final String PROPERTY_NAME3 = "property_name3";
    public static final String PROPERTY_NAME4 = "property_name4";
    public static final String PROPERTY_NAME5 = "property_name5";
    public static final String PROPERTY_NAME6 = "property_name6";
    public static final String PROPERTY_NAME7 = "property_name7";
    public static final String PROPERTY_NAME8 = "property_name8";
    public static final String PROPERTY_NAME9 = "property_name9";
    public static final String PROPERTY_NAME10 = "property_name10";
    public static final String PROPERTY_VALUE1 = "property_value1";
    public static final String PROPERTY_VALUE2 = "property_value2";
    public static final String PROPERTY_VALUE3 = "property_value3";
    public static final String PROPERTY_VALUE4 = "property_value4";
    public static final String PROPERTY_VALUE5 = "property_value5";
    public static final String PROPERTY_VALUE6 = "property_value6";
    public static final String PROPERTY_VALUE7 = "property_value7";
    public static final String PROPERTY_VALUE8 = "property_value8";
    public static final String PROPERTY_VALUE9 = "property_value9";
    public static final String PROPERTY_VALUE10 = "property_value10";
    public static final String ATTRIBUTE_NAME_ADDRESS1 = "attribute_name_address1";
    public static final String ATTRIBUTE_NAME_ADDRESS2 = "attribute_name_address2";
    public static final String ATTRIBUTE_NAME_ADDRESS3 = "attribute_name_address3";
    public static final String ATTRIBUTE_NAME_CITY = "attribute_name_city";
    public static final String ATTRIBUTE_NAME_OTHER_FILE_NAME = "attribute_name_other_file_name";
    public static final String ATTRIBUTE_NAME_DXF_HANDLE = "attribute_name_dxf_handle";
    public static final String ATTRIBUTE_NAME_OTHER_FILE_HANDLE = "attribute_name_other_file_handle";
    public static final String ATTRIBUTE_NAME_OUTPUTDB_ID = "attribute_name_outputdb_id";
    public static final String ATTRIBUTE_NAME_OUTPUT_GEOM_COLUMN = "attribute_name_output_geom_column";
    public static final String ATTRIBUTE_NAME_POLY_TABLE = "attribute_name_poly_table";
    public static final String ATTRIBUTE_NAME_MATCH_SRC_COLUMN = "attribute_name_match_src_column";
    public static final String ATTRIBUTE_NAME_MATCH_POLY_COLUMN = "attribute_name_match_poly_column";
    public static final String ATTRIBUTE_NAME_MATCH_GEOM = "attribute_name_match_geom";
    public static final String ATTRIBUTE_NAME_FILTER_COLUMN = "attribute_name_filter_column";
    public static final String ATTRIBUTE_NAME_FILTER_OPERATOR = "attribute_name_filter_operator";
    public static final String ATTRIBUTE_NAME_FILTER_VALUE = "attribute_name_filter_value";
    public static final String DATE_FORMAT = "date_format";
    public static final Log log = LogFactory.getLog(DataStoreLinker.class);

    public static Action createAction(String actionClassName, Map<String, Object> properties) throws Exception {
        //if its a writer action:
        if (isThisClass(actionClassName, ActionCombo_GeometrySplitter_Writer.class)
                || isThisClass(actionClassName, ActionCombo_GeometrySingle_Writer.class)
                || isThisClass(actionClassName, ActionDataStore_Writer.class)) {

            Map params = null;

            if (propertyCheck(properties, PARAMS)) {
                params = (Map) properties.get(PARAMS);
            }
            /**
             * Create ActionCombo_GeometrySplitter_Writer
             */
            if (isThisClass(actionClassName, ActionCombo_GeometrySplitter_Writer.class)) {
                if (params != null) {
                    return new ActionCombo_GeometrySplitter_Writer(params, properties);
                } else {
                    failedConstructor(ActionCombo_GeometrySplitter_Writer.class, properties);
                }
            } else if (isThisClass(actionClassName, ActionCombo_GeometrySingle_Writer.class)) {
                if (params != null) {
                    return new ActionCombo_GeometrySingle_Writer(params, properties);

                } else {
                    failedConstructor(ActionCombo_GeometrySingle_Writer.class, properties);
                }
            } else if (isThisClass(actionClassName, ActionDataStore_Writer.class)) {
                if (params != null) {
                    return new ActionDataStore_Writer(params, properties);
                } else {
                    failedConstructor(ActionDataStore_Writer.class, properties);
                }
                /**
                 * Create ActionFeatureType_AttributeType_Add
                 */
            }
        } else {
            if (isThisClass(actionClassName, ActionCombo_Recommended_Pro.class)) {
                if (propertyCheck(properties, NEW_TYPENAME, APPEND)) {
                    String new_typename = (String) properties.get(NEW_TYPENAME);
                    boolean append = toBoolean((String) properties.get(APPEND));
                    return new ActionCombo_Recommended_Pro(new_typename, append);

                } else {
                    failedConstructor(ActionCombo_GeometrySingle_Writer.class, properties);
                }
            } else if (isThisClass(actionClassName, ActionCombo_Recommended.class)) {
                return new ActionCombo_Recommended();


                /**
                 * Create ActionDataStore_Writer
                 */
            } else if (isThisClass(actionClassName, ActionFeatureType_AttributeType_Add.class)) {
                if (propertyCheck(properties, NEW_ATTRIBUTE_NAME, ATTRIBUTE_CLASS, ATTRIBUTE_ID)) {
                    String attributeName = (String) properties.get(NEW_ATTRIBUTE_NAME);
                    Class attributeClass = (Class) toClass((String) properties.get(ATTRIBUTE_CLASS));
                    int attributeID = toInteger((String) properties.get(ATTRIBUTE_ID));
                    return new ActionFeatureType_AttributeType_Add(attributeID, attributeName, attributeClass);

                } else if (propertyCheck(properties, NEW_ATTRIBUTE_NAME, ATTRIBUTE_CLASS)) {
                    String attributeName = (String) properties.get(NEW_ATTRIBUTE_NAME);
                    Class attributeClass = (Class) toClass((String) properties.get(ATTRIBUTE_CLASS));
                    return new ActionFeatureType_AttributeType_Add(attributeName, attributeClass);

                } else {
                    failedConstructor(ActionFeatureType_AttributeType_Add.class, properties);
                }



                /**
                 * Create ActionFeatureType_AttributeType_Remove
                 */
            } else if (isThisClass(actionClassName, ActionFeatureType_AttributeType_Remove.class)) {
                if (propertyCheck(properties, ATTRIBUTE_ID)) {
                    int attributeID = toInteger((String) properties.get(ATTRIBUTE_ID));
                    return new ActionFeatureType_AttributeType_Remove(attributeID);

                } else if (propertyCheck(properties, ATTRIBUTE_NAME)) {
                    String attributeName = (String) properties.get(ATTRIBUTE_NAME);
                    return new ActionFeatureType_AttributeType_Remove(attributeName);

                } else {
                    failedConstructor(ActionFeatureType_AttributeType_Remove.class, properties);
                }




                /**
                 * Create ActionFeatureType_AttributeType_Restriction
                 */
            } else if (isThisClass(actionClassName, ActionFeatureType_AttributeType_Restriction.class)) {
                if (propertyCheck(properties, ATTRIBUTE_ID, LENGTH)) {
                    int attributeID = toInteger((String) properties.get(ATTRIBUTE_ID));
                    int length = toInteger((String) properties.get(LENGTH));

                    return new ActionFeatureType_AttributeType_Restriction(attributeID, length);

                } else if (propertyCheck(properties, ATTRIBUTE_NAME, LENGTH)) {
                    String attributeName = (String) properties.get(ATTRIBUTE_NAME);
                    int length = toInteger((String) properties.get(LENGTH));

                    return new ActionFeatureType_AttributeType_Restriction(attributeName, length);

                } else {
                    failedConstructor(ActionFeatureType_AttributeType_Restriction.class, properties);
                }



                /**
                 * Create ActionFeatureType_Replace_Class
                 */
            } else if (isThisClass(actionClassName, ActionFeatureType_Replace_Class.class)) {
                if (propertyCheck(properties, ATTRIBUTE_NAME, NEW_ATTRIBUTE_CLASS, TRYCAST)) {
                    String attributeName = (String) properties.get(ATTRIBUTE_NAME);
                    Class newAttributeClass = toClass((String) properties.get(NEW_ATTRIBUTE_CLASS));
                    boolean tryCast = toBoolean((String) properties.get(TRYCAST));
                    return new ActionFeatureType_Replace_Class(attributeName, newAttributeClass, tryCast);

                } else if (propertyCheck(properties, ATTRIBUTE_ID, NEW_ATTRIBUTE_CLASS, TRYCAST)) {
                    int attributeID = toInteger((String) properties.get(ATTRIBUTE_ID));
                    Class newAttributeClass = (Class) toClass((String) properties.get(NEW_ATTRIBUTE_CLASS));
                    boolean tryCast = toBoolean((String) properties.get(TRYCAST));
                    return new ActionFeatureType_Replace_Class(attributeID, newAttributeClass, tryCast);

                } else if (propertyCheck(properties, NEW_ATTRIBUTE_CLASS, TRYCAST)) {
                    Class newAttributeClass = (Class) toClass((String) properties.get(NEW_ATTRIBUTE_CLASS));
                    boolean tryCast = toBoolean((String) properties.get(TRYCAST));
                    return new ActionFeatureType_Replace_Class(newAttributeClass, tryCast);

                } else {
                    failedConstructor(ActionFeatureType_Replace_Class.class, properties);
                }

                /**
                 * Create ActionFeatureType_Replace_Class_All
                 */
            } else if (isThisClass(actionClassName, ActionFeatureType_Replace_Class_All.class)) {
                if (propertyCheck(properties, ATTRIBUTE_CLASS, NEW_ATTRIBUTE_CLASS, TRYCAST)) {
                    Class attributeClass = toClass((String) properties.get(ATTRIBUTE_CLASS));
                    Class newAttributeClass = toClass((String) properties.get(NEW_ATTRIBUTE_CLASS));
                    boolean tryCast = toBoolean((String) properties.get(TRYCAST));
                    return new ActionFeatureType_Replace_Class_All(attributeClass, newAttributeClass, tryCast);

                } else if (propertyCheck(properties, ATTRIBUTE_CLASS, NEW_ATTRIBUTE_CLASS)) {
                    Class attributeClass = toClass((String) properties.get(ATTRIBUTE_CLASS));
                    Class newAttributeClass = toClass((String) properties.get(NEW_ATTRIBUTE_CLASS));
                    return new ActionFeatureType_Replace_Class_All(attributeClass, newAttributeClass, false);

                } else {
                    failedConstructor(ActionFeatureType_Replace_Class_All.class, properties);
                }

                /**
                 * Create ActionFeatureType_AttributeName_Rename
                 */
            } else if (isThisClass(actionClassName, ActionFeatureType_AttributeName_Rename.class)) {
                if (propertyCheck(properties, ATTRIBUTE_NAME, NEW_ATTRIBUTE_NAME)) {
                    String attributeName = (String) properties.get(ATTRIBUTE_NAME);
                    String newAttributeName = (String) properties.get(NEW_ATTRIBUTE_NAME);
                    return new ActionFeatureType_AttributeName_Rename(attributeName, newAttributeName);

                } else if (propertyCheck(properties, ATTRIBUTE_ID, NEW_ATTRIBUTE_NAME)) {
                    int attributeID = toInteger((String) properties.get(ATTRIBUTE_ID));
                    String newAttributeName = (String) properties.get(NEW_ATTRIBUTE_NAME);
                    return new ActionFeatureType_AttributeName_Rename(attributeID, newAttributeName);

                } else {
                    failedConstructor(ActionFeatureType_AttributeName_Rename.class, properties);
                }



                /**
                 * Create ActionFeatureType_Typename_Update
                 */
            } else if (isThisClass(actionClassName, ActionFeatureType_Typename_Update.class)) {
                if (propertyCheck(properties, NEW_TYPENAME, APPEND)) {
                    String newTypeName = (String) properties.get(NEW_TYPENAME);
                    boolean append = toBoolean(properties.get(APPEND).toString());
                    return new ActionFeatureType_Typename_Update(newTypeName, append);

                } else if (propertyCheck(properties, NEW_TYPENAME)) {
                    String newTypeName = (String) properties.get(NEW_TYPENAME);
                    return new ActionFeatureType_Typename_Update(newTypeName);

                } else {
                    failedConstructor(ActionFeatureType_Typename_Update.class, properties);
                }



                /**
                 * Create ActionFeature_Value_Replace
                 * As implied in DSL blocks, only works for replacing text, not other data types
                 */
            } else if (isThisClass(actionClassName, ActionFeature_Value_Replace.class)) {
                if (propertyCheck(properties, OBJECT_FIND, OBJECT_REPLACE)) {
                    
                    // the objects are already Strings, no need to cast
                    Object find = properties.get(OBJECT_FIND);
                    Object replace = properties.get(OBJECT_REPLACE);
                    return new ActionFeature_Value_Replace(find, replace);

                } else {
                    failedConstructor(ActionFeature_Value_Replace.class, properties);
                }



                /**
                 * Create ActionFeature_Value_Set
                 */
            } else if (isThisClass(actionClassName, ActionFeature_Value_Set.class)) {
                if (propertyCheck(properties, ATTRIBUTE_NAME, OBJECT_REPLACE, APPEND)) {
                    String attributeName = (String) properties.get(ATTRIBUTE_NAME);

                    Object replace = null;
                    Object obj = properties.get(OBJECT_REPLACE);
                    Map map = new HashMap();

                    if (obj instanceof String) {
                        map.put("class", "java.lang.String");
                        map.put("value", (String) obj);
                        replace = createObject(map);
                    } else if (obj instanceof Integer) {
                        map.put("class", "java.lang.Integer");
                        map.put("value", (Integer) obj);
                        replace = createObject(map);
                    } else if (obj instanceof Float) {
                        map.put("class", "java.lang.Float");
                        map.put("value", (Float) obj);
                        replace = createObject(map);
                    } else if (obj instanceof Double) {
                        map.put("class", "java.lang.Double");
                        map.put("value", (Double) obj);
                        replace = createObject(map);
                    } else {
                        Map newValue = (HashMap) obj;
                        replace = createObject(newValue);
                    }

                    boolean append = toBoolean((String) properties.get(APPEND));
                    return new ActionFeature_Value_Set(attributeName, replace, append);

                } else if (propertyCheck(properties, ATTRIBUTE_ID, OBJECT_REPLACE, APPEND)) {
                    int attributeID = toInteger((String) properties.get(ATTRIBUTE_ID));
                    Object replace = createObject((HashMap) properties.get(OBJECT_REPLACE));
                    boolean append = toBoolean((String) properties.get(APPEND));
                    return new ActionFeature_Value_Set(attributeID, replace, append);

                } else if (propertyCheck(properties, ATTRIBUTE_NAME, OBJECT_REPLACE)) {
                    String attributeName = (String) properties.get(ATTRIBUTE_NAME);
                    Object replace = createObject((HashMap) properties.get(OBJECT_REPLACE));
                    return new ActionFeature_Value_Set(attributeName, replace);

                } else if (propertyCheck(properties, ATTRIBUTE_ID, OBJECT_REPLACE)) {
                    int attributeID = toInteger((String) properties.get(ATTRIBUTE_ID));
                    Object replace = createObject((HashMap) properties.get(OBJECT_REPLACE));
                    return new ActionFeature_Value_Set(attributeID, replace);

                } else {
                    failedConstructor(ActionFeature_Value_Set.class, properties);
                }



                /**
                 * Create ActionGeometry_Buffer
                 */
            } else if (isThisClass(actionClassName, ActionGeometry_Buffer.class)) {
                if (propertyCheck(properties, BUFFERSIZE)) {
                    double bufferSize = toDouble((String) properties.get(BUFFERSIZE));                   
                    
                    return new ActionGeometry_Buffer(bufferSize);
                } else {
                    failedConstructor(ActionGeometry_Buffer.class, properties);
                }


                /**
                 * Create ActionFeatureType_Typename_Case
                 */
            } else if (isThisClass(actionClassName, ActionFeatureType_Typename_Case.class)) {
                if (propertyCheck(properties, UPPERCASE)) {
                    boolean toUppercase = toBoolean((String) properties.get(UPPERCASE));

                    return new ActionFeatureType_Typename_Case(toUppercase);
                } else {
                    failedConstructor(ActionFeatureType_Typename_Case.class, properties);
                }


                /**
                 * Create ActionFeatureType_Typename_Substring
                 */
            } else if (isThisClass(actionClassName, ActionFeatureType_Typename_Substring.class)) {
                if (propertyCheck(properties, BEGIN_INDEX, END_INDEX)) {
                    int beginIndex = toInteger((String) properties.get(BEGIN_INDEX));
                    int endIndex = toInteger((String) properties.get(END_INDEX));

                    return new ActionFeatureType_Typename_Substring(beginIndex, endIndex);
                } else if (propertyCheck(properties, BEGIN_INDEX)) {
                    int beginIndex = toInteger((String) properties.get(BEGIN_INDEX));

                    return new ActionFeatureType_Typename_Substring(beginIndex);
                } else if (propertyCheck(properties, LENGTH, REVERSE)) {
                    int length = toInteger((String) properties.get(LENGTH));
                    boolean reverse = toBoolean((String) properties.get(REVERSE));

                    return new ActionFeatureType_Typename_Substring(length, reverse);
                } else {
                    failedConstructor(ActionFeatureType_Typename_Substring.class, properties);
                }


                /**
                 * Create ActionCombo_Fix_To_Oracle
                 */
            } else if (isThisClass(actionClassName, ActionCombo_Fix_To_Oracle.class)) {
                return new ActionCombo_Fix_To_Oracle();



                /**
                 * Create ActionCombo_Fix_From_Oracle
                 */
            } else if (isThisClass(actionClassName, ActionCombo_Fix_From_Oracle.class)) {
                return new ActionCombo_Fix_From_Oracle();





            } else if (isThisClass(actionClassName, ActionFeatureType_Set_CRS.class)) {
                if (propertyCheck(properties, SRS)) {
                    String srs = (String) properties.get(SRS);

                    return new ActionFeatureType_Set_CRS(srs);
                } else if (propertyCheck(properties, PARAMS)) {
                    HashMap metadata = (HashMap) properties.get(PARAMS);

                    return new ActionFeatureType_Set_CRS(metadata);
                } else {
                    failedConstructor(ActionFeatureType_Set_CRS.class, properties);
                }





            } else if (isThisClass(actionClassName, ActionFeatureType_AttributeName_Case.class)) {
                if (propertyCheck(properties, ATTRIBUTE_ID, UPPERCASE)) {
                    int attributeID = toInteger((String) properties.get(ATTRIBUTE_ID));
                    boolean toUppercase = toBoolean((String) properties.get(UPPERCASE));

                    return new ActionFeatureType_AttributeName_Case(attributeID, toUppercase);
                } else if (propertyCheck(properties, ATTRIBUTE_NAME, UPPERCASE)) {
                    String attributeName = (String) properties.get(ATTRIBUTE_NAME);
                    boolean toUppercase = toBoolean((String) properties.get(UPPERCASE));

                    return new ActionFeatureType_AttributeName_Case(attributeName, toUppercase);
                } else {
                    failedConstructor(ActionFeatureType_AttributeName_Case.class, properties);
                }



            } else if (isThisClass(actionClassName, ActionFeatureType_AttributeNames_Case.class)) {
                if (propertyCheck(properties, UPPERCASE)) {
                    boolean toUppercase = toBoolean((String) properties.get(UPPERCASE));

                    return new ActionFeatureType_AttributeNames_Case(toUppercase);
                } else {
                    failedConstructor(ActionFeatureType_AttributeName_Case.class, properties);
                }


                /**
                 * ActionFeature_Value_Substring
                 */
            } else if (isThisClass(actionClassName, ActionFeature_Value_Substring.class)) {
                if (propertyCheck(properties, ATTRIBUTE_NAME, END_INDEX, BEGIN_INDEX)) {
                    int endIndex = toInteger((String) properties.get(END_INDEX));
                    int beginIndex = toInteger((String) properties.get(BEGIN_INDEX));
                    String attributeName = (String) properties.get(ATTRIBUTE_NAME);

                    return new ActionFeature_Value_Substring(attributeName, beginIndex, endIndex);
                } else if (propertyCheck(properties, ATTRIBUTE_NAME, BEGIN_INDEX)) {
                    int beginIndex = toInteger((String) properties.get(BEGIN_INDEX));
                    String attributeName = (String) properties.get(ATTRIBUTE_NAME);

                    return new ActionFeature_Value_Substring(attributeName, beginIndex);
                } else if (propertyCheck(properties, ATTRIBUTE_ID, END_INDEX, BEGIN_INDEX)) {
                    int endIndex = toInteger((String) properties.get(END_INDEX));
                    int beginIndex = toInteger((String) properties.get(BEGIN_INDEX));
                    int attributeID = toInteger((String) properties.get(ATTRIBUTE_ID));

                    return new ActionFeature_Value_Substring(attributeID, beginIndex, endIndex);
                } else if (propertyCheck(properties, ATTRIBUTE_ID, BEGIN_INDEX)) {
                    int beginIndex = toInteger((String) properties.get(BEGIN_INDEX));
                    int attributeID = toInteger((String) properties.get(ATTRIBUTE_ID));

                    return new ActionFeature_Value_Substring(attributeID, beginIndex);
                } else {
                    failedConstructor(ActionFeature_Value_Substring.class, properties);
                }


                /**
                 * ActionFeature_Value_Substring_All
                 */
            } else if (isThisClass(actionClassName, ActionFeature_Value_Substring_All.class)) {
                if (propertyCheck(properties, END_INDEX, BEGIN_INDEX)) {
                    int endIndex = toInteger((String) properties.get(END_INDEX));
                    int beginIndex = toInteger((String) properties.get(BEGIN_INDEX));

                    return new ActionFeature_Value_Substring_All(beginIndex, endIndex);
                } else if (propertyCheck(properties, BEGIN_INDEX)) {
                    int beginIndex = toInteger((String) properties.get(BEGIN_INDEX));

                    return new ActionFeature_Value_Substring_All(beginIndex);
                } else {
                    failedConstructor(ActionFeature_Value_Substring_All.class, properties);
                }


                /**
                 * ActionFeatureType_Typename_AppendAttribute
                 */
            } else if (isThisClass(actionClassName, ActionFeatureType_Typename_AppendAttribute.class)) {
                if (propertyCheck(properties, ATTRIBUTE_ID, LENGTH)) {
                    int attributeID = toInteger((String) properties.get(ATTRIBUTE_ID));
                    int maxLength = toInteger((String) properties.get(LENGTH));

                    return new ActionFeatureType_Typename_AppendAttribute(attributeID, maxLength);
                } else if (propertyCheck(properties, ATTRIBUTE_NAME, LENGTH)) {
                    String attributeName = (String) properties.get(ATTRIBUTE_NAME);
                    int maxLength = toInteger((String) properties.get(LENGTH));

                    return new ActionFeatureType_Typename_AppendAttribute(attributeName, maxLength);
                } else {
                    failedConstructor(ActionFeatureType_Typename_AppendAttribute.class, properties);
                }



                /**
                 * ActionGeometry_Make_Point
                 */
            } else if (isThisClass(actionClassName, ActionGeometry_Make_Point.class)) {
                if (propertyCheck(properties, ATTRIBUTE_ID_X, ATTRIBUTE_ID_Y, SRS)) {
                    int attributeIDx = toInteger((String) properties.get(ATTRIBUTE_ID_X));
                    int attributeIDy = toInteger((String) properties.get(ATTRIBUTE_ID_Y));
                    String srs = (String) properties.get(SRS);

                    return new ActionGeometry_Make_Point(attributeIDx, attributeIDy, srs);
                } else if (propertyCheck(properties, ATTRIBUTE_NAME_X, ATTRIBUTE_NAME_Y, SRS)) {
                    String attributeNamex = (String) properties.get(ATTRIBUTE_NAME_X);
                    String attributeNamey = (String) properties.get(ATTRIBUTE_NAME_Y);
                    String srs = (String) properties.get(SRS);

                    return new ActionGeometry_Make_Point(attributeNamex, attributeNamey, srs);
                } else {
                    failedConstructor(ActionFeatureType_Typename_AppendAttribute.class, properties);
                }




                /**
                 * ActionFeatureType_Typename_Replace
                 */
            } else if (isThisClass(actionClassName, ActionFeatureType_Typename_Replace.class)) {
                if (propertyCheck(properties, REGEX, REPLACEMENT)) {
                    String regex = (String) properties.get(REGEX);
                    String replacement = (String) properties.get(REPLACEMENT);

                    return new ActionFeatureType_Typename_Replace(regex, replacement);
                } else {
                    failedConstructor(ActionFeatureType_Typename_AppendAttribute.class, properties);
                }
            } else if (isThisClass(actionClassName, ActionGeometry_RemoveDuplicateVertices.class)) {
                return new ActionGeometry_RemoveDuplicateVertices();
            } else if (isThisClass(actionClassName, ActionFeature_Value_ReplaceText.class)) {

                ActionFeature_Value_ReplaceText a = new ActionFeature_Value_ReplaceText();

                a.setAttribute((String) properties.get(ATTRIBUTE_NAME));
                a.setSearch((String) properties.get(OBJECT_FIND));
                a.setReplacement((String) properties.get(OBJECT_REPLACE));

                return a;
            } else if (isThisClass(actionClassName, ActionGeometry_VertexPrecisionThreshold.class)) {
                ActionGeometry_VertexPrecisionThreshold a = new ActionGeometry_VertexPrecisionThreshold();

                a.setScale(toInteger((String) properties.get(SCALE)));
                //a.setFloatPrecision(toBoolean((String)properties.get(FLOAT_PRECISION)));

                return a;

                /* Constructors nagaan voor ActionGeometry_Make_Point_Address */
            } else if (isThisClass(actionClassName, ActionGeometry_Make_Point_Address.class)) {

                String address1 = (String) properties.get(ATTRIBUTE_NAME_ADDRESS1);
                String address2 = (String) properties.get(ATTRIBUTE_NAME_ADDRESS2);
                String address3 = (String) properties.get(ATTRIBUTE_NAME_ADDRESS3);
                String city = (String) properties.get(ATTRIBUTE_NAME_CITY);
                String srs = (String) properties.get(SRS);

                return new ActionGeometry_Make_Point_Address(address1, address2, address3, city, srs);

                /* Constructors nagaan voor ActionFeature_Add_External_Attributes */
            } else if (isThisClass(actionClassName, ActionFeature_Add_External_Attributes.class)) {

                String otherFileName = (String) properties.get(ATTRIBUTE_NAME_OTHER_FILE_NAME);
                String dxfHandle = (String) properties.get(ATTRIBUTE_NAME_DXF_HANDLE);
                String otherFileHandle = (String) properties.get(ATTRIBUTE_NAME_OTHER_FILE_HANDLE);

                return new ActionFeature_Add_External_Attributes(dxfHandle, otherFileHandle, otherFileName);

                /* Constructors nagaan voor Action_XY_Intersects_Add_Mapped_Attrib */
            } else if (isThisClass(actionClassName, Action_XY_Intersects_Add_Mapped_Attrib.class)) {

                Long outputDbId = toLong((String) properties.get(ATTRIBUTE_NAME_OUTPUTDB_ID));
                String outputgeomColumn = (String) properties.get(ATTRIBUTE_NAME_OUTPUT_GEOM_COLUMN);
                String polyTable = (String) properties.get(ATTRIBUTE_NAME_POLY_TABLE);
                String matchSrcColumn = (String) properties.get(ATTRIBUTE_NAME_MATCH_SRC_COLUMN);
                String matchPolyColumn = (String) properties.get(ATTRIBUTE_NAME_MATCH_POLY_COLUMN);
                Boolean matchGeom = toBoolean((String) properties.get(ATTRIBUTE_NAME_MATCH_GEOM));

                return new Action_XY_Intersects_Add_Mapped_Attrib(outputDbId, outputgeomColumn, polyTable, matchSrcColumn, matchPolyColumn, matchGeom);

                /* Constructors nagaan voor ActionFeature_Filter_Column_Value */
            } else if (isThisClass(actionClassName, ActionFeature_Filter_Column_Value.class)) {

                String column = (String) properties.get(ATTRIBUTE_NAME_FILTER_COLUMN);
                String operator = (String) properties.get(ATTRIBUTE_NAME_FILTER_OPERATOR);
                String value = (String) properties.get(ATTRIBUTE_NAME_FILTER_VALUE);

                return new ActionFeature_Filter_Column_Value(column, operator, value);
                
            } else if (isThisClass(actionClassName, Action_Add_Properties.class)) {
                
                Map<String, Object> m = new HashMap();
                String pn, pv;
                pn  = (String) properties.get(PROPERTY_NAME1);
                pv  = (String) properties.get(PROPERTY_VALUE1);
                m.put(pn, pv);
                pn  = (String) properties.get(PROPERTY_NAME2);
                pv  = (String) properties.get(PROPERTY_VALUE2);
                m.put(pn, pv);
                pn  = (String) properties.get(PROPERTY_NAME3);
                pv  = (String) properties.get(PROPERTY_VALUE3);
                m.put(pn, pv);
                pn  = (String) properties.get(PROPERTY_NAME4);
                pv  = (String) properties.get(PROPERTY_VALUE4);
                m.put(pn, pv);
                pn  = (String) properties.get(PROPERTY_NAME5);
                pv  = (String) properties.get(PROPERTY_VALUE5);
                m.put(pn, pv);
                pn  = (String) properties.get(PROPERTY_NAME6);
                pv  = (String) properties.get(PROPERTY_VALUE6);
                m.put(pn, pv);
                pn  = (String) properties.get(PROPERTY_NAME7);
                pv  = (String) properties.get(PROPERTY_VALUE7);
                m.put(pn, pv);
                pn  = (String) properties.get(PROPERTY_NAME8);
                pv  = (String) properties.get(PROPERTY_VALUE8);
                m.put(pn, pv);
                pn  = (String) properties.get(PROPERTY_NAME9);
                pv  = (String) properties.get(PROPERTY_VALUE9);
                m.put(pn, pv);
                pn  = (String) properties.get(PROPERTY_NAME10);
                pv  = (String) properties.get(PROPERTY_VALUE10);
                m.put(pn, pv);
                
                return new Action_Add_Properties(m);

            } else if (isThisClass(actionClassName, ActionFeatureType_AttributeNames_Rename.class)) {
                Integer size = properties.size();

                if (size == null && size < 1) {
                    return null;
                }

                List<String> currentNames = new ArrayList<String>();
                List<String> newNames = new ArrayList<String>();

                for (Entry<String, Object> entry : properties.entrySet()) {
                    String currentField = entry.getKey();
                    String nieuw = (String) entry.getValue();

                    if (currentField != null && currentField.length() > 0 && nieuw != null && nieuw.length() > 0) {
                        currentNames.add(currentField);
                        newNames.add(nieuw);
                    }
                }

                if (currentNames.size() < 1 && currentNames.size() != newNames.size()) {
                    return null;
                }

                String[] invoer = (String[]) currentNames.toArray(new String[currentNames.size()]);
                String[] uitvoer = (String[]) newNames.toArray(new String[newNames.size()]);

                return new ActionFeatureType_AttributeNames_Rename(invoer, uitvoer);

            } else if (isThisClass(actionClassName, ActionFeatureType_AttributeNames_Map_To_Output.class)) {
                Integer size = properties.size();

                if (size == null && size < 1) {
                    return null;
                }

                List<String> currentNames = new ArrayList<String>();
                List<String> newNames = new ArrayList<String>();

                List<String> allOutputColumns = new ArrayList<String>();

                for (Entry<String, Object> entry : properties.entrySet()) {
                    String currentField = entry.getKey();
                    String nieuw = (String) entry.getValue();

                    if (currentField != null && currentField.length() > 0 && nieuw != null && nieuw.length() > 0) {
                        currentNames.add(currentField);
                        newNames.add(nieuw);
                    }

                    if (currentField != null && currentField.length() > 0) {
                        allOutputColumns.add(currentField);
                    }
                }

                if (currentNames.size() < 1 && currentNames.size() != newNames.size()) {
                    return null;
                }

                String[] invoer = (String[]) currentNames.toArray(new String[currentNames.size()]);
                String[] uitvoer = (String[]) newNames.toArray(new String[newNames.size()]);

                return new ActionFeatureType_AttributeNames_Map_To_Output(invoer, uitvoer, allOutputColumns);

            } /**
             * Create ActionFeatureType_Parse_Date
             */
            else if (isThisClass(actionClassName, ActionFeatureType_Parse_Date.class)) {
                if (propertyCheck(properties, ATTRIBUTE_NAME, DATE_FORMAT)) {
                    String attributeName = (String) properties.get(ATTRIBUTE_NAME);
                    String dateFormat = (String) properties.get(DATE_FORMAT);
                    return new ActionFeatureType_Parse_Date(attributeName, dateFormat);

                } else {
                    failedConstructor(ActionFeatureType_Parse_Date.class, properties);
                }
            } else {
                throw new UnsupportedOperationException(actionClassName + " is not yet implemented in ActionFactory");
            }
        }

        return null;
    }

    /**
     * Check if properties map contains all properties wanted
     */
    public static boolean propertyCheck(Map properties, String... find) {
        if (properties==null) {
            return false;
        }
        boolean found = true;

        for (String prop : find) {

            // Check if map contains property
            if (!properties.containsKey(prop)) {
                found = false;
            } else if (properties.get(prop) == null) {
                found = false;
            } else if (properties.get(prop).toString().equals("")) {
                found = false;
            }
        }

        return found;
    }

    /**
     * Check if given class is equal to string
     */
    private static boolean isThisClass(String actionClassName, Class checkClass) {
        return actionClassName.equals(checkClass.getSimpleName());
    }

    /**
     * Constructing the action failed. This function helps the user resolve the
     * problem (find missing parameters)
     */
    public static void failedConstructor(Class actionClass, Map properties) throws Exception {
        Method[] methods = actionClass.getMethods();
        boolean found = false;
        List<List<String>> constructors = null;

        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals("getConstructors")) {
                constructors = (List<List<String>>) methods[i].invoke(actionClass);
                found = (constructors != null);
                break;
            }
        }

        if (found) {
            String constructorString = "";
            // Strip { and }
            String propertyString = properties.toString().substring(1, properties.toString().length() - 1).replaceAll(", ", "\n");

            for (int i = 0; i < constructors.size(); i++) {
                String missing = "";
                constructorString += " " + actionClass.getSimpleName() + "(";
                for (int j = 0; j < constructors.get(i).size(); j++) {
                    constructorString += constructors.get(i).get(j) + (j != constructors.get(i).size() - 1 ? ", " : "");
                    missing += (!properties.containsKey(constructors.get(i).get(j)) ? " " + constructors.get(i).get(j) : "");
                }
                constructorString += ")\n - Missing parameter(s):" + missing + "\n\n";
                if (missing.equals("")) {
                    constructorString += " * Parameters appear to be fine for this action.\n   Go to class ActionFactory > createAction(...) > " + actionClass.getSimpleName() + "\n"
                            + "   Function propertyCheck(...) probably has wrong parameters.";
                }
            }

            throw new Exception("\nFailed to create " + actionClass.getClass().getSimpleName() + " using properties:\n" + propertyString + "\n\nAllowed constructors:\n" + constructorString + "\n\n");
        } else {
            throw new Exception("Class " + actionClass.getClass().getSimpleName() + " not supported; method getConstructors() not found");
        }
    }

    private static Object createObject(Map<String, String> map) throws Exception {
        final String CLASS = "class";
        final String VALUE = "value";

        final String STRING = "java.lang.String";
        final String DOUBLE = "java.lang.Double";
        final String INTEGER = "java.lang.Integer";

        final String FLOAT = "java.lang.Float";

        if (map.containsKey(CLASS)) {
            if (map.containsKey(VALUE)) {
                String value = map.get(VALUE);

                if (map.get(CLASS).equals("null")) {
                    return null;

                } else if (map.get(CLASS).equals(STRING)) {
                    return value;

                } else if (map.get(CLASS).equals(DOUBLE)) {
                    return Double.parseDouble(value);

                } else if (map.get(CLASS).equals(INTEGER)) {
                    return Integer.parseInt(value);

                } else if (map.get(CLASS).equals(FLOAT)) {
                    return Float.parseFloat(value);
                }

            } else {
                return (Object) toClass(map.get(CLASS));
            }

        } else {
            throw new Exception("No objectClass specified: " + map.toString());
        }

        return null;
    }

    public static Class toClass(String className) throws Exception {
        if (className.equalsIgnoreCase("null")) {
            return null;

        } else {
            if (className.endsWith(".class")) {
                className = className.substring(0, className.length() - ".class".length());
            }

            try {
                return Class.forName(className);
            } catch (Exception ex) {
                throw new Exception("String \'" + className + "' could not be converted to class");
            }
        }
    }

    public static boolean toBoolean(String value) throws Exception {
        if (value.equalsIgnoreCase("true")) {
            return true;
        } else if (value.equalsIgnoreCase("false")) {
            return false;
        } else if (value.equals("1")) {
            return true;
        } else if (value.equals("0")) {
            return false;
        } else {
            throw new Exception("String \'" + value + "' could not be converted to boolean");
        }
    }

    public static int toInteger(String value) {
        return Integer.parseInt(value);
    }

    public static Long toLong(String value) {
        return new Long(value);
    }
    
    public static Double toDouble(String value) {
        return Double.parseDouble(value);
    }

    public static ActionCondition.CompareType toCompareType(String value) {
        return ActionCondition.CompareType.byString(value);
    }

    public static SortedMap<String, List<List<String>>> getSupportedActionBlocks(String[] invoer, String[] uitvoer, String templateOutputType) {
        SortedMap<String, List<List<String>>> actionBlocks = new TreeMap<String, List<List<String>>>();

        actionBlocks.put(ActionCombo_Fix_From_Oracle.class.getSimpleName(), ActionCombo_Fix_From_Oracle.getConstructors());
        actionBlocks.put(ActionCombo_Fix_To_Oracle.class.getSimpleName(), ActionCombo_Fix_To_Oracle.getConstructors());
        actionBlocks.put(ActionCombo_Recommended_Pro.class.getSimpleName(), ActionCombo_Recommended_Pro.getConstructors());
        actionBlocks.put(ActionCombo_Recommended.class.getSimpleName(), ActionCombo_Recommended.getConstructors());

        //actionBlocks.put(ActionCombo_GeometrySingle_Writer.class.getSimpleName(), ActionCombo_GeometrySingle_Writer.getConstructors());
        actionBlocks.put(ActionCombo_GeometrySplitter_Writer.class.getSimpleName(), ActionCombo_GeometrySplitter_Writer.getConstructors());
        //actionBlocks.put(ActionDataStore_Writer.class.getSimpleName(), ActionDataStore_Writer.getConstructors());

        actionBlocks.put(ActionFeatureType_AttributeNames_Case.class.getSimpleName(), ActionFeatureType_AttributeNames_Case.getConstructors());
        actionBlocks.put(ActionFeatureType_AttributeName_Case.class.getSimpleName(), ActionFeatureType_AttributeName_Case.getConstructors());
        actionBlocks.put(ActionFeatureType_AttributeName_Rename.class.getSimpleName(), ActionFeatureType_AttributeName_Rename.getConstructors());
        actionBlocks.put(ActionFeatureType_AttributeType_Add.class.getSimpleName(), ActionFeatureType_AttributeType_Add.getConstructors());
        actionBlocks.put(ActionFeatureType_AttributeType_Remove.class.getSimpleName(), ActionFeatureType_AttributeType_Remove.getConstructors());
        actionBlocks.put(ActionFeatureType_AttributeType_Restriction.class.getSimpleName(), ActionFeatureType_AttributeType_Restriction.getConstructors());
        actionBlocks.put(ActionFeatureType_Replace_Class.class.getSimpleName(), ActionFeatureType_Replace_Class.getConstructors());
        actionBlocks.put(ActionFeatureType_Replace_Class_All.class.getSimpleName(), ActionFeatureType_Replace_Class_All.getConstructors());
        actionBlocks.put(ActionFeatureType_Set_CRS.class.getSimpleName(), ActionFeatureType_Set_CRS.getConstructors());
        actionBlocks.put(ActionFeatureType_Typename_AppendAttribute.class.getSimpleName(), ActionFeatureType_Typename_AppendAttribute.getConstructors());
        actionBlocks.put(ActionFeatureType_Typename_Case.class.getSimpleName(), ActionFeatureType_Typename_Case.getConstructors());
        actionBlocks.put(ActionFeatureType_Typename_Substring.class.getSimpleName(), ActionFeatureType_Typename_Substring.getConstructors());
        actionBlocks.put(ActionFeatureType_Typename_Update.class.getSimpleName(), ActionFeatureType_Typename_Update.getConstructors());
        //actionBlocks.put(ActionFeatureType_Typename_Replace.class.getSimpleName(), ActionFeatureType_Typename_Replace.getConstructors());

        actionBlocks.put(ActionFeature_Value_ReplaceText.class.getSimpleName(), ActionFeature_Value_ReplaceText.getConstructors());
        actionBlocks.put(ActionFeature_Value_Replace.class.getSimpleName(), ActionFeature_Value_Replace.getConstructors());
        actionBlocks.put(ActionFeature_Value_Set.class.getSimpleName(), ActionFeature_Value_Set.getConstructors());
        actionBlocks.put(ActionFeature_Value_Substring.class.getSimpleName(), ActionFeature_Value_Substring.getConstructors());
        actionBlocks.put(ActionFeature_Value_Substring_All.class.getSimpleName(), ActionFeature_Value_Substring_All.getConstructors());

        actionBlocks.put(ActionGeometry_Buffer.class.getSimpleName(), ActionGeometry_Buffer.getConstructors());
        actionBlocks.put(ActionGeometry_Make_Point.class.getSimpleName(), ActionGeometry_Make_Point.getConstructors());
        actionBlocks.put(ActionGeometry_RemoveDuplicateVertices.class.getSimpleName(), ActionGeometry_RemoveDuplicateVertices.getConstructors());
        actionBlocks.put(ActionGeometry_VertexPrecisionThreshold.class.getSimpleName(), ActionGeometry_VertexPrecisionThreshold.getConstructors());

        actionBlocks.put(ActionGeometry_Make_Point_Address.class.getSimpleName(), ActionGeometry_Make_Point_Address.getConstructors());

        actionBlocks.put(ActionFeature_Add_External_Attributes.class.getSimpleName(), ActionFeature_Add_External_Attributes.getConstructors());

        if (templateOutputType != null && templateOutputType.equals(Inout.TEMPLATE_OUTPUT_NO_TABLE)) {
            actionBlocks.put(ActionFeatureType_AttributeNames_Rename.class.getSimpleName(), ActionFeatureType_AttributeNames_Rename.getConstructors(invoer));
        }

        if (templateOutputType != null && !templateOutputType.equals(Inout.TEMPLATE_OUTPUT_NO_TABLE)) {
            actionBlocks.put(ActionFeatureType_AttributeNames_Map_To_Output.class.getSimpleName(), ActionFeatureType_AttributeNames_Map_To_Output.getConstructors(uitvoer));
        }

        actionBlocks.put(Action_XY_Intersects_Add_Mapped_Attrib.class.getSimpleName(), Action_XY_Intersects_Add_Mapped_Attrib.getConstructors());
        actionBlocks.put(ActionFeature_Filter_Column_Value.class.getSimpleName(), ActionFeature_Filter_Column_Value.getConstructors());
        actionBlocks.put(Action_Add_Properties.class.getSimpleName(), Action_Add_Properties.getConstructors());

        actionBlocks.put(ActionFeatureType_Parse_Date.class.getSimpleName(), ActionFeatureType_Parse_Date.getConstructors());
        return actionBlocks;
    }

    public static SortedMap<String, List<List<String>>> createDefaultUseTableActionBlocks(String[] uitvoer) {
        SortedMap<String, List<List<String>>> actionBlocks = new TreeMap<String, List<List<String>>>().descendingMap();

        actionBlocks.put(ActionFeatureType_Typename_Update.class.getSimpleName(), ActionFeatureType_Typename_Update.getConstructors());
        actionBlocks.put(ActionFeatureType_Set_CRS.class.getSimpleName(), ActionFeatureType_Set_CRS.getConstructors());
        actionBlocks.put(ActionFeatureType_AttributeNames_Map_To_Output.class.getSimpleName(), ActionFeatureType_AttributeNames_Map_To_Output.getConstructors(uitvoer));

        return actionBlocks;
    }

    public static SortedMap<String, List<List<String>>> createDefaultUseAsTemplateActionBlocks(String[] uitvoer) {
        SortedMap<String, List<List<String>>> actionBlocks = new TreeMap<String, List<List<String>>>().descendingMap();

        actionBlocks.put(ActionFeatureType_Typename_Update.class.getSimpleName(), ActionFeatureType_Typename_Update.getConstructors());
        actionBlocks.put(ActionFeatureType_Set_CRS.class.getSimpleName(), ActionFeatureType_Set_CRS.getConstructors());
        actionBlocks.put(ActionFeatureType_AttributeNames_Map_To_Output.class.getSimpleName(), ActionFeatureType_AttributeNames_Map_To_Output.getConstructors(uitvoer));

        return actionBlocks;
    }

    public static SortedMap<String, List<List<String>>> createDefaultNoTableActionBlocks(String[] invoer) {
        SortedMap<String, List<List<String>>> actionBlocks = new TreeMap<String, List<List<String>>>().descendingMap();

        actionBlocks.put(ActionFeatureType_Typename_Update.class.getSimpleName(), ActionFeatureType_Typename_Update.getConstructors());
        actionBlocks.put(ActionFeatureType_Set_CRS.class.getSimpleName(), ActionFeatureType_Set_CRS.getConstructors());
        actionBlocks.put(ActionFeatureType_AttributeNames_Rename.class.getSimpleName(), ActionFeatureType_AttributeNames_Rename.getConstructors(invoer));

        return actionBlocks;
    }

    public static boolean isDataStoreAction(String classname) {
        return classname.equals(ActionCombo_GeometrySingle_Writer.class.getSimpleName())
                || classname.equals(ActionCombo_GeometrySplitter_Writer.class.getSimpleName())
                || classname.equals(ActionDataStore_Writer.class.getSimpleName());
    }
}
