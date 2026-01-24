$baseUrl = "http://localhost:8080/api/v1"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Eduwork Identity Module - API Testing" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Test 1: Register a new user
Write-Host "Test 1: Register User" -ForegroundColor Yellow
$registerBody = @{
    fullName = "Test User"
    email = "test@eduwork.com"
    password = "TestPass123!"
    role = "STUDENT"
} | ConvertTo-Json

try {
    $registerResponse = Invoke-WebRequest -Uri "$baseUrl/auth/register" `
        -Method POST `
        -ContentType "application/json" `
        -Body $registerBody
    
    Write-Host "✅ Registration successful!" -ForegroundColor Green
    Write-Host "Response: $($registerResponse.Content)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Registration failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Response: $($_.ErrorDetails.Message)" -ForegroundColor Gray
}

Write-Host ""

# Test 2: Login
Write-Host "Test 2: Login" -ForegroundColor Yellow
$loginBody = @{
    email = "test@eduwork.com"
    password = "TestPass123!"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-WebRequest -Uri "$baseUrl/auth/login" `
        -Method POST `
        -ContentType "application/json" `
        -Headers @{"User-Agent" = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"} `
        -Body $loginBody
    
    $loginData = ($loginResponse.Content | ConvertFrom-Json).data
    $accessToken = $loginData.accessToken
    $sessionId = $loginData.sessionId
    
    Write-Host "✅ Login successful!" -ForegroundColor Green
    Write-Host "Access Token: $accessToken" -ForegroundColor Gray
    Write-Host "Session ID: $sessionId" -ForegroundColor Gray
} catch {
    Write-Host "❌ Login failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Response: $($_.ErrorDetails.Message)" -ForegroundColor Gray
    exit 1
}

Write-Host ""

# Test 3: Complete student profile
Write-Host "Test 3: Complete Student Profile" -ForegroundColor Yellow
$profileBody = @{
    gradeLevel = 10
    schoolName = "Test High School"
    bio = "I am a student learning programming"
} | ConvertTo-Json

try {
    $profileResponse = Invoke-WebRequest -Uri "$baseUrl/users/me/profile/student" `
        -Method PUT `
        -ContentType "application/json" `
        -Headers @{Authorization = "Bearer $accessToken"} `
        -Body $profileBody
    
    Write-Host "✅ Profile completed!" -ForegroundColor Green
    Write-Host "Response: $($profileResponse.Content)" -ForegroundColor Gray
} catch {
    Write-Host "❌ Profile completion failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Response: $($_.ErrorDetails.Message)" -ForegroundColor Gray
}

Write-Host ""

# Test 4: Get user profile (Feature 1)
Write-Host "Test 4: Get User Profile (Feature 1)" -ForegroundColor Yellow
try {
    $getUserResponse = Invoke-WebRequest -Uri "$baseUrl/users/me" `
        -Method GET `
        -Headers @{Authorization = "Bearer $accessToken"}
    
    Write-Host "✅ Profile retrieved successfully!" -ForegroundColor Green
    Write-Host "Profile Data:" -ForegroundColor Gray
    Write-Host $getUserResponse.Content -ForegroundColor Gray
} catch {
    Write-Host "❌ Failed to get profile: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Response: $($_.ErrorDetails.Message)" -ForegroundColor Gray
}

Write-Host ""

# Test 5: View active sessions (Feature 2)
Write-Host "Test 5: View Active Sessions (Feature 2)" -ForegroundColor Yellow
try {
    $sessionsResponse = Invoke-WebRequest -Uri "$baseUrl/users/me/sessions" `
        -Method GET `
        -Headers @{
            Authorization = "Bearer $accessToken"
            "X-Session-Id" = $sessionId
        }
    
    Write-Host "✅ Sessions retrieved successfully!" -ForegroundColor Green
    Write-Host "Session Data:" -ForegroundColor Gray
    Write-Host $sessionsResponse.Content -ForegroundColor Gray
} catch {
    Write-Host "❌ Failed to get sessions: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Response: $($_.ErrorDetails.Message)" -ForegroundColor Gray
}

Write-Host ""

# Test 6: Upload KYC document (Feature 3)
Write-Host "Test 6: Upload KYC Document (Feature 3)" -ForegroundColor Yellow

# Create a test file
$testFilePath = "test-id.txt"
"This is a test ID card document" | Out-File -FilePath $testFilePath

try {
    # Read file content
    $fileBytes = [System.IO.File]::ReadAllBytes((Resolve-Path $testFilePath))
    $fileContent = [System.Text.Encoding]::GetEncoding('iso-8859-1').GetString($fileBytes)
    
    # Create multipart form data
    $boundary = [System.Guid]::NewGuid().ToString()
    $LF = "`r`n"
    
    $bodyLines = @(
        "--$boundary",
        "Content-Disposition: form-data; name=`"type`"$LF",
        "ID_CARD",
        "--$boundary",
        "Content-Disposition: form-data; name=`"file`"; filename=`"test-id.txt`"",
        "Content-Type: text/plain$LF",
        $fileContent,
        "--$boundary--$LF"
    ) -join $LF
    
    $kycResponse = Invoke-WebRequest -Uri "$baseUrl/users/me/kyc/upload" `
        -Method POST `
        -ContentType "multipart/form-data; boundary=$boundary" `
        -Headers @{Authorization = "Bearer $accessToken"} `
        -Body $bodyLines
    
    Write-Host "✅ KYC document uploaded!" -ForegroundColor Green
    Write-Host "Response: $($kycResponse.Content)" -ForegroundColor Gray
} catch {
    Write-Host "❌ KYC upload failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Response: $($_.ErrorDetails.Message)" -ForegroundColor Gray
} finally {
    # Clean up test file
    if (Test-Path $testFilePath) {
        Remove-Item $testFilePath
    }
}

Write-Host ""

# Test 7: Get KYC documents
Write-Host "Test 7: Get KYC Documents" -ForegroundColor Yellow
try {
    $kycListResponse = Invoke-WebRequest -Uri "$baseUrl/users/me/kyc" `
        -Method GET `
        -Headers @{Authorization = "Bearer $accessToken"}
    
    Write-Host "✅ KYC documents retrieved!" -ForegroundColor Green
    Write-Host "Documents:" -ForegroundColor Gray
    Write-Host $kycListResponse.Content -ForegroundColor Gray
} catch {
    Write-Host "❌ Failed to get KYC documents: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Response: $($_.ErrorDetails.Message)" -ForegroundColor Gray
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Testing Complete!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
