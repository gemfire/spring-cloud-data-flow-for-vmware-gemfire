# Property definitions for **Gemfire Source Kafka**
## Properties:

### gemfire.client

<table>
    <tr>
    <th style="text-align: left">Property Name</th>
    <th style="text-align: left">Description</th>
    <th style="text-align: left">Type</th>
    <th style="text-align: left">Defaults</th>
    </tr>
<tr><td>pdx-read-serialized</td>
      <td>Deserialize the Geode objects into PdxInstance instead of the domain class.</td>
      <td>Boolean</td>
      <td>false</td></tr>
</table>

### gemfire.pool

<table>
    <tr>
    <th style="text-align: left">Property Name</th>
    <th style="text-align: left">Description</th>
    <th style="text-align: left">Type</th>
    <th style="text-align: left">Defaults</th>
    </tr>
<tr><td>connect-type</td>
      <td>Specifies connection type: 'server' or 'locator'.</td>
      <td>ConnectType</td>
      <td><none></td></tr>
<tr><td>host-addresses</td>
      <td>Specifies one or more GemFire locator or server addresses formatted as [host]:[port].</td>
      <td>InetSocketAddress[]</td>
      <td><none></td></tr>
<tr><td>subscription-enabled</td>
      <td>Set to true to enable subscriptions for the client pool. Required to sync updates to the client cache.</td>
      <td>Boolean</td>
      <td>false</td></tr>
</table>

### gemfire.region

<table>
    <tr>
    <th style="text-align: left">Property Name</th>
    <th style="text-align: left">Description</th>
    <th style="text-align: left">Type</th>
    <th style="text-align: left">Defaults</th>
    </tr>
<tr><td>region-name</td>
      <td>The region name.</td>
      <td>String</td>
      <td><none></td></tr>
</table>

### gemfire.security

<table>
    <tr>
    <th style="text-align: left">Property Name</th>
    <th style="text-align: left">Description</th>
    <th style="text-align: left">Type</th>
    <th style="text-align: left">Defaults</th>
    </tr>
<tr><td>password</td>
      <td>The cache password.</td>
      <td>String</td>
      <td><none></td></tr>
<tr><td>username</td>
      <td>The cache username.</td>
      <td>String</td>
      <td><none></td></tr>
</table>

### gemfire.security.ssl

<table>
    <tr>
    <th style="text-align: left">Property Name</th>
    <th style="text-align: left">Description</th>
    <th style="text-align: left">Type</th>
    <th style="text-align: left">Defaults</th>
    </tr>
<tr><td>ciphers</td>
      <td>Configures the SSL ciphers used for secure Socket connections as an array of valid cipher names.</td>
      <td>String</td>
      <td>any</td></tr>
<tr><td>keystore-type</td>
      <td>Identifies the type of Keystore used for SSL communications (e.g. JKS, PKCS11, etc.).</td>
      <td>String</td>
      <td>JKS</td></tr>
<tr><td>keystore-uri</td>
      <td>Location of the pre-created Keystore URI to be used for connecting to the Geode cluster.</td>
      <td>Resource</td>
      <td><none></td></tr>
<tr><td>ssl-keystore-password</td>
      <td>Password for accessing the keys truststore.</td>
      <td>String</td>
      <td><none></td></tr>
<tr><td>ssl-truststore-password</td>
      <td>Password for accessing the trust store.</td>
      <td>String</td>
      <td><none></td></tr>
<tr><td>truststore-type</td>
      <td>Identifies the type of truststore used for SSL communications (e.g. JKS, PKCS11, etc.).</td>
      <td>String</td>
      <td>JKS</td></tr>
<tr><td>truststore-uri</td>
      <td>Location of the pre-created truststore URI to be used for connecting to the Geode cluster.</td>
      <td>Resource</td>
      <td><none></td></tr>
<tr><td>user-home-directory</td>
      <td>Local directory to cache the truststore and keystore files downloaded form the truststoreUri and keystoreUri locations.</td>
      <td>String</td>
      <td>user.home</td></tr>
</table>

### gemfire.supplier

<table>
    <tr>
    <th style="text-align: left">Property Name</th>
    <th style="text-align: left">Description</th>
    <th style="text-align: left">Type</th>
    <th style="text-align: left">Defaults</th>
    </tr>
<tr><td>event-expression</td>
      <td>SpEL expression to extract data from an {@link org.apache.geode.cache.EntryEvent} or {@link org.apache.geode.cache.query.CqEvent}.</td>
      <td>Expression</td>
      <td><none></td></tr>
<tr><td>query</td>
      <td>An OQL query. This will enable continuous query if provided.</td>
      <td>String</td>
      <td><none></td></tr>
</table>
