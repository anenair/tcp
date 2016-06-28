Build 
  mvn clean install
  
Usage :
  java -cp target/messge-delivery-0.0.1-SNAPSHOT.jar com.unity.socket.Server <port> 
    default port is 9090
  java -cp target/messge-delivery-0.0.1-SNAPSHOT.jar com.unity.socket.Client <port>
    default port is 9090

to get the identity of the client : {"type":"ME"}
to get others who are online : {"type":"OTHERS"}
to relay messages to other clients : {"type":"RELAY","message":"hi","to":"2,3"}
