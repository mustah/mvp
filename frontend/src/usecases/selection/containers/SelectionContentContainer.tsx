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
import {clearErrorSelections, fetchSelections} from '../../../state/domain-models/selections/selectionsApiActions';
import {toggleParameterInSelection} from '../../../state/search/selection/selectionActions';
import {
  LookupState,
  OnSelectParameter,
  ParameterName,
  SelectionListItem,
} from '../../../state/search/selection/selectionModels';
import {
  getAddresses,
  getCities,
  getCitiesSelection,
  getGatewayStatuses,
  getMeterStatuses,
} from '../../../state/search/selection/selectionSelectors';
import {Callback, ClearError, ErrorResponse, IdNamed} from '../../../types/Types';
import {SearchResultList} from '../components/SelectionResultList';

interface StateToProps {
  cities: SelectionListItem[];
  addresses: SelectionListItem[];
  meterStatuses: SelectionListItem[];
  gatewayStatuses: SelectionListItem[];
  citiesSelection: ObjectsById<IdNamed>;
  isFetching: boolean;
  error: Maybe<ErrorResponse>;
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

  componentWillReceiveProps(props: Props) {
    props.fetchSelections();
  }

  render() {
    const {
      toggleSelection,
      cities,
      addresses,
      meterStatuses,
      gatewayStatuses,
      citiesSelection,
      isFetching,
      error,
      clearError,
    } = this.props;

    const selectCity = (selection: IdNamed) => toggleSelection({...selection, parameter: ParameterName.cities});
    const selectAddress = (selection: IdNamed) => toggleSelection({...selection, parameter: ParameterName.addresses});
    const selectMeterStatus = (selection: IdNamed) =>
      toggleSelection({...selection, parameter: ParameterName.meterStatuses});
    const selectGatewayStatus = (selection: IdNamed) =>
      toggleSelection({...selection, parameter: ParameterName.gatewayStatuses});

    const citySelectionText = translate('city') + ': ';
    const addressSelectionText = translate('address') + ': ';
    const meterStatusSelectionText = translate('meter status') + ': ';
    const gatewayStatusSelectionText = translate('gateway status') + ': ';

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
          </Row>
          <SearchResultList/>
        </Column>
      </Loader>
    );
  }
}

const mapStateToProps = ({searchParameters: {selection}, domainModels}: RootState): StateToProps => {
  const lookupState: LookupState = {
    domainModels,
    selection,
  };
  const {cities, addresses, alarms} = domainModels;

  return {
    cities: getCities(lookupState),
    citiesSelection: getCitiesSelection(lookupState).entities,
    addresses: getAddresses(lookupState),
    meterStatuses: getMeterStatuses(lookupState),
    gatewayStatuses: getGatewayStatuses(lookupState),
    isFetching: cities.isFetching || addresses.isFetching || alarms.isFetching,
    error: getError(cities),
  };
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  toggleSelection: toggleParameterInSelection,
  fetchSelections,
  clearError: clearErrorSelections,
}, dispatch);

export const SelectionContentContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(SelectionContent);
