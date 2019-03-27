import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {TemporalResolution} from '../../../components/dates/dateModels';
import {RootState} from '../../../reducers/rootReducer';
import {exportToExcel} from '../../../state/ui/graph/measurement/measurementActions';
import {changeToolbarView} from '../../../state/ui/toolbar/toolbarActions';
import {OnChangeToolbarView, ToolbarView} from '../../../state/ui/toolbar/toolbarModels';
import {SelectionInterval} from '../../../state/user-selection/userSelectionModels';
import {Callback, CallbackWith, OnClick} from '../../../types/Types';
import {ReportToolbar} from '../components/ReportToolbar';
import {selectResolution, setReportTimePeriod, toggleComparePeriod, toggleShowAverage} from '../reportActions';
import {getMeterLegendItems, hasLegendItems} from '../reportSelectors';

interface StateToProps {
  canShowAverage: boolean;
  hasLegendItems: boolean;
  hasMeasurements: boolean;
  resolution: TemporalResolution;
  view: ToolbarView;
  isFetching: boolean;
  isExportingToExcel: boolean;
  timePeriod: SelectionInterval;
  shouldComparePeriod: boolean;
  shouldShowAverage: boolean;
}

interface DispatchToProps {
  changeToolbarView: OnChangeToolbarView;
  selectResolution: CallbackWith<TemporalResolution>;
  exportToExcel: Callback;
  setReportTimePeriod: CallbackWith<SelectionInterval>;
  toggleComparePeriod: Callback;
  toggleShowAverage: Callback;
}

interface OwnProps {
  showHideLegend: OnClick;
}

export type Props = StateToProps & DispatchToProps & OwnProps;

const mapStateToProps = ({
  report: {savedReports, temporal: {resolution, timePeriod, shouldComparePeriod}},
  measurement: {measurementResponse: {measurements, compare}, isFetching, isExportingToExcel},
  ui: {toolbar: {measurement: {view}}}
}: RootState): StateToProps =>
  ({
    canShowAverage: getMeterLegendItems(savedReports).length > 1,
    hasLegendItems: hasLegendItems(savedReports),
    hasMeasurements: measurements.length > 0 || compare.length > 0,
    isFetching,
    isExportingToExcel,
    resolution,
    timePeriod,
    shouldComparePeriod,
    shouldShowAverage: savedReports.meterPage.shouldShowAverage,
    view,
  });

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  changeToolbarView,
  exportToExcel,
  selectResolution,
  setReportTimePeriod,
  toggleComparePeriod,
  toggleShowAverage,
}, dispatch);

export const ToolbarContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(ReportToolbar);
