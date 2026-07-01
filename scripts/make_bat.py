import os
os.makedirs("app-dist", exist_ok=True)

run = "\r\n".join([
    "@echo off",
    "cd /d \"%~dp0\"",
    "set PATH=%~dp0javafx;%PATH%",
    "mkdir \"%USERPROFILE%\\.labelanalyzer\\logs\" 2>nul",
    "runtime\\bin\\javaw.exe --module-path \"javafx\" --add-modules javafx.controls,javafx.fxml -jar \"app\\app.jar\"",
    ""
])
with open("app-dist/LabelAnalyzer.bat", "w", encoding="ascii", errors="replace") as f:
    f.write(run)
print("LabelAnalyzer.bat created")

dbg = "\r\n".join([
    "@echo off",
    "cd /d \"%~dp0\"",
    "set PATH=%~dp0javafx;%PATH%",
    "mkdir \"%USERPROFILE%\\.labelanalyzer\\logs\" 2>nul",
    "echo Inditom a LabelAnalyzert...",
    "runtime\\bin\\java.exe --module-path \"javafx\" --add-modules javafx.controls,javafx.fxml -jar \"app\\app.jar\" > \"%USERPROFILE%\\.labelanalyzer\\logs\\startup.log\" 2>&1",
    "echo.",
    "echo Kilepes kodja: %ERRORLEVEL%",
    "echo Log: %USERPROFILE%\\.labelanalyzer\\logs\\startup.log",
    "echo.",
    "type \"%USERPROFILE%\\.labelanalyzer\\logs\\startup.log\"",
    "pause",
    ""
])
with open("app-dist/debug.bat", "w", encoding="ascii", errors="replace") as f:
    f.write(dbg)
print("debug.bat created")