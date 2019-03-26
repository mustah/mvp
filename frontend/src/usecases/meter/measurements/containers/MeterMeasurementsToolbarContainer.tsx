import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../../reducers/rootReducer';
import {changeMeterMeasurementsToolbarView} from '../../../../state/ui/toolbar/toolbarActions';
import {OnChangeToolbarView, ToolbarView} from '../../../../state/ui/toolbar/toolbarModels';
import {SelectionInterval} from '../../../../state/user-selection/userSelectionModels';
import {Callback, CallbackWith} from '../../../../types/Types';
import {MeterMeasurementsToolbar} from '../components/MeterMeasurementsToolbar';
import {setMeterDetailsTimePeriod} from '../meterDetailActions';
import {exportToExcel} from '../meterDetailMeasurementActions';

interface StateToProps {
  hasMeasurements: boolean;
  view: ToolbarView;
  isExportingToExcel: boolean;
  isFetching: boolean;
  timePeriod: SelectionInterval;
}

interface DispatchToProps {
  changeToolbarView: OnChangeToolbarView;
  exportToExcel: Callback;
  setMeterDetailsTimePeriod: CallbackWith<SelectionInterval>;
}

interface OwnProps {
  useCollectionPeriod?: boolean;
}

export type Props = StateToProps & DispatchToProps;

const mapStateToProps = (
  {
    meterDetail: {isTimePeriodDefault, timePeriod},
    ui: {toolbar: {meterMeasurement: {view}}},
    domainModels: {meterDetailMeasurement: {isFetching, isExportingToExcel, measurementResponse: {measurements}}},
    collection
  }: RootState,
  {useCollectionPeriod}: OwnProps
): StateToProps => ({
  isExportingToExcel,
  isFetching,
  hasMeasurements: measurements.length > 0,
  timePeriod: (useCollectionPeriod && isTimePeriodDefault) ? collection.timePeriod : timePeriod,
  view,
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  changeToolbarView: changeMeterMeasurementsToolbarView,
  exportToExcel,
  setMeterDetailsTimePeriod,
}, dispatch);

export const MeterMeasurementsToolbarContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(MeterMeasurementsToolbar);
