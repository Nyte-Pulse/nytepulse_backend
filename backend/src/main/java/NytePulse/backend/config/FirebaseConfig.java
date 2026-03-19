package NytePulse.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.InputStream;
@Configuration
public class FirebaseConfig {

    @Value("${firebase.key.path:/etc/secrets/npulse-push-notification-firebase-adminsdk-fbsvc-95e7983304.json}")
    private String firebaseKeyPath;


    @PostConstruct
    public void initialize() {
        try {
            // Use FileInputStream to read from an external server path
            InputStream serviceAccount = new FileInputStream(firebaseKeyPath);

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase initialized from: " + firebaseKeyPath);
            }
        } catch (Exception e) {
            System.err.println("Firebase Init Error: " + e.getMessage());
        }
    }
}