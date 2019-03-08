import {LegendPayload, LineType} from 'recharts';
import {TemporalResolution} from '../../components/dates/dateModels';
import {Medium, Quantity} from '../../state/ui/graph/measurement/measurementModels';
import {SelectionInterval} from '../../state/user-selection/userSelectionModels';
import {Identifiable, uuid} from '../../types/Types';
import {LegendType} from './reportModels';

export type LegendType = Medium | 'aggregate';

export const isMedium = (type: LegendType): type is Medium => Medium[type] !== undefined;
export const isAggregate = (type: LegendType): type is 'aggregate' => !isMedium(type);

export interface LegendItemSettings {
  isHidden?: boolean;
  isRowExpanded?: boolean;
}

export interface LegendItem extends LegendItemSettings {
  id: uuid;
  label: string;
  type: LegendType;
  quantities: Quantity[];
}

export interface ViewOptions {
  isAllLinesHidden?: boolean;
  quantities: Quantity[];
}

export type LegendViewOptions = { [p in LegendType]: ViewOptions };

export type SelectedQuantities = { [p in LegendType]: Quantity[] };

export interface Report extends Identifiable {
  legendItems: LegendItem[];
  legendViewOptions: LegendViewOptions;
  shouldShowAverage: boolean;
}

export interface SavedReportsState {
  meterPage: Report;
}

export interface TemporalReportState {
  resolution: TemporalResolution;
  shouldComparePeriod: boolean;
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

export interface LineProps {
  id: string;
  city?: string;
  address?: string;
  dataKey: string;
  key: string;
  medium?: string;
  name: string;
  strokeDasharray?: string;
  stroke: string;
  strokeWidth: number;
  type?: LineType;
  unit: string;
  yAxisId: string;
}

export interface GraphContents {
  axes: AxesProps;
  data: object[];
  legend: LegendPayload[];
  lines: LineProps[];
}

export interface QuantityLegendType {
  type: LegendType;
  quantity: Quantity;
}

export interface QuantityId {
  id: uuid;
  quantity: Quantity;
}
