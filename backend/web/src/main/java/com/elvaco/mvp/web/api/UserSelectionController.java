package com.elvaco.mvp.web.api;

import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.UserSelection;
import com.elvaco.mvp.core.usecase.UserSelectionUseCases;
import com.elvaco.mvp.web.dto.UserSelectionDto;
import com.elvaco.mvp.web.exception.UserSelectionNotFound;
import com.elvaco.mvp.web.mapper.UserSelectionDtoMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import static com.elvaco.mvp.web.mapper.UserSelectionDtoMapper.toDomainModel;
import static com.elvaco.mvp.web.mapper.UserSelectionDtoMapper.toDto;
import static java.util.stream.Collectors.toList;

@AllArgsConstructor
@RestApi("/api/v1/user/selections")
public class UserSelectionController {

  private final UserSelectionUseCases useCases;

  @GetMapping
  public List<UserSelectionDto> getAllUserSelections() {
    return useCases.findAllForCurrentUser()
      .stream()
      .map(UserSelectionDtoMapper::toDto)
      .collect(toList());
  }

  @GetMapping("{id}")
  public UserSelectionDto getUserSelectionsById(@PathVariable UUID id) {
    return useCases.findByIdForCurrentUser(id)
      .map(UserSelectionDtoMapper::toDto)
      .orElseThrow(() -> new UserSelectionNotFound(id));
  }

  @PostMapping
  public ResponseEntity<UserSelectionDto> saveSelection(
    @RequestBody UserSelectionDto userSelectionDto
  ) {
    return ResponseEntity
      .status(HttpStatus.CREATED)
      .body(toDto(useCases.save(toDomainModel(userSelectionDto))));
  }

  @PutMapping
  public UserSelectionDto updateSelection(@RequestBody UserSelectionDto userSelectionDto) {
    //Id may belong to another user, do not leak information.
    useCases.findByIdForCurrentUser(userSelectionDto.id)
      .orElseThrow(() -> new UserSelectionNotFound(userSelectionDto.id));

    return toDto(useCases.save(toDomainModel(userSelectionDto)));
  }

  @DeleteMapping("{id}")
  public UserSelectionDto deleteSelection(@PathVariable UUID id) {
    UserSelection userSelection = useCases.findByIdForCurrentUser(id)
      .orElseThrow(() -> new UserSelectionNotFound(id));

    useCases.delete(userSelection);

    return toDto(userSelection);
  }
}
