# webui-authorize
Restrict Hadoop WebUI to authorized users and groups defined by ACL

Provides class com.cloudera.ps.authentication.SpnegoAuthorizationHandler that extends org.apache.hadoop.security.authentication.server.KerberosAuthenticationHandler. The authenticate method offered by this class delegates to the parent class org.apache.hadoop.security.authentication.server.KerberosAuthenticationHandler to authenticate the user with SPENGO. Once Authenticated, it checks if the user is authorized against a configured ACL.

## Enabling SpnegoAuthorizationHandler
* Checkout the code and compile. It should create a jar file named webui-authorize.jar in the target directory.
* Copy the jar file into a custom directory outside the parcels directory (E.g. /opt/cloudera/custom/hadoop/jars) on all the nodes in the cluster.
* For Hadoop/HDFS, Set "HDFS Service Environment Advanced Configuration Snippet (Safety Valve)" as 
  ```
  HADOOP_CLASSPATH=$HADOOP_CLASSPATH:/opt/cloudera/custom/hadoop/jars/webui-authorize.jar
  ```
* For YARN, Set "YARN (MR2 Included) Service Environment Advanced Configuration Snippet (Safety Valve)" as 
  ```
  YARN_USER_CLASSPATH=$YARN_USER_CLASSPATH:/opt/cloudera/custom/hadoop/jars/webui-authorize.jar
  ```
* For Hadoop/HDFS and YARN set "Cluster-wide Advanced Configuration Snippet (Safety Valve) for core-site.xml" as
  ```xml
  <property>
    <name>hadoop.http.authentication.type</name>
    <value>com.cloudera.ps.authentication.SpnegoAuthorizationHandler</value>
  </property>
  <property>
    <name>hadoop.http.authentication.spnego-authorize.authorize.acl</name>
    <value>hdfs,hue authorized_group</value>
  </property>
  ```    
  * (Optional) You can also specify which user-agents you do not want to be considered as browsers by setting the following property as required. Note that all Java-based programs (such as Hadoop client) will use java as their user-agent.
    ```xml
     <property>
       <name>hadoop.http.authentication.spnego-authorize.non-browser.user-agents</name>
       <value>java,curl,wget,perl,python</value>
     </property>
    ```
* For Spark History Server, Set 
  * "History Server Environment Advanced Configuration Snippet (Safety Valve)" as
    ```
    CDH_SPARK_CLASSPATH=$CDH_SPARK_CLASSPATH:/opt/cloudera/custom/hadoop/jars/webui-authorize.jar
    ```
  * "History Server Advanced Configuration Snippet (Safety Valve) for spark-conf/spark-env.sh" as
    ```
    echo "spark.org.apache.hadoop.security.authentication.server.AuthenticationFilter.param.type=com.cloudera.ps.authentication.SpnegoAuthorizationHandler" >> "$SELF/spark-history-server.conf"
    ```
  * To set Authorization ACL Set History Server Advanced Configuration Snippet (Safety Valve) for spark-conf/spark-history-server.conf to
    ```
    spark.org.apache.hadoop.security.authentication.server.AuthenticationFilter.param.alt-kerberos.authorize.acl=spark,hue systest
    ```
* Restart cluster