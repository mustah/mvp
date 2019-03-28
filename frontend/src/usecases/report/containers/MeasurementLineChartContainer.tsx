import {uuid} from '../../../types/Types';
import {LineChartComponent} from '../components/line-chart/LineChartComponent';
import {GraphContents} from '../../../state/report/reportModels';

export interface OwnProps {
  outerHiddenKeys: uuid[];
  graphContents: GraphContents;
  isSideMenuOpen: boolean;
  hasMeters: boolean;
  hasContent: boolean;
}

export const MeasurementLineChartContainer = (LineChartComponent);
