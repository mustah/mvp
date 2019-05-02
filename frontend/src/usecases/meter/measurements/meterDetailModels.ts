import {TemporalResolution} from '../../../components/dates/dateModels';
import {SelectionInterval} from '../../../state/user-selection/userSelectionModels';

export interface MeterDetailState {
  isDirty: boolean;
  resolution: TemporalResolution;
  timePeriod: SelectionInterval;
}
