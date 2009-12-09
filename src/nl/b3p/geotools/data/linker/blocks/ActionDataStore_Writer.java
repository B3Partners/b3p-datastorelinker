package nl.b3p.geotools.data.linker.blocks;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.geotools.data.*;
import java.util.Map;
import nl.b3p.geotools.data.linker.ActionFactory;
import nl.b3p.geotools.data.linker.DataStoreLinker;
import nl.b3p.geotools.data.linker.feature.EasyFeature;
import org.geotools.data.jdbc.JDBCDataStore;
import org.geotools.feature.IllegalAttributeException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * Write to a datastore (file or JDBC)
 * @author Gertjan Al, B3Partners
 */
public class ActionDataStore_Writer extends Action {

    private boolean initDone = false;//,  runOnce = true;
    private DataStore dataStore2Write = null;
    private Map params;
    private final boolean append;
    private final boolean dropFirst;
    private Exception constructorEx;
    private HashMap<String, FeatureWriter> featureWriters = new HashMap();
    private HashMap<String, String> checked = new HashMap();
    private static final int MAX_CONNECTIONS_NR = 50;
    private static final String MAX_CONNECTIONS = "max connections";
    private static int processedTypes = 0;

    public ActionDataStore_Writer(Map params, boolean append, boolean dropFirst) {
        this.params = params;
        this.append = append;
        this.dropFirst = dropFirst;

        if (!params.containsKey(MAX_CONNECTIONS)) {
            params.put(MAX_CONNECTIONS, MAX_CONNECTIONS_NR);
        }

        try {
            dataStore2Write = DataStoreLinker.openDataStore(params);
            initDone = (dataStore2Write != null);

        } catch (Exception ex) {
            constructorEx = ex;
        }
    }

    public ActionDataStore_Writer(Map params) {
        this.params = params;
        this.append = false;
        this.dropFirst = true;

        if (!params.containsKey(MAX_CONNECTIONS)) {
            params.put(MAX_CONNECTIONS, MAX_CONNECTIONS_NR);
        }

        try {
            dataStore2Write = DataStoreLinker.openDataStore(params);
            initDone = (dataStore2Write != null);

        } catch (Exception ex) {
            constructorEx = ex;
        }
    }

    public EasyFeature execute(EasyFeature feature) throws Exception {
        if (!initDone) {
            throw new Exception("\nOpening dataStore failed; datastore could not be found, missing library or no access to file.\nUsed parameters:\n" + params.toString() + "\n\n" + constructorEx.getLocalizedMessage());

        } else {
            feature = fixFeatureTypeName(feature);
            String typename = feature.getFeatureType().getTypeName();

            FeatureWriter writer;
            if (featureWriters.containsKey(typename)) {
                writer = featureWriters.get(typename);

            } else {

                if (featureWriters.size() + 1 == MAX_CONNECTIONS_NR) {
                    // If max connections reached, commit all data and continue
                    close();
                    dataStore2Write = DataStoreLinker.openDataStore(params);
                    log.warn("Closing all connections (too many featureWriters loaded)");
                }

                if (!checked.containsKey(params.toString() + typename)) {
                    checkSchema(feature.getFeatureType());
                    checked.put(params.toString() + typename, "");
                    processedTypes++;
                }

                writer = dataStore2Write.getFeatureWriterAppend(typename, Transaction.AUTO_COMMIT);
                featureWriters.put(typename, writer);
            }

            try {
                Geometry the_geom = (Geometry) feature.getAttribute(feature.getFeatureType().getGeometryDescriptor().getLocalName());

                if (the_geom instanceof GeometryCollection) {
                    if (((GeometryCollection) the_geom).getNumGeometries() == 0) {
                        log.warn("Skipping feature with empty GeometryCollection; " + feature.getAttributes().toString());
                    } else {
                        write(writer, feature.getFeature());
                    }
                } else {
                    write(writer, feature.getFeature());
                }

            } catch (Exception ex) {
                log.warn("No DefaultGeometry AttributeType found in feature " + feature.toString());
                write(writer, feature.getFeature());
            }
        }
        return feature;
    }

    @Override
    public void close() throws Exception {
        closeConnections();
        dataStore2Write.dispose();
    }

    private void write(FeatureWriter writer, SimpleFeature feature) throws IOException {
        // Write to datastore
        SimpleFeature newFeature = (SimpleFeature) writer.next();

        try {
            newFeature.setAttributes(feature.getAttributes());
        } catch (IllegalAttributeException writeProblem) {
            throw new IllegalAttributeException("Could not create " + feature.getFeatureType().getTypeName() + " out of provided SimpleFeature: " + feature.getID() + "\n" + writeProblem);
        }

        writer.write();
    }

    private void closeConnections() throws Exception {
        // Commit datastore
        // close connection
        Iterator iter = featureWriters.keySet().iterator();
        while (iter.hasNext()) {
            FeatureWriter writer = featureWriters.get((String) iter.next());
            writer.close();
        }
        featureWriters.clear();
    }

    private void checkSchema(SimpleFeatureType featureType) throws Exception {
        if (initDone) {
            //boolean typeExists = false;
            //String[] typeNamesFound = dataStore2Write.getTypeNames();
            String typename2Write = featureType.getTypeName();
            boolean typeExists = Arrays.asList(dataStore2Write.getTypeNames()).contains(typename2Write);

            /*
            for (int i = 0; i < typeNamesFound.length; i++) {
            if (typename2Write.equals(typeNamesFound[i])) {
            typeExists = true;
            break;
            }
            }
             */

            if (dropFirst && typeExists) {
                // Check if DataStore is a Database
                if (dataStore2Write instanceof JDBCDataStore) {
                    // Drop table
                    JDBCDataStore database = (JDBCDataStore) dataStore2Write;
                    Connection con = database.getConnection(Transaction.AUTO_COMMIT);
                    con.setAutoCommit(true);

                    // TODO make this function work with all databases
                    PreparedStatement ps = con.prepareStatement("DROP TABLE \"" + featureType.getTypeName() + "\"; DELETE FROM \"geometry_columns\" WHERE f_table_name = '" + featureType.getTypeName() + "'");
                    ps.execute();

                    con.close();
                }
                typeExists = false;
            }


            // If table does not exist, create new
            if (!typeExists) {
                dataStore2Write.createSchema(featureType);
                log.info("Creating new table with name: " + featureType.getTypeName());

            } else if (!append) {

                // Check if DataStore is a Database
                if (dataStore2Write instanceof JDBCDataStore) {
                    // Empty table
                    JDBCDataStore database = (JDBCDataStore) dataStore2Write;
                    Connection con = database.getConnection(Transaction.AUTO_COMMIT);
                    con.setAutoCommit(true);

                    // TODO make this function work with all databases
                    PreparedStatement ps = con.prepareStatement("TRUNCATE TABLE \"" + featureType.getTypeName() + "\"");
                    ps.execute();

                    con.close();
                }
            }
        }
    }

    private EasyFeature fixFeatureTypeName(EasyFeature feature) throws Exception {
        String typename = fixTypename(feature.getTypeName().replaceAll(" ", "_"));
        feature.setTypeName(typename);

        return feature;
    }

    public Map getParams() {
        return params;
    }

    public String toString() {
        return "Write to datastore: " + params.toString();
    }

  public static  List<List<String>> getConstructors() {
        List<List<String>> constructors = new ArrayList<List<String>>();

        constructors.add(Arrays.asList(new String[]{
                    ActionFactory.PARAMS,
                    ActionFactory.APPEND,
                    ActionFactory.DROPFIRST
                }));

        constructors.add(Arrays.asList(new String[]{
                    ActionFactory.PARAMS
                }));

        return constructors;

    }

    public String getDescription_NL() {
        return "Schrijf de SimpleFeature weg naar een datastore. Als de datastore een database is, kan de SimpleFeature worden toegevoegd of kan de tabel worden geleegd voor het toevoegen";
    }
}
