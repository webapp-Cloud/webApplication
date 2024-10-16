package com.rio_rishabhNEU.UserApp;

import org.springframework.context.annotation.Configuration;

@Configuration
public class SimulationConfig {
    private static boolean simulateDbDisconnection = false;

    public static boolean isSimulateDbDisconnection() {
        return simulateDbDisconnection;
    }

    public static void setSimulateDbDisconnection(boolean simulate) {
        simulateDbDisconnection = simulate;
    }
}
