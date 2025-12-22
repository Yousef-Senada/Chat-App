# Rate Limiting Test Script
# استخدم هذا الـ script لاختبار Rate Limiting

Write-Host "=== Rate Limiting Test Script ===" -ForegroundColor Cyan
Write-Host ""

$baseUrl = "http://localhost:8080"
$authEndpoint = "$baseUrl/api/auth/login"

# Test Data
$testUser = @{
    username = "test@example.com"
    password = "testpassword"
} | ConvertTo-Json

Write-Host "Testing Authentication Endpoint Rate Limiting (Limit: 10 requests/minute)" -ForegroundColor Yellow
Write-Host "Sending 15 requests..." -ForegroundColor Yellow
Write-Host ""

$successCount = 0
$rateLimitedCount = 0

for ($i = 1; $i -le 15; $i++) {
    try {
        Write-Host "Request #$i : " -NoNewline
        
        $response = Invoke-WebRequest -Uri $authEndpoint `
            -Method POST `
            -Headers @{
                "Content-Type" = "application/json"
            } `
            -Body $testUser `
            -ErrorAction Stop
        
        $remaining = $response.Headers["X-Rate-Limit-Remaining"]
        Write-Host "SUCCESS (Remaining: $remaining)" -ForegroundColor Green
        $successCount++
        
    } catch {
        if ($_.Exception.Response.StatusCode -eq 429) {
            $retryAfter = $_.Exception.Response.Headers["X-Rate-Limit-Retry-After-Seconds"]
            Write-Host "RATE LIMITED (Retry after: $retryAfter seconds)" -ForegroundColor Red
            $rateLimitedCount++
        } else {
            Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Magenta
        }
    }
    
    # Small delay between requests
    Start-Sleep -Milliseconds 100
}

Write-Host ""
Write-Host "=== Test Results ===" -ForegroundColor Cyan
Write-Host "Successful Requests: $successCount" -ForegroundColor Green
Write-Host "Rate Limited Requests: $rateLimitedCount" -ForegroundColor Red
Write-Host ""

if ($rateLimitedCount -gt 0) {
    Write-Host "✅ Rate Limiting is working correctly!" -ForegroundColor Green
} else {
    Write-Host "⚠️  Rate Limiting might not be working. Check your configuration." -ForegroundColor Yellow
}
