import * as React from 'react';
import {
  ListingDropdownSelector,
  renderAddressLabel,
  renderCityLabel,
  SearchableDropdownSelector,
  SearchableProps
} from '../../../components/dropdown-selector/DropdownSelector';
import {connectedSuperAdminOnly} from '../../../components/hoc/withRoles';
import {Column} from '../../../components/layouts/column/Column';
import {Foldable} from '../../../components/layouts/foldable/Foldable';
import {Row} from '../../../components/layouts/row/Row';
import {MeterListContainer} from '../../../containers/MeterListContainer';
import {isDefined} from '../../../helpers/commonHelpers';
import {firstUpperTranslated, translate} from '../../../services/translationService';
import {Address, City} from '../../../state/domain-models/location/locationModels';
import {
  fetchAddresses,
  fetchAlarms,
  fetchCities,
  fetchFacilities,
  fetchGatewaySerials,
  fetchMedia,
  fetchOrganisationsToSelect,
  fetchReported,
  fetchSecondaryAddresses,
  mapSelectedIdToAddress,
  mapSelectedIdToCity
} from '../../../state/domain-models/selections/selectionsApiActions';
import {ParameterName, SelectionListItem} from '../../../state/user-selection/userSelectionModels';
import {SelectionContentProps} from '../containers/SelectionContentContainer';
import './SelectionContent.scss';
import {Thresholds} from './thresholds/Thresholds';

const unknownCity: City = mapSelectedIdToCity('unknown,unknown');
const unknownAddress: Address = mapSelectedIdToAddress('unknown,unknown,unknown');

const OrganisationDropDown = connectedSuperAdminOnly<SearchableProps>(SearchableDropdownSelector);

export const SelectionContent = ({
  addresses,
  alarms,
  cities,
  facilities,
  gatewaySerials,
  media,
  organisations,
  onChangeThreshold,
  reported,
  secondaryAddresses,
  threshold,
  toggleParameter,
  userSelectionId,
}: SelectionContentProps) => {

  const selectCity = (item: SelectionListItem) =>
    toggleParameter({item, parameter: ParameterName.cities});
  const selectAddress = (item: SelectionListItem) =>
    toggleParameter({item, parameter: ParameterName.addresses});
  const selectReported = (item: SelectionListItem) =>
    toggleParameter({item, parameter: ParameterName.reported});
  const selectAlarm = (item: SelectionListItem) =>
    toggleParameter({item, parameter: ParameterName.alarms});
  const selectMedium = (item: SelectionListItem) =>
    toggleParameter({item, parameter: ParameterName.media});
  const selectSecondaryAddresses = (item: SelectionListItem) =>
    toggleParameter({item, parameter: ParameterName.secondaryAddresses});
  const selectFacilities = (item: SelectionListItem) =>
    toggleParameter({item, parameter: ParameterName.facilities});
  const selectOrganisations = (item: SelectionListItem) =>
    toggleParameter({item, parameter: ParameterName.organisations});
  const selectGatewaySerials = (item: SelectionListItem) =>
    toggleParameter({item, parameter: ParameterName.gatewaySerials});

  // TODO[!must!] this cannot be inlined until React is updated. This is the workaround for Typescript 3.0.1
  const organisationDropdownProps: SearchableProps = {
    fetchItems: fetchOrganisationsToSelect,
    selectedItems: organisations,
    selectionText: `${translate('organisation')}: `,
    select: selectOrganisations,
  };

  return (
    <Column className="SelectionContentBox">

      <Foldable title={translate('filter')}>
        <Row className="SelectionDropdownOptions">
          <SearchableDropdownSelector
            fetchItems={fetchFacilities}
            selectedItems={facilities}
            selectionText={`${translate('facility')}: `}
            select={selectFacilities}
          />
          <OrganisationDropDown {...organisationDropdownProps}/>
          <SearchableDropdownSelector
            fetchItems={fetchCities}
            selectedItems={cities}
            selectionText={`${translate('city')}: `}
            select={selectCity}
            renderLabel={renderCityLabel}
            rowHeight={44}
            unknownItem={unknownCity as SelectionListItem}
          />
          <SearchableDropdownSelector
            fetchItems={fetchAddresses}
            selectedItems={addresses}
            selectionText={`${translate('address')}: `}
            select={selectAddress}
            renderLabel={renderAddressLabel}
            rowHeight={44}
            unknownItem={unknownAddress as SelectionListItem}
          />
          <ListingDropdownSelector
            fetchItems={fetchMedia}
            selectedItems={media}
            selectionText={`${translate('medium')}: `}
            select={selectMedium}
          />
        </Row>

        <Row className="SelectionDropdownOptions">
          <ListingDropdownSelector
            fetchItems={fetchReported}
            selectedItems={reported}
            selectionText={`${translate('reported')}: `}
            select={selectReported}
          />
          <ListingDropdownSelector
            fetchItems={fetchAlarms}
            selectedItems={alarms}
            selectionText={`${translate('alarm')}: `}
            select={selectAlarm}
          />
          <SearchableDropdownSelector
            fetchItems={fetchSecondaryAddresses}
            selectedItems={secondaryAddresses}
            selectionText={`${firstUpperTranslated('meter id')}: `}
            select={selectSecondaryAddresses}
          />
          <SearchableDropdownSelector
            fetchItems={fetchGatewaySerials}
            selectedItems={gatewaySerials}
            selectionText={`${translate('gateway serial')}: `}
            select={selectGatewaySerials}
          />
        </Row>
      </Foldable>

      <Foldable
        title={translate('threshold', {count: 2})}
        containerClassName="Thresholds"
        isVisible={isDefined(threshold)}
      >
        <Thresholds
          key={`thresholds-${userSelectionId}`}
          onChange={onChangeThreshold}
          query={threshold}
          className="Thresholds-container"
        />
      </Foldable>

      <MeterListContainer componentId="selectionMeterList"/>
    </Column>
  );
};
