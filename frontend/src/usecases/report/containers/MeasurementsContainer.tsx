import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {
  exportToExcelSuccess,
  fetchMeasurements,
  measurementClearError} from '../../../state/ui/graph/measurement/measurementActions';
import {
  FetchMeasurements,
  MeasurementParameters,
  MeasurementState
} from '../../../state/ui/graph/measurement/measurementModels';
import {hasMeasurements} from '../../../state/ui/graph/measurement/measurementSelectors';
import {getMeterParameters, getUserSelectionId} from '../../../state/user-selection/userSelectionSelectors';
import {Callback, CallbackWith, EncodedUriParameters, OnClick, uuid} from '../../../types/Types';
import {MeasurementLineChart} from '../components/MeasurementLineChart';
import {Measurements} from '../components/Measurements';
import {addAllToReport} from '../reportActions';
import {LegendItem} from '../reportModels';
import {getHiddenLines, getMeasurementParameters, hasLegendItems} from '../reportSelectors';

export interface StateToProps {
  hiddenLines: uuid[];
  parameters: EncodedUriParameters;
  requestParameters: MeasurementParameters;
  measurement: MeasurementState;
  userSelectionId: uuid;
  hasMeters: boolean;
  hasContent: boolean;
}

export interface DispatchToProps {
  clearError: OnClick;
  fetchMeasurements: FetchMeasurements;
  addAllToReport: CallbackWith<LegendItem[]>;
  exportToExcelSuccess: Callback;
}

const mapStateToProps = (rootState: RootState): StateToProps => {
  const {report, measurement, userSelection: {userSelection}} = rootState;
  return ({
    hasMeters: hasLegendItems(report),
    hasContent: hasMeasurements(measurement.measurementResponse),
    hiddenLines: getHiddenLines(report),
    measurement,
    parameters: getMeterParameters({userSelection}),
    requestParameters: getMeasurementParameters(rootState),
    userSelectionId: getUserSelectionId(rootState.userSelection),
  });
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  clearError: measurementClearError,
  fetchMeasurements,
  addAllToReport,
  exportToExcelSuccess,
}, dispatch);

export const GraphContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(MeasurementLineChart);

export const MeasurementsContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(Measurements);
