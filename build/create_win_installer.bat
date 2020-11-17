set jpackagePath="C:\Program Files\Java\jdk-14.0.2\bin\jpackage"
set version=%1
%jpackagePath% --name GraphicalClientStarter --main-jar GraphicalClientStarter-v%version%.jar --type exe --input ../shade --app-version %version% --win-shortcut --dest output