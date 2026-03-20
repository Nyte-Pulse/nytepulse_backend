package NytePulse.backend.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/.well-known")
public class DeepLinkController {

    // 1. Android App Links Configuration
    @GetMapping(value = "/assetlinks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getAndroidAssetLinks() {
        String json = "[\n" +
                "  {\n" +
                "    \"relation\": [\"delegate_permission/common.handle_all_urls\"],\n" +
                "    \"target\": {\n" +
                "      \"namespace\": \"android_app\",\n" +
                "      \"package_name\": \"com.abhishek.nytepulse\",\n" + // Replace with your actual package name
                "      \"sha256_cert_fingerprints\": [\"52:7D:CF:D1:EB:BE:A2:9F:A5:F5:03:33:80:58:AD:60:0D:E1:32:EB:02:4C:3D:C5:4C:AF:D2:2B:25:B0:D5:4B\"]\n" +
                "    }\n" +
                "  }\n" +
                "]";
        return ResponseEntity.ok(json);
    }

    // 2. iOS Universal Links Configuration
    @GetMapping(value = "/apple-app-site-association", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getIOSUniversalLinks() {
        String json = "{\n" +
                "  \"applinks\": {\n" +
                "    \"apps\": [],\n" +
                "    \"details\": [\n" +
                "      {\n" +
                "        \"appID\": \"YOUR_APPLE_TEAM_ID.com.nytepulse.app\",\n" + // Replace Team ID and Bundle ID
                "        \"paths\": [\"/profile/*\"]\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}";
        return ResponseEntity.ok(json);
    }
}