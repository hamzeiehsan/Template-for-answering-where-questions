package anonymous.gazetteers.osm;

import anonymous.gazetteers.osm.model.OSMModel;
import anonymous.Config;
import anonymous.gazetteers.geonames.model.ToponymExportModel;
import anonymous.parser.model.ResultModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import fr.dudie.nominatim.client.JsonNominatimClient;
import fr.dudie.nominatim.client.request.NominatimSearchRequest;
import fr.dudie.nominatim.client.request.paramhelper.PolygonFormat;
import fr.dudie.nominatim.model.Address;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.geonames.Toponym;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class Analyze {
    private static final Logger LOG = LoggerFactory.getLogger(Analyze.class);
    private static Gson GSON = new GsonBuilder().serializeSpecialFloatingPointValues().setLenient().setPrettyPrinting().serializeNulls().create();
    private static List<String> invalidFeatureCode = new ArrayList<>();

    static {
        invalidFeatureCode.add("PPL");
        invalidFeatureCode.add("PPLA");
        invalidFeatureCode.add("PPLA2");
        invalidFeatureCode.add("PPLA3");
        invalidFeatureCode.add("PPLA4");
        invalidFeatureCode.add("PPLC");
        invalidFeatureCode.add("PPLCH");
        invalidFeatureCode.add("PPLF");
        invalidFeatureCode.add("PPLG");
        invalidFeatureCode.add("PPLH");
        invalidFeatureCode.add("PPLL");
        invalidFeatureCode.add("PPLQ");
        invalidFeatureCode.add("PPLR");
        invalidFeatureCode.add("PPLS");
        invalidFeatureCode.add("PPLW");
        invalidFeatureCode.add("PPLX");
        invalidFeatureCode.add("STLMT");
    }

    public static void main(String[] args) throws IOException {
        LOG.info("Testing!");
        Properties PROPS = new Properties();
        PROPS.setProperty("nominatim.server.url", "https://nominatim.openstreetmap.org/");
        PROPS.setProperty("nominatim.headerEmail", Config.OSM_API_KEY);

        LOG.info("Preparing http client");
        final SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        final ClientConnectionManager connexionManager = new SingleClientConnManager(null, registry);

        final HttpClient httpClient = new DefaultHttpClient(connexionManager, null);

        final String baseUrl = PROPS.getProperty("nominatim.server.url");
        final String email = PROPS.getProperty("nominatim.headerEmail");
        JsonNominatimClient nominatimClient = new JsonNominatimClient(baseUrl, httpClient, email);


        LOG.info("reading result set");
        Type resultType = new TypeToken<ArrayList<ResultModel>>() {
        }.getType();
        JsonReader reader = new JsonReader(new FileReader(Config.PARSER_2019_FILE_PATH));
        List<ResultModel> results = GSON.fromJson(reader, resultType);

        LOG.info("Reading all disambiguated values!");
        JsonReader disamReader = new JsonReader(new FileReader(Config.GAZETTEERS_FOLDER+"all-disambiguated-values.json"));
        Type disambType = new TypeToken<HashMap<Long, Map<String, Map<String, Toponym>>>>() {
        }.getType();
        Map<Long, Map<String, Map<String, Toponym>>> allDisambiguatedValues = GSON.fromJson(disamReader, disambType);


        NominatimSearchRequest request1 = new NominatimSearchRequest();
        request1.setPolygonFormat(PolygonFormat.NONE);
        request1.setQuery("Asia");
        List<Address> addresses1 = nominatimClient.search(request1);

        LOG.info("reading the database!");
        Type dictType = new TypeToken<Map<String, ToponymExportModel>>() {
        }.getType();
        JsonReader dictReader = new JsonReader(new FileReader(Config.GAZETTEERS_FOLDER+"geonames.json"));
        Map<String, ToponymExportModel> database = GSON.fromJson(dictReader, dictType);

        Type osmType = new TypeToken<List<OSMModel>>() {
        }.getType();
        JsonReader osmReader = new JsonReader(new FileReader(Config.GAZETTEERS_FOLDER+"osm.json"));
        List<OSMModel> osmModels = GSON.fromJson(osmReader, osmType);

        Map<String, OSMModel> osmDictionary = new HashMap<>();
        for (OSMModel m : osmModels)
            osmDictionary.put(m.getName(), m);

        LOG.info("database size: " + database.size());
        int counter = 0;
        for (String db : database.keySet()) {
            ToponymExportModel val = database.get(db);
            String query = val.getName();
            counter++;
            LOG.info("counter: " + counter + "-- query: " + query);
            if (!osmDictionary.containsKey(query)) {
                NominatimSearchRequest request = new NominatimSearchRequest();
                request.setPolygonFormat(PolygonFormat.NONE);
                request.setQuery(query);
                List<Address> addresses = nominatimClient.search(request);
                OSMModel osmModel = new OSMModel(query, addresses);
                String str = GSON.toJson(osmModel);
                anonymous.gazetteers.geonames.Analyze.appendStrToFile(Config.GAZETTEERS_FOLDER+"osm", str + ",");
            }
        }
    }
}
