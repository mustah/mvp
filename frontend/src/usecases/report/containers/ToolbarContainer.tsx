import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {TemporalResolution} from '../../../components/dates/dateModels';
import {RootState} from '../../../reducers/rootReducer';
import {exportToExcel} from '../../../state/ui/graph/measurement/measurementActions';
import {changeToolbarView} from '../../../state/ui/toolbar/toolbarActions';
import {OnChangeToolbarView, ToolbarView} from '../../../state/ui/toolbar/toolbarModels';
import {OnSelectResolution, SelectionInterval} from '../../../state/user-selection/userSelectionModels';
import {Callback, CallbackWith, OnClick} from '../../../types/Types';
import {Toolbar} from '../components/Toolbar';
import {selectResolution, setReportTimePeriod} from '../reportActions';
import {getLegendItems} from '../reportSelectors';

interface StateToProps {
  hasLegendItems: boolean;
  hasMeasurements: boolean;
  resolution: TemporalResolution;
  view: ToolbarView;
  isFetching: boolean;
  isExportingToExcel: boolean;
  timePeriod: SelectionInterval;
}

interface DispatchToProps {
  changeToolbarView: OnChangeToolbarView;
  selectResolution: OnSelectResolution;
  exportToExcel: Callback;
  setReportTimePeriod: CallbackWith<SelectionInterval>;
}

interface OwnProps {
  showHideLegend: OnClick;
}

export type Props = StateToProps & DispatchToProps & OwnProps;

const mapStateToProps = ({
  report,
  measurement: {measurementResponse: {measurements}, isFetching, isExportingToExcel},
  ui: {toolbar: {measurement: {view}}}
}: RootState): StateToProps =>
  ({
    hasLegendItems: getLegendItems(report).length > 0,
    hasMeasurements: measurements.length > 0,
    isFetching,
    isExportingToExcel,
    resolution: report.resolution,
    view,
    timePeriod: report.timePeriod,
  });

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  changeToolbarView,
  selectResolution,
  exportToExcel,
  setReportTimePeriod,
}, dispatch);

export const ToolbarContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(Toolbar);
