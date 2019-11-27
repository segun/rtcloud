/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ng.com.idempotent.utils;

import com.kennycason.kumo.CollisionMode;
import com.kennycason.kumo.WordCloud;
import com.kennycason.kumo.WordFrequency;
import com.kennycason.kumo.bg.CircleBackground;
import com.kennycason.kumo.font.scale.SqrtFontScalar;
import com.kennycason.kumo.palette.ColorPalette;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.net.ssl.SSLContext;
import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author aardvocate
 */
public class GetTweets {

    final static Logger LOG = Logger.getLogger(GetTweets.class.getName());
    final static ObjectMapper OM = new ObjectMapper();

    public static CloseableHttpClient registerHttps() {
        int timeout = 60000;
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout * 1000)
                .setConnectionRequestTimeout(timeout * 1000)
                .setSocketTimeout(timeout * 1000).build();
        CloseableHttpClient client = null;
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, (certificate, authType) -> true).build();

            client = HttpClients.custom()
                    .setSSLContext(sslContext)
                    .setSSLHostnameVerifier(new NoopHostnameVerifier())
                    .setDefaultRequestConfig(config)
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return client;
    }

    public static BufferedImage wordCloud(String username, boolean createFile) throws URISyntaxException, IOException {        
        //final FrequencyAnalyzer frequencyAnalyzer = new FrequencyAnalyzer();        
        final List<WordFrequency> wordFrequencies = countWords(getTweets(username).stream().collect(Collectors.joining(" "))); //frequencyAnalyzer.load(GetTweets.getTweets(username));
        final Dimension dimension = new Dimension(600, 600);
        final WordCloud wordCloud = new WordCloud(dimension, CollisionMode.PIXEL_PERFECT);
        wordCloud.setPadding(2);
        wordCloud.setBackground(new CircleBackground(300));
        wordCloud.setColorPalette(new ColorPalette(new Color(0x4055F1), new Color(0x408DF1), new Color(0x40AAF1), new Color(0x40C5F1), new Color(0x40D3F1), new Color(0xFFFFFF)));
        wordCloud.setFontScalar(new SqrtFontScalar(10, 40));
        wordCloud.build(wordFrequencies);        
        if(createFile) {
            wordCloud.writeToFile(username + ".png");
        }
        return wordCloud.getBufferedImage();
    }

    public static List<WordFrequency> countWords(String tweets) {
        HashMap<String, Integer> wordsMap = new HashMap<>();
        StringTokenizer tokenizer = new StringTokenizer(tweets.replaceAll("[^A-Za-z0-9]", " "));
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            //remove words like you, the, a, an, of, etc. 
            if (token.length() <= 3) {
                continue;
            }
            
            if(token.equals("https")) {
                continue;
            }
            if (wordsMap.containsKey(token)) {
                int count = wordsMap.get(token);
                wordsMap.put(token, ++count);
            } else {
                wordsMap.put(token, 1);
            }
        }
        //return wordsMap;
        List<WordFrequency> wordFrequencies = new ArrayList<>();
        
        wordsMap.keySet().stream().forEach(key -> {
            wordFrequencies.add(new WordFrequency(key, wordsMap.get(key)));
        });
        
        return wordFrequencies;
        
    }

    //GET https://api.twitter.com/1.1/statuses/user_timeline.json?screen_name=twitterapi&count=2
    public static List<String> getTweets(String username) throws URISyntaxException, IOException {
        CloseableHttpClient client = registerHttps();
        List<NameValuePair> parameters = new ArrayList<>();

        parameters.add(new BasicNameValuePair("screen_name", username));
        //parameters.add(new BasicNameValuePair("include_rts", "false"));        
        parameters.add(new BasicNameValuePair("tweet_mode", "extended"));
        parameters.add(new BasicNameValuePair("count", "200"));

        URIBuilder query = new URIBuilder("https://api.twitter.com/1.1/statuses/user_timeline.json");
        query.addParameters(parameters);

        HttpGet getRequest = new HttpGet(query.build());
        getRequest.setHeader("content-type", "application/json");
        getRequest.setHeader("Accept", "application/json");
        getRequest.setHeader("Authorization", "Bearer " + "AAAAAAAAAAAAAAAAAAAAAC714QAAAAAAM%2FRkaAALasgNhofG5sxgS%2Bljc04%3DyF7pWr3DXDw2ZTZSJ5NcySyAGnuWsGFaeofJzmZGwcFUbmq3DR");

        CloseableHttpResponse httpResponse = client.execute(getRequest);
        InputStream is = httpResponse.getEntity().getContent();
        String s = IOUtils.toString(is, "UTF-8");
        List<HashMap<String, Object>> response = OM.readValue(s, List.class);
        List<String> tweets = new ArrayList<>();        
        response.stream().forEach(res -> {
            tweets.add(res.get("full_text").toString());
        });
                        
        return tweets;
    }
}
