# GCMAdaptiveHeartBeater
This is the repository for an Android Studio project implementing adaptive keep-alive interval. It has been used for expeimentation used in our paper that has been submitted to IEEE TON. The application is called GCMAdaptiveHeartBeater.

Originally, the code was written to adaptively update the heartbeat interval of GCM/FCM connection. Later on, we have changed to code to work with our custom notification service. (Refer to https://github.com/srautonu/AdaptiveHeartBeat). Nonetheless, with minor changes the adaptive scheme could be applied to GCM connection.

To compile and run the code, kindly launch in Android Studio.

To point the KA interval testing code to your own server, please change the first 2 lines of BackGroundServices.KATesterSerService class
    String m_strServerDNS = "Your_Server_Name_Here";
    int m_serverPort = Your_Port_Here;

To point the custom notification service code to your own server, please change the first 2 lines of BackGroundServices.KADataService class
(You will also need to run our custom notification server code at your server)
    String _strServerDNS = "Your_Server_Name_Here";
    int _serverPort = Your_Port_Here;
