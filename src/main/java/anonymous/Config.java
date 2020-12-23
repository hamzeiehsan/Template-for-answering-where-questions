package anonymous;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Properties;

/**
 * Modified by [anonymous] [anonymous] on 20/01/2020.
 * loading configuration file about the location of MS MACRO dataset
 */
public class Config {
    private static final Logger LOG = LoggerFactory.getLogger(Config.class);

    //Dataset Parameters
    public static String TRAIN_DATA_FILE_PATH;
    public static String TEST_DATA_FILE_PATH;
    public static String DEV_DATA_FILE_PATH;

    //Parser results
    public static String PARSER_2019_FILE_PATH;


    //Output folders
    public static String PARSER_OUTPUT_FOLDER;
    public static String GAZETTEERS_FOLDER;
    public static String SEQUENCE_OUTPUT_FOLDER;
    public static String PREDICTION_OUTPUT_FOLDER;

    //API keys
    public static String GEONAMES_API_KEY;
    public static String OSM_API_KEY;


    static {
        try {
            InputStream input = new FileInputStream("src/main/resources/properties.properties");
            Properties properties = new Properties();
            properties.load(input);
            Field[] datasetFields = Config.class.getFields();
            for (Field f : datasetFields) {
                Class<?> type = f.getType();
                if (type == String.class) {
                    LOG.debug("String Field :: " + f.getName());
                    f.set(Config.class, properties.get(f.getName()));
                } else if (type == Integer.class) {
                    LOG.debug("Integer Field :: " + f.getName());
                    f.set(Config.class, Integer.parseInt(properties.get(f.getName()).toString()));
                }
            }
        } catch (IOException e) {
            LOG.error("IOException for Loading the properties file :| the error message is : " + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            LOG.error("Illegal Access Exception for Setting the properties value for DatasetConfing class, the error message is : " + e.getMessage(), e);
        }
    }
}