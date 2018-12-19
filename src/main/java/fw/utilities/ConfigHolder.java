package fw.utilities;



import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Properties;

public class ConfigHolder {
    private static final ConfigHolder INSTANCE = new ConfigHolder();
    private static Properties configProp;
    private static Properties testProp;
    private Properties prop = null;


    private ConfigHolder(){
        // Singleton

        //Initialize the required files
        try {
            String configFilePath = System.getProperty("user.dir")+ File.separator + "src" + File.separator
                    + "test" + File.separator + "resources" + File.separator + "config" + File.separator
                    + "config.properties";

            File config = new File(configFilePath);
            if(!config.exists())
                throw new RuntimeException("Properties file '" + configFilePath +"' does not exist.");

            configProp = new Properties();
            configProp.load(new FileInputStream(configFilePath));

            String  env = System.getProperty("test.env");
            if(env == null || env.trim().isEmpty())
                env = (String) configProp.getProperty("test.env");

            if(env == null || env.trim().isEmpty())
                throw new RuntimeException("Value for property 'testProp.env' missing.");

            env = env.trim();

            String testFilePath = System.getProperty("user.dir")+ File.separator + "src" + File.separator
                    + "test" + File.separator + "resources" + File.separator + "config" + File.separator
                    + env + ".properties";
            File file = new File(testFilePath);
            if(!file.exists())
                throw new RuntimeException("Properties file '" + testFilePath +"' does not exist.");
            testProp = new Properties();
            testProp.load(new FileInputStream(testFilePath));

        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    public Properties getProp() {
        return prop;
    }

    public void setProp(Properties prop) {
        this.prop = prop;
    }

    public Object get(String key){
        //search for keys case-insensitive
        for(Object tempKey : this.prop.keySet()){
            if(((String) tempKey).equalsIgnoreCase(key))
                return this.prop.get(tempKey);
        }
        return null;
    }

    public static ConfigHolder getInstance() {
        return INSTANCE;
    }

    public String getBaseUrl(){
        String baseUrl = null;
        Properties prop = getProp();
        baseUrl = prop == null ? baseUrl : prop.getProperty("base_url");
        return baseUrl;
    }

    //newly added functions


    /** Call this function to retrieve values from both configPath & system properties always
     * @author bmani
     * @param key
     * @return
     */
    public String getProperty(String key){
        return getProperty(key,true);
    }

    /** Call this function to decide whether to look for values  in configPath
     * properties or include system properties
     * @author bmani
     * @param key
     * @param includeSystemProperty
     * @return
     */
    public String getProperty(String key, boolean includeSystemProperty){
        String value = null;
        if(includeSystemProperty && System.getProperties().containsKey(key)) {
            value = System.getProperty(key);
            if (value != null && !value.trim().isEmpty())
                return value;
        }

        if(configProp != null){
            value = (String) configProp.getProperty(key);
            if (value != null && !value.trim().isEmpty())
                return value;
        }

        if(testProp != null){
            value = (String) testProp.getProperty(key);
            if (value != null && !value.trim().isEmpty())
                return value;
        }
//
//        //search for keys case-insensitive
//        for(Object tempKey : this.prop.keySet()) {
//            if (((String) tempKey).equalsIgnoreCase(key))
//                return this.prop.getProperty((String)tempKey);
//        }
        return null;
    }

    public static void main(String[] args) {
        ConfigHolder holder = ConfigHolder.getInstance();
        System.out.println("Value: " + holder.getProperty("GRID.execution"));
        System.out.println("Value: " + holder.getProperty("grid.execution"));
    }
}
