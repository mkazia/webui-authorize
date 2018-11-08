# webui-authorize
Restrict Hadoop WebUI to a authorized users and groups defined by ACL

Provides class com.cloudera.ps.authentication.WebUIAuthorizationHandler that implementats org.apache.hadoop.security.authentication.serverAltKerberosAuthenticationHandler. The alternate authentication method offered by this class delegates to the parent class org.apache.hadoop.security.authentication.server.KerberosAuthenticationHandler to authenticate the user with SPENGO. Once Authenticated, it checks if the user is authorized against a configured ACL.

## Enabling WebUIAuthorizationHandler on Hadoop Web UIs
* Stop Hadoop
* Set the following property in core-site.xml
```xml
<property>
  <name>hadoop.http.authentication.type</name>
  <value>com.cloudera.ps.authentication.WebUIAuthorizationHandler</value>
</property>
<property>
  <name>hadoop.http.authentication.alt-kerberos.authorize.acl</name>
  <value>hue authorized_group</value>
</property>
```    
* (Optional) You can also specify which user-agents you do not want to be considered as browsers by setting the following property as required (default value is shown). Note that all Java-based programs (such as Hadoop client) will use java as their user-agent.
```xml
<property>
  <name>hadoop.http.authentication.alt-kerberos.non-browser.user-agents</name>
  <value>java,curl,wget,perl</value>
</property>
```
* Copy the JAR containing your subclass into $HADOOP_HOME/lib
* Start Hadoop
