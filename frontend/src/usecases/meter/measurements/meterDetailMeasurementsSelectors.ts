import {createSelector} from 'reselect';
import {identity} from '../../../helpers/commonHelpers';
import {readIntervalToTemporal} from '../../../helpers/dateHelpers';
import {MeterDetails} from '../../../state/domain-models/meter-details/meterDetailsModels';
import {MeasurementParameters, MeasurementResponse} from '../../../state/ui/graph/measurement/measurementModels';
import {SelectionInterval} from '../../../state/user-selection/userSelectionModels';
import {toLegendItemAllQuantities} from '../../report/helpers/legendHelper';

interface State {
  meter: MeterDetails;
  timePeriod: SelectionInterval;
}

export const getMeasurementParameters =
  createSelector<State, State, MeasurementParameters>(
    identity,
    ({meter, timePeriod}) => ({
      legendItems: [toLegendItemAllQuantities(meter)],
      resolution: readIntervalToTemporal(meter.readIntervalMinutes),
      dateRange: timePeriod,
      shouldComparePeriod: false,
      shouldShowAverage: false,
    })
  );

export const hasMeasurementValues = createSelector<MeasurementResponse, MeasurementResponse, boolean>(
  identity,
  ({measurements}) => measurements.length > 0
);
