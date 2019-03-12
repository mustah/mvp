import {SelectionInterval} from '../../state/user-selection/userSelectionModels';

export interface CollectionState {
  isTimePeriodDefault: boolean;
  timePeriod: SelectionInterval;
}
