call gradlew -Dorg.gradle.java.home="C:/Program Files/Eclipse Adoptium/jdk-21.0.3.9-hotspot" build
pushd "..\minecraft-heads-browser-template-1.21.5 - minimal\"
call gradlew -Dorg.gradle.java.home="C:/Program Files/Eclipse Adoptium/jdk-21.0.3.9-hotspot" build
popd
echo f | xcopy /s /y ".\versions\1.21.0-fabric\build\libs\minecraft-heads-browser-1.0.0.jar" ".\output\minecraft-heads-browser-1.0.0-1.21-1.21.4.jar"
echo f | xcopy /s /y "..\minecraft-heads-browser-template-1.21.5 - minimal\versions\1.21.0-fabric\build\libs\minecraft-heads-browser-1.0.0.jar" ".\output\minecraft-heads-browser-1.0.0-1.21-1.21.4_min.jar"
echo f | xcopy /s /y ".\versions\1.21.5-fabric\build\libs\minecraft-heads-browser-1.0.0.jar" ".\output\minecraft-heads-browser-1.0.0-1.21.5.jar"
echo f | xcopy /s /y "..\minecraft-heads-browser-template-1.21.5 - minimal\versions\1.21.5-fabric\build\libs\minecraft-heads-browser-1.0.0.jar" ".\output\minecraft-heads-browser-1.0.0-1.21.5_min.jar"