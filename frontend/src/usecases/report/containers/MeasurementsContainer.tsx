import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {fetchSelectionTree} from '../../../state/selection-tree/selectionTreeApiActions';
import {SelectionTreeState} from '../../../state/selection-tree/selectionTreeModels';
import {
  exportToExcelSuccess,
  fetchMeasurements,
  measurementClearError,
  MeasurementParameters
} from '../../../state/ui/graph/measurement/measurementActions';
import {FetchMeasurements, MeasurementState} from '../../../state/ui/graph/measurement/measurementModels';
import {getMeterParameters, getUserSelectionId} from '../../../state/user-selection/userSelectionSelectors';
import {Callback, CallbackWithIds, EncodedUriParameters, Fetch, OnClick, uuid} from '../../../types/Types';
import {Graph} from '../components/graph/Graph';
import {Measurements} from '../components/Measurements';
import {showMetersInGraph} from '../reportActions';
import {getMeasurementParameters} from '../reportSelectors';

export interface StateToProps {
  hiddenLines: uuid[];
  parameters: EncodedUriParameters;
  requestParameters: MeasurementParameters;
  selectionTree: SelectionTreeState;
  measurement: MeasurementState;
  userSelectionId: uuid;
}

export interface DispatchToProps {
  clearError: OnClick;
  fetchMeasurements: FetchMeasurements;
  fetchSelectionTree: Fetch;
  showMetersInGraph: CallbackWithIds;
  exportToExcelSuccess: Callback;
}

const mapStateToProps = (rootState: RootState): StateToProps => {
  const {report: {hiddenLines}, measurement, selectionTree, userSelection: {userSelection}} = rootState;
  return ({
    hiddenLines,
    measurement,
    parameters: getMeterParameters({userSelection}),
    requestParameters: getMeasurementParameters(rootState),
    selectionTree,
    userSelectionId: getUserSelectionId(rootState.userSelection),
  });
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  clearError: measurementClearError,
  fetchMeasurements,
  fetchSelectionTree,
  showMetersInGraph,
  exportToExcelSuccess,
}, dispatch);

export const GraphContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(Graph);

// TODO inject "export" into redux-state
export const MeasurementsContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(Measurements);
