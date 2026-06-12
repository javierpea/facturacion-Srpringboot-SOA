package com.empresa.sistema_facturacion.util.sri;

import com.empresa.sistema_facturacion.entity.ConfiguracionSRI;
import com.empresa.sistema_facturacion.repository.ConfiguracionSRIRepository;
import es.mityc.firmaJava.libreria.xades.DataToSign;
import es.mityc.firmaJava.libreria.xades.FirmaXML;
import es.mityc.firmaJava.libreria.xades.XAdESSchemas;
import es.mityc.javasign.EnumFormatoFirma;
import es.mityc.javasign.pkstore.IPKStoreManager;
import es.mityc.javasign.pkstore.IPassStoreKS;
import es.mityc.javasign.pkstore.keystore.KSStore;
import es.mityc.javasign.xml.refs.InternObjectToSign;
import es.mityc.javasign.xml.refs.ObjectToSign;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
import java.security.Provider;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

@Service
public class FirmaElectronicaService {

    private final ConfiguracionSRIRepository configRepository;
    private final com.empresa.sistema_facturacion.util.EncryptionUtil encryptionUtil;

    public FirmaElectronicaService(ConfiguracionSRIRepository configRepository, com.empresa.sistema_facturacion.util.EncryptionUtil encryptionUtil) {
        this.configRepository = configRepository;
        this.encryptionUtil = encryptionUtil;
    }

    public String firmarDocumentoXml(String xmlPlano) {
        try {
            // 1. Obtener P12 y clave de la base de datos
            ConfiguracionSRI config = configRepository.findTopByOrderByIdDesc();
            if (config == null || config.getArchivoP12() == null) {
                throw new RuntimeException("No se encontró el archivo .p12 en la configuración.");
            }
            
            // DESENCRIPTACIÓN: Recuperamos la clave real usando nuestra utilidad
            String password = encryptionUtil.desencriptar(config.getPasswordP12());

            // 2. Parsear el XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document docToSign = builder.parse(new ByteArrayInputStream(xmlPlano.getBytes("UTF-8")));

            // Identificar el nodo a firmar (Exigencia SRI)
            Element rootElement = docToSign.getDocumentElement();
            if (!rootElement.hasAttribute("id")) {
                rootElement.setAttribute("id", "comprobante");
            }
            rootElement.setIdAttribute("id", true);

            // 3. Cargar el KeyStore (Formato PKCS12)
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(new ByteArrayInputStream(config.getArchivoP12()), password.toCharArray());

            // Buscar el alias del certificado CORRECTO
            String alias = null;
            Enumeration<String> aliases = ks.aliases();
            while (aliases.hasMoreElements()) {
                String a = aliases.nextElement();
                if (ks.isKeyEntry(a)) {
                    X509Certificate certTemporal = (X509Certificate) ks.getCertificate(a);
                    boolean[] keyUsage = certTemporal.getKeyUsage();

                    // En Java, el bit 0 es 'Digital Signature' y el bit 1 es 'Non-Repudiation'
                    if (keyUsage != null && (keyUsage[0] || keyUsage[1])) {
                        alias = a;
                        break;
                    }
                }
            }
            if (alias == null) {
                throw new RuntimeException("El archivo .p12 no contiene llaves privadas.");
            }

            X509Certificate certificate = (X509Certificate) ks.getCertificate(alias);

            // 4. Configurar el gestor de claves de MITyCLib
            IPKStoreManager storeManager = new KSStore(ks, new PassStoreKS(password));
            PrivateKey privateKey = storeManager.getPrivateKey(certificate);
            Provider provider = storeManager.getProvider(certificate);

            // 5. Configurar los parámetros de la firma XAdES-BES
            DataToSign dataToSign = new DataToSign();
            dataToSign.setXadesFormat(EnumFormatoFirma.XAdES_BES);
            dataToSign.setEsquema(XAdESSchemas.XAdES_132); // Versión SRI Ecuador
            dataToSign.setXMLEncoding("UTF-8");
            dataToSign.setEnveloped(true);
            dataToSign.addObject(new ObjectToSign(new InternObjectToSign("comprobante"), "comprobante", null, "text/xml", null));
            dataToSign.setParentSignNode("comprobante");
            dataToSign.setDocument(docToSign);

            // 6. Firmar el documento
            FirmaXML firma = new FirmaXML();
            Object[] res = firma.signFile(certificate, dataToSign, privateKey, provider);
            Document docSigned = (Document) res[0];

            // 7. Transformar el documento firmado a String XML
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer trans = tf.newTransformer();
            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            trans.setOutputProperty(OutputKeys.STANDALONE, "no");

            StringWriter sw = new StringWriter();
            trans.transform(new DOMSource(docSigned), new StreamResult(sw));

            // Ajuste final para la cabecera exigida por el SRI
            return sw.toString().replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
                    "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");

        } catch (Exception e) {
            throw new RuntimeException("Error en firma MITyCLib XAdES-BES: " + e.getMessage(), e);
        }
    }

    // =========================================================================
    // CLASE AUXILIAR INTERNA REQUERIDA POR MITyCLib PARA MANEJO DE CONTRASEÑAS
    // =========================================================================
    private static class PassStoreKS implements IPassStoreKS {
        private transient String password;

        public PassStoreKS(String password) {
            this.password = password;
        }

        @Override
        public char[] getPassword(X509Certificate certificate, String alias) {
            return password.toCharArray();
        }
    }
}