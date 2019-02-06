import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../reducers/rootReducer';
import {fetchSelectionTree} from '../../../state/selection-tree/selectionTreeApiActions';
import {SelectionTreeState} from '../../../state/selection-tree/selectionTreeModels';
import {
  fetchMeasurements,
  measurementClearError,
  MeasurementParameters
} from '../../../state/ui/graph/measurement/measurementActions';
import {FetchMeasurements, MeasurementState} from '../../../state/ui/graph/measurement/measurementModels';
import {getMeterParameters} from '../../../state/user-selection/userSelectionSelectors';
import {CallbackWithIds, EncodedUriParameters, Fetch, OnClick, uuid} from '../../../types/Types';
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
}

export interface DispatchToProps {
  clearError: OnClick;
  fetchMeasurements: FetchMeasurements;
  fetchSelectionTree: Fetch;
  showMetersInGraph: CallbackWithIds;
}

const mapStateToProps = (rootState: RootState): StateToProps => {
  const {report: {hiddenLines}, measurement, selectionTree, userSelection: {userSelection}} = rootState;
  return ({
    hiddenLines,
    measurement,
    parameters: getMeterParameters({userSelection}),
    requestParameters: getMeasurementParameters(rootState),
    selectionTree,
  });
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  clearError: measurementClearError,
  fetchMeasurements,
  fetchSelectionTree,
  showMetersInGraph,
}, dispatch);

export const GraphContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(Graph);

export const MeasurementsContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(Measurements);
