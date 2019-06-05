import {MeterDetails} from '../../../state/domain-models/meter-details/meterDetailsModels';
import {ResolutionAware} from '../../../state/report/reportModels';
import {ToolbarViewSettings} from '../../../state/ui/toolbar/toolbarModels';
import {SelectionInterval} from '../../../state/user-selection/userSelectionModels';
import {uuid} from '../../../types/Types';

export interface MeterDetailState extends ResolutionAware {
  isDirty: boolean;
  timePeriod: SelectionInterval;
  selectedMeterId?: uuid;
}

export interface OwnProps {
  meter: MeterDetails;
  useCollectionPeriod: boolean;
}

export type MeterDetailProps = ToolbarViewSettings & OwnProps;
