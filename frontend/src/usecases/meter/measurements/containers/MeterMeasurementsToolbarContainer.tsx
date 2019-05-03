import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {TemporalResolution} from '../../../../components/dates/dateModels';
import {RootState} from '../../../../reducers/rootReducer';
import {changeMeterMeasurementsToolbarView} from '../../../../state/ui/toolbar/toolbarActions';
import {OnChangeToolbarView, ToolbarViewSettings} from '../../../../state/ui/toolbar/toolbarModels';
import {SelectionInterval} from '../../../../state/user-selection/userSelectionModels';
import {Callback, CallbackWith} from '../../../../types/Types';
import {MeterMeasurementsToolbar} from '../components/MeterMeasurementsToolbar';
import {selectResolution, setTimePeriod} from '../meterDetailActions';
import {exportToExcel} from '../meterDetailMeasurementActions';
import {getResolution} from '../meterDetailMeasurementsSelectors';
import {OwnProps} from '../meterDetailModels';

interface StateToProps extends ToolbarViewSettings {
  hasMeasurements: boolean;
  isExportingToExcel: boolean;
  isFetching: boolean;
  resolution: TemporalResolution;
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
): StateToProps => ({
  hasMeasurements: measurements.length > 0,
  isExportingToExcel,
  isFetching,
  resolution: getResolution({meterDetail, meter}),
  timePeriod: (useCollectionPeriod && !meterDetail.isDirty) ? collection.timePeriod : meterDetail.timePeriod,
  view,
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  changeToolbarView: changeMeterMeasurementsToolbarView,
  exportToExcel,
  selectResolution,
  setTimePeriod,
}, dispatch);

export const MeterMeasurementsToolbarContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(MeterMeasurementsToolbar);
