import {createSelector} from 'reselect';
import {defaultPeriodResolution, Period, TemporalResolution} from '../../../components/dates/dateModels';
import {identity} from '../../../helpers/commonHelpers';
import {readIntervalToTemporal} from '../../../helpers/dateHelpers';
import {MeterDetails} from '../../../state/domain-models/meter-details/meterDetailsModels';
import {ResolutionAware} from '../../../state/report/reportModels';
import {MeasurementParameters, MeasurementResponse} from '../../../state/ui/graph/measurement/measurementModels';
import {SelectionInterval} from '../../../state/user-selection/userSelectionModels';
import {toLegendItemAllSearchableQuantities} from '../../report/helpers/legendHelper';
import {MeterDetailState} from './meterDetailModels';

interface ResolutionState {
  meter: MeterDetails;
  meterDetail: MeterDetailState;
  period: Period;
}

export const getMeterResolution = createSelector<ResolutionState, ResolutionState, TemporalResolution>(
  identity,
  ({meter: {readIntervalMinutes}, meterDetail: {isDirty, resolution}, period}) =>
    isDirty ? resolution : readIntervalToTemporal(readIntervalMinutes, defaultPeriodResolution[period])
);

interface State extends ResolutionAware {
  meter: MeterDetails;
  timePeriod: SelectionInterval;
}

export const getMeasurementParameters =
  createSelector<State, State, MeasurementParameters>(
    identity,
    ({meter, resolution, timePeriod}) => ({
      legendItems: [toLegendItemAllSearchableQuantities(meter)],
      resolution,
      reportDateRange: timePeriod,
      shouldComparePeriod: false,
      shouldShowAverage: false,
    })
  );

export const hasMeasurementValues = createSelector<MeasurementResponse, MeasurementResponse, boolean>(
  identity,
  ({measurements}) => measurements.length > 0
);
