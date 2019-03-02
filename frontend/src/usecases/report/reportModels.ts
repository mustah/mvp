import {LegendPayload, LineType} from 'recharts';
import {TemporalResolution} from '../../components/dates/dateModels';
import {Medium, Quantity} from '../../state/ui/graph/measurement/measurementModels';
import {SelectionInterval} from '../../state/user-selection/userSelectionModels';
import {Identifiable, uuid} from '../../types/Types';
import {LegendType} from './reportModels';

export type LegendType = Medium | 'aggregate';

export const isMedium = (type: LegendType): type is Medium => Medium[type] !== undefined;
export const isAggregate = (type: LegendType): type is 'aggregate' => !isMedium(type);

export interface LegendItem {
  id: uuid;
  label: string;
  type: LegendType;
  isHidden: boolean;
  isRowExpanded?: boolean;
  quantities: Quantity[];
}

export interface ViewOptions {
  isAllLinesHidden?: boolean;
  quantities: Quantity[];
}

export type LegendViewOptions = { [p in LegendType]: ViewOptions };

export type SelectedQuantityColumns = { [p in LegendType]: Quantity[] };

export interface Report extends Identifiable {
  legendItems: LegendItem[];
  legendViewOptions: LegendViewOptions;
}

export interface SavedReportsState {
  meterPage: Report;
}

export interface TemporalReportState {
  resolution: TemporalResolution;
  timePeriod: SelectionInterval;
}

export interface ReportState {
  savedReports: SavedReportsState;
  temporal: TemporalReportState;
}

export interface ColumnQuantities {
  columnQuantities: Quantity[];
}

export interface AxesProps {
  left?: string;
  right?: string;
}

interface TooltipLineProps {
  dataKey: string;
  name: string;
  stroke: string;
  strokeWidth: number;
  unit: string;
}

export interface LineProps extends TooltipLineProps {
  id: string;
  key: string;
  city?: string;
  address?: string;
  medium?: string;
  strokeDasharray?: string;
  type?: LineType;
  yAxisId: string;
}

export interface GraphContents {
  axes: AxesProps;
  data: object[];
  legend: LegendPayload[];
  lines: LineProps[];
}

export interface ActivePointPayload {
  timestamp: number;
  name: number;

  [key: string]: number;
}

export interface ActiveDataPoint extends TooltipLineProps {
  color: any;
  fill: any;
  payload: ActivePointPayload;
  value: number;
}

export interface QuantityLegendType {
  type: LegendType;
  quantity: Quantity;
}

export interface QuantityId {
  id: uuid;
  quantity: Quantity;
}
