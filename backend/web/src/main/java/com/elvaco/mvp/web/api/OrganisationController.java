package com.elvaco.mvp.web.api;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.domainmodels.Asset;
import com.elvaco.mvp.core.domainmodels.AssetType;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.Theme;
import com.elvaco.mvp.core.domainmodels.UserSelection;
import com.elvaco.mvp.core.exception.InvalidFormat;
import com.elvaco.mvp.core.spi.data.RequestParameter;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.usecase.OrganisationThemeUseCases;
import com.elvaco.mvp.core.usecase.OrganisationUseCases;
import com.elvaco.mvp.core.usecase.UserSelectionUseCases;
import com.elvaco.mvp.web.dto.OrganisationDto;
import com.elvaco.mvp.web.dto.PropertyDto;
import com.elvaco.mvp.web.dto.SubOrganisationRequestDto;
import com.elvaco.mvp.web.exception.OrganisationNotFound;
import com.elvaco.mvp.web.exception.UserSelectionNotFound;
import com.elvaco.mvp.web.mapper.OrganisationDtoMapper;

import lombok.AllArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import static com.elvaco.mvp.web.mapper.OrganisationDtoMapper.toDomainModel;
import static com.elvaco.mvp.web.mapper.OrganisationDtoMapper.toDto;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@AllArgsConstructor
@RestApi("/api/v1/organisations")
public class OrganisationController {

  private final OrganisationUseCases organisationUseCases;
  private final OrganisationThemeUseCases organisationThemeUseCases;
  private final UserSelectionUseCases selections;

  @GetMapping
  public List<OrganisationDto> allOrganisations() {
    return organisationUseCases.findAll().stream()
      .map(OrganisationDtoMapper::toDto)
      .collect(toList());
  }

  @GetMapping("/sub-organisations")
  public List<OrganisationDto> subOrganisations(
    @RequestParam MultiValueMap<String, String> requestParams
  ) {
    RequestParameters params = RequestParametersAdapter.of(requestParams);
    return organisationUseCases.findAllSubOrganisations(
      UUID.fromString(params.getFirst(RequestParameter.ORGANISATION))).stream()
      .map(OrganisationDtoMapper::toDto)
      .collect(toList());
  }

  @GetMapping("{id}")
  public OrganisationDto organisationById(@PathVariable UUID id) {
    return organisationUseCases.findById(id)
      .map(OrganisationDtoMapper::toDto)
      .orElseThrow(() -> new OrganisationNotFound(id));
  }

  @PostMapping
  public ResponseEntity<OrganisationDto> createOrganisation(
    @RequestBody OrganisationDto organisation
  ) {
    OrganisationDto responseModel = toDto(organisationUseCases.create(toDomainModel(organisation)));
    return ResponseEntity.status(HttpStatus.CREATED).body(responseModel);
  }

  @PutMapping
  public OrganisationDto updateOrganisation(@RequestBody OrganisationDto organisation) {
    return toDto(organisationUseCases.update(toDomainModel(organisation)));
  }

  @DeleteMapping("{id}")
  public OrganisationDto deleteOrganisation(@PathVariable UUID id) {
    Organisation organisation = organisationUseCases.findById(id)
      .orElseThrow(() -> new OrganisationNotFound(id));
    organisationUseCases.delete(organisation);
    return toDto(organisation);
  }

  @PostMapping("{id}/sub-organisations")
  public ResponseEntity<OrganisationDto> createSubOrganisation(
    @PathVariable UUID id,
    @RequestBody SubOrganisationRequestDto requestDto
  ) {
    UserSelection selection = selections.findByIdForCurrentUser(requestDto.selectionId)
      .orElseThrow(() -> new UserSelectionNotFound(requestDto.selectionId));

    return organisationUseCases.findById(id)
      .map(parent -> OrganisationDtoMapper.toDomainModel(parent, selection, requestDto))
      .map(organisationUseCases::create)
      .map(OrganisationDtoMapper::toDto)
      .map(subOrganisationDto -> ResponseEntity.status(HttpStatus.CREATED).body(subOrganisationDto))
      .orElseThrow(() -> new OrganisationNotFound(id));
  }

  @PreAuthorize("hasRole('SUPER_ADMIN')")
  @PutMapping("{id}/assets/{assetTypeOptional}")
  public void createAsset(
    @PathVariable UUID id,
    @PathVariable Optional<AssetType> assetTypeOptional,
    @RequestParam("asset") MultipartFile file
  ) throws IOException {
    var assetType = assetTypeOptional.orElseThrow(InvalidFormat::assetType);

    var organisation = organisationUseCases.findById(id)
      .orElseThrow(() -> new OrganisationNotFound(id));

    organisationThemeUseCases.createAsset(
      organisation,
      Asset.builder()
        .assetType(assetType)
        .content(file.getBytes())
        .contentType(file.getContentType())
        .build()
    );
  }

  @PreAuthorize("hasRole('SUPER_ADMIN')")
  @DeleteMapping("{id}/assets/{assetTypeOptional}")
  public void deleteAsset(
    @PathVariable UUID id,
    @PathVariable Optional<AssetType> assetTypeOptional
  ) {
    var assetType = assetTypeOptional.orElseThrow(InvalidFormat::assetType);

    var organisation = organisationUseCases.findById(id)
      .orElseThrow(() -> new OrganisationNotFound(id));

    organisationThemeUseCases.deleteAsset(organisation, assetType);
  }

  @GetMapping("{slug}/assets/{assetTypeOptional}")
  public ResponseEntity<byte[]> logotype(
    @PathVariable String slug,
    @PathVariable Optional<AssetType> assetTypeOptional,
    @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch
  ) {
    var assetType = assetTypeOptional.orElseThrow(InvalidFormat::assetType);

    var organisation = organisationUseCases.findBySlug(slug).orElse(null);

    return organisationThemeUseCases.findAssetOrFallback(
      organisation,
      assetType,
      ifNoneMatch
    )
      .map(asset -> ResponseEntity.ok()
        .cacheControl(CacheControl.noCache())
        .eTag(asset.checksum)
        .contentType(MediaType.valueOf(asset.contentType))
        .body(asset.content)
      )
      .orElseGet(() -> {
        var response = ResponseEntity.ok()
          .cacheControl(CacheControl.noCache());

        if (ifNoneMatch != null) {
          response.eTag(ifNoneMatch);
        }

        return response.build();
      });
  }

  @GetMapping("{slug}/theme")
  public List<PropertyDto> themeOfSlug(@PathVariable String slug) {
    return organisationUseCases.findBySlug(slug)
      .map(organisationThemeUseCases::findTheme)
      .map(theme -> theme.properties.entrySet().stream()
        .map(entry -> new PropertyDto(entry.getKey(), entry.getValue()))
        .collect(toList()))
      .orElse(emptyList());
  }

  @PreAuthorize("hasRole('SUPER_ADMIN')")
  @PutMapping("{id}/theme")
  public List<PropertyDto> putTheme(
    @PathVariable UUID id,
    @RequestBody List<PropertyDto> properties
  ) {
    var organisation = organisationUseCases.findById(id)
      .orElseThrow(() -> new OrganisationNotFound(id));

    organisationThemeUseCases.saveTheme(
      Theme.builder().organisationId(organisation.id)
        .properties(properties.stream().collect(toMap(p -> p.key, p -> p.value))).build()
    );

    return organisationThemeUseCases.findTheme(organisation).properties.entrySet().stream()
      .map(entry -> new PropertyDto(entry.getKey(), entry.getValue()))
      .collect(toList());
  }

  @PreAuthorize("hasRole('SUPER_ADMIN')")
  @DeleteMapping("{id}/theme")
  public void deleteTheme(@PathVariable UUID id) {
    var organisation = organisationUseCases.findById(id)
      .orElseThrow(() -> new OrganisationNotFound(id));

    organisationThemeUseCases.deleteTheme(organisation);
  }
}
