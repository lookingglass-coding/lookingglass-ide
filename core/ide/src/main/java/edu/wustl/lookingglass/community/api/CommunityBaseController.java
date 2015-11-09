/*******************************************************************************
 * Copyright (c) 2008, 2015, Washington University in St. Louis.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Products derived from the software may not be called "Looking Glass", nor
 *    may "Looking Glass" appear in their name, without prior written permission
 *    of Washington University in St. Louis.
 *
 * 4. All advertising materials mentioning features or use of this software must
 *    display the following acknowledgement: "This product includes software
 *    developed by Washington University in St. Louis"
 *
 * 5. The gallery of art assets and animations provided with this software is
 *    contributed by Electronic Arts Inc. and may be used for personal,
 *    non-commercial, and academic use only. Redistributions of any program
 *    source code that utilizes The Sims 2 Assets must also retain the copyright
 *    notice, list of conditions and the disclaimer contained in
 *    The Alice 3.0 Art Gallery License.
 *
 * DISCLAIMER:
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND.  ANY AND ALL
 * EXPRESS, STATUTORY OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY,  FITNESS FOR A PARTICULAR PURPOSE,
 * TITLE, AND NON-INFRINGEMENT ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHORS,
 * COPYRIGHT OWNERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, PUNITIVE OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING FROM OR OTHERWISE RELATING TO
 * THE USE OF OR OTHER DEALINGS WITH THE SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package edu.wustl.lookingglass.community.api;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import org.scribe.model.Response;
import org.scribe.model.Verb;

import edu.cmu.cs.dennisc.java.util.logging.Logger;
import edu.wustl.lookingglass.common.VersionNumber;
import edu.wustl.lookingglass.community.CommunityStatus.AccessStatus;
import edu.wustl.lookingglass.community.CommunityStatus.ConnectionStatus;
import edu.wustl.lookingglass.community.CommunityStatusObserver;
import edu.wustl.lookingglass.community.api.packets.CapabilitiesPacket;
import edu.wustl.lookingglass.community.api.packets.ErrorPacket;
import edu.wustl.lookingglass.community.api.packets.FilePacket;
import edu.wustl.lookingglass.community.api.packets.JsonPacket;
import edu.wustl.lookingglass.community.api.packets.OAuthVerifierPacket;
import edu.wustl.lookingglass.community.api.packets.Packet;
import edu.wustl.lookingglass.community.api.packets.UserPacket;
import edu.wustl.lookingglass.community.exceptions.CommunityApiException;
import edu.wustl.lookingglass.community.exceptions.CommunityIOException;
import edu.wustl.lookingglass.community.exceptions.IncompatibleApiException;
import edu.wustl.lookingglass.community.exceptions.IncompatibleTypeException;
import edu.wustl.lookingglass.community.exceptions.InvalidConnectionException;
import edu.wustl.lookingglass.community.exceptions.InvalidRequestException;
import edu.wustl.lookingglass.community.exceptions.JsonParseException;
import edu.wustl.lookingglass.community.exceptions.UnauthorizedAccessException;
import edu.wustl.lookingglass.ide.LookingGlassIDE;

/**
 * @author Kyle J. Harms
 */
public abstract class CommunityBaseController {

	private final CommunityConnection connection;

	// OAuth
	private org.scribe.oauth.OAuthService service;
	private org.scribe.model.Token accessToken;

	private final org.scribe.model.Token EMPTY_TOKEN = new org.scribe.model.Token( "", "" );

	// JSON
	private com.google.gson.Gson jsonSerializer;
	private com.google.gson.Gson jsonDeserializer;

	// API
	private ConnectionStatus connectionStatus = ConnectionStatus.NONE;
	private AccessStatus accessStatus = AccessStatus.NONE;

	// Security
	private java.security.cert.X509Certificate[] certificates = null;

	static private enum ApiCompatibility {
		UNKNOWN,
		INCOMPATIBLE,
		COMPATIBLE;
	};

	private CapabilitiesPacket apiCapabilities = null;
	private ApiCompatibility apiCompatibility = ApiCompatibility.UNKNOWN;

	private UserPacket userPacket = null;
	private String username = null;

	private String sessionCookie = null;
	private final Object cookieLock = new Object();

	private boolean autoConnect;
	private boolean verbose;
	private int timeout;

	private java.util.concurrent.CopyOnWriteArrayList<CommunityStatusObserver> observers = new java.util.concurrent.CopyOnWriteArrayList<CommunityStatusObserver>();

	protected CommunityBaseController() {

		this.verbose = Boolean.valueOf( System.getProperty( "edu.wustl.lookingglass.community.verbose", "false" ) );
		this.autoConnect = Boolean.valueOf( System.getProperty( "edu.wustl.lookingglass.community.autoConnect", "false" ) );
		this.timeout = Integer.valueOf( System.getProperty( "edu.wustl.lookingglass.community.timeoutSeconds", "30" ) );
		boolean cacheEnabled = Boolean.valueOf( System.getProperty( "edu.wustl.lookingglass.community.cache", "true" ) );

		String communityOverride = System.getProperty( "edu.wustl.lookingglass.community.server", "production" );
		switch( communityOverride ) {
		case "production":
		default:
			this.connection = new FreeConnection();
			break;
		case "localhost":
			this.connection = new LocalhostConnection();
			break;
		}

		if( verbose ) {
			System.out.println( "url: " + getBaseUrl() );
		}

		org.scribe.builder.api.DefaultApi10a oauthProvider = (org.scribe.builder.api.DefaultApi10a)this.connection;
		this.service = new org.scribe.builder.ServiceBuilder().provider( oauthProvider ).apiKey( this.connection.getOauthKey() ).apiSecret( this.connection.getOauthSecret() ).build();
		this.accessToken = EMPTY_TOKEN;

		this.jsonSerializer = new com.google.gson.GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

		// Add any additional json deserializers here if necessary.
		this.jsonDeserializer = new com.google.gson.GsonBuilder().excludeFieldsWithoutExposeAnnotation()
				.registerTypeAdapter( VersionNumber.class, new edu.wustl.lookingglass.community.api.VersionNumberDeserializer() )
				.registerTypeAdapter( java.util.Date.class, new edu.wustl.lookingglass.community.api.DateDeserializer() )
				.registerTypeAdapter( java.util.UUID.class, new edu.wustl.lookingglass.community.api.UUIDDeserializer() )
				.create();

		// Initialize the cache
		String cachePath;
		if( cacheEnabled ) {
			cachePath = new java.io.File( edu.cmu.cs.dennisc.app.ApplicationRoot.getCacheDirectory(), "community" ).getAbsolutePath();
			if( verbose ) {
				System.out.println( "cache: " + cachePath );
			}
		} else {
			cachePath = null;
		}
		if( cacheEnabled && ( cachePath != null ) ) {
			try {
				/*
				 * Follow this documentation if you need help making this cache work...
				 *
				 * https://developer.android.com/reference/android/net/http/HttpResponseCache.html
				 */
				final java.io.File httpCacheDir = new java.io.File( cachePath );
				final long httpCacheSize = 40 * 1024 * 1024; // 40 MiB
				com.integralblue.httpresponsecache.HttpResponseCache.install( httpCacheDir, httpCacheSize );

				// Make sure to close the cache
				Runtime.getRuntime().addShutdownHook( new Thread( "Community Cache") {
					@Override
					public void run() {
						try {
							com.integralblue.httpresponsecache.HttpResponseCache cache = com.integralblue.httpresponsecache.HttpResponseCache.getInstalled();
							if( cache != null ) {
								cache.flush();
								cache.close();
							}
						} catch( Throwable t ) {
							// ignore. cache will be cleaned on next start.
						}
					}
				} );
			} catch( Exception e ) {
				edu.cmu.cs.dennisc.java.util.logging.Logger.throwable( e, this );
			}
		}
	}

	protected boolean isAutoConnect() {
		return this.autoConnect;
	}

	public String getHost() {
		return this.connection.getHost();
	}

	public String getBaseUrl() {
		return this.connection.getProtocol() + "://" + this.connection.getHost();
	}

	/*package-private*/String getBaseUrl( String location ) {
		return getBaseUrl() + location;
	}

	/*package-private*/String getSecureBaseUrl() {
		return this.connection.getSecureProtocol() + "://" + this.connection.getHost();
	}

	/*package-private*/String getSecureBaseUrl( String location ) {
		return getSecureBaseUrl() + location;
	}

	private static final String API_PATH = "/api";

	protected String getSecureAbsoluteApiUrl( String location ) {
		return getSecureBaseUrl() + API_PATH + location;
	}

	protected String getAbsoluteApiUrl( String location ) {
		return getBaseUrl() + API_PATH + location;
	}

	public java.net.URL getAbsoluteUrl( String relativeUrl ) {
		try {
			return new java.net.URL( getBaseUrl( relativeUrl ) );
		} catch( java.net.MalformedURLException e ) {
			return null;
		}
	}

	public void addAndInvokeObserver( CommunityStatusObserver observer ) {
		this.addObserver( observer );

		// access should get called first
		observer.accessChanged( accessStatus );
		observer.connectionChanged( connectionStatus );
	}

	public void addObserver( CommunityStatusObserver observer ) {
		if( !this.observers.contains( observer ) ) {
			this.observers.add( observer );
		}
	}

	public void removeObserver( CommunityStatusObserver observer ) {
		this.observers.remove( observer );
	}

	public boolean containsObserver( CommunityStatusObserver observer ) {
		return this.observers.contains( observer );
	}

	private synchronized void setAccessStatus( AccessStatus status ) {
		if( this.accessStatus != status ) {
			this.accessStatus = status;
			if( verbose ) {
				System.out.println( "access status: " + this.accessStatus );
			}
			for( CommunityStatusObserver observer : this.observers ) {
				observer.accessChanged( this.accessStatus );
			}
		}
	}

	public AccessStatus getAccessStatus() {
		return this.accessStatus;
	}

	private synchronized void setConnectionStatus( ConnectionStatus status ) {
		// We must first check to see whether the API is compatible.
		if( this.apiCompatibility == ApiCompatibility.INCOMPATIBLE ) {
			status = ConnectionStatus.INCOMPATIBLE_API;
		}

		if( this.connectionStatus != status ) {
			this.connectionStatus = status;
			if( verbose ) {
				System.out.println( "connection status: " + this.connectionStatus );
			}
			for( CommunityStatusObserver observer : this.observers ) {
				observer.connectionChanged( this.connectionStatus );
			}
		}
	}

	public ConnectionStatus getConnectionStatus() {
		return this.connectionStatus;
	}

	protected <T> T sendRequest( Verb action, String url, QueryParameter[] params, Class<T> t ) throws CommunityApiException {
		return sendRequest( action, url, params, null, t );
	}

	protected <T> T sendRequest( Verb action, String url, Class<T> t ) throws CommunityApiException {
		return sendRequest( action, url, null, null, t );
	}

	protected <T> T sendRequest( Verb action, String url, JsonPacket payload, Class<T> t ) throws CommunityApiException {
		return sendRequest( action, url, null, payload, t );
	}

	protected <T> T sendRequest( Verb action, String url, QueryParameter[] params ) throws CommunityApiException {
		return sendRequest( action, url, params, null, null );
	}

	protected <T> T sendRequest( Verb action, String url ) throws CommunityApiException {
		return sendRequest( action, url, null, null, null );
	}

	protected <T> T sendRequest( Verb action, java.net.URL url, QueryParameter[] params, JsonPacket payload, Class<T> t ) throws CommunityApiException {
		return sendRequest( action, url.toString(), params, payload, t );
	}

	protected <T> T sendRequest( Verb action, java.net.URL url, QueryParameter[] params, Class<T> t ) throws CommunityApiException {
		return sendRequest( action, url.toString(), params, null, t );
	}

	protected <T> T sendRequest( Verb action, java.net.URL url, Class<T> t ) throws CommunityApiException {
		return sendRequest( action, url.toString(), null, null, t );
	}

	protected <T> T sendRequest( Verb action, java.net.URL url, JsonPacket payload, Class<T> t ) throws CommunityApiException {
		return sendRequest( action, url.toString(), null, payload, t );
	}

	protected <T> T sendRequest( Verb action, java.net.URL url ) throws CommunityApiException {
		return sendRequest( action, url.toString(), null, null, null );
	}

	protected <T> T sendRequest( Verb action, java.net.URL url, QueryParameter[] params ) throws CommunityApiException {
		return sendRequest( action, url.toString(), params, null, null );
	}

	private org.scribe.model.OAuthRequest createRequest( Verb action, String url ) {
		org.scribe.model.OAuthRequest request = new org.scribe.model.OAuthRequest( action, url );
		request.setConnectionKeepAlive( true );
		request.setConnectTimeout( timeout, java.util.concurrent.TimeUnit.SECONDS );
		request.setReadTimeout( timeout, java.util.concurrent.TimeUnit.SECONDS );
		request.addHeader( "Accept", "application/json" );
		return request;
	}

	// TODO: We need a parameter that forces a refresh from the server if the request is cached.
	private <T> T sendRequest( Verb action, String url, QueryParameter[] params, JsonPacket payload, Class<T> t ) throws CommunityApiException {
		// This assert is absolutely necessary to not lock the croquet event thread. Do not remove.
		assert!java.awt.EventQueue.isDispatchThread();

		if( t != null ) {
			assert Packet.class.isAssignableFrom( t ) || ( t.isArray() && Packet.class.isAssignableFrom( t.getComponentType() ) );
		}

		assert action != null;
		assert url != null;

		// Query string parameters should only be used with GET requests.
		// Do not remove this check... and don't go to the community and change the action type to GET either.
		// Do the right thing and design the API to support proper behavior... you will thank yourself in the long-term.
		// For a good example check world's increment views counter or code test's execute.
		if( action != Verb.GET ) {
			assert params == null;
		}

		// Make sure the API is compatible... Do not continue if it isn't... we might corrupt the database.
		// Do not make this check, if we are trying to ask the server for the version.
		if( ( t != null ) && t.equals( CapabilitiesPacket.class ) && ( this.apiCapabilities == null ) ) {
			// pass. We need to community capability at the end of this function.
		} else {
			verifyApiCompatibility();
			assert this.apiCompatibility == ApiCompatibility.COMPATIBLE;
		}

		try {
			org.scribe.model.OAuthRequest request = this.createRequest( action, url );

			// Set session cookie if necessary
			synchronized( this.cookieLock ) {
				if( this.sessionCookie != null ) {
					request.addHeader( "Cookie", this.sessionCookie );
				}
			}

			// TODO: set this once we have a parameter to force a hit to bypass the cache.
			//request.addHeader( "Cache-Control", "max-age=0" );

			// Query String Parameters
			if( params != null ) {
				for( QueryParameter p : params ) {
					request.addQuerystringParameter( p.getKey(), p.getValue() );
				}
			}

			// The Payload
			if( payload != null ) {
				request.addHeader( "Content-Type", payload.getContentType() );
				request.addPayload( payload.getPayload( this.jsonSerializer ) );
			}

			String effectiveUrl = request.getQueryStringParams().appendTo( url );
			if( verbose ) {
				System.out.println( action + ":" + effectiveUrl );

				if( payload != null ) {
					System.out.println( "  " + payload.getJson( this.jsonSerializer ) );
				}
			}

			// Sign and send request
			this.service.signRequest( this.accessToken, request );
			Response response = request.send();
			T packet = processResponse( response, t );

			// check whether the API is compatible.
			if( packet instanceof CapabilitiesPacket ) {
				this.apiCapabilities = (CapabilitiesPacket)packet;

				// Major change; the API has deleted or modified method behavior
				// Minor change; the API has added, but not changed or deleted methods
				// Fix change; the API had some bugs fixed.
				if( this.apiCapabilities.getCommunityApiVersion().getMajor() == LookingGlassIDE.COMMUNITY_API_VERSION.getMajor() ) {
					this.apiCompatibility = ApiCompatibility.COMPATIBLE;
				} else {
					this.apiCompatibility = ApiCompatibility.INCOMPATIBLE;

					// We now know we can't talk to the server.
					this.setConnectionStatus( ConnectionStatus.INCOMPATIBLE_API );
				}

				// We have new information, we need to check and update status.
				this.verifyApiCompatibility();

				// Out capabilities packet initializes our communication with our server.
				// Get the SSL certificates if available.
				try {
					java.lang.reflect.Field connectionField = org.scribe.model.Request.class.getDeclaredField( "connection" );
					connectionField.setAccessible( true );

					HttpURLConnection connection = (HttpURLConnection)connectionField.get( request );
					if( connection != null ) {
						this.processCertificates( connection );
					}

				} catch( NoSuchFieldException e ) {
					e.printStackTrace();
				} catch( SecurityException e ) {
					e.printStackTrace();
				} catch( IllegalArgumentException e ) {
					e.printStackTrace();
				} catch( IllegalAccessException e ) {
					e.printStackTrace();
				}
			}

			// If we made it this far then we are talking to the server.
			this.setConnectionStatus( ConnectionStatus.CONNECTED );

			return packet;
		} catch( org.scribe.exceptions.OAuthException e ) {
			// We have failed to talk to the server.
			this.setConnectionStatus( ConnectionStatus.DISCONNECTED );
			throw new InvalidConnectionException( e );
		} catch( com.google.gson.JsonParseException e ) {
			throw new JsonParseException( e );
		} catch( java.io.IOException e ) {
			throw new CommunityIOException( e );
		}
	}

	/*
	 * This function should be thread safe. Do not set any instance variables in this function without
	 * adding proper thread-safe locks. Also, do not use any instance variables which may change
	 * during execution in a thread without adding the proper thread-safe locks.
	 */
	@SuppressWarnings( "unchecked" )
	private <T> T processResponse( Response response, Class<T> t ) throws CommunityApiException {
		int code = response.getCode();
		String contentType = response.getHeader( "Content-Type" );
		T packet = null;

		// Get the Response Body
		java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
		java.io.InputStream is = response.getStream();
		try {
			int thisLine;
			while( ( thisLine = is.read() ) != -1 ) {
				bos.write( thisLine );
			}
			bos.close();
		} catch( Exception e ) {
			throw new CommunityIOException( e );
		}
		byte[] content = bos.toByteArray();

		if( verbose ) {
			System.out.println( code + ":" + contentType );
		}

		switch( code ) {
		case HttpURLConnection.HTTP_NOT_MODIFIED:
		case HttpURLConnection.HTTP_OK:
		case HttpURLConnection.HTTP_CREATED:
		case HttpURLConnection.HTTP_ACCEPTED:
			// Check for cookies
			String setCookieHeader = response.getHeader( "Set-Cookie" );
			if( ( setCookieHeader != null ) && !setCookieHeader.isEmpty() ) {
				synchronized( this.cookieLock ) {
					this.sessionCookie = setCookieHeader;
				}
			}

			// Process the Packet
			if( t == null ) {
				packet = null;
			} else if( JsonPacket.class.isAssignableFrom( t ) ||
					( t.isArray() && JsonPacket.class.isAssignableFrom( t.getComponentType() ) ) ) {
				packet = processJsonPacket( content, contentType, code, t );
			} else if( FilePacket.class.isAssignableFrom( t ) ) {
				packet = (T)processFilePacket( content, contentType, code );
			} else {
				throw new InvalidRequestException( "unknown packet type: " + t.getName() );
			}
			break;
		default:
			processInvalidResponse( content, contentType, code );
		}

		return packet;
	}

	private <T> T processJsonPacket( byte[] content, String contentType, int code, Class<T> t ) throws CommunityApiException {
		if( !contentType.contains( "application/json" ) ) {
			throw new IncompatibleTypeException( "application/json", contentType );
		}

		// If this is an array get the array's type
		Class<?> type = t;
		if( t.isArray() ) {
			type = t.getComponentType();
		}

		// All, JsonPackets must only have the default constructor
		java.lang.reflect.Constructor<?>[] constructors = type.getDeclaredConstructors();
		assert constructors.length == 1;
		assert constructors[ 0 ].getParameterTypes().length == 0;

		String body = new String( content );
		if( verbose ) {
			System.out.println( "  " + body );
		}

		// Deserialize JSON
		T json;
		try {
			json = this.jsonDeserializer.fromJson( body, t );
		} catch( com.google.gson.JsonParseException e ) {
			throw new JsonParseException( e );
		}

		// Return the proper JsonPacket or JsonPacket[]
		if( json != null ) {
			if( t.isArray() ) {
				JsonPacket[] packets = (JsonPacket[])json;
				for( JsonPacket p : packets ) {
					if( !p.isValid() ) {
						throw new InvalidRequestException( code, body );
					}
				}
				return json;
			} else if( ( (JsonPacket)json ).isValid() ) {
				return json;
			} else {
				throw new InvalidRequestException( code, body );
			}
		} else {
			return json;
		}
	}

	private FilePacket processFilePacket( byte[] content, String contentType, int code ) throws CommunityApiException {
		if( verbose ) {
			System.out.println( "  <binary data>" );
		}
		FilePacket filePacket = new FilePacket();

		filePacket.setContentType( contentType );
		filePacket.setPayload( content );

		return filePacket;
	}

	private void processInvalidResponse( byte[] content, String contentType, int code ) throws CommunityApiException {
		String body = new String( content );
		if( verbose ) {
			System.out.println( code + ":" + body );
		}
		try {
			if( ( contentType != null ) && contentType.contains( "application/json" ) ) {
				ErrorPacket errorPacket = this.jsonDeserializer.fromJson( body, ErrorPacket.class );
				if( ( errorPacket == null ) || errorPacket.getError().isEmpty() ) {
					throw new InvalidRequestException( code, body );
				} else {
					throw new InvalidRequestException( errorPacket );
				}
			} else {
				throw new InvalidRequestException( code, body );
			}
		} catch( com.google.gson.JsonParseException e ) {
			throw new InvalidRequestException( code, body );
		}
	}

	protected java.security.cert.X509Certificate[] getCertificates() {
		return certificates;
	}

	private void processCertificates( HttpURLConnection connection ) {
		if( connection instanceof javax.net.ssl.HttpsURLConnection ) {
			javax.net.ssl.HttpsURLConnection sslConnection = (javax.net.ssl.HttpsURLConnection)connection;
			try {
				java.security.cert.Certificate[] certs = sslConnection.getServerCertificates();

				java.util.List<java.security.cert.X509Certificate> tempList = edu.cmu.cs.dennisc.java.util.Lists.newArrayList();
				for( Certificate cert : certs ) {
					if( cert instanceof java.security.cert.X509Certificate ) {
						tempList.add( (java.security.cert.X509Certificate)cert );
					}
				}

				this.certificates = tempList.toArray( new java.security.cert.X509Certificate[ tempList.size() ] );
			} catch( javax.net.ssl.SSLPeerUnverifiedException e ) {
				this.certificates = null;
			}
		}
	}

	public void verifySignedJarCertificate( java.util.jar.JarFile jarFile ) throws CertificateException, IOException {
		if( this.connection.verifyCertificates() ) {
			if( this.getCertificates() != null ) {
				org.whipplugin.data.bundle.JarVerifier.verify( jarFile, this.getCertificates() );
			} else {
				throw new CertificateException( "No trusted certificates for verification" );
			}
		} else {
			Logger.warning( "bypassed jar certificate validation" );
		}
	}

	private void verifyApiCompatibility() throws CommunityApiException {
		// This assert is absolutely necessary to not lock the croquet event thread. Do not remove.
		assert!java.awt.EventQueue.isDispatchThread();

		switch( this.apiCompatibility ) {
		case COMPATIBLE:
			break;
		case INCOMPATIBLE:
			throw new IncompatibleApiException( this.apiCapabilities.getCommunityApiVersion() );
		case UNKNOWN:
		default:
			throw new IncompatibleApiException();
		}
	}

	protected CapabilitiesPacket getCapabilitiesPacket() {
		return this.apiCapabilities;
	}

	private CapabilitiesPacket requestApiCapabilitiesPacket() throws CommunityApiException {
		// This assert is absolutely necessary to not lock the croquet event thread. Do not remove.
		assert!java.awt.EventQueue.isDispatchThread();

		return sendRequest( Verb.GET, getSecureAbsoluteApiUrl( "/capabilities.json" ), null, null, CapabilitiesPacket.class );
	}

	public void checkServerConnectionStatus() throws CommunityApiException {
		// This assert is absolutely necessary to not lock the croquet event thread. Do not remove.
		assert!java.awt.EventQueue.isDispatchThread();

		// The capabilities packet is the packet used to initiate a connection with the server
		requestApiCapabilitiesPacket();
	}

	protected UserPacket getUserPacket() {
		return this.userPacket;
	}

	/* Leave this method private. Do not change to protected or public. */
	private UserPacket requestUserAccountPacket() throws CommunityApiException {
		// This assert is absolutely necessary to not lock the croquet event thread. Do not remove.
		assert!java.awt.EventQueue.isDispatchThread();

		return sendRequest( Verb.GET, getAbsoluteApiUrl( "/users/account.json" ), UserPacket.class );
	}

	protected synchronized void anonymousAccess() throws CommunityApiException {
		// This assert is absolutely necessary to not lock the croquet event thread. Do not remove.
		assert!java.awt.EventQueue.isDispatchThread();

		// Before we can login we need to check whether we know how to talk to the server.
		requestApiCapabilitiesPacket();

		try {
			this.accessToken = EMPTY_TOKEN;
			setAccessStatus( AccessStatus.ANONYMOUS_ACCESS );
		} finally {
			this.userPacket = null;
			this.username = null;
		}
	}

	protected synchronized void userAccess( String username, String password ) throws CommunityApiException {
		// This assert is absolutely necessary to not lock the croquet event thread. Do not remove.
		assert!java.awt.EventQueue.isDispatchThread();

		// Before we can login we need to check whether we know how to talk to the server.
		requestApiCapabilitiesPacket();

		try {
			org.scribe.model.Token requestToken = this.service.getRequestToken();

			org.scribe.model.OAuthRequest request = this.createRequest( Verb.POST, getSecureBaseUrl( "/oauth/authorize_login" ) );
			request.addQuerystringParameter( "xoauth_request_token", requestToken.getToken() );
			request.addQuerystringParameter( "xoauth_username", username );
			request.addQuerystringParameter( "xoauth_password", password );
			this.service.signRequest( this.EMPTY_TOKEN, request );
			Response response = request.send();

			OAuthVerifierPacket verifierPacket = processResponse( response, OAuthVerifierPacket.class );
			org.scribe.model.Verifier verifier = new org.scribe.model.Verifier( verifierPacket.getVerifier() );
			this.accessToken = this.service.getAccessToken( requestToken, verifier );
			this.userPacket = this.requestUserAccountPacket();
			this.username = username;
			setAccessStatus( AccessStatus.USER_ACCESS );
		} catch( org.scribe.exceptions.OAuthException e ) {
			this.accessToken = this.EMPTY_TOKEN;
			this.userPacket = null;
			this.username = null;
			throw new InvalidConnectionException( e );
		}
	}

	protected synchronized void closeAccess() {
		// This assert is absolutely necessary to not lock the croquet event thread. Do not remove.
		assert!java.awt.EventQueue.isDispatchThread();

		this.accessToken = EMPTY_TOKEN;
		this.userPacket = null;
		this.username = null;
		setAccessStatus( AccessStatus.NONE );
	}

	public Boolean isUserLoggedIn() {
		return ( this.accessStatus == AccessStatus.USER_ACCESS );
	}

	public Boolean isAnonymousLoggedIn() {
		return ( this.accessStatus == AccessStatus.ANONYMOUS_ACCESS );
	}

	public String getUsername() {
		return this.username;
	}

	protected void anonymousRequired() throws UnauthorizedAccessException {
		if( this.accessStatus != AccessStatus.ANONYMOUS_ACCESS ) {
			throw new UnauthorizedAccessException();
		}
	}

	protected void anonymousOrLoginRequired() throws UnauthorizedAccessException {
		if( ( this.accessStatus != AccessStatus.ANONYMOUS_ACCESS ) &&
				( this.accessStatus != AccessStatus.USER_ACCESS ) ) {
			throw new UnauthorizedAccessException();
		}
	}

	protected void loginRequired() throws UnauthorizedAccessException {
		if( this.accessStatus != AccessStatus.USER_ACCESS ) {
			throw new UnauthorizedAccessException();
		}
	}
}
