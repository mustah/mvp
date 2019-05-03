import {TemporalResolution} from '../../../components/dates/dateModels';
import {MeterDetails} from '../../../state/domain-models/meter-details/meterDetailsModels';
import {ToolbarViewSettings} from '../../../state/ui/toolbar/toolbarModels';
import {SelectionInterval} from '../../../state/user-selection/userSelectionModels';

export interface MeterDetailState {
  isDirty: boolean;
  resolution: TemporalResolution;
  timePeriod: SelectionInterval;
}

export interface OwnProps {
  meter: MeterDetails;
  useCollectionPeriod: boolean;
}

export type MeterDetailProps = ToolbarViewSettings & OwnProps;
