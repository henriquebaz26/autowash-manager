/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.autowashmanager.service;

import br.com.autowashmanager.model.Clima;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author h24he
 */
import org.json.JSONObject;

public class ClimaService {

    private static final String API_KEY = "386dc0a963b9900b3f828a2e9a69d0e7";

    public static Clima obterClimaHoje() {
        try {
            // 1. Localização via IP
            URL urlLocal = new URL("http://ip-api.com/json/");
            HttpURLConnection connLocal = (HttpURLConnection) urlLocal.openConnection();
            connLocal.setRequestMethod("GET");

            BufferedReader readerLocal = new BufferedReader(
                    new InputStreamReader(connLocal.getInputStream())
            );

            StringBuilder responseLocal = new StringBuilder();
            String line;
            while ((line = readerLocal.readLine()) != null) {
                responseLocal.append(line);
            }

            JSONObject jsonLocal = new JSONObject(responseLocal.toString());
            String cidade = jsonLocal.getString("city");

            // encode da cidade
            String cidadeEncoded = URLEncoder.encode(cidade, StandardCharsets.UTF_8);

            // 2. Clima
            String urlClimaStr = String.format(
                    "https://api.openweathermap.org/data/2.5/weather?q=%s,BR&appid=%s&units=metric&lang=pt_br",
                    cidadeEncoded, API_KEY
            );

            URL urlClima = new URL(urlClimaStr);
            HttpURLConnection connClima = (HttpURLConnection) urlClima.openConnection();
            connClima.setRequestMethod("GET");

            // 🔥 CORREÇÃO: valida status
            int status = connClima.getResponseCode();
            if (status != 200) {
                System.out.println("Erro API clima: HTTP " + status);
                return null;
            }

            BufferedReader readerClima = new BufferedReader(
                    new InputStreamReader(connClima.getInputStream())
            );

            StringBuilder responseClima = new StringBuilder();
            while ((line = readerClima.readLine()) != null) {
                responseClima.append(line);
            }

            JSONObject jsonClima = new JSONObject(responseClima.toString());

            double temperatura = jsonClima.getJSONObject("main").getDouble("temp");
            String descricao = jsonClima.getJSONArray("weather")
                    .getJSONObject(0)
                    .getString("description");

            return new Clima(cidade, temperatura, descricao);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
