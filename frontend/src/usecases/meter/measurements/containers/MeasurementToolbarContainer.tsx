import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {RootState} from '../../../../reducers/rootReducer';
import {changeCollectionToolbarView} from '../../../../state/ui/toolbar/toolbarActions';
import {OnChangeToolbarView, ToolbarView} from '../../../../state/ui/toolbar/toolbarModels';
import {SelectionInterval} from '../../../../state/user-selection/userSelectionModels';
import {Callback, CallbackWith} from '../../../../types/Types';
import {MeasurementToolbar} from './MeasurementToolbar';
import {exportToExcel, setMeterDetailsTimePeriod} from '../meterDetailActions';

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

export type Props = StateToProps & DispatchToProps;

const mapStateToProps = ({
  meterDetail: {timePeriod},
  ui: {toolbar: {collection: {view}}},
  domainModels: { meterDetailMeasurement: {isFetching, measurementResponse: {measurements}}},
}: RootState): StateToProps =>
  ({
    hasMeasurements: measurements.length > 0,
    isFetching,
    view,
    timePeriod,
  });

const mapDispatchToProps = (dispatch): DispatchToProps => bindActionCreators({
  changeToolbarView: changeCollectionToolbarView,
  exportToExcel,
  setMeterDetailsTimePeriod,
}, dispatch);

export const MeasurementToolbarContainer =
  connect<StateToProps, DispatchToProps>(mapStateToProps, mapDispatchToProps)(MeasurementToolbar);
