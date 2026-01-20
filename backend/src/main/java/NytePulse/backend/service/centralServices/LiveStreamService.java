package NytePulse.backend.service.centralServices;


import NytePulse.backend.dto.StartStreamRequest;
import NytePulse.backend.dto.StreamResponse;

public interface LiveStreamService {
    StreamResponse startStream(StartStreamRequest request);
}