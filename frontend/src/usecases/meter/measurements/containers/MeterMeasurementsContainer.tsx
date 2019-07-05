import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../../reducers/rootReducer';
import {MeterDetails} from '../../../../state/domain-models/meter-details/meterDetailsModels';
import {ReportSector} from '../../../../state/report/reportModels';
import {
  fetchMeasurementsForMeterDetails as fetchMeasurements,
  measurementClearError
} from '../../../../state/ui/graph/measurement/measurementActions';
import {Quantity, QuantityDisplayMode} from '../../../../state/ui/graph/measurement/measurementModels';
import {isSideMenuOpen} from '../../../../state/ui/uiSelectors';
import {fetchUserSelections} from '../../../../state/user-selection/userSelectionActions';
import {SelectionInterval} from '../../../../state/user-selection/userSelectionModels';
import {getUserSelectionId} from '../../../../state/user-selection/userSelectionSelectors';
import {uuid} from '../../../../types/Types';
import {Measurements} from '../../../report/components/Measurements';
import {DispatchToProps, StateToProps} from '../../../report/containers/MeasurementsContainer';
import {meterDetailExportToExcelSuccess as exportToExcelSuccess} from '../meterDetailMeasurementActions';
import {getMeasurementParameters, getMeterResolution, hasMeasurementValues} from '../meterDetailMeasurementsSelectors';

export interface OwnProps {
  meter: MeterDetails;
  useCollectionPeriod?: boolean;
}

const hiddenLines: uuid[] = [];
const selectedQuantities: Quantity[] = [];

const mapStateToProps = (rootState: RootState, ownProps: OwnProps): StateToProps => {
  const {
    domainModels: {userSelections, meterDetailMeasurement},
    meterDetail,
    collection,
    ui,
  } = rootState;
  const {useCollectionPeriod, meter} = ownProps;
  const timePeriod: SelectionInterval = useCollectionPeriod && !meterDetail.isDirty
    ? collection.timePeriod
    : meterDetail.timePeriod;

  const resolution = getMeterResolution({meterDetail, meter, period: timePeriod.period});

  return {
    hasLegendItems: true,
    hasContent: hasMeasurementValues(meterDetailMeasurement.measurementResponse),
    hiddenLines,
    isFetching: meterDetailMeasurement.isFetching,
    isSideMenuOpen: isSideMenuOpen(ui),
    measurement: meterDetailMeasurement,
    parameters: '',
    measurementParameters: {
      ...getMeasurementParameters({meter, resolution, timePeriod}),
      displayMode: QuantityDisplayMode.readout,
    },
    selectedQuantities,
    shouldFetchMeasurements: userSelections.isSuccessfullyFetched,
    userSelectionId: getUserSelectionId(rootState.userSelection),
  };
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  clearError: measurementClearError(ReportSector.meterDetailsReport),
  exportToExcelSuccess,
  fetchMeasurements,
  fetchUserSelections,
}, dispatch);

export const MeterMeasurementsContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(Measurements);
