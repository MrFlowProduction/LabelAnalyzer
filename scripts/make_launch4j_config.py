import sys

version = sys.argv[1] if len(sys.argv) > 1 else "1.0.0"
xml = """<?xml version="1.0" encoding="UTF-8"?>
<launch4jConfig>
  <headerType>gui</headerType>
  <outfile>app-dist/LabelAnalyzer.exe</outfile>
  <jar>app-dist/app/app.jar</jar>
  <errTitle>LabelAnalyzer</errTitle>
  <icon>app.ico</icon>
  <classPath>
    <mainClass>hu.mrflow.labelanalyzer.MainApp</mainClass>
  </classPath>
  <jre>
    <path>runtime</path>
    <bundledJre64Bit>true</bundledJre64Bit>
    <opts>
      <opt>--module-path javafx</opt>
      <opt>--add-modules javafx.controls,javafx.fxml</opt>
    </opts>
  </jre>
  <versionInfo>
    <fileVersion>1.0.0.0</fileVersion>
    <txtFileVersion>""" + version + """</txtFileVersion>
    <fileDescription>Novenyvedoszer engedelyelemzo</fileDescription>
    <copyright>MrFlow</copyright>
    <productVersion>1.0.0.0</productVersion>
    <txtProductVersion>""" + version + """</txtProductVersion>
    <productName>LabelAnalyzer</productName>
    <internalName>LabelAnalyzer</internalName>
    <originalFilename>LabelAnalyzer.exe</originalFilename>
  </versionInfo>
</launch4jConfig>"""
with open("launch4j-config.xml", "w", encoding="utf-8") as f:
    f.write(xml)
print("launch4j-config.xml created")