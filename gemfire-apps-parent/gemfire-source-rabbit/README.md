# Property definitions for **Gemfire Source Rabbit**
## Properties:

### gemfire.client

> **_pdx-read-serialized_** 
 >> _Deserialize the Geode objects into PdxInstance instead of the domain class._ **( _Boolean_ , Default: _false_ )** 
 


### gemfire.pool

> **_connect-type_** 
 >> _Specifies connection type: 'server' or 'locator'._ **( _ConnectType_ , Default: _<none>_ )** 
 

> **_host-addresses_** 
 >> _Specifies one or more GemFire locator or server addresses formatted as [host]:[port]._ **( _InetSocketAddress[]_ , Default: _<none>_ )** 
 

> **_subscription-enabled_** 
 >> _Set to true to enable subscriptions for the client pool. Required to sync updates to the client cache._ **( _Boolean_ , Default: _false_ )** 
 


### gemfire.region

> **_region-name_** 
 >> _The region name._ **( _String_ , Default: _<none>_ )** 
 


### gemfire.security

> **_password_** 
 >> _The cache password._ **( _String_ , Default: _<none>_ )** 
 

> **_username_** 
 >> _The cache username._ **( _String_ , Default: _<none>_ )** 
 


### gemfire.security.ssl

> **_ciphers_** 
 >> _Configures the SSL ciphers used for secure Socket connections as an array of valid cipher names._ **( _String_ , Default: _any_ )** 
 

> **_keystore-type_** 
 >> _Identifies the type of Keystore used for SSL communications (e.g. JKS, PKCS11, etc.)._ **( _String_ , Default: _JKS_ )** 
 

> **_keystore-uri_** 
 >> _Location of the pre-created Keystore URI to be used for connecting to the Geode cluster._ **( _Resource_ , Default: _<none>_ )** 
 

> **_ssl-keystore-password_** 
 >> _Password for accessing the keys truststore._ **( _String_ , Default: _<none>_ )** 
 

> **_ssl-truststore-password_** 
 >> _Password for accessing the trust store._ **( _String_ , Default: _<none>_ )** 
 

> **_truststore-type_** 
 >> _Identifies the type of truststore used for SSL communications (e.g. JKS, PKCS11, etc.)._ **( _String_ , Default: _JKS_ )** 
 

> **_truststore-uri_** 
 >> _Location of the pre-created truststore URI to be used for connecting to the Geode cluster._ **( _Resource_ , Default: _<none>_ )** 
 

> **_user-home-directory_** 
 >> _Local directory to cache the truststore and keystore files downloaded form the truststoreUri and keystoreUri locations._ **( _String_ , Default: _user.home_ )** 
 


### gemfire.supplier

> **_event-expression_** 
 >> _SpEL expression to extract data from an {@link org.apache.geode.cache.EntryEvent} or {@link org.apache.geode.cache.query.CqEvent}._ **( _Expression_ , Default: _<none>_ )** 
 

> **_query_** 
 >> _An OQL query. This will enable continuous query if provided._ **( _String_ , Default: _<none>_ )** 
 

