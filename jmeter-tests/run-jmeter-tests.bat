@echo off
REM JMeter Test Runner for Genesys Integration
REM This batch file runs the JMeter tests for the Genesys integration system

set JMETER_HOME=C:\apache-jmeter-5.4.1
set TEST_FILE=genesys-load-test.jmx
set RESULTS_DIR=results
set LOG_FILE=jmeter-test.log

echo Starting JMeter Tests for Genesys Integration...
echo Test started at: %date% %time% > %LOG_FILE%

REM Create results directory if it doesn't exist
if not exist "%RESULTS_DIR%" mkdir "%RESULTS_DIR%"

REM Run JMeter test
echo Running JMeter test: %TEST_FILE%
"%JMETER_HOME%\bin\jmeter.bat" -n -t "%TEST_FILE%" -l "%RESULTS_DIR%\test-results.jtl" -e -o "%RESULTS_DIR%\dashboard"

if %ERRORLEVEL% EQU 0 (
    echo JMeter test completed successfully
    echo Test completed at: %date% %time% >> %LOG_FILE%
    echo Results available in: %RESULTS_DIR%\dashboard
    echo JTL results: %RESULTS_DIR%\test-results.jtl
) else (
    echo JMeter test failed with error code: %ERRORLEVEL%
    echo Test failed at: %date% %time% - Error: %ERRORLEVEL% >> %LOG_FILE%
)

echo.
echo Test execution completed.
pause