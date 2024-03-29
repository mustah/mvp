import {LegendPayload, LineType} from 'recharts';
import {TemporalResolution} from '../../components/dates/dateModels';
import {Identifiable, uuid} from '../../types/Types';
import {Medium, Quantity} from '../ui/graph/measurement/measurementModels';
import {SelectionInterval} from '../user-selection/userSelectionModels';
import {LegendType} from './reportModels';

export type LegendType = Medium | 'aggregate';

export interface LegendTyped {
  type: LegendType;
}

export interface LegendItemSettings {
  isHidden?: boolean;
  isRowExpanded?: boolean;
}

export interface LegendDto extends Identifiable {
  facility: string;
  medium: string;
}

export interface LegendItem extends LegendTyped, LegendItemSettings {
  id: uuid;
  label: string;
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

export interface ResolutionAware {
  resolution: TemporalResolution;
}

export interface TemporalReportState extends ResolutionAware {
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

export interface QuantityLegendType extends LegendTyped {
  quantity: Quantity;
}

export interface QuantityId {
  id: uuid;
  quantity: Quantity;
}

export const enum ReportSector {
  report = 'report',
  selectionReport = 'selectionReport',
  meterDetailsReport = 'meterDetailsReport',
}

const isOfTypeMedium = (type: LegendType): type is Medium => Medium[type] !== undefined;

export const isMedium = ({type}: LegendTyped): boolean => isOfTypeMedium(type);
export const isKnownMedium = (type: LegendType): type is Medium => Medium[type] !== Medium.unknown;
export const isAggregate = (type: LegendType): type is 'aggregate' => !isOfTypeMedium(type);
