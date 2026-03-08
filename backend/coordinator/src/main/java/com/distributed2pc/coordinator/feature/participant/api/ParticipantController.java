package com.distributed2pc.coordinator.feature.participant.api;

import com.distributed2pc.common.dto.ParticipantRegistrationDto;
import com.distributed2pc.coordinator.feature.participant.application.ParticipantRegistrationService;
import com.distributed2pc.coordinator.feature.participant.domain.RegisteredParticipant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for participant registration and listing.
 *
 * <p>
 * Participants call {@code POST /api/participants/register} during their
 * startup
 * to announce themselves to the coordinator.
 */
@Tag(name = "Participants", description = "Register and list participant nodes")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/participants")
public class ParticipantController {

 private final ParticipantRegistrationService registrationService;

 /**
  * Accepts a participant self-registration request.
  *
  * @param dto connection details of the registering participant.
  */
 @Operation(summary = "Registers a participant with the coordinator")
 @ApiResponse(responseCode = "201", description = "Participant registered")
 @ApiResponse(responseCode = "400", description = "Malformed registration payload")
 @PostMapping("/register")
 @ResponseStatus(HttpStatus.CREATED)
 public void register(@RequestBody ParticipantRegistrationDto dto) {
  registrationService.register(dto);
 }

 /**
  * Returns all participants currently known to the coordinator.
  *
  * @return list of all registered participants and their statuses.
  */
 @Operation(summary = "Returns all registered participants")
 @ApiResponse(responseCode = "200", description = "List of participants")
 @GetMapping
 public List<RegisteredParticipant> listAll() {
  return registrationService.listAll();
 }
}
