import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../../reducers/rootReducer';
import {MeterDetails} from '../../../../state/domain-models/meter-details/meterDetailsModels';
import {addAllToReport} from '../../../../state/report/reportActions';
import {ReportSector} from '../../../../state/report/reportModels';
import {
  fetchMeasurementsForMeterDetails,
  measurementClearError
} from '../../../../state/ui/graph/measurement/measurementActions';
import {QuantityDisplayMode} from '../../../../state/ui/graph/measurement/measurementModels';
import {isSideMenuOpen} from '../../../../state/ui/uiSelectors';
import {fetchUserSelections} from '../../../../state/user-selection/userSelectionActions';
import {SelectionInterval} from '../../../../state/user-selection/userSelectionModels';
import {getUserSelectionId} from '../../../../state/user-selection/userSelectionSelectors';
import {Measurements} from '../../../report/components/Measurements';
import {DispatchToProps, StateToProps} from '../../../report/containers/MeasurementsContainer';
import {meterDetailExportToExcelSuccess} from '../meterDetailMeasurementActions';
import {getMeasurementParameters, hasMeasurementValues} from '../meterDetailMeasurementsSelectors';

export interface OwnProps {
  meter: MeterDetails;
  useCollectionPeriod?: boolean;
}

const hiddenLines = [];

const mapStateToProps = (rootState: RootState, ownProps: OwnProps): StateToProps => {
  const {
    domainModels: {userSelections, meterDetailMeasurement},
    meterDetail: {isTimePeriodDefault, timePeriod},
    collection,
    ui,
  } = rootState;
  const {useCollectionPeriod, meter} = ownProps;
  const period: SelectionInterval = useCollectionPeriod && isTimePeriodDefault ? collection.timePeriod : timePeriod;

  return {
    hasLegendItems: true,
    hasContent: hasMeasurementValues(meterDetailMeasurement.measurementResponse),
    hiddenLines,
    isFetching: meterDetailMeasurement.isFetching,
    isSideMenuOpen: isSideMenuOpen(ui),
    measurement: meterDetailMeasurement,
    parameters: '',
    requestParameters: {
      ...getMeasurementParameters({meter, timePeriod: period}),
      displayMode: QuantityDisplayMode.readout,
    },
    userSelectionId: getUserSelectionId(rootState.userSelection),
    userSelections,
  };
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  addAllToReport,
  clearError: measurementClearError(ReportSector.meterDetailsReport),
  exportToExcelSuccess: meterDetailExportToExcelSuccess,
  fetchMeasurements: fetchMeasurementsForMeterDetails,
  fetchUserSelections,
}, dispatch);

export const MeterMeasurementsContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(Measurements);
