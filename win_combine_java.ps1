# =========================
# CONFIGURATION DES CHEMINS
# =========================

$projectRoot   = "D:\ITU\L3\S5\MmeBaovola\compagnie-aerienne"
$baseJavaDir   = "$projectRoot\src\main\project"
$baseWebAppDir = "$projectRoot\src\main\webapp"

# =========================
# FONCTION : COMBINAISON AVEC CHEMIN
# =========================

function Combine-WithPath($sourcePattern, $outputFile) {

    $folder = Split-Path $sourcePattern

    if (!(Test-Path $folder)) {
        Write-Host " Dossier introuvable : $folder" -ForegroundColor Red
        return
    }

    if (Test-Path $outputFile) {
        Remove-Item $outputFile
    }

    Get-ChildItem -Path $folder -Filter (Split-Path $sourcePattern -Leaf) -File | ForEach-Object {

        Add-Content -Path $outputFile -Value "==============================" -Encoding UTF8
        Add-Content -Path $outputFile -Value "FICHIER : $($_.FullName)" -Encoding UTF8
        Add-Content -Path $outputFile -Value "==============================`n" -Encoding UTF8

        Get-Content $_.FullName | Add-Content -Path $outputFile -Encoding UTF8

        Add-Content -Path $outputFile -Value "`n`n" -Encoding UTF8
    }

    Write-Host " Cree : $outputFile" -ForegroundColor Green
}

# =========================
# FONCTION : COMBINAISON RECURSIVE
# =========================

function Combine-WithPath-Recursive($baseFolder, $filter, $outputFile) {

    if (!(Test-Path $baseFolder)) {
        Write-Host " Dossier introuvable : $baseFolder" -ForegroundColor Red
        return
    }

    if (Test-Path $outputFile) {
        Remove-Item $outputFile
    }

    Get-ChildItem -Path $baseFolder -Filter $filter -File -Recurse | ForEach-Object {

        Add-Content -Path $outputFile -Value "==============================" -Encoding UTF8
        Add-Content -Path $outputFile -Value "FICHIER : $($_.FullName)" -Encoding UTF8
        Add-Content -Path $outputFile -Value "==============================`n" -Encoding UTF8

        Get-Content $_.FullName | Add-Content -Path $outputFile -Encoding UTF8

        Add-Content -Path $outputFile -Value "`n`n" -Encoding UTF8
    }

    Write-Host " Cree : $outputFile" -ForegroundColor Green
}

# =========================
# GENERATION STRUCTURE (TREE)
# =========================

Write-Host "`n Generation de struct.txt..."
tree "$projectRoot" /F | Out-File "$projectRoot\struct.txt" -Encoding UTF8

# =========================
# COMBINAISON DES FICHIERS JAVA
# =========================

Write-Host "`n Combinaison des fichiers Java..."

Combine-WithPath "$baseJavaDir\beans\*.java"   "$baseJavaDir\beans\beans.txt"
Combine-WithPath "$baseJavaDir\dao\*.java"     "$baseJavaDir\dao\daos.txt"
Combine-WithPath "$baseJavaDir\service\*.java" "$baseJavaDir\service\services.txt"
Combine-WithPath "$baseJavaDir\util\*.java"    "$baseJavaDir\util\utils.txt"
Combine-WithPath "$baseJavaDir\servlet\*.java" "$baseJavaDir\servlet\servlets.txt"

# =========================
# COMBINAISON DES JSP (RECURSIF)
# =========================

Write-Host "`n Combinaison des JSP..."

Combine-WithPath-Recursive "$baseWebAppDir" "*.jsp" "$baseWebAppDir\jsps.txt"

# =========================
# FIN
# =========================

Write-Host "`n Termine avec succes." -ForegroundColor Cyan
