import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {NormalizedState} from '../../../state/domain-models/domainModels';
import {
  exportToExcelSuccess,
  fetchMeasurements,
  measurementClearError
} from '../../../state/ui/graph/measurement/measurementActions';
import {
  FetchMeasurements,
  MeasurementParameters,
  MeasurementState
} from '../../../state/ui/graph/measurement/measurementModels';
import {hasMeasurementValues} from '../../../state/ui/graph/measurement/measurementSelectors';
import {fetchUserSelections} from '../../../state/user-selection/userSelectionActions';
import {UserSelection} from '../../../state/user-selection/userSelectionModels';
import {getMeterParameters, getUserSelectionId} from '../../../state/user-selection/userSelectionSelectors';
import {Callback, CallbackWith, EncodedUriParameters, Fetch, OnClick, uuid} from '../../../types/Types';
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
  userSelections: NormalizedState<UserSelection>;
  userSelectionId: uuid;
  hasLegendItems: boolean;
  hasContent: boolean;
}

export interface DispatchToProps {
  clearError: OnClick;
  fetchMeasurements: FetchMeasurements;
  fetchUserSelections: Fetch;
  addAllToReport: CallbackWith<LegendItem[]>;
  exportToExcelSuccess: Callback;
}

const mapStateToProps = (rootState: RootState): StateToProps => {
  const {
    domainModels: {userSelections},
    report: {savedReports},
    measurement,
    userSelection: {userSelection},
  } = rootState;
  return ({
    hasLegendItems: hasLegendItems(savedReports),
    hasContent: hasMeasurementValues(measurement.measurementResponse),
    hiddenLines: getHiddenLines(savedReports),
    measurement,
    parameters: getMeterParameters({userSelection}),
    requestParameters: getMeasurementParameters(rootState),
    userSelectionId: getUserSelectionId(rootState.userSelection),
    userSelections,
  });
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  clearError: measurementClearError,
  fetchMeasurements,
  fetchUserSelections,
  addAllToReport,
  exportToExcelSuccess,
}, dispatch);

export const GraphContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(MeasurementLineChart);

export const MeasurementsContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(Measurements);
