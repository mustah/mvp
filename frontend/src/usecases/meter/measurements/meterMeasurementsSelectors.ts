import {createSelector} from 'reselect';
import {identity} from '../../../helpers/commonHelpers';
import {readIntervalToTemporal} from '../../../helpers/dateHelpers';
import {MeterDetails} from '../../../state/domain-models/meter-details/meterDetailsModels';
import {MeasurementParameters, MeasurementResponse} from '../../../state/ui/graph/measurement/measurementModels';
import {SelectionInterval} from '../../../state/user-selection/userSelectionModels';
import {Omit} from '../../../types/Types';
import {toLegendItemAllQuantities} from '../../report/helpers/legendHelper';
import {OwnProps} from './containers/MeterMeasurementsContainer';
import {MeterDetailState} from './meterDetailModels';

const getMeter = (props: Omit<OwnProps, 'useCollectionPeriod'>) => props.meter;
const getPeriod = (state: Omit<MeterDetailState, 'isTimePeriodDefault'>) => state.timePeriod;

export const getMeasurementParameters =
  createSelector<
    Omit<MeterDetailState, 'isTimePeriodDefault'> & Omit<OwnProps, 'useCollectionPeriod'>,
    MeterDetails, SelectionInterval,
    MeasurementParameters
    >(
    getMeter,
    getPeriod,
    (meter: MeterDetails, timePeriod: SelectionInterval) => ({
      legendItems: [toLegendItemAllQuantities(meter)],
      resolution: readIntervalToTemporal(meter.readIntervalMinutes),
      dateRange: timePeriod,
      shouldComparePeriod: false,
      shouldShowAverage: false,
    })
  );

export const hasMeasurementValues = createSelector<MeasurementResponse, MeasurementResponse, boolean>(
  identity,
  ({measurements}: MeasurementResponse) =>
    measurements.length > 0
);
