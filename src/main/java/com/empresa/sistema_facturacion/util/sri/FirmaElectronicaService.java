package com.empresa.sistema_facturacion.util.sri;

import com.empresa.sistema_facturacion.entity.ConfiguracionSRI;
import com.empresa.sistema_facturacion.repository.ConfiguracionSRIRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FirmaElectronicaService {

    private final ConfiguracionSRIRepository configuracionRepository;

    public String firmarDocumentoXml(String xmlPlano) {
        try {
            ConfiguracionSRI config = configuracionRepository.findTopByOrderByIdDesc();
            if (config == null || config.getArchivoP12() == null) {
                throw new RuntimeException("No se encontró la firma electrónica (.p12) resguardada en la base de datos.");
            }

            byte[] p12Bytes = config.getArchivoP12();
            char[] password = config.getPasswordP12().toCharArray();

            // 2. Cargar el KeyStore PKCS12 directamente desde los bytes en memoria
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(new ByteArrayInputStream(p12Bytes), password);

            // 3. Extraer el Alias, la Llave Privada y el Certificado X509
            String alias = Collections.list(keyStore.aliases()).stream()
                    .filter(a -> {
                        try { return keyStore.isKeyEntry(a); } catch (Exception e) { return false; }
                    })
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No se encontró una llave válida en el certificado p12."));

            PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, password);
            X509Certificate cert = (X509Certificate) keyStore.getCertificate(alias);

            // 4. Convertir el String XML plano en un Objeto Document (DOM) de Java
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            Document doc = dbf.newDocumentBuilder().parse(new org.xml.sax.InputSource(new StringReader(xmlPlano)));

            DOMSignContext signContext = new DOMSignContext(privateKey, doc.getDocumentElement());

            XMLSignatureFactory signatureFactory = XMLSignatureFactory.getInstance("DOM");

            Transform transform = signatureFactory.newTransform(
                    Transform.ENVELOPED, (TransformParameterSpec) null);

            Reference reference = signatureFactory.newReference(
                    "", signatureFactory.newDigestMethod(DigestMethod.SHA1, null),
                    Collections.singletonList(transform), null, null);

            SignedInfo signedInfo = signatureFactory.newSignedInfo(
                    signatureFactory.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
                    signatureFactory.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
                    Collections.singletonList(reference));

            KeyInfoFactory kif = signatureFactory.getKeyInfoFactory();
            X509Data x509Data = kif.newX509Data(Collections.singletonList(cert));
            KeyInfo keyInfo = kif.newKeyInfo(Collections.singletonList(x509Data));

            XMLSignature signature = signatureFactory.newXMLSignature(signedInfo, keyInfo);
            signature.sign(signContext);

            // 7. Convertir el objeto DOM firmado de regreso a un String limpio
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));

            return writer.toString();

        } catch (Exception e) {
            throw new RuntimeException("Error crítico durante el proceso de firma digital XAdES-BES: " + e.getMessage(), e);
        }
    }
}
