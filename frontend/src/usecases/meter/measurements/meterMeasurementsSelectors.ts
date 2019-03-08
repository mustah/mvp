import {createSelector} from 'reselect';
import {TemporalResolution} from '../../../components/dates/dateModels';
import {identity} from '../../../helpers/commonHelpers';
import {MeterDetails} from '../../../state/domain-models/meter-details/meterDetailsModels';
import {MeasurementParameters, MeasurementResponse} from '../../../state/ui/graph/measurement/measurementModels';
import {SelectionInterval} from '../../../state/user-selection/userSelectionModels';
import {toLegendItemAllQuantities} from '../../report/helpers/legendHelper';
import {OwnProps} from './containers/MeterMeasurementsContainer';
import {MeterDetailState} from './MeterDetailModels';

const getMeter = (props: OwnProps) => props.meter;
const getPeriod = (state: MeterDetailState) => state.timePeriod;

export const getMeasurementParameters =
  createSelector<MeterDetailState & OwnProps, MeterDetails, SelectionInterval, MeasurementParameters>(
    getMeter,
    getPeriod,
    (meter: MeterDetails, timePeriod: SelectionInterval) => ({
      legendItems: [toLegendItemAllQuantities(meter)],
      resolution: TemporalResolution.day,
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
