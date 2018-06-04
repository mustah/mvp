import * as React from 'react';
import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import 'SelectionContentContainer.scss';
import {MultiDropdownSelector} from '../../../components/dropdown-selector/MultiDropdownSelector';
import {SimpleDropdownSelector} from '../../../components/dropdown-selector/SimpleDropdownSelector';
import {Column} from '../../../components/layouts/column/Column';
import {Row} from '../../../components/layouts/row/Row';
import {Loader} from '../../../components/loading/Loader';
import {Subtitle} from '../../../components/texts/Titles';
import {Maybe} from '../../../helpers/Maybe';
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {ObjectsById} from '../../../state/domain-models/domainModels';
import {getError} from '../../../state/domain-models/domainModelsSelectors';
import {
  clearErrorSelections,
  fetchSelections,
} from '../../../state/domain-models/selections/selectionsApiActions';
import {toggleParameterInSelection} from '../../../state/user-selection/userSelectionActions';
import {
  LookupState,
  OnSelectParameter,
  ParameterName,
  SelectionListItem,
} from '../../../state/user-selection/userSelectionModels';
import {
  getAddresses,
  getCities,
  getCitiesSelection, getFacilities, getGatewaySerials,
  getGatewayStatuses,
  getMedia,
  getMeterStatuses, getSecondaryAddresses,
} from '../../../state/user-selection/userSelectionSelectors';
import {Callback, ClearError, ErrorResponse, IdNamed} from '../../../types/Types';
import {SearchResultList} from '../components/SelectionResultList';

interface StateToProps {
  addresses: SelectionListItem[];
  cities: SelectionListItem[];
  citiesSelection: ObjectsById<IdNamed>;
  error: Maybe<ErrorResponse>;
  gatewayStatuses: SelectionListItem[];
  isFetching: boolean;
  media: SelectionListItem[];
  meterStatuses: SelectionListItem[];
  secondaryAddresses: SelectionListItem[];
  facilities: SelectionListItem[];
  gatewaySerials: SelectionListItem[];
}

interface DispatchToProps {
  toggleSelection: OnSelectParameter;
  fetchSelections: Callback;
  clearError: ClearError;
}

type Props = StateToProps & DispatchToProps;

class SelectionContent extends React.Component<Props> {

  componentDidMount() {
    this.props.fetchSelections();
  }

  componentWillReceiveProps({fetchSelections}: Props) {
    fetchSelections();
  }

  render() {
    const {
      addresses,
      cities,
      citiesSelection,
      clearError,
      error,
      gatewayStatuses,
      isFetching,
      media,
      meterStatuses,
      toggleSelection,
      secondaryAddresses,
      facilities,
      gatewaySerials,
    } = this.props;

    const selectCity = (selection: IdNamed) =>
      toggleSelection({...selection, parameter: ParameterName.cities});
    const selectAddress = (selection: IdNamed) =>
      toggleSelection({...selection, parameter: ParameterName.addresses});
    const selectMeterStatus = (selection: IdNamed) =>
      toggleSelection({...selection, parameter: ParameterName.meterStatuses});
    const selectGatewayStatus = (selection: IdNamed) =>
      toggleSelection({...selection, parameter: ParameterName.gatewayStatuses});
    const selectMedia = (selection: IdNamed) =>
      toggleSelection({...selection, parameter: ParameterName.media});
    const selectSecondaryAddresses = (selection: IdNamed) =>
      toggleSelection({...selection, parameter: ParameterName.secondaryAddresses});
    const selectFacilities = (selection: IdNamed) =>
      toggleSelection({...selection, parameter: ParameterName.facilities});
    const selectGatewaySerials = (selection: IdNamed) =>
      toggleSelection({...selection, parameter: ParameterName.gatewaySerials});

    const citySelectionText = translate('city') + ': ';
    const addressSelectionText = translate('address') + ': ';
    const meterStatusSelectionText = translate('meter status') + ': ';
    const gatewayStatusSelectionText = translate('gateway status') + ': ';
    const mediumStatusSelectionText = translate('medium') + ': ';
    const facilitySelectionText = translate('facility') + ': ';
    const secondaryAddressSelectionText = translate('secondary address') + ': ';
    const gatewaySerialSelectionText = translate('gateway serial') + ': ';

    return (
      <Loader isFetching={isFetching} error={error} clearError={clearError}>
        <Column className="SelectionContentBox">
          <Subtitle>{translate('filter')}</Subtitle>

          <Row className="SelectionDropdownOptions">
            <SimpleDropdownSelector
              list={cities}
              selectionText={citySelectionText}
              select={selectCity}
            />
            <MultiDropdownSelector
              list={addresses}
              selectionText={addressSelectionText}
              select={selectAddress}
              parentSelectionLookup={citiesSelection}
              parentIdentifier="parentId"
            />
            <SimpleDropdownSelector
              list={meterStatuses}
              selectionText={meterStatusSelectionText}
              select={selectMeterStatus}
            />
            <SimpleDropdownSelector
              list={gatewayStatuses}
              selectionText={gatewayStatusSelectionText}
              select={selectGatewayStatus}
            />
            <SimpleDropdownSelector
              list={media}
              selectionText={mediumStatusSelectionText}
              select={selectMedia}
            />
            <SimpleDropdownSelector
              list={facilities}
              selectionText={facilitySelectionText}
              select={selectFacilities}
            />
            <SimpleDropdownSelector
              list={secondaryAddresses}
              selectionText={secondaryAddressSelectionText}
              select={selectSecondaryAddresses}
            />
            <SimpleDropdownSelector
              list={gatewaySerials}
              selectionText={gatewaySerialSelectionText}
              select={selectGatewaySerials}
            />
          </Row>
          <SearchResultList/>
        </Column>
      </Loader>
    );
  }
}

const mapStateToProps = ({userSelection, domainModels}: RootState): StateToProps => {
  const lookupState: LookupState = {
    domainModels,
    userSelection,
  };
  const {cities, addresses, alarms, secondaryAddresses, facilities} = domainModels;

  return {
    addresses: getAddresses(lookupState),
    cities: getCities(lookupState),
    citiesSelection: getCitiesSelection(lookupState).entities,
    error: getError(cities),
    gatewayStatuses: getGatewayStatuses(lookupState),
    isFetching: [cities, addresses, alarms, secondaryAddresses, facilities]
      .some((criteria) => criteria.isFetching),
    media: getMedia(lookupState),
    meterStatuses: getMeterStatuses(lookupState),
    secondaryAddresses: getSecondaryAddresses(lookupState),
    facilities: getFacilities(lookupState),
    gatewaySerials: getGatewaySerials(lookupState),
  };
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  toggleSelection: toggleParameterInSelection,
  fetchSelections,
  clearError: clearErrorSelections,
}, dispatch);

export const SelectionContentContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(SelectionContent);
