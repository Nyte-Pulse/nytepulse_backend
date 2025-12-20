package NytePulse.backend.service.impl;

import NytePulse.backend.dto.ProfileLinkResponse;
import NytePulse.backend.dto.UserProfileDTO;
import NytePulse.backend.entity.ClubDetails;
import NytePulse.backend.entity.User;
import NytePulse.backend.entity.UserDetails;
import NytePulse.backend.exception.ResourceNotFoundException;
import NytePulse.backend.repository.ClubDetailsRepository;
import NytePulse.backend.repository.UserDetailsRepository;
import NytePulse.backend.repository.UserRepository;
import NytePulse.backend.service.centralServices.ProfileLinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ProfileLinkServiceImpl implements ProfileLinkService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDetailsRepository userDetailsRepository;

    @Autowired
    private ClubDetailsRepository clubDetailsRepository;

    @Value("${app.base.url:https://nytepulse.com/68eab83da0d013a61e366db6c8b43175}")
    private String baseUrl;
    @Override
    public ResponseEntity<?> generateProfileLink(String userId) {
        try{
            User user = userRepository.findByUserId(userId);

            String profileUrl = generateUrlByAccountType(user);

            ProfileLinkResponse response = new ProfileLinkResponse();
            response.setProfileUrl(profileUrl);
            response.setUserId(userId);
            response.setUsername(user.getUsername());
            response.setAccountType(user.getAccountType());

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Success!");
            result.put("response",response);
            result.put("status", HttpStatus.OK.value());

            return ResponseEntity.ok(response);
        } catch (Exception e){
            Map<String, Object> errorresult = new HashMap<>();
            errorresult.put("message", "User not found with userId: " + userId);
            errorresult.put("status", HttpStatus.NOT_FOUND.value());
            return ResponseEntity.ok(errorresult);
        }
    }
    @Override
    public ResponseEntity<?> generateProfileLinkByUsername(String username) {
        try{
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

            String profileUrl = generateUrlByAccountType(user);

            ProfileLinkResponse response = new ProfileLinkResponse();
            response.setProfileUrl(profileUrl);
            response.setUserId(user.getUserId());
            response.setUsername(user.getUsername());
            response.setAccountType(user.getAccountType());

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Success!");
            result.put("response",response);
            result.put("status", HttpStatus.OK.value());
            return ResponseEntity.ok(response);

        } catch (Exception e){
            Map<String, Object> errorresult = new HashMap<>();
            errorresult.put("message", "User not found with username: " + username);
            errorresult.put("status", HttpStatus.NOT_FOUND.value());
            return ResponseEntity.ok(errorresult);
        }

    }

    private String generateUrlByAccountType(User user) {
        try{
            String username = user.getUsername();
            String accountType = user.getAccountType();

            if ("club".equalsIgnoreCase(accountType) || "business".equalsIgnoreCase(accountType)) {
                return baseUrl + "/profile/club/" + username;
            } else {
                return baseUrl + "/profile/" + username;
            }
        } catch (Exception e){
            throw new ResourceNotFoundException("User not found");
        }

    }

    @Override
    public ResponseEntity<?> getUserProfileByUsername(String username) {
        try{
            UserDetails userDetails = userDetailsRepository.findByUsername(username);
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Success!");
            result.put("response",mapUserDetailsToDTO(userDetails));
            result.put("status", HttpStatus.OK.value());
            return ResponseEntity.ok(result);
        }catch (Exception e){
            throw new ResourceNotFoundException("User not found with username: " + username);
        }

    }

    public ResponseEntity<?> getClubProfileByUsername(String username) {
        try{
            ClubDetails clubDetails = clubDetailsRepository.findByUsername(username);

            UserProfileDTO dto = mapClubDetailsToDTO(clubDetails);
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Success!");
            result.put("response",dto);
            result.put("status", HttpStatus.OK.value());
            return ResponseEntity.ok(result);
        }catch (Exception e){
            throw new ResourceNotFoundException("Club not found with username: " + username);
        }

    }

    private UserProfileDTO mapUserDetailsToDTO(UserDetails userDetails) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setUserId(userDetails.getUserId());
        dto.setUsername(userDetails.getUsername());
        dto.setEmail(userDetails.getEmail());
        dto.setName(userDetails.getName());
        dto.setBio(userDetails.getBio());
        dto.setProfilePicture(userDetails.getProfilePicture());
        dto.setAccountType(userDetails.getAccountType());
        dto.setIsPrivate(userDetails.getIsPrivate());
        dto.setStatus(userDetails.getStatus());
        dto.setProfileUrl(baseUrl + "/profile/" + userDetails.getUsername());
        dto.setGender(userDetails.getGender());
        dto.setDateTimeCreated(userDetails.getDateTimeCreated());

        return dto;
    }

    private UserProfileDTO mapClubDetailsToDTO(ClubDetails clubDetails) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setUserId(clubDetails.getUserId());
        dto.setUsername(clubDetails.getUsername());
        dto.setEmail(clubDetails.getEmail());
        dto.setName(clubDetails.getName());
        dto.setBio(clubDetails.getBio());
        dto.setProfilePicture(clubDetails.getProfilePicture());
        dto.setAccountType(clubDetails.getAccountType());
        dto.setStatus(clubDetails.getStatus());
        dto.setProfileUrl(baseUrl + "/profile/club/" + clubDetails.getUsername());
        dto.setContactPhone(clubDetails.getContactPhone());
        dto.setFollowersCount(clubDetails.getFollowersCount());
        dto.setEventsPublishedCount(clubDetails.getEventsPublishedCount());
        dto.setRatingAvg(clubDetails.getRatingAvg());
        dto.setDateTimeCreated(clubDetails.getDateTimeCreated());

        return dto;
    }
}
