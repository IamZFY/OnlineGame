set PATH=D:\j2sdk1.4.0\bin;%PATH%
set PATH=C:\j2sdk1.4.0\bin;%PATH%
del /q classes\globaldef\*
del /q classes\client\*
javac -sourcepath src -classpath classes -d classes src\client\MainFrame.java
del /q classes\server\*
javac -sourcepath src -classpath classes -d classes src\server\GameServer.java
