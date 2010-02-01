/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.b3p.geotools.data.linker;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import nl.b3p.geotools.data.linker.blocks.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Create actionBlocks for given Classname and parameters (properties)
 * @author Gertjan Al, B3Partners
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
    public static final Log log = LogFactory.getLog(DataStoreLinker.class);

    public static Action createAction(String actionClassName, Map<String, Object> properties) throws Exception {
        //if its a writer action:
        if (isThisClass(actionClassName, ActionCombo_GeometrySplitter_Writer.class) ||
                isThisClass(actionClassName, ActionCombo_GeometrySingle_Writer.class) ||
                isThisClass(actionClassName, ActionDataStore_Writer.class)) {

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
                    boolean append = (Boolean) properties.get(APPEND);
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
                if (propertyCheck(properties, ATTRIBUTE_NAME, ATTRIBUTE_CLASS, ATTRIBUTE_ID)) {
                    String attributeName = (String) properties.get(ATTRIBUTE_NAME);
                    Class attributeClass = (Class) toClass((String) properties.get(ATTRIBUTE_CLASS));
                    int attributeID = toInteger((String) properties.get(ATTRIBUTE_ID));
                    return new ActionFeatureType_AttributeType_Add(attributeID, attributeName, attributeClass);

                } else if (propertyCheck(properties, ATTRIBUTE_NAME, ATTRIBUTE_CLASS)) {
                    String attributeName = (String) properties.get(ATTRIBUTE_NAME);
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
                    boolean tryCast = (Boolean) properties.get(TRYCAST);
                    return new ActionFeatureType_Replace_Class(attributeName, newAttributeClass, tryCast);

                } else if (propertyCheck(properties, ATTRIBUTE_ID, NEW_ATTRIBUTE_CLASS, TRYCAST)) {
                    int attributeID = toInteger((String) properties.get(ATTRIBUTE_ID));
                    Class newAttributeClass = (Class) toClass((String) properties.get(NEW_ATTRIBUTE_CLASS));
                    boolean tryCast = (Boolean) properties.get(TRYCAST);
                    return new ActionFeatureType_Replace_Class(attributeID, newAttributeClass, tryCast);

                } else if (propertyCheck(properties, NEW_ATTRIBUTE_CLASS, TRYCAST)) {
                    Class newAttributeClass = (Class) toClass((String) properties.get(NEW_ATTRIBUTE_CLASS));
                    boolean tryCast = (Boolean) properties.get(TRYCAST);
                    return new ActionFeatureType_Replace_Class(newAttributeClass, tryCast);

                } else {
                    failedConstructor(ActionFeatureType_Replace_Class.class, properties);
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
                    boolean append = (Boolean) properties.get(APPEND);
                    return new ActionFeatureType_Typename_Update(newTypeName, append);

                } else if (propertyCheck(properties, NEW_TYPENAME)) {
                    String newTypeName = (String) properties.get(NEW_TYPENAME);
                    return new ActionFeatureType_Typename_Update(newTypeName);

                } else {
                    failedConstructor(ActionFeatureType_Typename_Update.class, properties);
                }



                /**
                 * Create ActionFeature_Value_Replace
                 */
            } else if (isThisClass(actionClassName, ActionFeature_Value_Replace.class)) {
                if (propertyCheck(properties, OBJECT_FIND, OBJECT_REPLACE)) {
                    Object find = createObject((HashMap) properties.get(OBJECT_FIND));
                    Object replace = createObject((HashMap) properties.get(OBJECT_REPLACE));
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
                    Object replace = createObject((HashMap) properties.get(OBJECT_REPLACE));
                    boolean append = (Boolean) properties.get(APPEND);
                    return new ActionFeature_Value_Set(attributeName, replace, append);

                } else if (propertyCheck(properties, ATTRIBUTE_ID, OBJECT_REPLACE, APPEND)) {
                    int attributeID = toInteger((String) properties.get(ATTRIBUTE_ID));
                    Object replace = createObject((HashMap) properties.get(OBJECT_REPLACE));
                    boolean append = (Boolean) properties.get(APPEND);
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
                    int bufferSize = toInteger((String) properties.get(BUFFERSIZE));

                    return new ActionGeometry_Buffer(bufferSize);
                } else {
                    failedConstructor(ActionGeometry_Buffer.class, properties);
                }


                /**
                 * Create ActionFeatureType_Typename_Case
                 */
            } else if (isThisClass(actionClassName, ActionFeatureType_Typename_Case.class)) {
                if (propertyCheck(properties, UPPERCASE)) {
                    boolean toUppercase = (Boolean) properties.get(UPPERCASE);

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
                    boolean reverse = (Boolean) properties.get(REVERSE);

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
                    boolean toUppercase = (Boolean) properties.get(UPPERCASE);

                    return new ActionFeatureType_AttributeName_Case(attributeID, toUppercase);
                } else if (propertyCheck(properties, ATTRIBUTE_NAME, UPPERCASE)) {
                    String attributeName = (String) properties.get(ATTRIBUTE_NAME);
                    boolean toUppercase = (Boolean) properties.get(UPPERCASE);

                    return new ActionFeatureType_AttributeName_Case(attributeName, toUppercase);
                } else {
                    failedConstructor(ActionFeatureType_AttributeName_Case.class, properties);
                }



            } else if (isThisClass(actionClassName, ActionFeatureType_AttributeNames_Case.class)) {
                if (propertyCheck(properties, UPPERCASE)) {
                    boolean toUppercase = (Boolean) properties.get(UPPERCASE);

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




                /**
                 * Action not found
                 */
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
        boolean found = true;

        for (String prop : find) {

            // Check if map contains property
            if (!properties.containsKey(prop)) {

                /*
                // Check if class in propertyMap is right
                if (properties.get(prop) != null) {

                // Check if class is defined for this property
                if (mapping.containsKey(prop)) {
                boolean equals = properties.get(prop).getClass().equals(mapping.get(prop));
                boolean instanceOf = properties.get(prop).getClass().isInstance(mapping.get(prop));

                if (!equals && !instanceOf) {
                found = false;
                }
                }
                }
                } else {
                 */
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
     *  Constructing the action failed. This function helps the user resolve the problem (find missing parameters)
     */
    public static void failedConstructor(Class actionClass, Map properties) throws Exception {
        Method[] methods = actionClass.getMethods();
        boolean found = false;
        String[][] constructors = null;

        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals("getConstructors")) {
                constructors = (String[][]) methods[i].invoke(actionClass);
                found = (constructors != null);
                break;
            }
        }

        if (found) {
            String constructorString = "";
            // Strip { and }
            String propertyString = properties.toString().substring(1, properties.toString().length() - 1).replaceAll(", ", "\n");

            for (int i = 0; i < constructors.length; i++) {
                String missing = "";
                constructorString += " " + actionClass.getSimpleName() + "(";
                for (int j = 0; j < constructors[i].length; j++) {
                    constructorString += constructors[i][j] + (j != constructors[i].length - 1 ? ", " : "");
                    missing += (!properties.containsKey(constructors[i][j]) ? " " + constructors[i][j] : "");
                }
                constructorString += ")\n - Missing parameter(s):" + missing + "\n\n";
                if (missing.equals("")) {
                    constructorString += " * Parameters appear to be fine for this action.\n   Go to class ActionFactory > createAction(...) > " + actionClass.getSimpleName() + "\n" +
                            "   Function propertyCheck(...) probably has wrong parameters.";
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
                    return new String(value);

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
            return new Boolean(true);

        } else if (value.equalsIgnoreCase("false")) {
            return new Boolean(false);

        } else {
            throw new Exception("String \'" + value + "' could not be converted to boolean");
        }
    }

    public static int toInteger(String value) {
        return Integer.parseInt(value);
    }

    public static ActionCondition.CompareType toCompareType(String value) {
        return ActionCondition.CompareType.byString(value);
    }
    /*
    public static List<Class> getActionClasses() {
    Class cls = Action.class;
    Package pkg = cls.getPackage();
    String name = pkg.getName();

    try {
    Class[] classes = Class.forName(name).getClasses();
    return Arrays.asList(classes);
    } catch (Exception ex) {
    return new ArrayList();
    }
    }
     */
    // The following function

    public static SortedMap<String, List<List<String>>> getSupportedActionBlocks() {
        SortedMap<String, List<List<String>>> actionBlocks = new TreeMap<String, List<List<String>>>();

        actionBlocks.put(ActionCombo_Fix_From_Oracle.class.getSimpleName(), ActionCombo_Fix_From_Oracle.getConstructors());
        actionBlocks.put(ActionCombo_Fix_To_Oracle.class.getSimpleName(), ActionCombo_Fix_To_Oracle.getConstructors());
        actionBlocks.put(ActionCombo_Recommended_Pro.class.getSimpleName(), ActionCombo_Recommended_Pro.getConstructors());
        actionBlocks.put(ActionCombo_Recommended.class.getSimpleName(), ActionCombo_Recommended.getConstructors());

        //actionBlocks.put(ActionCombo_GeometrySingle_Writer.class.getSimpleName(), ActionCombo_GeometrySingle_Writer.getConstructors());
        //actionBlocks.put(ActionCombo_GeometrySplitter_Writer.class.getSimpleName(), ActionCombo_GeometrySplitter_Writer.getConstructors());
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

        actionBlocks.put(ActionFeature_Value_Replace.class.getSimpleName(), ActionFeature_Value_Replace.getConstructors());
        actionBlocks.put(ActionFeature_Value_Set.class.getSimpleName(), ActionFeature_Value_Set.getConstructors());
        actionBlocks.put(ActionFeature_Value_Substring.class.getSimpleName(), ActionFeature_Value_Substring.getConstructors());
        actionBlocks.put(ActionFeature_Value_Substring_All.class.getSimpleName(), ActionFeature_Value_Substring_All.getConstructors());


        actionBlocks.put(ActionGeometry_Buffer.class.getSimpleName(), ActionGeometry_Buffer.getConstructors());
        actionBlocks.put(ActionGeometry_Make_Point.class.getSimpleName(), ActionGeometry_Make_Point.getConstructors());

        return actionBlocks;
    }

    public static boolean isDataStoreAction(String classname) {
        return classname.equals(ActionCombo_GeometrySingle_Writer.class.getSimpleName()) ||
                classname.equals(ActionCombo_GeometrySplitter_Writer.class.getSimpleName()) ||
                classname.equals(ActionDataStore_Writer.class.getSimpleName());
    }
}
