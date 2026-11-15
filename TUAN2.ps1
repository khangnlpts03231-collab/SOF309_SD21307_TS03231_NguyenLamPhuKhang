param([switch]$Publish)
$ErrorActionPreference = 'Stop'
function GitRun([string[]]$Arguments) {
    & git @Arguments
    if ($LASTEXITCODE -ne 0) { throw 'Git failed. See output above.' }
}
try {
    Set-Location $PSScriptRoot
    $candidates = @()
    if ($env:JAVA_HOME) { $candidates += $env:JAVA_HOME }
    foreach ($parent in @("$env:LOCALAPPDATA\Programs\Eclipse Adoptium", "$env:ProgramFiles\Eclipse Adoptium", "$env:ProgramFiles\Java")) {
        if (Test-Path $parent) { $candidates += (Get-ChildItem $parent -Directory).FullName }
    }
    $selected = $null
    foreach ($candidate in $candidates) {
        $release = Join-Path $candidate 'release'
        if ((Test-Path "$candidate\bin\javac.exe") -and (Test-Path $release)) {
            if ((Get-Content $release -Raw) -match 'JAVA_VERSION="(17|21)\.') { $selected = $candidate; break }
        }
    }
    if (-not $selected) { throw 'JDK 17/21 not found. Install it or set JAVA_HOME to its folder.' }
    $env:JAVA_HOME = $selected
    $env:PATH = "$selected\bin;$env:PATH"
    Write-Host "JDK: $selected"
    $maven = Get-Command mvn.cmd -ErrorAction SilentlyContinue
    if ($maven) { $mvn = $maven.Source } else {
        $cache = Join-Path $env:LOCALAPPDATA 'CelineWeek2Tools'
        $mvn = Join-Path $cache 'apache-maven-3.9.9\bin\mvn.cmd'
        if (-not (Test-Path $mvn)) {
            New-Item $cache -ItemType Directory -Force | Out-Null
            [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
            $archive = Join-Path $cache 'maven-3.9.9.zip'
            $url = 'https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip'
            Write-Host 'Downloading Maven...'
            Invoke-WebRequest $url -OutFile $archive -UseBasicParsing
            $expected = (Invoke-WebRequest ($url + '.sha512') -UseBasicParsing).Content.Trim().Split(' ')[0]
            if ((Get-FileHash $archive -Algorithm SHA512).Hash -ne $expected) { throw 'Maven checksum mismatch.' }
            Expand-Archive $archive -DestinationPath $cache -Force
        }
    }
    & $mvn clean test '-Dtest=*ControllerTest' '-Dstyle.color=never'
    if ($LASTEXITCODE -ne 0) { throw 'Tests failed. Nothing has been pushed.' }
    $reports = @(Get-ChildItem "$PSScriptRoot\target\surefire-reports\TEST-*ControllerTest.xml")
    $total = 0
    foreach ($report in $reports) {
        [xml]$xml = Get-Content $report.FullName -Raw
        $total += [int]$xml.testsuite.tests
        if ([int]$xml.testsuite.failures -gt 0 -or [int]$xml.testsuite.errors -gt 0 -or [int]$xml.testsuite.skipped -gt 0) { throw 'Reports contain failed or skipped tests.' }
    }
    if ($total -lt 67) { throw "Expected at least 67 tests, got $total. Nothing pushed." }
    Write-Host "PASS: $total tests."
    if (-not $Publish) { exit 0 }
    if (-not (Get-Command git -ErrorAction SilentlyContinue)) { throw 'Install Git for Windows, then run again.' }
    $repo = 'https://github.com/khangnlpts03231-collab/SOF309_SD21307_TS03231_NguyenLamPhuKhang.git'
    $checkout = Join-Path (Split-Path $PSScriptRoot -Parent) ('GitHub_Tuan2_' + [guid]::NewGuid().ToString('N').Substring(0,8))
    GitRun @('clone','--branch','main','--single-branch',$repo,$checkout)
    $files = @('pom.xml','src/test/java/vn/celineclosset/controller/AuthControllerTest.java','src/test/java/vn/celineclosset/controller/AdminCategoryControllerTest.java','docs/DEMO_TRUOC_THAY.md','docs/KET_QUA_JUNIT_TUAN2.md','docs/ai-logs/02_junit_script_ai.md','TUAN2.ps1','01_KIEM_TRA_JUNIT.cmd','02_DANG_GITHUB.cmd','HUONG_DAN_NHANH.md')
    foreach ($relative in $files) {
        $dest = Join-Path $checkout $relative
        New-Item (Split-Path $dest -Parent) -ItemType Directory -Force | Out-Null
        Copy-Item (Join-Path $PSScriptRoot $relative) $dest -Force
    }
    # Only publish week-2 files; keep the latest week-1 docs and application source in GitHub.
    $proof = Join-Path $checkout 'docs/minh-chung-junit/windows-latest'
    New-Item $proof -ItemType Directory -Force | Out-Null
    Get-ChildItem "$PSScriptRoot\target\surefire-reports\*ControllerTest.txt" | Copy-Item -Destination $proof -Force
    @("Run time: $(Get-Date -Format o)","Tests: $total; failures/errors/skipped: 0",'Command: mvn clean test -Dtest=*ControllerTest') | Set-Content "$proof\result.txt" -Encoding UTF8
    Set-Location $checkout
    GitRun @('config','user.name','Nguyen Lam Phu Khang')
    & git config user.email
    if ($LASTEXITCODE -ne 0) { GitRun @('config','user.email','khangnlpts03231-collab@users.noreply.github.com') }
    GitRun (@('add','--') + $files + @('docs/minh-chung-junit/windows-latest'))
    GitRun @('commit','-m','Complete week 2 JUnit tests and execution evidence')
    GitRun @('-c','credential.username=khangnlpts03231-collab','push','origin','HEAD:main')
    $commit = (& git rev-parse HEAD).Trim()
    $link = "https://github.com/khangnlpts03231-collab/SOF309_SD21307_TS03231_NguyenLamPhuKhang/commit/$commit"
    Set-Content (Join-Path $PSScriptRoot 'LINK_NOP_TUAN2.txt') $link
    Write-Host "SUCCESS: $link"
    Start-Process $link
} catch {
    Write-Host $_.Exception.Message -ForegroundColor Red
    if ($checkout) { Write-Host "Working copy retained: $checkout" }
    exit 1
}
