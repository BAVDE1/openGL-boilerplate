#!/bin/bash
# manual build MEGA BROKEN RN
javac -cp modules/lwjgl-3.3.6-linux64-minimal-opengl/lwjgl.jar:\
modules/lwjgl-3.3.6-linux64-minimal-opengl/lwjgl-assimp.jar:\
modules/lwjgl-3.3.6-linux64-minimal-opengl/lwjgl-assimp-javadoc.jar:\
modules/lwjgl-3.3.6-linux64-minimal-opengl/lwjgl-assimp-natives-linux.jar:\
modules/lwjgl-3.3.6-linux64-minimal-opengl/lwjgl-assimp-sources.jar:\
modules/lwjgl-3.3.6-linux64-minimal-opengl/lwjgl-glfw.jar:\
modules/lwjgl-3.3.6-linux64-minimal-opengl/lwjgl-glfw-javadoc.jar:\
modules/lwjgl-3.3.6-linux64-minimal-opengl/lwjgl-glfw-natives-linux.jar:\
modules/lwjgl-3.3.6-linux64-minimal-opengl/lwjgl-glfw-sources.jar:\
modules/lwjgl-3.3.6-linux64-minimal-opengl/lwjgl-javadoc.jar:\
modules/lwjgl-3.3.6-linux64-minimal-opengl/lwjgl-natives-linux.jar:\
modules/lwjgl-3.3.6-linux64-minimal-opengl/lwjgl-openal.jar:\
modules/lwjgl-3.3.6-linux64-minimal-opengl/lwjgl-openal-javadoc.jar:\
modules/lwjgl-3.3.6-linux64-minimal-opengl/lwjgl-openal-natives-linux.jar:\
modules/lwjgl-3.3.6-linux64-minimal-opengl/lwjgl-openal-sources.jar:\
modules/lwjgl-3.3.6-linux64-minimal-opengl/lwjgl-opengl.jar:\
modules/lwjgl-3.3.6-linux64-minimal-opengl/lwjgl-opengl-javadoc.jar:\
modules/lwjgl-3.3.6-linux64-minimal-opengl/lwjgl-opengl-natives-linux.jar:\
modules/lwjgl-3.3.6-linux64-minimal-opengl/lwjgl-opengl-sources.jar:\
modules/lwjgl-3.3.6-linux64-minimal-opengl/lwjgl-sources.jar:\
modules/lwjgl-3.3.6-linux64-minimal-opengl/lwjgl-stb.jar:\
modules/lwjgl-3.3.6-linux64-minimal-opengl/lwjgl-stb-javadoc.jar:\
modules/lwjgl-3.3.6-linux64-minimal-opengl/lwjgl-stb-natives-linux.jar:\
modules/lwjgl-3.3.6-linux64-minimal-opengl/lwjgl-stb-sources.jar:\
manifest.mf\
 --source-path src/ src/boilerplate/ExampleMain.java
jar -cvfm build.jar manifest.mf boilerplate/ExampleMain.class res/