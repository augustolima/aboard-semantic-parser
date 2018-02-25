@echo off
rem jslim.bat -- created 26 Aug 2010 10:20:04, Andreas Struller
rem @Last Change: 26-Aug-2010.
rem @Revision:    0.2

rem installation dir
set JSLIM=../jslim2.1

rem classes and jar-files needed by jslim
set JSLIMLIB=%JSLIM%/lib

rem java interpreter
set JAVA6=java

rem main class of jslim
set MAIN=de.uni_erlangen.linguistik.lag.jslim.Main

rem classpath used for invoking jslim
set CLASSPATH=%JSLIMLIB%/jslim-cor.jar;%JSLIMLIB%/jslim-io.jar;%JSLIMLIB%/jslim-opt.jar;%JSLIMLIB%/jslim-tls.jar;%JSLIMLIB%/jslim-i18n.jar;%JSLIMLIB%/jslim-ops.jar;%JSLIMLIB%/jslim-shell.jar;%JSLIMLIB%/ext/antlr-3.1.1.jar;%JSLIMLIB%/ext/log4j-1.2.15.jar;%JSLIMLIB%/ext/antlr.jar;%JSLIMLIB%/ext/jline-0.9.94.jar;%JSLIMLIB%/ext/xalan.jar;

rem start jslim
%JAVA6% -cp %CLASSPATH% %MAIN% common/english1.pro

rem %*
rem %1 %2 %3 %4 %5 %6 %7 %8 %9

rem vi: 
