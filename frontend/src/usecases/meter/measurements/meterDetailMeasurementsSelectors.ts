import {createSelector} from 'reselect';
import {TemporalResolution} from '../../../components/dates/dateModels';
import {identity} from '../../../helpers/commonHelpers';
import {readIntervalToTemporal} from '../../../helpers/dateHelpers';
import {MeterDetails} from '../../../state/domain-models/meter-details/meterDetailsModels';
import {MeasurementParameters, MeasurementResponse} from '../../../state/ui/graph/measurement/measurementModels';
import {SelectionInterval} from '../../../state/user-selection/userSelectionModels';
import {toLegendItemAllQuantities} from '../../report/helpers/legendHelper';
import {MeterDetailState} from './meterDetailModels';

interface ResolutionState {
  meterDetail: MeterDetailState;
  meter: MeterDetails;
}

export const getResolution = createSelector<ResolutionState, ResolutionState, TemporalResolution>(
  identity,
  ({meter: {readIntervalMinutes}, meterDetail: {isDirty, resolution}}) =>
    isDirty ? resolution : readIntervalToTemporal(readIntervalMinutes)
);

interface State {
  meter: MeterDetails;
  resolution: TemporalResolution;
  timePeriod: SelectionInterval;
}

export const getMeasurementParameters =
  createSelector<State, State, MeasurementParameters>(
    identity,
    ({meter, resolution, timePeriod}) => ({
      legendItems: [toLegendItemAllQuantities(meter)],
      resolution,
      dateRange: timePeriod,
      shouldComparePeriod: false,
      shouldShowAverage: false,
    })
  );

export const hasMeasurementValues = createSelector<MeasurementResponse, MeasurementResponse, boolean>(
  identity,
  ({measurements}) => measurements.length > 0
);
