package co.edu.univalle.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * GET /api/demo/info
 * Devuelve el equipo y el nombre del pod que atendió la petición.
 * Sirve como evidencia de que el balanceador reparte el tráfico entre las 2 réplicas.
 */
@RestController
@CrossOrigin
public class InfoController {

    @Value("${team.name:Equipo sin nombre}")
    private String teamName;

    private final Environment environment;

    public InfoController(Environment environment) {
        this.environment = environment;
    }

    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("team", teamName);
        info.put("pod", hostname());
        info.put("profile", Arrays.toString(environment.getActiveProfiles()));
        return info;
    }

    private static String hostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
