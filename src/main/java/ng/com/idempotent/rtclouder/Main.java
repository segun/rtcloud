/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ng.com.idempotent.rtclouder;

import java.io.IOException;
import java.net.URISyntaxException;
import ng.com.idempotent.utils.GetTweets;

/**
 *
 * @author aardvocate
 */
public class Main {
    public static void main(String args[]) throws URISyntaxException, IOException {
        String username = "realDonaldTrump";
        GetTweets.wordCloud(username, true);
    }
}
