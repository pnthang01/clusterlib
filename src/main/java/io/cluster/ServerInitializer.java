/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.cluster;

import io.cluster.node.manager.SuperviseManager;
import java.io.IOException;

/**
 *
 * @author thangpham
 */
public class ServerInitializer {

    public static void main(String[] args) {
        String baseConfig = null;
        if (args.length > 0) {
            baseConfig = args[0];
        }
        try {
            ClusterLibInitializer cli = new ClusterLibInitializer(baseConfig);
            SuperviseManager.load();
            cli.run();
        } catch (IOException ex) {
            System.err.println("Could not initialize server");
            ex.printStackTrace();
        }
    }
    
}
