import {createSelector} from 'reselect';
import {TemporalResolution} from '../../../components/dates/dateModels';
import {MeterDetails} from '../../../state/domain-models/meter-details/meterDetailsModels';
import {SelectionInterval} from '../../../state/user-selection/userSelectionModels';
import {toLegendItemAllQuantities} from '../../report/helpers/legendHelper';
import {MeterDetailState} from './MeterDetailModels';
import {OwnProps} from './containers/MeterMeasurementsContainer';
import {identity} from '../../../helpers/commonHelpers';
import {MeasurementParameters, MeasurementResponse} from '../../../state/ui/graph/measurement/measurementModels';

const getMeter = (props: OwnProps) => props.meter;
const getPeriod = (state: MeterDetailState) => state.timePeriod;

export const getMeasurementParameters =
  createSelector<MeterDetailState & OwnProps, MeterDetails | SelectionInterval, MeasurementParameters>(
    [getMeter, getPeriod],
    (meter: MeterDetails, timePeriod: SelectionInterval
    ) =>
      ({
        legendItems: [toLegendItemAllQuantities(meter)],
        resolution: TemporalResolution.day,
        selectionParameters: {
          dateRange: timePeriod
        },
        shouldComparePeriod: false,
      })
  );

export const hasMeasurementValues = createSelector<MeasurementResponse, MeasurementResponse, boolean>(
  identity,
  ({measurements}: MeasurementResponse) =>
    measurements.length > 0
);
