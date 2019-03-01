import {LegendPayload} from 'recharts';
import {TemporalResolution} from '../../components/dates/dateModels';
import {ObjectsById} from '../../state/domain-models/domainModels';
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

export type SavedReportsState = ObjectsById<Report>;

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

export interface Axes {
  left?: string;
  right?: string;
}

export interface LineProps {
  id: string;
  dataKey: string;
  key: string;
  name: string;
  city?: string;
  address?: string;
  medium?: string;
  stroke: string;
  strokeWidth?: number;
  yAxisId: string;
}

export interface GraphContents {
  axes: Axes;
  data: object[];
  legend: LegendPayload[];
  lines: LineProps[];
}

export interface ActiveDataPoint {
  color: any;
  dataKey: uuid;
  fill: any;
  name: uuid;
  payload: {name: number; [key: string]: number};
  stroke: any;
  strokeWidth: number;
  unit: string;
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
