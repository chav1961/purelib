package chav1961.purelib.basic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Hashtable;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * <p>This class contains a lot of static methods to support SSL connections</p>
 *  
 * @author Alexander Chernomyrdin aka chav1961
 * @since 0.0.4
 */

public class SocketUtils {
	/**
	 * <p>This interface is used to name aliases for certificates</p> 
	 * @author Alexander Chernomyrdin aka chav1961
	 * @since 0.0.4
	 */
	@FunctionalInterface
	public interface AliasNaming {
		/**
		 * <p>Build alias name for certificate</p>
		 * @param host host name where certificate is from
		 * @param sertificateHash certificate hash
		 * @param certificateSeq certificate sequential number from this host
		 * @return any alias name built
		 */
		String getAlias(final String host, final int sertificateHash, final int certificateSeq);
	}
	
	/**
	 * <p>Collect public certificates from the given server to support SSL connections to it</p> 
	 * @param connection connection to server. Must be absolute (has host and port), and can contain optional query parameter 'type'={'SSL'|TSLv1'... etc}
	 * @param ks key store to save certificates into
	 * @return true if any certificate found
	 * @throws IOException on any I/O errors
	 * @throws NullPointerException on any parameters are null
	 * @throws IllegalArgumentException on illegal arguments
	 */
	public static boolean collectSSLCertificates(final URI connection, final KeyStore ks) throws IOException, NullPointerException, IllegalArgumentException {
		return collectSSLCertificates(connection,ks,(h,sh,ss)->h+".cert"+ss);
	}

	/**
	 * <p>Collect public certificates from the given server to support SSL connections to it</p> 
	 * @param connection connection to server. Must be absolute (has host and port), and can contain optional query parameter 'type'={'SSL'|TSLv1'... etc}
	 * @param ks key store to save certificates into
	 * @param an alias naming callback 
	 * @return true if any certificate found
	 * @throws IOException on any I/O errors
	 * @throws NullPointerException on any parameters are null
	 * @throws IllegalArgumentException on illegal arguments
	 * @see AliasNaming
	 */
	public static boolean collectSSLCertificates(final URI connection, final KeyStore ks, final AliasNaming an) throws IOException, NullPointerException, IllegalArgumentException {
		if (connection == null) {
			throw new NullPointerException("Connection URI can't be null");
		}
		else if (ks == null) {
			throw new NullPointerException("Keystore can't be null");
		}
		else if (an == null) {
			throw new NullPointerException("Alias naming can't be null");
		}
		else if (!connection.isAbsolute()) {
			throw new IllegalArgumentException("Connection URI ["+connection+"] must be absolute!");
		}
		else if (connection.getPort() == -1) {
			throw new IllegalArgumentException("Connection URI ["+connection+"] - port number is missing!");
		}
		else {
			final String			host = connection.getHost();
			final int				port = connection.getPort();
	        final Hashtable<String, String[]> 	query = URIUtils.parseQuery(connection);	// zzz?type={SSL|TLSv1|...}
			final TrustManager[]	emptyTrustManagers = new TrustManager[]{
								       new X509TrustManager() {
								           public java.security.cert.X509Certificate[] getAcceptedIssuers() {
								              return null;
								            }
								            public void checkClientTrusted(X509Certificate[] certs, String authType) { }
								            public void checkServerTrusted(X509Certificate[] certs, String authType) {}
								        }
								   };
			
			try{final SSLContext 		sslContext = SSLContext.getInstance(query.getOrDefault("type",new String[] {"TLSv1"})[0]);
	
				sslContext.init(null, emptyTrustManagers,  new java.security.SecureRandom());
	
				final SSLSocketFactory	sslsocketfactory = (SSLSocketFactory) sslContext.getSocketFactory();
	
				try(final SSLSocket 	sock = (SSLSocket)sslsocketfactory.createSocket(host,port == -1 ? 465 : port);) {
					int		count = 0;
					
					for (Certificate item : sock.getSession().getPeerCertificates()) {
						ks.setCertificateEntry(an.getAlias(host,Arrays.hashCode(item.getEncoded()),count++),item);
					}
					return count != 0;
				}
			} catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException | CertificateEncodingException e) {
				throw new IOException(e.getLocalizedMessage(),e);
			}
		}
	}
	
	/**
	 * <p>Collect public certificates from the given server to support SSL connections to it</p> 
	 * @param connection connection to server. Must be absolute (has host and port), and can contain optional query parameter 'type'={'SSL'|TSLv1'... etc}
	 * @param keystore key store file
	 * @param password password for key store file
	 * @return true if any certificate found
	 * @throws IOException on any I/O errors
	 * @throws NullPointerException on any parameters are null
	 * @throws IllegalArgumentException on illegal arguments
	 */
	public static boolean collectSSLCertificates(final URI connection, final File keystore, final char[] password) throws IOException, NullPointerException, IllegalArgumentException {
		return collectSSLCertificates(connection,keystore,password,(h,sh,ss)->h+".cert"+ss);
	}

	/**
	 * <p>Collect public certificates from the given server to support SSL connections to it</p> 
	 * @param connection
	 * @param keystore key store file
	 * @param password password for key store file
	 * @param an alias naming callback 
	 * @return true if any certificate found
	 * @throws IOException on any I/O errors
	 * @throws NullPointerException on any parameters are null
	 * @throws IllegalArgumentException on illegal arguments
	 * @see AliasNaming
	 */
	public static boolean collectSSLCertificates(final URI connection, final File keystore, final char[] password, final AliasNaming an) throws IOException, NullPointerException, IllegalArgumentException {
		if (connection == null) {
			throw new NullPointerException("Connection URI can't be null");
		}
		else if (keystore == null) {
			throw new NullPointerException("Keystore file can't be null");
		}
		else if (password == null || password.length == 0) {
			throw new IllegalArgumentException("Keystore password can't be null or empty array");
		}
		else if (an == null) {
			throw new NullPointerException("Alias naming can't be null");
		}
		else if (!connection.isAbsolute()) {
			throw new IllegalArgumentException("Connection URI ["+connection+"] must be absolute!");
		}
		else if (keystore.exists() && keystore.isFile()) {
			try{final KeyStore	ks = KeyStore.getInstance(keystore,password);
			
				if (collectSSLCertificates(connection, ks, an)) {
					try(final OutputStream	os = new FileOutputStream(keystore)) {
						ks.store(os, password);
					}
					return true;
				}
				else {
					return false;
				}
			} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
				throw new IOException(e.getLocalizedMessage(),e);
			}
		}
		else {
			try{final KeyStore	ks = KeyStore.getInstance("PKCS12");
			
				ks.load(null,null);
				if (collectSSLCertificates(connection, ks, an)) {
					try(final OutputStream	os = new FileOutputStream(keystore)) {
						ks.store(os, password);
					}
					return true;
				}
				else {
					return false;
				}
			} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
				throw new IOException(e.getLocalizedMessage(),e);
			}
		}
	}

	/**
	 * <p>Build {@linkplain SSLSocket} instance</p>
	 * @param ks key store with certificated and keys
	 * @param password password to key store
	 * @param addr connection address
	 * @param sslType type of SSL connection {'SSL'|'TLSv1'... etc}
	 * @return connection created
	 * @throws IOException on any I/O errors
	 * @throws NullPointerException on any parameters are null
	 * @throws IllegalArgumentException on illegal arguments
	 */
	public static SSLSocket buildSSLSocket(final KeyStore ks, final char[] password, final InetSocketAddress addr, final String sslType) throws IOException, NullPointerException, IllegalArgumentException {
		if (ks == null) {
			throw new NullPointerException("Keystore can't be null");
		}
		else if (password == null || password.length == 0) {
			throw new IllegalArgumentException("Keystore password can't be null or empty array");
		}
		else if (addr == null) {
			throw new NullPointerException("Inet address can't be null");
		}
		else if (sslType == null || sslType.isEmpty()) {
			throw new IllegalArgumentException("SSL type can't be null or empty");
		}
		else {
			try{final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
				trustManagerFactory.init(ks);
		
				final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("NewSunX509");
				keyManagerFactory.init(ks,password);
		
				final SSLContext context = SSLContext.getInstance(sslType);
				context.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new java.security.SecureRandom());
		
				final SSLSocketFactory factory = context.getSocketFactory();
		
				return (SSLSocket) factory.createSocket(addr.getHostName(),addr.getPort());
			} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException | UnrecoverableKeyException exc) {
				throw new IOException(exc.getLocalizedMessage(),exc);
			}
		}
	}

	/**
	 * <p>Build {@linkplain SSLSocket} instance</p>
	 * @param keystore key store file with certificated and keys
	 * @param password password to key store
	 * @param addr connection address
	 * @param sslType type of SSL connection {'SSL'|'TLSv1'... etc}
	 * @return connection created
	 * @throws IOException on any I/O errors
	 * @throws NullPointerException on any parameters are null
	 * @throws IllegalArgumentException on illegal arguments
	 */
	public static SSLSocket buildSSLSocket(final File keystore, final char[] password, final InetSocketAddress addr, final String sslType) throws IOException, NullPointerException, IllegalArgumentException {
		if (keystore == null) {
			throw new NullPointerException("Keystore file can't be null");
		}
		else if (password == null || password.length == 0) {
			throw new IllegalArgumentException("Keystore password can't be null or empty array");
		}
		else {
			try{return buildSSLSocket(KeyStore.getInstance(keystore,password), password, addr, sslType);
			} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
				throw new IOException(e.getLocalizedMessage(),e); 
			}
		}
	}

	/**
	 * <p>Build {@linkplain SSLSocket} instance</p>
	 * @param ks key store with certificated and keys
	 * @param password password to key store
	 * @param ts trust store with certificated and keys
	 * @param addr connection address
	 * @param sslType type of SSL connection {'SSL'|'TLSv1'... etc}
	 * @return connection created
	 * @throws IOException on any I/O errors
	 * @throws NullPointerException on any parameters are null
	 * @throws IllegalArgumentException on illegal arguments
	 */
	public static SSLSocket buildSSLSocket(final KeyStore ks, final char[] password, final KeyStore ts, final InetSocketAddress addr, final String sslType) throws IOException, NullPointerException, IllegalArgumentException {
		if (ks == null) {
			throw new NullPointerException("Keystore can't be null");
		}
		else if (password == null || password.length == 0) {
			throw new IllegalArgumentException("Keystore password can't be null or empty array");
		}
		else if (ts == null) {
			throw new NullPointerException("Truststore can't be null");
		}
		else if (addr == null) {
			throw new NullPointerException("Inet address can't be null");
		}
		else if (sslType == null || sslType.isEmpty()) {
			throw new IllegalArgumentException("SSL type can't be null or empty");
		}
		else {
			try{final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
				trustManagerFactory.init(ts);
		
				final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("NewSunX509");
				keyManagerFactory.init(ks,password);
		
				final SSLContext context = SSLContext.getInstance(sslType);
				context.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new java.security.SecureRandom());
		
				final SSLSocketFactory factory = context.getSocketFactory();
		
				return (SSLSocket) factory.createSocket(addr.getHostName(),addr.getPort());
			} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException | UnrecoverableKeyException exc) {
				throw new IOException(exc.getLocalizedMessage(),exc);
			}
		}
	}

	/**
	 * <p>Build {@linkplain SSLSocket} instance</p>
	 * @param keystore key store file
	 * @param password password to key store
	 * @param truststore trust store file
	 * @param tsPassword password to trust store
	 * @param addr connection address
	 * @param sslType type of SSL connection {'SSL'|'TLSv1'... etc}
	 * @return connection created
	 * @throws IOException on any I/O errors
	 * @throws NullPointerException on any parameters are null
	 * @throws IllegalArgumentException on illegal arguments
	 */
	public static SSLSocket buildSSLSocket(final File keystore, final char[] ksPassword, final File truststore, final char[] tsPassword, final InetSocketAddress addr, final String sslType) throws IOException, NullPointerException, IllegalArgumentException {
		if (keystore == null) {
			throw new NullPointerException("Keystore file can't be null");
		}
		else if (ksPassword == null || ksPassword.length == 0) {
			throw new IllegalArgumentException("Keystore password can't be null or empty array");
		}
		else if (truststore == null) {
			throw new NullPointerException("Truststore file can't be null");
		}
		else if (tsPassword == null || tsPassword.length == 0) {
			throw new IllegalArgumentException("Truststore password can't be null or empty array");
		}
		else {
			try{return buildSSLSocket(KeyStore.getInstance(keystore,ksPassword), ksPassword, KeyStore.getInstance(truststore,tsPassword), addr, sslType);
			} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
				throw new IOException(e.getLocalizedMessage(),e); 
			}
		}
	}
}
