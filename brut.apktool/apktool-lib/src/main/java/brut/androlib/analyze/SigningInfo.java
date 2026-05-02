package brut.androlib.analyze;

import java.util.ArrayList;
import java.util.List;

public class SigningInfo {
    private boolean v1Scheme;
    private boolean v2Scheme;
    private boolean v3Scheme;
    private List<CertificateInfo> certificates = new ArrayList<>();
    private String signingCertificateDigest;

    public boolean isV1Scheme() { return v1Scheme; }
    public void setV1Scheme(boolean v1Scheme) { this.v1Scheme = v1Scheme; }
    public boolean isV2Scheme() { return v2Scheme; }
    public void setV2Scheme(boolean v2Scheme) { this.v2Scheme = v2Scheme; }
    public boolean isV3Scheme() { return v3Scheme; }
    public void setV3Scheme(boolean v3Scheme) { this.v3Scheme = v3Scheme; }
    public List<CertificateInfo> getCertificates() { return certificates; }
    public void setCertificates(List<CertificateInfo> certificates) { this.certificates = certificates; }
    public String getSigningCertificateDigest() { return signingCertificateDigest; }
    public void setSigningCertificateDigest(String digest) { this.signingCertificateDigest = digest; }

    public static class CertificateInfo {
        private String subject;
        private String issuer;
        private String serialNumber;
        private String notBefore;
        private String notAfter;
        private String signatureAlgorithm;
        private String sha256;
        private String sha1;
        private String md5;

        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getIssuer() { return issuer; }
        public void setIssuer(String issuer) { this.issuer = issuer; }
        public String getSerialNumber() { return serialNumber; }
        public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
        public String getNotBefore() { return notBefore; }
        public void setNotBefore(String notBefore) { this.notBefore = notBefore; }
        public String getNotAfter() { return notAfter; }
        public void setNotAfter(String notAfter) { this.notAfter = notAfter; }
        public String getSignatureAlgorithm() { return signatureAlgorithm; }
        public void setSignatureAlgorithm(String signatureAlgorithm) { this.signatureAlgorithm = signatureAlgorithm; }
        public String getSha256() { return sha256; }
        public void setSha256(String sha256) { this.sha256 = sha256; }
        public String getSha1() { return sha1; }
        public void setSha1(String sha1) { this.sha1 = sha1; }
        public String getMd5() { return md5; }
        public void setMd5(String md5) { this.md5 = md5; }
    }
}
