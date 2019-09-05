import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {TemporalResolution} from '../../../components/dates/dateModels';
import {RootState} from '../../../reducers/rootReducer';
import {
  clearReport,
  selectResolution,
  setReportTimePeriod,
  toggleComparePeriod,
  toggleShowAverage
} from '../../../state/report/reportActions';
import {ReportSector, ResolutionAware} from '../../../state/report/reportModels';
import {getMeterLegendItems, hasLegendItems} from '../../../state/report/reportSelectors';
import {exportReportToExcel} from '../../../state/ui/graph/measurement/measurementActions';
import {changeToolbarView} from '../../../state/ui/toolbar/toolbarActions';
import {OnChangeToolbarView, ToolbarViewSettingsProps} from '../../../state/ui/toolbar/toolbarModels';
import {SelectionInterval} from '../../../state/user-selection/userSelectionModels';
import {Callback, CallbackWith, OnClick} from '../../../types/Types';
import {ReportToolbar} from '../components/ReportToolbar';

interface StateToProps extends ResolutionAware, ToolbarViewSettingsProps {
  canClearReport?: boolean;
  canShowAverage: boolean;
  hasLegendItems: boolean;
  hasMeasurements: boolean;
  isFetching: boolean;
  isExportingToExcel: boolean;
  timePeriod: SelectionInterval;
  shouldComparePeriod: boolean;
  shouldShowAverage: boolean;
}

interface DispatchToProps {
  changeToolbarView: OnChangeToolbarView;
  clearReport: Callback;
  exportToExcel: Callback;
  selectResolution: CallbackWith<TemporalResolution>;
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
    canClearReport: true,
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
  changeToolbarView: changeToolbarView(ReportSector.report),
  clearReport,
  exportToExcel: exportReportToExcel,
  selectResolution: selectResolution(ReportSector.report),
  setReportTimePeriod: setReportTimePeriod(ReportSector.report),
  toggleComparePeriod: toggleComparePeriod(ReportSector.report),
  toggleShowAverage: toggleShowAverage(ReportSector.report),
}, dispatch);

export const ReportToolbarContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(ReportToolbar);
