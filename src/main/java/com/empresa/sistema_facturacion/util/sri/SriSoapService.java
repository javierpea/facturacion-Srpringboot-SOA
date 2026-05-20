package com.empresa.sistema_facturacion.util.sri;

import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class SriSoapService {

    private static final String URL_RECEPCION_PRUEBAS = "https://celcer.sri.gob.ec/comprobantes-electronicos-ws/RecepcionComprobantesOffline";
    private static final String URL_AUTORIZACION_PRUEBAS = "https://celcer.sri.gob.ec/comprobantes-electronicos-ws/AutorizacionComprobantesOffline";

    private static final String URL_RECEPCION_PROD = "https://cel.sri.gob.ec/comprobantes-electronicos-ws/RecepcionComprobantesOffline";
    private static final String URL_AUTORIZACION_PROD = "https://cel.sri.gob.ec/comprobantes-electronicos-ws/AutorizacionComprobantesOffline";

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public String enviarARecepcion(String xmlFirmado, String ambiente) {
        String urlEndpoint = ambiente.equals("1") ? URL_RECEPCION_PRUEBAS : URL_RECEPCION_PROD;

        // El SRI exige estrictamente que el XML firmado viaje encriptado/codificado en Base64
        String xmlBase64 = Base64.getEncoder().encodeToString(xmlFirmado.getBytes(StandardCharsets.UTF_8));

        // Construcción del sobre SOAP oficial para la recepción
        String soapEnvelope =
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ec=\"http://ec.gob.sri.ws.recepcion\">" +
                        "   <soapenv:Header/>" +
                        "   <soapenv:Body>" +
                        "      <ec:validarComprobante>" +
                        "         <comprobante>" + xmlBase64 + "</comprobante>" +
                        "      </ec:validarComprobante>" +
                        "   </soapenv:Body>" +
                        "</soapenv:Envelope>";

        return webServicePost(urlEndpoint, soapEnvelope);
    }

    public String consultarAutorizacion(String claveAcceso, String ambiente) {
        String urlEndpoint = ambiente.equals("1") ? URL_AUTORIZACION_PRUEBAS : URL_AUTORIZACION_PROD;

        String soapEnvelope =
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ec=\"http://ec.gob.sri.ws.autorizacion\">" +
                        "   <soapenv:Header/>" +
                        "   <soapenv:Body>" +
                        "      <ec:autorizacionComprobante>" +
                        "         <claveAccesoComprobante>" + claveAcceso + "</claveAccesoComprobante>" +
                        "      </ec:autorizacionComprobante>" +
                        "   </soapenv:Body>" +
                        "</soapenv:Envelope>";

        return webServicePost(urlEndpoint, soapEnvelope);
    }

    private String webServicePost(String urlEndpoint, String soapEnvelope) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlEndpoint))
                    .header("Content-Type", "text/xml;charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(soapEnvelope, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return response.body(); // Retorna la respuesta XML cruda del SRI
            } else {
                throw new RuntimeException("Error en la conexión con el SRI. Código HTTP: " + response.statusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("No se pudo establecer comunicación con los servidores del SRI: " + e.getMessage(), e);
        }
    }
}
