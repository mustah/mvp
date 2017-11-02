import {Indicator} from '../../common/components/indicators/models/IndicatorModels';
import {uuid} from '../../../types/Types';

export interface SystemOverviewState {
  indicators: Indicator[];
}

export interface DashboardModel {
  id: uuid;
  systemOverview: SystemOverviewState;
}
