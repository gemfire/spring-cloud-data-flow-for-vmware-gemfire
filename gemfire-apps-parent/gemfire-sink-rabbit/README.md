# Property definitions for **Gemfire Sink Rabbit**
## Properties:

### gemfire.consumer

> **_json_** 
 >> _Indicates if the Geode region stores json objects as PdxInstance._ **( _Boolean_ , Default: _false_ )** 
 

> **_key-expression_** 
 >> _SpEL expression to use as a cache key._ **( _String_ , Default: _<none>_ )** 
 


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
 

