import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {TemporalResolution} from '../../../components/dates/dateModels';
import {RootState} from '../../../reducers/rootReducer';
import {
  selectResolution,
  setReportTimePeriod,
  toggleComparePeriod,
  toggleShowAverage
} from '../../../state/report/reportActions';
import {ReportSector, ResolutionAware} from '../../../state/report/reportModels';
import {getMeterLegendItems, hasLegendItems} from '../../../state/report/reportSelectors';
import {exportSelectionReportToExcel} from '../../../state/ui/graph/measurement/measurementActions';
import {changeToolbarView} from '../../../state/ui/toolbar/toolbarActions';
import {OnChangeToolbarView, ToolbarViewSettingsProps} from '../../../state/ui/toolbar/toolbarModels';
import {SelectionInterval} from '../../../state/user-selection/userSelectionModels';
import {Callback, CallbackWith, OnClick} from '../../../types/Types';
import {ReportToolbar} from '../../report/components/ReportToolbar';

interface StateToProps extends ToolbarViewSettingsProps, ResolutionAware {
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
  selectionReport: {savedReports, temporal: {resolution, timePeriod, shouldComparePeriod}},
  selectionMeasurement: {measurementResponse: {measurements, compare}, isFetching, isExportingToExcel},
  ui: {toolbar: {selectionReport: {view}}},
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
  changeToolbarView: changeToolbarView(ReportSector.selectionReport),
  exportToExcel: exportSelectionReportToExcel,
  selectResolution: selectResolution(ReportSector.selectionReport),
  setReportTimePeriod: setReportTimePeriod(ReportSector.selectionReport),
  toggleComparePeriod: toggleComparePeriod(ReportSector.selectionReport),
  toggleShowAverage: toggleShowAverage(ReportSector.selectionReport),
}, dispatch);

export const ToolbarContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(ReportToolbar);
