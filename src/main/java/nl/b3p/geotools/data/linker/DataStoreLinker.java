package nl.b3p.geotools.data.linker;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.*;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;

import nl.b3p.commons.jpa.JpaUtilServlet;
import nl.b3p.datastorelinker.entity.Database;
import nl.b3p.datastorelinker.util.Namespaces;
import nl.b3p.datastorelinker.util.Util;
import nl.b3p.geotools.data.linker.blocks.Action;
import nl.b3p.geotools.data.linker.blocks.WritableAction;
import nl.b3p.geotools.data.linker.feature.EasyFeature;
import nl.b3p.geotools.data.linker.util.LocalizationUtil;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStoreFactorySpi;
import org.geotools.data.oracle.OracleNGDataStoreFactory;
import org.geotools.data.postgis.PostgisNGDataStoreFactory;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
//import org.geotools.data.shapefile.indexed.IndexedShapefileDataStoreFactory;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.JDBCFeatureStore;
import org.geotools.jdbc.PrimaryKey;
import org.geotools.referencing.CRS;
import org.hibernate.Session;
import org.jdom.Element;
import org.jdom.input.DOMBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.BoundingBox;
import org.opengis.geometry.Geometry;

import javax.persistence.EntityManager;


/**
 * Convert batch to actionList and execute it
 *
 * @author Gertjan Al, B3Partners
 */
public class DataStoreLinker implements Runnable {

    private static final Log log = LogFactory.getLog(DataStoreLinker.class);
    private final static ResourceBundle resources = LocalizationUtil.getResources();
    public static final String ACTIONLIST_PREFIX = "actionlist";
    public static final String ACTION_PREFIX = "action";
    public static final String TYPE_PREFIX = "type";
    public static final String SETTINGS_PREFIX = "settings";
    public static final String DATASTORE2READ_PREFIX = "read.datastore.";
    public static final String READ_TYPENAME = "read.typename";
    protected static final String DEFAULT_WRITER = "ActionCombo_GeometrySingle_Writer";
    
    public static String DEFAULT_SMTPHOST = "kmail.b3partners.nl";
    public static String DEFAULT_FROM = "noreply@b3partners.nl";
    
    protected Status status;
    protected DataStore dataStore2Read;
    protected ActionList actionList;
    protected Properties batch;
    protected nl.b3p.datastorelinker.entity.Process process;
    protected boolean disposed;
    private int exceptionLogCount = 0;
    private static final int MAX_EXCEPTION_LOG_COUNT = 10;
    public static final String TYPE_ORACLE = "oracle";
    public static final String TYPE_POSTGIS = "postgis";
    private boolean writingFromOtherToOracle;
    private BoundingBox oracleBoundingBox;
    private String oracleTableName;
//    private static int srid;

    public synchronized Status getStatus() {
        return status;
    }

    public DataStoreLinker(nl.b3p.datastorelinker.entity.Process process) throws Exception {
        // test comment
        init(process);
        dataStore2Read = openDataStore();
        actionList = createActionList();
        postInit();
    }

    public DataStoreLinker(Properties batch) throws Exception {
        init(batch);
        dataStore2Read = createDataStore2Read(batch);
        actionList = createActionList(batch);
        postInit();
    }

    public DataStoreLinker(Map<String, Object> input, Map<String, Object> actions, Properties batch) throws Exception {
        init(batch);
        dataStore2Read = openDataStore(input);
        actionList = createActionList(actions);
        postInit();
    }

    private void init(nl.b3p.datastorelinker.entity.Process process) throws ConfigurationException, IOException {
        this.process = process;
        disposed = false;
        status = new Status(process);
    }

    private void init(Properties batch) throws ConfigurationException, IOException {
        this.batch = batch;
        disposed = false;
        status = new Status(batch);
    }

    /**
     * Calculating the size can take a very long time depending on the
     * implementation of the chosen datastore. Some implementations walk through
     * all features to calculate the size.
     *
     * @throws IOException
     */
    private void calculateSizes() throws IOException {
        int totalFeatureSize = 0;
        if (dataStore2Read != null) {
            for (String typeName2Read : getTypeNames()) {
                FeatureCollection fc = dataStore2Read.getFeatureSource(typeName2Read).getFeatures();
                totalFeatureSize += fc.size();
            }
        }
        status.setTotalFeatureSize(totalFeatureSize);
    }

    private void postInit() throws IOException {
        calculateSizes();
        this.writingFromOtherToOracle = process.isFromOtherToOracle();
        log.info("dsl init complete.");
    }

    public void run() {
        try {
            process();
        } catch (Exception ex) {
            log.error(ex);
        }
    }

    public boolean isDisposed(){
       return disposed; 
    }
    
    public synchronized void dispose() throws Exception {
        if (!disposed) {
            try {
                dataStore2Read.dispose();
                actionList.close();
            } finally {
                disposed = true;
            }
        }
    }

    /**
     * Process all features in dataStore2Read
     *
     * @throws Exception generic exception
     */
    public void process() throws Exception {
        if (disposed) {
            throw new Exception("Dsl already used. Please create a new instance of this class");
        }

        try {
            for (String typeName2Read : getTypeNames()) {
                processTypeName(typeName2Read);
            }
        } finally {
            try {
                if (batch != null) {
                    DataStoreLinkerMail.mail(batch, status.getNonFatalErrorReport("\n", 3));
                } else if (process != null) {
                    DataStoreLinkerMail.mail(process, status.getNonFatalErrorReport("\n", 3));
                }
            } catch (Exception mex) {
                status.addNonFatalError(mex.getLocalizedMessage(), "");
            }
            log.info("Error report: " + status.getNonFatalErrorReport("\n", 3));
            dispose();
        }
    }

    private void processTypeName(String typeName2Read) throws Exception, IOException {
        SimpleFeature feature = null;

        FeatureCollection fc = dataStore2Read.getFeatureSource(typeName2Read).getFeatures();
        FeatureIterator iterator = fc.features();

        PrimaryKey pk = null;
        if (dataStore2Read != null && (dataStore2Read instanceof JDBCDataStore)) {
            FeatureSource fs = ((JDBCDataStore) dataStore2Read).getFeatureSource(typeName2Read);
            if (fs instanceof JDBCFeatureStore) {
                pk = ((JDBCFeatureStore) fs).getPrimaryKey();
            }
        }

        Map properties = null;
        if (batch != null) {
            properties = new HashMap(batch);
        } else if (process != null) {
            properties = process.toOutputMap();
        }

        try {
            while (iterator.hasNext()) {                
                try {
                    feature = (SimpleFeature) iterator.next();
                } catch (Exception ex) {
                    log.error("Fout bij inlezen feature ", ex);
                    
                    String id = null;
                    if (feature != null ) {
                        id = feature.getID();
                    }
                    
                    status.addNonFatalError(ExceptionUtils.getRootCauseMessage(ex), id);
                    status.incrementVisitedFeatures();
                    
                    continue;
                }

                Map userData = feature.getUserData();
                if (pk != null && userData != null) {
                    userData.put("sourcePks", pk);
                }

                processFeature(feature);

                if (status.getVisitedFeatures() % 10000 == 0) {
                    log.info("" + status.getVisitedFeatures() + "/" + status.getTotalFeatureSize() + " features processed (" + typeName2Read + ")");
                }

                if (status.isInterrupted()) {
                    actionList.flush(status, properties);
                    actionList.processPostCollectionActions(status, properties);
                    
                    throw new InterruptedException("User canceled the process.");
                }
            }
            
            actionList.flush(status, properties);
            
            log.info("Try to do the Post actions");
            actionList.processPostCollectionActions(status, properties);
            
            log.info("Total of: " + status.getVisitedFeatures() + " features processed (" + typeName2Read + ")");
        } finally {
            if (this.writingFromOtherToOracle) {
                updateOracleExtents();
            }
            iterator.close();
            JpaUtilServlet.closeThreadEntityManager();
        }
    }
    
    private void processFeature(SimpleFeature feature) throws Exception {
        if (!mustProcessFeature()) {
            return;
        }

        /*TODO: Onderscheid maken tussen fatal Exception en niet Fatal Exception. Als er een Fatal
         wordt gethrowed dan moet er worden gestopt met features processen.*/

        try {
            EasyFeature ef = new EasyFeature(feature);
            if (ef.getFeature().getDefaultGeometry() != null) {
                // repair if necessary (e.g. no geom metadata in oracle)
                ef.repairGeometry();
            }

            EasyFeature easyFeature = actionList.process(ef);

            // update the oracle boundingBox if necessary
            if (easyFeature != null && this.writingFromOtherToOracle){
                addFeatureToOracleBoundingBox(easyFeature.getFeature());
            }
                // hier niet zetten, maar bij writer action
                //status.incrementProcessedFeatures();


        } catch (IllegalStateException ex) {
            status.addWriteError(ex.getLocalizedMessage(), feature.getID());
            log.error("Cannot write to datastore: ",ex);
            throw ex;
        } catch (Exception e) {
            if (exceptionLogCount++ < MAX_EXCEPTION_LOG_COUNT) {
                log.error("Exception tijdens processen van feature (exception nr. " + exceptionLogCount + " van max " + MAX_EXCEPTION_LOG_COUNT + " die worden gelogd)", e);
            }

            status.addNonFatalError(ExceptionUtils.getRootCauseMessage(e), feature.getID());
        }

        status.incrementVisitedFeatures();
    }

    private boolean mustProcessFeature() {
        return status.getVisitedFeatures() >= status.getFeatureStart() && ((status.getVisitedFeatures() <= status.getFeatureEnd() && status.getFeatureEnd() >= 0)
                || (status.getFeatureEnd() < 0));
    }

    /**
     * Create dataStore2Read from properties
     */
    private DataStore createDataStore2Read(Properties batch) throws Exception {
        return openDataStore(propertiesToMaps(batch, DATASTORE2READ_PREFIX));
    }

    private ActionList createActionList() throws Exception {
        ActionList newActionList = new ActionList();

        String outputWriter = null;

        org.w3c.dom.Element actions = process.getActions();
        if (actions != null) {
            Element actionsElement = new DOMBuilder().build(actions);
            for (Object actionObject : actionsElement.getChildren("action", Namespaces.DSL_NAMESPACE)) {
                Element actionElement = (Element) actionObject;

                Map<String, Object> parameters = new HashMap<String, Object>();
                Element parametersElement = actionElement.getChild("parameters", Namespaces.DSL_NAMESPACE);
                if (parametersElement != null) {
                    for (Object parameterObject : parametersElement.getChildren("parameter", Namespaces.DSL_NAMESPACE)) {
                        Element parameterElement = (Element) parameterObject;

                        String key = parameterElement.getChildTextTrim("paramId", Namespaces.DSL_NAMESPACE);
                        Object value = parameterElement.getChildTextTrim("value", Namespaces.DSL_NAMESPACE);

                        parameters.put(key, value);
                    }
                }

                String actionType = actionElement.getChildTextTrim("type", Namespaces.DSL_NAMESPACE);

                //if (ActionFactory.isDataStoreAction(actionType)) { // lelijke constructie; nooit meer gebruiken aub
                // FIXME: we moeten echt ophouden die oude meuk te ondersteunen.
                // De doorgegeven class moet natuurlijk gewoon de fully qualified class name zijn:
                // i.e.: nl.b3p.datastorelinker.actions.MyAction
                //if (WritableAction.class.isAssignableFrom(Class.forName(actionType))) {
                if (WritableAction.class.isAssignableFrom(
                        Class.forName("nl.b3p.geotools.data.linker.blocks." + actionType))) {
                    outputWriter = actionType;
                } else {
                    newActionList.add(ActionFactory.createAction(actionType, parameters));
                }
            }
        }

        // Finally: add output database to action list:
        if (outputWriter == null) {
            newActionList.add(ActionFactory.createAction(DEFAULT_WRITER,
                    process.toOutputMap()));
        } else {
            newActionList.add(ActionFactory.createAction(outputWriter,
                    process.toOutputMap()));
        }

        log.debug("Actions to be executed (in this order):");
        for (Action action : newActionList) {
            log.debug(action.getName());
        }

        return newActionList;
    }

    private Boolean parseBoolean(String bool) throws Exception {
        if (bool.equalsIgnoreCase("true")) {
            return Boolean.TRUE;
        } else if (bool.equalsIgnoreCase("false")) {
            return Boolean.FALSE;
        } else {
            throw new Exception(MessageFormat.format(resources.getString("parseBooleanFailed"), bool));
        }
    }

    /**
     * Create actionList from batch
     */
    private ActionList createActionList(Properties batch) throws Exception {
        Map<String, Object> actionlistParams = propertiesToMaps(batch, ACTIONLIST_PREFIX + "." + ACTION_PREFIX);
        return createActionList(actionlistParams);
    }

    public static ActionList createActionList(Map<String, Object> actionlistParams) throws Exception {
        ActionList actionList = new ActionList();

        int count = 1;
        while (true) {
            if (actionlistParams.containsKey(Integer.toString(count))) {
                Map<String, Object> params = (Map) actionlistParams.get(Integer.toString(count));

                if (params.containsKey(TYPE_PREFIX)) {

                    if (params.containsKey(SETTINGS_PREFIX)) {
                        if (params.get(TYPE_PREFIX) instanceof String && params.get(SETTINGS_PREFIX) instanceof Map) {
                            actionList.add(ActionFactory.createAction((String) params.get(TYPE_PREFIX), (Map) params.get(SETTINGS_PREFIX)));
                        } else {
                            throw new Exception("Expected " + ACTION_PREFIX + Integer.toString(count) + "." + TYPE_PREFIX + " to be String and " + ACTION_PREFIX + Integer.toString(count) + "." + SETTINGS_PREFIX + " to be a Map");
                        }
                    } else {
                        if (params.get(TYPE_PREFIX) instanceof String) {
                            actionList.add(ActionFactory.createAction((String) params.get(TYPE_PREFIX), new HashMap()));
                        } else {
                            throw new Exception("Expected " + ACTION_PREFIX + Integer.toString(count) + "." + TYPE_PREFIX + " to be String");
                        }
                    }
                    /*
                     if (params.get(TYPE_PREFIX) instanceof String && params.get(SETTINGS_PREFIX) instanceof Map) {
                     actionList.add(ActionFactory.createAction((String) params.get(TYPE_PREFIX), (Map) params.get(SETTINGS_PREFIX)));
                     } else {
                     throw new Exception("Expected " + ACTION_PREFIX + Integer.toString(count) + "." + TYPE_PREFIX + " to be String and " + ACTION_PREFIX + Integer.toString(count) + "." + SETTINGS_PREFIX + " to be a Map");
                     }
                     * */
                } else {
                    throw new Exception("No type found for action" + Integer.toString(count));
                }
            } else {
                break;
            }
            count++;
        }
        return actionList;
    }

    private DataStore openDataStore() throws ConfigurationException, Exception {
        Database database = process.getInput().getDatabase();
        String file = process.getInput().getFile();

        if (database != null) {
            return openDataStore(database);
        } else if (file != null) { // TODO: this should be a file now; change xsd to reflect this "xsd:choice"
            return openDataStore(file);
        } else { // safeguard:
            throw new ConfigurationException("Xml configuration exception: No input database or file specified.");
        }
    }

    public static DataStore openDataStore(Database database) throws ConfigurationException, Exception {
        return openDataStore(database.toGeotoolsDataStoreParametersMap());
    }

    public static DataStore openDataStore(String file) throws ConfigurationException, Exception {
        return openDataStore(Util.fileToMap(new File(file)));
    }

    public static DataStore openDataStore(Map params) throws Exception {
        return openDataStore(params, false);
    }

    /**
     * Open a dataStore (save). If create new is set to true, a datastore wil be
     * created even if the file is not there yet.
     *
     * @param params the datastore parameters
     * @param createNew create new or not
     * @return boolean indicating success
     * @throws Exception generic exception
     */
    public static DataStore openDataStore(Map params, boolean createNew) throws Exception {
        log.debug("openDataStore with: " + params);

        DataStore dataStore = null;

        // TODO: indien je bij het aanmaken van een nieuwe datastore primary
        // keys wil opgeven dan kan dat via PK_METADATA_TABLE.
        // Nu nog buggy, fix in 8.7

        String dbType = (String) params.get("dbtype");
        if (dbType != null && dbType.equals(TYPE_ORACLE)) {
            params.put(OracleNGDataStoreFactory.EXPOSE_PK.key, Boolean.TRUE);
            params.put("min connections", 0);
            params.put("max connections", 50);
            params.put("connection timeout", 60);
            params.put("validate connections", Boolean.FALSE);
        
            log.debug("Created OracleNGDataStoreFactory with: " + params);

            /* TODO: nagaan of je Geotools naar eigen gemaakte
             * geometry columns kunt laten kijken */
            //params.put("Geometry metadata table", "GEOMETRY_COLUMNS");

            return (new OracleNGDataStoreFactory()).createDataStore(params);

        } else if (dbType != null && dbType.equals(TYPE_POSTGIS)) {
            params.put(PostgisNGDataStoreFactory.EXPOSE_PK.key, Boolean.TRUE);
            params.put("min connections", 0);
            params.put("max connections", 50);
            params.put("connection timeout", 60);
            params.put("validate connections", Boolean.FALSE);

            log.debug("Created PostgisNGDataStoreFactory with: " + params);

            return (new PostgisNGDataStoreFactory()).createDataStore(params);
        }

        String errormsg = "DataStore could not be found using parameters";

        try {
            dataStore = DataStoreFinder.getDataStore(params);
            if (dataStore == null && createNew) {
                if (params.containsKey("url")) {
                    String url = (String) params.get("url");
                    if (url.lastIndexOf(".shp") == (url.length() - ".shp".length())) {
                        ShapefileDataStoreFactory factory = new ShapefileDataStoreFactory();
//                        FileDataStoreFactorySpi factory = new IndexedShapefileDataStoreFactory();

                        params.put("url", new File(url).toURL());
                        dataStore = factory.createNewDataStore(params);
                    } else {
                        log.warn("Can not create a new datastore, filetype unknown.");
                    }
                }
            }
        } catch (NullPointerException nullEx) {
            if (!urlExists(params)) {
                throw new Exception("URL in parameters seems to point to a non-existing file \n\n" + params.toString());
            } else {
                throw new Exception(errormsg + " " + params.toString());
            }
        }

        if (dataStore == null) {
            if (!urlExists(params)) {
                throw new Exception("URL in parameters may point to a non-existing file \n\n" + params.toString());
            } else {
                throw new Exception("DataStoreFinder returned null for \n\n" + params.toString());
            }

        } else {
            log.debug("DataStore class: " + dataStore.getClass());
            return dataStore;
        }
    }

    private static boolean urlExists(Map params) {
        try {
            if (params.containsKey("url")) {
                if (params.get("url") instanceof URL) {
                    URL url = (URL) params.get("url");
                    log.debug("Checking url exists: " + url);
                    File file = new File(url.toURI());

                    log.debug("Checking file url exists: " + file.getAbsolutePath());
                    return file.exists();

                } else if (params.get("url") instanceof String) {
                    String url = params.get("url").toString();
                    log.debug("Checking url exists: " + url);
                    File file = new File(url);

                    log.debug("Checking file url exists: " + file.getAbsolutePath());
                    return file.exists();
                }
            }
        } catch (URISyntaxException uriEx) {
            return false;
        }
        return true;
    }

    public static String getSaveProp(Properties batch, String key, String defaultValue) {
        if (batch.containsKey(key)) {
            return batch.getProperty(key);
        } else {
            return defaultValue;
        }
    }

    /**
     * Make HashMap structure from propertylist
     *
     * @param batch Original properties list
     * @param filter Specified to filter start of properties, like
     * 'actionlist.action'
     * @return the mapped propertylist
     * @throws IOException general IOexception
     */
    public static Map<String, Object> propertiesToMaps(Properties batch, String filter) throws IOException {
        Map<String, Object> map = new HashMap();
        Iterator iter = batch.keySet().iterator();

        while (iter.hasNext()) {
            String key = (String) iter.next();

            if (key.startsWith(filter)) {
                String value = batch.getProperty(key);
                String keypart = key.substring(filter.length());
                Map<String, Object> stepIn = map;

                while (keypart.contains(".")) {
                    String step = keypart.substring(0, keypart.indexOf("."));
                    keypart = keypart.substring(keypart.indexOf(".") + 1);

                    if (stepIn.containsKey(step)) {
                        if (stepIn.get(step) instanceof Map) {
                            stepIn = (Map) stepIn.get(step);
                        } else {
                            throw new IOException("Tried to save settingsMap for property '" + key + "', but property has already a value?\nValue = '" + stepIn.get(step).toString() + "'");
                        }
                    } else {
                        Map<String, Object> newStep = new HashMap();
                        stepIn.put(step, newStep);
                        stepIn = newStep;
                    }
                }

                if (value.toLowerCase().equals("false")) {
                    stepIn.put(keypart, false);

                } else if (value.toLowerCase().equals("true")) {
                    stepIn.put(keypart, true);

                } else if (keypart.toLowerCase().equals("url")) {
                    File file = new File(value);
                    if (file.exists()) {
                        stepIn.put(keypart, file.toURL());
                    } else {
                        stepIn.put(keypart, value);
                    }

                } else {
                    stepIn.put(keypart, value);
                }
            }
        }
        return map;
    }

    private String[] getTypeNames() throws IOException {
        String[] typenames = null;
        if (batch != null && batch.containsKey(READ_TYPENAME)) {
            // One table / schema
            typenames = new String[]{
                batch.getProperty(READ_TYPENAME)
            };
        } else if (process != null && process.getInput().getTableName() != null) {
            // One table / schema
            typenames = new String[]{
                process.getInput().getTableName()
            };
        }

        if (typenames == null) {
            // All tables / schema's
            typenames = dataStore2Read.getTypeNames();
        }
        return typenames;
    }

    /**
     * If copying from an non-oracle database to an oracle database, calculate the bounding box while iterating over
     * features.
     * @param simpleFeature The current feature.
     */
    private void addFeatureToOracleBoundingBox(SimpleFeature simpleFeature){

        // first round initiate the bounding box
        if (this.oracleBoundingBox == null){
            this.oracleBoundingBox = simpleFeature.getBounds();
            this.oracleTableName  = simpleFeature.getName().getLocalPart().toUpperCase();
        }
        // then just update
        else {
            this.oracleBoundingBox.include(simpleFeature.getBounds());
        }
    }

    /**
     * Update the oracle metadata table with the current extenst after iterating over all the features.
     */
    private void updateOracleExtents() {
        if (this.writingFromOtherToOracle) {

            // initiate parameters
            DataStore dataStore = null;
            JDBCDataStore jdbcDataStore = null;
            Connection con = null;

            // get the extents of the boundingBox of the features
            double minX = this.oracleBoundingBox.getMinX();
            double maxX = this.oracleBoundingBox.getMaxX();
            double minY = this.oracleBoundingBox.getMinY();
            double maxY = this.oracleBoundingBox.getMaxY();

            // calculate the tolerances the same way org.geotools.data.oracle.OracleDialect does
            double toleranceX = (maxX - minX) / 10000000;
            double toleranceY = (maxY - minY) / 10000000;

            // create the string with the dimensional info
            String dimensionalInfoString = "MDSYS.SDO_DIM_ARRAY( MDSYS.SDO_DIM_ELEMENT('X', " + minX + ", " + maxX + "," +
                    " " + toleranceX + "),MDSYS.SDO_DIM_ELEMENT('Y'," + minY + ", " + maxY + ", " + toleranceY + "))";


            // combine the dimensional string with the update string
            String sqlUpdateString = "UPDATE USER_SDO_GEOM_METADATA SET DIMINFO = " +
                    dimensionalInfoString + " WHERE TABLE_NAME='" + this.oracleTableName + "'";

            // get the parameters from the database output
            Map parameters = process.getOutput().getDatabase().toGeotoolsDataStoreParametersMap();
            // but change schema because updating metadata
            parameters.put("schema", "MDSYS");

            try {
                dataStore = openDataStore(parameters);
                jdbcDataStore = (JDBCDataStore) dataStore;
                con = jdbcDataStore.getDataSource().getConnection();
                con.setAutoCommit(true);

                PreparedStatement ps = con.prepareStatement(sqlUpdateString);
                ps.execute();

            } catch (Exception ex) {
                log.error(ex);

            } finally {
                if (jdbcDataStore != null) {
                    jdbcDataStore.closeSafe(con);
                    dataStore.dispose();

                }
            }

        }
    }

}
