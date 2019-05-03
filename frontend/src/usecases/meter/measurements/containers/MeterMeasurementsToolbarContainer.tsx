import {connect} from 'react-redux';
import {bindActionCreators} from 'redux';
import {TemporalResolution} from '../../../../components/dates/dateModels';
import {RootState} from '../../../../reducers/rootReducer';
import {MeterDetails} from '../../../../state/domain-models/meter-details/meterDetailsModels';
import {changeMeterMeasurementsToolbarView} from '../../../../state/ui/toolbar/toolbarActions';
import {OnChangeToolbarView, ToolbarView} from '../../../../state/ui/toolbar/toolbarModels';
import {SelectionInterval} from '../../../../state/user-selection/userSelectionModels';
import {Callback, CallbackWith} from '../../../../types/Types';
import {MeterMeasurementsToolbar} from '../components/MeterMeasurementsToolbar';
import {selectResolution, setTimePeriod} from '../meterDetailActions';
import {exportToExcel} from '../meterDetailMeasurementActions';
import {getResolution} from '../meterDetailMeasurementsSelectors';

interface StateToProps {
  hasMeasurements: boolean;
  isExportingToExcel: boolean;
  isFetching: boolean;
  resolution: TemporalResolution;
  timePeriod: SelectionInterval;
  view: ToolbarView;
}

interface DispatchToProps {
  changeToolbarView: OnChangeToolbarView;
  exportToExcel: Callback;
  selectResolution: CallbackWith<TemporalResolution>;
  setTimePeriod: CallbackWith<SelectionInterval>;
}

interface OwnProps {
  meter: MeterDetails;
  useCollectionPeriod: boolean;
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
