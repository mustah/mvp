import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../../reducers/rootReducer';
import {changeMeterMeasurementsToolbarView} from '../../../../state/ui/toolbar/toolbarActions';
import {OnChangeToolbarView, ToolbarView} from '../../../../state/ui/toolbar/toolbarModels';
import {SelectionInterval} from '../../../../state/user-selection/userSelectionModels';
import {Callback, CallbackWith} from '../../../../types/Types';
import {setMeterDetailsTimePeriod} from '../meterDetailActions';
import {exportToExcel} from '../meterDetailMeasurementActions';
import {MeasurementToolbar} from './MeasurementToolbar';

interface StateToProps {
  hasMeasurements: boolean;
  view: ToolbarView;
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

const mapStateToProps = ({
  meterDetail: {isTimePeriodDefault, timePeriod},
  ui: {toolbar: {meterMeasurement: {view}}},
  domainModels: {meterDetailMeasurement: {isFetching, measurementResponse: {measurements}}},
  collection
}: RootState,            {useCollectionPeriod}: OwnProps): StateToProps => ({
  hasMeasurements: measurements.length > 0,
  isFetching,
  view,
  timePeriod: (useCollectionPeriod && isTimePeriodDefault) ? collection.timePeriod : timePeriod,
});

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  changeToolbarView: changeMeterMeasurementsToolbarView,
  exportToExcel,
  setMeterDetailsTimePeriod,
}, dispatch);

export const MeasurementToolbarContainer =
  connect<StateToProps, DispatchToProps, OwnProps>(mapStateToProps, mapDispatchToProps)(MeasurementToolbar);
