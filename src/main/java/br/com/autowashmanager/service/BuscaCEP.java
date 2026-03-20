/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.autowashmanager.service;

import br.com.autowashmanager.model.Endereco;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

/**
 *
 * @author h24he
 */
public class BuscaCEP {

    public static Endereco buscar(String cep) {

// remove tudo que não for número
        cep = cep.replaceAll("[^0-9]", "");

        if (cep.length() != 8) {
            throw new IllegalArgumentException("CEP inválido");
        }

        try {
            String urlStr = "https://viacep.com.br/ws/" + cep + "/json/";
            URL url = new URL(urlStr);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "UTF-8")
            );

            StringBuilder resposta = new StringBuilder();
            String linha;

            while ((linha = reader.readLine()) != null) {
                resposta.append(linha);
            }

            JSONObject json = new JSONObject(resposta.toString());

            if (json.has("erro")) {
                return null;
            }

            return new Endereco(
                    json.optString("logradouro"),
                    json.optString("bairro"),
                    json.optString("localidade"),
                    json.optString("uf")
            );

        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar CEP", e);
        }
    }
}
