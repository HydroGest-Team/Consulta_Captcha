@echo off
::
:: Maven Wrapper startup script for Windows
::
:: This script was originally part of the Maven Wrapper project,
:: but has been modified to be a standalone wrapper script.
::
:: Copyright (c) 2018-2022 The Apache Software Foundation.
:: Licensed under the Apache License, Version 2.0 (the "License");
:: you may not use this file except in compliance with the License.
:: You may obtain a copy of the License at
::
::     https://www.apache.org/licenses/LICENSE-2.0
::
:: Unless required by applicable law or agreed to in writing, software
:: distributed under the License is distributed on an "AS IS" BASIS,
:: WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
:: See the License for the specific language governing permissions and
:: limitations under the License.

setlocal enabledelayedexpansion

set "MAVEN_VERSION=3.9.6"
set "MAVEN_HOME=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MAVEN_VERSION%-bin"
set "WRAPPER_JAR=%MAVEN_HOME%\maven-wrapper.jar"
set "WRAPPER_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"

if not exist "%WRAPPER_JAR%" (
    if not exist "%MAVEN_HOME%" mkdir "%MAVEN_HOME%"
    echo Downloading Maven Wrapper...
    curl -fsSL "%WRAPPER_URL%" -o "%WRAPPER_JAR%"
    if errorlevel 1 (
        echo Error: curl required to download Maven Wrapper
        exit /b 1
    )
)

java -cp "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*