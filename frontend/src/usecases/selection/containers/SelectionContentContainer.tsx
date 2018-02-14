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
import {RootState} from '../../../reducers/rootReducer';
import {translate} from '../../../services/translationService';
import {ObjectsById} from '../../../state/domain-models/domainModels';
import {fetchSelections} from '../../../state/domain-models/domainModelsActions';
import {toggleSelection} from '../../../state/search/selection/selectionActions';
import {
  LookupState,
  OnSelectParameter,
  ParameterName,
  SelectionListItem,
} from '../../../state/search/selection/selectionModels';
import {
  getAddresses,
  getAlarms,
  getCities,
  getCitiesSelection,
  getGatewayStatuses,
  getManufacturers,
  getMeterStatuses,
  getProductModels,
} from '../../../state/search/selection/selectionSelectors';
import {Callback, IdNamed} from '../../../types/Types';
import {SelectionQuantity} from '../components/SelectionQuantity';
import {SearchResultList} from '../components/SelectionResultList';

interface StateToProps {
  cities: SelectionListItem[];
  addresses: SelectionListItem[];
  alarms: SelectionListItem[];
  manufacturers: SelectionListItem[];
  productModels: SelectionListItem[];
  meterStatuses: SelectionListItem[];
  gatewayStatuses: SelectionListItem[];
  citiesSelection: ObjectsById<IdNamed>;
  isFetching: boolean;
}

interface DispatchToProps {
  toggleSelection: OnSelectParameter;
  fetchSelections: Callback;
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
      alarms,
      manufacturers,
      productModels,
      meterStatuses,
      gatewayStatuses,
      citiesSelection,
      isFetching,
    } = this.props;

    const selectCity = (selection: IdNamed) => toggleSelection({...selection, parameter: ParameterName.cities});
    const selectAddress = (selection: IdNamed) => toggleSelection({...selection, parameter: ParameterName.addresses});
    const selectAlarm = (selection: IdNamed) => toggleSelection({...selection, parameter: ParameterName.alarms});
    const selectManufacturer = (selection: IdNamed) =>
      toggleSelection({...selection, parameter: ParameterName.manufacturers});
    const selectProductModel = (selection: IdNamed) =>
      toggleSelection({...selection, parameter: ParameterName.productModels});
    const selectMeterStatus = (selection: IdNamed) =>
      toggleSelection({...selection, parameter: ParameterName.meterStatuses});
    const selectGatewayStatus = (selection: IdNamed) =>
      toggleSelection({...selection, parameter: ParameterName.gatewayStatuses});

    const citySelectionText = translate('city') + ': ';
    const addressSelectionText = translate('address') + ': ';
    const alarmSelectionText = translate('alarm') + ': ';
    const manufacturerSelectionText = translate('manufacturer') + ': ';
    const productModelSelectionText = translate('product model') + ': ';
    const meterStatusSelectionText = translate('meter status') + ': ';
    const gatewayStatusSelectionText = translate('gateway status') + ': ';

    return (

      <Loader isFetching={isFetching}>
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
              parentIdentifier="cityId"
            />
            <SimpleDropdownSelector
              list={productModels}
              selectionText={productModelSelectionText}
              select={selectProductModel}
            />
            <SimpleDropdownSelector
              list={gatewayStatuses}
              selectionText={gatewayStatusSelectionText}
              select={selectGatewayStatus}
            />
            <SimpleDropdownSelector
              list={manufacturers}
              selectionText={manufacturerSelectionText}
              select={selectManufacturer}
            />
            <SimpleDropdownSelector
              list={meterStatuses}
              selectionText={meterStatusSelectionText}
              select={selectMeterStatus}
            />
            <SimpleDropdownSelector
              list={alarms}
              selectionText={alarmSelectionText}
              select={selectAlarm}
            />
            <SelectionQuantity/>
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
    alarms: getAlarms(lookupState),
    manufacturers: getManufacturers(lookupState),
    productModels: getProductModels(lookupState),
    meterStatuses: getMeterStatuses(lookupState),
    gatewayStatuses: getGatewayStatuses(lookupState),
    isFetching: cities.isFetching || addresses.isFetching || alarms.isFetching,
  };
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  toggleSelection,
  fetchSelections,
}, dispatch);

export const SelectionContentContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(SelectionContent);
