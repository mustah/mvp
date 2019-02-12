import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {TemporalResolution} from '../../../components/dates/dateModels';
import {RootState} from '../../../reducers/rootReducer';
import {exportToExcel} from '../../../state/ui/graph/measurement/measurementActions';
import {changeToolbarView} from '../../../state/ui/toolbar/toolbarActions';
import {OnChangeToolbarView, ToolbarView} from '../../../state/ui/toolbar/toolbarModels';
import {OnSelectResolution} from '../../../state/user-selection/userSelectionModels';
import {Callback, OnClick} from '../../../types/Types';
import {Toolbar} from '../components/Toolbar';
import {selectResolution} from '../reportActions';

interface StateToProps {
  hasMeasurements: boolean;
  resolution: TemporalResolution;
  view: ToolbarView;
  isFetching: boolean;
  isExportingToExcel: boolean;
}

interface DispatchToProps {
  changeToolbarView: OnChangeToolbarView;
  selectResolution: OnSelectResolution;
  exportToExcel: Callback;
}

interface OwnProps {
  toggleLegend?: OnClick;
}

export type Props = StateToProps & DispatchToProps & OwnProps;

const mapStateToProps = ({
  report: {resolution},
  measurement: {measurementResponse: {measurements}, isFetching, isExportingToExcel},
  ui: {toolbar: {measurement: {view}}}
}: RootState): StateToProps =>
  ({resolution, view, hasMeasurements: measurements.length > 0, isFetching, isExportingToExcel});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  changeToolbarView,
  selectResolution,
  exportToExcel,
}, dispatch);

export const ToolbarContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(Toolbar);
