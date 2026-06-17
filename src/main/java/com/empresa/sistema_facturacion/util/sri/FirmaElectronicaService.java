package com.empresa.sistema_facturacion.util.sri;

import com.empresa.sistema_facturacion.entity.ConfiguracionSRI;
import com.empresa.sistema_facturacion.repository.ConfiguracionSRIRepository;
import com.empresa.sistema_facturacion.util.EncryptionUtil;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import xades4j.algorithms.EnvelopedSignatureTransform;
import xades4j.production.DataObjectReference;
import xades4j.production.SignedDataObjects;
import xades4j.production.XadesBesSigningProfile;
import xades4j.production.XadesSigner;
import xades4j.properties.DataObjectDesc;
import xades4j.providers.KeyingDataProvider;
import xades4j.providers.impl.DirectKeyingDataProvider;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

@Service
public class FirmaElectronicaService {

    private final ConfiguracionSRIRepository configRepository;
    private final EncryptionUtil encryptionUtil;

    public FirmaElectronicaService(ConfiguracionSRIRepository configRepository, EncryptionUtil encryptionUtil) {
        this.configRepository = configRepository;
        this.encryptionUtil = encryptionUtil;
    }

    public String firmarDocumentoXml(String xmlPlano) {
        try {
            // Obtener P12 y clave de la db
            ConfiguracionSRI config = configRepository.findTopByOrderByIdDesc();
            if (config == null || config.getArchivoP12() == null) {
                throw new RuntimeException("No se encontró el archivo .p12 en la configuración.");
            }
            
            // DESENCRIPTACIÓN
            String password;
            try {
                password = encryptionUtil.desencriptar(config.getPasswordP12());
            } catch (Exception e) {
                password = config.getPasswordP12();
            }

            // Parsear el XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document docToSign = builder.parse(new ByteArrayInputStream(xmlPlano.getBytes("UTF-8")));

            Element rootElement = docToSign.getDocumentElement();
            if (!rootElement.hasAttribute("id")) {
                rootElement.setAttribute("id", "comprobante");
            }
            rootElement.setIdAttribute("id", true);

            // Cargar el KeyStore (Formato PKCS12)
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(new ByteArrayInputStream(config.getArchivoP12()), password.toCharArray());

            // Buscar el alias del certificado válido para firma
            // enontrar la parte del certificado que permita firmar e emitir documentos
            String alias = null;
            Enumeration<String> aliases = ks.aliases();
            while (aliases.hasMoreElements()) {
                String a = aliases.nextElement();
                if (ks.isKeyEntry(a)) {
                    X509Certificate certTemporal = (X509Certificate) ks.getCertificate(a);
                    boolean[] keyUsage = certTemporal.getKeyUsage();

                    // Bit 0 = Digital Signature, Bit 1 = Non-Repudiation
                    if (keyUsage != null && (keyUsage[0] || keyUsage[1])) {
                        alias = a;
                        break;
                    }
                }
            }
            if (alias == null) {
                throw new RuntimeException("El archivo .p12 no contiene llaves privadas válidas para firma.");
            }

            PrivateKey privateKey = (PrivateKey) ks.getKey(alias, password.toCharArray());
            X509Certificate certificate = (X509Certificate) ks.getCertificate(alias);

            // Configurar el proveedor de claves para xades4j
            KeyingDataProvider kp = new DirectKeyingDataProvider(certificate, privateKey);
            XadesBesSigningProfile profile = new XadesBesSigningProfile(kp);
            XadesSigner signer = profile.newSigner();

            //Configurar los parámetros de la firma XAdES-BES (Enveloped)
            DataObjectDesc objRef = new DataObjectReference("#comprobante")
                    .withTransform(new EnvelopedSignatureTransform());
            SignedDataObjects dataObjs = new SignedDataObjects(objRef);

            // Firmar el documento
            signer.sign(dataObjs, rootElement);

            // Transformar el  firmado a String XML
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer trans = tf.newTransformer();
            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            trans.setOutputProperty(OutputKeys.STANDALONE, "no");

            StringWriter sw = new StringWriter();
            trans.transform(new DOMSource(docToSign), new StreamResult(sw));

            return sw.toString().replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");

        } catch (Exception e) {
            throw new RuntimeException("Error en firma electrónica XAdES-BES: " + e.getMessage(), e);
        }
    }
}
