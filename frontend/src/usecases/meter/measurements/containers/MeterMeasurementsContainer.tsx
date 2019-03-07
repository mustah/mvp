import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../../reducers/rootReducer';
import {MeterDetails} from '../../../../state/domain-models/meter-details/meterDetailsModels';
import {
  exportToExcelSuccess, fetchMeasurementsForMeterDetails,
  measurementClearError
} from '../../../../state/ui/graph/measurement/measurementActions';
import {fetchUserSelections} from '../../../../state/user-selection/userSelectionActions';
import {SelectionInterval} from '../../../../state/user-selection/userSelectionModels';
import {
  getUserSelectionId
} from '../../../../state/user-selection/userSelectionSelectors';
import {Measurements} from '../../../report/components/Measurements';
import {DispatchToProps, StateToProps} from '../../../report/containers/MeasurementsContainer';
import {addAllToReport} from '../../../report/reportActions';
import {getMeasurementParameters, hasMeasurementValues} from '../meterMeasurementsSelectors';

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
  } = rootState;
  const {useCollectionPeriod, meter} = ownProps;
  const period: SelectionInterval = useCollectionPeriod && isTimePeriodDefault ? collection.timePeriod : timePeriod;

  return ({
    hasLegendItems: true,
    hasContent: hasMeasurementValues(meterDetailMeasurement.measurementResponse),
    hiddenLines,
    measurement: meterDetailMeasurement,
    parameters: '',
    requestParameters: getMeasurementParameters({meter, timePeriod: period}),
    userSelectionId: getUserSelectionId(rootState.userSelection),
    userSelections,
  });
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  clearError: measurementClearError,
  fetchMeasurements: fetchMeasurementsForMeterDetails,
  fetchUserSelections,
  addAllToReport,
  exportToExcelSuccess,
}, dispatch);

export const MeterMeasurementsContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(Measurements);
