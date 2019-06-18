import {connect} from 'react-redux';
import {compose} from 'recompose';
import {bindActionCreators} from 'redux';
import {TemporalResolution} from '../../../../components/dates/dateModels';
import {ThemeContext, withCssStyles} from '../../../../components/hoc/withThemeProvider';
import {RootState} from '../../../../reducers/rootReducer';
import {ResolutionAware} from '../../../../state/report/reportModels';
import {changeMeterMeasurementsToolbarView} from '../../../../state/ui/toolbar/toolbarActions';
import {OnChangeToolbarView, ToolbarViewSettingsProps} from '../../../../state/ui/toolbar/toolbarModels';
import {SelectionInterval} from '../../../../state/user-selection/userSelectionModels';
import {Callback, CallbackWith} from '../../../../types/Types';
import {MeterMeasurementsToolbar} from '../components/MeterMeasurementsToolbar';
import {exportToExcel, selectResolution, setTimePeriod} from '../meterDetailMeasurementActions';
import {getMeterResolution} from '../meterDetailMeasurementsSelectors';
import {OwnProps} from '../meterDetailModels';

interface StateToProps extends ResolutionAware, ToolbarViewSettingsProps {
  hasMeasurements: boolean;
  isExportingToExcel: boolean;
  isFetching: boolean;
  timePeriod: SelectionInterval;
}

interface DispatchToProps {
  changeToolbarView: OnChangeToolbarView;
  exportToExcel: Callback;
  selectResolution: CallbackWith<TemporalResolution>;
  setTimePeriod: CallbackWith<SelectionInterval>;
}

export type Props = StateToProps & DispatchToProps;

const mapStateToProps = (
  {
    meterDetail,
    ui: {toolbar: {meterMeasurement: {view}}},
    domainModels: {meterDetailMeasurement: {isFetching, isExportingToExcel, measurementResponse: {measurements}}},
    collection
  }: RootState,
  {meter, useCollectionPeriod}: OwnProps
): StateToProps => {
  const timePeriod = (useCollectionPeriod && !meterDetail.isDirty) ? collection.timePeriod : meterDetail.timePeriod;
  return ({
    hasMeasurements: measurements.length > 0,
    isExportingToExcel,
    isFetching,
    resolution: getMeterResolution({meterDetail, meter, period: timePeriod.period}),
    timePeriod,
    view,
  });
};

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  changeToolbarView: changeMeterMeasurementsToolbarView,
  exportToExcel,
  selectResolution,
  setTimePeriod,
}, dispatch);

const ThemedMeterMeasurementsToolbar =
  compose<Props & OwnProps & ThemeContext, Props & OwnProps>(withCssStyles)(MeterMeasurementsToolbar);

export const MeterMeasurementsToolbarContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(ThemedMeterMeasurementsToolbar);
