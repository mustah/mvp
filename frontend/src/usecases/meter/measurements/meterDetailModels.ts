import {SelectionInterval} from '../../../state/user-selection/userSelectionModels';

export interface MeterDetailState {
  isTimePeriodDefault: boolean;
  timePeriod: SelectionInterval;
}
